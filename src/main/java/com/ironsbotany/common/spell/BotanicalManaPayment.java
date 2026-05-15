package com.ironsbotany.common.spell;

import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.config.ManaUnificationMode;
import com.ironsbotany.common.spell.catalyst.SpellContext;
import com.ironsbotany.common.util.ManaHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Single authoritative pipeline for Botania-mana payment on a botanical spell
 * cast. Centralises the five-mode branching that previously lived inline in
 * {@code AbstractBotanicalSpell.onCast}.
 *
 * <p>Returns {@code true} when payment succeeded (or no Botania payment is
 * required for the active {@link ManaUnificationMode}), {@code false} when the
 * player is short on Botania mana — in which case a translated chat message
 * has already been shown to the player and the caller should abort the cast.</p>
 */
public final class BotanicalManaPayment {
    private BotanicalManaPayment() {}

    public static boolean pay(Player player, SpellContext context,
                              AbstractBotanicalSpell spell, int spellLevel) {
        ManaUnificationMode mode = CommonConfig.MANA_UNIFICATION_MODE.get();

        // DISABLED / ISS_PRIMARY: spell pays only ISS mana; nothing to do here.
        if (mode == ManaUnificationMode.DISABLED || mode == ManaUnificationMode.ISS_PRIMARY) {
            return true;
        }

        // HYBRID without dual-cost: ISS mana only.
        if (mode == ManaUnificationMode.HYBRID && !CommonConfig.ENABLE_DUAL_COST_SPELLS.get()) {
            return true;
        }

        // BOTANIA_PRIMARY | SEPARATE | HYBRID-with-dual-cost: drain Botania.
        // Pool Attunement Charm hook: Iron's Botany spells are Nature-school
        // by contract (AbstractBotanicalSpell.getSchoolType), so the bound-pool
        // variant is the canonical entry point. ManaHelper enforces the
        // Nature-only filter implicitly because only this pipeline calls it.
        int botaniaRequired = context.getModifiedManaCost(spell.getBotaniaManaCost(spellLevel));
        if (!ManaHelper.hasBotaniaManaWithBoundPool(player, botaniaRequired)) {
            player.displayClientMessage(
                Component.translatable("ironsbotany.spell.insufficient_botania_mana", botaniaRequired),
                true);
            return false;
        }
        return ManaHelper.drainBotaniaManaWithBoundPool(player, botaniaRequired);
    }
}
