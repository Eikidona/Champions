package top.theillusivec4.champions.common.registry;

import com.mojang.serialization.Codec;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.common.entity.ArcticBulletEntity;
import top.theillusivec4.champions.common.entity.EnkindlingBulletEntity;
import top.theillusivec4.champions.common.item.ChampionEggItem;
import top.theillusivec4.champions.common.loot.ChampionLootModifier;
import top.theillusivec4.champions.common.potion.ParalysisEffect;
import top.theillusivec4.champions.common.potion.WoundEffect;
import top.theillusivec4.champions.server.command.AffixArgumentInfo;
import top.theillusivec4.champions.server.command.AffixArgumentType;

public class ChampionsRegistry {

  public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
    DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Champions.MODID);
  private static final DeferredRegister<Item> EGG = DeferredRegister.create(ForgeRegistries.ITEMS, Champions.MODID);
  // RANK
  private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPE = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Champions.MODID);
  // PARALYSIS
  private static final DeferredRegister<MobEffect> MOB_EFFECT = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Champions.MODID);
  private static final DeferredRegister<EntityType<?>> ENTITY_TYPE = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Champions.MODID);
  private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, Champions.MODID);
  public static RegistryObject<Codec<ChampionLootModifier>> CHAMPION_LOOT;
  public static RegistryObject<EntityType<? extends EnkindlingBulletEntity>> ENKINDLING_BULLET;
  public static RegistryObject<EntityType<? extends ArcticBulletEntity>> ARCTIC_BULLET;
  public static RegistryObject<ChampionEggItem> CHAMPION_EGG_ITEM;
  public static RegistryObject<SimpleParticleType> RANK_PARTICLE_TYPE;
  public static RegistryObject<ParalysisEffect> PARALYSIS_EFFECT_TYPE;
  public static RegistryObject<WoundEffect> WOUND_EFFECT_TYPE;
  public static RegistryObject<AffixArgumentInfo> AFFIX_ARGUMENT_TYPE;

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

  public static void register(IEventBus bus) {
    registerItems(bus);
    registerParticles(bus);
    registerMobEffects(bus);
    registerEntityTypes(bus);
    registerLootModifiers(bus);
    registerArgumentType(bus);
  }

}
