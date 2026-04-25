package com.ironsbotany.common.bridge;

/**
 * Outcome of {@link ManaBridgeManager#resolveCost}. Records exactly how many
 * units were charged from each pool so downstream listeners (HUD sync,
 * advancement criteria, telemetry) don't have to re-derive the split.
 *
 * <p>{@code ok} is the cancellation contract: {@code false} means the player
 * could not afford the spell and the caller must abort the cast.
 */
public record ManaResolutionResult(int issCharged, int botaniaCharged, boolean ok) {

    public static final ManaResolutionResult INSUFFICIENT = new ManaResolutionResult(0, 0, false);
    public static final ManaResolutionResult NOOP = new ManaResolutionResult(0, 0, true);

    public static ManaResolutionResult issOnly(int iss) {
        return new ManaResolutionResult(iss, 0, true);
    }

    public static ManaResolutionResult botaniaOnly(int botania) {
        return new ManaResolutionResult(0, botania, true);
    }

    public static ManaResolutionResult dual(int iss, int botania) {
        return new ManaResolutionResult(iss, botania, true);
    }
}
