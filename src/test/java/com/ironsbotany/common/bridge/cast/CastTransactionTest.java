package com.ironsbotany.common.bridge.cast;

import com.ironsbotany.common.bridge.mana.ManaPlan;
import com.ironsbotany.common.bridge.mana.ManaSourceRef;
import com.ironsbotany.testsupport.MinecraftBootstrap;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises the cast transaction's state machine and identity rules — the two things that made
 * same-tick casts share payment state in 1.9.0.
 */
class CastTransactionTest {

    private static final UUID PLAYER = UUID.nameUUIDFromBytes("player".getBytes());

    @BeforeAll
    static void bootstrap() {
        MinecraftBootstrap.ensure(); // ItemStack.EMPTY needs the vanilla registries
    }

    private static CastTransaction newTransaction() {
        return new CastTransaction(PLAYER, "ironsbotany:mana_bloom", 3,
                CastSource.SPELLBOOK, ItemStack.EMPTY, 1000L);
    }

    private static ManaPlan satisfiedPlan(int cost) {
        return new ManaPlan(cost, cost, List.of(new ManaPlan.PlannedDebit(
                new ManaSourceRef(ManaSourceRef.Kind.INVENTORY, 0, "tablet"), cost)));
    }

    @Test
    @DisplayName("a fresh transaction has taken nothing and decided nothing")
    void initialState() {
        CastTransaction tx = newTransaction();

        assertEquals(CastTransactionState.NEW, tx.state());
        assertFalse(tx.isTerminal());
        assertEquals(0, tx.botaniaCost());
        assertTrue(tx.plan().isFree());
        assertFalse(tx.botaniaPaysInsteadOfIss());
        assertFalse(tx.retainScroll());
    }

    @Test
    void progressesNewToPreflightedToReserved() {
        CastTransaction tx = newTransaction();

        tx.preflight(20, 10_000);
        assertEquals(CastTransactionState.PREFLIGHTED, tx.state());
        assertEquals(20, tx.issCost());
        assertEquals(10_000, tx.botaniaCost());

        tx.reserve(satisfiedPlan(10_000), List.of(), true, false);
        assertEquals(CastTransactionState.RESERVED, tx.state());
        assertTrue(tx.botaniaPaysInsteadOfIss());
        assertFalse(tx.isTerminal(), "a reservation is not a terminal state");
    }

    @Test
    @DisplayName("out-of-order transitions are rejected rather than silently accepted")
    void illegalTransitionsThrow() {
        CastTransaction tx = newTransaction();

        assertThrows(IllegalStateException.class, () -> tx.reserve(ManaPlan.FREE, List.of(), false, false),
                "cannot reserve before preflight");
        assertThrows(IllegalStateException.class, tx::commit, "cannot commit before reserving");

        tx.preflight(0, 100);
        assertThrows(IllegalStateException.class, () -> tx.preflight(0, 100), "cannot preflight twice");
    }

    @Test
    @DisplayName("cancelling a reservation is free — nothing was ever taken")
    void cancellingAReservationTakesNothing() {
        CastTransaction tx = newTransaction();
        tx.preflight(20, 10_000);
        tx.reserve(satisfiedPlan(10_000), List.of(), true, false);

        tx.cancel("interrupted");

        assertEquals(CastTransactionState.CANCELED, tx.state());
        assertTrue(tx.isTerminal());
        assertEquals("interrupted", tx.reason());
    }

    @Test
    @DisplayName("a commit whose sources have vanished takes nothing and cancels")
    void commitWithoutLiveSourcesCancels() {
        CastTransaction tx = newTransaction();
        tx.preflight(20, 10_000);
        // Reserved against sources that are no longer present at commit time — the long-cast case.
        tx.reserve(satisfiedPlan(10_000), List.of(), true, false);

        assertFalse(tx.commit());
        assertEquals(CastTransactionState.CANCELED, tx.state(),
                "a commit that cannot be honoured must not leave the transaction committed");
    }

    @Test
    @DisplayName("a zero-cost plan commits trivially")
    void freePlanCommits() {
        CastTransaction tx = newTransaction();
        tx.preflight(0, 0);
        tx.reserve(ManaPlan.FREE, List.of(), false, false);

        assertTrue(tx.commit());
        assertEquals(CastTransactionState.COMMITTED, tx.state());
    }

    @Test
    @DisplayName("close is idempotent and safe from a finally block")
    void closeIsIdempotent() {
        CastTransaction tx = newTransaction();
        tx.close();
        tx.close();
        assertEquals(CastTransactionState.CLOSED, tx.state());
        assertTrue(tx.isTerminal());
    }

    @Test
    @DisplayName("rollback before commit degrades to cancel rather than double-refunding")
    void rollbackBeforeCommitIsACancel() {
        CastTransaction tx = newTransaction();
        tx.preflight(0, 500);
        tx.reserve(satisfiedPlan(500), List.of(), false, false);

        tx.rollback("spell failed");

        assertEquals(CastTransactionState.CANCELED, tx.state());
    }

    @Test
    @DisplayName("identity distinguishes casts that 1.9.0's tick+hash key could confuse")
    void identityIsPerCast() {
        CastTransaction tx = newTransaction();

        assertTrue(tx.matches("ironsbotany:mana_bloom", 3, CastSource.SPELLBOOK));

        // Same tick, same player — all of these were indistinguishable when the key was
        // (tick, spellId.hashCode(), issCost) and the payment flags were tick-only.
        assertFalse(tx.matches("ironsbotany:gaia_wrath", 3, CastSource.SPELLBOOK), "different spell");
        assertFalse(tx.matches("ironsbotany:mana_bloom", 4, CastSource.SPELLBOOK), "different level");
        assertFalse(tx.matches("ironsbotany:mana_bloom", 3, CastSource.SCROLL), "different cast source");
    }

    @Test
    @DisplayName("every transaction gets its own id")
    void idsAreUnique() {
        assertNotEquals(newTransaction().id(), newTransaction().id());
    }

    @Test
    void dreamwoodCreditIsClampedNonNegative() {
        CastTransaction tx = newTransaction();
        tx.setIssCreditOnCommit(-50);
        assertEquals(0, tx.issCreditOnCommit());

        tx.setIssCreditOnCommit(25);
        assertEquals(25, tx.issCreditOnCommit());
    }
}
