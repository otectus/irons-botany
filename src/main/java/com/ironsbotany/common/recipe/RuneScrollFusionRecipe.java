package com.ironsbotany.common.recipe;

import com.google.gson.JsonObject;
import com.ironsbotany.common.registry.IBRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class RuneScrollFusionRecipe extends CustomRecipe {
    
    public RuneScrollFusionRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        ItemStack scroll = ItemStack.EMPTY;
        ItemStack rune = ItemStack.EMPTY;
        int itemCount = 0;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                itemCount++;
                
                // Check for scroll
                if (stack.getItem().toString().contains("scroll")) {
                    scroll = stack;
                }
                
                // Check for rune
                if (stack.getItem().toString().contains("rune")) {
                    rune = stack;
                }
            }
        }

        // Must have exactly 1 scroll and 1 rune
        return itemCount == 2 && !scroll.isEmpty() && !rune.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        ItemStack scroll = ItemStack.EMPTY;
        ItemStack rune = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem().toString().contains("scroll")) {
                    scroll = stack;
                }
                if (stack.getItem().toString().contains("rune")) {
                    rune = stack;
                }
            }
        }

        if (scroll.isEmpty() || rune.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // Create enhanced scroll
        ItemStack result = scroll.copy();
        CompoundTag tag = result.getOrCreateTag();
        
        // Store rune type
        String runeName = rune.getItem().toString();
        tag.putString("runeType", runeName);
        tag.putBoolean("runeEnhanced", true);
        
        // Add visual indicator
        result.setHoverName(scroll.getHoverName().copy().append(" (Rune Enhanced)"));

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return IBRecipeTypes.RUNE_SCROLL_FUSION_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<RuneScrollFusionRecipe> {
        
        @Override
        public RuneScrollFusionRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                json.get("category").getAsString(), CraftingBookCategory.MISC);
            return new RuneScrollFusionRecipe(recipeId, category);
        }

        @Override
        public RuneScrollFusionRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            return new RuneScrollFusionRecipe(recipeId, category);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, RuneScrollFusionRecipe recipe) {
            buffer.writeEnum(recipe.category());
        }
    }
}
