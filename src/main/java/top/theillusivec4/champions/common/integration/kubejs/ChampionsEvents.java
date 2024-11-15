package top.theillusivec4.champions.common.integration.kubejs;

import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.champions.api.IAffix;

public class ChampionsEvents {
  public static void onCustomAffixBuild(IAffix affix){
    NeoForge.EVENT_BUS.post(new CustomAffixEvent.OnBuild(affix));
  }
}
