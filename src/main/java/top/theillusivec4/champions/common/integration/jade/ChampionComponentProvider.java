package top.theillusivec4.champions.common.integration.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.Identifiers;
import snownee.jade.api.config.IPluginConfig;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.capability.ChampionCapability;

public enum ChampionComponentProvider implements IEntityComponentProvider {
  INSTANCE;

  private static Component getChampionName(Tuple<Integer, Integer> rank, IChampion champion) {
    return Component.translatable("rank.champions.title." + rank.getA()).append(" " + champion.getLivingEntity().getName().getString()).withStyle(Style.EMPTY.withColor(rank.getB()));
  }

  @Override
  public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
    ChampionCapability.getCapability(entityAccessor.getEntity()).ifPresent(
      champion -> {
        champion.getClient().getRank().ifPresent(rank -> iTooltip.add(getChampionName(rank, champion), Identifiers.CORE_OBJECT_NAME));
        champion.getClient().getAffixes().forEach(
          affix -> iTooltip.add(Component.translatable("affix." + Champions.MODID + "." + affix.getIdentifier()))
        );
      });
  }

  @Override
  public ResourceLocation getUid() {
    return new ResourceLocation(Champions.MODID, "enable_affix_compact");
  }
}
