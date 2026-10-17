package com.worldsreborn.travel;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.worldsreborn.core.model.Destination;
import com.worldsreborn.core.model.WRContent;
import com.worldsreborn.core.profile.Profiles;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** /wr travel to|unlock|list ... developer tools. "to" skips unlock checks on purpose. */
final class TravelCommands {
    private TravelCommands() {
    }

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("wr").requires(s -> s.hasPermission(2))
                .then(Commands.literal("travel")
                        .then(Commands.literal("list").executes(c -> {
                            ServerPlayer p = c.getSource().getPlayerOrException();
                            for (Destination d : WRContent.DESTINATIONS.all()) {
                                c.getSource().sendSuccess(() -> Component.literal(" " + d.id() + (TravelService.isUnlocked(p, d) ? " [unlocked]" : " [locked]")
                                        + (Profiles.get(p).discoveredDestinations().contains(d.id()) ? " [visited]" : "")), false);
                            }
                            return 1;
                        }))
                        .then(Commands.literal("to").then(Commands.argument("destination", StringArgumentType.greedyString())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(WRContent.DESTINATIONS.all().stream().map(Destination::id), b))
                                .executes(c -> {
                                    String id = StringArgumentType.getString(c, "destination");
                                    boolean ok = TravelService.beginTransit(c.getSource().getPlayerOrException(), id, null, 40);
                                    c.getSource().sendSuccess(() -> Component.literal(ok ? "Travelling to " + id : "Cannot travel to " + id), false);
                                    return ok ? 1 : 0;
                                })))
                        .then(Commands.literal("unlock").then(Commands.argument("destination", StringArgumentType.greedyString())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(WRContent.DESTINATIONS.all().stream().map(Destination::id), b))
                                .executes(c -> {
                                    String id = StringArgumentType.getString(c, "destination");
                                    ServerPlayer p = c.getSource().getPlayerOrException();
                                    TravelService.unlock(p, id);
                                    Profiles.get(p).setFlag(WRTravel.FREE_TRAVEL_FLAG);
                                    Profiles.sync(p);
                                    c.getSource().sendSuccess(() -> Component.literal("Unlocked " + id + " and map travel"), false);
                                    return 1;
                                })))));
    }
}
