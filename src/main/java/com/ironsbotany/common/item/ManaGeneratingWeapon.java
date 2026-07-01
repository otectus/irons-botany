package com.ironsbotany.common.item;

/**
 * Marker for melee weapons that generate Botania mana on a qualifying hit
 * (the Terrasteel Spell Blade and the Elementium/Gaia Mage Swords). The
 * shared mana-on-hit logic — damage gate, anti-autoclicker cooldown, and
 * deposit into the player's mana items — lives in
 * {@code TerrasteelBladeHandler}; each weapon only reports how much mana a
 * single qualifying hit yields.
 */
public interface ManaGeneratingWeapon {

    /** Botania mana granted per qualifying melee hit (0 disables generation). */
    int getManaPerHit();
}
