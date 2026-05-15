package com.ironsbotany.common.recipe;

import com.google.gson.JsonObject;
import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.registry.IBRecipeTypes;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Crafts an Iron's Spells 'n Spellbooks scroll bound to a specific Iron's
 * Botany spell. Replaces the broken {@code irons_spellbooks:scroll_forge}
 * recipes from earlier releases that referenced a non-existent recipe type.
 *
 * <p>Shapeless: requires exactly one of each configured ingredient (typically
 * {@code irons_spellbooks:common_ink} + a focus token like
 * {@code ironsbotany:spell_petal}). On assemble, builds a fresh
 * {@code irons_spellbooks:scroll} ItemStack and writes an ISS
 * {@link ISpellContainer} containing the named spell at level 1.</p>
 *
 * <p>JSON schema (under {@code data/ironsbotany/recipes/}):</p>
 * <pre>{@code
 * {
 *   "type": "ironsbotany:ib_spell_scroll",
 *   "ink": { "item": "irons_spellbooks:common_ink" },
 *   "focus": { "item": "ironsbotany:spell_petal" },
 *   "spell": "ironsbotany:mana_bloom",
 *   "level": 1
 * }
 * }</pre>
 */
public class IBSpellScrollRecipe extends CustomRecipe {

    private final Ingredient ink;
    private final Ingredient focus;
    private final ResourceLocation spellId;
    private final int level;

    public IBSpellScrollRecipe(ResourceLocation id, CraftingBookCategory category,
                               Ingredient ink, Ingredient focus,
                               ResourceLocation spellId, int level) {
        super(id, category);
        this.ink = ink;
        this.focus = focus;
        this.spellId = spellId;
        this.level = level;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        boolean haveInk = false;
        boolean haveFocus = false;
        int count = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (stack.isEmpty()) continue;
            count++;
            if (!haveInk && ink.test(stack)) { haveInk = true; continue; }
            if (!haveFocus && focus.test(stack)) { haveFocus = true; continue; }
            return false;
        }
        return haveInk && haveFocus && count == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
        // Resolve the scroll base item. ISS registers it as irons_spellbooks:scroll.
        var scrollItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("irons_spellbooks", "scroll"));
        if (scrollItem == null) {
            IronsBotany.LOGGER.warn("IB scroll recipe {}: irons_spellbooks:scroll not registered", getId());
            return ItemStack.EMPTY;
        }
        AbstractSpell spell = SpellRegistry.getSpell(spellId);
        if (spell == null) {
            IronsBotany.LOGGER.warn("IB scroll recipe {}: unknown spell {}", getId(), spellId);
            return ItemStack.EMPTY;
        }
        ItemStack result = new ItemStack(scrollItem);
        ISpellContainer.createScrollContainer(spell, this.level, result);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return IBRecipeTypes.IB_SPELL_SCROLL_SERIALIZER.get();
    }

    public Ingredient getInk() { return ink; }
    public Ingredient getFocus() { return focus; }
    public ResourceLocation getSpellId() { return spellId; }
    public int getLevel() { return level; }

    public static class Serializer implements RecipeSerializer<IBSpellScrollRecipe> {

        @Override
        public IBSpellScrollRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            CraftingBookCategory category = CraftingBookCategory.CODEC.byName(
                GsonHelper.getAsString(json, "category", CraftingBookCategory.MISC.getSerializedName()),
                CraftingBookCategory.MISC);
            Ingredient ink = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "ink"));
            Ingredient focus = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "focus"));
            ResourceLocation spell = new ResourceLocation(GsonHelper.getAsString(json, "spell"));
            int level = GsonHelper.getAsInt(json, "level", 1);
            return new IBSpellScrollRecipe(recipeId, category, ink, focus, spell, level);
        }

        @Override
        public IBSpellScrollRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            Ingredient ink = Ingredient.fromNetwork(buffer);
            Ingredient focus = Ingredient.fromNetwork(buffer);
            ResourceLocation spell = buffer.readResourceLocation();
            int level = buffer.readVarInt();
            return new IBSpellScrollRecipe(recipeId, category, ink, focus, spell, level);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, IBSpellScrollRecipe recipe) {
            buffer.writeEnum(recipe.category());
            recipe.ink.toNetwork(buffer);
            recipe.focus.toNetwork(buffer);
            buffer.writeResourceLocation(recipe.spellId);
            buffer.writeVarInt(recipe.level);
        }
    }
}
