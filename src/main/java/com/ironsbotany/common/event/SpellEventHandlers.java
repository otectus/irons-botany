package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.bridge.CostRoutedTag;
import com.ironsbotany.common.bridge.ManaBridgeManager;
import com.ironsbotany.common.bridge.ManaResolutionResult;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.config.ManaUnificationMode;
import io.redspace.ironsspellbooks.api.events.ChangeManaEvent;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge-bus subscribers that wire {@link ManaBridgeManager} into ISS's
 * cast pipeline. These are explicitly annotated with
 * {@link EventPriority#LOW} so that:
 * <ul>
 *   <li>Ars 'n Spells (running at default {@code NORMAL}) gets first
 *       crack at routing — which lets ANS write its routed tag before we
 *       check it.</li>
 *   <li>Player-supplied KubeJS scripts at {@code HIGH}/{@code HIGHEST}
 *       can still pre-empt Iron's Botany cleanly.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID)
public final class SpellEventHandlers {

    private SpellEventHandlers() {}

    /**
     * Gate the cast on Botania mana availability. Called before ISS
     * debits ISS mana, so cancellation here costs the player nothing.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onSpellPreCast(SpellPreCastEvent event) {
        if (CommonConfig.MANA_UNIFICATION_MODE.get() == ManaUnificationMode.DISABLED) return;

        Player player = event.getEntity();
        AbstractSpell spell = SpellRegistry.getSpell(event.getSpellId());
        if (spell == null) return;

        int level = event.getSpellLevel();
        CastSource source = event.getCastSource();

        ManaResolutionResult result = ManaBridgeManager.resolveCost(player, spell, level, source);
        if (!result.ok()) {
            player.displayClientMessage(
                    Component.translatable("ironsbotany.spell.insufficient_botania_mana"),
                    true);
            event.setCanceled(true);
        }
    }

    /**
     * Fires when ISS is about to mutate the player's mana value. The
     * bridge already covered this cast in the pre-cast hook (the
     * {@link CostRoutedTag} guarantees idempotency), so this handler
     * exists primarily as a safety net for cast paths that bypass
     * {@code SpellPreCastEvent} (e.g. some scroll implementations).
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onChangeMana(ChangeManaEvent event) {
        // Intentionally empty for now. The pre-cast hook handles the
        // routing decision; we leave this hook in place so future
        // BOTANIA_PRIMARY work (where we want to *cancel* the ISS debit
        // entirely) has a documented insertion point.
    }

    /**
     * Wipe stale cost-routed tags on a generous interval so the
     * persistent data tag set on the player doesn't accumulate
     * indefinitely. Cleared every 100 ticks (5 seconds); within that
     * window the same-tick equality check inside {@link CostRoutedTag}
     * still does the real work.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;
        if (event.player.tickCount % 100 != 0) return;
        long now = event.player.level().getGameTime();
        long stamped = event.player.getPersistentData().getLong(CostRoutedTag.KEY_TICK);
        if (stamped > 0 && stamped + 100 < now) {
            CostRoutedTag.clear(event.player);
        }
    }
}
