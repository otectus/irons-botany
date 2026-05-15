package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.util.DataKeys;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Phase 4.4 — Elven Bloom Scrolls.
 *
 * <p>When the {@code ENABLE_ELVEN_BLOOM_SCROLLS} config is true and a player
 * completes a {@link com.ironsbotany.common.recipe.RuneScrollFusionRecipe}
 * within {@link #PORTAL_RANGE} blocks of an Alfheim Portal block, the
 * resulting rune-enhanced scroll is upgraded with the {@code ELVEN_BLOOM}
 * flag. {@code AbstractBotanicalSpell.onCast} reads that flag to apply a
 * second-order spell-power bonus (gating Phase 4 endgame scroll content
 * behind real Botania late-game).</p>
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ElvenBloomScrollHandler {

    private static final int PORTAL_RANGE = 8;
    private static final ResourceLocation ALFHEIM_PORTAL_ID = new ResourceLocation("botania", "alfheim_portal");

    private ElvenBloomScrollHandler() {}

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!CommonConfig.ENABLE_ELVEN_BLOOM_SCROLLS.get()) return;
        if (!ModList.get().isLoaded("botania")) return;

        ItemStack result = event.getCrafting();
        if (!isRuneEnhancedScroll(result)) return;

        Player player = event.getEntity();
        if (player == null) return;

        if (!isNearAlfheimPortal(player)) return;

        CompoundTag tag = result.getOrCreateTag();
        if (tag.getBoolean(DataKeys.ELVEN_BLOOM)) return;
        tag.putBoolean(DataKeys.ELVEN_BLOOM, true);

        player.displayClientMessage(
            Component.translatable("ironsbotany.elven_bloom.upgraded")
                .withStyle(ChatFormatting.LIGHT_PURPLE),
            true);
    }

    private static boolean isRuneEnhancedScroll(ItemStack stack) {
        if (stack.isEmpty()) return false;
        CompoundTag tag = stack.getTag();
        if (tag == null) return false;
        if (!tag.getBoolean("runeEnhanced")) return false;
        TagKey<Item> scrollTag = TagKey.create(ForgeRegistries.Keys.ITEMS,
            new ResourceLocation(IronsBotany.MODID, "spell_scrolls"));
        return stack.is(scrollTag);
    }

    private static boolean isNearAlfheimPortal(Player player) {
        Level level = player.level();
        BlockPos here = player.blockPosition();
        var portalBlock = ForgeRegistries.BLOCKS.getValue(ALFHEIM_PORTAL_ID);
        if (portalBlock == null) return false;
        for (BlockPos pos : BlockPos.betweenClosed(
                here.offset(-PORTAL_RANGE, -PORTAL_RANGE, -PORTAL_RANGE),
                here.offset(PORTAL_RANGE, PORTAL_RANGE, PORTAL_RANGE))) {
            if (level.getBlockState(pos).is(portalBlock)) {
                return true;
            }
        }
        return false;
    }
}
