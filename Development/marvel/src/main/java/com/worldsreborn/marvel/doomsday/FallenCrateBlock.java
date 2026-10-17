package com.worldsreborn.marvel.doomsday;

import com.worldsreborn.core.api.BlockPromptProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Fallen supplies or debris blocking a route (Scenes 7 and 9). Using it moves it out of the way. */
public class FallenCrateBlock extends Block implements BlockPromptProvider {
    public FallenCrateBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            Doomsday.onCrateMoved(sp, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @Nullable
    public Component getInteractionPrompt(Player player, BlockState state, BlockPos pos) {
        return Component.translatable("prompt.wrmarvel.move_crate");
    }
}
