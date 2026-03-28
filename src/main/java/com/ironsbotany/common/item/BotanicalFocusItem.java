package com.ironsbotany.common.item;

import com.ironsbotany.common.util.ManaHelper;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

public class BotanicalFocusItem extends Item implements ICurioItem {
    private static final UUID MAX_MANA_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef0123456789");
    private static final UUID MANA_REGEN_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f01234567890");

    public BotanicalFocusItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            boolean current = stack.getOrCreateTag().getBoolean("siphonMode");
            stack.getOrCreateTag().putBoolean("siphonMode", !current);
            player.displayClientMessage(
                Component.translatable("item.ironsbotany.botanical_focus.siphon_" + (!current ? "on" : "off")),
                true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();
        modifiers.put(AttributeRegistry.MAX_MANA.get(),
            new AttributeModifier(MAX_MANA_UUID, "Botanical Focus Max Mana", 50,
                AttributeModifier.Operation.ADDITION));
        modifiers.put(AttributeRegistry.MANA_REGEN.get(),
            new AttributeModifier(MANA_REGEN_UUID, "Botanical Focus Mana Regen", 0.1,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        return modifiers;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity().level().isClientSide()) return;

        // Siphon: convert Botania mana from inventory items to ISS mana (once per second)
        if (stack.getOrCreateTag().getBoolean("siphonMode")) {
            if (slotContext.entity() instanceof Player player) {
                if (player.level().getGameTime() % 20 == 0) {
                    // Find a mana item in the player's inventory to siphon from
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack invStack = player.getInventory().getItem(i);
                        if (!invStack.isEmpty() && ManaHelper.tryConvertManaToISS(player, invStack)) {
                            break; // Converted from one source per tick
                        }
                    }
                }
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
        tooltip.add(Component.translatable("item.ironsbotany.botanical_focus.siphon_" + (siphonMode ? "on" : "off"))
                .withStyle(siphonMode ? ChatFormatting.GREEN : ChatFormatting.RED));

        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
