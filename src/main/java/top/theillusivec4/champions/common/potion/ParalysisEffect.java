package top.theillusivec4.champions.common.potion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import top.theillusivec4.champions.Champions;

public class ParalysisEffect extends MobEffect {

  public ParalysisEffect() {
    super(MobEffectCategory.HARMFUL, 0xff5733);
    this.addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE,
      ResourceLocation.fromNamespaceAndPath(Champions.MODID,"paralysis_modifier"), 1, AttributeModifier.Operation.ADD_VALUE);
  }
}
