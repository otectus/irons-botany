package com.ironsbotany.common.item;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

public class BotanicalRingItem extends Item implements ICurioItem {
    private static final UUID MAX_MANA_UUID = UUID.fromString("d1e2f3a4-b5c6-7890-1234-567890abcdef");
    private static final UUID SPELL_POWER_UUID = UUID.fromString("e2f3a4b5-c6d7-8901-2345-67890abcdef0");

    private static final int MAX_MANA_BONUS = 25;
    private static final double SPELL_POWER_BONUS = 0.05;

    public BotanicalRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();
        modifiers.put(AttributeRegistry.MAX_MANA.get(),
                new AttributeModifier(MAX_MANA_UUID, "Botanical Ring Max Mana",
                        MAX_MANA_BONUS, AttributeModifier.Operation.ADDITION));
        modifiers.put(AttributeRegistry.SPELL_POWER.get(),
                new AttributeModifier(SPELL_POWER_UUID, "Botanical Ring Spell Power",
                        SPELL_POWER_BONUS, AttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ironsbotany.botanical_ring.tooltip")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("+" + MAX_MANA_BONUS + " Max Mana").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("+" + (int)(SPELL_POWER_BONUS * 100) + "% Nature Spell Power").withStyle(ChatFormatting.GREEN));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
