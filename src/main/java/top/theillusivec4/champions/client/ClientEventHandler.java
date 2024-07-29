package top.theillusivec4.champions.client;

import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.champions.common.particle.RankParticle;
import top.theillusivec4.champions.common.registry.ChampionsRegistry;

public class ClientEventHandler {

  @SubscribeEvent
  public void renderChampionHealth(final CustomizeGuiOverlayEvent.BossEventProgress evt) {
    if (ChampionsOverlay.isRendering) {
      evt.setCanceled(true);
      ForgeHooksClient.onCustomizeBossEventProgress(evt.getGuiGraphics(), evt.getWindow(), evt.getBossEvent(), evt.getX(), evt.getY(), evt.getIncrement());
    }
  }

  @SubscribeEvent
  public void registerGuiOverlayEvent(final RegisterGuiOverlaysEvent evt) {
    evt.registerBelow(VanillaGuiOverlay.DEBUG_TEXT.id(), "champion_health_gui", new ChampionsOverlay());
  }

  @SubscribeEvent
  public void onRegisterParticleProviders(RegisterParticleProvidersEvent evt) {
    evt.registerSpriteSet(ChampionsRegistry.RANK_PARTICLE_TYPE.get(), RankParticle.RankFactory::new);
  }

}
