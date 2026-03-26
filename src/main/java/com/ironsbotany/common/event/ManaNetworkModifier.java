package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles spell-triggered modifications to Botania mana networks.
 * Spells can now affect Botania infrastructure in real-time.
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ManaNetworkModifier {
    
    // Track active modifications
    private static final Map<BlockPos, ActiveModification> ACTIVE_MODIFICATIONS = new ConcurrentHashMap<>();
    
    /**
     * Register a spell-triggered modification
     */
    public static void registerModification(Level level, BlockPos pos, 
                                           SpellTriggeredManaEvent.SpellTriggerType type, 
                                           float intensity, int duration) {
        if (!ModList.get().isLoaded("botania")) {
            return;
        }
        
        ActiveModification mod = new ActiveModification(type, intensity, duration, level.getGameTime());
        ACTIVE_MODIFICATIONS.put(pos, mod);
    }
    
    /**
     * Apply modifications to Botania block entities
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!ModList.get().isLoaded("botania")) return;
        
        Iterator<Map.Entry<BlockPos, ActiveModification>> iterator = 
            ACTIVE_MODIFICATIONS.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, ActiveModification> entry = iterator.next();
            BlockPos pos = entry.getKey();
            ActiveModification mod = entry.getValue();
            
            // Check if modification expired
            if (mod.isExpired(event.getServer().overworld().getGameTime())) {
                iterator.remove();
                continue;
            }
            
            // Apply modification to block entity
            for (ServerLevel level : event.getServer().getAllLevels()) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be != null) {
                    applyModification(be, mod);
                }
            }
        }
    }
    
    /**
     * Apply modification to a Botania block entity
     */
    private static void applyModification(BlockEntity blockEntity, ActiveModification mod) {
        try {
            String blockName = blockEntity.getClass().getSimpleName();
            
            switch (mod.type) {
                case LIGHTNING -> applyLightningBoost(blockEntity, mod);
                case EARTH -> applyEarthAcceleration(blockEntity, mod);
                case NATURE -> applyNatureBoost(blockEntity, mod);
                case FIRE -> applyFireBoost(blockEntity, mod);
                case WATER -> applyWaterFill(blockEntity, mod);
                case WIND -> applyWindSpeed(blockEntity, mod);
                default -> applyGenericResonance(blockEntity, mod);
            }
        } catch (Exception e) {
            // Safe failure - log and continue
            IronsBotany.LOGGER.debug("Could not apply modification to {}: {}", 
                blockEntity.getClass().getSimpleName(), e.getMessage());
        }
    }
    
    /**
     * Lightning spell boosts mana pool throughput
     */
    private static void applyLightningBoost(BlockEntity be, ActiveModification mod) {
        // Check if it's a mana pool
        if (be.getClass().getSimpleName().contains("Pool")) {
            CompoundTag tag = be.getPersistentData();
            tag.putFloat("IronsBotany_ThroughputBoost", mod.intensity);
            tag.putLong("IronsBotany_BoostExpiry", mod.expiryTime);
        }
    }
    
    /**
     * Earth spell accelerates passive flower generation
     */
    private static void applyEarthAcceleration(BlockEntity be, ActiveModification mod) {
        // Check if it's a generating flower
        if (be.getClass().getSimpleName().contains("Flower") || 
            be.getClass().getSimpleName().contains("Generating")) {
            CompoundTag tag = be.getPersistentData();
            tag.putFloat("IronsBotany_GenerationBoost", mod.intensity);
            tag.putLong("IronsBotany_BoostExpiry", mod.expiryTime);
        }
    }
    
    /**
     * Nature spell boosts generating flower efficiency
     */
    private static void applyNatureBoost(BlockEntity be, ActiveModification mod) {
        if (be.getClass().getSimpleName().contains("Flower")) {
            CompoundTag tag = be.getPersistentData();
            tag.putFloat("IronsBotany_EfficiencyBoost", mod.intensity);
            tag.putLong("IronsBotany_BoostExpiry", mod.expiryTime);
        }
    }
    
    /**
     * Fire spell increases Endoflame burn rate
     */
    private static void applyFireBoost(BlockEntity be, ActiveModification mod) {
        if (be.getClass().getSimpleName().contains("Endoflame")) {
            CompoundTag tag = be.getPersistentData();
            tag.putFloat("IronsBotany_BurnRateBoost", mod.intensity);
            tag.putLong("IronsBotany_BoostExpiry", mod.expiryTime);
        }
    }
    
    /**
     * Water spell fills mana pools
     */
    private static void applyWaterFill(BlockEntity be, ActiveModification mod) {
        if (be.getClass().getSimpleName().contains("Pool")) {
            CompoundTag tag = be.getPersistentData();
            tag.putInt("IronsBotany_ManaToAdd", (int)(1000 * mod.intensity));
        }
    }
    
    /**
     * Wind spell speeds up mana spreaders
     */
    private static void applyWindSpeed(BlockEntity be, ActiveModification mod) {
        if (be.getClass().getSimpleName().contains("Spreader")) {
            CompoundTag tag = be.getPersistentData();
            tag.putFloat("IronsBotany_SpeedBoost", mod.intensity);
            tag.putLong("IronsBotany_BoostExpiry", mod.expiryTime);
        }
    }
    
    /**
     * Generic arcane resonance
     */
    private static void applyGenericResonance(BlockEntity be, ActiveModification mod) {
        CompoundTag tag = be.getPersistentData();
        tag.putFloat("IronsBotany_ArcaneResonance", mod.intensity);
        tag.putLong("IronsBotany_ResonanceExpiry", mod.expiryTime);
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
