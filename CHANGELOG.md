# Changelog

All notable changes to Iron's Botany will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] - v1.5 scope

### Botany spell school (truth-in-advertising)

- **Added** `common/registry/IBSchools.java` registering a new `ironsbotany:botany`
  `SchoolType` via `DeferredRegister` against ISS's `SchoolRegistry.SCHOOL_REGISTRY_KEY`.
  The school reuses `AttributeRegistry.NATURE_SPELL_POWER` and
  `AttributeRegistry.NATURE_MAGIC_RESIST` in v1.5 so existing Manasteel Wizard armor,
  `orb_of_flora`, and `botanical_focus`/`botanical_ring` curios keep applying without
  migration. Dedicated `BOTANY_SPELL_POWER` / `BOTANY_MAGIC_RESIST` attributes are
  deferred to v1.6.
- **Added** `data/ironsbotany/damage_type/botany.json` and
  `data/ironsbotany/tags/items/botany_focus.json` (focus items:
  `ironsbotany:botanical_focus` plus optional `botania:mana_pearl`, `pixie_dust`,
  `livingwood_twig`, `dreamwood_twig`).
- **Fixed** dead `ENABLE_BOTANICAL_SCHOOL` config key — now actually read in
  `AbstractBotanicalSpell.getSchoolType()`. When `false`, spells fall back to
  ISS's Nature school for back-compat with existing saves.
- **Changed** all 9 spell classes (`ManaBloomSpell`, `BotanicalBurstSpell`,
  `FlowerShieldSpell`, `LivingRootGraspSpell`, `SparkSwarmSpell`, `RunicInfusionSpell`,
  `PetalStormSpell`, `GaiaWrathSpell`, `ManaRebirthSpell`) to declare
  `setSchoolResource(IBSchools.BOTANY_RESOURCE)` in their `DefaultConfig`.
- **Added** locale keys `school.ironsbotany.botany`, `death.attack.botany`,
  `death.attack.botany.player` to `en_us.json` and `en_gb.json`. Other 21 locales
  fall back to English — follow-up translation pass pending.

### Botania-native recipe progression

- **Added** 8 Botania recipe-type JSONs alongside the existing vanilla crafting
  recipes (kept as fallback):
  - `recipes/runic_altar/orb_of_flora.json` (Rune of Spring + Summer, 20k mana)
  - `recipes/runic_altar/orb_of_the_pool.json` (Rune of Mana + Water, 25k mana)
  - `recipes/runic_altar/orb_of_bursting.json` (Rune of Fire + Air, 20k mana)
  - `recipes/terra_plate/orb_of_terran_might.json` (Terrasteel agglomeration, 500k mana)
  - `recipes/petal_apothecary/spell_petal.json` (purple/magenta/pink/white petals)
  - `recipes/petal_apothecary/mana_infused_essence.json` (cyan/blue petals + dusts)
  - `recipes/mana_infusion/botanical_crystal.json` (amethyst shard + 10k mana)
  - `recipes/elven_trade/dreamwood_scepter.json` (Livingwood Staff + mana pearl + dreamwood log)

### Operator tools

- **Added** `/ironsbotany reload` and `/ironsbotany info` commands (op level 2).
  Re-validates config in place; useful when tweaking `config/ironsbotany-common.toml`
  mid-session. Implemented in `common/command/IBCommands.java`, wired via
  `RegisterCommandsEvent` in `IronsBotany.java`.

### Deferred to v1.6

- Dedicated `BOTANY_SPELL_POWER` / `BOTANY_MAGIC_RESIST` attributes (currently aliased
  to Nature attributes)
- Migration of hardcoded spell tuning to per-spell `SpellConfigParameter` entries
- Gametests covering HYBRID/SEPARATE mana modes and Ars 'n Spells deferral path
- Locale translations for the 21 non-English locales

## [1.4.1] - 2026-04-17

### Ars 'n' Spells soft-integration

- **Added** `common/compat/ArsNSpellsCompat.java` — reflective shim that
  detects Ars 'n' Spells at runtime (`ModList.isLoaded`) and probes
  `BridgeManager.getCurrentMode`, `BridgeManager.getBridge().getMaxMana`,
  and `AnsConfig.CONVERSION_RATE_IRON_TO_ARS`. Fail-soft — if ANS is
  absent, misversioned, or has renamed internals, each probe returns a
  neutral value and logs once.
- **Fixed** `DreamwoodConversionHandler` leaking Botania mana when the
  cast was cancelled by a higher-priority handler (cooldown, LP, etc.).
  Handler now runs at `EventPriority.LOW` and checks `event.isCanceled()`
  before draining Botania or pre-funding ISS mana.
- **Fixed** ISS mana clamp in `SpellReservoirBlockEntity` and
  `ManaConduitBlockEntity` when ANS is in `ARS_PRIMARY` mode —
  `player.getAttributeValue(MAX_MANA)` returns the ISS ceiling, but ANS
  redirects `MagicData.getMana()` to the Ars pool via mixin. Both block
  entities now use `ArsNSpellsCompat.getEffectiveMaxMana(player)`, which
  reads through ANS's bridge under `ARS_PRIMARY` and falls back to the
  ISS attribute otherwise.
- **Fixed** Dreamwood Scepter pre-fund amount under ANS `ARS_PRIMARY` —
  the ISS value handed to `magicData.addMana` is now scaled by
  `CONVERSION_RATE_IRON_TO_ARS` so the Botania→Ars effective rate
  matches what ANS expects.
- **Added** optional `ars_n_spells` dependency in `mods.toml` with
  `ordering="AFTER"` to stabilise mixin/subscriber registration order
  when both mods are present.
- **Added** `COMPAT-ARS-N-SPELLS.md` — full 5×5 mode-interaction matrix,
  caveats, and recommended combinations.

No mixins added, no hard dependency on Ars 'n' Spells, no change to
spell balance. Iron's Botany continues to build and run with ANS absent.

## [1.4.0] - 2026-04-17

### Visual Overhaul — Phase 1

- **Block Entity Renderers** — Spell Reservoir and Mana Conduit now render a
  floating, fill-scaled glow orb above the block. Orb intensity, scale, and
  alpha track the block's stored ISS mana in real time.
  - `SpellReservoirBER`: cyan-green halo + spinning core, subtle bob.
  - `ManaConduitBER`: violet-cyan halo + core + three orbital sparks
    (reuses the color-cycling / orbital trig pattern from SparkSwarmRenderer).
  - Both use `LightTexture.FULL_BRIGHT` + `RenderType.entityTranslucent` to
    stay bright in dark biomes without a custom shader pipeline.
  - Gated on `ClientConfig.enableManaParticles`.
- **Client-synced block entity state** — `SpellReservoirBlockEntity` and
  `ManaConduitBlockEntity` now implement `getUpdatePacket` /
  `getUpdateTag` and broadcast `sendBlockUpdated` on every mana mutation,
  so BERs (and any future client listeners) see live stored-mana values
  instead of stale NBT snapshots.
- **Spell particle signature** — Five spells gained Iron's Botany-flavored
  particles layered onto their existing vanilla VFX for visual cohesion:
  - `ManaBloomSpell` — petal bursts at each flower spawn.
  - `LivingRootGraspSpell` — botanical burst ring at target feet.
  - `RunicInfusionSpell` — mana transfer swirl on caster (scales with rune
    count).
  - `GaiaWrathSpell` — botanical burst on each target + petal storm column
    rising from caster.
  - `ManaRebirthSpell` — petal rebirth burst + rising mana transfer
    pillar.
- **Ambient block particles** — active `SpellReservoirBlockEntity` and
  `ManaConduitBlockEntity` now emit low-rate ambient wisps from their
  `serverTick` (~25–30% per second) while they hold mana, giving the
  blocks atmospheric presence beyond the BER orb. Uses existing particle
  types — no new assets.
- **Config gates** — `CommonConfig.enableSpellParticles` and
  `CommonConfig.enableBlockAmbientParticles` now actually govern
  server-side particle emission. `ClientConfig.enableSpellParticles` was
  dead code (client flag, server broadcast) and has been removed —
  existing configs with the key will log a warning once and be ignored.
- **HUD proximity pulse** — `ClientEventHandler` now scans a 6-block
  radius around the player every 20 ticks for active Spell Reservoirs /
  Mana Conduits and wraps the Botania mana bar in a cyan pulsing border
  while any are in range.
- **Block geometry** — `SpellReservoir` and `ManaConduit` replaced their
  `cube_all` models with hand-written multi-element geometry (pedestal +
  bowl rim, and pedestal + column + top cap, respectively). Reuses the
  existing block textures; collision stays a full cube.

## [1.3.3] - 2026-04-15

### Critical Fixes
- **Dedicated server crash fixed (second leak)** — `SpellCastSyncPacket` (a common-side packet class) held a `private static handleClient` method that directly referenced `net.minecraft.client.Minecraft`. When `PacketHandler.register()` ran inside `FMLCommonSetupEvent`, the JVM loaded `SpellCastSyncPacket` via the `consumerMainThread(SpellCastSyncPacket::handle)` method reference, and Forge's transforming classloader refused the client type on `DEDICATED_SERVER` with `BootstrapMethodError: Attempted to load class net/minecraft/client/multiplayer/ClientLevel for invalid dist DEDICATED_SERVER`. The existing `DistExecutor.unsafeRunWhenOn` guard did not prevent class loading because the client reference lived on the same common-side class.
  - Moved the client-only handler into a new `@OnlyIn(Dist.CLIENT)` class `com.ironsbotany.client.network.SpellCastClientHandler`.
  - Added public `getPos()` / `getSpellId()` accessors on `SpellCastSyncPacket` so the new client class can read packet state without exposing fields.
  - `SpellCastSyncPacket.handle()` now dispatches via the canonical Forge `() -> () -> SpellCastClientHandler.handleClient(packet)` double-lambda pattern, using a fully qualified class name so the client reference never appears in the common class's import table.
  - Verified: `./gradlew compileJava` clean; `grep` over `com.ironsbotany.common` returns zero `net.minecraft.client` / `net.minecraftforge.client` references.

## [1.3.2] - 2026-04-07

### Critical Fixes
- **Dedicated server crash fixed** — Four call sites in `BotanicalBurstProjectile` and `SparkSwarmEntity` called `Level.addParticle()` inside server-side `!isClientSide` blocks. Replaced with `ServerLevel.sendParticles()` which correctly sends particle packets to nearby clients
- **Memory leak in GaiaSpellTrials fixed** — Static `ACTIVE_TRIALS` HashMap was never cleaned up, causing unbounded memory growth on long-running servers. Added death event cleanup, 10-minute staleness purge, and server-stop clear
- **Botanical Focus and Ring can now be equipped** — Missing Curios slot tag files prevented equipping despite `ICurioItem` implementation. Added `data/curios/tags/items/charm.json` (focus) and `ring.json` (ring)

### Bug Fixes
- **Null safety in FlowerAura.appliesTo()** — `HeiseiDreamAura`, `RannuncarpusAura`, and `JadedAmaranthusAura` now guard against null spell parameter (BellethorneAura already had this)
- **SpellDrivenAutomation API safety** — `ManaSpreader.commitRedirection()` call wrapped in try-catch to handle Botania API version differences gracefully
- **GaiaSpellTrials spell lookup hardened** — `SpellRegistry.getSpell()` results now checked against both null and `SpellRegistry.none()` before use
- **SparkSwarmEntity particle throttle** — Client-side ambient particles now spawn every other tick instead of every tick, preventing FPS degradation with multiple swarms active

### Visual Overhaul
- **Custom particle behaviors** — All three particle classes rewritten with distinct character:
  - `ManaTransferParticle`: size grow/shrink animation, blue-to-purple gradient, quadratic alpha easing, gentle rotation
  - `BotanicalBurstParticle`: starts large and rapidly shrinks, green-to-gold color shift, upward drift, 12-tick lifetime
  - `PetalMagicParticle`: flutter rotation, sinusoidal horizontal drift, size pulse, pink-to-coral color shift
- **Custom particle textures** — Created 12 original 16×16 particle textures replacing vanilla sprite references: soft glow gradients (mana), star/diamond shapes (bursts), petal silhouettes (spells)
- **Emissive entity rendering** — `BotanicalBurstRenderer` and `SparkSwarmRenderer` upgraded with fullbright lighting (`LightTexture.FULL_BRIGHT`), dual-layer glow rendering, pulsing scale animation, and Z-axis spin
- **SparkSwarmRenderer orbital sparks** — 3 orbiting spark quads with trig-based positioning and color cycling for a true "swarm" visual
- **Interpolated projectile trail** — `BotanicalBurstProjectile` trail replaced from random scattered dots to a smooth 4-step interpolated chain with spiral offset perpendicular to velocity
- **Mod-wide custom particle usage** — Replaced vanilla `CHERRY_LEAVES` and `ELECTRIC_SPARK` with `IBParticles.BOTANICAL_BURST`, `PETAL_MAGIC`, and `MANA_TRANSFER` across all entities, spells, event handlers, and casting channels

### Improved
- Event priority ordering documented — `FlowerShieldHandler` (HIGH) processes before `ArmorSetBonusHandler` (NORMAL) with comments explaining intended stacking
- `ManaHelper.getManaAccesories()` call annotated as Botania API's spelling
- Orphaned `scroll_botanical.json` model and `scroll_botanical.png` texture removed (no corresponding item registration)

## [1.3.0] - 2026-03-27

### Critical Fixes
- **Dependencies now mandatory** — Botania, Iron's Spells 'n Spellbooks, and Curios are declared `mandatory=true` in `mods.toml`, matching actual hard requirements. Version ranges tightened to tested baselines
- **Catalyst registration completely rewritten** — Replaced all `Class.forName` reflection with `ForgeRegistries.ITEMS.getValue()` registry lookups. Fixed stale Botania field names: `lensVelocity`→`lens_speed`, `lensBore`→`lens_mine`, `gaiaSpirit`→`gaia_ingot`, `terrasteel`→`terrasteel_ingot`. Catalysts now register reliably
- **Corporea reagent duplication fixed** — `SpellCircleReagentSystem` was adding items to player inventory AND dropping them as world entities. Refactored to use new `extractFromCorporea()` that returns stacks without inventory insertion. `autoPlaceComponents()` now correctly returns `false` when reagents are unavailable
- **Mana deletion eliminated in all conversion paths** — ManaConduit, SpellReservoir, and ManaHelper now verify conversion yields a result before draining Botania mana. Integer division remainder is preserved by only draining the exact amount that converts cleanly. `SpellReservoirBlockEntity.addMana()` now returns the accepted amount so the conduit only subtracts what was actually inserted
- **Stage 4 non-functional NBT writes removed** — Only the WATER trigger (direct `pool.receiveMana()`) was functional; all other triggers wrote NBT tags that Botania never reads. Removed dead NBT writes for LIGHTNING, EARTH, NATURE, FIRE, WIND, and ARCANE. Expensive cuboid scan now skipped for non-WATER triggers

### Compatibility
- **Bellethorn aura typo fixed** — `FlowerAuraRegistration` used `"botania:bellethorne"` (wrong) instead of `"botania:bellethorn"`. Aura now registers correctly
- **Alfheim integration redesigned** — Replaced nonexistent `botania:alfheim` dimension check with Alfheim portal block proximity detection. Boost now scales by distance to nearest portal (full at 0, fading at 16 blocks)
- **Patchouli guidebook now opens** — Chronicle of the Green Mage right-click now calls `PatchouliAPI.get().openBookGUI()`. Falls back gracefully if Patchouli is absent
- **Broken `runData` target removed** — No data providers exist; the Gradle data task has been commented out with a TODO

### Integration Fixes
- **Casting channels wired into spell execution** — `CastingChannelRegistry` was fully implemented but never called. Channels now apply damage, cooldown, and casting speed modifiers during `AbstractBotanicalSpell.onCast()`
- **SpellContext additional effects now applied** — Catalyst/aura-granted `MobEffectInstance` effects from `context.getAdditionalEffects()` are applied to the caster after spell execution
- **ACTIVE_AURAS flag now written** — `AbstractBotanicalSpell` writes `DataKeys.ACTIVE_AURAS` to player persistent data after scanning for auras, enabling Gaia trial Phase 1
- **Gaia trial Phase 1 aura check fixed** — Now queries `FlowerAuraRegistry.getActiveAuras()` directly instead of reading a stale NBT flag that was never written
- **Mana payment unified with HUD** — `ManaHelper.hasBotaniaMana()`/`drainBotaniaMana()` now use `ManaItemHandler.getManaItems()`/`getManaAccesories()` to include Curios-slot mana items, matching what the mana HUD displays
- **BotanicalFocusItem fully functional** — Added right-click siphon toggle (`use()` override), Curios attribute modifiers (+50 Max Mana, +10% Mana Regen), and fixed `curioTick()` to siphon from player's inventory mana items instead of the focus stack itself. Rate-limited to once per second
- **Projectile pierce implemented** — `BotanicalBurstProjectile` now reads `max_pierce`/`piercing` from persistent data. Piercing projectiles track hit entities, apply 20% damage reduction per pierce, and only discard after reaching max pierce count
- **Spark swarm follows owner** — `SparkSwarmEntity` now uses stored `ownerUUID` for follow-owner behavior: teleports if >16 blocks away, navigates toward owner if >4 blocks, targets only monsters within 8 blocks of owner. Wander goal reduced to fallback priority
- **Rune Scroll Fusion uses item tags** — Replaced fragile `toString().contains("scroll")`/`contains("rune")` matching with `ironsbotany:spell_scrolls` and `ironsbotany:botania_runes` item tags. Hardcoded English " (Rune Enhanced)" suffix replaced with translatable component
- **ManaNetworkModifier dimension-safe** — Map key changed from `BlockPos` to `DimBlockPos(ResourceKey<Level>, BlockPos)` record. Modifications now only apply to the correct dimension
- **All remaining reflection eliminated** — `SpellDrivenAutomation` no longer uses `Class.forName` for `ManaBurstEntity`; uses direct entity iteration with `instanceof ManaBurst`

### Balance
- **Gaia's Wrath particle density reduced ~85%** — Shockwave loop cut from 100 angles × full radius to 36 angles × half radius steps. Explosion emitter count reduced from 5 to 3
- **Flower Shield single-axis** — Removed Resistance effect stacking. Shield is now purely a petal HP absorption buffer; breaking it returns the player to full damage exposure
- **Manasteel armor set bonus has cooldown** — Added 40-tick (2-second) internal cooldown to the 50% mana shield, preventing permanent damage reduction
- **TerrasteelSpellBlade naming fixed** — Renamed `MANA_COST_UUID` to `COOLDOWN_UUID` to match the actual `COOLDOWN_REDUCTION` attribute

### Added
- `BotanicalGrimoireItem.java` — Dedicated item class for the Chronicle of the Green Mage with Patchouli integration
- `data/ironsbotany/tags/items/spell_scrolls.json` — Item tag for ISS spell scrolls
- `data/ironsbotany/tags/items/botania_runes.json` — Item tag for all 16 Botania runes
- Right-click info display on Spell Reservoir and Mana Conduit (shows stored mana / capacity)
- Comparator output notifications — blocks now call `updateNeighbourForOutputSignal()` when stored mana changes
- New lang keys: block info display, siphon mode toggle, rune enhanced suffix, updated Alfheim advancement description

### Improved
- Swallowed `catch (Exception ignored)` in `SpellCatalystRegistry` now logs at debug level
- `SpellReservoirBlock` exception handler logs at debug instead of silently swallowing

## [1.2.1] - 2026-03-26

### Fixed
- **Upgrade Orbs now work in the Arcane Anvil** — `BotanicalUpgradeOrbItem` now extends ISS's `UpgradeOrbItem` with proper data-driven `UpgradeOrbType` registrations, so the Arcane Anvil recognizes them natively. Removed the old `UpgradeOrbHandler` that only worked with the vanilla anvil
- **Spell Petals now used in scroll crafting** — All 9 scroll forge recipes updated to use `ironsbotany:spell_petal` as the focus item instead of raw Botania petals, giving Spell Petals an actual purpose
- **Spell descriptions now appear in the ScrollForge GUI** — Added `.guide` translation keys for all 9 spells matching the key pattern ISS's ScrollForge expects
- **Missing item descriptions added to lang files** — Added tooltip keys for Livingwood Staff, Dreamwood Scepter, Gaia Spirit Wand, Mana-Infused Essence, Botanical Crystal, Spell Petal, and Chronicle of the Green Mage
- **Item tooltips for crafting components and guidebook** — Mana-Infused Essence, Botanical Crystal, Spell Petal, and Chronicle of the Green Mage now display description tooltips when hovered in inventory

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
