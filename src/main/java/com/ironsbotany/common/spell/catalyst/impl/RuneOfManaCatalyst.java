package com.ironsbotany.common.spell.catalyst.impl;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.spell.catalyst.CatalystEffect;
import com.ironsbotany.common.spell.catalyst.SpellContext;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RuneOfManaCatalyst implements CatalystEffect {
    private static final ResourceLocation ID = 
        new ResourceLocation(IronsBotany.MODID, "rune_of_mana");
    
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
        // Reduce mana cost by 25%
        context.multiplyManaCost(0.75f);
        
        // Slight damage reduction for efficiency
        context.multiplyDamage(0.95f);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("catalyst.ironsbotany.rune_of_mana")
            .withStyle(ChatFormatting.AQUA);
    }
    
    @Override
    public Component getDescription() {
        return Component.translatable("catalyst.ironsbotany.rune_of_mana.desc");
    }
    
    @Override
    public CatalystTier getTier() {
        return CatalystTier.ADVANCED;
    }
}
