package com.ironsbotany.common.registry;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.spell.*;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class IBSpells {
    public static final DeferredRegister<AbstractSpell> SPELLS = 
        DeferredRegister.create(io.redspace.ironsspellbooks.api.registry.SpellRegistry.SPELL_REGISTRY_KEY, IronsBotany.MODID);

    // Botanical Spells
    public static final RegistryObject<AbstractSpell> MANA_BLOOM = SPELLS.register("mana_bloom",
            ManaBloomSpell::new);

    public static final RegistryObject<AbstractSpell> BOTANICAL_BURST = SPELLS.register("botanical_burst",
            BotanicalBurstSpell::new);

    public static final RegistryObject<AbstractSpell> FLOWER_SHIELD = SPELLS.register("flower_shield",
            FlowerShieldSpell::new);

    public static final RegistryObject<AbstractSpell> LIVING_ROOT_GRASP = SPELLS.register("living_root_grasp",
            LivingRootGraspSpell::new);

    public static final RegistryObject<AbstractSpell> SPARK_SWARM = SPELLS.register("spark_swarm",
            SparkSwarmSpell::new);

    public static final RegistryObject<AbstractSpell> RUNIC_INFUSION = SPELLS.register("runic_infusion",
            RunicInfusionSpell::new);

    public static final RegistryObject<AbstractSpell> PETAL_STORM = SPELLS.register("petal_storm",
            PetalStormSpell::new);

    public static final RegistryObject<AbstractSpell> GAIA_WRATH = SPELLS.register("gaia_wrath",
            GaiaWrathSpell::new);

    public static final RegistryObject<AbstractSpell> MANA_REBIRTH = SPELLS.register("mana_rebirth",
            ManaRebirthSpell::new);

    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }
}
