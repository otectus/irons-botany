package com.ironsbotany.common.flower;

import com.ironsbotany.common.spell.catalyst.SpellContext;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents an aura effect from a functional flower.
 * Flowers create environmental spell buffs in a radius.
 */
public interface FlowerAura {
    
    /**
     * Get the aura's unique identifier
     */
    ResourceLocation getId();
    
    /**
     * Get the aura's display name
     */
    Component getDisplayName();
    
    /**
     * Get the aura's effective range in blocks
     */
    int getRange();
    
    /**
     * Calculate aura strength based on distance
     * @param distance Distance from flower
     * @return Strength multiplier (0.0 to 1.0)
     */
    float calculateStrength(double distance);
    
    /**
     * Apply aura effect to spell context
     * @param context The spell context to modify
     * @param strength The strength of the aura (0.0 to 1.0)
     */
    void applyToSpell(SpellContext context, float strength);
    
    /**
     * Check if this aura applies to the given spell
     */
    boolean appliesTo(AbstractSpell spell);
    
    /**
     * Get particle effect for visualization
     */
    ParticleOptions getParticle();
    
    /**
     * Get aura color for visualization (RGB hex)
     */
    int getColor();
}
