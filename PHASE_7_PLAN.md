# Phase 7 / v2.0 — Implementation Plan

**Status**: planning · **Target version**: 2.0.0 · **Estimated scope**: 6–9 months across multiple sessions

This is the long-deferred v2.0 headline release: the *"mana network as caster"* arc from
the original enhancement roadmap. Where v1.5 built the bridge architecture and v1.6 added
endgame items, v2.0 inverts the relationship — Botania flowers, spreaders, and the Corporea
network all become *active casters* of Iron's Spells 'n Spellbooks magic, and downstream
mods get a real public registration API to extend Iron's Botany themselves.

---

## What v2.0 delivers

Seven sub-phases, addressed in dependency order:

| Phase | Feature | Headline value |
|-------|---------|----------------|
| 7.1 | Six ISS-school generating flowers | Spells you cast feed the mana network |
| 7.2 | `BotanySchoolFlowerRegistry` activation | Downstream addons can register their own school flowers |
| 7.3 | Verdant Caster functional flower | **The v2.0 signature feature** — a flower that casts ISS spells |
| 7.4 | Performance hygiene (caches + FakePlayer pool) | Required prerequisite for 7.3 in a real world |
| 7.5 | Spreader-fired spell bursts (stretch) | Mana Spreaders launch spell projectiles |
| 7.6 | Corporea Scroll Rack + spell request matcher | "/corporea give me 2 fireball scrolls" works |
| 7.7 | KubeJS surface (`irons_botany_js`) | Datapack-friendly recipe + flower registration |

**Plus** Phase 7.X polish: Patchouli expansion, CHANGELOG, version bump to 2.0.0,
testing matrix.

---

## Phase 7.1 — Six ISS-school generating flowers

The cleanest bidirectional integration: when a player casts a fire-school spell within range
of an Emberlily, the lily generates Botania mana from the activity. The ISS ecosystem
becomes Botania fuel.

### Files to create

**Java**
- `common/block/flower/EmberlilyBlock.java` + `EmberlilyBlockEntity.java` (Fire school)
- `common/block/flower/FrostbudBlock.java` + `FrostbudBlockEntity.java` (Ice/frost)
- `common/block/flower/StormbloomBlock.java` + `StormbloomBlockEntity.java` (Lightning)
- `common/block/flower/SanctuaryLilyBlock.java` + `SanctuaryLilyBlockEntity.java` (Holy/undead damage)
- `common/block/flower/VoidpetalBlock.java` + `VoidpetalBlockEntity.java` (Ender/teleport)
- `common/block/flower/BloodweedBlock.java` + `BloodweedBlockEntity.java` (Blood/`LivingHurtEvent` from caster)

All BEs extend `vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity`. They override
`tickFlower()` for passive mana generation (slow trickle) and expose a public
`onTriggerEvent(int amount)` method that the central event handler calls when a matching
school cast happens nearby.

- `common/event/SchoolFlowerHandler.java` — single Forge-bus subscriber. Listens to:
  - `SpellOnCastEvent` (ISS) — for Fire/Ice/Lightning/Holy/Ender direct triggers
  - `LivingHurtEvent` — for Bloodweed (blood damage from the caster)
  - `EntityTeleportEvent` — for Voidpetal
  Maintains a chunk-scoped cache of (chunk → list of school flowers) keyed by school ID;
  invalidates on `ChunkEvent.Load/Unload`.

### Registries / wiring
- `IBBlocks` — 6 new entries with `SpecialFlowerBlock` (need to import this from Botania)
- `IBBlockEntities` — 6 new BE types
- `IBCreativeTabs` — flower section
- Lang entries for 6 names + 6 tooltips
- Item models, blockstate JSONs (use Botania's `botania:special_flower` model parent)
- Patchouli entries under `getting_started/` or new `flowers/` category

### Mana economy
- Per-cast trigger: 500 mana for level-1 spell, scaling linearly (configurable via TOML)
- Passive trickle: 1 mana/tick (matches Daybloom baseline)
- Configurable per-flower max storage (default 6,000 — Daybloom default)

### Risks
- **`SpecialFlowerBlock` and `GeneratingFlowerBlockEntity` are in `vazkii.botania.api.block_entity.*` — public**. Confirmed in the Phase 4 research notes.
- **Particle textures**: each flower needs a unique petal color. Six new `botania:petals/<color>` references — these tags exist in Botania.
- **Event coverage**: `SpellOnCastEvent` may not fire for every school. Fallback: hook into `LivingHurtEvent` with school-tag inspection.
- **Sanctuary Lily / Bloodweed double-counting**: a Holy spell that hurts an undead via `LivingHurtEvent` could trigger both lilies. Solution: per-tick-per-flower dedup keyed on the damage event's UUID.

---

## Phase 7.2 — `BotanySchoolFlowerRegistry` activation

The skeleton was added in v1.5 (`com.ironsbotany.api.BotanySchoolFlowerRegistry`). Phase 7.2
makes it *load-bearing*:

- `IronsBotany.commonSetup` — register the six built-in flowers from Phase 7.1 into the registry
- `SchoolFlowerHandler` — switch from hard-coded block lookups to `BotanySchoolFlowerRegistry.all()` iteration
- Add tooltip-overlay hook (uses the registry's `Entry.schoolId` to render a colored school badge on flower items)
- Add Patchouli auto-generation hook (downstream mods that register a flower can supply a Patchouli entry path)

### Files
- Modify `BotanySchoolFlowerRegistry` to add `Entry.tooltipKey` and `Entry.patchouliEntry` fields
- Modify `IronsBotany.commonSetup` to register the six flowers
- New `client/render/SchoolFlowerTooltip.java` for the school-badge overlay
- New documentation under `api/` JavaDoc explaining the registration pattern for addons

### Risk
- API stability commitment: once Phase 7.2 ships, `BotanySchoolFlowerRegistry.register*()` becomes a stable API. Future signature changes need deprecation cycles.

---

## Phase 7.3 — Verdant Caster (the headline feature)

A functional flower that **casts ISS spells autonomously**. Bind a spell scroll into the
flower, connect it to a Mana Pool, and watch it cast at a configurable interval at any
hostile mob in range.

### Files to create

- `common/block/flower/VerdantCasterBlock.java` — extends `SpecialFlowerBlock`
- `common/block/flower/VerdantCasterBlockEntity.java` — extends `FunctionalFlowerBlockEntity`. The flower scans a 16-block radius for hostile mobs, picks the nearest, and casts the bound spell at it on the configured interval (default every 100 ticks / 5 seconds).
- `common/util/FakePlayerPool.java` — singleton `WeakHashMap<ServerLevel, FakePlayer>`. Critical: ONE FakePlayer per world, reused across all Verdant Casters, never per-cast allocation.
- `common/casting/VerdantCasterContext.java` — synthesizes minimum-viable `MagicData` for the FakePlayer (no spellbook, no curios, just enough to satisfy the spell cast pipeline).
- `client/renderer/VerdantCasterBER.java` — block entity renderer that shows the bound spell's icon hovering above the flower.

### Bind mechanic
- Right-click the flower with a spell scroll → store the spell ID + level in BE NBT
- Right-click with empty hand → display "Bound to: $spellName" in the action bar
- Sneak-right-click with empty hand → unbind (drops the scroll)

### Cost model
- Per cast: `iss_mana_cost × VERDANT_CASTER_COST_MULTIPLIER` Botania mana (default ×4, since flower mana is abundant)
- Drained from the connected Mana Pool (via the standard Botania `FunctionalFlowerBlockEntity.requestMana` path)
- If the pool is empty, the flower idles silently (no error spam)

### Targeting
- Scan radius: 16 blocks, configurable via TOML
- Targets: nearest hostile `Monster` instance, line-of-sight check via `level.clip()`
- Whitelist/blacklist support: NBT-stored entity-tag filter, settable via Wand of the Forest right-click (matches Botania UX pattern)

### Risks
- **`FakePlayer` allocation cost**: Without pooling, a server with 50 Verdant Casters running every 5 seconds allocates 600 FakePlayers/minute. Pool ABSOLUTELY required (Phase 7.4).
- **`MagicData` capability lookup per tick**: hoist out of `tickFlower()` into a cached field, refresh on world reload.
- **Spell cast pipeline expects a real player**: some ISS spells query equipment, advancements, or active effects. Test thoroughly with: Magic Missile (trivial), Fireball (projectile-based), Heal (target self — won't work on FakePlayer, document as known limitation).
- **Permission level**: FakePlayer with op permission would let the flower cast operator-restricted spells. Run the FakePlayer at permission level 0.
- **Detection by anti-cheat**: some mods/server plugins flag FakePlayers. Document as a known interop concern.

### Configuration (new TOML keys)
- `VERDANT_CASTER_INTERVAL` — ticks between casts (default 100)
- `VERDANT_CASTER_RADIUS` — target scan radius (default 16)
- `VERDANT_CASTER_COST_MULTIPLIER` — mana cost vs ISS base (default 4.0)
- `VERDANT_CASTER_MAX_PER_CHUNK` — anti-grief cap (default 4)

---

## Phase 7.4 — Performance hygiene

Prerequisites for 7.3 to work in production. Some of this is also retroactive for v1.5/v1.6
features that grew sloppy as scope expanded.

### Tasks

1. **`FakePlayerPool`** — already listed in 7.3; surface it as a public utility under `common/util/`
2. **Chunk-scoped flower cache** in `SchoolFlowerHandler` — replace any whole-world BE iteration with a `Long2ObjectOpenHashMap<List<BlockPos>>` keyed on `ChunkPos`, invalidated on `ChunkEvent.Load/Unload`
3. **`MagicData` capability hoisting** — replace per-tick `MagicData.getPlayerMagicData(player)` calls with cached references refreshed on player join/respawn
4. **Tick throttling** — `FunctionalFlowerBlockEntity.tickFlower()` runs every tick; explicitly `if (level.getGameTime() % N != 0) return;` at the top of every IB flower BE
5. **Telemetry counters** — under `/irons_botany debug` subcommand: report flower-tick microseconds, FakePlayer pool size, chunk-cache hit rate

### Files
- New `common/util/FakePlayerPool.java`
- Modify `common/event/SchoolFlowerHandler.java` — add chunk cache
- New `common/command/IBDebugCommand.java` — `/irons_botany debug` subcommand surfacing telemetry

### Risks
- **Cache invalidation correctness**: a flower being broken inside a loaded chunk needs to update the cache. Hook `BlockEvent.BreakEvent` for our flower blocks specifically.
- **WeakHashMap and ServerLevel lifecycle**: a FakePlayer pinned by a flower BE could keep a `ServerLevel` from being GC'd. Verify with VisualVM after a `/reload` cycle.

---

## Phase 7.5 — Spreader-fired spell bursts (stretch)

Take a Botania Mana Spreader, give it a "Spell Burst" upgrade, and now its mana bursts
trigger a bound ISS spell on impact instead of just dropping mana into receivers.

### Files (if pursued)

- `common/item/SpellBurstLensItem.java` — new lens upgrade for Mana Spreaders
- `common/casting/BurstSpellCarrier.java` — wraps a `SpellId + level` in burst NBT, intercepted on impact
- Mixin or capability on `ManaBurst` to add the spell-cast-on-impact behavior

### Risks
- **`ManaBurst` is `vazkii.botania.api.mana.ManaBurst` — public interface**, but the impl is `vazkii.botania.common.entity.ManaBurstEntity` — internal. Mixin path needed if we want to inject impact behavior.
- **Mixin scope**: this is the first mixin in the project. Need to make `ironsbotany.mixins.json` actually load, register a mixin processor, etc. The skeleton from Phase 1C exists but has never been activated.
- **Stretch designation**: punt to v2.1 if Phase 7.3 alone soaks up the v2.0 timeline.

---

## Phase 7.6 — Corporea Scroll Rack + spell request matcher

Make ISS scrolls first-class Corporea citizens. `/corporea give me 2 fireball scrolls` should
work.

### Files

- `common/block/ScrollRackBlock.java` + `ScrollRackBlockEntity.java` — 27-slot inventory block, tagged `#botania:storage`
- `common/corporea/ScrollRackNodeDetector.java` — implements `vazkii.botania.api.corporea.CorporeaNodeDetector`, registered via `BotaniaAPI.INSTANCE.registerCorporeaNodeDetector(...)` in `commonSetup`
- `common/corporea/SpellRequestMatcher.java` — extends `vazkii.botania.api.corporea.CorporeaRequestMatcher`. Matches scrolls by spell ID and (optionally) level, e.g., `/corporea give me 1 fireball level 3 scroll`
- Modify `common/corporea/SpellLogisticsSystem.java` — register the matcher

### Recipe
- Scroll Rack crafted from 8× Manasteel + Bookshelf (vanilla shaped recipe)

### Risks
- **`CorporeaRequestMatcher` parser**: the request grammar is `/corporea give me $count $itemKey`. Adding a "level $n" qualifier requires custom parsing — Botania's matcher API supports it but we need to register a custom request parser too.
- **NBT comparison perf**: scrolls store spell data in NBT. The matcher does NBT equality checks per inventory item per request — fine for hundreds of scrolls, problematic for thousands. Document as a soft cap (max 100 scrolls per Scroll Rack).

---

## Phase 7.7 — KubeJS surface (`irons_botany_js`)

Make Iron's Botany scriptable from KubeJS so modpack devs can:
- Register custom Verdant-Caster-compatible flowers without writing Java
- Define new petal-apothecary recipes that span Iron's Botany and Botania
- Hook into `SchoolFlowerHandler` events (`SpellCastNearFlowerEvent`)

### Files

- `common/integration/kubejs/IronsBotanyKubeJSPlugin.java` — extends `dev.latvian.mods.kubejs.KubeJSPlugin`
- `common/integration/kubejs/RecipeSchemas.java` — declares schemas for `irons_botany.spell_manafusion`
- `common/integration/kubejs/StartupEvents.java` — registers `irons_botany.flowers` registry handler
- `common/integration/kubejs/ServerEvents.java` — exposes `SchoolFlowerHandler.onSpellCast` as a JS-subscribable event

### Soft-dependency
- Add `kubejs` to `mods.toml` as `mandatory=false` with version `[2001.6.0,)`
- Build won't fail without KubeJS on the classpath; `IronsBotanyKubeJSPlugin` is loaded reflectively via the standard KubeJS plugin discovery mechanism

### Risks
- **KubeJS plugin discovery**: relies on a `META-INF/services/` SPI file. Test with KubeJS present and absent.
- **API surface lock-in**: every JS hook becomes a public contract. Start narrow.

---

## Phase 7.X — Polish + release prep

- **Patchouli expansion**: 12+ new entries (one per flower, Verdant Caster, Scroll Rack, KubeJS overview)
- **Lexica Botania entry update**: append a "v2.0 features" page to the existing
  `assets/botania/patchouli_books/lexicon/en_us/entries/ironsbotany/index.json`
- **CLAUDE.md** — add `flower/`, `corporea/`, `integration/kubejs/`, `util/FakePlayerPool` to the architecture section
- **CHANGELOG**: detailed `[2.0.0]` section
- **Version bump**: `build.gradle` 1.6.0 → 2.0.0
- **README**: write the project's first README.md (none currently exists). Feature table, dependency matrix, install instructions.

---

## Risk register (consolidated)

| Risk | Phase | Mitigation |
|------|-------|-----------|
| FakePlayer allocation flooding | 7.3 | Phase 7.4 pool — strictly required |
| Botania API drift between -450 and -451 | all | Pin `compileOnly` to 1.20.1-450, test against -451 once before release |
| `SpecialFlowerBlock` rendering quirks | 7.1, 7.3 | Reuse Botania's `SpecialFlowerBlockEntityRenderer` rather than custom |
| Mixin activation failure | 7.5 | Stretch goal — drop if mixin tooling proves brittle |
| Anti-cheat plugins flag FakePlayers | 7.3 | Document; offer config to disable Verdant Caster entirely |
| KubeJS surface lock-in | 7.7 | Mark all v2.0 KubeJS hooks `@ApiStatus.Experimental`; promote to stable in v2.1 |
| Save migration: existing chunks gain new flower BEs | 7.1, 7.3 | Botania flowers are placed by player, not generated; no save migration needed |
| Corporea request grammar drift | 7.6 | Pin Botania API version; document grammar in entry tooltip |
| Worlds with very high flower counts (>1000) | 7.4 | Chunk-scoped cache + tick throttling — verify with VisualVM at 1000-flower stress test |
| Patchouli book entry count exceeds 32 | 7.X | Botania's book caps at ~50 entries per category; split into sub-categories if needed |

---

## Sequencing

```
7.4 (perf) ──┬──> 7.1 (school flowers) ──> 7.2 (registry) ──> 7.3 (Verdant Caster)
             │
             └──> 7.6 (Corporea) ──> 7.7 (KubeJS)
                                              │
                                              v
                                       7.5 (spreader bursts — stretch)
                                              │
                                              v
                                          7.X (polish + release)
```

**Critical path**: 7.4 → 7.3, since the Verdant Caster's correctness depends on the
FakePlayer pool. Everything else is roughly parallelizable.

**Estimated milestones**:
- v2.0 alpha: 7.1 + 7.2 + 7.4 + 7.6 (everything except the headline)
- v2.0 beta: + 7.3 (Verdant Caster)
- v2.0 release: + 7.5 (if pursued) + 7.7 + 7.X polish

---

## Estimated scope vs prior phases

| Phase | New Java files | New JSONs | Modified files |
|-------|----------------|-----------|----------------|
| v1.5 (Phases 1+2+3A/3B) | ~25 | ~40 | ~15 |
| v1.6 (Phases 6.1–6.6) | ~10 | ~20 | ~6 |
| **v2.0 (Phase 7)** | **~35** | **~50** | **~15** |

v2.0 is roughly the size of v1.5, but with one feature (the Verdant Caster) carrying the
majority of the architectural risk. Plan accordingly: a single 2-3 session push for
Phase 7.1 + 7.2 + 7.4 + 7.6 to land an alpha, then a second push focused entirely on
Phase 7.3.

---

## Handoff checklist (when picking this up)

- [ ] Re-verify Botania API surfaces against the JAR (`unzip` + `javap`) — version drift may have moved interfaces
- [ ] Confirm ISS 3.15.x is still pinned in `build.gradle`
- [ ] Run `./gradlew compileJava` on the v1.6 baseline to confirm starting point is green
- [ ] Read this plan top-to-bottom; the risk register is more important than the phase descriptions
- [ ] Start with Phase 7.4 — the perf foundation — even if the user asks for the Verdant Caster first
- [ ] When the Verdant Caster lands, stress-test with 50 flowers in one chunk before declaring it done

---

*This plan was generated as part of the v1.6 release wrap-up. It supersedes any earlier
Phase 4 references in `IRONS_BOTANY_ENHANCEMENT.md`. The numeric phases here align with
the v1.5/v1.6 numbering — there is no Phase 4 anymore; the work is Phase 7.*
