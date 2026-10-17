package com.worldsreborn.core.network;

import com.worldsreborn.core.WRCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: the player finished (or skipped) a dialogue. choiceId is "" when there was no choice. */
public record DialogueResponsePayload(String dialogueId, String choiceId, boolean skipped) implements CustomPacketPayload {
    public static final Type<DialogueResponsePayload> TYPE = new Type<>(WRCore.id("dialogue_response"));
    public static final StreamCodec<ByteBuf, DialogueResponsePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, DialogueResponsePayload::dialogueId,
            ByteBufCodecs.STRING_UTF8, DialogueResponsePayload::choiceId,
            ByteBufCodecs.BOOL, DialogueResponsePayload::skipped,
            DialogueResponsePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
