package com.ironsbotany.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Master Toggles
    public static final ForgeConfigSpec.BooleanValue BARE_BONES_MODE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DEEP_SYNERGY;

    // Mana System
    public static final ForgeConfigSpec.EnumValue<ManaUnificationMode> MANA_UNIFICATION_MODE;
    public static final ForgeConfigSpec.IntValue MANA_CONVERSION_RATIO;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DUAL_COST_SPELLS;
    public static final ForgeConfigSpec.IntValue SPELL_RESERVOIR_CAPACITY;
    public static final ForgeConfigSpec.IntValue MANA_TRANSFER_RATE;
    // Mana Conduit
    public static final ForgeConfigSpec.IntValue MANA_CONDUIT_CAPACITY;
    public static final ForgeConfigSpec.IntValue MANA_CONDUIT_CONVERSION_RATE;
    public static final ForgeConfigSpec.IntValue MANA_CONDUIT_RADIUS;

    public static final ForgeConfigSpec.BooleanValue BIDIRECTIONAL_CONVERSION;
    public static final ForgeConfigSpec.IntValue REVERSE_CONVERSION_RATIO;
    public static final ForgeConfigSpec.IntValue BLOCK_ENTITY_TRANSFER_RATE;
    // Mana Pool Access
    public static final ForgeConfigSpec.BooleanValue ENABLE_MANA_POOL_ACCESS;
    public static final ForgeConfigSpec.IntValue MANA_POOL_SEARCH_RADIUS;

    // Spells
    public static final ForgeConfigSpec.DoubleValue BOTANICAL_SPELL_POWER_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BOTANICAL_SCHOOL;
    public static final ForgeConfigSpec.DoubleValue SPELL_COOLDOWN_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SPELL_PARTICLES;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BLOCK_AMBIENT_PARTICLES;

    // Equipment - Terrasteel Spell Blade
    public static final ForgeConfigSpec.DoubleValue TERRASTEEL_BLADE_SPELL_POWER;
    public static final ForgeConfigSpec.IntValue TERRASTEEL_BLADE_MAX_MANA;
    public static final ForgeConfigSpec.DoubleValue TERRASTEEL_BLADE_MANA_COST_REDUCTION;
    public static final ForgeConfigSpec.IntValue TERRASTEEL_BLADE_MANA_PER_HIT;

    // Equipment - Livingwood Staff
    public static final ForgeConfigSpec.DoubleValue LIVINGWOOD_STAFF_SPELL_POWER;
    public static final ForgeConfigSpec.IntValue LIVINGWOOD_STAFF_MANA_CAPACITY;

    // Equipment - Dreamwood Scepter
    public static final ForgeConfigSpec.DoubleValue DREAMWOOD_SCEPTER_SPELL_POWER;
    public static final ForgeConfigSpec.DoubleValue DREAMWOOD_CONVERSION_PERCENT;

    // Equipment - Gaia Spirit Wand
    public static final ForgeConfigSpec.DoubleValue GAIA_WAND_SPELL_POWER;
    public static final ForgeConfigSpec.DoubleValue GAIA_WAND_COOLDOWN_REDUCTION;

    // Equipment - Manasteel Armor
    public static final ForgeConfigSpec.DoubleValue MANASTEEL_ARMOR_SPELL_POWER;
    public static final ForgeConfigSpec.IntValue MANASTEEL_ARMOR_MAX_MANA;

    // Balance
    public static final ForgeConfigSpec.BooleanValue ENABLE_CROSS_LOOT;
    public static final ForgeConfigSpec.DoubleValue UPGRADE_ORB_EFFECTIVENESS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_VANILLA_LOOT_INJECTION;

    // Spell Mechanics
    public static final ForgeConfigSpec.BooleanValue MANA_REBIRTH_DEATH_PREVENTION;
    public static final ForgeConfigSpec.DoubleValue MANA_REBIRTH_REVIVE_HEALTH_PERCENT;
    public static final ForgeConfigSpec.IntValue MANA_REBIRTH_REVIVE_MANA_COST;
    public static final ForgeConfigSpec.IntValue FLOWER_SHIELD_BASE_HP;
    public static final ForgeConfigSpec.IntValue FLOWER_SHIELD_HP_PER_LEVEL;
    public static final ForgeConfigSpec.BooleanValue LIVING_ROOT_IMMOBILIZE;
    public static final ForgeConfigSpec.BooleanValue RUNIC_INFUSION_RUNE_SCALING;

    // Per-Spell Multipliers
    public static final ForgeConfigSpec.DoubleValue MANA_BLOOM_POWER;
    public static final ForgeConfigSpec.DoubleValue MANA_BLOOM_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue BOTANICAL_BURST_POWER;
    public static final ForgeConfigSpec.DoubleValue BOTANICAL_BURST_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue FLOWER_SHIELD_POWER;
    public static final ForgeConfigSpec.DoubleValue FLOWER_SHIELD_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue LIVING_ROOT_GRASP_POWER;
    public static final ForgeConfigSpec.DoubleValue LIVING_ROOT_GRASP_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue SPARK_SWARM_POWER;
    public static final ForgeConfigSpec.DoubleValue SPARK_SWARM_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue RUNIC_INFUSION_POWER;
    public static final ForgeConfigSpec.DoubleValue RUNIC_INFUSION_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue PETAL_STORM_POWER;
    public static final ForgeConfigSpec.DoubleValue PETAL_STORM_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue GAIA_WRATH_POWER;
    public static final ForgeConfigSpec.DoubleValue GAIA_WRATH_COOLDOWN;
    public static final ForgeConfigSpec.DoubleValue MANA_REBIRTH_POWER;
    public static final ForgeConfigSpec.DoubleValue MANA_REBIRTH_COOLDOWN;

    // Casting Channels
    public static final ForgeConfigSpec.BooleanValue ENABLE_CASTING_CHANNELS;
    public static final ForgeConfigSpec.DoubleValue CHANNEL_POWER_MULTIPLIER;

    // Spell Catalysts
    public static final ForgeConfigSpec.BooleanValue ENABLE_SPELL_CATALYSTS;
    public static final ForgeConfigSpec.DoubleValue CATALYST_CONSUMPTION_CHANCE;
    public static final ForgeConfigSpec.IntValue CATALYST_DURABILITY_DAMAGE;
    public static final ForgeConfigSpec.BooleanValue ALLOW_MULTIPLE_CATALYSTS;
    public static final ForgeConfigSpec.IntValue MAX_CATALYSTS_PER_SPELL;
    public static final ForgeConfigSpec.DoubleValue CATALYST_POWER_MULTIPLIER;

    // Flower Auras
    public static final ForgeConfigSpec.BooleanValue ENABLE_FLOWER_AURAS;
    public static final ForgeConfigSpec.DoubleValue FLOWER_AURA_RANGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue FLOWER_AURA_STRENGTH_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue ALLOW_AURA_STACKING;
    public static final ForgeConfigSpec.IntValue MAX_ACTIVE_AURAS;
    public static final ForgeConfigSpec.BooleanValue SHOW_AURA_PARTICLES;

    // Spell-Triggered Mana Events
    public static final ForgeConfigSpec.BooleanValue ENABLE_SPELL_MANA_EVENTS;
    public static final ForgeConfigSpec.IntValue MANA_EVENT_DURATION;
    public static final ForgeConfigSpec.DoubleValue MANA_EVENT_INTENSITY;
    public static final ForgeConfigSpec.IntValue MANA_EVENT_RADIUS;

    // Corporea Logistics
    public static final ForgeConfigSpec.BooleanValue ENABLE_CORPOREA_LOGISTICS;
    public static final ForgeConfigSpec.BooleanValue AUTO_REQUEST_REAGENTS;
    public static final ForgeConfigSpec.IntValue CORPOREA_SEARCH_RADIUS;

    // Alfheim Integration
    public static final ForgeConfigSpec.BooleanValue ENABLE_ALFHEIM_BOOST;
    public static final ForgeConfigSpec.DoubleValue ALFHEIM_POWER_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DUAL_SCHOOL_SCROLLS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SPELLBOOK_ATTUNEMENT;

    static {
        BUILDER.push("Master Toggles");
        BUILDER.comment("=".repeat(60));
        BUILDER.comment("MASTER CONTROL - Use these for quick configuration");
        BUILDER.comment("=".repeat(60));
        
        BARE_BONES_MODE = BUILDER
                .comment("BARE-BONES MODE: Disable all deep synergy features.",
                         "Only mana conversion remains active.",
                         "Perfect for simple integration or performance-critical servers.")
                .define("bareBonesMode", false);
        
        ENABLE_DEEP_SYNERGY = BUILDER
                .comment("DEEP SYNERGY MASTER TOGGLE: Enable all 6 deep synergy stages.",
                         "Disable this to turn off catalysts, channels, auras, events, corporea, and alfheim.",
                         "Overrides individual stage toggles when set to false.")
                .define("enableDeepSynergy", true);
        BUILDER.pop();

        BUILDER.push("Mana System");
        BUILDER.comment("=".repeat(60));
        BUILDER.comment("MANA UNIFICATION - Choose how Botania and ISS mana interact");
        BUILDER.comment("=".repeat(60));
        
        MANA_UNIFICATION_MODE = BUILDER
                .comment("Mana unification mode:",
                         "BOTANIA_PRIMARY - ISS spells consume Botania mana directly",
                         "ISS_PRIMARY - Botania items grant ISS mana",
                         "HYBRID - Both systems work together with conversion (default)",
                         "SEPARATE - No conversion, dual-cost required",
                         "DISABLED - No mana integration")
                .defineEnum("manaUnificationMode", ManaUnificationMode.HYBRID);
        
        MANA_CONVERSION_RATIO = BUILDER
                .comment("How much Botania mana equals 1 ISS mana (default: 1000)",
                         "Only used in HYBRID and ISS_PRIMARY modes")
                .defineInRange("conversionRatio", 1000, 100, 10000);
        
        BIDIRECTIONAL_CONVERSION = BUILDER
                .comment("Allow converting ISS mana back to Botania mana",
                         "Only used in HYBRID mode")
                .define("bidirectionalConversion", false);
        
        REVERSE_CONVERSION_RATIO = BUILDER
                .comment("How much ISS mana equals 1 Botania mana for reverse conversion",
                         "Only used when bidirectionalConversion is true")
                .defineInRange("reverseConversionRatio", 800, 100, 10000);
        
        ENABLE_DUAL_COST_SPELLS = BUILDER
                .comment("Enable Botanical spells that consume both mana types",
                         "Forced to true in SEPARATE mode, ignored in BOTANIA_PRIMARY and ISS_PRIMARY")
                .define("enableDualCost", true);
        
        SPELL_RESERVOIR_CAPACITY = BUILDER
                .comment("Max ISS mana in Spell Reservoir augmented pools")
                .defineInRange("reservoirCapacity", 1000, 500, 5000);
        
        MANA_TRANSFER_RATE = BUILDER
                .comment("Mana transfer rate per second from Botanical Focus (Botania mana)")
                .defineInRange("manaTransferRate", 1000, 100, 10000);

        MANA_CONDUIT_CAPACITY = BUILDER
                .comment("Max ISS mana stored in a Mana Conduit block")
                .defineInRange("manaConduitCapacity", 500, 100, 5000);

        MANA_CONDUIT_CONVERSION_RATE = BUILDER
                .comment("Botania mana drained per tick cycle from an adjacent mana pool")
                .defineInRange("manaConduitConversionRate", 5000, 500, 50000);

        MANA_CONDUIT_RADIUS = BUILDER
                .comment("Radius (in blocks) within which the Mana Conduit distributes ISS mana to players")
                .defineInRange("manaConduitRadius", 8, 1, 32);

        BLOCK_ENTITY_TRANSFER_RATE = BUILDER
                .comment("ISS mana transferred per second from functional blocks (Conduit, Reservoir) to nearby players")
                .defineInRange("blockEntityTransferRate", 5, 1, 100);

        ENABLE_MANA_POOL_ACCESS = BUILDER
                .comment("Allow spells to draw Botania mana directly from nearby mana pools",
                         "When enabled, spells check nearby pools as a fallback if inventory mana is insufficient")
                .define("enableManaPoolAccess", true);

        MANA_POOL_SEARCH_RADIUS = BUILDER
                .comment("Radius to search for Botania mana pools when drawing mana for spells")
                .defineInRange("manaPoolSearchRadius", 8, 4, 16);
        BUILDER.pop();

        BUILDER.push("Spells");
        BOTANICAL_SPELL_POWER_MULTIPLIER = BUILDER
                .comment("Damage multiplier for Botanical school spells")
                .defineInRange("botanicalPowerMultiplier", 1.0, 0.5, 2.0);
        
        ENABLE_BOTANICAL_SCHOOL = BUILDER
                .comment("Enable the Botanical spell school")
                .define("enableBotanicalSchool", true);
        
        SPELL_COOLDOWN_MULTIPLIER = BUILDER
                .comment("Cooldown multiplier for Botanical spells")
                .defineInRange("spellCooldownMultiplier", 1.0, 0.5, 2.0);

        ENABLE_SPELL_PARTICLES = BUILDER
                .comment("Emit Iron's Botany-signature particles on spell cast (petal/botanical_burst/mana_transfer layered onto vanilla VFX). Server-authoritative.")
                .define("enableSpellParticles", true);

        ENABLE_BLOCK_AMBIENT_PARTICLES = BUILDER
                .comment("Emit ambient particles from active Spell Reservoir and Mana Conduit blocks.")
                .define("enableBlockAmbientParticles", true);
        BUILDER.pop();

        BUILDER.push("Equipment");
        BUILDER.comment("=".repeat(60));
        BUILDER.comment("EQUIPMENT - Stat customization for all botanical weapons and armor");
        BUILDER.comment("=".repeat(60));

        BUILDER.push("Terrasteel Spell Blade");
        TERRASTEEL_BLADE_SPELL_POWER = BUILDER
                .comment("Spell power bonus from Terrasteel Spell Blade (0.25 = +25%)")
                .defineInRange("terrasteelBladeSpellPower", 0.25, 0.0, 1.0);

        TERRASTEEL_BLADE_MAX_MANA = BUILDER
                .comment("Max mana bonus from Terrasteel Spell Blade")
                .defineInRange("terrasteelBladeMaxMana", 200, 0, 1000);

        TERRASTEEL_BLADE_MANA_COST_REDUCTION = BUILDER
                .comment("Mana cost reduction from Terrasteel Spell Blade (0.2 = -20%)")
                .defineInRange("terrasteelBladeCostReduction", 0.2, 0.0, 0.5);

        TERRASTEEL_BLADE_MANA_PER_HIT = BUILDER
                .comment("Botania mana generated per melee hit with Terrasteel Spell Blade")
                .defineInRange("terrasteelBladeManaPerHit", 5000, 0, 50000);
        BUILDER.pop();

        BUILDER.push("Livingwood Staff");
        LIVINGWOOD_STAFF_SPELL_POWER = BUILDER
                .comment("Botanical spell power bonus from Livingwood Staff (0.1 = +10%)")
                .defineInRange("livingwoodStaffSpellPower", 0.1, 0.0, 1.0);

        LIVINGWOOD_STAFF_MANA_CAPACITY = BUILDER
                .comment("Botania mana storage capacity of Livingwood Staff")
                .defineInRange("livingwoodStaffManaCapacity", 500000, 100000, 2000000);
        BUILDER.pop();

        BUILDER.push("Dreamwood Scepter");
        DREAMWOOD_SCEPTER_SPELL_POWER = BUILDER
                .comment("Botanical spell power bonus from Dreamwood Scepter (0.2 = +20%)")
                .defineInRange("dreamwoodScepterSpellPower", 0.2, 0.0, 1.0);

        DREAMWOOD_CONVERSION_PERCENT = BUILDER
                .comment("Percentage of ISS mana cost converted to Botania mana (1.0 = 100%)")
                .defineInRange("dreamwoodConversionPercent", 1.0, 0.1, 1.0);
        BUILDER.pop();

        BUILDER.push("Gaia Spirit Wand");
        GAIA_WAND_SPELL_POWER = BUILDER
                .comment("Spell power bonus from Gaia Spirit Wand (0.3 = +30%)")
                .defineInRange("gaiaWandSpellPower", 0.3, 0.0, 1.0);

        GAIA_WAND_COOLDOWN_REDUCTION = BUILDER
                .comment("Cooldown reduction from Gaia Spirit Wand (0.25 = -25%)")
                .defineInRange("gaiaWandCooldownReduction", 0.25, 0.0, 0.5);
        BUILDER.pop();

        BUILDER.push("Manasteel Armor");
        MANASTEEL_ARMOR_SPELL_POWER = BUILDER
                .comment("Spell power bonus per piece of Manasteel Wizard Armor")
                .defineInRange("manasteelArmorSpellPower", 0.15, 0.0, 1.0);

        MANASTEEL_ARMOR_MAX_MANA = BUILDER
                .comment("Max mana bonus per piece of Manasteel Wizard Armor")
                .defineInRange("manasteelArmorMaxMana", 150, 0, 500);
        BUILDER.pop();

        BUILDER.pop(); // Equipment

        BUILDER.push("Spell Mechanics");
        BUILDER.comment("=".repeat(60));
        BUILDER.comment("SPELL MECHANICS - Fine-tune individual spell behaviors");
        BUILDER.comment("=".repeat(60));

        MANA_REBIRTH_DEATH_PREVENTION = BUILDER
                .comment("Enable Mana Rebirth death prevention (totem-like mechanic)")
                .define("manaRebirthDeathPrevention", true);

        MANA_REBIRTH_REVIVE_HEALTH_PERCENT = BUILDER
                .comment("Health percentage on revive (0.3 = 30% of max HP)")
                .defineInRange("manaRebirthReviveHealth", 0.3, 0.1, 1.0);

        MANA_REBIRTH_REVIVE_MANA_COST = BUILDER
                .comment("Botania mana consumed on death prevention")
                .defineInRange("manaRebirthReviveManaCost", 100000, 10000, 1000000);

        FLOWER_SHIELD_BASE_HP = BUILDER
                .comment("Base shield HP for Flower Shield spell")
                .defineInRange("flowerShieldBaseHp", 20, 5, 100);

        FLOWER_SHIELD_HP_PER_LEVEL = BUILDER
                .comment("Additional shield HP per spell level")
                .defineInRange("flowerShieldHpPerLevel", 5, 1, 20);

        LIVING_ROOT_IMMOBILIZE = BUILDER
                .comment("Use full immobilization (Slowness 255) instead of Slowness IV")
                .define("livingRootImmobilize", true);

        RUNIC_INFUSION_RUNE_SCALING = BUILDER
                .comment("Scale Runic Infusion effects based on runes in inventory")
                .define("runicInfusionRuneScaling", true);
        BUILDER.pop();

        BUILDER.push("Per-Spell Multipliers");
        BUILDER.comment("=".repeat(60));
        BUILDER.comment("PER-SPELL TUNING - Power and cooldown multipliers for each spell");
        BUILDER.comment("=".repeat(60));

        MANA_BLOOM_POWER = BUILDER.comment("Mana Bloom power multiplier").defineInRange("manaBloomPower", 1.0, 0.1, 5.0);
        MANA_BLOOM_COOLDOWN = BUILDER.comment("Mana Bloom cooldown multiplier").defineInRange("manaBloomCooldown", 1.0, 0.1, 5.0);
        BOTANICAL_BURST_POWER = BUILDER.comment("Botanical Burst power multiplier").defineInRange("botanicalBurstPower", 1.0, 0.1, 5.0);
        BOTANICAL_BURST_COOLDOWN = BUILDER.comment("Botanical Burst cooldown multiplier").defineInRange("botanicalBurstCooldown", 1.0, 0.1, 5.0);
        FLOWER_SHIELD_POWER = BUILDER.comment("Flower Shield power multiplier").defineInRange("flowerShieldPower", 1.0, 0.1, 5.0);
        FLOWER_SHIELD_COOLDOWN = BUILDER.comment("Flower Shield cooldown multiplier").defineInRange("flowerShieldCooldown", 1.0, 0.1, 5.0);
        LIVING_ROOT_GRASP_POWER = BUILDER.comment("Living Root Grasp power multiplier").defineInRange("livingRootGraspPower", 1.0, 0.1, 5.0);
        LIVING_ROOT_GRASP_COOLDOWN = BUILDER.comment("Living Root Grasp cooldown multiplier").defineInRange("livingRootGraspCooldown", 1.0, 0.1, 5.0);
        SPARK_SWARM_POWER = BUILDER.comment("Spark Swarm power multiplier").defineInRange("sparkSwarmPower", 1.0, 0.1, 5.0);
        SPARK_SWARM_COOLDOWN = BUILDER.comment("Spark Swarm cooldown multiplier").defineInRange("sparkSwarmCooldown", 1.0, 0.1, 5.0);
        RUNIC_INFUSION_POWER = BUILDER.comment("Runic Infusion power multiplier").defineInRange("runicInfusionPower", 1.0, 0.1, 5.0);
        RUNIC_INFUSION_COOLDOWN = BUILDER.comment("Runic Infusion cooldown multiplier").defineInRange("runicInfusionCooldown", 1.0, 0.1, 5.0);
        PETAL_STORM_POWER = BUILDER.comment("Petal Storm power multiplier").defineInRange("petalStormPower", 1.0, 0.1, 5.0);
        PETAL_STORM_COOLDOWN = BUILDER.comment("Petal Storm cooldown multiplier").defineInRange("petalStormCooldown", 1.0, 0.1, 5.0);
        GAIA_WRATH_POWER = BUILDER.comment("Gaia's Wrath power multiplier").defineInRange("gaiaWrathPower", 1.0, 0.1, 5.0);
        GAIA_WRATH_COOLDOWN = BUILDER.comment("Gaia's Wrath cooldown multiplier").defineInRange("gaiaWrathCooldown", 1.0, 0.1, 5.0);
        MANA_REBIRTH_POWER = BUILDER.comment("Mana Rebirth power multiplier").defineInRange("manaRebirthPower", 1.0, 0.1, 5.0);
        MANA_REBIRTH_COOLDOWN = BUILDER.comment("Mana Rebirth cooldown multiplier").defineInRange("manaRebirthCooldown", 1.0, 0.1, 5.0);
        BUILDER.pop();

        BUILDER.push("Balance");
        ENABLE_CROSS_LOOT = BUILDER
                .comment("Enable cross-mod loot injection")
                .define("enableCrossLoot", true);

        UPGRADE_ORB_EFFECTIVENESS = BUILDER
                .comment("Effectiveness multiplier for Botanical Upgrade Orbs")
                .defineInRange("upgradeOrbEffectiveness", 1.0, 0.5, 2.0);

        ENABLE_VANILLA_LOOT_INJECTION = BUILDER
                .comment("Inject Iron's Botany items into vanilla structure chests",
                         "Adds Spell Petals to villages, Botanical Crystals to mineshafts, etc.")
                .define("enableVanillaLootInjection", true);
        BUILDER.pop();

        BUILDER.push("Casting Channels");
        BUILDER.comment("Stage 2: Hardware vs Software - Casting performance profiles");
        
        ENABLE_CASTING_CHANNELS = BUILDER
                .comment("Enable casting channel system",
                         "Disabled in bare-bones mode")
                .define("enableCastingChannels", true);
        
        CHANNEL_POWER_MULTIPLIER = BUILDER
                .comment("Global multiplier for casting channel effects")
                .defineInRange("channelPowerMultiplier", 1.0, 0.1, 5.0);
        BUILDER.pop();

        BUILDER.push("Spell Catalysts");
        BUILDER.comment("Stage 1: Spell Augmentation - Items modify spell behavior");
        
        ENABLE_SPELL_CATALYSTS = BUILDER
                .comment("Enable Botania items as spell catalysts",
                         "Disabled in bare-bones mode")
                .define("enableSpellCatalysts", true);
        
        CATALYST_CONSUMPTION_CHANCE = BUILDER
                .comment("Chance (0.0-1.0) that catalyst is consumed on spell cast")
                .defineInRange("catalystConsumptionChance", 0.0, 0.0, 1.0);
        
        CATALYST_DURABILITY_DAMAGE = BUILDER
                .comment("Durability damage to catalyst items per spell cast (0 = no damage)")
                .defineInRange("catalystDurabilityDamage", 0, 0, 100);
        
        ALLOW_MULTIPLE_CATALYSTS = BUILDER
                .comment("Allow multiple catalysts to stack effects")
                .define("allowMultipleCatalysts", true);
        
        MAX_CATALYSTS_PER_SPELL = BUILDER
                .comment("Maximum number of catalysts that can affect one spell")
                .defineInRange("maxCatalystsPerSpell", 3, 1, 10);
        
        CATALYST_POWER_MULTIPLIER = BUILDER
                .comment("Global multiplier for catalyst effect strength")
                .defineInRange("catalystPowerMultiplier", 1.0, 0.1, 5.0);
        BUILDER.pop();

        BUILDER.push("Flower Auras");
        ENABLE_FLOWER_AURAS = BUILDER
                .comment("Enable functional flowers as spell enhancers")
                .define("enableFlowerAuras", true);
        
        FLOWER_AURA_RANGE_MULTIPLIER = BUILDER
                .comment("Multiplier for flower aura ranges")
                .defineInRange("flowerAuraRangeMultiplier", 1.0, 0.1, 5.0);
        
        FLOWER_AURA_STRENGTH_MULTIPLIER = BUILDER
                .comment("Multiplier for flower aura effect strength")
                .defineInRange("flowerAuraStrengthMultiplier", 1.0, 0.1, 5.0);
        
        ALLOW_AURA_STACKING = BUILDER
                .comment("Allow multiple flower auras to stack")
                .define("allowAuraStacking", true);
        
        MAX_ACTIVE_AURAS = BUILDER
                .comment("Maximum number of flower auras that can affect one spell")
                .defineInRange("maxActiveAuras", 5, 1, 20);
        
        SHOW_AURA_PARTICLES = BUILDER
                .comment("Show particle effects for active auras")
                .define("showAuraParticles", true);
        BUILDER.pop();

        BUILDER.push("Spell-Triggered Mana Events");
        ENABLE_SPELL_MANA_EVENTS = BUILDER
                .comment("EXPERIMENTAL: Enable spells to affect Botania mana networks. Most effects are not yet fully functional except Water Fill.")
                .define("enableSpellManaEvents", false);
        
        MANA_EVENT_DURATION = BUILDER
                .comment("Duration of spell-triggered mana effects (ticks)")
                .defineInRange("manaEventDuration", 200, 20, 6000);
        
        MANA_EVENT_INTENSITY = BUILDER
                .comment("Intensity multiplier for mana network effects")
                .defineInRange("manaEventIntensity", 1.0, 0.1, 5.0);
        
        MANA_EVENT_RADIUS = BUILDER
                .comment("Radius for spell-triggered mana effects")
                .defineInRange("manaEventRadius", 8, 1, 32);
        BUILDER.pop();

        BUILDER.push("Corporea Logistics");
        ENABLE_CORPOREA_LOGISTICS = BUILDER
                .comment("Enable Corporea integration for spell logistics")
                .define("enableCorporeaLogistics", true);
        
        AUTO_REQUEST_REAGENTS = BUILDER
                .comment("Automatically request spell reagents from Corporea")
                .define("autoRequestReagents", true);
        
        CORPOREA_SEARCH_RADIUS = BUILDER
                .comment("Radius to search for Corporea Index")
                .defineInRange("corporeaSearchRadius", 16, 4, 64);
        BUILDER.pop();

        BUILDER.push("Alfheim Integration");
        ENABLE_ALFHEIM_BOOST = BUILDER
                .comment("Enable spell power boost in Alfheim dimension")
                .define("enableAlfheimBoost", true);
        
        ALFHEIM_POWER_MULTIPLIER = BUILDER
                .comment("Power multiplier for spells cast in Alfheim")
                .defineInRange("alfheimPowerMultiplier", 1.5, 1.0, 5.0);
        
        ENABLE_DUAL_SCHOOL_SCROLLS = BUILDER
                .comment("Enable dual-school scrolls crafted in Alfheim")
                .define("enableDualSchoolScrolls", true);
        
        ENABLE_SPELLBOOK_ATTUNEMENT = BUILDER
                .comment("Enable spellbook attunement in Alfheim")
                .define("enableSpellbookAttunement", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
