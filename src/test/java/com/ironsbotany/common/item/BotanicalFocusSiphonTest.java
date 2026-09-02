package com.ironsbotany.common.item;

import com.ironsbotany.testsupport.MinecraftBootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Siphon Mode's stored state and the gesture rule that keeps equipping and toggling apart.
 *
 * <p>Before this, {@code canEquipFromUse} was unconditional, so Curios' right-click handler
 * cancelled the interaction before {@code use()} could ever toggle the mode the tooltip promised.
 */
class BotanicalFocusSiphonTest {

    @BeforeAll
    static void bootstrap() {
        MinecraftBootstrap.ensure(); // ItemStack needs the vanilla registries
    }

    @Test
    @DisplayName("a fresh stack reads OFF without being given a tag")
    void readDoesNotCreateATag() {
        ItemStack stack = new ItemStack(Items.STICK);

        assertFalse(BotanicalFocusSiphon.isSiphonMode(stack));
        assertNull(stack.getTag(), "merely reading Siphon Mode must not write NBT onto the stack");
    }

    @Test
    @DisplayName("toggling on returns the new state and writes it")
    void toggleOn() {
        ItemStack stack = new ItemStack(Items.STICK);

        assertTrue(BotanicalFocusSiphon.toggle(stack));
        assertNotNull(stack.getTag());
        assertTrue(stack.getTag().getBoolean(BotanicalFocusSiphon.TAG_SIPHON_MODE));
        assertTrue(BotanicalFocusSiphon.isSiphonMode(stack));
    }

    @Test
    @DisplayName("toggling again turns it back off")
    void toggleOff() {
        ItemStack stack = new ItemStack(Items.STICK);
        BotanicalFocusSiphon.toggle(stack);

        assertFalse(BotanicalFocusSiphon.toggle(stack));
        assertFalse(stack.getTag().getBoolean(BotanicalFocusSiphon.TAG_SIPHON_MODE));
        assertFalse(BotanicalFocusSiphon.isSiphonMode(stack));
    }

    @Test
    @DisplayName("the on-disk key is pinned — renaming it must go through the migration package")
    void keyIsPinned() {
        assertEquals("siphonMode", BotanicalFocusSiphon.TAG_SIPHON_MODE,
                "every already-saved Botanical Focus stores its mode under this key");
    }

    @Test
    @DisplayName("sneak equips, plain right-click toggles — the two gestures stay disjoint")
    void gesturesAreDisjoint() {
        assertTrue(BotanicalFocusSiphon.equipsOnUse(true));
        assertFalse(BotanicalFocusSiphon.equipsOnUse(false));
    }
}
