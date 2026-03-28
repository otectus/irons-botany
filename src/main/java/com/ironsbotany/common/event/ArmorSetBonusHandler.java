package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.item.ManasteelWizardArmorItem;
import com.ironsbotany.common.util.ManaHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArmorSetBonusHandler {

    private static final int MANA_PER_DAMAGE = 10000; // 10k Botania mana per heart absorbed
    private static final int COOLDOWN_TICKS = 40; // 2-second internal cooldown
    private static final String LAST_SHIELD_TIME_KEY = "IronsBotany_ManaShieldCooldown";

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        // Check if player has full Manasteel Wizard Armor set
        if (!hasFullManasteelSet(player)) return;

        // Check internal cooldown
        long lastProc = player.getPersistentData().getLong(LAST_SHIELD_TIME_KEY);
        long currentTime = player.level().getGameTime();
        if (currentTime - lastProc < COOLDOWN_TICKS) return;

        float damage = event.getAmount();
        float absorbAmount = damage * 0.5f; // Absorb 50% of damage

        // Calculate mana cost
        int manaCost = (int) (absorbAmount * MANA_PER_DAMAGE);

        // Try to drain Botania mana
        if (ManaHelper.drainBotaniaMana(player, manaCost)) {
            // Reduce damage by 50%
            event.setAmount(damage * 0.5f);
            player.getPersistentData().putLong(LAST_SHIELD_TIME_KEY, currentTime);
            
            // Grant mana_shield advancement
            if (player instanceof ServerPlayer serverPlayer) {
                var advancement = serverPlayer.server.getAdvancements()
                    .getAdvancement(new ResourceLocation(IronsBotany.MODID, "mana_shield"));
                if (advancement != null) {
                    serverPlayer.getAdvancements().award(advancement, "shield_proc");
                }
            }

            // Spawn particles
            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.ENCHANT,
                    player.getX(),
                    player.getY() + 1,
                    player.getZ(),
                    10, 0.5, 0.5, 0.5, 0.1
                );
            }
        }
    }
    
    private static boolean hasFullManasteelSet(Player player) {
        int count = 0;
        
        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.getItem() instanceof ManasteelWizardArmorItem) {
                count++;
            }
        }
        
        return count == 4;
    }
}
