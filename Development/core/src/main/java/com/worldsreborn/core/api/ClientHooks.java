package com.worldsreborn.core.api;

import com.worldsreborn.core.WRCore;
import net.minecraft.nbt.CompoundTag;

import java.util.List;

/**
 * Client-side presentation hooks. Worlds Reborn: Interface installs the real implementation; without it the
 * fallback just keeps {@link ClientState} current and logs. Kept free of client classes so servers can load it.
 */
public interface ClientHooks {
    void onProfileSync(CompoundTag data);

    void openDialogue(String dialogueId);

    void startCinematic(String cinematicId, int durationTicks, String argument);

    void showToast(String style, String titleKey, String detailKey, String argument);

    void showInfoPanel(String style, String titleKey, List<String> lineKeys);

    /** A spoken line shown as a subtitle while play continues. {@code voice} may be empty. */
    default void showSpeech(String speakerKey, String textKey, String voice) {
    }

    static ClientHooks get() {
        return Holder.instance;
    }

    static void install(ClientHooks hooks) {
        Holder.instance = hooks;
    }

    final class Holder {
        private static volatile ClientHooks instance = new Fallback();

        private Holder() {
        }
    }

    class Fallback implements ClientHooks {
        @Override
        public void onProfileSync(CompoundTag data) {
            ClientState.update(data);
        }

        @Override
        public void openDialogue(String dialogueId) {
            WRCore.LOGGER.warn("[Worlds Reborn] Dialogue {} requested but Worlds Reborn: Interface is not installed", dialogueId);
        }

        @Override
        public void startCinematic(String cinematicId, int durationTicks, String argument) {
            WRCore.LOGGER.warn("[Worlds Reborn] Cinematic {} requested but Worlds Reborn: Interface is not installed", cinematicId);
        }

        @Override
        public void showToast(String style, String titleKey, String detailKey, String argument) {
        }

        @Override
        public void showInfoPanel(String style, String titleKey, List<String> lineKeys) {
        }
    }
}
