/*
 * Copyright (C) 2018-2019  C4
 *
 * This file is part of Champions, a mod made for Minecraft.
 *
 * Champions is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Champions is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Champions.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.champions;

import com.electronwill.nightconfig.core.CommentedConfig;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.api.IChampionsApi;
import top.theillusivec4.champions.api.impl.ChampionsApiImpl;
import top.theillusivec4.champions.client.config.ClientChampionsConfig;
import top.theillusivec4.champions.common.affix.core.AffixManager;
import top.theillusivec4.champions.common.capability.ChampionCapability;
import top.theillusivec4.champions.common.config.ChampionsConfig;
import top.theillusivec4.champions.common.integration.theoneprobe.TheOneProbePlugin;
import top.theillusivec4.champions.common.item.ChampionEggItem;
import top.theillusivec4.champions.common.loot.EntityIsChampion;
import top.theillusivec4.champions.common.loot.LootItemChampionPropertyCondition;
import top.theillusivec4.champions.common.network.NetworkHandler;
import top.theillusivec4.champions.common.rank.RankManager;
import top.theillusivec4.champions.common.registry.ChampionsRegistry;
import top.theillusivec4.champions.common.registry.RegistryReference;
import top.theillusivec4.champions.common.stat.ChampionsStats;
import top.theillusivec4.champions.common.util.EntityManager;
import top.theillusivec4.champions.server.command.ChampionSelectorOptions;
import top.theillusivec4.champions.server.command.ChampionsCommand;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Mod(Champions.MODID)
public class Champions {

    public static final String MODID = "champions";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final IChampionsApi API = ChampionsApiImpl.getInstance();

    public static boolean scalingHealthLoaded = false;
    public static boolean gameStagesLoaded = false;

    public Champions() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::enqueueIMC);
        ModLoadingContext.get().registerConfig(Type.CLIENT, ClientChampionsConfig.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(Type.SERVER, ChampionsConfig.SERVER_SPEC);
        ModLoadingContext.get().registerConfig(Type.COMMON, ChampionsConfig.COMMON_SPEC);
        createServerConfig(ChampionsConfig.RANKS_SPEC, "ranks");
        createServerConfig(ChampionsConfig.AFFIXES_SPEC, "affixes");
        createServerConfig(ChampionsConfig.ENTITIES_SPEC, "entities");
        gameStagesLoaded = ModList.get().isLoaded("gamestages");

        if (gameStagesLoaded) {
            ModLoadingContext.get()
                    .registerConfig(Type.SERVER, ChampionsConfig.STAGE_SPEC, "champions-gamestages.toml");
        }

        eventBus.addListener(this::config);
        eventBus.addListener(this::setup);
        eventBus.addListener(this::registerCaps);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        ChampionsRegistry.register(eventBus);
        scalingHealthLoaded = ModList.get().isLoaded("scalinghealth");
    }

    private static void createServerConfig(ForgeConfigSpec spec, String suffix) {
        String fileName = "champions-" + suffix + ".toml";
        ModLoadingContext.get().registerConfig(Type.SERVER, spec, fileName);
        File defaults = FMLPaths.GAMEDIR.get().resolve("defaultconfigs").resolve(fileName).toFile();

        if (!defaults.exists()) {
            try {
                FileUtils.copyInputStreamToFile(
                        Objects.requireNonNull(Champions.class.getClassLoader().getResourceAsStream(fileName)),
                        defaults);
            } catch (IOException e) {
                LOGGER.error("Error creating default config for " + fileName);
            }
        }
    }

    private void setup(final FMLCommonSetupEvent evt) {
        ChampionCapability.register();
        NetworkHandler.register();
        AffixManager.register();
        evt.enqueueWork(() -> {
            ChampionsStats.setup();
            ChampionSelectorOptions.setup();
            Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE,
                    new ResourceLocation(RegistryReference.IS_CHAMPION), EntityIsChampion.type);
            Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE,
                    new ResourceLocation(RegistryReference.CHAMPION_PROPERTIES),
                    LootItemChampionPropertyCondition.INSTANCE);
            DispenseItemBehavior dispenseBehavior = (source, stack) -> {
                Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
                Optional<EntityType<?>> entityType = ChampionEggItem.getType(stack);
                entityType.ifPresent(type -> {
                    Entity entity = type.create(source.getLevel(), stack.getTag(), null,
                            source.getPos().relative(direction), MobSpawnType.DISPENSER, true,
                            direction != Direction.UP);

                    if (entity instanceof LivingEntity) {
                        ChampionCapability.getCapability(entity)
                                .ifPresent(champion -> ChampionEggItem.read(champion, stack));
                        source.getLevel().addFreshEntity(entity);
                        stack.shrink(1);
                    }
                });
                return stack;
            };
            DispenserBlock.registerBehavior(ChampionsRegistry.CHAMPION_EGG_ITEM.get(), dispenseBehavior);
        });
    }

    private void registerCaps(final RegisterCapabilitiesEvent evt) {
        evt.register(IChampion.class);
    }

    private void registerCommands(final RegisterCommandsEvent evt) {
        ChampionsCommand.register(evt.getDispatcher());
    }

    private void config(final ModConfigEvent evt) {

        if (!evt.getConfig().getModId().equals(MODID)) {
            return;
        }

        if (evt.getConfig().getType() == Type.SERVER) {
            synchronized (this) {

                IConfigSpec<?> spec = evt.getConfig().getSpec();
                CommentedConfig commentedConfig = evt.getConfig().getConfigData();

                if (evt instanceof ModConfigEvent.Loading) {
                    ChampionsConfig.bake();
                    // 重建管理器
                    if (spec == ChampionsConfig.RANKS_SPEC) {
                        ChampionsConfig.transformRanks(commentedConfig);
                        RankManager.buildRanks();
                    } else if (spec == ChampionsConfig.AFFIXES_SPEC) {
                        ChampionsConfig.transformAffixes(commentedConfig);
                        AffixManager.buildAffixSettings();
                    } else if (spec == ChampionsConfig.ENTITIES_SPEC) {
                        ChampionsConfig.transformEntities(commentedConfig);
                        EntityManager.buildEntitySettings();
                    } else if (spec == ChampionsConfig.STAGE_SPEC && Champions.gameStagesLoaded) {
                        ChampionsConfig.entityStages = ChampionsConfig.STAGE.entityStages.get();
                        ChampionsConfig.tierStages = ChampionsConfig.STAGE.tierStages.get();
                    }
                }
            }
        } else if (evt.getConfig().getType() == Type.CLIENT) {
            ClientChampionsConfig.bake();
        } else if (evt.getConfig().getType() == Type.COMMON) {
            ChampionsConfig.bakeCommon();
        }
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // register TheOneProbe integration
        if (ModList.get().isLoaded("theoneprobe")) {
            Champions.LOGGER.info("Champions detected TheOneProbe, registering plugin now");
            InterModComms.sendTo(MODID, "theoneprobe", "getTheOneProbe",
                    TheOneProbePlugin.GetTheOneProbe::new);
        }
    }
}
