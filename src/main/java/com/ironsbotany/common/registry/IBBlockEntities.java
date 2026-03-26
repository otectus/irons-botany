package com.ironsbotany.common.registry;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.block.entity.ManaConduitBlockEntity;
import com.ironsbotany.common.block.entity.SpellReservoirBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class IBBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, IronsBotany.MODID);

    public static final RegistryObject<BlockEntityType<SpellReservoirBlockEntity>> SPELL_RESERVOIR = 
        BLOCK_ENTITIES.register("spell_reservoir", () ->
            BlockEntityType.Builder.of(SpellReservoirBlockEntity::new,
                    IBBlocks.SPELL_RESERVOIR.get()).build(null));

    public static final RegistryObject<BlockEntityType<ManaConduitBlockEntity>> MANA_CONDUIT =
        BLOCK_ENTITIES.register("mana_conduit", () ->
            BlockEntityType.Builder.of(ManaConduitBlockEntity::new,
                    IBBlocks.MANA_CONDUIT.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
