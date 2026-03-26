package com.ironsbotany.common.registry;

import com.ironsbotany.IronsBotany;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class IBSchools {
    public static final DeferredRegister<SchoolType> SCHOOLS = 
        DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, IronsBotany.MODID);

    public static final RegistryObject<SchoolType> BOTANICAL = SCHOOLS.register("botanical",
            () -> new SchoolType(
                    ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "botanical"),
                    TagKey.create(net.minecraft.core.registries.Registries.ITEM, 
                        ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "botanical_focus")),
                    Component.translatable("school.ironsbotany.botanical"),
                    LazyOptional.of(() -> IBAttributes.BOTANICAL_SPELL_POWER.get()),
                    LazyOptional.of(() -> IBAttributes.BOTANICAL_RESIST.get()),
                    LazyOptional.empty(),
                    net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                        ResourceLocation.fromNamespaceAndPath(IronsBotany.MODID, "botanical"))
            ));

    public static void register(IEventBus eventBus) {
        SCHOOLS.register(eventBus);
    }
}
