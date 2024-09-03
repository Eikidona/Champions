package top.theillusivec4.champions.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.capability.ChampionAttachment;

import java.util.Set;

public record SPacketSyncChampion(int entityId, int tier, int defaultColor,
                                  Set<String> affixes) implements CustomPacketPayload {

  public static final ResourceLocation ID = new ResourceLocation(Champions.MODID, "sync_champion");

  public SPacketSyncChampion(final FriendlyByteBuf buffer) {
    this(buffer.readInt(), buffer.readInt(), buffer.readInt(), Set.copyOf(buffer.readList(FriendlyByteBuf::readUtf)));
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeInt(this.entityId);
    buffer.writeInt(this.tier);
    buffer.writeInt(this.affixes.size());
    buffer.writeInt(this.defaultColor);
    this.affixes.forEach(buffer::writeUtf);
  }

  @Override
  @NotNull
  public ResourceLocation id() {
    return ID;
  }

  public static class ChampionHandler {

    private static final ChampionHandler INSTANCE = new ChampionHandler();

    public static ChampionHandler getInstance() {
      return INSTANCE;
    }

    public void handle(final SPacketSyncChampion data, final PlayPayloadContext cxt) {
      cxt.workHandler().submitAsync(() -> {
        ClientLevel world = Minecraft.getInstance().level;

        if (world != null) {
          Entity entity = world.getEntity(data.entityId);
          ChampionAttachment.getAttachment(entity).ifPresent(champion -> {
            IChampion.Client clientChampion = champion.getClient();
            clientChampion.setRank(new Tuple<>(data.tier, data.defaultColor));
            clientChampion.setAffixes(data.affixes);
          });
        }
      });
    }
  }
}
