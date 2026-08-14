package com.ironsbotany.common.bridge.mana;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ManaPlannerTest {

    private static ManaPlanner.Snapshot inv(int ordinal, int available) {
        return new ManaPlanner.Snapshot(
                new ManaSourceRef(ManaSourceRef.Kind.INVENTORY, ordinal, "tablet" + ordinal), available);
    }

    private static ManaPlanner.Snapshot accessory(int ordinal, int available) {
        return new ManaPlanner.Snapshot(
                new ManaSourceRef(ManaSourceRef.Kind.ACCESSORY, ordinal, "ring" + ordinal), available);
    }

    private static ManaPlanner.Snapshot pool(int ordinal, int available) {
        return new ManaPlanner.Snapshot(
                new ManaSourceRef(ManaSourceRef.Kind.POOL, ordinal, "pool" + ordinal), available);
    }

    @Nested
    @DisplayName("regressions against the 1.9.0 aggregation defect")
    class Regressions {

        @Test
        @DisplayName("a single mana item can pay — 1.9.0 could never spend it")
        void singleSourcePays() {
            // Botania's requestMana(stack, ...) skips the stack passed to it, so probing the
            // player's only mana item returned 0 and Botania payment always failed.
            ManaPlan plan = ManaPlanner.plan(500, List.of(inv(0, 1000)));

            assertTrue(plan.isSatisfied());
            assertEquals(1, plan.debits().size());
            assertEquals(500, plan.debits().get(0).amount());
        }

        @Test
        @DisplayName("three partial sources are never over-counted into a false 'affordable'")
        void threeSourcesDoNotOverCount() {
            // The exact scenario from the audit: 3 x 100 mana, cost 500.
            // 1.9.0 summed three probes of "everything except me" = 200+200+200 = 600 >= 500,
            // reported affordable, then drained 300 before returning false.
            List<ManaPlanner.Snapshot> sources = List.of(inv(0, 100), inv(1, 100), inv(2, 100));

            ManaPlan plan = ManaPlanner.plan(500, sources);

            assertFalse(plan.isSatisfied(), "300 available cannot pay 500");
            assertEquals(300, plan.planned(), "reports what it actually found, for diagnostics");
            assertEquals(300, ManaPlanner.totalAvailable(sources));
        }

        @Test
        @DisplayName("an unsatisfied plan still names zero committed debits' worth of damage")
        void unsatisfiedPlanIsInert() {
            ManaPlan plan = ManaPlanner.plan(500, List.of(inv(0, 100)));

            assertFalse(plan.isSatisfied());
            // The plan is a statement of intent only. Nothing has been taken; the caller is
            // contractually required not to commit it. This is what makes "no partial drain on
            // failure" structural rather than a matter of getting the loop right.
            assertEquals(100, plan.debitTotal());
        }
    }

    @Nested
    @DisplayName("allocation")
    class Allocation {

        @Test
        void spreadsAcrossSourcesUntilPaid() {
            ManaPlan plan = ManaPlanner.plan(250, List.of(inv(0, 100), inv(1, 100), inv(2, 100)));

            assertTrue(plan.isSatisfied());
            assertEquals(250, plan.debitTotal());
            assertEquals(List.of(100, 100, 50),
                    plan.debits().stream().map(ManaPlan.PlannedDebit::amount).toList());
        }

        @Test
        void takesNothingFromSourcesItDoesNotNeed() {
            ManaPlan plan = ManaPlanner.plan(50, List.of(inv(0, 100), inv(1, 100)));

            assertEquals(1, plan.debits().size(), "the second item must not be touched");
            assertEquals(50, plan.debits().get(0).amount());
        }

        @Test
        void skipsEmptySources() {
            ManaPlan plan = ManaPlanner.plan(100, List.of(inv(0, 0), inv(1, 100)));

            assertTrue(plan.isSatisfied());
            assertEquals(1, plan.debits().size());
            assertEquals(ManaSourceRef.Kind.INVENTORY, plan.debits().get(0).ref().kind());
            assertEquals(1, plan.debits().get(0).ref().ordinal());
        }

        @Test
        void exactCostIsSatisfied() {
            assertTrue(ManaPlanner.plan(100, List.of(inv(0, 100))).isSatisfied());
        }

        @Test
        void oneBelowCostIsNotSatisfied() {
            assertFalse(ManaPlanner.plan(101, List.of(inv(0, 100))).isSatisfied());
        }

        @Test
        void zeroCostNeedsNoSources() {
            ManaPlan plan = ManaPlanner.plan(0, List.of());
            assertTrue(plan.isSatisfied());
            assertTrue(plan.isFree());
            assertTrue(plan.debits().isEmpty());
        }

        @Test
        void noSourcesCannotPay() {
            assertFalse(ManaPlanner.plan(1, List.of()).isSatisfied());
        }
    }

    @Nested
    @DisplayName("determinism and priority")
    class Determinism {

        @Test
        @DisplayName("carried before worn before pooled, regardless of input order")
        void drainOrderIsByKindThenOrdinal() {
            List<ManaPlanner.Snapshot> shuffled =
                    new ArrayList<>(List.of(pool(0, 100), accessory(0, 100), inv(0, 100)));
            Collections.reverse(shuffled);

            ManaPlan plan = ManaPlanner.plan(300, shuffled);

            assertEquals(
                    List.of(ManaSourceRef.Kind.INVENTORY, ManaSourceRef.Kind.ACCESSORY, ManaSourceRef.Kind.POOL),
                    plan.debits().stream().map(d -> d.ref().kind()).toList());
        }

        @Test
        @DisplayName("a shared pool is untouched while a personal source can pay")
        void poolIsLastResort() {
            ManaPlan plan = ManaPlanner.plan(100, List.of(pool(0, 100_000), inv(0, 100)));

            assertEquals(1, plan.debits().size());
            assertEquals(ManaSourceRef.Kind.INVENTORY, plan.debits().get(0).ref().kind());
        }

        @Test
        void identicalInputsProduceIdenticalPlans() {
            List<ManaPlanner.Snapshot> sources = List.of(inv(2, 70), inv(0, 60), accessory(1, 90));

            assertEquals(ManaPlanner.plan(200, sources).debits(),
                    ManaPlanner.plan(200, sources).debits());
        }
    }

    @Nested
    @DisplayName("hostile input")
    class HostileInput {

        @Test
        @DisplayName("a negative stored mana value is clamped, not subtracted")
        void negativeAvailableIsClampedToZero() {
            // Malformed/hand-edited NBT can report negative stored mana. Summing it would shrink
            // the plan total and could make a genuinely affordable cast fail.
            ManaPlanner.Snapshot corrupt = new ManaPlanner.Snapshot(
                    new ManaSourceRef(ManaSourceRef.Kind.INVENTORY, 0, "corrupt"), -5000);

            assertEquals(0, corrupt.available());
            assertEquals(100, ManaPlanner.totalAvailable(List.of(corrupt, inv(1, 100))));
        }

        @Test
        @DisplayName("totalling many huge sources saturates instead of wrapping negative")
        void totalAvailableSaturates() {
            List<ManaPlanner.Snapshot> huge =
                    List.of(inv(0, Integer.MAX_VALUE), inv(1, Integer.MAX_VALUE), inv(2, Integer.MAX_VALUE));

            assertEquals(Integer.MAX_VALUE, ManaPlanner.totalAvailable(huge));
        }

        @Test
        void negativeCostIsTreatedAsFree() {
            assertTrue(ManaPlanner.plan(-1, List.of()).isFree());
        }

        @Test
        void planRejectsNegativeConstruction() {
            assertThrows(IllegalArgumentException.class, () -> new ManaPlan(-1, 0, List.of()));
        }
    }
}
