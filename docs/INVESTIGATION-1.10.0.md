# Iron's Botany 1.10.0 — Phase 0 investigation note

Status: **Phase 0 complete.** This note records the evidence gathered before any gameplay code
was changed, per `NEXT_MAJOR_UPDATE_AGENT_INSTRUCTIONS.md`.

---

## 1. Repository and release baseline

### 1.1 Branch/base decision — resolved, no ambiguity

The brief anticipated that `v1.9.0` and `main` were divergent lines that would need a reconciliation
decision. They are not divergent any more:

```
$ git rev-list --count v1.9.0..main
0
$ git rev-list --count main..v1.9.0
6
$ git merge-base main v1.9.0
9a7ee495b928d9ae941f319853f8d7c278643a30   (== tip of main)
```

`v1.9.0` already contains **every commit on `main`** — its tip commit `aa28ea5` is literally
"Merge main (1.7.x line) into v1.9.0 — reconcile divergent histories". `main` is a strict subset.

**Decision: the 1.10.0 branch is cut from `v1.9.0`.** No cherry-picking from `main` is required,
because nothing exists on `main` that is not already in `v1.9.0`.

```
$ git checkout -b next/1.10.0 v1.9.0
```

Both histories are preserved; neither `main` nor the `v1.9.0` tag was reset, rebased or overwritten.

> **Why the working tree looked like 1.7.2.** The checkout was sitting on `main`, whose
> `build.gradle` says `version = '1.7.2'` and whose `libs/` pins Iron's Spells **3.15.2**.
> The `v1.9.0` tree says `version = '1.9.0'` and pins Iron's Spells **3.16.1**. Anyone who
> inspected the working tree rather than the tag would have audited the wrong source and
> compiled against the wrong ISS ABI. This alone explains part of the "source of truth" problem.

### 1.2 Dependency matrix actually used for this investigation

| Component | Version | Provenance |
| --- | --- | --- |
| Minecraft | 1.20.1 | `build.gradle` |
| Forge | 47.4.16 | `build.gradle` |
| Java | 17 (toolchain) | `build.gradle` |
| Mappings | official 1.20.1 | `build.gradle` |
| Botania | 1.20.1-450-FORGE | `libs/` (compileOnly) |
| Iron's Spells 'n Spellbooks | **1.20.1-3.16.1** | `libs/` (compileOnly) — *3.15.2 on `main`* |
| Curios | forge-5.14.1+1.20.1 | `libs/` (compileOnly) |
| Patchouli | 1.20.1-84.1-FORGE | `libs/` (compileOnly) |
| GeckoLib | forge-1.20.1-4.8.2 | `libs/` (compileOnly) |
| Player Animation Lib | forge-1.0.2-rc1+1.20 | `libs/` (compileOnly) |

### 1.3 Baseline artifact

```
$ ./gradlew clean build      → BUILD SUCCESSFUL, exit 0, 59 deprecation warnings, 0 tests
build/libs/ironsbotany-1.9.0.jar
  size    620 839 bytes
  entries 582
  sha256  e744edd10025a2b3403713290babab91829f88178bc4cdfedbbef80a6cfa1945
```

**The public 1.9.0 CurseForge download was not available to this investigation**, so the
"compare public JAR against the tag" step in the brief could not be executed. That row stays
**open** — see §5. Everything below is therefore stated about the JAR built from the `v1.9.0`
tag, which is the artifact the release *should* correspond to.

---

## 2. Symptom → root cause map

Each row separates **verified** (proven by bytecode/source/artifact inspection) from **inferred**.

### 2.1 P0 — Crash when ISS-Scroll Descriptions / ISS-Restrictions inspect a Botany scroll

**VERIFIED. Complete cause chain, proven at bytecode level.**

`IBSchools.BOTANY` is constructed with an unstyled display name
(`src/main/java/com/ironsbotany/common/registry/IBSchools.java:49`):

```java
Component.translatable("school.ironsbotany.botany")   // Style.EMPTY — no colour
```

ISS 3.16.1's `SchoolType` constructor derives and caches a `Style` from that component
(`javap -c io/redspace/ironsspellbooks/api/spells/SchoolType.class`):

```
19: aload_0
20: aload_3
21: invokeinterface Component.m_7383_:()Lnet/minecraft/network/chat/Style;   // displayName.getStyle()
26: putfield        displayStyle:Lnet/minecraft/network/chat/Style;
```

and `getTargetingColor()` dereferences that style's colour **with no null check**:

```
0: aload_0
1: getfield        displayStyle:Lnet/minecraft/network/chat/Style;
4: invokevirtual   Style.m_131135_:()Lnet/minecraft/network/chat/TextColor;   // → null for Style.EMPTY
7: invokevirtual   TextColor.m_131265_:()I                                     // → NullPointerException
10: invokestatic   Utils.deconstructRGB:(I)Lorg/joml/Vector3f;
```

`Style.EMPTY.getColor()` returns `null`, so **`SchoolType#getTargetingColor()` throws NPE for the
Botany school and for no other school in the pack.**

This is *not* limited to third-party add-ons. ISS's own `AbstractSpell` exposes the same call:

```
public org.joml.Vector3f getTargetingColor();
  0: invokevirtual getSchoolType:()Lio/redspace/ironsspellbooks/api/spells/SchoolType;
  4: invokevirtual SchoolType.getTargetingColor:()Lorg/joml/Vector3f;
```

and 20 further ISS classes call it, including `render/SpellTargetingLayer`,
`gui/overlays/RecastOverlay`, `entity/spells/*` and many `spells/*` implementations. Any add-on
that renders a targeting hint, a school-coloured widget, or a spell description will hit it.

**Fix:** give Botany an explicit non-null RGB style. Covered by a contract test that calls every
public `SchoolType` accessor on every registered Iron's Botany school.

### 2.2 P0 — "Spell scrolls appear to lack icons"

**VERIFIED that the icons are present and valid; the missing-icon symptom is a *consequence* of
2.1, not an independent asset defect.**

ISS resolves a spell icon as (constant pool `#97` of `AbstractSpell`):

```
textures/gui/spell_icons/.png      ← String.concat template
getSpellIconResource() = ResourceLocation(getSpellResource().getNamespace(),
                                          "textures/gui/spell_icons/" + getSpellName() + ".png")
```

The nine registered spell IDs in `IBSpells` are `mana_bloom, botanical_burst, flower_shield,
living_root_grasp, spark_swarm, runic_infusion, petal_storm, gaia_wrath, mana_rebirth`. All nine
files exist in the built JAR at exactly those paths, and every one decodes:

| Icon | Size | Bytes | Visible px |
| --- | --- | --- | --- |
| botanical_burst.png | 16×16 | 172 | 64 |
| flower_shield.png | 16×16 | 237 | 88 |
| gaia_wrath.png | 16×16 | 254 | 156 |
| living_root_grasp.png | 16×16 | 192 | 104 |
| mana_bloom.png | 16×16 | 201 | 52 |
| mana_rebirth.png | 16×16 | 252 | 116 |
| petal_storm.png | 16×16 | 238 | 63 |
| runic_infusion.png | 16×16 | 267 | 119 |
| spark_swarm.png | 16×16 | 163 | 47 |

A full sweep of the artifact decoded **86/86 PNGs with 0 problems** — every texture in the JAR is
a valid image with non-zero visible pixels, `arcane_mana_altar.png` included (16×16, 256 visible px).

**Inference (high confidence):** an exception thrown while building a scroll tooltip aborts the
remainder of that tooltip/render pass. A user sees "no icon" and then a crash, and reasonably
reports them as two bugs. They are one bug — 2.1.

**This inference is not treated as proof.** The icon path stays instrumented: 1.10.0 adds a
runtime icon-resolution self-check and a final-JAR asset gate, so if icons ever *are* genuinely
missing the build fails rather than shipping.

### 2.3 P1 — "A magic texture renders as purple-and-black"

**NOT REPRODUCED. Cause remains open. Row stays open in the release matrix.**

Facts established:
- Every PNG in the built artifact decodes (86/86).
- `arcane_mana_altar.png` is present, 16×16, fully opaque, referenced by a blockstate and two models
  that both resolve.
- ISS's GUI/render classes derive **no** texture path from a school ID — the only school-derived
  value is the targeting *colour* (2.1). So an invalid school cannot by itself produce a
  missing-texture swatch.

Remaining candidate causes, none eliminated:
1. the public 1.9.0 upload differs from the tag (release-pipeline defect) — **cannot be tested
   without the public JAR**;
2. a resource pack or shader in the reporter's profile;
3. a texture owned by Botania/ISS rather than Iron's Botany that the reporter attributed to this mod;
4. a stale client resource cache after an in-place update.

**Action taken:** rather than guess, 1.10.0 ships the asset gate described in §4 so that a
missing/undecodable/mis-referenced texture becomes a build failure, and the release notes carry an
explicit "unresolved, please attach `latest.log` + resource pack list" note instead of a
"fixed" claim.

### 2.4 P0 — Mana source aggregation is wrong at the API level

**VERIFIED. This is the strongest candidate for "interactions are unreliable or broken."**

`ManaHelper#requestManaFromAllSources` calls, for each stack in the player's own mana-item list:

```java
available += ManaItemHandler.instance().requestMana(stack, player, amount, false);
```

Botania's implementation (`vazkii/botania/common/impl/mana/ManaItemHandlerImpl#requestMana`) is:

```
 0: if (requester.isEmpty()) return 0;
 9: List mainInv     = getManaItems(player);
16: List accessories = getManaAccesories(player);
30: for (ItemStack invItem : Iterables.concat(mainInv, accessories)) {
62:     if (invItem == requester) continue;          ← identity skip of the passed stack
        ...
135:    int drain = Math.min(manaToGet - total, manaItem.getMana());
151:    if (remove) manaItem.addMana(-drain);
166:    total += drain;
    }
185: return total;
```

The first argument is the **requesting** stack and is *excluded* from the search. It is not a
source selector. Consequences, all deterministic:

| Player's mana items | `hasBotaniaMana` behaviour |
| --- | --- |
| **exactly 1** (e.g. one Mana Tablet) | every probe skips the only item → available = 0 → **Botania payment always fails**, silently falls through to the pool scan |
| 2 | over-count cancels out by coincidence; appears to work |
| **3 or more** | availability is over-counted by roughly ×(N−1) → the affordability gate passes when the player cannot actually pay |

and the commit pass then partially drains before returning `false`:

> Worked example, three tablets holding 100 each, cost 500.
> Sweep: probe(a)=200, probe(b)=200 → 400 ≥ ... continues → 600 ≥ 500 → **reports affordable**.
> Commit: `requestMana(a,…,500,true)` drains b and c (200). `requestMana(b,…,300,true)` drains a (100).
> `requestMana(c,…,200,true)` finds nothing. `remaining = 200 > 0` → **returns false after
> destroying 300 mana.** The cast is refused *and* the player is charged.

**Correct API:** `BotaniaForgeCapabilities.MANA_ITEM` is a public capability giving true per-stack
access — `ManaItem { getMana(); getMaxMana(); addMana(int); canExportManaToItem(ItemStack);
isNoExport(); }`. 1.10.0 plans against `getMana()` and commits with `addMana(-n)`, per source.

### 2.5 P0 — Cast routing identity collides; refunds leak to unrelated events

**VERIFIED.**

`ManaBridgeManager.resolveCostInternal` keys idempotency on
`(level.getGameTime(), spellId.hashCode(), issCost)` (`ManaBridgeManager.java:71-78`), while
`CostRoutedTag.markBotaniaPaid` / `markScrollPaid` store **only a tick**
(`CostRoutedTag.java:78,67`). `SpellEventHandlers.onChangeMana` then refunds *any* mana decrease
in a tick that carries the Botania-paid stamp (`SpellEventHandlers.java:78-84`):

```java
if (event.getNewMana() >= event.getOldMana()) return;
if (CostRoutedTag.isBotaniaPaid(player, tick)) event.setNewMana(event.getOldMana());
```

`ChangeManaEvent` carries no spell identity at all (its fields are `magicData, oldMana, newMana`),
so this cannot distinguish the cast that Botania paid for from any other same-tick debit.

**A materially better mechanism exists and was being ignored.** ISS's cast pipeline is:

```
attemptInitiateCast(stack, level, world, player, source, …)
    canBeCastedBy(...)                       ← ISS affordability check
    checkPreCastConditions(...)
    post SpellPreCastEvent                   ← CANCELLABLE, costs the player nothing
    MagicData.initiateCast(...)
    MagicData.setPlayerCastingItem(stack)    ← the exact casting stack is recorded here
    onServerPreCast(...)

castSpell(level, spellLevel, serverPlayer, castSource, triggerCooldown)
    post SpellOnCastEvent                    ← has setManaCost(int) and setSpellLevel(int)
    if (castSource.consumesMana() && !hasRecast && !creativeExempt)
        magicData.setMana(max(0, mana - event.getManaCost()))   ← debit uses the EVENT value
    onCast(level, event.getSpellLevel(), …)  ← spell effect
    …cooldown, client packet
```

So `SpellOnCastEvent.setManaCost(0)` is ISS's own supported way to say "something else paid for
this cast". It is per-cast, carries spell/level/source identity, and needs no persistent NBT tag
and no interception of unrelated mana mutations. `MagicData.getPlayerCastingItem()` supplies the
exact casting stack, which fixes the "whichever hand happens to hold something eligible" problem
in channel and Elementium-scroll handling.

**1.10.0 deletes the `ChangeManaEvent` interception entirely.**

### 2.6 P0 — Costs are not transactional

**VERIFIED.** In `AbstractBotanicalSpell.onCast`:

- line 120 `consumeCatalysts(player, catalysts)` — **consumes items**
- line 165 `if (!SpellCircleReagentSystem.prepareSpellCircle(...)) { …; return; }` — **bails out**

so a reagent failure returns *after* catalysts were destroyed. Botania mana was already charged
earlier still, during `SpellPreCastEvent`. Ordering is: pay mana → consume catalysts → maybe fail.

### 2.7 P0 — Negative-amount mana generation

**VERIFIED.** `TerrasteelBladeHandler.java:50-53`:

```java
ManaItemHandler.instance().requestManaExact(stack, player, -manaPerHit, true)
```

Given the disassembly in 2.4, `Math.min(manaToGet - total, item.getMana())` with a negative
`manaToGet` yields a negative `drain`, and `addMana(-drain)` therefore *adds* mana — to some
**other** item in the inventory, never the iterated `stack`, because of the identity skip. The
loop's `break` and the particle burst fire on that unrelated success. `dispatchMana` /
`dispatchManaExact` are the supported additive calls and are used instead.

### 2.8 P0 — Canonical school resource is contradicted by config

**VERIFIED, and worse than the audit stated.**

All nine spells declare `.setSchoolResource(SchoolRegistry.NATURE_RESOURCE)` in `getDefaultConfig()`,
while `AbstractBotanicalSpell.getSchoolType()` hard-returns `IBSchools.BOTANY`. The reason this
matters more than a cosmetic mismatch is that ISS's **default** `getSchoolType()` is config-driven:

```
public SchoolType getSchoolType();
  0: getstatic     SpellConfigParameter.SCHOOL
  4: invokestatic  SpellConfigManager.getSpellConfigValue(AbstractSpell, SpellConfigParameter)
```

`SpellConfigManager` is a `SimpleJsonResourceReloadListener` over the datapack folder
`irons_spellbooks_spell_config`, seeded from `DefaultConfig` into a per-spell JSON with keys
`school, min_rarity, max_level, enabled, cooldown_in_seconds, allow_crafting, power_multiplier,
mana_cost_multiplier`.

Therefore, in 1.9.0:
- the generated ISS config file for each Iron's Botany spell says `irons_spellbooks:nature`;
- the runtime says `ironsbotany:botany`;
- **an operator editing the school in that config file changes nothing**, because the override
  ignores it;
- any add-on that reads the config rather than calling `getSchoolType()` sees Nature.

#### 2.8.1 Two follow-on facts that shaped the fix (and one trap avoided)

**(a) A spell-config datapack override would have been a severe regression.** The obvious way to
force upgraded worlds onto the canonical school is to ship
`data/ironsbotany/irons_spellbooks_spell_config/<spell>.json`. `SpellConfigManager.onDatapackSync`
disassembles to:

```java
if (INSTANCE.dirty) {
    INSTANCE.dirty = false;
    if (INSTANCE.datapackOverride != null) {
        ok = INSTANCE.buildConfigManager(INSTANCE.datapackOverride, true);   // datapack ONLY
        INSTANCE.datapackOverride = null;
    } else {
        ok = INSTANCE.buildConfigManager(toJson(getConfigFiles(configDir)), true);  // folder ONLY
    }
}
```

The two sources are **mutually exclusive, not merged**. Shipping even one spell-config JSON would
make `datapackOverride` non-null on every world load and therefore cause ISS to **ignore the
operator's entire `config/irons_spellbooks/spells/` directory for every spell in the modpack**,
Iron's Spells' own spells included. This approach was rejected. No spell-config datapack is shipped.

**(b) ISS never repairs an existing spell config file.** `generateSpellConfigFile` short-circuits:

```
76: invokevirtual File.exists:()Z
79: ifeq 96
83: ifne 96          ← if (exists && !overwrite)
92: Pair.of(false, file)
95: areturn
```

So a world upgraded from 1.9.0 keeps `"school": "irons_spellbooks:nature"` in
`config/irons_spellbooks/spells/ironsbotany/*.json` indefinitely. Correcting `DefaultConfig` alone
fixes new installs only.

**Resulting design.** `AbstractBotanicalSpell` keeps an explicit `getSchoolType()` override so the
runtime school is deterministic and independent of config-load timing (which also matters
client-side, before config sync, where `getSpellConfigValue` would otherwise fall back to the
global default school). `DefaultConfig` is corrected so new installs generate the right file. The
migration service repairs the stale value in place — scoped strictly to the `ironsbotany`
namespace subdirectory, only when the value is exactly the legacy `irons_spellbooks:nature`, and
logging every file it touches.

### 2.9 P1 — Botany attributes are registered but never attached

**VERIFIED.** `IBAttributes` registers `botany_spell_power`, `botany_magic_resist` and
`mana_efficiency`. `EntityAttributeHandler` only handles `EntityAttributeCreationEvent` for the
Spark Swarm entity; nothing subscribes `EntityAttributeModificationEvent`, so no player ever has
these attributes.

ISS guards against the resulting absence rather than crashing —

```
getPowerFor(LivingEntity):
  16: invokevirtual AttributeMap.m_22171_ (hasAttribute)
  19: ifeq 41
  41: dconst_1       ← returns 1.0 when the attribute is absent
```

— which means every Botany power/resist bonus on every item is **silently inert**. No crash, no
effect, and tooltips that advertise a bonus that cannot exist.

`mana_efficiency` is separately incoherent: `RangedAttribute("…", default 1.0, min 0.0, max 1.0)`
with equipment adding *positive* modifiers to a value already at its maximum, and no reader
anywhere in the cost path.

### 2.10 P2 — Aura cache key, and the inert mana-network trigger

**VERIFIED.** `FlowerAuraRegistry.AURA_CACHE` is `Map<UUID, CachedAuraData>` — no radius, no
dimension, no position (`FlowerAuraRegistry.java:25`), while callers pass radius 8 and 16.
A radius-16 miss scans `33³ = 35 937` positions.

`SpellManaNetworkIntegration.getTriggerType` maps Botany **and** Nature to `NATURE` before any
name check (`SpellManaNetworkIntegration.java:83-88`), and only the `WATER` trigger has an
implementation — so the feature is inert for every spell this mod ships.

---

## 3. What could and could not be executed in this environment

Stated plainly, because the brief forbids claiming a fix is verified when it is not.

**Executed**
- clean Gradle build from the `v1.9.0` tag (`BUILD SUCCESSFUL`);
- full JAR entry enumeration (582 entries) and SHA-256;
- programmatic decode of **every** PNG in the artifact (86/86 pass, dimensions + alpha asserted);
- bytecode-level contract verification of ISS 3.16.1 (`SchoolType`, `AbstractSpell`,
  `SpellConfigManager`, `SpellConfigParameter`, the five cast events, `MagicData`) and of
  Botania 450 (`ManaItemHandler`, `ManaItemHandlerImpl`, `ManaItem`, `ManaPool`,
  `BotaniaForgeCapabilities`);
- source audit of all 147 Java files against the audit table.

**Not executed — no interactive client, and the add-ons are not vendored**
- launching the real client or a dedicated server;
- installing ISS-Scroll Descriptions / ISS-Restrictions and opening a scroll;
- world upgrade fixtures from real 1.7.x / 1.9.0 saves;
- the screenshot-based manual QA record;
- comparison against the **public** 1.9.0 CurseForge artifact.

Those rows are carried forward as **open** in the release matrix rather than being marked passed.
`gradlew runData` / `runGameTestServer` additionally cannot run here: the dependencies in `libs/`
are production (obfuscated) JARs supplied `compileOnly`, so no Forge run task can boot — this is a
pre-existing property of the project recorded in `build.gradle`, not a regression.

---

## 4. Consequences for the plan

1. The crash, the missing icons, and a large part of "interactions are unreliable" reduce to
   **three** verified defects: the unstyled school component (2.1), the misuse of
   `ManaItemHandler.requestMana` (2.4), and the non-transactional cost path (2.5–2.7).
2. Fixing the school metadata is a two-line change; **proving** it stays fixed is the real work,
   so it is landed together with a contract test that exercises every public `SchoolType` accessor.
3. The mana rework is not a speculative refactor: it replaces an API misuse that is demonstrably
   wrong for N=1 and N≥3 sources, with per-source capability access that can be planned and
   committed exactly.
4. `SpellOnCastEvent.setManaCost` lets the bridge stop intercepting `ChangeManaEvent` altogether,
   which removes the entire class of "unrelated debit inherits a refund" bugs instead of narrowing it.
5. The purple/black report has **no** confirmed cause. It gets a build-time gate and an open
   matrix row, not a speculative fix.

## 5. Open items carried into the release matrix

| # | Item | Why still open |
| --- | --- | --- |
| O-1 | Public 1.9.0 JAR vs `v1.9.0` tag | public artifact unavailable here; release provenance unproven either way |
| O-2 | Purple/black render | not reproduced; no named root cause (§2.3) |
| O-3 | Add-on profile (Scroll Descriptions / Restrictions) | add-on JARs not vendored; contract test substitutes at the API level only |
| O-4 | Real world-upgrade fixtures | no 1.7.x / 1.9.0 saves available |
| O-5 | Client / dedicated-server smoke, GameTest | Forge run tasks cannot boot against obfuscated `compileOnly` deps |
