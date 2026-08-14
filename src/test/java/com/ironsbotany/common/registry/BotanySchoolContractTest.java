package com.ironsbotany.common.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the metadata contract that Iron's Spells 'n Spellbooks 3.16.1 imposes on a custom
 * school, and that Iron's Botany 1.9.0 violated.
 *
 * <h3>What this test can and cannot prove</h3>
 * It cannot construct a real {@code SchoolType}: the ISS JAR in {@code libs/} is a
 * production artifact compiled against SRG names, so it cannot be linked in a
 * Mojang-mapped test runtime. What it does instead is assert the contract on the exact
 * value ISS consumes — the display {@link Component} — reproducing ISS's own accessor
 * chain against real Minecraft classes:
 *
 * <pre>
 *   SchoolType(id, focus, displayName, ...) { this.displayStyle = displayName.getStyle(); }
 *   getTargetingColor()                     { return deconstructRGB(displayStyle.getColor().getValue()); }
 * </pre>
 *
 * Both steps are executed literally below, so a regression that drops the style is caught
 * here rather than in a user's crash report.
 */
class BotanySchoolContractTest {

    /**
     * Performs exactly what {@code SchoolType#getTargetingColor()} does, on the component
     * this mod hands to the {@code SchoolType} constructor.
     */
    private static int targetingColorOf(Component displayName) {
        Style cachedByConstructor = displayName.getStyle();
        TextColor color = cachedByConstructor.getColor();
        return color.getValue(); // ISS dereferences with no null check
    }

    @Test
    @DisplayName("Botany's display name survives ISS's getTargetingColor() chain")
    void targetingColorResolvesWithoutNpe() {
        assertEquals(BotanySchool.DISPLAY_COLOR, targetingColorOf(BotanySchool.displayName()));
    }

    @Test
    @DisplayName("regression: the 1.9.0 construction is what threw, and still would")
    void unstyledComponentIsTheDefect() {
        // Verbatim reproduction of IBSchools.java:49 as it stood in 1.9.0.
        Component asShippedIn190 = Component.translatable(BotanySchool.TRANSLATION_KEY);

        assertNull(asShippedIn190.getStyle().getColor(),
                "an unstyled translatable component carries Style.EMPTY, whose colour is null");
        assertThrows(NullPointerException.class, () -> targetingColorOf(asShippedIn190),
                "this is the crash users hit with ISS-Scroll Descriptions / ISS-Restrictions");
    }

    @Test
    @DisplayName("the style is attached to the component itself, not only to a sibling")
    void styleIsOnTheRootComponent() {
        // SchoolType caches getStyle() of the component it is handed. A style applied to a
        // child or sibling would be invisible to it, so assert on the root specifically.
        assertNotNull(BotanySchool.displayName().getStyle().getColor());
        assertEquals(BotanySchool.DISPLAY_STYLE, BotanySchool.displayName().getStyle());
    }

    @Test
    @DisplayName("canonical school identity is stable and self-consistent")
    void canonicalIdentity() {
        assertEquals("ironsbotany:botany", BotanySchool.ID_STRING);
        assertEquals(BotanySchool.ID_STRING, BotanySchool.ID.toString());
        assertEquals("botany", BotanySchool.ID.getPath());
        assertEquals("ironsbotany", BotanySchool.ID.getNamespace());
    }

    @Test
    @DisplayName("Botany-ownership check accepts our own spellings and rejects ISS Nature")
    void ownershipCheckDoesNotClaimNatureContent() {
        assertTrue(BotanySchool.isBotanyId("ironsbotany:botany"));
        assertTrue(BotanySchool.isBotanyId("ironsbotany:botanical"), "pre-1.6.0 spelling");

        // The whole point of the migration safety rule: a genuine Iron's Spells Nature
        // scroll must never be treated as Iron's Botany content.
        assertFalse(BotanySchool.isBotanyId("irons_spellbooks:nature"));
        assertFalse(BotanySchool.isBotanyId("irons_spellbooks:fire"));
        assertFalse(BotanySchool.isBotanyId(null));
        assertFalse(BotanySchool.isBotanyId(""));
    }

    @Test
    @DisplayName("display colour is readable on Minecraft's tooltip background")
    void displayColourHasAdequateContrast() {
        // Vanilla tooltip fill is #100010. WCAG AA for normal text is 4.5:1.
        double contrast = contrastRatio(BotanySchool.DISPLAY_COLOR, 0x100010);
        assertTrue(contrast >= 4.5,
                () -> "contrast on the tooltip background was " + contrast + ":1");

        // And it must not be confusable with the ISS school it used to be filed under
        // (Nature is ChatFormatting.GREEN, #55FF55).
        assertNotEquals(0x55FF55, BotanySchool.DISPLAY_COLOR);
    }

    private static double contrastRatio(int rgbA, int rgbB) {
        double la = relativeLuminance(rgbA);
        double lb = relativeLuminance(rgbB);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    private static double relativeLuminance(int rgb) {
        double r = channel((rgb >> 16) & 0xFF);
        double g = channel((rgb >> 8) & 0xFF);
        double b = channel(rgb & 0xFF);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private static double channel(int raw) {
        double c = raw / 255.0;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }
}
