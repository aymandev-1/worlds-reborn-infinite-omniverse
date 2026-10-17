package com.worldsreborn.worlds;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/** Surface and safe-spot finders used for building and for arrival placement. */
public final class Placement {
    private Placement() {
    }

    /** Top solid ground at x/z (ignores leaves), as the position to stand on top of. */
    public static BlockPos surface(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new BlockPos(x, Math.max(y, level.getMinBuildHeight() + 1), z);
    }

    /** True if a player can stand at {@code feet}: solid floor, two free blocks, no fluids. */
    public static boolean isSafe(ServerLevel level, BlockPos feet) {
        BlockState below = level.getBlockState(feet.below());
        BlockState at = level.getBlockState(feet);
        BlockState head = level.getBlockState(feet.above());
        return below.isFaceSturdy(level, feet.below(), net.minecraft.core.Direction.UP)
                && !below.is(BlockTags.FIRE) && below.getFluidState().isEmpty()
                && at.getCollisionShape(level, feet).isEmpty() && at.getFluidState().isEmpty()
                && head.getCollisionShape(level, feet.above()).isEmpty() && head.getFluidState().isEmpty();
    }

    /**
     * Nearest safe standing spot around {@code preferred}, searching outward. Falls back to the surface above
     * the preferred column, and finally to the preferred position itself.
     */
    public static BlockPos findSafe(ServerLevel level, BlockPos preferred, int radius) {
        if (isSafe(level, preferred)) {
            return preferred;
        }
        for (int r = 1; r <= radius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) {
                        continue;
                    }
                    for (int dy = -2; dy <= 3; dy++) {
                        BlockPos p = preferred.offset(dx, dy, dz);
                        if (isSafe(level, p)) {
                            return p;
                        }
                    }
                }
            }
        }
        BlockPos top = surface(level, preferred.getX(), preferred.getZ());
        return isSafe(level, top) ? top : preferred;
    }
}
