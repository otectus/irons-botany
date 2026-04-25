package com.ironsbotany.common.item;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.UUID;

/**
 * Legendary necklace curio. While worn, Botany-school spells gain +1
 * effective level via {@code ModifySpellLevelEvent} (subscribed in
 * {@link com.ironsbotany.common.event.CurioEffectsHandler}). The +1 is
 * paid for by draining 100,000 Botania mana from a {@code ManaPool}
 * within 16 blocks of the caster — if no pool can pay, the bonus
 * fizzles silently for that cast.
 *
 * <p>This is the canonical Phase 6.5 endgame curio: low passive
 * footprint, high reward when paired with Botania mana infrastructure.
 */
public class GaiasBlessingItem extends Item implements ICurioItem {

    /** Mana cost per spell cast where the +1 level is granted. */
    public static final int MANA_PER_CAST = 100_000;

    /** Range to scan for mana pools when paying the cost. */
    public static final int POOL_SCAN_RADIUS = 16;

    public GaiasBlessingItem(Properties properties) {
        super(properties.rarity(Rarity.EPIC).fireResistant());
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
        // No passive attributes — the value is the +1 spell level granted via event.
        return LinkedHashMultimap.create();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.ironsbotany.gaias_blessing.tooltip.1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.ironsbotany.gaias_blessing.tooltip.2")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.ironsbotany.gaias_blessing.tooltip.3",
                MANA_PER_CAST, POOL_SCAN_RADIUS)
                .withStyle(ChatFormatting.DARK_PURPLE));
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
