package com.ironsbotany.common.bridge.mana;

/**
 * The single place where a Botania mana cost is composed from its modifiers.
 *
 * <p>Every multiplier in the mod funnels through {@link #compose} so that stacking order is
 * defined once, each factor is applied exactly once, and the arithmetic is overflow-safe.
 * Through 1.9.0 the channel multiplier was applied in three different call sites, the mana
 * efficiency attribute was applied nowhere, and the conversion multiply was done in {@code int}.
 *
 * <h3>Documented stacking order</h3>
 * <ol>
 *   <li><b>base</b> — the spell's Botania cost ladder, or {@code issCost × conversionRatio} for a
 *       non-Botanical spell;</li>
 *   <li><b>channel</b> — the casting channel bound to the <em>actual casting stack</em>
 *       (Livingwood −10%, Dreamwood +10%, Terra Rod +30%);</li>
 *   <li><b>efficiency</b> — the summed {@code ironsbotany:mana_efficiency} attribute, as a
 *       fractional discount;</li>
 *   <li><b>set bonus</b> — the full Manasteel Wizard set discount.</li>
 * </ol>
 * Multiplicative, in that order, rounded once at the end. Applying discounts multiplicatively
 * rather than additively is what stops a player stacking several sources to a free cast.
 */
public final class ManaCostComposer {

    private ManaCostComposer() {}

    /**
     * @param base            non-negative base cost in Botania mana
     * @param channelFactor   multiplier from the held casting channel; {@code 1.0} for none
     * @param efficiency      fractional discount in {@code [0, 1)} from the mana-efficiency
     *                        attribute; {@code 0.0} for none
     * @param setFactor       multiplier from an armour set discount; {@code 1.0} for none
     * @return the composed cost, clamped to {@code [0, Integer.MAX_VALUE]}
     * @throws IllegalArgumentException if any factor is NaN or infinite — a non-finite multiplier
     *         would otherwise produce a nonsensical cost (or 0, i.e. a free cast) silently
     */
    public static int compose(int base, double channelFactor, double efficiency, double setFactor) {
        if (base <= 0) return 0;
        requireFinite(channelFactor, "channelFactor");
        requireFinite(efficiency, "efficiency");
        requireFinite(setFactor, "setFactor");

        double efficiencyFactor = 1.0 - clamp(efficiency, 0.0, 0.99);
        double result = (double) base
                * Math.max(0.0, channelFactor)
                * efficiencyFactor
                * clamp(setFactor, 0.0, 1.0);

        return saturate(Math.round(result));
    }

    /**
     * Convert an ISS mana cost into Botania units.
     *
     * <p>Multiplies in {@code long}. {@code issCost * ratio} in {@code int} overflows for a
     * high-cost spell at a large configured ratio and can wrap to a negative — i.e. a free cast.
     */
    public static int convertIssToBotania(int issCost, int conversionRatio) {
        if (issCost <= 0 || conversionRatio <= 0) return 0;
        return saturate((long) issCost * (long) conversionRatio);
    }

    /**
     * Convert Botania mana into ISS units, flooring. Returns 0 for a non-positive ratio rather
     * than dividing by zero.
     */
    public static int convertBotaniaToIss(int botaniaAmount, int conversionRatio) {
        if (botaniaAmount <= 0 || conversionRatio <= 0) return 0;
        return (int) ((long) botaniaAmount / (long) conversionRatio);
    }

    /**
     * The exact Botania amount that maps to {@code issAmount} at {@code conversionRatio}.
     *
     * <p>Used to avoid over-charging on conversion: {@link #convertBotaniaToIss} floors, so
     * draining the raw transfer rate would take mana the player is not credited for.
     */
    public static int botaniaCostForIssGain(int issAmount, int conversionRatio) {
        if (issAmount <= 0 || conversionRatio <= 0) return 0;
        return saturate((long) issAmount * (long) conversionRatio);
    }

    private static int saturate(long value) {
        if (value <= 0L) return 0;
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite but was " + value);
        }
    }
}
