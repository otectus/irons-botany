package com.ironsbotany.common.bridge.mana;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.api.mana.ManaPool;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The one place in Iron's Botany that reads or writes Botania mana. Everything above it works in
 * terms of {@link ManaPlanner.Snapshot} and {@link ManaPlan}, which is what keeps the aggregation
 * rules testable without a game runtime.
 *
 * <h3>Why this does not use {@code ManaItemHandler.requestMana}</h3>
 * That method's first argument is the <em>requesting</em> stack, which Botania excludes from its
 * own search by reference identity. It is a player-wide "give me mana from everything except this"
 * call, not a per-source accessor, and using it as one produced the defects described on
 * {@link ManaPlanner}. Per-source access is the {@code BotaniaForgeCapabilities.MANA_ITEM}
 * capability, which is public API and exposes exactly {@code getMana()} / {@code addMana(int)}.
 *
 * <h3>Atomicity</h3>
 * {@link #commit} runs synchronously inside a single event handler, so no other actor can mutate a
 * source between its re-validation and its debit. If any source has moved since planning, nothing
 * is taken: the debits already applied in that pass are restored to the exact items they came from
 * and the commit reports failure. That is what makes "a failed cast conserves all mana" structural.
 */
public final class BotaniaManaGateway {

    /** A source bound to its live handle, valid only for the tick it was collected in. */
    public record LiveSource(ManaSourceRef ref, ManaItem item, ManaPool pool) {

        public int available() {
            if (item != null) return Math.max(0, item.getMana());
            if (pool != null) return Math.max(0, pool.getCurrentMana());
            return 0;
        }

        /** Remove {@code amount}; callers must have validated availability first. */
        void withdraw(int amount) {
            if (amount <= 0) return;
            if (item != null) item.addMana(-amount);
            else if (pool != null) pool.receiveMana(-amount);
        }

        /** Put back {@code amount} previously taken by {@link #withdraw}. */
        void restore(int amount) {
            if (amount <= 0) return;
            if (item != null) item.addMana(amount);
            else if (pool != null) pool.receiveMana(amount);
        }

        ManaPlanner.Snapshot snapshot() {
            return new ManaPlanner.Snapshot(ref, available());
        }
    }

    private BotaniaManaGateway() {}

    // ------------------------------------------------------------------
    // Collection
    // ------------------------------------------------------------------

    /**
     * Gather every Botania mana source this player may spend from, deduplicated and in a
     * deterministic order.
     *
     * <p>Item sources come from Botania's own {@code getManaItems} / {@code getManaAccesories},
     * so Curios and any other mod that contributes to Botania's mana-item event are honoured
     * without this mod reimplementing their traversal. Those two lists can surface the same
     * physical stack, so they are deduplicated by <em>reference identity</em> — the correct notion
     * of "the same stack", and the double-count the 1.9.0 HUD and cost path both suffered.
     */
    public static List<LiveSource> collectSources(Player player) {
        List<LiveSource> sources = new ArrayList<>();
        if (player == null || player.level().isClientSide()) return sources;

        if (CommonConfig.ENABLE_INVENTORY_MANA_SOURCES.get()) {
            Map<ItemStack, Boolean> seen = new IdentityHashMap<>();
            collectItems(player, ManaItemHandler.instance().getManaItems(player),
                    ManaSourceRef.Kind.INVENTORY, seen, sources);
            collectItems(player, ManaItemHandler.instance().getManaAccesories(player),
                    ManaSourceRef.Kind.ACCESSORY, seen, sources);
        }

        if (CommonConfig.ENABLE_MANA_POOL_ACCESS.get()) {
            collectPools(player, sources);
        }

        return sources;
    }

    private static void collectItems(Player player, List<ItemStack> stacks, ManaSourceRef.Kind kind,
                                     Map<ItemStack, Boolean> seen, List<LiveSource> out) {
        if (stacks == null) return;
        boolean allowMirror = CommonConfig.ENABLE_MANA_MIRROR_SUPPORT.get();
        int ordinal = 0;

        for (ItemStack stack : stacks) {
            int index = ordinal++;
            if (stack == null || stack.isEmpty()) continue;
            if (seen.putIfAbsent(stack, Boolean.TRUE) != null) continue; // same physical stack
            if (!allowMirror && isManaMirror(stack)) continue;

            ManaItem manaItem = stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM).resolve().orElse(null);
            if (manaItem == null) continue;

            // An item that declares it will not export mana is not a spendable source. Honouring
            // this is what keeps a bound Mana Mirror or a no-export relic from being silently drained.
            if (manaItem.isNoExport()) continue;

            out.add(new LiveSource(new ManaSourceRef(kind, index, describe(stack)), manaItem, null));
        }
    }

    /**
     * Find {@link ManaPool} block entities near the caster.
     *
     * <p>Iterates the block entities of the chunks in range rather than every block position in a
     * cuboid. 1.9.0 walked {@code BlockPos.betweenClosed} over the full volume — at the default
     * radius that is tens of thousands of {@code getBlockEntity} lookups per affordability check.
     * Chunks that are not already loaded are skipped rather than loaded, so a proximity query can
     * never trigger world generation.
     */
    private static void collectPools(Player player, List<LiveSource> out) {
        Level level = player.level();
        int radius = Math.max(0, CommonConfig.MANA_POOL_SEARCH_RADIUS.get());
        if (radius == 0) return;

        BlockPos center = player.blockPosition();
        int minChunkX = SectionPos.blockToSectionCoord(center.getX() - radius);
        int maxChunkX = SectionPos.blockToSectionCoord(center.getX() + radius);
        int minChunkZ = SectionPos.blockToSectionCoord(center.getZ() - radius);
        int maxChunkZ = SectionPos.blockToSectionCoord(center.getZ() + radius);

        int verticalRadius = Math.max(1, radius / 2);
        long radiusSq = (long) radius * radius;
        List<LiveSource> found = new ArrayList<>();

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue; // not loaded — never force-load to answer a query

                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos pos = entry.getKey();
                    if (Math.abs(pos.getY() - center.getY()) > verticalRadius) continue;

                    long dx = pos.getX() - center.getX();
                    long dz = pos.getZ() - center.getZ();
                    if (dx * dx + dz * dz > radiusSq) continue;

                    ManaPool pool = asPool(entry.getValue());
                    if (pool == null) continue;
                    found.add(new LiveSource(
                            new ManaSourceRef(ManaSourceRef.Kind.POOL, 0, pos.toShortString()), null, pool));
                }
            }
        }

        // Rank by distance so the nearest pool is drained first, and re-stamp the ordinal so the
        // order is a stable property of the ref rather than of chunk iteration order.
        found.sort((a, b) -> Long.compare(distanceSq(center, a), distanceSq(center, b)));
        for (int i = 0; i < found.size(); i++) {
            LiveSource s = found.get(i);
            out.add(new LiveSource(
                    new ManaSourceRef(ManaSourceRef.Kind.POOL, i, s.ref().descriptor()), null, s.pool()));
        }
    }

    private static long distanceSq(BlockPos center, LiveSource source) {
        ManaPool pool = source.pool();
        if (pool == null) return Long.MAX_VALUE;
        BlockPos pos = pool.getManaReceiverPos();
        long dx = pos.getX() - center.getX();
        long dy = pos.getY() - center.getY();
        long dz = pos.getZ() - center.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private static ManaPool asPool(BlockEntity be) {
        if (be == null) return null;
        if (be instanceof ManaPool pool) return pool;
        return be.getCapability(BotaniaForgeCapabilities.MANA_RECEIVER).resolve()
                .filter(ManaPool.class::isInstance)
                .map(ManaPool.class::cast)
                .orElse(null);
    }

    private static boolean isManaMirror(ItemStack stack) {
        var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && "botania".equals(id.getNamespace()) && "mana_mirror".equals(id.getPath());
    }

    private static String describe(ItemStack stack) {
        var id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? "unknown" : id.toString();
    }

    // ------------------------------------------------------------------
    // Plan / commit / rollback
    // ------------------------------------------------------------------

    /** Snapshot {@code sources} and allocate {@code cost} across them. Performs no mutation. */
    public static ManaPlan plan(int cost, List<LiveSource> sources) {
        List<ManaPlanner.Snapshot> snapshots = new ArrayList<>(sources.size());
        for (LiveSource s : sources) snapshots.add(s.snapshot());
        return ManaPlanner.plan(cost, snapshots);
    }

    /**
     * Apply {@code plan} against {@code sources}, all or nothing.
     *
     * <p>Each source is re-checked immediately before it is debited. If any planned amount is no
     * longer present, every debit already applied in this pass is restored to the same source and
     * the method returns {@code false} having changed nothing observable.
     *
     * @return {@code true} iff the full requested amount was taken
     */
    public static boolean commit(ManaPlan plan, List<LiveSource> sources) {
        if (plan == null) return false;
        if (plan.isFree()) return true;
        if (!plan.isSatisfied()) return false;

        Map<ManaSourceRef, LiveSource> byRef = new java.util.HashMap<>();
        for (LiveSource s : sources) byRef.put(s.ref(), s);

        List<ManaPlan.PlannedDebit> applied = new ArrayList<>(plan.debits().size());
        for (ManaPlan.PlannedDebit debit : plan.debits()) {
            LiveSource source = byRef.get(debit.ref());
            if (source == null || source.available() < debit.amount()) {
                rollbackApplied(applied, byRef);
                IronsBotany.LOGGER.debug(
                        "Botania mana commit aborted: source {} could not supply {} — nothing was taken",
                        debit.ref(), debit.amount());
                return false;
            }
            source.withdraw(debit.amount());
            applied.add(debit);
        }
        return true;
    }

    /**
     * Return a committed plan's mana to the sources it came from.
     *
     * <p>Used when a cast fails after the debit — the amount is bounded by what was taken from the
     * same items moments earlier, so it cannot overfill them.
     */
    public static void rollback(ManaPlan plan, List<LiveSource> sources) {
        if (plan == null || plan.isFree()) return;
        Map<ManaSourceRef, LiveSource> byRef = new java.util.HashMap<>();
        for (LiveSource s : sources) byRef.put(s.ref(), s);
        rollbackApplied(plan.debits(), byRef);
    }

    private static void rollbackApplied(List<ManaPlan.PlannedDebit> applied,
                                        Map<ManaSourceRef, LiveSource> byRef) {
        for (ManaPlan.PlannedDebit debit : applied) {
            LiveSource source = byRef.get(debit.ref());
            if (source != null) source.restore(debit.amount());
        }
    }

    // ------------------------------------------------------------------
    // Additive dispatch (mana-generating weapons)
    // ------------------------------------------------------------------

    /**
     * Add mana to the player's carried mana items, returning how much was actually accepted.
     *
     * <p>Replaces the 1.9.0 idiom of calling {@code requestManaExact(stack, player, -amount, true)}
     * and relying on {@code Math.min(negative, mana)} flowing through {@code addMana(-drain)} to
     * add instead of remove. That trick worked only by accident, credited an item other than the
     * one being iterated (Botania skips the stack passed to it), and reported success without
     * proving anything was accepted. {@code dispatchMana} is the supported additive call.
     *
     * @return the amount accepted; {@code 0} if the player has no room. Callers must gate their
     *         particles, advancements and cooldowns on a positive return.
     */
    public static int dispatchMana(Player player, int amount) {
        if (player == null || amount <= 0 || player.level().isClientSide()) return 0;

        int accepted = 0;
        for (ItemStack stack : ManaItemHandler.instance().getManaItems(player)) {
            if (accepted >= amount) break;
            if (stack == null || stack.isEmpty()) continue;

            ManaItem manaItem = stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM).resolve().orElse(null);
            if (manaItem == null) continue;

            int room = manaItem.getMaxMana() - manaItem.getMana();
            if (room <= 0) continue;

            int give = Math.min(room, amount - accepted);
            manaItem.addMana(give);
            accepted += give;
        }
        return accepted;
    }
}
