package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.item.ElementiumWandItem;
import com.ironsbotany.common.registry.IBParticles;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * "Elven Favor" — the Elementium Wand's Alfheim identity. While the wand is in
 * hand, each spell cast has a configurable chance to refund part of its mana
 * cost. Implemented on {@link SpellOnCastEvent}, which exposes the cost before
 * it is debited, so lowering it is a clean partial refund (same mechanism as
 * the Elementium mage-armor set bonus).
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID)
public class ElementiumWandHandler {

    // LOW priority: let HIGHEST-priority cancellers (cooldowns, add-on costs) settle first.
    // Note: this refund intentionally STACKS with the Elementium mage-armor set bonus
    // (ArmorSetBonusHandler#onSpellCast) — a full Elementium loadout is meant to feel
    // maximally efficient. Each handler reduces event.getManaCost() independently.
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onSpellCast(SpellOnCastEvent event) {
        Player player = event.getEntity();
        if (player == null || player.level().isClientSide()) return;

        boolean holdingWand = player.getMainHandItem().getItem() instanceof ElementiumWandItem
                || player.getOffhandItem().getItem() instanceof ElementiumWandItem;
        if (!holdingWand) return;

        double chance = CommonConfig.ELEMENTIUM_WAND_REFUND_CHANCE.get();
        if (chance <= 0) return;
        if (player.getRandom().nextDouble() >= chance) return;

        int cost = event.getManaCost();
        if (cost <= 0) return;

        double refundPercent = CommonConfig.ELEMENTIUM_WAND_REFUND_PERCENT.get();
        int refunded = (int) Math.max(1, Math.round(cost * refundPercent));
        event.setManaCost(Math.max(0, cost - refunded));

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    IBParticles.MANA_TRANSFER.get(),
                    player.getX(), player.getY() + 1, player.getZ(),
                    8, 0.3, 0.5, 0.3, 0.15);
        }
    }
}
