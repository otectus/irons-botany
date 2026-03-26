package com.ironsbotany.common.config;

/**
 * Defines how mana unification works between Botania and ISS.
 * Provides complete flexibility for different playstyles and modpack configurations.
 */
public enum ManaUnificationMode {
    /**
     * Botania is primary - ISS spells consume Botania mana directly.
     * No ISS mana needed, everything runs on Botania infrastructure.
     */
    BOTANIA_PRIMARY("Botania Primary", 
        "ISS spells consume Botania mana directly. Build Botania infrastructure to power all magic."),
    
    /**
     * ISS is primary - Botania items grant ISS mana.
     * Botania becomes a mana generation method for ISS.
     */
    ISS_PRIMARY("ISS Primary",
        "Botania items and systems generate ISS mana. Use Botania as mana generation infrastructure."),
    
    /**
     * Hybrid mode - Both systems work together with conversion.
     * Current default behavior with configurable conversion ratio.
     */
    HYBRID("Hybrid",
        "Both mana systems work together. Conversion between Botania and ISS mana at configurable ratio."),
    
    /**
     * Separate systems - No conversion, dual-cost required.
     * Must maintain both mana types independently.
     */
    SEPARATE("Separate",
        "No mana conversion. Botanical spells require BOTH Botania and ISS mana. Most challenging mode."),
    
    /**
     * Disabled - No mana integration.
     * Mods function completely independently.
     */
    DISABLED("Disabled",
        "No mana integration. Botania and ISS function as separate mods. Bare-bones mode.");
    
    private final String displayName;
    private final String description;
    
    ManaUnificationMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if conversion is enabled in this mode
     */
    public boolean allowsConversion() {
        return this == HYBRID || this == BOTANIA_PRIMARY || this == ISS_PRIMARY;
    }
    
    /**
     * Check if dual-cost is required in this mode
     */
    public boolean requiresDualCost() {
        return this == SEPARATE;
    }
    
    /**
     * Check if mana integration is active
     */
    public boolean isActive() {
        return this != DISABLED;
    }
}
