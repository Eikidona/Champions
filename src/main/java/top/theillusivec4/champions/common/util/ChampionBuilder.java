package top.theillusivec4.champions.common.util;

import com.google.common.collect.ImmutableSortedMap;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.api.AffixCategory;
import top.theillusivec4.champions.api.IAffix;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.config.ChampionsConfig;
import top.theillusivec4.champions.common.rank.Rank;
import top.theillusivec4.champions.common.rank.RankManager;
import top.theillusivec4.champions.common.util.EntityManager.EntitySettings;

import java.util.*;

public class ChampionBuilder {
  private static final RandomSource RAND = RandomSource.createNewThreadLocalInstance();
  private static final ResourceLocation MAX_HEALTH_MODIFIER = Champions.getLocation("max_health_modifier");
  private static final ResourceLocation ATTACK_DAMAGE_MODIFIER = Champions.getLocation("attack_damage_modifier");
  private static final ResourceLocation ARMOR_MODIFIER = Champions.getLocation("armor_modifier");
  private static final ResourceLocation ARMOR_TOUGHNESS_MODIFIER = Champions.getLocation("armor_toughness_modifier");
  private static final ResourceLocation KNOCKBACK_RESISTANCE_MODIFIER = Champions.getLocation("knock_back_resistance_modifier");

  public static void spawn(final IChampion champion) {

    if (ChampionData.read(champion)) {
      return;
    }
    LivingEntity entity = champion.getLivingEntity();
    Rank newRank = ChampionBuilder.createRank(entity);
    champion.getServer().setRank(newRank);
    ChampionBuilder.applyGrowth(entity, newRank.getGrowthFactor());
    List<IAffix> newAffixes = ChampionBuilder.createAffixes(newRank, champion);
    champion.getServer().setAffixes(newAffixes);
    newAffixes.forEach(affix -> affix.onInitialSpawn(champion));
  }

  public static void spawnPreset(final IChampion champion, int tier, List<IAffix> affixes) {
    LivingEntity entity = champion.getLivingEntity();
    Rank newRank = RankManager.getRank(tier);
    champion.getServer().setRank(newRank);
    ChampionBuilder.applyGrowth(entity, newRank.getGrowthFactor());
    affixes = affixes.isEmpty() ? ChampionBuilder.createAffixes(newRank, champion) : affixes;
    champion.getServer().setAffixes(affixes);
    affixes.forEach(affix -> affix.onInitialSpawn(champion));
  }

  public static List<IAffix> createAffixes(final Rank rank, final IChampion champion) {
    int size = rank.getNumAffixes();
    List<IAffix> affixesToAdd = new ArrayList<>();
    Optional<EntitySettings> entitySettings = EntityManager
      .getSettings(champion.getLivingEntity().getType());

    if (size > 0) {
      entitySettings.ifPresent(settings -> {

        if (settings.presetAffixes != null) {
          affixesToAdd.addAll(settings.presetAffixes);
        }
      });
      rank.getPresetAffixes().forEach(affix -> {

        if (!affixesToAdd.contains(affix)) {
          affixesToAdd.add(affix);
        }
      });
    }
    Map<AffixCategory, List<IAffix>> allAffixes = Champions.API.getCategoryMap();
    Map<AffixCategory, List<IAffix>> validAffixes = new HashMap<>();

    for (AffixCategory category : Champions.API.getCategories()) {
      validAffixes.put(category, new ArrayList<>());
    }
    allAffixes.forEach((k, v) -> validAffixes.get(k).addAll(v.stream().filter(affix -> {
      /*
        return new affix list that can apply with entity and affix settings, and affix can apply to champion.
       */
      return !affixesToAdd.contains(affix) && entitySettings
        .map(entitySettings1 -> entitySettings1.canApply(affix)).orElse(true) && affix
        .canApply(champion);
    }).toList()));
    addAffixToList(size, affixesToAdd, validAffixes, RAND);
    return affixesToAdd;
  }

  public static Rank createRank(final LivingEntity livingEntity) {

    if (ChampionHelper.notPotential(livingEntity)) {
      return RankManager.getEmptyRank();
    }
    ImmutableSortedMap<Integer, Rank> ranks = RankManager.getRanks();

    if (ranks.isEmpty()) {
      Champions.LOGGER.error(
        "No rank configuration found! Please check the 'champions-ranks.toml' file in the 'serverconfigs'.");
      return RankManager.getEmptyRank();
    }
    Integer[] tierRange = new Integer[]{null, null};

    EntityManager.getSettings(livingEntity.getType()).ifPresent(entitySettings -> {
      tierRange[0] = entitySettings.minTier;
      tierRange[1] = entitySettings.maxTier;
    });

    Integer firstTier = tierRange[0] != null ? tierRange[0] : ranks.firstKey();
    int maxTier = tierRange[1] != null ? tierRange[1] : -1;

    ImmutableSortedMap<Integer, Rank> filteredRanks;

    if (maxTier == -1) {
      /* 如果 maxTier 未设置，则仅过滤 firstTier 以上的 Rank */
      filteredRanks = ranks.tailMap(firstTier, true);
    } else {
      /* 如果 maxTier 设置了，过滤 firstTier 和 maxTier 范围内的 Rank */
      filteredRanks = ranks.tailMap(firstTier, true).headMap(maxTier + 1, true);
    }

    // 如果没有符合条件的 Rank，返回 EmptyRank
    if (filteredRanks.isEmpty()) {
      Champions.LOGGER.warn(
        "No valid ranks found in the specified range! Assigning EmptyRank to {}", livingEntity);
      return RankManager.getEmptyRank();
    }
    int totalWeight = filteredRanks.values().stream()
      .mapToInt(Rank::getWeight)
      .sum();

    // 如果所有权重为 0，返回 EmptyRank
    if (totalWeight <= 0) {
      Champions.LOGGER.warn(
        "All ranks have zero weight! Assigning EmptyRank to {}", livingEntity);
      return RankManager.getEmptyRank();
    }
    int randomValue = RAND.nextInt(totalWeight);
    int cumulativeWeight = 0;

    for (Rank rank : filteredRanks.values()) {
      cumulativeWeight += rank.getWeight();
      if (randomValue < cumulativeWeight) {
        return rank;
      }
    }

    return RankManager.getEmptyRank();
  }

  public static void applyGrowth(final LivingEntity livingEntity, float growthFactor) {

    if (growthFactor < 1) {
      return;
    }
    applyAttributeModifier(livingEntity, Holder.direct(Attributes.MAX_HEALTH), MAX_HEALTH_MODIFIER, ChampionsConfig.healthGrowth * growthFactor, AttributeModifier.Operation.MULTIPLY_TOTAL);
    applyAttributeModifier(livingEntity, Holder.direct(Attributes.ATTACK_DAMAGE), ATTACK_DAMAGE_MODIFIER, ChampionsConfig.attackGrowth * growthFactor, AttributeModifier.Operation.MULTIPLY_TOTAL);
    applyAttributeModifier(livingEntity, Holder.direct(Attributes.ARMOR), ARMOR_MODIFIER, ChampionsConfig.armorGrowth * growthFactor, AttributeModifier.Operation.ADDITION);
    applyAttributeModifier(livingEntity, Holder.direct(Attributes.ARMOR_TOUGHNESS), ARMOR_TOUGHNESS_MODIFIER, ChampionsConfig.toughnessGrowth * growthFactor, AttributeModifier.Operation.ADDITION);
    applyAttributeModifier(livingEntity, Holder.direct(Attributes.KNOCKBACK_RESISTANCE), KNOCKBACK_RESISTANCE_MODIFIER, ChampionsConfig.knockbackResistanceGrowth * growthFactor, AttributeModifier.Operation.ADDITION);
  }

  public static void applyAttributeModifier(LivingEntity livingEntity, Holder<Attribute> attribute, ResourceLocation modifierId, double amount, AttributeModifier.Operation operation) {
    var attributeInstance = livingEntity.getAttributes().getInstance(attribute);
    var attributeModifier = new AttributeModifier(modifierId.toString(), amount, operation);
    if (attributeInstance != null && !attributeInstance.hasModifier(attributeModifier)) {
      attributeInstance.addPermanentModifier(new AttributeModifier(modifierId.toString(), amount, operation));
      if (attributeInstance.getAttribute() == Attributes.MAX_HEALTH) {
        livingEntity.setHealth(livingEntity.getMaxHealth());
      }
    }
  }

  public static void copy(IChampion oldChampion, IChampion newChampion) {
    IChampion.Server oldServer = oldChampion.getServer();
    IChampion.Server newServer = newChampion.getServer();
    Rank rank = oldServer.getRank().orElse(RankManager.getLowestRank());
    newServer.setRank(rank);
    ChampionBuilder.applyGrowth(newChampion.getLivingEntity(), rank.getGrowthFactor());
    List<IAffix> oldAffixes = oldChampion.getServer().getAffixes();
    newServer.setAffixes(oldAffixes);
    oldAffixes.forEach(affix -> affix.onInitialSpawn(newChampion));
  }

  /**
   * Add random affix from random affixes list
   *
   * @param size         How much random affix to add
   * @param toModifier   Affix list that will add random affix to it.
   * @param validAffixes Affix list that can apply with entity and affix settings, and can apply to champion.
   * @param rand         mojang random source used get affix from random list.
   */
  public static void addAffixToList(int size, List<IAffix> toModifier, Map<AffixCategory, List<IAffix>> validAffixes, RandomSource rand) {
    List<IAffix> randomList = new ArrayList<>();
    validAffixes.forEach((k, v) -> randomList.addAll(v));

    while (!randomList.isEmpty() && toModifier.size() < size) {
      int randomIndex = rand.nextInt(randomList.size());
      IAffix randomAffix = randomList.get(randomIndex);

      if (toModifier.stream().allMatch(affix -> affix.isCompatible(randomAffix) && (
        randomAffix.getCategory() == AffixCategory.OFFENSE || (affix.getCategory() != randomAffix
          .getCategory())))) {
        toModifier.add(randomAffix);
      }
      randomList.remove(randomIndex);
    }
  }
}
