package com.ironsbotany.common.migration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The textual rewrite that repairs the stale school value Iron's Spells wrote into its generated
 * spell-config files for this mod's spells. Iron's Spells never repairs an existing file
 * ({@code generateSpellConfigFile} short-circuits on {@code file.exists()}), so an upgraded world
 * keeps the wrong value forever without this.
 */
class SpellConfigRepairTest {

    @Test
    @DisplayName("the legacy Nature school value is replaced with canonical Botany")
    void rewritesLegacyNature() {
        String before = """
                {
                  "school": "irons_spellbooks:nature",
                  "max_level": 5,
                  "cooldown_in_seconds": 30.0
                }""";

        String after = SpellConfigRepair.rewriteSchool(before);

        assertNotNull(after);
        assertTrue(after.contains("\"school\": \"ironsbotany:botany\""));
        assertTrue(after.contains("\"max_level\": 5"), "other settings survive");
        assertTrue(after.contains("\"cooldown_in_seconds\": 30.0"));
    }

    @Test
    @DisplayName("the pre-1.6 school spelling is also repaired")
    void rewritesLegacyBotanical() {
        String after = SpellConfigRepair.rewriteSchool("{\"school\":\"ironsbotany:botanical\"}");

        assertNotNull(after);
        assertTrue(after.contains("ironsbotany:botany"));
    }

    @Test
    @DisplayName("an already-correct file is not rewritten at all")
    void canonicalFileIsUntouched() {
        assertNull(SpellConfigRepair.rewriteSchool("{\"school\": \"ironsbotany:botany\"}"));
    }

    @Test
    @DisplayName("an operator's deliberate choice of another school is preserved")
    void deliberateOperatorChoiceSurvives() {
        assertNull(SpellConfigRepair.rewriteSchool("{\"school\": \"irons_spellbooks:fire\"}"));
    }

    @Test
    @DisplayName("only the school VALUE is touched, never a matching string elsewhere")
    void onlyTheSchoolKeyIsRewritten() {
        String before = """
                {
                  "school": "irons_spellbooks:nature",
                  "_comment": "was irons_spellbooks:nature before 1.10.0",
                  "some_other_key": "irons_spellbooks:nature"
                }""";

        String after = SpellConfigRepair.rewriteSchool(before);

        assertNotNull(after);
        assertTrue(after.contains("\"school\": \"ironsbotany:botany\""));
        assertTrue(after.contains("\"_comment\": \"was irons_spellbooks:nature before 1.10.0\""),
                "a comment mentioning the old value must not be edited");
        assertTrue(after.contains("\"some_other_key\": \"irons_spellbooks:nature\""),
                "an unrelated key holding the same string must not be edited");
    }

    @Test
    @DisplayName("formatting and key order survive, because this is not a parse-and-reserialise")
    void formattingIsPreserved() {
        String before = "{\n\t\"max_level\" : 5,\n\t\"school\"   :   \"irons_spellbooks:nature\",\n\t\"enabled\": true\n}";

        String after = SpellConfigRepair.rewriteSchool(before);

        assertNotNull(after);
        assertTrue(after.startsWith("{\n\t\"max_level\" : 5,"), "key order and whitespace kept");
        assertTrue(after.endsWith("\t\"enabled\": true\n}"));
        assertTrue(after.contains("ironsbotany:botany"));
    }

    @Test
    void rewriteIsIdempotent() {
        String once = SpellConfigRepair.rewriteSchool("{\"school\": \"irons_spellbooks:nature\"}");
        assertNotNull(once);
        assertNull(SpellConfigRepair.rewriteSchool(once), "a repaired file needs no second repair");
    }

    @Test
    void handlesNullAndUnrelatedContent() {
        assertNull(SpellConfigRepair.rewriteSchool(null));
        assertNull(SpellConfigRepair.rewriteSchool(""));
        assertNull(SpellConfigRepair.rewriteSchool("{\"max_level\": 5}"));
    }
}
