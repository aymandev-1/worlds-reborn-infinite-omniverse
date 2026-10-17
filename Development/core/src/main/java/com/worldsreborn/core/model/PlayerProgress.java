package com.worldsreborn.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Everything Worlds Reborn saves per player. Server-authoritative; clients receive a read-only copy.
 * <p>
 * Data-version strategy: {@link #CURRENT_DATA_VERSION} is written with every save. When loading an older version,
 * {@link #migrate(int)} upgrades it in place, one version at a time. Never repurpose an existing id; add new ones.
 */
public final class PlayerProgress {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final int ABILITY_SLOTS = 3;

    private final Map<String, QuestState> quests = new LinkedHashMap<>();
    private final Set<String> claimedRewards = new LinkedHashSet<>();
    private final Set<String> flags = new LinkedHashSet<>();
    private final Set<String> unlockedDestinations = new LinkedHashSet<>();
    private final Set<String> discoveredDestinations = new LinkedHashSet<>();
    private final Set<String> unlockedAbilities = new LinkedHashSet<>();
    private final String[] equippedAbilities = new String[ABILITY_SLOTS];
    private final Map<String, Long> abilityCooldownUntil = new LinkedHashMap<>();

    private String trackedQuest;
    private String pendingDialogue;
    private String activeCinematic;
    private int cinematicTicksLeft;
    private String transitDestination;
    private String transitCinematic;
    private int transitTicksLeft;
    private ObjectiveTarget target;
    private Location checkpoint;
    private Location returnLocation;
    /** Skin of the character the player is currently playing as, or null for their own (e.g. Steve). */
    private String perspectiveSkin;

    // ---------------------------------------------------------------- quests

    /** @return false if the quest was already started (or completed). */
    public boolean startQuest(String questId) {
        if (quests.containsKey(questId)) {
            return false;
        }
        quests.put(questId, new QuestState());
        if (trackedQuest == null || isQuestCompleted(trackedQuest)) {
            trackedQuest = questId;
        }
        return true;
    }

    public boolean isQuestStarted(String questId) {
        return quests.containsKey(questId);
    }

    public boolean isQuestCompleted(String questId) {
        QuestState s = quests.get(questId);
        return s != null && s.completed;
    }

    public boolean isQuestActive(String questId) {
        QuestState s = quests.get(questId);
        return s != null && !s.completed;
    }

    public QuestState questState(String questId) {
        return quests.get(questId);
    }

    public Map<String, QuestState> quests() {
        return quests;
    }

    /** True if the quest is active and its current step has this id. */
    public boolean isOnStep(QuestDef quest, String stepId) {
        QuestState s = quests.get(quest.id());
        if (s == null || s.completed) {
            return false;
        }
        StepDef step = quest.step(s.stepIndex);
        return step != null && step.id().equals(stepId);
    }

    public StepDef currentStep(QuestDef quest) {
        QuestState s = quests.get(quest.id());
        return s == null || s.completed ? null : quest.step(s.stepIndex);
    }

    /**
     * Advances {@code stepId} of {@code quest} by {@code amount}. Only the current step can progress, so replayed
     * or out-of-order triggers are ignored instead of skipping ahead or completing twice.
     */
    public ProgressResult progress(QuestDef quest, String stepId, int amount) {
        QuestState s = quests.get(quest.id());
        if (s == null || s.completed || amount <= 0) {
            return ProgressResult.IGNORED;
        }
        StepDef step = quest.step(s.stepIndex);
        if (step == null || !step.id().equals(stepId)) {
            return ProgressResult.IGNORED;
        }
        s.count = Math.min(step.required(), s.count + amount);
        if (s.count < step.required()) {
            return ProgressResult.PROGRESSED;
        }
        s.count = 0;
        s.stepIndex++;
        if (target != null && target.questId().equals(quest.id())) {
            target = null;
        }
        if (s.stepIndex >= quest.steps().size()) {
            s.stepIndex = quest.steps().size() - 1;
            s.completed = true;
            if (quest.id().equals(trackedQuest)) {
                trackedQuest = null;
            }
            return ProgressResult.QUEST_COMPLETED;
        }
        return ProgressResult.STEP_COMPLETED;
    }

    /** Developer tool: force a quest to a step. */
    public void setQuestStep(QuestDef quest, int stepIndex) {
        QuestState s = quests.computeIfAbsent(quest.id(), k -> new QuestState());
        s.stepIndex = Math.max(0, Math.min(stepIndex, quest.steps().size() - 1));
        s.count = 0;
        s.completed = false;
    }

    public void forceComplete(QuestDef quest) {
        QuestState s = quests.computeIfAbsent(quest.id(), k -> new QuestState());
        s.stepIndex = quest.steps().size() - 1;
        s.count = 0;
        s.completed = true;
    }

    public void removeQuest(String questId) {
        quests.remove(questId);
    }

    public String trackedQuest() {
        return trackedQuest;
    }

    public void setTrackedQuest(String questId) {
        this.trackedQuest = questId;
    }

    // ---------------------------------------------------------------- rewards and flags

    /** @return true exactly once per reward id; use it to guard every item or unlock grant. */
    public boolean claimReward(String rewardId) {
        return claimedRewards.add(rewardId);
    }

    public boolean hasClaimed(String rewardId) {
        return claimedRewards.contains(rewardId);
    }

    public Set<String> claimedRewards() {
        return claimedRewards;
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    /** @return true if the flag was newly set. */
    public boolean setFlag(String flag) {
        return flags.add(flag);
    }

    public void clearFlag(String flag) {
        flags.remove(flag);
    }

    public Set<String> flags() {
        return flags;
    }

    // ---------------------------------------------------------------- destinations

    public Set<String> unlockedDestinations() {
        return unlockedDestinations;
    }

    public Set<String> discoveredDestinations() {
        return discoveredDestinations;
    }

    public boolean isDestinationUnlocked(Destination d) {
        return d.unlockedByDefault() || unlockedDestinations.contains(d.id())
                || (d.unlockQuestId() != null && isQuestCompleted(d.unlockQuestId()));
    }

    // ---------------------------------------------------------------- abilities

    public Set<String> unlockedAbilities() {
        return unlockedAbilities;
    }

    public String equipped(int slot) {
        return slot >= 0 && slot < ABILITY_SLOTS ? equippedAbilities[slot] : null;
    }

    public boolean equip(int slot, String abilityId) {
        if (slot < 0 || slot >= ABILITY_SLOTS) {
            return false;
        }
        if (abilityId != null && !unlockedAbilities.contains(abilityId)) {
            return false;
        }
        equippedAbilities[slot] = abilityId;
        return true;
    }

    public boolean hasAnyEquippedAbility() {
        return Arrays.stream(equippedAbilities).anyMatch(a -> a != null);
    }

    public Map<String, Long> abilityCooldowns() {
        return abilityCooldownUntil;
    }

    public long cooldownRemaining(String abilityId, long gameTime) {
        Long until = abilityCooldownUntil.get(abilityId);
        return until == null ? 0 : Math.max(0, until - gameTime);
    }

    // ---------------------------------------------------------------- sessions (dialogue, cinematic, transit)

    public String pendingDialogue() {
        return pendingDialogue;
    }

    public void setPendingDialogue(String dialogueId) {
        this.pendingDialogue = dialogueId;
    }

    public String activeCinematic() {
        return activeCinematic;
    }

    public int cinematicTicksLeft() {
        return cinematicTicksLeft;
    }

    public void setCinematic(String id, int ticks) {
        this.activeCinematic = id;
        this.cinematicTicksLeft = id == null ? 0 : ticks;
    }

    public void tickCinematic() {
        if (cinematicTicksLeft > 0) {
            cinematicTicksLeft--;
        }
    }

    public String transitDestination() {
        return transitDestination;
    }

    public String transitCinematic() {
        return transitCinematic;
    }

    public int transitTicksLeft() {
        return transitTicksLeft;
    }

    public void setTransit(String destinationId, String cinematicId, int ticks) {
        this.transitDestination = destinationId;
        this.transitCinematic = destinationId == null ? null : cinematicId;
        this.transitTicksLeft = destinationId == null ? 0 : ticks;
    }

    public void tickTransit() {
        if (transitTicksLeft > 0) {
            transitTicksLeft--;
        }
    }

    public boolean inTransit() {
        return transitDestination != null;
    }

    // ---------------------------------------------------------------- locations

    public ObjectiveTarget target() {
        return target;
    }

    public void setTarget(ObjectiveTarget target) {
        this.target = target;
    }

    public Location checkpoint() {
        return checkpoint;
    }

    public void setCheckpoint(Location checkpoint) {
        this.checkpoint = checkpoint;
    }

    public String perspectiveSkin() {
        return perspectiveSkin;
    }

    public void setPerspectiveSkin(String skin) {
        this.perspectiveSkin = skin == null || skin.isEmpty() ? null : skin;
    }

    public Location returnLocation() {
        return returnLocation;
    }

    public void setReturnLocation(Location returnLocation) {
        this.returnLocation = returnLocation;
    }

    // ---------------------------------------------------------------- data version

    /**
     * Upgrades data saved by an older version of the mod. Called after loading, before use.
     * Version 1 is the first release, so there is nothing to migrate yet; future versions add cases here.
     */
    public void migrate(int fromVersion) {
        if (fromVersion > CURRENT_DATA_VERSION) {
            throw new IllegalStateException("Save data version " + fromVersion + " is newer than this mod ("
                    + CURRENT_DATA_VERSION + "). Update Worlds Reborn before loading this world.");
        }
        // fromVersion < 1 means "no Worlds Reborn data yet": nothing to convert.
    }

    /** Clears everything (developer reset). */
    public void reset() {
        quests.clear();
        claimedRewards.clear();
        flags.clear();
        unlockedDestinations.clear();
        discoveredDestinations.clear();
        unlockedAbilities.clear();
        Arrays.fill(equippedAbilities, null);
        abilityCooldownUntil.clear();
        trackedQuest = null;
        pendingDialogue = null;
        activeCinematic = null;
        cinematicTicksLeft = 0;
        transitDestination = null;
        transitCinematic = null;
        transitTicksLeft = 0;
        target = null;
        checkpoint = null;
        returnLocation = null;
        perspectiveSkin = null;
    }

    public List<String> describe() {
        List<String> out = new ArrayList<>();
        quests.forEach((id, s) -> out.add(id + " step=" + s.stepIndex + " count=" + s.count + (s.completed ? " DONE" : "")));
        out.add("tracked=" + trackedQuest + " pendingDialogue=" + pendingDialogue + " cinematic=" + activeCinematic
                + " transit=" + transitDestination);
        out.add("flags=" + flags);
        out.add("rewards=" + claimedRewards);
        out.add("destinations unlocked=" + unlockedDestinations + " discovered=" + discoveredDestinations);
        return out;
    }
}
