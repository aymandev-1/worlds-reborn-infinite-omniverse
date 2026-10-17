package com.worldsreborn.abilities;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client -> server: activate the ability in a slot (0-2). The server checks unlock, slot and cooldown. */
public record UseAbilityPayload(int slot) implements CustomPacketPayload {
    public static final Type<UseAbilityPayload> TYPE = new Type<>(WRAbilities.id("use_ability"));
    public static final StreamCodec<ByteBuf, UseAbilityPayload> CODEC =
            ByteBufCodecs.VAR_INT.map(UseAbilityPayload::new, UseAbilityPayload::slot);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
