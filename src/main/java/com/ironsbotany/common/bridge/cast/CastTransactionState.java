package com.ironsbotany.common.bridge.cast;

/**
 * Lifecycle of a single cast's cost transaction. Every terminal state releases the transaction
 * from {@link CastTransactions}; nothing is left behind on the player.
 */
public enum CastTransactionState {

    /** Immutable cast inputs gathered; no cost computed yet. */
    NEW,

    /**
     * Costs and modifiers computed once, and cooldown, spell, source item, mana, catalysts,
     * reagents and placement all validated — with no mutation of any kind.
     */
    PREFLIGHTED,

    /**
     * A concrete {@link com.ironsbotany.common.bridge.mana.ManaPlan} naming exact sources and
     * amounts is held. Still no externally visible effect: a reservation in this design is a
     * plan, not a withdrawal, so abandoning one costs nothing and needs no compensation.
     */
    RESERVED,

    /** Mana debited once, consumables consumed once, spell applied once, feedback emitted once. */
    COMMITTED,

    /** A committed debit was returned to the exact sources it came from after a later failure. */
    ROLLED_BACK,

    /** Ended before commit — insufficient mana, a failed precondition, or an interrupted cast. */
    CANCELED,

    /** Terminal. All transient state released. */
    CLOSED;

    public boolean isTerminal() {
        return this == COMMITTED || this == ROLLED_BACK || this == CANCELED || this == CLOSED;
    }
}
