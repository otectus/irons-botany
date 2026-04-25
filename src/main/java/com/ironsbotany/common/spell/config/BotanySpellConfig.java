package com.ironsbotany.common.spell.config;

import com.ironsbotany.IronsBotany;
import com.mojang.serialization.Codec;
import io.redspace.ironsspellbooks.api.config.RegisterConfigParametersEvent;
import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.config.SpellConfigParameter;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers Iron's Botany–specific {@link SpellConfigParameter}s with ISS so
 * server operators can override Botanical spell mana costs and dual-cost
 * gating per-spell via datapack JSON without modifying TOML or recompiling.
 *
 * <p>Datapack path: {@code data/<namespace>/spell_configs/<spell_id>.json}.
 * The values registered here become readable through
 * {@link SpellConfigManager#getSpellConfigValue} from any server-side code.
 *
 * <p>Defaults are intentionally sentinels (-1 for cost, true for the toggle):
 * a -1 cost means "use the value compiled into the spell class," letting the
 * existing {@code AbstractBotanicalSpell.getBotaniaManaCost(level)} ladder
 * remain authoritative until an operator opts into a per-spell override.
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class BotanySpellConfig {

    /** Per-cast Botania mana cost. -1 means "fall back to the spell's hard-coded ladder." */
    public static final SpellConfigParameter<Integer> BOTANIA_MANA_COST = new SpellConfigParameter<>(
            new ResourceLocation(IronsBotany.MODID, "botania_mana_cost"),
            Codec.INT,
            -1
    );

    /** Whether this spell consumes both ISS and Botania mana under HYBRID mode. */
    public static final SpellConfigParameter<Boolean> DUAL_COST_ENABLED = new SpellConfigParameter<>(
            new ResourceLocation(IronsBotany.MODID, "dual_cost_enabled"),
            Codec.BOOL,
            true
    );

    private BotanySpellConfig() {}

    @SubscribeEvent
    public static void onRegisterConfigParameters(RegisterConfigParametersEvent event) {
        event.register(BOTANIA_MANA_COST);
        event.register(DUAL_COST_ENABLED);
    }

    /**
     * Resolve the effective Botania cost for a spell at a given level.
     * Returns the datapack override if set; otherwise returns the
     * fallback the spell computed on its own.
     */
    public static int resolveBotaniaCost(AbstractSpell spell, int fallback) {
        Integer override = SpellConfigManager.getSpellConfigValue(spell, BOTANIA_MANA_COST);
        if (override == null || override < 0) return fallback;
        return override;
    }

    /** True if dual-cost mode applies for this spell (per-spell override). */
    public static boolean isDualCostEnabled(AbstractSpell spell) {
        Boolean v = SpellConfigManager.getSpellConfigValue(spell, DUAL_COST_ENABLED);
        return v == null ? true : v;
    }
}
