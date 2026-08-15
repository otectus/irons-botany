package com.ironsbotany.common.migration;

import com.ironsbotany.IronsBotany;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Runs {@link IBDataMigration} when a player joins.
 *
 * <p>Login is the right moment: the player's inventory, ender chest and Curios are all loaded, and
 * an item that only exists in a chest somewhere in the world is migrated whenever it next passes
 * through a player rather than by an unsafe whole-world rewrite.
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SchoolMigrationHandler {

    private SchoolMigrationHandler() {}

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        try {
            IBDataMigration.migratePlayer(event.getEntity());
        } catch (RuntimeException e) {
            // A migration failure must never stop a player joining. Leaving the schema version
            // unchanged means the attempt is retried on the next login rather than being recorded
            // as done.
            IronsBotany.LOGGER.error("Data migration failed for {} — the player joined unmigrated "
                            + "and will be retried on next login",
                    event.getEntity().getName().getString(), e);
        }
    }
}
