package com.ironsbotany.data;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.loot.AddPoolModifier;
import com.ironsbotany.common.loot.AddPoolModifier.Entry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

import java.util.List;

/**
 * Generates {@code data/ironsbotany/loot_modifiers/*.json} for the four
 * vanilla chests Iron's Botany injects into. The Java payload here is exactly
 * the migration of the old {@code LootTableInjector} runtime hack into
 * datapack-overridable form.
 *
 * <p>Each modifier targets a single loot table via {@link LootTableIdCondition}
 * and adds a weighted list of IB items as one extra roll.</p>
 */
public class IBLootModifierProvider extends GlobalLootModifierProvider {

    public IBLootModifierProvider(PackOutput output) {
        super(output, IronsBotany.MODID);
    }

    @Override
    protected void start() {
        // Village houses — three biome variants get the same drop pool.
        addVillageHouse("village_plains_house", "minecraft:chests/village/village_plains_house");
        addVillageHouse("village_taiga_house",  "minecraft:chests/village/village_taiga_house");
        addVillageHouse("village_snowy_house",  "minecraft:chests/village/village_snowy_house");

        add("mineshaft",
            new AddPoolModifier(
                conditionsFor("minecraft:chests/abandoned_mineshaft"),
                List.of(
                    new Entry(new ResourceLocation(IronsBotany.MODID, "botanical_crystal"), 5, 1, 1),
                    new Entry(new ResourceLocation(IronsBotany.MODID, "spell_petal"),     15, 2, 5)
                )));

        add("stronghold_library",
            new AddPoolModifier(
                conditionsFor("minecraft:chests/stronghold_library"),
                List.of(
                    new Entry(new ResourceLocation(IronsBotany.MODID, "botanical_grimoire"),     5, 1, 1),
                    new Entry(new ResourceLocation(IronsBotany.MODID, "botanical_crystal"),     10, 1, 1),
                    new Entry(new ResourceLocation(IronsBotany.MODID, "mana_infused_essence"),  15, 1, 3)
                )));

        add("end_city",
            new AddPoolModifier(
                conditionsFor("minecraft:chests/end_city_treasure"),
                List.of(
                    new Entry(new ResourceLocation(IronsBotany.MODID, "orb_of_terran_might"),  5, 1, 1),
                    new Entry(new ResourceLocation(IronsBotany.MODID, "botanical_crystal"),   10, 1, 2)
                )));
    }

    private void addVillageHouse(String name, String tableId) {
        add(name, new AddPoolModifier(
            conditionsFor(tableId),
            List.of(
                new Entry(new ResourceLocation(IronsBotany.MODID, "spell_petal"),          15, 1, 3),
                new Entry(new ResourceLocation(IronsBotany.MODID, "mana_infused_essence"), 10, 1, 1)
            )));
    }

    private static LootItemCondition[] conditionsFor(String lootTableId) {
        return new LootItemCondition[] {
            LootTableIdCondition.builder(new ResourceLocation(lootTableId)).build()
        };
    }
}
