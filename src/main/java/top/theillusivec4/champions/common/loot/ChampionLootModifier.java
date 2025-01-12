package top.theillusivec4.champions.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
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
import top.theillusivec4.champions.common.registry.ModLootModifiers;

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
        if (IS_PROCESSING.get()) {
            return generatedLoot;
        }

        IS_PROCESSING.set(true);
        try {
            Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);

            if (entity == null) {
                return generatedLoot;
            }
            DamageSource damageSource = context.getParamOrNull(LootContextParams.DAMAGE_SOURCE);

            if (damageSource == null) {
                return generatedLoot;
            }

            if (!entity.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT) ||
                    (!ChampionsConfig.fakeLoot && damageSource.getDirectEntity() instanceof FakePlayer)) {
                return generatedLoot;
            }

            ChampionCapability.getCapability(entity).ifPresent(champion -> {
                IChampion.Server serverChampion = champion.getServer();
                ServerLevel serverWorld = (ServerLevel) entity.level();

                if (ChampionsConfig.lootSource != ConfigEnums.LootSource.CONFIG) {
                    LootTable lootTable = serverWorld.getServer().getLootData()
                            .getLootTable(ModLootModifiers.CHAMPION_LOOT.getId());
                    LootParams.Builder lootParamsBuilder = new LootParams.Builder(serverWorld)
                            .withParameter(LootContextParams.THIS_ENTITY, entity)
                            .withParameter(LootContextParams.ORIGIN, entity.position())
                            .withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
                            .withOptionalParameter(LootContextParams.KILLER_ENTITY, damageSource.getEntity())
                            .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, damageSource.getDirectEntity())
                            .withLuck(context.getLuck());

                    if (entity instanceof LivingEntity livingEntity) {
                        LivingEntity attackingEntity = livingEntity.getKillCredit();

                        if (attackingEntity instanceof Player) {
                            lootParamsBuilder = lootParamsBuilder
                                    .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, (Player) attackingEntity)
                                    .withLuck(((Player) attackingEntity).getLuck());
                        }
                    }

                    // 使用新的 LootParams 而不是原始的 LootContext
                    LootParams lootParams = lootParamsBuilder.create(LootContextParamSets.ENTITY);
                    lootTable.getRandomItems(lootParams, generatedLoot::add);
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
        } finally {
            IS_PROCESSING.set(false);
        }
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

}
