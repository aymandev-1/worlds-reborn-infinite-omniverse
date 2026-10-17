package com.worldsreborn.travel.event;

import com.worldsreborn.core.model.Destination;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** Fired after a player has been placed at a destination. */
public class TravelArrivedEvent extends Event {
    private final ServerPlayer player;
    private final Destination destination;

    public TravelArrivedEvent(ServerPlayer player, Destination destination) {
        this.player = player;
        this.destination = destination;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public Destination getDestination() {
        return destination;
    }
}
