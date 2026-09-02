package com.ironsbotany.common.registry;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.item.*;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.registries.FluidRegistry;
import net.minecraft.ChatFormatting;
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

    // Mage swords — melee caster ladder around the Terrasteel Spell Blade (Elementium -> Terrasteel -> Gaia)
    public static final RegistryObject<Item> ELEMENTIUM_MAGE_SWORD = ITEMS.register("elementium_mage_sword",
            () -> new MageSwordItem(MageSwordItem.Tier.ELEMENTIUM, new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> GAIA_MAGE_SWORD = ITEMS.register("gaia_mage_sword",
            () -> new MageSwordItem(MageSwordItem.Tier.GAIA, new Item.Properties().stacksTo(1)));

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

    // Elementium Wand — Elementium rung of the wand ladder; real ISS StaffItem caster
    public static final RegistryObject<Item> ELEMENTIUM_WAND = ITEMS.register("elementium_wand",
            () -> new ElementiumWandItem(new Item.Properties().stacksTo(1).durability(1500)));

    // Terrasteel Wand — late-Botania caster rung between Elementium and Gaia
    public static final RegistryObject<Item> TERRASTEEL_WAND = ITEMS.register("terrasteel_wand",
            () -> new TerrasteelWandItem(new Item.Properties().stacksTo(1).durability(2500)));

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

    // Armor - Mage tiers above Manasteel (Elementium -> Terrasteel -> Gaia)
    public static final RegistryObject<Item> ELEMENTIUM_MAGE_HELMET = registerMageArmor("elementium_mage_helmet", MageArmorItem.Tier.ELEMENTIUM, net.minecraft.world.item.ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> ELEMENTIUM_MAGE_CHESTPLATE = registerMageArmor("elementium_mage_chestplate", MageArmorItem.Tier.ELEMENTIUM, net.minecraft.world.item.ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> ELEMENTIUM_MAGE_LEGGINGS = registerMageArmor("elementium_mage_leggings", MageArmorItem.Tier.ELEMENTIUM, net.minecraft.world.item.ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> ELEMENTIUM_MAGE_BOOTS = registerMageArmor("elementium_mage_boots", MageArmorItem.Tier.ELEMENTIUM, net.minecraft.world.item.ArmorItem.Type.BOOTS);

    public static final RegistryObject<Item> TERRASTEEL_MAGE_HELMET = registerMageArmor("terrasteel_mage_helmet", MageArmorItem.Tier.TERRASTEEL, net.minecraft.world.item.ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> TERRASTEEL_MAGE_CHESTPLATE = registerMageArmor("terrasteel_mage_chestplate", MageArmorItem.Tier.TERRASTEEL, net.minecraft.world.item.ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> TERRASTEEL_MAGE_LEGGINGS = registerMageArmor("terrasteel_mage_leggings", MageArmorItem.Tier.TERRASTEEL, net.minecraft.world.item.ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> TERRASTEEL_MAGE_BOOTS = registerMageArmor("terrasteel_mage_boots", MageArmorItem.Tier.TERRASTEEL, net.minecraft.world.item.ArmorItem.Type.BOOTS);

    public static final RegistryObject<Item> GAIA_MAGE_HELMET = registerMageArmor("gaia_mage_helmet", MageArmorItem.Tier.GAIA, net.minecraft.world.item.ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> GAIA_MAGE_CHESTPLATE = registerMageArmor("gaia_mage_chestplate", MageArmorItem.Tier.GAIA, net.minecraft.world.item.ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> GAIA_MAGE_LEGGINGS = registerMageArmor("gaia_mage_leggings", MageArmorItem.Tier.GAIA, net.minecraft.world.item.ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> GAIA_MAGE_BOOTS = registerMageArmor("gaia_mage_boots", MageArmorItem.Tier.GAIA, net.minecraft.world.item.ArmorItem.Type.BOOTS);

    private static RegistryObject<Item> registerMageArmor(String name, MageArmorItem.Tier tier, net.minecraft.world.item.ArmorItem.Type type) {
        return ITEMS.register(name, () -> new MageArmorItem(tier, type, new Item.Properties()));
    }

    // Upgrade Orbs
    public static final RegistryObject<Item> ORB_OF_FLORA = ITEMS.register("orb_of_flora",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.typeKey("flora"), "flora", ChatFormatting.GREEN));

    public static final RegistryObject<Item> ORB_OF_THE_POOL = ITEMS.register("orb_of_the_pool",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.typeKey("pool"), "pool", ChatFormatting.BLUE));

    public static final RegistryObject<Item> ORB_OF_BURSTING = ITEMS.register("orb_of_bursting",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.typeKey("bursting"), "bursting", ChatFormatting.LIGHT_PURPLE));

    public static final RegistryObject<Item> ORB_OF_TERRAN_MIGHT = ITEMS.register("orb_of_terran_might",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.typeKey("terran"), "terran", ChatFormatting.GOLD));

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

    // Mana Inks (Phase 2B) — petal apothecary outputs that replace vanilla ink for ISS scroll forging.
    // These must extend ISS's InkItem: the Scroll Forge's ink slot filters on
    // `instanceof InkItem`, not on a tag, so a look-alike item is rejected outright.
    // The SpellRarity is what caps the scribable spell level, so it has to match the tooltip.
    public static final RegistryObject<Item> MINOR_MANA_INK = ITEMS.register("minor_mana_ink",
            () -> new ManaInkItem(SpellRarity.UNCOMMON, FluidRegistry.UNCOMMON_INK, new Item.Properties(),
                    "item.ironsbotany.minor_mana_ink.tooltip"));

    public static final RegistryObject<Item> GREATER_MANA_INK = ITEMS.register("greater_mana_ink",
            () -> new ManaInkItem(SpellRarity.RARE, FluidRegistry.RARE_INK, new Item.Properties(),
                    "item.ironsbotany.greater_mana_ink.tooltip"));

    public static final RegistryObject<Item> PRIME_MANA_INK = ITEMS.register("prime_mana_ink",
            () -> new ManaInkItem(SpellRarity.EPIC, FluidRegistry.EPIC_INK, new Item.Properties(),
                    "item.ironsbotany.prime_mana_ink.tooltip"));

    // School-tied upgrade orbs (Phase 2C) — runic altar outputs, one per ISS school's spell-power attribute
    public static final RegistryObject<Item> ORB_OF_FIRE_POWER = ITEMS.register("orb_of_fire_power",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.typeKey("fire"), "fire", ChatFormatting.RED));

    public static final RegistryObject<Item> ORB_OF_FROST_POWER = ITEMS.register("orb_of_frost_power",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.typeKey("frost"), "frost", ChatFormatting.AQUA));

    public static final RegistryObject<Item> ORB_OF_LIGHTNING_POWER = ITEMS.register("orb_of_lightning_power",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.typeKey("lightning"), "lightning", ChatFormatting.YELLOW));

    public static final RegistryObject<Item> ORB_OF_HOLY_POWER = ITEMS.register("orb_of_holy_power",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.typeKey("holy"), "holy", ChatFormatting.WHITE));

    public static final RegistryObject<Item> ORB_OF_ENDER_POWER = ITEMS.register("orb_of_ender_power",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.typeKey("ender"), "ender", ChatFormatting.DARK_PURPLE));

    public static final RegistryObject<Item> ORB_OF_BLOOD_POWER = ITEMS.register("orb_of_blood_power",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.typeKey("blood"), "blood", ChatFormatting.DARK_RED));

    public static final RegistryObject<Item> ORB_OF_NATURE_POWER = ITEMS.register("orb_of_nature_power",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.typeKey("nature"), "nature", ChatFormatting.DARK_GREEN));

    public static final RegistryObject<Item> ORB_OF_ELDRITCH_POWER = ITEMS.register("orb_of_eldritch_power",
            () -> new BotanicalUpgradeOrbItem(new Item.Properties().stacksTo(1), BotanicalUpgradeOrbItem.typeKey("eldritch"), "eldritch", ChatFormatting.DARK_AQUA));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
