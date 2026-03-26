package com.ironsbotany.common.spell.catalyst.impl;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.spell.catalyst.CatalystEffect;
import com.ironsbotany.common.spell.catalyst.SpellContext;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Rune of Water catalyst - converts healing to AoE splash
 */
public class RuneOfWaterCatalyst implements CatalystEffect {
    private static final ResourceLocation ID = 
        ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "rune_of_water");
    
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    
    @Override
    public boolean appliesTo(AbstractSpell spell) {
        // Apply to healing spells
        return spell.getSpellId().contains("heal") ||
               spell.getSpellId().contains("regenerat") ||
               spell.getSpellId().contains("rebirth");
    }
    
    @Override
    public void modifySpell(AbstractSpell spell, SpellContext context) {
        // Convert single-target heal to AoE splash
        context.setCustomData("splash_heal", true);
        context.setCustomData("splash_radius", 5.0);
        
        // Reduce per-target healing by 30% to balance AoE
        context.multiplyDamage(0.7f);
        
        // Add water breathing effect
        context.setCustomData("grant_water_breathing", true);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("catalyst.ironsbotany.rune_of_water")
            .withStyle(ChatFormatting.BLUE);
    }
    
    @Override
    public Component getDescription() {
        return Component.translatable("catalyst.ironsbotany.rune_of_water.desc");
    }
    
    @Override
    public CatalystTier getTier() {
        return CatalystTier.ADVANCED;
    }
}
