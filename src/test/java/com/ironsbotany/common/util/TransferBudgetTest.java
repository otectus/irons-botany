package com.ironsbotany.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The per-tick transfer budget shared by the Spell Reservoir and Mana Conduit.
 *
 * <p>Both used to grant every nearby player a full transfer rate each tick, so the configured
 * "block entity transfer rate" was silently multiplied by the number of players standing nearby.
 */
class TransferBudgetTest {

    /** Sum of every recipient's share, which must exactly equal the budget. */
    private static int distributed(int budget, int count) {
        int total = 0;
        for (int i = 0; i < count; i++) total += TransferBudget.shareFor(budget, count, i);
        return total;
    }

    @Test
    @DisplayName("the budget is the block's, not each player's")
    void budgetIsNotMultipliedByRecipients() {
        // Four players, rate 100. Before 1.10.0 this drained 400 per tick.
        assertEquals(100, distributed(100, 4));
        assertEquals(100, distributed(100, 1));
        assertEquals(100, distributed(100, 17));
    }

    @Test
    @DisplayName("an evenly divisible budget splits evenly")
    void evenSplit() {
        for (int i = 0; i < 4; i++) {
            assertEquals(25, TransferBudget.shareFor(100, 4, i));
        }
    }

    @Test
    @DisplayName("a remainder is handed out rather than lost")
    void remainderIsDistributed() {
        // 10 across 3: 4, 3, 3 — not 3, 3, 3 with one unit dropped.
        assertEquals(4, TransferBudget.shareFor(10, 3, 0));
        assertEquals(3, TransferBudget.shareFor(10, 3, 1));
        assertEquals(3, TransferBudget.shareFor(10, 3, 2));
        assertEquals(10, distributed(10, 3));
    }

    @Test
    @DisplayName("a budget smaller than the recipient count still spends every unit")
    void scarceBudgetIsFullySpent() {
        assertEquals(2, distributed(2, 5));
        assertEquals(1, TransferBudget.shareFor(2, 5, 0));
        assertEquals(1, TransferBudget.shareFor(2, 5, 1));
        assertEquals(0, TransferBudget.shareFor(2, 5, 2));
    }

    @Test
    @DisplayName("rotation moves the head of the queue so nobody is permanently favoured")
    void rotationSpreadsTheAdvantage() {
        int count = 3;
        Map<Integer, Integer> timesFirst = new HashMap<>();
        for (long tick = 0; tick < 30; tick++) {
            timesFirst.merge(TransferBudget.rotationFor(tick, count), 1, Integer::sum);
        }
        assertEquals(3, timesFirst.size(), "every recipient should lead sometimes");
        timesFirst.values().forEach(n -> assertEquals(10, n));
    }

    @Test
    @DisplayName("rotation is non-negative even for a negative clock")
    void rotationHandlesNegativeGameTime() {
        // floorMod, not %: a negative index would throw on the callers' list access.
        assertTrue(TransferBudget.rotationFor(-1L, 3) >= 0);
        assertTrue(TransferBudget.rotationFor(-1L, 3) < 3);
        assertEquals(0, TransferBudget.rotationFor(-1L, 0), "no recipients, no rotation");
    }

    @Test
    void degenerateInputsYieldNothing() {
        assertEquals(0, TransferBudget.shareFor(0, 4, 0));
        assertEquals(0, TransferBudget.shareFor(-50, 4, 0));
        assertEquals(0, TransferBudget.shareFor(100, 0, 0));
        assertEquals(0, TransferBudget.shareFor(100, -1, 0));
        assertEquals(0, TransferBudget.shareFor(100, 4, -1), "index outside range");
        assertEquals(0, TransferBudget.shareFor(100, 4, 4), "index outside range");
    }

    @Test
    @DisplayName("no recipient can ever exceed the whole budget")
    void noRecipientExceedsBudget() {
        for (int count = 1; count <= 8; count++) {
            for (int budget = 0; budget <= 64; budget++) {
                for (int i = 0; i < count; i++) {
                    int share = TransferBudget.shareFor(budget, count, i);
                    assertTrue(share >= 0 && share <= budget,
                            () -> "share out of range for budget/count/index");
                }
                assertEquals(budget, distributed(budget, count),
                        "the whole budget must be distributed exactly once");
            }
        }
    }
}
