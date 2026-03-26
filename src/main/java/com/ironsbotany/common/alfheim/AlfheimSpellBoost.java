package com.ironsbotany.common.alfheim;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.spell.catalyst.SpellContext;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

/**
 * Alfheim spell dimension system.
 * Alfheim becomes the "arcane firmware update realm" where spells reach full power.
 */
public class AlfheimSpellBoost {
    
    // Alfheim dimension key (Botania's dimension)
    private static final ResourceKey<Level> ALFHEIM = 
        ResourceKey.create(Registries.DIMENSION, 
            ResourceLocation.tryParse("botania:alfheim"));
    
    /**
     * Check if player is in Alfheim
     */
    public static boolean isInAlfheim(Player player) {
        if (!ModList.get().isLoaded("botania")) {
            return false;
        }
        
        try {
            return player.level().dimension().equals(ALFHEIM);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Apply Alfheim spell boost to context
     * @param context The spell context to modify
     * @param spell The spell being cast
     * @param player The caster
     */
    public static void applyAlfheimBoost(SpellContext context, AbstractSpell spell, Player player) {
        if (!isInAlfheim(player)) {
            return;
        }
        
        // Alfheim boosts all spells
        float powerBoost = getAlfheimPowerBoost(spell);
        context.multiplyDamage(1.0f + powerBoost);
        
        // Reduce cooldowns in Alfheim
        context.multiplyCooldown(0.8f); // -20% cooldown
        
        // Increase range
        context.multiplyRange(1.3f); // +30% range
        
        // Add Alfheim resonance
        context.setCustomData("alfheim_resonance", true);
        context.setCustomData("alfheim_power_boost", powerBoost);
        
        IronsBotany.LOGGER.debug("Applied Alfheim boost to {}: +{}% power", 
            spell.getSpellId(), (int)(powerBoost * 100));
    }
    
    /**
     * Calculate Alfheim power boost based on spell type
     */
    private static float getAlfheimPowerBoost(AbstractSpell spell) {
        String spellId = spell.getSpellId();
        
        // Botanical spells get massive boost in Alfheim
        if (spellId.contains("botanical") || spellId.contains("bloom") || 
            spellId.contains("petal") || spellId.contains("root")) {
            return 0.5f; // +50% power
        }
        
        // Nature spells get good boost
        if (spellId.contains("nature") || spellId.contains("earth") || 
            spellId.contains("plant")) {
            return 0.35f; // +35% power
        }
        
        // All other spells get moderate boost
        return 0.25f; // +25% power
    }
    
    /**
     * Check if a spell can only reach full power in Alfheim
     */
    public static boolean requiresAlfheim(AbstractSpell spell) {
        // Ultimate Botanical spells require Alfheim
        return spell.getSpellId().contains("gaia");
    }
}
