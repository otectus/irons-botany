package com.ironsbotany.common.item.cap;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaItem;

/**
 * Capability provider that exposes {@link BotaniaForgeCapabilities#MANA_ITEM}
 * for an {@link ItemStack}, backed by an {@link ItemManaStorage}.
 *
 * <p>Created in {@code AttachCapabilitiesEvent<ItemStack>} for items that
 * Iron's Botany wants to make first-class mana network citizens
 * (Livingwood Staff, Botanical Focus, Botanical Ring, Manasteel Wizard
 * armor, future spellbooks).
 */
public final class ItemManaCapabilityProvider implements ICapabilityProvider {

    private final LazyOptional<ManaItem> instance;

    public ItemManaCapabilityProvider(ItemStack stack, int maxMana) {
        this.instance = LazyOptional.of(() -> new ItemManaStorage(stack, maxMana));
    }

    @Override
    @NotNull
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == BotaniaForgeCapabilities.MANA_ITEM) {
            return instance.cast();
        }
        return LazyOptional.empty();
    }
}
