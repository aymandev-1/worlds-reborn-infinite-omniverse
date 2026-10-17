package com.worldsreborn.worlds;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class WorldsConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SPIDER_FREE = B
            .comment("Worlds Reborn is spider-free: stop spiders and cave spiders from spawning in every dimension")
            .define("spiderFree", true);

    public static final ModConfigSpec SPEC = B.build();

    private WorldsConfig() {
    }
}
