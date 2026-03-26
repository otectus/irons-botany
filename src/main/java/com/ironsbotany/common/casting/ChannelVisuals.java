package com.ironsbotany.common.casting;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;

/**
 * Visual and audio effects for a casting channel
 */
public class ChannelVisuals {
    private final ParticleOptions castParticle;
    private final ParticleOptions trailParticle;
    private final int particleColor;
    private final SoundEvent castSound;
    
    public ChannelVisuals(ParticleOptions castParticle, ParticleOptions trailParticle,
                          int particleColor, SoundEvent castSound) {
        this.castParticle = castParticle;
        this.trailParticle = trailParticle;
        this.particleColor = particleColor;
        this.castSound = castSound;
    }
    
    public ParticleOptions getCastParticle() {
        return castParticle;
    }
    
    public ParticleOptions getTrailParticle() {
        return trailParticle;
    }
    
    public int getParticleColor() {
        return particleColor;
    }
    
    public SoundEvent getCastSound() {
        return castSound;
    }
}
