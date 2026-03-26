package com.ironsbotany.common.event;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.util.DataKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * One-time migration handler that rewrites old "ironsbotany:botanical" school
 * references in item NBT to "irons_spellbooks:nature" for existing worlds.
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SchoolMigrationHandler {

    private static final String OLD_SCHOOL = "ironsbotany:botanical";
    private static final String NEW_SCHOOL = "irons_spellbooks:nature";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        CompoundTag persistentData = player.getPersistentData();

        if (persistentData.getBoolean(DataKeys.SCHOOL_MIGRATED)) {
            return;
        }

        AtomicInteger migratedCount = new AtomicInteger(0);
        // Migrate player inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.hasTag()) {
                migratedCount.addAndGet(migrateItemNbt(stack.getTag()));
            }
        }

        // Migrate ender chest
        for (int i = 0; i < player.getEnderChestInventory().getContainerSize(); i++) {
            ItemStack stack = player.getEnderChestInventory().getItem(i);
            if (!stack.isEmpty() && stack.hasTag()) {
                migratedCount.addAndGet(migrateItemNbt(stack.getTag()));
            }
        }

        // Migrate Curios slots
        try {
            top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                var curios = handler.getEquippedCurios();
                for (int i = 0; i < curios.getSlots(); i++) {
                    ItemStack stack = curios.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.hasTag()) {
                        migratedCount.addAndGet(migrateItemNbt(stack.getTag()));
                    }
                }
            });
        } catch (Exception e) {
            IronsBotany.LOGGER.debug("Curios migration skipped: {}", e.getMessage());
        }

        persistentData.putBoolean(DataKeys.SCHOOL_MIGRATED, true);

        if (migratedCount.get() > 0) {
            IronsBotany.LOGGER.info("Migrated {} school references from '{}' to '{}' for player {}",
                    migratedCount.get(), OLD_SCHOOL, NEW_SCHOOL, player.getName().getString());
        }
    }

    private static int migrateItemNbt(CompoundTag tag) {
        int count = 0;

        for (String key : tag.getAllKeys()) {
            Tag value = tag.get(key);
            if (value instanceof net.minecraft.nbt.StringTag stringTag) {
                if (stringTag.getAsString().equals(OLD_SCHOOL)) {
                    tag.putString(key, NEW_SCHOOL);
                    count++;
                }
            } else if (value instanceof CompoundTag compound) {
                count += migrateItemNbt(compound);
            } else if (value instanceof ListTag list) {
                count += migrateListNbt(list);
            }
        }

        return count;
    }

    private static int migrateListNbt(ListTag list) {
        int count = 0;

        for (int i = 0; i < list.size(); i++) {
            Tag element = list.get(i);
            if (element instanceof CompoundTag compound) {
                count += migrateItemNbt(compound);
            } else if (element instanceof ListTag subList) {
                count += migrateListNbt(subList);
            }
        }

        return count;
    }
}
