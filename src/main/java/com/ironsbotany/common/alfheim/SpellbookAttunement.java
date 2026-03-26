package com.ironsbotany.common.alfheim;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Spellbook attunement system for Alfheim.
 * Spellbooks attuned in Alfheim gain permanent passive bonuses.
 */
public class SpellbookAttunement {
    
    private static final String ATTUNEMENT_TAG = "IronsBotany_AlfheimAttunement";
    private static final String ATTUNEMENT_LEVEL_TAG = "IronsBotany_AttunementLevel";
    private static final String ATTUNEMENT_TIME_TAG = "IronsBotany_AttunementTime";
    
    /**
     * Check if a spellbook is attuned to Alfheim
     */
    public static boolean isAttuned(ItemStack spellbook) {
        CompoundTag tag = spellbook.getTag();
        return tag != null && tag.getBoolean(ATTUNEMENT_TAG);
    }
    
    /**
     * Get attunement level (0-3)
     */
    public static int getAttunementLevel(ItemStack spellbook) {
        CompoundTag tag = spellbook.getTag();
        if (tag == null) return 0;
        return tag.getInt(ATTUNEMENT_LEVEL_TAG);
    }
    
    /**
     * Attune a spellbook to Alfheim
     * @param spellbook The spellbook to attune
     * @param timeInAlfheim Time spent in Alfheim (ticks)
     * @return true if attunement increased
     */
    public static boolean attuneSpellbook(ItemStack spellbook, long timeInAlfheim) {
        CompoundTag tag = spellbook.getOrCreateTag();
        
        // Mark as attuned
        tag.putBoolean(ATTUNEMENT_TAG, true);
        
        // Increase attunement level based on time
        long currentTime = tag.getLong(ATTUNEMENT_TIME_TAG);
        long totalTime = currentTime + timeInAlfheim;
        tag.putLong(ATTUNEMENT_TIME_TAG, totalTime);
        
        // Calculate attunement level (1 hour per level)
        int newLevel = Math.min(3, (int)(totalTime / 72000)); // 72000 ticks = 1 hour
        int oldLevel = tag.getInt(ATTUNEMENT_LEVEL_TAG);
        
        if (newLevel > oldLevel) {
            tag.putInt(ATTUNEMENT_LEVEL_TAG, newLevel);
            return true; // Level increased
        }
        
        return false;
    }
    
    /**
     * Get cooldown reduction from attunement
     */
    public static float getCooldownReduction(ItemStack spellbook) {
        int level = getAttunementLevel(spellbook);
        return level * 0.05f; // 5% per level, max 15%
    }
    
    /**
     * Get mana cost reduction from attunement
     */
    public static float getManaCostReduction(ItemStack spellbook) {
        int level = getAttunementLevel(spellbook);
        return level * 0.03f; // 3% per level, max 9%
    }
    
    /**
     * Get spell power bonus from attunement
     */
    public static float getSpellPowerBonus(ItemStack spellbook) {
        int level = getAttunementLevel(spellbook);
        return level * 0.04f; // 4% per level, max 12%
    }
    
    /**
     * Check if spellbook grants dual-school access
     */
    public static boolean hasDualSchoolAccess(ItemStack spellbook) {
        return getAttunementLevel(spellbook) >= 3; // Max level grants dual-school
    }
    
    /**
     * Get attunement tooltip
     */
    public static Component getAttunementTooltip(ItemStack spellbook) {
        if (!isAttuned(spellbook)) {
            return Component.translatable("tooltip.ironsbotany.not_attuned");
        }
        
        int level = getAttunementLevel(spellbook);
        return Component.translatable("tooltip.ironsbotany.alfheim_attuned", level);
    }
}
