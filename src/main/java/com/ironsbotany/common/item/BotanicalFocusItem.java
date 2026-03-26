package com.ironsbotany.common.item;

import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.util.ManaHelper;
import io.redspace.ironsspellbooks.api.magic.MagicData;
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

public class BotanicalFocusItem extends Item implements ICurioItem {
    
    public BotanicalFocusItem(Properties properties) {
        super(properties);
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity().level().isClientSide()) return;
        
        // Check if siphon mode is enabled
        if (stack.getOrCreateTag().getBoolean("siphonMode")) {
            // Attempt mana conversion every tick
            if (slotContext.entity() instanceof net.minecraft.world.entity.player.Player player) {
                ManaHelper.tryConvertManaToISS(player, stack);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ironsbotany.botanical_focus.tooltip.1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ironsbotany.botanical_focus.tooltip.2")
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal("+50 Max Mana").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("+10% Mana Regeneration").withStyle(ChatFormatting.GREEN));
        
        boolean siphonMode = stack.getOrCreateTag().getBoolean("siphonMode");
        tooltip.add(Component.literal("Siphon Mode: " + (siphonMode ? "ON" : "OFF"))
                .withStyle(siphonMode ? ChatFormatting.GREEN : ChatFormatting.RED));
        
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
