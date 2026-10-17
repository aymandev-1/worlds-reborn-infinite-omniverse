package com.worldsreborn.core.profile;

import com.worldsreborn.core.model.Location;
import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.network.SyncProfilePayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

/** Server-side access to a player's Worlds Reborn profile. */
public final class Profiles {
    private Profiles() {
    }

    public static PlayerProgress get(Player player) {
        return player.getData(WRAttachments.PROFILE).progress();
    }

    /** Sends the player's current profile to their client (HUD, journal, map). */
    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncProfilePayload(ProfileCodec.write(get(player))));
    }

    public static Location locationOf(ServerPlayer player) {
        return new Location(player.level().dimension().location().toString(), player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot());
    }
}
