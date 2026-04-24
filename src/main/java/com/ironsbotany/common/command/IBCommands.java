package com.ironsbotany.common.command;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.config.CommonConfig;
import com.ironsbotany.common.config.ConfigHelper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class IBCommands {
    private IBCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal(IronsBotany.MODID)
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("reload").executes(IBCommands::reload))
                .then(Commands.literal("info").executes(IBCommands::info))
        );
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        ConfigHelper.validateConfig();
        CommandSourceStack src = ctx.getSource();
        src.sendSuccess(() -> Component.literal(
            "[Iron's Botany] Config re-validated. Mana mode: " +
            CommonConfig.MANA_UNIFICATION_MODE.get() +
            " (ratio " + CommonConfig.MANA_CONVERSION_RATIO.get() + ":1)"), true);
        IronsBotany.LOGGER.info("[Iron's Botany] /ironsbotany reload invoked by {}", src.getTextName());
        return Command.SINGLE_SUCCESS;
    }

    private static int info(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        src.sendSuccess(() -> Component.literal(
            "[Iron's Botany] v" + IronsBotany.class.getPackage().getImplementationVersion() +
            " | Mode: " + CommonConfig.MANA_UNIFICATION_MODE.get() +
            " | Ratio: " + CommonConfig.MANA_CONVERSION_RATIO.get() +
            " | Deep synergy: " + ConfigHelper.isDeepSynergyEnabled() +
            " | Botanical school: " + CommonConfig.ENABLE_BOTANICAL_SCHOOL.get()), false);
        return Command.SINGLE_SUCCESS;
    }
}
