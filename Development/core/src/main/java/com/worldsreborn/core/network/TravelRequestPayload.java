package com.worldsreborn.core.network;

import com.worldsreborn.core.WRCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: travel to a destination from the multiverse map. Validated by the Travel module. */
public record TravelRequestPayload(String destinationId) implements CustomPacketPayload {
    public static final Type<TravelRequestPayload> TYPE = new Type<>(WRCore.id("travel_request"));
    public static final StreamCodec<ByteBuf, TravelRequestPayload> CODEC =
            ByteBufCodecs.STRING_UTF8.map(TravelRequestPayload::new, TravelRequestPayload::destinationId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
