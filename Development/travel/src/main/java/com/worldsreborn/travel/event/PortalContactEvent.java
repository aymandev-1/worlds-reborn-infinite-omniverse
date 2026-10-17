package com.worldsreborn.travel.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** A player walked into a destination portal. Call {@link #setHandled()} to replace the default travel. */
public class PortalContactEvent extends Event {
    private final ServerPlayer player;
    private final String destinationId;
    private final BlockPos pos;
    private boolean handled;

    public PortalContactEvent(ServerPlayer player, String destinationId, BlockPos pos) {
        this.player = player;
        this.destinationId = destinationId;
        this.pos = pos;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public BlockPos getPos() {
        return pos;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled() {
        this.handled = true;
    }
}
