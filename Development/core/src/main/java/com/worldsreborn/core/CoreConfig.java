package com.worldsreborn.core;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CoreConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue DEBUG_LOGGING = BUILDER
            .comment("Print detailed story, travel and quest logs. Useful when testing progression.")
            .define("debugLogging", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private CoreConfig() {
    }
}
