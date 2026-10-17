package com.worldsreborn.story.event;

import com.worldsreborn.core.model.ProgressResult;
import com.worldsreborn.core.model.QuestDef;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * Fired after a quest starts (stepId null, result STEP_COMPLETED) or progresses. Campaigns use it to set up the next
 * objective: spawn NPCs, set targets, build areas.
 */
public class QuestProgressEvent extends Event {
    private final ServerPlayer player;
    private final QuestDef quest;
    private final String stepId;
    private final ProgressResult result;
    private final int previousStepIndex;

    public QuestProgressEvent(ServerPlayer player, QuestDef quest, String stepId, ProgressResult result, int previousStepIndex) {
        this.player = player;
        this.quest = quest;
        this.stepId = stepId;
        this.result = result;
        this.previousStepIndex = previousStepIndex;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public QuestDef getQuest() {
        return quest;
    }

    /** The step that progressed, or null when the quest just started. */
    public String getStepId() {
        return stepId;
    }

    public ProgressResult getResult() {
        return result;
    }

    public int getPreviousStepIndex() {
        return previousStepIndex;
    }

    public boolean isQuestStart() {
        return stepId == null;
    }
}
