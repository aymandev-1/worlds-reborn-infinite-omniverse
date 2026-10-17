package com.worldsreborn.marvel.doomsday;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * One cell of Sue Storm's invisible force field (Scene 15). Solid, so it can carry a person or stop falling debris;
 * placed and released only by the field ability, never mined. Faces between cells are hidden so a field reads as one
 * sheet.
 */
public class ForceFieldBlock extends HalfTransparentBlock {
    public ForceFieldBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState neighbour, Direction direction) {
        return neighbour.is(this) || super.skipRendering(state, neighbour, direction);
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0f;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
