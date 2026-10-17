package com.worldsreborn.core.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** A player asked to travel from the multiverse map. The Travel module validates and performs it. */
public class TravelRequestEvent extends Event {
    private final ServerPlayer player;
    private final String destinationId;

    public TravelRequestEvent(ServerPlayer player, String destinationId) {
        this.player = player;
        this.destinationId = destinationId;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public String getDestinationId() {
        return destinationId;
    }
}
