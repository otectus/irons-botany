package com.ironsbotany.common.spell;

import com.ironsbotany.common.event.ManaNetworkModifier;
import com.ironsbotany.common.event.SpellTriggeredManaEvent;
import com.ironsbotany.common.registry.IBSchools;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.ModList;

/**
 * Integrates spell casting with Botania mana network.
 * Allows spells to trigger real-time modifications to Botania systems.
 */
public class SpellManaNetworkIntegration {
    
    private static final int DEFAULT_SEARCH_RADIUS = 8;
    private static final int DEFAULT_DURATION = 200; // 10 seconds
    
    /**
     * Trigger mana network effects based on spell cast
     * @param spell The spell being cast
     * @param caster The entity casting the spell
     * @param spellLevel The level of the spell
     */
    public static void triggerManaNetworkEffects(AbstractSpell spell, LivingEntity caster, int spellLevel) {
        if (!ModList.get().isLoaded("botania")) {
            return;
        }
        
        Level level = caster.level();
        if (level.isClientSide) {
            return;
        }
        
        BlockPos casterPos = caster.blockPosition();
        float intensity = 1.0f + (spellLevel * 0.1f); // Scale with spell level
        
        // Determine trigger type based on spell school
        SpellTriggeredManaEvent.SpellTriggerType triggerType = getTriggerType(spell);
        if (triggerType == null) {
            return; // Spell doesn't trigger mana network effects
        }
        
        // Find nearby Botania blocks and apply effects
        int radius = DEFAULT_SEARCH_RADIUS + spellLevel;
        AABB searchBox = new AABB(casterPos).inflate(radius);
        
        for (BlockPos pos : BlockPos.betweenClosed(
            (int)searchBox.minX, (int)searchBox.minY, (int)searchBox.minZ,
            (int)searchBox.maxX, (int)searchBox.maxY, (int)searchBox.maxZ)) {
            
            if (isBotaniaBlock(level, pos)) {
                // Calculate distance-based intensity
                double distance = Math.sqrt(casterPos.distSqr(pos));
                float distanceIntensity = intensity * (1.0f - (float)(distance / radius));
                
                if (distanceIntensity > 0.1f) {
                    ManaNetworkModifier.registerModification(
                        level, pos.immutable(), triggerType, 
                        distanceIntensity, DEFAULT_DURATION
                    );
                }
            }
        }
    }
    
    /**
     * Determine trigger type based on spell school and name
     */
    private static SpellTriggeredManaEvent.SpellTriggerType getTriggerType(AbstractSpell spell) {
        // Check spell school
        if (spell.getSchoolType() == IBSchools.BOTANICAL.get()) {
            return SpellTriggeredManaEvent.SpellTriggerType.BOTANICAL;
        }
        if (spell.getSchoolType() == SchoolRegistry.LIGHTNING.get()) {
            return SpellTriggeredManaEvent.SpellTriggerType.LIGHTNING;
        }
        if (spell.getSchoolType() == SchoolRegistry.NATURE.get()) {
            return SpellTriggeredManaEvent.SpellTriggerType.NATURE;
        }
        if (spell.getSchoolType() == SchoolRegistry.FIRE.get()) {
            return SpellTriggeredManaEvent.SpellTriggerType.FIRE;
        }
        
        // Check spell name
        String spellId = spell.getSpellId();
        if (spellId.contains("earth") || spellId.contains("stone") || spellId.contains("root")) {
            return SpellTriggeredManaEvent.SpellTriggerType.EARTH;
        }
        if (spellId.contains("teleport") || spellId.contains("blink")) {
            return SpellTriggeredManaEvent.SpellTriggerType.TELEPORT;
        }
        if (spellId.contains("water") || spellId.contains("ice")) {
            return SpellTriggeredManaEvent.SpellTriggerType.WATER;
        }
        if (spellId.contains("wind") || spellId.contains("air")) {
            return SpellTriggeredManaEvent.SpellTriggerType.WIND;
        }
        
        return null; // No trigger for this spell
    }
    
    /**
     * Check if a block is a Botania block entity
     */
    private static boolean isBotaniaBlock(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return false;
        
        String className = be.getClass().getName();
        return className.contains("vazkii.botania") || className.contains("botania");
    }
    
    /**
     * Represents an active modification to a Botania system
     */
    private static class ActiveModification {
        final SpellTriggeredManaEvent.SpellTriggerType type;
        final float intensity;
        final int duration;
        final long startTime;
        final long expiryTime;
        
        ActiveModification(SpellTriggeredManaEvent.SpellTriggerType type, 
                          float intensity, int duration, long currentTime) {
            this.type = type;
            this.intensity = intensity;
            this.duration = duration;
            this.startTime = currentTime;
            this.expiryTime = currentTime + duration;
        }
        
        boolean isExpired(long currentTime) {
            return currentTime >= expiryTime;
        }
    }
}
