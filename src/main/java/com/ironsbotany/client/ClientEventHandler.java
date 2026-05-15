package com.ironsbotany.client;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaItemHandler;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {

    private static final int PROXIMITY_RADIUS = 6;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!ClientConfig.SHOW_MANA_HUD.get()) return;
        if (!ModList.get().isLoaded("botania")) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) return;

        // Sum Botania mana from all equipped items and accessories
        AtomicInteger totalMana = new AtomicInteger(0);
        AtomicInteger totalMax = new AtomicInteger(0);

        List<ItemStack> manaItems = ManaItemHandler.instance().getManaItems(player);
        List<ItemStack> manaAccessories = ManaItemHandler.instance().getManaAccesories(player);

        for (ItemStack stack : manaItems) {
            stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM).ifPresent(manaItem -> {
                totalMana.addAndGet(manaItem.getMana());
                totalMax.addAndGet(manaItem.getMaxMana());
            });
        }
        for (ItemStack stack : manaAccessories) {
            stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM).ifPresent(manaItem -> {
                totalMana.addAndGet(manaItem.getMana());
                totalMax.addAndGet(manaItem.getMaxMana());
            });
        }

        if (totalMax.get() == 0) return; // No mana items equipped

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        int xOffset = ClientConfig.HUD_X_OFFSET.get();
        int yOffset = ClientConfig.HUD_Y_OFFSET.get();

        int barWidth = 80;
        int barHeight = 6;
        int x = screenWidth / 2 + xOffset;
        int y = screenHeight + yOffset;

        float fillRatio = (float) totalMana.get() / totalMax.get();

        // Proximity pulse — cached cube scan, refreshed at most every 20 ticks
        // or whenever the player moves > 4 blocks from the last scan origin.
        boolean nearActive = NearbyIBBlockCache.hasNearbyActive(player, PROXIMITY_RADIUS);
        if (nearActive) {
            float t = (mc.level == null ? 0 : mc.level.getGameTime()) + event.getPartialTick();
            float pulse = 0.5F + 0.5F * (float) Math.sin(t * 0.18);
            int borderAlpha = (int) (80 + 120 * pulse);
            int borderColor = (borderAlpha << 24) | 0x00FFE0; // cyan-white
            // Top / bottom / left / right single-pixel border, 1px outset
            event.getGuiGraphics().fill(x - 1, y - 1, x + barWidth + 1, y, borderColor);
            event.getGuiGraphics().fill(x - 1, y + barHeight, x + barWidth + 1, y + barHeight + 1, borderColor);
            event.getGuiGraphics().fill(x - 1, y, x, y + barHeight, borderColor);
            event.getGuiGraphics().fill(x + barWidth, y, x + barWidth + 1, y + barHeight, borderColor);
        }

        // Background bar
        event.getGuiGraphics().fill(x, y, x + barWidth, y + barHeight, 0x80000000);
        // Mana fill bar
        int fillWidth = (int) (barWidth * fillRatio);
        event.getGuiGraphics().fill(x, y, x + fillWidth, y + barHeight, 0xFF00AAFF);
        // Text label
        String manaText = totalMana.get() + " / " + totalMax.get();
        event.getGuiGraphics().drawString(mc.font, manaText, x, y - 10, 0x00FFFF, true);
    }
}
