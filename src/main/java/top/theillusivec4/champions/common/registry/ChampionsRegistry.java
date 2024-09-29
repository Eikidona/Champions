package top.theillusivec4.champions.common.registry;

import com.mojang.serialization.MapCodec;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.common.capability.ChampionAttachment;
import top.theillusivec4.champions.common.entity.ArcticBulletEntity;
import top.theillusivec4.champions.common.entity.EnkindlingBulletEntity;
import top.theillusivec4.champions.common.item.ChampionEggItem;
import top.theillusivec4.champions.common.loot.ChampionLootModifier;
import top.theillusivec4.champions.common.loot.ChampionPropertyCondition;
import top.theillusivec4.champions.common.loot.EntityIsChampion;
import top.theillusivec4.champions.common.potion.ParalysisEffect;
import top.theillusivec4.champions.common.potion.WoundEffect;
import top.theillusivec4.champions.server.command.AffixArgumentInfo;
import top.theillusivec4.champions.server.command.AffixArgumentType;

import java.util.HashMap;
import java.util.Map;


public class ChampionsRegistry {

  public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
    DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Champions.MODID);
  private static final DeferredRegister<Item> EGG = DeferredRegister.create(BuiltInRegistries.ITEM, Champions.MODID);
  private static final DeferredRegister<DataComponentType<?>> COMPONENTS  = DeferredRegister.createDataComponents(Champions.MODID);

  // RANK
  private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPE = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, Champions.MODID);
  // PARALYSIS
  private static final DeferredRegister<MobEffect> MOB_EFFECT = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, Champions.MODID);
  private static final DeferredRegister<EntityType<?>> ENTITY_TYPE = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Champions.MODID);
  private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, Champions.MODID);
  private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Champions.MODID);
  private static final DeferredRegister<LootItemConditionType> LOOT_ITEM_CONDITION_TYPE = DeferredRegister.create(BuiltInRegistries.LOOT_CONDITION_TYPE, Champions.MODID);
  private static final DeferredRegister<ResourceLocation> CHAMPIONS_STATS = DeferredRegister.create(BuiltInRegistries.CUSTOM_STAT, Champions.MODID);
  private static final Map<ResourceLocation, StatFormatter> CUSTOM_STAT_FORMATTERS = new HashMap<>();
  public static DeferredHolder<AttachmentType<?>, AttachmentType<ChampionAttachment.Provider>> CHAMPION_ATTACHMENT;
  public static DeferredHolder<ResourceLocation, ResourceLocation> CHAMPION_MOBS_KILLED;
  public static DeferredHolder<LootItemConditionType, LootItemConditionType> ENTITY_IS_CHAMPION;
  public static DeferredHolder<LootItemConditionType, LootItemConditionType> CHAMPION_PROPERTIES;
  public static DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ChampionLootModifier>> CHAMPION_LOOT;
  public static DeferredHolder<EntityType<?>, EntityType<EnkindlingBulletEntity>> ENKINDLING_BULLET;
  public static DeferredHolder<EntityType<?>, EntityType<ArcticBulletEntity>> ARCTIC_BULLET;
  public static DeferredHolder<Item, ChampionEggItem> CHAMPION_EGG_ITEM;
  public static DeferredHolder<ParticleType<?>, SimpleParticleType> RANK_PARTICLE_TYPE;
  public static DeferredHolder<MobEffect, ParalysisEffect> PARALYSIS_EFFECT_TYPE;
  public static DeferredHolder<MobEffect, WoundEffect> WOUND_EFFECT_TYPE;
  public static DeferredHolder<ArgumentTypeInfo<?, ?>, AffixArgumentInfo> AFFIX_ARGUMENT_TYPE;
  public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> ENTITY_TAG_COMPONENT = COMPONENTS.register("entity_tag",()-> DataComponentType.<CompoundTag>builder().persistent(CompoundTag.CODEC).build());

  public static void registerItems(IEventBus bus) {
    CHAMPION_EGG_ITEM = EGG.register("champion_egg", ChampionEggItem::new);
    EGG.register(bus);
  }

  public static void registerLootModifiers(IEventBus bus) {
    CHAMPION_LOOT = LOOT_MODIFIER_SERIALIZERS.register(
      "champion_loot", () -> ChampionLootModifier.CODEC);
    LOOT_MODIFIER_SERIALIZERS.register(bus);
  }

  public static void registerArgumentType(IEventBus bus) {
    AFFIX_ARGUMENT_TYPE = ARGUMENT_TYPES.register("affixes", () -> ArgumentTypeInfos.registerByClass(AffixArgumentType.class, new AffixArgumentInfo()));
    ARGUMENT_TYPES.register(bus);
  }

  public static void registerParticles(IEventBus bus) {
    RANK_PARTICLE_TYPE = PARTICLE_TYPE.register("rank", () -> new SimpleParticleType(true));
    PARTICLE_TYPE.register(bus);
  }

  public static void registerMobEffects(IEventBus bus) {
    PARALYSIS_EFFECT_TYPE = MOB_EFFECT.register("paralysis", ParalysisEffect::new);
    WOUND_EFFECT_TYPE = MOB_EFFECT.register("wound", WoundEffect::new);
    MOB_EFFECT.register(bus);
  }

  public static void registerEntityTypes(IEventBus bus) {
    ARCTIC_BULLET = ENTITY_TYPE.register("arctic_bullet", () -> EntityType.Builder.<ArcticBulletEntity>of(ArcticBulletEntity::new, MobCategory.MISC).sized(2, 2).build(Champions.MODID));
    ENKINDLING_BULLET = ENTITY_TYPE.register("enkindling_bullet", () -> EntityType.Builder.<EnkindlingBulletEntity>of(EnkindlingBulletEntity::new, MobCategory.MISC).sized(2, 2).build(Champions.MODID));
    ENTITY_TYPE.register(bus);
  }

  public static void registerLootItemConditions(IEventBus bus) {
    ENTITY_IS_CHAMPION = LOOT_ITEM_CONDITION_TYPE.register("entity_champion", () -> new LootItemConditionType(EntityIsChampion.CODEC));
    CHAMPION_PROPERTIES = LOOT_ITEM_CONDITION_TYPE.register("champion_properties", () -> new LootItemConditionType(ChampionPropertyCondition.CODEC));
    LOOT_ITEM_CONDITION_TYPE.register(bus);
  }

  public static void registerCustomStats(IEventBus bus) {
    CHAMPION_MOBS_KILLED = makeCustomStat("champion_mobs_killed", StatFormatter.DEFAULT);
    CHAMPIONS_STATS.register(bus);
  }

  public static void registerAttachment(IEventBus bus) {
    CHAMPION_ATTACHMENT = ATTACHMENTS.register("champion_attachment", () -> AttachmentType.serializable(entity -> ChampionAttachment.createProvider((LivingEntity) entity)).build());
    ATTACHMENTS.register(bus);
  }

  private static DeferredHolder<ResourceLocation, ResourceLocation> makeCustomStat(String key, StatFormatter formatter) {
    ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(CHAMPIONS_STATS.getNamespace(), key);
    var holder = CHAMPIONS_STATS.register(key, () -> resourceLocation);
    CUSTOM_STAT_FORMATTERS.put(resourceLocation, formatter);
    return holder;
  }

  public static void register(IEventBus bus) {
    registerItems(bus);
    registerParticles(bus);
    registerMobEffects(bus);
    registerEntityTypes(bus);
    registerLootModifiers(bus);
    registerLootItemConditions(bus);
    registerArgumentType(bus);
    registerCustomStats(bus);
    registerAttachment(bus);
  }

  public static void registerFormatter() {
    CUSTOM_STAT_FORMATTERS.forEach(Stats.CUSTOM::get);
  }
}
