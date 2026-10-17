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

/** The interview room's display wall (Scene 6). Using it plays the recording of Steve's New York arrival. */
public class RecordingDisplayBlock extends Block implements BlockPromptProvider {
    public RecordingDisplayBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            Doomsday.onDisplayUsed(sp);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @Nullable
    public Component getInteractionPrompt(Player player, BlockState state, BlockPos pos) {
        return Component.translatable("prompt.wrmarvel.recording_display");
    }
}
