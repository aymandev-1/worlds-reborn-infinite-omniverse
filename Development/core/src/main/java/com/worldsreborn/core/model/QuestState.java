package com.worldsreborn.core.model;

/** Mutable per-player state of one quest. */
public final class QuestState {
    public int stepIndex;
    public int count;
    public boolean completed;

    public QuestState() {
    }

    public QuestState(int stepIndex, int count, boolean completed) {
        this.stepIndex = stepIndex;
        this.count = count;
        this.completed = completed;
    }

    public QuestState copy() {
        return new QuestState(stepIndex, count, completed);
    }
}
