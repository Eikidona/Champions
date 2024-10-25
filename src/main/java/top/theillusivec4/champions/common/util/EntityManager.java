package top.theillusivec4.champions.common.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.api.IAffix;
import top.theillusivec4.champions.common.config.ChampionsConfig;
import top.theillusivec4.champions.common.config.ConfigEnums.Permission;
import top.theillusivec4.champions.common.config.EntitiesConfig.EntityConfig;

import javax.annotation.Nullable;
import java.util.*;

public class EntityManager {

  private static final Map<EntityType<?>, EntitySettings> SETTINGS = new HashMap<>();

  public static Optional<EntitySettings> getSettings(EntityType<?> type) {
    return Optional.ofNullable(SETTINGS.get(type));
  }

  public static void buildEntitySettings() {
    List<EntityConfig> configs = ChampionsConfig.entities;
    SETTINGS.clear();

    if (configs == null || configs.isEmpty()) {
      return;
    }

    configs.forEach(entityConfig -> {

      if (entityConfig.entity == null) {
        Champions.LOGGER.error("Missing identifier while building entity settings, skipping...");
        return;
      }
      var type = BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(entityConfig.entity));

      if (type.isEmpty()) {
        Champions.LOGGER.error("Invalid identifier while building entity settings, skipping...");
        return;
      }
      EntitySettings settings = new EntitySettings(type.get(), entityConfig.minTier, entityConfig.maxTier,
        entityConfig.presetAffixes, entityConfig.affixList, entityConfig.affixPermission);
      SETTINGS.put(type.get(), settings);
    });
  }

  public static class EntitySettings {

    final EntityType<?> entityType;
    @Nullable
    final Integer minTier;
    @Nullable
    final Integer maxTier;
    final List<IAffix> presetAffixes;
    final List<IAffix> affixList;
    final Permission affixPermission;

    public EntitySettings(EntityType<?> type, @Nullable Integer minTier, @Nullable Integer maxTier,
                          List<String> presetAffixes, List<String> affixList,
                          String affixPermission) {
      this.entityType = type;
      this.minTier = minTier;
      this.maxTier = maxTier;
      this.presetAffixes = new ArrayList<>();

      if (presetAffixes != null) {

        for (String s : presetAffixes) {
          Champions.API.getAffix(s).ifPresent(this.presetAffixes::add);
        }
      }
      this.affixList = new ArrayList<>();

      if (affixList != null) {

        for (String s : affixList) {
          Champions.API.getAffix(s).ifPresent(this.affixList::add);
        }
      }
      Permission permission = Permission.BLACKLIST;

      try {
        permission = Permission.valueOf(affixPermission);
      } catch (IllegalArgumentException e) {
        Champions.LOGGER.error("Invalid permission value {}", affixPermission);
      }
      this.affixPermission = permission;
    }

    public boolean canApply(IAffix affix) {
      boolean isValidAffix;

      if (affixPermission == Permission.BLACKLIST) {
        isValidAffix = !affixList.contains(affix);
      } else {
        isValidAffix = affixList.contains(affix);
      }
      return isValidAffix;
    }
  }
}
