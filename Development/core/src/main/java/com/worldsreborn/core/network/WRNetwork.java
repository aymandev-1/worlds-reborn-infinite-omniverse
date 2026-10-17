package com.worldsreborn.core.network;

import com.worldsreborn.core.WRCore;
import com.worldsreborn.core.api.ClientHooks;
import com.worldsreborn.core.event.CinematicDoneEvent;
import com.worldsreborn.core.event.DialogueResponseEvent;
import com.worldsreborn.core.event.TravelRequestEvent;
import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.model.WRContent;
import com.worldsreborn.core.profile.Profiles;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers every cross-module payload. Client-bound payloads go through {@link ClientHooks} so this class never
 * loads client code on a dedicated server. Server-bound payloads are re-posted as events after basic validation;
 * the owning module validates them again against the player's server-side state.
 */
public final class WRNetwork {
    public static final String PROTOCOL = "1";
    private static final int MAX_ID_LENGTH = 128;

    private WRNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar(PROTOCOL);

        r.playToClient(SyncProfilePayload.TYPE, SyncProfilePayload.CODEC,
                (p, ctx) -> ctx.enqueueWork(() -> ClientHooks.get().onProfileSync(p.data())));
        r.playToClient(OpenDialoguePayload.TYPE, OpenDialoguePayload.CODEC,
                (p, ctx) -> ctx.enqueueWork(() -> ClientHooks.get().openDialogue(p.dialogueId())));
        r.playToClient(StartCinematicPayload.TYPE, StartCinematicPayload.CODEC,
                (p, ctx) -> ctx.enqueueWork(() -> ClientHooks.get().startCinematic(p.cinematicId(), p.durationTicks(), p.argument())));
        r.playToClient(ToastPayload.TYPE, ToastPayload.CODEC,
                (p, ctx) -> ctx.enqueueWork(() -> ClientHooks.get().showToast(p.style(), p.titleKey(), p.detailKey(), p.argument())));
        r.playToClient(InfoPanelPayload.TYPE, InfoPanelPayload.CODEC,
                (p, ctx) -> ctx.enqueueWork(() -> ClientHooks.get().showInfoPanel(p.style(), p.titleKey(), p.lineKeys())));
        r.playToClient(SpeechPayload.TYPE, SpeechPayload.CODEC,
                (p, ctx) -> ctx.enqueueWork(() -> ClientHooks.get().showSpeech(p.speakerKey(), p.textKey(), p.voice())));

        r.playToServer(DialogueResponsePayload.TYPE, DialogueResponsePayload.CODEC, (p, ctx) -> ctx.enqueueWork(() -> {
            ServerPlayer player = serverPlayer(ctx);
            if (player != null && valid(p.dialogueId()) && p.choiceId().length() <= MAX_ID_LENGTH) {
                NeoForge.EVENT_BUS.post(new DialogueResponseEvent(player, p.dialogueId(), p.choiceId(), p.skipped()));
            }
        }));
        r.playToServer(CinematicDonePayload.TYPE, CinematicDonePayload.CODEC, (p, ctx) -> ctx.enqueueWork(() -> {
            ServerPlayer player = serverPlayer(ctx);
            if (player != null && valid(p.cinematicId())) {
                NeoForge.EVENT_BUS.post(new CinematicDoneEvent(player, p.cinematicId(), p.skipped()));
            }
        }));
        r.playToServer(TravelRequestPayload.TYPE, TravelRequestPayload.CODEC, (p, ctx) -> ctx.enqueueWork(() -> {
            ServerPlayer player = serverPlayer(ctx);
            if (player != null && WRContent.DESTINATIONS.contains(p.destinationId())) {
                NeoForge.EVENT_BUS.post(new TravelRequestEvent(player, p.destinationId()));
            }
        }));
        r.playToServer(TrackQuestPayload.TYPE, TrackQuestPayload.CODEC, (p, ctx) -> ctx.enqueueWork(() -> {
            ServerPlayer player = serverPlayer(ctx);
            if (player == null) {
                return;
            }
            PlayerProgress progress = Profiles.get(player);
            if (p.questId().isEmpty()) {
                progress.setTrackedQuest(null);
            } else if (WRContent.QUESTS.contains(p.questId()) && progress.isQuestActive(p.questId())) {
                progress.setTrackedQuest(p.questId());
            } else {
                WRCore.debug("Rejected track request for {} from {}", p.questId(), player.getScoreboardName());
                return;
            }
            Profiles.sync(player);
        }));
    }

    private static ServerPlayer serverPlayer(IPayloadContext ctx) {
        return ctx.player() instanceof ServerPlayer sp ? sp : null;
    }

    private static boolean valid(String id) {
        return id != null && !id.isEmpty() && id.length() <= MAX_ID_LENGTH && id.indexOf(':') > 0;
    }
}
