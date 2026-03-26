package com.ironsbotany.common.registry;

import com.ironsbotany.IronsBotany;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class IBParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = 
        DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, IronsBotany.MODID);

    public static final RegistryObject<SimpleParticleType> MANA_TRANSFER = PARTICLES.register("mana_transfer",
            () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> BOTANICAL_BURST = PARTICLES.register("botanical_burst",
            () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> PETAL_MAGIC = PARTICLES.register("petal_magic",
            () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}
