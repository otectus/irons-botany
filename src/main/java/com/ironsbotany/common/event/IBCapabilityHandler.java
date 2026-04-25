package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.item.cap.ItemManaCapabilityProvider;
import com.ironsbotany.common.registry.IBItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Attaches Botania {@code MANA_ITEM} capabilities to Iron's Botany items
 * that should participate in the mana network. This is what lets a
 * Livingwood Staff appear in the mana HUD, accept Spark deposits, and be
 * drained by {@code ManaItemHandler.requestMana} alongside Mana Tablets.
 *
 * <p>Stacks are inspected by item identity, not subclass — adding a new
 * mana-network item is a one-line addition to {@link #attach}.
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID)
public final class IBCapabilityHandler {

    private static final ResourceLocation MANA_CAP_ID =
            new ResourceLocation(IronsBotany.MODID, "mana_storage");

    private IBCapabilityHandler() {}

    @SubscribeEvent
    public static void onAttachItemCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.isEmpty()) return;

        Item item = stack.getItem();
        int cap = capacityFor(item);
        if (cap > 0) {
            event.addCapability(MANA_CAP_ID, new ItemManaCapabilityProvider(stack, cap));
        }
    }

    private static int capacityFor(Item item) {
        if (item == IBItems.LIVINGWOOD_STAFF.get()) return CommonConfig.LIVINGWOOD_STAFF_MANA_CAPACITY.get();
        if (item == IBItems.DREAMWOOD_SCEPTER.get()) return 250_000;
        if (item == IBItems.GAIA_SPIRIT_WAND.get()) return 1_000_000;
        if (item == IBItems.MANASTEEL_STAFF.get()) return 50_000;
        if (item == IBItems.TERRASTEEL_SPELLBOOK.get()) return 200_000;
        if (item == IBItems.ARCANE_CODEX.get()) return 500_000;
        if (item == IBItems.MANA_RESERVOIR_RING.get()) return 200_000;
        if (item == IBItems.BOTANICAL_FOCUS.get()) return 50_000;
        if (item == IBItems.BOTANICAL_RING.get()) return 25_000;
        return 0;
    }
}
