package com.ironsbotany.common.spell.catalyst.impl;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.spell.catalyst.CatalystEffect;
import com.ironsbotany.common.spell.catalyst.SpellContext;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Terrasteel catalyst - critical hits and cooldown reduction
 */
public class TerrasteelCatalyst implements CatalystEffect {
    private static final ResourceLocation ID = 
        ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "terrasteel");
    
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
        // 20% chance for critical hit (double damage)
        if (context.getLevel().random.nextFloat() < 0.20f) {
            context.multiplyDamage(2.0f);
            context.setCustomData("critical_hit", true);
        }
        
        // Reduce cooldown by 15%
        context.multiplyCooldown(0.85f);
        
        // Slight mana cost increase for power
        context.multiplyManaCost(1.1f);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("catalyst.ironsbotany.terrasteel")
            .withStyle(ChatFormatting.GREEN);
    }
    
    @Override
    public Component getDescription() {
        return Component.translatable("catalyst.ironsbotany.terrasteel.desc");
    }
    
    @Override
    public CatalystTier getTier() {
        return CatalystTier.ELITE;
    }
}
