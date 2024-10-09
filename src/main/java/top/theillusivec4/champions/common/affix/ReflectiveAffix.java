package top.theillusivec4.champions.common.affix;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.champions.api.AffixCategory;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.affix.core.BasicAffix;
import top.theillusivec4.champions.common.config.ChampionsConfig;
import top.theillusivec4.champions.common.registry.ModDamageTypes;

public class ReflectiveAffix extends BasicAffix {


  public ReflectiveAffix() {
    super("reflective", AffixCategory.OFFENSE, true);
  }

  @SubscribeEvent
  public void onDamageEvent(LivingDamageEvent evt) {
    if (!ChampionsConfig.reflectiveLethal && evt.getSource().is(ModDamageTypes.REFLECTION_DAMAGE)) {
      LivingEntity living = evt.getEntity();
      float currentDamage = evt.getAmount();

      if (currentDamage >= living.getHealth()) {
        evt.setAmount(living.getHealth() - 1);
      }
    }
  }

  @Override
  public float onDamage(IChampion champion, DamageSource source, float amount, float newAmount) {

    if (source.getDirectEntity() instanceof LivingEntity sourceEntity) {

      if (source.is(ModDamageTypes.REFLECTION_DAMAGE) || (source.getEntity() instanceof LivingEntity && source.typeHolder().is(DamageTypes.THORNS))) {
        return newAmount;
      }
      DamageSources newSources = new DamageSources(champion.getLivingEntity().level().registryAccess());
      DamageSource newSource = newSources.magic();
      //newSource.setThorns();
      float min = (float) ChampionsConfig.reflectiveMinPercent;
      float damage = (float) Math.min(amount * (sourceEntity.getRandom().nextFloat() * (ChampionsConfig.reflectiveMaxPercent - min) + min), ChampionsConfig.reflectiveMax);
      if (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE)) {
        if (source.getEntity() instanceof LivingEntity living)
          living.setSecondsOnFire(champion.getLivingEntity().getRemainingFireTicks());
      }
      if (source.is(DamageTypes.EXPLOSION)) {
        newSource = newSources.explosion(sourceEntity, source.getDirectEntity());
      }

      if (source.is(DamageTypes.MAGIC)) {
        newSource = newSources.magic();
      }

      if (source.scalesWithDifficulty()) {
        newSource.scalesWithDifficulty();
      }

      sourceEntity.hurt(newSource, damage);
    }
    return newAmount;
  }
}
