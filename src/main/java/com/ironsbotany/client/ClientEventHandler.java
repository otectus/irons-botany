package com.ironsbotany.client;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.block.entity.ManaConduitBlockEntity;
import com.ironsbotany.common.block.entity.SpellReservoirBlockEntity;
import com.ironsbotany.common.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaItemHandler;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The carried-mana HUD bar.
 *
 * <h3>What was wrong before 1.10.0</h3>
 * <ul>
 *   <li><b>It ran on every overlay, not just one.</b> {@code RenderGuiOverlayEvent.Post} fires once
 *       per registered overlay — roughly fifteen times a frame — and the handler never checked
 *       which one. The full capability sweep and the bar draw therefore happened about fifteen
 *       times per frame instead of once.</li>
 *   <li><b>It enumerated capabilities every frame.</b> Two Botania list calls plus a capability
 *       resolve per stack, at the display refresh rate, to show a value that changes at most twenty
 *       times a second.</li>
 *   <li><b>It double-counted.</b> Botania's item and accessory lists can surface the same physical
 *       stack, and both totals simply added it twice.</li>
 *   <li><b>It ignored the configured HUD scale</b> entirely.</li>
 *   <li><b>It did not clamp the fill.</b> A stack whose stored mana exceeded its capacity — which
 *       hand-edited or malformed NBT can produce — drew the fill past the end of the bar.</li>
 *   <li><b>It summed into an int.</b> Enough high-capacity items overflow to a negative total and
 *       the bar inverts.</li>
 *   <li><b>Its proximity check walked 2 197 block positions</b> once a second, and the static
 *       result was never reset on disconnect, so it survived into the next world.</li>
 *   <li><b>The pulse could not be turned off</b>, which matters for motion sensitivity.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {

    private static final int PROXIMITY_RADIUS = 6;
    private static final int PROXIMITY_RESCAN_TICKS = 20;

    /** Recompute carried mana at most five times a second; it is a status readout, not an animation. */
    private static final int MANA_RESCAN_TICKS = 4;

    private static final int BAR_WIDTH = 80;
    private static final int BAR_HEIGHT = 6;

    // Client-only cached state, reset on disconnect and dimension change. See resetClientState().
    private static long lastProximityScanTick = Long.MIN_VALUE;
    private static boolean nearActiveIBBlock = false;
    private static long lastManaScanTick = Long.MIN_VALUE;
    private static long cachedMana = 0L;
    private static long cachedMax = 0L;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        // Draw once per frame, not once per registered overlay.
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;
        if (!ClientConfig.SHOW_MANA_HUD.get()) return;
        if (!ModList.get().isLoaded("botania")) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        refreshManaTotals(player, mc.level.getGameTime());
        if (cachedMax <= 0L) return; // no mana items carried

        double scale = clamp(ClientConfig.HUD_SCALE.get(), 0.5, 2.0);
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        // Keep the whole bar on screen at its drawn size, so a large offset or a small window
        // cannot push it out of view.
        int scaledW = (int) Math.ceil(BAR_WIDTH * scale);
        int scaledH = (int) Math.ceil(BAR_HEIGHT * scale);
        int drawX = clampInt(screenWidth / 2 + ClientConfig.HUD_X_OFFSET.get(),
                0, Math.max(0, screenWidth - scaledW));
        int drawY = clampInt(screenHeight + ClientConfig.HUD_Y_OFFSET.get(),
                10, Math.max(10, screenHeight - scaledH));

        float fillRatio = (float) clamp((double) cachedMana / (double) cachedMax, 0.0, 1.0);

        GuiGraphics graphics = event.getGuiGraphics();
        graphics.pose().pushPose();
        graphics.pose().translate(drawX, drawY, 0);
        graphics.pose().scale((float) scale, (float) scale, 1.0F);

        if (nearActiveIBBlock) {
            if (ClientConfig.HUD_PULSE_NEARBY.get()) {
                float t = mc.level.getGameTime() + event.getPartialTick();
                float pulse = 0.5F + 0.5F * (float) Math.sin(t * 0.18);
                drawBorder(graphics, ((int) (80 + 120 * pulse) << 24) | 0x00FFE0);
            } else {
                // Motion disabled: a steady border still says "an Iron's Botany block is active
                // nearby" without animating.
                drawBorder(graphics, 0x9000FFE0);
            }
        }

        graphics.fill(0, 0, BAR_WIDTH, BAR_HEIGHT, 0x80000000);
        graphics.fill(0, 0, Math.round(BAR_WIDTH * fillRatio), BAR_HEIGHT, 0xFF00AAFF);
        graphics.drawString(mc.font, cachedMana + " / " + cachedMax, 0, -10, 0x00FFFF, true);

        graphics.pose().popPose();

        updateProximityFlag(player);
    }

    private static void drawBorder(GuiGraphics graphics, int color) {
        graphics.fill(-1, -1, BAR_WIDTH + 1, 0, color);
        graphics.fill(-1, BAR_HEIGHT, BAR_WIDTH + 1, BAR_HEIGHT + 1, color);
        graphics.fill(-1, 0, 0, BAR_HEIGHT, color);
        graphics.fill(BAR_WIDTH, 0, BAR_WIDTH + 1, BAR_HEIGHT, color);
    }

    /**
     * Recompute carried mana, at most every {@link #MANA_RESCAN_TICKS} ticks.
     *
     * <p>Deduplicates by stack reference identity, because Botania's item and accessory lists can
     * contain the same physical stack. Accumulates in {@code long} and clamps each item's stored
     * value into its own capacity, so neither an overflowing inventory nor a malformed NBT value
     * can produce a nonsensical readout.
     */
    private static void refreshManaTotals(Player player, long gameTime) {
        if (gameTime >= lastManaScanTick && gameTime - lastManaScanTick < MANA_RESCAN_TICKS) return;
        lastManaScanTick = gameTime;

        long mana = 0L;
        long max = 0L;
        Map<ItemStack, Boolean> seen = new IdentityHashMap<>();

        for (List<ItemStack> source : List.of(ManaItemHandler.instance().getManaItems(player),
                                              ManaItemHandler.instance().getManaAccesories(player))) {
            for (ItemStack stack : source) {
                if (stack == null || stack.isEmpty()) continue;
                if (seen.putIfAbsent(stack, Boolean.TRUE) != null) continue;

                ManaItem manaItem = stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM).resolve().orElse(null);
                if (manaItem == null) continue;

                long capacity = Math.max(0, manaItem.getMaxMana());
                mana += Math.min(capacity, Math.max(0, manaItem.getMana()));
                max += capacity;
            }
        }

        cachedMana = mana;
        cachedMax = max;
    }

    /**
     * Is an Iron's Botany block holding mana nearby?
     *
     * <p>Iterates the block-entity maps of the chunks in range rather than walking every position in
     * a cuboid, and skips chunks the client has not received.
     */
    private static void updateProximityFlag(Player player) {
        Level level = player.level();
        if (level == null) return;
        long now = level.getGameTime();
        if (now >= lastProximityScanTick && now - lastProximityScanTick < PROXIMITY_RESCAN_TICKS) return;
        lastProximityScanTick = now;

        BlockPos origin = player.blockPosition();
        int minChunkX = SectionPos.blockToSectionCoord(origin.getX() - PROXIMITY_RADIUS);
        int maxChunkX = SectionPos.blockToSectionCoord(origin.getX() + PROXIMITY_RADIUS);
        int minChunkZ = SectionPos.blockToSectionCoord(origin.getZ() - PROXIMITY_RADIUS);
        int maxChunkZ = SectionPos.blockToSectionCoord(origin.getZ() + PROXIMITY_RADIUS);
        long radiusSq = (long) PROXIMITY_RADIUS * PROXIMITY_RADIUS;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    if (entry.getKey().distSqr(origin) > radiusSq) continue;
                    BlockEntity be = entry.getValue();
                    if ((be instanceof SpellReservoirBlockEntity r && r.getStoredMana() > 0)
                            || (be instanceof ManaConduitBlockEntity c && c.getStoredMana() > 0)) {
                        nearActiveIBBlock = true;
                        return;
                    }
                }
            }
        }
        nearActiveIBBlock = false;
    }

    /**
     * Drop cached client state.
     *
     * <p>These are per-world observations. Leaving them set across a disconnect made a freshly
     * joined world inherit the previous one's "a block is active nearby" pulse and a stale mana
     * readout until the next rescan.
     */
    private static void resetClientState() {
        lastProximityScanTick = Long.MIN_VALUE;
        lastManaScanTick = Long.MIN_VALUE;
        nearActiveIBBlock = false;
        cachedMana = 0L;
        cachedMax = 0L;
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        resetClientState();
    }

    /** Fires when the client player is rebuilt — dimension change and respawn. */
    @SubscribeEvent
    public static void onClone(ClientPlayerNetworkEvent.Clone event) {
        resetClientState();
    }

    private static double clamp(double value, double min, double max) {
        if (!Double.isFinite(value)) return min;
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
