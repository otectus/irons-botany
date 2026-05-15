package com.ironsbotany.client;

import com.ironsbotany.common.block.entity.ManaConduitBlockEntity;
import com.ironsbotany.common.block.entity.SpellReservoirBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Client-side cache flagging whether any active Spell Reservoir or Mana
 * Conduit lives near the local player. Used by the HUD proximity pulse to
 * avoid the per-tick 13×13×13 cube walk the 1.7.0 audit flagged.
 *
 * <p>Updated at most every {@link #TTL_TICKS} ticks or whenever the player
 * moves more than {@link #MOVE_THRESHOLD_SQR} blocks² from the last scan
 * origin.</p>
 */
public final class NearbyIBBlockCache {
    private NearbyIBBlockCache() {}

    private static final int TTL_TICKS = 20;
    private static final double MOVE_THRESHOLD_SQR = 16.0; // 4 blocks

    private static long lastScanTick = Long.MIN_VALUE;
    private static BlockPos lastScanPos = BlockPos.ZERO;
    private static String lastDim = "";
    private static boolean nearActive = false;

    public static boolean hasNearbyActive(Player player, int radius) {
        Level level = player.level();
        if (level == null) return false;

        long now = level.getGameTime();
        BlockPos here = player.blockPosition();
        String dim = level.dimension().location().toString();

        boolean stale = !dim.equals(lastDim)
            || (now - lastScanTick) > TTL_TICKS
            || lastScanPos.distSqr(here) > MOVE_THRESHOLD_SQR;

        if (!stale) {
            return nearActive;
        }

        lastScanTick = now;
        lastScanPos = here.immutable();
        lastDim = dim;
        nearActive = scan(level, here, radius);
        return nearActive;
    }

    private static boolean scan(Level level, BlockPos origin, int radius) {
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-radius, -radius, -radius),
                origin.offset(radius, radius, radius))) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SpellReservoirBlockEntity r && r.getStoredMana() > 0) return true;
            if (be instanceof ManaConduitBlockEntity c && c.getStoredMana() > 0) return true;
        }
        return false;
    }
}
