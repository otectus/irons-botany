package com.ironsbotany.common.util;

import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.config.ManaUnificationMode;
import com.ironsbotany.common.item.PoolAttunementCharm;
import com.ironsbotany.common.registry.IBItems;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.api.mana.ManaPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManaHelper {

    public static int convertBotaniaToISS(int botaniaAmount) {
        ManaUnificationMode mode = CommonConfig.MANA_UNIFICATION_MODE.get();
        if (!mode.allowsConversion()) return 0;
        int ratio = CommonConfig.MANA_CONVERSION_RATIO.get();
        return botaniaAmount / ratio;
    }

    public static int convertISSToBotania(int issAmount) {
        ManaUnificationMode mode = CommonConfig.MANA_UNIFICATION_MODE.get();
        if (!mode.allowsConversion()) return 0;
        int ratio = CommonConfig.MANA_CONVERSION_RATIO.get();
        return issAmount * ratio;
    }

    public static boolean tryConvertManaToISS(Player player, ItemStack stack) {
        if (player.level().isClientSide()) return false;
        MagicData magicData = MagicData.getPlayerMagicData(player);
        if (magicData == null) return false;

        float currentMana = magicData.getMana();
        float maxMana = (float) player.getAttributeValue(AttributeRegistry.MAX_MANA.get());
        if (currentMana >= maxMana) return false;

        int transferRate = CommonConfig.MANA_TRANSFER_RATE.get();
        int issToAdd = convertBotaniaToISS(transferRate);
        if (issToAdd <= 0) return false;

        int issRoom = (int) (maxMana - currentMana);
        if (issToAdd > issRoom) {
            issToAdd = issRoom;
            int ratio = CommonConfig.MANA_CONVERSION_RATIO.get();
            transferRate = issToAdd * ratio;
        }

        boolean extracted = ManaItemHandler.instance().requestManaExact(stack, player, transferRate, true);
        if (extracted) {
            magicData.addMana(issToAdd);
            return true;
        }
        return false;
    }

    /**
     * Check if the player can pay {@code amount} Botania mana from any combination
     * of valid sources (mana items, accessories, and — if enabled — nearby pools).
     *
     * Aggregates across all sources, mirroring what the mana HUD shows. This is
     * the contract callers expect; a player with two half-full tablets must be
     * able to pay a full-tablet cost.
     */
    public static boolean hasBotaniaMana(Player player, int amount) {
        return aggregateAndDrain(player, amount, false);
    }

    /**
     * Drain {@code amount} Botania mana from any combination of valid sources.
     * Two-pass: simulate to confirm coverage, then drain proportionally across
     * items → accessories → pools. Returns false if total available is short;
     * no partial drain occurs.
     */
    public static boolean drainBotaniaMana(Player player, int amount) {
        return aggregateAndDrain(player, amount, true);
    }

    /**
     * Nature-spell variant: like {@link #drainBotaniaMana} but also consults
     * the bound pool of a worn {@link PoolAttunementCharm}, if any, as one
     * additional source. The charm's bound pool is gated by range and
     * bandwidth configs and only fires for Nature-school casts.
     */
    public static boolean drainBotaniaManaWithBoundPool(Player player, int amount) {
        if (aggregateAndDrain(player, amount, true)) return true;
        // Items + accessories + nearby pools couldn't cover. Fall back to the
        // bound pool, draining the entire shortfall in one hit if possible.
        int remaining = computeShortfall(player, amount);
        if (remaining <= 0) return true;
        return drainFromBoundPool(player, remaining);
    }

    /**
     * Pre-check variant of {@link #drainBotaniaManaWithBoundPool}.
     */
    public static boolean hasBotaniaManaWithBoundPool(Player player, int amount) {
        if (aggregateAndDrain(player, amount, false)) return true;
        int remaining = computeShortfall(player, amount);
        if (remaining <= 0) return true;
        return simulateBoundPool(player, remaining);
    }

    private static int computeShortfall(Player player, int amount) {
        if (amount <= 0) return 0;
        List<ItemStack> items = ManaItemHandler.instance().getManaItems(player);
        List<ItemStack> accessories = ManaItemHandler.instance().getManaAccesories(player);
        int sim = sumFromStacks(player, items, amount, false);
        if (sim < amount) sim += sumFromStacks(player, accessories, amount - sim, false);
        if (sim < amount && CommonConfig.ENABLE_MANA_POOL_ACCESS.get()) {
            sim += simulatePools(player, amount - sim, new ArrayList<>());
        }
        return Math.max(0, amount - sim);
    }

    private static boolean simulateBoundPool(Player player, int need) {
        if (!CommonConfig.ENABLE_POOL_ATTUNEMENT.get()) return false;
        Optional<ItemStack> charm = findBoundCharm(player);
        if (charm.isEmpty()) return false;

        BlockPos pos = PoolAttunementCharm.getBoundPos(charm.get());
        ResourceLocation dim = PoolAttunementCharm.getBoundDimension(charm.get());
        if (pos == null || dim == null) return false;

        // Same dimension only.
        if (!player.level().dimension().location().equals(dim)) return false;

        // Range cap.
        int range = CommonConfig.POOL_ATTUNEMENT_RANGE.get();
        if (player.blockPosition().distSqr(pos) > (double) range * range) return false;

        BlockEntity be = player.level().getBlockEntity(pos);
        if (!(be instanceof ManaPool pool)) return false;

        int bandwidth = CommonConfig.POOL_ATTUNEMENT_BANDWIDTH.get();
        int contribution = Math.min(pool.getCurrentMana(), Math.min(need, bandwidth));
        return contribution >= need;
    }

    private static boolean drainFromBoundPool(Player player, int need) {
        if (!CommonConfig.ENABLE_POOL_ATTUNEMENT.get()) return false;
        Optional<ItemStack> charm = findBoundCharm(player);
        if (charm.isEmpty()) return false;

        BlockPos pos = PoolAttunementCharm.getBoundPos(charm.get());
        ResourceLocation dim = PoolAttunementCharm.getBoundDimension(charm.get());
        if (pos == null || dim == null) return false;
        if (!player.level().dimension().location().equals(dim)) return false;

        int range = CommonConfig.POOL_ATTUNEMENT_RANGE.get();
        if (player.blockPosition().distSqr(pos) > (double) range * range) return false;

        BlockEntity be = player.level().getBlockEntity(pos);
        if (!(be instanceof ManaPool pool)) return false;

        int bandwidth = CommonConfig.POOL_ATTUNEMENT_BANDWIDTH.get();
        int contribution = Math.min(pool.getCurrentMana(), Math.min(need, bandwidth));
        if (contribution < need) return false;
        pool.receiveMana(-need);
        return true;
    }

    private static Optional<ItemStack> findBoundCharm(Player player) {
        try {
            return CuriosApi.getCuriosInventory(player)
                .map(handler -> handler.findCurios(
                    s -> s.getItem() == IBItems.POOL_ATTUNEMENT_CHARM.get()
                      && PoolAttunementCharm.isBound(s)))
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0).stack());
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    private static boolean aggregateAndDrain(Player player, int amount, boolean doExtract) {
        if (amount <= 0) return true;

        List<ItemStack> items = ManaItemHandler.instance().getManaItems(player);
        List<ItemStack> accessories = ManaItemHandler.instance().getManaAccesories(player);

        // === Simulate pass ===
        int simulated = sumFromStacks(player, items, amount, false);
        if (simulated < amount) {
            simulated += sumFromStacks(player, accessories, amount - simulated, false);
        }

        List<BlockPos> contributingPools = new ArrayList<>();
        int poolSim = 0;
        if (simulated < amount && CommonConfig.ENABLE_MANA_POOL_ACCESS.get()) {
            poolSim = simulatePools(player, amount - simulated, contributingPools);
        }

        if (simulated + poolSim < amount) {
            return false;
        }
        if (!doExtract) {
            return true;
        }

        // === Drain pass ===
        int drained = sumFromStacks(player, items, amount, true);
        if (drained < amount) {
            drained += sumFromStacks(player, accessories, amount - drained, true);
        }
        if (drained < amount && !contributingPools.isEmpty()) {
            drained += drainPools(player.level(), contributingPools, amount - drained);
        }
        return drained >= amount;
    }

    private static int sumFromStacks(Player player, List<ItemStack> stacks, int need, boolean doExtract) {
        if (need <= 0) return 0;
        int total = 0;
        for (ItemStack stack : stacks) {
            int got = ManaItemHandler.instance().requestMana(stack, player, need - total, doExtract);
            total += got;
            if (total >= need) break;
        }
        return total;
    }

    private static int simulatePools(Player player, int need, List<BlockPos> contributing) {
        if (!BotaniaIntegration.isBotaniaLoaded() || need <= 0) return 0;

        Level level = player.level();
        int radius = CommonConfig.MANA_POOL_SEARCH_RADIUS.get();
        int total = 0;

        // Cached cube scan — refreshed at most every 2s or 4-block move.
        for (BlockPos pos : NearbyManaPoolCache.get(player, radius)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ManaPool pool) {
                int available = pool.getCurrentMana();
                if (available <= 0) continue;
                int contribution = Math.min(available, need - total);
                if (contribution > 0) {
                    contributing.add(pos.immutable());
                    total += contribution;
                    if (total >= need) break;
                }
            }
        }
        return total;
    }

    private static int drainPools(Level level, List<BlockPos> pools, int need) {
        int total = 0;
        for (BlockPos pos : pools) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ManaPool pool) {
                int remaining = need - total;
                if (remaining <= 0) break;
                int take = Math.min(pool.getCurrentMana(), remaining);
                if (take > 0) {
                    pool.receiveMana(-take);
                    total += take;
                }
            }
        }
        return total;
    }

    /**
     * @deprecated Pool-only checks are now folded into {@link #hasBotaniaMana(Player, int)}.
     * Kept for compatibility with any external callers; prefer the aggregated entry point.
     */
    @Deprecated
    public static boolean hasBotaniaManaFromPools(Player player, int amount) {
        if (!CommonConfig.ENABLE_MANA_POOL_ACCESS.get()) return false;
        return simulatePools(player, amount, new ArrayList<>()) >= amount;
    }

    /**
     * @deprecated Pool-only drains are now folded into {@link #drainBotaniaMana(Player, int)}.
     */
    @Deprecated
    public static boolean drainBotaniaManaFromPools(Player player, int amount) {
        if (!CommonConfig.ENABLE_MANA_POOL_ACCESS.get()) return false;
        List<BlockPos> pools = new ArrayList<>();
        int sim = simulatePools(player, amount, pools);
        if (sim < amount) return false;
        return drainPools(player.level(), pools, amount) >= amount;
    }
}
