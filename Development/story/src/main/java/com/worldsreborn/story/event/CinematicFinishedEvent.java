package com.worldsreborn.story.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** A story cinematic ended: watched, skipped, timed out on the server, or recovered after a disconnect. */
public class CinematicFinishedEvent extends Event {
    private final ServerPlayer player;
    private final String cinematicId;
    private final boolean skipped;
    private final boolean interrupted;

    public CinematicFinishedEvent(ServerPlayer player, String cinematicId, boolean skipped, boolean interrupted) {
        this.player = player;
        this.cinematicId = cinematicId;
        this.skipped = skipped;
        this.interrupted = interrupted;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public String getCinematicId() {
        return cinematicId;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public boolean isInterrupted() {
        return interrupted;
    }
}
