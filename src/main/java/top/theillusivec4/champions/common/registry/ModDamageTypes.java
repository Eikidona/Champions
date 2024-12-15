package top.theillusivec4.champions.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import top.theillusivec4.champions.Champions;

public class ModDamageTypes {
  public static final ResourceKey<DamageType> REFLECTION_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, Champions.getLocation("reflection"));
  public static final ResourceKey<DamageType> CINDER_BULLET = ResourceKey.create(Registries.DAMAGE_TYPE, Champions.getLocation("cinder_bullet"));

  public static DamageSource of(Level level, ResourceKey<DamageType> key) {
    return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(key).orElseThrow());
  }

  public static DamageSource of(Level level, ResourceKey<DamageType> key, Entity source) {
    return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key), source);
  }

}
