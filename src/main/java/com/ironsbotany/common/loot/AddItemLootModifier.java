package com.ironsbotany.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

/**
 * Forge global loot modifier that appends a single {@link ItemStack} to a
 * loot table's output, gated by the modifier's own conditions array. Used
 * to inject Iron's Botany items into vanilla / Botania loot tables without
 * overwriting the original table JSON.
 *
 * <p>JSON shape:
 * <pre>
 * {
 *   "type": "ironsbotany:add_item",
 *   "conditions": [ ... vanilla LootItemCondition list ... ],
 *   "item": "irons_spellbooks:epic_ink",
 *   "count": 1
 * }
 * </pre>
 */
public class AddItemLootModifier extends LootModifier {

    public static final Codec<AddItemLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst)
            .and(BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(m -> m.item))
            .and(Codec.INT.optionalFieldOf("count", 1).forGetter(m -> m.count))
            .apply(inst, AddItemLootModifier::new));

    private final Item item;
    private final int count;

    public AddItemLootModifier(LootItemCondition[] conditions, Item item, int count) {
        super(conditions);
        this.item = item;
        this.count = count;
    }

    @Override
    @NotNull
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.add(new ItemStack(item, count));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    /** Resource id used in JSON {@code "type"} fields. */
    public static final ResourceLocation ID = new ResourceLocation("ironsbotany", "add_item");
}
