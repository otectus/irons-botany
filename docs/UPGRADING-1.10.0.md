# Upgrading to Iron's Botany 1.10.0

**Back up your world before upgrading.** This release migrates item NBT and repairs a config file
written by Iron's Spells. Both are designed to be safe and idempotent, and both are covered by
tests — but a backup costs nothing and a corrupted save costs everything.

Upgrading from **1.7.x** or **1.9.0** is supported. Downgrading afterwards is not required to keep
playing, but is not tested.

---

## Why you should upgrade from 1.9.0

1.9.0 crashes when ISS-Scroll Descriptions or ISS-Restrictions inspect any Iron's Botany spell.
The Botany school's display name carried no colour, and Iron's Spells' `SchoolType#getTargetingColor()`
reads that colour without checking for null. Iron's Spells' own code calls it too, so the crash was
not limited to those add-ons. Spell icons appearing "missing" was the same bug: the tooltip aborted
before it drew them. The icons themselves were always present and valid.

If you downgraded to 1.7.0 because of 1.9.0, this release is the one to come back on.

---

## What happens on first launch

### Your items

On your next login, Iron's Botany migrates school identifiers in items you are carrying — your
inventory, armour, offhand, ender chest, and Curios slots. It records a schema version so it never
runs twice, and it is written so that running it twice would be harmless anyway.

It rewrites only fields this mod owns:

* `ironsbotany:botanical` (the pre-1.6 school id) becomes `ironsbotany:botany` wherever it appears.
* `irons_spellbooks:nature` becomes `ironsbotany:botany` **only** in this mod's own dual-school NBT
  fields, and **only** on an item whose spell id is in this mod's namespace.

**Your genuine Iron's Spells Nature scrolls, focuses and equipment are not touched.** There is a
test that asserts a real Nature scroll comes out of the migration bit-for-bit identical.

Items sitting in chests around your world are migrated the next time they pass through a player's
hands rather than by a whole-world rewrite, which is the kind of bulk operation that corrupts saves.

### Your Iron's Spells spell config

Through 1.9.0 every Iron's Botany spell declared its school as `irons_spellbooks:nature` in the
config file Iron's Spells generates, while the code reported `ironsbotany:botany` — so the file and
the game disagreed, and editing the school in that file did nothing.

Iron's Spells never rewrites a spell-config file that already exists, so 1.10.0 repairs it: on
server start it rewrites the `"school"` value in
`config/irons_spellbooks/spells/ironsbotany/*.json`, and only there. It touches no other namespace,
no other key, and only values equal to the legacy string this mod itself wrote. If you deliberately
set some other school, your choice is kept. Formatting, key order and comments survive. Every file
changed is named in the log.

Run `/irons_botany diagnose` afterwards to confirm every spell reports canonical Botany metadata.

---

## Changes you will notice

### Casting actually works with one mana item

Botania payment used `ManaItemHandler.requestMana(stack, …)` as if it were a per-item query. It is
not — Botania excludes the stack you pass it and searches everything else. With exactly one mana
item every check returned zero, so Botania could never pay. With three or more it over-counted, let
an unaffordable cast through, drained part of your mana and then refused the cast anyway.

Both are fixed. A failed, cancelled or interrupted cast now conserves everything.

### Bonuses that never worked now work

* **Botany spell power and resistance** were registered but attached to no entity, so every Botany
  bonus on every ring, staff, amulet and armour set silently did nothing. They are attached to
  players now.
* **Mana efficiency** was defined with a maximum equal to its default, so every item's positive
  modifier was clamped away — and nothing read it anyway. Rebased and consumed by the cost path.
  Your items' existing numbers now mean what their tooltips always claimed.
* **Rune-enhanced scrolls** cost less Botania mana, scaled by the rune you fused in. The recipe
  previously consumed a rune for a renamed scroll with no mechanical effect.

### Upgrade orbs apply the right bonus

The eight school orbs shared four orb types, so each applied another orb's attribute *and* handed
back another orb when consumed — Orb of Fire Power gave ISS Nature power and turned into an Orb of
Flora. Each orb now has its own type, attribute and container item.

**Orb of Flora now boosts Botany, not ISS Nature.** If you were using it to power Iron's Spells
Nature spells, use the Orb of Nature Power instead.

### Equipment schools

Livingwood Staff, Dreamwood Scepter and Daybloom Amulet granted ISS Nature power only, which did
nothing for the spells this mod ships. They now grant **both** Botany and Nature at the same value,
so they work with this mod's spells without downgrading an existing Nature build. A spell reads one
school's power attribute, so this is not a stacking buff.

Botanical Ring's tooltip said "Nature Spell Power"; it always granted global spell power. The text
now matches.

### Command rename

`/irons_botany reload` is now **`/irons_botany flushcaches`**. It never reloaded configuration — it
cleared the flower-aura cache — and the name led operators to think their config edits had been
applied. New: `/irons_botany diagnose` reports school metadata for every registered spell.

### Config

* New: `runeScrollManaDiscount`.
* Twelve settings that never did anything are now labelled
  `[NOT IMPLEMENTED in 1.10.0 - this setting has no effect]` in the generated config, with a pointer
  to `docs/SYSTEM-DECISIONS-1.10.0.md`. They are kept so existing configs load unchanged.

---

## Known limitations in this release

Listed honestly rather than implied fixed. See `docs/SYSTEM-DECISIONS-1.10.0.md` for the full record.

1. **The reported purple/black "magic texture" has no confirmed root cause and was not reproduced.**
   Every texture in the released JAR decodes and every reference resolves inside the archive; the
   build now fails if that ever stops being true. If you see it, please attach `latest.log`, your
   resource pack list, and whether you are running a downloaded or self-built JAR.
2. **Dual-school scroll NBT** is written but has no cast-time effect.
3. **`SpellDrivenAutomation`, the spell-triggered mana-network events, and the progression unlock
   flags** are inert and default to off.
4. **22 language files fall back to English for 87 keys each.** Honest fallback was preferred to
   unreviewed machine translation. The Orb of Flora's old translation was removed rather than
   renamed, because its meaning changed.
5. **This release was verified by automated tests, a clean build, and a final-artifact gate.** It
   has not been exercised in a live client or dedicated server with the scroll add-ons installed —
   see the handoff report for exactly what was and was not run.
