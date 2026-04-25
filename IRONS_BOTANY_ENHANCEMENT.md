# Iron's Botany: deep review and enhancement roadmap

## 1. Executive summary of current state

**Iron's Botany is a nascent, single-sentence-scoped bridge mod that sits in the shadow of its sibling, Ars 'n Spells, and has not yet earned the architectural depth its CurseForge pitch promises.** As of April 24 2026 the mod has **~668 downloads**, was first published **April 7 2026**, targets **Minecraft 1.20.1 / Forge**, is **distributed only on CurseForge** (no GitHub mirror, no Modrinth listing), and ships with an advertised feature set of *"Botania mana powering spells, spells interacting with Botania systems, a new school, new spells, new armor."* That description names five features but implies none of the structural depth that Ars 'n Spells already has in production (`BridgeManager`, five-mode unification, resonance, affinity, cross-inscription ritual).

The decisive finding of this review is that **Iron's Botany is currently a first-iteration content patch rather than a systems bridge**, whereas the two mods it stitches together (**ISS 3.15.5.1 on 1.20.1**, **Botania 1.20.1-451**) both expose mature, well-factored extension APIs that invite far deeper integration. The roadmap below treats the current 1.0 line as a *content beachhead* and lays out a v1.x stabilization path plus a v2.0 "mana-network-as-caster" vision that would make Iron's Botany the most ambitious ISS bridge in existence.

A secondary finding: **the developer's Ars 'n Spells architecture is re-usable almost wholesale**, and cross-compatibility between the two bridge mods should be solved *inside Iron's Botany* by delegating cost routing to Ars 'n Spells's `BridgeManager` when present, rather than racing it on `SpellPreCastEvent`. Getting this wrong will quietly double-bill players running both bridges.

Because the source repository is not public, every concrete code-level finding below is **inferential**, triangulated from (a) the CurseForge one-liner, (b) Otectus's public Ars 'n Spells and Locks Reforged repos, (c) Otectus's documented conventions (TOML-split configs, JSON-overlay data, mixin-per-target, GPL-3.0, `CLAUDE.md` companion docs), and (d) ISS/Botania API surfaces the mod must touch to do what it advertises.

## 2. Code review findings (inferred, pending source access)

### Critical unknowns blocking a real review

The Iron's Botany source is not published at `github.com/otectus/irons-botany` or any plausible slug, and the mod is CurseForge-only. A full review requires the JAR or a repo link. The issues below are therefore **structural hypotheses** ranked by likelihood given Otectus's pattern from Ars 'n Spells and the advertised scope.

### Likely high-severity issues

**Event-ordering race with Ars 'n Spells on `SpellPreCastEvent` / `ChangeManaEvent`.** Both bridges will subscribe to `io.redspace.ironsspellbooks.api.events.SpellPreCastEvent` and `ChangeManaEvent`, and without explicit `@SubscribeEvent(priority = …)` annotations the dispatch order is undefined. If Ars 'n Spells reroutes the cost to Ars mana and Iron's Botany then debits a Botania tablet, the player pays twice. Conversely, if Iron's Botany cancels the event for insufficient Botania mana before Ars 'n Spells gets a chance to fall back to LP/Aura, the cast aborts needlessly. **This is the single most important correctness bug to pre-empt**, and it almost certainly exists today because neither mod advertises awareness of the other.

**Mixin target overlap risk on the Ars side.** Ars 'n Spells ships `MixinArsManaRegen` targeting `ManaCapEvents.playerOnTick`. If Iron's Botany also tries to modify Ars mana regen (unlikely given its Botania scope, but possible if the "Botania mana powers spells" feature extends to Ars), the mixins will silently stomp each other. Declare a distinct mixin JSON (`irons_botany.mixins.json` vs `irons_botany_iss.mixins.json`) and **avoid any Ars-internal mixin targets entirely**; route through ISS events.

**Mana-payment double accounting under `ChangeManaEvent`.** Because ISS's mana flow is: *check mana → fire `SpellPreCastEvent` → fire `ModifySpellLevelEvent` → `MagicManager.setPlayerCurrentMana` (which itself fires `ChangeManaEvent`)*, a bridge that both cancels the pre-cast on insufficient Botania mana **and** mutates `setNewMana` in the change-event handler needs to be idempotent across the two hooks. Otectus's Ars 'n Spells has had this class of bug (CHANGELOG notes "double event firing" and "pending cost TTL under server lag"); Iron's Botany almost certainly has the same latent issue.

**Client/server desync of the HUD bar.** ISS renders its own mana bar via `ManaBarOverlay`. If Iron's Botany displays Botania mana in place of (or alongside) ISS mana, the overlay needs suppression/replacement on the client. Ars 'n Spells does this; Iron's Botany likely ships a visible duplicate or a stale display if the sync packet format changed in ISS 3.15.

**Capability / attachment persistence across death and dimension change.** Ars 'n Spells had to solve this for Aura. If Iron's Botany tracks any per-player state (affinity, Botania-mana-debt, channeled-pool binding), the 1.20.1 Forge `LazyOptional<…>` lifecycle for player capabilities drops on respawn unless `ICapabilitySerializable` is implemented and `PlayerEvent.Clone` is handled with `wasDeath=true` copy. This is a classic first-release miss.

### Likely architectural concerns

**Hard-coded school addition vs data-driven school registration.** ISS's `SchoolRegistry` uses `DeferredRegister<SchoolType>` against `SchoolRegistry.SCHOOL_REGISTRY_KEY`. A new "Botany" school needs a `SchoolType(ResourceLocation, TagKey<Item> focus, Component name, LazyOptional<Attribute> power, LazyOptional<Attribute> resist, LazyOptional<SoundEvent> castSound, ResourceKey<DamageType> damageType)`. The damage-type must be a datapack JSON under `data/irons_botany/damage_type/botany.json`. If the current mod hardcodes these rather than exposing the focus tag and attribute hooks for downstream datapack override, community extension will be painful.

**Soft-dep handling in `mods.toml`.** Given Otectus's pattern of local flat-jar dependencies in Ars 'n Spells, the `mods.toml` likely hard-depends on both Botania and ISS. Moving Botania to `mandatory=false` (with a `ModList.get().isLoaded("botania")` guard) would allow the mod to degrade gracefully for users who drop Botania mid-progression — minor but worth fixing.

**No public API package.** Unlike ISS (`io.redspace.ironsspellbooks.api.*`) or Botania (`vazkii.botania.api.*`), Iron's Botany likely does not expose a stable `com.otectus.irons_botany.api` subtree. For a bridge mod this is acceptable in v1 but becomes a blocker for v2's "custom subtile registration" vision.

**Single-mode mana routing.** The CurseForge copy says "Botania mana power spells" — singular. Ars 'n Spells ships five modes (`iss_primary`, `ars_primary`, `hybrid`, `separate`, `disabled`). Iron's Botany almost certainly ships one default with perhaps a ratio config and no authority-choice; this is the most obvious gap relative to the sibling mod.

### Likely code smells / minor issues

NBT key namespacing will likely follow Ars 'n Spells's precedent (`arsnspells:cross_spells`, dropping the underscore). Prefer the mod ID verbatim (`irons_botany:…`) to keep search/save-editor tooling clean. Expect hardcoded constants for conversion ratios, spell-cost multipliers, and radius checks; these should live in TOML. Magic numbers in spell code (damage, mana cost per level, cooldown) should be `SpellConfigParameter<T>` entries via `RegisterConfigParametersEvent` so server operators can datapack-override without a recompile. The absence of `/irons_botany reload` (present in Effective Instruments as a signature Otectus touch) is almost certain in v1.

### Likely dead code / incomplete features

Based on the "new school, new spells, new armor" pitch, the following are either stubbed or missing entirely: damage-type JSON for the new school; spell-power upgrade orbs for the new school (ISS expects `{school}_spell_power` orbs in `UpgradeTypes`); `InkItem` integration so scrolls of the new school are generatable at the Scroll Forge; loot injection so the school's focus drops somewhere thematic; villager-trade additions. Expect a Patchouli Lexica Botania entry to be absent (this is a major UX gap — see §5b).

## 3. ISS architecture — extensibility points that matter for a Botania bridge

ISS's public API lives at `io.redspace.ironsspellbooks.api.*` and is published as a classifier-`api` artifact at `https://code.redspace.io/releases`. Gradle:

```gradle
compileOnly fg.deobf("io.redspace:irons_spellbooks:${ver}:api")
runtimeOnly  fg.deobf("io.redspace:irons_spellbooks:${ver}")
```

The **seven highest-leverage hook points** for a Botania bridge are:

**`SpellPreCastEvent` (Forge bus, cancellable)** fires after spellbook/scroll validation but before mana is debited and before the cast timer starts. This is the correct place to **gate** casting on Botania mana availability and to substitute cost pools. Access via `event.getSpell()`, `event.getSpellLevel()`, `event.getCastSource()`, `event.getMagicData()`. If `event.setCanceled(true)` is called, the cast aborts with no mana or cooldown spent — critical for graceful "not enough Botania mana" handling.

**`ChangeManaEvent` (Forge bus, cancellable, exposes `setNewMana(int)`)** fires inside `MagicManager.setPlayerCurrentMana` immediately before the write. This is the correct place to **redirect** the debit: cancel the event, leave ISS mana untouched, and instead call `ManaItemHandler.INSTANCE.requestManaForTool(stack, player, convertedCost, true)` against a tablet/ring in the player's inventory. The discount-aware variant automatically applies Manasteel/Terrasteel/Manaweave set-piece discounts — which means your bridge gets Botania's armor-bonus system for free.

**`ModifySpellLevelEvent`** lets Botania relics and armor bump the effective spell level. Example: Terrasteel helm grants +1 level to nature-school spells. `event.addLevel(1)` is all it takes.

**`SpellSelectionManager.SpellSelectionEvent`** lets the bridge **inject virtual spell-wheel slots** keyed to held items. A held Living Rod could grant access to a "Living Roots" spell without needing a spellbook. `event.addSelectionOption(new SpellData(spellId, level), "botania_wand", 0)` is the pattern.

**`RegisterConfigParametersEvent` and `ModifyDefaultConfigValuesEvent` (mod bus)** allow registering new per-spell config keys and overriding ISS's own spell defaults — useful if the bridge wants to add "botania_mana_cost" as a parallel config key on every spell.

**`AttributeRegistry` (`DeferredHolder<Attribute, Attribute>`)** is where the ten attribute families live: `MAX_MANA`, `MANA_REGEN`, `SPELL_POWER`, `SPELL_RESIST`, `COOLDOWN_REDUCTION`, `CAST_TIME_REDUCTION`, `SUMMON_DAMAGE`, and nine `{school}_spell_power` / `{school}_magic_resist` pairs. For a Botany school, register `BOTANY_SPELL_POWER` and `BOTANY_MAGIC_RESIST` via your own `DeferredRegister<Attribute>`.

**`SchoolRegistry` (`DeferredRegister<SchoolType>`)** registers the new school and ties it to a focus item-tag (e.g. `#irons_botany:botany_focus` on a Pixie Dust / Mana Pearl / Dreamwood Twig), the two attributes above, a cast sound, and a damage-type JSON at `data/irons_botany/damage_type/botany.json`.

**Known gotchas the review must flag:** (1) ISS events are **server-side only** — there is no `ClientSpellPreCastEvent` (open issue #955); client-side Botania visual effects require mixin or client-sync packets. (2) In 1.20.1 `MagicData` is a Forge capability; in 1.21.1 it is a NeoForge data-attachment — a multi-loader release needs an adapter. (3) The 3.15 package rename (`io.redspace.ironsspellbooks.irons_spellbooks` → `io.redspace.irons_spellbooks`) breaks reflective access; pin your `compileOnly` to a stable 3.15.x. (4) `ISpellContainer` migrated from NBT to Data Components in 1.21 — the spell-container code needs version-gating. (5) `AbstractMagicProjectile`'s piercing/homing additions in 3.15 are explicitly marked "API unstable"; isolate usage behind an adapter.

## 4. Botania architecture — extensibility points that matter for ISS integration

Botania 1.20.1-451 (Feb 19 2026, Forge-only; 1.21 port is on a branch, no release) exposes `vazkii.botania.api.*` via a **ServiceLoader-resolved `BotaniaAPI.INSTANCE`** (Xplat pattern — loader-independent). Maven:

```gradle
repositories { maven { url 'https://maven.blamejared.com' } }
compileOnly 'vazkii.botania:Botania:1.20.1-451:api'
runtimeOnly 'vazkii.botania:Botania:1.20.1-451'
```

The **eight highest-leverage hook points** for an ISS bridge are:

**`ManaItemHandler.INSTANCE.requestManaForTool(stack, player, manaToGet, remove)`** is the keystone. It drains from all of a player's `IManaItem` instances (Mana Tablet, Mana Ring, Mana Mirror) with armor-discount applied. One line converts any spell cost into a Botania-mana debit. There is also a non-discount `requestMana`, an exact-match variant, and a `dispatchMana` for refilling.

**`IManaItem` + `IManaUsingItem`** on a custom spellbook makes it behave like a Mana Tablet: the HUD renders its charge, Dispersive Sparks will push mana into it, and every `ManaItemHandler` lookup in the inventory will see it. This is the shortest path to "Mana-Enchanted Spellbook."

**`BotaniaForgeCapabilities.MANA_RECEIVER`** exposed on a custom `BlockEntity` turns a "Spell Altar" block into a full citizen of the mana network — spreaders will shoot at it, sparks will feed it, and `isFull()` gating works out of the box.

**`SparkAttachable`** on that same Spell Altar lets the player attach a Spark, raising throughput to 1 000 mana/tick and letting multiple pools feed one altar simultaneously. Combined with the Dominant/Recessive/Isolated augments, this gives emergent routing behavior for free.

**Recipe-type extension via pure datapack JSON**: `botania:petal_apothecary`, `botania:runic_altar`, `botania:mana_infusion`, `botania:elven_trade`, `botania:pure_daisy`, `botania:terra_plate`, `botania:brew`, `botania:orechid`. Every ISS item (scrolls, ink, focuses, spellbooks, upgrade orbs) can be made a Botania recipe output without one line of Java. The Runic Altar is the natural home for scroll-forging; the Terra Plate is the natural home for Legendary Spellbook agglomeration; the Alfheim Portal is the natural home for the Ancient Codex or a Gaia-tier focus.

**`ManaDiscountArmor`** + `ManaDiscountEvent` hooks let a new mage-robe armor piece grant a stacking mana-cost discount that combines cleanly with Manasteel/Terrasteel without mixin.

**`BrewRecipe` + `BotaniaAPI.getBrewRegistry().register(...)`** maps ISS status effects onto Botania's Brew system, so Botanical Brewery flasks can carry spell-mod effects. This is pure-content and extremely cheap.

**`BotaniaAPI.INSTANCE.registerCorporeaNodeDetector(detector)`** lets a held spellbook (or a "Spell Library" block) appear as an inventory to the Corporea network. `/corporea give me 4 fireball_scrolls` becomes a real request. Few addons use this surface — it is a defining feature opportunity.

**Subtile extension** for custom flowers: extend `GeneratingFlowerBlockEntity` or `FunctionalFlowerBlockEntity` (package `vazkii.botania.common.block.flower.*`), register via `DeferredRegister<Block>` with `SpecialFlowerBlock` and a matching `BlockEntityType`, and wire a BER. `SpecialFlowerBlockEntityRenderer` can be reused. There is **no central registry to add yourself to** — just register your block and it works polymorphically.

Two important caveats for the bridge: (1) Some interfaces were renamed during the 1.16→1.18 rewrite (drop of `I` prefix — `ManaReceiver` now, `IManaReceiver` in older branches). Pin to the 1.20.1-451 API. (2) Full-set armor effects beyond discount are partly hardcoded in Botania; addons can register discount hooks cleanly but not arbitrary set bonuses without mixin.

## 5. Enhancement plan

### 5a. Content expansion roadmap

The content layer should stop being "one school, some spells, some armor" and become a **coherent sub-mythology**: a **Botany school** of Iron's Spells 'n Spellbooks that flavors into Botania's fae-magic, mana-stream, Alfheim, and Gaia-guardian themes. Priority tiers below are numbered P0 (must-ship in v1.2), P1 (v1.3–1.4), P2 (v2.0).

**Botany-school spells (P0–P1).** The school should have a laddered set of 10–14 spells across rarities, each subclassing `AbstractSpell`. Concretely: *Manabolt* (common instant, Botania-colored Mana-Burst projectile reusing `ManaBurstEntity` visuals), *Living Roots* (uncommon long-cast, entangles targets in a 3-block radius and spawns temporary livingwood), *Petal Storm* (uncommon continuous channel, AoE damage + slow, uses `botania:petals/*` tag for particle colors), *Pure Daisy Gale* (rare instant, transforms blocks in a cone using the `PureDaisyRecipe` registry as the transformation table), *Mana Siphon* (rare instant, drains mana from pools in range and refills the caster's tablets — inverts the usual flow), *Ley Line* (rare long-cast, places a persistent mana-burst tether between two mana pools for passive transfer), *Heart of Gaia* (epic recast, summons a short-lived Pixie that repeats the caster's next three spells at reduced cost), *Terraform* (epic continuous, regrows foliage and self-plants flowers in a radius — use the `botania:mystical_flowers` tag for output variety), *Alfheim Step* (legendary teleport, requires a nearby Alfheim portal, fires `SpellTeleportEvent` for curio compat), *Guardian's Ward* (legendary, taps Gaia Spirit — consumes an inventory Gaia Spirit and grants a potent ward buff). Each spell should register `SpellConfigParameter`s for mana cost, cooldown, and damage so operators can datapack-tune.

**Generating flowers themed to ISS schools (P1).** Six new generating flowers, each producing mana from the activity of a matching ISS school happening in its radius. *Emberlily* (fire) generates mana when a fire-school spell deals damage within 8 blocks. *Frostbud* (ice) generates on freezing damage. *Stormbloom* (lightning) on lightning casts or natural strikes. *Sanctuary Lily* (holy) on undead-damage events. *Voidpetal* (ender) on teleport events. *Bloodweed* (blood) on `LivingHurtEvent` from the caster themselves. This turns the ISS ecosystem into Botania fuel and is the cleanest bidirectional integration — the `SpellOnCastEvent` handler scans for the appropriate flower BE in range and credits mana via `receiveMana(int)`.

**Functional flowers that cast ISS spells (P2 — the signature feature).** A new subtile *Verdant Caster* binds to a spell scroll placed in it (data-component binding in 1.21, NBT in 1.20.1), pulls from a connected `ManaPool`, and casts the stored spell at nearby mobs at a configurable interval. Mana cost per cast is the spell's normal ISS cost multiplied by a config ratio (default 4×, since flower mana is abundant). Implementation: subclass `FunctionalFlowerBlockEntity`, override `tickFlower()` to call `spell.onCast(level, spellLevel, fakePlayer, CastSource.OTHER, fakeMagicData)`, use a cached `FakePlayer` as caster. This is the "**mana network as caster**" vision and deserves v2.0's headline.

**Mana-infused items (P0–P1).** A **Manasteel Staff** (extends ISS `StaffItem`, adds `IManaItem` with 50k buffer, repairs via mana, grants +5% cast speed passive). A **Terrasteel Spellbook** (12 slots like Ancient Codex, `withSpellbookAttributes(new AttributeContainer(MAX_MANA, 300), new AttributeContainer(BOTANY_SPELL_POWER, 0.15))`, implements `IManaUsingItem`, draws 1 mana/tick passively from player tablets to refill ISS mana at a 10:1 ratio). **Elementium Scroll** (scrolls that pull their cost from the mana network — override scroll behavior so `CastSource.SCROLL` casts debit Botania mana instead of being free, gated by a config — this solves the "scrolls are too cheap" complaint). **Living Rod Focus** (focus item tagged in `#irons_spellbooks:focus/botany`, usable at the Scroll Forge to generate Botany-school scrolls).

**Petal Apothecary–crafted catalysts (P0).** Three tiers of "Mana Ink" (`MINOR_MANA_INK`, `GREATER_MANA_INK`, `PRIME_MANA_INK`) output by petal-apothecary JSONs, replacing vanilla ink for the Scroll Forge at rarity Uncommon / Rare / Epic respectively. This is **zero Java code** — pure datapack JSON under `data/irons_botany/recipes/petal_apothecary/*.json`.

**Runic Altar recipes for ISS materials (P0).** `data/irons_botany/recipes/runic_altar/*.json` converts Rune of Mana + spell ingredient → Upgrade Orb of Mana; Rune of Fire + Blaze Rod → Fire-school spell-power orb; Rune of Earth + Manasteel Ingot → Cooldown Reduction orb; Rune of Spring + Sculk Sensor → Nature-school power orb. The 16-rune system maps naturally onto ISS's 9 schools plus 7 generic buff orbs.

**Terra Plate recipe for the Legendary Spellbook (P1).** Terrasteel Ingot + Mana Pearl + Mana Diamond + Gaia Spirit + Ancient Codex + 500 000 mana on the Terrestrial Agglomeration Plate → **Arcane Codex** (14 slots, +300 max mana, +20% CDR, +15% spell power). This is a natural progression gate and uses Botania's most iconic ritual.

**Alfheim Portal trades (P1).** Elven trades convert Manasteel items → Elementium variants — e.g. Manasteel Spellbook → Elementium Spellbook (+pierce on imbued weapon hits, pixie spawn on damage). Three to five recipes via `data/irons_botany/recipes/elven_trade/*.json`.

**Pure Daisy transformations (P2).** Pure Daisy converts a Normal Spellbook into a Livingwood Spellbook (cosmetic + thematic starter gate). Stone Altar into Dreamrock Altar. This is flavor but cheap.

**Gaia Guardian drop integration (P1).** Add `irons_spellbooks:epic_scroll` and `irons_spellbooks:legendary_spellbook` to the Gaia II drop table via `data/botania:loot_tables/gameplay/gaia_guardian_2.json` overlay — making the guardian a source of late-game ISS content.

**Curio accessories (P1).** Three new curios in ISS-recognized slots. **Mana Reservoir Ring** (`curios:ring`): implements `IManaItem` with 200k capacity, +100 `MAX_MANA`, passively converts Botania mana to ISS mana at 20:1 when ISS mana < 50%. **Daybloom Amulet** (`curios:necklace`): +15% `NATURE_SPELL_POWER`, +5% `CAST_TIME_REDUCTION` in daylight. **Gaia's Blessing** (`curios:necklace`, legendary): +1 level to all Botany-school spells via `ModifySpellLevelEvent`, consumes Mana Pool mana in a 16-block radius when casting.

**Datapack-driven content (P0, foundational).** Every number above should be a JSON somewhere. Ship `config/irons_botany/_example.*.json.disabled` templates for spell costs, flower radii, mana conversion ratios, upgrade-orb outputs, and school attribute scaling. Follow the two-tier Locks Reforged pattern: baked-in defaults under `data/irons_botany/`, user overrides under `config/irons_botany/`, and datapack overrides at `data/<ns>/irons_botany/*_overrides/`. Add `/irons_botany reload` as runtime-reload.

### 5b. Architectural / integration depth roadmap

**P0 — mana substitution architecture.** Replace whatever single-path routing exists today with a **BridgeManager analogue** modeled on Ars 'n Spells. Create `com.otectus.irons_botany.bridge.ManaBridgeManager` with modes `iss_primary` (default, Botania supplements), `botania_primary` (ISS mana regen suppressed, all costs pull Botania first), `hybrid` (shared bar, configurable split), `separate` (normal independent pools), `disabled`. Expose a `ManaResolutionResult(int issCharged, int botaniaCharged, boolean ok)` record from a single `resolveCost(Player, AbstractSpell, int level, CastSource)` entry point. Call this from `SpellPreCastEvent.Low` and `ChangeManaEvent.High` — cancellation of the `ChangeManaEvent` prevents ISS's debit, then the manager applies its own.

**P0 — Ars 'n Spells coexistence.** On modlist detection of `ars_n_spells`, auto-switch to a `defer_to_ars_n_spells` mode: Iron's Botany subscribes at `EventPriority.LOWEST` and only acts on the residual cost that Ars 'n Spells's `BridgeManager` didn't already cover. Concretely, read a shared ThreadLocal (or publish a custom `CostResolvedEvent`) to check "already handled." Coordinate with the sibling mod by adding an explicit `arsNSpells.costRoutedTag` on `MagicData` (data attachment key) and respecting it. This is the **single most important interop work** and should ship as a v1.1 hotfix if not in v1.0.

**P1 — mana network participation.** Make the spellbook a first-class Botania citizen by implementing `IManaItem` + `IManaUsingItem`. Make a new **Arcane Mana Altar** block implement `IManaReceiver` + `IManaPool` (exposed via `BotaniaForgeCapabilities.MANA_RECEIVER`) + `SparkAttachable`. The altar becomes the bridge between a Botania pool and a held spellbook: stand within range, and any cast debits the altar's pool directly. This collapses "mana routing" into "block placement" and is Botania-native.

**P1 — Corporea integration.** Register a `CorporeaNodeDetector` that treats a nearby Scroll Rack (new block — a 27-slot inventory tagged `#botania:storage`) as a Corporea node. Register an index interceptor that responds to `/corporea give me 2 fireball scrolls` by scanning matching scrolls. As a stretch, register a `CorporeaRequestMatcher` subclass that matches scrolls by spell ID, not by item type, so `/corporea give me 1 fireball level 3 scroll` works.

**P1 — deep event bridging.** Subscribe `SpellOnCastEvent` → scan for Botany flowers in a 16-block radius → dispatch to flower-specific hooks. Emberlily.onNearbyCast(FireSchoolSpell) adds 500 mana. Sanctuary Lily.onHolyDamage() adds 200 per damage. This needs careful batching: cache the BE scan per (player, tick) to avoid O(N×M) lookups in the hot cast path.

**P2 — mana-cost conversion as a first-class config surface.** Each ISS spell gets a parallel `botania_mana_cost` value (default = `iss_mana_cost * 100`) exposed via `RegisterConfigParametersEvent`. Players and server operators datapack-override per spell. This is more flexible than a global ratio and replaces the inevitable hardcoded `* 100` multiplier buried somewhere in the current mana bridge.

**P2 — custom subtile registration API.** Expose `com.otectus.irons_botany.api.BotanySchoolFlowerRegistry.register(ResourceLocation, Class<? extends FunctionalFlowerBlockEntity>)` so other mods (e.g. someone writing a hypothetical Iron's Extra Spells bridge) can ship new school-themed flowers that Iron's Botany recognizes for things like tooltip localization, creative-tab grouping, and Patchouli page generation. This is the v2.0 "addon-for-addon" opportunity that Mythic Botany never quite built.

**P2 — attribute synergies.** Register an `ArmorSetBonusEvent`-like listener (or use `LivingEquipmentChangeEvent` since Botania's set-bonus system is partially hardcoded) that detects Terrasteel full-set and applies `+0.25 SPELL_POWER`, `+0.15 MANA_REGEN`, and `+0.10 CAST_TIME_REDUCTION` modifiers through `CurioAttributeModifierEvent`. Elementium full-set: +20% pierce on imbued-weapon spell damage (mixin into `MagicSwordItem#hurtEnemy`). Manasteel full-set: baseline +50 `MAX_MANA`. This gives Botania armor genuine spell-mod identity.

**P2 — KubeJS surface.** ISS ships `irons_spells_js`; Botania ships KubeJS Botania. Iron's Botany should ship `irons_botany_js` exposing `StartupEvents.registry('irons_botany:botany_flowers')` for datapack flower registration and `ServerEvents.recipes` helpers like `event.recipes.irons_botany.spell_manafusion(outputScroll, catalystSpell, manaCost)` for compound recipes that span both mods. This matches the Effective Instruments precedent of live-reloadable JSON.

**P2 — performance hygiene.** Tick-heavy subtile + spell event interactions need two specific protections. First, the `SpellOnCastEvent` flower-scan must use a chunk-scoped cache (invalidate on `ChunkEvent.Load/Unload`) rather than full world BE iteration. Second, `FunctionalFlowerBlockEntity.tickFlower()` for spell-casting flowers should run every N ticks (N configurable, default 20) and use `IForgeBlockEntity.getCapability(...)` for the `MagicData` attachment rather than capability lookup per tick. Third, `FakePlayer` allocation for flower-cast spells must be pooled — one per world, not per flower per tick.

**P2 — networking and sync.** New packets are needed for: (1) HUD bar mode state (client needs to know which of the 4 modes is active to render correctly), (2) Altar-binding state (which pool the altar is feeding from), (3) Corporea-scroll response (for the index's chat feedback to include scroll metadata). Follow Effective Instruments's versioned network protocol pattern (`NETWORK_VERSION = 3`) so mismatched client/server don't crash — they disconnect with a clear message.

### 5c. Cross-compat notes for Ars 'n Spells

Three concrete interop needs must be engineered, not left to chance. **First, event priority coordination.** Document a shared convention: Iron's Botany subscribes to `SpellPreCastEvent` at `EventPriority.LOW` and `ChangeManaEvent` at `EventPriority.LOW`; Ars 'n Spells already uses `NORMAL`. This leaves `HIGHEST` free for users' own KubeJS scripts. **Second, a shared cost-routing tag.** Both bridges should read and write to a namespaced transient key on `MagicData` — e.g. `irons_botany.cost_already_routed` and `ars_n_spells.cost_already_routed` — so that when both mods are present they can early-exit cleanly. **Third, a mana priority chain.** When all three resource systems (Botania mana, Ars source, ISS mana, plus optional Covenant LP/Aura) are present, expose a user-configurable ordering — `priority_chain = ["botania", "ars", "iss", "lp", "aura"]` — consumed by both bridges. This elevates the two mods from "racing peers" to "cooperative pipeline stages."

Practically, ship a tiny **`otectus-mana-bridge-common`** jar-in-jar dependency shared between both mods, containing just the `IManaSource` SPI, the cost-routed tags, and a `ManaPriorityChain` resolver. Both mods can evolve independently while sharing the one contract that matters. If Otectus is reluctant to fragment the codebase, the minimum-viable version is for Iron's Botany to declare a soft-dep on Ars 'n Spells and duck-type-import the `BridgeManager` via reflection inside an optional integration class (`com.otectus.irons_botany.integration.arsnspells.AnSBridgeAdapter`), matching Iron's Botany's broader pattern of optional-integration modules.

One cross-bridge content opportunity is underrated: **an Ars Nouveau ritual that inscribes Botania runes.** Ars 'n Spells registers `ars_n_spells:spell_transcription`; a complementary `irons_botany:rune_inscription` ritual would let Ars glyphs produce the Runic Altar's rune items directly, weaving the three mods together at the lore level. This is a v2.x reach goal.

### 5d. Quick wins vs long-term vision

**Quick wins (ship within two weeks of reading this).** Add priority annotations to all ISS event subscribers. Ship the Ars 'n Spells detection + deferral handler. Add the Petal Apothecary → Mana Ink recipe JSONs. Add the Runic Altar → Upgrade Orb recipe JSONs (pure datapack, no Java). Add the Terra Plate → Arcane Codex recipe. Add a `/irons_botany reload` command. Open a public GitHub repo with a `CLAUDE.md`, README table, and CHANGELOG matching the Otectus template — this alone would bring the mod in line with Ars 'n Spells's polish tier. Ship a Lexica Botania Patchouli entry (pure JSON under `assets/irons_botany/patchouli_books/lexicon/en_us/entries/…`) — Botania users live in that book and the mod is currently invisible there.

**Long-term vision (v2.0, 6–12 months out).** The "mana network as caster" arc. Functional flowers casting ISS spells. Spreaders firing spells down their burst vector. Mana pools acting as per-player mana reservoirs when the player is nearby. Corporea scroll library. Gaia Guardian fight modifier that scales with equipped spellbook power. A Patchouli Lexica Botania mini-book-inside-a-book for the Botany school's lore. Full cross-bridge interop with Ars 'n Spells via the shared SPI jar.

## 6. Technical risks, considerations, open questions

**ISS's client-side event gap (issue #955) forces mixin for any client-synchronous Botania visual.** Keep mixins scoped to `client/` source set and isolated in their own mixin JSON so they cannot break server-side integration. Prefer sending a custom packet from the server-side `SpellOnCastEvent` handler.

**The 1.21 port gap.** Botania 1.21 has no official release; ISS has NeoForge 21.1.125 and 1.21.1-3.15.5. If Iron's Botany wants to track ISS to NeoForge, it must either wait for Botania's port or temporarily restrict to 1.20.1 until Botania catches up. Plan documentation around this.

**Attribute cap behaviors differ between ISS and Botania.** ISS attributes are `RangedAttribute`s with explicit min/max. When stacking Botania armor bonuses onto `SPELL_POWER`, verify the max isn't exceeded silently (ISS clamps without warning). Add logging at `DEBUG` level for attribute computations.

**Upgrade-orb stacking interactions.** A Manasteel Helm that gives +10% `NATURE_SPELL_POWER` combined with a Nature Spell Power Orb upgrade combined with a Terrasteel set bonus can exceed intended scaling. Ship a config cap mirror of Ars 'n Spells's additive-with-configurable-cap model.

**Save compatibility.** If the bridge adds a player capability/attachment for cross-mod state, every save format change needs migration code. Otectus's Locks Reforged tracks schema versions in config — follow that pattern.

**Botania's multi-loader split.** The Xplat service pattern works but can trip up IDE compilation. Iron's Botany should follow Botania's own pattern of `compileOnly` against the `:api` classifier and `runtimeOnly` against the full jar, never against subprojects.

**Open questions for the developer.** Is Iron's Botany intended to ship for NeoForge 1.20.4+/1.21 or is it 1.20.1-only long-term? Should the mod ship a Fabric port once Botania's 1.21 port lands? Is the "new school" intended to be a single all-purpose "Botany" school or multiple sub-schools (Flora, Fae, Mana)? What is the canonical focus item for the new school — Mana Pearl, Pixie Dust, Dreamwood Twig, or a mod-new item? Does the mod already ship a Patchouli entry into Lexica Botania, and if not, is Patchouli an acceptable dependency? Finally — and critically — **is the source code going to be published to GitHub to match Ars 'n Spells?** This decision shapes community contribution and bug-report quality.

## 7. Suggested version roadmap

**v1.0 (shipped, April 7 2026).** Botany school, new spells, new armor, single-mode mana routing. Content beachhead. Known gaps per §2.

**v1.1 (hotfix, two-week turnaround).** Event priorities explicit. Ars 'n Spells detection + deferral adapter. `/irons_botany reload` command. GitHub repo published with README, CHANGELOG, CLAUDE.md, TESTING_GUIDE.md matching the Locks Reforged / Ars 'n Spells template. Mixin JSONs split per integration target.

**v1.2 (one month).** Five-mode mana unification (`iss_primary` / `botania_primary` / `hybrid` / `separate` / `disabled`) with TOML configuration. Petal Apothecary and Runic Altar recipe JSONs for ISS materials (Mana Ink tiers, Upgrade Orbs). Terra Plate recipe for the Arcane Codex. Lexica Botania Patchouli entries for the Botany school. Per-spell `SpellConfigParameter<Integer> botania_mana_cost`.

**v1.3 (two months).** Six ISS-school-themed generating flowers. Three new curios (Mana Reservoir Ring, Daybloom Amulet, Gaia's Blessing). Alfheim Portal trades for Elementium-variant spellbooks and staves. Gaia Guardian drop table extension. Elementium Scroll pulling cost from the mana network.

**v1.4 (four months).** Arcane Mana Altar block (`IManaPool` + `SparkAttachable`). Terrasteel / Elementium / Manasteel full-set spell-power bonuses. Ley Line spell. Manasteel Staff. Terrasteel Spellbook.

**v2.0 (six to nine months — the headline release).** Verdant Caster (functional flower that casts ISS spells). Spreader-fired spell bursts. Corporea integration (Scroll Rack, index voice commands, `CorporeaRequestMatcher` for spells). Custom subtile registration API (`com.otectus.irons_botany.api.BotanySchoolFlowerRegistry`). KubeJS surface (`irons_botany_js`). Shared `otectus-mana-bridge-common` SPI with Ars 'n Spells. Full Patchouli mini-book for Botany-school lore. Performance-hardened event scanning with chunk-scoped caches.

**v2.1+ (stretch).** NeoForge 1.21.x port (gated on Botania's own 1.21 release). Fabric port if Botania's 1.21 Fabric build lands. Cross-bridge ritual with Ars 'n Spells (`irons_botany:rune_inscription`). Gaia III variant fight with spell-modifier mechanics.

## Conclusion

Iron's Botany is a promising but architecturally thin first release whose surface area — one school, a few spells, some armor — sits on top of two of the richest extension APIs in the Minecraft mod ecosystem. The single most valuable next step is **not** more content but **coexistence engineering with Ars 'n Spells**; the second is **participation in Botania's mana network** (via `IManaItem` on spellbooks and `BotaniaForgeCapabilities.MANA_RECEIVER` on a new altar block); the third is **treating the Runic Altar and Terra Plate as pure-JSON content vectors** that cost almost nothing to ship but unlock satisfying progression curves. The v2.0 "mana network as caster" vision — flowers and spreaders that cast ISS spells natively — is the feature that would distinguish Iron's Botany from every other ISS bridge and is reachable in three to four versions if the foundation is laid deliberately in v1.1 through v1.4. The developer's proven pattern with Ars 'n Spells (five modes, BridgeManager, resonance, affinity, configurable LP chain) is a ready-made template; apply it, publish the source, and Iron's Botany graduates from a content patch to the defining Botania-spell integration.