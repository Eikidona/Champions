package top.theillusivec4.champions.common.integration.kubejs;

import net.neoforged.bus.api.Event;
import top.theillusivec4.champions.api.IAffix;

public abstract class CustomAffixEvent extends Event {
  private final IAffix affix;

  private CustomAffixEvent(IAffix affix) {
    this.affix = affix;
  }

  public IAffix getAffix() {
    return affix;
  }

  public static class OnBuild extends CustomAffixEvent {
    public OnBuild(IAffix affix) {
      super(affix);
    }
  }

}
