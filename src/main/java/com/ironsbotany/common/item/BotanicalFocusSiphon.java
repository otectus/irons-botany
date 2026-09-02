package com.ironsbotany.common.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Siphon Mode state for the Botanical Focus, plus the one rule that splits its two gestures.
 *
 * <p>Curios' {@code RightClickItem} handler runs before {@link net.minecraft.world.item.Item#use}
 * and cancels the event whenever {@code canEquipFromUse} is {@code true}, so an unconditional
 * "equip on use" swallows the siphon toggle entirely. The two gestures therefore have to be
 * disjoint, and both sides have to decide them by the same rule: {@link #equipsOnUse(boolean)}
 * is that rule, kept here so the equip path and the toggle path cannot drift apart.
 *
 * <p>Living outside the item class also keeps this loadable in plain unit tests, which cannot
 * link the SRG-mapped Curios and Iron's Spells jars.
 */
public final class BotanicalFocusSiphon {

    /** On-disk NBT key. Pre-dates the {@code ironsbotany_} key convention; see the migration package. */
    public static final String TAG_SIPHON_MODE = "siphonMode";

    private BotanicalFocusSiphon() {}

    /** Reads Siphon Mode without creating a tag on a stack that has none. */
    public static boolean isSiphonMode(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_SIPHON_MODE);
    }

    /** Flips Siphon Mode and returns the new state. */
    public static boolean toggle(ItemStack stack) {
        boolean enabled = !isSiphonMode(stack);
        stack.getOrCreateTag().putBoolean(TAG_SIPHON_MODE, enabled);
        return enabled;
    }

    /** Sneaking equips the curio; a plain right-click is reserved for the siphon toggle. */
    public static boolean equipsOnUse(boolean sneaking) {
        return sneaking;
    }
}
