package com.worldsreborn.core.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** Raw client report that a dialogue ended. The Story module checks it against the open session. */
public class DialogueResponseEvent extends Event {
    private final ServerPlayer player;
    private final String dialogueId;
    private final String choiceId;
    private final boolean skipped;

    public DialogueResponseEvent(ServerPlayer player, String dialogueId, String choiceId, boolean skipped) {
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
