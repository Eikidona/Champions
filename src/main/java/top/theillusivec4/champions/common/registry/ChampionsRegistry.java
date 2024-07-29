package top.theillusivec4.champions.common.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.common.entity.ArcticBulletEntity;
import top.theillusivec4.champions.common.entity.EnkindlingBulletEntity;
import top.theillusivec4.champions.common.item.ChampionEggItem;
import top.theillusivec4.champions.common.potion.ParalysisEffect;
import top.theillusivec4.champions.common.potion.WoundEffect;

public class ChampionsRegistry {

  private static final DeferredRegister<Item> EGG = DeferredRegister.create(ForgeRegistries.ITEMS, Champions.MODID);
  // RANK
  private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPE = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Champions.MODID);
  // PARALYSIS
  private static final DeferredRegister<MobEffect> MOB_EFFECT = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Champions.MODID);
  private static final DeferredRegister<EntityType<?>> ENTITY_TYPE = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Champions.MODID);
  public static RegistryObject<EntityType<? extends EnkindlingBulletEntity>> ENKINDLING_BULLET;
  public static RegistryObject<EntityType<? extends ArcticBulletEntity>> ARCTIC_BULLET;
  public static RegistryObject<ChampionEggItem> CHAMPION_EGG_ITEM;
  public static RegistryObject<SimpleParticleType> RANK_PARTICLE_TYPE;
  public static RegistryObject<ParalysisEffect> PARALYSIS_PARTICLE_TYPE;
  public static RegistryObject<WoundEffect> WOUND_PARTICLE_TYPE;

  public static void registerItems(IEventBus bus) {
    CHAMPION_EGG_ITEM = EGG.register("champion_egg", ChampionEggItem::new);
    EGG.register(bus);
  }


  public static void registerParticles(IEventBus bus) {
//    RANK_PARTICLE_TYPE = PARTICLE_TYPE.register("rank", () -> new RankParticle());
    PARTICLE_TYPE.register(bus);
  }

  public static void registerMobEffects(IEventBus bus) {
    PARALYSIS_PARTICLE_TYPE = MOB_EFFECT.register("paralysis", ParalysisEffect::new);
    WOUND_PARTICLE_TYPE = MOB_EFFECT.register("wound", WoundEffect::new);
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
  }

}
