package com.ironsbotany.client.renderer;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.entity.SparkSwarmEntity;
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
public class SparkSwarmRenderer extends EntityRenderer<SparkSwarmEntity> {
    private static final ResourceLocation TEXTURE =
        new ResourceLocation(IronsBotany.MODID, "textures/entity/spark_swarm.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);
    private static final int ORBITAL_COUNT = 3;

    public SparkSwarmRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(SparkSwarmEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float time = entity.tickCount + partialTicks;

        // --- Glow layer (behind, larger) ---
        poseStack.pushPose();
        float glowPulse = 1.0F + (float) Math.sin(time * 0.2) * 0.12F
                                + (float) Math.sin(time * 0.7) * 0.06F;
        poseStack.scale(0.5F * glowPulse, 0.5F * glowPulse, 0.5F * glowPulse);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        PoseStack.Pose glowPose = poseStack.last();
        VertexConsumer glowConsumer = buffer.getBuffer(RENDER_TYPE);

        // Color cycling for glow
        float colorPhase = time * 0.1F;
        int gr = (int) (80 + Math.sin(colorPhase) * 40);
        int gg = (int) (220 + Math.sin(colorPhase + 1.0) * 35);
        int gb = (int) (255 - Math.sin(colorPhase) * 20);

        drawQuad(glowConsumer, glowPose.pose(), glowPose.normal(), LightTexture.FULL_BRIGHT, gr, gg, gb, 80);
        poseStack.popPose();

        // --- Main body ---
        poseStack.pushPose();
        float mainPulse = 1.0F + (float) Math.sin(time * 0.3) * 0.1F
                                + (float) Math.sin(time * 0.9) * 0.05F;
        poseStack.scale(0.3F * mainPulse, 0.3F * mainPulse, 0.3F * mainPulse);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        PoseStack.Pose mainPose = poseStack.last();
        VertexConsumer mainConsumer = buffer.getBuffer(RENDER_TYPE);

        // Color cycling for main body
        int mr = (int) (100 + Math.sin(colorPhase) * 50);
        int mg = (int) (255 - Math.sin(colorPhase) * 30);
        int mb = 255;

        drawQuad(mainConsumer, mainPose.pose(), mainPose.normal(), LightTexture.FULL_BRIGHT, mr, mg, mb, 200);
        poseStack.popPose();

        // --- Orbital sparks ---
        for (int i = 0; i < ORBITAL_COUNT; i++) {
            poseStack.pushPose();

            float orbitalAngle = time * 2.5F + (i * (360.0F / ORBITAL_COUNT));
            float orbitalRadius = 0.35F + (float) Math.sin(time * 0.5 + i) * 0.05F;
            double rad = Math.toRadians(orbitalAngle);
            float ox = (float) (Math.cos(rad) * orbitalRadius);
            float oy = (float) (Math.sin(time * 1.5 + i * 2.0) * 0.15F);
            float oz = (float) (Math.sin(rad) * orbitalRadius);

            poseStack.translate(ox, oy, oz);
            poseStack.scale(0.1F, 0.1F, 0.1F);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

            PoseStack.Pose orbPose = poseStack.last();
            VertexConsumer orbConsumer = buffer.getBuffer(RENDER_TYPE);

            // Bright cyan-white for orbital sparks
            int or = (int) (180 + Math.sin(time * 3.0 + i) * 75);
            drawQuad(orbConsumer, orbPose.pose(), orbPose.normal(), LightTexture.FULL_BRIGHT, or, 255, 255, 220);

            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void drawQuad(VertexConsumer consumer, Matrix4f pose, Matrix3f normal,
                                  int light, int r, int g, int b, int a) {
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
    public ResourceLocation getTextureLocation(SparkSwarmEntity entity) {
        return TEXTURE;
    }
}
