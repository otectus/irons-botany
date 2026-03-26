# Changelog

All notable changes to Iron's Botany will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.2] - 2026-03-25

### Added
- **Spell descriptions** — All 9 Botanical spells now show descriptions in the spellbook and scroll tooltips via `getUniqueInfo()` override
- **Device tooltips** — Spell Reservoir and Mana Conduit blocks now display description tooltips when hovered in inventory
- **Spell icon textures** — 9 unique 16x16 spell icons at `textures/gui/spell_icons/` (mana_bloom, botanical_burst, flower_shield, living_root_grasp, spark_swarm, runic_infusion, petal_storm, gaia_wrath, mana_rebirth)
- **Botanical scroll texture** — `scroll_botanical.png` and corresponding item model for the Botanical school's scroll appearance
- 10 new localization keys for spell descriptions and the Spell Reservoir tooltip

### Fixed
- Removed broken custom texture references (`book_texture`, `filler_texture`, `crafting_texture`) from Patchouli book.json — the Chronicle of the Green Mage now uses Patchouli's built-in defaults instead of missing textures

## [1.1.1] - 2026-03-25

### Fixed
- Fixed crash on Forge 47.4.10: "Unable to have damage AND stack" caused by LivingwoodStaffItem, DreamwoodScepterItem, and GaiaSpiritWandItem constructors redundantly calling `.stacksTo(1)` on properties that already had durability set

## [1.1.0] - 2026-03-25

### Added
- **Mana Conduit block** — Place adjacent to a Botania mana pool to convert pool mana → ISS mana for nearby players. Configurable radius, conversion rate, and capacity
- **Direct mana pool access** — Spells can now draw Botania mana directly from nearby mana pools as a fallback when inventory mana is insufficient. Configurable search radius
- **9 new advancements** — Full Garden, Master Botanist, Battle Mage, Living Barrier, Augmented Casting, Gaia's Blessing, Garden of Power, Supply Chain Magic, Otherworldly Power. Makes deep synergy systems discoverable
- **Patchouli guidebook expansion** — 15 new entries: individual pages for all 9 spells, catalyst reference, flower aura guide, casting channel profiles, and advanced systems overview
- **Vanilla loot table injection** — Mod items now appear in village, mineshaft, stronghold, and end city chests (configurable)
- Config: `enableManaPoolAccess`, `manaPoolSearchRadius`, `manaConduitCapacity`, `manaConduitConversionRate`, `manaConduitRadius`, `enableVanillaLootInjection`

## [1.0.1] - 2026-03-25

### Added
- Catalyst consumption: catalysts can now be consumed or damaged on use (controlled by `catalystConsumptionChance` and `catalystDurabilityDamage` config options)
- Spellbook attunement bonuses now apply during spell casting (cooldown reduction, mana cost reduction, spell power bonus based on attunement level)
- First Bloom advancement now triggers correctly when casting any Botanical spell
- Spell cast visual sync: nearby players now see cherry leaf particles when someone casts a Botanical spell

### Fixed
- Added missing `minecraft:mineable/pickaxe` block tag for Spell Reservoir (now properly mineable with pickaxes)
- Fixed catalyst count in documentation (9 catalysts, not 8)
- Fixed entity texture count in changelog (was incorrect)
- Removed broken `logoFile` reference from mods.toml (file did not exist)
- Differentiated particle colors: botanical_burst now renders green/gold, petal_magic now renders pink/magenta (previously all three particles shared identical blue-purple coloring)

### Changed
- Upgraded entity textures: botanical_burst (32x32 green mana orb with energy rays), spark_swarm (32x32 electric spark cluster)

### Removed
- Elven Communion spell, ElvenAllyEntity, and all related assets (renderer, texture, scroll recipe, config options, lang entry)
- Unused GeckoLib dependency from build.gradle

## [1.0.0] - 2026-03-24

Initial release of Iron's Botany for Minecraft Forge 1.20.1.

### Added

#### Core Systems
- Mana Unification System with 5 configurable modes (Hybrid, Botania Primary, ISS Primary, Separate, Disabled)
- Bidirectional mana conversion between Botania mana and ISS mana at configurable ratios
- Botanical spell school with custom attributes (Botanical Spell Power, Botanical Resistance, Mana Efficiency)
- ManaHelper utility for cross-system mana operations
- Master toggle system: bare-bones mode and deep synergy master switch

#### Spells (9)
- Mana Bloom (Lv 1-5) — spawns mystical generating flowers
- Botanical Burst (Lv 1-8) — fires damaging mana burst projectiles
- Flower Shield (Lv 1-10) — creates protective petal barrier with absorption
- Living Root Grasp (Lv 1-6) — roots and binds enemies with vines
- Spark Swarm (Lv 1-7) — summons combat Spark entities
- Runic Infusion (Lv 1-10) — buffs spell power based on held Botania runes
- Petal Storm (Lv 1-5) — AoE petal projectile attack
- Gaia's Wrath (Lv 1-10) — legendary AoE damage spell (180s cooldown)
- Mana Rebirth (Lv 1-5) — resurrection with Black Lotus effect

#### Equipment
- Terrasteel Spell Blade (+25% spell power, +200 max mana, -20% mana cost, attacks generate Botania mana)
- Livingwood Staff (+10% Botanical spell power, stores 500k Botania mana)
- Dreamwood Scepter (+20% Botanical spell power, converts ISS mana cost to Botania)
- Gaia Spirit Wand (+30% Botanical spell power, -25% cooldowns)
- Manasteel Wizard Armor set (helmet, chestplate, leggings, boots) with 4-piece Mana Shield set bonus
- Botanical Focus curio (passive mana conversion with toggleable Siphon Mode)
- Botanical Ring curio (botanical spellcasting enhancement)
- 4 Upgrade Orbs (Flora, Pool, Bursting, Terran Might) for Arcane Anvil

#### Blocks
- Spell Reservoir — stores and distributes ISS mana to nearby players

#### Deep Synergy Stage 1: Spell Catalysts
- 5 Rune Catalysts (Fire, Water, Earth, Air, Mana) modifying spell behavior from inventory
- 2 Lens Catalysts (Velocity, Bore) for projectile modifications
- 2 Material Catalysts (Terrasteel, Gaia Spirit) for power bonuses
- Configurable consumption chance, stacking, and power multiplier

#### Deep Synergy Stage 2: Casting Channels
- Livingwood Staff Channel (casting speed focus)
- Dreamwood Focus Channel (mana regeneration focus)
- Terra Rod Channel (burst damage focus)

#### Deep Synergy Stage 3: Flower Auras
- Bellethorne Aura (thorns/retaliation damage)
- Jaded Amaranthus Aura
- Heisei Dream Aura
- Rannuncarpus Aura (ritual automation, casting speed, component auto-placement)
- Configurable range/strength multipliers, stacking limit, and particle effects

#### Deep Synergy Stage 4: Spell-Triggered Mana Events
- 8 trigger types (Lightning, Earth, Nature, Fire, Water, Wind, Botanical, Arcane)
- Distance-based intensity scaling
- Configurable duration, intensity multiplier, and effect radius

#### Deep Synergy Stage 5: Corporea Logistics
- Automatic reagent requests from Corporea networks for level 5+ spells
- School-to-rune mapping for reagent resolution
- Configurable search radius and auto-request toggle

#### Deep Synergy Stage 6: Alfheim Integration
- Spell power multiplier when casting in Alfheim dimension
- Dual-school scroll crafting in Alfheim
- Spellbook attunement system with level-based bonuses
- Alfheim-crafted scroll tracking

#### Boss Integration
- Gaia Guardian Spell Trials (Phase 1: environmental magic, Phase 2: school counters)
- Botanical school vulnerability bonus against Gaia Guardian

#### Entities
- BotanicalBurstProjectile (spell projectile)
- SparkSwarmEntity (summoned combat entity)

#### Recipes
- 32 crafting recipes covering all equipment, spells, components, and blocks
- Rune Scroll Fusion custom recipe type

#### Configuration
- 70+ configurable options across 11 categories
- Common config (server-side) and Client config (HUD, particles)

#### Assets
- 18 item textures, 1 block texture, 2 entity textures
- 2 armor layer textures (64x64, battle mage style)
- 3 particle definitions (botanical burst, mana transfer, petal magic)
- 4 sound events (mana conversion, botanical cast, flower bloom, spark summon)
- 128 localization keys (English)
- 3 advancements (Natural Awakening, First Bloom, Terran Ascension)
- Patchouli guidebook: Chronicle of the Green Mage

#### Documentation
- In-game Patchouli guidebook with 3 categories and 5 entries
- Loot table injection for dungeon loot
- Item and block tags for botanical equipment and upgrade orbs
