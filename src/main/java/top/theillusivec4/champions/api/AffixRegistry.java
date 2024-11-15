package top.theillusivec4.champions.api;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegistryBuilder;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.common.integration.kubejs.CustomAffixEvent;

public class AffixRegistry {
  public static final ResourceKey<Registry<IAffix>> AFFIX_REGISTRY_KEY = ResourceKey.createRegistryKey(Champions.getLocation("affixes"));
  public static final ResourceLocation EMPTY = Champions.getLocation("empty");

  public static final Registry<IAffix> AFFIX_REGISTRY = new RegistryBuilder<>(AFFIX_REGISTRY_KEY)
    .sync(true)
    .maxId(2048)
    .defaultKey(EMPTY)
    .onAdd((registry, id, resourceKey, affix) -> {
      Champions.LOGGER.info("Affix added to registry: " + resourceKey);
      NeoForge.EVENT_BUS.post(new CustomAffixEvent.OnBuild(affix));
    })
    .create();

}
