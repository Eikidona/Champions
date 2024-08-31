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
import java.util.Set;

public record EntityIsChampion(Integer minTier, Integer maxTier,
                               LootContext.EntityTarget target) implements LootItemCondition {

  public static final Codec<EntityIsChampion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    Codec.INT.fieldOf("minTier").forGetter(EntityIsChampion::minTier),
    Codec.INT.fieldOf("maxTier").forGetter(EntityIsChampion::maxTier),
    LootContext.EntityTarget.CODEC.fieldOf("target").forGetter(EntityIsChampion::target)
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
        boolean aboveMin = minTier == null ? tier >= 1 : tier >= minTier;
        boolean belowMax = maxTier == null || tier <= maxTier;
        return aboveMin && belowMax;
      }).orElse(false);
    }
  }

  @Nonnull
  @Override
  public LootItemConditionType getType() {
    return ChampionsRegistry.ENTITY_IS_CHAMPION.get();
  }

  /*public static class Serializer
    implements net.minecraft.world.level.storage.loot.Serializer<EntityIsChampion> {

    @Override
    public void serialize(final JsonObject json, final EntityIsChampion value,
                          final JsonSerializationContext context) {
      json.addProperty("maxTier", value.maxTier);
      json.addProperty("minTier", value.minTier);
      json.add("entity", context.serialize(value.target));
    }

    @Nonnull
    @Override
    public EntityIsChampion deserialize(
      JsonObject json,
      @Nonnull JsonDeserializationContext context) {
      Integer minTier = json.has("minTier") ? GsonHelper.getAsInt(json, "minTier") : null;
      Integer maxTier = json.has("maxTier") ? GsonHelper.getAsInt(json, "maxTier") : null;

      return new EntityIsChampion(minTier, maxTier,
        GsonHelper.getAsObject(json, "entity", context, LootContext.EntityTarget.class));
    }
  }*/
}
