package com.worldsreborn.core.network;

import com.worldsreborn.core.WRCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> client: the player's full profile. */
public record SyncProfilePayload(CompoundTag data) implements CustomPacketPayload {
    public static final Type<SyncProfilePayload> TYPE = new Type<>(WRCore.id("sync_profile"));
    public static final StreamCodec<ByteBuf, SyncProfilePayload> CODEC =
            ByteBufCodecs.COMPOUND_TAG.map(SyncProfilePayload::new, SyncProfilePayload::data);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
