package com.ironsbotany.common.casting;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Context for spell casting through a casting channel
 */
public class SpellCastContext {
    private final Player player;
    private final AbstractSpell spell;
    private final int spellLevel;
    private final ItemStack castingItem;
    
    private float castingSpeed = 1.0f;
    private float manaRegen = 0.0f;
    private float burstDamage = 1.0f;
    private float cooldown = 1.0f;
    private float manaCost = 1.0f;
    
    public SpellCastContext(Player player, AbstractSpell spell, int spellLevel, ItemStack castingItem) {
        this.player = player;
        this.spell = spell;
        this.spellLevel = spellLevel;
        this.castingItem = castingItem;
    }
    
    // Getters
    public Player getPlayer() {
        return player;
    }
    
    public AbstractSpell getSpell() {
        return spell;
    }
    
    public int getSpellLevel() {
        return spellLevel;
    }
    
    public ItemStack getCastingItem() {
        return castingItem;
    }
    
    // Modifiers
    public void multiplyCastingSpeed(float multiplier) {
        this.castingSpeed *= multiplier;
    }
    
    public void addManaRegen(float bonus) {
        this.manaRegen += bonus;
    }
    
    public void multiplyBurstDamage(float multiplier) {
        this.burstDamage *= multiplier;
    }
    
    public void multiplyCooldown(float multiplier) {
        this.cooldown *= multiplier;
    }
    
    public void multiplyManaCost(float multiplier) {
        this.manaCost *= multiplier;
    }
    
    // Getters for final values
    public float getCastingSpeed() {
        return castingSpeed;
    }
    
    public float getManaRegen() {
        return manaRegen;
    }
    
    public float getBurstDamage() {
        return burstDamage;
    }
    
    public float getCooldown() {
        return cooldown;
    }
    
    public float getManaCost() {
        return manaCost;
    }
}
