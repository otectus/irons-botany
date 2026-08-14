package com.ironsbotany.common.bridge.cast;

import com.ironsbotany.common.bridge.mana.BotaniaManaGateway;
import com.ironsbotany.common.bridge.mana.ManaPlan;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * One server-side cast, from preflight to terminal state.
 *
 * <h3>What this replaces</h3>
 * 1.9.0 identified a cast by {@code (world tick, spellId.hashCode(), ISS cost)} stored in the
 * player's persistent NBT, with the "Botania paid" and "scroll paid" flags keyed on the tick
 * <em>alone</em>. Two casts in one tick could therefore inherit each other's payment state, and
 * {@code ChangeManaEvent} — which carries no spell identity at all, only
 * {@code (magicData, oldMana, newMana)} — would refund whichever mana decrease happened to arrive
 * first in a tick that carried the stamp, including one belonging to an unrelated spell.
 *
 * <p>A transaction instead carries its own {@link #id}, the exact {@link #castingStack} taken from
 * {@code MagicData.getPlayerCastingItem()}, and the spell/level/source it was opened for. Payment
 * redirection uses ISS's own {@code SpellOnCastEvent.setManaCost(0)} rather than intercepting mana
 * mutations, so an unrelated debit can no longer be captured. Nothing is written to player NBT.
 *
 * <h3>Reservation is non-destructive by construction</h3>
 * {@link CastTransactionState#RESERVED} holds a {@link ManaPlan} — a statement of intent. Mana
 * moves only in {@link #commit()}. An abandoned reservation therefore needs no compensation, which
 * is why an interrupted or cancelled cast conserves everything without a rollback path having to
 * be correct.
 */
public final class CastTransaction {

    private final UUID id = UUID.randomUUID();
    private final UUID playerId;
    private final String spellId;
    private final int spellLevel;
    private final CastSource castSource;
    private final ItemStack castingStack;
    private final long openedAtTick;

    private CastTransactionState state = CastTransactionState.NEW;
    private String reason = "";

    private int issCost;
    private int botaniaCost;
    private ManaPlan plan = ManaPlan.FREE;
    private List<BotaniaManaGateway.LiveSource> sources = List.of();

    /**
     * True when Botania is paying <em>instead of</em> ISS mana, so the ISS debit for this cast is
     * zeroed via {@code SpellOnCastEvent.setManaCost(0)}. False for dual-cost modes, where paying
     * both is the configured intent.
     */
    private boolean botaniaPaysInsteadOfIss;

    /** True when a successful Botania payment lets an Elementium scroll survive the cast. */
    private boolean retainScroll;

    /**
     * ISS mana to credit the caster once the Botania debit commits — the Dreamwood Scepter's
     * conversion, folded into this transaction.
     *
     * <p>1.9.0 ran that conversion from a <em>second</em> {@code SpellPreCastEvent} subscriber at
     * the same {@code LOW} priority as the bridge. Same-priority listener order is undefined, so
     * the two handlers raced for ownership of the cast and coordinated through a shared NBT tag;
     * whichever lost skipped, which made the scepter's behaviour depend on listener registration
     * order. Here it is one more component of one transaction, debited once.
     */
    private int issCreditOnCommit;

    public CastTransaction(UUID playerId, String spellId, int spellLevel,
                           CastSource castSource, ItemStack castingStack, long openedAtTick) {
        this.playerId = playerId;
        this.spellId = spellId == null ? "" : spellId;
        this.spellLevel = spellLevel;
        this.castSource = castSource;
        this.castingStack = castingStack == null ? ItemStack.EMPTY : castingStack;
        this.openedAtTick = openedAtTick;
    }

    // ---------------------------------------------------------------- identity

    public UUID id() { return id; }
    public UUID playerId() { return playerId; }
    public String spellId() { return spellId; }
    public int spellLevel() { return spellLevel; }
    public CastSource castSource() { return castSource; }
    public long openedAtTick() { return openedAtTick; }

    /**
     * The exact stack this cast was initiated from, as recorded by ISS in
     * {@code MagicData.setPlayerCastingItem}. Channel and Elementium-scroll behaviour bind to this
     * rather than to "whichever hand happens to hold something eligible", which in 1.9.0 let an
     * off-hand item modify a cast made with the main hand.
     */
    public ItemStack castingStack() { return castingStack; }

    /**
     * True if this transaction was opened for the given cast. Used to reject a
     * {@code SpellOnCastEvent} that does not belong to the open transaction rather than letting it
     * inherit the payment state.
     */
    public boolean matches(String otherSpellId, int otherLevel, CastSource otherSource) {
        return spellId.equals(otherSpellId) && spellLevel == otherLevel && castSource == otherSource;
    }

    // ---------------------------------------------------------------- state

    public CastTransactionState state() { return state; }
    public String reason() { return reason; }
    public boolean isTerminal() { return state.isTerminal(); }

    public int issCost() { return issCost; }
    public int botaniaCost() { return botaniaCost; }
    public ManaPlan plan() { return plan; }
    public boolean botaniaPaysInsteadOfIss() { return botaniaPaysInsteadOfIss; }
    public boolean retainScroll() { return retainScroll; }
    public int issCreditOnCommit() { return issCreditOnCommit; }

    /** Record the Dreamwood conversion's ISS credit, applied only if {@link #commit()} succeeds. */
    public void setIssCreditOnCommit(int issCredit) {
        this.issCreditOnCommit = Math.max(0, issCredit);
    }

    // ---------------------------------------------------------------- transitions

    /** Record the costs computed once during preflight. */
    public void preflight(int issCost, int botaniaCost) {
        require(CastTransactionState.NEW);
        this.issCost = Math.max(0, issCost);
        this.botaniaCost = Math.max(0, botaniaCost);
        this.state = CastTransactionState.PREFLIGHTED;
    }

    /** Attach the non-destructive payment plan and the live handles it was built against. */
    public void reserve(ManaPlan plan, List<BotaniaManaGateway.LiveSource> sources,
                        boolean botaniaPaysInsteadOfIss, boolean retainScroll) {
        require(CastTransactionState.PREFLIGHTED);
        this.plan = plan == null ? ManaPlan.FREE : plan;
        this.sources = sources == null ? List.of() : List.copyOf(sources);
        this.botaniaPaysInsteadOfIss = botaniaPaysInsteadOfIss;
        this.retainScroll = retainScroll;
        this.state = CastTransactionState.RESERVED;
    }

    /**
     * Debit the reserved plan. All-or-nothing: on failure nothing is taken and the transaction
     * moves to {@link CastTransactionState#CANCELED}.
     *
     * @return {@code true} iff the debit succeeded
     */
    public boolean commit() {
        require(CastTransactionState.RESERVED);
        if (BotaniaManaGateway.commit(plan, sources)) {
            this.state = CastTransactionState.COMMITTED;
            return true;
        }
        // The sources moved between reservation and commit — possible only across a long cast.
        // Nothing was taken; the caller leaves ISS to charge its own cost rather than granting a
        // free cast.
        cancel("botania commit failed: sources changed since preflight");
        return false;
    }

    /** Return a committed debit to the exact sources it came from. */
    public void rollback(String why) {
        if (state != CastTransactionState.COMMITTED) {
            cancel(why);
            return;
        }
        BotaniaManaGateway.rollback(plan, sources);
        this.reason = why;
        this.state = CastTransactionState.ROLLED_BACK;
    }

    /** End before commit. Safe from any pre-commit state; nothing needs undoing. */
    public void cancel(String why) {
        if (state == CastTransactionState.COMMITTED) {
            rollback(why);
            return;
        }
        this.reason = why;
        this.state = CastTransactionState.CANCELED;
    }

    /** Release transient state. Idempotent, and safe to call from a {@code finally}. */
    public void close() {
        this.sources = List.of();
        this.state = CastTransactionState.CLOSED;
    }

    private void require(CastTransactionState expected) {
        if (state != expected) {
            throw new IllegalStateException(
                    "cast transaction " + id + " expected " + expected + " but was " + state);
        }
    }

    @Override
    public String toString() {
        return "CastTransaction{" + id + " " + spellId + " L" + spellLevel + " via " + castSource
                + " state=" + state + " iss=" + issCost + " botania=" + botaniaCost
                + (reason.isEmpty() ? "" : " reason=" + reason) + '}';
    }
}
