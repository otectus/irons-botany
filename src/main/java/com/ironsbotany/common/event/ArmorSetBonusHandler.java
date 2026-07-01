package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.item.MageArmorItem;
import com.ironsbotany.common.util.MageArmorSets;
import com.ironsbotany.common.util.ManaHelper;
import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Mage-armor set bonuses across the Manasteel → Elementium → Terrasteel → Gaia
 * ladder. Each tier has a distinct identity:
 *
 * <ul>
 *   <li><b>Manasteel</b> (entry): a small Botania spell-cost discount, applied in
 *       {@code ManaBridgeManager} via {@link MageArmorSets#applyBotaniaDiscount}
 *       (no handler here).</li>
 *   <li><b>Elementium</b> (mid): chance to refund part of a spell's mana cost
 *       ({@link #onSpellCast}).</li>
 *   <li><b>Terrasteel</b> (late): incoming damage reduction while ISS mana is high
 *       — a battlemage identity ({@link #onLivingHurt}).</li>
 *   <li><b>Gaia</b> (endgame): the strong Mana Shield that absorbs damage by
 *       spending Botania mana — moved here from the old Manasteel set
 *       ({@link #onLivingHurt}).</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArmorSetBonusHandler {

    private static final int GAIA_MANA_PER_DAMAGE = 10000; // Botania mana per point of damage absorbed
    private static final int GAIA_COOLDOWN_TICKS = 40;     // 2-second internal cooldown
    private static final String GAIA_SHIELD_TIME_KEY = "IronsBotany_ManaShieldCooldown";

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        // Gaia set — Mana Shield: absorb a fraction of damage by spending Botania mana.
        if (MageArmorSets.hasFullTier(player, MageArmorItem.Tier.GAIA)) {
            long lastProc = player.getPersistentData().getLong(GAIA_SHIELD_TIME_KEY);
            long now = player.level().getGameTime();
            if (now - lastProc >= GAIA_COOLDOWN_TICKS) {
                float damage = event.getAmount();
                double absorbPct = CommonConfig.GAIA_SET_ABSORB_PERCENT.get();
                int manaCost = (int) (damage * absorbPct * GAIA_MANA_PER_DAMAGE);
                if (manaCost > 0 && ManaHelper.drainBotaniaMana(player, manaCost)) {
                    event.setAmount((float) (damage * (1.0 - absorbPct)));
                    player.getPersistentData().putLong(GAIA_SHIELD_TIME_KEY, now);
                    awardManaShield(player);
                    spawnShieldParticles(player);
                    return; // Gaia shield consumed this hit
                }
            }
        }

        // Terrasteel set — battlemage: reduce incoming damage while ISS mana is above the threshold.
        if (MageArmorSets.hasFullTier(player, MageArmorItem.Tier.TERRASTEEL)) {
            MagicData magicData = MagicData.getPlayerMagicData(player);
            if (magicData != null) {
                float maxMana = (float) player.getAttributeValue(AttributeRegistry.MAX_MANA.get());
                double threshold = CommonConfig.TERRASTEEL_SET_MANA_THRESHOLD.get();
                if (maxMana > 0 && magicData.getMana() >= maxMana * threshold) {
                    double reduction = CommonConfig.TERRASTEEL_SET_DAMAGE_REDUCTION.get();
                    event.setAmount((float) (event.getAmount() * (1.0 - reduction)));
                }
            }
        }
    }

    /**
     * Elementium set — chance to refund part of a spell's mana cost. Fires on
     * {@link SpellOnCastEvent}, which exposes the cost before it is debited, so
     * lowering it here is a clean partial refund.
     *
     * <p>This intentionally stacks with the Elementium Wand's "Elven Favor"
     * ({@code ElementiumWandHandler}); a full Elementium loadout is meant to be
     * maximally mana-efficient.
     */
    @SubscribeEvent
    public static void onSpellCast(SpellOnCastEvent event) {
        Player player = event.getEntity();
        if (player == null || player.level().isClientSide) return;
        if (!MageArmorSets.hasFullTier(player, MageArmorItem.Tier.ELEMENTIUM)) return;

        double chance = CommonConfig.ELEMENTIUM_SET_REFUND_CHANCE.get();
        if (chance <= 0) return;
        if (player.getRandom().nextDouble() < chance) {
            int cost = event.getManaCost();
            if (cost > 0) {
                // Refund 25% of the cost.
                int refunded = Math.max(1, cost / 4);
                event.setManaCost(Math.max(0, cost - refunded));
            }
        }
    }

    private static void awardManaShield(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            var advancement = serverPlayer.server.getAdvancements()
                    .getAdvancement(new ResourceLocation(IronsBotany.MODID, "mana_shield"));
            if (advancement != null) {
                serverPlayer.getAdvancements().award(advancement, "shield_proc");
            }
        }
    }

    private static void spawnShieldParticles(Player player) {
        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    com.ironsbotany.common.registry.IBParticles.MANA_TRANSFER.get(),
                    player.getX(), player.getY() + 1, player.getZ(),
                    10, 0.5, 0.5, 0.5, 0.1);
        }
    }
}
