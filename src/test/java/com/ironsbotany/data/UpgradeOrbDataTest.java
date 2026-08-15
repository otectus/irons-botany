package com.ironsbotany.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Data-driven identity checks for the upgrade orbs.
 *
 * <p>Through 1.9.0 twelve orb items shared four orb types. Each of the eight school-named orbs
 * therefore applied some other orb's attribute and handed back some other orb's item when consumed,
 * because an orb type's {@code containerItem} names the orb it returns — Orb of Fire Power granted
 * ISS <i>nature</i> spell power and turned into an Orb of Flora. These tests make that class of
 * mistake impossible to reintroduce without a failing build.
 */
class UpgradeOrbDataTest {

    private static final Path ORB_TYPES =
            Path.of("src/main/resources/data/ironsbotany/irons_spellbooks/upgrade_orb_type");
    private static final Path ITEMS_SOURCE =
            Path.of("src/main/java/com/ironsbotany/common/registry/IBItems.java");
    private static final Path EN_US =
            Path.of("src/main/resources/assets/ironsbotany/lang/en_us.json");

    /** item registry path -> orb type path, read from the registration source. */
    private static Map<String, String> registeredOrbs() throws IOException {
        String source = Files.readString(ITEMS_SOURCE, StandardCharsets.UTF_8);
        Matcher m = Pattern.compile(
                "ITEMS\\.register\\(\"([a-z0-9_]+)\",\\s*\\(\\) -> new BotanicalUpgradeOrbItem\\("
                        + ".*?typeKey\\(\"([a-z0-9_]+)\"\\)", Pattern.DOTALL).matcher(source);

        Map<String, String> orbs = new HashMap<>();
        while (m.find()) orbs.put(m.group(1), m.group(2));

        // Guard against a vacuous pass: if the source shape changes and this stops matching, every
        // test below would silently succeed against an empty map.
        assertFalse(orbs.isEmpty(), "no orb registrations were parsed from " + ITEMS_SOURCE);
        return orbs;
    }

    private static JsonObject orbType(String type) throws IOException {
        Path file = ORB_TYPES.resolve(type + ".json");
        assertTrue(Files.isRegularFile(file), () -> "missing orb type data file: " + file);
        return JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    @Test
    @DisplayName("every registered orb has its own orb type — no two orbs share one")
    void orbTypesAreNotShared() throws IOException {
        Map<String, String> orbs = registeredOrbs();
        assertEquals(12, orbs.size(), "expected all twelve orb items to be discovered");

        Map<String, String> typeToItem = new HashMap<>();
        orbs.forEach((item, type) -> {
            String previous = typeToItem.put(type, item);
            assertNull(previous, () -> "orb type '" + type + "' is claimed by both "
                    + previous + " and " + item + " — one of them will apply the wrong attribute"
                    + " and return the wrong item");
        });
    }

    @Test
    @DisplayName("every orb type's containerItem is the orb that uses it")
    void containerItemRoundTrips() throws IOException {
        for (Map.Entry<String, String> entry : registeredOrbs().entrySet()) {
            String item = entry.getKey();
            JsonObject data = orbType(entry.getValue());

            assertTrue(data.has("containerItem"), () -> entry.getValue() + ".json has no containerItem");
            String container = data.getAsJsonObject("containerItem").get("id").getAsString();

            assertEquals("ironsbotany:" + item, container,
                    () -> "consuming " + item + " would hand back " + container);
        }
    }

    @Test
    @DisplayName("each school orb boosts its own school's attribute")
    void schoolOrbsBoostTheirOwnSchool() throws IOException {
        Map<String, String> expected = Map.of(
                "fire", "irons_spellbooks:fire_spell_power",
                "frost", "irons_spellbooks:ice_spell_power",
                "lightning", "irons_spellbooks:lightning_spell_power",
                "holy", "irons_spellbooks:holy_spell_power",
                "ender", "irons_spellbooks:ender_spell_power",
                "blood", "irons_spellbooks:blood_spell_power",
                "nature", "irons_spellbooks:nature_spell_power",
                "eldritch", "irons_spellbooks:eldritch_spell_power");

        for (Map.Entry<String, String> entry : expected.entrySet()) {
            JsonObject data = orbType(entry.getKey());
            assertEquals(entry.getValue(), data.get("attribute").getAsString(),
                    () -> "orb type '" + entry.getKey() + "' boosts the wrong school");
        }
    }

    @Test
    @DisplayName("the Orb of Flora boosts this mod's own school, not ISS Nature")
    void floraBoostsBotany() throws IOException {
        assertEquals("ironsbotany:botany_spell_power", orbType("flora").get("attribute").getAsString(),
                "Flora is Iron's Botany's school orb; boosting irons_spellbooks:nature made it "
                        + "useless for this mod's own spells");
    }

    @Test
    @DisplayName("every orb type file is well-formed")
    void orbTypeFilesAreComplete() throws IOException {
        try (Stream<Path> files = Files.list(ORB_TYPES)) {
            List<Path> jsons = files.filter(p -> p.toString().endsWith(".json")).toList();
            assertFalse(jsons.isEmpty());

            for (Path file : jsons) {
                JsonObject data = JsonParser.parseString(
                        Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();

                assertTrue(data.has("amount"), () -> file + ": no amount");
                assertTrue(data.has("attribute"), () -> file + ": no attribute");
                assertTrue(data.has("operation"), () -> file + ": no operation");
                assertTrue(data.has("containerItem"), () -> file + ": no containerItem");

                assertTrue(Set.of("ADDITION", "MULTIPLY_BASE", "MULTIPLY_TOTAL")
                                .contains(data.get("operation").getAsString()),
                        () -> file + ": unknown operation " + data.get("operation"));
                assertTrue(data.get("attribute").getAsString().contains(":"),
                        () -> file + ": attribute must be a namespaced id");
            }
        }
    }

    @Test
    @DisplayName("every orb has a name and a bonus tooltip in en_us")
    void everyOrbIsTranslated() throws IOException {
        JsonObject lang = JsonParser.parseString(
                Files.readString(EN_US, StandardCharsets.UTF_8)).getAsJsonObject();

        Set<String> missing = new HashSet<>();
        registeredOrbs().forEach((item, type) -> {
            if (!lang.has("item.ironsbotany." + item)) missing.add("item.ironsbotany." + item);
            // The 1.9.0 tooltip was a switch with cases for only four of the twelve orbs, so the
            // eight school orbs rendered with no bonus line at all.
            if (!lang.has("item.ironsbotany.orb." + type + ".bonus")) {
                missing.add("item.ironsbotany.orb." + type + ".bonus");
            }
        });

        assertTrue(missing.isEmpty(), () -> "missing translation keys: " + missing);
    }

    @Test
    @DisplayName("no orphan orb type data — every file belongs to a registered orb")
    void noOrphanOrbTypes() throws IOException {
        Set<String> used = new HashSet<>(registeredOrbs().values());
        try (Stream<Path> files = Files.list(ORB_TYPES)) {
            files.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                String type = p.getFileName().toString().replace(".json", "");
                assertTrue(used.contains(type),
                        () -> "orb type '" + type + "' is defined but no registered orb uses it");
            });
        }
    }
}
