package com.ironsbotany.common.command;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.flower.FlowerAuraRegistry;
import com.mojang.brigadier.Command;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Server commands under the {@code /irons_botany} root. Today the only
 * subcommand is {@code reload}, which flushes runtime caches so freshly
 * edited TOML / datapack changes take effect without restarting the
 * server.
 *
 * <p>What {@code reload} does:
 * <ul>
 *   <li>Drops the player→{@code ActiveFlowerAura} cache so flower aura
 *       lookups re-scan with the new config radius / strength.</li>
 *   <li>Future hooks (catalyst registry rebuild, casting-channel registry
 *       refresh, datapack spell-config re-resolution) will register
 *       themselves here.</li>
 * </ul>
 *
 * <p>Permission level 2 is required (op / cheats), matching vanilla
 * {@code /reload}.
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID)
public final class IronsBotanyCommands {

    private IronsBotanyCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("irons_botany")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("reload")
                    .executes(IronsBotanyCommands::reload))
        );
    }

    private static int reload(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        FlowerAuraRegistry.cleanupCache();
        IronsBotany.LOGGER.info("Iron's Botany: runtime caches flushed by /irons_botany reload");
        ctx.getSource().sendSuccess(
            () -> Component.translatable("ironsbotany.command.reload.success"),
            true);
        return Command.SINGLE_SUCCESS;
    }
}
