package com.ironsbotany.common.registry;

import com.ironsbotany.IronsBotany;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class IBAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = 
        DeferredRegister.create(ForgeRegistries.ATTRIBUTES, IronsBotany.MODID);

    /**
     * Maximum total Botania-cost reduction obtainable from {@link #MANA_EFFICIENCY}. Casting is
     * never free: at the cap a spell still costs a quarter of its listed Botania mana.
     */
    public static final double MAX_MANA_EFFICIENCY = 0.75;

    /**
     * Fractional reduction of a spell's <em>Botania</em> mana cost. {@code 0.0} means no
     * discount; {@code 0.75} is the cap. Equipment contributes {@code ADDITION} modifiers that
     * sum, e.g. a Gaia Spirit Wand's {@code 0.15} is "−15% Botania mana cost".
     *
     * <h3>Why the range changed in 1.10.0</h3>
     * Through 1.9.0 this was {@code RangedAttribute(default 1.0, min 0.0, max 1.0)} while every
     * item added a <em>positive</em> {@code ADDITION} modifier between {@code 0.03} and
     * {@code 0.15}. A value already sitting at its own maximum cannot be increased, so every
     * modifier was clamped away — and nothing read the attribute in the cost path anyway, so
     * even an unclamped value would have done nothing. The advertised bonus was doubly inert.
     *
     * <p>Rebasing to {@code default 0.0, max 0.75} makes each item's existing number mean exactly
     * what its tooltip already claimed, with no data or recipe changes, and gives "efficiency"
     * its natural direction: higher is better. The value is consumed once, in documented order,
     * by the cast transaction's cost composition.
     *
     * <p>The default is a computed attribute base, not saved state, so this carries no world
     * migration.
     */
    public static final RegistryObject<Attribute> MANA_EFFICIENCY = ATTRIBUTES.register("mana_efficiency",
            () -> new RangedAttribute("attribute.ironsbotany.mana_efficiency", 0.0, 0.0, MAX_MANA_EFFICIENCY)
                    .setSyncable(true));

    /** Botany school spell power multiplier. Range mirrors ISS's other school power attributes. */
    public static final RegistryObject<Attribute> BOTANY_SPELL_POWER = ATTRIBUTES.register("botany_spell_power",
            () -> new RangedAttribute("attribute.ironsbotany.botany_spell_power", 1.0, 0.0, 100.0)
                    .setSyncable(true));

    /** Botany school resistance multiplier. */
    public static final RegistryObject<Attribute> BOTANY_MAGIC_RESIST = ATTRIBUTES.register("botany_magic_resist",
            () -> new RangedAttribute("attribute.ironsbotany.botany_magic_resist", 1.0, 0.0, 2.0)
                    .setSyncable(true));

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }
}
