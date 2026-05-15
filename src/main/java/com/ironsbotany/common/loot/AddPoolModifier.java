package com.ironsbotany.common.loot;

import com.google.common.base.Suppliers;
import com.ironsbotany.common.config.CommonConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Adds a configured set of weighted item entries to every loot table that
 * matches this modifier's conditions. Replaces the previous code-based
 * {@code LootTableInjector}; targeting goes through Forge's
 * {@code forge:loot_table_id} condition in the modifier JSON, so packs can
 * override or disable the whole thing via datapack.
 *
 * <p>Schema (under {@code data/ironsbotany/loot_modifiers/...}):</p>
 * <pre>{@code
 * {
 *   "type": "ironsbotany:add_pool",
 *   "conditions": [
 *     { "condition": "forge:loot_table_id",
 *       "loot_table_id": "minecraft:chests/village/village_plains_house" }
 *   ],
 *   "entries": [
 *     { "item": "ironsbotany:spell_petal", "weight": 15, "min": 1, "max": 3 },
 *     { "item": "ironsbotany:mana_infused_essence", "weight": 10, "min": 1, "max": 1 }
 *   ]
 * }
 * }</pre>
 *
 * <p>Runtime master toggle: {@link CommonConfig#ENABLE_VANILLA_LOOT_INJECTION}
 * — if false, the modifier returns the unchanged loot.</p>
 */
public class AddPoolModifier extends LootModifier {

    public record Entry(ResourceLocation item, int weight, int min, int max) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter(Entry::item),
            Codec.INT.optionalFieldOf("weight", 1).forGetter(Entry::weight),
            Codec.INT.optionalFieldOf("min", 1).forGetter(Entry::min),
            Codec.INT.optionalFieldOf("max", 1).forGetter(Entry::max)
        ).apply(inst, Entry::new));
    }

    public static final java.util.function.Supplier<Codec<AddPoolModifier>> CODEC = Suppliers.memoize(() ->
        RecordCodecBuilder.create(inst -> codecStart(inst)
            .and(Entry.CODEC.listOf().fieldOf("entries").forGetter(m -> m.entries))
            .apply(inst, AddPoolModifier::new)));

    private final List<Entry> entries;
    private final int totalWeight;

    public AddPoolModifier(LootItemCondition[] conditions, List<Entry> entries) {
        super(conditions);
        this.entries = entries;
        int total = 0;
        for (Entry e : entries) total += Math.max(0, e.weight);
        this.totalWeight = total;
    }

    @Nonnull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!CommonConfig.ENABLE_VANILLA_LOOT_INJECTION.get() || totalWeight <= 0) {
            return generatedLoot;
        }
        RandomSource random = context.getRandom();
        int roll = random.nextInt(totalWeight);
        int acc = 0;
        for (Entry entry : entries) {
            acc += Math.max(0, entry.weight);
            if (roll < acc) {
                Item item = BuiltInRegistries.ITEM.get(entry.item);
                if (item != null) {
                    int count = entry.min + (entry.max > entry.min
                        ? random.nextInt(entry.max - entry.min + 1) : 0);
                    if (count > 0) {
                        generatedLoot.add(new ItemStack(item, count));
                    }
                }
                break;
            }
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
