package com.worldsreborn.worlds;

import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** World-wide creature rules. Spider-free: the only spider in Worlds Reborn is the tiny one in Peter Parker's story. */
public final class CreatureRules {
    private CreatureRules() {
    }

    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !WorldsConfig.SPIDER_FREE.getAsBoolean()) {
            return;
        }
        EntityType<?> type = event.getEntity().getType();
        if (type == EntityType.SPIDER || type == EntityType.CAVE_SPIDER) {
            event.setCanceled(true);
        }
    }
}
