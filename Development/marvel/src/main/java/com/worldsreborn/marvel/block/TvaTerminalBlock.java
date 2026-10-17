package com.worldsreborn.marvel.block;

import com.worldsreborn.core.api.BlockPromptProvider;
import com.worldsreborn.marvel.MarvelStory;
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

/** The TVA's records terminal. Using it runs the scan on Steve. */
public class TvaTerminalBlock extends Block implements BlockPromptProvider {
    public TvaTerminalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            if (!com.worldsreborn.marvel.doomsday.Doomsday.onTerminalUsed(sp, pos)) {
                MarvelStory.onTerminalUsed(sp);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    @Nullable
    public Component getInteractionPrompt(Player player, BlockState state, BlockPos pos) {
        return Component.translatable("prompt.wrmarvel.tva_terminal");
    }
}
