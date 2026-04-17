package com.ironsbotany.client;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.client.particle.BotanicalBurstParticle;
import com.ironsbotany.client.particle.ManaTransferParticle;
import com.ironsbotany.client.particle.PetalMagicParticle;
import com.ironsbotany.client.renderer.BotanicalBurstRenderer;
import com.ironsbotany.client.renderer.ManaConduitBER;
import com.ironsbotany.client.renderer.SparkSwarmRenderer;
import com.ironsbotany.client.renderer.SpellReservoirBER;
import com.ironsbotany.common.registry.IBBlockEntities;
import com.ironsbotany.common.registry.IBEntities;
import com.ironsbotany.common.registry.IBParticles;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        IronsBotany.LOGGER.info("Iron's Botany client setup complete!");
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(IBEntities.BOTANICAL_BURST.get(), BotanicalBurstRenderer::new);
        event.registerEntityRenderer(IBEntities.SPARK_SWARM.get(), SparkSwarmRenderer::new);

        event.registerBlockEntityRenderer(IBBlockEntities.SPELL_RESERVOIR.get(), SpellReservoirBER::new);
        event.registerBlockEntityRenderer(IBBlockEntities.MANA_CONDUIT.get(), ManaConduitBER::new);

        IronsBotany.LOGGER.info("Registered entity and block entity renderers");
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(IBParticles.MANA_TRANSFER.get(), ManaTransferParticle.Provider::new);
        event.registerSpriteSet(IBParticles.BOTANICAL_BURST.get(), BotanicalBurstParticle.Provider::new);
        event.registerSpriteSet(IBParticles.PETAL_MAGIC.get(), PetalMagicParticle.Provider::new);

        IronsBotany.LOGGER.info("Registered particle providers");
    }
}
