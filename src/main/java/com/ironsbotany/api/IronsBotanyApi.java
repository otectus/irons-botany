package com.ironsbotany.api;

import com.ironsbotany.common.bridge.ManaBridgeManager;
import com.ironsbotany.common.bridge.ManaResolutionResult;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.entity.player.Player;

/**
 * Iron's Botany public API entry point. Downstream addons and KubeJS
 * scripts should depend only on classes under {@code com.ironsbotany.api.*}
 * — these maintain backward compatibility across patch releases.
 *
 * <p>Everything under {@code com.ironsbotany.common.*} is internal and
 * may change without notice.
 */
public final class IronsBotanyApi {

    /** Canonical NBT/data prefix used by all IB-published persistent state. */
    public static final String NAMESPACE = "ironsbotany";

    private IronsBotanyApi() {}

    /**
     * Ask whether a cast can be paid for, and reserve an exact payment plan if it can.
     *
     * <p>This performs <strong>no mutation</strong>. The reservation is a plan; mana moves only
     * when Iron's Botany commits it during ISS's {@code SpellOnCastEvent}. A caller that decides
     * not to proceed simply lets the reservation lapse — there is nothing to undo.
     *
     * @return {@code true} if the cast may proceed. {@code false} means the player cannot pay in a
     *         mode that requires Botania payment, and the caller must cancel the cast.
     */
    public static boolean preflightCost(Player player, AbstractSpell spell, int level, CastSource source) {
        return ManaBridgeManager.preflight(player, spell, level, source).allow();
    }

    /**
     * @deprecated since 1.10.0. Cost routing is no longer a single "resolve and charge" call: it is
     *         a two-phase transaction (reserve during {@code SpellPreCastEvent}, debit during
     *         {@code SpellOnCastEvent}) so that a cast which fails after preflight cannot leave a
     *         partial debit behind. This shim runs only the reserving half and reports no charged
     *         amounts, because at preflight time nothing has been charged. Use
     *         {@link #preflightCost} instead.
     */
    @Deprecated(since = "1.10.0", forRemoval = true)
    public static ManaResolutionResult resolveCost(Player player, AbstractSpell spell, int level, CastSource source) {
        return preflightCost(player, spell, level, source)
                ? ManaResolutionResult.NOOP
                : ManaResolutionResult.INSUFFICIENT;
    }

    /**
     * @return the central flower registry for downstream mods that want to
     *         contribute Botany-school-themed generating or functional
     *         flowers. See {@link BotanySchoolFlowerRegistry} for usage.
     */
    public static BotanySchoolFlowerRegistry flowerRegistry() {
        return BotanySchoolFlowerRegistry.INSTANCE;
    }
}
