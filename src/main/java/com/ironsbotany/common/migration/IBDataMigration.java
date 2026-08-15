package com.ironsbotany.common.migration;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.util.DataKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

/**
 * Versioned, idempotent data migration for Iron's Botany-owned content.
 *
 * <h3>Why a version number rather than a boolean</h3>
 * 1.9.0 recorded migration as a single boolean on the player. Once set, no later migration could
 * ever run, and there was no way to tell <em>which</em> transformation had been applied — so the
 * 1.9.0 rewrite (legacy Botany → ISS Nature), which is the wrong destination now that
 * {@code ironsbotany:botany} is canonical, could neither be detected nor corrected. An integer
 * schema version lets each release add a step that runs exactly once per data holder and states
 * precisely what it did.
 *
 * <h3>Schema history</h3>
 * <ul>
 *   <li><b>0</b> — pre-1.10.0. Either never migrated, or migrated by the 1.9.0 boolean handler.</li>
 *   <li><b>1</b> — school identifiers normalised onto canonical {@code ironsbotany:botany}
 *       ({@link ItemNbtMigration}).</li>
 * </ul>
 *
 * <h3>Scope</h3>
 * Player-reachable inventories only: main inventory (which includes armour and offhand), ender
 * chest, and Curios slots. World containers and offline players are deliberately not rewritten —
 * a whole-world scan is exactly the kind of unsafe bulk operation that can corrupt a save, and the
 * migration is written to be safe to apply lazily whenever an item next passes through a player.
 */
public final class IBDataMigration {

    /** Current schema. Increment when adding a step, and add it to {@link #migratePlayer}. */
    public static final int SCHEMA_VERSION = 1;

    /** Integer schema marker under this mod's namespace. */
    public static final String KEY_SCHEMA_VERSION = "IronsBotany_SchemaVersion";

    private IBDataMigration() {}

    /**
     * Bring one player's reachable items up to {@link #SCHEMA_VERSION}.
     *
     * <p>Safe to call repeatedly: a player already at the current version does no work, and every
     * step is individually idempotent even if it does run again.
     */
    public static void migratePlayer(Player player) {
        if (player == null || player.level().isClientSide()) return;

        CompoundTag persistentData = player.getPersistentData();
        int from = readSchemaVersion(persistentData);
        if (from >= SCHEMA_VERSION) return;

        int rewritten = 0;
        if (from < 1) {
            rewritten += migrateSchoolIdentifiers(player);
        }

        persistentData.putInt(KEY_SCHEMA_VERSION, SCHEMA_VERSION);
        // The 1.9.0 boolean is superseded by the version number. Removing it stops a downgrade
        // from seeing a "migrated" flag whose meaning has changed.
        persistentData.remove(DataKeys.SCHOOL_MIGRATED);

        if (rewritten > 0) {
            IronsBotany.LOGGER.info(
                    "Migrated {} school reference(s) to {} for {} (schema {} -> {})",
                    rewritten, com.ironsbotany.common.registry.BotanySchool.ID_STRING,
                    player.getName().getString(), from, SCHEMA_VERSION);
        } else {
            IronsBotany.LOGGER.debug("Player {} needed no data migration (schema {} -> {})",
                    player.getName().getString(), from, SCHEMA_VERSION);
        }
    }

    /**
     * Read the stored schema version, tolerating the 1.9.0 boolean.
     *
     * <p>A player carrying the old boolean is still treated as version 0, because that boolean's
     * transformation targeted ISS Nature and therefore left work for step 1 to finish.
     */
    static int readSchemaVersion(CompoundTag persistentData) {
        if (persistentData.contains(KEY_SCHEMA_VERSION)) {
            return Math.max(0, persistentData.getInt(KEY_SCHEMA_VERSION));
        }
        return 0;
    }

    private static int migrateSchoolIdentifiers(Player player) {
        int[] count = {0};
        forEachReachableItem(player, stack -> {
            if (stack.hasTag()) {
                count[0] += ItemNbtMigration.migrateItemTag(stack.getTag());
            }
        });
        return count[0];
    }

    /** Visit every item the player can reach: inventory, ender chest, Curios. */
    private static void forEachReachableItem(Player player, Consumer<ItemStack> visitor) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) visitor.accept(stack);
        }

        for (int i = 0; i < player.getEnderChestInventory().getContainerSize(); i++) {
            ItemStack stack = player.getEnderChestInventory().getItem(i);
            if (!stack.isEmpty()) visitor.accept(stack);
        }

        try {
            top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                var curios = handler.getEquippedCurios();
                for (int i = 0; i < curios.getSlots(); i++) {
                    ItemStack stack = curios.getStackInSlot(i);
                    if (!stack.isEmpty()) visitor.accept(stack);
                }
            });
        } catch (RuntimeException e) {
            // Curios is a hard dependency, but a version skew must not abort the whole migration
            // and leave the player's main inventory half-done.
            IronsBotany.LOGGER.warn("Curios slots skipped during migration for {}: {}",
                    player.getName().getString(), e.toString());
        }
    }
}
