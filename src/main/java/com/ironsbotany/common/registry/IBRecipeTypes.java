package com.ironsbotany.common.registry;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.recipe.ElementiumScrollTradeRecipe;
import com.ironsbotany.common.recipe.RuneScrollFusionRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class IBRecipeTypes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = 
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, IronsBotany.MODID);

    public static final RegistryObject<RecipeSerializer<RuneScrollFusionRecipe>> RUNE_SCROLL_FUSION_SERIALIZER = 
        RECIPE_SERIALIZERS.register("rune_scroll_fusion", RuneScrollFusionRecipe.Serializer::new);

    /**
     * Alfheim Portal trade that carries a scroll's bound spell onto the Elementium Scroll. Its
     * recipes declare Botania's elven-trade {@code RecipeType}, so the portal picks them up
     * alongside Botania's own; only the serializer is ours.
     */
    public static final RegistryObject<RecipeSerializer<ElementiumScrollTradeRecipe>> ELEMENTIUM_SCROLL_TRADE_SERIALIZER =
        RECIPE_SERIALIZERS.register("elementium_scroll_trade", ElementiumScrollTradeRecipe.Serializer::new);

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
