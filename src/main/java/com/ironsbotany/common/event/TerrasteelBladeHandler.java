package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.item.TerrasteelSpellBladeItem;
import com.ironsbotany.common.util.ManaHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IronsBotany.MODID)
public class TerrasteelBladeHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        ItemStack mainHand = player.getMainHandItem();
        if (!(mainHand.getItem() instanceof TerrasteelSpellBladeItem)) return;

        // Generate Botania mana on melee hit
        int manaPerHit = CommonConfig.TERRASTEEL_BLADE_MANA_PER_HIT.get();
        if (manaPerHit > 0) {
            // Add mana to any mana-holding item in the player's inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                try {
                    if (vazkii.botania.api.mana.ManaItemHandler.instance()
                            .requestManaExact(stack, player, -manaPerHit, true)) {
                        break;
                    }
                } catch (Exception ignored) {
                    // Item doesn't support mana operations
                }
            }

            // Visual feedback
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    ParticleTypes.ENCHANT,
                    event.getEntity().getX(),
                    event.getEntity().getY() + event.getEntity().getBbHeight() / 2,
                    event.getEntity().getZ(),
                    8, 0.3, 0.3, 0.3, 0.2
                );
            }
        }
    }
}
