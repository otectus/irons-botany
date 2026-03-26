package com.ironsbotany.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.util.DataKeys;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class LivingwoodStaffItem extends Item {
    private static final UUID SPELL_POWER_UUID = UUID.fromString("d1e2f3a4-b5c6-7890-1234-56789abcde01");

    public LivingwoodStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(super.getDefaultAttributeModifiers(slot));

            builder.put(AttributeRegistry.NATURE_SPELL_POWER.get(),
                    new AttributeModifier(SPELL_POWER_UUID, "Livingwood Spell Power",
                            CommonConfig.LIVINGWOOD_STAFF_SPELL_POWER.get(),
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

            return builder.build();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    public int getManaCapacity() {
        return CommonConfig.LIVINGWOOD_STAFF_MANA_CAPACITY.get();
    }

    public int getStoredMana(ItemStack stack) {
        if (stack.hasTag()) {
            return stack.getTag().getInt(DataKeys.BOTANIA_MANA);
        }
        return 0;
    }

    public void setStoredMana(ItemStack stack, int mana) {
        stack.getOrCreateTag().putInt(DataKeys.BOTANIA_MANA, Mth.clamp(mana, 0, getManaCapacity()));
    }

    public int addMana(ItemStack stack, int amount) {
        int current = getStoredMana(stack);
        int capacity = getManaCapacity();
        int toAdd = Math.min(amount, capacity - current);
        if (toAdd > 0) {
            setStoredMana(stack, current + toAdd);
        }
        return toAdd;
    }

    public int drainMana(ItemStack stack, int amount) {
        int current = getStoredMana(stack);
        int toDrain = Math.min(amount, current);
        if (toDrain > 0) {
            setStoredMana(stack, current - toDrain);
        }
        return toDrain;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getStoredMana(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int capacity = getManaCapacity();
        if (capacity == 0) return 0;
        return Math.round(13.0F * getStoredMana(stack) / capacity);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00C8FF; // Botania mana blue
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int percent = (int) (CommonConfig.LIVINGWOOD_STAFF_SPELL_POWER.get() * 100);
        tooltip.add(Component.translatable("item.ironsbotany.livingwood_staff.spell_power", percent).withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.ironsbotany.livingwood_staff.mana_stored", getStoredMana(stack), getManaCapacity())
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
