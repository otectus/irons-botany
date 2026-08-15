package com.ironsbotany.common.migration;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.registry.BotanySchool;
import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Repairs the stale {@code "school"} value in Iron's Spells' generated spell-config files for this
 * mod's spells.
 *
 * <h3>Why this is necessary</h3>
 * Iron's Spells resolves a spell's school from config, seeding each spell's file from its
 * {@code DefaultConfig} the first time it is seen. Through 1.9.0 every Iron's Botany spell declared
 * {@code irons_spellbooks:nature} there while the code overrode {@code getSchoolType()} to return
 * {@code ironsbotany:botany} — so the file and the runtime disagreed, and an operator editing the
 * school in that file changed nothing.
 *
 * <p>1.10.0 corrects the declared default, which fixes new installs. It cannot fix existing ones on
 * its own, because {@code SpellConfigManager.generateSpellConfigFile} short-circuits when the file
 * already exists:
 * <pre>
 *   if (file.exists() &amp;&amp; !overwrite) return Pair.of(false, file);
 * </pre>
 * so an upgraded world keeps the wrong value indefinitely.
 *
 * <h3>Why not a datapack override</h3>
 * Shipping {@code data/ironsbotany/irons_spellbooks_spell_config/*.json} looks like the tidy answer
 * and is actively dangerous. {@code SpellConfigManager.onDatapackSync} uses the datapack map
 * <em>instead of</em> the config directory, not merged with it — so a single such file would make
 * Iron's Spells ignore the operator's entire {@code config/irons_spellbooks/spells/} directory, for
 * every spell in the modpack.
 *
 * <h3>Scope of the edit</h3>
 * Strictly the {@code ironsbotany} subdirectory of Iron's Spells' spell-config folder; strictly the
 * {@code "school"} value; strictly when that value is the legacy one this mod itself wrote. Files
 * belonging to any other namespace are never opened. Every changed file is logged by name. An
 * operator who has deliberately set some other school keeps it, because the value will not match
 * the legacy string.
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SpellConfigRepair {

    private SpellConfigRepair() {}

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        try {
            List<String> repaired = repair();
            if (!repaired.isEmpty()) {
                IronsBotany.LOGGER.info(
                        "Repaired the stale school value in {} Iron's Spells spell-config file(s): {}",
                        repaired.size(), String.join(", ", repaired));
            }
        } catch (RuntimeException e) {
            IronsBotany.LOGGER.warn("Could not repair Iron's Spells spell-config files: {}", e.toString());
        }
    }

    /**
     * @return the names of the files changed; empty when nothing needed repair
     */
    static List<String> repair() {
        List<String> repaired = new ArrayList<>();

        File spellConfigDir = SpellConfigManager.getSpellConfigDir();
        if (spellConfigDir == null) return repaired;

        File ourDir = new File(spellConfigDir, IronsBotany.MODID);
        if (!ourDir.isDirectory()) return repaired;

        File[] files = ourDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return repaired;

        for (File file : files) {
            try {
                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                String updated = rewriteSchool(content);
                if (updated != null) {
                    Files.writeString(file.toPath(), updated, StandardCharsets.UTF_8);
                    repaired.add(file.getName());
                }
            } catch (Exception e) {
                IronsBotany.LOGGER.warn("Skipped spell config {}: {}", file.getName(), e.toString());
            }
        }
        return repaired;
    }

    /**
     * Replace a legacy Nature school value with the canonical Botany one.
     *
     * <p>Textual rather than a parse-and-reserialise so the operator's formatting, key order and
     * any comment keys Iron's Spells writes survive untouched.
     *
     * @return the rewritten content, or {@code null} if nothing needed changing
     */
    static String rewriteSchool(String content) {
        if (content == null) return null;
        if (!content.contains(BotanySchool.ISS_NATURE_ID_STRING)
                && !content.contains(BotanySchool.LEGACY_ID_STRING)) {
            return null;
        }

        String updated = content;
        for (String legacy : new String[]{BotanySchool.ISS_NATURE_ID_STRING, BotanySchool.LEGACY_ID_STRING}) {
            // Match only the value of the "school" key, so an unrelated string that happens to
            // equal a school id elsewhere in the file is left alone.
            updated = updated.replaceAll(
                    "(\"school\"\\s*:\\s*\")" + java.util.regex.Pattern.quote(legacy) + "(\")",
                    "$1" + java.util.regex.Matcher.quoteReplacement(BotanySchool.ID_STRING) + "$2");
        }
        return updated.equals(content) ? null : updated;
    }
}
