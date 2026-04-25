package com.ironsbotany.datagen;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.registry.IBItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

/**
 * Vanilla shaped/shapeless recipes for Iron's Botany items.
 *
 * <p>Botania custom-recipe types (petal_apothecary, runic_altar,
 * terra_plate, elven_trade) stay hand-written under
 * {@code src/main/resources/data/ironsbotany/recipes/} — Botania's
 * recipe builders aren't part of its public API, and a thin JSON helper
 * here would just duplicate the existing hand-written content without
 * type-checking benefits. As Phase 6 adds new staff/spellbook crafting,
 * those vanilla shaped recipes land in this provider.
 */
public class IBRecipeProvider extends RecipeProvider {

    public IBRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {
        // Botanical Grimoire — single-slot guidebook, crafted from a vanilla book + Mana Pearl
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, IBItems.BOTANICAL_GRIMOIRE.get())
                .requires(Items.BOOK)
                .requires(IBItems.SPELL_PETAL.get())
                .unlockedBy("has_spell_petal", has(IBItems.SPELL_PETAL.get()))
                .save(writer, modLoc("botanical_grimoire"));

        // Botanical Crystal — assembled from spell petals + a diamond core
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, IBItems.BOTANICAL_CRYSTAL.get())
                .pattern(" P ")
                .pattern("PDP")
                .pattern(" P ")
                .define('P', IBItems.SPELL_PETAL.get())
                .define('D', Items.DIAMOND)
                .unlockedBy("has_spell_petal", has(IBItems.SPELL_PETAL.get()))
                .save(writer, modLoc("botanical_crystal"));

        // Mana-Infused Essence — distilled from petals + glowstone
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, IBItems.MANA_INFUSED_ESSENCE.get(), 2)
                .requires(IBItems.SPELL_PETAL.get())
                .requires(IBItems.SPELL_PETAL.get())
                .requires(Items.GLOWSTONE_DUST)
                .unlockedBy("has_spell_petal", has(IBItems.SPELL_PETAL.get()))
                .save(writer, modLoc("mana_infused_essence"));
    }

    private static ResourceLocation modLoc(String path) {
        return new ResourceLocation(IronsBotany.MODID, path);
    }
}
