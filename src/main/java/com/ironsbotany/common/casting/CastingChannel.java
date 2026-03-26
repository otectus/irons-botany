package com.ironsbotany.common.casting;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;

/**
 * Represents a casting channel - the "hardware" for spellcasting.
 * Spellbooks are software (libraries), casting channels are hardware.
 */
public interface CastingChannel {
    
    /**
     * Get the channel's unique identifier
     */
    ResourceLocation getId();
    
    /**
     * Get the channel's display name
     */
    Component getDisplayName();
    
    /**
     * Modify spell casting parameters
     */
    void modifyCast(SpellCastContext context);
    
    /**
     * Check if this channel can cast the given spell
     */
    boolean canCast(AbstractSpell spell, Player player);
    
    /**
     * Get visual effects for this channel
     */
    ChannelVisuals getVisuals();
    
    /**
     * Get the casting speed multiplier
     */
    float getCastingSpeedMultiplier();
    
    /**
     * Get the mana regeneration bonus
     */
    float getManaRegenBonus();
    
    /**
     * Get the burst damage multiplier
     */
    float getBurstDamageMultiplier();
    
    /**
     * Get the cooldown multiplier
     */
    float getCooldownMultiplier();
}
