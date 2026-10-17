package com.worldsreborn.core.model;

public enum ProgressResult {
    /** Not applicable (quest not started, already done, or a different step is current). Nothing changed. */
    IGNORED,
    /** Counter increased but the step is not finished. */
    PROGRESSED,
    /** The step finished and the quest moved to its next step. */
    STEP_COMPLETED,
    /** The final step finished; the quest is complete. */
    QUEST_COMPLETED;

    public boolean changed() {
        return this != IGNORED;
    }
}
