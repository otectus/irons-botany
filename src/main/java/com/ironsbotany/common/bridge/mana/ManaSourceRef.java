package com.ironsbotany.common.bridge.mana;

import java.util.Objects;

/**
 * Stable, comparable identity for one Botania mana source considered by a cast.
 *
 * <p>Two things depend on this being a value type rather than "whatever stack the API handed
 * back". First, <b>deduplication</b>: Botania's {@code getManaItems} and {@code getManaAccesories}
 * can surface the same physical stack, and the 1.9.0 HUD and cost paths both double-counted as a
 * result. Second, <b>determinism</b>: the order sources are drained in must not depend on hash
 * iteration or block-scan order, or two identical casts can spend different items.
 *
 * <p>Physical-stack dedup itself is done by reference identity at collection time — this ref is
 * the ordering key and the diagnostic label.
 */
public record ManaSourceRef(Kind kind, int ordinal, String descriptor) implements Comparable<ManaSourceRef> {

    public enum Kind {
        /** A mana item in the player's inventory (including armour and offhand). */
        INVENTORY,
        /** A mana accessory — Curios/Baubles, as reported by Botania's accessory list. */
        ACCESSORY,
        /** A Botania mana pool or other {@code ManaPool} block entity near the caster. */
        POOL
    }

    public ManaSourceRef {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(descriptor, "descriptor");
    }

    /**
     * Drain order: all carried items before any accessory, all accessories before any pool.
     *
     * <p>Rationale: spend what the player is carrying before what they are wearing, and never
     * touch shared infrastructure (a pool serving a whole base) while a personal source can pay.
     * Within a kind, {@code ordinal} is the collector's stable index — inventory slot number,
     * accessory slot order, or pool distance rank.
     */
    @Override
    public int compareTo(ManaSourceRef other) {
        int byKind = Integer.compare(kind.ordinal(), other.kind.ordinal());
        if (byKind != 0) return byKind;
        int byOrdinal = Integer.compare(ordinal, other.ordinal);
        if (byOrdinal != 0) return byOrdinal;
        return descriptor.compareTo(other.descriptor);
    }

    @Override
    public String toString() {
        return kind + "[" + ordinal + "]:" + descriptor;
    }
}
