package com.ironsbotany.common.bridge.cast;

import com.ironsbotany.IronsBotany;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side registry of open {@link CastTransaction}s, one per player.
 *
 * <p>ISS permits a player only one cast at a time ({@code MagicData.isCasting()} gates
 * {@code attemptInitiateCast}), so a single open transaction per player is the correct cardinality;
 * a second {@code open} for the same player cancels the first rather than leaking it.
 *
 * <h3>Why this is not player NBT</h3>
 * 1.9.0 stored routing state in {@code player.getPersistentData()}, which persists across sessions,
 * survives a mid-cast crash, and had to be swept on a timer to stop it accumulating. A cast lives
 * for at most a few seconds and is meaningless after a restart, so it belongs in server memory and
 * is cleared at every lifecycle boundary — see {@code CastTransactionLifecycle}.
 */
public final class CastTransactions {

    /**
     * A transaction older than this without reaching a terminal state is abandoned. Comfortably
     * longer than the longest ISS cast time, so it only ever catches genuinely orphaned entries
     * (a cast interrupted in a way that fires no event we observe).
     */
    static final long MAX_AGE_TICKS = 20L * 60L; // 60 seconds

    private static final Map<UUID, CastTransaction> OPEN = new ConcurrentHashMap<>();

    private CastTransactions() {}

    /** Open a transaction for {@code player}, discarding any stale one it replaces. */
    public static CastTransaction open(Player player, CastTransaction transaction) {
        CastTransaction previous = OPEN.put(player.getUUID(), transaction);
        if (previous != null && !previous.isTerminal()) {
            previous.cancel("superseded by a new cast");
            previous.close();
        }
        return transaction;
    }

    /** The player's open transaction, or {@code null}. */
    public static CastTransaction get(Player player) {
        return player == null ? null : OPEN.get(player.getUUID());
    }

    /**
     * The player's open transaction if it was opened for this exact cast, else {@code null}.
     *
     * <p>The identity check is the point: without it a {@code SpellOnCastEvent} for a different
     * spell could consume payment state that belongs to another cast — the 1.9.0 same-tick
     * collision, in a different disguise.
     */
    public static CastTransaction getMatching(Player player, String spellId, int level,
                                              io.redspace.ironsspellbooks.api.spells.CastSource source) {
        CastTransaction tx = get(player);
        return (tx != null && !tx.isTerminal() && tx.matches(spellId, level, source)) ? tx : null;
    }

    /**
     * True if Botania paid for the cast made with exactly {@code scrollStack}, so an Elementium
     * scroll may survive it.
     *
     * <p>The stack identity check is the safety property. 1.9.0 stamped only a tick on the player,
     * so any scroll cast in a tick where Botania had paid for <em>something</em> was retained free.
     * The transaction records the stack {@code MagicData.setPlayerCastingItem} was called with, and
     * {@code Scroll.attemptRemoveScrollAfterCast} consumes that same stack, so comparing references
     * ties the retention to the one cast that was actually paid for.
     */
    public static boolean wasScrollRetained(Player player, net.minecraft.world.item.ItemStack scrollStack) {
        CastTransaction tx = get(player);
        return tx != null
                && tx.state() == CastTransactionState.COMMITTED
                && tx.retainScroll()
                && tx.castingStack() == scrollStack;
    }

    /** Close and remove the player's transaction. Idempotent. */
    public static void close(Player player) {
        if (player == null) return;
        CastTransaction tx = OPEN.remove(player.getUUID());
        if (tx != null) tx.close();
    }

    /**
     * Cancel and remove the player's transaction, rolling back a committed debit if one exists.
     * Used by every lifecycle boundary: logout, death, dimension change, server stop.
     */
    public static void abort(Player player, String why) {
        if (player == null) return;
        CastTransaction tx = OPEN.remove(player.getUUID());
        if (tx == null) return;
        if (!tx.isTerminal()) tx.cancel(why);
        tx.close();
    }

    /**
     * Drop transactions older than {@link #MAX_AGE_TICKS}.
     *
     * <p>A reservation holds no mana, so expiring one cannot lose a player anything. A committed
     * transaction that somehow survived is rolled back rather than silently discarded.
     */
    public static void expireStale(long gameTime) {
        OPEN.values().removeIf(tx -> {
            if (gameTime - tx.openedAtTick() < MAX_AGE_TICKS) return false;
            if (!tx.isTerminal()) {
                IronsBotany.LOGGER.debug("Expiring abandoned cast transaction {}", tx);
                tx.cancel("abandoned: no terminal event within " + MAX_AGE_TICKS + " ticks");
            }
            tx.close();
            return true;
        });
    }

    /** Clear everything. Called on server stop so nothing survives into the next world. */
    public static void clearAll() {
        OPEN.values().forEach(tx -> {
            if (!tx.isTerminal()) tx.cancel("server stopping");
            tx.close();
        });
        OPEN.clear();
    }

    /** Visible for tests and diagnostics. */
    public static int openCount() {
        return OPEN.size();
    }
}
