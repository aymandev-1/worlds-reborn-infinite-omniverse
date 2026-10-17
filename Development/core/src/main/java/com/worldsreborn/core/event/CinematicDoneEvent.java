package com.worldsreborn.core.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** Raw client report that a cinematic ended. Story and Travel each check it against their own session. */
public class CinematicDoneEvent extends Event {
    private final ServerPlayer player;
    private final String cinematicId;
    private final boolean skipped;

    public CinematicDoneEvent(ServerPlayer player, String cinematicId, boolean skipped) {
        this.player = player;
        this.cinematicId = cinematicId;
        this.skipped = skipped;
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
}
