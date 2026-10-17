package com.worldsreborn.core.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerProgressTest {
    private static final QuestDef QUEST = new QuestDef("test:scene", "test", "Scene 1", QuestCategory.MAIN,
            "t", "s", List.of(StepDef.action("talk", "d"), StepDef.count("repair", "d", 3), StepDef.reach("go", "d")),
            List.of(new RewardDef("test:map", "r")), "test:next");

    @Test
    void stepsOnlyAdvanceInOrder() {
        PlayerProgress p = new PlayerProgress();
        assertEquals(ProgressResult.IGNORED, p.progress(QUEST, "talk", 1), "not started yet");
        assertTrue(p.startQuest(QUEST.id()));
        assertFalse(p.startQuest(QUEST.id()), "starting twice is a no-op");
        assertEquals(ProgressResult.IGNORED, p.progress(QUEST, "repair", 1), "future step ignored");
        assertEquals(ProgressResult.STEP_COMPLETED, p.progress(QUEST, "talk", 1));
        assertEquals(ProgressResult.IGNORED, p.progress(QUEST, "talk", 1), "replayed trigger ignored");
        assertEquals(ProgressResult.PROGRESSED, p.progress(QUEST, "repair", 1));
        assertEquals(ProgressResult.PROGRESSED, p.progress(QUEST, "repair", 1));
        assertEquals(ProgressResult.STEP_COMPLETED, p.progress(QUEST, "repair", 5), "count clamps and completes");
        assertTrue(p.isOnStep(QUEST, "go"));
        assertEquals(ProgressResult.QUEST_COMPLETED, p.progress(QUEST, "go", 1));
        assertTrue(p.isQuestCompleted(QUEST.id()));
        assertEquals(ProgressResult.IGNORED, p.progress(QUEST, "go", 1), "completed quests never complete twice");
    }

    @Test
    void rewardsClaimOnce() {
        PlayerProgress p = new PlayerProgress();
        assertTrue(p.claimReward("test:map"));
        assertFalse(p.claimReward("test:map"));
        assertTrue(p.hasClaimed("test:map"));
    }

    @Test
    void trackingFollowsNewQuestAndClearsOnCompletion() {
        PlayerProgress p = new PlayerProgress();
        p.startQuest(QUEST.id());
        assertEquals(QUEST.id(), p.trackedQuest());
        p.forceComplete(QUEST);
        p.setTrackedQuest(null);
        p.startQuest("test:next");
        assertEquals("test:next", p.trackedQuest());
    }

    @Test
    void targetClearedWhenStepCompletes() {
        PlayerProgress p = new PlayerProgress();
        p.startQuest(QUEST.id());
        p.setTarget(new ObjectiveTarget(QUEST.id(), "talk", new Location("minecraft:overworld", 0, 64, 0), 3));
        p.progress(QUEST, "talk", 1);
        assertNull(p.target());
    }

    @Test
    void destinationUnlockRules() {
        PlayerProgress p = new PlayerProgress();
        Destination home = new Destination("t:home", "MC-616", "minecraft:overworld", "n", "d", null, true, false, 0, 0, 0);
        Destination ny = new Destination("t:ny", "MU-616", "t:ny", "n", "d", QUEST.id(), false, false, 0, 0, 0);
        Destination tva = new Destination("t:tva", null, "t:tva", "n", "d", null, false, false, 0, 0, 0);
        assertTrue(p.isDestinationUnlocked(home));
        assertFalse(p.isDestinationUnlocked(ny));
        p.startQuest(QUEST.id());
        p.forceComplete(QUEST);
        assertTrue(p.isDestinationUnlocked(ny));
        assertFalse(p.isDestinationUnlocked(tva));
        p.unlockedDestinations().add("t:tva");
        assertTrue(p.isDestinationUnlocked(tva));
    }

    @Test
    void abilitiesMustBeUnlockedToEquip() {
        PlayerProgress p = new PlayerProgress();
        assertFalse(p.equip(0, "t:dash"));
        p.unlockedAbilities().add("t:dash");
        assertTrue(p.equip(0, "t:dash"));
        assertFalse(p.equip(7, "t:dash"));
        assertTrue(p.hasAnyEquippedAbility());
        p.abilityCooldowns().put("t:dash", 120L);
        assertEquals(20, p.cooldownRemaining("t:dash", 100));
        assertEquals(0, p.cooldownRemaining("t:dash", 200));
    }

    @Test
    void transitAndCinematicSessions() {
        PlayerProgress p = new PlayerProgress();
        p.setTransit("t:ny", "t:fall", 3);
        assertTrue(p.inTransit());
        p.tickTransit();
        p.tickTransit();
        p.tickTransit();
        p.tickTransit();
        assertEquals(0, p.transitTicksLeft());
        p.setTransit(null, "ignored", 50);
        assertFalse(p.inTransit());
        assertNull(p.transitCinematic());
    }

    @Test
    void newerSaveVersionIsRejected() {
        PlayerProgress p = new PlayerProgress();
        assertDoesNotThrow(() -> p.migrate(0));
        assertDoesNotThrow(() -> p.migrate(PlayerProgress.CURRENT_DATA_VERSION));
        assertThrows(IllegalStateException.class, () -> p.migrate(PlayerProgress.CURRENT_DATA_VERSION + 1));
    }
}
