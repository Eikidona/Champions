package top.theillusivec4.champions.common.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.champions.common.capability.ChampionAttachment;
import top.theillusivec4.champions.common.rank.Rank;
import top.theillusivec4.champions.common.registry.ChampionsRegistry;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.Set;

public record EntityIsChampion(Optional<Integer> minTier, Optional<Integer> maxTier,
                               LootContext.EntityTarget target) implements LootItemCondition {

  public static final Codec<EntityIsChampion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    Codec.INT.optionalFieldOf("minTier").forGetter(EntityIsChampion::minTier),
    Codec.INT.optionalFieldOf("maxTier").forGetter(EntityIsChampion::maxTier),
    LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(EntityIsChampion::target)
  ).apply(instance, EntityIsChampion::new));

  @NotNull
  @Override
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return ImmutableSet.of(target.getParam());
  }

  @Override
  public boolean test(LootContext context) {
    Entity entity = context.getParamOrNull(target.getParam());

    if (entity == null) {
      return false;
    } else {
      return ChampionAttachment.getAttachment(entity).map(champion -> {
        int tier = champion.getServer().getRank().map(Rank::getTier).orElse(0);
        boolean aboveMin = minTier.map(integer -> tier >= integer).orElseGet(() -> tier >= 1);
        boolean belowMax = maxTier.isEmpty() || tier <= maxTier.get();
        return aboveMin && belowMax;
      }).orElse(false);
    }
  }

  @Nonnull
  @Override
  public LootItemConditionType getType() {
    return ChampionsRegistry.ENTITY_IS_CHAMPION.get();
  }
}
