package com.worldsreborn.core.network;

import com.worldsreborn.core.WRCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -> client: play a cinematic. The server runs its own timer, so the player is never stuck if the client
 * fails to answer.
 *
 * @param argument free text for the renderer, e.g. the destination name for a travel transition
 */
public record StartCinematicPayload(String cinematicId, int durationTicks, String argument) implements CustomPacketPayload {
    public static final Type<StartCinematicPayload> TYPE = new Type<>(WRCore.id("start_cinematic"));
    public static final StreamCodec<ByteBuf, StartCinematicPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, StartCinematicPayload::cinematicId,
            ByteBufCodecs.VAR_INT, StartCinematicPayload::durationTicks,
            ByteBufCodecs.STRING_UTF8, StartCinematicPayload::argument,
            StartCinematicPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
