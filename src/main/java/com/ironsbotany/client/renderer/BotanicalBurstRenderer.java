package com.ironsbotany.client.renderer;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.entity.BotanicalBurstProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class BotanicalBurstRenderer extends EntityRenderer<BotanicalBurstProjectile> {
    private static final ResourceLocation TEXTURE =
        new ResourceLocation(IronsBotany.MODID, "textures/entity/botanical_burst.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);

    public BotanicalBurstRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BotanicalBurstProjectile entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float time = entity.tickCount + partialTicks;

        // --- Glow layer (behind, larger, softer) ---
        poseStack.pushPose();
        float glowScale = 0.8F;
        float glowPulse = 1.0F + (float) Math.sin(time * 0.3) * 0.15F;
        poseStack.scale(glowScale * glowPulse, glowScale * glowPulse, glowScale * glowPulse);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        PoseStack.Pose glowPose = poseStack.last();
        Matrix4f glowMatrix = glowPose.pose();
        Matrix3f glowNormal = glowPose.normal();
        VertexConsumer glowConsumer = buffer.getBuffer(RENDER_TYPE);

        // Glow quad — lower alpha, green-tinted
        vertex(glowConsumer, glowMatrix, glowNormal, LightTexture.FULL_BRIGHT, 0.0F, 0, 0, 1, 120, 255, 180, 100);
        vertex(glowConsumer, glowMatrix, glowNormal, LightTexture.FULL_BRIGHT, 1.0F, 0, 1, 1, 120, 255, 180, 100);
        vertex(glowConsumer, glowMatrix, glowNormal, LightTexture.FULL_BRIGHT, 1.0F, 1, 1, 0, 120, 255, 180, 100);
        vertex(glowConsumer, glowMatrix, glowNormal, LightTexture.FULL_BRIGHT, 0.0F, 1, 0, 0, 120, 255, 180, 100);
        poseStack.popPose();

        // --- Main projectile layer ---
        poseStack.pushPose();
        float mainScale = 0.5F;
        float mainPulse = 1.0F + (float) Math.sin(time * 0.5) * 0.1F;
        poseStack.scale(mainScale * mainPulse, mainScale * mainPulse, mainScale * mainPulse);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        // Z-axis spin
        poseStack.mulPose(Axis.ZP.rotationDegrees(time * 10.0F));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);

        // Main quad — fullbright, full white
        vertex(vertexConsumer, matrix4f, matrix3f, LightTexture.FULL_BRIGHT, 0.0F, 0, 0, 1, 255, 255, 255, 255);
        vertex(vertexConsumer, matrix4f, matrix3f, LightTexture.FULL_BRIGHT, 1.0F, 0, 1, 1, 255, 255, 255, 255);
        vertex(vertexConsumer, matrix4f, matrix3f, LightTexture.FULL_BRIGHT, 1.0F, 1, 1, 0, 255, 255, 255, 255);
        vertex(vertexConsumer, matrix4f, matrix3f, LightTexture.FULL_BRIGHT, 0.0F, 1, 0, 0, 255, 255, 255, 255);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, int light,
                                float x, int y, int u, int v, int r, int g, int b, int a) {
        consumer.vertex(pose, x - 0.5F, (float) y - 0.5F, 0.0F)
            .color(r, g, b, a)
            .uv((float) u, (float) v)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(light)
            .normal(normal, 0.0F, 1.0F, 0.0F)
            .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(BotanicalBurstProjectile entity) {
        return TEXTURE;
    }
}
