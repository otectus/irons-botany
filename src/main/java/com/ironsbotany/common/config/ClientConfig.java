package com.ironsbotany.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Visual Settings
    public static final ForgeConfigSpec.BooleanValue ENABLE_MANA_PARTICLES;
    public static final ForgeConfigSpec.IntValue PARTICLE_DENSITY;

    // HUD Settings
    public static final ForgeConfigSpec.BooleanValue SHOW_MANA_HUD;
    public static final ForgeConfigSpec.IntValue HUD_X_OFFSET;
    public static final ForgeConfigSpec.IntValue HUD_Y_OFFSET;
    public static final ForgeConfigSpec.DoubleValue HUD_SCALE;
    /** Accessibility: when false the nearby-block indicator is drawn steady instead of pulsing. */
    public static final ForgeConfigSpec.BooleanValue HUD_PULSE_NEARBY;

    static {
        BUILDER.push("Visual");
        ENABLE_MANA_PARTICLES = BUILDER
                .comment("Enable mana transfer particle effects and block entity renderer orbs")
                .define("enableManaParticles", true);

        PARTICLE_DENSITY = BUILDER
                .comment("[NOT IMPLEMENTED in 1.10.0 - this setting has no effect]",
                         "Iron's Botany spawns its particles server-side via ServerLevel.sendParticles,",
                         "which chooses the count before the packet is sent, so a client-side setting",
                         "cannot influence it. Use Video Settings -> Particles to reduce particles on",
                         "this client; the vanilla particle engine honours it for every mod.",
                         "See docs/SYSTEM-DECISIONS-1.10.0.md.")
                .defineInRange("particleDensity", 5, 1, 10);
        BUILDER.pop();

        BUILDER.push("HUD");
        SHOW_MANA_HUD = BUILDER
                .comment("Show Botania mana indicator on HUD")
                .define("showManaHUD", true);
        
        HUD_X_OFFSET = BUILDER
                .comment("HUD X position offset")
                .defineInRange("hudXOffset", 100, -500, 500);
        
        HUD_Y_OFFSET = BUILDER
                .comment("HUD Y position offset")
                .defineInRange("hudYOffset", -60, -500, 500);
        
        HUD_SCALE = BUILDER
                .comment("HUD scale (0.5 - 2.0). The bar is kept fully on screen at its drawn size.",
                         "Before 1.10.0 this value was read from the config but never applied.")
                .defineInRange("hudScale", 1.0, 0.5, 2.0);

        HUD_PULSE_NEARBY = BUILDER
                .comment("Pulse the mana bar's border while an Iron's Botany block holding mana is",
                         "nearby. Set to false to keep the indicator but stop it animating -",
                         "an accessibility option for motion sensitivity. The indicator itself",
                         "still appears, drawn steady.")
                .define("hudPulseNearby", true);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
