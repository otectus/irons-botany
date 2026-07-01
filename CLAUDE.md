# Iron's Botany — Forge 1.20.1 Mod

## Quick Reference
- **Mod ID**: `ironsbotany`
- **Package**: `com.ironsbotany`
- **Version**: 1.9.0 (targets Iron's Spells 3.16+)
- **MC**: 1.20.1 | **Forge**: 47.4.16 | **Java**: 17
- **Mappings**: Official

## Build
- `./gradlew build` — full build
- `./gradlew compileJava` — compile-only
- `./gradlew runData` — regenerate datagen output under `src/generated/resources/`

## Roadmap
- v1.5 (released) — bridge architecture, Botany SchoolType, Arcane Mana Altar
- v1.6 (in development) — endgame items: Manasteel Staff, Terrasteel Spellbook, Arcane Codex, Elementium Scroll, 3 new curios, datagen revival
- v2.0 (planned) — see [`PHASE_7_PLAN.md`](PHASE_7_PLAN.md): generating flowers, Verdant Caster (functional flower casts ISS spells), Corporea Scroll Rack, KubeJS surface

## Project Structure
- `api/` — public API surface (stable across minor versions)
  - `IronsBotanyApi`, `IManaSource`, `BotanySchoolFlowerRegistry`
- `client/` — rendering, particles, client logic
  - `particle/` — custom particle effects
  - `renderer/` — custom renderers
- `common/` — shared/server-side logic
  - `alfheim/` — Alfheim dimension integration
  - `api/` — public API package (mirror of root `api/` for downstream addons)
  - `automation/` — automation mechanics
  - `block/` + `block/entity/` — custom blocks and block entities
  - `boss/` — Gaia Spell Trials boss mechanics
  - `bridge/` — `ManaBridgeManager`, `ManaResolutionResult`, `CostRoutedTag`, `ManaPriorityChain`, `ManaSource`
  - `casting/` + `casting/channels/` — spell casting and channel system
  - `command/` — `/irons_botany reload` and other ops commands
  - `compat/` — Ars 'n Spells reflective compat (`ArsNSpellsCompat`)
  - `config/` — mod configuration
  - `event/` — Forge event subscribers (incl. `SpellEventHandlers`, `IBCapabilityHandler`)
  - `item/cap/` — `ItemManaStorage` + capability provider for mana network items
  - `loot/` — global loot modifier (`AddItemLootModifier`, `IBLootModifiers`)
  - `spell/config/` — `BotanySpellConfig` per-spell SpellConfigParameters
- `datagen/` — `IBDatagen` + provider stack (recipe / item-model / blockstate / lang / loot)
- `src/generated/resources/` — datagen output (currently disabled)
- `libs/` — local dependency JARs (compileOnly)

## Key Dependencies
- **Botania** 1.20.1-450+ (required, local JAR)
- **Iron's Spells 'n Spellbooks** 3.16+ (required, local JAR; 3.16 changed the `Scroll` constructor, so 3.15.2 is no longer supported)
- **Curios** 5.14.1+ (required, local JAR)
- **Patchouli** 84+ (optional, local JAR)
- **Player Animation Lib** (optional, local JAR)
- **Ars 'n Spells** (optional soft-integration via reflection, no classpath dep)

## Architecture — Bridge

All spell cost routing goes through `ManaBridgeManager.resolveCost(player, spell, level, source)`,
called from `SpellEventHandlers` at `EventPriority.LOW` on `SpellPreCastEvent`. The bridge
respects the 5-mode `ManaUnificationMode`, defers to Ars 'n Spells when present, and uses
`CostRoutedTag` (player persistent data) for tick-scoped idempotency. See
`ManaPriorityChain` for the configurable resource ordering.

`AbstractBotanicalSpell.onCast()` no longer routes cost inline — by the time the spell
enters `onCast`, the bridge has already paid the price (or cancelled the cast).

## School Registration

The custom **Botany** SchoolType is registered in `IBSchools` against `SchoolRegistry.SCHOOL_REGISTRY_KEY`,
with focus tag `#ironsbotany:focus/botany`, attributes `BOTANY_SPELL_POWER` / `BOTANY_MAGIC_RESIST`,
and damage type at `data/ironsbotany/damage_type/botany.json`. Botanical spells return
`IBSchools.BOTANY.get()` from `getSchoolType()`.

## Per-Spell SpellConfigParameter

`BotanySpellConfig` registers `botania_mana_cost` (Integer) and `dual_cost_enabled` (Boolean)
per-spell parameters via `RegisterConfigParametersEvent`. Server operators override these through
ISS's own spell-config datapack mechanism (its `SpellConfigManager` subconfig folder), keyed by
the parameter ids. The default sentinel of `-1` for cost means "fall back to the
constructor-supplied ladder." Reads are guarded against ISS's `SpellConfigManager` not yet being
initialized (client-side pre-sync).

## Mana Network Citizenship

Iron's Botany items (Livingwood Staff, Botanical Focus, Botanical Ring, etc.) attach a
`MANA_ITEM` capability via `IBCapabilityHandler` (`AttachCapabilitiesEvent<ItemStack>`).
Storage is per-stack NBT under the `ironsbotany_mana` key, capped per item via `capacityFor`
(each cap now config-driven under the "Mana Network Capacities" config group). The Livingwood
Staff's own tooltip/bar/drain logic reads/writes that same `ironsbotany_mana` key, so the item
and the network share one value.

The **Arcane Mana Altar** (`ArcaneManaAltarBlockEntity`) implements `ManaPool`,
`ManaReceiver`, and `SparkAttachable` simultaneously (its cap is config-driven via
`arcaneAltarMaxMana`). Because it is a `ManaPool`, a player casting in range draws from it
through the normal Botania pool path (`ManaHelper.findAndDrainPool`, gated by
`enableManaPoolAccess`) — no separate altar scan is needed.

## Conventions
- Registration: DeferredRegister on MOD bus
- Dependencies via local JARs in `libs/` folder (compileOnly, not deobfed)
- All persistent NBT keys namespaced as `ironsbotany_<key>` (underscore-separated, no colons)
- Event subscribers explicit about priority (`@SubscribeEvent(priority = ...)`) for any ISS hook
- Public API in `com.ironsbotany.api.*` is stable across minor versions; `com.ironsbotany.common.*` is internal
- License: MIT
