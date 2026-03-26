package com.ironsbotany.common.corporea;

import com.ironsbotany.IronsBotany;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import vazkii.botania.api.corporea.CorporeaHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Integrates spell casting with Botania's Corporea system.
 * Allows spells to automatically request reagents from Corporea networks.
 */
public class SpellLogisticsSystem {

    /**
     * Request spell reagents from nearby Corporea network
     * @param player The player casting the spell
     * @param spell The spell being cast
     * @param spellLevel The spell level
     * @return true if all reagents were obtained
     */
    public static boolean requestSpellReagents(Player player, AbstractSpell spell, int spellLevel) {
        if (!ModList.get().isLoaded("botania")) {
            return true; // No Corporea, proceed normally
        }

        // Get required reagents for spell
        List<ItemStack> requiredReagents = getSpellReagents(spell, spellLevel);
        if (requiredReagents.isEmpty()) {
            return true; // No reagents needed
        }

        // Find nearby Corporea spark
        BlockPos sparkPos = findNearestCorporeaNode(player);
        if (sparkPos == null) {
            return false; // No Corporea network available
        }

        // Request each reagent
        boolean anyRequested = false;
        for (ItemStack reagent : requiredReagents) {
            if (!requestFromCorporea(player, sparkPos, reagent)) {
                return false; // Failed to get reagent
            }
            anyRequested = true;
        }

        // Grant corporea_link advancement on first successful request
        if (anyRequested && player instanceof ServerPlayer serverPlayer) {
            var advancement = serverPlayer.server.getAdvancements()
                .getAdvancement(ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "corporea_link"));
            if (advancement != null) {
                serverPlayer.getAdvancements().award(advancement, "corporea_request");
            }
        }

        return true;
    }

    /**
     * Get required reagents for a spell based on level
     */
    private static List<ItemStack> getSpellReagents(AbstractSpell spell, int spellLevel) {
        List<ItemStack> reagents = new ArrayList<>();

        // High-tier spells require rune components
        if (spellLevel >= 5) {
            ItemStack rune = getRuneForSpell(spell);
            if (!rune.isEmpty()) {
                reagents.add(rune);
            }
        }

        // Ultimate spells require Gaia components
        if (spellLevel >= 8) {
            ItemStack gaia = getGaiaComponent(spell);
            if (!gaia.isEmpty()) {
                reagents.add(gaia);
            }
        }

        return reagents;
    }

    /**
     * Get appropriate rune for spell school via reflection on BotaniaItems
     */
    private static ItemStack getRuneForSpell(AbstractSpell spell) {
        try {
            String runeFieldName;
            var school = spell.getSchoolType();

            if (school == SchoolRegistry.FIRE.get()) {
                runeFieldName = "runeFire";
            } else if (school == SchoolRegistry.ICE.get()) {
                runeFieldName = "runeWater";
            } else if (school == SchoolRegistry.LIGHTNING.get()) {
                runeFieldName = "runeAir";
            } else if (school == SchoolRegistry.NATURE.get()) {
                runeFieldName = "runeEarth";
            } else if (school == SchoolRegistry.HOLY.get()) {
                runeFieldName = "runeMana";
            } else {
                // Botanical school and others default to Rune of Mana
                runeFieldName = "runeMana";
            }

            return getBotaniaItem(runeFieldName);
        } catch (Exception e) {
            IronsBotany.LOGGER.debug("Failed to resolve rune for spell: {}", e.getMessage());
            return ItemStack.EMPTY;
        }
    }

    /**
     * Get Gaia component for ultimate spells
     */
    private static ItemStack getGaiaComponent(AbstractSpell spell) {
        try {
            return getBotaniaItem("gaiaIngot");
        } catch (Exception e) {
            IronsBotany.LOGGER.debug("Failed to resolve Gaia Spirit: {}", e.getMessage());
            return ItemStack.EMPTY;
        }
    }

    /**
     * Resolve a Botania item by field name using reflection.
     * BotaniaItems fields are Supplier<Item>, following the pattern in CatalystRegistration.
     */
    static ItemStack getBotaniaItem(String fieldName) {
        try {
            Class<?> botaniaItems = Class.forName("vazkii.botania.common.item.BotaniaItems");
            Object fieldValue = botaniaItems.getField(fieldName).get(null);
            if (fieldValue instanceof Supplier<?> supplier) {
                Object item = supplier.get();
                if (item instanceof Item i) {
                    return new ItemStack(i);
                }
            }
            // Some fields may be Item directly rather than Supplier
            if (fieldValue instanceof Item i) {
                return new ItemStack(i);
            }
        } catch (Exception e) {
            IronsBotany.LOGGER.debug("Failed to resolve Botania item '{}': {}", fieldName, e.getMessage());
        }
        return ItemStack.EMPTY;
    }

    /**
     * Find nearest block with a Corporea spark
     */
    private static BlockPos findNearestCorporeaNode(Player player) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();
        int searchRadius = 16;

        for (BlockPos pos : BlockPos.betweenClosed(
            playerPos.offset(-searchRadius, -searchRadius, -searchRadius),
            playerPos.offset(searchRadius, searchRadius, searchRadius))) {

            if (CorporeaHelper.instance().doesBlockHaveSpark(level, pos)) {
                return pos.immutable();
            }
        }

        return null;
    }

    /**
     * Request an item from Corporea network
     */
    static boolean requestFromCorporea(Player player, BlockPos sparkPos, ItemStack requested) {
        if (requested.isEmpty()) {
            return true; // Nothing to request
        }

        try {
            Level level = player.level();

            // Check if player already has the item
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, requested)) {
                    return true;
                }
            }

            // Try to request from Corporea network
            if (!CorporeaHelper.instance().doesBlockHaveSpark(level, sparkPos)) {
                return false;
            }

            var spark = CorporeaHelper.instance().getSparkForBlock(level, sparkPos);
            if (spark == null) {
                return false;
            }

            var matcher = CorporeaHelper.instance().createMatcher(requested, true);
            var result = CorporeaHelper.instance().requestItem(
                matcher, requested.getCount(), spark, player, false);

            if (result.extractedCount() >= requested.getCount()) {
                for (ItemStack stack : result.stacks()) {
                    if (!player.getInventory().add(stack)) {
                        player.drop(stack, false);
                    }
                }
                return true;
            }

            return false;
        } catch (Exception e) {
            IronsBotany.LOGGER.warn("Corporea request failed: {}", e.getMessage());
            return false;
        }
    }
}
