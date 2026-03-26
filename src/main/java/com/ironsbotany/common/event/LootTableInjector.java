package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.registry.IBItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LootTableInjector {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (!CommonConfig.ENABLE_VANILLA_LOOT_INJECTION.get()) return;

        String path = event.getName().getPath();

        // Village houses
        if (path.equals("chests/village/village_plains_house") ||
            path.equals("chests/village/village_taiga_house") ||
            path.equals("chests/village/village_snowy_house")) {
            event.getTable().addPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(IBItems.SPELL_PETAL.get()).setWeight(15)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                .add(LootItem.lootTableItem(IBItems.MANA_INFUSED_ESSENCE.get()).setWeight(10))
                .name("ironsbotany_village")
                .build());
        }

        // Mineshafts
        if (path.equals("chests/abandoned_mineshaft")) {
            event.getTable().addPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(IBItems.BOTANICAL_CRYSTAL.get()).setWeight(5))
                .add(LootItem.lootTableItem(IBItems.SPELL_PETAL.get()).setWeight(15)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))))
                .name("ironsbotany_mineshaft")
                .build());
        }

        // Stronghold library
        if (path.equals("chests/stronghold_library")) {
            event.getTable().addPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(IBItems.BOTANICAL_GRIMOIRE.get()).setWeight(5))
                .add(LootItem.lootTableItem(IBItems.BOTANICAL_CRYSTAL.get()).setWeight(10))
                .add(LootItem.lootTableItem(IBItems.MANA_INFUSED_ESSENCE.get()).setWeight(15)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                .name("ironsbotany_stronghold")
                .build());
        }

        // End city
        if (path.equals("chests/end_city_treasure")) {
            event.getTable().addPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(IBItems.ORB_OF_TERRAN_MIGHT.get()).setWeight(5))
                .add(LootItem.lootTableItem(IBItems.BOTANICAL_CRYSTAL.get()).setWeight(10)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))))
                .name("ironsbotany_end_city")
                .build());
        }
    }
}
