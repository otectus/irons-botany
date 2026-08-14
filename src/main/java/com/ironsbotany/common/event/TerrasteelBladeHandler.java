package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.item.ManaGeneratingWeapon;
import com.ironsbotany.common.bridge.mana.BotaniaManaGateway;
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

        // Internal cooldown: ignore procs that arrive faster than the configured gap, so
        // attack-speed mods and autoclickers can't farm the mana economy. The previous value is
        // kept so a proc that stores nothing can restore it rather than burning the window.
        int cooldown = CommonConfig.TERRASTEEL_BLADE_MANA_COOLDOWN.get();
        long lastProcTick = player.getPersistentData().getLong(LAST_GEN_TICK_KEY);
        if (cooldown > 0) {
            long now = player.level().getGameTime();
            if (lastProcTick != 0L && now - lastProcTick < cooldown) return;
            player.getPersistentData().putLong(LAST_GEN_TICK_KEY, now);
        }

        int manaPerHit = weapon.getManaPerHit();
        if (manaPerHit <= 0) return;

        // Supported additive dispatch. 1.9.0 called
        //     requestManaExact(stack, player, -manaPerHit, true)
        // and relied on Botania's Math.min(negative, mana) flowing into addMana(-drain) to *add*
        // mana. That worked only by accident of the arithmetic; it credited some item other than
        // the one being iterated, because Botania skips the stack passed to it by identity; and it
        // reported success without proving anything had been accepted, so the particle burst and
        // the internal cooldown fired even when every item was already full.
        int accepted = BotaniaManaGateway.dispatchMana(player, manaPerHit);
        if (accepted <= 0) {
            // Nothing was stored — do not consume the proc window or show feedback for a no-op.
            if (cooldown > 0) {
                player.getPersistentData().putLong(LAST_GEN_TICK_KEY, lastProcTick);
            }
            return;
        }

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
