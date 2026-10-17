package com.worldsreborn.characters.entity;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** A player right-clicked a story NPC (server side). Campaigns decide what happens. */
public class NpcInteractEvent extends Event {
    private final ServerPlayer player;
    private final StoryNpcEntity npc;

    public NpcInteractEvent(ServerPlayer player, StoryNpcEntity npc) {
        this.player = player;
        this.npc = npc;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public StoryNpcEntity getNpc() {
        return npc;
    }

    public String getCharacterId() {
        return npc.getCharacterId();
    }
}
