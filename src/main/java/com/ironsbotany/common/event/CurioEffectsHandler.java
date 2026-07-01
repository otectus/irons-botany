package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.item.GaiasBlessingItem;
import com.ironsbotany.common.registry.IBItems;
import com.ironsbotany.common.registry.IBSchools;
import io.redspace.ironsspellbooks.api.events.ModifySpellLevelEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import vazkii.botania.api.mana.ManaPool;

/**
 * Forge-bus subscriber that powers curio side-effects which can't be
 * expressed as static attribute modifiers — currently just Gaia's
 * Blessing.
 *
 * <p>Subscribes at default priority. Iron's Botany doesn't otherwise
 * touch {@code ModifySpellLevelEvent}, so priority isn't a concern.
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID)
public final class CurioEffectsHandler {

    // Per-cast-tick payment marker. ModifySpellLevelEvent can fire many times per cast
    // (and in non-cast level queries), so we must grant +1 on every firing for a
    // consistent effective level, but drain the pool only ONCE per (tick, spell).
    private static final String KEY_PAID_TICK = "ironsbotany_gaia_blessing_paid_tick";
    private static final String KEY_PAID_SPELL = "ironsbotany_gaia_blessing_paid_spell";

    private CurioEffectsHandler() {}

    @SubscribeEvent
    public static void onModifySpellLevel(ModifySpellLevelEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (event.getSpell().getSchoolType() != IBSchools.BOTANY.get()) return;

        // Find a Gaia's Blessing in the caster's curio slots.
        boolean wearingBlessing = CuriosApi.getCuriosHelper()
                .findFirstCurio(player, IBItems.GAIAS_BLESSING.get())
                .isPresent();
        if (!wearingBlessing) return;

        long tick = player.level().getGameTime();
        int spellHash = event.getSpell().getSpellId().hashCode();
        net.minecraft.nbt.CompoundTag data = player.getPersistentData();

        // Already paid for this (tick, spell): grant the level again without re-draining.
        if (data.contains(KEY_PAID_TICK)
                && data.getLong(KEY_PAID_TICK) == tick
                && data.getInt(KEY_PAID_SPELL) == spellHash) {
            event.addLevels(1);
            return;
        }

        // First firing this cast-tick: pay the cost from a nearby mana pool.
        // If no pool can cover it, fizzle silently (no level, no marker).
        if (!drainNearbyPool(player, GaiasBlessingItem.MANA_PER_CAST, GaiasBlessingItem.POOL_SCAN_RADIUS)) {
            return;
        }
        data.putLong(KEY_PAID_TICK, tick);
        data.putInt(KEY_PAID_SPELL, spellHash);
        event.addLevels(1);
    }

    /**
     * Search the surrounding area for a {@link ManaPool} block entity
     * with at least {@code amount} mana and drain it.
     *
     * @return true if the full amount was drained
     */
    private static boolean drainNearbyPool(LivingEntity entity, int amount, int radius) {
        Level level = entity.level();
        BlockPos origin = entity.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-radius, -radius / 2, -radius),
                origin.offset(radius, radius / 2, radius))) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ManaPool pool && pool.getCurrentMana() >= amount) {
                pool.receiveMana(-amount);
                return true;
            }
        }
        return false;
    }
}
