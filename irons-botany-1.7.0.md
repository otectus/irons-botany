# Iron's Botany Technical Research Audit and Execution Blueprint

## Executive Summary

Iron's Botany is not a throwaway prototype. It has a real Forge 1.20.1 mod structure, broad content coverage, a meaningful amount of client/server separation work, custom networking, a Patchouli book, multilingual resources, custom spells, gear, block entities, Curios slot tags, and several recent fixes specifically aimed at dedicated-server safety and cross-mod compatibility. The problem is not lack of ambition. The problem is that the ambition is wider than the current wiring. Several “deep synergy” systems are present in code or documentation, but parts of that stack are still only partially operational, overly hardcoded, or insufficiently data-driven for long-term modpack use. fileciteturn14file0L3-L3 fileciteturn108file0L3-L3 fileciteturn109file0L3-L3

The strongest current direction is **not** a rewrite. The right move is a disciplined stabilization-and-hardening pass, followed by a targeted integration redesign that makes Botania-native progression and Iron’s Spells-native progression meet in the middle instead of stapling one system on top of the other. In plain English: keep the solid scaffolding, kill the dead wiring, stop pretending vanilla crafting is enough for late-game Botania integration, and move more of the cross-mod logic into configs, tags, recipes, and constrained compat modules. fileciteturn111file0L3-L3 fileciteturn13file0L3-L3 fileciteturn117file0L3-L3

The biggest risks are practical, not aesthetic: Botania mana aggregation is currently incorrect for multi-item mana sources; some advertised systems appear structurally present but effectively unhooked or unused; late-game progression is mostly hand-authored and vanilla-recipe-heavy instead of Botania-native; and the repository has no real datagen pipeline despite a broad resource surface area. The biggest opportunities are equally practical: standardize on the ISS Nature school, push progression through Petal Apothecary / Runic Altar / mana-pool style gates, convert hardcoded compat content into tags and datapack definitions, and turn the current feature sprawl into a modpack-friendly system with clear balance rails. fileciteturn159file0L3-L3 fileciteturn133file0L3-L3 fileciteturn137file0L3-L3 fileciteturn111file0L3-L3 fileciteturn163file0L1-L3

My recommended development direction is blunt: **Phase one should focus on correctness and feature truthfulness, phase two on architecture, phase three on Botania-native and ISS-native progression, and only then should content expansion resume.** If you skip that order, you will just build fancier bugs with flowers on them. fileciteturn109file0L3-L3 fileciteturn111file0L3-L3

## Confirmed Environment and Current Architecture

Iron’s Botany is built as a Forge mod for Minecraft 1.20.1 using ForgeGradle 6, official Mojang mappings for 1.20.1, and Java 17. The build pulls Forge 47.4.16 and compiles against local jars for Botania 1.20.1-450, Iron’s Spells ’n Spellbooks 1.20.1-3.15.2, Curios 5.14.1, Patchouli 84.1, and player-animation-lib 1.0.2-rc1. The mod metadata declares Botania, Iron’s Spells, and Curios as mandatory dependencies, with Patchouli, JEI, and Ars ’n’ Spells marked optional-after. That dependency posture is technically correct, because the common code imports Botania, ISS, and Curios types directly in many places; soft-optionalizing those three without a deeper module split would be fantasy cosplay, not engineering. fileciteturn111file0L3-L3 fileciteturn13file0L3-L3 fileciteturn160file0L3-L3 fileciteturn152file0L3-L3

The resource pack is configured for the 1.20.1-era formats, and the repository includes `src/generated/resources`, but the actual `runData` configuration is commented out with a TODO. Forge’s own docs explicitly expect data generation to be driven by `GatherDataEvent` and the `runData` task, and the current repository does not implement that pipeline. That matters because the mod already has a broad surface area of recipes, advancements, tags, sounds, models, and multilingual lang files. Hand-authoring all of that forever is how you end up debugging JSON drift at 2 a.m. like it’s some kind of lifestyle brand. fileciteturn127file0L3-L3 fileciteturn111file0L3-L3 citeturn7view1turn5view0

Architecturally, the mod entrypoint registers items, blocks, block entities, entities, particles, recipe types, creative tabs, custom attributes, spells, sounds, and configs on the mod event bus, and then uses common setup to validate config plus register spell catalysts and flower auras. Client setup is isolated into a client-only subscriber that registers entity renderers, block entity renderers, and particle providers. Networking uses a `SimpleChannel` with the modern client-handler indirection pattern that Forge recommends for server-to-client packets, which is exactly the right fix after previous dedicated-server crashes. That part is healthy. fileciteturn14file0L3-L3 fileciteturn151file0L3-L3 fileciteturn28file0L3-L3 fileciteturn29file0L3-L3 fileciteturn109file0L3-L3 citeturn8view0turn6view0

The current implementation is mostly organized into sensible verticals: `common/spell`, `common/item`, `common/block`, `common/event`, `common/casting`, `common/flower`, `common/corporea`, `common/alfheim`, `common/progression`, `common/util`, and `client`. That looks good on paper. The real issue is that some of those modules are full system verticals with only partial operational linkage. In other words, the package tree looks like a full orchestra, but some chairs still have cardboard cutouts instead of musicians. fileciteturn108file0L3-L3 fileciteturn14file0L3-L3

From the external-mod side, Iron’s Spells currently advertises more than 100 upgradable spells, boss content, structures, armor and upgrade systems, and extensive per-spell configuration, with official developer documentation linked from its project page. Botania describes itself as a natural-magic-themed tech mod with strong in-world automation, no-GUI/no-wire design philosophy, mana pools, spreaders, sparks, functional/generating flora, corporea logistics, trinket sharing through Curios, and in-world progression via Petal Apothecary, Runic Altar, mana infusions, elven content, and related systems. Curios itself is explicitly tag-driven, creates slots on demand, merges identical identifiers for compatibility, and expects item-to-slot relations to be defined through `data/curios/tags`. Those external design constraints matter a lot: they strongly favor data-driven slot tagging, in-world progression gates, and limited, readable power synergies over giant opaque stat piles. citeturn17view0turn21view0turn11view0turn23view2turn23view3turn22view0

## Findings

### Critical

**Aggregated Botania mana checks are functionally wrong for split mana storage**

**Affected files/classes:** `ManaHelper.java`, and every system that calls `hasBotaniaMana` or `drainBotaniaMana`, including Dreamwood conversion, armor damage absorption, Mana Rebirth, and spell dual-cost logic.  
**Description:** `requestManaFromAllSources` iterates through mana items and accessories one stack at a time and returns success only if a single stack can satisfy the full requested amount. It does not aggregate across multiple mana-holding items even though the HUD explicitly sums all carried/equipped mana. This creates false failures for players whose mana is split across tablets, rings, bands, or other containers.  
**Evidence:** the helper loops over `getManaItems` and `getManaAccesories`, calls `requestManaExact(stack, player, amount, doExtract)`, and returns immediately on the first single-stack success; otherwise it falls through to `false`. The client HUD, by contrast, sums total mana and max mana across all sources. Botania’s own item model expects mana items and accessories to work as a broader ecosystem, not as isolated silos. fileciteturn159file0L3-L3 fileciteturn149file0L3-L3 citeturn21view0turn22view0  
**Why it matters:** this is player-facing breakage. Spells, damage shields, or mana conversions will randomly fail under legitimate equipment setups. That is the kind of bug players interpret as “this mod is flaky,” and they will not be wrong.  
**Recommended fix:** replace the single-stack exact request loop with a two-pass aggregated mana transaction helper: first simulate total available mana across all valid sources, then drain incrementally over multiple sources until the exact cost is met. If Botania 1.20.1 exposes a better aggregate helper in your compiled API, prefer that; otherwise implement a deterministic multi-source drain utility in `ManaHelper` and route every Botania mana payment through it.  
**Risk level:** Critical.  
**Estimated implementation difficulty:** Medium.

**Casting channels are structurally present but appear unregistered**

**Affected files/classes:** `CastingChannelRegistry.java`, all channel implementations under `common/casting/channels`, `IronsBotany.java`, `AbstractBotanicalSpell.java`.  
**Description:** the registry exists, item-channel registration helpers exist, and the changelog claims channels are wired into spell execution, but I found no evidence of actual channel registration in common setup or elsewhere. A repository search for `registerItemChannel(` returned only the registry itself, which strongly suggests the feature is not actually initialized at runtime.  
**Evidence:** common setup currently performs config validation plus catalyst and aura registration, but not channel registration. The registry class only stores channels; it does not self-populate. The connector search for `registerItemChannel(` surfaced only `CastingChannelRegistry.java`. The README still advertises casting-channel profiles as a core deep-synergy stage. fileciteturn14file0L3-L3 fileciteturn133file0L3-L3 fileciteturn134file0L1-L3 fileciteturn108file0L3-L3  
**Why it matters:** advertised gear-channel behavior that never actually initializes is worse than a missing feature because it quietly poisons balance assumptions, QA effort, and player trust.  
**Recommended fix:** add a dedicated `CastingChannelRegistration` bootstrap called from common setup, register every channel explicitly, and add a startup log summary with channel count and item bindings. Then write at least one GameTest or runtime assertion that verifies the expected items return non-null channels.  
**Risk level:** Critical.  
**Estimated implementation difficulty:** Low to Medium.

**Cross-mod progression unlock flags appear to be dead state**

**Affected files/classes:** `UnifiedAdvancementSystem.java`, `DataKeys.java`, any future progression consumers.  
**Description:** the advancement handler writes `TIER4_UNLOCKED`, `DUAL_SCHOOL_UNLOCKED`, and `OVERCHARGE_UNLOCKED` flags into player persistent data, but the exposed query methods only appear in that same class. I did not find evidence that the rest of the mod actually consumes these unlocks to gate any mechanics.  
**Evidence:** `UnifiedAdvancementSystem` contains the fields, the award logic, and the getters; a repo search for the getter names surfaced only that file. The flags exist in `DataKeys` as well. That means the progression system is currently closer to an announcement machine than a gameplay gate. fileciteturn137file0L3-L3 fileciteturn161file0L3-L3 fileciteturn138file0L1-L3  
**Why it matters:** this is dead complexity. It makes the codebase look more advanced than it functionally is, confuses future contributors, and blocks clean balancing because “unlocked” stops meaning anything.  
**Recommended fix:** either wire those unlocks into real mechanics immediately or delete the system until those mechanics exist. My recommendation is to keep the concept but repurpose it into datapack-driven capability gates for a very small number of truly late-game features, not as a general-purpose NBT graveyard.  
**Risk level:** Critical.  
**Estimated implementation difficulty:** Medium.

### High Priority

**The mod is still too vanilla-crafting-heavy for a Botania integration mod**

**Affected files/classes:** recipe JSONs under `data/ironsbotany/recipes`, `RuneScrollFusionRecipe.java`, progression design broadly.  
**Description:** the repository clearly has many hand-authored recipes and one custom crafting recipe, but I did not find Botania-native recipe content such as Petal Apothecary, Runic Altar, mana infusion/alchemy, or elven trade recipe assets during the sampled repository scan. That means a large share of the integration progression is still delivered through ordinary crafting JSONs rather than Botania’s in-world systems.  
**Evidence:** repository search surfaced shaped/shapeless recipes such as equipment, materials, blocks, and orb recipes, plus the custom `rune_scroll_fusion` crafting recipe. Botania’s own progression model centers Petal Apothecary, Runic Altar, Mana Pool transformations, sparks, corporea, and Alfheim/elven content. fileciteturn117file0L3-L3 fileciteturn118file0L3-L3 fileciteturn163file0L1-L3 citeturn11view0turn13view1turn23view2turn23view3  
**Why it matters:** Botania is designed around in-world, visible, automatable progression. If Iron’s Botany keeps handing out late-game cross-mod toys through normal crafting tables, it will feel like a costume pack, not a bridge mod.  
**Recommended fix:** move early/mid/late tiers into Botania-native gates: Petal Apothecary for floral components and entry curios, Runic Altar for mid-tier spell hardware, Mana Pool transformations for refined materials, and Elven/Alfheim gates for the true endgame. Keep vanilla crafting for support materials and convenience, not for the main identity pieces.  
**Risk level:** High.  
**Estimated implementation difficulty:** Medium.

**No real datagen pipeline despite a large and drift-prone resource surface**

**Affected files/classes:** `build.gradle`, all recipe/tag/advancement/lang/model/sound resources.  
**Description:** `runData` is commented out, no `GatherDataEvent` providers are present, yet the project already carries a wide hand-authored content footprint: many recipe JSONs, Curios tags, Patchouli content, sounds, advancements, and more than twenty language files. Forge explicitly supports generating these categories, and the current repo does not use that support.  
**Evidence:** `build.gradle` comments out the datagen run configuration with a TODO, while searches show many recipe files and many language files. Forge’s docs explicitly describe `runData`, `GatherDataEvent`, and provider classes for recipes, tags, advancements, language files, sounds, blockstates, and models. fileciteturn111file0L3-L3 fileciteturn146file0L1-L3 fileciteturn163file0L1-L3 citeturn7view1  
**Why it matters:** this is how content drift creeps in. One new item becomes five JSON edits, four lang edits, two advancement changes, a tag tweak, and some future “why is JEI wrong again” nonsense.  
**Recommended fix:** introduce datagen in phase two for recipes, tags, advancements, sounds, blockstates/models, and at minimum the authoritative `en_us` lang file. Keep hand-authored only the things that are genuinely bespoke, such as Patchouli prose and select shaped recipes if you prefer readability.  
**Risk level:** High.  
**Estimated implementation difficulty:** Medium.

**Botania mana pool access is correct in concept but too scan-heavy and too generic**

**Affected files/classes:** `ManaHelper.java`, any spell or item that calls nearby-pool access.  
**Description:** nearby mana pools are discovered by scanning a 3D box around the player every time the helper checks or drains pool mana. That is acceptable at small scale, but it is exactly the kind of “works in dev, gets gross in modpacks” pattern that piles up under frequent casts, repeated hurt events, or multiplayer.  
**Evidence:** `findAndDrainPool` scans `BlockPos.betweenClosed` for the full configured radius box and checks each block entity for `instanceof ManaPool`. Botania does support mana pools as a real system, but its design philosophy is strongly in-world and automation-oriented, not “spray a cube scan every time a stat operation happens.” fileciteturn159file0L3-L3 citeturn21view0turn23view2  
**Why it matters:** performance bugs in magic mods almost never come from one catastrophic line; they come from a thousand small scans hiding inside cool-sounding features.  
**Recommended fix:** introduce a short-lived per-player cache of nearby valid pools refreshed on interval or on movement thresholds, or better, define an explicit “bound pool” mechanic for equipment that wants remote pool access. That also improves balance because not every cast magically reaches any pool in a radius.  
**Risk level:** High.  
**Estimated implementation difficulty:** Medium.

**Advanced systems lean too hard on hardcoded IDs and fragile assumptions**

**Affected files/classes:** `UnifiedAdvancementSystem.java`, likely parts of catalyst/aura registration, optional compat shims, and any class keyed to literal resource locations or specific upstream assets.  
**Description:** the repo already contains direct references to upstream advancement IDs and cross-mod literals. That is unavoidable in moderation, but when deep-synergy systems are based mostly on hardcoded registry names and assumed paths, every upstream rename becomes a silent regression.  
**Evidence:** `UnifiedAdvancementSystem` hardcodes Botania and ISS advancement resource locations. The changelog documents prior bugs caused by stale Botania identifiers and later rewrites to hardcoded registry lookups instead of reflection. The README advertises systems broad enough that hardcoded one-offs will not scale cleanly. fileciteturn137file0L3-L3 fileciteturn109file0L3-L3 fileciteturn108file0L3-L3  
**Why it matters:** hardcoding is fine for a small bridge. It becomes a maintenance bomb when the bridge starts pretending to be an airport.  
**Recommended fix:** move as much cross-mod mapping as possible into tags, datapack entries, or narrow registries owned by Iron’s Botany. Literal upstream IDs should remain only at the boundary layer and should be validated loudly at startup.  
**Risk level:** High.  
**Estimated implementation difficulty:** Medium.

### Medium Priority

**The current attribute model should be simplified around ISS-native stats**

**Affected files/classes:** `IBAttributes.java`, gear item classes, spell calculation flow.  
**Description:** the mod still carries custom “botanical” attributes while also integrating directly with ISS spell systems and using the ISS Nature school. Unless every custom attribute is meaningfully consumed in spell math, those custom attributes are just parallel bookkeeping.  
**Evidence:** the repo registers custom attributes, but the mod also migrated from a custom school to `irons_spellbooks:nature`, and several integrations already rely on ISS-native mana and spell APIs. The current design goal should be tighter alignment with ISS-native `MAX_MANA`, school power, cooldown, and mana-related attributes instead of growing a shadow stat sheet. fileciteturn21file0L3-L3 fileciteturn152file0L3-L3 fileciteturn109file0L3-L3  
**Why it matters:** extra attributes are not free. They complicate balancing, Curios interactions, item tooltip clarity, and addon compatibility.  
**Recommended fix:** audit every custom attribute. If it is not consumed in an intelligible way by spell execution or item logic, retire it. Prefer ISS-native Nature, all-spell power, max mana, mana regen, and cooldown modifiers wherever possible.  
**Risk level:** Medium.  
**Estimated implementation difficulty:** Medium.

**Loot injection should become data-driven and modpack-overridable**

**Affected files/classes:** `LootTableInjector.java`.  
**Description:** the current loot injection works, but it is code-based and table-name-specific. Forge already treats loot as server data, and its datagen stack includes providers for loot-related content. For a modpack-facing integration mod, code-driven injection should be the exception, not the default.  
**Evidence:** `LootTableInjector` adds pools directly to selected vanilla chests at load time. Forge’s documentation frames loot tables and related data as server-data content and supports generation/provider workflows for them. fileciteturn158file0L3-L3 citeturn7view1turn5view0  
**Why it matters:** modpack authors want to override loot with datapacks, not crack open Java every time they dislike your economy. Fair, honestly.  
**Recommended fix:** migrate to Global Loot Modifiers or explicitly generated JSON-based content with conditions, and keep config toggles only as coarse enable/disable switches.  
**Risk level:** Medium.  
**Estimated implementation difficulty:** Low to Medium.

**Client HUD logic is serviceable but brute-force**

**Affected files/classes:** `ClientEventHandler.java`.  
**Description:** the Botania mana HUD sums mana across items and accessories correctly, but its block-proximity pulse rescans a 13×13×13 cube every second. That is not catastrophic, yet it is also not elegant, especially as client overlays stack with particle-heavy spell combat.  
**Evidence:** the client handler rescans nearby blocks every 20 ticks and checks every block entity in range for an active reservoir or conduit. Forge documentation emphasizes side correctness and packet discipline; while this code is client-safe, it is still a brute-force query that can be trimmed. fileciteturn149file0L3-L3 citeturn6view0  
**Why it matters:** overlays become performance death by a thousand papercuts faster than people expect.  
**Recommended fix:** replace the cube walk with cached nearby active positions synced from block entities, or at minimum switch to a cheaper distance-checked list from chunk-local block entities if available.  
**Risk level:** Medium.  
**Estimated implementation difficulty:** Low.

### Low Priority

**A few systems still have documentation-to-implementation trust issues**

**Affected files/classes:** `README.md`, `CHANGELOG.md`, feature-facing docs broadly.  
**Description:** the documentation is rich, but because some systems are only partially operational, the docs currently oversell maturity in a few areas.  
**Evidence:** the README still markets deep-synergy stages as fully legible player features, while the repo also contains unregistered systems and dead progression flags. fileciteturn108file0L3-L3 fileciteturn133file0L3-L3 fileciteturn137file0L3-L3  
**Why it matters:** inaccurate docs waste tester time and make balance feedback noisy.  
**Recommended fix:** once phase one lands, rewrite the feature matrix around what is actually shipped, experimental, or disabled-by-default.  
**Risk level:** Low.  
**Estimated implementation difficulty:** Low.

**Minor key/constant hygiene still needs cleanup**

**Affected files/classes:** `ArmorSetBonusHandler.java`, `DataKeys.java`, similar persistent-data users.  
**Description:** most NBT keys were centralized, but not all of them.  
**Evidence:** `ArmorSetBonusHandler` still defines its cooldown key inline instead of using `DataKeys`. fileciteturn155file0L3-L3 fileciteturn161file0L3-L3  
**Why it matters:** not a big deal, but inconsistency breeds typo bugs over time.  
**Recommended fix:** finish the centralization pass and add a naming convention comment so future contributors stop improvising.  
**Risk level:** Low.  
**Estimated implementation difficulty:** Low.

## Integration, Optimization, and Config Strategy

### Iron’s Spells Integration Plan

The correct long-term move is to **double down on ISS-native mechanics, not re-invent them**. Iron’s Botany already compiles against `SpellRegistry`, `MagicData`, `SpellPreCastEvent`, `AttributeRegistry`, `UpgradeOrbItem`, and `UpgradeOrbTypeRegistry`, and it already migrated away from a custom spell school toward ISS’s built-in Nature school. Keep that trajectory. Do **not** resurrect a separate Botanical school unless there is a truly unavoidable API constraint, because that would immediately fracture balance, scroll compatibility, spellbook discoverability, and addon interoperability. fileciteturn153file0L3-L3 fileciteturn141file0L3-L3 fileciteturn152file0L3-L3 citeturn17view0

What should be added is a **central spell integration service** that owns dual-cost mana payment, catalyst application, aura modifiers, and optional pool access. Right now too much of that logic is spread across item handlers, spell classes, and event handlers. Build one authoritative pipeline for “ISS cast of an Iron’s Botany nature spell.” The pipeline should verify the final spell/cooldown state, resolve any Dreamwood substitution, calculate added Botania mana cost, check aggregated Botania supply correctly, then commit payment and side effects. The Dreamwood scepter hook is pointing in the right direction already because it listens to `SpellPreCastEvent` and tries to pre-fund mana safely; that pattern should become the canonical path instead of a special-case trick. fileciteturn153file0L3-L3 fileciteturn159file0L3-L3

What should be changed is the balance model. Iron’s Spells is already a huge spell mod with upgradable spells, armor, structures, and per-spell config. Iron’s Botany should not try to “win” by universally replacing ISS mana with Botania mana or by adding a best-in-slot botanical item for every situation. Instead, keep the bridge **narrow and meaningful**: let Botania amplify Nature-school playstyles, extend endurance, enable specific augmentation paths, and open late-game alternatives. If Botania becomes a generic battery pack for every ISS build, you have not built synergy; you have built an exploit with leaves on it. citeturn17view0turn21view0

What should be avoided is a blanket “Botania mana can pay all ISS spell costs all the time” model. That would violate both mods’ identities. Botania’s design emphasizes in-world mana systems and engineered setups, while ISS centers on spell progression, discovery, and spellcasting identity. A good bridge should reward players who invest in both systems, not erase the need to care about either. citeturn21view0turn11view0turn17view0

The highest-value ISS-side implementation tasks are these. First, collapse unused custom attributes into ISS-native ones wherever possible. Second, make spell tuning data-driven: Botania mana cost surcharge, Nature school scaling multipliers, aura/catalyst caps, and optional pool-access rules should live in server config and datapack content, not in constructors. Third, verify that upgrade orb types are registered in the exact ISS 3.15.2-compatible way; if that requires datapack or registry data beyond item construction, generate it. Fourth, ensure scroll-related integrations stay inside ISS expectations: a rune-enhanced scroll path is good, but wild custom scroll semantics that bypass ISS progression are not. fileciteturn21file0L3-L3 fileciteturn141file0L3-L3 fileciteturn117file0L3-L3 fileciteturn119file0L3-L3 citeturn17view0

Testing on the ISS side should explicitly cover spellbook visibility, scroll compatibility, pre-cast cancellation, mana refund edge cases, upgrade orb availability in the Arcane Anvil, Curios attribute propagation, and multiplayer synchronization of every spell-side effect. The packet layer is now aligned with Forge’s recommended client-handler split, so keep that pattern for any future client visual sync work. fileciteturn29file0L3-L3 fileciteturn109file0L3-L3 citeturn8view0

### Botania Integration Plan

Iron’s Botany is currently missing the single biggest thing a Botania bridge mod needs: **Botania-style progression grammar**. Botania is explicitly built around in-world, automatable, visible systems such as the Petal Apothecary, Runic Altar, Mana Pool transformations, Mana Spreaders, Sparks, Corporea networks, and Curios-style trinkets. Botania’s own docs are extremely clear that mana is supposed to move through world logic, not just disappear into menu math. That means your current center of gravity should shift away from “here is another item with stats” and toward “here is a device, ritual, flower, or pool interaction that meaningfully affects spellcasting.” citeturn11view0turn23view2turn23view3turn22view0

The most important Botania-side additions are recipe-path changes. Entry-tier floral components and the focus/ring tier belong in Petal Apothecary routes. Mid-tier spellcraft components and selected catalysts belong in the Runic Altar. Refined botanical magic materials should use Mana Pool transformations or catalyst-backed pool recipes where appropriate. Dreamwood and Gaia-tier content should be gated by Botania’s higher progression, ideally using Elven trade or equivalent late-stage Botania ritual paths. That change alone would make the mod feel ten times more “Botania” and ten times less “random compatibility addon with extra crafts.” fileciteturn163file0L1-L3 citeturn13view1turn23view2turn13view5

What should be changed in the runtime interaction layer is the current tendency toward heuristic scans and direct mutation. `SpellDrivenAutomation` is cool in concept, but it currently rotates spreaders directly, mutates burst velocity, and writes temporary data to Botania-related block entities in a very ad hoc way. Keep that feature experimental and behind config until it is rebuilt around clearly bounded interactions, strict performance throttles, and real Botania affordances. Similarly, nearby-pool scanning should be narrowed into cached or bound interactions rather than becoming the hidden answer to every mana problem. fileciteturn102file0L3-L3 fileciteturn159file0L3-L3

What should be avoided is trying to simulate a nonexistent “Alfheim dimension” model too aggressively. Your changelog already acknowledges that Botania does not expose the old assumption the way the mod originally tried to treat it, and the current direction moved toward portal-proximity logic. That is the correct instinct: Botania 1.20.1 integration should be based on real portal blocks, real elven trade, and real late-game structures, not on invented dimensions or lore abstractions that the code cannot reliably observe. fileciteturn109file0L3-L3 citeturn13view5

The Botania-side architecture recommendation is simple. Keep Botania as a hard dependency in this branch. Put all Botania-specific content under explicit compat/service packages anyway, not because Botania is optional here, but because boundary clarity matters. Use real Botania systems where possible: Curios slot tags for wearables, mana items and pool capabilities for energy access, spark-aware mechanics for late-game throughput, and corporea only where item logistics genuinely improve spellcasting workflows. Corporea is especially worth using carefully because Botania’s docs make clear it is an inventory network, not a magical item-spawn machine. Any Iron’s Botany corporea feature should respect that distinction. fileciteturn114file0L3-L3 fileciteturn115file0L3-L3 fileciteturn160file0L3-L3 citeturn22view0turn23view3

### Cross-Mod Design Opportunities

**Runic Spell Catalysis** should be the first real expansion feature. The gameplay purpose is to make Botania runes matter in spell loadouts without turning them into dumb inventory tax. The technical path is to replace hardcoded catalyst matching with tag-driven catalyst definitions that map rune tags to modifier templates for specific Nature-school spells. Balance-wise, modifiers should be mutually exclusive by tier and capped per cast, not stackable into nonsense. Required data would be rune tags, catalyst definition JSONs, tooltip/lang keys, and perhaps alternate Runic Altar recipes. This should be enabled by default, configurable, medium difficulty, and high priority. fileciteturn120file0L3-L3 fileciteturn117file0L3-L3 citeturn13view1

**Mana Pool Attunement** is the cleanest answer to “can Botania mana feed ISS spells?” The gameplay purpose is endurance and infrastructure reward for dedicated nature casters. The technical path is a bound-item or bound-trinket mechanic that authorizes a limited nearby pool or spark network as a supplementary source for Nature-school botanical spells only. Balance-wise, bandwidth must be capped, range limited, and off by default for non-botanical schools. Required data would be a bindable item, config caps, pool access rules, particles, and HUD feedback. This should be enabled by default once stable, configurable, medium difficulty, and top priority. fileciteturn159file0L3-L3 citeturn23view1turn23view2

**Corporea Reagent Recall** is a strong late-game convenience feature if implemented honestly. The gameplay purpose is to reward automated Botania bases by letting certain high-tier preparations or spell rituals request tagged reagents from Corporea networks. The technical path is to scope the feature to explicit reagent lists and explicit device contexts instead of letting any spell vacuum items from the void. Balance-wise, this should never replace carrying normal casting resources for every spell; it should be a premium logistics perk for ritual-grade content. Required data would be reagent tags, corporea request mappings, failure messaging, and optional JEI/EMI integration notes. This should be disabled by default until stable, configurable, medium-to-high difficulty, and medium priority. fileciteturn102file0L3-L3 citeturn23view3

**Elven Bloom Scrolls** are viable as late-game luxury content. The gameplay purpose is to give Botania endgame a tasteful spellcraft reward without breaking ISS’s entire scroll economy. The technical path is a small set of elven-trade or portal-gated upgrades for selected botanical scrolls that add second-order properties, not raw damage inflation. Balance-wise, these should be rare, spell-specific, and best understood as sidegrades. Required data would be upgraded scroll assets, recipe/trade definitions, tooltips, and attunement gating. This should likely be off by default in normal balance configs, configurable, medium difficulty, and medium priority. fileciteturn117file0L3-L3 citeturn13view5turn17view0

### Optimization and Config Strategy

For **startup and registration**, the first priority is to make every registered system prove it is alive. Add explicit initialization classes for casting channels, catalysts, flower auras, and any deep-synergy service registries. Log counts. Fail loudly in dev if a registry intended to be populated is empty after setup. The current architecture already has a clean place for this work in common setup; it just needs to stop being selective about what it actually initializes. fileciteturn14file0L3-L3 fileciteturn133file0L3-L3

For **runtime performance**, the main targets are mana-source aggregation, nearby-pool scans, and any repeated area scans tied to overlays or spell side effects. Convert full-cube scans into cached searches or explicit bindings where feasible. Keep experimental automation features disabled by default until they are costed properly. Continue throttling particles and visual effects; the changelog already shows the right instinct there, so keep following that road instead of backsliding into “pretty equals more packets.” fileciteturn159file0L3-L3 fileciteturn149file0L3-L3 fileciteturn109file0L3-L3

For **networking and side safety**, keep the current packet pattern and extend it consistently. Forge’s networking docs explicitly recommend separate client handlers plus `DistExecutor` wrapping for clientbound message handling, and the repository’s recent fixes already moved in that direction after real dedicated-server failures. That part should be treated as settled law. No more “just this once” client references in common code. That road leads directly back to bootstrap explosions. fileciteturn28file0L3-L3 fileciteturn29file0L3-L3 fileciteturn109file0L3-L3 citeturn8view0turn6view0

For **config and datapack boundaries**, server/common config should own numeric balance and enable/disable gates: mana conversion ratio, transfer rates, bandwidth caps, spell-side Botania surcharges, aura/catalyst multipliers, feature master toggles, and automation radii. Client config should stay visual: HUD position, particles, overlay pulse, and optional extra tooltip verbosity. Datapacks should own content definitions: recipes, cross-mod loot, catalyst mappings, aura mappings, valid reagent tags, progression recipe gates, and any upgrade-orb or spell-modifier definitions that do not need Java to exist. Curios slot assignment should continue using tags, because Curios is explicitly designed around tag-driven categorization. fileciteturn13file0L3-L3 fileciteturn111file0L3-L3 fileciteturn114file0L3-L3 fileciteturn115file0L3-L3 citeturn22view0turn7view1

## Testing and Roadmap

### Testing Plan

The testing bar for this mod needs to be much higher than “client launches and flowers happen.” Forge itself recommends always testing dedicated-server behavior, and this repository already has real history proving why that matters. Minimum validation should include: clean client launch; clean dedicated-server launch; client-to-dedicated-server connection; install matrix coverage with Botania + ISS + Curios, with and without optional Patchouli/JEI/Ars ’n’ Spells; spellbook visibility; scroll crafting and scroll enhancement; Curios equipping via tag files; block entity sync for reservoir/conduit visuals; mana aggregation across multiple Botania mana items; Dreamwood substitution with canceled and successful casts; Mana Rebirth death prevention; armor shield cooldown; packet safety; multiplayer spell visuals; and datapack reload behavior for all moved data-driven definitions. citeturn5view0turn6view0turn8view0 fileciteturn114file0L3-L3 fileciteturn115file0L3-L3 fileciteturn153file0L3-L3 fileciteturn156file0L3-L3 fileciteturn155file0L3-L3

You also need explicit **modpack-realism tests**. That means split mana across multiple tablets/bands/accessories, high-latency multiplayer, large numbers of nearby functional flora, repeated casting near pools, mixed Curios loadouts, loot-table override datapacks, and server uptime long enough to catch state leakage around persistent data and any static caches. If a feature only behaves correctly in a clean singleplayer dev run, it is not done. It is merely wearing a tie. fileciteturn159file0L3-L3 fileciteturn149file0L3-L3 fileciteturn109file0L3-L3

### Implementation Roadmap

**Phase one should be stabilization.** Fix the aggregated mana bug, register casting channels, either wire or remove dead progression flags, and add assertions/logging for every deep-synergy registry. Verify Dreamwood + pool payment paths against multi-source mana. Re-test dedicated server, client-server join, Curios equipping, and every event-driven defensive mechanic. This phase is about making the current promises true, not adding new promises. fileciteturn159file0L3-L3 fileciteturn133file0L3-L3 fileciteturn137file0L3-L3

**Phase two should be architecture cleanup.** Introduce datagen, centralize all remaining stray data keys, move cross-mod mappings out of Java where possible, split integration boundaries more cleanly, and replace hand-authored loot injection with data-driven loot mechanisms. This is where the mod stops fighting its own content footprint. fileciteturn111file0L3-L3 fileciteturn158file0L3-L3 fileciteturn161file0L3-L3 citeturn7view1

**Phase three should be core integration enhancement.** Rebuild cross-mod progression around real Botania stages and real ISS Nature-school identity. Add Petal Apothecary, Runic Altar, Mana Pool, and select Elven/late-game paths for the actual mod-defining items and upgrades. Centralize spell-Botania payment and scaling logic. Keep the feature list small and real. citeturn11view0turn13view1turn23view2turn17view0

**Phase four should be content expansion.** Add only the cross-mod features that survive all previous constraints: runic spell catalysis, bounded Mana Pool attunement, corporea reagent recall, and possibly late-game Elven Bloom Scrolls. Skip bloated novelty features that mainly exist to make tooltips longer. Nobody needs “obvious best-in-slot nonsense with twelve particle layers” sabotaging pack balance. citeturn21view0turn17view0

**Phase five should be polish and modpack support.** Finish JEI/EMI discoverability, Patchouli documentation accuracy, config comments, generated resource hygiene, balance presets, and regression tests. Make it obvious which systems are default, optional, experimental, or intended for packs to tune. That is how you graduate from “cool addon” to “serious bridge mod.” fileciteturn126file0L3-L3 fileciteturn13file0L3-L3

## Claude Code Execution Prompt

The confirmed execution environment for this repository is Forge 1.20.1 / Forge 47.4.16 / Java 17 / official mappings, with compile-time targets Botania 1.20.1-450, Iron’s Spells ’n Spellbooks 1.20.1-3.15.2, and Curios 5.14.1, plus optional Patchouli and JEI. Use that as the compatibility floor unless the report explicitly says to verify a detail first. fileciteturn111file0L3-L3 fileciteturn13file0L3-L3

```text
You are implementing changes for the Minecraft Forge 1.20.1 mod Iron's Botany.

Use the audit report as the source of truth.
Do not do a speculative rewrite.
Do not redesign systems that are not called out by the report.
Do not change confirmed environment targets.

Core rules:
1. Inspect code before editing each file.
2. Work phase by phase in this order:
   - Phase 1: Stabilization
   - Phase 2: Architecture Cleanup
   - Phase 3: Core Integration Enhancements
   - Phase 4: Content Expansion
   - Phase 5: Polish and Modpack Support
3. Make small, verifiable changes.
4. Preserve existing behavior unless the report identifies it as broken, dead, misleading, or unsafe.
5. Prefer data-driven solutions over hardcoded logic where the report recommends them.
6. Keep Botania, Iron's Spells 'n Spellbooks, and Curios as hard dependencies in this branch.
7. Treat Patchouli, JEI, and Ars 'n' Spells integration as optional compat boundaries.
8. After each major change set, run or describe the relevant validation step.
9. Record unresolved issues rather than guessing.
10. Keep all client-only code isolated from common/server code.

Implementation priorities:
- First fix the aggregated Botania mana bug in ManaHelper so costs can be paid across multiple mana items/accessories.
- Register casting channels properly and add dev-time assertions/logging so empty registries are caught.
- Either wire UnifiedAdvancementSystem into real mechanics or remove/defer dead progression flags.
- Add tests/checks for dedicated server safety, spell casting, Curios equipping, and mana behavior.
- Introduce datagen for recipes, tags, advancements, sounds, language, and model/blockstate coverage where practical.
- Migrate late-game progression away from vanilla-only crafting and into Botania-native systems:
  Petal Apothecary, Runic Altar, Mana Pool transformations, and late-game Elven/Botania gates.
- Keep Iron's Botany aligned with ISS Nature school behavior. Do not reintroduce a custom Botanical school unless an unavoidable API blocker is proven.
- Centralize spell/Botania mana payment and modifier logic into a single authoritative integration flow.
- Move cross-mod mappings and feature definitions into tags, configs, and datapack content where possible.

Validation expectations:
- Clean client launch
- Clean dedicated server launch
- Client join to dedicated server
- Spell casting correctness
- Multi-source Botania mana payment
- Dreamwood conversion behavior
- Flower Shield / Mana Rebirth / armor shield behavior
- Curios equip tags
- Recipe loading
- Datapack reload
- Multiplayer sync
- No client class references from common code

When finished with each phase:
- Summarize files changed
- Summarize behavior changed
- Summarize validation done
- List open risks or follow-up tasks
```

## Open Questions and Limitations

I was able to verify the repository itself in depth and cross-check it against Forge docs, Botania’s official Lexica/CurseForge materials, Curios’ official project documentation, and Iron’s Spells’ public project page. The largest remaining uncertainty is the exact **ISS developer-side schema and extension hooks** for every advanced feature in version 3.15.2, because I could confirm Iron’s Botany’s imported ISS classes and public project information, but I could not independently retrieve the full ISS developer docs within the research constraints. Before implementing upgrade-orb registry data, advanced spell metadata, or any deeper spellbook/scroll data paths, verify those exact ISS 3.15.2 expectations against the dependency jars or official developer docs. fileciteturn111file0L3-L3 fileciteturn141file0L3-L3 citeturn17view0

I also did not line-audit every single asset, model, or Patchouli entry individually. The report is therefore strongest on runtime behavior, architecture, dependencies, and progression structure, which is exactly where the highest-value work lives anyway. The missing line-by-line asset audit should be handled during phase five once the code stops lying about what the systems actually do. fileciteturn126file0L3-L3 fileciteturn146file0L1-L3 fileciteturn163file0L1-L3