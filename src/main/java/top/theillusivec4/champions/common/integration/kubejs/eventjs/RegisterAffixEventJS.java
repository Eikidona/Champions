package top.theillusivec4.champions.common.integration.kubejs.eventjs;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.api.AffixCategory;
import top.theillusivec4.champions.api.BasicAffixBuilder;
import top.theillusivec4.champions.api.IAffixBuilder;
import top.theillusivec4.champions.common.integration.kubejs.affixjs.CustomAffix;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RegisterAffixEventJS extends EventJS {
	private final Map<ResourceLocation, IAffixBuilder<?>> builderMap = new HashMap<>();
	
	public RegisterAffixEventJS() {
	
	}
	
	public CustomAffix.CustomAffixBuilder createAffix(){
		return new CustomAffix.CustomAffixBuilder();
	}
	
	public IAffixBuilder<?> createAffixType(ResourceLocation name, Supplier<CustomAffix> supplier){
		IAffixBuilder<?> builder = new BasicAffixBuilder<>(supplier).setType(name).setCategory(AffixCategory.CC).setEnable(true);
		builderMap.put(name, builder);
		return builder;
	}
	
	public Map<ResourceLocation, IAffixBuilder<?>> getBuilderMap() {
		return builderMap;
	}
}
