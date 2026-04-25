package com.ironsbotany.api;

import net.minecraft.resources.ResourceLocation;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public surface for downstream mods to register their own Botany-school
 * flowers. Iron's Botany uses the registered metadata for:
 * <ul>
 *   <li>Creative-tab grouping of school-themed flowers</li>
 *   <li>Patchouli auto-generation of flower index pages</li>
 *   <li>Tooltip overlay (which ISS school each flower reacts to)</li>
 * </ul>
 *
 * <p>Registration does not <em>create</em> the BlockEntity — the addon
 * mod is responsible for its own {@code DeferredRegister<Block>} +
 * {@code DeferredRegister<BlockEntityType>}. This registry only records
 * the mapping so Iron's Botany can find and decorate them.
 *
 * <p>Singleton accessed via {@link IronsBotanyApi#flowerRegistry()}.
 */
public final class BotanySchoolFlowerRegistry {

    public static final BotanySchoolFlowerRegistry INSTANCE = new BotanySchoolFlowerRegistry();

    private final Map<ResourceLocation, Entry> entries = new LinkedHashMap<>();

    private BotanySchoolFlowerRegistry() {}

    /**
     * Register a generating flower keyed to one ISS school.
     *
     * @param id        canonical resource location of the flower's block
     * @param schoolId  the {@code SchoolType} resource location it reacts to
     *                  (e.g. {@code irons_spellbooks:fire})
     * @param beClass   the BE class so Iron's Botany can verify type at runtime
     */
    public void registerGenerating(ResourceLocation id,
                                   ResourceLocation schoolId,
                                   Class<? extends GeneratingFlowerBlockEntity> beClass) {
        entries.put(id, new Entry(id, schoolId, FlowerKind.GENERATING, beClass));
    }

    /** Register a functional flower (e.g. Verdant Caster) tied to a school. */
    public void registerFunctional(ResourceLocation id,
                                   ResourceLocation schoolId,
                                   Class<? extends FunctionalFlowerBlockEntity> beClass) {
        entries.put(id, new Entry(id, schoolId, FlowerKind.FUNCTIONAL, beClass));
    }

    public Map<ResourceLocation, Entry> all() {
        return Collections.unmodifiableMap(entries);
    }

    public Entry get(ResourceLocation id) {
        return entries.get(id);
    }

    public enum FlowerKind { GENERATING, FUNCTIONAL }

    public record Entry(ResourceLocation id,
                        ResourceLocation schoolId,
                        FlowerKind kind,
                        Class<?> beClass) {}
}
