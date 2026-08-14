package com.ironsbotany.common.bridge.mana;

import java.util.Collections;
import java.util.List;

/**
 * An immutable, fully-resolved statement of intent: "to pay {@code requested} mana, take exactly
 * these amounts from exactly these sources."
 *
 * <p>A plan performs no mutation. It is produced during preflight, checked, and only then handed
 * to the gateway for commit. This separation is the fix for the 1.9.0 behaviour where affordability
 * was probed with the same destructive API used to pay, so a cast that turned out to be unaffordable
 * had already destroyed part of the player's mana by the time it was refused.
 */
public record ManaPlan(int requested, int planned, List<PlannedDebit> debits) {

    /** One source's share of a plan. */
    public record PlannedDebit(ManaSourceRef ref, int amount) {}

    public static final ManaPlan FREE = new ManaPlan(0, 0, List.of());

    public ManaPlan {
        if (requested < 0) throw new IllegalArgumentException("requested must be >= 0: " + requested);
        if (planned < 0) throw new IllegalArgumentException("planned must be >= 0: " + planned);
        debits = List.copyOf(debits);
    }

    /** True when the plan covers the full requested cost and may therefore be committed. */
    public boolean isSatisfied() {
        return planned >= requested;
    }

    /** True when nothing needs to be taken — a zero-cost cast. */
    public boolean isFree() {
        return requested == 0;
    }

    public List<PlannedDebit> debits() {
        return Collections.unmodifiableList(debits);
    }

    /** Sum of the planned per-source amounts. Equals {@link #planned} for any plan this mod builds. */
    public int debitTotal() {
        int total = 0;
        for (PlannedDebit d : debits) total += d.amount();
        return total;
    }

    @Override
    public String toString() {
        return "ManaPlan{requested=" + requested + ", planned=" + planned
                + ", satisfied=" + isSatisfied() + ", debits=" + debits + '}';
    }
}
