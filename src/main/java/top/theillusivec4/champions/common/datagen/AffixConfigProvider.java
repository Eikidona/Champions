package top.theillusivec4.champions.common.datagen;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.api.data.AffixSetting;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AffixConfigProvider implements DataProvider {

    private final PackOutput packOutput;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public AffixConfigProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.packOutput = packOutput;
        this.lookupProvider = lookupProvider;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        Champions.API.getAffixes().forEach(affix -> {
            var affixId = affix.getIdentifier();

            Path outputPath = packOutput.getOutputFolder()
                    .resolve("data/" + affixId.getNamespace() + "/affix_setting/" + affixId.getPath() + ".json");
            futures.add(lookupProvider.thenCompose(provider ->
                    DataProvider.saveStable(cachedOutput, AffixSetting.CODEC.encodeStart(JsonOps.INSTANCE, affix.getSetting()).get().orThrow().getAsJsonObject(), outputPath)
            ));
        });

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Affix_configs";
    }
}
