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

public class GaiaSpiritCatalyst implements CatalystEffect {
    private static final ResourceLocation ID = 
        new ResourceLocation(IronsBotany.MODID, "gaia_spirit");
    
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    
    @Override
    public boolean appliesTo(AbstractSpell spell) {
        return true; // Ultimate catalyst applies to all spells
    }
    
    @Override
    public void modifySpell(AbstractSpell spell, SpellContext context) {
        // Massive damage boost
        context.multiplyDamage(1.5f);
        
        // Significant cooldown reduction
        context.multiplyCooldown(0.7f);
        
        // Add Gaia's blessing effects
        context.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, 1));
        context.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1));
        context.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
        
        // Enable special Gaia effects
        context.setCustomData("gaia_blessed", true);
        context.setCustomData("particle_trail", "gaia");
        
        // High mana cost for ultimate power
        context.multiplyManaCost(1.5f);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("catalyst.ironsbotany.gaia_spirit")
            .withStyle(ChatFormatting.LIGHT_PURPLE);
    }
    
    @Override
    public Component getDescription() {
        return Component.translatable("catalyst.ironsbotany.gaia_spirit.desc");
    }
    
    @Override
    public CatalystTier getTier() {
        return CatalystTier.LEGENDARY;
    }
}
