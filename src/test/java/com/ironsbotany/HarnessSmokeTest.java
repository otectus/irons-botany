package com.ironsbotany;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Proves the unit-test harness can link against Minecraft classes without booting
 * the game. If this fails, every other test in this source set is meaningless.
 */
class HarnessSmokeTest {

    @Test
    void minecraftDataClassesAreOnTheTestClasspath() {
        CompoundTag tag = new CompoundTag();
        tag.putString("k", "v");
        assertEquals("v", tag.getString("k"));

        assertEquals("ironsbotany:botany", new ResourceLocation("ironsbotany", "botany").toString());

        // The exact accessor ISS's SchoolType#getTargetingColor() dereferences.
        assertNull(Style.EMPTY.getColor(), "baseline: an unstyled Style has no colour");
        assertEquals(0x6ABE30, Style.EMPTY.withColor(TextColor.fromRgb(0x6ABE30)).getColor().getValue());
    }
}
