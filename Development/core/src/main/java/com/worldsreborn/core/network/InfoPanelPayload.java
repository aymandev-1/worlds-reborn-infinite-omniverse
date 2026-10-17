package com.worldsreborn.core.network;

import com.worldsreborn.core.WRCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/**
 * Server -> client: a read-out panel (scanner results, records). Lines are translation keys.
 *
 * @param style visual style id, e.g. "tva" or "scanner"
 */
public record InfoPanelPayload(String style, String titleKey, List<String> lineKeys) implements CustomPacketPayload {
    public static final Type<InfoPanelPayload> TYPE = new Type<>(WRCore.id("info_panel"));
    public static final StreamCodec<ByteBuf, InfoPanelPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, InfoPanelPayload::style,
            ByteBufCodecs.STRING_UTF8, InfoPanelPayload::titleKey,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), InfoPanelPayload::lineKeys,
            InfoPanelPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
