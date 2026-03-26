package com.ironsbotany.common.config;

/**
 * Helper class for checking configuration states.
 * Centralizes logic for master toggles and bare-bones mode.
 */
public class ConfigHelper {
    
    /**
     * Check if deep synergy features are enabled
     */
    public static boolean isDeepSynergyEnabled() {
        return !CommonConfig.BARE_BONES_MODE.get() && 
               CommonConfig.ENABLE_DEEP_SYNERGY.get();
    }
    
    /**
     * Check if spell catalysts are enabled
     */
    public static boolean areCatalystsEnabled() {
        return isDeepSynergyEnabled() && 
               CommonConfig.ENABLE_SPELL_CATALYSTS.get();
    }
    
    /**
     * Check if casting channels are enabled
     */
    public static boolean areChannelsEnabled() {
        return isDeepSynergyEnabled() && 
               CommonConfig.ENABLE_CASTING_CHANNELS.get();
    }
    
    /**
     * Check if flower auras are enabled
     */
    public static boolean areAurasEnabled() {
        return isDeepSynergyEnabled() && 
               CommonConfig.ENABLE_FLOWER_AURAS.get();
    }
    
    /**
     * Check if spell-triggered mana events are enabled
     */
    public static boolean areManaEventsEnabled() {
        return isDeepSynergyEnabled() && 
               CommonConfig.ENABLE_SPELL_MANA_EVENTS.get();
    }
    
    /**
     * Check if Corporea logistics are enabled
     */
    public static boolean isCorporeaEnabled() {
        return isDeepSynergyEnabled() && 
               CommonConfig.ENABLE_CORPOREA_LOGISTICS.get();
    }
    
    /**
     * Check if Alfheim integration is enabled
     */
    public static boolean isAlfheimEnabled() {
        return isDeepSynergyEnabled() && 
               CommonConfig.ENABLE_ALFHEIM_BOOST.get();
    }
    
    /**
     * Check if mana integration is active
     */
    public static boolean isManaIntegrationActive() {
        ManaUnificationMode mode = CommonConfig.MANA_UNIFICATION_MODE.get();
        return mode.isActive();
    }
    
    /**
     * Check if dual-cost is required
     */
    public static boolean isDualCostRequired() {
        ManaUnificationMode mode = CommonConfig.MANA_UNIFICATION_MODE.get();
        return mode.requiresDualCost() || 
               (mode == ManaUnificationMode.HYBRID && CommonConfig.ENABLE_DUAL_COST_SPELLS.get());
    }
    
    /**
     * Check if Botania mana should be consumed
     */
    public static boolean shouldConsumeBotaniaMana() {
        ManaUnificationMode mode = CommonConfig.MANA_UNIFICATION_MODE.get();
        return mode == ManaUnificationMode.BOTANIA_PRIMARY || 
               mode == ManaUnificationMode.SEPARATE ||
               (mode == ManaUnificationMode.HYBRID && CommonConfig.ENABLE_DUAL_COST_SPELLS.get());
    }
    
    /**
     * Get effective mana conversion ratio
     */
    public static int getManaConversionRatio() {
        return CommonConfig.MANA_CONVERSION_RATIO.get();
    }
    
    /**
     * Get effective reverse conversion ratio
     */
    public static int getReverseConversionRatio() {
        return CommonConfig.REVERSE_CONVERSION_RATIO.get();
    }
}
