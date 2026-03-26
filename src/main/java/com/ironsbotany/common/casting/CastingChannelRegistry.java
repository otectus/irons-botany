package com.ironsbotany.common.casting;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for casting channels
 */
public class CastingChannelRegistry {
    private static final Map<ResourceLocation, CastingChannel> CHANNELS = new HashMap<>();
    private static final Map<Item, CastingChannel> ITEM_CHANNELS = new HashMap<>();
    
    public static void registerChannel(CastingChannel channel) {
        CHANNELS.put(channel.getId(), channel);
    }
    
    public static void registerItemChannel(Item item, CastingChannel channel) {
        ITEM_CHANNELS.put(item, channel);
        registerChannel(channel);
    }
    
    public static CastingChannel getChannel(ResourceLocation id) {
        return CHANNELS.get(id);
    }
    
    public static CastingChannel getChannelForItem(ItemStack stack) {
        return ITEM_CHANNELS.get(stack.getItem());
    }
    
    public static boolean hasChannel(ItemStack stack) {
        return ITEM_CHANNELS.containsKey(stack.getItem());
    }
    
    public static Map<ResourceLocation, CastingChannel> getAllChannels() {
        return new HashMap<>(CHANNELS);
    }
}
