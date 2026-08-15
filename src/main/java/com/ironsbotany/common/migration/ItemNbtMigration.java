package com.ironsbotany.common.migration;

import com.ironsbotany.common.registry.BotanySchool;
import com.ironsbotany.common.util.DataKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/**
 * Pure NBT rewriting for the school migration. No Minecraft world, no player, no I/O — so every
 * rule below is directly unit-testable against hand-built fixtures.
 *
 * <h3>What 1.9.0 did, and why it was unsafe</h3>
 * {@code SchoolMigrationHandler} walked <em>every</em> string in <em>every</em> tag of every item a
 * player carried and rewrote any value equal to {@code ironsbotany:botanical} to
 * {@code irons_spellbooks:nature}. Two problems: the destination is wrong now that
 * {@code ironsbotany:botany} is canonical, and a blanket recursive string rewrite has no notion of
 * which fields belong to this mod. It was also guarded by a single boolean, so it could never be
 * revised or re-run.
 *
 * <h3>The rules here</h3>
 * <ol>
 *   <li>{@code ironsbotany:botanical} is rewritten to {@code ironsbotany:botany} wherever it
 *       appears. That string is in this mod's own namespace, so no other mod can own it and the
 *       rewrite is unambiguous.</li>
 *   <li>{@code irons_spellbooks:nature} is rewritten <strong>only</strong> under the two NBT keys
 *       this mod writes school IDs to ({@link DataKeys#PRIMARY_SCHOOL},
 *       {@link DataKeys#SECONDARY_SCHOOL}), and <strong>only</strong> when the enclosing item is
 *       demonstrably an Iron's Botany spell — its ISS {@code id} field names a spell in this mod's
 *       namespace. This is the case where 1.9.0's blanket pass already converted a legacy Botany
 *       value into a Nature one and the provenance would otherwise be lost.</li>
 *   <li>Nothing else is touched. A genuine Iron's Spells Nature scroll, a Nature focus, or any
 *       other mod's Nature reference is left exactly as it is.</li>
 * </ol>
 *
 * Every rule is idempotent: running the migration twice produces the same result as running it once.
 */
public final class ItemNbtMigration {

    /** ISS's spell-id NBT key ({@code SpellData.SPELL_ID}). */
    private static final String ISS_SPELL_ID = "id";

    private ItemNbtMigration() {}

    /**
     * Migrate one item's root NBT in place.
     *
     * @return the number of values rewritten; {@code 0} means the item was already current
     */
    public static int migrateItemTag(CompoundTag root) {
        if (root == null) return 0;
        return visit(root, isOwnedByThisMod(root));
    }

    /**
     * Is this item demonstrably Iron's Botany content?
     *
     * <p>True when an ISS spell-id anywhere in the item's NBT names a spell in this mod's
     * namespace. That is the "provenance is known" test: only then may an ambiguous Nature value in
     * one of this mod's own fields be reinterpreted as Botany.
     */
    static boolean isOwnedByThisMod(CompoundTag root) {
        return containsOwnSpellId(root);
    }

    private static boolean containsOwnSpellId(Tag tag) {
        if (tag instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) {
                Tag value = compound.get(key);
                if (ISS_SPELL_ID.equals(key) && value instanceof StringTag string
                        && string.getAsString().startsWith(com.ironsbotany.IronsBotany.MODID + ":")) {
                    return true;
                }
                if (containsOwnSpellId(value)) return true;
            }
            return false;
        }
        if (tag instanceof ListTag list) {
            for (Tag element : list) {
                if (containsOwnSpellId(element)) return true;
            }
        }
        return false;
    }

    private static int visit(Tag tag, boolean ownedByThisMod) {
        int rewritten = 0;

        if (tag instanceof CompoundTag compound) {
            // Copy the key set: rewriting a value under an existing key does not structurally
            // modify the map, but iterating the live set while editing is fragile enough to avoid.
            for (String key : java.util.List.copyOf(compound.getAllKeys())) {
                Tag value = compound.get(key);

                if (value instanceof StringTag string) {
                    String migrated = migrateValue(key, string.getAsString(), ownedByThisMod);
                    if (migrated != null) {
                        compound.putString(key, migrated);
                        rewritten++;
                    }
                } else {
                    rewritten += visit(value, ownedByThisMod);
                }
            }
            return rewritten;
        }

        if (tag instanceof ListTag list) {
            for (int i = 0; i < list.size(); i++) {
                Tag element = list.get(i);
                if (element instanceof StringTag string) {
                    // A bare string in a list has no key, so only the unambiguous rule can apply.
                    if (BotanySchool.LEGACY_ID_STRING.equals(string.getAsString())) {
                        list.set(i, StringTag.valueOf(BotanySchool.ID_STRING));
                        rewritten++;
                    }
                } else {
                    rewritten += visit(element, ownedByThisMod);
                }
            }
        }
        return rewritten;
    }

    /**
     * @return the replacement value, or {@code null} to leave the value untouched
     */
    private static String migrateValue(String key, String value, boolean ownedByThisMod) {
        // Rule 1 — unambiguous: this mod's own legacy school spelling.
        if (BotanySchool.LEGACY_ID_STRING.equals(value)) {
            return BotanySchool.ID_STRING;
        }

        // Rule 2 — narrow: a Nature value in one of this mod's school fields, on an item proven to
        // be ours. Anywhere else, Nature belongs to Iron's Spells and is left alone.
        if (ownedByThisMod
                && BotanySchool.ISS_NATURE_ID_STRING.equals(value)
                && isOwnSchoolField(key)) {
            return BotanySchool.ID_STRING;
        }

        return null;
    }

    private static boolean isOwnSchoolField(String key) {
        return DataKeys.PRIMARY_SCHOOL.equals(key) || DataKeys.SECONDARY_SCHOOL.equals(key);
    }
}
