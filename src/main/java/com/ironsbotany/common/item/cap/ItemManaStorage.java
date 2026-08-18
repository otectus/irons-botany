package com.ironsbotany.common.item.cap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import vazkii.botania.api.mana.ManaItem;

/**
 * Per-{@link ItemStack} {@link ManaItem} implementation backed by the
 * stack's NBT. Attached to Iron's Botany–owned items via
 * {@link com.ironsbotany.common.event.IBCapabilityHandler} so they
 * appear to Botania's mana network as legitimate mana storage:
 * <ul>
 *   <li>Sparks can deposit/withdraw mana from them.</li>
 *   <li>The HUD overlay reflects their charge level.</li>
 *   <li>{@code ManaItemHandler.requestMana} aggregates them across the
 *       player's inventory and curios slots.</li>
 * </ul>
 *
 * <p>The maximum mana cap is supplied per-item via the
 * {@link #ItemManaStorage(ItemStack, int)} constructor — typically
 * pulled from a CommonConfig key so server operators can rebalance
 * without recompiling.
 */
public final class ItemManaStorage implements ManaItem {

    public static final String NBT_KEY = "ironsbotany_mana";

    private final ItemStack stack;
    private final int maxMana;

    public ItemManaStorage(ItemStack stack, int maxMana) {
        this.stack = stack;
        // A non-positive configured capacity would make every clamp collapse to zero; treat it as
        // "no storage" explicitly rather than letting it produce negative room in arithmetic.
        this.maxMana = Math.max(0, maxMana);
    }

    /**
     * Stored mana, clamped into {@code [0, maxMana]} on every read.
     *
     * <p>Clamping on read rather than trusting the tag matters for two reasons. Hand-edited or
     * malformed NBT can hold a negative value, which 1.9.0 returned verbatim — and a negative
     * "available" figure subtracts from a payment plan's total, so one corrupt item could make an
     * affordable cast fail. And an operator who <em>lowers</em> a capacity config key leaves items
     * holding more than their new maximum; reporting the clamped value keeps the item consistent
     * with its own capacity without destroying the stored figure, so raising the key again restores
     * it.
     */
    @Override
    public int getMana() {
        CompoundTag tag = stack.getTag();
        if (tag == null) return 0;
        return (int) Math.max(0L, Math.min((long) tag.getInt(NBT_KEY), maxMana));
    }

    @Override
    public int getMaxMana() {
        return maxMana;
    }

    /**
     * Add (or, with a negative amount, remove) mana, clamped into {@code [0, maxMana]}.
     *
     * <p>The sum is computed in {@code long}. In {@code int}, a large enough {@code amount} wraps
     * negative, and {@code Math.max(0, ...)} then turns what should have been a top-up into
     * <em>zeroing the item</em>.
     */
    @Override
    public void addMana(int amount) {
        long next = (long) getMana() + (long) amount;
        int clamped = (int) Math.max(0L, Math.min(next, maxMana));
        stack.getOrCreateTag().putInt(NBT_KEY, clamped);
    }

    @Override
    public boolean canReceiveManaFromPool(BlockEntity pool) {
        return true;
    }

    @Override
    public boolean canReceiveManaFromItem(ItemStack other) {
        return true;
    }

    @Override
    public boolean canExportManaToPool(BlockEntity pool) {
        return true;
    }

    @Override
    public boolean canExportManaToItem(ItemStack other) {
        return true;
    }

    @Override
    public boolean isNoExport() {
        return false;
    }
}
