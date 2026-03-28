package com.ironsbotany.common.casting.channels;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.casting.CastingChannel;
import com.ironsbotany.common.casting.ChannelVisuals;
import com.ironsbotany.common.casting.SpellCastContext;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

/**
 * Dreamwood Focus Channel - Faster casting speed
 * Optimized for rapid spell deployment
 */
public class DreamwoodFocusChannel implements CastingChannel {
    private static final ResourceLocation ID = 
        new ResourceLocation(IronsBotany.MODID, "dreamwood_focus");
    
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("casting_channel.ironsbotany.dreamwood_focus")
            .withStyle(ChatFormatting.LIGHT_PURPLE);
    }
    
    @Override
    public void modifyCast(SpellCastContext context) {
        // Faster casting speed
        context.multiplyCastingSpeed(1.4f); // +40% casting speed
        context.multiplyCooldown(0.9f); // -10% cooldown
        context.multiplyManaCost(1.1f); // +10% mana cost for speed
    }
    
    @Override
    public boolean canCast(AbstractSpell spell, Player player) {
        return true; // Can cast all spells
    }
    
    @Override
    public ChannelVisuals getVisuals() {
        return new ChannelVisuals(
            ParticleTypes.ENCHANT,
            ParticleTypes.PORTAL,
            0xCC00FF,
            SoundEvents.ENCHANTMENT_TABLE_USE
        );
    }
    
    @Override
    public float getCastingSpeedMultiplier() {
        return 1.4f;
    }
    
    @Override
    public float getManaRegenBonus() {
        return 0.0f;
    }
    
    @Override
    public float getBurstDamageMultiplier() {
        return 1.0f;
    }
    
    @Override
    public float getCooldownMultiplier() {
        return 0.9f;
    }
}
