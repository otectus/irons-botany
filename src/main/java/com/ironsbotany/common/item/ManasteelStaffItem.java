package com.ironsbotany.common.item;

import com.ironsbotany.common.registry.IBAttributes;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import io.redspace.ironsspellbooks.item.weapons.StaffTier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;

/**
 * Botany-flavored ISS staff. Extends {@link StaffItem} so it inherits
 * the cast-on-use behavior, lectern interaction, and HUD overlay
 * support; otherwise it's a thin subclass that supplies a custom
 * {@link StaffTier}.
 *
 * <p>Mana network participation is handled separately by
 * {@code IBCapabilityHandler}, which attaches a 50,000-mana
 * {@code MANA_ITEM} capability to every Manasteel Staff stack — so the
 * staff appears in the Botania mana HUD and can be drained by
 * {@code ManaItemHandler.requestMana} alongside Mana Tablets.
 */
public class ManasteelStaffItem extends StaffItem {

    /**
     * Tier values: lower physical damage than Graybeard (-2 melee) but
     * higher cast support — +20 max mana, +5% cast-time reduction, +10%
     * Botany spell power, +0.05 mana efficiency.
     */
    public static final StaffTier MANASTEEL_TIER = new StaffTier(
            2.0f,   // attack damage bonus
            -3.0f,  // attack speed (slow swing, normal staff feel)
            new AttributeContainer(AttributeRegistry.MAX_MANA, 20.0, AttributeModifier.Operation.ADDITION),
            new AttributeContainer(AttributeRegistry.CAST_TIME_REDUCTION, 0.05, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(IBAttributes.BOTANY_SPELL_POWER, 0.10, AttributeModifier.Operation.MULTIPLY_BASE),
            new AttributeContainer(IBAttributes.MANA_EFFICIENCY, 0.05, AttributeModifier.Operation.ADDITION)
    );

    public ManasteelStaffItem(Item.Properties properties) {
        super(properties, MANASTEEL_TIER);
    }
}
