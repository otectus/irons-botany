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
     * Get appropriate rune for spell school via Forge registry
     */
    private static ItemStack getRuneForSpell(AbstractSpell spell) {
        String registryName;
        var school = spell.getSchoolType();

        if (school == SchoolRegistry.FIRE.get()) {
            registryName = "botania:rune_fire";
        } else if (school == SchoolRegistry.ICE.get()) {
            registryName = "botania:rune_water";
        } else if (school == SchoolRegistry.LIGHTNING.get()) {
            registryName = "botania:rune_air";
        } else if (school == SchoolRegistry.NATURE.get()) {
            registryName = "botania:rune_earth";
        } else {
            registryName = "botania:rune_mana";
        }

        return getBotaniaItem(registryName);
    }

    /**
     * Get Gaia component for ultimate spells
     */
    private static ItemStack getGaiaComponent(AbstractSpell spell) {
        return getBotaniaItem("botania:gaia_ingot");
    }

    /**
     * Resolve a Botania item by registry name (e.g. "botania:rune_fire").
     */
    static ItemStack getBotaniaItem(String registryName) {
        ResourceLocation rl = ResourceLocation.tryParse(registryName);
        if (rl != null) {
            Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(rl);
            if (item != null && item != net.minecraft.world.item.Items.AIR) {
                return new ItemStack(item);
            }
        }
        IronsBotany.LOGGER.debug("Failed to resolve Botania item '{}'", registryName);
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
     * Request an item from Corporea network and add to player inventory.
     */
    static boolean requestFromCorporea(Player player, BlockPos sparkPos, ItemStack requested) {
        List<ItemStack> extracted = extractFromCorporea(player, sparkPos, requested);
        if (extracted == null) {
            return false;
        }
        for (ItemStack stack : extracted) {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        return true;
    }

    /**
     * Extract items from Corporea network without adding to inventory.
     * Returns the extracted stacks, or null if the request could not be fulfilled.
     * Checks player inventory first — if the player already has a matching item, returns empty list (success).
     */
    static List<ItemStack> extractFromCorporea(Player player, BlockPos sparkPos, ItemStack requested) {
        if (requested.isEmpty()) {
            return List.of();
        }

        try {
            Level level = player.level();

            // Check if player already has the item
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && ItemStack.isSameItemSameTags(stack, requested)
                        && stack.getCount() >= requested.getCount()) {
                    return List.of(); // Already has enough
                }
            }

            // Try to request from Corporea network
            if (!CorporeaHelper.instance().doesBlockHaveSpark(level, sparkPos)) {
                return null;
            }

            var spark = CorporeaHelper.instance().getSparkForBlock(level, sparkPos);
            if (spark == null) {
                return null;
            }

            var matcher = CorporeaHelper.instance().createMatcher(requested, true);
            var result = CorporeaHelper.instance().requestItem(
                matcher, requested.getCount(), spark, player, false);

            if (result.extractedCount() >= requested.getCount()) {
                return new ArrayList<>(result.stacks());
            }

            return null;
        } catch (Exception e) {
            IronsBotany.LOGGER.warn("Corporea request failed: {}", e.getMessage());
            return null;
        }
    }
}
