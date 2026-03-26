# Iron's Botany

**Bridge the gap between Botania's natural mana and Iron's Spells 'n Spellbooks' arcane power.**

Iron's Botany is a compatibility mod that creates deep, rewarding synergy between two of Minecraft's best magic mods. Harness Botania's flowers and mana network to fuel a new school of spellcasting — or let your spells reshape the mana network itself.

---

## What Does This Mod Do?

At its simplest: **Botania mana can power ISS spells, and ISS spells can affect Botania systems.** At its deepest: six stages of integration turn you into a battle mage who commands both natural and arcane forces simultaneously.

Everything is configurable. Want just mana conversion? Enable bare-bones mode. Want the full experience? All six synergy stages activate by default.

---

## Getting Started

1. **Craft a Botanical Focus** (Mana Pearl + Rune of Spring + Mana Ring) — equip it in a Curio slot.
2. **Toggle Siphon Mode** (right-click) — your Focus begins passively converting Botania mana into ISS spell mana.
3. **Craft your first spell scroll** at an ISS Scroll Forge — each Botanical spell requires Spell Petals and ink.
4. **Explore the synergy** — carry Botania runes for spell catalysts, build near flowers for aura buffs, and craft stronger weapons as you progress through Botania's tiers.

Craft the **Chronicle of the Green Mage** (Patchouli guidebook) for a complete in-game reference.

---

## The Botanical Spell School

A new spell school with 9 dual-cost spells that consume both Botania and ISS mana. Three custom attributes — **Botanical Spell Power**, **Botanical Resistance**, and **Mana Efficiency** — govern the school's scaling.

| Spell | Max Level | Type | Description |
|-------|-----------|------|-------------|
| Mana Bloom | 5 | Utility | Summons Botania mystical flowers on nearby ground. Flower count and range scale with level. |
| Botanical Burst | 8 | Attack | Fires a botanical projectile that deals magic damage. Catalysts can add piercing or extra projectiles. |
| Flower Shield | 10 | Defense | Creates a petal barrier that absorbs damage and grants Resistance. Shield HP scales with level. |
| Living Root Grasp | 6 | Control | Roots nearby enemies in place, applying Slowness and Weakness. |
| Spark Swarm | 7 | Summon | Summons mana sparks that orbit and attack nearby enemies. Spark count scales with level. |
| Runic Infusion | 10 | Buff | Grants Strength, Speed, and Regeneration. Carrying Botania runes in your inventory enhances the effect. |
| Petal Storm | 5 | AoE | Unleashes a spiral storm of petals that damages and knocks back nearby enemies. |
| Gaia's Wrath | 10 | Legendary | Channels the fury of Gaia to deal massive AoE damage, applying Wither, Weakness, and Slowness. |
| Mana Rebirth | 5 | Healing | Heals the caster, removes negative effects, and grants Regeneration and Absorption. Can prevent death at higher levels. |

All spell scrolls are crafted at the ISS Scroll Forge using Spell Petals and various ink tiers.

---

## Equipment

### Curios
- **Botanical Focus** — Equip in a Curio slot to passively convert Botania mana to ISS mana. Toggle siphon mode on/off with right-click. The core item that bridges both mana systems.
- **Botanical Ring** — +25 max mana, +5% Botanical spell power while worn. A simple passive boost.

### Weapons
Four weapons that bridge both magic systems, each with a unique casting channel profile:

- **Terrasteel Spell Blade** (3000 durability) — +25% spell power, +200 max mana, -20% mana cost. Melee attacks generate Botania mana. Crafted via smithing (Rune of Wrath + Terra Sword + Arcane Essence).
- **Livingwood Staff** (1000 durability) — +10% Botanical spell power, stores 500k Botania mana. Casting Channel: speed-focused (1.2x cast speed, 1.1x range).
- **Dreamwood Scepter** (2000 durability) — +20% Botanical spell power. Converts ISS mana costs to Botania mana — cast ISS spells using Botania power. Casting Channel: regen-focused (1.3x cast speed, +0.1 mana regen).
- **Gaia Spirit Wand** (5000 durability) — +30% spell power, -25% cooldowns. The ultimate casting implement. Casting Channel: burst-focused (1.75x burst damage, 1.5x cooldown penalty).

### Manasteel Wizard Armor
A full 4-piece battle mage armor set smithed from Manasteel:
- +15% spell power per piece
- +150 max mana per piece
- Protection: Helmet 2, Chestplate 7, Leggings 5, Boots 2
- Enchantability: 18, Toughness: 1.0
- **Set Bonus (4 pieces):** Mana Shield — absorbs 50% of incoming damage using your Botania mana (10,000 mana per heart absorbed)

### Upgrade Orbs
Enhance your equipment at the Arcane Anvil. Each orb can only be applied once per item:
- **Orb of Flora** — +10% Botanical spell power
- **Orb of the Pool** — +100 max ISS mana
- **Orb of Bursting** — Spells deal bonus Mana Burst damage
- **Orb of Terran Might** — +5% all spell power, +5% cooldown reduction

### Crafting Components
Three core components used across many recipes:
- **Mana-Infused Essence** — Mana Powder + Arcane Essence (shapeless). Used in weapon and orb crafting.
- **Botanical Crystal** — Mana Diamond + Dragonstone (shaped). Used in advanced recipes and the Mana Conduit.
- **Spell Petal** — Botania Petals + Arcane Essence (shapeless, yields 4). Used in all spell scroll recipes.

---

## Blocks

- **Spell Reservoir** — Stores ISS mana and distributes it to nearby players within a 5-block radius. Build it into your base for passive mana supply. Configurable capacity (default 1000).
- **Mana Conduit** — Place adjacent to a Botania mana pool to automatically convert pool mana into ISS mana for nearby players within an 8-block radius. Creates a "spellcasting zone" around your mana infrastructure. Crafted from Manasteel Ingot + Botanical Crystal + Mana Tablet.

**Direct Pool Access:** Spells can draw Botania mana directly from nearby mana pools when your inventory runs dry (configurable, 8-block search radius by default). No special block needed — just stand near a pool.

---

## Six Stages of Deep Synergy

Iron's Botany rewards players who master both mods with escalating layers of integration. Each stage is independently toggleable. Use `bareBonesMode` to disable everything except mana conversion, or `enableDeepSynergy` as a master switch.

### Stage 1: Spell Catalysts
Hold Botania runes or lenses in your inventory to modify your spells. 9 catalysts across 4 tiers:

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
| Gaia Spirit | Legendary | +50% damage, -30% cooldown, Gaia's blessing |

Multiple catalysts can stack (configurable, max 3 by default). Catalysts may be consumed on use (configurable chance, default 0%).

### Stage 2: Casting Channels
Each botanical weapon provides a unique casting profile that changes how your spells behave:

- **Livingwood Staff Channel** — Speed build: 1.2x casting speed, 1.1x range. Best for sustained DPS.
- **Dreamwood Focus Channel** — Efficiency build: 1.3x casting speed, +0.1 mana regen, 0.9x range. Best for prolonged fights.
- **Terra Rod Channel** — Alpha-strike build: 1.75x burst damage, but 1.5x cooldowns, 0.8x casting speed, 1.3x mana cost. Best for single devastating hits.

### Stage 3: Flower Auras
Nearby Botania functional flowers passively enhance your spellcasting. Strength scales with distance — stand closer for maximum effect.

- **Bellethorne** (8-block range) — Buffs shield/damage spells: +30% thorns, +20% damage
- **Jaded Amaranthus** (10-block range) — Buffs summon spells: +5s duration, +50% health, 30% extra summon chance
- **Heisei Dream** (12-block range) — Buffs illusion spells: +40% damage, applies confusion
- **Rannuncarpus** (6-block range) — Buffs ritual spells: +30% cast speed, -15% mana cost, auto-ritual placement

Build a spell garden around your casting area for stacking bonuses (up to 5 auras by default).

### Stage 4: Spell-Triggered Mana Events
Your spells send ripples through the Botania mana network. 8 trigger types (Lightning, Earth, Nature, Fire, Water, Wind, Botanical, Arcane) that affect nearby Botania infrastructure based on the spell you cast. Distance-based intensity scaling with configurable duration and radius.

### Stage 5: Corporea Logistics
High-tier spells (level 5+) automatically request reagent components from nearby Corporea networks. Level 8+ spells request Gaia Spirit components. Your spell infrastructure becomes self-sustaining — just keep the Corporea network stocked.

### Stage 6: Alfheim Integration
- Spells cast in the Alfheim dimension gain up to **+50% power** (Botanical spells), +35% (nature spells), or +25% (all others), plus -20% cooldown and +30% range.
- Spell scrolls crafted in Alfheim gain **dual-school properties**.
- Spellbooks develop **attunement levels** over time spent in Alfheim (1 hour per level, max 3). Each level grants: -5% cooldown, -3% mana cost, +4% spell power. Level 3 unlocks dual-school scroll access.

### Boss Integration: Gaia Guardian Spell Trials
The Gaia Guardian fight gains two new phases when Iron's Botany is installed:
- **Phase 1 (>50% HP):** Environmental magic requirement — spells deal 50% less damage without active flower auras, but +50% with them.
- **Phase 2 (<50% HP):** School-specific counters — the Guardian resists Fire and Ice schools (70% resistance) but is **vulnerable to Botanical spells** (+50% damage).

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

Default conversion ratio: 1000 Botania mana = 1 ISS mana (configurable from 100 to 10,000). Bidirectional conversion available as an option.

---

## Discovery & Progression

- **12 Advancements** guide you through the mod's systems — from your first Botanical Focus through catalysts, flower auras, armor set bonuses, Corporea logistics, and casting in Alfheim.
- **Botanical Grimoire** (Patchouli guidebook) — Comprehensive in-game reference with 18 entries covering all spells, equipment, catalysts, flower auras, casting channels, mana systems, and advanced integration.
- **Loot Integration** — Mod items appear in vanilla structure chests: Spell Petals in villages, Botanical Crystals in mineshafts, the Grimoire in stronghold libraries, and Orbs of Terran Might in end cities (toggleable).

---

## Configuration

All settings are in `ironsbotany-common.toml`. 80+ options across 12 categories:

| Category | Key Options |
|----------|-------------|
| **Master Toggles** | Bare-bones mode, deep synergy master switch |
| **Mana System** | Unification mode, conversion ratio, bidirectional toggle, dual-cost, reservoir/conduit capacity |
| **Mana Conduit** | Distribution radius, conversion rate, buffer capacity |
| **Mana Pool Access** | Direct pool access toggle, search radius |
| **Spells** | School toggle, power multiplier, cooldown multiplier |
| **Equipment** | Per-weapon stat bonuses (power, mana, cost reduction, mana-per-hit) |
| **Spell Mechanics** | Per-spell power/cooldown multipliers, shield HP, root immobilization |
| **Balance** | Cross-loot, orb effectiveness, vanilla loot injection toggle |
| **Casting Channels** | Toggle, global power multiplier |
| **Spell Catalysts** | Toggle, consumption chance, durability damage, max stacking, power multiplier |
| **Flower Auras** | Toggle, range/strength multipliers, stacking limit, particles |
| **Deep Integration** | Mana events (duration/intensity/radius), Corporea (toggle/radius), Alfheim (boost/scrolls/attunement) |

---

## Requirements

**Required:**
- Minecraft Forge 1.20.1 (47.3.0+)
- Botania 1.20.1-441+
- Iron's Spells 'n Spellbooks 1.20.1-3.0.0+
- Curios API 5.0.0+

**Optional:**
- Patchouli (in-game guidebook: *Chronicle of the Green Mage*)
- JEI (recipe viewing for all 32 recipes)
