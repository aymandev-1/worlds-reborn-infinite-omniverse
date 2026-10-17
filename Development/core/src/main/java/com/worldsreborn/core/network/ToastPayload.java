package com.worldsreborn.core.network;

import com.worldsreborn.core.WRCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Server -> client: HUD notification banner.
 *
 * @param style one of quest_started, quest_updated, quest_completed, item, info
 */
public record ToastPayload(String style, String titleKey, String detailKey, String argument) implements CustomPacketPayload {
    public static final Type<ToastPayload> TYPE = new Type<>(WRCore.id("toast"));
    public static final StreamCodec<ByteBuf, ToastPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ToastPayload::style,
            ByteBufCodecs.STRING_UTF8, ToastPayload::titleKey,
            ByteBufCodecs.STRING_UTF8, ToastPayload::detailKey,
            ByteBufCodecs.STRING_UTF8, ToastPayload::argument,
            ToastPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
