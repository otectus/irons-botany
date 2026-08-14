# Iron's Botany: next major update investigation and implementation brief

## Mission

Prepare the next major Iron's Botany release as a stability, compatibility, and correctness update. Start from evidence, reproduce the reported failures, fix the underlying contracts rather than their visible symptoms, preserve existing worlds and items, and do not add unrelated features until the release-blocking work is complete.

The release must address these user reports:

- Version 1.9.0 causes enough regressions that a user downgraded to 1.7.0.
- Spell scrolls appear to lack icons and the game crashes when ISS-Scroll Descriptions or ISS-Restrictions inspects them.
- A magic texture renders as the purple-and-black missing-texture pattern.
- Interactions are unreliable or broken.

Also use the repository findings below to correct other bugs, logical inconsistencies, misleading features, and avoidable performance costs.

## Operating rules

1. Treat the work as a staged repair project: reproduce, write a failing test where feasible, plan, implement, validate, then release.
2. Do not claim a bug is fixed because a source file exists or the project compiles. Verify behavior in a clean runtime and verify the exact JAR that will be published.
3. Do not replace or regenerate the existing spell art unless runtime evidence shows the art itself is invalid. The audited 1.9.0 source and locally built JAR already contain valid-looking icons.
4. Do not silently discard compatibility data. Any registry, school, spell, item, capability, or NBT change needs an explicit migration decision and a regression test.
5. Make costs and consumptions transactional. A canceled or failed cast must not consume mana, a catalyst, a reagent, a scroll, durability, or a cooldown.
6. Keep client-only code out of dedicated-server class-loading paths and make the server authoritative for gameplay state.
7. Prefer supported APIs over reflection, broad exception handling, negative-value API tricks, or tick-scoped persistent-NBT flags.
8. Keep commits small and ordered by the phases in this document. Record the reproduction evidence and test result in each relevant commit or pull-request description.

## Repository and release baseline

Resolve the source-of-truth problem before editing gameplay code:

- The checked-out `main` branch currently identifies itself as 1.7.2.
- The `v1.9.0` tag exists on a separate/divergent line of history and contains the reported release's large feature set.
- The next-update branch must therefore be created from an explicitly documented base. Normally that should be `v1.9.0` plus any intentional fixes from `main`, not an accidental implementation on the current 1.7.2 tree.
- Preserve both histories until the base decision is reviewed. Do not force-reset or overwrite either line.
- Compare the public 1.9.0 download with the `v1.9.0` tag. If they differ, treat the release-pipeline discrepancy as a release blocker and save checksums plus a JAR-entry diff.

Record the following in an investigation note before implementation:

- base commit and branch strategy;
- exact Forge, Minecraft, Java, Botania, Iron's Spells 'n Spellbooks, Curios, ISS-Scroll Descriptions, and ISS-Restrictions versions;
- SHA-256 and size of every tested Iron's Botany JAR;
- whether the failure occurs in a new world, an upgraded world, or both;
- active resource packs, shaders, and rendering mods;
- full `latest.log`, crash report, and the first exception caused by Iron's Botany rather than the final wrapper exception.

## Audit facts that must inform the plan

These findings were verified against the repository's `v1.9.0` tag unless marked as a hypothesis.

| Priority | Finding | Required response |
| --- | --- | --- |
| P0 | `IBSchools.BOTANY` creates its display component without a text color. ISS 3.16.1's `SchoolType#getTargetingColor()` dereferences that style color without a null check. Add-ons that inspect school/targeting data can therefore encounter a null-pointer failure. | Give Botany an explicit, non-null RGB style and add a contract test that calls the same ISS accessors used by description/restriction add-ons. Confirm the actual crash stack before declaring this the sole root cause. |
| P0 | All nine spell classes put `SchoolRegistry.NATURE_RESOURCE` in their default config while `AbstractBotanicalSpell#getSchoolType()` returns the custom `ironsbotany:botany` school. | Define one canonical Botany resource ID and use it in registration, runtime school lookup, defaults, data, equipment, migration, and tests. |
| P0 | `ManaHelper#requestManaFromAllSources` passes each purported source stack to Botania's player-wide request API. That argument is the requesting/excluded stack, not an isolated source. Multiple passes can count the same mana repeatedly, partially drain on commit, and then report failure. | Replace this with a real transaction/plan. Prove that insufficient aggregate mana never changes any source. |
| P0 | The cast-routing identity is `(world tick, spellId.hashCode(), ISS cost)`, while Botania-paid and scroll-paid flags are tick-only. Multiple same-tick casts can collide or inherit payment/refund state. `ChangeManaEvent` can also cancel an unrelated debit in that tick. | Introduce a per-cast transaction identity tied to the actual cast and actual casting stack. Clear it on every terminal path. |
| P0 | Botania mana is charged during `SpellPreCastEvent`, but catalyst consumption and Corporea preparation happen later in `AbstractBotanicalSpell#onCast`. Reagent validation can fail after payment; catalysts can be consumed before reagent failure. Corporea placement itself can partially extract/place components before a later component fails. | Implement preflight/reserve/commit semantics across mana, catalysts, reagents, scroll retention, cooldown, and spell effect. |
| P0 | `TerrasteelBladeHandler` uses a negative `requestManaExact` amount to generate mana. This relies on unintended API arithmetic, can target a different item, and gives success feedback without proving that mana was accepted. | Use Botania's supported dispatch/addition mechanism or a capacity-aware capability operation, and report/proc only the amount actually accepted. |
| P1 | The tag source contains all nine 16×16 spell icons at `assets/ironsbotany/textures/gui/spell_icons/`, and a clean local 1.9.0 build packaged all nine. | Do not merely “add missing icons.” Compare the user's/public JAR, call each spell's runtime icon accessor, and test resource-manager resolution with both reported add-ons. |
| P1 | `arcane_mana_altar.png` exists, decodes, and is packaged by a clean build. Changelog history says it was once the purple/black cause, but the user still sees that symptom in 1.9.0. | Determine the exact rendered object/particle/UI element and inspect the published JAR and missing-resource log. Treat an invalid school color, another resource, a stale artifact, or resource-pack interaction as separate possibilities. |
| P1 | `ElementiumScrollItem` is produced by a standard Botania elven-trade JSON that declares only a bare output item. No Iron's Botany code copies the input ISS scroll's spell NBT into the output. | Confirm the runtime result, then replace it with a serializer/recipe that preserves and validates spell data, or redesign the item. Test every supported spell, stack count, malformed NBT, and automation path. |
| P1 | The rune-scroll fusion recipe writes `runeType` and `runeEnhanced`, but no code reads either value. It consumes a rune for a renamed scroll with no gameplay effect. | Implement a documented, bounded effect end to end or remove/disable the recipe and migrate existing cosmetic NBT safely. |
| P1 | Eight school-named upgrade-orb items reuse four unrelated orb-type keys. Their type JSON points at different container items/attributes, and their tooltip switch has no cases for the eight school names. The Flora orb still boosts ISS Nature rather than custom Botany. | Create correct unique orb types and translations, or remove the misleading items/recipes. Add data-driven identity and attribute-effect tests. |
| P1 | Custom Botany power/resistance and mana-efficiency attributes are registered but never added to Player attributes. Mana efficiency is not consumed by the cost router; its default is `1.0`, maximum is `1.0`, and equipment adds positive values, making the intended meaning internally contradictory. | Attach required attributes to supported entities, define one semantic model for efficiency, clamp it, apply it once, and test equipment stacking. |
| P1 | Livingwood Staff, Dreamwood Scepter, and Daybloom Amulet advertise or grant Nature power while primary mod spells are now Botany. Botanical Ring grants global power while its tooltip says Nature. | Decide and document which bonuses are Botany, Nature, global, or intentionally dual-school, then align code, data, tooltips, and guidebook text. |
| P1 | `SchoolMigrationHandler` still migrates `ironsbotany:botanical` to ISS Nature and uses a one-time boolean, despite 1.9.0 introducing canonical `ironsbotany:botany`. | Replace it with a versioned, idempotent migration. Never rewrite every Nature item; only migrate Iron's Botany-owned spell/item fields whose provenance is known. |
| P1 | The v1.9.0 build succeeds with 59 deprecation warnings but has no test sources. The existing `validateAssets` task checks source presence/zero length, not the final JAR, PNG decoding, dimensions, runtime icon resolution, or compatibility metadata. | Add automated unit/GameTests, clean client and dedicated-server smoke tests, and final-artifact validation. Treat warnings as a tracked maintenance budget. |
| P2 | `SpellContext` cooldown, mana-cost, and casting-speed multipliers are written by catalysts, auras, channels, Alfheim, and attunement but never read by the cast pipeline. | Move effective modifiers into the pre-cast cost/timing transaction or remove the advertised bonuses. Test that every tooltip/configured modifier changes observable behavior. |
| P2 | `FlowerAuraRegistry` caches only by player UUID although callers request radii 8 and 16. The first query can contaminate later queries; dimension/time changes can preserve invalid entries. A radius-16 miss scans 35,937 block positions. | Include radius, dimension, and relevant position/config generation in cache validity; clear on lifecycle events; replace repeated cuboid scans with a bounded spatial index or chunk-aware cache. |
| P2 | Large block-volume scans occur in pool lookup, Gaia's Blessing, Corporea, Alfheim, aura, automation, mana-network, and client HUD paths. | Centralize bounded spatial lookup, avoid scanning unloaded chunks, cache safely, instrument it, and set a measurable tick-time budget. |
| P2 | Client HUD recomputes item capabilities every render frame, may double-count overlapping mana-item/accessory lists, ignores `HUD_SCALE`, does not clamp fill, and performs a 2,197-position proximity scan each second. `PARTICLE_DENSITY` is also unused. | Deduplicate by stack identity/slot, cache event-driven totals, honor scale/density, clamp rendering values, and reset client caches on disconnect/dimension change. |
| P2 | Stored mana read from item and block NBT is not consistently clamped to `[0, capacity]`; several `current + amount` operations can overflow. Block transfer favors iteration order and can spend a full rate per player. | Validate NBT, use overflow-safe arithmetic, define a per-tick transfer budget, and distribute fairly. Sync only when state changes. |
| P2 | Several public/configured systems are dead or misleading: `ManaPriorityChain`/`IManaSource`, `SpellDrivenAutomation`, `AlfheimScrollCrafting`, unlock flags in `UnifiedAdvancementSystem`, most spell-triggered mana events, and `IBSounds`. | Either wire each system to an observable tested behavior or remove/deprecate it. Do not keep user-facing claims for inert code. |
| P2 | Botany spells resolve to a Nature mana-network trigger before name checks, but only the Water trigger has an implementation. The current feature is therefore effectively inert for this mod's spells. Its static state also uses overworld time for every dimension and is not cleared on server stop. | Disable/remove the unfinished feature by default unless implementing supported Botania API behavior. Fix dimension time and lifecycle cleanup if retained. |
| P2 | Every non-US language file has 178 keys versus 256 in `en_us`, leaving 78 keys to fallback. | Add a key-parity check. Preserve honest fallback text rather than machine-translating without review, and document the translation workflow. |
| P2 | Numerous config entries are explicitly reserved or unused: `BIDIRECTIONAL_CONVERSION`, `REVERSE_CONVERSION_RATIO`, `ENABLE_BOTANICAL_SCHOOL`, `ENABLE_CROSS_LOOT`, `UPGRADE_ORB_EFFECTIVENESS`, `CHANNEL_POWER_MULTIPLIER`, `ALLOW_AURA_STACKING`, `MANA_PRIORITY_CHAIN`, `MANA_EVENT_*`, `AUTO_REQUEST_REAGENTS`, `CORPOREA_SEARCH_RADIUS`, `ALFHEIM_POWER_MULTIPLIER`, and `ENABLE_DUAL_SCHOOL_SCROLLS`. | Implement, remove, or deprecate each key with migration. The generated config and guide must never imply that a no-op setting works. |
| P3 | `updateJSONURL` points at a normal releases page instead of a Forge update JSON document. Dependency resolution relies on local `compileOnly` JARs and no CI configuration is present. | Provide a valid update manifest or remove the URL; prefer stable Maven coordinates where available; pin/lock the supported dependency matrix; add CI from a clean cache. |

## Phase 0: reproduce and preserve evidence

Complete this phase before changing the likely crash site.

1. Build two isolated profiles:
   - minimum: Forge + Botania + Iron's Spells + Curios + Iron's Botany;
   - compatibility: minimum profile + ISS-Scroll Descriptions + ISS-Restrictions, one at a time and together.
2. Test the exact public 1.9.0 JAR first, then a clean JAR built from `v1.9.0`.
3. In a new world, obtain all nine spells as normal ISS scrolls and exercise inventory display, hover text, JEI/creative display if present, spellbook insertion, selection, restriction checks, casting, and resource reload (`F3+T`).
4. Repeat with an upgraded copy of a 1.7.0/1.7.2 world. Preserve an untouched backup.
5. Capture the first failing stack, the affected spell/item NBT, and every missing-resource line. Identify whether “magic texture” means the altar block, a spell icon, a particle, an entity renderer, a school-colored widget, or another asset.
6. Inspect JAR entries, not only `src/main/resources`. At minimum assert these final-artifact paths:
   - `assets/ironsbotany/textures/gui/spell_icons/{all nine registered spell IDs}.png`;
   - `assets/ironsbotany/textures/block/arcane_mana_altar.png`;
   - every texture referenced by item models, block models, blockstates, particles, and renderers.
7. Compare the public and locally built JARs by entry name, uncompressed size, and content digest. Ignore ZIP timestamp-only differences.
8. Add a regression test that reproduces the crash or nearest callable contract before applying the fix.

Exit criteria:

- the report is reproduced, or a written evidence table shows every attempted matrix combination and why the cause remains unconfirmed;
- the public artifact is proven to correspond to a source commit, or release provenance is declared broken;
- the exact resource or Java contract behind each symptom is named.

## Planning gate

Before implementation, turn Phase 0 evidence and the audit table into a reviewed execution plan containing:

1. a root-cause map that links each user symptom to a stack trace, resource ID, metadata contract, or still-open hypothesis;
2. architecture decisions for the authoritative branch, canonical school ID, cast transaction, source aggregation, config compatibility, and migration schema;
3. an ordered issue list with owner, priority, dependencies, affected saves/APIs, test to add, and rollback strategy;
4. a scope boundary that defers new content until P0/P1 stability gates pass;
5. a test matrix identifying what can be automated and what requires the real client/add-on profile;
6. a commit/PR sequence that keeps school/resource fixes, mana transaction changes, migrations, performance work, and release engineering independently reviewable;
7. explicit decisions for every inert or misleading system: implement, remove, or deprecate.

Do not begin a broad refactor without failing tests or measurements for the behavior it replaces. Revisit the plan when reproduction disproves an assumed cause, and record that change rather than quietly shifting scope.

## Phase 1: compatibility, school, icon, and texture repairs

### 1.1 Make the Botany school a complete ISS school

- Add a canonical `BOTANY_RESOURCE` constant and use it everywhere.
- Style `Component.translatable("school.ironsbotany.botany")` with an explicit, stable RGB color. Select a readable color with adequate contrast and reuse it for school UI/targeting where appropriate.
- Add Botany power and resistance attributes to Player through the correct Forge attribute-modification event. Add them to other living entities only if they can legitimately cast or resist Botany spells.
- Test all public ISS school getters used by UI/add-ons, including display component style/color, targeting color, power, resistance, focus tag, cast sound, and damage type.
- Change every Botanical spell's default school resource from ISS Nature to canonical Botany. Verify all nine registered spells agree across runtime school, default config, resolved config, scroll NBT, commands, loot, and add-on inspection.
- Do not use a null fallback, a catch-all `try/catch`, or a fake texture to conceal invalid metadata.

### 1.2 Validate icons at runtime and in the final artifact

- Enumerate spells from the actual spell registry rather than a regex over `IBSpells.java`.
- For each Iron's Botany spell, obtain its icon resource through the same ISS API that add-ons call, resolve it with Minecraft's resource manager, open the stream, decode the PNG, and assert expected nonzero dimensions and alpha content.
- Add a final-JAR task that performs equivalent entry and PNG checks after `jar`/`reobfJar`.
- Run the add-on compatibility profile and inspect all nine scrolls. A missing icon, error placeholder, null component, or exception fails the gate.

### 1.3 Diagnose the purple/black render separately

- Turn on missing-model/texture logging and inspect the exact resource ID requested by the renderer.
- Validate namespace, path, case, model parent, blockstate variant, particle atlas entry, and renderer constant.
- Test a clean resource pack, resource reload, reconnect, new world, and upgraded world.
- If only the public JAR fails, repair publishing and add artifact provenance checks. If both JARs fail, repair the actual reference or metadata contract.
- Add a screenshot-based manual QA record for all three blocks, all custom particles/entities, all nine spell icons, held/equipped items, armor layers, and the mana HUD.

## Phase 2: replace mana routing with one cast transaction

Design one server-side transaction object per cast. It should contain a unique ID, player, spell, level, cast source, exact casting hand/stack, computed ISS and Botania costs, modifiers, selected mana sources, reserved consumables, state, and diagnostic reason.

Required state flow:

1. `NEW` — gather immutable cast inputs.
2. `PREFLIGHTED` — compute costs/timing once; validate cooldown, spell, source item, mana, catalysts, reagents, and placement without mutation.
3. `RESERVED` — reserve exact unique sources/components without making externally visible partial effects.
4. `COMMITTED` — debit once, consume once, apply the spell once, and emit feedback once.
5. `ROLLED_BACK` or `CANCELED` — release reservations and restore any mutation that happened before failure.
6. `CLOSED` — remove all transient state in a `finally`/terminal path.

Implementation requirements:

- Deduplicate inventory, Curios, accessories, mirrors, nearby pools/altars, and any future sources by stable source identity.
- Respect `ENABLE_INVENTORY_MANA_SOURCES`, mirror support, pool access, and the documented priority chain only if those options remain public.
- Never infer per-item availability by repeatedly invoking a player-wide Botania request method with different requester stacks.
- A cost may aggregate multiple sources only if the commit plan can debit the exact planned amounts. If Botania cannot guarantee atomic aggregation, prefer one exact source or implement compensating rollback proven by tests.
- Use overflow-safe conversion (`long` during multiplication), validate ratios above zero, define rounding once, clamp after all modifiers, and reject non-finite multiplier inputs.
- Apply mana efficiency, armor, channel, catalyst, aura, attunement, and add-on modifiers exactly once and in a documented order.
- Bind channel and Elementium-scroll behavior to the actual casting stack, not whichever eligible object happens to be in either hand.
- Replace tick/hash persistent tags with the transaction ID or an ISS-provided cast identifier. Never allow an unrelated `ChangeManaEvent` to inherit a refund.
- Narrow exception catches to expected integration failures, log enough source context to diagnose them, and fail safely. In a mode that requires Botania payment, an internal routing error must cancel without debit rather than silently granting the cast or changing its configured economy.
- Centralize Dreamwood conversion and Gaia's Blessing in this transaction. Do not have equal-priority subscribers race for ownership.
- Charge Gaia's Blessing only for a committed cast, not for arbitrary `ModifySpellLevelEvent` queries.
- Replace negative mana-generation calls with supported positive dispatch/capability additions. Award particles, advancements, and cooldown only when a positive amount is accepted.

Minimum automated mana matrix:

- modes: `DISABLED`, `BOTANIA_PRIMARY`, `ISS_PRIMARY`, `HYBRID`, and `SEPARATE`;
- spell types: each Botanical spell, a non-Botanical ISS spell, normal scroll, Elementium scroll, spellbook, staff/wand, and any continuous/long cast;
- source sets: none, one item, multiple partial items, duplicate API views of one item, Curios, Mana Mirror, one/multiple pools, altar, mixed item/pool, full destination, and malformed NBT;
- affordability: zero cost, exact cost, one below, surplus, maximum configured value, and conversion overflow boundary;
- concurrency: two identical casts in one tick, different casts in one tick, scroll plus spellbook in one tick, canceled cast, interrupted continuous cast, death/logout/dimension change during cast, and add-on cancellation;
- assertions: no partial drain on failure, no double debit/refund, conservation across conversions, deterministic source priority, no free retained scroll, and no unrelated mana mutation intercepted.

## Phase 3: make interactions and content truthful

### 3.1 Consumables and recipes

- Implement Elementium scroll conversion with a custom recipe that copies only the validated ISS spell payload and permitted display data from one input scroll. Set output count to one and reject blank/malformed/multiple scrolls.
- Test that the output renders the correct icon, passes restriction/description add-ons, casts the original spell, retains itself only after successful Botania payment, and follows ordinary consumption on fallback/failure according to the documented design.
- Give rune-enhanced scroll NBT an actual effect recognized at cast time, with school/rune validation and balance limits, or remove the recipe. Do not charge players for a cosmetic boolean presented as an upgrade.
- Give every school-power orb a matching unique orb-type data entry, item, attribute, tooltip, recipe, and container item. Correct the Flora/Botany identity and decide whether Nature remains a separate offering.
- Add data validation that every registered item/block/spell with intended survival use has a valid acquisition path, and every recipe result/ingredient/tag ID resolves under the supported dependency matrix.

### 3.2 Cast preflight and modifiers

- Move reagent and placement validation before debit. Validate the whole Corporea placement plan before extracting anything, then commit atomically or roll back all extracted stacks.
- Move catalyst consumption after a successful spell commit. Record exact slots/stacks so identical items are not consumed from a different slot.
- Feed cooldown, cast speed, mana cost, damage, range, projectile, piercing, homing, and additional effects into supported phases where they can actually affect behavior. Delete or stop advertising a modifier that cannot be supported.
- Clamp every composed modifier, document stacking order, and add observable tests for every catalyst, aura, channel, attunement, and equipment bonus.
- Enforce block modification/protection hooks for spells that place or change blocks, respect mob-griefing or a documented config, skip unloaded chunks, and avoid replacing block entities or protected blocks.

### 3.3 Progression and system cleanup

For each item below, choose **implement and test**, **remove**, or **deprecate with a visible migration/known limitation**:

- `ManaPriorityChain` and `IManaSource`;
- `SpellDrivenAutomation`;
- `AlfheimScrollCrafting` and dual-school scroll NBT;
- `UnifiedAdvancementSystem` unlock flags;
- spell-triggered mana-network events;
- unused custom sound registrations;
- every reserved/no-op common and client config key listed in the audit table.

Additional consistency work:

- Rename `/irons_botany reload` if it only clears aura caches, or implement a real supported reload path. Command names and descriptions must match effects.
- Audit guidebook, tooltips, config comments, changelog, and README against live behavior. Remove versioned roadmap claims from player-facing text.
- Make advancement triggers correspond to real committed actions. Do not award for failed or merely queried casts.
- Expand equipment/tag sets so newly added tiers are included where intended; add tests so future registered equipment cannot silently fall out of relevant tags or creative/progression lists.

## Phase 4: performance, lifecycle, and data hardening

1. Profile before and after with several players casting near dense Botania infrastructure. Record server tick time, allocations, block lookups, packets, and client frame cost.
2. Replace repeated cuboid scans with a shared, dimension-aware, chunk-aware lookup/cache. Do not load chunks to answer proximity queries. Invalidate on block placement/removal, config reload, dimension change, logout, world unload, and server stop.
3. Fix aura cache identity so different radii and dimensions cannot reuse incompatible results. Select nearest/strongest auras deterministically before applying `MAX_ACTIVE_AURAS`; do not let block iteration order determine winners.
4. Add fair transfer scheduling for reservoirs/conduits: one documented total budget per interval, deterministic or round-robin player selection, and no repeated client sync when the value did not change.
5. Clamp all deserialized mana and color/state fields. Handle config capacity reductions explicitly. Use safe arithmetic for additions and conversion multiplication.
6. Clear static maps such as active network modifications and trials at all lifecycle boundaries. Use each record's dimension clock rather than overworld time.
7. Cache HUD totals outside the render hot path, deduplicate sources, honor `HUD_SCALE` and `PARTICLE_DENSITY`, clamp fill to `[0, 1]`, keep the bar on-screen, and add an accessibility option to disable motion/pulsing.
8. Budget particles and network packets by distance, client setting, spell level, and nearby viewers. Avoid one packet/particle burst per aura/catalyst when aggregated feedback is sufficient.

Suggested performance acceptance targets should be written before implementation. At minimum, no single player action may synchronously inspect tens of thousands of block positions, no client render frame may enumerate all capabilities, and no idle block entity may send unchanged update packets.

## Phase 5: migration and compatibility

Create a versioned data migration service, not a one-time boolean.

- Store an integer schema version under the mod namespace.
- Make every step idempotent and test every supported starting schema.
- Recognize historical `ironsbotany:botanical`, transitional ISS Nature metadata on Iron's Botany-owned spells/items, and canonical `ironsbotany:botany`.
- Only rewrite a Nature school when the same record is demonstrably an Iron's Botany spell/item. Preserve genuine ISS Nature scrolls and equipment.
- Scan player inventory, armor, offhand, Curios, ender chest, and other data that can be migrated safely. For world containers/offline players, use a documented lazy migration on load rather than an unsafe whole-world rewrite.
- Preserve spell level, upgrade data, custom names, enchantments, durability, mana NBT, and allowed add-on data.
- Back up the test world and produce an item-by-item before/after report. A downgrade is not required to understand new content, but a failed upgrade must not corrupt the original save.
- Test upgrades from actual 1.7.0/1.7.2 and 1.9.0 fixtures, repeated upgrade loads, and mixed old/new items.

## Automated verification and CI gates

Add tests in layers:

### Fast unit/contract tests

- mana conversion, cost composition, source deduplication, transaction state, rollback, and overflow;
- school metadata completeness and canonical resource identity;
- migration of representative NBT fixtures, including proof that unrelated Nature scrolls are untouched;
- config-key usage/deprecation manifest and multiplier clamps;
- cache keys/lifecycle behavior and fair transfer accounting.

### Registry/data/resource tests

- every registered spell has a canonical school, translation, recipe/loot intent, and runtime-resolvable icon;
- every registered item/block has required model/texture/translation/loot/acquisition data;
- every model, blockstate, particle definition, renderer texture, advancement, loot table, recipe, tag, damage type, and Patchouli reference resolves;
- every PNG decodes, has expected dimensions, and is included in the reobfuscated release JAR;
- all language JSON parses, has no duplicate keys, and is compared with `en_us` for parity;
- each upgrade-orb item maps one-to-one to the expected orb type and attribute;
- the Forge update URL, if present, serves valid update JSON.

### Game/integration tests

- clean client startup, resource reload, world join, all nine scroll tooltips/icons/casts, all blocks/renderers/particles, armor, curios, recipes, and commands;
- dedicated-server startup and player join without loading client classes;
- the complete mana matrix from Phase 2;
- world upgrades and repeated migrations;
- compatibility with the minimum supported and latest supported versions of ISS 3.16.x, Botania 450-compatible releases, Curios, Patchouli present/absent, Ars 'n Spells present/absent, and the two reported scroll add-ons.

Run from a clean dependency cache in CI. After configuring a Forge GameTest server run, a normal release candidate should pass at least:

```text
gradlew clean build
gradlew runData
gradlew runGameTestServer
```

Also run documented client and dedicated-server smoke profiles. If Forge tasks do not exit automatically, CI may use a bounded harness that asserts the expected ready state and scans logs for errors before orderly shutdown.

Fail CI on:

- test failure;
- missing or undecodable final-JAR resource;
- missing registry reference or translation key required by `en_us`;
- dedicated-server client-class error;
- mixin/registry/data-pack load error;
- new high-severity warning, missing-texture line, or uncaught exception in smoke logs;
- a published artifact whose checksum was not generated by the tagged CI run.

## Manual release matrix

Before publishing, complete and attach a matrix with these rows:

- new world and upgraded 1.7.0/1.7.2 world;
- upgraded 1.9.0 world;
- minimum dependencies only;
- ISS-Scroll Descriptions only;
- ISS-Restrictions only;
- both scroll add-ons;
- Patchouli absent/present;
- Ars 'n Spells absent/present in each supported bridge mode;
- default config and a boundary-value config;
- single-player, LAN/integrated server, and dedicated server.

For every row verify icons, scroll descriptions/restrictions, all spell casts, failed-cast conservation, mana HUD, block/entity/particle rendering, recipes, equipment attributes, server restart persistence, resource reload, logout/rejoin, and dimension change.

## Release engineering

- Publish only from the reviewed tag/commit that CI built.
- Save SHA-256, artifact size, dependency matrix, Java/Forge versions, and test report next to the release.
- Generate a JAR-entry manifest and compare it with the validated artifact before upload.
- Use a valid Forge update JSON file or omit `updateJSONURL`; do not point it at an HTML releases page.
- Replace local flat-directory dependencies with authenticated/stable Maven coordinates where practical. Otherwise document exact vendored JAR provenance and checksums without redistributing files unlawfully.
- Add a concise upgrade note explaining the canonical Botany-school migration, config changes, removed/deprecated inert features, and world-backup recommendation.
- Mark unresolved compatibility limitations explicitly. Do not hide them behind broad exception catches or a “known issue fixed” claim without a passing matrix row.

## Definition of done

The next major update is ready only when all of the following are true:

- the exact published candidate opens every Iron's Botany scroll with ISS-Scroll Descriptions and ISS-Restrictions without crashing;
- all nine icons resolve through the runtime resource manager and exist as decodable images in the final JAR;
- the reported purple/black render has a named root cause and no longer reproduces in its failing profile;
- every Botanical spell reports canonical `ironsbotany:botany` metadata and every public school accessor is valid;
- failed, interrupted, or canceled casts conserve all mana and consumables; successful casts debit each cost exactly once;
- same-tick casts and unrelated mana events cannot share payment/refund state;
- upgrade paths preserve real Nature content and migrate Iron's Botany-owned legacy content idempotently;
- equipment, orbs, recipes, tooltips, guidebook pages, commands, advancements, and configs match observable behavior;
- no public “feature” remains silently inert without an explicit deprecation/known-limitation decision;
- clean build, data generation, automated tests, client smoke, dedicated-server smoke, and compatibility matrix pass;
- performance profiling confirms that the update removed the large synchronous scans and render-frame enumeration described above, or a reviewed bounded alternative and budget is documented;
- the tagged source, tested artifact, and uploaded artifact are provably the same release output.

## Required agent handoff

At the end of each phase, report:

1. evidence gathered and exact reproduction result;
2. root cause, distinguishing verified facts from inference;
3. files and data schemas changed;
4. migrations and compatibility impact;
5. automated and manual tests run, including failures or omissions;
6. performance measurements where relevant;
7. remaining risks and the next smallest safe step.

Do not move a P1/P2 feature ahead of an unresolved P0 correctness or data-loss issue. If a reported failure cannot be reproduced, keep its diagnostic logging and matrix row open rather than substituting a speculative fix.
