package com.ironsbotany.common.alfheim;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.spell.catalyst.SpellContext;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Alfheim spell boost system.
 * Players casting spells near an active Alfheim portal receive scaling power boosts.
 * Botania 1.20.1 does not have an Alfheim dimension — boosts are proximity-based.
 */
public class AlfheimSpellBoost {

    private static final int PORTAL_SEARCH_RADIUS = 16;
    private static final ResourceLocation ALFHEIM_PORTAL_ID =
        new ResourceLocation("botania", "alfheim_portal");

    /**
     * Check if player is near an Alfheim portal
     * @return distance to nearest portal, or -1 if none found
     */
    public static double distanceToAlfheimPortal(Player player) {
        if (!ModList.get().isLoaded("botania")) {
            return -1;
        }

        Block portalBlock = ForgeRegistries.BLOCKS.getValue(ALFHEIM_PORTAL_ID);
        if (portalBlock == null) {
            return -1;
        }

        Level level = player.level();
        BlockPos playerPos = player.blockPosition();
        double closestDistSq = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-PORTAL_SEARCH_RADIUS, -PORTAL_SEARCH_RADIUS / 2, -PORTAL_SEARCH_RADIUS),
                playerPos.offset(PORTAL_SEARCH_RADIUS, PORTAL_SEARCH_RADIUS / 2, PORTAL_SEARCH_RADIUS))) {
            if (level.getBlockState(pos).is(portalBlock)) {
                double distSq = playerPos.distSqr(pos);
                if (distSq < closestDistSq) {
                    closestDistSq = distSq;
                }
            }
        }

        return closestDistSq < Double.MAX_VALUE ? Math.sqrt(closestDistSq) : -1;
    }

    /**
     * Check if player is near an Alfheim portal (convenience method)
     */
    public static boolean isNearAlfheimPortal(Player player) {
        return distanceToAlfheimPortal(player) >= 0;
    }

    /**
     * Apply Alfheim spell boost to context, scaled by distance to portal
     */
    public static void applyAlfheimBoost(SpellContext context, AbstractSpell spell, Player player) {
        double distance = distanceToAlfheimPortal(player);
        if (distance < 0) {
            return;
        }

        // Scale boost by proximity (full at distance 0, fading to 0 at PORTAL_SEARCH_RADIUS)
        float proximityFactor = Math.max(0, 1.0f - (float)(distance / PORTAL_SEARCH_RADIUS));
        
        // Scale boost by proximity and spell type
        float powerBoost = getAlfheimPowerBoost(spell) * proximityFactor;
        context.multiplyDamage(1.0f + powerBoost);

        // Reduce cooldowns (scaled by proximity)
        context.multiplyCooldown(1.0f - (0.2f * proximityFactor)); // Up to -20% cooldown

        // Increase range (scaled by proximity)
        context.multiplyRange(1.0f + (0.3f * proximityFactor)); // Up to +30% range

        // Add Alfheim resonance flags
        context.setCustomData("alfheim_resonance", true);
        context.setCustomData("alfheim_power_boost", powerBoost);

        IronsBotany.LOGGER.debug("Applied Alfheim portal boost to {}: +{}% power (proximity: {}%)",
            spell.getSpellId(), (int)(powerBoost * 100), (int)(proximityFactor * 100));
    }
    
    /**
     * Calculate Alfheim power boost based on spell type
     */
    private static float getAlfheimPowerBoost(AbstractSpell spell) {
        String spellId = spell.getSpellId();
        
        // Botanical spells get massive boost in Alfheim
        if (spellId.contains("botanical") || spellId.contains("bloom") || 
            spellId.contains("petal") || spellId.contains("root")) {
            return 0.5f; // +50% power
        }
        
        // Nature spells get good boost
        if (spellId.contains("nature") || spellId.contains("earth") || 
            spellId.contains("plant")) {
            return 0.35f; // +35% power
        }
        
        // All other spells get moderate boost
        return 0.25f; // +25% power
    }
    
    /**
     * Check if a spell can only reach full power in Alfheim
     */
    public static boolean requiresAlfheim(AbstractSpell spell) {
        // Ultimate Botanical spells require Alfheim
        return spell.getSpellId().contains("gaia");
    }
}
