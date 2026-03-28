package com.ironsbotany.common.spell.catalyst;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.spell.catalyst.impl.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Handles registration of all catalyst effects.
 * Resolves Botania items via the Forge item registry for stability across versions.
 */
public class CatalystRegistration {

    /**
     * Register all catalyst effects.
     * Called during mod initialization.
     */
    public static void registerCatalysts() {
        // Elemental Runes
        registerCatalystSafe("botania:rune_fire", new RuneOfFireCatalyst());
        registerCatalystSafe("botania:rune_water", new RuneOfWaterCatalyst());
        registerCatalystSafe("botania:rune_earth", new RuneOfEarthCatalyst());
        registerCatalystSafe("botania:rune_air", new RuneOfAirCatalyst());
        registerCatalystSafe("botania:rune_mana", new RuneOfManaCatalyst());

        // Lenses
        registerCatalystSafe("botania:lens_speed", new LensVelocityCatalyst());
        registerCatalystSafe("botania:lens_mine", new LensBoreCatalyst());

        // Materials
        registerCatalystSafe("botania:terrasteel_ingot", new TerrasteelCatalyst());
        registerCatalystSafe("botania:gaia_ingot", new GaiaSpiritCatalyst());

        IronsBotany.LOGGER.info("Registered {} catalyst effects",
            SpellCatalystRegistry.getAllEffects().size());
    }

    /**
     * Resolve a Botania item by registry name and register its catalyst effect.
     */
    private static void registerCatalystSafe(String registryName, CatalystEffect catalyst) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(registryName));
        if (item != null && item != net.minecraft.world.item.Items.AIR) {
            SpellCatalystRegistry.registerCatalyst(item, catalyst);
            IronsBotany.LOGGER.info("Registered catalyst for {}", registryName);
        } else {
            IronsBotany.LOGGER.warn("Could not find Botania item for catalyst: {}", registryName);
        }
    }
}
