package top.theillusivec4.champions.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.api.IAffix;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.client.ChampionsOverlay;
import top.theillusivec4.champions.client.config.ClientChampionsConfig;
import top.theillusivec4.champions.common.capability.ChampionAttachment;
import top.theillusivec4.champions.common.util.ChampionHelper;

import java.util.Set;
import java.util.stream.Collectors;

public class HUDHelper {

  private static final ResourceLocation GUI_BAR_TEXTURES = ResourceLocation.fromNamespaceAndPath(Champions.MODID,
    "textures/gui/bars.png");
  private static final ResourceLocation GUI_STAR = ResourceLocation.fromNamespaceAndPath(Champions.MODID,
    "textures/gui/staricon.png");

  public static boolean renderHealthBar(GuiGraphics guiGraphics, final LivingEntity livingEntity) {
    return ChampionAttachment.getAttachment(livingEntity).map(champion -> {
      IChampion.Client clientChampion = champion.getClient();
      return ChampionHelper.isValidChampion(clientChampion) && clientChampion.getRank().map(rank -> {
        int championLevel = rank.getA();
        Set<String> affixSet = clientChampion.getAffixes().stream().map(IAffix::getIdentifier)
          .collect(Collectors.toSet());

        if (championLevel > 0 || !affixSet.isEmpty()) {
          Minecraft client = Minecraft.getInstance();
          // calculate render position
          int i = client.getWindow().getGuiScaledWidth();
          int k = i / 2 - 91;
          int j = 21;
          int xOffset = ClientChampionsConfig.hudXOffset;
          int yOffset = ClientChampionsConfig.hudYOffset;
          int color = rank.getB();

          float r = (float) FastColor.ARGB32.red(color) / 255F;
          float g = (float) FastColor.ARGB32.green(color) / 255F;
          float b = (float) FastColor.ARGB32.blue(color) / 255F;

          RenderSystem.defaultBlendFunc();
          // set shader color for render element
          RenderSystem.setShaderColor(r, g, b, 1.0F);
          RenderSystem.enableBlend();
          ChampionsOverlay.startX = xOffset + k;
          ChampionsOverlay.startY = yOffset + 1;

          guiGraphics.blit(GUI_BAR_TEXTURES, xOffset + k, yOffset + j, 0, 60, 182, 5, 256, 256);
          int healthOffset =
            (int) ((livingEntity.getHealth() / livingEntity.getMaxHealth()) * 183.0F);

          if (healthOffset > 0) {
            guiGraphics.blit(GUI_BAR_TEXTURES, xOffset + k, yOffset + j, 0, 65, healthOffset, 5, 256,
              256);
          }

          if (championLevel <= 18) {
            int startStarsX = xOffset + i / 2 - 5 - 5 * (championLevel - 1);

            for (int tier = 0; tier < championLevel; tier++) {
              guiGraphics.blit(GUI_STAR, startStarsX, yOffset + 1, 0, 0, 9, 9, 9, 9);
              startStarsX += 10;
            }
          } else {
            int startStarsX = xOffset + i / 2 - 5;
            String count = "x" + championLevel;
            guiGraphics.blit(GUI_STAR, startStarsX - client.font.width(count) / 2,
              yOffset + 1, 0, 0, 9, 9, 9, 9);
            guiGraphics.drawString(client.font, count,
              startStarsX + 10 - client.font.width(count) / 2.0F, yOffset + 2,
              16777215, true);
          }
          Component customName = livingEntity.getCustomName();
          String name;

          if (customName == null) {
            name = Component.translatable("rank.champions.title." + championLevel).getString();
            name += " " + livingEntity.getName().getString();
          } else {
            name = customName.getString();
          }
          guiGraphics.drawString(client.font, name,
            xOffset + (float) (i / 2 - client.font.width(name) / 2),
            yOffset + (float) (j - 9), color, true);
          // reset shader color
          RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
          StringBuilder builder = new StringBuilder();

          for (String affix : affixSet) {
            builder.append(
              Component.translatable("affix." + Champions.MODID + "." + affix).getString());
            builder.append(" ");
          }
          String affixes = builder.toString().trim();
          guiGraphics.drawString(client.font, affixes,
            xOffset + (float) (i / 2 - client.font.width(affixes) / 2),
            yOffset + (float) (j + 6), 16777215, true);
          RenderSystem.disableBlend();
          return true;
        }
        return false;
      }).orElse(false);
    }).orElse(false);
  }
}
