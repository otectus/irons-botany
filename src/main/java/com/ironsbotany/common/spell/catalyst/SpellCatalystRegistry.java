package com.ironsbotany.common.spell.catalyst;

import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.config.ConfigHelper;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry for spell catalysts.
 * Manages which items provide which catalyst effects.
 */
public class SpellCatalystRegistry {
    private static final Map<Item, List<CatalystEffect>> CATALYST_EFFECTS = new HashMap<>();
    private static final Map<ResourceLocation, CatalystEffect> REGISTERED_EFFECTS = new HashMap<>();
    private static final Map<ResourceLocation, Item> CATALYST_ITEMS = new HashMap<>();
    
    /**
     * Register a catalyst effect for an item
     * @param item The Botania item that acts as catalyst
     * @param effect The effect it applies to spells
     */
    public static void registerCatalyst(Item item, CatalystEffect effect) {
        CATALYST_EFFECTS.computeIfAbsent(item, k -> new ArrayList<>()).add(effect);
        REGISTERED_EFFECTS.put(effect.getId(), effect);
        CATALYST_ITEMS.put(effect.getId(), item);
    }

    /**
     * Get the item associated with a catalyst effect (for consumption)
     */
    public static Item getItemForCatalyst(CatalystEffect effect) {
        return CATALYST_ITEMS.get(effect.getId());
    }
    
    /**
     * Get all catalyst effects from player's inventory
     * @param player The player casting the spell
     * @return List of active catalyst effects
     */
    public static List<CatalystEffect> getActiveCatalysts(Player player) {
        // Check master toggles
        if (CommonConfig.BARE_BONES_MODE.get() || 
            !CommonConfig.ENABLE_DEEP_SYNERGY.get() ||
            !CommonConfig.ENABLE_SPELL_CATALYSTS.get()) {
            return new ArrayList<>();
        }
        
        List<CatalystEffect> effects = new ArrayList<>();
        int maxCatalysts = CommonConfig.MAX_CATALYSTS_PER_SPELL.get();
        
        // Check main inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (effects.size() >= maxCatalysts) break;
            
            ItemStack stack = player.getInventory().getItem(i);
            List<CatalystEffect> itemEffects = CATALYST_EFFECTS.get(stack.getItem());
            if (itemEffects != null) {
                for (CatalystEffect effect : itemEffects) {
                    if (effects.size() < maxCatalysts) {
                        effects.add(effect);
                    }
                }
            }
        }
        
        // Check curios slots
        if (effects.size() < maxCatalysts) {
            effects.addAll(getCuriosCatalysts(player, maxCatalysts - effects.size()));
        }
        
        return effects;
    }
    
    /**
     * Apply catalyst effects to a spell context
     * @param spell The spell being cast
     * @param context The spell context to modify
     * @param catalysts Active catalysts
     */
    public static void applyCatalysts(AbstractSpell spell, SpellContext context, 
                                      List<CatalystEffect> catalysts) {
        if (!ConfigHelper.areCatalystsEnabled()) {
            return;
        }
        
        boolean allowMultiple = CommonConfig.ALLOW_MULTIPLE_CATALYSTS.get();
        double powerMultiplier = CommonConfig.CATALYST_POWER_MULTIPLIER.get();
        
        for (CatalystEffect catalyst : catalysts) {
            if (catalyst.appliesTo(spell)) {
                // Store original multipliers to apply power multiplier
                float originalDamage = context.getDamageMultiplier();
                
                catalyst.modifySpell(spell, context);
                
                // Apply global power multiplier
                if (powerMultiplier != 1.0) {
                    float newDamage = context.getDamageMultiplier();
                    float damageChange = newDamage - originalDamage;
                    context.multiplyDamage(1.0f + (float)((damageChange) * (powerMultiplier - 1.0)));
                }
                
                if (!allowMultiple) {
                    break; // Only apply first matching catalyst
                }
            }
        }
    }
    
    /**
     * Get catalyst effects from Curios slots
     */
    private static List<CatalystEffect> getCuriosCatalysts(Player player, int maxCount) {
        List<CatalystEffect> effects = new ArrayList<>();
        
        try {
            List<SlotResult> curios = CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findCurios(stack -> CATALYST_EFFECTS.containsKey(stack.getItem())))
                .orElse(new ArrayList<>());
            
            for (SlotResult result : curios) {
                if (effects.size() >= maxCount) break;
                
                List<CatalystEffect> itemEffects = CATALYST_EFFECTS.get(result.stack().getItem());
                if (itemEffects != null) {
                    for (CatalystEffect effect : itemEffects) {
                        if (effects.size() < maxCount) {
                            effects.add(effect);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Curios not available or error occurred
        }
        
        return effects;
    }
    
    /**
     * Get a registered catalyst effect by ID
     */
    public static CatalystEffect getEffect(ResourceLocation id) {
        return REGISTERED_EFFECTS.get(id);
    }
    
    /**
     * Check if an item has catalyst effects
     */
    public static boolean hasCatalyst(Item item) {
        return CATALYST_EFFECTS.containsKey(item);
    }
    
    /**
     * Get all registered catalyst effects
     */
    public static Map<ResourceLocation, CatalystEffect> getAllEffects() {
        return new HashMap<>(REGISTERED_EFFECTS);
    }
}
