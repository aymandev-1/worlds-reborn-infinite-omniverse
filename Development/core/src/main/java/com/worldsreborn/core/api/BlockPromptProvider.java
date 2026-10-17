package com.worldsreborn.core.api;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Blocks that show a contextual interaction prompt on the HUD when the player looks at them. */
public interface BlockPromptProvider {
    @Nullable
    Component getInteractionPrompt(Player player, BlockState state, BlockPos pos);
}
