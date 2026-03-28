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

/**
 * Bellethorne Aura - Increases thorns and retaliation spell potency
 */
public class BellethorneAura implements FlowerAura {
    private static final ResourceLocation ID = 
        new ResourceLocation(IronsBotany.MODID, "bellethorne");
    
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("flower_aura.ironsbotany.bellethorne")
            .withStyle(ChatFormatting.DARK_RED);
    }
    
    @Override
    public int getRange() {
        return 8; // 8 block radius
    }
    
    @Override
    public float calculateStrength(double distance) {
        // Linear falloff
        return Math.max(0, 1.0f - (float)(distance / getRange()));
    }
    
    @Override
    public void applyToSpell(SpellContext context, float strength) {
        // Increase thorns/retaliation damage
        float thornsBonus = 0.3f * strength; // Up to +30%
        context.setCustomData("thorns_bonus", thornsBonus);
        
        // Add retaliation effect
        if (strength > 0.5f) {
            context.setCustomData("retaliation_enabled", true);
            context.setCustomData("retaliation_damage", 5.0f * strength);
        }
        
        // Boost damage for defensive spells (caller already verified appliesTo)
        if (strength > 0.5f) {
            context.multiplyDamage(1.0f + (0.2f * strength));
        }
    }
    
    @Override
    public boolean appliesTo(AbstractSpell spell) {
        if (spell == null) return false;
        
        // Applies to defensive spells and damage spells
        return spell.getSpellId().contains("shield") ||
               spell.getSpellId().contains("protect") ||
               spell.getSpellId().contains("burst") ||
               spell.getSpellId().contains("damage");
    }
    
    @Override
    public ParticleOptions getParticle() {
        return ParticleTypes.DAMAGE_INDICATOR;
    }
    
    @Override
    public int getColor() {
        return 0xFF0000; // Red
    }
}
