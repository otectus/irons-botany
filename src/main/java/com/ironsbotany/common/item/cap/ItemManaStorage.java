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
        this.maxMana = maxMana;
    }

    @Override
    public int getMana() {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : Math.min(tag.getInt(NBT_KEY), maxMana);
    }

    @Override
    public int getMaxMana() {
        return maxMana;
    }

    @Override
    public void addMana(int amount) {
        int current = getMana();
        int next = Math.max(0, Math.min(current + amount, maxMana));
        stack.getOrCreateTag().putInt(NBT_KEY, next);
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
