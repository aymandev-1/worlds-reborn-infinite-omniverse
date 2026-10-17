package com.worldsreborn.travel;

import com.worldsreborn.core.WRCore;
import com.worldsreborn.core.model.Destination;
import com.worldsreborn.core.model.Location;
import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.model.WRContent;
import com.worldsreborn.core.network.StartCinematicPayload;
import com.worldsreborn.core.profile.Profiles;
import com.worldsreborn.travel.event.TravelArrivedEvent;
import com.worldsreborn.worlds.Placement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

public final class TravelService {
    /** Extra server wait beyond the transition length before arriving without the client. */
    public static final int GRACE_TICKS = 60;
    private static final Map<String, ArrivalResolver> RESOLVERS = new HashMap<>();

    private TravelService() {
    }

    /** Campaigns register custom arrival logic (e.g. "arrive in the alley, build the district first"). */
    public static void registerArrivalResolver(String destinationId, ArrivalResolver resolver) {
        RESOLVERS.put(destinationId, resolver);
    }

    public static Destination destination(String id) {
        return WRContent.DESTINATIONS.getOrNull(id);
    }

    public static ResourceKey<Level> dimensionKey(Destination d) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(d.dimension()));
    }

    public static boolean isUnlocked(ServerPlayer player, Destination d) {
        return Profiles.get(player).isDestinationUnlocked(d);
    }

    public static void unlock(ServerPlayer player, String destinationId) {
        if (Profiles.get(player).unlockedDestinations().add(destinationId)) {
            Profiles.sync(player);
        }
    }

    public static void discover(ServerPlayer player, String destinationId) {
        if (Profiles.get(player).discoveredDestinations().add(destinationId)) {
            Profiles.sync(player);
        }
    }

    /**
     * Starts travel. The player stays where they are while the transition plays, then arrives when the client reports
     * the cinematic finished or the server timer runs out, whichever is first.
     *
     * @param cinematicId cinematic to show during transit, or null for the generic loading transition
     */
    public static boolean beginTransit(ServerPlayer player, String destinationId, String cinematicId, int durationTicks) {
        Destination d = destination(destinationId);
        PlayerProgress p = Profiles.get(player);
        if (d == null || p.inTransit()) {
            return false;
        }
        String cinematic = cinematicId == null ? WRTravel.LOADING_CINEMATIC : cinematicId;
        p.setTransit(destinationId, cinematic, durationTicks + GRACE_TICKS);
        player.setInvulnerable(true);
        PacketDistributor.sendToPlayer(player, new StartCinematicPayload(cinematic, durationTicks, d.nameKey()));
        Profiles.sync(player);
        WRCore.debug("{} began transit to {} ({})", player.getScoreboardName(), destinationId, cinematic);
        return true;
    }

    /** Completes a pending transit now: resolves the arrival point and moves the player safely. */
    public static void arrive(ServerPlayer player) {
        PlayerProgress p = Profiles.get(player);
        String destId = p.transitDestination();
        p.setTransit(null, null, 0);
        player.setInvulnerable(player.isCreative() || player.isSpectator() ? player.getAbilities().invulnerable : false);
        Destination d = destination(destId);
        if (d == null) {
            Profiles.sync(player);
            return;
        }
        ServerLevel level = player.server.getLevel(dimensionKey(d));
        if (level == null) {
            WRCore.LOGGER.error("[Worlds Reborn] Destination {} points at missing dimension {}", destId, d.dimension());
            player.sendSystemMessage(Component.translatable("message.wrtravel.missing_dimension", d.dimension()));
            Profiles.sync(player);
            return;
        }

        // Remember where the player left from so "home" travel can bring them back there.
        if (player.level().dimension() == Level.OVERWORLD) {
            p.setReturnLocation(Profiles.locationOf(player));
        }

        ArrivalResolver resolver = RESOLVERS.get(destId);
        Vec3 target = resolver != null ? resolver.resolve(player, level)
                : Vec3.atBottomCenterOf(new BlockPos(d.arrivalX(), d.arrivalY(), d.arrivalZ()));
        BlockPos safe = Placement.findSafe(level, BlockPos.containing(target), 6);
        Vec3 arrival = safe.equals(BlockPos.containing(target)) ? target : Vec3.atBottomCenterOf(safe);

        player.teleportTo(level, arrival.x, arrival.y, arrival.z, player.getYRot(), player.getXRot());
        player.fallDistance = 0;
        player.setDeltaMovement(Vec3.ZERO);

        // Respawn checkpoint: story destinations bring the player back here after death; the Overworld uses vanilla.
        if (level.dimension() == Level.OVERWORLD) {
            p.setCheckpoint(null);
        } else {
            p.setCheckpoint(new Location(level.dimension().location().toString(), arrival.x, arrival.y, arrival.z,
                    player.getYRot(), player.getXRot()));
        }
        p.discoveredDestinations().add(destId);
        Profiles.sync(player);
        WRCore.LOGGER.info("[Worlds Reborn] {} arrived at {} ({} {})", player.getScoreboardName(), destId,
                level.dimension().location(), safe.toShortString());
        NeoForge.EVENT_BUS.post(new TravelArrivedEvent(player, d));
    }

    /** Overworld return point: where the player last left the Overworld, else their spawn. */
    public static Vec3 overworldReturn(ServerPlayer player, ServerLevel overworld) {
        Location r = Profiles.get(player).returnLocation();
        if (r != null && r.dimension().equals(Level.OVERWORLD.location().toString())) {
            return new Vec3(r.x(), r.y(), r.z());
        }
        BlockPos spawn = player.getRespawnPosition() != null && player.getRespawnDimension() == Level.OVERWORLD
                ? player.getRespawnPosition() : overworld.getSharedSpawnPos();
        return Vec3.atBottomCenterOf(Placement.findSafe(overworld, Placement.surface(overworld, spawn.getX(), spawn.getZ()), 8));
    }
}
