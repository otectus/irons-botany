package com.ironsbotany.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BotanicalBurstParticle extends TextureSheetParticle {
    private final float baseSize;

    protected BotanicalBurstParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.lifetime = 12;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.baseSize = 0.4F + this.random.nextFloat() * 0.2F;
        this.quadSize = this.baseSize;

        // Slight upward drift
        this.yd = 0.02;

        // Start bright green
        this.rCol = 0.3F;
        this.gCol = 1.0F;
        this.bCol = 0.2F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTicks) {
        float t = ((float) this.age + partialTicks) / (float) this.lifetime;
        // Start large, shrink to 0
        return this.baseSize * (1.0F - t);
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

        // Fast exponential alpha fade
        this.alpha = (1.0F - t) * (1.0F - t);

        // Color gradient: bright green -> gold
        this.rCol = 0.3F + t * 0.6F;
        this.gCol = 1.0F - t * 0.2F;
        this.bCol = 0.2F - t * 0.1F;

        // Rotation
        this.roll += 0.15F;

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
            BotanicalBurstParticle particle = new BotanicalBurstParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
