package top.theillusivec4.champions.client.renderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.common.registry.ChampionsRegistry;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = Champions.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ChampionsRenderer {

  @SubscribeEvent
  public static void rendererRegistering(final EntityRenderersEvent.RegisterRenderers evt) {
    evt.registerEntityRenderer(ChampionsRegistry.ARCTIC_BULLET.get(),
      (renderManager) -> new ColorizedBulletRenderer(renderManager, 0x42F5E3));
    evt.registerEntityRenderer(ChampionsRegistry.ENKINDLING_BULLET.get(),
      (renderManager) -> new ColorizedBulletRenderer(renderManager, 0xFC5A03));
  }
}
