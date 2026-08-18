# Changelog

All notable changes to Iron's Botany will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] — v2.0 planning

The forward-looking v2.0 roadmap is captured in
[`PHASE_7_PLAN.md`](PHASE_7_PLAN.md) at the project root. Headline features:
six ISS-school generating flowers (mana feedback from spell casts), the Verdant
Caster (a functional flower that casts ISS spells autonomously — the v2.0
signature), Corporea Scroll Rack with spell-aware request matcher, and a
KubeJS plugin surface for downstream addons.

## [1.11.0] - Client HUD, transfer fairness & data hardening

Completes the P2 performance and lifecycle work deferred from 1.10.0. Includes everything in
1.10.0; the upgrade guidance in [`docs/UPGRADING-1.10.0.md`](docs/UPGRADING-1.10.0.md) still applies.

### Fixed

- **The mana HUD ran roughly fifteen times per frame.** `RenderGuiOverlayEvent.Post` fires once per
  registered overlay and the handler never checked which one, so the full Botania capability sweep
  and the bar draw repeated for every overlay in the game. It now draws once, on the hotbar overlay.
- **The HUD double-counted mana.** Botania's item and accessory lists can surface the same physical
  stack; both totals added it twice. Deduplicated by stack identity.
- **The HUD recomputed capabilities every frame** to display a value that changes at most twenty
  times a second. Totals are now cached and refreshed at most five times a second.
- **`hudScale` was read from the config and never applied.** It is applied now, and the bar is kept
  fully on screen at its drawn size so a large offset or a small window cannot push it out of view.
- **The HUD fill was not clamped and summed into an `int`.** A stack reporting more mana than its
  capacity drew past the end of the bar, and a large enough inventory could overflow the total to a
  negative and invert it. Per-item values are clamped into their own capacity and totals accumulate
  in `long`.
- **Spell Reservoirs and Mana Conduits gave *each* nearby player a full transfer rate every tick,**
  so the configured block rate was silently multiplied by the number of players standing nearby.
  There is now one budget per tick, divided fairly, with the remainder rotated by the world clock so
  no player is permanently favoured. Service order is by UUID rather than entity-list order.
- **Block entities sent update packets for transfers that moved nothing** — once per nearby player
  per tick, and on every `drainMana` call including no-ops. They now sync once, and only when the
  stored value actually changed.
- **Stored item mana was not clamped below zero, and `addMana` could overflow.** Malformed NBT
  reporting negative mana subtracted from payment plans; a large enough `addMana` wrapped negative
  and *zeroed* the item. Both are computed in `long` and clamped.
- **Mana-network modifications expired against the overworld's clock** regardless of which dimension
  they belonged to, and their static map was never cleared on server stop, so a single-player
  session that loaded a different save inherited the previous world's pending modifications.
- **The HUD's proximity indicator leaked across worlds.** Its static state was never reset on
  disconnect, so a freshly joined world inherited the previous one's pulse until the next rescan.

### Changed

- The HUD proximity check iterates the block-entity maps of nearby chunks instead of walking 2,197
  block positions, and skips chunks the client has not received.
- New client option **`hudPulseNearby`** (default on). Turning it off keeps the nearby-block
  indicator but draws it steady instead of animating — an accessibility option for motion
  sensitivity.
- `particleDensity` is labelled `[NOT IMPLEMENTED]` in the generated client config. Iron's Botany
  spawns particles server-side via `sendParticles`, which chooses the count before the packet is
  sent, so a client setting cannot influence it. Video Settings → Particles is the setting that
  works, and the vanilla particle engine already honours it.

### Added

- `TransferBudget`, extracted so the fair-share arithmetic is unit-testable without a world, with
  tests asserting the whole budget is distributed exactly once for every budget/recipient
  combination up to 64×8.
- 8 more tests (85 total).

## [1.10.0] - Stability, compatibility & correctness

Fixes the crash that made 1.9.0 unusable alongside ISS-Scroll Descriptions and ISS-Restrictions,
rebuilds mana routing as a single transactional path, and makes a dozen advertised features that
did nothing actually do it. Read [`docs/UPGRADING-1.10.0.md`](docs/UPGRADING-1.10.0.md) before
upgrading; back up your world.

### Fixed — crashes and data loss

- **Crash when a scroll add-on inspects a Botany spell.** The Botany school's display name carried
  no colour, and Iron's Spells' `SchoolType#getTargetingColor()` dereferences that colour with no
  null check. Iron's Spells' own `AbstractSpell` and roughly twenty further ISS classes call it, so
  this was never add-on-only. Spell icons appearing "missing" was the same bug — the tooltip aborted
  before drawing them. The icons were always present and valid; all 86 textures in the 1.9.0
  artifact decode.
- **Botania payment could never succeed with a single mana item, and over-counted with three or
  more.** `ManaItemHandler.requestMana(stack, ...)` excludes the stack passed to it and searches
  everything else; it was being used as a per-source query. The over-count let an unaffordable cast
  pass the check, drain part of the player's mana, and then be refused anyway.
- **A failed cast could cost mana and catalysts.** Mana was charged during `SpellPreCastEvent`,
  catalysts were consumed near the top of `onCast`, and the reagent check that aborts the cast ran
  after both.
- **Same-tick casts could inherit each other's payment state,** and an unrelated mana debit could
  absorb a refund, because routing was keyed on `(tick, spellId.hashCode(), cost)` with tick-only
  payment flags and refunds applied through `ChangeManaEvent` — an event carrying no spell identity.
- **Mana-generating weapons credited the wrong item.** They called `requestManaExact` with a
  negative amount, relying on arithmetic that adds mana as a side effect, to an item other than the
  one iterated, and fired particles without proving anything was accepted.

### Fixed — features that did nothing

- **Botany spell power and resistance** were registered but attached to no entity, so every Botany
  bonus on every item was silently inert.
- **Mana efficiency** had a maximum equal to its default, so every item's modifier was clamped away
  — and no cost path read it.
- **All eight school upgrade orbs** shared four orb types, so each applied another orb's attribute
  and returned another orb when consumed; the tooltip had no case for any of them.
- **Orb of Flora** boosted ISS Nature rather than this mod's own Botany school.
- **Rune-scroll fusion** consumed a rune to write NBT nothing read.
- **Livingwood Staff, Dreamwood Scepter and Daybloom Amulet** granted ISS Nature power only, which
  no spell in this mod reads. They are now dual-school.
- **Gaia's Blessing** charged a mana pool from a level *query*, so opening a spell book could cost
  mana.

### Changed

- `/irons_botany reload` is now `/irons_botany flushcaches`; it never reloaded configuration. New
  `/irons_botany diagnose` reports school metadata for every registered spell.
- Botanical Ring's tooltip now says "All Spell Power", which is what it has always granted.
- The flower-aura cache is keyed by player *and radius*, validated against dimension and position,
  and its `maxActiveAuras` truncation ranks by strength and distance instead of block-iteration
  order. Aura and mana-pool scans skip unloaded chunks instead of loading them.
- Twelve config settings that never had an effect are labelled as such in the generated config.
- `updateJSONURL` points at a real Forge update manifest instead of an HTML releases page.

### Added

- A `validateArtifact` build gate that decodes every PNG in the published JAR, checks spell-icon
  dimensions, resolves every asset reference inside the archive, compares language files against
  `en_us`, and prints the artifact SHA-256 and size.
- A runtime spell integrity check that runs on every resource reload against the live spell registry.
- 77 unit and contract tests where there were none.
- `runeScrollManaDiscount` config key.
- `docs/INVESTIGATION-1.10.0.md`, `docs/SYSTEM-DECISIONS-1.10.0.md`, `docs/UPGRADING-1.10.0.md`.

### Known limitations

The reported purple/black texture was **not reproduced and has no confirmed root cause**. Dual-school
scroll NBT, `SpellDrivenAutomation`, the spell-triggered mana-network events and the progression
unlock flags remain inert and default to off. See `docs/SYSTEM-DECISIONS-1.10.0.md`.

## [1.9.0] - Stability, correctness & the school-migration finish line

A full-codebase audit pass. Two advertised features that never actually worked now do,
the half-finished NATURE→BOTANY school migration is completed across every subsystem, and
a batch of correctness/duplication/performance bugs are fixed. Item and registry IDs are
unchanged — **saves are not broken**. Still targets **Iron's Spells 'n Spellbooks 3.16+**.

### Fixed — behavior features that were inert

- **BOTANIA_PRIMARY no longer double-charges.** The mode drained Botania mana but never
  cancelled ISS's own debit, so casting in `BOTANIA_PRIMARY` charged the player *both*
  pools. `ChangeManaEvent` handling now refunds the ISS debit for any cast Botania paid
  for (BOTANIA_PRIMARY and Elementium Scroll), tracked via a per-tick "Botania-paid"
  marker. HYBRID/SEPARATE dual-cost casts intentionally still pay both.
- **Elementium Scroll "Botania pays, scroll retained" now triggers.** Scroll spells report
  0 ISS mana cost, so the old `issCost × ratio` Botania price was always 0 and the scroll
  was consumed the vanilla way every time. The scroll now draws a configurable flat cost
  (`elementiumScrollManaCost`, default 10 000) and is retained when Botania covers it.
- **Casting-channel mana-cost modifiers now apply.** Livingwood Staff (−10%), Dreamwood
  Focus (+10%) and Terra Rod (+30%) advertised a mana-cost modifier that was never
  consumed. The channel's multiplier is now folded into the Botania cost the bridge
  computes.

### Fixed — school migration (NATURE → BOTANY)

Botanical spells report the custom **Botany** school, but several subsystems still compared
against ISS's Nature school, so their mechanics were silently dead. Repointed:

- **Gaia Spell Trials** Phase-2 "botanical spells hit Gaia harder" vulnerability + the
  `botanicalSpellsUsed` counter.
- **Corporea** spell-circle reagent mapping (botanical spells now map to the earth rune, not
  the generic mana rune).
- **Spell-triggered mana events** trigger typing, and the **Livingwood Staff channel**
  eligibility check.
- **Alfheim dual-school crafting** now yields compatible schools for a Botany primary (was
  empty), and **persists both primary and secondary schools** to NBT (the primary was
  previously dropped).
- The **Botanical Focus** now powers the Botany school: `ironsbotany:botanical_focus` was
  added to `#ironsbotany:focus/botany` and removed from ISS's `#irons_spellbooks:nature_focus`.

### Fixed — correctness & exploits

- **Livingwood Staff split mana store.** The staff's tooltip/bar/drain read a different NBT
  key (`IronsBotany_BotaniaMana`) than the Botania `MANA_ITEM` capability
  (`ironsbotany_mana`), so Spark deposits and HUD aggregation were invisible to the staff.
  Both now share `ironsbotany_mana`.
- **Rune Scroll Fusion duplication exploit.** The result copied the input scroll stack
  without resetting its count, so N scrolls + 1 rune yielded N enhanced scrolls while
  consuming one of each. Result count is now clamped to 1. The recipe serializer also no
  longer NPEs when the optional `category` field is omitted.
- **Piercing Botanical Burst projectiles now pierce.** The `onHit` override discarded the
  projectile on first contact, killing the piercing path and double-spawning impact
  particles. It now survives live piercing hits and only finalizes once.
- **Flower auras can no longer register against `minecraft:air`.** An unknown/renamed block
  id resolves to AIR (not null); the guard now excludes AIR, preventing an aura that would
  match near every position around every player.
- **Corporea reagent cache** is now keyed by spell *and level* (reagents scale with level)
  and hands out copies, so cached prototypes are no longer mutated across players/casts.
- **Botania→ISS conversion over-drain.** `tryConvertManaToISS` drained the full transfer
  rate but granted only the floored ISS conversion; it now drains exactly the Botania that
  maps to the ISS actually added.
- **Reservoir/conduit free-mana underflow.** A config-lowered reservoir capacity could make
  `addMana` return negative and create mana; the accepted amount is now clamped to ≥ 0.
- **Gaia's Blessing** drained 100 000 mana on *every* `ModifySpellLevelEvent` firing (many
  per cast) via a large block-entity cube scan; it now drains once per cast-tick.

### Fixed — stability & hardening

- `ManaBridgeManager.resolveCost` is wrapped defensively — a third-party Botania/ANS
  exception can no longer abort or crash a cast.
- **Ars 'n Spells compat** no longer permanently disables itself after an ANS mode switch:
  the reflective `getMaxMana` method is re-resolved per concrete bridge class instead of
  being cached across incompatible bridge implementations.
- **Daybloom Amulet** uses a *transient* attribute modifier (was permanent), so its
  day-only buff can never bake permanently into a player's save.
- **Arcane Mana Altar** resolves its attached spark lazily and drops the reference once the
  spark entity is gone (previously a removed spark blocked all future attachments).
- `BotanySpellConfig` reads are guarded against ISS's `SpellConfigManager` being
  uninitialized (client-side, pre-config-sync).
- `SpellCastSyncPacket` no longer double-enqueues its handler and now declares an explicit
  `PLAY_TO_CLIENT` direction. `SparkSwarmEntity` lifetime is a plain server field instead of
  per-tick synched data (was spamming a metadata packet every tick and flickering on the
  client).
- Spell-catalyst global power multiplier now scales only each catalyst's own contribution
  instead of compounding over the full accumulated damage multiplier.

### Changed

- **New config group "Mana Network Capacities":** every mana-network item cap (previously
  hardcoded) and the Arcane Mana Altar pool cap are now configurable, plus the new
  `elementiumScrollManaCost`.
- Removed the dead, redundant `ManaBridgeManager.tryDrainFromNearbyAltar` cube scan (the
  altar is a `ManaPool`, already covered by the normal pool-draw path).
- Note on design intent: in HYBRID/SEPARATE modes a Botanical cast is cancelled when Botania
  is insufficient even if ISS mana is available — dual-cost is mandatory by design.
- Documentation reconciled to the 1.9.0 behavior (CLAUDE.md, README, CurseForge description).

## [1.8.1] - Iron's Spells 3.16 compatibility & asset hotfix

Player-reported release blockers: a purple/black "magic" texture, and a question
about Iron's Spells 3.16 support. Both are resolved here. **This build now targets
Iron's Spells 'n Spellbooks 3.16+** and no longer loads on 3.15.2 (see below);
players still on 3.15.2 should remain on Iron's Botany 1.8.0. Item/registry IDs are
unchanged — saves are not broken.

### Fixed

- **Missing Arcane Mana Altar texture rendering purple/black.** The block model
  referenced `ironsbotany:block/arcane_mana_altar`, but the texture PNG was never
  shipped, so the altar (and its item form and break particles) rendered as the
  missing-texture checkerboard. Added `textures/block/arcane_mana_altar.png`.
- **Iron's Spells 3.16 compatibility.** ISS 3.16 changed the `Scroll` constructor
  from no-arg to `Scroll(Item.Properties)`. The reusable Elementium Scroll extends
  `Scroll`, so the old binary threw `NoSuchMethodError` on 3.16. Updated to the new
  constructor and rebuilt against 3.16.1 (compiles and builds clean). Because the
  two `Scroll` constructors are mutually exclusive, this is a hard version boundary:
  1.8.1 requires **3.16+**; 3.15.2 is no longer supported.
- **Dreamwood Scepter double-charge.** `DreamwoodConversionHandler` and
  `ManaBridgeManager` both route cost on `SpellPreCastEvent` at `EventPriority.LOW`.
  The Dreamwood handler drained Botania mana without consulting the shared
  `CostRoutedTag`, so in `BOTANIA_PRIMARY`/`HYBRID` a scepter cast could drain
  Botania mana twice. The handler now checks-and-sets the tag, so Botania is drained
  exactly once per cast regardless of listener order.

### Changed

- `mods.toml` Iron's Spells dependency range: `[1.20.1-3.15.2,)` → `[1.20.1-3.16,)`.
- Documentation reconciled to the actual 1.8.x implementation (custom **Botany**
  spell school, v1.8 equipment ladders, mana aggregation, Mana Mirror, upgrade-orb
  compat). `CURSEFORGE_DESCRIPTION.md` in particular was stale (listed ISS 3.0.0 /
  Botania 441 / Curios 5.0.0 and a "Nature school").

### Added

- **Asset-integrity validation** (`./gradlew validateAssets`, run automatically by
  `build`/`check`). Fails the build if any model, blockstate, or particle JSON
  references a texture/model that doesn't exist, or if a registered spell is missing
  its icon — the guard that would have caught the missing altar texture.

> Note: `1.7.x` release notes live in `irons-botany-1.7.0.md` at the project root;
> this changelog jumps from 1.8.x back to 1.6.0 for the older in-repo history.

## [1.8.0] - Player-feedback balance & usability pass

Follows player feedback on `1.7.2`: mana integration that only worked next to a
Mana Pool, upgrade orbs rejected by Botania casters, a backwards wand progression,
and over-strong early mage armor with no higher tiers. Existing item/registry IDs
are preserved — saves are not broken (display names, stats, and recipes change in
place; new items are added alongside).

### Fixed — Mana source support (highest priority)

- **Unified mana aggregation.** `ManaHelper` now aggregates Botania mana across
  *all* carried and equipped sources — Mana Tablet, Mana Ring, Greater Band of
  Mana, Mana Mirror (remote, via its bound pool), and Iron's Botany mana items —
  instead of the old all-or-nothing per-item `requestManaExact`. A cost that no
  single item fully covers can now be paid from several. Two-pass (availability
  sweep, then commit) so a cast never partially drains when it can't be afforded.
  Nearby Mana Pools remain the final fallback.
  - **Root cause:** the previous code asked each item for the *entire* cost
    individually; when no single carried item covered it, the only thing that
    worked was a Mana Pool — exactly the reported symptom.
- New config: `enableInventoryManaSources`, `enableManaMirrorSupport`
  (`enableManaPoolAccess` / `manaPoolSearchRadius` continue to govern the
  nearby-pool fallback).

### Fixed — Upgrade-orb compatibility

- Botanical Spell Blade and the Botania wands now accept Iron's Spells upgrade
  orbs, via the `irons_spellbooks:can_be_upgraded` data tag (the ISS-sanctioned
  integration path). Spellbooks, the Manasteel caster, and mage armor already
  qualified through their ISS/Forge base classes.

### Changed — Spell Blade mana-generation safeguards

- Mana generation now requires a real, damage-dealing hit and is rate-limited by
  an internal cooldown, so autoclickers / attack-speed mods can't farm the mana
  economy. New config: `terrasteelBladeManaCooldown` (a.k.a.
  spellBladeManaGenerationCooldown); `terrasteelBladeManaPerHit` unchanged.

### Added — Melee mage sword ladder

- New **Elementium Spell Sword** and **Gaia Spell Sword** flank the existing
  **Terrasteel Spell Blade**, forming a melee caster ladder
  (Elementium → Terrasteel → Gaia). Like the blade, they boost spell power /
  max mana / cooldown while held and generate Botania mana on a qualifying hit —
  now sharing one `ManaGeneratingWeapon` path so the damage gate and
  anti-autoclicker cooldown apply to all three. Config-driven
  (`elementiumSword*` / `gaiaSword*`); accept upgrade orbs via the
  `can_be_upgraded` tag. Crafted via smithing (Elementium from
  `botania:elementium_sword`; Gaia upgrades the Terrasteel Spell Blade with a
  Gaia Spirit Ingot).

### Added — Wand progression ladder

- Coherent **Manasteel → Elementium → Terrasteel → Gaia** caster ladder. New
  **Terrasteel Wand** fills the late-game gap; existing wands are rebalanced and
  re-themed in place (Manasteel Staff → "Manasteel Wand", Gaia Spirit Wand →
  "Gaia Wand"; Livingwood Staff remains a pre-tier wood starter). Wands now scale
  spell power / cooldown / mana efficiency monotonically and are config-driven
  (`*WandSpellPower`, etc.).
- **Elementium Wand** — dedicated Elementium rung (a real ISS `StaffItem` caster,
  not a stat-stick), crafted via an Alfheim Elven Trade that upgrades the Manasteel
  Wand. Identity: **Elven Favor** — a configurable chance on cast to refund part of
  the spell's mana cost (`elementiumWand*` config). The **Dreamwood Scepter** now
  reverts to its own name as a separate Alfheim ISS→Botania conversion utility
  (it had been temporarily re-lettered as the Elementium rung). 250k Botania-mana
  buffer; accepts upgrade orbs inherently as a casting item.

### Changed — Mage armor: nerf + new tiers

- **Manasteel Wizard** nerfed: per-piece spell power 0.15 → 0.05, max mana
  150 → 50. Its strong 50%-absorb Mana Shield set bonus is **moved to the Gaia
  set**; Manasteel's set bonus is now a small Botania spell-cost discount.
- New **Elementium / Terrasteel / Gaia Mage** armor sets (12 items), each
  config-driven with a distinct set bonus:
  - Elementium — chance to refund part of a spell's mana cost.
  - Terrasteel — incoming damage reduction while ISS mana is high (battlemage).
  - Gaia — the endgame Mana Shield (absorbs damage by spending Botania mana).
- Smithing-ladder recipes (each tier upgrades the previous piece). New config:
  `*ArmorSpellPower`, `*ArmorMaxMana`, and per-set bonus tunables.

## [1.6.0] - In Development

Endgame content layer. Adds the four major Botany progression items, three new
curios, and revives the datagen pipeline so future asset additions don't accumulate
hand-written JSON debt.

### Added — Phase 6.1: Datagen revival

- **`IBDatagen`** + provider stack (`IBItemModelProvider`, `IBBlockStateProvider`,
  `IBLanguageProvider`, `IBLootTableProvider`, `IBRecipeProvider`) — wired to
  `GatherDataEvent`. The datagen run config in `build.gradle` is uncommented;
  `./gradlew runData` regenerates JSON under `src/generated/resources/`.
- Botania custom-recipe JSONs (`petal_apothecary`, `runic_altar`, `terra_plate`,
  `elven_trade`) and Patchouli book entries stay hand-written by design.

### Added — Phase 6.2: Manasteel Staff

- **`ManasteelStaffItem`** — extends ISS `StaffItem` with a custom `MANASTEEL_TIER`
  (damage 2.0, speed -3.0) plus four `AttributeContainer`s: +20 Max Mana,
  +5% Cast Time Reduction, +10% Botany Spell Power, +0.05 Mana Efficiency.
- Mana network: 50,000-unit `MANA_ITEM` capability via `IBCapabilityHandler`.
- Crafting: shaped recipe (Manasteel + Mana Pearl + Sticks).

### Added — Phase 6.3: Spellbooks

- **`TerrasteelSpellbookItem`** — 12-slot Rare-tier `SimpleAttributeSpellBook`.
  Stats: +200 Max Mana, +15% Botany Spell Power, +10% Nature Spell Power,
  +0.10 Mana Efficiency. Mana network: 200,000-unit capacity.
- **`ArcaneCodexItem`** — 14-slot Epic-tier endgame spellbook. Stats: +300 Max
  Mana, +20% Cooldown Reduction, +15% Botany Spell Power, +10% Spell Power
  (all schools), +0.15 Mana Efficiency. Mana network: 500,000-unit capacity.
  Fire-resistant.
- Crafting: Terrasteel Spellbook is a vanilla shaped recipe (`#botania:terrasteel_ingots`
  surrounding any spellbook). Arcane Codex is a Terra Plate recipe (500,000 mana
  + Terrasteel + Terrasteel Spellbook + Mana Pearl + Mana Diamond + Gaia Ingot).

### Added — Phase 6.4: Elementium Scroll

- **`ElementiumScrollItem`** — extends ISS `Scroll`, overrides
  `removeScrollAfterCast` to skip consumption when `ManaBridgeManager` paid the
  cost from Botania mana that tick. Falls back to vanilla single-use behavior if
  no Botania mana is available.
- **`ManaBridgeManager.resolveCost`** — adds an Elementium-scroll branch that
  checks for the item in either hand on `CastSource.SCROLL`, charges
  `issCost × MANA_CONVERSION_RATIO` Botania mana, marks the routed tag, and
  returns `botaniaOnly` so the scroll's override sees the marker.
- Crafting: Alfheim elven trade (any ISS scroll + Elementium + Pixie Dust).

### Added — Phase 6.5: Three new curios

- **`ManaReservoirRingItem`** — ring slot. +100 Max Mana attribute. Every 20
  ticks, while ISS mana is below 50%, drains 20 Botania mana from any source
  in the wearer's inventory and adds 1 ISS mana. Doubles as a 200,000-unit
  Botania mana store via `IBCapabilityHandler`.
- **`DaybloomAmuletItem`** — necklace slot. Daytime-conditional bonuses:
  +15% Nature Spell Power and +5% Cast Time Reduction while in direct
  sunlight. Implemented as transient UUID-keyed attribute modifiers added/
  removed in `curioTick` based on `isDay() && canSeeSky(pos)`.
- **`GaiasBlessingItem`** — necklace slot, Epic. While worn, Botany-school
  spells gain +1 effective level via `ModifySpellLevelEvent`. Each cast drains
  100,000 mana from a `ManaPool` within 16 blocks; if no pool can pay, the
  bonus fizzles silently.
- **`CurioEffectsHandler`** — Forge-bus subscriber that wires Gaia's Blessing
  into the spell-level event.
- Crafting: Mana Reservoir Ring is Petal Apothecary; Daybloom Amulet is Petal
  Apothecary; Gaia's Blessing is Terra Plate (200,000 mana + Gaia Spirit +
  Daybloom Amulet + Pixie Dust + Dragonstone).
- Curios slot tags updated: `ring.json` adds Mana Reservoir Ring; new
  `necklace.json` registers Daybloom Amulet and Gaia's Blessing.

### Added — Phase 6.6: Patchouli + release prep

- 5 new Patchouli entries under `equipment/` (manasteel_staff, terrasteel_spellbook,
  arcane_codex, elementium_scroll, new_curios).
- Version bump to 1.6.0 in `build.gradle`.

## [1.5.0] - Released

Foundation release implementing Phases 1–3 of the v2.0 enhancement roadmap. Adds the centralized
mana bridge, custom Botany school, recipe content, and the first mana-network blocks.

### Phase 1: Foundation Hardening

- **Added** `ManaBridgeManager` (`com.ironsbotany.common.bridge`) — single entry point for
  spell cost routing. Handles all five `ManaUnificationMode` values with a single
  `resolveCost(player, spell, level, source)` call, returning
  `ManaResolutionResult(issCharged, botaniaCharged, ok)`.
- **Added** `CostRoutedTag` — tick-scoped idempotency tag stored on the player's persistent
  data, preventing double-billing when both `SpellPreCastEvent` and `ChangeManaEvent` fire
  for the same cast. Reads ANS's mirror tag to defer cleanly.
- **Added** `SpellEventHandlers` — explicit `EventPriority.LOW` subscribers for ISS spell
  events. Yields to Ars 'n Spells (which runs at `NORMAL`) and to KubeJS scripts
  (`HIGH`/`HIGHEST`).
- **Added** `ManaPriorityChain` + `ManaSource` enum — config-driven cross-bridge resource
  ordering (default `["botania", "ars", "iss"]`). Configured via the new
  `MANA_PRIORITY_CHAIN` TOML key.
- **Added** `ArsNSpellsCompat.shouldDeferRouting()` — IB cleanly defers routing when ANS
  is in `ARS_PRIMARY` or `HYBRID` mode.
- **Added** public API package at `com.ironsbotany.api.*`:
  - `IronsBotanyApi` — stable entry point for downstream addons
  - `IManaSource` — SPI for cross-bridge mana source contract
  - `BotanySchoolFlowerRegistry` — registration surface for school-themed flowers
- **Added** `BotanySpellConfig` — per-spell `SpellConfigParameter`s (`botania_mana_cost`,
  `dual_cost_enabled`) via `RegisterConfigParametersEvent`. Datapack-overridable per spell
  at `data/<namespace>/spell_configs/<spell_id>.json`.
- **Added** `/irons_botany reload` — server command (perm 2) that flushes runtime caches.
- **Added** mixin JSON skeleton (`ironsbotany.mixins.json`) for future client-side HUD work.

### Phase 2: Content & School

- **Added** Botany SchoolType (`IBSchools.BOTANY`) — replaces the earlier shortcut of
  piggybacking on Nature. Wires:
  - Focus tag `#ironsbotany:focus/botany` (Mana Pearl, Pixie Dust, Dragonstone)
  - Attributes `BOTANY_SPELL_POWER`, `BOTANY_MAGIC_RESIST`
  - Damage type `ironsbotany:botany` (data-driven JSON at
    `data/ironsbotany/damage_type/botany.json`)
- **Changed** all 9 existing Botanical spells now return `IBSchools.BOTANY.get()` from
  `getSchoolType()`.
- **Added** Mana Inks (`MINOR_MANA_INK`, `GREATER_MANA_INK`, `PRIME_MANA_INK`) — petal
  apothecary recipes that craft tiered scroll inks.
- **Added** 8 School Power Orbs (Fire, Frost, Lightning, Holy, Ender, Blood, Nature,
  Eldritch), crafted at the Runic Altar with the matching Botania rune.
- **Added** 3 Alfheim Portal trades — Manasteel→Elementium spellblade,
  Livingwood→Dreamwood scepter, Greater→Prime ink discount.
- **Added** Gaia Guardian II loot modifier — Forge GLM (`AddItemLootModifier` +
  `IBLootModifiers`) that adds Legendary Ink to the hardmode loot table without
  overwriting Botania's JSON.
- **Added** Lexica Botania Patchouli entry at
  `assets/botania/patchouli_books/lexicon/en_us/entries/ironsbotany/index.json` — five-page
  guide covering Iron's Botany content from inside Botania's own book.

### Phase 3: Mana Network Citizenship

- **Added** `ItemManaStorage` + `ItemManaCapabilityProvider` — per-stack `ManaItem`
  capability provider, attached via `IBCapabilityHandler.onAttachItemCapabilities`. Iron's
  Botany weapons and curios now appear in the Botania mana HUD, accept Spark deposits, and
  are drained by `ManaItemHandler.requestMana` alongside Mana Tablets.
- **Added** Arcane Mana Altar (`ArcaneManaAltarBlock` + `ArcaneManaAltarBlockEntity`) —
  block entity implementing `ManaPool`, `ManaReceiver`, and `SparkAttachable`
  simultaneously. Default capacity 1,000,000 mana. Drainable by nearby players during cast
  resolution via `ManaBridgeManager.tryDrainFromNearbyAltar`.
- **Added** `ManaBridgeManager.tryDrainFromNearbyAltar(player, amount, radius)` — bridge
  helper that scans for an Arcane Mana Altar within range and drains from it before
  falling through to the player's tablets.

### Changed

- `AbstractBotanicalSpell.onCast` — removed inline mana routing (the bridge handles it
  before this method runs). Cost handling is now centralized in `ManaBridgeManager`.
- `AbstractBotanicalSpell.getBotaniaManaCost(level)` — now consults the
  `botania_mana_cost` SpellConfigParameter via `BotanySpellConfig.resolveBotaniaCost` and
  falls back to the constructor-supplied ladder.

### Deferred to v1.6 / v2.0

The following enhancement-doc items are scoped for follow-up releases:

- **v1.6**: Manasteel Staff, Terrasteel Spellbook, Elementium Scroll (ISS spellbook
  subclasses); 3 new curios (Mana Reservoir Ring, Daybloom Amulet, Gaia's Blessing);
  `ArcaneCodexItem` + Terra Plate recipe; datagen revival.
- **v2.0**: Six ISS-school generating flowers (Emberlily, Frostbud, Stormbloom,
  Sanctuary Lily, Voidpetal, Bloodweed); Verdant Caster functional flower;
  Spreader-fired spell bursts; Corporea Scroll Rack + spell request matcher;
  performance hygiene with `FakePlayer` pool; KubeJS surface (`irons_botany_js`).

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
