package com.ironsbotany.common.spell.catalyst.impl;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.spell.catalyst.CatalystEffect;
import com.ironsbotany.common.spell.catalyst.SpellContext;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Mana Lens: Velocity catalyst - increases projectile speed and range
 */
public class LensVelocityCatalyst implements CatalystEffect {
    private static final ResourceLocation ID = 
        ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "lens_velocity");
    
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    
    @Override
    public boolean appliesTo(AbstractSpell spell) {
        // Apply to projectile spells
        return spell.getSpellId().contains("projectile") ||
               spell.getSpellId().contains("burst") ||
               spell.getSpellId().contains("bolt") ||
               spell.getSpellId().contains("arrow");
    }
    
    @Override
    public void modifySpell(AbstractSpell spell, SpellContext context) {
        // Increase projectile speed by 50%
        context.setCustomData("projectile_speed_multiplier", 1.5f);
        
        // Increase range by 30%
        context.multiplyRange(1.3f);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("catalyst.ironsbotany.lens_velocity")
            .withStyle(ChatFormatting.YELLOW);
    }
    
    @Override
    public Component getDescription() {
        return Component.translatable("catalyst.ironsbotany.lens_velocity.desc");
    }
    
    @Override
    public CatalystTier getTier() {
        return CatalystTier.BASIC;
    }
}
