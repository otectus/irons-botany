package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.item.BotanicalUpgradeOrbItem;
import com.ironsbotany.common.registry.IBAttributes;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class UpgradeOrbHandler {
    
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        
        // Check if right item is a Botanical Upgrade Orb
        if (!(right.getItem() instanceof BotanicalUpgradeOrbItem orb)) return;
        
        // Check if left item is equipment (has attributes)
        if (!hasAttributes(left)) return;
        
        // Create result
        ItemStack result = left.copy();
        CompoundTag tag = result.getOrCreateTag();
        
        // Track applied orbs
        if (!tag.contains("AppliedOrbs")) {
            tag.put("AppliedOrbs", new ListTag());
        }
        
        ListTag appliedOrbs = tag.getList("AppliedOrbs", 8); // 8 = String type
        String orbType = orb.getOrbType();
        
        // Check if this orb type already applied
        for (int i = 0; i < appliedOrbs.size(); i++) {
            if (appliedOrbs.getString(i).equals(orbType)) {
                return; // Already applied
            }
        }
        
        // Apply orb effect
        appliedOrbs.add(net.minecraft.nbt.StringTag.valueOf(orbType));
        tag.put("AppliedOrbs", appliedOrbs);
        
        // Add attribute modifiers based on orb type
        applyOrbBonus(result, orbType);
        
        // Set anvil result
        event.setOutput(result);
        event.setCost(5);
        event.setMaterialCost(1);
    }
    
    private static boolean hasAttributes(ItemStack stack) {
        return stack.hasTag() || 
               stack.getItem() instanceof net.minecraft.world.item.ArmorItem ||
               stack.getItem() instanceof net.minecraft.world.item.SwordItem;
    }
    
    private static void applyOrbBonus(ItemStack stack, String orbType) {
        CompoundTag tag = stack.getOrCreateTag();
        
        if (!tag.contains("AttributeModifiers")) {
            tag.put("AttributeModifiers", new ListTag());
        }
        
        ListTag modifiers = tag.getList("AttributeModifiers", 10); // 10 = Compound type
        double effectiveness = CommonConfig.UPGRADE_ORB_EFFECTIVENESS.get();
        
        switch (orbType) {
            case "flora" -> {
                // +10% Botanical spell power
                addAttributeModifier(modifiers, 
                    IBAttributes.BOTANICAL_SPELL_POWER.get().getDescriptionId(),
                    0.10 * effectiveness,
                    "multiply_total");
            }
            case "pool" -> {
                // +100 max mana
                addAttributeModifier(modifiers,
                    AttributeRegistry.MAX_MANA.get().getDescriptionId(),
                    100.0 * effectiveness,
                    "addition");
            }
            case "bursting" -> {
                // +5% spell power
                addAttributeModifier(modifiers,
                    AttributeRegistry.SPELL_POWER.get().getDescriptionId(),
                    0.05 * effectiveness,
                    "multiply_total");
            }
            case "terran" -> {
                // +5% spell power and +5% cooldown reduction
                addAttributeModifier(modifiers,
                    AttributeRegistry.SPELL_POWER.get().getDescriptionId(),
                    0.05 * effectiveness,
                    "multiply_total");
                addAttributeModifier(modifiers,
                    AttributeRegistry.COOLDOWN_REDUCTION.get().getDescriptionId(),
                    0.05 * effectiveness,
                    "multiply_total");
            }
        }
        
        tag.put("AttributeModifiers", modifiers);
    }
    
    private static void addAttributeModifier(ListTag modifiers, String attributeName, 
                                             double amount, String operation) {
        CompoundTag modifier = new CompoundTag();
        modifier.putString("AttributeName", attributeName);
        modifier.putString("Name", "Botanical Orb Bonus");
        modifier.putDouble("Amount", amount);
        modifier.putString("Operation", operation);
        modifier.putUUID("UUID", UUID.randomUUID());
        modifier.putString("Slot", "mainhand");
        
        modifiers.add(modifier);
    }
}
