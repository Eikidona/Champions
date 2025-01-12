package top.theillusivec4.champions.common.datagen;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.champions.api.AttributesModifierDataLoader;
import top.theillusivec4.champions.api.ChampionModifierCondition;
import top.theillusivec4.champions.api.ModifierSetting;
import top.theillusivec4.champions.common.config.ConfigEnums;
import top.theillusivec4.champions.common.loot.AffixesPredicate;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class AttributesModifierDataProvider implements DataProvider {

    private final PackOutput packOutput;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public AttributesModifierDataProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.packOutput = packOutput;
        this.lookupProvider = lookupProvider;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        try {
            lookupProvider.get().lookupOrThrow(Registries.ATTRIBUTE).listElements().forEach(attribute -> {

                var attributeId = attribute.key().location();
                var ref = new Object() {
                    double baseValue = 1d;
                    boolean enable = false;
                    AttributeModifier.Operation operation = AttributeModifier.Operation.ADDITION;
                };


                Path outputPath = packOutput.getOutputFolder()
                        .resolve("data").resolve(attributeId.getNamespace()).resolve(AttributesModifierDataLoader.getFolder()).resolve(attributeId.getPath() + ".json");
                // apples default setting
                if (attributeId == ForgeRegistries.ATTRIBUTES.getKey(Attributes.MAX_HEALTH)) {
                    ref.baseValue = 0.35D;
                    ref.enable = true;
                    ref.operation = AttributeModifier.Operation.MULTIPLY_TOTAL;
                } else if (attributeId == ForgeRegistries.ATTRIBUTES.getKey(Attributes.ATTACK_DAMAGE)) {
                    ref.baseValue = 0.5D;
                    ref.enable = true;
                    ref.operation = AttributeModifier.Operation.MULTIPLY_TOTAL;
                } else if (attributeId == ForgeRegistries.ATTRIBUTES.getKey(Attributes.ARMOR)) {
                    ref.baseValue = 2.0D;
                    ref.enable = true;
                } else if (attributeId == ForgeRegistries.ATTRIBUTES.getKey(Attributes.ARMOR_TOUGHNESS)) {
                    ref.baseValue = 1.0D;
                    ref.enable = true;
                } else if (attributeId == ForgeRegistries.ATTRIBUTES.getKey(Attributes.KNOCKBACK_RESISTANCE)) {
                    ref.baseValue = 0.05D;
                    ref.enable = true;
                } else {
                    ref.baseValue = 0;
                    ref.enable = false;
                    ref.operation = AttributeModifier.Operation.ADDITION;
                }

                futures.add(lookupProvider.thenCompose(provider ->
                        DataProvider.saveStable(cachedOutput, ModifierSetting.MAP_CODEC.codec().encodeStart(JsonOps.INSTANCE, new ModifierSetting(attributeId,
                                        ref.enable, Pair.of(ref.baseValue, ref.operation),
                                        Optional.of(new ChampionModifierCondition(Optional.of(Set.of(new ResourceLocation("minecraft:creeper"))),
                                                Optional.of(MinMaxBounds.Ints.ANY),
                                                Optional.of(new AffixesPredicate(Set.of(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY)), ConfigEnums.Permission.BLACKLIST)))).result().orElseThrow()
                                , outputPath)
                ));
            });
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "attribute modifiers";
    }
}
