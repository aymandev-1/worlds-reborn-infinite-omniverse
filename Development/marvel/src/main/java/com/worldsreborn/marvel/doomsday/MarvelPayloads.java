package com.worldsreborn.marvel.doomsday;

import com.worldsreborn.marvel.WRMarvel;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.Consumer;

/** The Marvel module's own small payloads: the server asks the client to open a story screen, the client reports back. */
public final class MarvelPayloads {
    /** Set by the client module; opens a story screen by name ("recording"). */
    public static Consumer<String> clientScreenOpener = name -> {
    };

    private MarvelPayloads() {
    }

    /** Server -> client: open a story screen. */
    public record OpenScreen(String name) implements CustomPacketPayload {
        public static final Type<OpenScreen> TYPE = new Type<>(WRMarvel.id("open_screen"));
        public static final StreamCodec<ByteBuf, OpenScreen> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, OpenScreen::name, OpenScreen::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** Client -> server: something happened on a story screen ("recording_identified"). Validated against the step. */
    public record ScreenAction(String action) implements CustomPacketPayload {
        public static final Type<ScreenAction> TYPE = new Type<>(WRMarvel.id("screen_action"));
        public static final StreamCodec<ByteBuf, ScreenAction> CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ScreenAction::action, ScreenAction::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar("1");
        r.playToClient(OpenScreen.TYPE, OpenScreen.CODEC, (p, ctx) -> ctx.enqueueWork(() -> clientScreenOpener.accept(p.name())));
        r.playToServer(ScreenAction.TYPE, ScreenAction.CODEC, (p, ctx) -> ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer player && p.action().length() <= 64) {
                Doomsday.onScreenAction(player, p.action());
            }
        }));
    }
}
