package top.theillusivec4.champions.client;

import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEventHandler {

  @SubscribeEvent
  public void renderChampionHealth(final RenderGuiOverlayEvent.Post evt) {

    if (ChampionsOverlay.isRendering) {
      evt.setCanceled(true);
      ForgeHooksClient.onCustomizeBossEventProgress(evt.getGuiGraphics(), evt.getWindow(),null,0,0,0);
    }
  }
}
