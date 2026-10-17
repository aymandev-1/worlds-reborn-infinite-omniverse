package com.worldsreborn.ui.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * Plays voice-over lines. A voice is an ordinary sound declared in a mod's sounds.json, played on the Voice/Speech
 * channel so players control its volume with that slider. Only one line plays at a time: a new line cuts the last.
 */
public final class Voice {
    private static SoundInstance current;

    private Voice() {
    }

    public static void play(String soundId) {
        stop();
        ResourceLocation id = soundId == null || soundId.isEmpty() ? null : ResourceLocation.tryParse(soundId);
        if (id == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager().getSoundEvent(id) == null) {
            return; // not recorded yet: the subtitle still shows
        }
        current = new SimpleSoundInstance(id, SoundSource.VOICE, 1f, 1f, RandomSource.create(), false, 0,
                SoundInstance.Attenuation.NONE, 0, 0, 0, true);
        mc.getSoundManager().play(current);
    }

    public static void stop() {
        if (current != null) {
            Minecraft.getInstance().getSoundManager().stop(current);
            current = null;
        }
    }

    public static boolean playing() {
        return current != null && Minecraft.getInstance().getSoundManager().isActive(current);
    }
}
