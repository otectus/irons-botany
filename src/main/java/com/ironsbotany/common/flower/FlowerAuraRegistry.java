package com.ironsbotany.common.flower;

import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.config.ConfigHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for flower auras.
 * Manages which flowers provide which aura effects.
 */
public class FlowerAuraRegistry {
    private static final Map<Block, FlowerAura> FLOWER_AURAS = new HashMap<>();
    private static final Map<ResourceLocation, FlowerAura> REGISTERED_AURAS = new HashMap<>();
    
    private static final int CACHE_DURATION = 40; // 2 seconds

    /** Re-scan once the player has moved this far from where the cached result was taken. */
    private static final int CACHE_MOVE_THRESHOLD = 4;

    /**
     * Vertical half-height of the scan box, regardless of the horizontal radius.
     *
     * <p>Flowers grow on the ground, so scanning the full cubic volume spends most of its budget on
     * air far above and bedrock far below. Capping the vertical extent at 6 turns a radius-16 query
     * from 33x33x33 = 35 937 block reads into 33x13x33 = 14 157 — the same auras, well under half
     * the work.
     */
    private static final int VERTICAL_RADIUS = 6;

    /**
     * Cached aura scans.
     *
     * <p>Keyed by <em>player and radius</em>, and validated against dimension, time and position.
     * Through 1.9.0 the key was the player UUID alone while callers requested radius 8 (Corporea
     * reagents) and radius 16 (spell casting, Gaia trials) — so whichever query ran first served
     * its result to the other, and a cached radius-8 scan could hide auras a radius-16 caller
     * should have seen. Dimension was not checked either, so walking through a portal kept the
     * previous world's results until they aged out.
     */
    private static final Map<CacheKey, CachedAuraData> AURA_CACHE = new ConcurrentHashMap<>();

    private record CacheKey(UUID playerId, int radius) {}

    private record CachedAuraData(List<ActiveFlowerAura> auras, long lastUpdate,
                                  ResourceLocation dimension, BlockPos origin) {

        boolean isValid(long now, ResourceLocation currentDimension, BlockPos currentPos) {
            return now - lastUpdate < CACHE_DURATION
                    && now >= lastUpdate                       // guard a rewound world clock
                    && dimension.equals(currentDimension)
                    && origin.distSqr(currentPos) <= (long) CACHE_MOVE_THRESHOLD * CACHE_MOVE_THRESHOLD;
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
        
        // The world's own clock, not the overworld's — a cache entry made in the Nether must not
        // be judged against overworld time.
        long currentTime = player.level().getGameTime();
        ResourceLocation dimension = player.level().dimension().location();
        BlockPos position = player.blockPosition();
        CacheKey key = new CacheKey(player.getUUID(), radius);

        CachedAuraData cached = AURA_CACHE.get(key);
        if (cached != null && cached.isValid(currentTime, dimension, position)) {
            return new ArrayList<>(cached.auras());
        }

        List<ActiveFlowerAura> auras = scanForAuras(player, radius);
        AURA_CACHE.put(key, new CachedAuraData(auras, currentTime, dimension, position));

        // Return a copy, not the cached list itself — a caller mutating the result must
        // not corrupt the cache (the cache-hit path above already copies).
        return new ArrayList<>(auras);
    }

    /**
     * Scan the area around {@code player} for aura-providing flowers.
     *
     * <h3>Deterministic selection</h3>
     * Every candidate in range is collected first, then sorted by strength (strongest first),
     * distance (nearest first) and finally position, and only then truncated to
     * {@code MAX_ACTIVE_AURAS}. Through 1.9.0 the scan stopped the moment it had collected the
     * maximum, so which auras a player actually benefited from was decided by
     * {@code BlockPos.betweenClosed} iteration order — standing in the same spot facing the same
     * flowers could favour a distant weak flower over the strong one at the player's feet, and the
     * result changed if the player moved one block.
     */
    private static List<ActiveFlowerAura> scanForAuras(Player player, int radius) {
        BlockPos playerPos = player.blockPosition();
        Level level = player.level();

        double rangeMultiplier = CommonConfig.FLOWER_AURA_RANGE_MULTIPLIER.get();
        double strengthMultiplier = CommonConfig.FLOWER_AURA_STRENGTH_MULTIPLIER.get();
        int maxAuras = Math.max(0, CommonConfig.MAX_ACTIVE_AURAS.get());
        if (maxAuras == 0 || FLOWER_AURAS.isEmpty()) return List.of();

        int vertical = Math.min(radius, VERTICAL_RADIUS);
        List<ActiveFlowerAura> candidates = new ArrayList<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                // Never load a chunk to answer a proximity query. An unloaded chunk simply has no
                // auras as far as this scan is concerned.
                if (!level.hasChunkAt(cursor.set(playerPos.getX() + dx, playerPos.getY(), playerPos.getZ() + dz))) {
                    continue;
                }
                for (int dy = -vertical; dy <= vertical; dy++) {
                    cursor.set(playerPos.getX() + dx, playerPos.getY() + dy, playerPos.getZ() + dz);

                    BlockState state = level.getBlockState(cursor);
                    FlowerAura aura = FLOWER_AURAS.get(state.getBlock());
                    if (aura == null) continue;

                    double distance = Math.sqrt(playerPos.distSqr(cursor));
                    if (distance > aura.getRange() * rangeMultiplier) continue;

                    float strength = (float) (aura.calculateStrength(distance) * strengthMultiplier);
                    candidates.add(new ActiveFlowerAura(aura, cursor.immutable(), strength));
                }
            }
        }

        if (candidates.size() > maxAuras) {
            candidates.sort(Comparator
                    .comparingDouble((ActiveFlowerAura a) -> -a.getStrength())
                    .thenComparingDouble(a -> playerPos.distSqr(a.getPosition()))
                    .thenComparing(a -> a.getPosition().asLong()));
            return new ArrayList<>(candidates.subList(0, maxAuras));
        }
        return candidates;
    }

    /** Drop every cached scan for one player, across all radii. */
    public static void invalidateCache(UUID playerUUID) {
        AURA_CACHE.keySet().removeIf(key -> key.playerId().equals(playerUUID));
    }

    /**
     * Drop every cached scan.
     *
     * <p>Called on logout, dimension change and server stop as well as by the reload command.
     * The entries are only a performance shortcut, so clearing them can never lose state — but
     * leaving them behind across a world unload would let one save's results answer another's.
     */
    public static void cleanupCache() {
        AURA_CACHE.clear();
    }

    /** Visible for diagnostics and tests. */
    public static int cachedEntryCount() {
        return AURA_CACHE.size();
    }
    
    /**
     * Get all registered auras
     */
    public static Map<ResourceLocation, FlowerAura> getAllAuras() {
        return new HashMap<>(REGISTERED_AURAS);
    }
}
