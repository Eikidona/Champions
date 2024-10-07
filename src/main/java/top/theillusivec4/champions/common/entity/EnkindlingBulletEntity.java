package top.theillusivec4.champions.common.entity;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.common.registry.ChampionsRegistry;

import javax.annotation.Nonnull;

public class EnkindlingBulletEntity extends BaseBulletEntity {
  private static final ResourceKey<DamageType> CINDER_BULLET_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Champions.MODID, "cinder_bullet_damage"));

  public EnkindlingBulletEntity(Level level) {
    super(ChampionsRegistry.ENKINDLING_BULLET.get(), level);
  }

  public EnkindlingBulletEntity(Level level, LivingEntity livingEntity, @Nonnull Entity entity,
                                Direction.Axis axis) {
    super(ChampionsRegistry.ENKINDLING_BULLET.get(), level, livingEntity, entity, axis);

  }

  public EnkindlingBulletEntity(EntityType<? extends EnkindlingBulletEntity> enkindlingBulletEntityEntityType, Level level) {
    super(enkindlingBulletEntityEntityType, level);
  }

  @Override
  protected void bulletEffect(LivingEntity target) {

    if (this.getOwner() != null) {
      DamageSource magic = this.damageSources().indirectMagic(this, this.getOwner());
      DamageSource fire = this.damageSources().inFire();
      target.hurt(magic, 1);
      target.hurt(fire, 0);
    } else {
      target.hurt(new DamageSource(target.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(CINDER_BULLET_DAMAGE), this), 1);
    }
    target.setSecondsOnFire(8);
  }

  @Override
  protected ParticleOptions getParticle() {
    return ParticleTypes.FLAME;
  }
}
