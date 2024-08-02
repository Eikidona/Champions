package top.theillusivec4.champions.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.common.util.FakePlayer;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.capability.ChampionCapability;
import top.theillusivec4.champions.common.config.ChampionsConfig;
import top.theillusivec4.champions.common.config.ConfigEnums;
import top.theillusivec4.champions.common.config.ConfigLoot;
import top.theillusivec4.champions.common.rank.Rank;
import top.theillusivec4.champions.common.registry.RegistryReference;

import javax.annotation.Nonnull;
import java.util.List;

public class ChampionLootModifier extends LootModifier {
  public static final Codec<ChampionLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, ChampionLootModifier::new));
  private static final ThreadLocal<Boolean> IS_PROCESSING = ThreadLocal.withInitial(() -> false);

  public ChampionLootModifier(LootItemCondition[] conditions) {
    super(conditions);
  }

  @Nonnull
  @Override
  public ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
    Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);

    if (entity == null) {
      return generatedLoot;
    }
    DamageSource damageSource = context.getParamOrNull(LootContextParams.DAMAGE_SOURCE);

    if (damageSource == null) {
      return generatedLoot;
    }

    if (!entity.getLevel().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT) ||
      (!ChampionsConfig.fakeLoot && damageSource.getDirectEntity() instanceof FakePlayer)) {
      return generatedLoot;
    }
    ChampionCapability.getCapability(entity).ifPresent(champion -> {
      IChampion.Server serverChampion = champion.getServer();
      ServerLevel serverWorld = (ServerLevel) entity.getLevel();

      if (ChampionsConfig.lootSource != ConfigEnums.LootSource.CONFIG) {
        LootTable lootTable = serverWorld.getServer().getLootTables()
          .get(new ResourceLocation(RegistryReference.CHAMPION_LOOT));
        LootContext.Builder lootcontext$builder = (new LootContext.Builder(serverWorld)
          .withRandom(entity.level.getRandom())
          .withParameter(LootContextParams.THIS_ENTITY, entity)
          .withParameter(LootContextParams.ORIGIN, entity.position())
          .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
          .withOptionalParameter(LootContextParams.KILLER_ENTITY, damageSource.getEntity())
          .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY,
            damageSource.getDirectEntity()));

        if (entity instanceof LivingEntity livingEntity) {
          LivingEntity attackingEntity = livingEntity.getKillCredit();

          if (attackingEntity instanceof Player) {
            lootcontext$builder = lootcontext$builder
              .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, (Player) attackingEntity)
              .withLuck(((Player) attackingEntity).getLuck());
          }
        }
        lootTable.getRandomItemsRaw(lootcontext$builder.create(LootContextParamSets.ENTITY),
          generatedLoot::add);
      }

      if (ChampionsConfig.lootSource != ConfigEnums.LootSource.LOOT_TABLE) {
        List<ItemStack> loot = ConfigLoot
          .getLootDrops(serverChampion.getRank().map(Rank::getTier).orElse(0));

        if (!loot.isEmpty()) {
          generatedLoot.addAll(loot);
        }
      }
    });
    return generatedLoot;
  }

  @Override
  public Codec<? extends IGlobalLootModifier> codec() {
    return CODEC;
  }

}
