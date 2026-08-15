package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.bridge.ManaBridgeManager;
import com.ironsbotany.common.bridge.cast.CastTransactions;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.config.ManaUnificationMode;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Wires the cast transaction into ISS's pipeline.
 *
 * <p>Both subscribers run at {@link EventPriority#LOW} so Ars 'n Spells (at {@code NORMAL}) routes
 * first and player KubeJS scripts at {@code HIGH}/{@code HIGHEST} can still pre-empt this mod.
 *
 * <h3>ChangeManaEvent is deliberately not handled</h3>
 * Through 1.9.0 this class subscribed {@code ChangeManaEvent} and cancelled any mana decrease that
 * occurred in a tick stamped "Botania paid". {@code ChangeManaEvent} exposes only
 * {@code (magicData, oldMana, newMana)} — no spell, level, or cast source — so it could not tell
 * the cast Botania had paid for from any other debit in the same tick, including a second cast or
 * an unrelated mod's drain. ISS's own {@code SpellOnCastEvent.setManaCost(int)} redirects cost for
 * exactly one cast, so that entire interception was removed rather than narrowed.
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID)
public final class SpellEventHandlers {

    private SpellEventHandlers() {}

    /**
     * Preflight and reserve. Cancelling here costs the player nothing: ISS has not yet debited
     * mana, initiated the cast, or consumed a scroll.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onSpellPreCast(SpellPreCastEvent event) {
        if (CommonConfig.MANA_UNIFICATION_MODE.get() == ManaUnificationMode.DISABLED) return;

        Player player = event.getEntity();
        if (player == null || player.level().isClientSide()) return;

        AbstractSpell spell = SpellRegistry.getSpell(event.getSpellId());
        if (spell == null) return;

        ManaBridgeManager.Preflight result =
                ManaBridgeManager.preflight(player, spell, event.getSpellLevel(), event.getCastSource());

        if (!result.allow()) {
            player.displayClientMessage(Component.translatable(result.denyKey()), true);
            event.setCanceled(true);
        }
    }

    /**
     * Commit the reserved plan, and zero the ISS cost when Botania paid in its place.
     *
     * <p>This fires immediately before ISS evaluates
     * {@code magicData.setMana(max(0, mana - event.getManaCost()))}, so setting the cost to zero is
     * the whole of the redirection — no refund, no interception, no persistent flag.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onSpellCast(SpellOnCastEvent event) {
        if (CommonConfig.MANA_UNIFICATION_MODE.get() == ManaUnificationMode.DISABLED) return;

        Player player = event.getEntity();
        if (player == null || player.level().isClientSide()) return;

        ItemStack castingStack = ItemStack.EMPTY;
        MagicData magicData = MagicData.getPlayerMagicData(player);
        if (magicData != null && magicData.getPlayerCastingItem() != null) {
            castingStack = magicData.getPlayerCastingItem();
        }

        boolean botaniaPaidInsteadOfIss = ManaBridgeManager.commit(
                player, event.getSpellId(), event.getSpellLevel(), event.getCastSource(), castingStack);

        if (botaniaPaidInsteadOfIss) {
            event.setManaCost(0);
        }
    }

    // ------------------------------------------------------------------
    // Lifecycle — no transaction may outlive the cast it belongs to
    // ------------------------------------------------------------------

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        CastTransactions.abort(event.getEntity(), "player logged out");
        com.ironsbotany.common.flower.FlowerAuraRegistry.invalidateCache(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            CastTransactions.abort(player, "player died");
        }
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        CastTransactions.abort(event.getEntity(), "player changed dimension");
        // Aura scans are per-dimension; a stale entry from the world just left must not answer a
        // query in the new one.
        com.ironsbotany.common.flower.FlowerAuraRegistry.invalidateCache(event.getEntity().getUUID());
    }

    /**
     * Sweep transactions abandoned without a terminal event.
     *
     * <p>Runs once a second on the server tick rather than per player, and only expires entries
     * older than a minute. A reservation holds no mana, so expiring one can never cost a player
     * anything; this exists to stop the map growing, not to protect state.
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer().getTickCount() % 20 != 0) return;
        CastTransactions.expireStale(event.getServer().overworld().getGameTime());
    }

    /**
     * Drop every open transaction when the server stops, so nothing survives into the next world
     * loaded in the same JVM — the single-player "quit to title, load another save" path.
     */
    @SubscribeEvent
    public static void onServerStopped(net.minecraftforge.event.server.ServerStoppedEvent event) {
        CastTransactions.clearAll();
        com.ironsbotany.common.flower.FlowerAuraRegistry.cleanupCache();
    }
}
