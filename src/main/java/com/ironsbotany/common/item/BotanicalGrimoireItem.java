package com.ironsbotany.common.item;

import com.ironsbotany.IronsBotany;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

/**
 * The Chronicle of the Green Mage — opens the Patchouli guidebook when right-clicked.
 */
public class BotanicalGrimoireItem extends TooltipItem {
    private static final ResourceLocation BOOK_ID =
        new ResourceLocation(IronsBotany.MODID, "botanical_grimoire");

    public BotanicalGrimoireItem(Properties properties) {
        super(properties, "item.ironsbotany.botanical_grimoire.tooltip");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            if (ModList.get().isLoaded("patchouli")) {
                try {
                    vazkii.patchouli.api.PatchouliAPI.get().openBookGUI(serverPlayer, BOOK_ID);
                    return InteractionResultHolder.success(stack);
                } catch (Exception e) {
                    IronsBotany.LOGGER.debug("Could not open Patchouli book: {}", e.getMessage());
                }
            }
        }
        return InteractionResultHolder.pass(stack);
    }
}
