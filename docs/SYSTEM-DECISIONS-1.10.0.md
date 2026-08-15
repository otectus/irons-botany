# Iron's Botany 1.10.0 — decisions on inert and misleading systems

The brief requires an explicit **implement**, **remove**, or **deprecate with a visible
migration/known limitation** for every system and config key that was public but did nothing.
This is that record. No entry is left as "still there, still silent".

---

## 1. Systems

| System | Decision | What was actually wrong | What 1.10.0 does |
| --- | --- | --- | --- |
| **Botany school metadata** | **implement** | Display name had no colour, so ISS's `SchoolType#getTargetingColor()` — reached by ISS's own `AbstractSpell` and ~20 ISS classes plus description/restriction add-ons — threw `NullPointerException` for every Botany spell. | Explicit `#3FD8A0` style, canonical `ironsbotany:botany` everywhere, contract test replaying ISS's own accessor chain, runtime reload check, `/irons_botany diagnose`. |
| **Mana source aggregation** | **implement** | `ManaItemHandler.requestMana(stack, …)` excludes the stack passed to it. Used as a per-source probe it returned 0 for a player with one mana item and over-counted for three or more, then partially drained before reporting failure. | Per-source access via `BotaniaForgeCapabilities.MANA_ITEM`; plan/commit split so an unaffordable cast cannot drain anything. |
| **Cast cost routing** | **implement** | Keyed on `(tick, spellId.hashCode(), issCost)` with tick-only payment flags, and refunds applied by cancelling the next `ChangeManaEvent` mana decrease — an event with no spell identity. | One `CastTransaction` per cast with its own id and casting stack; payment redirected via ISS's own `SpellOnCastEvent.setManaCost(0)`. The `ChangeManaEvent` subscriber is **deleted**, not narrowed. |
| **`ManaPriorityChain` / `IManaSource`** | **remove** | A public extension point with no implementations and no callers. The `MANA_PRIORITY_CHAIN` config listed source names that nothing read. | Superseded by `ManaSourceRef`, whose ordering (carried → worn → pooled) is documented, deterministic and unit-tested. The old types are gone; the config key is deprecated (§2). |
| **`SpellDrivenAutomation`** | **deprecate — known limitation** | Registered and configurable, but its effects wrote NBT that Botania never reads. | Left in place, defaulted off, and documented here and in the config comment as unimplemented. It is not advertised in player-facing text. Removing it outright would break existing configs for no gain; it is inert either way, and now says so. |
| **Spell-triggered mana-network events** | **deprecate — known limitation** | `getTriggerType` maps Botany *and* Nature to `NATURE` before any name check, and only the `WATER` trigger has an implementation. Every spell this mod ships therefore resolves to a trigger that does nothing. | Defaulted off with a config comment stating that only Water Fill is implemented and that no Iron's Botany spell can produce it. The wasteful cuboid scan for non-Water triggers already short-circuits. |
| **`AlfheimScrollCrafting` / dual-school NBT** | **deprecate — with migration** | Writes `IronsBotany_PrimarySchool` / `_SecondarySchool`; only the primary is read, by a getter nothing calls. | The NBT is now the *only* place the migration service reinterprets an ambiguous ISS-Nature value, and only on items proven to be ours. The crafting path stays available; the dual-school *effect* remains unimplemented and is listed as a known limitation rather than advertised. |
| **`UnifiedAdvancementSystem` unlock flags** | **deprecate — known limitation** | `TIER4_UNLOCKED`, `DUAL_SCHOOL_UNLOCKED`, `OVERCHARGE_UNLOCKED` are written and never read; no recipe, item or spell gates on them. | Documented as inert. They are player NBT, so removing them would be a data change for no behavioural gain; nothing in the guidebook or tooltips claims they do anything. |
| **Custom sound registrations (`IBSounds`)** | **deprecate — known limitation** | Registered sound events with no `sounds.json` entries and no play sites. | Documented. The Botany school's cast sound is vanilla `AMETHYST_BLOCK_CHIME`, stated plainly in `IBSchools`, rather than a custom event that would resolve to silence. |
| **Gaia's Blessing charging** | **implement** | Drained a mana pool from `ModifySpellLevelEvent`, which ISS also fires for non-cast level queries — a player could be charged for opening a spell book. | Affordability checked non-destructively when granting the level; the surcharge is debited once by the cast transaction's commit. Also drops a 35 937-position cuboid scan. |
| **Dreamwood conversion** | **implement** | A second `SpellPreCastEvent` subscriber at the same `LOW` priority as the bridge, coordinating through a shared NBT tag. Same-priority listener order is undefined, so behaviour depended on registration order. | Folded into the one transaction as `issCreditOnCommit`, credited only after the Botania debit succeeds. |
| **Rune-scroll fusion** | **implement** | Consumed a Botania rune and wrote `runeType` / `runeEnhanced` that nothing read. Players paid a rune for a renamed scroll. | The NBT now means a Botania cost discount scaled by rune tier, read once by the cost composition, clamped to 50%, configurable via `runeScrollManaDiscount` (set 0 to keep it cosmetic). |
| **School-power upgrade orbs** | **implement** | Eight orbs shared four orb types, so each applied another orb's attribute *and* returned another orb when consumed; the tooltip switch had no case for any of the eight. | One orb type per orb, correct per-school attribute, correct container item, tooltip key derived from the type. `UpgradeOrbDataTest` enforces the 1:1 mapping. |
| **Botany power / resistance attributes** | **implement** | Registered but attached to no entity. ISS returns `1.0` for an absent attribute, so every Botany bonus on every item was silently inert. | Attached to players via `EntityAttributeModificationEvent`. |
| **`mana_efficiency` attribute** | **implement** | `default 1.0, max 1.0` with every item adding *positive* modifiers to a value already at maximum, and no reader in the cost path. | Rebased to `default 0.0, max 0.75` and consumed once by the cost composition. Existing item values now mean exactly what their tooltips claimed. |
| **`/irons_botany reload`** | **rename** | Named like vanilla `/reload` but only cleared the flower-aura cache, so an operator editing the TOML would see no change and blame the config. | Renamed `/irons_botany flushcaches`, reports what it cleared, and says explicitly that it does not re-read configuration. New `/irons_botany diagnose` reports school metadata. |
| **Flower aura cache** | **implement** | Keyed on player UUID only while callers request radius 8 and 16, so one radius' result served the other; no dimension check; `MAX_ACTIVE_AURAS` truncation decided by block-iteration order. | Keyed by `(player, radius)`, validated against dimension, time and position; candidates sorted by strength then distance then position before truncation; cleared on logout, dimension change and server stop. |

---

## 2. Config keys

Every key listed in the audit as reserved or unused, with its decision. **No key is silently
no-op any more**: each is either wired, or its comment now states plainly that it does nothing.

| Key | Decision | Note |
| --- | --- | --- |
| `runeScrollManaDiscount` | **new, implemented** | Added in 1.10.0. Drives the rune-enhanced scroll discount. |
| `manaPriorityChain` | **deprecate** | Superseded by the documented, deterministic source order in `ManaSourceRef`. Kept so existing configs load; comment says it is ignored. |
| `bidirectionalConversion`, `reverseConversionRatio` | **deprecate** | ISS→Botania conversion is not implemented. Comment states so. |
| `enableBotanicalSchool` | **deprecate** | The school is always registered; disabling it would leave nine spells with no school, which is exactly the invalid-metadata state 1.10.0 exists to prevent. |
| `enableCrossLoot` | **deprecate** | Loot injection is driven by the hand-authored loot-modifier JSONs, not this flag. |
| `upgradeOrbEffectiveness` | **deprecate** | Orb strength is data-driven per orb type; a global multiplier would silently contradict the JSON. |
| `channelPowerMultiplier` | **deprecate** | Channels contribute damage, cooldown, cast-speed and mana-cost factors; there is no separate "power" term for this to scale. |
| `allowAuraStacking` | **deprecate** | Aura selection is governed by `maxActiveAuras` and the deterministic ranking. |
| `manaEvent*` | **deprecate** | Only Water Fill is implemented, and no Iron's Botany spell resolves to it. |
| `autoRequestReagents`, `corporeaSearchRadius` | **deprecate** | The Corporea reagent path validates and commits its plan; neither knob is consulted. |
| `alfheimPowerMultiplier` | **deprecate** | The Alfheim proximity boost uses its own configured values. |
| `enableDualSchoolScrolls` | **deprecate** | Dual-school scroll NBT is written but has no cast-time effect (see §1). |

---

## 3. Known limitations shipped with this release

Stated here so the release notes can link to them rather than implying they are fixed.

1. **The purple/black render is not reproduced and has no named root cause.** Every PNG in the
   artifact decodes, every reference resolves inside the JAR, and no ISS render path derives a
   texture path from a school id. The build gate now fails on a missing or undecodable texture, so
   if it recurs it is not an Iron's Botany asset. Reports should attach `latest.log`, the resource
   pack list, and whether the public or a self-built JAR was used.
2. **The public 1.9.0 artifact was never compared against the `v1.9.0` tag** — it was not available
   to the investigation. Release provenance for 1.9.0 is therefore neither proven nor disproven.
   1.10.0 prints the artifact SHA-256 and size at build time so this cannot recur.
3. **Dual-school scrolls carry metadata with no cast-time effect.**
4. **`SpellDrivenAutomation`, the mana-network trigger, and the progression unlock flags are inert**
   and default to off.
5. **22 language files fall back to `en_us` for 87 keys each.** The build reports the count per
   file. Honest fallback was chosen over machine translation; the Flora orb's stale translation was
   deleted rather than renamed because its meaning changed from Nature to Botany.
