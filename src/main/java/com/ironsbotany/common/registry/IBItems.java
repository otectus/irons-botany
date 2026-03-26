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
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> BOTANICAL_CRYSTAL = ITEMS.register("botanical_crystal",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SPELL_PETAL = ITEMS.register("spell_petal",
            () -> new Item(new Item.Properties()));

    // Patchouli Book - using simple item for now (Patchouli API changed)
    public static final RegistryObject<Item> BOTANICAL_GRIMOIRE = ITEMS.register("botanical_grimoire",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
