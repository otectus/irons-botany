package com.ironsbotany.common.flower;

import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.config.ConfigHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for flower auras.
 * Manages which flowers provide which aura effects.
 */
public class FlowerAuraRegistry {
    private static final Map<Block, FlowerAura> FLOWER_AURAS = new HashMap<>();
    private static final Map<ResourceLocation, FlowerAura> REGISTERED_AURAS = new HashMap<>();
    
    // Cache for performance
    private static final Map<UUID, CachedAuraData> AURA_CACHE = new ConcurrentHashMap<>();
    private static final int CACHE_DURATION = 40; // 2 seconds
    
    private static class CachedAuraData {
        List<ActiveFlowerAura> auras;
        long lastUpdate;
        
        boolean isValid(long currentTime) {
            return (currentTime - lastUpdate) < CACHE_DURATION;
        }
    }
    
    public static void registerFlowerAura(Block flower, FlowerAura aura) {
        FLOWER_AURAS.put(flower, aura);
        REGISTERED_AURAS.put(aura.getId(), aura);
    }
    
    public static FlowerAura getAura(Block flower) {
        return FLOWER_AURAS.get(flower);
    }
    
    /**
     * Get all active flower auras affecting a player
     * @param player The player to check
     * @param radius Search radius
     * @return List of active auras
     */
    public static List<ActiveFlowerAura> getActiveAuras(Player player, int radius) {
        // Check master toggles using ConfigHelper
        if (!ConfigHelper.areAurasEnabled()) {
            return new ArrayList<>();
        }
        
        UUID playerUUID = player.getUUID();
        long currentTime = player.level().getGameTime();
        
        // Check cache
        CachedAuraData cached = AURA_CACHE.get(playerUUID);
        if (cached != null && cached.isValid(currentTime)) {
            return new ArrayList<>(cached.auras);
        }
        
        // Rebuild cache
        List<ActiveFlowerAura> auras = scanForAuras(player, radius);
        
        cached = new CachedAuraData();
        cached.auras = auras;
        cached.lastUpdate = currentTime;
        AURA_CACHE.put(playerUUID, cached);
        
        return auras;
    }
    
    private static List<ActiveFlowerAura> scanForAuras(Player player, int radius) {
        List<ActiveFlowerAura> auras = new ArrayList<>();
        BlockPos playerPos = player.blockPosition();
        Level level = player.level();
        
        double rangeMultiplier = CommonConfig.FLOWER_AURA_RANGE_MULTIPLIER.get();
        int maxAuras = CommonConfig.MAX_ACTIVE_AURAS.get();
        
        // Search for flowers in radius
        for (BlockPos pos : BlockPos.betweenClosed(
            playerPos.offset(-radius, -radius, -radius),
            playerPos.offset(radius, radius, radius))) {
            
            if (auras.size() >= maxAuras) break;
            
            BlockState state = level.getBlockState(pos);
            FlowerAura aura = FLOWER_AURAS.get(state.getBlock());
            
            if (aura != null) {
                double distance = Math.sqrt(playerPos.distSqr(pos));
                double effectiveRange = aura.getRange() * rangeMultiplier;
                
                if (distance <= effectiveRange) {
                    // Calculate strength based on distance
                    float strength = aura.calculateStrength(distance);
                    strength *= CommonConfig.FLOWER_AURA_STRENGTH_MULTIPLIER.get().floatValue();
                    
                    auras.add(new ActiveFlowerAura(aura, pos.immutable(), strength));
                }
            }
        }
        
        return auras;
    }
    
    /**
     * Invalidate cache for a player
     */
    public static void invalidateCache(UUID playerUUID) {
        AURA_CACHE.remove(playerUUID);
    }
    
    /**
     * Clean up stale cache entries
     */
    public static void cleanupCache() {
        AURA_CACHE.clear();
    }
    
    /**
     * Get all registered auras
     */
    public static Map<ResourceLocation, FlowerAura> getAllAuras() {
        return new HashMap<>(REGISTERED_AURAS);
    }
}
