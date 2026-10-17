package com.worldsreborn.marvel;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** /wr marvel scene &lt;1-11&gt; jumps to the start of a scene for testing; /wr marvel restart starts Scene 1 again. */
public final class MarvelCommands {
    private MarvelCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("wr").requires(s -> s.hasPermission(2))
                .then(Commands.literal("marvel")
                        .then(Commands.literal("scene").then(Commands.argument("number", IntegerArgumentType.integer(1, 11))
                                .executes(c -> {
                                    ServerPlayer player = c.getSource().getPlayerOrException();
                                    int n = IntegerArgumentType.getInteger(c, "number");
                                    MarvelStory.jumpToScene(player, n);
                                    c.getSource().sendSuccess(() -> Component.translatable("command.wrmarvel.scene", n), true);
                                    return 1;
                                })))
                        .then(Commands.literal("restart").executes(c -> {
                            MarvelStory.jumpToScene(c.getSource().getPlayerOrException(), 1);
                            c.getSource().sendSuccess(() -> Component.translatable("command.wrmarvel.scene", 1), true);
                            return 1;
                        }))));
    }
}
