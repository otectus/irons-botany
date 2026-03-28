package com.ironsbotany.common.spell.catalyst.impl;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.spell.catalyst.CatalystEffect;
import com.ironsbotany.common.spell.catalyst.SpellContext;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Mana Lens: Bore catalyst - adds piercing to projectiles
 */
public class LensBoreCatalyst implements CatalystEffect {
    private static final ResourceLocation ID = 
        new ResourceLocation(IronsBotany.MODID, "lens_bore");
    
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    
    @Override
    public boolean appliesTo(AbstractSpell spell) {
        return spell.getSpellId().contains("projectile") ||
               spell.getSpellId().contains("burst") ||
               spell.getSpellId().contains("bolt");
    }
    
    @Override
    public void modifySpell(AbstractSpell spell, SpellContext context) {
        // Enable piercing
        context.setPiercing(true);
        
        // Set max pierce count
        context.setCustomData("max_pierce", 3);
        
        // Reduce damage per pierce by 15%
        context.setCustomData("pierce_damage_reduction", 0.15f);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("catalyst.ironsbotany.lens_bore")
            .withStyle(ChatFormatting.GRAY);
    }
    
    @Override
    public Component getDescription() {
        return Component.translatable("catalyst.ironsbotany.lens_bore.desc");
    }
    
    @Override
    public CatalystTier getTier() {
        return CatalystTier.ADVANCED;
    }
}
