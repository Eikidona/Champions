package top.theillusivec4.champions.common.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import top.theillusivec4.champions.api.IAffix;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.capability.ChampionAttachment;
import top.theillusivec4.champions.common.rank.Rank;
import top.theillusivec4.champions.common.registry.ChampionsRegistry;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public record ChampionPropertyCondition(LootContext.EntityTarget target,
                                        Optional<MinMaxBounds.Ints> tier, Optional<AffixesPredicate> affixes)
  implements LootItemCondition {

  public static final Codec<ChampionPropertyCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(ChampionPropertyCondition::target),
    MinMaxBounds.Ints.CODEC.optionalFieldOf("tier").forGetter(ChampionPropertyCondition::tier),
    AffixesPredicate.CODEC.optionalFieldOf("affixes").forGetter(ChampionPropertyCondition::affixes)
  ).apply(instance, ChampionPropertyCondition::new));

  @Nonnull
  @Override
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return ImmutableSet.of(this.target.getParam());
  }

  @Override
  public boolean test(LootContext context) {
    Entity entity = context.getParamOrNull(this.target.getParam());
    return ChampionAttachment.getAttachment(entity).map(champion -> {
      IChampion.Server server = champion.getServer();
      int tier = server.getRank().map(Rank::getTier).orElse(0);

      if (tier <= 0 || !this.tier.map(t -> t.matches(tier)).orElse(true)) {
        return false;
      }
      List<IAffix> affixes = server.getAffixes();
      return this.affixes.map(affixesPredicate -> affixesPredicate.matches(affixes)).orElse(true);
    }).orElse(true);
  }

  @Nonnull
  @Override
  public LootItemConditionType getType() {
    return ChampionsRegistry.CHAMPION_PROPERTIES.get();
  }

  private record AffixesPredicate(Set<String> values, MinMaxBounds.Ints matches,
                                  MinMaxBounds.Ints count) {

    public static final Codec<AffixesPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      NeoForgeExtraCodecs.setOf(Codec.STRING).fieldOf("values").forGetter(AffixesPredicate::values),
      MinMaxBounds.Ints.CODEC.fieldOf("matches").forGetter(AffixesPredicate::matches),
      MinMaxBounds.Ints.CODEC.fieldOf("count").forGetter(AffixesPredicate::count)
    ).apply(instance, AffixesPredicate::new));

    private boolean matches(List<IAffix> input) {

      if (this.values.isEmpty()) {
        return this.count.matches(input.size());
      } else {
        Set<String> affixes = input.stream().map(IAffix::getIdentifier).collect(Collectors.toSet());
        int found = 0;

        for (String affix : this.values) {

          if (affixes.contains(affix)) {
            found++;
          }
        }
        return this.matches.matches(found) && this.count.matches(input.size());
      }
    }
  }
}
