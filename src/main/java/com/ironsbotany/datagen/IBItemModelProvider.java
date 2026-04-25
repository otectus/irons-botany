package com.ironsbotany.datagen;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.registry.IBItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

/**
 * Generates flat {@code minecraft:item/generated} models for every
 * registered item, with {@code layer0} pointing at
 * {@code ironsbotany:item/<name>}. Block-form items get a separate
 * provider (parent → block model) in {@link IBBlockStateProvider}.
 */
public class IBItemModelProvider extends ItemModelProvider {

    public IBItemModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, IronsBotany.MODID, existingFiles);
    }

    @Override
    protected void registerModels() {
        // All non-block items use the standard generated parent
        for (RegistryObject<Item> entry : IBItems.ITEMS.getEntries()) {
            String path = entry.getId().getPath();
            // Skip block-items — those are handled in IBBlockStateProvider
            if (isBlockItem(path)) continue;
            simpleItem(entry.getId(), path);
        }
    }

    private boolean isBlockItem(String path) {
        return path.equals("spell_reservoir")
                || path.equals("mana_conduit")
                || path.equals("arcane_mana_altar");
    }

    private void simpleItem(ResourceLocation id, String name) {
        singleTexture(id.getPath(),
                new ResourceLocation("item/generated"),
                "layer0",
                new ResourceLocation(IronsBotany.MODID, "item/" + name));
    }
}
