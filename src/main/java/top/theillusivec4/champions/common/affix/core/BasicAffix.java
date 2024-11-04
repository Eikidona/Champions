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
import top.theillusivec4.champions.api.AffixCategory;
import top.theillusivec4.champions.api.AffixRegistry;
import top.theillusivec4.champions.api.IAffix;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.config.ChampionsConfig;
import top.theillusivec4.champions.common.network.SPacketSyncAffixData;

public abstract class BasicAffix implements IAffix {
  private final AffixCategory category;

  public BasicAffix(AffixCategory category) {
    this(category, false);
  }

  public BasicAffix(AffixCategory category, boolean hasSubscriptions) {
    this.category = category;

    if (hasSubscriptions) {
      NeoForge.EVENT_BUS.register(this);
    }
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
  public void sync(IChampion champion) {
    LivingEntity livingEntity = champion.getLivingEntity();
    CompoundTag tag = this.writeSyncTag(champion);
    PacketDistributor.sendToPlayersTrackingEntity(livingEntity,
      new SPacketSyncAffixData(livingEntity.getId(), this.toString(), tag));
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
}
