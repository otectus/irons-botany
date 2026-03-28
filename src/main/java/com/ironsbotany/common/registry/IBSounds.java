package com.ironsbotany.common.registry;

import com.ironsbotany.IronsBotany;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class IBSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = 
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, IronsBotany.MODID);

    public static final RegistryObject<SoundEvent> MANA_CONVERSION = registerSound("mana_conversion");
    public static final RegistryObject<SoundEvent> BOTANICAL_CAST = registerSound("botanical_cast");
    public static final RegistryObject<SoundEvent> FLOWER_BLOOM = registerSound("flower_bloom");
    public static final RegistryObject<SoundEvent> SPARK_SUMMON = registerSound("spark_summon");

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                new ResourceLocation(IronsBotany.MODID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
