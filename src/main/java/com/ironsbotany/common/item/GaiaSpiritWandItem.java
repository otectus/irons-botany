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

public class GaiaSpiritWandItem extends Item {
    private static final UUID SPELL_POWER_UUID = UUID.fromString("f3a4b5c6-d7e8-9012-3456-789abcdef034");
    private static final UUID COOLDOWN_UUID = UUID.fromString("a4b5c6d7-e8f9-0123-4567-89abcdef0345");

    public GaiaSpiritWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(super.getDefaultAttributeModifiers(slot));

            // +30% spell power
            builder.put(AttributeRegistry.SPELL_POWER.get(),
                    new AttributeModifier(SPELL_POWER_UUID, "Gaia Spell Power",
                            CommonConfig.GAIA_WAND_SPELL_POWER.get(),
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

            // -25% cooldowns
            builder.put(AttributeRegistry.COOLDOWN_REDUCTION.get(),
                    new AttributeModifier(COOLDOWN_UUID, "Gaia Cooldown Reduction",
                            CommonConfig.GAIA_WAND_COOLDOWN_REDUCTION.get(),
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

            return builder.build();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int spellPower = (int) (CommonConfig.GAIA_WAND_SPELL_POWER.get() * 100);
        int cooldown = (int) (CommonConfig.GAIA_WAND_COOLDOWN_REDUCTION.get() * 100);
        tooltip.add(Component.literal("+" + spellPower + "% Spell Power").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("-" + cooldown + "% Spell Cooldowns").withStyle(ChatFormatting.GOLD));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
