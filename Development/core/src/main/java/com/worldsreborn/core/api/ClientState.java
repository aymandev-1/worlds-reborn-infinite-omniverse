package com.worldsreborn.core.api;

import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.profile.ProfileCodec;
import net.minecraft.nbt.CompoundTag;

/** The local player's read-only copy of their profile, as last synced by the server. */
public final class ClientState {
    private static PlayerProgress progress = new PlayerProgress();
    private static long version;

    private ClientState() {
    }

    public static PlayerProgress progress() {
        return progress;
    }

    /** Increments on every sync so screens can refresh cheaply. */
    public static long version() {
        return version;
    }

    public static void update(CompoundTag data) {
        PlayerProgress fresh = new PlayerProgress();
        ProfileCodec.read(fresh, data);
        progress = fresh;
        version++;
    }

    public static void clear() {
        progress = new PlayerProgress();
        version++;
    }
}
