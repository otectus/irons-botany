package com.ironsbotany.common.item;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.ironsbotany.common.util.ManaHelper;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
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

/**
 * Ring curio that bridges Botania mana into ISS mana passively. While
 * worn:
 * <ul>
 *   <li>+100 Max Mana attribute modifier</li>
 *   <li>While ISS mana is below 50% of max, every 20 ticks the ring
 *       converts 20 Botania mana → 1 ISS mana from any source the
 *       wearer's inventory exposes (tablets, accessories, the ring's
 *       own buffer if charged via Sparks).</li>
 * </ul>
 *
 * <p>The ring also participates in the Botania mana network as a 200,000
 * mana store via the capability provider in {@code IBCapabilityHandler},
 * so Sparks can deposit into it.
 */
public class ManaReservoirRingItem extends Item implements ICurioItem {

    private static final UUID MAX_MANA_UUID = UUID.fromString("a3b4c5d6-e7f8-9012-3456-7890abcdef01");
    private static final int MAX_MANA_BONUS = 100;

    public ManaReservoirRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = LinkedHashMultimap.create();
        modifiers.put(AttributeRegistry.MAX_MANA.get(),
                new AttributeModifier(MAX_MANA_UUID, "Mana Reservoir Ring Max Mana",
                        MAX_MANA_BONUS, AttributeModifier.Operation.ADDITION));
        return modifiers;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (player.tickCount % 20 != 0) return;

        MagicData magicData = MagicData.getPlayerMagicData(player);
        if (magicData == null) return;

        float current = magicData.getMana();
        float max = (float) player.getAttributeValue(AttributeRegistry.MAX_MANA.get());
        // Trigger only below the 50% threshold.
        if (current >= max * 0.5f) return;

        // Try to siphon — ManaHelper handles the ratio and clamping.
        ManaHelper.tryConvertManaToISS(player, stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ironsbotany.mana_reservoir_ring.tooltip")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("+" + MAX_MANA_BONUS + " Max Mana").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.ironsbotany.mana_reservoir_ring.tooltip.refill")
                .withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
