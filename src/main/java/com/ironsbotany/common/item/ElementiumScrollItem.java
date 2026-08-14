package com.ironsbotany.common.item;

import com.ironsbotany.common.bridge.cast.CastTransactions;
import io.redspace.ironsspellbooks.item.Scroll;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * A scroll that spends Botania mana instead of consuming itself.
 *
 * <h3>Mechanics</h3>
 * <ul>
 *   <li>{@code ManaBridgeManager.preflight} prices the cast at
 *       {@code elementiumScrollManaCost} — a flat floor, because scroll spells usually report a
 *       zero ISS mana cost — and reserves an exact payment plan.</li>
 *   <li>If the plan commits, the cast is paid for by the mana network and this scroll survives.</li>
 *   <li>If the network cannot pay, the cast still proceeds and the scroll is consumed exactly like
 *       an ordinary one. That fallback is the documented design, not a failure path.</li>
 * </ul>
 *
 * <h3>Why retention is keyed to the stack</h3>
 * Through 1.9.0 retention was decided by a "Botania paid on tick N" flag on the player. Any scroll
 * cast in a tick where Botania had paid for <em>any</em> cast was kept for free. Retention is now
 * tied to the transaction opened for this cast <em>and</em> to the identity of the stack ISS
 * recorded as the casting item — the same stack this method is asked to consume.
 */
public class ElementiumScrollItem extends Scroll {

    public ElementiumScrollItem() {
        // ISS 3.16 changed Scroll's constructor from no-arg to Scroll(Item.Properties). Mirrors
        // ISS's own base scroll registration (RARE), and binds this class to the 3.16+ API.
        super(new Item.Properties().rarity(Rarity.RARE));
    }

    @Override
    protected void removeScrollAfterCast(ServerPlayer player, ItemStack stack) {
        if (CastTransactions.wasScrollRetained(player, stack)) {
            return;
        }
        super.removeScrollAfterCast(player, stack);
    }
}
