package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.util.BotaniaIntegration;
import com.ironsbotany.common.util.DataKeys;
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
        if (BotaniaIntegration.isManaPool(be)) {
            CompoundTag tag = be.getPersistentData();
            tag.putFloat(DataKeys.THROUGHPUT_BOOST, mod.intensity);
            tag.putLong(DataKeys.BOOST_EXPIRY, mod.expiryTime);
        }
    }
    
    /**
     * Earth spell accelerates passive flower generation
     */
    private static void applyEarthAcceleration(BlockEntity be, ActiveModification mod) {
        if (BotaniaIntegration.isGeneratingFlower(be)) {
            CompoundTag tag = be.getPersistentData();
            tag.putFloat(DataKeys.GENERATION_BOOST, mod.intensity);
            tag.putLong(DataKeys.BOOST_EXPIRY, mod.expiryTime);
        }
    }
    
    /**
     * Nature spell boosts generating flower efficiency
     */
    private static void applyNatureBoost(BlockEntity be, ActiveModification mod) {
        if (BotaniaIntegration.isGeneratingFlower(be) || BotaniaIntegration.isFunctionalFlower(be)) {
            CompoundTag tag = be.getPersistentData();
            tag.putFloat(DataKeys.EFFICIENCY_BOOST, mod.intensity);
            tag.putLong(DataKeys.BOOST_EXPIRY, mod.expiryTime);
        }
    }
    
    /**
     * Fire spell increases Endoflame burn rate
     */
    private static void applyFireBoost(BlockEntity be, ActiveModification mod) {
        if (BotaniaIntegration.isGeneratingFlower(be)) {
            CompoundTag tag = be.getPersistentData();
            tag.putFloat(DataKeys.BURN_RATE_BOOST, mod.intensity);
            tag.putLong(DataKeys.BOOST_EXPIRY, mod.expiryTime);
        }
    }
    
    /**
     * Water spell fills mana pools
     */
    private static void applyWaterFill(BlockEntity be, ActiveModification mod) {
        if (BotaniaIntegration.isManaPool(be)) {
            // Direct API call -- this actually works unlike the NBT approach
            BotaniaIntegration.addPoolMana(be, (int)(1000 * mod.intensity));
        }
    }
    
    /**
     * Wind spell speeds up mana spreaders
     */
    private static void applyWindSpeed(BlockEntity be, ActiveModification mod) {
        if (BotaniaIntegration.isManaSpreader(be)) {
            CompoundTag tag = be.getPersistentData();
            tag.putFloat(DataKeys.SPEED_BOOST, mod.intensity);
            tag.putLong(DataKeys.BOOST_EXPIRY, mod.expiryTime);
        }
    }
    
    /**
     * Generic arcane resonance
     */
    private static void applyGenericResonance(BlockEntity be, ActiveModification mod) {
        CompoundTag tag = be.getPersistentData();
        tag.putFloat(DataKeys.ARCANE_RESONANCE, mod.intensity);
        tag.putLong(DataKeys.RESONANCE_EXPIRY, mod.expiryTime);
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
