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
     * Route the cost of a single spell cast through Iron's Botany's bridge.
     * Idempotent within a server tick — safe to call from multiple hook
     * points without double-billing.
     *
     * @return the routing decision; {@code result.ok() == false} means the
     *         player could not afford the spell and the cast must be
     *         cancelled.
     */
    public static ManaResolutionResult resolveCost(Player player, AbstractSpell spell, int level, CastSource source) {
        return ManaBridgeManager.resolveCost(player, spell, level, source);
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
