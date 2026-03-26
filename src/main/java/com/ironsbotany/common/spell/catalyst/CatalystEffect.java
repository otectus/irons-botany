package com.ironsbotany.common.spell.catalyst;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Represents a catalyst effect that can modify spell behavior.
 * Catalysts are Botania items that act as semantic amplifiers for spells.
 */
public interface CatalystEffect {
    
    /**
     * Unique identifier for this catalyst effect
     */
    ResourceLocation getId();
    
    /**
     * Check if this catalyst applies to the given spell
     * @param spell The spell being cast
     * @return true if this catalyst should modify the spell
     */
    boolean appliesTo(AbstractSpell spell);
    
    /**
     * Modify the spell context with catalyst effects
     * @param spell The spell being cast
     * @param context The spell context to modify
     */
    void modifySpell(AbstractSpell spell, SpellContext context);
    
    /**
     * Get the display name for tooltips
     */
    Component getDisplayName();
    
    /**
     * Get the description for tooltips
     */
    Component getDescription();
    
    /**
     * Get the rarity/tier of this catalyst
     */
    CatalystTier getTier();
    
    /**
     * Catalyst tier system for visual feedback and balancing
     */
    enum CatalystTier {
        BASIC(ChatFormatting.WHITE),
        ADVANCED(ChatFormatting.BLUE),
        ELITE(ChatFormatting.DARK_PURPLE),
        LEGENDARY(ChatFormatting.GOLD);
        
        private final ChatFormatting color;
        
        CatalystTier(ChatFormatting color) {
            this.color = color;
        }
        
        public ChatFormatting getColor() {
            return color;
        }
    }
}
