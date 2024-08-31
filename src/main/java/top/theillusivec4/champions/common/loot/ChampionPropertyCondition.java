package top.theillusivec4.champions.common.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import top.theillusivec4.champions.api.IAffix;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.capability.ChampionAttachment;
import top.theillusivec4.champions.common.rank.Rank;
import top.theillusivec4.champions.common.registry.ChampionsRegistry;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record ChampionPropertyCondition(LootContext.EntityTarget target,
                                        MinMaxBounds.Ints tier, AffixesPredicate affixes)
  implements LootItemCondition {

  public static final Codec<ChampionPropertyCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    LootContext.EntityTarget.CODEC.fieldOf("target").forGetter(ChampionPropertyCondition::target),
    MinMaxBounds.Ints.CODEC.fieldOf("tier").forGetter(ChampionPropertyCondition::tier),
    AffixesPredicate.CODEC.fieldOf("affixes").forGetter(ChampionPropertyCondition::affixes)
  ).apply(instance, ChampionPropertyCondition::new));

  @Nonnull
  @Override
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return ImmutableSet.of(this.target.getParam());
  }

  @Override
  public boolean test(LootContext context) {
    Entity entity = context.getParamOrNull(this.target.getParam());
    return ChampionAttachment.getAttachment(entity).map(champion -> {
      IChampion.Server server = champion.getServer();
      int tier = server.getRank().map(Rank::getTier).orElse(0);

      if (tier <= 0 || !this.tier.matches(tier)) {
        return false;
      }
      List<IAffix> affixes = server.getAffixes();
      return this.affixes.matches(affixes);
    }).orElse(false);
  }

  @Nonnull
  @Override
  public LootItemConditionType getType() {
    return ChampionsRegistry.CHAMPION_PROPERTIES.get();
  }

  /*public static class ChampionConditionSerializer
    implements Serializer<ChampionPropertyCondition> {

    @Override
    public void serialize(final JsonObject json, final ChampionPropertyCondition value,
                          final JsonSerializationContext context) {
      json.add("tier", value.tier.serializeToJson());
      json.add("affixes", value.affixes.serializeToJson());
      json.add("entity", context.serialize(value.target));
    }

    @Nonnull
    @Override
    public ChampionPropertyCondition deserialize(JsonObject json, @Nonnull
    JsonDeserializationContext context) {
      MinMaxBounds.Ints tier = MinMaxBounds.Ints.fromJson(json.get("tier"));
      AffixesPredicate affixes = AffixesPredicate.fromJson(json.get("affixes"));
      return new ChampionPropertyCondition(
        GsonHelper.getAsObject(json, "entity", context, LootContext.EntityTarget.class), tier,
        affixes);
    }
  }*/

  private record AffixesPredicate(Set<String> values, MinMaxBounds.Ints matches,
                                  MinMaxBounds.Ints count) {

    public static final Codec<AffixesPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      NeoForgeExtraCodecs.setOf(Codec.STRING).fieldOf("values").forGetter(AffixesPredicate::values),
      MinMaxBounds.Ints.CODEC.fieldOf("matches").forGetter(AffixesPredicate::matches),
      MinMaxBounds.Ints.CODEC.fieldOf("count").forGetter(AffixesPredicate::count)
    ).apply(instance, AffixesPredicate::new));
    private static final AffixesPredicate ANY =
      new AffixesPredicate(new HashSet<>(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY);

    @Deprecated
    private static AffixesPredicate fromJson(JsonElement json) throws CommandSyntaxException {

      if (json != null && !json.isJsonNull()) {

        if (json.isJsonArray()) {
          JsonArray jsonArray = GsonHelper.convertToJsonArray(json, "affixes");
          Set<String> affixes = new HashSet<>();

          for (JsonElement jsonElement : jsonArray) {

            if (jsonElement.isJsonPrimitive()) {
              affixes.add(jsonElement.getAsString());
            }
          }
          return new AffixesPredicate(affixes, MinMaxBounds.Ints.atLeast(1), MinMaxBounds.Ints.ANY);
        } else {
          JsonObject jsonObject = json.getAsJsonObject();
          Set<String> affixes = new HashSet<>();

          if (jsonObject.has("values")) {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "values");

            for (JsonElement jsonElement : jsonArray) {

              if (jsonElement.isJsonPrimitive()) {
                affixes.add(jsonElement.getAsString());
              }
            }
          }
          MinMaxBounds.Ints matches = MinMaxBounds.Ints.atLeast(1);

          if (jsonObject.has("matches")) {
            matches = MinMaxBounds.Ints.fromReader(new StringReader(jsonObject.get("matches").getAsString()));
          }
          MinMaxBounds.Ints count = MinMaxBounds.Ints.ANY;

          if (jsonObject.has("count")) {
            count = MinMaxBounds.Ints.fromReader(new StringReader(jsonObject.get("count").getAsString()));
          }
          return new AffixesPredicate(affixes, matches, count);
        }
      }
      return ANY;
    }

    private boolean matches(List<IAffix> input) {

      if (this.values.isEmpty()) {
        return this.count.matches(input.size());
      } else {
        Set<String> affixes = input.stream().map(IAffix::getIdentifier).collect(Collectors.toSet());
        int found = 0;

        for (String affix : this.values) {

          if (affixes.contains(affix)) {
            found++;
          }
        }
        return this.matches.matches(found) && this.count.matches(input.size());
      }
    }

    @Deprecated
    public JsonElement serializeToJson() {
      if (this.values.isEmpty() && this.count.isAny() && this.matches.isAny()) {
        return JsonNull.INSTANCE;
      } else {
//        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        for (String value : this.values) {
          jsonArray.add(value);
        }
        Integer min = this.count.min().orElse(null);
        Integer max = this.count.max().orElse(null);

        if (min != null && min == 1 && max == null) {
          return jsonArray;
        }
//        jsonObject.add("values", jsonArray);
//        jsonObject.add("matches", this.matches.serializeToJson());
//        jsonObject.add("count", this.count.serializeToJson());
        return CODEC.encodeStart(JsonOps.INSTANCE, new AffixesPredicate(values, matches, count)).get().left().orElse(JsonNull.INSTANCE);
      }
    }
  }
}
