package com.ironsbotany.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.registry.IBAttributes;
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

/**
 * Terrasteel Wand — the late-Botania rung of the wand progression ladder
 * (Manasteel → Elementium → <strong>Terrasteel</strong> → Gaia). A pure-caster
 * "stat-stick": held in the main hand it boosts spell power, cuts cooldowns, and
 * improves mana efficiency, giving casters a ranged-focused alternative to the
 * melee Terrasteel Spell Blade.
 *
 * <p>All stats are config-driven via {@link CommonConfig}. The Botania mana buffer
 * is attached by {@code IBCapabilityHandler}, and upgrade-orb compatibility is
 * granted by the {@code irons_spellbooks:can_be_upgraded} data tag.
 */
public class TerrasteelWandItem extends Item {
    private static final UUID SPELL_POWER_UUID = UUID.fromString("b7c8d9e0-f1a2-3456-789a-bcdef0123789");
    private static final UUID COOLDOWN_UUID = UUID.fromString("c8d9e0f1-a2b3-4567-89ab-cdef01234890");
    private static final UUID MANA_EFFICIENCY_UUID = UUID.fromString("d9e0f1a2-b3c4-5678-9abc-def012345901");

    public TerrasteelWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(super.getDefaultAttributeModifiers(slot));

            builder.put(AttributeRegistry.SPELL_POWER.get(),
                    new AttributeModifier(SPELL_POWER_UUID, "Terrasteel Wand Spell Power",
                            CommonConfig.TERRASTEEL_WAND_SPELL_POWER.get(),
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

            builder.put(AttributeRegistry.COOLDOWN_REDUCTION.get(),
                    new AttributeModifier(COOLDOWN_UUID, "Terrasteel Wand Cooldown Reduction",
                            CommonConfig.TERRASTEEL_WAND_COOLDOWN_REDUCTION.get(),
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

            builder.put(IBAttributes.MANA_EFFICIENCY.get(),
                    new AttributeModifier(MANA_EFFICIENCY_UUID, "Terrasteel Wand Mana Efficiency",
                            CommonConfig.TERRASTEEL_WAND_MANA_EFFICIENCY.get(),
                            AttributeModifier.Operation.ADDITION));

            return builder.build();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int spellPower = (int) (CommonConfig.TERRASTEEL_WAND_SPELL_POWER.get() * 100);
        int cooldown = (int) (CommonConfig.TERRASTEEL_WAND_COOLDOWN_REDUCTION.get() * 100);
        tooltip.add(Component.translatable("item.ironsbotany.terrasteel_wand.spell_power", spellPower).withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.ironsbotany.terrasteel_wand.cooldown", cooldown).withStyle(ChatFormatting.GREEN));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
