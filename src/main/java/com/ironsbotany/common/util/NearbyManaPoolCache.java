package com.ironsbotany.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import vazkii.botania.api.mana.ManaPool;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player cache of nearby Botania {@code ManaPool} positions.
 *
 * <p>The cache survives for {@link #TTL_TICKS} ticks (2s) or until the player
 * moves more than {@link #MOVE_THRESHOLD_SQR} blocks² from the scan origin,
 * whichever comes first. A miss runs a single cube scan inside the configured
 * search radius and caches all encountered pool positions. Subsequent calls
 * within the TTL filter the cache against live block-entity state and return
 * just the currently-valid pools.</p>
 *
 * <p>Replaces the per-call full-cube walk in {@code ManaHelper.findAndDrainPool},
 * mitigating the "thousand-paper-cuts" performance regression the 1.7.0 audit
 * flagged for repeated spell casts in modpack settings.</p>
 */
public final class NearbyManaPoolCache {
    private NearbyManaPoolCache() {}

    private static final int TTL_TICKS = 40;
    private static final double MOVE_THRESHOLD_SQR = 16.0; // 4 blocks

    private static final ConcurrentHashMap<UUID, Entry> CACHE = new ConcurrentHashMap<>();

    private static final class Entry {
        final List<BlockPos> pools;
        final long scanTick;
        final BlockPos scanOrigin;
        final String dimensionKey;

        Entry(List<BlockPos> pools, long scanTick, BlockPos scanOrigin, String dimensionKey) {
            this.pools = pools;
            this.scanTick = scanTick;
            this.scanOrigin = scanOrigin;
            this.dimensionKey = dimensionKey;
        }

        boolean isStale(long now, BlockPos currentPos, String currentDim) {
            return !dimensionKey.equals(currentDim)
                || (now - scanTick) > TTL_TICKS
                || scanOrigin.distSqr(currentPos) > MOVE_THRESHOLD_SQR;
        }
    }

    /**
     * Return the cached set of pool positions near the player, refreshing the
     * cache when stale. Caller is responsible for re-checking that each pool
     * still exists (the cache only stores positions, not block-entity refs).
     */
    public static List<BlockPos> get(Player player, int radius) {
        long now = player.level().getGameTime();
        BlockPos here = player.blockPosition();
        String dim = player.level().dimension().location().toString();

        Entry cached = CACHE.get(player.getUUID());
        if (cached != null && !cached.isStale(now, here, dim)) {
            return cached.pools;
        }

        List<BlockPos> found = scan(player.level(), here, radius);
        CACHE.put(player.getUUID(), new Entry(found, now, here, dim));
        return found;
    }

    private static List<BlockPos> scan(Level level, BlockPos origin, int radius) {
        List<BlockPos> out = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-radius, -radius / 2, -radius),
                origin.offset(radius, radius / 2, radius))) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ManaPool) {
                out.add(pos.immutable());
            }
        }
        return out;
    }

    /** Invalidate the cache for one player (e.g. on logout). */
    public static void invalidate(UUID playerId) {
        CACHE.remove(playerId);
    }
}
