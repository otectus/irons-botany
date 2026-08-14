package com.ironsbotany.common.util;

import com.ironsbotany.common.bridge.mana.BotaniaManaGateway;
import com.ironsbotany.common.bridge.mana.ManaCostComposer;
import com.ironsbotany.common.bridge.mana.ManaPlan;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.config.ManaUnificationMode;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaItem;

import java.util.List;

/**
 * Convenience wrappers over {@link BotaniaManaGateway} for the passive systems — the Botanical
 * Focus tick, reservoir rings, conduits, armour set bonuses.
 *
 * <p>Per-cast costs do <strong>not</strong> come through here: they use the cast transaction, which
 * plans and commits as one unit. These helpers exist for the "top up a little, if possible" flows
 * where an all-or-nothing transaction would be overkill. They are still exact — every one of them
 * plans first and only then commits, so none of them can partially drain.
 *
 * <h3>What changed in 1.10.0</h3>
 * The old implementation probed availability with
 * {@code ManaItemHandler.requestMana(stack, player, amount, false)} once per carried stack. Botania
 * excludes the stack passed to it from its own search, so that call answers "everything except
 * this", not "this". With one mana item it always returned 0 — Botania payment could never
 * succeed — and with three or more it over-counted, letting an unaffordable request pass the check
 * and then partially drain before failing.
 */
public class ManaHelper {

    // ------------------------------------------------------------------
    // Conversion
    // ------------------------------------------------------------------

    /** Botania → ISS at the configured ratio; {@code 0} in modes that forbid conversion. */
    public static int convertBotaniaToISS(int botaniaAmount) {
        if (!CommonConfig.MANA_UNIFICATION_MODE.get().allowsConversion()) return 0;
        return ManaCostComposer.convertBotaniaToIss(botaniaAmount, CommonConfig.MANA_CONVERSION_RATIO.get());
    }

    /** ISS → Botania at the configured ratio; {@code 0} in modes that forbid conversion. */
    public static int convertISSToBotania(int issAmount) {
        if (!CommonConfig.MANA_UNIFICATION_MODE.get().allowsConversion()) return 0;
        return ManaCostComposer.convertIssToBotania(issAmount, CommonConfig.MANA_CONVERSION_RATIO.get());
    }

    // ------------------------------------------------------------------
    // Queries and transfers
    // ------------------------------------------------------------------

    /** Total Botania mana the player can spend, across items, accessories and nearby pools. */
    public static int availableBotaniaMana(Player player) {
        List<BotaniaManaGateway.LiveSource> sources = BotaniaManaGateway.collectSources(player);
        long total = 0;
        for (BotaniaManaGateway.LiveSource source : sources) {
            total += source.available();
            if (total >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        }
        return (int) total;
    }

    /** True if the player's sources hold {@code amount} in total. Mutates nothing. */
    public static boolean hasBotaniaMana(Player player, int amount) {
        if (amount <= 0) return true;
        return BotaniaManaGateway.plan(amount, BotaniaManaGateway.collectSources(player)).isSatisfied();
    }

    /**
     * Drain {@code amount}, all or nothing.
     *
     * @return {@code true} iff the full amount was taken. On {@code false} nothing was taken.
     */
    public static boolean drainBotaniaMana(Player player, int amount) {
        if (amount <= 0) return true;
        List<BotaniaManaGateway.LiveSource> sources = BotaniaManaGateway.collectSources(player);
        ManaPlan plan = BotaniaManaGateway.plan(amount, sources);
        if (!plan.isSatisfied()) return false;
        return BotaniaManaGateway.commit(plan, sources);
    }

    /**
     * Add Botania mana to the player's carried items.
     *
     * @return the amount actually accepted, which may be less than requested and may be zero
     */
    public static int giveBotaniaMana(Player player, int amount) {
        return BotaniaManaGateway.dispatchMana(player, amount);
    }

    /**
     * Move one transfer-rate tick of Botania mana out of {@code stack} and into the player's ISS
     * mana pool. This is the Botanical Focus's passive conversion.
     *
     * <p>Charges only the Botania that maps exactly to the ISS actually granted. Integer division
     * floors the ISS gain, so draining the raw transfer rate would take mana the player is never
     * credited for.
     *
     * @return {@code true} if any mana moved
     */
    public static boolean tryConvertManaToISS(Player player, ItemStack stack) {
        if (player == null || player.level().isClientSide() || stack == null || stack.isEmpty()) return false;

        MagicData magicData = MagicData.getPlayerMagicData(player);
        if (magicData == null) return false;

        float currentMana = magicData.getMana();
        float maxMana = (float) player.getAttributeValue(AttributeRegistry.MAX_MANA.get());
        if (currentMana >= maxMana) return false;

        ManaItem manaItem = stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM).resolve().orElse(null);
        if (manaItem == null) return false;

        int ratio = CommonConfig.MANA_CONVERSION_RATIO.get();
        int issRoom = (int) Math.floor(maxMana - currentMana);
        if (issRoom <= 0) return false;

        int issToAdd = Math.min(convertBotaniaToISS(Math.max(0, manaItem.getMana())),
                Math.min(convertBotaniaToISS(CommonConfig.MANA_TRANSFER_RATE.get()), issRoom));
        if (issToAdd <= 0) return false;

        int botaniaToDrain = ManaCostComposer.botaniaCostForIssGain(issToAdd, ratio);
        if (botaniaToDrain <= 0 || manaItem.getMana() < botaniaToDrain) return false;

        manaItem.addMana(-botaniaToDrain);
        magicData.addMana(issToAdd);
        return true;
    }

    /** @deprecated use {@link #hasBotaniaMana}; pools are part of the ordinary source set now. */
    @Deprecated(forRemoval = true)
    public static boolean hasBotaniaManaFromPools(Player player, int amount) {
        return hasBotaniaMana(player, amount);
    }

    /** @deprecated use {@link #drainBotaniaMana}; pools are part of the ordinary source set now. */
    @Deprecated(forRemoval = true)
    public static boolean drainBotaniaManaFromPools(Player player, int amount) {
        return drainBotaniaMana(player, amount);
    }

    /** Kept for the {@code SEPARATE}/{@code DISABLED} checks that read it directly. */
    public static ManaUnificationMode mode() {
        return CommonConfig.MANA_UNIFICATION_MODE.get();
    }
}
