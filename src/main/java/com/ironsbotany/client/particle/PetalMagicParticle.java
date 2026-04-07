package com.ironsbotany.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PetalMagicParticle extends TextureSheetParticle {
    private final float baseSize;

    protected PetalMagicParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.lifetime = 30;
        this.gravity = 0.01F;
        this.hasPhysics = false;
        this.baseSize = 0.15F + this.random.nextFloat() * 0.1F;
        this.quadSize = this.baseSize;

        // Pink-magenta
        this.rCol = 1.0F;
        this.gCol = 0.4F;
        this.bCol = 0.7F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTicks) {
        float t = ((float) this.age + partialTicks) / (float) this.lifetime;
        // Gentle size pulse
        float pulse = (float) Math.sin((this.age + partialTicks) * 0.5) * 0.02F;
        // Fade size toward end of life
        float lifeFade = t < 0.7F ? 1.0F : 1.0F - ((t - 0.7F) / 0.3F);
        return (this.baseSize + pulse) * lifeFade;
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

        // Smooth ease-out alpha
        this.alpha = 1.0F - t * t;

        // Color: pink -> warm coral
        this.rCol = 1.0F;
        this.gCol = 0.4F + t * 0.2F;
        this.bCol = 0.7F - t * 0.2F;

        // Flutter rotation (oscillating)
        this.roll += (float) Math.sin(this.age * 0.3) * 0.15F;

        // Sinusoidal horizontal drift
        this.xd += Math.sin(this.age * 0.2) * 0.005;
        this.zd += Math.cos(this.age * 0.2) * 0.005;

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
            PetalMagicParticle particle = new PetalMagicParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
