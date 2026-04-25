package com.ironsbotany.common.compat;

import com.ironsbotany.IronsBotany;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Soft-integration shim for Ars 'n' Spells. All calls are reflective and
 * fail-soft: if ANS is absent, misversioned, or has renamed internals, each
 * probe returns a neutral value and logs once. IB never requires ANS on the
 * classpath.
 *
 * Only ANS mode that needs special handling is ARS_PRIMARY — in that mode
 * ANS's MixinIronsMagicDataMana redirects ISS MagicData get/set/addMana to
 * the Ars pool, which means IB writes land in Ars units and IB's ISS-max
 * clamp reads the wrong attribute.
 */
public final class ArsNSpellsCompat {

    private static final String ANS_MODID = "ars_n_spells";
    private static final String BRIDGE_MANAGER_CLASS = "com.otectus.arsnspells.bridge.BridgeManager";
    private static final String ANS_CONFIG_CLASS = "com.otectus.arsnspells.config.AnsConfig";

    private static Boolean loadedCache;
    private static boolean reflectionFailed;

    private static Method getCurrentModeMethod;
    private static Method getBridgeMethod;
    private static Method bridgeGetMaxManaMethod;
    private static Field conversionRateField;
    private static Method configValueGetMethod;

    private ArsNSpellsCompat() {}

    public static boolean isLoaded() {
        if (loadedCache == null) {
            try {
                loadedCache = ModList.get() != null && ModList.get().isLoaded(ANS_MODID);
            } catch (Throwable t) {
                loadedCache = Boolean.FALSE;
            }
        }
        return loadedCache;
    }

    /**
     * Returns ANS's current mana_unification_mode as its enum name
     * ("ISS_PRIMARY", "ARS_PRIMARY", "HYBRID", "SEPARATE", "DISABLED"), or
     * null if ANS isn't loaded or reflection failed.
     */
    public static String getActiveMode() {
        if (!isLoaded() || reflectionFailed) return null;
        try {
            if (getCurrentModeMethod == null) {
                Class<?> cls = Class.forName(BRIDGE_MANAGER_CLASS);
                getCurrentModeMethod = cls.getMethod("getCurrentMode");
            }
            Object mode = getCurrentModeMethod.invoke(null);
            return mode == null ? null : ((Enum<?>) mode).name();
        } catch (Throwable t) {
            markFailed("getActiveMode", t);
            return null;
        }
    }

    public static boolean isArsPrimary() {
        return isLoaded() && "ARS_PRIMARY".equals(getActiveMode());
    }

    /**
     * True if Iron's Botany should yield cost routing to Ars 'n Spells
     * for this cast. Returns true when ANS is loaded and currently in a
     * mode where ANS owns the player-side mana pool.
     *
     * <p>This check is consulted by {@link com.ironsbotany.common.bridge.ManaBridgeManager}
     * before charging Botania, so a player running both bridges only ever
     * pays once per cast.
     */
    public static boolean shouldDeferRouting() {
        if (!isLoaded()) return false;
        String mode = getActiveMode();
        return "ARS_PRIMARY".equals(mode) || "HYBRID".equals(mode);
    }

    /**
     * Effective max mana for the player — honours ANS's bridge under
     * ARS_PRIMARY, falls back to ISS MAX_MANA attribute otherwise.
     */
    public static float getEffectiveMaxMana(Player player) {
        if (isArsPrimary()) {
            float bridged = getMaxManaViaBridge(player);
            if (!Float.isNaN(bridged)) return bridged;
        }
        return (float) player.getAttributeValue(AttributeRegistry.MAX_MANA.get());
    }

    /**
     * Returns BridgeManager.getBridge().getMaxMana(player) via reflection,
     * or Float.NaN on any failure.
     */
    public static float getMaxManaViaBridge(Player player) {
        if (!isLoaded() || reflectionFailed) return Float.NaN;
        try {
            if (getBridgeMethod == null) {
                Class<?> cls = Class.forName(BRIDGE_MANAGER_CLASS);
                getBridgeMethod = cls.getMethod("getBridge");
            }
            Object bridge = getBridgeMethod.invoke(null);
            if (bridge == null) return Float.NaN;
            if (bridgeGetMaxManaMethod == null) {
                bridgeGetMaxManaMethod = bridge.getClass().getMethod("getMaxMana", Player.class);
            }
            Object result = bridgeGetMaxManaMethod.invoke(bridge, player);
            return result instanceof Float f ? f : Float.NaN;
        } catch (Throwable t) {
            markFailed("getMaxManaViaBridge", t);
            return Float.NaN;
        }
    }

    /**
     * Reflectively reads AnsConfig.CONVERSION_RATE_IRON_TO_ARS (a
     * ForgeConfigSpec.DoubleValue). Defaults to 1.0f on failure.
     */
    public static float getIronToArsConversionRate() {
        if (!isLoaded() || reflectionFailed) return 1.0f;
        try {
            if (conversionRateField == null) {
                Class<?> cls = Class.forName(ANS_CONFIG_CLASS);
                conversionRateField = cls.getField("CONVERSION_RATE_IRON_TO_ARS");
            }
            Object value = conversionRateField.get(null);
            if (value == null) return 1.0f;
            if (configValueGetMethod == null) {
                configValueGetMethod = value.getClass().getMethod("get");
            }
            Object rate = configValueGetMethod.invoke(value);
            if (rate instanceof Double d) return d.floatValue();
            if (rate instanceof Number n) return n.floatValue();
            return 1.0f;
        } catch (Throwable t) {
            markFailed("getIronToArsConversionRate", t);
            return 1.0f;
        }
    }

    private static void markFailed(String probe, Throwable t) {
        if (!reflectionFailed) {
            reflectionFailed = true;
            IronsBotany.LOGGER.warn(
                "Ars 'n' Spells integration disabled: reflective probe '{}' failed ({}). "
                    + "IB will behave as if ANS is absent.",
                probe, t.toString());
        }
    }
}
