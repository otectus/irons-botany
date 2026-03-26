package com.ironsbotany.common.alfheim;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.util.DataKeys;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/**
 * Alfheim scroll crafting system.
 * Scrolls crafted in Alfheim gain dual-school effects.
 */
public class AlfheimScrollCrafting {
    
    /**
     * Mark a scroll as Alfheim-crafted with dual-school
     */
    public static void markAlfheimScroll(ItemStack scroll, SchoolType primarySchool, 
                                         SchoolType secondarySchool) {
        if (!ModList.get().isLoaded("botania")) {
            return;
        }
        
        CompoundTag tag = scroll.getOrCreateTag();
        tag.putBoolean(DataKeys.ALFHEIM_CRAFTED, true);
        tag.putBoolean(DataKeys.DUAL_SCHOOL, true);
        tag.putString(DataKeys.SECONDARY_SCHOOL, secondarySchool.getId().toString());
    }
    
    /**
     * Check if scroll is Alfheim-crafted
     */
    public static boolean isAlfheimCrafted(ItemStack scroll) {
        CompoundTag tag = scroll.getTag();
        return tag != null && tag.getBoolean(DataKeys.ALFHEIM_CRAFTED);
    }
    
    /**
     * Check if scroll has dual-school
     */
    public static boolean hasDualSchool(ItemStack scroll) {
        CompoundTag tag = scroll.getTag();
        return tag != null && tag.getBoolean(DataKeys.DUAL_SCHOOL);
    }
    
    /**
     * Get secondary school for dual-school scroll
     */
    public static SchoolType getSecondarySchool(ItemStack scroll) {
        CompoundTag tag = scroll.getTag();
        if (tag == null || !tag.contains(DataKeys.SECONDARY_SCHOOL)) {
            return null;
        }
        
        String schoolId = tag.getString(DataKeys.SECONDARY_SCHOOL);
        ResourceLocation rl = ResourceLocation.tryParse(schoolId);
        if (rl == null) {
            return null;
        }
        return SchoolRegistry.getSchool(rl);
    }
    
    /**
     * Get compatible secondary schools for a primary school
     */
    public static List<SchoolType> getCompatibleSchools(SchoolType primarySchool) {
        List<SchoolType> compatible = new ArrayList<>();
        
        // Nature school is compatible with Holy
        if (primarySchool == SchoolRegistry.NATURE.get()) {
            compatible.add(SchoolRegistry.HOLY.get());
        }
        
        // Fire is compatible with Lightning
        if (primarySchool == SchoolRegistry.FIRE.get()) {
            compatible.add(SchoolRegistry.LIGHTNING.get());
        }
        
        // Ice is compatible with Water/Nature
        if (primarySchool == SchoolRegistry.ICE.get()) {
            compatible.add(SchoolRegistry.NATURE.get());
        }
        
        return compatible;
    }
    
    /**
     * Calculate dual-school bonus
     */
    public static float getDualSchoolBonus(ItemStack scroll) {
        if (!hasDualSchool(scroll)) {
            return 0.0f;
        }
        
        // Dual-school scrolls get +15% power
        return 0.15f;
    }
}
