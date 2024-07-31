package top.theillusivec4.champions.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.common.item.ChampionEggItem;
import top.theillusivec4.champions.common.particle.RankParticle;
import top.theillusivec4.champions.common.registry.ChampionsRegistry;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = Champions.MODID)
public class ClientEventHandler {

  @SubscribeEvent
  public static void onClientSetup(final FMLClientSetupEvent event) {
    Minecraft.getInstance().getItemColors()
      .register(ChampionEggItem::getColor, ChampionsRegistry.CHAMPION_EGG_ITEM.get());
  }

  @SubscribeEvent
  public static void registerGuiOverlayEvent(final RegisterGuiOverlaysEvent evt) {
    evt.registerAboveAll(Champions.MODID + "_health_overlay", new ChampionsOverlay());
  }

  @SubscribeEvent
  public static void onRegisterParticleProviders(RegisterParticleProvidersEvent evt) {
    evt.registerSpriteSet(ChampionsRegistry.RANK_PARTICLE_TYPE.get(), RankParticle.RankFactory::new);
  }

}
