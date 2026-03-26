# Iron's Botany

**A Minecraft Forge 1.20.1 compatibility mod bridging Botania and Iron's Spells 'n Spellbooks.**

Iron's Botany creates deep mechanical synergy between Botania's natural mana systems and ISS's arcane spellcasting. It adds 9 Nature-school spells with dual mana costs, cross-mod equipment, a configurable mana unification system, and six stages of progressive integration that reward mastering both mods together.

## Features

### Mana Unification System

Five configurable modes control how Botania and ISS mana interact:

| Mode | Behavior |
|------|----------|
| **HYBRID** (default) | Both systems coexist with conversion at a configurable ratio (1000:1) |
| **BOTANIA_PRIMARY** | ISS spells consume Botania mana directly |
| **ISS_PRIMARY** | Botania items passively generate ISS mana |
| **SEPARATE** | No conversion — spells require both mana types (dual-cost) |
| **DISABLED** | No mana integration at all |

Key items:
- **Botanical Focus** — Curio that enables passive Botania-to-ISS mana conversion
- **Spell Reservoir** — Block that stores and distributes ISS mana to nearby players. Right-click with mana items to deposit. Comparator output.
- **Mana Conduit** — Block that converts Botania mana pool energy into ISS mana for nearby players. Feeds adjacent Reservoirs. Comparator output.

### Botanical Spell School

9 Nature-school spells with dual-cost mechanics (Botania + ISS mana):

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

### Equipment

**Weapons:**
- **Terrasteel Spell Blade** — +25% spell power, +200 max mana, -20% cooldown. Attacks generate Botania mana. Repairable with Terrasteel Ingots.
- **Livingwood Staff** — +10% Botanical spell power, stores 500k Botania mana
- **Dreamwood Scepter** — +20% Botanical spell power, converts ISS mana cost to Botania
- **Gaia Spirit Wand** — +30% Botanical spell power, -25% cooldowns

**Manasteel Wizard Armor (4-piece set):**
- +15% spell power and +150 max mana per piece
- Repairable with Manasteel Ingots
- Set Bonus: Mana Shield absorbs 50% damage using Botania mana

**Upgrade Orbs** (used in Arcane Anvil):
- Orb of Flora (+10% Nature spell power)
- Orb of the Pool (+100 max ISS mana)
- Orb of Bursting (+5% all spell power)
- Orb of Terran Might (+5% all spell power)

### Deep Synergy (6 Stages)

All stages are individually toggleable. Use `bareBonesMode` to disable everything except mana conversion, or `enableDeepSynergy` as a master switch.

1. **Spell Catalysts** — Botania runes and lenses in your inventory modify spell behavior. 9 catalysts: elemental runes (+damage, AoE splash, pierce, speed), Terrasteel (crit chance), Gaia Spirit (+50% damage).
2. **Casting Channels** — Livingwood Staff, Dreamwood Focus, and Terra Rod each provide unique casting profiles (speed, regen, burst damage).
3. **Flower Auras** — Nearby Botania flowers passively buff spellcasting. Bellethorne (thorns/retaliation), Jaded Amaranthus, Heisei Dream, Rannuncarpus (ritual automation).
4. **Spell-Triggered Mana Events** (Experimental, disabled by default) — Casting spells sends ripples through the Botania mana network, boosting nearby spreaders, pools, and flowers.
5. **Corporea Logistics** — High-tier spells (level 5+) auto-request reagents from Corporea networks.
6. **Alfheim Integration** — Spells gain power in the Alfheim dimension, scrolls gain dual-school properties, spellbooks gain attunement levels.

## Localization

Fully translated into 22 languages: Afrikaans, Arabic, Bengali, German, British English, Argentine Spanish, Spanish, Mexican Spanish, French, Hindi, Italian, Japanese, Korean, Dutch, Brazilian Portuguese, Russian, Turkish, Ukrainian, Vietnamese, Simplified Chinese, Traditional Chinese (HK), Traditional Chinese (TW).

## Dependencies

**Required:**
- Minecraft Forge 1.20.1 (47.3.0+)
- Botania 1.20.1-441+
- Iron's Spells 'n Spellbooks 1.20.1-3.0.0+
- Curios API 5.0.0+

**Optional:**
- Patchouli (in-game guidebook: *Chronicle of the Green Mage*)
- JEI (recipe viewing)

## Installation

1. Install Minecraft Forge for 1.20.1
2. Install all required dependencies
3. Place the Iron's Botany JAR in your `mods/` folder
4. Launch Minecraft

## Configuration

All settings are in the common config (`ironsbotany-common.toml`). 80+ options across 13 categories:

- **Master Toggles** — Bare-bones mode, deep synergy master switch
- **Mana System** — Unification mode, conversion ratio (100–10000), bidirectional conversion, dual-cost, reservoir capacity
- **Spells** — Power multiplier, cooldown multiplier, Botanical school toggle
- **Equipment** — Per-item stat bonuses
- **Balance** — Cross-loot, upgrade orb effectiveness
- **Casting Channels** — Toggle + power multiplier
- **Spell Catalysts** — Toggle, consumption chance, max stacking, power multiplier
- **Flower Auras** — Toggle, range/strength multipliers, stacking limit, particles
- **Mana Events** — Toggle (disabled by default), duration, intensity, radius
- **Corporea Logistics** — Toggle, auto-request, search radius
- **Alfheim Integration** — Dimension boost, dual-school scrolls, attunement

## Development

### Building from Source

```bash
git clone <repository-url>
cd ironsbotany
./gradlew build
```

The built JAR will be in `build/libs/`.

### Project Structure
```
src/main/java/com/ironsbotany/
├── IronsBotany.java          # Mod entry point
├── client/                    # Rendering, particles, HUD
└── common/
    ├── alfheim/               # Dimension integration
    ├── automation/            # Spell-driven Botania automation
    ├── block/                 # Spell Reservoir, Mana Conduit
    ├── boss/                  # Gaia Guardian spell trials
    ├── casting/               # Casting channel system
    ├── config/                # 70+ config options
    ├── corporea/              # Corporea logistics
    ├── entity/                # Projectiles and summons
    ├── event/                 # Armor set bonus
    ├── flower/                # Flower aura system
    ├── item/                  # Weapons, armor, curios, orbs
    ├── network/               # Client-server sync
    ├── progression/           # Advancement tracking
    ├── recipe/                # Rune Scroll Fusion
    ├── registry/              # Deferred registers
    ├── spell/                 # 9 spells + catalyst system
    └── util/                  # ManaHelper, BotaniaIntegration, DataKeys
```

## Credits

- **Botania** by Vazkii
- **Iron's Spells 'n Spellbooks** by Iron431
- **Curios API** by TheIllusiveC4

## License

MIT License
