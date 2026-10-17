package com.worldsreborn.core.api;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/** Entities that show a contextual interaction prompt on the HUD when the player looks at them. */
public interface EntityPromptProvider {
    @Nullable
    Component getInteractionPrompt(Player player);
}
