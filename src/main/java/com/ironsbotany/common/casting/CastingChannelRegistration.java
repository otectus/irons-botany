package com.ironsbotany.common.casting;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.casting.channels.DreamwoodFocusChannel;
import com.ironsbotany.common.casting.channels.LivingwoodStaffChannel;
import com.ironsbotany.common.casting.channels.TerraRodChannel;
import com.ironsbotany.common.registry.IBItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Wires the three casting channel implementations to the items that grant them.
 * Called from common setup; mirrors {@link com.ironsbotany.common.spell.catalyst.CatalystRegistration}.
 *
 * Hardware vs software profiles (per 1.7.0 audit):
 *   - Livingwood Staff  → sustained / efficiency
 *   - Dreamwood Scepter → speed / utility
 *   - Terra Rod profile → burst / heavy-cooldown (bound to Gaia Spirit Wand,
 *                         Terrasteel Spell Blade, and Botania's Terra Truncator)
 */
public class CastingChannelRegistration {

    public static void registerChannels() {
        LivingwoodStaffChannel livingwood = new LivingwoodStaffChannel();
        DreamwoodFocusChannel dreamwood = new DreamwoodFocusChannel();
        TerraRodChannel terra = new TerraRodChannel();

        bind(IBItems.LIVINGWOOD_STAFF.get(), livingwood);
        bind(IBItems.DREAMWOOD_SCEPTER.get(), dreamwood);
        bind(IBItems.GAIA_SPIRIT_WAND.get(), terra);
        bind(IBItems.TERRASTEEL_SPELL_BLADE.get(), terra);

        // Optional Botania-side bind for the actual Terra Truncator if present.
        bindFromRegistry("botania:terra_truncator", terra);

        IronsBotany.LOGGER.info("Registered {} casting channels across {} items",
                CastingChannelRegistry.getAllChannels().size(),
                CastingChannelRegistry.getItemBindingCount());
    }

    private static void bind(Item item, CastingChannel channel) {
        if (item == null || item == Items.AIR) {
            IronsBotany.LOGGER.warn("Channel '{}' has no item to bind to (skipped)", channel.getId());
            return;
        }
        CastingChannelRegistry.registerItemChannel(item, channel);
    }

    private static void bindFromRegistry(String registryName, CastingChannel channel) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(registryName));
        if (item != null && item != Items.AIR) {
            CastingChannelRegistry.registerItemChannel(item, channel);
        }
    }
}
