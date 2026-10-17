package com.worldsreborn.marvel.doomsday;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * A point Spider-Man can anchor a web to (Scene 11): the unstable car, the posts either side of the danger area.
 * Unwebbed anchors glint so they are easy to find; webbed ones show the web.
 */
public class WebAnchorBlock extends Block {
    public static final BooleanProperty WEBBED = BooleanProperty.create("webbed");

    public WebAnchorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(WEBBED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WEBBED);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(WEBBED) && random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.WAX_OFF, pos.getX() + random.nextDouble(), pos.getY() + 1.05, pos.getZ() + random.nextDouble(), 0, 0.02, 0);
        }
    }
}
