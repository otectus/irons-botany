package com.ironsbotany.common.casting.channels;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.casting.CastingChannel;
import com.ironsbotany.common.casting.ChannelVisuals;
import com.ironsbotany.common.casting.SpellCastContext;
import com.ironsbotany.common.registry.IBSchools;
import com.ironsbotany.common.spell.AbstractBotanicalSpell;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

/**
 * Livingwood Staff Channel - Higher regen, lower burst
 * Optimized for sustained casting and mana efficiency
 */
public class LivingwoodStaffChannel implements CastingChannel {
    private static final ResourceLocation ID = 
        ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "livingwood_staff");
    
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("casting_channel.ironsbotany.livingwood_staff")
            .withStyle(ChatFormatting.GREEN);
    }
    
    @Override
    public void modifyCast(SpellCastContext context) {
        // Higher regen, lower burst
        context.addManaRegen(0.5f); // +50% mana regen
        context.multiplyBurstDamage(0.85f); // -15% burst damage
        context.multiplyCastingSpeed(1.1f); // +10% casting speed
        context.multiplyManaCost(0.9f); // -10% mana cost
    }
    
    @Override
    public boolean canCast(AbstractSpell spell, Player player) {
        // Can cast all Botanical spells efficiently
        return spell instanceof AbstractBotanicalSpell ||
               spell.getSchoolType() == IBSchools.BOTANICAL.get();
    }
    
    @Override
    public ChannelVisuals getVisuals() {
        return new ChannelVisuals(
            ParticleTypes.HAPPY_VILLAGER,
            ParticleTypes.COMPOSTER,
            0x00FF00,
            SoundEvents.GRASS_BREAK
        );
    }
    
    @Override
    public float getCastingSpeedMultiplier() {
        return 1.1f;
    }
    
    @Override
    public float getManaRegenBonus() {
        return 0.5f;
    }
    
    @Override
    public float getBurstDamageMultiplier() {
        return 0.85f;
    }
    
    @Override
    public float getCooldownMultiplier() {
        return 1.0f;
    }
}
