package top.theillusivec4.champions.common.integration.kubejs;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.champions.api.IAffix;

public class ChampionsEvents {
  public static void onCustomAffixBuild(IAffix affix){
    ModLoader.postEvent(new CustomAffixEvent.OnBuild(affix));
  }
}
