package com.ironsbotany.common.spell;

import com.ironsbotany.common.event.ManaNetworkModifier;
import com.ironsbotany.common.event.SpellTriggeredManaEvent;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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

        // Only WATER trigger has a real effect (direct pool.receiveMana API call).
        // Other trigger types wrote NBT tags that Botania never reads — skip the
        // expensive cuboid scan for those until proper implementations are added.
        if (triggerType != SpellTriggeredManaEvent.SpellTriggerType.WATER) {
            return;
        }

        // Find nearby Botania mana pools and apply water fill
        int radius = DEFAULT_SEARCH_RADIUS + spellLevel;
        for (BlockPos pos : BlockPos.betweenClosed(
            casterPos.offset(-radius, -radius / 2, -radius),
            casterPos.offset(radius, radius / 2, radius))) {

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof vazkii.botania.api.mana.ManaPool) {
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
}
