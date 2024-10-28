package top.theillusivec4.champions.common.integration.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.capability.ChampionAttachment;

public enum ChampionComponentProvider implements IEntityComponentProvider {
  INSTANCE;

  private static Component getChampionName(Tuple<Integer, Integer> rank, IChampion champion) {
    return Component.translatable("rank.champions.title." + rank.getA()).append(" " + champion.getLivingEntity().getName().getString()).withColor(rank.getB());
  }

  @Override
  public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
    ChampionAttachment.getAttachment(entityAccessor.getEntity()).ifPresent(
      champion -> {
        champion.getClient().getRank().ifPresent(rank -> iTooltip.replace(JadeIds.CORE_OBJECT_NAME, getChampionName(rank, champion)));
        champion.getClient().getAffixes().forEach(
          affix -> iTooltip.add(Component.translatable("affix." + Champions.MODID + "." + affix.getIdentifier()))
        );
      });
  }

  @Override
  public ResourceLocation getUid() {
    return Champions.getLocation("enable_affix_compact");
  }
}
