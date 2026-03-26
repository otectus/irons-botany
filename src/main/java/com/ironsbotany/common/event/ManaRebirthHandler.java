package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.util.DataKeys;
import com.ironsbotany.common.util.ManaHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IronsBotany.MODID)
public class ManaRebirthHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (!CommonConfig.MANA_REBIRTH_DEATH_PREVENTION.get()) return;

        // Check if player has an active Mana Rebirth buff
        if (!player.getPersistentData().contains(DataKeys.MANA_REBIRTH_EXPIRY)) return;

        long expiry = player.getPersistentData().getLong(DataKeys.MANA_REBIRTH_EXPIRY);
        if (player.level().getGameTime() > expiry) {
            // Buff expired, clean up
            player.getPersistentData().remove(DataKeys.MANA_REBIRTH_LEVEL);
            player.getPersistentData().remove(DataKeys.MANA_REBIRTH_EXPIRY);
            return;
        }

        // Check and consume Botania mana
        int manaCost = CommonConfig.MANA_REBIRTH_REVIVE_MANA_COST.get();
        if (!ManaHelper.hasBotaniaMana(player, manaCost)) return;
        if (!ManaHelper.drainBotaniaMana(player, manaCost)) return;

        // Cancel death
        event.setCanceled(true);

        // Heal to configured percentage of max HP
        int spellLevel = player.getPersistentData().getInt(DataKeys.MANA_REBIRTH_LEVEL);
        float revivePercent = (float) (double) CommonConfig.MANA_REBIRTH_REVIVE_HEALTH_PERCENT.get();
        float reviveHealth = player.getMaxHealth() * (revivePercent + (spellLevel * 0.05f));
        player.setHealth(Math.min(reviveHealth, player.getMaxHealth()));

        // Remove the buff (one-time use)
        player.getPersistentData().remove(DataKeys.MANA_REBIRTH_LEVEL);
        player.getPersistentData().remove(DataKeys.MANA_REBIRTH_EXPIRY);

        // Remove only harmful effects, preserving beneficial ones
        player.getActiveEffects().stream()
                .filter(effect -> !effect.getEffect().isBeneficial())
                .map(net.minecraft.world.effect.MobEffectInstance::getEffect)
                .toList()
                .forEach(player::removeEffect);

        // Visual and audio feedback
        if (player.level() instanceof ServerLevel serverLevel) {
            // Black Lotus / dark rebirth particles
            serverLevel.sendParticles(
                ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1, player.getZ(),
                100, 0.5, 1.0, 0.5, 0.5
            );
            serverLevel.sendParticles(
                ParticleTypes.SOUL,
                player.getX(), player.getY() + 1, player.getZ(),
                30, 0.3, 0.5, 0.3, 0.1
            );
            serverLevel.sendParticles(
                ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 1, player.getZ(),
                50, 1.0, 1.0, 1.0, 0.5
            );

            serverLevel.playSound(null, player.blockPosition(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 0.8f);
        }
    }
}
