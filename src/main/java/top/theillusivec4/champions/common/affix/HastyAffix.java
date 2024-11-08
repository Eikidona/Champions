package top.theillusivec4.champions.common.affix;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.api.*;
import top.theillusivec4.champions.common.affix.core.BasicAffix;
import top.theillusivec4.champions.common.config.ChampionsConfig;

public class HastyAffix extends BasicAffix {

  @Override
  public void onInitialSpawn(IChampion champion) {
    AttributeInstance speed = champion.getLivingEntity().getAttribute(Attributes.MOVEMENT_SPEED);
    AttributeModifier hastyModifier =
      new AttributeModifier(Champions.getLocation("hasty"),
        ChampionsConfig.hastyMovementBonus,
        AttributeModifier.Operation.ADD_VALUE);

    if (speed != null && !speed.hasModifier(hastyModifier.id())) {
      speed.addTransientModifier(hastyModifier);
    }
  }

  @Override
  public boolean canApply(IChampion champion) {
    return champion.getLivingEntity().getAttribute(Attributes.MOVEMENT_SPEED) != null;
  }

  @Override
  public void onServerUpdate(IChampion champion) {
    LivingEntity livingEntity = champion.getLivingEntity();

    if (livingEntity.tickCount % 20 == 0) {
      onInitialSpawn(champion);
    }
  }
}
