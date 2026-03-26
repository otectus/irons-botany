package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.util.DataKeys;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IronsBotany.MODID)
public class FlowerShieldHandler {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        // Check for active Flower Shield
        if (!player.getPersistentData().contains(DataKeys.FLOWER_SHIELD_HP)) return;

        long expiry = player.getPersistentData().getLong(DataKeys.FLOWER_SHIELD_EXPIRY);
        if (player.level().getGameTime() > expiry) {
            // Shield expired, clean up
            player.getPersistentData().remove(DataKeys.FLOWER_SHIELD_HP);
            player.getPersistentData().remove(DataKeys.FLOWER_SHIELD_EXPIRY);
            return;
        }

        int shieldHp = player.getPersistentData().getInt(DataKeys.FLOWER_SHIELD_HP);
        if (shieldHp <= 0) {
            player.getPersistentData().remove(DataKeys.FLOWER_SHIELD_HP);
            player.getPersistentData().remove(DataKeys.FLOWER_SHIELD_EXPIRY);
            return;
        }

        float incomingDamage = event.getAmount();
        float absorbed = Math.min(incomingDamage, shieldHp);
        float remainingDamage = incomingDamage - absorbed;
        int remainingShield = shieldHp - (int) Math.ceil(absorbed);

        if (remainingShield <= 0) {
            // Shield broke
            player.getPersistentData().remove(DataKeys.FLOWER_SHIELD_HP);
            player.getPersistentData().remove(DataKeys.FLOWER_SHIELD_EXPIRY);

            // Shield break visual
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    ParticleTypes.CHERRY_LEAVES,
                    player.getX(), player.getY() + 1, player.getZ(),
                    60, 1.0, 1.0, 1.0, 0.3
                );
                serverLevel.sendParticles(
                    ParticleTypes.POOF,
                    player.getX(), player.getY() + 1, player.getZ(),
                    10, 0.5, 0.5, 0.5, 0.1
                );
                serverLevel.playSound(null, player.blockPosition(),
                    SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8f, 1.5f);
            }
        } else {
            // Shield absorbing damage
            player.getPersistentData().putInt(DataKeys.FLOWER_SHIELD_HP, remainingShield);

            // Hit feedback particles
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    ParticleTypes.CHERRY_LEAVES,
                    player.getX(), player.getY() + 1, player.getZ(),
                    15, 0.5, 0.5, 0.5, 0.15
                );
                serverLevel.playSound(null, player.blockPosition(),
                    SoundEvents.AZALEA_LEAVES_HIT, SoundSource.PLAYERS, 0.6f, 1.2f);
            }
        }

        // Reduce or cancel the damage
        if (remainingDamage <= 0) {
            event.setCanceled(true);
        } else {
            event.setAmount(remainingDamage);
        }
    }
}
