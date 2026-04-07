# Iron's Botany — Forge 1.20.1 Mod

## Quick Reference
- **Mod ID**: `ironsbotany`
- **Package**: `com.ironsbotany`
- **Version**: 1.3.2
- **MC**: 1.20.1 | **Forge**: 47.4.16 | **Java**: 17
- **Mappings**: Official

## Build
- `./gradlew build` — full build
- `./gradlew compileJava` — compile-only

## Project Structure
- `client/` — rendering, particles, client logic
  - `particle/` — custom particle effects
  - `renderer/` — custom renderers
- `common/` — shared/server-side logic
  - `alfheim/` — Alfheim dimension integration
  - `automation/` — automation mechanics
  - `block/` + `block/entity/` — custom blocks and block entities
  - `boss/` — boss encounters
  - `casting/` + `casting/channels/` — spell casting and channel system
  - `config/` — mod configuration
- `src/generated/resources/` — datagen output
- `libs/` — local dependency JARs (compileOnly)

## Key Dependencies
- **Botania** 1.20.1-450+ (required, local JAR)
- **Iron's Spells 'n Spellbooks** 3.15.2+ (required, local JAR)
- **Curios** 5.14.1+ (required, local JAR)
- **Patchouli** 84+ (optional, local JAR)
- **Player Animation Lib** (optional, local JAR)

## Conventions
- Registration: DeferredRegister on MOD bus
- Dependencies via local JARs in `libs/` folder (compileOnly, not deobfed)
- Bridges Botania and Iron's Spellbooks systems
- License: MIT
