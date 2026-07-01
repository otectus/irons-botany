package com.ironsbotany.common.item;

import com.ironsbotany.common.bridge.CostRoutedTag;
import io.redspace.ironsspellbooks.item.Scroll;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * Reusable scroll variant that pulls cast cost from the Botania mana
 * network instead of consuming itself. Falls back to single-use scroll
 * behavior if no Botania mana is available.
 *
 * <p>Mechanics:
 * <ul>
 *   <li>{@link com.ironsbotany.common.bridge.ManaBridgeManager}
 *       recognizes Elementium scrolls in the player's hand and tries to
 *       charge the {@code elementiumScrollManaCost} config value (a flat
 *       Botania floor, since scroll spells usually report 0 ISS mana cost)
 *       before the ISS pipeline consumes the scroll.</li>
 *   <li>{@link #removeScrollAfterCast} checks the
 *       {@link CostRoutedTag} — if Botania paid for this tick, the
 *       scroll stays in inventory; otherwise the parent's vanilla
 *       single-use behavior consumes the stack.</li>
 * </ul>
 */
public class ElementiumScrollItem extends Scroll {

    public ElementiumScrollItem() {
        // Iron's Spells 3.16 changed Scroll's constructor from no-arg to
        // Scroll(Item.Properties). Mirror ISS's own base scroll registration
        // (RARE rarity). This binds to the 3.16 API, so 1.8.1 requires ISS 3.16+.
        super(new Item.Properties().rarity(Rarity.RARE));
    }

    @Override
    protected void removeScrollAfterCast(ServerPlayer player, ItemStack stack) {
        long tick = player.level().getGameTime();
        // Keep the scroll only if Botania specifically paid for THIS scroll cast this
        // tick. Using the dedicated scroll-paid marker (not the generic routed tag)
        // prevents a same-tick non-scroll routed cast from granting a free scroll.
        if (CostRoutedTag.isScrollPaid(player, tick)) {
            return;
        }
        super.removeScrollAfterCast(player, stack);
    }
}
