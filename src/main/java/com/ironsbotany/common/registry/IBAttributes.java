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

    public static final RegistryObject<Attribute> MANA_EFFICIENCY = ATTRIBUTES.register("mana_efficiency",
            () -> new RangedAttribute("attribute.ironsbotany.mana_efficiency", 1.0, 0.0, 1.0)
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
