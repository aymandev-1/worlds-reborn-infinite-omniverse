package com.worldsreborn.travel;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Computes where a player arrives (and may build the arrival area first). */
@FunctionalInterface
public interface ArrivalResolver {
    Vec3 resolve(ServerPlayer player, ServerLevel level);
}
