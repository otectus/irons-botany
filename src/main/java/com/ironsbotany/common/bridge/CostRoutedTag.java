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
    }
}
