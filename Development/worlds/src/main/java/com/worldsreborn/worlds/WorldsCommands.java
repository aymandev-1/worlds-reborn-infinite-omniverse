package com.worldsreborn.worlds;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** /wr worlds sites — lists built sites and world flags. */
final class WorldsCommands {
    private WorldsCommands() {
    }

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("wr").requires(s -> s.hasPermission(2))
                .then(Commands.literal("worlds").then(Commands.literal("sites").executes(c -> {
                    WorldSites sites = WorldSites.get(c.getSource().getServer());
                    c.getSource().sendSuccess(() -> Component.literal("Worlds Reborn sites: " + sites.all().size()), false);
                    sites.all().forEach((id, site) -> c.getSource().sendSuccess(() -> Component.literal(
                            "  " + id + " -> " + site.dimension().location() + " " + site.pos().toShortString()), false));
                    return 1;
                }))));
    }
}
