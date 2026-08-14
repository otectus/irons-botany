package com.ironsbotany.common.bridge.mana;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Builds a {@link ManaPlan} from a snapshot of available sources. Pure: no Minecraft, no Botania,
 * no mutation, no I/O — which is what makes the aggregation rules directly unit-testable.
 *
 * <h3>The bug this replaces</h3>
 * 1.9.0 asked Botania for availability with
 * {@code ManaItemHandler.requestMana(stack, player, amount, false)} once per carried stack and
 * summed the answers. That call's first argument is the <em>requesting</em> stack and Botania
 * skips it by reference identity, then scans every <em>other</em> mana item the player has:
 *
 * <pre>
 *   for (ItemStack invItem : concat(getManaItems(player), getManaAccesories(player))) {
 *       if (invItem == requester) continue;      // ← the stack you asked about is excluded
 *       ...
 *   }
 * </pre>
 *
 * So each probe returned "everything except the item being probed". With one mana item every probe
 * returned 0 and Botania payment could never succeed; with three or more the sum over-counted by
 * roughly a factor of N−1 and the affordability gate passed when the player could not pay — after
 * which the commit pass partially drained and then reported failure.
 *
 * <p>Planning against a snapshot of each source's own {@code getMana()} removes the whole class of
 * error: every source is counted exactly once, and the plan is either satisfied or it is not.
 */
public final class ManaPlanner {

    /** One source's state at planning time. */
    public record Snapshot(ManaSourceRef ref, int available) {
        public Snapshot {
            if (available < 0) {
                // Defensive: a malformed NBT mana value must not become negative capacity that
                // silently subtracts from the plan total.
                available = 0;
            }
        }
    }

    private ManaPlanner() {}

    /**
     * Allocate {@code cost} across {@code sources} in ascending {@link ManaSourceRef} order,
     * taking as much as possible from each in turn.
     *
     * <p>Greedy in a fixed order rather than "largest first" on purpose: the order is the
     * documented, player-visible priority (carried → worn → nearby pool), and it must be
     * reproducible so two identical casts spend the same items.
     *
     * @return a plan that is {@linkplain ManaPlan#isSatisfied() satisfied} only if the sources
     *         genuinely hold {@code cost} in total. An unsatisfied plan still reports what it
     *         could find, for diagnostics, and must never be committed.
     */
    public static ManaPlan plan(int cost, Collection<Snapshot> sources) {
        if (cost <= 0) return ManaPlan.FREE;

        List<Snapshot> ordered = new ArrayList<>(sources);
        ordered.sort((a, b) -> a.ref().compareTo(b.ref()));

        List<ManaPlan.PlannedDebit> debits = new ArrayList<>();
        int remaining = cost;
        int planned = 0;

        for (Snapshot s : ordered) {
            if (remaining <= 0) break;
            int take = Math.min(remaining, s.available());
            if (take <= 0) continue;
            debits.add(new ManaPlan.PlannedDebit(s.ref(), take));
            planned += take;
            remaining -= take;
        }

        return new ManaPlan(cost, planned, debits);
    }

    /**
     * Total mana visible across {@code sources}, saturating at {@link Integer#MAX_VALUE}.
     *
     * <p>Uses a {@code long} accumulator: a player carrying many high-capacity items can otherwise
     * overflow {@code int} and report a negative total, which would make an affordable cast look
     * unaffordable.
     */
    public static int totalAvailable(Collection<Snapshot> sources) {
        long total = 0L;
        for (Snapshot s : sources) {
            total += s.available();
            if (total >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        }
        return (int) total;
    }
}
