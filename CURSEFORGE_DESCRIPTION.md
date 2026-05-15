# Iron's Botany

**Bridge the gap between Botania's natural mana and Iron's Spells 'n Spellbooks' arcane power.**

Iron's Botany is a compatibility mod that creates deep, rewarding synergy between two of Minecraft's best magic mods. Harness Botania's flowers and mana network to fuel a new school of spellcasting — or let your spells reshape the mana network itself.

---

## What Does This Mod Do?

At its simplest: **Botania mana can power ISS spells, and ISS spells can affect Botania systems.** At its deepest: six stages of integration turn you into a battle mage who commands both natural and arcane forces simultaneously.

Everything is configurable. Want just mana conversion? Enable bare-bones mode. Want the full experience? All six synergy stages activate by default.

---

## Getting Started

1. **Craft a Botanical Focus** at a **Petal Apothecary** (purple petals + blue petals + Spell Petal + Mana-Infused Essence + ISS Arcane Essence). Build the Apothecary the standard Botania way; drop the reagent seed to start the recipe.
2. **Equip the Focus** to your Curios slot — either drag it into the slot via the inventory UI, or **sneak + right-click in hand**. Once equipped, **right-click in hand (no sneak)** toggles **Siphon Mode** on or off; while on, the Focus passively converts Botania mana into ISS spell mana once per second.
3. **Craft your first IB spell scroll** at a vanilla crafting table: combine `irons_spellbooks:common_ink` with `ironsbotany:spell_petal` (the focus token) — the new `ironsbotany:ib_spell_scroll` recipe produces a fully bound ISS scroll for the spell. No ISS Scroll Forge required.
4. **Explore the synergy** — carry Botania runes for spell catalysts, build near functional flowers for aura buffs, and progress through Botania's tiers to unlock late-game cross-mod gear via Runic Altar and Elven Trade recipes.

Craft the **Chronicle of the Green Mage** (Patchouli guidebook) at the Runic Altar for a complete in-game reference.

---

## The Botanical Spell School

9 Nature-school spells with dual-cost mechanics that consume both Botania and ISS mana. Uses ISS's built-in Nature school; cost, cooldown, and power are governed by ISS-native attributes (`max_mana`, `mana_regen`, school-specific spell power) — no parallel attribute stack.

| Spell | Max Level | Type | Description |
|-------|-----------|------|-------------|
| Mana Bloom | 5 | Utility | Summons Botania mystical flowers on nearby ground. Flower count and range scale with level. |
| Botanical Burst | 8 | Attack | Fires a botanical projectile that deals magic damage. Catalysts can add piercing or extra projectiles. |
| Flower Shield | 10 | Defense | Creates a petal barrier that absorbs damage and grants Resistance. Shield HP scales with level. |
| Living Root Grasp | 6 | Control | Roots nearby enemies in place, applying Slowness and Weakness. |
| Spark Swarm | 7 | Summon | Summons mana sparks that orbit and attack nearby enemies. Spark count scales with level. |
| Runic Infusion | 10 | Buff | Grants Strength, Speed, and Regeneration. Carrying Botania runes in your inventory enhances the effect. |
| Petal Storm | 5 | AoE | Unleashes a spiral storm of petals that damages and knocks back nearby enemies. |
| Gaia's Wrath | 10 | Legendary | Channels the fury of Gaia to deal massive AoE damage, applying Wither, Weakness, and Slowness. Ritual-grade (Corporea Recall eligible). |
| Mana Rebirth | 5 | Healing | Heals the caster, removes negative effects, and grants Regeneration and Absorption. Ritual-grade. Can prevent death at higher levels. |

All spell scrolls are crafted at a vanilla crafting table from `irons_spellbooks:common_ink` plus an `ironsbotany:spell_petal` focus token; the result is an ISS scroll bound to the IB spell via `ISpellContainer`.

---

## Equipment

### Curios

- **Botanical Focus** — Curio that bridges both mana systems. **Right-click in hand to toggle Siphon Mode**; **sneak + right-click in hand to equip**, or drag it into the Charm slot via the inventory UI. When equipped, +50 max mana and +10% mana regeneration; Siphon Mode passively converts Botania mana from inventory items into ISS mana.
- **Botanical Ring** — +25 max mana, +5% spell power while worn. Stat bonuses applied via Curios attribute system.
- **Pool Attunement Charm** (1.7.0) — Curios charm slot. Right-click on a Botania Mana Pool to bind it (requires the Gaia Guardian advancement / Spell Overcharge unlock). While worn, the bound pool acts as a supplementary Botania mana source **for Nature-school spells only**, capped by `poolAttunementRange` (default 64 blocks) and `poolAttunementBandwidth` (default 50,000 mana per cast).

### Weapons

Four weapons that bridge both magic systems, each with a unique casting channel profile (registered at startup in 1.7.0):

- **Terrasteel Spell Blade** (3000 durability) — +25% spell power, +200 max mana, -20% cooldown. Melee attacks generate Botania mana. Crafted via smithing (Rune of Wrath + Terra Sword + Arcane Essence). Repairable with Terrasteel Ingots. Channel: Terra Rod (burst).
- **Livingwood Staff** (1000 durability) — +10% Botanical spell power, stores 500k Botania mana. Channel: Livingwood (sustained / efficiency — +50% mana regen, -15% burst, +10% cast speed, -10% mana cost on Nature spells).
- **Dreamwood Scepter** (2000 durability) — +20% Botanical spell power. Converts ISS mana costs to Botania mana — cast ISS spells using Botania power. Channel: Dreamwood Focus (+40% cast speed, -10% cooldown, +10% mana cost).
- **Gaia Spirit Wand** (5000 durability) — +30% spell power, -25% cooldowns. The ultimate casting implement. Channel: Terra Rod (+75% burst damage at the cost of +50% cooldown).

### Manasteel Wizard Armor

A full 4-piece battle mage armor set:
- +15% spell power per piece, +150 max mana per piece
- Protection: Helmet 2, Chestplate 7, Leggings 5, Boots 2; Toughness 1.0
- Repairable with Manasteel Ingots
- **Set Bonus (4 pieces):** Mana Shield — absorbs 50% of incoming damage using your Botania mana (10,000 mana per heart absorbed). 2-second internal cooldown.

### Upgrade Orbs

Enhance your equipment at the Arcane Anvil. Each orb can only be applied once per item:
- **Orb of Flora** — +10% Nature spell power
- **Orb of the Pool** — +100 max ISS mana
- **Orb of Bursting** — +5% all spell power
- **Orb of Terran Might** — +5% all spell power

### Crafting Components

Three core components used across many recipes. **1.7.0 moved every mid- and late-tier IB item to Botania-native progression — vanilla crafting tables only produce the entry-tier consumables (Spell Petal bulk variant, three of the four upgrade orbs, and the spell scrolls).**

| Item | Recipe | Where |
|------|--------|-------|
| **Spell Petal** (×4) | Botania petals + Arcane Essence + reagent seed | Petal Apothecary |
| **Botanical Focus** | Purple/blue petals + Spell Petal + Mana-Infused Essence + Arcane Essence + reagent seed | Petal Apothecary |
| **Botanical Ring** | Green/yellow petals + Spell Petal + Manasteel Ingot + reagent seed | Petal Apothecary |
| **Mana-Infused Essence** | Arcane Essence + 4,000 Botania mana | Mana Pool transformation |
| **Botanical Crystal** | Mana Diamond + Gaia Ingot + 2× Mana-Infused Essence | Runic Altar (12,000 mana) |
| **Botanical Grimoire** | Book + Spell Petal + Mana-Infused Essence + Enchanted Book | Runic Altar (8,000 mana) |
| **Mana Conduit** | 2× Manasteel + Mana-Infused Essence + Redstone Block | Runic Altar (10,000 mana) |
| **Spell Reservoir** | Manasteel + 2× Mana-Infused Essence + Obsidian + Arcane Essence | Runic Altar (8,000 mana) |
| **Livingwood Staff** | 3× Livingwood + Mana-Infused Essence + Botanical Crystal | Runic Altar (15,000 mana) |
| **Manasteel Wizard armor** (4 pieces) | Manasteel Ingots + Mana-Infused Essence | Runic Altar (10k–16k mana) |
| **Dreamwood Scepter** | Livingwood Staff + Dreamwood + Botanical Crystal | Elven Trade (portal-gated) |
| **Gaia Spirit Wand** | Dreamwood Scepter + 3× Gaia Ingot + Botanical Crystal | Elven Trade |
| **Orb of Terran Might** | Terrasteel + 2× Botanical Crystal + Nature Upgrade Orb | Elven Trade |

---

## Blocks

- **Spell Reservoir** — Stores ISS mana and distributes it to nearby players within a 5-block radius. Right-click with any Botania mana item to deposit mana. Supports comparator output. Configurable capacity (default 1000).
- **Mana Conduit** — Place adjacent to a Botania mana pool to automatically convert pool mana into ISS mana for nearby players within an 8-block radius. Also feeds ISS mana into any adjacent Spell Reservoirs. Supports comparator output.

**Direct Pool Access:** Spells can draw Botania mana directly from nearby mana pools when your inventory runs dry (configurable, 8-block search radius by default, **per-player cube-scan cache** since 1.7.0).

---

## Six Stages of Deep Synergy

Iron's Botany rewards players who master both mods with escalating layers of integration. Each stage is independently toggleable. Use `bareBonesMode` to disable everything except mana conversion, or `enableDeepSynergy` as a master switch.

### Stage 1: Spell Catalysts *(default ON)*
Hold Botania runes or lenses in your inventory to modify your spells. 9 hardcoded Java catalysts across 4 tiers:

| Catalyst | Tier | Effect |
|----------|------|--------|
| Rune of Fire | Advanced | +25% fire damage, lingering burn |
| Rune of Water | Advanced | Converts healing to AoE splash |
| Rune of Earth | Advanced | +30% shield strength, knockback resistance |
| Rune of Air | Advanced | +30% projectile and casting speed |
| Rune of Mana | Advanced | -25% mana cost |
| Velocity Lens | Basic | +50% projectile speed, +30% range |
| Bore Lens | Advanced | Projectiles pierce up to 3 targets |
| Terrasteel | Elite | 20% critical chance (2x damage), -15% cooldown |
| Gaia Spirit | Legendary | +50% damage, -30% cooldown — requires the **Tier 4** unlock (Terrasteel Pickup advancement) |

Multiple catalysts can stack (configurable, max 3 by default). Catalysts may be consumed on use (configurable chance, default 0%).

**Runic Catalysis (1.7.0)** — pack authors and modpack curators can add new catalysts entirely via JSON under `data/ironsbotany/catalysts/*.json`. Templated catalysts support per-school filters and five modifier multipliers.

### Stage 2: Casting Channels *(default ON)*
Each botanical weapon provides a unique casting profile that changes how your spells behave:

- **Livingwood Staff Channel** — Sustained / efficiency: +50% mana regen, -15% burst, +10% cast speed, -10% mana cost (Nature spells only).
- **Dreamwood Focus Channel** — Speed: +40% cast speed, -10% cooldown, +10% mana cost (all spells).
- **Terra Rod Channel** — Alpha-strike: +75% burst damage, +50% cooldown penalty, -20% cast speed, +30% mana cost (damage spells only). Also bound to the Terrasteel Spell Blade and Gaia Spirit Wand.

### Stage 3: Flower Auras *(default ON)*
Nearby Botania functional flowers passively enhance your spellcasting. Strength scales with distance — stand closer for maximum effect.

- **Bellethorne** (8-block range) — Buffs shield/damage spells: +30% thorns, +20% damage
- **Jaded Amaranthus** (10-block range) — Buffs summon spells: +5s duration, +50% health, 30% extra summon chance
- **Heisei Dream** (12-block range) — Buffs illusion spells: +40% damage, applies confusion
- **Rannuncarpus** (6-block range) — Buffs ritual spells: +30% cast speed, -15% mana cost, auto-ritual placement

Build a spell garden around your casting area for stacking bonuses (up to 5 auras by default).

### Stage 4: Spell-Triggered Mana Events *(default OFF, experimental)*
Your spells send ripples through the Botania mana network. 8 trigger types (Lightning, Earth, Nature, Fire, Water, Wind, Botanical, Arcane) that affect nearby Botania infrastructure based on the spell you cast. Only the water-fill trigger is currently fully functional; other effects are stubbed.

### Stage 5: Corporea Reagent Recall *(default OFF, experimental)*
**Ritual-grade spells only** (Gaia's Wrath, Mana Rebirth) automatically request reagent components from nearby Corporea Indices. Scope was tightened in 1.7.0 from "any long cast" to ritual-grade so the system can't drain unrelated inventories during normal combat casts.

### Stage 6: Alfheim Integration *(default ON)*
- Spells cast in the Alfheim dimension gain up to **+50% power** (Botanical spells), +35% (nature spells), or +25% (all others), plus -20% cooldown and +30% range.
- Spellbooks develop **attunement levels** over time spent in Alfheim (1 hour per level, max 3). Each level grants: -5% cooldown, -3% mana cost, +4% spell power.
- **Dual-school scroll crafting** requires the Alfheim Portal advancement (Dual-School Unlock).

### Boss Integration: Gaia Guardian Spell Trials
The Gaia Guardian fight gains two new phases when Iron's Botany is installed:
- **Phase 1 (>50% HP):** Environmental magic requirement — spells deal 50% less damage without active flower auras, but +50% with them.
- **Phase 2 (<50% HP):** School-specific counters — the Guardian resists Fire and Ice schools (70% resistance) but is **vulnerable to Botanical spells** (+50% damage).

Defeating the Guardian permanently unlocks **Spell Overcharge** — every Nature-school spell you cast gains +5% damage from then on, and the Pool Attunement Charm becomes bindable.

---

## Phase 4 Content (1.7.0+)

- **Pool Attunement Charm** — see Curios section. The first cross-mod gear that requires a Botania endgame advancement to function.
- **Runic Catalysis** — datapack-driven catalyst tags under `data/ironsbotany/catalysts/`. Modpacks can extend or override the catalyst roster without touching Java.
- **Elven Bloom Scrolls** *(default OFF, experimental)* — rune-fused spell scrolls crafted within 8 blocks of an Alfheim Portal block gain +15% damage and -10% cooldown when cast. Enable via `enableElvenBloomScrolls` in config.
- **Spell Overcharge** — +5% permanent Nature-spell damage buff awarded on Gaia Guardian kill (the unified progression flag also gates Pool Attunement binding).

---

## Mana Unification

Choose how the two mana systems interact:

| Mode | Description |
|------|-------------|
| **Hybrid** (default) | Both systems coexist with configurable conversion ratio |
| **Botania Primary** | ISS spells consume Botania mana directly |
| **ISS Primary** | Botania items passively generate ISS mana |
| **Separate** | No conversion — spells require both mana types (challenging!) |
| **Disabled** | No mana integration |

Default conversion ratio: 1000 Botania mana = 1 ISS mana (configurable from 100 to 10,000). Bidirectional conversion available as an option. The 1.7.0 ManaHelper aggregation pass means split mana across multiple tablets/rings/bands now drains correctly across all sources.

---

## Discovery & Progression

- **12 Advancements** guide you through the mod's systems — from your first Botanical Focus through catalysts, flower auras, armor set bonuses, Corporea logistics, and casting in Alfheim.
- **Chronicle of the Green Mage** (Patchouli guidebook) — Comprehensive in-game reference with 22+ entries covering all spells, equipment, catalysts, flower auras, casting channels, mana systems, and the new 1.7.0 features. Crafted at the Runic Altar.
- **Loot Integration** — Iron's Botany items appear in vanilla structure chests via the Forge **Global Loot Modifier** system (`data/ironsbotany/loot_modifiers/`, new in 1.7.0): Spell Petals and Mana-Infused Essence in villages; Botanical Crystals and Spell Petals in mineshafts; Botanical Grimoires, Crystals and Essence in stronghold libraries; Orbs of Terran Might in end city treasure. Pack authors can override or disable individual entries by replacing the JSON files.

---

## Configuration

All settings are in `ironsbotany-common.toml` and `ironsbotany-server.toml` (new in 1.7.0). 90+ options across 15 categories:

| Category | Key Options |
|----------|-------------|
| **Master Toggles** | Bare-bones mode, deep synergy master switch |
| **Mana System** | Unification mode, conversion ratio, bidirectional toggle, dual-cost, reservoir/conduit capacity |
| **Mana Conduit** | Distribution radius, conversion rate, buffer capacity |
| **Mana Pool Access** | Direct pool access toggle, search radius (per-player cached since 1.7.0) |
| **Spells** | School toggle, power multiplier, cooldown multiplier |
| **Equipment** | Per-weapon stat bonuses |
| **Spell Mechanics** | Per-spell power/cooldown multipliers, shield HP, root immobilization |
| **Balance** | Cross-loot, orb effectiveness, vanilla loot injection toggle |
| **Casting Channels** | Toggle, global power multiplier |
| **Spell Catalysts** | Toggle, consumption chance, durability damage, max stacking, power multiplier |
| **Flower Auras** | Toggle, range/strength multipliers, stacking limit, particles |
| **Spell-Triggered Mana Events** | Toggle (off by default), duration, intensity, radius |
| **Corporea Logistics** | Toggle (**off by default since 1.7.0**), auto-request, search radius |
| **Alfheim Integration** | Dimension boost, dual-school scrolls, attunement |
| **Phase 4 Content (1.7.0+)** | `enablePoolAttunement`, `poolAttunementRange` (default 64), `poolAttunementBandwidth` (default 50,000), `enableElvenBloomScrolls` (off by default) |
| **Progression Gates** (server config) | Upstream advancement IDs for the three unified-progression milestones — pack-overridable |

---

## Supported Languages

Iron's Botany is fully translated into 22 languages:

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
- Iron's Spells 'n Spellbooks 1.20.1-3.15.2+
- Curios API 5.14.1+

**Optional:**
- Patchouli 1.20.1-84+ (in-game guidebook: *Chronicle of the Green Mage*)
- JEI (recipe viewing for all crafting + custom recipe types)
- Ars 'n' Spells 1.8.0+ (soft compat via reflective shim — Iron's Botany behaves correctly with or without ANS; see `COMPAT-ARS-N-SPELLS.md` in the repo)
