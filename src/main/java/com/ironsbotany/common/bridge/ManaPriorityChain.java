package com.ironsbotany.common.bridge;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.compat.ArsNSpellsCompat;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.util.BotaniaIntegration;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the configured mana resource ordering. The user (or modpack
 * dev) supplies a list like {@code ["botania","ars","iss"]} and this
 * class:
 * <ul>
 *   <li>parses it,</li>
 *   <li>filters out sources whose mod is absent,</li>
 *   <li>guarantees ISS appears as a final fallback,</li>
 *   <li>logs a warning once per session if the input was invalid.</li>
 * </ul>
 *
 * <p>The chain is consulted by {@link ManaBridgeManager} when a Botanical
 * spell needs to charge cost from "whatever pool the player has set up,"
 * rather than the hard-coded Botania-first behavior of the simpler modes.
 *
 * <p>Live config: the list is re-parsed on every call so {@code /irons_botany
 * reload} picks up changes without restart.
 */
public final class ManaPriorityChain {

    private static boolean warnedInvalidToken = false;

    private ManaPriorityChain() {}

    /**
     * @return the active priority chain, with absent-mod sources removed.
     *         Always ends with {@link ManaSource#ISS} as a safety net.
     */
    public static List<ManaSource> resolve() {
        List<? extends String> raw = CommonConfig.MANA_PRIORITY_CHAIN.get();
        List<ManaSource> result = new ArrayList<>(raw.size() + 1);

        for (String token : raw) {
            ManaSource source = ManaSource.fromToken(token);
            if (source == null) {
                if (!warnedInvalidToken) {
                    warnedInvalidToken = true;
                    IronsBotany.LOGGER.warn(
                        "ManaPriorityChain: ignoring unknown source token '{}'. "
                            + "Valid tokens: botania, ars, iss, lp, aura.", token);
                }
                continue;
            }
            if (!isSourceAvailable(source)) continue;
            if (result.contains(source)) continue;
            result.add(source);
        }

        if (!result.contains(ManaSource.ISS)) {
            result.add(ManaSource.ISS);
        }
        return result;
    }

    private static boolean isSourceAvailable(ManaSource source) {
        return switch (source) {
            case BOTANIA -> BotaniaIntegration.isBotaniaLoaded();
            case ARS     -> ArsNSpellsCompat.isLoaded();
            case ISS     -> true; // hard-dep
            case LP, AURA -> false; // not yet supported; keep enum for forward-compat
        };
    }
}
