package com.worldsreborn.story;

import com.worldsreborn.core.WRCore;
import com.worldsreborn.core.event.CinematicDoneEvent;
import com.worldsreborn.core.event.DialogueResponseEvent;
import com.worldsreborn.core.model.DialogueChoice;
import com.worldsreborn.core.model.DialogueDef;
import com.worldsreborn.core.model.ObjectiveTarget;
import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.model.QuestDef;
import com.worldsreborn.core.model.StepDef;
import com.worldsreborn.core.model.StepType;
import com.worldsreborn.core.model.WRContent;
import com.worldsreborn.core.profile.Profiles;
import com.worldsreborn.story.event.CinematicFinishedEvent;
import com.worldsreborn.story.event.DialogueFinishedEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Validates client reports, runs server-side session timers and recovers interrupted sessions on login. */
public final class StoryEvents {
    private StoryEvents() {
    }

    @SubscribeEvent
    public static void onDialogueResponse(DialogueResponseEvent event) {
        ServerPlayer player = event.getPlayer();
        PlayerProgress p = Profiles.get(player);
        if (!event.getDialogueId().equals(p.pendingDialogue())) {
            WRCore.debug("Ignored response for {} from {} (open: {})", event.getDialogueId(), player.getScoreboardName(),
                    p.pendingDialogue());
            return;
        }
        DialogueDef def = WRContent.DIALOGUES.getOrNull(event.getDialogueId());
        String choice = event.getChoiceId();
        if (def != null && !choice.isEmpty() && def.choices().stream().map(DialogueChoice::id).noneMatch(choice::equals)) {
            choice = "";
        }
        finishDialogue(player, event.getDialogueId(), choice, event.isSkipped());
    }

    private static void finishDialogue(ServerPlayer player, String dialogueId, String choice, boolean skipped) {
        Profiles.get(player).setPendingDialogue(null);
        NeoForge.EVENT_BUS.post(new DialogueFinishedEvent(player, dialogueId, choice, skipped));
        Profiles.sync(player);
    }

    @SubscribeEvent
    public static void onCinematicDone(CinematicDoneEvent event) {
        ServerPlayer player = event.getPlayer();
        PlayerProgress p = Profiles.get(player);
        if (event.getCinematicId().equals(p.activeCinematic())) {
            finishCinematic(player, event.isSkipped(), false);
        }
    }

    private static void finishCinematic(ServerPlayer player, boolean skipped, boolean interrupted) {
        PlayerProgress p = Profiles.get(player);
        String id = p.activeCinematic();
        p.setCinematic(null, 0);
        NeoForge.EVENT_BUS.post(new CinematicFinishedEvent(player, id, skipped, interrupted));
        Profiles.sync(player);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PlayerProgress p = Profiles.get(player);

        if (p.activeCinematic() != null) {
            p.tickCinematic();
            if (p.cinematicTicksLeft() <= 0) {
                WRCore.debug("Server finished cinematic {} for {}", p.activeCinematic(), player.getScoreboardName());
                finishCinematic(player, false, true);
            }
        }

        // REACH steps complete when the player gets close to the target.
        if (player.tickCount % 10 == 0) {
            ObjectiveTarget t = p.target();
            if (t != null && t.radius() > 0) {
                QuestDef q = WRContent.QUESTS.getOrNull(t.questId());
                StepDef step = q == null ? null : p.currentStep(q);
                if (step != null && step.type() == StepType.REACH && step.id().equals(t.stepId())
                        && t.location().distanceSq(player.level().dimension().location().toString(),
                        player.getX(), player.getY(), player.getZ()) <= t.radius() * t.radius()) {
                    StoryService.progress(player, q.id(), step.id(), 1);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PlayerProgress p = Profiles.get(player);
        // A cinematic interrupted by disconnecting is finished now, so its consequences (e.g. arrival) still happen.
        if (p.activeCinematic() != null) {
            WRCore.LOGGER.info("[Worlds Reborn] Recovering interrupted cinematic {} for {}", p.activeCinematic(),
                    player.getScoreboardName());
            finishCinematic(player, false, true);
        }
        // An interrupted dialogue is shown again from the start.
        if (p.pendingDialogue() != null) {
            if (WRContent.DIALOGUES.contains(p.pendingDialogue())) {
                StoryService.openDialogue(player, p.pendingDialogue());
            } else {
                p.setPendingDialogue(null);
            }
        }
        Profiles.sync(player);
    }
}
