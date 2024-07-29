package top.theillusivec4.champions.client;

import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import top.theillusivec4.champions.common.particle.RankParticle;
import top.theillusivec4.champions.common.registry.ChampionsRegistry;

public class ClientEventHandler {

  @SubscribeEvent
  public void renderChampionHealth(final RenderGuiOverlayEvent.Post evt) {

    if (ChampionsOverlay.isRendering) {
      evt.setCanceled(true);
      ForgeHooksClient.onCustomizeBossEventProgress(evt.getGuiGraphics(), evt.getWindow(), null, 0, 0, 0);
    }
  }

  @SubscribeEvent
  public void onRegisterParticleProviders(RegisterParticleProvidersEvent evt) {
    evt.registerSpriteSet(ChampionsRegistry.RANK_PARTICLE_TYPE.get(), RankParticle.RankFactory::new);
  }

}
