package com.ironsbotany.common.spell.catalyst;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ironsbotany.IronsBotany;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

/**
 * Datapack reload listener that loads {@link TemplatedCatalystEffect}s from
 * {@code data/ironsbotany/catalysts/*.json} and registers each one against a
 * Botania (or any) item identified by the JSON's {@code item} or
 * {@code item_tag} field.
 *
 * <p>Schema:</p>
 * <pre>{@code
 * {
 *   "item": "botania:rune_air",
 *   "tier": "ADVANCED",
 *   "modifiers": { "damage": 1.1, "cast_speed": 1.1 },
 *   "applies_to_schools": ["irons_spellbooks:nature"]
 * }
 * }</pre>
 *
 * <p>Use {@code "item_tag"} instead of {@code "item"} to bind to every item
 * carrying a particular tag — e.g. {@code "botania:runes"} — so a single
 * datapack entry can cover an entire rune family.</p>
 */
public class CatalystDataLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final String DIRECTORY = "catalysts";

    public CatalystDataLoader() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        int added = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : entries.entrySet()) {
            ResourceLocation id = entry.getKey();
            try {
                JsonObject root = entry.getValue().getAsJsonObject();
                TemplatedCatalystEffect catalyst = TemplatedCatalystEffect.fromJson(id, root);

                if (root.has("item")) {
                    ResourceLocation itemId = ResourceLocation.tryParse(root.get("item").getAsString());
                    if (itemId == null) {
                        IronsBotany.LOGGER.warn("Catalyst {} has invalid item id; skipping", id);
                        continue;
                    }
                    Item item = ForgeRegistries.ITEMS.getValue(itemId);
                    if (item != null && item != net.minecraft.world.item.Items.AIR) {
                        SpellCatalystRegistry.registerCatalyst(item, catalyst);
                        added++;
                    } else {
                        IronsBotany.LOGGER.debug("Catalyst {} item {} not present; skipped", id, itemId);
                    }
                } else if (root.has("item_tag")) {
                    ResourceLocation tagId = ResourceLocation.tryParse(root.get("item_tag").getAsString());
                    if (tagId == null) continue;
                    TagKey<Item> tag = ItemTags.create(tagId);
                    // Iterate items currently in the tag and register each.
                    ForgeRegistries.ITEMS.tags().getTag(tag).forEach(item -> {
                        SpellCatalystRegistry.registerCatalyst(item, catalyst);
                    });
                    added++;
                }
            } catch (Exception ex) {
                IronsBotany.LOGGER.warn("Failed to load catalyst {}: {}", id, ex.getMessage());
            }
        }
        IronsBotany.LOGGER.info("Loaded {} templated catalysts from datapack", added);
    }

    @Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class Events {
        @SubscribeEvent
        public static void onReload(AddReloadListenerEvent event) {
            event.addListener(new CatalystDataLoader());
        }
    }
}
