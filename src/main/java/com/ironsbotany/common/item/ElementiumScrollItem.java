package com.ironsbotany.common.item;

import com.ironsbotany.common.bridge.CostRoutedTag;
import io.redspace.ironsspellbooks.item.Scroll;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * Reusable scroll variant that pulls cast cost from the Botania mana
 * network instead of consuming itself. Falls back to single-use scroll
 * behavior if no Botania mana is available.
 *
 * <p>Mechanics:
 * <ul>
 *   <li>{@link com.ironsbotany.common.bridge.ManaBridgeManager}
 *       recognizes Elementium scrolls in the player's hand and tries
 *       to charge {@code spell.getManaCost(level) × elementiumScrollMultiplier}
 *       Botania mana before the ISS pipeline debits the scroll.</li>
 *   <li>{@link #removeScrollAfterCast} checks the
 *       {@link CostRoutedTag} — if Botania paid for this tick, the
 *       scroll stays in inventory; otherwise the parent's vanilla
 *       single-use behavior consumes the stack.</li>
 * </ul>
 */
public class ElementiumScrollItem extends Scroll {

    public ElementiumScrollItem() {
        super();
    }

    @Override
    protected void removeScrollAfterCast(ServerPlayer player, ItemStack stack) {
        long tick = player.level().getGameTime();
        // If our bridge already routed cost this tick, Botania paid — don't burn the scroll.
        if (player.getPersistentData().contains(CostRoutedTag.KEY_TICK)
                && player.getPersistentData().getLong(CostRoutedTag.KEY_TICK) == tick) {
            return;
        }
        super.removeScrollAfterCast(player, stack);
    }
}
