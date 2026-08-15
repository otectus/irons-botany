package com.ironsbotany.client;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.registry.IBSchools;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies, against the <em>live</em> spell registry, that every Iron's Botany spell resolves the
 * icon and school metadata that Iron's Spells and its add-ons will ask for.
 *
 * <p>The Gradle {@code validateArtifact} task checks the shipped JAR, but it can only cross-check
 * the registration source. Only the running game can enumerate what actually registered, resolve a
 * texture through the real resource manager with the player's resource packs applied, and exercise
 * the ISS accessors an add-on calls. That is what this does, on every resource reload — so
 * {@code F3+T} re-runs it and a resource pack that shadows an icon with a broken file is reported.
 *
 * <p>This is a diagnostic, not a gate: it logs. A missing icon should not stop a player launching.
 * The build gate is where a missing icon fails the release.
 */
public final class SpellIconIntegrityCheck implements ResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        run(resourceManager);
    }

    /** Also callable from a debug command. Returns the problems found, empty when healthy. */
    public static List<String> run(ResourceManager resourceManager) {
        List<String> problems = new ArrayList<>();
        int checked = 0;

        for (AbstractSpell spell : SpellRegistry.REGISTRY.get()) {
            ResourceLocation spellId = spell.getSpellResource();
            if (spellId == null || !IronsBotany.MODID.equals(spellId.getNamespace())) continue;
            checked++;

            checkIcon(resourceManager, spell, problems);
            checkSchoolMetadata(spell, problems);
        }

        if (problems.isEmpty()) {
            IronsBotany.LOGGER.info("Spell integrity check: {} spell(s) OK — icons resolve and school metadata is complete",
                    checked);
        } else {
            IronsBotany.LOGGER.error("Spell integrity check found {} problem(s) across {} spell(s):",
                    problems.size(), checked);
            problems.forEach(p -> IronsBotany.LOGGER.error("  {}", p));
        }
        return problems;
    }

    /**
     * Resolve the icon through the same accessor add-ons use, then open, decode and measure it.
     *
     * <p>ISS derives the path as
     * {@code <namespace>:textures/gui/spell_icons/<spellName>.png}. Asserting existence is not
     * enough: a zero-byte or undecodable file passes that and still renders as the missing-texture
     * swatch.
     */
    private static void checkIcon(ResourceManager resourceManager, AbstractSpell spell, List<String> problems) {
        ResourceLocation icon;
        try {
            icon = spell.getSpellIconResource();
        } catch (RuntimeException e) {
            problems.add(spell.getSpellId() + ": getSpellIconResource() threw " + e);
            return;
        }
        if (icon == null) {
            problems.add(spell.getSpellId() + ": getSpellIconResource() returned null");
            return;
        }

        var resource = resourceManager.getResource(icon);
        if (resource.isEmpty()) {
            problems.add(spell.getSpellId() + ": icon " + icon + " does not resolve — it will render as missing texture");
            return;
        }

        try (InputStream in = resource.get().open()) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                problems.add(spell.getSpellId() + ": icon " + icon + " is not a decodable PNG");
            } else if (image.getWidth() != 16 || image.getHeight() != 16) {
                problems.add(spell.getSpellId() + ": icon " + icon + " is "
                        + image.getWidth() + "x" + image.getHeight() + ", expected 16x16");
            }
        } catch (Exception e) {
            problems.add(spell.getSpellId() + ": icon " + icon + " could not be read — " + e);
        }
    }

    /**
     * Call the public {@code SchoolType} accessors that description and restriction add-ons use.
     *
     * <p>{@code getTargetingColor()} is the one that mattered: it dereferences the school's cached
     * display style colour with no null check, so an unstyled display name made it throw for every
     * Botany spell. Exercising it here means a regression is reported on the very next resource
     * reload rather than in a user's crash report.
     */
    private static void checkSchoolMetadata(AbstractSpell spell, List<String> problems) {
        SchoolType school;
        try {
            school = spell.getSchoolType();
        } catch (RuntimeException e) {
            problems.add(spell.getSpellId() + ": getSchoolType() threw " + e);
            return;
        }
        if (school == null) {
            problems.add(spell.getSpellId() + ": getSchoolType() returned null");
            return;
        }

        try {
            spell.getTargetingColor(); // the 1.9.0 crash site
        } catch (RuntimeException e) {
            problems.add(spell.getSpellId() + ": getTargetingColor() threw " + e
                    + " — the school's display name is missing an explicit colour");
        }

        try {
            Component name = school.getDisplayName();
            if (name == null) {
                problems.add(spell.getSpellId() + ": school " + school.getId() + " has a null display name");
            } else if (name.getStyle().getColor() == null) {
                problems.add(spell.getSpellId() + ": school " + school.getId()
                        + " has an uncoloured display name — getTargetingColor() will throw");
            }
            if (school.getDamageType() == null) {
                problems.add(spell.getSpellId() + ": school " + school.getId() + " has no damage type");
            }
            if (school.getFocus() == null) {
                problems.add(spell.getSpellId() + ": school " + school.getId() + " has no focus tag");
            }
            if (school.getCastSound() == null) {
                problems.add(spell.getSpellId() + ": school " + school.getId() + " has no cast sound");
            }
        } catch (RuntimeException e) {
            problems.add(spell.getSpellId() + ": school accessor threw " + e);
        }

        // Every spell this mod registers should belong to this mod's school. A mismatch means a
        // stale generated ISS spell config is overriding it, which is worth naming precisely.
        if (school != IBSchools.BOTANY.get()) {
            problems.add(spell.getSpellId() + ": resolved school is " + school.getId()
                    + " rather than ironsbotany:botany — check config/irons_spellbooks/spells/ironsbotany/");
        }
    }

    /** Convenience for a client that already has a resource manager to hand. */
    public static List<String> runOnClient() {
        Minecraft mc = Minecraft.getInstance();
        return mc == null ? List.of() : run(mc.getResourceManager());
    }
}
