package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.compat.ArsNSpellsCompat;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.item.DreamwoodScepterItem;
import com.ironsbotany.common.util.ManaHelper;
import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import com.ironsbotany.common.registry.IBParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IronsBotany.MODID)
public class DreamwoodConversionHandler {

    // Run at LOW so HIGHEST-priority cancellers (cooldowns, LP shortfalls, etc.
    // from ISS add-ons like Ars 'n' Spells) get first refusal before we
    // drain Botania mana.
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onSpellPreCast(SpellPreCastEvent event) {
        if (event.isCanceled()) return;
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        // Check if player is holding Dreamwood Scepter
        boolean hasScepter = player.getMainHandItem().getItem() instanceof DreamwoodScepterItem
            || player.getOffhandItem().getItem() instanceof DreamwoodScepterItem;

        if (!hasScepter) return;

        double conversionPercent = CommonConfig.DREAMWOOD_CONVERSION_PERCENT.get();
        if (conversionPercent <= 0) return;

        // Look up the spell and its mana cost
        AbstractSpell spell = SpellRegistry.getSpell(event.getSpellId());
        if (spell == null) return;

        int spellManaCost = spell.getManaCost(event.getSpellLevel());
        int manaToConvert = (int) (spellManaCost * conversionPercent);

        if (manaToConvert <= 0) return;

        // Convert ISS mana cost to Botania mana cost
        int botaniaEquivalent = ManaHelper.convertISSToBotania(manaToConvert);
        if (botaniaEquivalent <= 0) return;

        // Check if player has enough Botania mana
        if (!ManaHelper.hasBotaniaMana(player, botaniaEquivalent)) return;

        // Drain Botania mana
        if (!ManaHelper.drainBotaniaMana(player, botaniaEquivalent)) return;

        // Pre-fund ISS mana so the spell can consume it instead.
        // Under Ars 'n' Spells ARS_PRIMARY mode, ANS's mixin redirects this
        // addMana call into the Ars pool verbatim (no rate applied), so we
        // must scale by ANS's ISS->Ars conversion rate up-front.
        MagicData magicData = MagicData.getPlayerMagicData(player);
        if (magicData != null) {
            int adjusted = manaToConvert;
            if (ArsNSpellsCompat.isArsPrimary()) {
                float rate = ArsNSpellsCompat.getIronToArsConversionRate();
                adjusted = Math.max(1, Math.round(manaToConvert * rate));
            }
            magicData.addMana(adjusted);
        }

        // Visual feedback
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                IBParticles.MANA_TRANSFER.get(),
                player.getX(), player.getY() + 1, player.getZ(),
                10, 0.3, 0.5, 0.3, 0.2
            );
        }
    }
}
