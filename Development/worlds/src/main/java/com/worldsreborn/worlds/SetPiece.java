package com.worldsreborn.worlds;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Places blocks relative to an origin. Writes skip neighbour and shape updates for speed (so half-built pieces don't pop
 * plants, doors or lanterns off), and every set-piece has a hard block
 * budget so a bad layout can't freeze the server.
 */
public final class SetPiece {
    public static final int DEFAULT_BUDGET = 400_000;

    private final ServerLevel level;
    private final BlockPos origin;
    private final java.util.List<BlockPos> shaped = new java.util.ArrayList<>();
    private int budget;

    public SetPiece(ServerLevel level, BlockPos origin) {
        this(level, origin, DEFAULT_BUDGET);
    }

    public SetPiece(ServerLevel level, BlockPos origin, int budget) {
        this.level = level;
        this.origin = origin;
        this.budget = budget;
    }

    public ServerLevel level() {
        return level;
    }

    public BlockPos origin() {
        return origin;
    }

    public BlockPos at(int x, int y, int z) {
        return origin.offset(x, y, z);
    }

    public SetPiece set(int x, int y, int z, BlockState state) {
        if (budget-- <= 0) {
            throw new IllegalStateException("Set-piece at " + origin + " exceeded its block budget");
        }
        BlockPos pos = origin.offset(x, y, z);
        // Known shape: neighbours aren't reshaped (or knocked out) mid-build; settle() fixes connections afterwards.
        level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        if (connects(state)) {
            shaped.add(pos);
        }
        return this;
    }

    private static boolean connects(BlockState state) {
        Block b = state.getBlock();
        return b instanceof net.minecraft.world.level.block.CrossCollisionBlock || b instanceof net.minecraft.world.level.block.StairBlock
                || b instanceof net.minecraft.world.level.block.WallBlock || b instanceof net.minecraft.world.level.block.RedStoneWireBlock;
    }

    /**
     * Fixes the shapes of panes, bars, fences, walls and stairs placed so far (they are placed without neighbour
     * updates, so they don't know about each other yet). Call once at the end of a build.
     */
    public void settle() {
        for (BlockPos pos : shaped) {
            BlockState state = level.getBlockState(pos);
            BlockState fixed = Block.updateFromNeighbourShapes(state, level, pos);
            if (fixed != state) {
                level.setBlock(pos, fixed, Block.UPDATE_CLIENTS);
            }
        }
        shaped.clear();
    }

    public BlockState get(int x, int y, int z) {
        return level.getBlockState(origin.offset(x, y, z));
    }

    /** Fills the inclusive box. */
    public SetPiece fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                    set(x, y, z, state);
                }
            }
        }
        return this;
    }

    /** Walls of the inclusive box (no floor or ceiling). */
    public SetPiece walls(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
        int ax = Math.min(x1, x2), bx = Math.max(x1, x2), az = Math.min(z1, z2), bz = Math.max(z1, z2);
        fill(ax, y1, az, bx, y2, az, state);
        fill(ax, y1, bz, bx, y2, bz, state);
        fill(ax, y1, az, ax, y2, bz, state);
        fill(bx, y1, az, bx, y2, bz, state);
        return this;
    }

    /** Hollow room: floor, walls, ceiling, air inside. */
    public SetPiece room(int x1, int y1, int z1, int x2, int y2, int z2, BlockState floor, BlockState wall, BlockState ceiling) {
        BlockState air = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        fill(x1, y1 + 1, z1, x2, y2 - 1, z2, air);
        fill(x1, y1, z1, x2, y1, z2, floor);
        fill(x1, y2, z1, x2, y2, z2, ceiling);
        walls(x1, y1 + 1, z1, x2, y2 - 1, z2, wall);
        return this;
    }

    public SetPiece clear(int x1, int y1, int z1, int x2, int y2, int z2) {
        return fill(x1, y1, z1, x2, y2, z2, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
    }
}
