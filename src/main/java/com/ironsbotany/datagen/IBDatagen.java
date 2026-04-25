package com.ironsbotany.datagen;

import com.ironsbotany.IronsBotany;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Mod-bus datagen entry point. Wired into ForgeGradle's {@code runData}
 * task via the build.gradle data run configuration.
 *
 * <p>Each provider here generates one slice of the output tree under
 * {@code src/generated/resources/}; the resulting JSON is committed
 * alongside hand-written content so contributors don't need a working
 * datagen toolchain to make assets.
 *
 * <h3>Migration policy</h3>
 * As providers come online, the corresponding hand-written JSON under
 * {@code src/main/resources/} is deleted to avoid Gradle source-set
 * conflicts. Botania custom-recipe JSONs (petal_apothecary, runic_altar,
 * terra_plate, elven_trade) and Patchouli book entries stay
 * hand-written — they're declarative content, not lookup data.
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class IBDatagen {

    private IBDatagen() {}

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFiles = event.getExistingFileHelper();

        // Client-side data
        if (event.includeClient()) {
            generator.addProvider(true, new IBItemModelProvider(output, existingFiles));
            generator.addProvider(true, new IBBlockStateProvider(output, existingFiles));
            generator.addProvider(true, new IBLanguageProvider(output));
        }

        // Server-side data
        if (event.includeServer()) {
            generator.addProvider(true, new IBLootTableProvider(output));
            generator.addProvider(true, new IBRecipeProvider(output));
        }
    }
}
