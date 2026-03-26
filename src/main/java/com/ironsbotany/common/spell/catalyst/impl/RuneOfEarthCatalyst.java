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

public class RuneOfEarthCatalyst implements CatalystEffect {
    private static final ResourceLocation ID = 
        ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "rune_of_earth");
    
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    
    @Override
    public boolean appliesTo(AbstractSpell spell) {
        return spell.getSpellId().contains("shield") ||
               spell.getSpellId().contains("protect") ||
               spell.getSpellId().contains("defense");
    }
    
    @Override
    public void modifySpell(AbstractSpell spell, SpellContext context) {
        // Add knockback resistance
        context.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0));
        
        // Increase shield strength
        context.multiplyDamage(1.3f);
        
        // Add stability
        context.setCustomData("knockback_resistance", 0.5f);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("catalyst.ironsbotany.rune_of_earth")
            .withStyle(ChatFormatting.DARK_GREEN);
    }
    
    @Override
    public Component getDescription() {
        return Component.translatable("catalyst.ironsbotany.rune_of_earth.desc");
    }
    
    @Override
    public CatalystTier getTier() {
        return CatalystTier.ADVANCED;
    }
}
