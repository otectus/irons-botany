package com.ironsbotany.client.renderer;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.block.entity.ManaConduitBlockEntity;
import com.ironsbotany.common.config.ClientConfig;
import com.ironsbotany.common.config.CommonConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class ManaConduitBER implements BlockEntityRenderer<ManaConduitBlockEntity> {
    private static final ResourceLocation TEXTURE =
        new ResourceLocation(IronsBotany.MODID, "textures/particle/soft_glow_0.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);
    private static final int ORBITAL_COUNT = 3;

    public ManaConduitBER(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(ManaConduitBlockEntity be, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (be.getLevel() == null) return;
        if (!ClientConfig.ENABLE_MANA_PARTICLES.get()) return;

        int capacity = Math.max(1, CommonConfig.MANA_CONDUIT_CAPACITY.get());
        float fill = Math.min(1.0F, be.getStoredMana() / (float) capacity);
        if (fill <= 0.001F) return;

        float time = be.getLevel().getGameTime() + partialTicks;

        poseStack.pushPose();
        float bob = (float) Math.sin(time * 0.1) * 0.04F;
        poseStack.translate(0.5F, 1.0F + bob, 0.5F);

        renderGlow(poseStack, buffer, time, fill);
        renderCore(poseStack, buffer, time, fill);
        renderOrbitals(poseStack, buffer, time, fill);

        poseStack.popPose();
    }

    private void renderGlow(PoseStack poseStack, MultiBufferSource buffer, float time, float fill) {
        poseStack.pushPose();
        float pulse = 1.0F + (float) Math.sin(time * 0.2) * 0.14F;
        float scale = 0.5F * pulse * (0.5F + 0.5F * fill);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        PoseStack.Pose pose = poseStack.last();
        VertexConsumer vc = buffer.getBuffer(RENDER_TYPE);

        // Violet-cyan color cycling
        float phase = time * 0.08F;
        int r = (int) (140 + Math.sin(phase) * 60);
        int g = (int) (120 + Math.sin(phase + 1.2) * 40);
        int b = 255;
        int alpha = (int) (85 * fill);
        quad(vc, pose.pose(), pose.normal(), LightTexture.FULL_BRIGHT, r, g, b, alpha);
        poseStack.popPose();
    }

    private void renderCore(PoseStack poseStack, MultiBufferSource buffer, float time, float fill) {
        poseStack.pushPose();
        float pulse = 1.0F + (float) Math.sin(time * 0.32) * 0.08F;
        float scale = 0.26F * pulse * (0.6F + 0.4F * fill);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        PoseStack.Pose pose = poseStack.last();
        VertexConsumer vc = buffer.getBuffer(RENDER_TYPE);

        int alpha = (int) (220 * (0.4F + 0.6F * fill));
        quad(vc, pose.pose(), pose.normal(), LightTexture.FULL_BRIGHT, 200, 200, 255, alpha);
        poseStack.popPose();
    }

    private void renderOrbitals(PoseStack poseStack, MultiBufferSource buffer, float time, float fill) {
        float radius = 0.28F + 0.06F * fill;
        for (int i = 0; i < ORBITAL_COUNT; i++) {
            poseStack.pushPose();

            float angle = time * 2.8F + (i * (360.0F / ORBITAL_COUNT));
            double rad = Math.toRadians(angle);
            float ox = (float) (Math.cos(rad) * radius);
            float oy = (float) (Math.sin(time * 1.3 + i * 2.0) * 0.08F);
            float oz = (float) (Math.sin(rad) * radius);

            poseStack.translate(ox, oy, oz);
            poseStack.scale(0.09F, 0.09F, 0.09F);
            poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

            PoseStack.Pose pose = poseStack.last();
            VertexConsumer vc = buffer.getBuffer(RENDER_TYPE);

            int sparkAlpha = (int) (230 * fill);
            quad(vc, pose.pose(), pose.normal(), LightTexture.FULL_BRIGHT, 170, 220, 255, sparkAlpha);
            poseStack.popPose();
        }
    }

    private static void quad(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, int light,
                             int r, int g, int b, int a) {
        vertex(consumer, pose, normal, light, 0.0F, 0, 0, 1, r, g, b, a);
        vertex(consumer, pose, normal, light, 1.0F, 0, 1, 1, r, g, b, a);
        vertex(consumer, pose, normal, light, 1.0F, 1, 1, 0, r, g, b, a);
        vertex(consumer, pose, normal, light, 0.0F, 1, 0, 0, r, g, b, a);
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
    public int getViewDistance() {
        return 48;
    }
}
