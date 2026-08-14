package com.ironsbotany.common.bridge.mana;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManaCostComposerTest {

    private static final double NO_CHANNEL = 1.0;
    private static final double NO_EFFICIENCY = 0.0;
    private static final double NO_SET_BONUS = 1.0;

    @Test
    void unmodifiedCostPassesThrough() {
        assertEquals(10_000, ManaCostComposer.compose(10_000, NO_CHANNEL, NO_EFFICIENCY, NO_SET_BONUS));
    }

    @Test
    void zeroAndNegativeBaseAreFree() {
        assertEquals(0, ManaCostComposer.compose(0, 0.5, 0.5, 0.5));
        assertEquals(0, ManaCostComposer.compose(-1, NO_CHANNEL, NO_EFFICIENCY, NO_SET_BONUS));
    }

    @Test
    @DisplayName("each modifier applies once, multiplicatively, in the documented order")
    void modifiersCompose() {
        // Livingwood channel -10%, 15% efficiency, Manasteel set -10%.
        // 10000 * 0.90 * 0.85 * 0.90 = 6885
        assertEquals(6885, ManaCostComposer.compose(10_000, 0.90, 0.15, 0.90));
    }

    @Test
    @DisplayName("stacked discounts multiply rather than sum, so they cannot reach a free cast")
    void discountsCannotReachZero() {
        // Additive stacking of 0.75 + 0.5 + 0.5 would be >100% off. Multiplicative is 12.5%.
        int cost = ManaCostComposer.compose(1000, 0.5, 0.75, 0.5);
        assertEquals(63, cost);
        assertTrue(cost > 0, "a cast must never become free through stacking");
    }

    @Test
    @DisplayName("efficiency is capped below 1.0 even if an attribute somehow exceeds it")
    void efficiencyIsCapped() {
        assertTrue(ManaCostComposer.compose(1000, NO_CHANNEL, 5.0, NO_SET_BONUS) > 0);
        assertEquals(10, ManaCostComposer.compose(1000, NO_CHANNEL, 0.99, NO_SET_BONUS));
    }

    @Test
    void channelCanIncreaseCost() {
        // Terra Rod is +30%.
        assertEquals(13_000, ManaCostComposer.compose(10_000, 1.30, NO_EFFICIENCY, NO_SET_BONUS));
    }

    @Test
    @DisplayName("a non-finite multiplier is rejected, not silently turned into a free cast")
    void nonFiniteFactorsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> ManaCostComposer.compose(100, Double.NaN, NO_EFFICIENCY, NO_SET_BONUS));
        assertThrows(IllegalArgumentException.class,
                () -> ManaCostComposer.compose(100, NO_CHANNEL, Double.POSITIVE_INFINITY, NO_SET_BONUS));
        assertThrows(IllegalArgumentException.class,
                () -> ManaCostComposer.compose(100, NO_CHANNEL, NO_EFFICIENCY, Double.NaN));
    }

    @Test
    @DisplayName("composition saturates instead of overflowing to a negative cost")
    void compositionSaturates() {
        assertEquals(Integer.MAX_VALUE,
                ManaCostComposer.compose(Integer.MAX_VALUE, 1000.0, NO_EFFICIENCY, NO_SET_BONUS));
    }

    @Test
    @DisplayName("ISS->Botania conversion multiplies in long, so a big cost cannot wrap negative")
    void conversionDoesNotOverflow() {
        // 1.9.0 computed `issCost * ratio` in int. At issCost=100000 and ratio=30000 that wraps
        // to a negative value, i.e. a free cast.
        assertEquals(Integer.MAX_VALUE, ManaCostComposer.convertIssToBotania(100_000, 30_000));
        assertEquals(3_000_000, ManaCostComposer.convertIssToBotania(100, 30_000));
    }

    @Test
    void conversionRejectsNonPositiveRatio() {
        assertEquals(0, ManaCostComposer.convertIssToBotania(100, 0));
        assertEquals(0, ManaCostComposer.convertIssToBotania(100, -5));
        assertEquals(0, ManaCostComposer.convertBotaniaToIss(100, 0));
    }

    @Test
    @DisplayName("Botania->ISS floors, and the reverse quote charges only for what was granted")
    void conversionRoundTripDoesNotOvercharge() {
        int ratio = 30;
        int issGained = ManaCostComposer.convertBotaniaToIss(100, ratio);
        assertEquals(3, issGained, "100/30 floors to 3");

        // Charging the raw 100 would take 10 mana the player was never credited for.
        assertEquals(90, ManaCostComposer.botaniaCostForIssGain(issGained, ratio));
    }

    @Test
    void conversionHandlesZeroAndNegativeAmounts() {
        assertEquals(0, ManaCostComposer.convertIssToBotania(0, 30));
        assertEquals(0, ManaCostComposer.convertIssToBotania(-10, 30));
        assertEquals(0, ManaCostComposer.convertBotaniaToIss(-10, 30));
        assertEquals(0, ManaCostComposer.botaniaCostForIssGain(-1, 30));
    }
}
