package com.ironsbotany.common.bridge;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.compat.ArsNSpellsCompat;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.config.ManaUnificationMode;
import com.ironsbotany.common.block.entity.ArcaneManaAltarBlockEntity;
import com.ironsbotany.common.spell.AbstractBotanicalSpell;
import com.ironsbotany.common.util.ManaHelper;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Single entry point for routing spell cost between ISS mana and Botania
 * mana. All cost decisions in Iron's Botany should go through
 * {@link #resolveCost} so the routing rules live in one place and remain
 * coherent under all five {@link ManaUnificationMode} values.
 *
 * <h3>Why this exists</h3>
 * Before this class, cost routing lived inline in {@code AbstractBotanicalSpell.onCast()}.
 * Two problems followed:
 * <ul>
 *   <li>Non-Botanical ISS spells were ignored in {@code BOTANIA_PRIMARY} mode —
 *       a Fireball cast still drained ISS mana even when the player explicitly
 *       opted Botania-as-primary.</li>
 *   <li>The routing decision was duplicated across catalyst, channel, and
 *       per-mode branches, drifting subtly with each new feature.</li>
 * </ul>
 *
 * <h3>Idempotency contract</h3>
 * Cost routing fires from two hook points: {@code SpellPreCastEvent} (gating)
 * and {@code ChangeManaEvent} (debit redirection). Both call {@link #resolveCost};
 * the second call returns {@link ManaResolutionResult#NOOP} because the
 * {@link CostRoutedTag} from the first call is still live for the same tick.
 *
 * <h3>Ars 'n Spells coexistence</h3>
 * If ANS is loaded and active, this manager defers to it via
 * {@link ArsNSpellsCompat}: ANS's bridge runs at {@code EventPriority.NORMAL}
 * and writes its own routed tag; IB subscribes at {@code LOW} and skips any
 * cost ANS already covered.
 */
public final class ManaBridgeManager {

    private ManaBridgeManager() {}

    /**
     * Resolve cost for a single cast attempt. Idempotent within a tick.
     *
     * @param player   the caster (server-side player; client calls return {@link ManaResolutionResult#NOOP})
     * @param spell    the ISS spell being cast
     * @param level    spell level (1-based)
     * @param source   how the cast was initiated (spellbook, scroll, sword, etc.)
     * @return the routing decision; {@code ok=false} means cancel the cast
     */
    public static ManaResolutionResult resolveCost(Player player, AbstractSpell spell, int level, CastSource source) {
        if (player == null || spell == null) return ManaResolutionResult.NOOP;
        if (player.level().isClientSide()) return ManaResolutionResult.NOOP;

        long tick = player.level().getGameTime();
        int spellHash = spell.getSpellId().hashCode();
        int issCost = safeIssCost(spell, level);

        // Idempotency: same tick + same spell + same cost → already handled.
        if (CostRoutedTag.isMarked(player, tick, spellHash, issCost)) {
            return ManaResolutionResult.NOOP;
        }

        // Elementium scroll: try to pay from Botania pool first; if it sticks,
        // mark the routed tag so the scroll's overridden removeScrollAfterCast
        // sees the marker and skips consumption.
        if (source == CastSource.SCROLL && holdingElementiumScroll(player)) {
            int botaniaCost = Math.max(0, issCost) * CommonConfig.MANA_CONVERSION_RATIO.get();
            if (botaniaCost > 0
                    && com.ironsbotany.common.util.ManaHelper.hasBotaniaMana(player, botaniaCost)
                    && com.ironsbotany.common.util.ManaHelper.drainBotaniaMana(player, botaniaCost)) {
                CostRoutedTag.mark(player, tick, spellHash, issCost);
                return ManaResolutionResult.botaniaOnly(botaniaCost);
            }
            // Insufficient Botania → fall through to normal mode handling
            // (which will consume the scroll the vanilla way).
        }

        // Defer to Ars 'n Spells if it has already routed this tick.
        if (CostRoutedTag.isExternallyRouted(player, tick)) {
            CostRoutedTag.mark(player, tick, spellHash, issCost);
            return ManaResolutionResult.NOOP;
        }

        ManaUnificationMode mode = CommonConfig.MANA_UNIFICATION_MODE.get();

        // ANS handles routing entirely when in ARS_PRIMARY mode and IB is
        // configured to defer (any mode except SEPARATE / DISABLED).
        if (mode != ManaUnificationMode.SEPARATE
                && mode != ManaUnificationMode.DISABLED
                && ArsNSpellsCompat.isArsPrimary()) {
            CostRoutedTag.mark(player, tick, spellHash, issCost);
            return ManaResolutionResult.NOOP;
        }

        ManaResolutionResult result = switch (mode) {
            case DISABLED       -> ManaResolutionResult.NOOP;
            case BOTANIA_PRIMARY -> chargeBotaniaPrimary(player, spell, level, issCost);
            case ISS_PRIMARY    -> chargeIssPrimary(player, spell, level, issCost);
            case HYBRID         -> chargeHybrid(player, spell, level, issCost);
            case SEPARATE       -> chargeSeparate(player, spell, level, issCost);
        };

        if (result.ok()) {
            CostRoutedTag.mark(player, tick, spellHash, issCost);
        }
        return result;
    }

    /**
     * Botania-primary: convert ISS cost into Botania units and charge the
     * mana network. ISS's own debit will still happen (we don't cancel it
     * here), so this mode effectively makes the player pay <em>both</em> in
     * exchange for being able to refill ISS mana from Botania trivially.
     *
     * <p>For Botanical spells, we charge the Botanical-specific cost; for
     * other ISS spells we use {@code issCost * conversionRatio}.
     */
    private static ManaResolutionResult chargeBotaniaPrimary(Player player, AbstractSpell spell, int level, int issCost) {
        int botaniaCost = computeBotaniaCost(spell, level, issCost);
        if (botaniaCost <= 0) return ManaResolutionResult.NOOP;
        if (!ManaHelper.hasBotaniaMana(player, botaniaCost)) {
            return ManaResolutionResult.INSUFFICIENT;
        }
        if (!ManaHelper.drainBotaniaMana(player, botaniaCost)) {
            return ManaResolutionResult.INSUFFICIENT;
        }
        return ManaResolutionResult.botaniaOnly(botaniaCost);
    }

    /**
     * ISS-primary: passive Botania→ISS conversion happens via the Botanical
     * Focus tick; the per-cast path here is a no-op because ISS's own
     * pipeline will debit normally.
     */
    private static ManaResolutionResult chargeIssPrimary(Player player, AbstractSpell spell, int level, int issCost) {
        return ManaResolutionResult.NOOP;
    }

    /**
     * Hybrid: when {@link CommonConfig#ENABLE_DUAL_COST_SPELLS} is on AND
     * the per-spell {@code dual_cost_enabled} parameter is true AND the
     * spell is Botanical, charge Botania alongside ISS. Otherwise behave
     * like ISS-primary (passive conversion, no per-cast Botania debit).
     */
    private static ManaResolutionResult chargeHybrid(Player player, AbstractSpell spell, int level, int issCost) {
        if (!CommonConfig.ENABLE_DUAL_COST_SPELLS.get()) return ManaResolutionResult.NOOP;
        if (!(spell instanceof AbstractBotanicalSpell botanical)) return ManaResolutionResult.NOOP;
        if (!com.ironsbotany.common.spell.config.BotanySpellConfig.isDualCostEnabled(spell)) return ManaResolutionResult.NOOP;
        int botaniaCost = botanical.getBotaniaManaCost(level);
        if (botaniaCost <= 0) return ManaResolutionResult.NOOP;
        if (!ManaHelper.hasBotaniaMana(player, botaniaCost)) {
            return ManaResolutionResult.INSUFFICIENT;
        }
        if (!ManaHelper.drainBotaniaMana(player, botaniaCost)) {
            return ManaResolutionResult.INSUFFICIENT;
        }
        return ManaResolutionResult.botaniaOnly(botaniaCost);
    }

    /**
     * Separate: dual-cost is mandatory. Botanical spells charge Botania;
     * other ISS spells are unaffected (they're not "ours" to dual-bill).
     */
    private static ManaResolutionResult chargeSeparate(Player player, AbstractSpell spell, int level, int issCost) {
        if (!(spell instanceof AbstractBotanicalSpell botanical)) return ManaResolutionResult.NOOP;
        int botaniaCost = botanical.getBotaniaManaCost(level);
        if (botaniaCost <= 0) return ManaResolutionResult.NOOP;
        if (!ManaHelper.hasBotaniaMana(player, botaniaCost)) {
            return ManaResolutionResult.INSUFFICIENT;
        }
        if (!ManaHelper.drainBotaniaMana(player, botaniaCost)) {
            return ManaResolutionResult.INSUFFICIENT;
        }
        return ManaResolutionResult.botaniaOnly(botaniaCost);
    }

    private static int computeBotaniaCost(AbstractSpell spell, int level, int issCost) {
        if (spell instanceof AbstractBotanicalSpell botanical) {
            return botanical.getBotaniaManaCost(level);
        }
        int ratio = CommonConfig.MANA_CONVERSION_RATIO.get();
        return Math.max(0, issCost * ratio);
    }

    /**
     * Search for a {@link ArcaneManaAltarBlockEntity} within {@code radius}
     * blocks of the player and try to drain {@code amount} mana from it.
     * Used by Botanical cost paths so altars take precedence over the
     * player's tablets when available.
     *
     * @return true if the full amount was drained from a single altar
     */
    public static boolean tryDrainFromNearbyAltar(Player player, int amount, int radius) {
        if (player == null || amount <= 0) return false;
        Level level = player.level();
        if (level.isClientSide()) return false;
        BlockPos origin = player.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-radius, -radius, -radius),
                origin.offset(radius, radius, radius))) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ArcaneManaAltarBlockEntity altar && altar.tryDrainForCast(amount)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if either of the player's hands holds an
     *         {@link com.ironsbotany.common.item.ElementiumScrollItem}.
     *         Used to gate the Botania-pays-for-scroll path.
     */
    private static boolean holdingElementiumScroll(Player player) {
        return player.getMainHandItem().getItem() instanceof com.ironsbotany.common.item.ElementiumScrollItem
                || player.getOffhandItem().getItem() instanceof com.ironsbotany.common.item.ElementiumScrollItem;
    }

    private static int safeIssCost(AbstractSpell spell, int level) {
        try {
            return Math.max(0, spell.getManaCost(level));
        } catch (Throwable t) {
            IronsBotany.LOGGER.debug("ManaBridgeManager: ISS cost lookup failed for {} L{}: {}",
                    spell.getSpellId(), level, t.toString());
            return 0;
        }
    }
}
