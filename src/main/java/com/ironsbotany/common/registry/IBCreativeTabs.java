package com.ironsbotany.common.registry;

import com.ironsbotany.IronsBotany;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class IBCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IronsBotany.MODID);

    public static final RegistryObject<CreativeModeTab> IRONS_BOTANY_TAB = CREATIVE_MODE_TABS.register("ironsbotany_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.ironsbotany"))
                    .icon(() -> new ItemStack(IBItems.BOTANICAL_FOCUS.get()))
                    .displayItems((parameters, output) -> {
                        // Curios
                        output.accept(IBItems.BOTANICAL_FOCUS.get());
                        output.accept(IBItems.BOTANICAL_RING.get());
                        
                        // Weapons
                        output.accept(IBItems.TERRASTEEL_SPELL_BLADE.get());
                        output.accept(IBItems.LIVINGWOOD_STAFF.get());
                        output.accept(IBItems.DREAMWOOD_SCEPTER.get());
                        output.accept(IBItems.GAIA_SPIRIT_WAND.get());
                        
                        // Armor
                        output.accept(IBItems.MANASTEEL_WIZARD_HELMET.get());
                        output.accept(IBItems.MANASTEEL_WIZARD_CHESTPLATE.get());
                        output.accept(IBItems.MANASTEEL_WIZARD_LEGGINGS.get());
                        output.accept(IBItems.MANASTEEL_WIZARD_BOOTS.get());
                        
                        // Upgrade Orbs
                        output.accept(IBItems.ORB_OF_FLORA.get());
                        output.accept(IBItems.ORB_OF_THE_POOL.get());
                        output.accept(IBItems.ORB_OF_BURSTING.get());
                        output.accept(IBItems.ORB_OF_TERRAN_MIGHT.get());
                        
                        // Components
                        output.accept(IBItems.MANA_INFUSED_ESSENCE.get());
                        output.accept(IBItems.BOTANICAL_CRYSTAL.get());
                        output.accept(IBItems.SPELL_PETAL.get());
                        
                        // Blocks
                        output.accept(IBBlocks.SPELL_RESERVOIR.get());
                        output.accept(IBBlocks.MANA_CONDUIT.get());
                        
                        // Book
                        output.accept(IBItems.BOTANICAL_GRIMOIRE.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
