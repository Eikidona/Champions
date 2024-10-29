package top.theillusivec4.champions.common.registry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.api.AffixRegistry;
import top.theillusivec4.champions.api.IAffix;
import top.theillusivec4.champions.common.affix.*;

public class ModAffixTypes {
  private static final DeferredRegister<IAffix> AFFIXES = DeferredRegister.create(AffixRegistry.AFFIX_REGISTRY, Champions.MODID);
  public static final DeferredHolder<IAffix, AdaptableAffix> ADAPTABLE = AFFIXES.register("adaptable", AdaptableAffix::new);
  public static final DeferredHolder<IAffix, ArcticAffix> ARCTIC = AFFIXES.register("arctic", ArcticAffix::new);
  public static final DeferredHolder<IAffix, DampeningAffix> DAMPENING = AFFIXES.register("dampening", DampeningAffix::new);
  public static final DeferredHolder<IAffix, DesecratingAffix> DESECRATING = AFFIXES.register("desecrating", DesecratingAffix::new);
  public static final DeferredHolder<IAffix, EnkindlingAffix> ENKINDLING = AFFIXES.register("enkindling", EnkindlingAffix::new);
  public static final DeferredHolder<IAffix, HastyAffix> HASTY = AFFIXES.register("hasty", HastyAffix::new);
  public static final DeferredHolder<IAffix, InfestedAffix> INFESTED = AFFIXES.register("infested", InfestedAffix::new);
  public static final DeferredHolder<IAffix, KnockingAffix> KNOCKING = AFFIXES.register("knocking", KnockingAffix::new);
  public static final DeferredHolder<IAffix, LivelyAffix> LIVELY = AFFIXES.register("lively", LivelyAffix::new);
  public static final DeferredHolder<IAffix, MagneticAffix> MAGNETIC = AFFIXES.register("magnetic", MagneticAffix::new);
  public static final DeferredHolder<IAffix, MoltenAffix> MOLTEN = AFFIXES.register("molten", MoltenAffix::new);
  public static final DeferredHolder<IAffix, ParalyzingAffix> PARALYZING = AFFIXES.register("paralyzing", ParalyzingAffix::new);
  public static final DeferredHolder<IAffix, PlaguedAffix> PLAGUED = AFFIXES.register("plagued", PlaguedAffix::new);
  public static final DeferredHolder<IAffix, ReflectiveAffix> REFLECTIVE = AFFIXES.register("reflective", ReflectiveAffix::new);
  public static final DeferredHolder<IAffix, ShieldingAffix> SHIELDING = AFFIXES.register("shielding", ShieldingAffix::new);
  public static final DeferredHolder<IAffix, WoundingAffix> WOUNDING = AFFIXES.register("wounding", WoundingAffix::new);

  public static void register(IEventBus bus) {
    AFFIXES.register(bus);
  }
}
