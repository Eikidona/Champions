package top.theillusivec4.champions.common;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.client.ChampionsOverlay;
import top.theillusivec4.champions.common.capability.ChampionAttachment;
import top.theillusivec4.champions.common.config.ChampionsConfig;
import top.theillusivec4.champions.common.rank.Rank;
import top.theillusivec4.champions.common.registry.ChampionsRegistry;
import top.theillusivec4.champions.common.util.ChampionBuilder;
import top.theillusivec4.champions.common.util.ChampionHelper;

import java.util.List;
import java.util.Optional;

public class ChampionEventsHandler {
  @SubscribeEvent
  public void onLivingXpDrop(LivingExperienceDropEvent evt) {
    LivingEntity livingEntity = evt.getEntity();
    ChampionAttachment.getAttachment(livingEntity).flatMap(champion -> champion.getServer().getRank()).ifPresent(rank -> {
      int growth = rank.getGrowthFactor();

      if (growth > 0) {
        evt.setDroppedExperience(
          (growth * ChampionsConfig.experienceGrowth * evt.getOriginalExperience() +
            evt.getDroppedExperience()));
      }
    });
  }

  @SubscribeEvent
  public void onExplosion(ExplosionEvent.Start evt) {
    Explosion explosion = evt.getExplosion();
    Entity entity = explosion.getDirectSourceEntity();

    if (entity != null && !entity.level().isClientSide()) {
      ChampionAttachment.getAttachment(entity)
        .ifPresent(champion -> champion.getServer().getRank().ifPresent(rank -> {
          int growth = rank.getGrowthFactor();

          if (growth > 0) {
            explosion.radius += ChampionsConfig.explosionGrowth * growth;
          }
        }));
    }
  }

  @SubscribeEvent
  public void onLivingJoinWorld(EntityJoinLevelEvent evt) {
    Entity entity = evt.getEntity();

    if (!entity.level().isClientSide()) {
      ChampionAttachment.getAttachment(entity).ifPresent(champion -> {
        IChampion.Server serverChampion = champion.getServer();
        Optional<Rank> maybeRank = serverChampion.getRank();

        if (maybeRank.isEmpty()) {
          ChampionBuilder.spawn(champion);
        }
        serverChampion.getAffixes().forEach(affix -> affix.onSpawn(champion));
        serverChampion.getRank().ifPresent(rank -> {
          List<Tuple<MobEffect, Integer>> effects = rank.getEffects();
          effects.forEach(effectPair -> champion.getLivingEntity()
            .addEffect(new MobEffectInstance(effectPair.getA(), 200, effectPair.getB())));
        });
      });
    }
  }

  @SubscribeEvent
  public void onLivingUpdate(LivingEvent.LivingTickEvent evt) {
    LivingEntity livingEntity = evt.getEntity();

    if (livingEntity.level().isClientSide()) {
      ChampionAttachment.getAttachment(livingEntity).ifPresent(champion -> {
        IChampion.Client clientChampion = champion.getClient();
        clientChampion.getAffixes().forEach(affix -> affix.onClientUpdate(champion));
        clientChampion.getRank().ifPresent(rank -> {
          if (ChampionsConfig.showParticles && rank.getA() > 0) {
            int color = rank.getB();
            float r = (float) ((color >> 16) & 0xFF) / 255f;
            float g = (float) ((color >> 8) & 0xFF) / 255f;
            float b = (float) ((color) & 0xFF) / 255f;

            livingEntity.level().addParticle(ChampionsRegistry.RANK_PARTICLE_TYPE.get(),
              livingEntity.position().x + (livingEntity.getRandom().nextDouble() - 0.5D) *
                (double) livingEntity.getBbWidth(), livingEntity.position().y +
                livingEntity.getRandom().nextDouble() * livingEntity.getBbHeight(),
              livingEntity.position().z + (livingEntity.getRandom().nextDouble() - 0.5D) *
                (double) livingEntity.getBbWidth(), r, g, b);
          }
        });
      });
    } else if (livingEntity.tickCount % 10 == 0) {
      ChampionAttachment.getAttachment(livingEntity).ifPresent(champion -> {
        IChampion.Server serverChampion = champion.getServer();
        serverChampion.getAffixes().forEach(affix -> affix.onServerUpdate(champion));
        serverChampion.getRank().ifPresent(rank -> {
          if (livingEntity.tickCount % 4 == 0) {
            List<Tuple<MobEffect, Integer>> effects = rank.getEffects();
            effects.forEach(effectPair -> livingEntity.addEffect(
              new MobEffectInstance(effectPair.getA(), 100, effectPair.getB())));
          }
        });
      });
    }
  }

  @SubscribeEvent
  public void onLivingAttack(LivingAttackEvent evt) {
    LivingEntity livingEntity = evt.getEntity();

    if (livingEntity.level().isClientSide()) {
      return;
    }
    ChampionAttachment.getAttachment(livingEntity).ifPresent(champion -> {
      IChampion.Server serverChampion = champion.getServer();
      serverChampion.getAffixes().forEach(affix -> {

        if (!affix.onAttacked(champion, evt.getSource(), evt.getAmount())) {
          evt.setCanceled(true);
        }
      });
    });

    if (evt.isCanceled()) {
      return;
    }
    Entity source = evt.getSource().getDirectEntity();
    ChampionAttachment.getAttachment(source).ifPresent(champion -> {
      IChampion.Server serverChampion = champion.getServer();
      serverChampion.getAffixes().forEach(affix -> {

        if (!affix.onAttack(champion, evt.getEntity(), evt.getSource(), evt.getAmount())) {
          evt.setCanceled(true);
        }
      });
    });
  }

  @SubscribeEvent
  public void onLivingHurt(LivingHurtEvent evt) {
    LivingEntity livingEntity = evt.getEntity();

    if (!livingEntity.level().isClientSide()) {
      float[] amounts = new float[]{evt.getAmount(), evt.getAmount()};
      ChampionAttachment.getAttachment(livingEntity).ifPresent(champion -> {
        IChampion.Server serverChampion = champion.getServer();
        serverChampion.getAffixes().forEach(
          affix -> amounts[1] = affix.onHurt(champion, evt.getSource(), amounts[0], amounts[1]));
      });
      evt.setAmount(amounts[1]);
    }
  }

  @SubscribeEvent
  public void onLivingDamage(LivingDamageEvent evt) {
    LivingEntity livingEntity = evt.getEntity();

    if (!livingEntity.level().isClientSide()) {
      float[] amounts = new float[]{evt.getAmount(), evt.getAmount()};
      ChampionAttachment.getAttachment(livingEntity).ifPresent(champion -> {
        IChampion.Server serverChampion = champion.getServer();
        serverChampion.getAffixes().forEach(affix -> amounts[1] = affix
          .onDamage(champion, evt.getSource(), amounts[0], amounts[1]));
      });
      evt.setAmount(amounts[1]);
    }
  }

  @SubscribeEvent
  public void onLivingDeath(LivingDeathEvent evt) {
    LivingEntity livingEntity = evt.getEntity();

    if (livingEntity.level().isClientSide()) {
      return;
    }
    ChampionAttachment.getAttachment(livingEntity).ifPresent(champion -> {
      IChampion.Server serverChampion = champion.getServer();
      serverChampion.getAffixes().forEach(affix -> {

        if (!affix.onDeath(champion, evt.getSource())) {
          evt.setCanceled(true);
        }
      });
      serverChampion.getRank().ifPresent(rank -> {
        if (!evt.isCanceled()) {
          Entity source = evt.getSource().getEntity();

          if (source instanceof ServerPlayer player && !(source instanceof FakePlayer)) {
            player.awardStat(ChampionsRegistry.CHAMPION_MOBS_KILLED.get());
            int messageTier = ChampionsConfig.deathMessageTier;

            if (messageTier > 0 && rank.getTier() >= messageTier) {
              MinecraftServer server = livingEntity.getServer();

              if (server != null) {
                server.getPlayerList().broadcastSystemMessage(
                  Component.translatable("rank.champions.title." + rank.getTier())
                    .append(" ")
                    .append(livingEntity.getCombatTracker().getDeathMessage())
                  , false);
              }
            }
          }
        }
      });
    });
  }

  @SubscribeEvent
  public void onServerStart(ServerAboutToStartEvent evt) {
    ChampionHelper.setServer(evt.getServer());
  }

  @SubscribeEvent
  public void onServerClose(ServerStoppedEvent evt) {
    ChampionHelper.setServer(null);
    ChampionHelper.clearBeacons();
  }

  /*@SubscribeEvent
  public void onBeaconStart(AttachCapabilitiesEvent<BlockEntity> evt) {
    BlockEntity blockEntity = evt.getObject();

    if (blockEntity instanceof BeaconBlockEntity) {
      ChampionHelper.addBeacon(blockEntity.getBlockPos());
    }
  }*/

  @SubscribeEvent
  public void onLivingHeal(LivingHealEvent evt) {
    LivingEntity livingEntity = evt.getEntity();

    if (!livingEntity.level().isClientSide()) {
      float[] amounts = new float[]{evt.getAmount(), evt.getAmount()};
      ChampionAttachment.getAttachment(livingEntity).ifPresent(champion -> {
        IChampion.Server serverChampion = champion.getServer();
        serverChampion.getAffixes()
          .forEach(affix -> amounts[1] = affix.onHeal(champion, amounts[0], amounts[1]));
      });
      evt.setAmount(amounts[1]);
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onBossBarEvent(final CustomizeGuiOverlayEvent.BossEventProgress evt) {
    if (ChampionsOverlay.isRendering) {
      evt.setCanceled(true);
    }
  }
}
