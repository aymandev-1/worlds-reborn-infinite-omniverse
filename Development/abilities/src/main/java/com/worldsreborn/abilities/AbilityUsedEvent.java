package com.worldsreborn.abilities;

import com.worldsreborn.core.model.AbilityDef;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** A validated ability activation. The module that owns the ability performs the effect. */
public class AbilityUsedEvent extends Event {
    private final ServerPlayer player;
    private final AbilityDef ability;

    public AbilityUsedEvent(ServerPlayer player, AbilityDef ability) {
        this.player = player;
        this.ability = ability;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public AbilityDef getAbility() {
        return ability;
    }
}
