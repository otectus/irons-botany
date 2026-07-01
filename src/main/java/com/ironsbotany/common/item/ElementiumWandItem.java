package com.ironsbotany.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.registry.IBAttributes;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import io.redspace.ironsspellbooks.item.weapons.StaffTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Elementium Wand — the Elementium rung of the wand progression ladder
 * (Manasteel → <strong>Elementium</strong> → Terrasteel → Gaia). Unlike the
 * higher stat-stick wands it is a real ISS {@link StaffItem}: it casts the
 * imbued spell on use and shows the caster HUD, like {@link ManasteelStaffItem}.
 *
 * <p>Spell stats are config-driven and layered on a minimal {@link #ELEMENTIUM_TIER}
 * via {@link #getDefaultAttributeModifiers} (the tier itself is static, so the
 * tunable values are added in the override — same approach as the vanilla-Item
 * wands). Its Alfheim identity, "Elven Favor" (a chance to refund part of a
 * spell's mana cost), lives in {@code ElementiumWandHandler}.
 *
 * <p>Mana-network participation (250k buffer) is attached by
 * {@code IBCapabilityHandler}; upgrade-orb compatibility is inherent because
 * {@code StaffItem extends CastingItem}.
 */
public class ElementiumWandItem extends StaffItem {

    /** Minimal tier — base melee feel only; spell stats come from config in the override. */
    public static final StaffTier ELEMENTIUM_TIER = new StaffTier(2.0f, -3.0f);

    private static final UUID SPELL_POWER_UUID = UUID.fromString("e1f2a3b4-c5d6-7890-1234-56789abcde11");
    private static final UUID COOLDOWN_UUID = UUID.fromString("f2a3b4c5-d6e7-8901-2345-6789abcdef22");
    private static final UUID MANA_EFFICIENCY_UUID = UUID.fromString("a3b4c5d6-e7f8-9012-3456-789abcdef033");

    public ElementiumWandItem(Properties properties) {
        super(properties, ELEMENTIUM_TIER);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        // Start from StaffItem's tier-based modifiers, then layer config-driven spell stats.
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(super.getDefaultAttributeModifiers(slot));
        if (slot == EquipmentSlot.MAINHAND) {
            builder.put(AttributeRegistry.SPELL_POWER.get(),
                    new AttributeModifier(SPELL_POWER_UUID, "Elementium Wand Spell Power",
                            CommonConfig.ELEMENTIUM_WAND_SPELL_POWER.get(),
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

            builder.put(AttributeRegistry.COOLDOWN_REDUCTION.get(),
                    new AttributeModifier(COOLDOWN_UUID, "Elementium Wand Cooldown Reduction",
                            CommonConfig.ELEMENTIUM_WAND_COOLDOWN_REDUCTION.get(),
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

            builder.put(IBAttributes.MANA_EFFICIENCY.get(),
                    new AttributeModifier(MANA_EFFICIENCY_UUID, "Elementium Wand Mana Efficiency",
                            CommonConfig.ELEMENTIUM_WAND_MANA_EFFICIENCY.get(),
                            AttributeModifier.Operation.ADDITION));
        }
        return builder.build();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int spellPower = (int) (CommonConfig.ELEMENTIUM_WAND_SPELL_POWER.get() * 100);
        int cooldown = (int) (CommonConfig.ELEMENTIUM_WAND_COOLDOWN_REDUCTION.get() * 100);
        int refundChance = (int) (CommonConfig.ELEMENTIUM_WAND_REFUND_CHANCE.get() * 100);
        int refundPercent = (int) (CommonConfig.ELEMENTIUM_WAND_REFUND_PERCENT.get() * 100);
        tooltip.add(Component.translatable("item.ironsbotany.elementium_wand.spell_power", spellPower).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.ironsbotany.elementium_wand.cooldown", cooldown).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.ironsbotany.elementium_wand.elven_favor", refundChance, refundPercent).withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
