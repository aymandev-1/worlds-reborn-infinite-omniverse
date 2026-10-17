package com.worldsreborn.story;

import com.worldsreborn.core.WRCore;
import com.worldsreborn.core.model.Location;
import com.worldsreborn.core.model.ObjectiveTarget;
import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.model.ProgressResult;
import com.worldsreborn.core.model.QuestDef;
import com.worldsreborn.core.model.StepDef;
import com.worldsreborn.core.model.WRContent;
import com.worldsreborn.core.network.InfoPanelPayload;
import com.worldsreborn.core.network.OpenDialoguePayload;
import com.worldsreborn.core.network.StartCinematicPayload;
import com.worldsreborn.core.network.ToastPayload;
import com.worldsreborn.core.profile.Profiles;
import com.worldsreborn.story.event.QuestProgressEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/** Server-authoritative story API used by campaign mods. All methods must be called on the server thread. */
public final class StoryService {
    /** Extra time the server waits beyond a cinematic's length before finishing it on the client's behalf. */
    public static final int CINEMATIC_GRACE_TICKS = 60;

    private StoryService() {
    }

    // ------------------------------------------------------------------ quests

    public static QuestDef quest(String questId) {
        QuestDef q = WRContent.QUESTS.getOrNull(questId);
        if (q == null) {
            throw new IllegalArgumentException("Unknown quest " + questId);
        }
        return q;
    }

    /** Starts a quest once. Returns false if the player already had it. */
    public static boolean start(ServerPlayer player, String questId) {
        QuestDef q = quest(questId);
        PlayerProgress p = Profiles.get(player);
        if (!p.startQuest(questId)) {
            return false;
        }
        WRCore.debug("{} started quest {}", player.getScoreboardName(), questId);
        toast(player, "quest_started", q.titleKey(), q.step(0).descriptionKey(), "");
        NeoForge.EVENT_BUS.post(new QuestProgressEvent(player, q, null, ProgressResult.STEP_COMPLETED, 0));
        Profiles.sync(player);
        return true;
    }

    public static boolean isOnStep(ServerPlayer player, String questId, String stepId) {
        return Profiles.get(player).isOnStep(quest(questId), stepId);
    }

    public static boolean isCompleted(ServerPlayer player, String questId) {
        return Profiles.get(player).isQuestCompleted(questId);
    }

    /**
     * Advances the current step. Ignored unless {@code stepId} is the player's current step, so triggers can fire
     * repeatedly without double progress. Starts the next quest automatically when the quest completes.
     */
    public static ProgressResult progress(ServerPlayer player, String questId, String stepId, int amount) {
        QuestDef q = quest(questId);
        PlayerProgress p = Profiles.get(player);
        int stepBefore = p.questState(questId) == null ? -1 : p.questState(questId).stepIndex;
        ProgressResult result = p.progress(q, stepId, amount);
        if (!result.changed()) {
            return result;
        }
        WRCore.debug("{} progressed {}/{} -> {}", player.getScoreboardName(), questId, stepId, result);
        switch (result) {
            case PROGRESSED -> toast(player, "quest_updated", q.titleKey(), q.step(stepBefore).descriptionKey(),
                    p.questState(questId).count + "/" + q.step(stepBefore).required());
            case STEP_COMPLETED -> {
                StepDef next = p.currentStep(q);
                toast(player, "quest_updated", q.titleKey(), next.descriptionKey(), "");
                player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 0.6f, 1.4f);
            }
            case QUEST_COMPLETED -> {
                toast(player, "quest_completed", q.titleKey(), q.summaryKey(), "");
                player.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 0.7f, 0.8f);
            }
            default -> {
            }
        }
        NeoForge.EVENT_BUS.post(new QuestProgressEvent(player, q, stepId, result, stepBefore));
        if (result == ProgressResult.QUEST_COMPLETED && q.nextQuestId() != null) {
            start(player, q.nextQuestId());
        }
        Profiles.sync(player);
        return result;
    }

    // ------------------------------------------------------------------ objective target

    public static void setTarget(ServerPlayer player, String questId, String stepId, ResourceKey<Level> dimension,
                                 BlockPos pos, double radius) {
        PlayerProgress p = Profiles.get(player);
        p.setTarget(new ObjectiveTarget(questId, stepId,
                new Location(dimension.location().toString(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5), radius));
        Profiles.sync(player);
    }

    // ------------------------------------------------------------------ rewards

    /** Runs {@code grant} only the first time this reward id is claimed by the player. */
    public static boolean grantOnce(ServerPlayer player, String rewardId, Runnable grant) {
        PlayerProgress p = Profiles.get(player);
        if (!p.claimReward(rewardId)) {
            return false;
        }
        grant.run();
        Profiles.sync(player);
        return true;
    }

    // ------------------------------------------------------------------ dialogue

    /** Opens a dialogue. The outcome arrives later as a validated {@code DialogueFinishedEvent}. */
    public static void openDialogue(ServerPlayer player, String dialogueId) {
        if (!WRContent.DIALOGUES.contains(dialogueId)) {
            throw new IllegalArgumentException("Unknown dialogue " + dialogueId);
        }
        Profiles.get(player).setPendingDialogue(dialogueId);
        PacketDistributor.sendToPlayer(player, new OpenDialoguePayload(dialogueId));
        WRCore.debug("{} opened dialogue {}", player.getScoreboardName(), dialogueId);
    }

    // ------------------------------------------------------------------ cinematics

    /**
     * Plays a story cinematic for this player only. The server finishes it itself after the duration plus a grace
     * period, so a crashed or closed client can never leave the player stuck.
     */
    public static void playCinematic(ServerPlayer player, String cinematicId, int durationTicks) {
        Profiles.get(player).setCinematic(cinematicId, durationTicks + CINEMATIC_GRACE_TICKS);
        PacketDistributor.sendToPlayer(player, new StartCinematicPayload(cinematicId, durationTicks, ""));
    }

    // ------------------------------------------------------------------ presentation

    public static void toast(ServerPlayer player, String style, String titleKey, String detailKey, String argument) {
        PacketDistributor.sendToPlayer(player, new ToastPayload(style, titleKey, detailKey == null ? "" : detailKey,
                argument == null ? "" : argument));
    }

    /** A spoken line shown as a subtitle while play continues (a bark). {@code voice} may be null. */
    public static void say(ServerPlayer player, String speakerKey, String textKey, String voice) {
        PacketDistributor.sendToPlayer(player, new com.worldsreborn.core.network.SpeechPayload(speakerKey, textKey,
                voice == null ? "" : voice));
    }

    public static void infoPanel(ServerPlayer player, String style, String titleKey, List<String> lineKeys) {
        PacketDistributor.sendToPlayer(player, new InfoPanelPayload(style, titleKey, lineKeys));
    }
}
