package com.ironsbotany.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.ironsbotany.common.config.CommonConfig;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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

public class DreamwoodScepterItem extends Item {
    private static final UUID SPELL_POWER_UUID = UUID.fromString("e2f3a4b5-c6d7-8901-2345-6789abcdef02");

    public DreamwoodScepterItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(super.getDefaultAttributeModifiers(slot));

            builder.put(AttributeRegistry.NATURE_SPELL_POWER.get(),
                    new AttributeModifier(SPELL_POWER_UUID, "Dreamwood Spell Power",
                            CommonConfig.DREAMWOOD_SCEPTER_SPELL_POWER.get(),
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

            return builder.build();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int percent = (int) (CommonConfig.DREAMWOOD_SCEPTER_SPELL_POWER.get() * 100);
        tooltip.add(Component.translatable("item.ironsbotany.dreamwood_scepter.spell_power", percent).withStyle(ChatFormatting.GREEN));
        int conversionPercent = (int) (CommonConfig.DREAMWOOD_CONVERSION_PERCENT.get() * 100);
        // Conversion logic implemented in DreamwoodConversionHandler
        tooltip.add(Component.translatable("item.ironsbotany.dreamwood_scepter.conversion", conversionPercent).withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
