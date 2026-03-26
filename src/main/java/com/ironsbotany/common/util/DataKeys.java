package com.ironsbotany.common.util;

/**
 * Centralized registry of all NBT key strings used by Iron's Botany.
 * Prevents typos and makes key usage discoverable via IDE references.
 */
public final class DataKeys {
    private DataKeys() {}

    // === Player Persistent Data Keys ===

    // Flower Shield system
    public static final String FLOWER_SHIELD_HP = "IronsBotany_FlowerShield_HP";
    public static final String FLOWER_SHIELD_EXPIRY = "IronsBotany_FlowerShield_Expiry";

    // Mana Rebirth system
    public static final String MANA_REBIRTH_LEVEL = "IronsBotany_ManaRebirth_Level";
    public static final String MANA_REBIRTH_EXPIRY = "IronsBotany_ManaRebirth_Expiry";

    // Spell tracking
    public static final String LAST_SPELL_ID = "IronsBotany_LastSpellId";
    public static final String LAST_SPELL_TIME = "IronsBotany_LastSpellTime";
    public static final String UNIQUE_SPELLS = "IronsBotany_UniqueSpells";

    // Gaia trials
    public static final String ACTIVE_AURAS = "IronsBotany_ActiveAuras";

    // School migration
    public static final String SCHOOL_MIGRATED = "IronsBotany_SchoolMigrated";

    // Progression
    public static final String TIER4_UNLOCKED = "IronsBotany_Tier4Unlocked";
    public static final String DUAL_SCHOOL_UNLOCKED = "IronsBotany_DualSchoolUnlocked";
    public static final String OVERCHARGE_UNLOCKED = "IronsBotany_OverchargeUnlocked";

    // Automation
    public static final String ILLUSION_ACTIVE = "IronsBotany_IllusionActive";
    public static final String ILLUSION_EXPIRY = "IronsBotany_IllusionExpiry";
    public static final String GROWTH_BOOST = "IronsBotany_GrowthBoost";
    public static final String GROWTH_EXPIRY = "IronsBotany_GrowthExpiry";

    // === Item NBT Keys ===

    // Livingwood Staff mana storage
    public static final String BOTANIA_MANA = "IronsBotany_BotaniaMana";

    // Alfheim scroll crafting
    public static final String DUAL_SCHOOL = "IronsBotany_DualSchool";
    public static final String SECONDARY_SCHOOL = "IronsBotany_SecondarySchool";
    public static final String ALFHEIM_CRAFTED = "IronsBotany_AlfheimCrafted";

    // Spellbook attunement
    public static final String ALFHEIM_ATTUNEMENT = "IronsBotany_AlfheimAttunement";
    public static final String ATTUNEMENT_LEVEL = "IronsBotany_AttunementLevel";
    public static final String ATTUNEMENT_TIME = "IronsBotany_AttunementTime";

    // === Block Entity Persistent Data Keys (Stage 4 - Experimental) ===

    public static final String THROUGHPUT_BOOST = "IronsBotany_ThroughputBoost";
    public static final String BOOST_EXPIRY = "IronsBotany_BoostExpiry";
    public static final String GENERATION_BOOST = "IronsBotany_GenerationBoost";
    public static final String EFFICIENCY_BOOST = "IronsBotany_EfficiencyBoost";
    public static final String BURN_RATE_BOOST = "IronsBotany_BurnRateBoost";
    public static final String MANA_TO_ADD = "IronsBotany_ManaToAdd";
    public static final String SPEED_BOOST = "IronsBotany_SpeedBoost";
    public static final String ARCANE_RESONANCE = "IronsBotany_ArcaneResonance";
    public static final String RESONANCE_EXPIRY = "IronsBotany_ResonanceExpiry";
}
