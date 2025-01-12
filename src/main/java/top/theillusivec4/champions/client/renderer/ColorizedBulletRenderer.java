package top.theillusivec4.champions.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import top.theillusivec4.champions.Champions;
import top.theillusivec4.champions.common.entity.BaseBulletEntity;

import javax.annotation.Nonnull;

public class ColorizedBulletRenderer extends EntityRenderer<BaseBulletEntity> {

    private static final ResourceLocation GENERIC_SPARK_TEXTURE = new ResourceLocation(
            Champions.MODID, "textures/entity/generic_spark.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(GENERIC_SPARK_TEXTURE);
    private final ShulkerBulletModel<BaseBulletEntity> model;

    private final int color;

    public ColorizedBulletRenderer(EntityRendererProvider.Context manager, int color) {
        super(manager);
        this.color = color;
        this.model = new ShulkerBulletModel<>(manager.bakeLayer(ModelLayers.SHULKER_BULLET));
    }

    @Override
    protected int getBlockLightLevel(@Nonnull final BaseBulletEntity bullet,
                                     @Nonnull final BlockPos blockPos) {
        return 15;
    }

    @Override
    public void render(BaseBulletEntity entity, float entityYaw, float partialTicks,
                       PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();
        float yRot = Mth.rotLerp(entity.yRotO, entity.getYRot(), partialTicks);
        float xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        float tickModifier = (float) entity.tickCount + partialTicks;
        matrixStack.translate(0.0D, 0.15000000596046448D, 0.0D);
        matrixStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(tickModifier * 0.1F) * 180.0F));
        matrixStack.mulPose(Axis.XP.rotationDegrees(Mth.cos(tickModifier * 0.1F) * 180.0F));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(tickModifier * 0.15F) * 360.0F));
        float r = (float) ((this.color >> 16) & 0xFF) / 255F;
        float g = (float) ((this.color >> 8) & 0xFF) / 255F;
        float b = (float) ((this.color) & 0xFF) / 255F;
        matrixStack.scale(-0.5F, -0.5F, 0.5F);
        this.model.setupAnim(entity, 0.0F, 0.0F, 0.0F, yRot, xRot);
        VertexConsumer vertexconsumer = buffer.getBuffer(this.model.renderType(GENERIC_SPARK_TEXTURE));
        this.model.renderToBuffer(matrixStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY,
                r, g, b, 1.0F);
        matrixStack.scale(1.5F, 1.5F, 1.5F);
        VertexConsumer vertexconsumer1 = buffer.getBuffer(RENDER_TYPE);
        this.model.renderToBuffer(matrixStack, vertexconsumer1, packedLight, OverlayTexture.NO_OVERLAY,
                r, g, b, 0.15F);
        matrixStack.popPose();
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }

    @Nonnull
    @Override
    public ResourceLocation getTextureLocation(@Nonnull BaseBulletEntity entity) {
        return GENERIC_SPARK_TEXTURE;
    }
}
