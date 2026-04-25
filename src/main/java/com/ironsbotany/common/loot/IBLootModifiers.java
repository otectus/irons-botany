package com.ironsbotany.common.loot;

import com.ironsbotany.IronsBotany;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Forge global loot modifier codec registry. The actual modifier
 * instances live in {@code data/ironsbotany/loot_modifiers/} and the
 * master pointer file at
 * {@code data/forge/loot_modifiers/global_loot_modifiers.json} lists
 * which modifier JSONs are active.
 */
public final class IBLootModifiers {

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, IronsBotany.MODID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> ADD_ITEM =
            MODIFIERS.register("add_item", () -> AddItemLootModifier.CODEC);

    private IBLootModifiers() {}

    public static void register(IEventBus eventBus) {
        MODIFIERS.register(eventBus);
    }
}
