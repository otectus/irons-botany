package com.ironsbotany.common.recipe;

import com.google.gson.JsonObject;
import com.ironsbotany.common.registry.IBRecipeTypes;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import vazkii.botania.api.recipe.ElvenTradeRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * An Alfheim Portal trade that promotes an Iron's Spells scroll to the Elementium Scroll
 * <em>while carrying its bound spell across</em>.
 *
 * <p>Botania's stock {@code botania:elven_trade} serializer builds its outputs from fixed stacks
 * written into the recipe JSON and never looks at what was actually thrown in, so the ordinary
 * recipe consumed a spell-carrying scroll and returned a blank Elementium Scroll — an item with no
 * spell and no way to ever get one, because the Scroll Forge outputs Iron's Spells' scroll and not
 * this one. The Botanical Grimoire has always described the intended behaviour ("the trade preserves
 * the bound spell while promoting the scroll to its reusable form"); this makes it true.
 *
 * <p>The mechanism is part of Botania's public recipe API rather than a workaround:
 * {@link ElvenTradeRecipe#getOutputs(List)} is handed the matched input stacks precisely so a recipe
 * can derive its output from them. Botania's own {@code LexiconElvenTradeRecipe} does the same thing
 * to stamp an NBT flag onto the lexicon it was given.
 *
 * <p>Ingredients stay data-driven: the JSON keeps the same {@code ingredients} and {@code output}
 * shape the stock serializer uses, and matching follows Botania's algorithm exactly — each input
 * satisfies at most one ingredient, unmatched extras in the portal are ignored, and the returned
 * list holds the caller's own {@code ItemStack} instances because the portal removes them by
 * identity before asking for the outputs.
 */
public class ElementiumScrollTradeRecipe implements ElvenTradeRecipe {

    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputs;
    private final ItemStack output;

    public ElementiumScrollTradeRecipe(ResourceLocation id, ItemStack output, Ingredient... inputs) {
        this.id = id;
        this.output = output;
        this.inputs = NonNullList.create();
        Collections.addAll(this.inputs, inputs);
    }

    @Override
    public Optional<List<ItemStack>> match(List<ItemStack> stacks) {
        List<Ingredient> remaining = new ArrayList<>(inputs);
        List<ItemStack> used = new ArrayList<>();

        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            if (remaining.isEmpty()) {
                break;
            }
            for (int i = 0; i < remaining.size(); i++) {
                if (remaining.get(i).test(stack)) {
                    if (!used.contains(stack)) {
                        used.add(stack);
                    }
                    remaining.remove(i);
                    break;
                }
            }
        }

        return remaining.isEmpty() ? Optional.of(used) : Optional.empty();
    }

    @Override
    public boolean containsItem(ItemStack stack) {
        for (Ingredient ingredient : inputs) {
            if (ingredient.test(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ItemStack> getOutputs() {
        return List.of(output.copy());
    }

    @Override
    public List<ItemStack> getOutputs(List<ItemStack> inputs) {
        ItemStack result = output.copy();
        SpellData bound = boundSpell(inputs);
        if (bound != null) {
            ISpellContainer.createScrollContainer(bound.getSpell(), bound.getLevel(), result);
        }
        return List.of(result);
    }

    /**
     * The spell carried by whichever consumed stack was a scroll, or {@code null} if none was.
     *
     * <p>A null result is not an error path worth failing on: the ingredients decide what the portal
     * accepts, so a recipe configured without a spell-bearing input should still produce its output
     * rather than swallow the materials.
     */
    private static SpellData boundSpell(List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (stack.isEmpty() || !ISpellContainer.isSpellContainer(stack)) {
                continue;
            }
            ISpellContainer container = ISpellContainer.get(stack);
            if (container.isEmpty()) {
                continue;
            }
            SpellData data = container.getSpellAtIndex(0);
            if (data != null && data != SpellData.EMPTY && data.getSpell() != null) {
                return data;
            }
        }
        return null;
    }

    /**
     * Botania's API interface re-declares {@code Recipe#getIngredients()} as abstract, and the
     * production jar we compile against carries it under its obfuscated name. Vanilla's own
     * declaration is a default, so this is the one member that has to be spelled the production way
     * to satisfy the compiler; {@code reobfJar} leaves it untouched because it is already the name
     * the shipped jar uses. Everything else on this class keeps its readable name.
     */
    @Override
    public NonNullList<Ingredient> m_7527_() {
        return inputs;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return IBRecipeTypes.ELEMENTIUM_SCROLL_TRADE_SERIALIZER.get();
    }

    // ---------------------------------------------------------------------------------------------
    // Recipe<Container> plumbing. Botania's interface already defaults all of these, but its
    // defaults are compiled against the obfuscated names, so from this side vanilla's declarations
    // still read as unimplemented. These restate the interface's behaviour verbatim: an elven trade
    // is resolved by the portal through match/getOutputs and never through the crafting-grid path.
    // ---------------------------------------------------------------------------------------------

    @Override
    public RecipeType<?> getType() {
        return BuiltInRegistries.RECIPE_TYPE.get(ElvenTradeRecipe.TYPE_ID);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<ElementiumScrollTradeRecipe> {

        @Override
        public ElementiumScrollTradeRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            var array = GsonHelper.getAsJsonArray(json, "ingredients");
            Ingredient[] ingredients = new Ingredient[array.size()];
            for (int i = 0; i < array.size(); i++) {
                ingredients[i] = Ingredient.fromJson(array.get(i));
            }
            return new ElementiumScrollTradeRecipe(recipeId, output, ingredients);
        }

        @Override
        public ElementiumScrollTradeRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            ItemStack output = buffer.readItem();
            Ingredient[] ingredients = new Ingredient[buffer.readVarInt()];
            for (int i = 0; i < ingredients.length; i++) {
                ingredients[i] = Ingredient.fromNetwork(buffer);
            }
            return new ElementiumScrollTradeRecipe(recipeId, output, ingredients);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ElementiumScrollTradeRecipe recipe) {
            buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.inputs.size());
            for (Ingredient ingredient : recipe.inputs) {
                ingredient.toNetwork(buffer);
            }
        }
    }
}
