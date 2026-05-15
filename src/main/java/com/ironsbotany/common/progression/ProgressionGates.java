package com.ironsbotany.common.progression;

import com.ironsbotany.common.util.DataKeys;
import net.minecraft.world.entity.player.Player;

/**
 * Reader-side companion to {@link UnifiedAdvancementSystem}. The advancement
 * system writes flags into player persistent data on Botania milestones; these
 * helpers expose them to gameplay code so the flags actually mean something.
 *
 * <ul>
 *   <li>{@link #isTier4Unlocked(Player)} — gates LEGENDARY-tier catalysts
 *       (consumed by {@code SpellCatalystRegistry.getActiveCatalysts}).</li>
 *   <li>{@link #isDualSchoolUnlocked(Player)} — gates dual-school scroll
 *       enchantment via {@code AlfheimScrollCrafting.markAlfheimScroll}.</li>
 *   <li>{@link #isOverchargeUnlocked(Player)} — gates Pool Attunement Charm
 *       activation in Phase 4 and long-range pool binding.</li>
 * </ul>
 */
public final class ProgressionGates {
    private ProgressionGates() {}

    public static boolean isTier4Unlocked(Player player) {
        return player != null && player.getPersistentData().getBoolean(DataKeys.TIER4_UNLOCKED);
    }

    public static boolean isDualSchoolUnlocked(Player player) {
        return player != null && player.getPersistentData().getBoolean(DataKeys.DUAL_SCHOOL_UNLOCKED);
    }

    public static boolean isOverchargeUnlocked(Player player) {
        return player != null && player.getPersistentData().getBoolean(DataKeys.OVERCHARGE_UNLOCKED);
    }
}
