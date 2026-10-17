package com.worldsreborn.core.network;

import com.worldsreborn.core.WRCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: choose which quest the HUD tracks. */
public record TrackQuestPayload(String questId) implements CustomPacketPayload {
    public static final Type<TrackQuestPayload> TYPE = new Type<>(WRCore.id("track_quest"));
    public static final StreamCodec<ByteBuf, TrackQuestPayload> CODEC =
            ByteBufCodecs.STRING_UTF8.map(TrackQuestPayload::new, TrackQuestPayload::questId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
