# Changelog

All notable changes to Iron's Botany will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.1] - 2026-03-26

### Fixed
- **Upgrade Orbs now work in the Arcane Anvil** — `BotanicalUpgradeOrbItem` now extends ISS's `UpgradeOrbItem` with proper data-driven `UpgradeOrbType` registrations, so the Arcane Anvil recognizes them natively. Removed the old `UpgradeOrbHandler` that only worked with the vanilla anvil
- **Spell Petals now used in scroll crafting** — All 9 scroll forge recipes updated to use `ironsbotany:spell_petal` as the focus item instead of raw Botania petals, giving Spell Petals an actual purpose
- **Spell descriptions now appear in the ScrollForge GUI** — Added `.guide` translation keys for all 9 spells matching the key pattern ISS's ScrollForge expects
- **Missing item descriptions added to lang files** — Added tooltip keys for Livingwood Staff, Dreamwood Scepter, Gaia Spirit Wand, Mana-Infused Essence, Botanical Crystal, Spell Petal, and Chronicle of the Green Mage

### Changed
- Upgrade Orb tooltips and weapon tooltips now use `Component.translatable()` instead of hardcoded English strings, enabling proper localization
- Orb of Terran Might simplified to +5% Spell Power (ISS's `UpgradeOrbType` supports one attribute per type)
- All 23 language files updated with new tooltip, orb bonus, and `.guide` keys

### Removed
- `UpgradeOrbHandler.java` — replaced by ISS's native Arcane Anvil upgrade system

## [1.2.0] - 2026-03-26

### Added
- **Spell Reservoir right-click input** — Right-click a Spell Reservoir with any Botania mana-holding item to transfer mana into it. Converts Botania mana to ISS mana at the configured ratio
- **Comparator output** for Spell Reservoir and Mana Conduit — signal strength proportional to stored mana fill level (0-15)
- **Mana Conduit feeds adjacent Reservoirs** — ManaConduit now transfers ISS mana into any SpellReservoir placed directly adjacent to it
- **BotaniaIntegration utility** — New centralized utility class for direct Botania API access, replacing fragile reflection
- **DataKeys constants class** — All 32 `IronsBotany_*` NBT key strings centralized into a single constants class
- **Configurable block entity transfer rate** — New `blockEntityTransferRate` config option controls ISS mana per second from Conduit/Reservoir to players (default 5, range 1-100)
- **Config validation** — Logs a warning at startup when SEPARATE mana mode is active, alerting to extreme dual-cost implications
- **SpellContext damage cap** — New `capDamageMultiplier()` method on SpellContext; total damage multiplier from all sources capped at 5.0x
- **22 language translations** — Full localization for: Afrikaans, Arabic, Bengali, German, British English, Argentine Spanish, Spanish, Mexican Spanish, French, Hindi, Italian, Japanese, Korean, Dutch, Brazilian Portuguese, Russian, Turkish, Ukrainian, Vietnamese, Simplified Chinese, Traditional Chinese (HK), Traditional Chinese (TW)

### Fixed
- **BotanicalRing was cosmetic-only** — Tooltip claimed +25 Max Mana and +5% Spell Power but the ring applied zero attribute modifiers. Now properly implements Curios `getAttributeModifiers` with real stat bonuses
- **FlowerShield gave free HP** — Float-to-int truncation on shield damage meant fractional hits (e.g. 3.7 damage) only consumed 3 shield HP instead of 4. Fixed with `Math.ceil()`
- **Stage 4 Mana Events were entirely non-functional** — NBT tags written to Botania block entities were never read by anything. Disabled by default; `applyWaterFill` now uses direct ManaPool API (the one effect that can actually work)
- **Duplicate reflection for compile-time API** — Both ManaHelper and ManaConduitBlockEntity independently reflected on `ManaPool` when it's available at compile time. Replaced with direct `instanceof ManaPool` checks
- **String-based Botania class detection** — Replaced all `getSimpleName().contains("Pool")` / `getName().startsWith("vazkii.botania")` patterns with proper `instanceof` API checks via BotaniaIntegration
- **ManaTransferPacket was dead code** — Packet deserialized fields but `handle()` only played a sound. No code ever sent it. Removed entirely
- **SpellReservoir had no way to receive mana** — `addMana()` existed but nothing called it. Now reachable via right-click and adjacent ManaConduit feeding
- **TerrasteelSpellBlade tooltip lied about mana cost** — Tooltip said "-20% Mana Cost" but the attribute was `COOLDOWN_REDUCTION`. Corrected tooltip to "-20% Cooldown". Tooltip values now read from config dynamically
- **Living Root Grasp Slowness 255 was absolute freeze** — Reduced immobilize slowness from 255 (total paralysis, no counterplay) to 10 (~80% speed reduction, strong but breakable). Removed ineffective `setDeltaMovement(Vec3.ZERO)` call
- **Runic Infusion buff stacking was unbounded** — At level 10 with all 5 runes, Strength reached XI (+33 attack damage). Capped Strength at V, Speed at IV
- **Mana Rebirth stripped ALL effects on revival** — `removeAllEffects()` cleared beneficial effects too (Strength, Speed, Regen). Now only removes non-beneficial effects
- **SchoolMigrationHandler only migrated inventory** — Missed Ender Chest and Curios slots. Now migrates all three. Fixed lambda accumulation bug for Curios migration count
- **Armor and weapons were unrepairable** — Manasteel Wizard Armor and Terrasteel Spell Blade had `Ingredient.EMPTY` as repair material. Now repairable with Manasteel Ingots and Terrasteel Ingots respectively
- **TerrasteelBladeHandler swallowed exceptions** — `catch (Exception ignored)` silently discarded all mana operation failures. Now logs at debug level
- **DreamwoodScepter tooltip was vague** — "Converts ISS mana costs to Botania mana" now shows the actual configured percentage
- **SpellManaNetworkIntegration had dead inner class** — Duplicate `ActiveModification` class never used within the file. Removed
- **Misplaced damage type JSON** — `data/irons_spellbooks/damage_type/nature_magic.json` was at the project root, not in `src/main/resources/data/`. Moved to correct location so it's included in the built JAR
- **Gaia's Wrath Botania mana costs were prohibitive** — Reduced from 150,000 base + 50,000/level (600k at L10) to 80,000 base + 30,000/level (350k at L10)

### Changed
- **Non-Player entities can now cast botanical spells** — Restructured `AbstractBotanicalSpell.onCast()` so `executeBotanicalEffect()` runs for any LivingEntity. Player-specific systems (catalysts, auras, Alfheim, advancements, mana checks) are conditional
- **School migration: ironsbotany:botanical → irons_spellbooks:nature** — Custom Botanical school removed in favor of ISS's built-in Nature school. `SchoolMigrationHandler` rewrites old references on login
- `ENABLE_SPELL_MANA_EVENTS` now defaults to `false` (Stage 4 marked experimental)
- 14 files updated to use `DataKeys` constants instead of inline string literals

### Removed
- `ManaTransferPacket.java` (dead code)
- `IBSchools.java` (custom school replaced by ISS Nature school)
- Duplicate reflection code in ManaHelper and ManaConduitBlockEntity
- Dead `ActiveModification` inner class from SpellManaNetworkIntegration

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
