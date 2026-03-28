package com.ironsbotany.common.boss;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.util.DataKeys;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gaia Guardian Spell Trials - Boss validates magical mastery across both systems.
 * 
 * Phase 1: Requires environmental spell usage (flower auras)
 * Phase 2: Counters specific spell schools
 * Drops: Spell catalysts and glyph fragments
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GaiaSpellTrials {
    
    private static final Map<UUID, GaiaTrialData> ACTIVE_TRIALS = new HashMap<>();
    
    /**
     * Track Gaia Guardian fight data
     */
    @SubscribeEvent
    public static void onGaiaHurt(LivingHurtEvent event) {
        if (!ModList.get().isLoaded("botania")) {
            return;
        }
        
        // Check if entity is Gaia Guardian
        if (!isGaiaGuardian(event.getEntity())) {
            return;
        }
        
        // Check if damage source is a player with spell
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        
        LivingEntity gaia = event.getEntity();
        GaiaTrialData trialData = ACTIVE_TRIALS.computeIfAbsent(
            gaia.getUUID(), 
            k -> new GaiaTrialData()
        );
        
        // Determine Gaia phase based on health
        int phase = getGaiaPhase(gaia);
        
        // Apply phase-specific mechanics
        if (phase == 1) {
            applyPhase1Mechanics(event, player, trialData);
        } else if (phase == 2) {
            applyPhase2Mechanics(event, player, trialData);
        }
    }
    
    /**
     * Phase 1: Requires environmental spell usage
     */
    private static void applyPhase1Mechanics(LivingHurtEvent event, Player player, GaiaTrialData trialData) {
        // Check if player has active flower auras nearby (direct query, not stale flag)
        boolean hasActiveAuras = !com.ironsbotany.common.flower.FlowerAuraRegistry
            .getActiveAuras(player, 16).isEmpty();
        
        if (!hasActiveAuras) {
            // Reduce damage if not using environmental magic
            event.setAmount(event.getAmount() * 0.5f);
            
            if (player.level().getGameTime() % 100 == 0) {
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                        "ironsbotany.gaia.phase1_hint"
                    ), true
                );
            }
        } else {
            // Bonus damage for using environmental magic
            event.setAmount(event.getAmount() * 1.5f);
            trialData.environmentalSpellsUsed++;
        }
    }
    
    /**
     * Phase 2: Counters specific spell schools
     */
    private static void applyPhase2Mechanics(LivingHurtEvent event, Player player, GaiaTrialData trialData) {
        // Get last spell cast by player
        AbstractSpell lastSpell = getLastSpellCast(player);
        if (lastSpell == null) {
            return;
        }
        
        SchoolType school = lastSpell.getSchoolType();
        
        // Gaia resists certain schools in Phase 2
        if (school == SchoolRegistry.ICE.get()) {
            event.setAmount(event.getAmount() * 0.3f); // 70% resistance to ice
            trialData.resistedSchools.add("ice");
        } else if (school == SchoolRegistry.NATURE.get()) {
            event.setAmount(event.getAmount() * 1.25f); // Vulnerable to Nature
            trialData.botanicalSpellsUsed++;
        } else if (school == SchoolRegistry.FIRE.get()) {
            event.setAmount(event.getAmount() * 1.25f); // Vulnerable to Fire
        }
        
        // Hint system
        if (trialData.resistedSchools.size() >= 2 && player.level().getGameTime() % 100 == 0) {
            player.displayClientMessage(
                net.minecraft.network.chat.Component.translatable(
                    "ironsbotany.gaia.phase2_hint"
                ), true
            );
        }
    }
    
    /**
     * Check if entity is Gaia Guardian
     */
    private static boolean isGaiaGuardian(LivingEntity entity) {
        net.minecraft.resources.ResourceLocation entityId =
            net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return entityId != null && entityId.toString().contains("gaia_guardian");
    }
    
    /**
     * Get Gaia phase based on health
     */
    private static int getGaiaPhase(LivingEntity gaia) {
        float healthPercent = gaia.getHealth() / gaia.getMaxHealth();
        if (healthPercent > 0.5f) {
            return 1;
        } else {
            return 2;
        }
    }
    
    /**
     * Get last spell cast by player
     */
    private static AbstractSpell getLastSpellCast(Player player) {
        // Check if the player is currently casting via ISS
        MagicData magicData = MagicData.getPlayerMagicData(player);
        if (magicData.isCasting()) {
            String spellId = magicData.getCastingSpellId();
            if (spellId != null && !spellId.isEmpty()) {
                return SpellRegistry.getSpell(spellId);
            }
        }

        // Fall back to stored last-cast data from AbstractBotanicalSpell
        CompoundTag pdata = player.getPersistentData();
        if (pdata.contains(DataKeys.LAST_SPELL_ID)) {
            long castTime = pdata.getLong(DataKeys.LAST_SPELL_TIME);
            // Only consider spells cast within last 5 seconds (100 ticks)
            if (player.level().getGameTime() - castTime < 100) {
                String spellId = pdata.getString(DataKeys.LAST_SPELL_ID);
                return SpellRegistry.getSpell(spellId);
            }
        }

        return null;
    }
    
    /**
     * Trial data for a Gaia fight
     */
    private static class GaiaTrialData {
        int environmentalSpellsUsed = 0;
        int botanicalSpellsUsed = 0;
        java.util.Set<String> resistedSchools = new java.util.HashSet<>();
        long startTime = System.currentTimeMillis();
    }
}
