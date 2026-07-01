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
 * <p>Overrides are supplied through ISS's own spell-config datapack mechanism
 * (its {@code SpellConfigManager} subconfig folder), keyed by these parameter
 * ids ({@code ironsbotany:botania_mana_cost}, {@code ironsbotany:dual_cost_enabled}).
 * The values registered here become readable through
 * {@link SpellConfigManager#getSpellConfigValue} from any server-side code.
 *
 * <p>Defaults are intentionally sentinels (-1 for cost, true for the toggle):
 * a -1 cost means "use the value compiled into the spell class," letting the
 * existing {@code AbstractBotanicalSpell.getBotaniaManaCost(level)} ladder
 * remain authoritative until an operator opts into a per-spell override.
 */
// FORGE bus: ISS posts RegisterConfigParametersEvent on MinecraftForge.EVENT_BUS
// (from SpellConfigManager.registerConfigParameterTypes at datapack/world-load time),
// not the MOD bus. Subscribing on MOD would silently never fire.
@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class BotanySpellConfig {

    // These are intentionally NOT initialized at class-load time. Forge's
    // AutomaticEventSubscriber does Class.forName on this @EventBusSubscriber class
    // during the CONSTRUCT phase, and constructing a SpellConfigParameter there would
    // trigger ISS's SpellConfigParameter.<clinit>, which reads SchoolRegistry.REGISTRY.get()
    // — null before registries are built → NPE crash. Assigning them inside the event
    // handler (which fires after registries exist) keeps the class-load harmless.

    /** Per-cast Botania mana cost. -1 means "fall back to the spell's hard-coded ladder." */
    public static SpellConfigParameter<Integer> BOTANIA_MANA_COST;

    /** Whether this spell consumes both ISS and Botania mana under HYBRID mode. */
    public static SpellConfigParameter<Boolean> DUAL_COST_ENABLED;

    private BotanySpellConfig() {}

    @SubscribeEvent
    public static void onRegisterConfigParameters(RegisterConfigParametersEvent event) {
        BOTANIA_MANA_COST = new SpellConfigParameter<>(
                new ResourceLocation(IronsBotany.MODID, "botania_mana_cost"),
                Codec.INT,
                -1
        );
        DUAL_COST_ENABLED = new SpellConfigParameter<>(
                new ResourceLocation(IronsBotany.MODID, "dual_cost_enabled"),
                Codec.BOOL,
                true
        );
        event.register(BOTANIA_MANA_COST);
        event.register(DUAL_COST_ENABLED);
    }

    /**
     * Resolve the effective Botania cost for a spell at a given level.
     * Returns the datapack override if set; otherwise returns the
     * fallback the spell computed on its own.
     */
    public static int resolveBotaniaCost(AbstractSpell spell, int fallback) {
        if (BOTANIA_MANA_COST == null) return fallback; // params not yet registered
        try {
            Integer override = SpellConfigManager.getSpellConfigValue(spell, BOTANIA_MANA_COST);
            if (override == null || override < 0) return fallback;
            return override;
        } catch (Throwable t) {
            // ISS's SpellConfigManager reads a static INSTANCE.config with no null check;
            // a call before the config reload listener has run (e.g. client-side pre-sync)
            // can NPE. Fall back to the compiled ladder rather than propagate.
            return fallback;
        }
    }

    /** True if dual-cost mode applies for this spell (per-spell override). */
    public static boolean isDualCostEnabled(AbstractSpell spell) {
        if (DUAL_COST_ENABLED == null) return true; // params not yet registered
        try {
            Boolean v = SpellConfigManager.getSpellConfigValue(spell, DUAL_COST_ENABLED);
            return v == null ? true : v;
        } catch (Throwable t) {
            return true; // see resolveBotaniaCost: guard against ISS INSTANCE not yet set
        }
    }
}
