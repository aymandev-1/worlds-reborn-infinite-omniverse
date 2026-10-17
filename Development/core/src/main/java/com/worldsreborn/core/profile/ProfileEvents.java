package com.worldsreborn.core.profile;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Keeps the client's copy of the profile current. Other modules add their own login recovery on top. */
public final class ProfileEvents {
    private ProfileEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Profiles.sync(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Profiles.sync(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Profiles.sync(player);
        }
    }
}
