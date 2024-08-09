package top.theillusivec4.champions.common.network;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.capability.ChampionCapability;

public record SPacketSyncChampion(int entityId, int tier, int defaultColor, Set<String> affixes) implements CustomPacketPayload {

  public static final ResourceLocation ID = new ResourceLocation(Champions.MODID, "sync_champion");

  public SPacketSyncChampion(final FriendlyByteBuf buffer) {
    this(
      buffer.readInt(),
      buffer.readInt(),
      buffer.readInt(),
      IntStream.range(0, buffer.readInt())
        .mapToObj(i -> buffer.readUtf())
        .collect(Collectors.toSet())
    );
  }

  @Override
  public void write(final FriendlyByteBuf buffer) {
    buffer.writeInt(entityId);
    buffer.writeInt(tier);
    buffer.writeInt(defaultColor);
    buffer.writeInt(affixes.size());
    affixes.forEach(buffer::writeUtf);
  }

  @Override
  public ResourceLocation id() {
    return ID;
  }
  public static void handle(SPacketSyncChampion msg, PlayPayloadContext ctx) {
    ctx.workHandler().submitAsync(() -> {
      ClientLevel world = Minecraft.getInstance().level;

      if (world != null) {
        Entity entity = world.getEntity(msg.entityId);
        ChampionCapability.getCapability(entity).ifPresent(champion -> {
          IChampion.Client clientChampion = champion.getClient();
          clientChampion.setRank(new Tuple<>(msg.tier, msg.defaultColor));
          clientChampion.setAffixes(msg.affixes);
        });
      }
    });
  }
}
