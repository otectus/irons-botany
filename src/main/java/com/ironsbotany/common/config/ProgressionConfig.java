package com.ironsbotany.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Maps upstream advancement resource locations to Iron's Botany progression
 * flags. Stored in server config so packs can retarget the gates without a
 * Java change when upstream renames an advancement.
 *
 * The defaults match Botania 1.20.1-450. {@code UnifiedAdvancementSystem}
 * validates that each ID resolves at startup; missing IDs log a warning but
 * never throw, so a pack with a renamed Botania never blocks server boot.
 */
public class ProgressionConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<String> BOTANIA_TERRASTEEL_ADVANCEMENT;
    public static final ForgeConfigSpec.ConfigValue<String> BOTANIA_ALFHEIM_ADVANCEMENT;
    public static final ForgeConfigSpec.ConfigValue<String> BOTANIA_GAIA_ADVANCEMENT;

    static {
        BUILDER.push("Progression Gates");
        BUILDER.comment(
            "Upstream advancement IDs that drive Iron's Botany's cross-mod progression.",
            "Defaults target Botania 1.20.1-450.",
            "Missing IDs at startup log a warning and the corresponding gate stays locked.");

        BOTANIA_TERRASTEEL_ADVANCEMENT = BUILDER
            .comment("Earning this advancement unlocks LEGENDARY-tier spell catalysts.")
            .define("terrasteelAdvancement", "botania:main/terrasteel_pickup");

        BOTANIA_ALFHEIM_ADVANCEMENT = BUILDER
            .comment("Earning this advancement unlocks dual-school scroll crafting.")
            .define("alfheimAdvancement", "botania:main/alfheim_portal_open");

        BOTANIA_GAIA_ADVANCEMENT = BUILDER
            .comment("Earning this advancement enables Spell Overcharge (+5% Nature damage).")
            .define("gaiaAdvancement", "botania:challenge/gaia_guardian_kill");
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
