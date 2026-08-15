# Iron's Botany 1.10.0 — agent handoff report

Structured to the seven points the brief requires at the end of each phase, aggregated across all
phases. Verified facts are separated from inference throughout.

---

## 1. Evidence gathered and exact reproduction result

**Reproduced, with proof:**

| Report | Result | Evidence |
| --- | --- | --- |
| Crash when scroll add-ons inspect a spell | **Reproduced at the contract level** | `SchoolType` ctor caches `displayName.getStyle()`; `getTargetingColor()` calls `displayStyle.getColor().getValue()` with no null check; Botany's component was unstyled, so `Style.EMPTY.getColor()` is null. Bytecode in `docs/INVESTIGATION-1.10.0.md` §2.1. A test replays the exact chain and asserts the old construction still throws. |
| Spell scrolls lack icons | **Reproduced as a consequence, not a cause** | All nine icons exist at the path ISS derives (`textures/gui/spell_icons/<name>.png`, constant `#97` of `AbstractSpell`), are 16×16, and decode. A full sweep of the 1.9.0 artifact decoded 86/86 PNGs with zero problems. |
| Interactions unreliable/broken | **Reproduced at the API level** | `ManaItemHandlerImpl#requestMana` skips the passed stack by identity (offset 62). With one mana item every probe returns 0; with N≥3 the sum over-counts ~(N−1)×, then the commit pass partially drains before returning false. Worked example in §2.4. |
| Purple/black magic texture | **NOT reproduced** | No confirmed root cause. See §5. |

**Not reproduced, and why:** the purple/black render. Every PNG in the artifact decodes; every
model, blockstate and particle reference resolves inside the archive; and no ISS render or GUI class
derives a texture path from a school id — only the targeting *colour*. Four candidate causes remain
open, none eliminated: a public artifact differing from the tag, a resource pack/shader in the
reporter's profile, a texture owned by Botania or ISS attributed to this mod, or a stale client
cache after an in-place update. **The public 1.9.0 JAR was not available to this investigation**, so
the artifact-provenance candidate could not be tested at all.

---

## 2. Root cause, verified vs inferred

**Verified (bytecode, source, or artifact inspection):**

1. Unstyled school display component → guaranteed NPE in `getTargetingColor()`.
2. `ManaItemHandler.requestMana` misused as a per-source query → zero for N=1, over-count for N≥3,
   partial drain on failure.
3. Cast identity `(tick, spellId.hashCode(), issCost)` with tick-only payment flags; refunds applied
   via `ChangeManaEvent`, which carries no spell identity.
4. Ordering: mana charged at pre-cast, catalysts consumed at `onCast` line 120, reagent check that
   aborts at line 165.
5. Negative-amount `requestManaExact` used to add mana, to an item other than the one iterated.
6. All nine spells declared `SchoolRegistry.NATURE_RESOURCE` while the code overrode
   `getSchoolType()` to Botany — and ISS resolves that method from config, so the config was inert.
7. Botany attributes registered but on no entity; ISS returns 1.0 for an absent attribute.
8. `mana_efficiency` default equal to its maximum, with positive modifiers and no reader.
9. Eight orbs sharing four orb types → wrong attribute and wrong container item each.
10. `runeType`/`runeEnhanced` written, never read.
11. Aura cache keyed on UUID alone with callers at radius 8 and 16.

**Inferred (stated as inference, not fixed speculatively):**

- That "missing icons" is a *rendering consequence* of the NPE rather than an independent defect.
  High confidence — the assets are provably valid — but not directly observed in a client. The icon
  path is therefore instrumented rather than assumed: a build gate and a runtime reload check.

**Two findings the audit did not anticipate, discovered while implementing:**

- **A spell-config datapack override would have been a severe regression.** `SpellConfigManager`
  uses the datapack map *instead of* the config directory, not merged. Shipping one such file would
  have made Iron's Spells ignore the operator's entire `config/irons_spellbooks/spells/` directory
  for every spell in the modpack. Rejected; the migration repairs the file in place instead.
- **`SpellOnCastEvent.setManaCost(int)` exists** and is ISS's own supported cost-redirection hook.
  This let the `ChangeManaEvent` interception be deleted outright rather than narrowed.

---

## 3. Files and data schemas changed

**New (main):** `registry/BotanySchool`, `bridge/mana/{ManaSourceRef,ManaPlan,ManaPlanner,ManaCostComposer,BotaniaManaGateway}`,
`bridge/cast/{CastTransaction,CastTransactionState,CastTransactions}`,
`migration/{IBDataMigration,ItemNbtMigration,SchoolMigrationHandler,SpellConfigRepair}`,
`item/RuneEnhancement`, `client/SpellIconIntegrityCheck`.

**Removed:** `bridge/CostRoutedTag`, `event/DreamwoodConversionHandler`,
`event/SchoolMigrationHandler` (replaced by the versioned service in `migration/`).

**Rewritten:** `ManaBridgeManager`, `ManaHelper`, `SpellEventHandlers`, `CurioEffectsHandler`,
`TerrasteelBladeHandler`, `BotanicalUpgradeOrbItem`, `ElementiumScrollItem`, `IronsBotanyCommands`,
`FlowerAuraRegistry`, `EntityAttributeHandler`.

**Data schemas:**

| Schema | Change | Migration |
| --- | --- | --- |
| Player persistent NBT | `IronsBotany_SchemaVersion` (int) replaces the `IronsBotany_SchoolMigrated` boolean | Boolean readers treat its presence as version 0, so the work it claimed is redone correctly; the boolean is removed after migrating |
| Item NBT | `IronsBotany_PrimarySchool` / `_SecondarySchool` normalised to `ironsbotany:botany` | Provenance-gated; genuine ISS Nature untouched |
| Item NBT | `runeEnhanced` / `runeType` now read | None needed — existing scrolls gain the effect |
| Upgrade orb data | 8 new `upgrade_orb_type` files; `flora` retargeted to `ironsbotany:botany_spell_power` | None — orb items keep their identity, only their type data changes |
| Lang keys | `item.ironsbotany.orb_of_*.bonus` → `item.ironsbotany.orb.<type>.bonus` | Renamed in all 22 translations; Flora's dropped because its meaning changed |
| Attribute | `ironsbotany:mana_efficiency` rebased from (1.0, 0–1.0) to (0.0, 0–0.75) | None — attribute bases are computed, not saved |
| ISS spell config | `"school"` repaired in `config/irons_spellbooks/spells/ironsbotany/` only | Idempotent textual rewrite, logged per file |

---

## 4. Migrations and compatibility impact

- **Supported upgrade paths:** 1.7.x → 1.10.0, 1.9.0 → 1.10.0. Both idempotent and version-gated.
- **Preserved:** spell level, custom names, enchantments, durability, stored mana NBT, and all
  add-on data. Asserted by test.
- **Explicitly not touched:** genuine Iron's Spells Nature content. A test asserts a real Nature
  scroll is bit-for-bit identical after migration.
- **Scope:** player-reachable inventories. World containers migrate lazily; no whole-world rewrite.
- **Failure behaviour:** a migration that throws leaves the schema version unchanged, so it retries
  on next login rather than recording false success. The player still joins.
- **Dependency floor unchanged:** ISS 3.16+ (the `Scroll(Item.Properties)` ABI break predates this
  release), Botania 450+, Curios 5.14+.
- **API:** `IronsBotanyApi.resolveCost` deprecated in favour of `preflightCost`, which states that
  it reserves rather than charges. The shim keeps existing callers compiling.

---

## 5. Tests run, and what was NOT run

**Run and passing:**

- `gradlew test` — **77 tests, 0 failures.** Mana planning (including the exact three-partial-source
  over-count and the single-item case), cost composition (overflow, non-finite rejection, discount
  stacking), transaction state machine and per-cast identity, school metadata contract, NBT
  migration fixtures, spell-config repair, upgrade-orb data identity.
- `gradlew build` — clean, including `validateAssets` and `validateArtifact`.
- `validateArtifact` on the release candidate: **611 entries, 86/86 PNGs decoded, 9/9 spell icons
  present and 16×16**, every asset reference resolves inside the archive.

**Two tests caught real bugs during development, which is the point of having them:**

- `SpellConfigRepairTest` caught the repair's early-return guard checking only for the Nature string,
  so a pre-1.6 `botanical` value would never have been repaired.
- `UpgradeOrbDataTest` caught its own source regex failing to match, which would have made every orb
  assertion pass against an empty map. It now fails loudly if parsing yields nothing.

**NOT run — stated plainly:**

| Not run | Why |
| --- | --- |
| Live client, dedicated server, `runGameTestServer`, `runData` | The dependencies in `libs/` are production (SRG-mapped) JARs supplied `compileOnly`, so no Forge run task can boot. Pre-existing project property, documented in `build.gradle`. |
| ISS-Scroll Descriptions / ISS-Restrictions profile | Those add-on JARs are not vendored. The contract test substitutes at the API level only. |
| Real 1.7.x / 1.9.0 world upgrade fixtures | No save files available. Migration is covered by NBT fixtures instead. |
| Screenshot-based manual QA | No interactive client. |
| Public-vs-tag artifact comparison | The public 1.9.0 download was not available. |
| Performance profiling with several players near dense infrastructure | Requires a running server. |

**Consequence:** every "Definition of done" item that depends on a live client or the add-on profile
is **not satisfied by this work** and must be completed before publishing. The code changes are
verified by construction, tests and artifact inspection — not by play.

---

## 6. Performance measurements

No before/after profiling was possible (no running server). What changed is structural and can be
counted rather than measured:

| Path | Before | After |
| --- | --- | --- |
| Mana pool proximity lookup | `BlockPos.betweenClosed` over the full cuboid — at radius 8 that is 17×9×17 = 2 601 `getBlockEntity` calls per affordability check, and the check ran twice per cast (has + drain) | Iterates the block-entity maps of chunks in range; unloaded chunks skipped, never loaded. Cost is proportional to nearby block entities, not volume. Runs once per cast. |
| Flower aura scan | 33×33×33 = 35 937 block reads at radius 16 | 33×13×33 = 14 157, vertical extent capped at ±6; unloaded chunks skipped |
| Aura cache | Keyed on UUID only, so radius-8 and radius-16 callers shared one entry and thrashed it | Keyed by (player, radius), validated on dimension and position |
| Gaia's Blessing | A 35 937-position cuboid scan per `ModifySpellLevelEvent` — including non-cast level queries | Folded into the cast transaction's source collection; no separate scan |

**Not addressed, and honestly out of scope for this pass:** the client HUD still recomputes item
capabilities per render frame and does not honour `HUD_SCALE` or `PARTICLE_DENSITY`. This is a P2 in
the audit and is the largest remaining item.

---

## 7. Remaining risks, and the next smallest safe step

**Risks, highest first:**

1. **No live verification.** The single highest risk. The crash fix is proven at the contract level
   and the transaction is proven by unit tests, but neither has been exercised in a running game.
2. **The purple/black report is unresolved.** If it is a publishing defect, the new artifact gate
   will catch a recurrence; if it is environmental, it will recur and still be unexplained.
3. **`SpellConfigRepair` writes into another mod's config directory.** Narrowly scoped (one
   namespace, one key, one legacy value) and logged, but it is an intrusive operation. An operator
   who does not expect it may be surprised.
4. **The long-cast commit path** — where sources change between reservation and commit — falls back
   to letting ISS charge normally. This is documented and conserves mana, but is the least-exercised
   branch.
5. **Phase 4 is partially complete.** Aura caching, scan bounding and lifecycle clearing are done;
   the HUD render-frame work and per-tick transfer budgets are not.

**Next smallest safe step:** assemble the minimum profile (Forge + Botania + Iron's Spells + Curios
+ this JAR), start a client, obtain all nine spells, and run `/irons_botany diagnose`. That single
command exercises the exact accessor chain that crashed 1.9.0 across every registered spell and
reports any spell whose resolved school is not canonical Botany. If it reports clean, add
ISS-Scroll Descriptions and open each scroll. Those two steps close the largest risk for the least
effort, and neither requires touching code.
