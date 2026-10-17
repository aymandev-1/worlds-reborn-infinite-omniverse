package com.worldsreborn.core.network;

import com.worldsreborn.core.WRCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -> client: a spoken line outside a conversation (a bark), shown as a subtitle while play continues.
 *
 * @param voice sound event id of the voice-over, or "" for none
 */
public record SpeechPayload(String speakerKey, String textKey, String voice) implements CustomPacketPayload {
    public static final Type<SpeechPayload> TYPE = new Type<>(WRCore.id("speech"));
    public static final StreamCodec<ByteBuf, SpeechPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SpeechPayload::speakerKey,
            ByteBufCodecs.STRING_UTF8, SpeechPayload::textKey,
            ByteBufCodecs.STRING_UTF8, SpeechPayload::voice,
            SpeechPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
