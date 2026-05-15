package com.ironsbotany.data;

import com.ironsbotany.IronsBotany;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

/**
 * Datagen entrypoint for Iron's Botany.
 *
 * <p>Providers are intentionally minimal in 1.7.0; this scaffold exists so
 * future phases can add recipes, advancements, language, models, loot tables,
 * and tags as data-driven content instead of hand-edited JSON. See Phase 2.2
 * for the Global Loot Modifier provider.</p>
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class IBDataGenerator {
    private IBDataGenerator() {}

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        PackOutput output = gen.getPackOutput();
        ExistingFileHelper existing = event.getExistingFileHelper();
        CompletableFuture<net.minecraft.core.HolderLookup.Provider> lookup = event.getLookupProvider();

        // Server data
        gen.addProvider(event.includeServer(),
            new IBLootModifierProvider(output));
    }
}
