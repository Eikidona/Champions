package top.theillusivec4.champions.server.command;

import com.google.gson.JsonObject;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

public class AffixArgumentInfo implements ArgumentTypeInfo<AffixArgument, AffixArgumentInfo.Template> {

    @Override
    public void serializeToNetwork(Template template, FriendlyByteBuf buffer) {
        // 通常为空，除非你需要发送额外数据
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf buffer) {
        return new Template();
    }

    @Override
    public void serializeToJson(Template template, JsonObject json) {
        // 通常为空，除非你需要序列化额外数据
    }

    @Override
    public Template unpack(AffixArgument argument) {
        return new Template();
    }

    public class Template implements ArgumentTypeInfo.Template<AffixArgument> {
        @Override
        public AffixArgument instantiate(CommandBuildContext context) {
            return new AffixArgument();
        }

        @Override
        public ArgumentTypeInfo<AffixArgument, ?> type() {
            return AffixArgumentInfo.this;
        }
    }
}
