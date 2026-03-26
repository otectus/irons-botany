package com.ironsbotany;

import com.ironsbotany.common.config.ClientConfig;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.config.ConfigHelper;
import com.ironsbotany.common.flower.FlowerAuraRegistration;
import com.ironsbotany.common.network.PacketHandler;
import com.ironsbotany.common.registry.*;
import com.ironsbotany.common.spell.catalyst.CatalystRegistration;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(IronsBotany.MODID)
public class IronsBotany {
    public static final String MODID = "ironsbotany";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IronsBotany() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register deferred registers
        IBItems.register(modEventBus);
        IBBlocks.register(modEventBus);
        IBSpells.register(modEventBus);
        IBAttributes.register(modEventBus);
        IBEntities.register(modEventBus);
        IBRecipeTypes.register(modEventBus);
        IBCreativeTabs.register(modEventBus);
        IBSounds.register(modEventBus);
        IBParticles.register(modEventBus);
        IBBlockEntities.register(modEventBus);

        // Register configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        // Register setup
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events
        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("Iron's Botany initialized!");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Network setup
            PacketHandler.register();
            
            // Register spell catalysts
            CatalystRegistration.registerCatalysts();
            
            // Register flower auras
            FlowerAuraRegistration.registerFlowerAuras();

            // Validate config combinations
            ConfigHelper.validateConfig();

            LOGGER.info("Iron's Botany common setup complete!");
        });
    }
}
