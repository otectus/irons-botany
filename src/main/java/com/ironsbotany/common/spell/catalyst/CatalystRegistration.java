package com.ironsbotany.common.spell.catalyst;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.spell.catalyst.impl.*;
import net.minecraftforge.fml.ModList;

/**
 * Handles registration of all catalyst effects.
 * Uses soft dependency pattern to safely integrate with Botania.
 */
public class CatalystRegistration {
    
    /**
     * Register all catalyst effects.
     * Called during mod initialization.
     */
    public static void registerCatalysts() {
        if (!ModList.get().isLoaded("botania")) {
            IronsBotany.LOGGER.info("Botania not detected, skipping catalyst registration");
            return;
        }
        
        try {
            registerRuneCatalysts();
            registerLensCatalysts();
            registerMaterialCatalysts();
            
            IronsBotany.LOGGER.info("Successfully registered {} catalyst effects", 
                SpellCatalystRegistry.getAllEffects().size());
        } catch (Exception e) {
            IronsBotany.LOGGER.error("Failed to register catalysts", e);
        }
    }
    
    /**
     * Register rune-based catalysts
     */
    private static void registerRuneCatalysts() {
        try {
            // Get Botania items via reflection to avoid hard dependency
            Class<?> botaniaItems = Class.forName("vazkii.botania.common.item.BotaniaItems");
            
            // Elemental Runes
            registerCatalystSafe(botaniaItems, "runeFire", new RuneOfFireCatalyst());
            registerCatalystSafe(botaniaItems, "runeWater", new RuneOfWaterCatalyst());
            registerCatalystSafe(botaniaItems, "runeEarth", new RuneOfEarthCatalyst());
            registerCatalystSafe(botaniaItems, "runeAir", new RuneOfAirCatalyst());
            
            // Mana Rune
            registerCatalystSafe(botaniaItems, "runeMana", new RuneOfManaCatalyst());
            
            // Note: Additional rune catalysts (seasons, sins) can be added as needed
            
        } catch (ClassNotFoundException e) {
            IronsBotany.LOGGER.warn("Could not find Botania items class for rune catalysts");
        }
    }
    
    /**
     * Register lens-based catalysts
     */
    private static void registerLensCatalysts() {
        try {
            Class<?> botaniaItems = Class.forName("vazkii.botania.common.item.BotaniaItems");
            
            // Velocity Lens
            registerCatalystSafe(botaniaItems, "lensVelocity", new LensVelocityCatalyst());
            
            // Bore Lens
            registerCatalystSafe(botaniaItems, "lensBore", new LensBoreCatalyst());
            
            // Note: Additional lens catalysts (magnetizing, phantom, influence) can be added
            
        } catch (ClassNotFoundException e) {
            IronsBotany.LOGGER.warn("Could not find Botania items class for lens catalysts");
        }
    }
    
    /**
     * Register material-based catalysts
     */
    private static void registerMaterialCatalysts() {
        try {
            Class<?> botaniaItems = Class.forName("vazkii.botania.common.item.BotaniaItems");
            
            // Terrasteel
            registerCatalystSafe(botaniaItems, "terrasteel", new TerrasteelCatalyst());
            
            // Gaia Spirit
            registerCatalystSafe(botaniaItems, "gaiaSpirit", new GaiaSpiritCatalyst());
            
            // Note: Additional material catalysts (elementium, dragonstone) can be added
            
        } catch (ClassNotFoundException e) {
            IronsBotany.LOGGER.warn("Could not find Botania items class for material catalysts");
        }
    }
    
    /**
     * Safely register a catalyst using reflection
     */
    private static void registerCatalystSafe(Class<?> botaniaItems, String fieldName, CatalystEffect catalyst) {
        try {
            Object itemSupplier = botaniaItems.getField(fieldName).get(null);
            
            // Get the actual item from the supplier
            if (itemSupplier instanceof java.util.function.Supplier<?>) {
                Object item = ((java.util.function.Supplier<?>) itemSupplier).get();
                if (item instanceof net.minecraft.world.item.Item) {
                    SpellCatalystRegistry.registerCatalyst(
                        (net.minecraft.world.item.Item) item, 
                        catalyst
                    );
                }
            }
        } catch (Exception e) {
            IronsBotany.LOGGER.debug("Could not register catalyst for {}: {}", fieldName, e.getMessage());
        }
    }
}
