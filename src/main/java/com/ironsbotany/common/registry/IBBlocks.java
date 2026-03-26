package com.ironsbotany.common.registry;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.block.ManaConduitBlock;
import com.ironsbotany.common.block.SpellReservoirBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class IBBlocks {
    public static final DeferredRegister<Block> BLOCKS = 
        DeferredRegister.create(ForgeRegistries.BLOCKS, IronsBotany.MODID);

    // Spell Reservoir (Mana Pool Augment)
    public static final RegistryObject<Block> SPELL_RESERVOIR = registerBlock("spell_reservoir",
            () -> new SpellReservoirBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .noOcclusion()));

    // Mana Conduit (Botania mana pool -> ISS mana converter)
    public static final RegistryObject<Block> MANA_CONDUIT = registerBlock("mana_conduit",
            () -> new ManaConduitBlock(BlockBehaviour.Properties.of()
                    .strength(2.0F)
                    .noOcclusion()));

    // Helper method to register block with item
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        IBItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
