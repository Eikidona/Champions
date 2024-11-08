package top.theillusivec4.champions.common.affix.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.api.*;
import top.theillusivec4.champions.common.config.ChampionsConfig;
import top.theillusivec4.champions.common.network.SPacketSyncAffixData;

public abstract class BasicAffix implements IAffix {
  private AffixCategory category;
  private boolean hasSubscriptions;
  private String prefix;

  public BasicAffix() {

    if (hasSubscriptions()) {
      NeoForge.EVENT_BUS.register(this);
    }
  }


  public static boolean canTarget(LivingEntity livingEntity, LivingEntity target,
                                  boolean sightCheck) {

    if (target == null || !target.isAlive() || target instanceof ArmorStand || (sightCheck
      && !hasLineOfSight(livingEntity, target))) {
      return false;
    }
    AttributeInstance attributeInstance = livingEntity.getAttribute(Attributes.FOLLOW_RANGE);
    double range = attributeInstance == null ? 16.0D : attributeInstance.getValue();
    range = ChampionsConfig.affixTargetRange == 0 ? range
      : Math.min(range, ChampionsConfig.affixTargetRange);
    return livingEntity.distanceTo(target) <= range;
  }

  private static boolean hasLineOfSight(LivingEntity livingEntity, LivingEntity target) {

    if (livingEntity instanceof Mob mob) {
      return mob.getSensing().hasLineOfSight(target);
    } else {
      return livingEntity.hasLineOfSight(target);
    }
  }

  @Override
  public void setSubscriptions(boolean hasSubscriptions) {
    this.hasSubscriptions = hasSubscriptions;
  }

  @Override
  public boolean hasSubscriptions() {
    return hasSubscriptions;
  }

  @Override
  public void setCategory(AffixCategory category) {
    this.category = category;
    Champions.API.addCategory(this.getCategory(), this);
  }

  @Override
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public ResourceLocation getIdentifier() {
    return AffixRegistry.AFFIX_REGISTRY.getKey(this);
  }

  @Override
  public String toString() {
    return this.getIdentifier().toString();
  }

  @Override
  public AffixCategory getCategory() {
    return this.category;
  }

  @Override
  public String getPrefix() {
    return this.prefix == null ? "affix." : this.prefix;
  }

  @Override
  public void sync(IChampion champion) {
    LivingEntity livingEntity = champion.getLivingEntity();
    CompoundTag tag = this.writeSyncTag(champion);
    PacketDistributor.sendToPlayersTrackingEntity(livingEntity,
      new SPacketSyncAffixData(livingEntity.getId(), this.toString(), tag));
  }
}
