package com.ironsbotany.common.util;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.ModList;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.api.mana.ManaCollector;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.ManaSpreader;

/**
 * Direct Botania API integration utility. Replaces reflection-based access
 * with compile-time-safe instanceof checks and method calls.
 */
public final class BotaniaIntegration {
    private BotaniaIntegration() {}

    public static boolean isBotaniaLoaded() {
        return ModList.get().isLoaded("botania");
    }

    public static boolean isManaPool(BlockEntity be) {
        return be instanceof ManaPool;
    }

    public static boolean isManaCollector(BlockEntity be) {
        return be instanceof ManaCollector;
    }

    public static boolean isManaSpreader(BlockEntity be) {
        return be instanceof ManaSpreader;
    }

    public static boolean isGeneratingFlower(BlockEntity be) {
        return be instanceof GeneratingFlowerBlockEntity;
    }

    public static boolean isFunctionalFlower(BlockEntity be) {
        return be instanceof FunctionalFlowerBlockEntity;
    }

    public static boolean isBotaniaBlockEntity(BlockEntity be) {
        return be instanceof ManaReceiver
            || be instanceof ManaSpreader
            || be instanceof GeneratingFlowerBlockEntity
            || be instanceof FunctionalFlowerBlockEntity;
    }

    public static int getPoolMana(BlockEntity be) {
        if (be instanceof ManaPool pool) return pool.getCurrentMana();
        return 0;
    }

    public static boolean poolHasMana(BlockEntity be, int amount) {
        if (be instanceof ManaPool pool) return pool.getCurrentMana() >= amount;
        return false;
    }

    public static void drainPoolMana(BlockEntity be, int amount) {
        if (be instanceof ManaPool pool) pool.receiveMana(-amount);
    }

    public static void addPoolMana(BlockEntity be, int amount) {
        if (be instanceof ManaPool pool) pool.receiveMana(amount);
    }
}
