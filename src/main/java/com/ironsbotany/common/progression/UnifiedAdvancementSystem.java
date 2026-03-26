package com.ironsbotany.common.progression;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.util.DataKeys;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/**
 * Unified Advancement Chain - Weaves Botania and ISS progression together.
 * 
 * Progression gates:
 * - Unlock Terrasteel → Unlock Tier 4 Spell Modifier
 * - Unlock Alfheim → Unlock dual-school casting
 * - Defeat Gaia → Unlock spell overcharge mechanic
 * 
 * Makes both mods co-dependent evolutions of the same magical discipline.
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UnifiedAdvancementSystem {
    
    // Botania advancement keys
    private static final ResourceLocation BOTANIA_TERRASTEEL = 
        ResourceLocation.tryParse("botania:main/terrasteel_pickup");
    private static final ResourceLocation BOTANIA_ALFHEIM = 
        ResourceLocation.tryParse("botania:main/alfheim_portal_open");
    private static final ResourceLocation BOTANIA_GAIA = 
        ResourceLocation.tryParse("botania:challenge/gaia_guardian_kill");
    
    // ISS advancement keys (examples)
    private static final ResourceLocation ISS_FIRST_SPELL = 
        ResourceLocation.tryParse("irons_spellbooks:first_spell_cast");
    private static final ResourceLocation ISS_LEGENDARY_SPELL = 
        ResourceLocation.tryParse("irons_spellbooks:legendary_spell");
    
    /**
     * Track advancement completion and unlock cross-mod features
     */
    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!ModList.get().isLoaded("botania")) {
            return;
        }
        
        ServerPlayer player = (ServerPlayer) event.getEntity();
        Advancement advancement = event.getAdvancement();
        ResourceLocation advId = advancement.getId();
        
        // Terrasteel unlock → Tier 4 Spell Modifiers
        if (advId.equals(BOTANIA_TERRASTEEL)) {
            unlockTier4SpellModifiers(player);
        }
        
        // Alfheim unlock → Dual-school casting
        if (advId.equals(BOTANIA_ALFHEIM)) {
            unlockDualSchoolCasting(player);
        }
        
        // Gaia defeat → Spell overcharge
        if (advId.equals(BOTANIA_GAIA)) {
            unlockSpellOvercharge(player);
        }
    }
    
    /**
     * Unlock Tier 4 spell modifiers
     */
    private static void unlockTier4SpellModifiers(ServerPlayer player) {
        player.getPersistentData().putBoolean(DataKeys.TIER4_UNLOCKED, true);
        
        player.displayClientMessage(
            net.minecraft.network.chat.Component.translatable(
                "ironsbotany.progression.tier4_unlocked"
            ).withStyle(net.minecraft.ChatFormatting.GOLD),
            false
        );
        
        IronsBotany.LOGGER.info("Player {} unlocked Tier 4 spell modifiers", player.getName().getString());
    }
    
    /**
     * Unlock dual-school casting
     */
    private static void unlockDualSchoolCasting(ServerPlayer player) {
        player.getPersistentData().putBoolean(DataKeys.DUAL_SCHOOL_UNLOCKED, true);
        
        player.displayClientMessage(
            net.minecraft.network.chat.Component.translatable(
                "ironsbotany.progression.dual_school_unlocked"
            ).withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE),
            false
        );
        
        IronsBotany.LOGGER.info("Player {} unlocked dual-school casting", player.getName().getString());
    }
    
    /**
     * Unlock spell overcharge mechanic
     */
    private static void unlockSpellOvercharge(ServerPlayer player) {
        player.getPersistentData().putBoolean(DataKeys.OVERCHARGE_UNLOCKED, true);
        
        player.displayClientMessage(
            net.minecraft.network.chat.Component.translatable(
                "ironsbotany.progression.overcharge_unlocked"
            ).withStyle(net.minecraft.ChatFormatting.RED),
            false
        );
        
        IronsBotany.LOGGER.info("Player {} unlocked spell overcharge", player.getName().getString());
    }
    
    /**
     * Check if player has unlocked Tier 4 modifiers
     */
    public static boolean hasTier4Unlocked(Player player) {
        return player.getPersistentData().getBoolean(DataKeys.TIER4_UNLOCKED);
    }
    
    /**
     * Check if player has unlocked dual-school casting
     */
    public static boolean hasDualSchoolUnlocked(Player player) {
        return player.getPersistentData().getBoolean(DataKeys.DUAL_SCHOOL_UNLOCKED);
    }
    
    /**
     * Check if player has unlocked spell overcharge
     */
    public static boolean hasOverchargeUnlocked(Player player) {
        return player.getPersistentData().getBoolean(DataKeys.OVERCHARGE_UNLOCKED);
    }
}
