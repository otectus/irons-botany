package com.ironsbotany.common.bridge;

import java.util.Locale;

/**
 * Enumerates the mana resource pools that may participate in routing.
 * Used by {@link ManaPriorityChain} to express user-configurable
 * ordering across mods (Botania, Ars 'n Spells, ISS, plus reach-goal
 * sources for Covenant LP / Aura).
 *
 * <p>Adding a new source is a two-step process: declare the enum value
 * and teach {@link ManaBridgeManager} to charge it under the chain.
 * Sources whose mod is absent at runtime are simply skipped — the chain
 * itself never errors on a missing dependency.
 */
public enum ManaSource {
    BOTANIA,
    ARS,
    ISS,
    LP,
    AURA;

    public String token() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static ManaSource fromToken(String token) {
        if (token == null) return null;
        try {
            return ManaSource.valueOf(token.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
