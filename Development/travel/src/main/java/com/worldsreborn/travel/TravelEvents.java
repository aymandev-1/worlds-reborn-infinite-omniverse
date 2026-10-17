package com.worldsreborn.travel;

import com.worldsreborn.core.WRCore;
import com.worldsreborn.core.event.CinematicDoneEvent;
import com.worldsreborn.core.event.TravelRequestEvent;
import com.worldsreborn.core.model.Destination;
import com.worldsreborn.core.model.Location;
import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.profile.Profiles;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class TravelEvents {
    private TravelEvents() {
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerProgress p = Profiles.get(player);
            if (p.inTransit()) {
                p.tickTransit();
                if (p.transitTicksLeft() <= 0) {
                    WRCore.debug("Server-timed arrival for {}", player.getScoreboardName());
                    TravelService.arrive(player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onCinematicDone(CinematicDoneEvent event) {
        PlayerProgress p = Profiles.get(event.getPlayer());
        if (p.inTransit() && event.getCinematicId().equals(p.transitCinematic())) {
            TravelService.arrive(event.getPlayer());
        }
    }

    /** Disconnected mid-transit: finish the journey on login so the player is never stranded "between worlds". */
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && Profiles.get(player).inTransit()) {
            WRCore.LOGGER.info("[Worlds Reborn] Completing interrupted transit for {}", player.getScoreboardName());
            TravelService.arrive(player);
        }
    }

    /** Death in a story destination respawns at its checkpoint instead of the Overworld spawn. */
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.isEndConquered()) {
            return;
        }
        Location cp = Profiles.get(player).checkpoint();
        if (cp == null) {
            return;
        }
        ServerLevel level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(cp.dimension())));
        if (level != null) {
            player.teleportTo(level, cp.x(), cp.y(), cp.z(), cp.yaw(), cp.pitch());
        }
    }

    @SubscribeEvent
    public static void onTravelRequest(TravelRequestEvent event) {
        ServerPlayer player = event.getPlayer();
        PlayerProgress p = Profiles.get(player);
        Destination d = TravelService.destination(event.getDestinationId());
        if (d == null || p.inTransit()) {
            return;
        }
        if (!p.hasFlag(WRTravel.FREE_TRAVEL_FLAG)) {
            player.sendSystemMessage(Component.translatable("message.wrtravel.no_free_travel"));
            return;
        }
        if (!TravelService.isUnlocked(player, d)) {
            player.sendSystemMessage(Component.translatable("message.wrtravel.locked", Component.translatable(d.nameKey())));
            return;
        }
        if (player.level().dimension().location().toString().equals(d.dimension())) {
            player.sendSystemMessage(Component.translatable("message.wrtravel.already_here"));
            return;
        }
        TravelService.beginTransit(player, d.id(), null, 40);
    }
}
