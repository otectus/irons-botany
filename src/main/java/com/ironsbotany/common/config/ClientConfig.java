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

    static {
        BUILDER.push("Visual");
        ENABLE_MANA_PARTICLES = BUILDER
                .comment("Enable mana transfer particle effects and block entity renderer orbs")
                .define("enableManaParticles", true);

        PARTICLE_DENSITY = BUILDER
                .comment("Particle density (1-10, higher = more particles)")
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
                .comment("HUD scale")
                .defineInRange("hudScale", 1.0, 0.5, 2.0);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
