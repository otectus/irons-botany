package com.ironsbotany.common.flower;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.flower.auras.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Handles registration of flower auras.
 * Uses soft dependency pattern to safely integrate with Botania.
 */
public class FlowerAuraRegistration {
    
    /**
     * Register all flower auras.
     * Called during mod initialization.
     */
    public static void registerFlowerAuras() {
        if (!ModList.get().isLoaded("botania")) {
            IronsBotany.LOGGER.info("Botania not detected, skipping flower aura registration");
            return;
        }
        
        try {
            // Register Botania functional flower auras
            registerAuraSafe("botania:bellethorn", new BellethorneAura());
            registerAuraSafe("botania:jaded_amaranthus", new JadedAmaranthusAura());
            registerAuraSafe("botania:heisei_dream", new HeiseiDreamAura());
            registerAuraSafe("botania:rannuncarpus", new RannuncarpusAura());
            
            IronsBotany.LOGGER.info("Successfully registered {} flower auras", 
                FlowerAuraRegistry.getAllAuras().size());
        } catch (Exception e) {
            IronsBotany.LOGGER.error("Failed to register flower auras", e);
        }
    }
    
    /**
     * Safely register a flower aura using registry lookup
     */
    private static void registerAuraSafe(String blockId, FlowerAura aura) {
        try {
            Block block = ForgeRegistries.BLOCKS.getValue(
                net.minecraft.resources.ResourceLocation.tryParse(blockId)
            );
            
            if (block != null) {
                FlowerAuraRegistry.registerFlowerAura(block, aura);
                IronsBotany.LOGGER.debug("Registered aura for: {}", blockId);
            } else {
                IronsBotany.LOGGER.warn("Could not find block: {}", blockId);
            }
        } catch (Exception e) {
            IronsBotany.LOGGER.debug("Could not register aura for {}: {}", blockId, e.getMessage());
        }
    }
}
