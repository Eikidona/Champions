package top.theillusivec4.champions.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handlers.ClientPayloadHandler;
import net.neoforged.neoforge.network.handlers.ServerPayloadHandler;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import top.theillusivec4.champions.Champions;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkHandler {

  private static final String PTC_VERSION = "1";

  public static SimpleChannel INSTANCE;

  private static int id = 0;

  public static void register() {
    INSTANCE = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(Champions.MODID, "main"))
        .networkProtocolVersion(() -> PTC_VERSION).clientAcceptedVersions(PTC_VERSION::equals)
        .serverAcceptedVersions(PTC_VERSION::equals).simpleChannel();

    register(SPacketSyncChampion.class, SPacketSyncChampion::encode, SPacketSyncChampion::decode,
        SPacketSyncChampion::handle);
    register(SPacketSyncAffixData.class, SPacketSyncAffixData::encode, SPacketSyncAffixData::decode,
        SPacketSyncAffixData::handle);
  }

  private static <M> void register(Class<M> messageType, BiConsumer<M, FriendlyByteBuf> encoder,
                                   Function<FriendlyByteBuf, M> decoder,
                                   BiConsumer<M, Supplier<NetworkEvent.Context>> messageConsumer) {
    INSTANCE.registerMessage(id++, messageType, encoder, decoder, messageConsumer);
  }

  @SubscribeEvent
  public static void register(final RegisterPayloadHandlerEvent event) {
    final IPayloadRegistrar registrar = event.registrar(Champions.MODID);
    registrar.play(SPacketSyncChampion.ID,SPacketSyncChampion::new,handler->handler
      .client(ClientPayloadHandler.getInstance()::handle)
      .server(ServerPayloadHandler.getInstance()::handle)
    );
  }
}
