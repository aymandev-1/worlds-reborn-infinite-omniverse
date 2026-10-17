package com.worldsreborn.core.network;

import com.worldsreborn.core.WRCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Server -> client: open a registered dialogue. */
public record OpenDialoguePayload(String dialogueId) implements CustomPacketPayload {
    public static final Type<OpenDialoguePayload> TYPE = new Type<>(WRCore.id("open_dialogue"));
    public static final StreamCodec<ByteBuf, OpenDialoguePayload> CODEC =
            ByteBufCodecs.STRING_UTF8.map(OpenDialoguePayload::new, OpenDialoguePayload::dialogueId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
