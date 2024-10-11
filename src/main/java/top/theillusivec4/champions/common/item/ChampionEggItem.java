package top.theillusivec4.champions.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.api.IAffix;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.capability.ChampionAttachment;
import top.theillusivec4.champions.common.registry.ModDataComponents;
import top.theillusivec4.champions.common.util.ChampionBuilder;

import javax.annotation.Nonnull;
import java.util.*;

public class ChampionEggItem extends EggItem {

  private static final String ID_TAG = "Id";
  private static final String ENTITY_TAG = "EntityTag";
  private static final String TIER_TAG = "Tier";
  private static final String AFFIX_TAG = "Affix";
  private static final String CHAMPION_TAG = "Champion";

  public ChampionEggItem() {
    super(new Item.Properties());
  }

  public static int getColor(ItemStack stack, int tintIndex) {
    SpawnEggItem eggItem =
      SpawnEggItem.byId(getType(stack).orElse(EntityType.ZOMBIE));
    return eggItem != null ? FastColor.ARGB32.opaque(eggItem.getColor(tintIndex)) : 0;
  }

  public static Optional<EntityType<?>> getType(ItemStack stack) {

    if (stack.has(ModDataComponents.ENTITY_TAG_COMPONENT)) {
      CompoundTag entityTag = stack.get(ModDataComponents.ENTITY_TAG_COMPONENT);

      if (entityTag != null) {
        String id = entityTag.getCompound(ENTITY_TAG).getString(ID_TAG);

        if (!id.isEmpty()) {
          EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id));

          return Optional.of(type);
        }
      }
    }
    return Optional.empty();
  }

  public static void read(IChampion champion, ItemStack stack) {

    if (stack.has(ModDataComponents.ENTITY_TAG_COMPONENT)) {
      CompoundTag tag = stack.get(ModDataComponents.ENTITY_TAG_COMPONENT);

      if (tag != null) {
        int tier = tag.getCompound(CHAMPION_TAG).getInt(TIER_TAG);
        ListTag listNBT = tag.getList(AFFIX_TAG, CompoundTag.TAG_STRING);
        List<IAffix> affixes = new ArrayList<>();
        listNBT.forEach(
          affix -> Champions.API.getAffix(affix.getAsString()).ifPresent(affixes::add));
        ChampionBuilder.spawnPreset(champion, tier, affixes);
      }
    }
  }

  public static void write(
    ItemStack stack, ResourceLocation entityId, int tier,
    Collection<IAffix> affixes) {
    CompoundTag tag = stack.getOrDefault(ModDataComponents.ENTITY_TAG_COMPONENT, new CompoundTag());

    CompoundTag compoundNBT = new CompoundTag();
    compoundNBT.putString(ID_TAG, entityId.toString());
    tag.put(ENTITY_TAG, compoundNBT);

    CompoundTag compoundNBT1 = new CompoundTag();
    compoundNBT1.putInt(TIER_TAG, tier);
    ListTag listNBT = new ListTag();
    affixes.forEach(affix -> listNBT.add(StringTag.valueOf(affix.getIdentifier())));
    compoundNBT1.put(AFFIX_TAG, listNBT);
    tag.put(CHAMPION_TAG, compoundNBT1);
    stack.set(ModDataComponents.ENTITY_TAG_COMPONENT, tag);
  }

  @Nonnull
  @Override
  public Component getName(@Nonnull ItemStack stack) {
    int tier = 0;
    Optional<EntityType<?>> type = getType(stack);

    if (stack.has(ModDataComponents.ENTITY_TAG_COMPONENT)) {
      CompoundTag tag = stack.get(ModDataComponents.ENTITY_TAG_COMPONENT);

      if (tag != null) {
        tier = tag.getCompound(CHAMPION_TAG).getInt(TIER_TAG);
      }
    }
    MutableComponent root = Component.translatable("rank.champions.title." + tier);
    root.append(" ");
    root.append(type.map(EntityType::getDescription).orElse(EntityType.ZOMBIE.getDescription()));
    root.append(" ");
    root.append(this.getDescription());
    return root;
  }

  @Override
  public void appendHoverText(ItemStack stack,@Nonnull TooltipContext pContext,
                              @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn) {
    boolean hasAffix = false;

    if (stack.has(ModDataComponents.ENTITY_TAG_COMPONENT)) {
      CompoundTag tag = stack.get(ModDataComponents.ENTITY_TAG_COMPONENT);

      if (tag != null) {
        ListTag listNBT = tag.getCompound(CHAMPION_TAG).getList(AFFIX_TAG, CompoundTag.TAG_STRING);

        if (!listNBT.isEmpty()) {
          hasAffix = true;
        }

        listNBT.forEach(affix -> Champions.API.getAffix(affix.getAsString()).ifPresent(
          affix1 -> {
            final MutableComponent component =
              Component.translatable("affix.champions." + affix1.getIdentifier());
            component.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
            tooltip.add(component);
          }));
      }
    }

    if (!hasAffix) {
      final MutableComponent component = Component.translatable("item.champions.egg.tooltip");
      component.setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA));
      tooltip.add(component);
    }
  }

  @Nonnull
  @Override
  public InteractionResult useOn(UseOnContext context) {
    Level world = context.getLevel();

    if (!world.isClientSide() && world instanceof ServerLevel) {
      ItemStack itemstack = context.getItemInHand();
      BlockPos blockpos = context.getClickedPos();
      Direction direction = context.getClickedFace();
      BlockState blockstate = world.getBlockState(blockpos);
      BlockPos blockpos1;

      if (blockstate.getCollisionShape(world, blockpos).isEmpty()) {
        blockpos1 = blockpos;
      } else {
        blockpos1 = blockpos.relative(direction);
      }
      Optional<EntityType<?>> entitytype = getType(itemstack);
      entitytype.ifPresent(type -> {
        Entity entity = type
          .create((ServerLevel) world, null, blockpos1,
            MobSpawnType.SPAWN_EGG, true,
            !Objects.equals(blockpos, blockpos1) && direction == Direction.UP);

        if (entity instanceof LivingEntity livingEntity) {
          ChampionAttachment.getAttachment(livingEntity).ifPresent(iChampion -> {
            read(iChampion, itemstack);
            world.addFreshEntity(entity);
            itemstack.shrink(1);
          });
        }
      });
    }
    return InteractionResult.SUCCESS;
  }

  @Nonnull
  @Override
  public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn,
                                                @Nonnull InteractionHand handIn) {
    ItemStack itemstack = playerIn.getItemInHand(handIn);

    if (worldIn.isClientSide()) {
      return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
    } else if (worldIn instanceof ServerLevel) {
      BlockHitResult raytraceresult = getPlayerPOVHitResult(worldIn, playerIn,
        ClipContext.Fluid.SOURCE_ONLY);

      if (raytraceresult.getType() != HitResult.Type.BLOCK) {
        return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
      } else {
        BlockPos blockpos = raytraceresult.getBlockPos();

        if (!(worldIn.getFluidState(blockpos).getType() instanceof FlowingFluid)) {
          return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
        } else if (worldIn.mayInteract(playerIn, blockpos) && playerIn
          .mayUseItemAt(blockpos, raytraceresult.getDirection(), itemstack)) {
          Optional<EntityType<?>> entityType = getType(itemstack);
          return entityType.map(type -> {
            Entity entity = type
              .create((ServerLevel) worldIn, null, blockpos,
                MobSpawnType.SPAWN_EGG, false, false);

            if (entity instanceof LivingEntity) {
              ChampionAttachment.getAttachment(entity).ifPresent(iChampion -> read(iChampion, itemstack));
              worldIn.addFreshEntity(entity);

              if (!playerIn.getAbilities().invulnerable) {
                itemstack.shrink(1);
              }
              playerIn.awardStat(Stats.ITEM_USED.get(this));
              return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
            } else {
              return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
            }
          }).orElse(new InteractionResultHolder<>(InteractionResult.PASS, itemstack));
        } else {
          return new InteractionResultHolder<>(InteractionResult.FAIL, itemstack);
        }
      }
    }
    return new InteractionResultHolder<>(InteractionResult.FAIL, itemstack);
  }
}
