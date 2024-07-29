package top.theillusivec4.champions.common.affix;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.api.AffixCategory;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.affix.core.BasicAffix;
import top.theillusivec4.champions.common.config.ChampionsConfig;
import top.theillusivec4.champions.common.registry.ChampionsRegistry;

public class ParalyzingAffix extends BasicAffix {
  public ParalyzingAffix() {
    super("paralyzing", AffixCategory.CC);
  }

  @Override
  public boolean onAttack(
      IChampion champion, LivingEntity target, DamageSource source,
      float amount) {

    if (target.getRandom().nextFloat() < ChampionsConfig.paralyzingChance && !target.hasEffect(
        ChampionsRegistry.PARALYSIS_PARTICLE_TYPE.get())) {
      target.addEffect(new MobEffectInstance(ChampionsRegistry.PARALYSIS_PARTICLE_TYPE.get(), 60, 0));
    }
    return true;
  }
}
