package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.item.ManaGeneratingWeapon;
import com.ironsbotany.common.util.ManaHelper;
import com.ironsbotany.common.registry.IBParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IronsBotany.MODID)
public class TerrasteelBladeHandler {

    // Per-player tick of the last mana-generation proc; gates autoclicker / attack-speed farming.
    private static final String LAST_GEN_TICK_KEY = "ironsbotany_blade_mana_tick";

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        ItemStack mainHand = player.getMainHandItem();
        if (!(mainHand.getItem() instanceof ManaGeneratingWeapon weapon)) return;

        // Only real, damage-dealing combat hits generate mana — never 0-damage /
        // fully-absorbed / cancelled swings.
        if (event.getAmount() <= 0.0f) return;

        // Internal cooldown: ignore procs that arrive faster than the configured gap,
        // so attack-speed mods and autoclickers can't farm the mana economy.
        int cooldown = CommonConfig.TERRASTEEL_BLADE_MANA_COOLDOWN.get();
        if (cooldown > 0) {
            long now = player.level().getGameTime();
            long last = player.getPersistentData().getLong(LAST_GEN_TICK_KEY);
            if (last != 0L && now - last < cooldown) return;
            player.getPersistentData().putLong(LAST_GEN_TICK_KEY, now);
        }

        // Generate Botania mana on melee hit (amount is per-weapon)
        int manaPerHit = weapon.getManaPerHit();
        if (manaPerHit > 0) {
            // Add mana to any mana-holding item in the player's inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                try {
                    if (vazkii.botania.api.mana.ManaItemHandler.instance()
                            .requestManaExact(stack, player, -manaPerHit, true)) {
                        break;
                    }
                } catch (Exception e) {
                    IronsBotany.LOGGER.debug("Mana operation on item {} failed: {}", stack.getItem(), e.getMessage());
                }
            }

            // Visual feedback
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    IBParticles.BOTANICAL_BURST.get(),
                    event.getEntity().getX(),
                    event.getEntity().getY() + event.getEntity().getBbHeight() / 2,
                    event.getEntity().getZ(),
                    8, 0.3, 0.3, 0.3, 0.2
                );
            }
        }
    }
}
