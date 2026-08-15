package com.ironsbotany.common.item;

import com.ironsbotany.common.config.CommonConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * The gameplay effect of a rune-enhanced scroll.
 *
 * <h3>Why this class exists</h3>
 * Through 1.9.0 the rune-scroll fusion recipe consumed a Botania rune and wrote {@code runeType}
 * and {@code runeEnhanced} into the resulting scroll's NBT — and <em>nothing in the mod ever read
 * either value</em>. The recipe charged a player a rune for a renamed scroll with a suffix and no
 * mechanical difference whatsoever.
 *
 * <p>Rather than delete a recipe players may already have used, 1.10.0 gives the NBT a real,
 * bounded meaning: a rune-enhanced scroll costs less Botania mana to cast, by an amount that
 * depends on which rune was fused into it. The value is read once, by the cast transaction's cost
 * composition, alongside every other modifier.
 *
 * <h3>Bounds</h3>
 * The discount is clamped to {@code [0, 0.5]} and applied multiplicatively with every other
 * discount, so no combination of rune, channel, efficiency and armour set can reach a free cast.
 * An unrecognised or malformed {@code runeType} yields the base discount rather than an error —
 * scrolls fused before this release, and scrolls fused with runes from a Botania addon, still work.
 */
public final class RuneEnhancement {

    public static final String KEY_ENHANCED = "runeEnhanced";
    public static final String KEY_RUNE_TYPE = "runeType";

    /** Hard ceiling on the discount, independent of config, so this can never trivialise casting. */
    public static final double MAX_DISCOUNT = 0.5;

    private RuneEnhancement() {}

    /** True if {@code stack} carries the fusion recipe's marker. */
    public static boolean isEnhanced(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) return false;
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(KEY_ENHANCED);
    }

    /** The fused rune's item id, or empty if absent/malformed. */
    public static String runeType(ItemStack stack) {
        if (!isEnhanced(stack)) return "";
        CompoundTag tag = stack.getTag();
        return tag == null ? "" : tag.getString(KEY_RUNE_TYPE);
    }

    /**
     * Fractional Botania-cost discount this scroll grants, in {@code [0, MAX_DISCOUNT]}.
     *
     * <p>Elemental runes (the four cheap first-tier runes) give the base discount; the more
     * expensive seasonal and celestial runes give a larger one, so the choice of rune matters and
     * the recipe's cost is reflected in its benefit.
     *
     * @return {@code 0.0} for a stack that is not rune-enhanced or when the feature is disabled
     */
    public static double manaDiscount(ItemStack stack) {
        if (!isEnhanced(stack)) return 0.0;

        double base = CommonConfig.RUNE_SCROLL_MANA_DISCOUNT.get();
        if (base <= 0) return 0.0;

        double multiplier = tierMultiplierFor(runeType(stack));
        return Math.max(0.0, Math.min(MAX_DISCOUNT, base * multiplier));
    }

    /**
     * Rune tiers follow Botania's own progression: the four elemental runes are the entry tier, the
     * eight seasonal/thematic runes the second, and the rest (Mana, Gaia, Pride, and any modded
     * rune) the third.
     */
    private static double tierMultiplierFor(String runeId) {
        if (runeId == null || runeId.isEmpty()) return 1.0;

        ResourceLocation id = ResourceLocation.tryParse(runeId);
        String path = id == null ? runeId : id.getPath();

        return switch (path) {
            case "rune_water", "rune_fire", "rune_earth", "rune_air" -> 1.0;
            case "rune_spring", "rune_summer", "rune_autumn", "rune_winter",
                 "rune_mana", "rune_lust", "rune_gluttony", "rune_greed" -> 1.5;
            default -> 2.0; // Gaia, Pride, Envy, Wrath, Sloth, and any addon rune
        };
    }
}
