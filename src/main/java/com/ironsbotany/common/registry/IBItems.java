package com.ironsbotany.common.registry;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.item.*;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class IBItems {
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, IronsBotany.MODID);

    // Curio Items
    public static final RegistryObject<Item> BOTANICAL_FOCUS = ITEMS.register("botanical_focus",
            () -> new BotanicalFocusItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> BOTANICAL_RING = ITEMS.register("botanical_ring",
            () -> new BotanicalRingItem(new Item.Properties().stacksTo(1)));

    // Weapons
    public static final RegistryObject<Item> TERRASTEEL_SPELL_BLADE = ITEMS.register("terrasteel_spell_blade",
            () -> new TerrasteelSpellBladeItem(new Item.Properties().stacksTo(1).durability(3000)));

    public static final RegistryObject<Item> LIVINGWOOD_STAFF = ITEMS.register("livingwood_staff",
            () -> new LivingwoodStaffItem(new Item.Properties().stacksTo(1).durability(1000)));

    // Manasteel Staff (Phase 6.2) — extends ISS StaffItem; mana cap attached via IBCapabilityHandler
    public static final RegistryObject<Item> MANASTEEL_STAFF = ITEMS.register("manasteel_staff",
            () -> new ManasteelStaffItem(new Item.Properties().stacksTo(1).durability(750)));

    // Spellbooks (Phase 6.3) — ISS SpellBook subclasses with multi-attribute containers
    public static final RegistryObject<Item> TERRASTEEL_SPELLBOOK = ITEMS.register("terrasteel_spellbook",
            TerrasteelSpellbookItem::new);

    public static final RegistryObject<Item> ARCANE_CODEX = ITEMS.register("arcane_codex",
            ArcaneCodexItem::new);

    // Elementium Scroll (Phase 6.4) — reusable scroll; pulls cost from Botania mana network
    public static final RegistryObject<Item> ELEMENTIUM_SCROLL = ITEMS.register("elementium_scroll",
            ElementiumScrollItem::new);

    // New curios (Phase 6.5)
    public static final RegistryObject<Item> MANA_RESERVOIR_RING = ITEMS.register("mana_reservoir_ring",
            () -> new ManaReservoirRingItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DAYBLOOM_AMULET = ITEMS.register("daybloom_amulet",
            () -> new DaybloomAmuletItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> GAIAS_BLESSING = ITEMS.register("gaias_blessing",
            () -> new GaiasBlessingItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DREAMWOOD_SCEPTER = ITEMS.register("dreamwood_scepter",
            () -> new DreamwoodScepterItem(new Item.Properties().stacksTo(1).durability(2000)));

    public static final RegistryObject<Item> GAIA_SPIRIT_WAND = ITEMS.register("gaia_spirit_wand",
            () -> new GaiaSpiritWandItem(new Item.Properties().stacksTo(1).durability(5000)));

    // Armor - Manasteel Wizard Set
    public static final RegistryObject<Item> MANASTEEL_WIZARD_HELMET = ITEMS.register("manasteel_wizard_helmet",
            () -> new ManasteelWizardArmorItem(IBArmorMaterials.MANASTEEL_WIZARD, 
                    net.minecraft.world.item.ArmorItem.Type.HELMET, new Item.Properties()));

    public static final RegistryObject<Item> MANASTEEL_WIZARD_CHESTPLATE = ITEMS.register("manasteel_wizard_chestplate",
            () -> new ManasteelWizardArmorItem(IBArmorMaterials.MANASTEEL_WIZARD, 
                    net.minecraft.world.item.ArmorItem.Type.CHESTPLATE, new Item.Properties()));

    public static final RegistryObject<Item> MANASTEEL_WIZARD_LEGGINGS = ITEMS.register("manasteel_wizard_leggings",
            () -> new ManasteelWizardArmorItem(IBArmorMaterials.MANASTEEL_WIZARD, 
                    net.minecraft.world.item.ArmorItem.Type.LEGGINGS, new Item.Properties()));

    public static final RegistryObject<Item> MANASTEEL_WIZARD_BOOTS = ITEMS.register("manasteel_wizard_boots",
            () -> new ManasteelWizardArmorItem(IBArmorMaterials.MANASTEEL_WIZARD, 
                    net.minecraft.world.item.ArmorItem.Type.BOOTS, new Item.Properties()));

    // Upgrade Orbs
    public static final RegistryObject<Item> ORB_OF_FLORA = ITEMS.register("orb_of_flora",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.FLORA_ORB_TYPE, "flora"));

    public static final RegistryObject<Item> ORB_OF_THE_POOL = ITEMS.register("orb_of_the_pool",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.POOL_ORB_TYPE, "pool"));

    public static final RegistryObject<Item> ORB_OF_BURSTING = ITEMS.register("orb_of_bursting",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.BURSTING_ORB_TYPE, "bursting"));

    public static final RegistryObject<Item> ORB_OF_TERRAN_MIGHT = ITEMS.register("orb_of_terran_might",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.TERRAN_ORB_TYPE, "terran"));

    // Crafting Components
    public static final RegistryObject<Item> MANA_INFUSED_ESSENCE = ITEMS.register("mana_infused_essence",
            () -> new TooltipItem(new Item.Properties(), "item.ironsbotany.mana_infused_essence.tooltip"));

    public static final RegistryObject<Item> BOTANICAL_CRYSTAL = ITEMS.register("botanical_crystal",
            () -> new TooltipItem(new Item.Properties(), "item.ironsbotany.botanical_crystal.tooltip"));

    public static final RegistryObject<Item> SPELL_PETAL = ITEMS.register("spell_petal",
            () -> new TooltipItem(new Item.Properties(), "item.ironsbotany.spell_petal.tooltip"));

    // Patchouli Book - opens guidebook GUI when right-clicked (if Patchouli is present)
    public static final RegistryObject<Item> BOTANICAL_GRIMOIRE = ITEMS.register("botanical_grimoire",
            () -> new com.ironsbotany.common.item.BotanicalGrimoireItem(new Item.Properties().stacksTo(1)));

    // Mana Inks (Phase 2B) — petal apothecary outputs that replace vanilla ink for ISS scroll forging
    public static final RegistryObject<Item> MINOR_MANA_INK = ITEMS.register("minor_mana_ink",
            () -> new TooltipItem(new Item.Properties(), "item.ironsbotany.minor_mana_ink.tooltip"));

    public static final RegistryObject<Item> GREATER_MANA_INK = ITEMS.register("greater_mana_ink",
            () -> new TooltipItem(new Item.Properties(), "item.ironsbotany.greater_mana_ink.tooltip"));

    public static final RegistryObject<Item> PRIME_MANA_INK = ITEMS.register("prime_mana_ink",
            () -> new TooltipItem(new Item.Properties(), "item.ironsbotany.prime_mana_ink.tooltip"));

    // School-tied upgrade orbs (Phase 2C) — runic altar outputs, one per ISS school's spell-power attribute
    public static final RegistryObject<Item> ORB_OF_FIRE_POWER = ITEMS.register("orb_of_fire_power",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.FLORA_ORB_TYPE, "fire"));

    public static final RegistryObject<Item> ORB_OF_FROST_POWER = ITEMS.register("orb_of_frost_power",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.POOL_ORB_TYPE, "frost"));

    public static final RegistryObject<Item> ORB_OF_LIGHTNING_POWER = ITEMS.register("orb_of_lightning_power",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.BURSTING_ORB_TYPE, "lightning"));

    public static final RegistryObject<Item> ORB_OF_HOLY_POWER = ITEMS.register("orb_of_holy_power",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.FLORA_ORB_TYPE, "holy"));

    public static final RegistryObject<Item> ORB_OF_ENDER_POWER = ITEMS.register("orb_of_ender_power",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.POOL_ORB_TYPE, "ender"));

    public static final RegistryObject<Item> ORB_OF_BLOOD_POWER = ITEMS.register("orb_of_blood_power",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.BURSTING_ORB_TYPE, "blood"));

    public static final RegistryObject<Item> ORB_OF_NATURE_POWER = ITEMS.register("orb_of_nature_power",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.TERRAN_ORB_TYPE, "nature"));

    public static final RegistryObject<Item> ORB_OF_ELDRITCH_POWER = ITEMS.register("orb_of_eldritch_power",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.TERRAN_ORB_TYPE, "eldritch"));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
