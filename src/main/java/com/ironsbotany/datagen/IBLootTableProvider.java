package com.ironsbotany.datagen;

import com.ironsbotany.common.registry.IBBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generates block-drops-self loot tables for every Iron's Botany block.
 * Replaces the hand-written tables under
 * {@code data/ironsbotany/loot_tables/blocks/} once datagen is run.
 */
public class IBLootTableProvider extends LootTableProvider {

    public IBLootTableProvider(PackOutput output) {
        super(output,
              Set.of(),
              List.of(new SubProviderEntry(IBBlockLoot::new, LootContextParamSets.BLOCK)));
    }

    private static class IBBlockLoot extends BlockLootSubProvider {
        protected IBBlockLoot() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            IBBlocks.BLOCKS.getEntries().forEach(reg -> dropSelf(reg.get()));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return IBBlocks.BLOCKS.getEntries().stream()
                    .map(reg -> (Block) reg.get())
                    .collect(Collectors.toList());
        }
    }

    /** Suppresses an unused-import warning for ForgeRegistries / Item — they're referenced indirectly via ResourceLocation lookup. */
    @SuppressWarnings("unused")
    private static final Item ANCHOR = (Item) ForgeRegistries.ITEMS.getValue(net.minecraft.resources.ResourceLocation.tryParse("minecraft:air"));
}
