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
 * Jaded Amaranthus Aura - Boosts summoning spells
 */
public class JadedAmaranthusAura implements FlowerAura {
    private static final ResourceLocation ID = 
        ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "jaded_amaranthus");
    
    @Override
    public ResourceLocation getId() {
        return ID;
    }
    
    @Override
    public Component getDisplayName() {
        return Component.translatable("flower_aura.ironsbotany.jaded_amaranthus")
            .withStyle(ChatFormatting.LIGHT_PURPLE);
    }
    
    @Override
    public int getRange() {
        return 10;
    }
    
    @Override
    public float calculateStrength(double distance) {
        return Math.max(0, 1.0f - (float)(distance / getRange()));
    }
    
    @Override
    public void applyToSpell(SpellContext context, float strength) {
        // Mark as summon for other systems
        context.setCustomData("is_summon", true);
        
        // Increase summon duration
        int durationBonus = (int)(100 * strength); // Up to 5 seconds
        context.setCustomData("summon_duration_bonus", durationBonus);
        
        // Increase summon health
        float healthBonus = 0.5f * strength; // Up to +50% health
        context.setCustomData("summon_health_bonus", healthBonus);
        
        // Chance to summon additional entity
        if (strength > 0.7f && context.getLevel().random.nextFloat() < 0.3f) {
            context.setCustomData("bonus_summon", true);
        }
    }
    
    @Override
    public boolean appliesTo(AbstractSpell spell) {
        return spell.getSpellId().contains("summon") ||
               spell.getSpellId().contains("communion") ||
               spell.getSpellId().contains("swarm");
    }
    
    @Override
    public ParticleOptions getParticle() {
        return ParticleTypes.HAPPY_VILLAGER;
    }
    
    @Override
    public int getColor() {
        return 0xFF00FF; // Magenta
    }
}
