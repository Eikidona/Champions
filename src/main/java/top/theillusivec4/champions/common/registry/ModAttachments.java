package top.theillusivec4.champions.common.registry;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.common.capability.ChampionAttachment;

public class ModAttachments {

  private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Champions.MODID);
  public static DeferredHolder<AttachmentType<?>, AttachmentType<ChampionAttachment.Provider>> CHAMPION_ATTACHMENT = ATTACHMENTS.register("champion_attachment", () -> AttachmentType.serializable(entity -> ChampionAttachment.createProvider((LivingEntity) entity)).build());

  public static void register(IEventBus bus) {
    ATTACHMENTS.register(bus);
  }
}
