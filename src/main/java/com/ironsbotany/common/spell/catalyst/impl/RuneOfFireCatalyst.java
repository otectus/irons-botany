package com.ironsbotany.common.spell.catalyst.impl;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.spell.catalyst.CatalystEffect;
import com.ironsbotany.common.spell.catalyst.SpellContext;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Rune of Fire catalyst - adds lingering burn and fire damage boost
 */
public class RuneOfFireCatalyst implements CatalystEffect {
    private static final ResourceLocation ID = 
        new ResourceLocation(IronsBotany.MODID, "rune_of_fire");
    
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    
    @Override
    public boolean appliesTo(AbstractSpell spell) {
        // Apply to fire school spells or any damage spell
        return spell.getSchoolType() == SchoolRegistry.FIRE.get() ||
               spell.getSpellId().contains("fire") ||
               spell.getSpellId().contains("burn");
    }
    
    @Override
    public void modifySpell(AbstractSpell spell, SpellContext context) {
        // Add fire resistance to caster (protection from own fire)
        context.addEffect(new MobEffectInstance(
            MobEffects.FIRE_RESISTANCE, 100, 0
        ));
        
        // Increase fire damage by 25%
        if (spell.getSchoolType() == SchoolRegistry.FIRE.get()) {
            context.multiplyDamage(1.25f);
        }
        
        // Add custom data for lingering fire
        context.setCustomData("lingering_fire", true);
        context.setCustomData("fire_duration", 60); // 3 seconds of burning
        context.setCustomData("fire_damage_per_tick", 1.0f);
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("catalyst.ironsbotany.rune_of_fire")
            .withStyle(ChatFormatting.RED);
    }
    
    @Override
    public Component getDescription() {
        return Component.translatable("catalyst.ironsbotany.rune_of_fire.desc");
    }
    
    @Override
    public CatalystTier getTier() {
        return CatalystTier.ADVANCED;
    }
}
