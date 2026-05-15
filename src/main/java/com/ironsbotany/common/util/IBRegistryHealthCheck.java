package com.ironsbotany.common.util;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.casting.CastingChannelRegistry;
import com.ironsbotany.common.flower.FlowerAuraRegistry;
import com.ironsbotany.common.spell.catalyst.SpellCatalystRegistry;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Asserts that every deep-synergy registry was actually populated during common
 * setup. Catches the kind of structural-but-unwired bug the 1.7.0 audit flagged:
 * registries exist, registration helpers exist, but nothing ever calls them, so
 * lookups silently return null.
 *
 * <p>Dev (data-gen / IDE / GameTest namespace active): throws on empty registry
 * to force a hard fail before testing.</p>
 * <p>Production: logs a warning so dedicated servers don't refuse to boot, but
 * the absence is visible in the log.</p>
 */
public final class IBRegistryHealthCheck {
    private IBRegistryHealthCheck() {}

    public static void runAfterSetup() {
        int catalysts = SpellCatalystRegistry.getAllEffects().size();
        int auras = FlowerAuraRegistry.getAllAuras().size();
        int channels = CastingChannelRegistry.getAllChannels().size();
        int channelBindings = CastingChannelRegistry.getItemBindingCount();

        IronsBotany.LOGGER.info(
            "[health-check] catalysts={} auras={} channels={} channel-bindings={}",
            catalysts, auras, channels, channelBindings);

        StringBuilder missing = new StringBuilder();
        if (catalysts == 0) missing.append("catalysts ");
        if (auras == 0) missing.append("auras ");
        if (channels == 0) missing.append("channels ");
        if (channelBindings == 0) missing.append("channel-bindings ");

        if (missing.length() == 0) return;

        String message = "Deep-synergy registries empty after setup: " + missing.toString().trim();
        if (isDevEnvironment()) {
            throw new IllegalStateException(message);
        }
        IronsBotany.LOGGER.warn(message);
    }

    private static boolean isDevEnvironment() {
        // Production-vs-dev signal: Forge's FMLEnvironment marks the production
        // bundle separately from the deobfuscated dev/IDE environment.
        return !FMLEnvironment.production;
    }
}
