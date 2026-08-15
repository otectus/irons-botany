package com.ironsbotany.common.command;

import com.ironsbotany.IronsBotany;
import com.ironsbotany.common.bridge.cast.CastTransactions;
import com.ironsbotany.common.flower.FlowerAuraRegistry;
import com.ironsbotany.common.registry.BotanySchool;
import com.ironsbotany.common.registry.IBSchools;
import com.mojang.brigadier.Command;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Server commands under {@code /irons_botany}. Permission level 2 (op / cheats).
 *
 * <h3>Why {@code reload} was renamed</h3>
 * The subcommand was called {@code reload}, which reads as "re-read my configuration" — the
 * meaning vanilla's {@code /reload} has. It never did that. It cleared the flower-aura cache and
 * nothing else, so an operator who edited the TOML and ran it would see no change and reasonably
 * conclude the config was broken. It is now {@code flushcaches}, which is what it does, and a
 * {@code diagnose} subcommand reports the metadata state that used to require reading the log.
 */
@Mod.EventBusSubscriber(modid = IronsBotany.MODID)
public final class IronsBotanyCommands {

    private IronsBotanyCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("irons_botany")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("flushcaches")
                    .executes(IronsBotanyCommands::flushCaches))
                .then(Commands.literal("diagnose")
                    .executes(IronsBotanyCommands::diagnose))
        );
    }

    /**
     * Drop this mod's runtime caches. Purely a performance shortcut, so this can never lose state.
     *
     * <p>It does <em>not</em> re-read configuration: Forge owns the TOML lifecycle and Iron's
     * Spells owns spell-config reloading via {@code /reload}. Saying so in the command's own
     * feedback is the point — the old name implied otherwise.
     */
    private static int flushCaches(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        int auras = FlowerAuraRegistry.cachedEntryCount();
        int casts = CastTransactions.openCount();

        FlowerAuraRegistry.cleanupCache();
        CastTransactions.clearAll();

        IronsBotany.LOGGER.info("Flushed Iron's Botany caches: {} aura scan(s), {} open cast transaction(s)",
                auras, casts);
        ctx.getSource().sendSuccess(
            () -> Component.translatable("ironsbotany.command.flushcaches.success", auras, casts),
            true);
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Report the school metadata every Iron's Botany spell resolves, server-side.
     *
     * <p>This is the check that would have made the 1.9.0 crash obvious without a stack trace: it
     * names any spell whose resolved school is not canonical Botany (a stale generated Iron's
     * Spells config) or whose school lacks the display colour that
     * {@code SchoolType#getTargetingColor()} dereferences without a null check.
     */
    private static int diagnose(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
        List<String> problems = new ArrayList<>();
        int checked = 0;

        for (AbstractSpell spell : SpellRegistry.REGISTRY.get()) {
            var id = spell.getSpellResource();
            if (id == null || !IronsBotany.MODID.equals(id.getNamespace())) continue;
            checked++;

            SchoolType school = spell.getSchoolType();
            if (school == null) {
                problems.add(spell.getSpellId() + ": no school");
                continue;
            }
            if (school != IBSchools.BOTANY.get()) {
                problems.add(spell.getSpellId() + ": resolved school is " + school.getId()
                        + " (expected " + BotanySchool.ID_STRING
                        + ") — check config/irons_spellbooks/spells/ironsbotany/");
            }
            if (school.getDisplayName() == null || school.getDisplayName().getStyle().getColor() == null) {
                problems.add(spell.getSpellId() + ": school " + school.getId()
                        + " has no display colour — getTargetingColor() will throw");
            }
            try {
                spell.getTargetingColor();
            } catch (RuntimeException e) {
                problems.add(spell.getSpellId() + ": getTargetingColor() threw " + e);
            }
        }

        final int total = checked;
        if (problems.isEmpty()) {
            ctx.getSource().sendSuccess(
                () -> Component.translatable("ironsbotany.command.diagnose.ok", total)
                        .withStyle(ChatFormatting.GREEN), false);
        } else {
            ctx.getSource().sendFailure(
                Component.translatable("ironsbotany.command.diagnose.problems", problems.size(), total));
            problems.forEach(p -> ctx.getSource().sendFailure(
                    Component.literal("  " + p).withStyle(ChatFormatting.RED)));
        }
        return problems.isEmpty() ? Command.SINGLE_SUCCESS : 0;
    }
}
