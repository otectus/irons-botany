package com.ironsbotany.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the particle sprite-id contract fixed in commit 3c3d64b: the entries in a particle JSON are
 * <em>atlas sprite ids</em>, not texture paths, so they carry no {@code particle/} prefix, no
 * {@code textures/} segment and no {@code .png} suffix. Reintroducing any of those loads nothing and
 * the effect renders as the pink-and-black missing-texture square.
 */
@DisplayName("particle sprite-id contract (pins the fix in commit 3c3d64b: atlas ids, no particle/ prefix)")
class ParticleResourceContractTest {

    private static final Path PARTICLES =
            Path.of("src/main/resources/assets/ironsbotany/particles");
    private static final Path PARTICLE_TEXTURES =
            Path.of("src/main/resources/assets/ironsbotany/textures/particle");

    /**
     * Mirrors {@code IBParticles}, which is a Forge {@code DeferredRegister} and so cannot be
     * class-loaded in a plain JUnit run. Add the registry name here when a particle is added.
     */
    private static final Set<String> EXPECTED = Set.of("mana_transfer", "botanical_burst", "petal_magic");

    private static List<Path> particleFiles() throws IOException {
        try (Stream<Path> files = Files.list(PARTICLES)) {
            return files.filter(p -> p.toString().endsWith(".json")).sorted().toList();
        }
    }

    private static String basename(Path file) {
        return file.getFileName().toString().replace(".json", "");
    }

    @Test
    @DisplayName("every registered particle has a definition, and no definition is an orphan")
    void definitionsMatchRegistrations() throws IOException {
        Set<String> found = new TreeSet<>();
        for (Path file : particleFiles()) found.add(basename(file));

        assertEquals(new TreeSet<>(EXPECTED), found,
                "the particle JSONs and the registry names in IBParticles have diverged");
    }

    @Test
    @DisplayName("every sprite id is a bare ironsbotany atlas id backed by a real PNG")
    void spriteIdsResolveToTextures() throws IOException {
        List<String> failures = new ArrayList<>();

        for (Path file : particleFiles()) {
            JsonObject json = JsonParser.parseString(
                    Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();

            if (!json.has("textures") || !json.get("textures").isJsonArray()) {
                failures.add(file + ": no 'textures' array");
                continue;
            }
            JsonArray textures = json.getAsJsonArray("textures");
            if (textures.isEmpty()) {
                failures.add(file + ": 'textures' is empty");
                continue;
            }

            for (JsonElement element : textures) {
                if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
                    failures.add(file + ": non-string entry " + element);
                    continue;
                }
                String id = element.getAsString();

                // A bare path would resolve to minecraft:, which never carries our sprites.
                int colon = id.indexOf(':');
                if (colon < 0) {
                    failures.add(file + ": '" + id + "' has no namespace");
                    continue;
                }
                String namespace = id.substring(0, colon);
                String path = id.substring(colon + 1);
                if (!"ironsbotany".equals(namespace)) {
                    failures.add(file + ": '" + id + "' is not in the ironsbotany namespace");
                    continue;
                }
                if (path.startsWith("particle/")) {
                    failures.add(file + ": '" + id + "' carries the particle/ prefix removed in 3c3d64b");
                    continue;
                }
                if (path.contains("textures/")) {
                    failures.add(file + ": '" + id + "' is a texture path, not an atlas sprite id");
                    continue;
                }
                if (path.endsWith(".png")) {
                    failures.add(file + ": '" + id + "' must not name a file extension");
                    continue;
                }

                Path png = PARTICLE_TEXTURES.resolve(path + ".png");
                if (!Files.isRegularFile(png)) {
                    failures.add(file + ": '" + id + "' has no texture at " + png);
                }
            }
        }

        assertTrue(failures.isEmpty(), () -> "broken particle sprite ids:\n" + String.join("\n", failures));
    }
}
