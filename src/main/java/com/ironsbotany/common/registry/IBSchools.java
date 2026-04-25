package com.ironsbotany.common.registry;

import com.ironsbotany.IronsBotany;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Custom Iron's Spells 'n Spellbooks school: <b>Botany</b>. Replaces the
 * earlier shortcut of piggybacking on the Nature school, giving Iron's
 * Botany its own focus tag, attribute pair, damage type, and cast sound.
 *
 * <p>Wiring summary:
 * <ul>
 *   <li>Focus item tag: {@code #ironsbotany:focus/botany} — datapack-defined,
 *       expected to include Mana Pearl, Pixie Dust, Dreamwood Twig, etc.</li>
 *   <li>Power attribute: {@link IBAttributes#BOTANY_SPELL_POWER}</li>
 *   <li>Resist attribute: {@link IBAttributes#BOTANY_MAGIC_RESIST}</li>
 *   <li>Damage type: {@link IBDamageTypes#BOTANY} (JSON at
 *       {@code data/ironsbotany/damage_type/botany.json})</li>
 *   <li>Cast sound: vanilla amethyst block chime — placeholder until a
 *       custom sound asset is recorded.</li>
 * </ul>
 *
 * <p>Existing Botanical spells continue to extend
 * {@code AbstractBotanicalSpell}; their {@code getSchoolType()} now
 * returns {@link #BOTANY} instead of {@code SchoolRegistry.NATURE}.
 */
public final class IBSchools {

    public static final DeferredRegister<SchoolType> SCHOOLS =
            DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, IronsBotany.MODID);

    public static final TagKey<Item> BOTANY_FOCUS_TAG = ItemTags.create(
            new ResourceLocation(IronsBotany.MODID, "focus/botany"));

    public static final RegistryObject<SchoolType> BOTANY = SCHOOLS.register("botany",
            () -> new SchoolType(
                    new ResourceLocation(IronsBotany.MODID, "botany"),
                    BOTANY_FOCUS_TAG,
                    Component.translatable("school.ironsbotany.botany"),
                    IBAttributes.BOTANY_SPELL_POWER::get,
                    IBAttributes.BOTANY_MAGIC_RESIST::get,
                    () -> SoundEvents.AMETHYST_BLOCK_CHIME,
                    IBDamageTypes.BOTANY
            ));

    private IBSchools() {}

    public static void register(IEventBus eventBus) {
        SCHOOLS.register(eventBus);
    }
}
