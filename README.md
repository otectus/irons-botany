# Iron's Botany

**A Minecraft Forge 1.20.1 compatibility mod bridging Botania and Iron's Spells 'n Spellbooks.**

Iron's Botany creates deep mechanical synergy between Botania's natural mana systems and ISS's arcane spellcasting. As of v1.6 it ships its own custom **Botany** spell school, the centralized `ManaBridgeManager` cost router, mana-network-aware blocks and items, endgame spellbooks crafted at the Terra Plate, three new curios, and graceful soft-integration with Ars 'n Spells when present.

## Features

### Mana Bridge Architecture (v1.5)

All spell cost routing flows through `ManaBridgeManager.resolveCost(player, spell, level, source)`, called at `EventPriority.LOW` on `SpellPreCastEvent`. The bridge:

- Handles all five `ManaUnificationMode` values from one entry point
- Uses a tick-scoped `CostRoutedTag` for idempotency across `SpellPreCastEvent` and `ChangeManaEvent`
- Defers cleanly to Ars 'n Spells when both bridges are active (no double-billing)
- Honors a config-driven `ManaPriorityChain` (`["botania", "ars", "iss"]` default)

Five configurable modes control how Botania and ISS mana interact:

| Mode | Behavior |
|------|----------|
| **HYBRID** (default) | Both systems coexist with conversion at a configurable ratio (1000:1) |
| **BOTANIA_PRIMARY** | ISS spells consume Botania mana directly |
| **ISS_PRIMARY** | Botania items passively generate ISS mana |
| **SEPARATE** | No conversion — spells require both mana types (dual-cost) |
| **DISABLED** | No mana integration at all |

### Botany Spell School (v1.5)

Iron's Botany registers its own `SchoolType` against `SchoolRegistry.SCHOOL_REGISTRY_KEY` — no longer piggybacking on Nature. Custom focus tag (`#ironsbotany:focus/botany`), attribute pair (`BOTANY_SPELL_POWER` / `BOTANY_MAGIC_RESIST`), and damage type. The 9 Botany spells:

| Spell | Max Level | Rarity | Description |
|-------|-----------|--------|-------------|
| Mana Bloom | 5 | Common | Summons Botania mystical flowers on nearby ground. |
| Botanical Burst | 8 | Common | Fires a botanical projectile that deals magic damage. |
| Flower Shield | 10 | Uncommon | Creates a petal barrier that absorbs damage. |
| Living Root Grasp | 6 | Common | Roots nearby enemies with Slowness and Weakness. |
| Spark Swarm | 7 | Rare | Summons mana sparks that orbit and attack enemies. |
| Runic Infusion | 10 | Rare | Strength + Speed + Regeneration; runes amplify the effect. |
| Petal Storm | 5 | Uncommon | Spiral storm of petals damaging nearby enemies. |
| Gaia's Wrath | 10 | Legendary | Massive AoE; Wither + Weakness + Slowness. |
| Mana Rebirth | 5 | Epic | Heals, removes negatives; can prevent death at high levels. |

Each spell exposes per-spell `botania_mana_cost` and `dual_cost_enabled` SpellConfigParameters — datapack-overridable at `data/<namespace>/spell_configs/<spell_id>.json`.

### Mana Network Citizenship (v1.5 + v1.6)

**Arcane Mana Altar** — block entity implementing `ManaPool`, `ManaReceiver`, and `SparkAttachable` simultaneously. 1,000,000 mana capacity. Drainable by nearby players during cast resolution via `ManaBridgeManager.tryDrainFromNearbyAltar`.

**Mana Conduit** — drains adjacent Botania pools and feeds nearby players' ISS mana directly.

**Spell Reservoir** — block that stores and distributes ISS mana to nearby players.

Iron's Botany items also attach a per-stack `MANA_ITEM` capability via `IBCapabilityHandler` — they appear in the Botania mana HUD, accept Spark deposits, and are drained by `ManaItemHandler.requestMana` alongside Mana Tablets:

| Item | Mana Capacity |
|------|---------------|
| Arcane Codex | 500,000 |
| Gaia Spirit Wand | 1,000,000 |
| Livingwood Staff | 500,000 (configurable) |
| Dreamwood Scepter | 250,000 |
| Terrasteel Spellbook | 200,000 |
| Mana Reservoir Ring | 200,000 |
| Manasteel Staff | 50,000 |
| Botanical Focus | 50,000 |
| Botanical Ring | 25,000 |

### Equipment

**Weapons:**
- **Terrasteel Spell Blade** — +25% spell power, +200 max mana, -20% cooldown. Attacks generate Botania mana.
- **Manasteel Staff** *(v1.6)* — extends ISS `StaffItem`. +20 Max Mana, +5% Cast Speed, +10% Botany Spell Power.
- **Livingwood Staff** — +10% Botanical spell power, stores 500k Botania mana.
- **Dreamwood Scepter** — +20% Botanical spell power, converts ISS mana cost to Botania.
- **Gaia Spirit Wand** — +30% Botanical spell power, -25% cooldowns.

**Spellbooks (v1.6):**
- **Terrasteel Spellbook** — 12 slots, Rare. +200 Max Mana, +15% Botany Spell Power, +10% Nature Spell Power.
- **Arcane Codex** — 14 slots, Epic. Crafted at the Terra Plate (500,000 mana). +300 Max Mana, +20% CDR, +15% Botany Spell Power.

**Scrolls (v1.6):**
- **Elementium Scroll** — reusable scroll, pulls cast cost from your Botania mana network. Falls back to single-use if mana is exhausted.

**Manasteel Wizard Armor (4-piece set):**
- +15% spell power and +150 max mana per piece
- Set Bonus: Mana Shield absorbs 50% damage using Botania mana

**Curios:**
- **Botanical Focus** — passive Botania-to-ISS mana conversion when held active.
- **Botanical Ring** — +25 max mana, +5% Nature spell power.
- **Mana Reservoir Ring** *(v1.6)* — +100 max mana; auto-converts Botania → ISS when ISS mana is low.
- **Daybloom Amulet** *(v1.6)* — +15% Nature Spell Power and +5% Cast Speed while in direct sunlight.
- **Gaia's Blessing** *(v1.6)* — Botany spells gain +1 effective level. Drains 100k mana from a nearby Mana Pool per cast.

**Upgrade Orbs** (used at the Arcane Anvil; crafted at the Runic Altar in v1.5):
- 4 original orbs: Flora, Pool, Bursting, Terran Might
- 8 ISS-school orbs: Fire / Frost / Lightning / Holy / Ender / Blood / Nature / Eldritch power

### Petal Apothecary, Runic Altar, Terra Plate, Alfheim Portal

Iron's Botany ships custom recipes for every major Botania crafting station:

- **Petal Apothecary**: 3 tiers of Mana Ink (Minor / Greater / Prime), Mana Reservoir Ring, Daybloom Amulet
- **Runic Altar**: 8 ISS-school spell-power orbs (Fire / Frost / Lightning / Holy / Ender / Blood / Nature / Eldritch)
- **Terra Plate**: Arcane Codex (500k mana), Gaia's Blessing (200k mana)
- **Alfheim Portal**: Manasteel→Elementium spellblade, Livingwood→Dreamwood scepter, Greater→Prime ink discount, Elementium Scroll promotion

### Gaia Guardian II Loot

Iron's Botany ships a Forge global loot modifier that adds Legendary Ink to the Gaia Guardian II hardmode loot table without overwriting Botania's JSON.

### Deep Synergy (6 Stages)

All stages are individually toggleable. Use `bareBonesMode` to disable everything except mana conversion, or `enableDeepSynergy` as a master switch.

1. **Spell Catalysts** — Botania runes and lenses in your inventory modify spell behavior. 9 catalysts: elemental runes, lens upgrades, Terrasteel crit, Gaia Spirit damage.
2. **Casting Channels** — Livingwood Staff, Dreamwood Focus, and Terra Rod each provide unique casting profiles.
3. **Flower Auras** — Nearby Botania flowers passively buff spellcasting. Bellethorne, Jaded Amaranthus, Heisei Dream, Rannuncarpus.
4. **Spell-Triggered Mana Events** *(Experimental, disabled by default)* — Casts ripple through the Botania mana network.
5. **Corporea Logistics** — High-tier spells auto-request reagents from Corporea networks.
6. **Alfheim Integration** — Spells gain power near Alfheim Portals; scrolls gain dual-school properties.

### Patchouli Documentation

Iron's Botany ships a 24+ entry Patchouli book (*Botanical Grimoire*) covering every system. v2.0+ entries are organized under categories: Getting Started, Spells, Equipment, Advanced Systems, Deep Synergy.

A 5-page Iron's Botany entry is also injected into the **Lexica Botania** itself — visible from inside Botania's own guidebook.

## Localization

Fully translated into 22+ languages.

## Dependencies

**Required:**
- Minecraft Forge 1.20.1 (47.4.16+)
- Botania 1.20.1-450+
- Iron's Spells 'n Spellbooks 1.20.1-3.15.2+
- Curios API 5.14.1+

**Optional:**
- Patchouli (in-game guidebook: *Botanical Grimoire*)
- Ars 'n Spells (auto-detected via reflection; cost routing yields cleanly when present)

## Installation

1. Install Minecraft Forge for 1.20.1
2. Install all required dependencies
3. Place the Iron's Botany JAR in your `mods/` folder
4. Launch Minecraft

## Server Commands

- `/irons_botany reload` *(perm 2)* — flushes runtime caches; pick up TOML / datapack changes without restart

## Configuration

All settings are in the common config (`ironsbotany-common.toml`). 90+ options across 14 categories:

- **Master Toggles** — Bare-bones mode, deep synergy master switch
- **Mana System** — Unification mode, conversion ratio, bidirectional conversion, dual-cost, reservoir capacity, **priority chain**
- **Spells** — Power multiplier, cooldown multiplier
- **Equipment** — Per-item stat bonuses
- **Balance** — Cross-loot, upgrade orb effectiveness
- **Casting Channels** — Toggle + power multiplier
- **Spell Catalysts** — Toggle, consumption chance, stacking, power
- **Flower Auras** — Toggle, range/strength, stacking, particles
- **Mana Events** — Toggle (disabled by default), duration, intensity, radius
- **Corporea Logistics** — Toggle, auto-request, radius
- **Alfheim Integration** — Boost, dual-school scrolls, attunement

## Development

### Building from Source

```bash
git clone <repository-url>
cd ironsbotany
./gradlew build
```

The built JAR will be in `build/libs/`.

### Datagen

```bash
./gradlew runData
```

Outputs to `src/generated/resources/`. Hand-written JSONs under `src/main/resources/`
take precedence; Botania custom-recipe JSONs (petal_apothecary, runic_altar, terra_plate,
elven_trade) and Patchouli book entries are intentionally hand-written.

### Project Structure

```
src/main/java/com/ironsbotany/
├── IronsBotany.java                # Mod entry point
├── api/                            # Public API (stable across minor versions)
│   ├── IronsBotanyApi              #   — bridge entry point
│   ├── IManaSource                 #   — cross-bridge SPI
│   └── BotanySchoolFlowerRegistry  #   — flower registration surface
├── datagen/                        # Recipe / model / lang / loot providers
└── common/
    ├── alfheim/                    # Dimension proximity boost
    ├── automation/                 # Spell-driven Botania automation
    ├── block/ + block/entity/      # Mana Conduit, Spell Reservoir, Arcane Mana Altar
    ├── boss/                       # Gaia Guardian spell trials
    ├── bridge/                     # ManaBridgeManager, CostRoutedTag, ManaPriorityChain
    ├── casting/                    # Casting channel system
    ├── command/                    # /irons_botany reload
    ├── compat/                     # ArsNSpellsCompat reflective probe
    ├── config/                     # 90+ config options
    ├── corporea/                   # Corporea logistics
    ├── entity/                     # Projectiles and summons
    ├── event/                      # SpellEventHandlers, IBCapabilityHandler, CurioEffectsHandler
    ├── flower/                     # Flower aura system
    ├── item/ + item/cap/           # Weapons, armor, curios, orbs, mana capability provider
    ├── loot/                       # AddItemLootModifier (Gaia Guardian drops)
    ├── network/                    # Client-server sync
    ├── progression/                # Advancement tracking
    ├── recipe/                     # Rune Scroll Fusion
    ├── registry/                   # Deferred registers (incl. IBSchools, IBDamageTypes)
    ├── spell/ + spell/config/      # 9 Botany spells + per-spell SpellConfigParameters
    └── util/                       # ManaHelper, BotaniaIntegration, DataKeys
```

## Roadmap

- **v1.5** *(released)* — Bridge architecture, Botany SchoolType, Arcane Mana Altar, recipe content
- **v1.6** *(in development)* — Manasteel Staff, Terrasteel Spellbook, Arcane Codex, Elementium Scroll, 3 new curios, datagen revival
- **v2.0** *(planned)* — see [`PHASE_7_PLAN.md`](PHASE_7_PLAN.md): six ISS-school generating flowers, the Verdant Caster (functional flower casts ISS spells), Corporea Scroll Rack, KubeJS surface

## Credits

- **Botania** by Vazkii
- **Iron's Spells 'n Spellbooks** by Iron431
- **Curios API** by TheIllusiveC4

## License

MIT License
