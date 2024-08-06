package top.theillusivec4.champions.client;


import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiOverlaysEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.common.item.ChampionEggItem;
import top.theillusivec4.champions.common.particle.RankParticle;
import top.theillusivec4.champions.common.registry.ChampionsRegistry;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = Champions.MODID)
public class ClientEventHandler {

  @SubscribeEvent
  public static void onRegisterColor(final RegisterColorHandlersEvent.Item event) {
    event.register(ChampionEggItem::getColor, ChampionsRegistry.CHAMPION_EGG_ITEM.get());

  }

  @SubscribeEvent
  public static void registerGuiOverlayEvent(final RegisterGuiOverlaysEvent evt) {
    evt.registerBelow(VanillaGuiOverlay.BOSS_EVENT_PROGRESS.id(), new ResourceLocation(Champions.MODID , "health_overlay"), new ChampionsOverlay());
  }

  @SubscribeEvent
  public static void onRegisterParticleProviders(RegisterParticleProvidersEvent evt) {
    evt.registerSpriteSet(ChampionsRegistry.RANK_PARTICLE_TYPE.get(), RankParticle.RankFactory::new);
  }

}
