package top.theillusivec4.champions.common.integration.jade;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;
import top.theillusivec4.champions.client.util.HUDHelper;

import java.util.Objects;

public class StarElement extends Element {
    private final int rank;
    private final int spacing;
    private final float r;
    private final float g;
    private final float b;

    public StarElement(final int rank, final String colorCode, int spacing) {
        int color = Objects.requireNonNull(TextColor.parseColor(colorCode)).getValue();
        this.rank = rank;
        this.spacing = spacing;
        r = FastColor.ARGB32.red(color) / 255.0F;
        g = FastColor.ARGB32.green(color) / 255.0F;
        b = FastColor.ARGB32.blue(color) / 255.0F;
    }

    public ResourceLocation getTexture() {
        return HUDHelper.getGuiStar();
    }

    @Override
    public Vec2 getSize() {
        // 返回固定大小 9x9
        return new Vec2(9, 9);
    }

    @Override
    public void render(GuiGraphics guiGraphics, float x, float y, float maxX, float maxY) {
        // 设置渲染状态
        RenderSystem.setShaderColor(r, g, b, 1.0F);

        // 渲染图标
        for (int i = 0; i < rank; i++) {
            guiGraphics.blit(
                    getTexture(),
                    (int) x + (9 + spacing) * i,      // 屏幕X位置
                    (int) y,      // 屏幕Y位置
                    0,           // UV的X坐标
                    0,           // UV的Y坐标
                    9,           // 渲染宽度
                    9,           // 渲染高度
                    9,           // 材质宽度
                    9           // 材质高度
            );
        }

        RenderSystem.setShaderColor(1F, 1F, 1F, 1.0F);

    }
}