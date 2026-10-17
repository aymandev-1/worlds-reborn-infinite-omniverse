package com.worldsreborn.core.model;

import java.util.List;

/**
 * Static definition of a quest. One quest normally maps to one story scene.
 *
 * @param sceneLabel   short human label such as "Act 1 · Scene 3"
 * @param nextQuestId  quest started automatically when this one completes, or null
 */
public record QuestDef(String id, String campaign, String sceneLabel, QuestCategory category, String titleKey,
                       String summaryKey, List<StepDef> steps, List<RewardDef> rewards, String nextQuestId) {
    public QuestDef {
        if (steps == null || steps.isEmpty()) {
            throw new IllegalArgumentException("Quest " + id + " needs at least one step");
        }
        steps = List.copyOf(steps);
        rewards = rewards == null ? List.of() : List.copyOf(rewards);
    }

    public StepDef step(int index) {
        return index >= 0 && index < steps.size() ? steps.get(index) : null;
    }

    public int indexOfStep(String stepId) {
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).id().equals(stepId)) {
                return i;
            }
        }
        return -1;
    }
}
