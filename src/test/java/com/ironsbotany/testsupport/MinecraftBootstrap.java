package com.ironsbotany.testsupport;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;

/**
 * Boots the minimum of Minecraft that data classes need in a plain JUnit run.
 *
 * <p>Several Minecraft value types have static initialisers that read the built-in registries;
 * {@code ItemStack.EMPTY} is the one this project needs. Without {@link Bootstrap#bootStrap()} they
 * fail to initialise, and every later reference reports the far less obvious
 * {@code NoClassDefFoundError: Could not initialize class ...}.
 *
 * <p>This is not a game launch: no world, no server, no rendering, no mod loading.
 *
 * <h3>About the swallowed failure</h3>
 * Forge extends vanilla's bootstrap with a pass that walks event classes to precompute listener
 * lists. That pass needs a mod-loading environment and throws
 * {@code NoSuchMethodException: net.minecraftforge.network.NetworkEvent.<init>()} without one. It
 * runs <em>after</em> the registries are populated, so the part these tests depend on has already
 * succeeded by the time it fails.
 *
 * <p>Rather than trust that, {@link #ensure()} verifies the outcome it actually needs and rethrows
 * if the bootstrap genuinely did not get far enough — so a future Forge or Minecraft change that
 * breaks registry population fails loudly here instead of surfacing as mysterious errors in
 * unrelated tests.
 */
public final class MinecraftBootstrap {

    private static boolean done;

    private MinecraftBootstrap() {}

    public static synchronized void ensure() {
        if (done) return;

        SharedConstants.tryDetectVersion();

        Throwable bootstrapFailure = null;
        try {
            Bootstrap.bootStrap();
        } catch (Throwable t) {
            bootstrapFailure = t;
        }

        if (!registriesArePopulated()) {
            throw new IllegalStateException(
                    "Minecraft bootstrap did not populate the built-in registries; "
                            + "unit tests that touch Minecraft data classes cannot run",
                    bootstrapFailure);
        }

        done = true;
    }

    private static boolean registriesArePopulated() {
        try {
            return BuiltInRegistries.ITEM.size() > 0 && ItemStack.EMPTY.isEmpty();
        } catch (Throwable t) {
            return false;
        }
    }
}
