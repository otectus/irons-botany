package com.ironsbotany.client.renderer;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.block.entity.SpellReservoirBlockEntity;
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
public class SpellReservoirBER implements BlockEntityRenderer<SpellReservoirBlockEntity> {
    private static final ResourceLocation TEXTURE =
        new ResourceLocation(IronsBotany.MODID, "textures/particle/soft_glow_0.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);

    public SpellReservoirBER(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(SpellReservoirBlockEntity be, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (be.getLevel() == null) return;
        if (!ClientConfig.ENABLE_MANA_PARTICLES.get()) return;

        int capacity = Math.max(1, CommonConfig.SPELL_RESERVOIR_CAPACITY.get());
        float fill = Math.min(1.0F, be.getStoredMana() / (float) capacity);
        if (fill <= 0.001F) return;

        float time = be.getLevel().getGameTime() + partialTicks;

        // Anchor orb slightly above top face of the block
        poseStack.pushPose();
        float bob = (float) Math.sin(time * 0.08) * 0.05F;
        poseStack.translate(0.5F, 1.1F + bob, 0.5F);

        // Glow halo — soft, fill-scaled
        renderGlow(poseStack, buffer, time, fill);

        // Core orb — brighter, smaller, spinning
        renderCore(poseStack, buffer, time, fill);

        poseStack.popPose();
    }

    private void renderGlow(PoseStack poseStack, MultiBufferSource buffer, float time, float fill) {
        poseStack.pushPose();
        float pulse = 1.0F + (float) Math.sin(time * 0.18) * 0.12F;
        float scale = 0.55F * pulse * (0.5F + 0.5F * fill);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f m = pose.pose();
        Matrix3f n = pose.normal();
        VertexConsumer vc = buffer.getBuffer(RENDER_TYPE);

        int alpha = (int) (90 * fill);
        quad(vc, m, n, LightTexture.FULL_BRIGHT, 90, 255, 170, alpha);
        poseStack.popPose();
    }

    private void renderCore(PoseStack poseStack, MultiBufferSource buffer, float time, float fill) {
        poseStack.pushPose();
        float pulse = 1.0F + (float) Math.sin(time * 0.3) * 0.08F;
        float scale = 0.3F * pulse * (0.6F + 0.4F * fill);
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(time * 3.0F));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f m = pose.pose();
        Matrix3f n = pose.normal();
        VertexConsumer vc = buffer.getBuffer(RENDER_TYPE);

        int alpha = (int) (220 * (0.4F + 0.6F * fill));
        quad(vc, m, n, LightTexture.FULL_BRIGHT, 180, 255, 220, alpha);
        poseStack.popPose();
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
