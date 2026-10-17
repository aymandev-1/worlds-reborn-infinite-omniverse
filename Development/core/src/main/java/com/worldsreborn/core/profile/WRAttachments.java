package com.worldsreborn.core.profile;

import com.worldsreborn.core.WRCore;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class WRAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, WRCore.MODID);

    /** The per-player save profile. Kept through death; saved with the player data. */
    public static final Supplier<AttachmentType<PlayerProfile>> PROFILE = ATTACHMENTS.register("profile",
            () -> AttachmentType.serializable(() -> new PlayerProfile()).copyOnDeath().build());

    private WRAttachments() {
    }
}
