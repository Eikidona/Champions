package top.theillusivec4.champions.common.entity;

import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import top.theillusivec4.champions.common.registry.ChampionsRegistry;

import javax.annotation.Nonnull;

public class EnkindlingBulletEntity extends BaseBulletEntity {

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
      DamageSource damageSource = new DamageSources(target.level.registryAccess()).inFire();
      target.hurt(new DamageSource(new Holder.Direct<>(new DamageType("cinderBullet", 0.1f)), this), 1); //.setIsFire().setMagic()
    }
    target.setSecondsOnFire(8);
  }


  @Override
  protected ParticleOptions getParticle() {
    return ParticleTypes.FLAME;
  }
}
