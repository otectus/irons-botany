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

import java.lang.reflect.Method;

public class ManaHelper {

    // Cached reflection for Botania mana pool API
    private static boolean poolReflectionInitialized = false;
    private static boolean poolReflectionAvailable = false;
    private static Class<?> manaPoolClass;
    private static Method getCurrentManaMethod;
    private static Method receiveManaMethod;

    private static void initPoolReflection() {
        if (poolReflectionInitialized) return;
        poolReflectionInitialized = true;
        try {
            manaPoolClass = Class.forName("vazkii.botania.api.mana.ManaPool");
            getCurrentManaMethod = manaPoolClass.getMethod("getCurrentMana");
            receiveManaMethod = manaPoolClass.getMethod("receiveMana", int.class);
            poolReflectionAvailable = true;
        } catch (Exception e) {
            poolReflectionAvailable = false;
            IronsBotany.LOGGER.debug("Botania mana pool API not available for direct access: {}", e.getMessage());
        }
    }
    
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
        int botaniaNeeded = transferRate;
        
        // Try to extract Botania mana from the item
        boolean extracted = ManaItemHandler.instance().requestManaExact(stack, player, botaniaNeeded, true);
        
        if (extracted) {
            int issToAdd = convertBotaniaToISS(botaniaNeeded);
            magicData.addMana(issToAdd);
            return true;
        }
        
        return false;
    }

    /**
     * Checks if a player has enough Botania mana in their inventory/curios
     * @param player The player to check
     * @param amount Amount of Botania mana needed
     * @return true if player has enough mana
     */
    public static boolean hasBotaniaMana(Player player, int amount) {
        // Check all items in player's inventory for mana
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ManaItemHandler.instance().requestManaExact(stack, player, amount, false)) {
                return true;
            }
        }
        // Fallback: check nearby mana pools
        if (CommonConfig.ENABLE_MANA_POOL_ACCESS.get()) {
            return hasBotaniaManaFromPools(player, amount);
        }
        return false;
    }

    /**
     * Drains Botania mana from a player's items
     * @param player The player
     * @param amount Amount to drain
     * @return true if successfully drained
     */
    public static boolean drainBotaniaMana(Player player, int amount) {
        // Try to drain from each item in inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (ManaItemHandler.instance().requestManaExact(stack, player, amount, true)) {
                return true;
            }
        }
        // Fallback: drain from nearby mana pools
        if (CommonConfig.ENABLE_MANA_POOL_ACCESS.get()) {
            return drainBotaniaManaFromPools(player, amount);
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
        initPoolReflection();
        if (!poolReflectionAvailable) return false;

        Level level = player.level();
        int radius = CommonConfig.MANA_POOL_SEARCH_RADIUS.get();
        BlockPos playerPos = player.blockPosition();

        for (BlockPos checkPos : BlockPos.betweenClosed(
                playerPos.offset(-radius, -radius / 2, -radius),
                playerPos.offset(radius, radius / 2, radius))) {
            BlockEntity blockEntity = level.getBlockEntity(checkPos);
            if (blockEntity != null && manaPoolClass.isInstance(blockEntity)) {
                try {
                    int currentMana = (int) getCurrentManaMethod.invoke(blockEntity);
                    if (currentMana >= amount) {
                        if (doDrain) {
                            receiveManaMethod.invoke(blockEntity, -amount);
                        }
                        return true;
                    }
                } catch (Exception e) {
                    IronsBotany.LOGGER.debug("Failed to access Botania mana pool: {}", e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }
}
