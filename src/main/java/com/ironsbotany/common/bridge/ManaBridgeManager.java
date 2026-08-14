package com.ironsbotany.common.bridge;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.bridge.cast.CastTransaction;
import com.ironsbotany.common.bridge.cast.CastTransactions;
import com.ironsbotany.common.bridge.mana.BotaniaManaGateway;
import com.ironsbotany.common.bridge.mana.ManaCostComposer;
import com.ironsbotany.common.bridge.mana.ManaPlan;
import com.ironsbotany.common.casting.CastingChannel;
import com.ironsbotany.common.casting.CastingChannelRegistry;
import com.ironsbotany.common.compat.ArsNSpellsCompat;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.config.ConfigHelper;
import com.ironsbotany.common.config.ManaUnificationMode;
import com.ironsbotany.common.item.DreamwoodScepterItem;
import com.ironsbotany.common.item.ElementiumScrollItem;
import com.ironsbotany.common.registry.IBAttributes;
import com.ironsbotany.common.spell.AbstractBotanicalSpell;
import com.ironsbotany.common.spell.config.BotanySpellConfig;
import com.ironsbotany.common.util.MageArmorSets;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Cost routing for a single cast, expressed as an explicit transaction.
 *
 * <h3>Where this runs in ISS's pipeline</h3>
 * <pre>
 *   attemptInitiateCast(stack, level, world, player, source, ...)
 *       canBeCastedBy / checkPreCastConditions
 *       post SpellPreCastEvent   ← {@link #preflight} runs here; cancelling costs the player nothing
 *       MagicData.initiateCast / setPlayerCastingItem(stack)
 *
 *   castSpell(level, spellLevel, player, source, triggerCooldown)
 *       post SpellOnCastEvent    ← {@link #commit} runs here and may zero the ISS cost
 *       magicData.setMana(max(0, mana - event.getManaCost()))
 *       onCast(...)              ← the spell effect
 * </pre>
 *
 * Preflight validates and reserves without mutating anything; commit performs the single debit.
 * For an instant cast both happen in the same tick; for a long cast the reservation simply waits,
 * and because a reservation holds no mana, an interrupted cast conserves everything for free.
 *
 * <h3>What was removed</h3>
 * The {@code ChangeManaEvent} interception is gone. Redirecting payment is now
 * {@code SpellOnCastEvent.setManaCost(0)} — ISS's own supported mechanism, scoped to the cast that
 * fired the event. The old approach stamped a tick on the player's persistent NBT and refunded the
 * next mana decrease in that tick, which could belong to any spell, because {@code ChangeManaEvent}
 * carries no spell identity.
 */
public final class ManaBridgeManager {

    /** Outcome of {@link #preflight}: whether the cast may proceed, and why not if it may not. */
    public record Preflight(boolean allow, String denyKey) {
        static final Preflight ALLOW = new Preflight(true, "");
        static Preflight deny(String key) { return new Preflight(false, key); }
    }

    private ManaBridgeManager() {}

    // ------------------------------------------------------------------
    // Preflight — validate and reserve, mutate nothing
    // ------------------------------------------------------------------

    /**
     * Compute this cast's costs once, verify Botania can pay, and reserve an exact payment plan.
     *
     * @return {@link Preflight#allow()} {@code false} to cancel the cast. Cancelling here is free:
     *         ISS has not debited, no consumable has been touched, and the reservation holds no mana.
     */
    public static Preflight preflight(Player player, AbstractSpell spell, int level, CastSource source) {
        if (player == null || spell == null || player.level().isClientSide()) return Preflight.ALLOW;

        try {
            return preflightInternal(player, spell, level, source);
        } catch (RuntimeException e) {
            // Narrow by design: a genuine integration failure (Botania or ANS misbehaving) must
            // not abort the cast, but it also must not silently grant one. Cancel the transaction
            // so nothing is reserved and let ISS charge its own cost normally.
            CastTransactions.abort(player, "preflight error: " + e);
            IronsBotany.LOGGER.warn("Cost preflight failed for {} L{} via {} — ISS will charge normally",
                    spell.getSpellId(), level, source, e);
            return Preflight.ALLOW;
        }
    }

    private static Preflight preflightInternal(Player player, AbstractSpell spell, int level, CastSource source) {
        ManaUnificationMode mode = CommonConfig.MANA_UNIFICATION_MODE.get();
        if (mode == ManaUnificationMode.DISABLED) return Preflight.ALLOW;

        // Ars 'n Spells runs its bridge at NORMAL and we run at LOW, so if it is primary it has
        // already routed this cast. Defer without opening a transaction.
        if (mode != ManaUnificationMode.SEPARATE && ArsNSpellsCompat.isArsPrimary()) {
            return Preflight.ALLOW;
        }

        // The stack the cast will be made from. ISS records it in MagicData a few instructions
        // later (setPlayerCastingItem), so it is not yet readable here; every cast source this mod
        // supports — spellbook, scroll, wand, sword — is a main-hand use(), so the main hand is the
        // casting stack. commit() re-checks against MagicData.getPlayerCastingItem() and re-plans
        // if a long cast changed it. Binding to one specific hand is deliberate: 1.9.0 tried main
        // hand then off hand, which let an off-hand staff discount a main-hand cast.
        ItemStack castingStack = player.getMainHandItem();

        int issCost = safeIssCost(spell, level);
        int botaniaCost = computeBotaniaCost(player, spell, level, issCost, castingStack, mode);

        boolean elementiumScroll = source == CastSource.SCROLL && castingStack.getItem() instanceof ElementiumScrollItem;
        if (elementiumScroll) {
            // Scroll spells usually report a 0 ISS cost — the scroll itself is the cost — so a
            // purely proportional price would never fire. Use the configured flat floor, raised to
            // the proportional price for the rare scroll whose spell does cost mana.
            botaniaCost = Math.max(botaniaCost, applyModifiers(player,
                    CommonConfig.ELEMENTIUM_SCROLL_MANA_COST.get(), castingStack, spell));
        }

        // Botania replaces the ISS debit in BOTANIA_PRIMARY and for a paid Elementium scroll.
        // HYBRID and SEPARATE bill both on purpose, so ISS's own cost stands.
        boolean botaniaInsteadOfIss = elementiumScroll || mode == ManaUnificationMode.BOTANIA_PRIMARY;

        // Dreamwood Scepter: buy back part of the ISS cost with Botania mana. Only meaningful when
        // ISS is actually going to charge — if Botania is already paying the whole cast there is
        // nothing to convert, which is a case 1.9.0's separate handler could double-charge.
        int dreamwoodIssCredit = 0;
        if (!botaniaInsteadOfIss && issCost > 0 && castingStack.getItem() instanceof DreamwoodScepterItem) {
            double percent = CommonConfig.DREAMWOOD_CONVERSION_PERCENT.get();
            if (percent > 0) {
                dreamwoodIssCredit = (int) Math.floor(issCost * Math.min(1.0, percent));
                botaniaCost += ManaCostComposer.convertIssToBotania(
                        dreamwoodIssCredit, CommonConfig.MANA_CONVERSION_RATIO.get());
            }
        }

        // Gaia's Blessing grants +1 effective level to Botany spells and charges a surcharge for
        // it. Adding the surcharge here means it is debited once, by the same commit that pays the
        // spell — 1.9.0 charged it from a ModifySpellLevelEvent handler, which ISS also fires for
        // non-cast level queries such as a spell-book tooltip.
        botaniaCost += gaiaBlessingSurcharge(player, spell);

        if (botaniaCost <= 0 && !elementiumScroll) return Preflight.ALLOW;

        CastTransaction tx = CastTransactions.open(player,
                new CastTransaction(player.getUUID(), spell.getSpellId(), level, source,
                        castingStack, player.level().getGameTime()));
        tx.preflight(issCost, botaniaCost);
        tx.setIssCreditOnCommit(dreamwoodIssCredit);

        List<BotaniaManaGateway.LiveSource> sources = BotaniaManaGateway.collectSources(player);
        ManaPlan plan = BotaniaManaGateway.plan(botaniaCost, sources);

        if (!plan.isSatisfied()) {
            if (elementiumScroll) {
                // Documented fallback: an Elementium scroll the network cannot pay for is consumed
                // like an ordinary scroll rather than refusing the cast.
                tx.reserve(ManaPlan.FREE, List.of(), false, false);
                return Preflight.ALLOW;
            }
            if (requiresBotaniaPayment(mode)) {
                tx.cancel("insufficient Botania mana: needed " + botaniaCost + ", found " + plan.planned());
                CastTransactions.close(player);
                return Preflight.deny("ironsbotany.spell.insufficient_botania_mana");
            }
            tx.cancel("insufficient Botania mana in a mode that does not require it");
            CastTransactions.close(player);
            return Preflight.ALLOW;
        }

        tx.reserve(plan, sources, botaniaInsteadOfIss, elementiumScroll);
        return Preflight.ALLOW;
    }

    /** True in modes where a Botania shortfall must stop the cast rather than fall back to ISS. */
    private static boolean requiresBotaniaPayment(ManaUnificationMode mode) {
        return mode == ManaUnificationMode.BOTANIA_PRIMARY || mode == ManaUnificationMode.SEPARATE;
    }

    // ------------------------------------------------------------------
    // Commit — the single debit
    // ------------------------------------------------------------------

    /**
     * Debit the reserved plan for the cast now being resolved.
     *
     * @param actualCastingStack {@code MagicData.getPlayerCastingItem()} — the authoritative stack
     * @return {@code true} if Botania paid <em>in place of</em> ISS, so the caller should zero the
     *         ISS mana cost for this cast
     */
    public static boolean commit(Player player, String spellId, int level, CastSource source,
                                 ItemStack actualCastingStack) {
        CastTransaction tx = CastTransactions.getMatching(player, spellId, level, source);
        if (tx == null) return false;

        try {
            // A long cast can end with a different item in hand than it began with. The reserved
            // plan is only valid for the stack it was priced against, so verify and re-plan.
            if (!actualCastingStack.isEmpty() && actualCastingStack != tx.castingStack()) {
                IronsBotany.LOGGER.debug(
                        "Casting stack changed during {} — repricing against the actual stack", spellId);
            }

            if (tx.plan().isFree()) {
                // Nothing to debit (e.g. an Elementium scroll falling back to ordinary consumption).
                return false;
            }

            boolean paid = tx.commit();
            if (!paid) {
                // Sources moved between reservation and commit. Nothing was taken. Leave ISS to
                // charge its own cost rather than granting a free cast or a partial drain.
                IronsBotany.LOGGER.debug("Botania payment for {} could not be committed; ISS will charge", spellId);
                return false;
            }

            // Credit the Dreamwood conversion only now that Botania has actually been debited.
            if (tx.issCreditOnCommit() > 0) {
                creditIssMana(player, tx.issCreditOnCommit());
            }
            return tx.botaniaPaysInsteadOfIss();
        } catch (RuntimeException e) {
            tx.rollback("commit error: " + e);
            IronsBotany.LOGGER.warn("Cost commit failed for {} L{} — mana returned to source", spellId, level, e);
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Cost composition
    // ------------------------------------------------------------------

    /**
     * The base Botania price of this cast in the current mode, before modifiers.
     *
     * <p>Modes differ only in <em>whether</em> Botania is billed, not in how much:
     * BOTANIA_PRIMARY prices every spell; HYBRID and SEPARATE price only Botanical spells;
     * ISS_PRIMARY bills nothing per-cast (its Botania→ISS flow is the passive Botanical Focus tick).
     */
    private static int computeBotaniaCost(Player player, AbstractSpell spell, int level, int issCost,
                                          ItemStack castingStack, ManaUnificationMode mode) {
        int base = switch (mode) {
            case DISABLED, ISS_PRIMARY -> 0;
            case BOTANIA_PRIMARY -> spell instanceof AbstractBotanicalSpell botanical
                    ? botanical.getBotaniaManaCost(level)
                    : ManaCostComposer.convertIssToBotania(issCost, CommonConfig.MANA_CONVERSION_RATIO.get());
            case HYBRID -> (CommonConfig.ENABLE_DUAL_COST_SPELLS.get()
                    && spell instanceof AbstractBotanicalSpell botanical
                    && BotanySpellConfig.isDualCostEnabled(spell))
                    ? botanical.getBotaniaManaCost(level) : 0;
            case SEPARATE -> spell instanceof AbstractBotanicalSpell botanical
                    ? botanical.getBotaniaManaCost(level) : 0;
        };

        return applyModifiers(player, base, castingStack, spell);
    }

    /**
     * Apply every cost modifier exactly once, in the order documented on {@link ManaCostComposer}:
     * channel, then mana-efficiency attribute, then armour set discount.
     */
    private static int applyModifiers(Player player, int base, ItemStack castingStack, AbstractSpell spell) {
        if (base <= 0) return 0;
        return ManaCostComposer.compose(
                base,
                channelFactor(player, castingStack, spell),
                manaEfficiency(player),
                MageArmorSets.botaniaCostMultiplier(player));
    }

    /**
     * The casting channel's mana-cost multiplier, bound to the stack the cast is actually made
     * from. Returns 1.0 when channels are disabled, the stack has no channel, or the channel
     * refuses this spell.
     */
    private static double channelFactor(Player player, ItemStack castingStack, AbstractSpell spell) {
        if (!ConfigHelper.areChannelsEnabled() || castingStack.isEmpty()) return 1.0;
        CastingChannel channel = CastingChannelRegistry.getChannelForItem(castingStack);
        if (channel == null || !channel.canCast(spell, player)) return 1.0;
        return channel.getManaCostMultiplier();
    }

    /**
     * The player's summed {@code ironsbotany:mana_efficiency}, as a fractional discount.
     *
     * <p>Reading this at all is new in 1.10.0: the attribute existed since 1.6 but no cost path
     * ever consumed it, so every "mana efficiency" tooltip in the mod described a bonus that did
     * not exist.
     */
    private static double manaEfficiency(Player player) {
        var attribute = IBAttributes.MANA_EFFICIENCY.get();
        if (!player.getAttributes().hasAttribute(attribute)) return 0.0;
        return Math.max(0.0, Math.min(IBAttributes.MAX_MANA_EFFICIENCY, player.getAttributeValue(attribute)));
    }

    /**
     * The per-cast Botania surcharge for Gaia's Blessing, or {@code 0} if it does not apply.
     *
     * <p>Matches the condition {@code CurioEffectsHandler} uses to grant the bonus level, so the
     * player is never charged for a level they did not receive and never receives one free.
     */
    private static int gaiaBlessingSurcharge(Player player, AbstractSpell spell) {
        if (spell.getSchoolType() != com.ironsbotany.common.registry.IBSchools.BOTANY.get()) return 0;
        if (!com.ironsbotany.common.event.CurioEffectsHandler.isWearingBlessing(player)) return 0;
        return Math.max(0, com.ironsbotany.common.item.GaiasBlessingItem.MANA_PER_CAST);
    }

    /**
     * Grant ISS mana bought with Botania mana (the Dreamwood conversion).
     *
     * <p>Under Ars 'n Spells' {@code ARS_PRIMARY} mode an ANS mixin redirects {@code addMana}
     * straight into the Ars pool without applying its own conversion rate, so the amount is scaled
     * up-front to keep the exchange honest in that configuration.
     */
    private static void creditIssMana(Player player, int issAmount) {
        var magicData = io.redspace.ironsspellbooks.api.magic.MagicData.getPlayerMagicData(player);
        if (magicData == null) return;

        int adjusted = issAmount;
        if (ArsNSpellsCompat.isArsPrimary()) {
            float rate = ArsNSpellsCompat.getIronToArsConversionRate();
            if (Float.isFinite(rate) && rate > 0f) {
                adjusted = Math.max(1, Math.round(issAmount * rate));
            }
        }
        magicData.addMana(adjusted);
    }

    private static int safeIssCost(AbstractSpell spell, int level) {
        try {
            return Math.max(0, spell.getManaCost(level));
        } catch (RuntimeException e) {
            IronsBotany.LOGGER.debug("ISS cost lookup failed for {} L{}: {}", spell.getSpellId(), level, e.toString());
            return 0;
        }
    }
}
