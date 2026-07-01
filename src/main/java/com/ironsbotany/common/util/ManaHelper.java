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
        }

        // Only drain the exact Botania that maps to the ISS actually granted. Integer
        // division in convertBotaniaToISS floors the ISS gain, so draining the raw
        // transferRate would over-charge Botania (e.g. drain 100 for 3 ISS at ratio 30).
        int ratio = CommonConfig.MANA_CONVERSION_RATIO.get();
        transferRate = issToAdd * ratio;

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
     * (inventory, Curios slots, accessories — same sources the mana HUD shows),
     * then nearby pools as a fallback.
     */
    public static boolean hasBotaniaMana(Player player, int amount) {
        if (amount <= 0) return true;
        // Aggregate across all carried/equipped mana items + accessories (matches HUD).
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
     * (uses the same aggregation as the mana HUD for consistency),
     * then nearby pools as a fallback.
     */
    public static boolean drainBotaniaMana(Player player, int amount) {
        if (amount <= 0) return true;
        // Aggregate-drain across all carried/equipped mana sources (matches HUD).
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
     * Request mana from all Botania mana sources the player carries or has equipped.
     *
     * <p>Unlike a per-item {@code requestManaExact} (which is all-or-nothing against a
     * <em>single</em> item), this <strong>aggregates</strong> across every mana item and
     * accessory — Mana Tablet, Mana Ring, Greater Band of Mana, Mana Mirror (remote, via
     * its bound pool), and our own {@link com.ironsbotany.common.item.cap.ItemManaStorage}
     * items — so a cost that no single item fully covers can still be paid from several.
     *
     * <p>Two passes when extracting: a non-destructive availability sweep first, so we never
     * partially drain when the player can't actually afford the full {@code amount} (which would
     * let the cast proceed for free or leave items short-changed). Only when the full amount is
     * confirmed available do we commit the drain.
     *
     * @param doExtract {@code true} to actually remove mana; {@code false} to only test availability
     * @return {@code true} iff the full {@code amount} is available (and, when {@code doExtract},
     *         was drained)
     */
    private static boolean requestManaFromAllSources(Player player, int amount, boolean doExtract) {
        if (amount <= 0) return true;
        if (!CommonConfig.ENABLE_INVENTORY_MANA_SOURCES.get()) return false;

        java.util.List<ItemStack> sources = collectManaSources(player);
        if (sources.isEmpty()) return false;

        // Pass 1: availability sweep (never removes mana).
        long available = 0L;
        for (ItemStack stack : sources) {
            // requestMana returns the partial amount that could be supplied (capped at request).
            available += ManaItemHandler.instance().requestMana(stack, player, amount, false);
            if (available >= amount) break;
        }
        if (available < amount) return false;
        if (!doExtract) return true;

        // Pass 2: commit the drain across sources until the cost is fully paid.
        int remaining = amount;
        for (ItemStack stack : sources) {
            if (remaining <= 0) break;
            int drained = ManaItemHandler.instance().requestMana(stack, player, remaining, true);
            remaining -= drained;
        }
        // Defensive: with the availability sweep above this should always be fully paid.
        return remaining <= 0;
    }

    /**
     * Gather the player's mana items and accessories into a single ordered list.
     * Inventory items first, then accessories (Curios/Baubles). Honors the Mana Mirror
     * config toggle.
     *
     * <p>Note: "getManaAccesories" is the Botania API's spelling (missing an 's'), not a typo.
     */
    private static java.util.List<ItemStack> collectManaSources(Player player) {
        java.util.List<ItemStack> sources = new java.util.ArrayList<>();
        boolean allowMirror = CommonConfig.ENABLE_MANA_MIRROR_SUPPORT.get();
        for (ItemStack stack : ManaItemHandler.instance().getManaItems(player)) {
            if (!stack.isEmpty() && (allowMirror || !isManaMirror(stack))) {
                sources.add(stack);
            }
        }
        for (ItemStack stack : ManaItemHandler.instance().getManaAccesories(player)) {
            if (!stack.isEmpty() && (allowMirror || !isManaMirror(stack))) {
                sources.add(stack);
            }
        }
        return sources;
    }

    private static boolean isManaMirror(ItemStack stack) {
        net.minecraft.resources.ResourceLocation id =
                net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && "botania".equals(id.getNamespace()) && "mana_mirror".equals(id.getPath());
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
