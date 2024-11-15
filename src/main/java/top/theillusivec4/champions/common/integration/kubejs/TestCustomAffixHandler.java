package top.theillusivec4.champions.common.integration.kubejs;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import top.theillusivec4.champions.Champions;
@EventBusSubscriber(modid = Champions.MODID)
public class TestCustomAffixHandler {

  @SubscribeEvent
  private static void testOnCustomAffixBuild(CustomAffixEvent.OnBuild event) {
    Champions.LOGGER.info(event.getAffix());
    Champions.LOGGER.info("testOnCustomAffixBuild");
  }
}
