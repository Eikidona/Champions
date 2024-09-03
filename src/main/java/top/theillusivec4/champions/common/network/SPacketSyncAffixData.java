package top.theillusivec4.champions.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.api.IChampion;
import top.theillusivec4.champions.common.capability.ChampionAttachment;

public record SPacketSyncAffixData(int entityId, String affixId, CompoundTag data) implements CustomPacketPayload {

  public static final ResourceLocation ID = new ResourceLocation(Champions.MODID, "main");

  public SPacketSyncAffixData(final FriendlyByteBuf buffer) {
    this(buffer.readInt(), buffer.readUtf(), buffer.readNbt());
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeInt(this.entityId);
    buffer.writeUtf(this.affixId);
    buffer.writeNbt(this.data);
  }

  @Override
  public ResourceLocation id() {
    return ID;
  }

  public static class AffixDataHandler {

    private static final AffixDataHandler INSTANCE = new AffixDataHandler();

    public static AffixDataHandler getInstance() {
      return INSTANCE;
    }

    public void handle(final SPacketSyncAffixData data, final PlayPayloadContext cxt) {
      cxt.workHandler().submitAsync(() -> {
        ClientLevel world = Minecraft.getInstance().level;

        if (world != null) {
          Entity entity = world.getEntity(data.entityId);
          ChampionAttachment.getAttachment(entity).ifPresent(champion -> {
            IChampion.Client clientChampion = champion.getClient();
            clientChampion.getAffix(data.affixId)
              .ifPresent(affix -> affix.readSyncTag(champion, data.data));
          });
        }
      });
    }
  }
}

