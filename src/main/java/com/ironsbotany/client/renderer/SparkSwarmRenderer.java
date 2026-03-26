package com.ironsbotany.client.renderer;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.entity.SparkSwarmEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
        ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "textures/entity/spark_swarm.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);

    public SparkSwarmRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(SparkSwarmEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        
        // Scale smaller
        poseStack.scale(0.3F, 0.3F, 0.3F);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        
        // Pulsing effect
        float pulse = (float) Math.sin((entity.tickCount + partialTicks) * 0.3) * 0.1F + 1.0F;
        poseStack.scale(pulse, pulse, pulse);
        
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();
        
        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);
        
        // Draw quad with glow
        vertex(vertexConsumer, matrix4f, matrix3f, 240, 0.0F, 0, 0, 1); // Full bright
        vertex(vertexConsumer, matrix4f, matrix3f, 240, 1.0F, 0, 1, 1);
        vertex(vertexConsumer, matrix4f, matrix3f, 240, 1.0F, 1, 1, 0);
        vertex(vertexConsumer, matrix4f, matrix3f, 240, 0.0F, 1, 0, 0);
        
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, int light,
                                float x, int y, int u, int v) {
        consumer.vertex(pose, x - 0.5F, (float) y - 0.5F, 0.0F)
            .color(100, 255, 255, 200) // Cyan glow
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
