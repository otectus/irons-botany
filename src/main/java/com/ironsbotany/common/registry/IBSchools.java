package com.ironsbotany.common.registry;

import com.ironsbotany.IronsBotany;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class IBSchools {
    public static final ResourceLocation BOTANY_RESOURCE = new ResourceLocation(IronsBotany.MODID, "botany");

    public static final ResourceKey<DamageType> BOTANY_DAMAGE_TYPE =
        ResourceKey.create(Registries.DAMAGE_TYPE, BOTANY_RESOURCE);

    public static final TagKey<Item> BOTANY_FOCUS_TAG =
        ItemTags.create(new ResourceLocation(IronsBotany.MODID, "botany_focus"));

    public static final DeferredRegister<SchoolType> SCHOOLS =
        DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, IronsBotany.MODID);

    public static final RegistryObject<SchoolType> BOTANY = SCHOOLS.register("botany",
        () -> new SchoolType(
            BOTANY_RESOURCE,
            BOTANY_FOCUS_TAG,
            Component.translatable("school.ironsbotany.botany"),
            AttributeRegistry.NATURE_SPELL_POWER,
            AttributeRegistry.NATURE_MAGIC_RESIST,
            () -> SoundEvents.GROWING_PLANT_CROP,
            BOTANY_DAMAGE_TYPE));

    public static void register(IEventBus eventBus) {
        SCHOOLS.register(eventBus);
    }
}
