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
 * Terra Rod Channel - Huge burst, heavy cooldown
 * Optimized for devastating single strikes
 */
public class TerraRodChannel implements CastingChannel {
    private static final ResourceLocation ID = 
        new ResourceLocation(IronsBotany.MODID, "terra_rod");
    
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("casting_channel.ironsbotany.terra_rod")
            .withStyle(ChatFormatting.GOLD);
    }
    
    @Override
    public void modifyCast(SpellCastContext context) {
        // Huge burst, heavy cooldown
        context.multiplyBurstDamage(1.75f); // +75% burst damage
        context.multiplyCooldown(1.5f); // +50% cooldown
        context.multiplyManaCost(1.3f); // +30% mana cost
        context.multiplyCastingSpeed(0.8f); // -20% casting speed
    }
    
    @Override
    public boolean canCast(AbstractSpell spell, Player player) {
        // Best for damage spells
        return spell.getSpellId().contains("burst") ||
               spell.getSpellId().contains("damage") ||
               spell.getSpellId().contains("wrath") ||
               spell.getSpellId().contains("storm");
    }
    
    @Override
    public ChannelVisuals getVisuals() {
        return new ChannelVisuals(
            ParticleTypes.FLAME,
            ParticleTypes.LAVA,
            0xFF6600,
            SoundEvents.BLAZE_SHOOT
        );
    }
    
    @Override
    public float getCastingSpeedMultiplier() {
        return 0.8f;
    }
    
    @Override
    public float getManaRegenBonus() {
        return -0.2f; // Penalty
    }
    
    @Override
    public float getBurstDamageMultiplier() {
        return 1.75f;
    }
    
    @Override
    public float getCooldownMultiplier() {
        return 1.5f;
    }
}
