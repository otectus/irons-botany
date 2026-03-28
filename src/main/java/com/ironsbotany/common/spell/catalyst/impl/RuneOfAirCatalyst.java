package com.ironsbotany.common.spell.catalyst.impl;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.spell.catalyst.CatalystEffect;
import com.ironsbotany.common.spell.catalyst.SpellContext;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class RuneOfAirCatalyst implements CatalystEffect {
    private static final ResourceLocation ID = 
        new ResourceLocation(IronsBotany.MODID, "rune_of_air");
    
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    
    @Override
    public boolean appliesTo(AbstractSpell spell) {
        return true; // Applies to all spells
    }
    
    @Override
    public void modifySpell(AbstractSpell spell, SpellContext context) {
        // Increase projectile speed
        Float currentSpeed = context.getCustomData("projectile_speed_multiplier", Float.class);
        float newSpeed = (currentSpeed != null ? currentSpeed : 1.0f) * 1.3f;
        context.setCustomData("projectile_speed_multiplier", newSpeed);
        
        // Add speed effect to caster
        context.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0));
        
        // Reduce casting time
        context.multiplyCastingSpeed(1.2f);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("catalyst.ironsbotany.rune_of_air")
            .withStyle(ChatFormatting.WHITE);
    }
    
    @Override
    public Component getDescription() {
        return Component.translatable("catalyst.ironsbotany.rune_of_air.desc");
    }
    
    @Override
    public CatalystTier getTier() {
        return CatalystTier.ADVANCED;
    }
}
