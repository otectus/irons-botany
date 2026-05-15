package com.ironsbotany.common.loot;

import com.ironsbotany.IronsBotany;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * Deferred registers for Iron's Botany Global Loot Modifier codecs.
 *
 * Replaces the runtime {@code LootTableLoadEvent} hack with data-driven JSON
 * modifiers, so pack authors can disable or replace injected loot through a
 * datapack instead of cracking open Java.
 */
public final class IBLootModifiers {
    private IBLootModifiers() {}

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> MODIFIERS =
        DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, IronsBotany.MODID);

    public static final RegistryObject<Codec<AddPoolModifier>> ADD_POOL =
        register("add_pool", AddPoolModifier.CODEC);

    private static <T extends IGlobalLootModifier> RegistryObject<Codec<T>> register(
            String name, Supplier<Codec<T>> codec) {
        return MODIFIERS.register(name, codec);
    }

    public static void register(IEventBus eventBus) {
        MODIFIERS.register(eventBus);
    }
}
