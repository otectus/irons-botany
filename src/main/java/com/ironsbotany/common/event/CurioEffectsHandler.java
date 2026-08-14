package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.bridge.mana.BotaniaManaGateway;
import com.ironsbotany.common.item.GaiasBlessingItem;
import com.ironsbotany.common.registry.IBItems;
import com.ironsbotany.common.registry.IBSchools;
import io.redspace.ironsspellbooks.api.events.ModifySpellLevelEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Curio side-effects that cannot be expressed as static attribute modifiers — currently only
 * Gaia's Blessing.
 *
 * <h3>Why this no longer charges anything</h3>
 * {@code ModifySpellLevelEvent} is a <em>query</em>. ISS fires it from
 * {@code AbstractSpell.getLevelFor}, which runs whenever anything needs a spell's effective level:
 * casting, yes, but also spell-book UI, tooltips and add-on inspection. Through 1.9.0 this handler
 * drained a mana pool on the first firing per (tick, spell), so a player could be charged for
 * opening their spell book, and the "did I already pay" bookkeeping lived in persistent NBT keyed
 * on a tick — the same collision-prone pattern the cast bridge used.
 *
 * <p>Now the bonus level is granted whenever the surcharge is <em>affordable</em>, checked without
 * mutating anything, and the surcharge itself is folded into the cast transaction so it is debited
 * exactly once, only for a cast that actually commits. See
 * {@code ManaBridgeManager#gaiaBlessingSurcharge}.
 *
 * <p>Behaviour change worth noting in the release notes: the surcharge is now drawn from the
 * player's ordinary mana sources in the documented priority order (carried, then worn, then nearby
 * pools) rather than from a pool only. It also no longer performs its own cuboid block scan.
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID)
public final class CurioEffectsHandler {

    private CurioEffectsHandler() {}

    @SubscribeEvent
    public static void onModifySpellLevel(ModifySpellLevelEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (event.getSpell().getSchoolType() != IBSchools.BOTANY.get()) return;
        if (!isWearingBlessing(player)) return;

        // Non-destructive affordability check: grant the level only if the surcharge could be
        // paid, so the level a player is shown is the level they will actually cast at.
        int surcharge = GaiasBlessingItem.MANA_PER_CAST;
        if (surcharge > 0
                && !BotaniaManaGateway.plan(surcharge, BotaniaManaGateway.collectSources(player)).isSatisfied()) {
            return;
        }

        event.addLevels(1);
    }

    /** True if the player has Gaia's Blessing in a curio slot. */
    public static boolean isWearingBlessing(Player player) {
        try {
            return CuriosApi.getCuriosHelper()
                    .findFirstCurio(player, IBItems.GAIAS_BLESSING.get())
                    .isPresent();
        } catch (RuntimeException e) {
            // Curios is a hard dependency, but a version skew in its helper API must not break
            // casting. Treat "cannot determine" as "not worn" — the player loses a bonus rather
            // than being charged for one they did not get.
            IronsBotany.LOGGER.debug("Curios lookup for Gaia's Blessing failed: {}", e.toString());
            return false;
        }
    }
}
