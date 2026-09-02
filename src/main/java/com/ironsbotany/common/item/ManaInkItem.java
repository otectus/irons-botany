package com.ironsbotany.common.item;

import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.item.InkItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * A Petal Apothecary ink that the Iron's Spells Scroll Forge actually accepts.
 *
 * <p>The forge's ink slot filters on {@code stack.getItem() instanceof InkItem}
 * ({@code ScrollForgeMenu$1#mayPlace}), not on a tag, so the three Mana Inks have to
 * <em>be</em> {@link InkItem}s rather than merely resemble them. Before this class they
 * were plain {@link TooltipItem}s and the slot rejected them, while their tooltips, the
 * Botanical Grimoire's "Scribing the Scroll" pages and the Lexica Botania entry all
 * promised they scribed ISS scrolls.
 *
 * <p>The {@link SpellRarity} passed here is what caps the spell level the forge will
 * scribe, so it must match what the tooltip advertises.
 *
 * <p>The fluid supplier is only ever dereferenced by ISS's Alchemist Cauldron, and only
 * for inks it looked up itself through {@code InkItem#getInkForRarity} — which returns
 * Iron's Spells' own items, never these. It is still given the matching ISS ink fluid so
 * the value is correct if anything ever does read it; passing the {@code RegistryObject}
 * straight through keeps the lookup lazy and free of registry-ordering hazards.
 */
public class ManaInkItem extends InkItem {

    private final String tooltipKey;

    public ManaInkItem(SpellRarity rarity, Supplier<Fluid> fluid, Properties properties, String tooltipKey) {
        super(rarity, fluid, properties);
        this.tooltipKey = tooltipKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
