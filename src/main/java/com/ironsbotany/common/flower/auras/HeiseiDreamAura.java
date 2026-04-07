package com.ironsbotany.common.flower.auras;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.flower.FlowerAura;
import com.ironsbotany.common.spell.catalyst.SpellContext;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Heisei Dream Aura - Increases illusion and mind spell power
 */
public class HeiseiDreamAura implements FlowerAura {
    private static final ResourceLocation ID = 
        new ResourceLocation(IronsBotany.MODID, "heisei_dream");
    
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("flower_aura.ironsbotany.heisei_dream")
            .withStyle(ChatFormatting.AQUA);
    }
    
    @Override
    public int getRange() {
        return 12;
    }
    
    @Override
    public float calculateStrength(double distance) {
        // Quadratic falloff for dream aura
        float linear = Math.max(0, 1.0f - (float)(distance / getRange()));
        return linear * linear;
    }
    
    @Override
    public void applyToSpell(SpellContext context, float strength) {
        // Boost illusion and mind spells
        context.multiplyDamage(1.0f + (0.4f * strength)); // Up to +40% power
        
        // Add confusion effect to targets
        if (strength > 0.5f) {
            context.setCustomData("apply_confusion", true);
            context.setCustomData("confusion_duration", (int)(100 * strength));
        }
        
        // Increase spell duration
        context.setCustomData("duration_multiplier", 1.0f + (0.5f * strength));
    }
    
    @Override
    public boolean appliesTo(AbstractSpell spell) {
        if (spell == null) return false;
        // Check for illusion/mind school or keywords
        return spell.getSpellId().contains("illusion") ||
               spell.getSpellId().contains("mind") ||
               spell.getSpellId().contains("sleep") ||
               spell.getSpellId().contains("charm") ||
               spell.getSpellId().contains("infusion");
    }
    
    @Override
    public ParticleOptions getParticle() {
        return ParticleTypes.ENCHANT;
    }
    
    @Override
    public int getColor() {
        return 0x00FFFF; // Cyan
    }
}
