package com.ironsbotany.common.migration;

import com.ironsbotany.common.registry.BotanySchool;
import com.ironsbotany.common.util.DataKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fixtures for every starting shape a real save can present, plus the safety property that matters
 * most: genuine Iron's Spells Nature content must survive untouched.
 */
class ItemNbtMigrationTest {

    private static final String BOTANY = "ironsbotany:botany";
    private static final String LEGACY = "ironsbotany:botanical";
    private static final String NATURE = "irons_spellbooks:nature";

    /** An Iron's Botany scroll: ISS spell data naming one of our spells. */
    private static CompoundTag ourScroll() {
        CompoundTag root = new CompoundTag();
        CompoundTag spell = new CompoundTag();
        spell.putString("id", "ironsbotany:mana_bloom");
        spell.putInt("level", 3);
        root.put("ISB_Spell", spell);
        return root;
    }

    /** A genuine Iron's Spells Nature scroll. Nothing about it belongs to this mod. */
    private static CompoundTag genuineNatureScroll() {
        CompoundTag root = new CompoundTag();
        CompoundTag spell = new CompoundTag();
        spell.putString("id", "irons_spellbooks:poison_arrow");
        spell.putInt("level", 4);
        root.put("ISB_Spell", spell);
        return root;
    }

    @Nested
    @DisplayName("unambiguous legacy identifier")
    class LegacyIdentifier {

        @Test
        @DisplayName("this mod's own pre-1.6 school spelling is always rewritten")
        void legacyIsAlwaysMigrated() {
            CompoundTag tag = new CompoundTag();
            tag.putString(DataKeys.PRIMARY_SCHOOL, LEGACY);

            assertEquals(1, ItemNbtMigration.migrateItemTag(tag));
            assertEquals(BOTANY, tag.getString(DataKeys.PRIMARY_SCHOOL));
        }

        @Test
        @DisplayName("even under a key this mod does not own — the namespace proves ownership")
        void legacyIsMigratedAnywhere() {
            CompoundTag tag = new CompoundTag();
            tag.putString("SomeOtherModsField", LEGACY);

            assertEquals(1, ItemNbtMigration.migrateItemTag(tag));
            assertEquals(BOTANY, tag.getString("SomeOtherModsField"));
        }

        @Test
        void legacyIsMigratedInsideNestedCompoundsAndLists() {
            CompoundTag root = new CompoundTag();
            CompoundTag nested = new CompoundTag();
            nested.putString(DataKeys.SECONDARY_SCHOOL, LEGACY);
            root.put("nested", nested);

            ListTag list = new ListTag();
            list.add(StringTag.valueOf(LEGACY));
            root.put("list", list);

            assertEquals(2, ItemNbtMigration.migrateItemTag(root));
            assertEquals(BOTANY, root.getCompound("nested").getString(DataKeys.SECONDARY_SCHOOL));
            assertEquals(BOTANY, root.getList("list", 8).getString(0));
        }
    }

    @Nested
    @DisplayName("the ambiguous Nature value")
    class AmbiguousNature {

        @Test
        @DisplayName("rewritten in our own school field on an item proven to be ours")
        void natureIsMigratedOnOurItems() {
            CompoundTag tag = ourScroll();
            tag.putString(DataKeys.PRIMARY_SCHOOL, NATURE);

            assertEquals(1, ItemNbtMigration.migrateItemTag(tag));
            assertEquals(BOTANY, tag.getString(DataKeys.PRIMARY_SCHOOL));
        }

        @Test
        @DisplayName("NOT rewritten on an item that is not ours, even in our own field")
        void natureSurvivesOnForeignItems() {
            CompoundTag tag = genuineNatureScroll();
            tag.putString(DataKeys.PRIMARY_SCHOOL, NATURE);

            assertEquals(0, ItemNbtMigration.migrateItemTag(tag));
            assertEquals(NATURE, tag.getString(DataKeys.PRIMARY_SCHOOL),
                    "provenance is unknown, so the value must be left alone");
        }

        @Test
        @DisplayName("NOT rewritten under a field this mod does not own, even on our item")
        void natureSurvivesOutsideOurFields() {
            CompoundTag tag = ourScroll();
            tag.putString("SomeOtherModsSchool", NATURE);

            assertEquals(0, ItemNbtMigration.migrateItemTag(tag));
            assertEquals(NATURE, tag.getString("SomeOtherModsSchool"));
        }
    }

    @Nested
    @DisplayName("safety properties")
    class Safety {

        @Test
        @DisplayName("a genuine Iron's Spells Nature scroll is bit-for-bit unchanged")
        void genuineNatureScrollIsUntouched() {
            CompoundTag tag = genuineNatureScroll();
            CompoundTag before = tag.copy();

            assertEquals(0, ItemNbtMigration.migrateItemTag(tag));
            assertEquals(before, tag);
        }

        @Test
        @DisplayName("migration is idempotent — running twice equals running once")
        void idempotent() {
            CompoundTag once = ourScroll();
            once.putString(DataKeys.PRIMARY_SCHOOL, NATURE);
            once.putString(DataKeys.SECONDARY_SCHOOL, LEGACY);
            ItemNbtMigration.migrateItemTag(once);
            CompoundTag afterFirst = once.copy();

            assertEquals(0, ItemNbtMigration.migrateItemTag(once), "second run must find nothing to do");
            assertEquals(afterFirst, once);
        }

        @Test
        @DisplayName("unrelated data on a migrated item is preserved exactly")
        void preservesEverythingElse() {
            CompoundTag tag = ourScroll();
            tag.putString(DataKeys.PRIMARY_SCHOOL, LEGACY);
            tag.putInt("Damage", 42);
            tag.putString("CustomName", "{\"text\":\"Gran's Wand\"}");
            tag.putInt(DataKeys.BOTANIA_MANA, 12_345);
            CompoundTag ench = new CompoundTag();
            ench.putString("id", "minecraft:unbreaking");
            ench.putInt("lvl", 3);
            ListTag enchantments = new ListTag();
            enchantments.add(ench);
            tag.put("Enchantments", enchantments);

            ItemNbtMigration.migrateItemTag(tag);

            assertEquals(BOTANY, tag.getString(DataKeys.PRIMARY_SCHOOL));
            assertEquals(42, tag.getInt("Damage"));
            assertEquals("{\"text\":\"Gran's Wand\"}", tag.getString("CustomName"));
            assertEquals(3, tag.getList("Enchantments", 10).getCompound(0).getInt("lvl"));
            assertEquals(12_345, tag.getInt(DataKeys.BOTANIA_MANA), "stored mana NBT preserved");
            assertEquals("ironsbotany:mana_bloom", tag.getCompound("ISB_Spell").getString("id"));
            assertEquals(3, tag.getCompound("ISB_Spell").getInt("level"), "spell level preserved");
        }

        @Test
        void nullAndEmptyTagsAreHandled() {
            assertEquals(0, ItemNbtMigration.migrateItemTag(null));
            assertEquals(0, ItemNbtMigration.migrateItemTag(new CompoundTag()));
        }

        @Test
        @DisplayName("already-canonical data needs no work")
        void canonicalIsANoOp() {
            CompoundTag tag = ourScroll();
            tag.putString(DataKeys.PRIMARY_SCHOOL, BotanySchool.ID_STRING);

            assertEquals(0, ItemNbtMigration.migrateItemTag(tag));
        }
    }

    @Nested
    @DisplayName("schema versioning")
    class SchemaVersioning {

        @Test
        @DisplayName("a player carrying only the 1.9.0 boolean is treated as unmigrated")
        void legacyBooleanCountsAsVersionZero() {
            CompoundTag persistentData = new CompoundTag();
            persistentData.putBoolean(DataKeys.SCHOOL_MIGRATED, true);

            // The 1.9.0 boolean recorded a migration whose destination was ISS Nature, which is the
            // wrong target now — so it must not be read as "already current".
            assertEquals(0, IBDataMigration.readSchemaVersion(persistentData));
        }

        @Test
        void explicitVersionIsRead() {
            CompoundTag persistentData = new CompoundTag();
            persistentData.putInt(IBDataMigration.KEY_SCHEMA_VERSION, 1);
            assertEquals(1, IBDataMigration.readSchemaVersion(persistentData));
        }

        @Test
        @DisplayName("a corrupt negative version is clamped rather than skipping every step")
        void negativeVersionIsClamped() {
            CompoundTag persistentData = new CompoundTag();
            persistentData.putInt(IBDataMigration.KEY_SCHEMA_VERSION, -7);
            assertEquals(0, IBDataMigration.readSchemaVersion(persistentData));
        }

        @Test
        void freshPlayerIsVersionZero() {
            assertEquals(0, IBDataMigration.readSchemaVersion(new CompoundTag()));
        }
    }
}
