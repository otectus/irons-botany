package com.ironsbotany.common.util;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.config.ManaUnificationMode;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import vazkii.botania.api.mana.ManaItemHandler;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import vazkii.botania.api.mana.ManaPool;

public class ManaHelper {
    
    /**
     * Converts Botania mana to ISS mana based on configured ratio and mode
     * @param botaniaAmount Amount of Botania mana
     * @return Equivalent ISS mana
     */
    public static int convertBotaniaToISS(int botaniaAmount) {
        ManaUnificationMode mode = CommonConfig.MANA_UNIFICATION_MODE.get();
        
        // Check if conversion is allowed in current mode
        if (!mode.allowsConversion()) {
            return 0; // No conversion in SEPARATE or DISABLED modes
        }
        
        int ratio = CommonConfig.MANA_CONVERSION_RATIO.get();
        return botaniaAmount / ratio;
    }

    /**
     * Converts ISS mana to Botania mana based on configured ratio
     * @param issAmount Amount of ISS mana
     * @return Equivalent Botania mana
     */
    public static int convertISSToBotania(int issAmount) {
        ManaUnificationMode mode = CommonConfig.MANA_UNIFICATION_MODE.get();

        if (!mode.allowsConversion()) {
            return 0;
        }

        int ratio = CommonConfig.MANA_CONVERSION_RATIO.get();
        return issAmount * ratio;
    }

    /**
     * Attempts to convert Botania mana from an item to ISS mana for a player
     * @param player The player receiving ISS mana
     * @param stack The item stack containing Botania mana
     * @return true if conversion was successful
     */
    public static boolean tryConvertManaToISS(Player player, ItemStack stack) {
        if (player.level().isClientSide()) return false;

        MagicData magicData = MagicData.getPlayerMagicData(player);
        if (magicData == null) return false;

        float currentMana = magicData.getMana();
        float maxMana = (float) player.getAttributeValue(io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA.get());

        if (currentMana >= maxMana) return false; // Already at max

        int transferRate = CommonConfig.MANA_TRANSFER_RATE.get();

        // Check conversion yields something before draining
        int issToAdd = convertBotaniaToISS(transferRate);
        if (issToAdd <= 0) return false;

        // Clamp to available ISS room
        int issRoom = (int) (maxMana - currentMana);
        if (issToAdd > issRoom) {
            issToAdd = issRoom;
            // Only drain the exact Botania amount for the clamped ISS gain
            int ratio = CommonConfig.MANA_CONVERSION_RATIO.get();
            transferRate = issToAdd * ratio;
        }

        // Now drain — we know it converts cleanly and there's room
        boolean extracted = ManaItemHandler.instance().requestManaExact(stack, player, transferRate, true);
        if (extracted) {
            magicData.addMana(issToAdd);
            return true;
        }

        return false;
    }

    /**
     * Checks if a player has enough Botania mana across all sources
     * (inventory, Curios slots, accessories — same sources the mana HUD shows)
     */
    public static boolean hasBotaniaMana(Player player, int amount) {
        // Use ManaItemHandler to check all mana items + accessories (matches HUD)
        // requestManaExactForTool with simulate=false just checks availability
        if (requestManaFromAllSources(player, amount, false)) {
            return true;
        }
        // Fallback: check nearby mana pools
        if (CommonConfig.ENABLE_MANA_POOL_ACCESS.get()) {
            return hasBotaniaManaFromPools(player, amount);
        }
        return false;
    }

    /**
     * Drains Botania mana from a player's items and accessories
     * (uses the same aggregation as the mana HUD for consistency)
     */
    public static boolean drainBotaniaMana(Player player, int amount) {
        // Use ManaItemHandler to drain from all sources (matches HUD)
        if (requestManaFromAllSources(player, amount, true)) {
            return true;
        }
        // Fallback: drain from nearby mana pools
        if (CommonConfig.ENABLE_MANA_POOL_ACCESS.get()) {
            return drainBotaniaManaFromPools(player, amount);
        }
        return false;
    }

    /**
     * Request mana from all Botania mana sources (items + accessories).
     * Uses ManaItemHandler which aggregates inventory, Curios, and other mana providers.
     */
    private static boolean requestManaFromAllSources(Player player, int amount, boolean doExtract) {
        // Check mana items (main inventory)
        for (ItemStack stack : ManaItemHandler.instance().getManaItems(player)) {
            if (ManaItemHandler.instance().requestManaExact(stack, player, amount, doExtract)) {
                return true;
            }
        }
        // Check mana accessories (Curios, Baubles, etc.)
        for (ItemStack stack : ManaItemHandler.instance().getManaAccesories(player)) {
            if (ManaItemHandler.instance().requestManaExact(stack, player, amount, doExtract)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if nearby Botania mana pools have enough mana
     */
    public static boolean hasBotaniaManaFromPools(Player player, int amount) {
        return findAndDrainPool(player, amount, false);
    }

    /**
     * Drain Botania mana from nearby mana pools
     */
    public static boolean drainBotaniaManaFromPools(Player player, int amount) {
        return findAndDrainPool(player, amount, true);
    }

    private static boolean findAndDrainPool(Player player, int amount, boolean doDrain) {
        if (!BotaniaIntegration.isBotaniaLoaded()) return false;

        Level level = player.level();
        int radius = CommonConfig.MANA_POOL_SEARCH_RADIUS.get();
        BlockPos playerPos = player.blockPosition();

        for (BlockPos checkPos : BlockPos.betweenClosed(
                playerPos.offset(-radius, -radius / 2, -radius),
                playerPos.offset(radius, radius / 2, radius))) {
            BlockEntity blockEntity = level.getBlockEntity(checkPos);
            if (blockEntity instanceof ManaPool pool) {
                if (pool.getCurrentMana() >= amount) {
                    if (doDrain) {
                        pool.receiveMana(-amount);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
