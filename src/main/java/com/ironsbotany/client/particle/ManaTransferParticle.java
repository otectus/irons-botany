package com.ironsbotany.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ManaTransferParticle extends TextureSheetParticle {
    private final float baseSize;

    protected ManaTransferParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.lifetime = 20;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.baseSize = 0.15F + this.random.nextFloat() * 0.05F;
        this.quadSize = this.baseSize;

        // Start blue
        this.rCol = 0.4F;
        this.gCol = 0.3F;
        this.bCol = 1.0F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTicks) {
        float t = ((float) this.age + partialTicks) / (float) this.lifetime;
        // Grow over first 25% of life, then shrink to 0
        float sizeCurve;
        if (t < 0.25F) {
            sizeCurve = Mth.lerp(t / 0.25F, 1.0F, 2.0F);
        } else {
            sizeCurve = Mth.lerp((t - 0.25F) / 0.75F, 2.0F, 0.0F);
        }
        return this.baseSize * sizeCurve;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        float t = (float) this.age / (float) this.lifetime;

        // Quadratic ease-out alpha
        this.alpha = (1.0F - t) * (1.0F - t);

        // Color gradient: blue -> purple
        this.rCol = 0.4F + t * 0.3F;
        this.gCol = 0.3F - t * 0.1F;
        this.bCol = 1.0F - t * 0.1F;

        // Gentle rotation
        this.roll += 0.1F;

        this.move(this.xd, this.yd, this.zd);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                        double xSpeed, double ySpeed, double zSpeed) {
            ManaTransferParticle particle = new ManaTransferParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
