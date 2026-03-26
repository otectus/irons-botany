package com.ironsbotany.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BotanicalUpgradeOrbItem extends Item {
    private final String orbType;

    public BotanicalUpgradeOrbItem(Properties properties, String orbType) {
        super(properties);
        this.orbType = orbType;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        switch (orbType) {
            case "flora" -> {
                tooltip.add(Component.literal("+10% Nature Spell Power").withStyle(ChatFormatting.GREEN));
            }
            case "pool" -> {
                tooltip.add(Component.literal("+100 Max ISS Mana").withStyle(ChatFormatting.BLUE));
            }
            case "bursting" -> {
                tooltip.add(Component.literal("Spells apply Mana Burst damage bonus").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            case "terran" -> {
                tooltip.add(Component.literal("+5% All Spell Power").withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.literal("+5% Cooldown Reduction").withStyle(ChatFormatting.GOLD));
            }
        }
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.ironsbotany.upgrade_orb.usage")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    public String getOrbType() {
        return orbType;
    }
}
