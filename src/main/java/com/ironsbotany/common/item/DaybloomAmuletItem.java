package com.ironsbotany.common.item;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

/**
 * Necklace curio that grants daytime-conditional spell-power and
 * cast-speed bonuses. The static {@code getAttributeModifiers}
 * doesn't see the world state, so the time-of-day check happens in
 * {@link #curioTick}: a transient attribute modifier (UUID-keyed) is
 * applied during day and removed at night.
 *
 * <p>Stats while in daylight (sky visible + day):
 * <ul>
 *   <li>+15% Nature Spell Power</li>
 *   <li>+5% Cast Time Reduction</li>
 * </ul>
 *
 * <p>Pattern adapted from Botanical Focus's siphon-toggle: transient
 * UUID-keyed modifiers added/removed each tick. Costs ~one attribute
 * lookup per equipped wearer per tick — negligible.
 */
public class DaybloomAmuletItem extends Item implements ICurioItem {

    private static final UUID DAY_NATURE_POWER_UUID = UUID.fromString("c5d6e7f8-9012-3456-7890-abcdef012345");
    private static final UUID DAY_CAST_SPEED_UUID = UUID.fromString("c5d6e7f8-9012-3456-7890-abcdef012346");

    private static final double NATURE_POWER_DAY = 0.15;
    private static final double CAST_SPEED_DAY = 0.05;

    public DaybloomAmuletItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        // Static modifiers: empty. Daytime bonuses are applied in curioTick.
        return LinkedHashMultimap.create();
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.level().isClientSide()) return;
        // Only re-check every 20 ticks; the day/night transition is gradual.
        if (entity.tickCount % 20 != 0) return;

        boolean inDaylight = entity.level().isDay() && entity.level().canSeeSky(entity.blockPosition());
        applyConditional(entity, AttributeRegistry.NATURE_SPELL_POWER.get(),
                DAY_NATURE_POWER_UUID, "Daybloom Amulet Nature Power", NATURE_POWER_DAY, inDaylight);
        applyConditional(entity, AttributeRegistry.CAST_TIME_REDUCTION.get(),
                DAY_CAST_SPEED_UUID, "Daybloom Amulet Cast Speed", CAST_SPEED_DAY, inDaylight);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        // Strip transient modifiers when removed.
        LivingEntity entity = slotContext.entity();
        applyConditional(entity, AttributeRegistry.NATURE_SPELL_POWER.get(),
                DAY_NATURE_POWER_UUID, "Daybloom Amulet Nature Power", NATURE_POWER_DAY, false);
        applyConditional(entity, AttributeRegistry.CAST_TIME_REDUCTION.get(),
                DAY_CAST_SPEED_UUID, "Daybloom Amulet Cast Speed", CAST_SPEED_DAY, false);
    }

    private static void applyConditional(LivingEntity entity, Attribute attribute, UUID uuid,
                                         String name, double amount, boolean shouldBeActive) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) return;
        AttributeModifier existing = instance.getModifier(uuid);
        if (shouldBeActive && existing == null) {
            instance.addPermanentModifier(new AttributeModifier(uuid, name, amount,
                    AttributeModifier.Operation.MULTIPLY_BASE));
        } else if (!shouldBeActive && existing != null) {
            instance.removeModifier(uuid);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ironsbotany.daybloom_amulet.tooltip")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("+15% Nature Spell Power (in daylight)").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("+5% Cast Speed (in daylight)").withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
