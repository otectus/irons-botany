package com.ironsbotany.common.progression;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.ProgressionConfig;
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
 * Progression gates (upstream IDs resolved from {@link ProgressionConfig}):
 * - Terrasteel pickup → LEGENDARY-tier catalyst unlock
 * - Alfheim portal   → dual-school scroll casting unlock
 * - Gaia Guardian    → Spell Overcharge (+5% Nature damage)
 *
 * Reader-side gates live in {@link ProgressionGates}.
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UnifiedAdvancementSystem {

    /** Resolve a config'd advancement id. {@code null} if the config is empty or unparseable. */
    private static ResourceLocation resolve(String configValue) {
        if (configValue == null || configValue.isBlank()) return null;
        return ResourceLocation.tryParse(configValue);
    }

    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
        if (!ModList.get().isLoaded("botania")) return;

        ServerPlayer player = (ServerPlayer) event.getEntity();
        Advancement advancement = event.getAdvancement();
        ResourceLocation advId = advancement.getId();

        ResourceLocation terrasteel = resolve(ProgressionConfig.BOTANIA_TERRASTEEL_ADVANCEMENT.get());
        ResourceLocation alfheim = resolve(ProgressionConfig.BOTANIA_ALFHEIM_ADVANCEMENT.get());
        ResourceLocation gaia = resolve(ProgressionConfig.BOTANIA_GAIA_ADVANCEMENT.get());

        if (terrasteel != null && advId.equals(terrasteel)) {
            unlockTier4SpellModifiers(player);
        } else if (alfheim != null && advId.equals(alfheim)) {
            unlockDualSchoolCasting(player);
        } else if (gaia != null && advId.equals(gaia)) {
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
