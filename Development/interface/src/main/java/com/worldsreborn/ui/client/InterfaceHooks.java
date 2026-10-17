package com.worldsreborn.ui.client;

import com.worldsreborn.core.WRCore;
import com.worldsreborn.core.api.ClientHooks;
import com.worldsreborn.core.api.ClientState;
import com.worldsreborn.core.model.DialogueDef;
import com.worldsreborn.core.model.WRContent;
import com.worldsreborn.core.network.DialogueResponsePayload;
import com.worldsreborn.ui.client.screen.CinematicScreen;
import com.worldsreborn.ui.client.screen.DialogueScreen;
import com.worldsreborn.ui.client.screen.InfoPanelScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

/** The real client presentation, installed into Core at client setup. */
public final class InterfaceHooks implements ClientHooks {
    /** A dialogue that arrived while a cinematic or info panel was showing; it opens once that screen closes. */
    private static DialogueDef queued;

    /** Called every client tick with no screen open. */
    static void openQueued() {
        if (queued != null) {
            DialogueDef def = queued;
            queued = null;
            Minecraft.getInstance().setScreen(new DialogueScreen(def));
        }
    }

    static void clearQueue() {
        queued = null;
    }
    @Override
    public void onProfileSync(CompoundTag data) {
        ClientState.update(data);
    }

    @Override
    public void openDialogue(String dialogueId) {
        Minecraft mc = Minecraft.getInstance();
        DialogueDef def = WRContent.DIALOGUES.getOrNull(dialogueId);
        if (def == null) {
            // Unknown on this client (mismatched mod versions): answer at once so the server isn't left waiting.
            WRCore.LOGGER.warn("[Worlds Reborn] Unknown dialogue {}; skipping", dialogueId);
            PacketDistributor.sendToServer(new DialogueResponsePayload(dialogueId, "", true));
            return;
        }
        if (mc.screen instanceof DialogueScreen open && open.dialogueId().equals(dialogueId)) {
            return;
        }
        if (mc.screen instanceof CinematicScreen || mc.screen instanceof InfoPanelScreen) {
            queued = def;
            return;
        }
        queued = null;
        mc.setScreen(new DialogueScreen(def));
    }

    @Override
    public void startCinematic(String cinematicId, int durationTicks, String argument) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof CinematicScreen open && open.cinematicId().equals(cinematicId)) {
            return;
        }
        mc.setScreen(new CinematicScreen(cinematicId, durationTicks, argument));
    }

    @Override
    public void showToast(String style, String titleKey, String detailKey, String argument) {
        Component detail = detailKey.isEmpty() ? Component.empty()
                : argument.isEmpty() ? Component.translatable(detailKey) : Component.translatable(detailKey, argument);
        ToastQueue.push(new ToastQueue.Toast(style, Component.translatable(titleKey), detail));
    }

    @Override
    public void showSpeech(String speakerKey, String textKey, String voice) {
        Subtitles.push(new Subtitles.Line(Component.translatable(speakerKey), Component.translatable(textKey), voice));
    }

    @Override
    public void showInfoPanel(String style, String titleKey, List<String> lineKeys) {
        Minecraft.getInstance().setScreen(new InfoPanelScreen(style, titleKey, lineKeys));
    }
}
