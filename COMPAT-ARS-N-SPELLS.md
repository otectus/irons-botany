# Iron's Botany × Ars 'n' Spells Compatibility

Both mods hook Iron's Spells 'n Spellbooks (ISS) mana. Iron's Botany v1.4.0+
soft-integrates with Ars 'n' Spells (ANS) v1.8.0+ through a reflective compat
shim (`com.ironsbotany.common.compat.ArsNSpellsCompat`). ANS is **not**
required — when it is absent, Iron's Botany behaves as before.

## What each mod means by "mode"

Both mods expose a config named `mana_unification_mode` with overlapping
names that do **different** things.

### Iron's Botany modes (`CommonConfig.MANA_UNIFICATION_MODE`)

Controls how Botanical spells interact with Botania mana and ISS mana.

| Mode               | Spell cost source                              |
|--------------------|------------------------------------------------|
| `BOTANIA_PRIMARY`  | Botany spells drain Botania mana only          |
| `ISS_PRIMARY`      | Botany spells drain ISS mana only              |
| `HYBRID` (default) | ISS mana, with optional Botania→ISS conversion |
| `SEPARATE`         | Botany spells require Botania **and** ISS mana |
| `DISABLED`         | Botania integration off                        |

### Ars 'n' Spells modes (`AnsConfig.MANA_UNIFICATION_MODE`)

Controls how Ars Nouveau source and ISS mana interact.

| Mode                   | Authoritative mana pool                |
|------------------------|----------------------------------------|
| `ISS_PRIMARY` (default) | ISS pool; Ars costs route through ISS |
| `ARS_PRIMARY`           | Ars pool; ISS `MagicData` is redirected via mixin |
| `HYBRID`                | Bidirectional sync                     |
| `SEPARATE`              | Dual cost on Ars spells (Ars + ISS %)  |
| `DISABLED`              | Integration off                        |

## Interaction matrix

Columns = Ars 'n' Spells mode. Rows = Iron's Botany mode.

| IB \ ANS          | ISS_PRIMARY   | ARS_PRIMARY   | HYBRID        | SEPARATE      | DISABLED      |
|-------------------|---------------|---------------|---------------|---------------|---------------|
| `BOTANIA_PRIMARY` | OK            | OK *(1)*      | OK            | OK            | OK            |
| `ISS_PRIMARY`     | OK (default combo for ISS-first builds) | Caveat *(2)*  | OK            | OK            | OK            |
| `HYBRID`          | **Recommended** | OK *(1)*     | OK            | Caveat *(3)*  | OK            |
| `SEPARATE`        | OK            | Caveat *(2)*  | OK            | Caveat *(4)*  | OK            |
| `DISABLED`        | OK            | OK            | OK            | OK            | OK            |

## Caveats

**(1) ARS_PRIMARY + Botany spells.** Under ANS ARS_PRIMARY, writes to ISS
`MagicData` are redirected into the Ars pool. Iron's Botany detects this and:
- reservoirs/conduits clamp against the Ars max (via
  `BridgeManager.getBridge().getMaxMana`), not the ISS `MAX_MANA` attribute;
- the Dreamwood Scepter's ISS pre-fund is scaled by
  `AnsConfig.CONVERSION_RATE_IRON_TO_ARS` so the Botania → Ars conversion
  matches ANS's expected rate.
Botany spells that directly consume Botania mana (BOTANIA_PRIMARY / SEPARATE)
are unaffected by ANS mode — they never touch the ISS pool.

**(2) IB `ISS_PRIMARY` or `SEPARATE` + ANS `ARS_PRIMARY`.** Botany spells
cost ISS mana, but ANS has made the Ars pool authoritative. Casts still
work (ISS cost is routed through the bridge), but Iron's Botany
reservoirs/conduits are effectively donating to the Ars pool. This is the
intended ANS semantic — flagged here so you understand the effect.

**(3) IB `HYBRID` + ANS `SEPARATE`.** If you cast an Ars spell while IB
HYBRID has dual-cost spells enabled, ANS's SEPARATE mode will charge Ars + ISS
for the Ars spell; Iron's Botany does not also charge Botania for Ars spells
(Botany spells are ISS spells). No interaction, just an inconsistency in
how "costs both systems" is interpreted — expected.

**(4) Both `SEPARATE`.** For a Botany spell: Botania + ISS. For an Ars spell:
Ars + ISS. There is no path on which a single cast charges all three pools
today — the two SEPARATE modes do not compose into triple-cost because
Botany spells and Ars spells route through disjoint code paths.

## Recommended combinations

- **Most tested:** IB `HYBRID` + ANS `ISS_PRIMARY` (both defaults).
- **Botania-first build:** IB `BOTANIA_PRIMARY` + ANS `ISS_PRIMARY`.
- **Ars-first build:** IB `BOTANIA_PRIMARY` + ANS `ARS_PRIMARY`. Botany spells
  stay on Botania mana; Ars spells stay on Ars mana; the two systems
  co-exist without crossing. Iron's Botany conduits/reservoirs become
  ornamental in this combo (they top up the Ars pool via the bridge).

## Things Iron's Botany does **not** do

- Does not register any Ars Nouveau glyphs or Ars Nouveau spells.
- Does not listen to `SpellCostCalcEvent` (the Ars-side cost event). Botany
  spells are ISS spells and use ISS cost paths; ANS's Blasphemy / Virtue
  Ring discounts do not apply to Botany spells.
- Does not add mixins into ISS or Ars code.
- Does not apply the Resonance multiplier — that is ANS's
  `MixinIronsSpellDamage` acting on `AbstractSpell.getSpellPower()`. Botany
  spells inherit the multiplier because they extend ISS's `AbstractSpell`;
  this is the intended ANS behaviour, not an IB integration.

## Implementation reference

- `src/main/java/com/ironsbotany/common/compat/ArsNSpellsCompat.java` —
  reflective shim. All ANS interaction funnels through this file.
- `src/main/java/com/ironsbotany/common/event/DreamwoodConversionHandler.java` —
  priority `LOW`, checks `event.isCanceled()`, scales ISS pre-fund under
  ARS_PRIMARY.
- `src/main/java/com/ironsbotany/common/block/entity/SpellReservoirBlockEntity.java`
  and `.../ManaConduitBlockEntity.java` — use
  `ArsNSpellsCompat.getEffectiveMaxMana(player)` for the transfer clamp.
- `src/main/resources/META-INF/mods.toml` — `ars_n_spells` declared as an
  optional `AFTER` dependency so mixin/subscriber ordering is stable when
  both mods are present.
