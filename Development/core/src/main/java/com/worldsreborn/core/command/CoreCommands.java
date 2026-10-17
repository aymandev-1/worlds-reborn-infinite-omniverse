package com.worldsreborn.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.model.WRContent;
import com.worldsreborn.core.profile.Profiles;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** /wr profile ... developer tools. Other modules add their own /wr subcommands. */
public final class CoreCommands {
    private CoreCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("wr").requires(s -> s.hasPermission(2))
                .then(Commands.literal("profile")
                        .then(Commands.literal("show")
                                .executes(c -> show(c.getSource(), c.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(c -> show(c.getSource(), EntityArgument.getPlayer(c, "player")))))
                        .then(Commands.literal("reset")
                                .executes(c -> reset(c.getSource(), c.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(c -> reset(c.getSource(), EntityArgument.getPlayer(c, "player"))))))
                .then(Commands.literal("content").executes(c -> {
                    c.getSource().sendSuccess(() -> Component.literal("Worlds Reborn content: " + WRContent.QUESTS.size()
                            + " quests, " + WRContent.DIALOGUES.size() + " dialogues, " + WRContent.DESTINATIONS.size()
                            + " destinations, " + WRContent.ABILITIES.size() + " abilities"), false);
                    return 1;
                })));
    }

    private static int show(CommandSourceStack source, ServerPlayer player) {
        PlayerProgress p = Profiles.get(player);
        source.sendSuccess(() -> Component.literal("Worlds Reborn profile of " + player.getScoreboardName()), false);
        for (String line : p.describe()) {
            source.sendSuccess(() -> Component.literal("  " + line), false);
        }
        return 1;
    }

    private static int reset(CommandSourceStack source, ServerPlayer player) throws CommandSyntaxException {
        Profiles.get(player).reset();
        Profiles.sync(player);
        source.sendSuccess(() -> Component.literal("Reset Worlds Reborn profile of " + player.getScoreboardName()), true);
        return 1;
    }
}
