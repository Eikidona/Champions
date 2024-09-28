package top.theillusivec4.champions.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.api.IAffix;
import top.theillusivec4.champions.api.impl.ChampionsApiImpl;
import top.theillusivec4.champions.common.capability.ChampionAttachment;
import top.theillusivec4.champions.common.item.ChampionEggItem;
import top.theillusivec4.champions.common.registry.ChampionsRegistry;
import top.theillusivec4.champions.common.util.ChampionBuilder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

public class ChampionsCommand {

  public static final SuggestionProvider<CommandSourceStack> AFFIXES = SuggestionProviders
    .register(ResourceLocation.fromNamespaceAndPath(Champions.MODID, "affixes"),
      (context, builder) -> SharedSuggestionProvider.suggest(
        ChampionsApiImpl.getInstance().getAffixes().stream().map(IAffix::getIdentifier),
        builder));

  public static final SuggestionProvider<CommandSourceStack> MONSTER_ENTITIES = SuggestionProviders
    .register(ResourceLocation.fromNamespaceAndPath(Champions.MODID, "monster_entities"),
      (context, builder) -> SharedSuggestionProvider.suggestResource(
        BuiltInRegistries.ENTITY_TYPE.stream()
          .filter(type -> type.getCategory() == MobCategory.MONSTER),
        builder, EntityType::getKey,
        (type) -> Component.translatable(
          Util.makeDescriptionId("entity", EntityType.getKey(type)))));


  private static final DynamicCommandExceptionType UNKNOWN_ENTITY = new DynamicCommandExceptionType(
    type -> Component.translatable("command.champions.egg.unknown_entity", type));

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    int opPermissionLevel = 2;
    LiteralArgumentBuilder<CommandSourceStack> championsCommand = Commands.literal("champions")
      .requires(player -> player.hasPermission(opPermissionLevel));

    championsCommand.then(Commands.literal("egg").then(
      Commands.argument("entity", ResourceLocationArgument.id()).suggests(MONSTER_ENTITIES)
        .then(Commands.argument("tier", IntegerArgumentType.integer()).executes(
          context -> createEgg(context.getSource(),
            ResourceLocationArgument.getId(context, "entity"),
            IntegerArgumentType.getInteger(context, "tier"), new ArrayList<>())).then(
          Commands.argument("affixes", AffixArgumentType.affix()).suggests(AFFIXES).executes(
            context -> createEgg(context.getSource(),
              ResourceLocationArgument.getId(context, "entity"),
              IntegerArgumentType.getInteger(context, "tier"),
              AffixArgumentType.getAffixes(context, "affixes")))))));

    championsCommand.then(Commands.literal("summon").then(
      Commands.argument("entity", ResourceLocationArgument.id()).suggests(MONSTER_ENTITIES)
        .then(Commands.argument("tier", IntegerArgumentType.integer()).executes(
          context -> summon(context.getSource(),
            ResourceLocationArgument.getId(context, "entity"),
            IntegerArgumentType.getInteger(context, "tier"), new ArrayList<>())).then(
          Commands.argument("affixes", AffixArgumentType.affix()).suggests(AFFIXES).executes(
            context -> summon(context.getSource(),
              ResourceLocationArgument.getId(context, "entity"),
              IntegerArgumentType.getInteger(context, "tier"),
              AffixArgumentType.getAffixes(context, "affixes")))))));

    championsCommand.then(Commands.literal("summonpos").then(
      Commands.argument("pos", BlockPosArgument.blockPos()).then(
        Commands.argument("entity", ResourceLocationArgument.id())
          .suggests(MONSTER_ENTITIES).then(
            Commands.argument("tier", IntegerArgumentType.integer()).executes(
              context -> summon(context.getSource(),
                BlockPosArgument.getSpawnablePos(context, "pos"),
                ResourceLocationArgument.getId(context, "entity"),
                IntegerArgumentType.getInteger(context, "tier"), new ArrayList<>())).then(
              Commands.argument("affixes", AffixArgumentType.affix()).suggests(AFFIXES).executes(
                context -> summon(context.getSource(),
                  BlockPosArgument.getSpawnablePos(context, "pos"),
                  ResourceLocationArgument.getId(context, "entity"),
                  IntegerArgumentType.getInteger(context, "tier"),
                  AffixArgumentType.getAffixes(context, "affixes"))))))));

    dispatcher.register(championsCommand);
  }

  private static int summon(CommandSourceStack source, ResourceLocation resourceLocation, int tier,
                            Collection<IAffix> affixes) throws CommandSyntaxException {
    return summon(source, null, resourceLocation, tier, affixes);
  }

  private static int summon(CommandSourceStack source, @Nullable BlockPos pos,
                            ResourceLocation resourceLocation, int tier, Collection<IAffix> affixes)
    throws CommandSyntaxException {
    EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(resourceLocation);


    if (entityType == null) {
      throw UNKNOWN_ENTITY.create(resourceLocation);
    } else {
      final Entity sourceEntity = source.getEntity();

      if (sourceEntity != null) {
        Entity entity = entityType.create((ServerLevel) sourceEntity.level(), null,
          pos != null ? pos : new BlockPos(sourceEntity.blockPosition()), MobSpawnType.COMMAND,
          false, false);

        if (entity instanceof LivingEntity) {
          ChampionAttachment.getAttachment(entity).ifPresent(
            champion -> ChampionBuilder.spawnPreset(champion, tier, new ArrayList<>(affixes)));
          source.getLevel().addFreshEntity(entity);
          source.sendSuccess(() -> Component.translatable("commands.champions.summon.success",
            Component.translatable("rank.champions.title." + tier).getString() + " " + entity
              .getDisplayName().getString()), false);
        }
      }
    }

    return Command.SINGLE_SUCCESS;
  }

  private static int createEgg(CommandSourceStack source, ResourceLocation resourceLocation,
                               int tier,
                               Collection<IAffix> affixes) throws CommandSyntaxException {
    EntityType<?> entity = BuiltInRegistries.ENTITY_TYPE.get(resourceLocation);

    if (entity == null) {
      throw UNKNOWN_ENTITY.create(resourceLocation);
    } else if (source.getEntity() instanceof ServerPlayer playerEntity) {
      ItemStack egg = new ItemStack(ChampionsRegistry.CHAMPION_EGG_ITEM.get());
      ChampionEggItem.write(egg, resourceLocation, tier, affixes);
      ItemHandlerHelper.giveItemToPlayer(playerEntity, egg, 1);
      source.sendSuccess(
        () -> Component.translatable("commands.champions.egg.success", egg.getDisplayName()), false);
    }
    return Command.SINGLE_SUCCESS;
  }
}
