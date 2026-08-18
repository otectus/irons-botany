package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.util.BotaniaIntegration;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
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

    /** Dimension-aware block position key to prevent cross-dimension collisions */
    private record DimBlockPos(ResourceKey<Level> dimension, BlockPos pos) {}

    // Track active modifications keyed by dimension + position
    private static final Map<DimBlockPos, ActiveModification> ACTIVE_MODIFICATIONS = new ConcurrentHashMap<>();

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
        ACTIVE_MODIFICATIONS.put(new DimBlockPos(level.dimension(), pos), mod);
    }

    /**
     * Clear every tracked modification when the server stops.
     *
     * <p>The map is static, so without this a single-player session that quits to the title screen
     * and loads a different save would carry the previous world's pending modifications into it.
     */
    @SubscribeEvent
    public static void onServerStopped(net.minecraftforge.event.server.ServerStoppedEvent event) {
        int pending = ACTIVE_MODIFICATIONS.size();
        ACTIVE_MODIFICATIONS.clear();
        if (pending > 0) {
            IronsBotany.LOGGER.debug("Cleared {} pending mana-network modification(s) on server stop", pending);
        }
    }

    /**
     * Apply modifications to Botania block entities
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!ModList.get().isLoaded("botania")) return;

        Iterator<Map.Entry<DimBlockPos, ActiveModification>> iterator =
            ACTIVE_MODIFICATIONS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<DimBlockPos, ActiveModification> entry = iterator.next();
            DimBlockPos key = entry.getKey();
            ActiveModification mod = entry.getValue();

            // Resolve the record's own dimension first: expiry must be judged against the clock
            // of the world the modification lives in, not the overworld's. Vanilla shares game
            // time across dimensions, but a mod-added dimension need not, and 1.9.0 compared every
            // record against overworld time regardless.
            ServerLevel level = event.getServer().getLevel(key.dimension());
            if (level == null) {
                // The dimension is gone (unloaded, or removed from the pack). The record can never
                // be applied or expire on its own clock again, so drop it rather than leak it.
                iterator.remove();
                continue;
            }

            if (mod.isExpired(level.getGameTime())) {
                iterator.remove();
                continue;
            }

            {
                BlockEntity be = level.getBlockEntity(key.pos());
                if (be != null) {
                    applyModification(be, mod);
                }
            }
        }
    }
    
    /**
     * Apply modification to a Botania block entity.
     * Currently only WATER trigger has a real effect (direct pool.receiveMana API call).
     * Other trigger types (LIGHTNING, EARTH, NATURE, FIRE, WIND, ARCANE) previously wrote
     * custom NBT tags to Botania block entities, but Botania never reads those tags.
     * TODO: Implement other trigger types via Botania API when feasible.
     */
    private static void applyModification(BlockEntity blockEntity, ActiveModification mod) {
        try {
            if (mod.type == SpellTriggeredManaEvent.SpellTriggerType.WATER) {
                applyWaterFill(blockEntity, mod);
            }
            // Other trigger types are not yet functional — see TODO above
        } catch (Exception e) {
            IronsBotany.LOGGER.debug("Could not apply modification to {}: {}",
                blockEntity.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * Water spell fills mana pools via direct Botania API call
     */
    private static void applyWaterFill(BlockEntity be, ActiveModification mod) {
        if (BotaniaIntegration.isManaPool(be)) {
            BotaniaIntegration.addPoolMana(be, (int)(1000 * mod.intensity));
        }
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
