package com.ironsbotany.common.util;

import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.item.MageArmorItem;
import com.ironsbotany.common.item.ManasteelWizardArmorItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Helpers for detecting full mage-armor sets and applying their passive
 * bonuses. Set <em>effects</em> live in {@code ArmorSetBonusHandler}; this
 * class only answers "is the full set worn?" and computes the Manasteel
 * Botania-cost discount used by the mana bridge.
 */
public final class MageArmorSets {

    private MageArmorSets() {}

    /** True iff all four armor slots hold {@link ManasteelWizardArmorItem}. */
    public static boolean hasFullManasteel(Player player) {
        int count = 0;
        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.getItem() instanceof ManasteelWizardArmorItem) count++;
        }
        return count == 4;
    }

    /** True iff all four armor slots hold {@link MageArmorItem} of the given {@code tier}. */
    public static boolean hasFullTier(Player player, MageArmorItem.Tier tier) {
        int count = 0;
        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.getItem() instanceof MageArmorItem mage && mage.getTier() == tier) count++;
        }
        return count == 4;
    }

    /**
     * Multiplier applied to Botania spell costs for the full Manasteel set's
     * small mana-cost discount. Returns {@code 1.0} (no discount) otherwise.
     */
    public static double botaniaCostMultiplier(Player player) {
        if (player != null && hasFullManasteel(player)) {
            return Math.max(0.0, 1.0 - CommonConfig.MANASTEEL_SET_COST_DISCOUNT.get());
        }
        return 1.0;
    }

    /** Apply the Manasteel set discount to a Botania cost, rounding down but never below 0. */
    public static int applyBotaniaDiscount(Player player, int cost) {
        if (cost <= 0) return cost;
        double mult = botaniaCostMultiplier(player);
        if (mult >= 1.0) return cost;
        return Math.max(0, (int) Math.floor(cost * mult));
    }
}
