package com.worldsreborn.core.network;

import com.worldsreborn.core.WRCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: the cinematic finished or was skipped. */
public record CinematicDonePayload(String cinematicId, boolean skipped) implements CustomPacketPayload {
    public static final Type<CinematicDonePayload> TYPE = new Type<>(WRCore.id("cinematic_done"));
    public static final StreamCodec<ByteBuf, CinematicDonePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CinematicDonePayload::cinematicId,
            ByteBufCodecs.BOOL, CinematicDonePayload::skipped,
            CinematicDonePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
