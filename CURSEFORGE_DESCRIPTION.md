# Iron's Botany

**Bridge the gap between Botania's natural mana and Iron's Spells 'n Spellbooks' arcane power.**

Iron's Botany is a compatibility mod that creates deep, rewarding synergy between two of Minecraft's best magic mods. Harness Botania's flowers and mana network to fuel a new school of spellcasting — or let your spells reshape the mana network itself.

---

## What Does This Mod Do?

At its simplest: **Botania mana can power ISS spells, and ISS spells can affect Botania systems.** At its deepest: six stages of integration turn you into a battle mage who commands both natural and arcane forces simultaneously.

Everything is configurable. Want just mana conversion? Enable bare-bones mode. Want the full experience? All six synergy stages activate by default.

---

## Getting Started

1. **Craft a Botanical Focus** (Mana Pearls + Runes of Spring around a Mana Ring) — **sneak + right-click** to equip it in a Curio slot.
2. **Toggle Siphon Mode** (plain right-click) — your Focus begins passively converting Botania mana into ISS spell mana.
3. **Craft your first spell scroll** at an ISS Scroll Forge, using a Botany focus and one of the three Mana Inks.
4. **Explore the synergy** — carry Botania runes for spell catalysts, build near flowers for aura buffs, and climb the Manasteel → Elementium → Terrasteel → Gaia equipment ladder.

Craft the **Chronicle of the Green Mage** (Book + Petals + Arcane Essence) for the complete in-game reference.

---

## The Botanical Spell School

9 spells in Iron's Botany's own custom **Botany** spell school — registered against ISS's `SchoolRegistry` with its own focus tag (`#ironsbotany:focus/botany`), spell-power / magic-resist attributes, and damage type. Optional dual-cost mechanics can consume both Botania and ISS mana.

| Spell | Max Level | Rarity | Description |
|-------|-----------|--------|-------------|
| Mana Bloom | 5 | Common | Summons Botania mystical flowers on nearby ground. Flower count and range scale with level. |
| Botanical Burst | 8 | Common | Fires a botanical projectile that deals magic damage. Catalysts can add piercing or extra projectiles. |
| Flower Shield | 10 | Uncommon | Creates a petal barrier that absorbs damage and grants Resistance. Shield HP scales with level. |
| Living Root Grasp | 6 | Common | Roots nearby enemies in place, applying Slowness and Weakness. |
| Spark Swarm | 7 | Rare | Summons mana sparks that orbit and attack nearby enemies. Spark count scales with level. |
| Runic Infusion | 10 | Rare | Grants Strength, Speed, and Regeneration. Carrying Botania runes in your inventory enhances the effect. |
| Petal Storm | 5 | Uncommon | Unleashes a spiral storm of petals that damages and knocks back nearby enemies. |
| Gaia's Wrath | 10 | Legendary | Channels the fury of Gaia to deal massive AoE damage, applying Wither, Weakness, and Slowness. |
| Mana Rebirth | 5 | Epic | Heals the caster, removes negative effects, and grants Regeneration and Absorption. Can prevent death at higher levels. |

**Scroll Forge citizenship:** the Botanical Focus and the Spell Petal are both members of `#ironsbotany:focus/botany`, which is registered into ISS's `#irons_spellbooks:school_focus` — so a Botany focus is accepted in the Scroll Forge's focus slot. The three **Mana Inks** (Minor / Greater / Prime) are real ISS ink items at Uncommon, Rare and Epic rarity, accepted in the ink slot.

Every spell exposes per-spell `botania_mana_cost` and `dual_cost_enabled` config parameters, overridable by server operators through ISS's own spell-config datapack mechanism.

---

## Equipment

### Curios
- **Botanical Focus** — passively converts Botania mana to ISS mana. Sneak + right-click to equip, plain right-click to toggle Siphon Mode. The core item that bridges both mana systems.
- **Botanical Ring** — +25 max mana, +5% all spell power.
- **Mana Reservoir Ring** — +100 max mana; auto-converts Botania mana into ISS mana when your ISS mana runs low.
- **Daybloom Amulet** — +15% Nature and Botany spell power and +5% cast speed while standing in direct sunlight.
- **Gaia's Blessing** — Botany spells cast at +1 effective level, paid for by draining a nearby Mana Pool (16-block scan). Crafted at the Terra Plate.

### Melee: the Mage Sword ladder
Real combat hits generate Botania mana, rate-limited so it cannot be autoclicker-farmed. The aggressive alternative to the pure-caster wands.

| Tier | Sword | Spell Power | Max Mana | Cooldown / Cost | Mana per Hit |
|------|-------|-------------|----------|-----------------|--------------|
| Terrasteel | **Terrasteel Spell Blade** (3000 dur.) | +25% | +200 | −20% mana cost | 5,000 |
| Elementium | **Elementium Spell Sword** | +18% | +150 | −12% cooldown | 3,500 |
| Gaia | **Gaia Spell Sword** | +38% | +250 | −28% cooldown | 7,000 |

The Terrasteel Spell Blade is smithed from a Botania Terra Sword; the Elementium and Gaia swords continue the smithing ladder.

### Ranged: the Wand ladder
Hold one and cast from your spellbook. All values are config-driven.

| Tier | Wand | Spell Power | Cooldown | Mana Efficiency | Notes |
|------|------|-------------|----------|-----------------|-------|
| Pre | **Livingwood Staff** (1000 dur.) | +10% Botany | — | — | Stores 500,000 Botania mana. Speed casting channel (1.2× cast speed, 1.1× range). |
| 1 | **Manasteel Wand** (750 dur.) | +10% Botany | +5% cast-time reduction | +0.05 | +20 max mana. A real self-casting ISS staff. |
| 2 | **Elementium Wand** (1500 dur.) | +18% | −10% | +0.10 | **Elven Favor** — 15% chance to refund 25% of a spell's mana cost. |
| 3 | **Terrasteel Wand** (2500 dur.) | +28% | −15% | +0.12 | |
| 4 | **Gaia Wand** (5000 dur.) | +40% | −25% | +0.15 | The endgame casting implement. |

The **Dreamwood Scepter** (2000 dur.) is a separate Alfheim utility rather than a ladder rung: +20% Botanical spell power, and it converts 100% of an ISS mana cost into Botania mana, letting you cast ISS spells on natural power. Regen casting channel (1.3× cast speed, +0.1 mana regen). Traded for at the Alfheim Portal by upgrading a Livingwood Staff.

### Mage Armor: four tiers
Entry → endgame, each a 4-piece set smithed up from the previous tier. Per-piece values shown; a full set is 4×. All config-driven.

| Tier | Set | Spell Power / pc | Max Mana / pc | Other / pc | Set Bonus (4 pieces) |
|------|-----|------------------|---------------|------------|----------------------|
| Entry | **Manasteel Wizard** | +5% | +50 | — | −5% Botania mana cost on Botanical casts |
| Mid | **Elementium Mage** | +8% | +75 | +0.03 mana efficiency | 10% chance to refund part of a spell's mana cost |
| Late | **Terrasteel Mage** | +12% | +100 | −3% cooldown | −20% incoming damage while above 50% max ISS mana |
| Endgame | **Gaia Mage** | +15% | +125 | −4% cooldown | **Mana Shield** — absorbs 50% of incoming damage using your Botania mana |

Armour values (helmet / chest / legs / boots), enchantability and toughness:

| Set | Protection | Ench. | Toughness | Knockback Res. | Repairs with |
|-----|-----------|-------|-----------|----------------|--------------|
| Manasteel Wizard | 2 / 7 / 5 / 2 | 18 | 1.0 | 0.0 | Manasteel Ingot |
| Elementium Mage | 3 / 7 / 5 / 2 | 20 | 1.5 | 0.0 | Elementium Ingot |
| Terrasteel Mage | 3 / 8 / 6 / 3 | 22 | 2.0 | 0.05 | Terrasteel Ingot |
| Gaia Mage | 3 / 8 / 6 / 3 | 25 | 3.0 | 0.10 | Gaia Ingot |

### Spellbooks and Scrolls
- **Terrasteel Spellbook** — 12 spell slots, Rare. +200 max mana, plus Botany and Nature spell power.
- **Arcane Codex** — 14 spell slots, Epic, fire-resistant. +300 max mana, cooldown reduction, Botany spell power. Crafted at the Terra Plate.
- **Elementium Scroll** — a reusable spell scroll that pulls its cast cost from your Botania mana network, falling back to single-use when that mana is exhausted. Made by trading a bound spell scroll at the Alfheim Portal; the bound spell carries across.

### Upgrade Orbs
Applied at the Arcane Anvil; each orb can only be applied once per item. Iron's Botany weapons, wands, spellbooks and mage armour all carry ISS's `can_be_upgraded` tag, so both Iron's Botany and Iron's Spells orbs work on them.

- **4 original orbs** — Orb of Flora, Orb of the Pool, Orb of Bursting, Orb of Terran Might.
- **8 ISS-school orbs**, crafted at the Runic Altar — Fire, Frost, Lightning, Holy, Ender, Blood, Nature and Eldritch Power, each boosting its matching Iron's Spells school.

### Crafting Components
- **Mana-Infused Essence** — Mana Powder + Arcane Essence. Used in weapon and orb crafting.
- **Botanical Crystal** — Mana Diamond + Dragonstone. Used in advanced recipes and the Mana Conduit.
- **Spell Petal** — Botania Petals + Arcane Essence (yields 4). Also a valid Scroll Forge focus for the Botany school.
- **Mana Inks** — Minor / Greater / Prime, made at the Petal Apothecary. Prime Ink can also be traded for at the Alfheim Portal at a discount.

---

## Blocks

- **Spell Reservoir** — stores ISS mana and distributes it to players within 5 blocks. Right-click with any Botania mana item to deposit. Comparator output. Configurable capacity (default 1000).
- **Mana Conduit** — place adjacent to a Botania mana pool to convert pool mana into ISS mana for players within 8 blocks (configurable), and to feed adjacent Spell Reservoirs. Comparator output. Crafted from Manasteel Ingots + Botanical Crystals + a Mana Tablet.
- **Arcane Mana Altar** — a block entity that is a Botania `ManaPool`, `ManaReceiver` and `SparkAttachable` all at once, holding 1,000,000 mana by default. Because it is a genuine mana pool, a player casting in range simply draws from it through Botania's normal pool path — Sparks can fill it, and no special wiring is needed.

The Reservoir and the Conduit share a single per-tick transfer budget fairly among every nearby player, so standing in a crowd does not silently multiply a block's throughput.

**Direct Pool Access:** spells can draw Botania mana straight from nearby mana pools when your carried mana runs dry — 8-block search radius by default, no special block required.

---

## Mana Network Citizenship

Iron's Botany items attach Botania's `MANA_ITEM` capability per stack, so they show up in the Botania mana HUD, accept Spark deposits, and are drained by `ManaItemHandler.requestMana` alongside Mana Tablets. Every capacity is config-driven:

| Item | Default Capacity |
|------|------------------|
| Gaia Wand | 1,000,000 |
| Livingwood Staff | 500,000 |
| Terrasteel Wand | 500,000 |
| Arcane Codex | 500,000 |
| Dreamwood Scepter | 250,000 |
| Elementium Wand | 250,000 |
| Terrasteel Spellbook | 200,000 |
| Mana Reservoir Ring | 200,000 |
| Manasteel Wand | 50,000 |
| Botanical Focus | 50,000 |
| Botanical Ring | 25,000 |

When a spell or mechanic needs Botania mana, Iron's Botany draws from — and **sums across** — every source you carry or wear: Mana Tablets, Mana Rings, Greater Bands of Mana, the Mana Mirror (through its bound pool) and every Iron's Botany mana item. A cost that no single item can cover on its own is still paid. Nearby mana pools are the fallback. Each layer has its own toggle.

### Mana HUD
A client-side Botania mana bar shows your aggregated carried mana and pulses when a Spell Reservoir, Mana Conduit or Arcane Mana Altar is nearby. Position, scale, visibility and the nearby-pulse animation are all client config options — the pulse can be turned off for motion sensitivity without losing the indicator itself.

---

## Six Stages of Deep Synergy

Each stage is independently toggleable. Use `bareBonesMode` to disable everything except mana conversion, or `enableDeepSynergy` as a master switch.

### Stage 1: Spell Catalysts
Hold Botania runes or lenses in your inventory to modify your spells. 9 catalysts across 4 tiers:

| Catalyst | Tier | Effect |
|----------|------|--------|
| Rune of Fire | Advanced | +25% fire damage, lingering burn |
| Rune of Water | Advanced | Converts healing to AoE splash |
| Rune of Earth | Advanced | +30% shield strength, knockback resistance |
| Rune of Air | Advanced | +30% projectile and casting speed |
| Rune of Mana | Advanced | −25% mana cost |
| Velocity Lens | Basic | +50% projectile speed, +30% range |
| Bore Lens | Advanced | Projectiles pierce up to 3 targets |
| Terrasteel | Elite | 20% critical chance (2× damage), −15% cooldown |
| Gaia Spirit | Legendary | +50% damage, −30% cooldown, Gaia's blessing |

Up to 3 catalysts stack by default. Catalysts may be consumed on use (configurable chance, default 0%).

### Stage 2: Casting Channels
Each botanical casting implement provides a profile that changes how your spells behave:

- **Livingwood Staff Channel** — speed build: 1.2× casting speed, 1.1× range. Best for sustained DPS.
- **Dreamwood Focus Channel** — efficiency build: 1.3× casting speed, +0.1 mana regen, 0.9× range. Best for prolonged fights.
- **Terra Rod Channel** — alpha-strike build: 1.75× burst damage, but 1.5× cooldowns, 0.8× casting speed, 1.3× mana cost.

### Stage 3: Flower Auras
Nearby Botania functional flowers passively enhance your spellcasting. Strength scales with distance — stand closer for maximum effect.

- **Bellethorne** (8 blocks) — buffs shield/damage spells: +30% thorns, +20% damage
- **Jaded Amaranthus** (10 blocks) — buffs summon spells: +5s duration, +50% health, 30% extra summon chance
- **Heisei Dream** (12 blocks) — buffs illusion spells: +40% damage, applies confusion
- **Rannuncarpus** (6 blocks) — buffs ritual spells: +30% cast speed, −15% mana cost, auto-ritual placement

Build a spell garden around your casting area for stacking bonuses (up to 5 auras by default).

### Stage 4: Spell-Triggered Mana Events (Experimental)
Your spells send ripples through the Botania mana network. 8 trigger types (Lightning, Earth, Nature, Fire, Water, Wind, Botanical, Arcane) affect nearby Botania infrastructure based on the spell you cast, with distance-based intensity scaling. Disabled by default — enable via `enableSpellManaEvents`.

### Stage 5: Corporea Logistics
High-tier spells (level 5+) automatically request reagent components from nearby Corporea networks. Level 8+ spells request Gaia Spirit components. Keep the network stocked and your spell infrastructure sustains itself.

### Stage 6: Alfheim Integration
- Spells cast in Alfheim gain up to **+50% power** (Botanical), +35% (nature), or +25% (all others), plus −20% cooldown and +30% range.
- Spell scrolls crafted in Alfheim gain **dual-school properties**.
- Spellbooks develop **attunement levels** over time spent in Alfheim (1 hour per level, max 3). Each level grants −5% cooldown, −3% mana cost, +4% spell power; level 3 unlocks dual-school scroll access.

### Boss Integration: Gaia Guardian Spell Trials
The Gaia Guardian fight gains two new phases when Iron's Botany is installed:
- **Phase 1 (>50% HP):** environmental magic requirement — spells deal 50% less damage without active flower auras, but +50% with them.
- **Phase 2 (<50% HP):** school-specific counters — the Guardian resists Fire and Ice (70% resistance) but is **vulnerable to Botanical spells** (+50% damage).

The Gaia Guardian II hardmode fight also drops **Legendary Ink**, added through a Forge global loot modifier that leaves Botania's own loot table untouched.

---

## Mana Unification

Choose how the two mana systems interact:

| Mode | Description |
|------|-------------|
| **Hybrid** (default) | Both systems coexist with a configurable conversion ratio |
| **Botania Primary** | ISS spells consume Botania mana directly |
| **ISS Primary** | Botania items passively generate ISS mana |
| **Separate** | No conversion — spells require both mana types (challenging!) |
| **Disabled** | No mana integration |

Default conversion ratio: 1000 Botania mana = 1 ISS mana (configurable from 100 to 10,000). Optional bidirectional conversion has its own reverse ratio. A configurable **priority chain** (`botania`, `ars`, `iss` by default) decides which resource pays first, and when **Ars 'n Spells** is installed Iron's Botany detects it reflectively and yields cost routing to it rather than double-billing.

---

## Crafting Stations

Iron's Botany ships 60 recipes spread across every major station in both mods:

- **Crafting table** — Botanical Focus, Botanical Ring, Chronicle of the Green Mage, the three components, Spell Reservoir, Mana Conduit, the four base orbs and the Manasteel Wizard set.
- **Smithing table** — the whole upgrade ladder: the Terrasteel Spell Blade from a Botania Terra Sword, and each mage armour and sword tier smithed up from the one below.
- **Petal Apothecary** — the three Mana Inks, the Mana Reservoir Ring, the Daybloom Amulet.
- **Runic Altar** — the 8 ISS-school spell-power orbs.
- **Terra Plate** — Arcane Codex, Gaia's Blessing.
- **Alfheim Portal** — Livingwood Staff → Dreamwood Scepter, Manasteel Wand → Elementium Wand, Greater → Prime Ink at a discount, and the Elementium Scroll promotion (which carries the bound spell across).
- **Scroll Forge** — all Botany spell scrolls, using a Botany focus and the matching ink rarity.

---

## Discovery & Progression

- **12 Advancements** guide you through the mod's systems — from your first Botanical Focus through catalysts, flower auras, armour set bonuses, Corporea logistics, and casting in Alfheim.
- **Chronicle of the Green Mage** — a Patchouli guidebook with 23 entries across 5 categories (Getting Started, Spells, Equipment, Advanced Systems, Deep Synergy), covering every spell, item, catalyst, aura and mana mechanic.
- A 5-page Iron's Botany entry is also injected into the **Lexica Botania**, readable from inside Botania's own guidebook.
- **Loot Integration** — mod items appear in vanilla structure chests: Spell Petals and Mana-Infused Essence in village houses, Botanical Crystals and Spell Petals in abandoned mineshafts, the Chronicle in stronghold libraries, and Orbs of Terran Might in end city treasure. Toggleable.

---

## Configuration

Around 140 options across 24 groups live in `ironsbotany-common.toml`, with 7 more in the client config:

| Category | Key Options |
|----------|-------------|
| **Master Toggles** | Bare-bones mode, deep synergy master switch |
| **Mana System** | Unification mode, conversion ratio, bidirectional conversion, dual-cost, priority chain, reservoir/conduit capacity and radius, pool access and search radius, inventory and Mana Mirror sources |
| **Spells** | School toggle, power and cooldown multipliers, particle toggles |
| **Equipment** | Per-item stat groups for all three mage swords, all five wands, the Livingwood Staff and the Dreamwood Scepter |
| **Mage Armor** | Per-tier spell power, max mana, cooldown reduction and mana efficiency |
| **Mage Armor Set Bonuses** | Per-set bonus strength and thresholds |
| **Mana Network Capacities** | Per-item Botania mana capacity, plus the Arcane Mana Altar pool |
| **Spell Mechanics** | Mana Rebirth revival, Flower Shield HP, root immobilization, rune scaling |
| **Per-Spell Multipliers** | Power and cooldown multipliers for each of the 9 spells |
| **Balance** | Vanilla loot injection toggle |
| **Casting Channels** | Toggle |
| **Spell Catalysts** | Toggle, consumption chance, durability damage, max stacking, power |
| **Flower Auras** | Toggle, range and strength multipliers, stacking limit, particles |
| **Deep Integration** | Mana events, Corporea logistics, Alfheim boost / scrolls / attunement |
| **Client** | Mana HUD toggle, X/Y offset, scale, nearby pulse, mana particles |

A handful of reserved-but-inert options are labelled `[NOT IMPLEMENTED]` directly in the generated config file, so a setting that currently does nothing says so rather than quietly misleading you.

### Server Command
- `/irons_botany reload` (permission level 2) — flushes runtime caches so TOML and datapack changes take effect without a restart.

---

## Supported Languages

Iron's Botany is fully translated into 22 languages beyond US English:

| Language | Locale | Language | Locale |
|----------|--------|----------|--------|
| Afrikaans | af_za | Korean | ko_kr |
| Arabic | ar_sa | Dutch | nl_nl |
| Bengali | bn_bd | Brazilian Portuguese | pt_br |
| German | de_de | Russian | ru_ru |
| British English | en_gb | Turkish | tr_tr |
| Argentine Spanish | es_ar | Ukrainian | uk_ua |
| Spanish (Spain) | es_es | Vietnamese | vi_vn |
| Mexican Spanish | es_mx | Simplified Chinese | zh_cn |
| French | fr_fr | Traditional Chinese (HK) | zh_hk |
| Hindi | hi_in | Traditional Chinese (TW) | zh_tw |
| Italian | it_it | Japanese | ja_jp |

---

## Requirements

**Required:**
- Minecraft Forge 1.20.1 (47.4.16+)
- Botania 1.20.1-450+
- Iron's Spells 'n Spellbooks 1.20.1-**3.16+** (tested on 3.16.1; 3.15.2 users should stay on Iron's Botany 1.8.0)
- Curios API 5.14.0+

**Optional:**
- Patchouli 84+ (in-game guidebook: *Chronicle of the Green Mage*)
- Ars 'n Spells 1.8.0+ (auto-detected via reflection; cost routing yields cleanly when present)
- JEI (recipe viewing)
