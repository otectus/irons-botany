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
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ManasteelWizardArmorItem extends ArmorItem {
    private static final UUID[] SPELL_POWER_UUIDS = {
            UUID.fromString("c1d2e3f4-a5b6-7890-1234-56789abcdef0"),
            UUID.fromString("d2e3f4a5-b6c7-8901-2345-6789abcdef01"),
            UUID.fromString("e3f4a5b6-c7d8-9012-3456-789abcdef012"),
            UUID.fromString("f4a5b6c7-d8e9-0123-4567-89abcdef0123")
    };
    
    private static final UUID[] MAX_MANA_UUIDS = {
            UUID.fromString("a5b6c7d8-e9f0-1234-5678-9abcdef01234"),
            UUID.fromString("b6c7d8e9-f0a1-2345-6789-abcdef012345"),
            UUID.fromString("c7d8e9f0-a1b2-3456-789a-bcdef0123456"),
            UUID.fromString("d8e9f0a1-b2c3-4567-89ab-cdef01234567")
    };

    public ManasteelWizardArmorItem(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == this.type.getSlot()) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(super.getDefaultAttributeModifiers(slot));
            
            int index = this.type.getSlot().getIndex();
            
            // Add spell power
            builder.put(AttributeRegistry.SPELL_POWER.get(),
                    new AttributeModifier(SPELL_POWER_UUIDS[index], "Manasteel Wizard Spell Power",
                            CommonConfig.MANASTEEL_ARMOR_SPELL_POWER.get(),
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
            
            // Add max mana
            builder.put(AttributeRegistry.MAX_MANA.get(),
                    new AttributeModifier(MAX_MANA_UUIDS[index], "Manasteel Wizard Max Mana",
                            CommonConfig.MANASTEEL_ARMOR_MAX_MANA.get(),
                            AttributeModifier.Operation.ADDITION));
            
            return builder.build();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int spellPowerPercent = (int) (CommonConfig.MANASTEEL_ARMOR_SPELL_POWER.get() * 100);
        int maxMana = CommonConfig.MANASTEEL_ARMOR_MAX_MANA.get();
        tooltip.add(Component.translatable("item.ironsbotany.manasteel_wizard_armor.spell_power", spellPowerPercent)
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("item.ironsbotany.manasteel_wizard_armor.max_mana", maxMana)
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.ironsbotany.manasteel_wizard_armor.set_bonus")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.ironsbotany.manasteel_wizard_armor.mana_shield")
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
