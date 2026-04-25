package com.ironsbotany.datagen;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.registry.IBBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Generates blockstate JSONs and the parent-link item models for every
 * Iron's Botany block. The actual block models are kept hand-written
 * (the existing JSONs have custom multi-element geometry that codegen
 * can't replicate cleanly), so this provider points at the existing
 * model files via {@link #models()#getExistingFile}.
 */
public class IBBlockStateProvider extends BlockStateProvider {

    public IBBlockStateProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, IronsBotany.MODID, existingFiles);
    }

    @Override
    protected void registerStatesAndModels() {
        existingBlock("spell_reservoir");
        existingBlock("mana_conduit");
        existingBlock("arcane_mana_altar");
    }

    private void existingBlock(String name) {
        var blockModel = models().getExistingFile(
                new ResourceLocation(IronsBotany.MODID, "block/" + name));
        getVariantBuilder(IBBlocks.BLOCKS.getEntries().stream()
                .filter(r -> r.getId().getPath().equals(name))
                .findFirst()
                .orElseThrow()
                .get())
            .partialState().setModels(new ConfiguredModel(blockModel));
        // Item model: parent → block model
        itemModels().withExistingParent(name,
                new ResourceLocation(IronsBotany.MODID, "block/" + name));
    }
}
