package com.ironsbotany.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

public class BotanicalRingItem extends Item implements ICurioItem {
    
    public BotanicalRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ironsbotany.botanical_ring.tooltip")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("+25 Max Mana").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("+5% Botanical Spell Power").withStyle(ChatFormatting.GREEN));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
