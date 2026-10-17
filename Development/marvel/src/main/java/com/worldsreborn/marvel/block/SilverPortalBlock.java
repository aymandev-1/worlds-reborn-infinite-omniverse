package com.worldsreborn.marvel.block;

import com.worldsreborn.travel.DestinationPortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The 5x5 silver-blue portal. Each block knows its place in the opening (TILE_X from the west, TILE_Y from the top),
 * so together they show one big swirl instead of 25 small ones; it sheds drifting sparks and hums.
 */
public class SilverPortalBlock extends DestinationPortalBlock {
    public static final IntegerProperty TILE_X = IntegerProperty.create("tile_x", 0, 4);
    public static final IntegerProperty TILE_Y = IntegerProperty.create("tile_y", 0, 4);
    private static final VoxelShape SHAPE = Block.box(0, 0, 6, 16, 16, 10);

    public SilverPortalBlock(String destinationId, Properties properties) {
        super(destinationId, properties);
        registerDefaultState(stateDefinition.any().setValue(TILE_X, 2).setValue(TILE_Y, 2));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TILE_X, TILE_Y);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + random.nextDouble();
            double z = pos.getZ() + 0.5 + (random.nextBoolean() ? 0.3 : -0.3);
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0.01 + random.nextDouble() * 0.02, (z - pos.getZ() - 0.5) * 0.08);
        }
        if (random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.REVERSE_PORTAL, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                    pos.getZ() + 0.5, 0, 0, 0);
        }
        if (state.getValue(TILE_X) == 2 && state.getValue(TILE_Y) == 2 && random.nextInt(60) == 0) {
            level.playLocalSound(pos, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.35f, 1.4f + random.nextFloat() * 0.2f, false);
        }
    }
}
