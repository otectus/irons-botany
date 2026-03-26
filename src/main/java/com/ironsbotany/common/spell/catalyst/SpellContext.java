package com.ironsbotany.common.spell.catalyst;

import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context object that holds spell modifications from catalysts and auras.
 * This allows multiple systems to modify spell behavior without tight coupling.
 */
public class SpellContext {
    private final Level level;
    private final LivingEntity caster;
    private final int spellLevel;
    private final CastSource castSource;
    
    // Catalyst modifications
    private float damageMultiplier = 1.0f;
    private float rangeMultiplier = 1.0f;
    private float cooldownMultiplier = 1.0f;
    private float manaCostMultiplier = 1.0f;
    private float castingSpeedMultiplier = 1.0f;
    private int additionalProjectiles = 0;
    private boolean piercing = false;
    private boolean homing = false;
    private final List<MobEffectInstance> additionalEffects = new ArrayList<>();
    private final Map<String, Object> customData = new HashMap<>();
    
    public SpellContext(Level level, LivingEntity caster, int spellLevel, CastSource castSource) {
        this.level = level;
        this.caster = caster;
        this.spellLevel = spellLevel;
        this.castSource = castSource;
    }
    
    // Getters
    public Level getLevel() {
        return level;
    }
    
    public LivingEntity getCaster() {
        return caster;
    }
    
    public int getSpellLevel() {
        return spellLevel;
    }
    
    public CastSource getCastSource() {
        return castSource;
    }
    
    // Multiplier methods
    public void multiplyDamage(float multiplier) {
        this.damageMultiplier *= multiplier;
    }
    
    public void multiplyRange(float multiplier) {
        this.rangeMultiplier *= multiplier;
    }
    
    public void multiplyCooldown(float multiplier) {
        this.cooldownMultiplier *= multiplier;
    }
    
    public void multiplyManaCost(float multiplier) {
        this.manaCostMultiplier *= multiplier;
    }
    
    public void multiplyCastingSpeed(float multiplier) {
        this.castingSpeedMultiplier *= multiplier;
    }
    
    // Projectile modifications
    public void addProjectiles(int count) {
        this.additionalProjectiles += count;
    }
    
    public void setPiercing(boolean piercing) {
        this.piercing = piercing;
    }
    
    public void setHoming(boolean homing) {
        this.homing = homing;
    }
    
    // Effect management
    public void addEffect(MobEffectInstance effect) {
        this.additionalEffects.add(effect);
    }
    
    public List<MobEffectInstance> getAdditionalEffects() {
        return new ArrayList<>(additionalEffects);
    }
    
    // Custom data storage
    public void setCustomData(String key, Object value) {
        this.customData.put(key, value);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getCustomData(String key, Class<T> type) {
        Object value = customData.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    public boolean hasCustomData(String key) {
        return customData.containsKey(key);
    }
    
    // Apply modifications to base values
    public float getModifiedDamage(float baseDamage) {
        return baseDamage * damageMultiplier;
    }
    
    public float getModifiedRange(float baseRange) {
        return baseRange * rangeMultiplier;
    }
    
    public int getModifiedCooldown(int baseCooldown) {
        return (int) (baseCooldown * cooldownMultiplier);
    }
    
    public int getModifiedManaCost(int baseManaCost) {
        return (int) (baseManaCost * manaCostMultiplier);
    }
    
    public float getModifiedCastingSpeed(float baseCastingSpeed) {
        return baseCastingSpeed * castingSpeedMultiplier;
    }
    
    // Getters for modifications
    public int getAdditionalProjectiles() {
        return additionalProjectiles;
    }
    
    public boolean isPiercing() {
        return piercing;
    }
    
    public boolean isHoming() {
        return homing;
    }
    
    public float getDamageMultiplier() {
        return damageMultiplier;
    }
    
    public float getRangeMultiplier() {
        return rangeMultiplier;
    }
    
    public float getCooldownMultiplier() {
        return cooldownMultiplier;
    }
    
    public float getManaCostMultiplier() {
        return manaCostMultiplier;
    }
    
    public float getCastingSpeedMultiplier() {
        return castingSpeedMultiplier;
    }
}
