package com.worldsreborn.marvel.block;

import com.worldsreborn.core.api.BlockPromptProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** A flat shimmer on the ground with drifting silver-blue sparks: the last trace of the portal. */
public class FadingTraceBlock extends Block implements BlockPromptProvider {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 1, 16);

    public FadingTraceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 2; i++) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + random.nextDouble(), pos.getY() + 0.1 + random.nextDouble() * 0.4,
                    pos.getZ() + random.nextDouble(), 0, 0.01 + random.nextDouble() * 0.02, 0);
        }
    }

    @Override
    @Nullable
    public Component getInteractionPrompt(Player player, BlockState state, BlockPos pos) {
        return Component.translatable("prompt.wrmarvel.fading_trace");
    }
}
