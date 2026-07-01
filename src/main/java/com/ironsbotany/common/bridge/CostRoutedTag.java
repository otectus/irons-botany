package com.ironsbotany.common.bridge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Idempotency tag stored on the player's transient persistent data that
 * marks a spell cost as already-routed for the current server tick.
 *
 * <p>The cost-routing path fires twice for any cast that completes:
 * once via {@code SpellPreCastEvent} (where IB checks affordability and
 * pre-charges Botania) and once via {@code ChangeManaEvent} (where ISS
 * actually mutates the mana pool). Without this tag, IB would see both
 * events for the same cast and risk double-billing.
 *
 * <p>The tag is keyed by (tick, spell hash, ISS cost) — a triple that is
 * extremely unlikely to collide for a single player within a single tick.
 * It does not need to persist across sessions; if the server restarts
 * mid-cast the cast is already aborted.
 *
 * <p>Sister mods (Ars 'n Spells in particular) write a parallel tag under
 * their own namespace; both bridges should respect each other's tags via
 * {@link #isExternallyRouted}.
 */
public final class CostRoutedTag {

    public static final String KEY_TICK = "ironsbotany_cost_routed_tick";
    public static final String KEY_SPELL_HASH = "ironsbotany_cost_routed_spell";
    public static final String KEY_COST = "ironsbotany_cost_routed_cost";

    /** Mirror tag written by Ars 'n Spells (defensive interop). */
    public static final String ANS_KEY_TICK = "arsnspells_cost_routed_tick";

    /**
     * Tick on which Botania specifically paid for an Elementium-scroll cast.
     * Distinct from the generic routed tag so a same-tick non-scroll cast can't
     * trick {@code ElementiumScrollItem} into keeping the scroll for free.
     */
    public static final String KEY_SCROLL_PAID_TICK = "ironsbotany_scroll_paid_tick";

    /**
     * Tick on which Botania fully paid a cast's cost <em>in place of</em> ISS mana
     * (BOTANIA_PRIMARY / scroll paths). {@code SpellEventHandlers.onChangeMana} reads
     * this to refund the ISS debit ISS would otherwise still apply, so the player is
     * charged once. NOT set for HYBRID/SEPARATE dual-cost, where paying both is intended.
     */
    public static final String KEY_BOTANIA_PAID_TICK = "ironsbotany_botania_paid_tick";

    private CostRoutedTag() {}

    public static void mark(Player player, long tick, int spellHash, int issCost) {
        CompoundTag data = player.getPersistentData();
        data.putLong(KEY_TICK, tick);
        data.putInt(KEY_SPELL_HASH, spellHash);
        data.putInt(KEY_COST, issCost);
    }

    public static boolean isMarked(Player player, long tick, int spellHash, int issCost) {
        CompoundTag data = player.getPersistentData();
        return data.contains(KEY_TICK)
                && data.getLong(KEY_TICK) == tick
                && data.getInt(KEY_SPELL_HASH) == spellHash
                && data.getInt(KEY_COST) == issCost;
    }

    /** Stamp that Botania paid for an Elementium-scroll cast on {@code tick}. */
    public static void markScrollPaid(Player player, long tick) {
        player.getPersistentData().putLong(KEY_SCROLL_PAID_TICK, tick);
    }

    /** True if Botania paid for an Elementium-scroll cast on {@code tick}. */
    public static boolean isScrollPaid(Player player, long tick) {
        CompoundTag data = player.getPersistentData();
        return data.contains(KEY_SCROLL_PAID_TICK) && data.getLong(KEY_SCROLL_PAID_TICK) == tick;
    }

    /** Stamp that Botania paid a cast's cost in place of ISS mana on {@code tick}. */
    public static void markBotaniaPaid(Player player, long tick) {
        player.getPersistentData().putLong(KEY_BOTANIA_PAID_TICK, tick);
    }

    /** True if Botania paid in place of ISS mana on {@code tick} (→ refund the ISS debit). */
    public static boolean isBotaniaPaid(Player player, long tick) {
        CompoundTag data = player.getPersistentData();
        return data.contains(KEY_BOTANIA_PAID_TICK) && data.getLong(KEY_BOTANIA_PAID_TICK) == tick;
    }

    /** True if any sister bridge has already routed cost this tick. */
    public static boolean isExternallyRouted(Player player, long tick) {
        CompoundTag data = player.getPersistentData();
        return data.contains(ANS_KEY_TICK) && data.getLong(ANS_KEY_TICK) == tick;
    }

    public static void clear(Player player) {
        CompoundTag data = player.getPersistentData();
        data.remove(KEY_TICK);
        data.remove(KEY_SPELL_HASH);
        data.remove(KEY_COST);
        // Also wipe the per-cast payment markers so they don't accumulate on the
        // player's persistent NBT. (The ANS mirror tag is owned by Ars 'n Spells;
        // it is a single tick-scoped long that ANS overwrites each cast, so we
        // deliberately leave it alone rather than stomp a possibly-fresh value.)
        data.remove(KEY_SCROLL_PAID_TICK);
        data.remove(KEY_BOTANIA_PAID_TICK);
    }
}
