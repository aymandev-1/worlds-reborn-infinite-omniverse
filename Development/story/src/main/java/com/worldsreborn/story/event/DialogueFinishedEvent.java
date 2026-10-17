package com.worldsreborn.story.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** A validated dialogue ending. Campaigns react to this to advance quests. choiceId is "" if no choice was made. */
public class DialogueFinishedEvent extends Event {
    private final ServerPlayer player;
    private final String dialogueId;
    private final String choiceId;
    private final boolean skipped;

    public DialogueFinishedEvent(ServerPlayer player, String dialogueId, String choiceId, boolean skipped) {
        this.player = player;
        this.dialogueId = dialogueId;
        this.choiceId = choiceId;
        this.skipped = skipped;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public String getDialogueId() {
        return dialogueId;
    }

    public String getChoiceId() {
        return choiceId;
    }

    public boolean isSkipped() {
        return skipped;
    }
}
