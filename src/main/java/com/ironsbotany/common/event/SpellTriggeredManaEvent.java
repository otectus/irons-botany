package com.ironsbotany.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

/**
 * Event fired when a spell triggers a Botania mana system modification.
 * This allows spells to affect Botania infrastructure in real-time.
 */
public class SpellTriggeredManaEvent extends Event {
    private final Level level;
    private final BlockPos position;
    private final SpellTriggerType triggerType;
    private final float intensity;
    
    public SpellTriggeredManaEvent(Level level, BlockPos position, 
                                   SpellTriggerType triggerType, float intensity) {
        this.level = level;
        this.position = position;
        this.triggerType = triggerType;
        this.intensity = intensity;
    }
    
    public Level getLevel() {
        return level;
    }
    
    public BlockPos getPosition() {
        return position;
    }
    
    public SpellTriggerType getTriggerType() {
        return triggerType;
    }
    
    public float getIntensity() {
        return intensity;
    }
    
    /**
     * Types of spell triggers that can affect Botania systems
     */
    public enum SpellTriggerType {
        LIGHTNING,      // Boosts mana pool throughput
        EARTH,          // Accelerates passive flower generation
        NATURE,         // Boosts generating flower efficiency
        FIRE,           // Increases Endoflame burn rate
        TELEPORT,       // Redirects mana bursts
        WATER,          // Fills mana pools
        WIND,           // Speeds up mana spreaders
        ARCANE          // Generic magical resonance
    }
}
