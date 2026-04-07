package com.ironsbotany.common.flower.auras;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.flower.FlowerAura;
import com.ironsbotany.common.spell.catalyst.SpellContext;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Rannuncarpus Aura - Ritual automation and casting speed
 */
public class RannuncarpusAura implements FlowerAura {
    private static final ResourceLocation ID = 
        new ResourceLocation(IronsBotany.MODID, "rannuncarpus");
    
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("flower_aura.ironsbotany.rannuncarpus")
            .withStyle(ChatFormatting.YELLOW);
    }
    
    @Override
    public int getRange() {
        return 6;
    }
    
    @Override
    public float calculateStrength(double distance) {
        return Math.max(0, 1.0f - (float)(distance / getRange()));
    }
    
    @Override
    public void applyToSpell(SpellContext context, float strength) {
        // Enable ritual automation
        context.setCustomData("auto_ritual", true);
        context.setCustomData("ritual_range", (int)(5 * strength));
        
        // Reduce ritual casting time
        context.multiplyCastingSpeed(1.0f + (0.3f * strength));
        
        // Auto-place ritual components
        if (strength > 0.6f) {
            context.setCustomData("auto_place_components", true);
        }
        
        // Reduce mana cost for long casts
        context.multiplyManaCost(1.0f - (0.15f * strength));
    }
    
    @Override
    public boolean appliesTo(AbstractSpell spell) {
        if (spell == null) return false;
        return spell.getCastType() == CastType.LONG ||
               spell.getCastType() == CastType.CONTINUOUS ||
               spell.getSpellId().contains("ritual") ||
               spell.getSpellId().contains("circle") ||
               spell.getSpellId().contains("bloom");
    }
    
    @Override
    public ParticleOptions getParticle() {
        return ParticleTypes.COMPOSTER;
    }
    
    @Override
    public int getColor() {
        return 0xFFFF00; // Yellow
    }
}
