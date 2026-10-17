package com.worldsreborn.core.profile;

import com.worldsreborn.core.model.PlayerProgress;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

/** NeoForge data attachment wrapping the Minecraft-free {@link PlayerProgress} save model. */
public final class PlayerProfile implements INBTSerializable<CompoundTag> {
    private final PlayerProgress progress = new PlayerProgress();

    public PlayerProgress progress() {
        return progress;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return ProfileCodec.write(progress);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        ProfileCodec.read(progress, tag);
    }
}
