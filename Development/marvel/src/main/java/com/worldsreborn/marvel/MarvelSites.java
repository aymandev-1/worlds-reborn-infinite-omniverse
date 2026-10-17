package com.worldsreborn.marvel;

import com.worldsreborn.marvel.block.MarvelBlocks;
import com.worldsreborn.marvel.block.SilverPortalBlock;
import com.worldsreborn.worlds.Placement;
import com.worldsreborn.worlds.SetPiece;
import com.worldsreborn.worlds.WorldSites;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The places of Scenes 1-5, built once per world from code (no structure files) and recorded in {@link WorldSites}.
 * All key positions are offsets from each site's origin so story code never hard-codes world coordinates.
 */
public final class MarvelSites {
    public static final ResourceKey<Level> NEW_YORK = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(MarvelIds.DEST_NEW_YORK));
    public static final ResourceKey<Level> TVA = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(MarvelIds.DEST_TVA));

    // Village (Overworld), relative to the village centre at standing height.
    public static final BlockPos V_CARTOGRAPHER = new BlockPos(5, 0, 11);
    /** Steve's bed (head) in his house west of the plaza, and the spot beside it where he gets up. */
    public static final BlockPos V_STEVE_BED = new BlockPos(-15, 0, 10);
    public static final BlockPos V_STEVE_WAKE = new BlockPos(-14, 0, 10);
    public static final BlockPos V_TRAVELLER = new BlockPos(-22, 0, 16);
    /** Where each background villager (MarvelIds.VILLAGERS, same order) lives. */
    public static final List<BlockPos> V_VILLAGERS = List.of(new BlockPos(12, 0, -4), new BlockPos(14, 0, -18),
            new BlockPos(-4, 0, 4), new BlockPos(4, 0, -4), new BlockPos(-12, 0, 16), new BlockPos(-5, 0, -17));
    /** Ruins origin relative to the village origin. */
    public static final int RUINS_OFFSET_X = 220;

    // Ruins (Overworld), relative to the ruins origin at surface standing height.
    public static final BlockPos R_ENTRANCE = new BlockPos(0, 0, 27);
    public static final BlockPos R_TRAVELLER = new BlockPos(-14, 0, 38);
    public static final BlockPos R_CHAMBER = new BlockPos(0, -11, 0);
    public static final BlockPos R_CORE = new BlockPos(0, -11, -3);
    public static final BlockPos R_PORTAL = new BlockPos(0, -8, -5);
    private static final List<BlockPos> R_WIRE = List.of(new BlockPos(4, -11, 3), new BlockPos(3, -11, 3),
            new BlockPos(1, -11, 3), new BlockPos(0, -11, 3), new BlockPos(0, -11, 2), new BlockPos(0, -11, 0),
            new BlockPos(0, -11, -1));
    /** Gaps the player must fill: (2,-11,3), (0,-11,1), (0,-11,-2). */
    private static final BlockPos R_LEVER = new BlockPos(5, -11, 3);
    private static final BlockPos R_CHEST = new BlockPos(-4, -11, 4);

    // New York (MU-616), relative to (0, 63, 0).
    public static final BlockPos NY_ORIGIN = new BlockPos(0, 63, 0);
    public static final BlockPos NY_ARRIVAL = new BlockPos(5, 0, 19);
    public static final BlockPos NY_TRACE = new BlockPos(6, 0, 22);
    public static final BlockPos NY_SHOP_DOOR = new BlockPos(0, 0, -9);
    public static final List<BlockPos> NY_CUSTOMERS = List.of(new BlockPos(-6, 0, -20), new BlockPos(-4, 0, -21), new BlockPos(5, 0, -21));
    public static final List<BlockPos> NY_ROBBERS = List.of(new BlockPos(-2, 0, -13), new BlockPos(2, 0, -12), new BlockPos(5, 0, -15));
    public static final BlockPos NY_SPIDERMAN_DROP = new BlockPos(0, 9, -6);
    public static final BlockPos NY_MOBIUS = new BlockPos(5, 0, 15);
    // The shop rooftop (Scenes 4-5). The roof deck is the top of y=17, so people stand at y=18.
    public static final BlockPos NY_ROOF_TRACE = new BlockPos(1, 18, -16);
    public static final BlockPos NY_ROOF_SPIDEY = new BlockPos(3, 18, -15);
    public static final BlockPos NY_ROOF_MOBIUS = new BlockPos(-5, 18, -19);
    public static final List<BlockPos> NY_ROOF_AGENTS = List.of(new BlockPos(-3, 18, -18), new BlockPos(-7, 18, -18));
    /** Centre of the bottom row of the TVA time door; the door is 3 wide and 4 tall, facing south. */
    public static final BlockPos NY_ROOF_DOOR = new BlockPos(-5, 18, -21);

    // TVA, relative to (0, 64, 0).
    public static final BlockPos TVA_ORIGIN = new BlockPos(0, 64, 0);
    public static final BlockPos TVA_ARRIVAL = new BlockPos(0, 0, 4);
    public static final BlockPos TVA_RECORDS = new BlockPos(0, 0, -58);
    public static final BlockPos TVA_TERMINAL = new BlockPos(0, 0, -66);
    public static final BlockPos TVA_MOBIUS = new BlockPos(2, 0, -63);
    /** Where Mobius waits when Steve steps out of the time door into the arrival hall. */
    public static final BlockPos TVA_MOBIUS_GREET = new BlockPos(2, 0, -1);
    /** Guard posts: {x, z, yaw}. */
    public static final List<int[]> TVA_GUARDS = List.of(new int[]{-4, -8, 0}, new int[]{4, -8, 0},
            new int[]{-4, -19, 180}, new int[]{4, -19, 180}, new int[]{-5, -49, 0}, new int[]{5, -49, 0},
            new int[]{-10, 8, 180}, new int[]{10, 8, 180});

    private MarvelSites() {
    }

    // ================================================================== lookups

    public static BlockPos villageOrigin(MinecraftServer server) {
        WorldSites sites = WorldSites.get(server);
        if (!sites.has(MarvelIds.SITE_VILLAGE)) {
            buildOverworld(server.overworld(), sites);
        } else if (!sites.hasFlag(MarvelIds.LAYOUT_V2_VILLAGE)) {
            // World made with the first village layout: rebuild it in place.
            BlockPos v = sites.site(MarvelIds.SITE_VILLAGE).orElseThrow().pos();
            buildVillage(new SetPiece(server.overworld(), v));
            sites.setFlag(MarvelIds.LAYOUT_V2_VILLAGE);
        }
        return sites.site(MarvelIds.SITE_VILLAGE).orElseThrow().pos();
    }

    public static BlockPos ruinsOrigin(MinecraftServer server) {
        WorldSites sites = WorldSites.get(server);
        if (!sites.has(MarvelIds.SITE_RUINS)) {
            buildOverworld(server.overworld(), sites);
        }
        BlockPos ruins = sites.site(MarvelIds.SITE_RUINS).orElseThrow().pos();
        if (!sites.hasFlag(MarvelIds.LAYOUT_V4_RUINS)) {
            // Older worlds: the ruins move further out and become the big ancient structure.
            ServerLevel level = server.overworld();
            clearOldRuins(new SetPiece(level, ruins, 60_000));
            BlockPos old = ruins;
            for (var player : level.players()) {
                if (player.blockPosition().closerThan(old, 32)) {
                    BlockPos safe = Placement.findSafe(level, Placement.surface(level, old.getX(), old.getZ() + 30), 6);
                    player.teleportTo(safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5);
                }
            }
            BlockPos village = villageOrigin(server);
            ruins = Placement.surface(level, village.getX() + RUINS_OFFSET_X, village.getZ());
            buildRuins(new SetPiece(level, ruins, 1_500_000));
            sites.record(MarvelIds.SITE_RUINS, Level.OVERWORLD, ruins);
            sites.clearFlag(MarvelIds.WORLD_FLAG_PORTAL_LIT);
            sites.setFlag(MarvelIds.LAYOUT_V4_RUINS);
            sites.setFlag(MarvelIds.LAYOUT_V5_PORTAL);
        }
        if (!sites.hasFlag(MarvelIds.LAYOUT_V5_PORTAL)) {
            // Restyled portal gate; a portal that was already open stays open.
            ServerLevel level = server.overworld();
            boolean lit = portalIsLit(level, ruins);
            buildPortalFrame(new SetPiece(level, ruins, 4_000));
            if (lit) {
                lightPortal(level, ruins);
            }
            sites.setFlag(MarvelIds.LAYOUT_V5_PORTAL);
        }
        return ruins;
    }

    public static BlockPos village(MinecraftServer server, BlockPos offset) {
        return villageOrigin(server).offset(offset);
    }

    public static BlockPos ruins(MinecraftServer server, BlockPos offset) {
        return ruinsOrigin(server).offset(offset);
    }

    public static BlockPos ny(BlockPos offset) {
        return NY_ORIGIN.offset(offset);
    }

    public static BlockPos tva(BlockPos offset) {
        return TVA_ORIGIN.offset(offset);
    }

    public static Vec3 nyArrival(ServerLevel level) {
        ensureNewYork(level);
        return Vec3.atBottomCenterOf(ny(NY_ARRIVAL));
    }

    public static Vec3 tvaArrival(ServerLevel level) {
        ensureTva(level);
        return Vec3.atBottomCenterOf(tva(TVA_ARRIVAL));
    }

    // ================================================================== Overworld: village + ruins

    private static void buildOverworld(ServerLevel level, WorldSites sites) {
        BlockPos spawn = level.getSharedSpawnPos();
        int[][] offsets = {{0, 40}, {40, 0}, {-40, 0}, {0, -40}, {60, 60}, {-60, 60}, {60, -60}, {-60, -60}};
        BlockPos village = null;
        BlockPos ruins = null;
        for (int[] o : offsets) {
            BlockPos v = Placement.surface(level, spawn.getX() + o[0], spawn.getZ() + o[1]);
            BlockPos r = Placement.surface(level, v.getX() + RUINS_OFFSET_X, v.getZ());
            if (dry(level, v) && dry(level, r)) {
                village = v;
                ruins = r;
                break;
            }
        }
        if (village == null) {
            village = Placement.surface(level, spawn.getX() + offsets[0][0], spawn.getZ() + offsets[0][1]);
            ruins = Placement.surface(level, village.getX() + RUINS_OFFSET_X, village.getZ());
        }
        buildVillage(new SetPiece(level, village));
        sites.record(MarvelIds.SITE_VILLAGE, Level.OVERWORLD, village);
        sites.setFlag(MarvelIds.LAYOUT_V2_VILLAGE);
        buildRuins(new SetPiece(level, ruins, 1_500_000));
        sites.record(MarvelIds.SITE_RUINS, Level.OVERWORLD, ruins);
        sites.setFlag(MarvelIds.LAYOUT_V4_RUINS);
        sites.setFlag(MarvelIds.LAYOUT_V5_PORTAL);
    }

    private static boolean dry(ServerLevel level, BlockPos standing) {
        return level.getBlockState(standing.below()).getFluidState().isEmpty() && standing.getY() > level.getSeaLevel() - 2;
    }

    /** Levels a square: grass at y=-1, solid ground below, air above (removes trees and hills). */
    private static void flatten(SetPiece s, int r, int depth, int clearHeight, BlockState top, BlockState fill) {
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                for (int y = 0; y <= clearHeight; y++) {
                    if (!s.get(x, y, z).isAir()) {
                        s.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
                s.set(x, -1, z, top);
                for (int y = -2; y >= -depth; y--) {
                    BlockState b = s.get(x, y, z);
                    if (b.isAir() || !b.getFluidState().isEmpty() || b.canBeReplaced()) {
                        s.set(x, y, z, fill);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ village

    /** Building materials for one house. */
    private record Palette(Block log, Block planks, Block roof, Block roofTop, Block door) {
    }

    private static final Palette OAK = new Palette(Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_PLANKS, Blocks.OAK_DOOR);
    private static final Palette SPRUCE = new Palette(Blocks.SPRUCE_LOG, Blocks.SPRUCE_PLANKS, Blocks.STONE_BRICK_STAIRS, Blocks.STONE_BRICKS, Blocks.SPRUCE_DOOR);
    private static final Palette BIRCH = new Palette(Blocks.STRIPPED_OAK_LOG, Blocks.BIRCH_PLANKS, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_PLANKS, Blocks.BIRCH_DOOR);

    private static void buildVillage(SetPiece s) {
        RandomSource rnd = RandomSource.create(1616L);
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState path = Blocks.DIRT_PATH.defaultBlockState();
        int r = 30;
        flatten(s, r, 6, 24, grass, Blocks.DIRT.defaultBlockState());

        // Two crossing lanes, a little uneven at the edges.
        for (int i = -r; i <= r; i++) {
            for (int w = -1; w <= 1; w++) {
                s.set(i, -1, w, rnd.nextInt(8) == 0 ? Blocks.COARSE_DIRT.defaultBlockState() : path);
                s.set(w, -1, i, rnd.nextInt(8) == 0 ? Blocks.COARSE_DIRT.defaultBlockState() : path);
            }
        }

        // Cobbled plaza with a roofed well.
        for (int x = -6; x <= 6; x++) {
            for (int z = -6; z <= 6; z++) {
                if (Math.abs(x) + Math.abs(z) <= 10) {
                    s.set(x, -1, z, pick(rnd, Blocks.COBBLESTONE.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState(),
                            Blocks.ANDESITE.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState()));
                }
            }
        }
        BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
        s.fill(-2, -3, -2, 2, 0, 2, cobble);
        s.fill(-1, -3, -1, 1, 0, 1, Blocks.WATER.defaultBlockState());
        for (int[] c : new int[][]{{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
            s.fill(c[0], 1, c[1], c[0], 2, c[1], Blocks.OAK_FENCE.defaultBlockState());
        }
        roofStrip(s, -3, 3, -2, 2, 3, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_PLANKS);
        s.set(0, 4, 0, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        s.set(5, 0, -5, Blocks.BELL.defaultBlockState());
        for (int[] p : new int[][]{{6, 6}, {-6, 6}, {6, -6}, {-6, -6}, {3, 18}, {-3, -18}, {18, 3}, {-18, -3}, {3, 28}, {-3, -28}}) {
            lampPost(s, p[0], p[1]);
        }

        // Houses. The cartographer's is the big one east of the plaza.
        house(s, 8, 6, 16, 14, Direction.WEST, OAK);
        s.set(14, 0, 12, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
        s.set(15, 0, 8, Blocks.LECTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LecternBlock.FACING, Direction.WEST));
        s.set(9, 0, 13, Blocks.BARREL.defaultBlockState());
        s.set(9, 0, 7, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
        laneTo(s, 7, 10, 2);
        s.set(6, 0, 12, Blocks.FLOWER_POT.defaultBlockState());

        house(s, -16, 6, -8, 12, Direction.EAST, SPRUCE);
        laneTo(s, -7, 9, -2);
        house(s, -16, -14, -8, -8, Direction.EAST, BIRCH);
        laneTo(s, -7, -11, -2);
        house(s, 8, 18, 15, 24, Direction.WEST, BIRCH);
        laneTo(s, 7, 21, 2);
        house(s, -15, 18, -8, 24, Direction.EAST, OAK);
        laneTo(s, -7, 21, -2);
        house(s, -14, -26, -8, -20, Direction.EAST, SPRUCE);
        laneTo(s, -7, -23, -2);

        // Fields: wheat, carrots and potatoes with irrigation, fenced.
        farm(s, rnd, 6, -16, 20, -6);
        // Animal pen.
        BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
        s.walls(8, 0, -29, 20, 0, -20, fence);
        s.set(14, 0, -20, Blocks.OAK_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, Direction.SOUTH));
        s.set(19, 0, -28, Blocks.HAY_BLOCK.defaultBlockState());
        s.set(18, 0, -28, Blocks.HAY_BLOCK.defaultBlockState());
        s.set(9, 0, -28, Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(net.minecraft.world.level.block.LayeredCauldronBlock.LEVEL, 3));
        animals(s, EntityType.SHEEP, 3, 12, -25);
        animals(s, EntityType.COW, 2, 16, -24);
        animals(s, EntityType.CHICKEN, 3, 11, -22);

        // Trees around the edge, flower beds, odds and ends.
        for (int[] t : new int[][]{{24, 24}, {-24, -26}, {-25, 3}, {25, 6}, {-3, 27}, {5, -26}, {-26, 26}, {26, -4}, {22, 13}}) {
            tree(s, rnd, t[0], t[1]);
        }
        Block[] flowers = {Blocks.POPPY, Blocks.DANDELION, Blocks.CORNFLOWER, Blocks.AZURE_BLUET, Blocks.OXEYE_DAISY, Blocks.ALLIUM};
        for (int i = 0; i < 70; i++) {
            int x = rnd.nextInt(2 * r - 3) - r + 2;
            int z = rnd.nextInt(2 * r - 3) - r + 2;
            if (s.get(x, -1, z).is(Blocks.GRASS_BLOCK) && s.get(x, 0, z).isAir()) {
                s.set(x, 0, z, rnd.nextInt(3) == 0 ? flowers[rnd.nextInt(flowers.length)].defaultBlockState()
                        : Blocks.SHORT_GRASS.defaultBlockState());
            }
        }
        s.set(-4, 0, 7, Blocks.HAY_BLOCK.defaultBlockState()).set(-5, 0, 7, Blocks.HAY_BLOCK.defaultBlockState())
                .set(-5, 1, 7, Blocks.HAY_BLOCK.defaultBlockState());
        s.set(4, 0, -8, Blocks.COMPOSTER.defaultBlockState());
        s.set(-4, 0, -8, Blocks.BARREL.defaultBlockState());
        s.settle();
    }

    /**
     * Timber house with a gable roof (ridge along X), a real door, windows, a bed, a chimney with a smoking campfire
     * and a hanging lantern. {@code door} is the wall the door is in.
     */
    private static void house(SetPiece s, int x1, int z1, int x2, int z2, Direction door, Palette p) {
        BlockState planks = p.planks().defaultBlockState();
        BlockState log = p.log().defaultBlockState();
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        // Foundation, floor, walls with log corners and a log band under the roof.
        s.fill(x1, -1, z1, x2, -1, z2, Blocks.COBBLESTONE.defaultBlockState());
        s.fill(x1 + 1, -1, z1 + 1, x2 - 1, -1, z2 - 1, planks);
        s.walls(x1, 0, z1, x2, 3, z2, planks);
        s.walls(x1, 0, z1, x2, 0, z2, Blocks.COBBLESTONE.defaultBlockState());
        s.walls(x1, 3, z1, x2, 3, z2, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        for (int[] c : new int[][]{{x1, z1}, {x2, z1}, {x1, z2}, {x2, z2}}) {
            s.fill(c[0], 0, c[1], c[0], 3, c[1], log);
        }
        s.fill(x1 + 1, 0, z1 + 1, x2 - 1, 12, z2 - 1, air);
        int top = roofStrip(s, x1 - 1, x2 + 1, z1 - 1, z2 + 1, 3, p.roof(), p.roofTop());
        // Gable ends.
        for (int y = 4; y < top; y++) {
            int k = y - 3;
            for (int x : new int[]{x1, x2}) {
                s.fill(x, y, z1 - 1 + k + 1, x, y, z2 + 1 - k - 1, planks);
            }
        }
        int midX = (x1 + x2) / 2;
        int midZ = (z1 + z2) / 2;
        // Windows two high on every wall, skipping the door.
        for (int x = x1 + 2; x <= x2 - 2; x += 3) {
            for (int z : new int[]{z1, z2}) {
                s.fill(x, 1, z, x, 2, z, glass);
            }
        }
        for (int x : new int[]{x1, x2}) {
            s.fill(x, 1, midZ - 1, x, 2, midZ - 1, glass);
            s.fill(x, 1, midZ + 1, x, 2, midZ + 1, glass);
        }
        int dx = door == Direction.WEST ? x1 : door == Direction.EAST ? x2 : midX;
        int dz = door == Direction.NORTH ? z1 : door == Direction.SOUTH ? z2 : midZ;
        BlockState d = p.door().defaultBlockState().setValue(DoorBlock.FACING, door.getOpposite());
        s.set(dx, 0, dz, d.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        s.set(dx, 1, dz, d.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        s.fill(dx, 2, dz, dx, 2, dz, planks);
        s.set(dx + door.getStepX(), -1, dz + door.getStepZ(), Blocks.DIRT_PATH.defaultBlockState());
        // Bed against the far wall, table, storage.
        int bx = door == Direction.WEST ? x2 - 1 : x1 + 1;
        BlockState bed = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH);
        s.set(bx, 0, z2 - 1, bed.setValue(BedBlock.PART, BedPart.FOOT));
        s.set(bx, 0, z2 - 2, bed.setValue(BedBlock.PART, BedPart.HEAD));
        s.set(bx, 0, z1 + 2, Blocks.CRAFTING_TABLE.defaultBlockState());
        s.set(midX, 0, z1 + 1, Blocks.BARREL.defaultBlockState());
        s.set(midX, 0, z2 - 1, Blocks.POTTED_RED_TULIP.defaultBlockState());
        s.set(midX, top - 1, midZ, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        // Chimney in a back corner, smoking.
        int cx = door == Direction.WEST ? x2 - 1 : x1 + 1;
        s.fill(cx, 0, z1 + 1, cx, top + 1, z1 + 1, Blocks.COBBLESTONE.defaultBlockState());
        s.set(cx, top + 2, z1 + 1, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.SIGNAL_FIRE, false));
    }

    /**
     * Gable roof over x1..x2 / z1..z2 (already including overhang), eaves at height {@code y}. Returns the ridge height.
     */
    private static int roofStrip(SetPiece s, int x1, int x2, int z1, int z2, int y, Block stairs, Block ridge) {
        BlockState north = stairs.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
        BlockState south = stairs.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
        int a = z1;
        int b = z2;
        int h = y;
        while (a < b) {
            s.fill(x1, h, a, x2, h, a, north);
            s.fill(x1, h, b, x2, h, b, south);
            a++;
            b--;
            h++;
        }
        if (a == b) {
            s.fill(x1, h, a, x2, h, a, ridge.defaultBlockState());
            return h;
        }
        return h - 1;
    }

    /** A path from a door out to the lane at x = {@code toX}. */
    private static void laneTo(SetPiece s, int fromX, int z, int toX) {
        int step = toX < fromX ? -1 : 1;
        for (int x = fromX; x != toX + step; x += step) {
            s.set(x, -1, z, Blocks.DIRT_PATH.defaultBlockState());
        }
    }

    private static void lampPost(SetPiece s, int x, int z) {
        s.fill(x, 0, z, x, 2, z, Blocks.SPRUCE_FENCE.defaultBlockState());
        s.set(x, 3, z, Blocks.LANTERN.defaultBlockState());
    }

    private static void farm(SetPiece s, RandomSource rnd, int x1, int z1, int x2, int z2) {
        BlockState fence = Blocks.OAK_FENCE.defaultBlockState();
        s.walls(x1 - 1, 0, z1 - 1, x2 + 1, 0, z2 + 1, fence);
        s.set(x1 - 1, 0, (z1 + z2) / 2, Blocks.OAK_FENCE_GATE.defaultBlockState().setValue(FenceGateBlock.FACING, Direction.WEST));
        Block[] crops = {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES};
        for (int z = z1; z <= z2; z++) {
            boolean water = Math.floorMod(z - z1, 4) == 2;
            for (int x = x1; x <= x2; x++) {
                if (water) {
                    s.set(x, -1, z, Blocks.WATER.defaultBlockState());
                    s.set(x, 0, z, Blocks.AIR.defaultBlockState());
                } else {
                    s.set(x, -1, z, Blocks.FARMLAND.defaultBlockState().setValue(net.minecraft.world.level.block.FarmBlock.MOISTURE, 7));
                    Block crop = crops[Math.floorMod((z - z1) / 4, crops.length)];
                    s.set(x, 0, z, crop.defaultBlockState().setValue(CropBlock.AGE, 3 + rnd.nextInt(5)));
                }
            }
        }
        s.set(x2 + 1, 1, z1 - 1, Blocks.JACK_O_LANTERN.defaultBlockState());
    }

    private static void tree(SetPiece s, RandomSource rnd, int x, int z) {
        int h = 4 + rnd.nextInt(2);
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = h - 2; dy <= h + 1; dy++) {
                    int rr = dy >= h ? 1 : 2;
                    if (Math.abs(dx) <= rr && Math.abs(dz) <= rr && !(Math.abs(dx) == rr && Math.abs(dz) == rr && rnd.nextBoolean())) {
                        s.set(x + dx, dy, z + dz, leaves);
                    }
                }
            }
        }
        s.fill(x, 0, z, x, h, z, Blocks.OAK_LOG.defaultBlockState());
    }

    private static void animals(SetPiece s, EntityType<? extends Mob> type, int count, int x, int z) {
        for (int i = 0; i < count; i++) {
            Mob mob = type.create(s.level());
            if (mob != null) {
                BlockPos p = s.at(x + i, 0, z + (i % 2));
                mob.moveTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, i * 90f, 0f);
                mob.setPersistenceRequired();
                s.level().addFreshEntity(mob);
            }
        }
    }

    /**
     * The ancient structure (Scene 2): a broken outer wall around a stepped stone ziggurat, an avenue of pillars
     * leading to a stairwell in the courtyard, and a great pillared chamber deep under the ziggurat with the portal
     * frame, carved glyph walls, blue soul lanterns and red candle light by the broken redstone machinery.
     */
    private static void buildRuins(SetPiece s) {
        ServerLevel level = s.level();
        RandomSource rnd = RandomSource.create(616L);
        BlockState bricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState cracked = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        BlockState mossy = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        BlockState deep = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState chiseled = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // Clear and level the whole site, then an old cracked stone floor.
        flatten(s, 36, 4, 40, Blocks.GRASS_BLOCK.defaultBlockState(), Blocks.DIRT.defaultBlockState());
        for (int x = -30; x <= 30; x++) {
            for (int z = -30; z <= 34; z++) {
                if (rnd.nextInt(5) != 0) {
                    s.set(x, -1, z, pick(rnd, bricks, cracked, mossy, Blocks.COBBLESTONE.defaultBlockState()));
                }
            }
        }

        // Outer wall: a 63x67 ring, broken into random heights with gaps, corner towers.
        for (int i = -31; i <= 31; i++) {
            for (int[] w : new int[][]{{i, -31}, {i, 35}, {-31, i + 2}, {31, i + 2}}) {
                if (w[1] == 35 && Math.abs(w[0]) <= 4) {
                    continue; // the gate
                }
                int h = rnd.nextInt(7) == 0 ? 0 : 2 + rnd.nextInt(5);
                for (int y = 0; y < h; y++) {
                    s.set(w[0], y, w[1], pick(rnd, bricks, cracked, mossy, mossy));
                }
            }
        }
        for (int[] c : new int[][]{{-31, -31}, {31, -31}, {-31, 35}, {31, 35}}) {
            int h = 7 + rnd.nextInt(5);
            s.fill(c[0] - 1, 0, c[1] - 1, c[0] + 1, h, c[1] + 1, bricks);
            s.fill(c[0] - 1, h + 1, c[1] - 1, c[0] + 1, h + 1, c[1] + 1, Blocks.STONE_BRICK_WALL.defaultBlockState());
        }
        // Gate posts.
        for (int gx : new int[]{-5, 5}) {
            s.fill(gx, 0, 35, gx, 9, 35, bricks);
            s.set(gx, 10, 35, chiseled);
        }

        // Stepped ziggurat over the chamber, four tiers, with a small shrine on top.
        int[][] tiers = {{15, 0, 3}, {11, 3, 3}, {7, 6, 3}, {4, 9, 3}};
        for (int[] t : tiers) {
            int r = t[0];
            for (int y = t[1]; y < t[1] + t[2]; y++) {
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        boolean edge = Math.abs(x) == r || Math.abs(z) == r;
                        if (edge || y == t[1] + t[2] - 1) {
                            s.set(x, y, z, edge && y == t[1] + 1 && (x + z) % 4 == 0 ? chiseled
                                    : pick(rnd, bricks, cracked, mossy, bricks));
                        }
                    }
                }
            }
        }
        s.fill(-2, 12, -2, 2, 12, 2, chiseled);
        for (int[] c : new int[][]{{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
            s.fill(c[0], 13, c[1], c[0], 16, c[1], Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        }
        s.fill(-2, 17, -2, 2, 17, 2, Blocks.DEEPSLATE_TILE_SLAB.defaultBlockState());
        s.set(0, 13, 0, Blocks.LODESTONE.defaultBlockState());
        s.set(0, 16, 0, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        // Collapsed corner of the ziggurat, rubble at its feet.
        s.fill(10, 3, 10, 15, 8, 15, air);
        for (int i = 0; i < 40; i++) {
            int x = 12 + rnd.nextInt(10) - 3;
            int z = 12 + rnd.nextInt(10) - 3;
            if (Math.abs(x) > 15 || Math.abs(z) > 15) {
                s.set(x, 0, z, pick(rnd, cracked, mossy, Blocks.COBBLESTONE.defaultBlockState(), Blocks.MOSSY_COBBLESTONE.defaultBlockState()));
            }
        }
        // Vines down the ziggurat's south face.
        for (int x = -15; x <= 15; x += 3) {
            for (int y = 0; y < 3 + rnd.nextInt(4); y++) {
                if (s.get(x, y, 16).isAir()) {
                    s.set(x, y, 16, Blocks.VINE.defaultBlockState().setValue(net.minecraft.world.level.block.VineBlock.NORTH, true));
                }
            }
        }

        // Avenue of pillars from the gate to the stairwell, some broken.
        for (int z = 19; z <= 33; z += 3) {
            for (int px : new int[]{-5, 5}) {
                int h = rnd.nextInt(4) == 0 ? 1 + rnd.nextInt(2) : 5;
                s.fill(px, 0, z, px, h - 1, z, pick(rnd, bricks, cracked, mossy, bricks));
                if (h == 5) {
                    s.set(px, 5, z, chiseled);
                }
            }
        }

        // Stairwell: from (0,0,24) down to the chamber's south door at walking level -11.
        BlockState stair = Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
        for (int[] c : new int[][]{{-3, 26}, {3, 26}, {-3, 20}, {3, 20}}) {
            s.fill(c[0], 0, c[1], c[0], 2, c[1], bricks);
        }
        s.fill(-2, 0, 25, 2, 0, 25, Blocks.STONE_BRICK_WALL.defaultBlockState());
        s.set(0, 0, 25, air);
        for (int k = 0; k <= 11; k++) {
            int z = 24 - k;
            int y = -k;
            s.fill(-1, y, z, 1, y + 3, z, air);
            s.fill(-1, y - 1, z, 1, y - 1, z, k == 11 ? Blocks.DEEPSLATE_TILES.defaultBlockState() : stair);
            for (int side : new int[]{-2, 2}) {
                s.fill(side, y - 1, z, side, Math.min(y + 3, -1), z, pick(rnd, bricks, cracked, mossy, bricks));
            }
            if (y + 4 < 0) {
                s.fill(-2, y + 4, z, 2, y + 4, z, bricks);
            }
            if (k % 4 == 2) {
                s.set(-1, y + 2, z, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
        }

        // The buried chamber: 29 wide, 27 deep, 9 high, with pillars.
        s.room(-14, -12, -14, 14, -2, 12, Blocks.DEEPSLATE_TILES.defaultBlockState(), deep, bricks);
        for (int i = 0; i < 120; i++) {
            int x = rnd.nextInt(29) - 14;
            int y = -11 + rnd.nextInt(9);
            boolean ns = rnd.nextBoolean();
            s.set(ns ? x : (rnd.nextBoolean() ? -14 : 14), y, ns ? (rnd.nextBoolean() ? -14 : 12) : rnd.nextInt(27) - 14,
                    pick(rnd, Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(), cracked, mossy, deep));
        }
        s.fill(-1, -11, 12, 1, -9, 12, air);
        for (int px : new int[]{-9, 9}) {
            for (int pz : new int[]{-9, -1, 7}) {
                s.fill(px, -11, pz, px, -3, pz, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                s.set(px, -7, pz, Blocks.CHISELED_DEEPSLATE.defaultBlockState());
                s.set(px + (px < 0 ? 1 : -1), -5, pz, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
        }

        // Glyph wall behind the portal.
        BlockState glyphA = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState glyphB = Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
        BlockState glyphC = Blocks.CHISELED_TUFF_BRICKS.defaultBlockState();
        for (int x = -13; x <= 13; x++) {
            for (int y = -11; y <= -3; y++) {
                s.set(x, y, -14, (x + y) % 3 == 0 ? glyphB : (x * y) % 2 == 0 ? glyphA : glyphC);
            }
        }

        buildPortalFrame(s);
        s.fill(-4, -12, -7, 4, -12, -3, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        s.set(R_CORE.getX(), R_CORE.getY(), R_CORE.getZ(), MarvelBlocks.PORTAL_CORE.get().defaultBlockState());

        // Red light around the machinery: lit red candles.
        BlockState candles = Blocks.RED_CANDLE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 3)
                .setValue(net.minecraft.world.level.block.CandleBlock.LIT, true);
        for (int[] c : new int[][]{{-4, -4}, {4, -4}, {-3, -2}, {3, -2}, {6, 3}, {-6, 3}}) {
            s.set(c[0], -11, c[1], candles);
        }

        // Broken redstone line: lever, wire with three gaps, ending at the core.
        BlockPos o = s.origin();
        level.setBlock(o.offset(R_LEVER), Blocks.LEVER.defaultBlockState()
                .setValue(LeverBlock.FACE, AttachFace.FLOOR).setValue(LeverBlock.FACING, Direction.WEST), Block.UPDATE_ALL);
        for (BlockPos w : R_WIRE) {
            BlockPos p = o.offset(w);
            level.setBlock(p, Block.updateFromNeighbourShapes(Blocks.REDSTONE_WIRE.defaultBlockState(), level, p), Block.UPDATE_ALL);
        }

        // Spare redstone in an old chest.
        BlockPos chestPos = o.offset(R_CHEST);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(13, new ItemStack(Items.REDSTONE, 4));
        }
        s.settle();
    }

    /** Removes the first, smaller ruins (older worlds) so only the new structure remains. */
    private static void clearOldRuins(SetPiece s) {
        s.fill(-8, 0, -7, 8, 10, 24, Blocks.AIR.defaultBlockState());
        s.fill(-8, -13, -7, 8, -2, 24, Blocks.STONE.defaultBlockState());
        s.fill(-8, -1, -7, 8, -1, 24, Blocks.GRASS_BLOCK.defaultBlockState());
    }

    private static BlockState pick(RandomSource rnd, BlockState a, BlockState b, BlockState c, BlockState d) {
        return switch (rnd.nextInt(4)) {
            case 0 -> a;
            case 1 -> b;
            case 2 -> c;
            default -> d;
        };
    }

    public static boolean coreIsPowered(ServerLevel overworld, BlockPos ruinsOrigin) {
        return overworld.hasNeighborSignal(ruinsOrigin.offset(R_CORE));
    }

    /**
     * The portal gate: quartz pillars with chiseled corners, set in a deepslate surround, glowing end-rod strips down
     * both sides and an amethyst crown; 5x5 opening, unlit until the redstone is repaired.
     */
    private static void buildPortalFrame(SetPiece s) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState up = Blocks.QUARTZ_PILLAR.defaultBlockState();
        BlockState across = Blocks.QUARTZ_PILLAR.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        BlockState surround = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        // Clear the old gate (and its crown) first.
        s.fill(-4, -11, -6, 4, -3, -4, air);
        s.fill(-4, -11, -5, 4, -4, -5, surround);
        s.fill(-4, -11, -6, 4, -11, -4, Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState());
        s.fill(-3, -10, -5, -3, -6, -5, up);
        s.fill(3, -10, -5, 3, -6, -5, up);
        s.fill(-2, -11, -5, 2, -11, -5, across);
        s.fill(-2, -5, -5, 2, -5, -5, across);
        for (int[] c : new int[][]{{-3, -11}, {3, -11}, {-3, -5}, {3, -5}}) {
            s.set(c[0], c[1], -5, Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState());
        }
        s.fill(-2, -10, -5, 2, -6, -5, air);
        // Light strips down the pillars, front and back.
        for (int z : new int[]{-6, -4}) {
            s.fill(-3, -10, z, -3, -6, z, Blocks.END_ROD.defaultBlockState());
            s.fill(3, -10, z, 3, -6, z, Blocks.END_ROD.defaultBlockState());
        }
        // Amethyst crown with a cluster on top.
        s.fill(-2, -4, -5, 2, -4, -5, Blocks.AMETHYST_BLOCK.defaultBlockState());
        s.set(-3, -4, -5, Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));
        s.set(3, -4, -5, Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
        s.set(0, -3, -5, Blocks.AMETHYST_CLUSTER.defaultBlockState());
        s.set(-1, -3, -5, Blocks.MEDIUM_AMETHYST_BUD.defaultBlockState());
        s.set(1, -3, -5, Blocks.MEDIUM_AMETHYST_BUD.defaultBlockState());
    }

    /** Fills the frame with the silver-blue portal: one big swirl, each block showing its own part. */
    public static void lightPortal(ServerLevel overworld, BlockPos ruinsOrigin) {
        SetPiece s = new SetPiece(overworld, ruinsOrigin, 64);
        BlockState portal = MarvelBlocks.SILVER_PORTAL.get().defaultBlockState();
        for (int x = -2; x <= 2; x++) {
            for (int y = -10; y <= -6; y++) {
                s.set(x, y, -5, portal.setValue(SilverPortalBlock.TILE_X, x + 2).setValue(SilverPortalBlock.TILE_Y, -6 - y));
            }
        }
    }

    public static boolean portalIsLit(ServerLevel overworld, BlockPos ruinsOrigin) {
        return overworld.getBlockState(ruinsOrigin.offset(R_PORTAL)).is(MarvelBlocks.SILVER_PORTAL.get());
    }

    // ================================================================== MU-616 New York

    private static final int NY_LEN = 120;
    private static final int NY_AVE_LEN = 70;
    private static final int[] NY_AVENUES = {-48, 48};

    public static void ensureNewYork(ServerLevel level) {
        WorldSites sites = WorldSites.get(level.getServer());
        if (sites.hasFlag(MarvelIds.LAYOUT_V2_NEW_YORK)) {
            ensureRooftop(level);
            return;
        }
        if (sites.has(MarvelIds.SITE_NEW_YORK)) {
            // First-layout street: clear it before the bigger city goes up.
            new SetPiece(level, NY_ORIGIN, 400_000).clear(-62, 0, -25, 62, 40, 25);
        }
        buildNewYork(new SetPiece(level, NY_ORIGIN, 3_000_000));
        sites.record(MarvelIds.SITE_NEW_YORK, NEW_YORK, NY_ORIGIN);
        sites.setFlag(MarvelIds.LAYOUT_V2_NEW_YORK);
        ensureRooftop(level);
    }

    /**
     * Turns the shop's roof into the Scene 4 rooftop: a walkable deck with a parapet, a water tower, air conditioning
     * units, and a ladder up the storefront. Added on top of an already-built city, so older worlds get it too.
     */
    public static void ensureRooftop(ServerLevel level) {
        WorldSites sites = WorldSites.get(level.getServer());
        if (sites.hasFlag(MarvelIds.LAYOUT_V3_ROOFTOP)) {
            return;
        }
        SetPiece s = new SetPiece(level, NY_ORIGIN, 200_000);
        int front = -9;
        int back = -25;
        BlockState deck = Blocks.SMOOTH_STONE.defaultBlockState();
        BlockState gravel = Blocks.GRAY_CONCRETE.defaultBlockState(); // not powder: powder falls through
        s.fill(-10, 17, back, 10, 17, front, deck);
        for (int x = -9; x <= 9; x += 3) {
            s.fill(x, 17, back + 1, x, 17, front - 1, gravel);
        }
        s.clear(-10, 18, back, 10, 26, front);
        // Parapet with a gap above the ladder.
        BlockState parapet = Blocks.STONE_BRICK_WALL.defaultBlockState();
        for (int x = -10; x <= 10; x++) {
            s.set(x, 18, back, parapet);
            if (x != 9) {
                s.set(x, 18, front, parapet);
            }
        }
        for (int z = back; z <= front; z++) {
            s.set(-10, 18, z, parapet).set(10, 18, z, parapet);
        }
        // Water tower on four legs.
        BlockState leg = Blocks.SPRUCE_FENCE.defaultBlockState();
        BlockState tank = Blocks.SPRUCE_PLANKS.defaultBlockState();
        for (int[] l : new int[][]{{5, -24}, {8, -24}, {5, -21}, {8, -21}}) {
            s.fill(l[0], 18, l[1], l[0], 20, l[1], leg);
        }
        s.fill(5, 21, -24, 8, 24, -21, tank);
        s.fill(6, 25, -23, 7, 25, -22, Blocks.SPRUCE_SLAB.defaultBlockState());
        s.fill(5, 22, -24, 8, 22, -21, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState());
        // Air conditioning units and a roof hatch.
        for (int x : new int[]{-8, -6}) {
            s.fill(x, 18, -12, x + 1, 18, -11, Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState());
            s.set(x, 19, -12, Blocks.IRON_TRAPDOOR.defaultBlockState()).set(x + 1, 19, -12, Blocks.IRON_TRAPDOOR.defaultBlockState());
        }
        s.set(6, 18, -12, Blocks.IRON_BLOCK.defaultBlockState()).set(6, 19, -12, Blocks.LIGHTNING_ROD.defaultBlockState());
        s.set(-2, 18, -23, Blocks.SPRUCE_TRAPDOOR.defaultBlockState());
        // Ladder up the storefront, right of the glass; the awning makes way for it.
        s.set(9, 4, front, Blocks.BRICKS.defaultBlockState());
        s.set(9, 4, front + 1, Blocks.AIR.defaultBlockState());
        s.set(10, 4, front + 1, Blocks.AIR.defaultBlockState());
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.SOUTH);
        for (int y = 0; y <= 17; y++) {
            s.set(9, y, front + 1, ladder);
        }
        s.settle();
        sites.setFlag(MarvelIds.LAYOUT_V3_ROOFTOP);
    }

    public static void placeRoofTrace(ServerLevel ny) {
        BlockPos p = ny(NY_ROOF_TRACE);
        if (ny.isLoaded(p) && !ny.getBlockState(p).is(MarvelBlocks.FADING_TRACE.get())) {
            ny.setBlock(p, MarvelBlocks.FADING_TRACE.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    public static void removeRoofTrace(ServerLevel ny) {
        BlockPos p = ny(NY_ROOF_TRACE);
        if (ny.getBlockState(p).is(MarvelBlocks.FADING_TRACE.get())) {
            ny.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /** Opens (or closes) the TVA time door on the rooftop. @return true if anything changed. */
    public static boolean setTvaDoor(ServerLevel ny, boolean open) {
        BlockPos base = ny(NY_ROOF_DOOR);
        boolean changed = false;
        BlockState frame = Blocks.ORANGE_GLAZED_TERRACOTTA.defaultBlockState();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 0; dy <= 4; dy++) {
                boolean edge = Math.abs(dx) == 2 || dy == 4;
                BlockPos p = base.offset(dx, dy, 0);
                BlockState want = !open ? Blocks.AIR.defaultBlockState()
                        : edge ? frame : MarvelBlocks.TVA_DOORWAY.get().defaultBlockState();
                if (!ny.getBlockState(p).is(want.getBlock())) {
                    ny.setBlock(p, want, Block.UPDATE_CLIENTS);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static void buildNewYork(SetPiece s) {
        RandomSource rnd = RandomSource.create(616616L);

        // Main street (east-west) and two avenues crossing it.
        for (int x = -NY_LEN; x <= NY_LEN; x++) {
            for (int z = -8; z <= 8; z++) {
                s.set(x, -1, z, streetBlock(z, x));
            }
        }
        for (int c : NY_AVENUES) {
            for (int z = -NY_AVE_LEN; z <= NY_AVE_LEN; z++) {
                if (Math.abs(z) <= 8) {
                    continue;
                }
                for (int dx = -8; dx <= 8; dx++) {
                    s.set(c + dx, -1, z, streetBlock(dx, z));
                }
            }
            // Crosswalks where the avenue meets the street.
            for (int dx = -4; dx <= 4; dx++) {
                for (int z : new int[]{-7, -6, 6, 7}) {
                    if (Math.floorMod(dx, 2) == 0) {
                        s.set(c + dx, -1, z, Blocks.WHITE_CONCRETE.defaultBlockState());
                    }
                }
            }
        }

        // Street-front buildings along the main street. Gaps are left for the avenues, the shop and the alley.
        streetRow(s, rnd, -NY_LEN, -57, -9, Direction.SOUTH);
        streetRow(s, rnd, -39, -11, -9, Direction.SOUTH);
        buildShop(s);
        streetRow(s, rnd, 11, 39, -9, Direction.SOUTH);
        streetRow(s, rnd, 57, NY_LEN, -9, Direction.SOUTH);
        streetRow(s, rnd, -NY_LEN, -57, 9, Direction.NORTH);
        streetRow(s, rnd, -39, 3, 9, Direction.NORTH);
        streetRow(s, rnd, 8, 39, 9, Direction.NORTH);
        streetRow(s, rnd, 57, NY_LEN, 9, Direction.NORTH);
        // Along the avenues.
        for (int c : NY_AVENUES) {
            for (int[] zr : new int[][]{{-NY_AVE_LEN, -26}, {26, NY_AVE_LEN}}) {
                avenueRow(s, rnd, zr[0], zr[1], c - 9, Direction.EAST);
                avenueRow(s, rnd, zr[0], zr[1], c + 9, Direction.WEST);
            }
        }
        // Skyline: towers behind the street fronts.
        for (int[] xr : new int[][]{{-22, 22}, {-NY_LEN, -75}, {75, NY_LEN}}) {
            for (int[] zr : new int[][]{{-68, -28}, {28, 68}}) {
                towers(s, rnd, xr[0], xr[1], zr[0], zr[1], zr[0] < 0 ? zr[1] : zr[0]);
            }
        }

        // The dead-end alley on the south side (arrival point and the fading trace).
        BlockState alleyFloor = Blocks.ANDESITE.defaultBlockState();
        s.fill(4, -1, 9, 7, -1, 24, alleyFloor);
        s.fill(4, 0, 9, 7, 30, 24, Blocks.AIR.defaultBlockState());
        s.fill(3, 0, 9, 3, 18, 25, Blocks.BRICKS.defaultBlockState());
        s.fill(8, 0, 9, 8, 22, 25, Blocks.BRICKS.defaultBlockState());
        s.fill(4, 0, 25, 7, 18, 25, Blocks.BRICKS.defaultBlockState());
        s.fill(4, 0, 23, 5, 1, 24, Blocks.GREEN_TERRACOTTA.defaultBlockState());
        s.set(7, 0, 24, Blocks.BARREL.defaultBlockState());
        s.set(7, 0, 16, Blocks.BARREL.defaultBlockState());
        s.set(7, 1, 16, Blocks.COMPOSTER.defaultBlockState());
        for (int y = 3; y <= 15; y += 4) {
            // Fire escape landings.
            s.fill(4, y, 12, 4, y, 20, Blocks.IRON_TRAPDOOR.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.TrapDoorBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP));
            s.fill(5, y + 1, 12, 5, y + 1, 20, Blocks.IRON_BARS.defaultBlockState());
        }
        s.set(7, 3, 19, Blocks.LANTERN.defaultBlockState());
        s.fill(7, 0, 19, 7, 2, 19, Blocks.IRON_BARS.defaultBlockState());

        // Street furniture: lamps, trees, hydrants, parked cars and taxis.
        for (int x = -NY_LEN + 6; x <= NY_LEN - 6; x += 12) {
            if (nearAvenue(x, 10)) {
                continue;
            }
            for (int z : new int[]{-7, 7}) {
                streetLamp(s, x, z, z < 0 ? Direction.SOUTH : Direction.NORTH);
            }
        }
        for (int x = -NY_LEN + 12; x <= NY_LEN - 12; x += 24) {
            if (nearAvenue(x, 11) || Math.abs(x) < 14) {
                continue;
            }
            streetTree(s, rnd, x, -7);
            int sx = x + 12;
            if (sx < NY_LEN - 4 && !nearAvenue(sx, 11) && (sx < 0 || sx > 12)) {
                streetTree(s, rnd, sx, 7);
            }
        }
        for (int x = -NY_LEN + 18; x <= NY_LEN - 18; x += 36) {
            if (!nearAvenue(x, 10) && Math.abs(x) > 12) {
                s.set(x, 0, -6, Blocks.RED_NETHER_BRICK_WALL.defaultBlockState());
                s.set(x + 2, 0, 6, Blocks.RED_NETHER_BRICK_WALL.defaultBlockState());
            }
        }
        for (int c : NY_AVENUES) {
            for (int z = -NY_AVE_LEN + 8; z <= NY_AVE_LEN - 8; z += 12) {
                if (Math.abs(z) > 14) {
                    streetLampZ(s, c - 7, z);
                    streetLampZ(s, c + 7, z);
                }
            }
        }
        int[][] parked = {{-112, 3}, {-100, -4}, {-88, 3}, {-74, -4}, {-30, 3}, {-24, -4}, {-18, 3}, {18, -4},
                {24, 3}, {32, -4}, {66, 3}, {78, -4}, {94, 3}, {108, -4}};
        for (int i = 0; i < parked.length; i++) {
            car(s, rnd, parked[i][0], parked[i][1], i % 3 == 0);
        }
        for (int c : NY_AVENUES) {
            for (int z : new int[]{-60, -40, 22, 44, 62}) {
                carZ(s, rnd, c + (z % 2 == 0 ? 3 : -4), z, z == -40 || z == 44);
            }
        }

        // Roads closed at the edges.
        for (int x : new int[]{-NY_LEN, NY_LEN}) {
            s.fill(x, 0, -8, x, 0, 8, Blocks.ORANGE_CONCRETE.defaultBlockState());
            s.fill(x, 1, -8, x, 1, 8, Blocks.WHITE_CONCRETE.defaultBlockState());
            s.fill(x, 2, -8, x, 40, 8, Blocks.BARRIER.defaultBlockState());
        }
        for (int c : NY_AVENUES) {
            for (int z : new int[]{-NY_AVE_LEN, NY_AVE_LEN}) {
                s.fill(c - 8, 0, z, c + 8, 0, z, Blocks.ORANGE_CONCRETE.defaultBlockState());
                s.fill(c - 8, 1, z, c + 8, 1, z, Blocks.WHITE_CONCRETE.defaultBlockState());
                s.fill(c - 8, 2, z, c + 8, 40, z, Blocks.BARRIER.defaultBlockState());
            }
        }
        s.settle();
    }

    private static boolean nearAvenue(int x, int margin) {
        for (int c : NY_AVENUES) {
            if (Math.abs(x - c) <= margin) {
                return true;
            }
        }
        return false;
    }

    /** Road cross-section: {@code d} is the distance from the centre line, {@code along} the position along it. */
    private static BlockState streetBlock(int d, int along) {
        int a = Math.abs(d);
        if (a <= 4) {
            return d == 0 && Math.floorMod(along, 6) < 3 ? Blocks.YELLOW_CONCRETE.defaultBlockState()
                    : a == 4 && Math.floorMod(along, 9) == 0 ? Blocks.GRAY_CONCRETE.defaultBlockState()
                    : Blocks.BLACK_CONCRETE.defaultBlockState();
        }
        if (a == 5) {
            return Blocks.STONE.defaultBlockState();
        }
        return Math.floorMod(along, 4) == 0 ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.SMOOTH_STONE.defaultBlockState();
    }

    private static final Block[] FACADES = {Blocks.BRICKS, Blocks.STONE_BRICKS, Blocks.GRANITE, Blocks.DEEPSLATE_BRICKS,
            Blocks.LIGHT_GRAY_CONCRETE, Blocks.SANDSTONE, Blocks.MUD_BRICKS, Blocks.POLISHED_ANDESITE, Blocks.RED_TERRACOTTA};
    private static final Block[] AWNINGS = {Blocks.RED_WOOL, Blocks.GREEN_WOOL, Blocks.BLUE_WOOL, Blocks.BLACK_WOOL,
            Blocks.ORANGE_WOOL, Blocks.CYAN_WOOL};

    /** Buildings along the main street between x1 and x2, front wall on z = {@code front}, facing {@code faces}. */
    private static void streetRow(SetPiece s, RandomSource rnd, int x1, int x2, int front, Direction faces) {
        int x = x1;
        while (x2 - x >= 4) {
            int w = 8 + rnd.nextInt(9);
            if (x2 - (x + w) < 6) {
                w = x2 - x;
            }
            int back = faces == Direction.SOUTH ? front - 16 : front + 16;
            building(s, rnd, x, Math.min(front, back), x + w, Math.max(front, back), faces, 14 + rnd.nextInt(28), true);
            x += w;
        }
    }

    /** Buildings along an avenue between z1 and z2, front wall on x = {@code front}. */
    private static void avenueRow(SetPiece s, RandomSource rnd, int z1, int z2, int front, Direction faces) {
        int z = z1;
        while (z2 - z >= 4) {
            int w = 8 + rnd.nextInt(9);
            if (z2 - (z + w) < 6) {
                w = z2 - z;
            }
            int back = faces == Direction.EAST ? front - 16 : front + 16;
            building(s, rnd, Math.min(front, back), z, Math.max(front, back), z + w, faces, 16 + rnd.nextInt(30), true);
            z += w;
        }
    }

    /** Tall towers filling a block, with billboards on some faces toward {@code streetZ}. */
    private static void towers(SetPiece s, RandomSource rnd, int x1, int x2, int z1, int z2, int streetZ) {
        Direction faces = streetZ < 0 ? Direction.SOUTH : Direction.NORTH;
        int x = x1;
        while (x2 - x >= 8) {
            int w = 12 + rnd.nextInt(8);
            if (x2 - (x + w) < 10) {
                w = x2 - x;
            }
            int depth = 14 + rnd.nextInt(10);
            int zf = streetZ;
            int zb = faces == Direction.SOUTH ? zf - depth : zf + depth;
            int h = 45 + rnd.nextInt(50);
            building(s, rnd, x, Math.min(zf, zb), x + w, Math.max(zf, zb), faces, h, false);
            if (rnd.nextInt(3) == 0 && w >= 12) {
                billboard(s, rnd, x + 2, x + w - 2, zf + faces.getStepZ(), h - 18);
            }
            x += w + 2;
        }
    }

    /**
     * One building: shell of facade, a grid of windows (some lit) on the street face, a cornice, and on the roof a
     * parapet plus sometimes a water tower. Street-level shops get a glass front and an awning.
     */
    private static void building(SetPiece s, RandomSource rnd, int x1, int z1, int x2, int z2, Direction faces, int h, boolean shopFront) {
        BlockState wall = FACADES[rnd.nextInt(FACADES.length)].defaultBlockState();
        BlockState dark = Blocks.BLACK_STAINED_GLASS.defaultBlockState();
        BlockState lit = Blocks.OCHRE_FROGLIGHT.defaultBlockState();
        BlockState trim = Blocks.SMOOTH_STONE.defaultBlockState();
        s.walls(x1, 0, z1, x2, h, z2, wall);
        s.fill(x1, h + 1, z1, x2, h + 1, z2, Blocks.GRAY_CONCRETE.defaultBlockState());
        s.walls(x1, h + 2, z1, x2, h + 2, z2, trim);
        boolean alongX = faces.getAxis() == Direction.Axis.Z;
        int fz = faces == Direction.SOUTH ? z2 : z1;
        int fx = faces == Direction.EAST ? x2 : x1;
        int a1 = alongX ? x1 : z1;
        int a2 = alongX ? x2 : z2;
        int[][] faceList = alongX ? new int[][]{{fz, faces.getStepZ()}} : new int[][]{{fx, faces.getStepX()}};
        int f = faceList[0][0];
        int out = faceList[0][1];
        int startY = shopFront ? 6 : 2;
        boolean tall = rnd.nextBoolean();
        for (int a = a1 + 2; a <= a2 - 2; a += 3) {
            for (int y = startY; y < h - 1; y += 4) {
                BlockState win = rnd.nextInt(4) == 0 ? lit : dark;
                put(s, alongX, a, y, f, win);
                put(s, alongX, a, y + 1, f, tall ? win : wall);
                put(s, alongX, a + 1, y, f, win);
                put(s, alongX, a + 1, y + 1, f, tall ? win : wall);
            }
        }
        // Cornice line every floor band on taller buildings.
        for (int a = a1; a <= a2; a++) {
            put(s, alongX, a, h, f + out, Blocks.SMOOTH_STONE_SLAB.defaultBlockState()
                    .setValue(SlabBlock.TYPE, SlabType.TOP));
        }
        if (shopFront) {
            BlockState awning = AWNINGS[rnd.nextInt(AWNINGS.length)].defaultBlockState();
            for (int a = a1 + 1; a <= a2 - 1; a++) {
                put(s, alongX, a, 0, f, Blocks.GLASS_PANE.defaultBlockState());
                put(s, alongX, a, 1, f, Blocks.GLASS_PANE.defaultBlockState());
                put(s, alongX, a, 2, f, Blocks.GLASS_PANE.defaultBlockState());
                put(s, alongX, a, 4, f + out, awning);
                put(s, alongX, a, 3, f, wall);
            }
            int door = (a1 + a2) / 2;
            put(s, alongX, door, 0, f, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            put(s, alongX, door, 1, f, Blocks.DARK_OAK_PLANKS.defaultBlockState());
            // Shop interior glow so the windows aren't black boxes.
            put(s, alongX, door, 2, f - out, Blocks.SEA_LANTERN.defaultBlockState());
        }
        // Rooftop: water tower or AC units.
        int cx = (x1 + x2) / 2;
        int cz = (z1 + z2) / 2;
        if (rnd.nextInt(3) == 0 && x2 - x1 >= 8 && z2 - z1 >= 8) {
            waterTower(s, cx, h + 2, cz);
        } else {
            s.fill(cx - 1, h + 2, cz - 1, cx, h + 2, cz, Blocks.IRON_BLOCK.defaultBlockState());
            s.set(cx + 2, h + 2, cz + 1, Blocks.LIGHTNING_ROD.defaultBlockState());
        }
    }

    /** Sets a block on a building face: along X (z fixed) or along Z (x fixed). */
    private static void put(SetPiece s, boolean alongX, int a, int y, int f, BlockState state) {
        if (alongX) {
            s.set(a, y, f, state);
        } else {
            s.set(f, y, a, state);
        }
    }

    private static void waterTower(SetPiece s, int x, int y, int z) {
        BlockState leg = Blocks.SPRUCE_FENCE.defaultBlockState();
        BlockState tank = Blocks.SPRUCE_PLANKS.defaultBlockState();
        for (int[] c : new int[][]{{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) {
            s.fill(x + c[0], y, z + c[1], x + c[0], y + 2, z + c[1], leg);
        }
        s.fill(x - 1, y + 3, z - 1, x + 1, y + 6, z + 1, tank);
        for (int[] c : new int[][]{{-2, 0}, {2, 0}, {0, -2}, {0, 2}}) {
            s.fill(x + c[0], y + 3, z + c[1], x + c[0], y + 6, z + c[1], tank);
        }
        s.fill(x - 1, y + 7, z, x + 1, y + 7, z, Blocks.SPRUCE_SLAB.defaultBlockState());
        s.fill(x, y + 7, z - 1, x, y + 7, z + 1, Blocks.SPRUCE_SLAB.defaultBlockState());
        s.set(x, y + 8, z, Blocks.SPRUCE_SLAB.defaultBlockState());
    }

    /** A lit advertising board (plain colours, no brands) hung on a tower face. */
    private static void billboard(SetPiece s, RandomSource rnd, int x1, int x2, int z, int y) {
        Block[][] schemes = {{Blocks.MAGENTA_CONCRETE, Blocks.PURPLE_CONCRETE}, {Blocks.CYAN_CONCRETE, Blocks.BLUE_CONCRETE},
                {Blocks.ORANGE_CONCRETE, Blocks.RED_CONCRETE}, {Blocks.LIME_CONCRETE, Blocks.GREEN_CONCRETE}};
        Block[] c = schemes[rnd.nextInt(schemes.length)];
        s.fill(x1, y, z, x2, y + 8, z, Blocks.SEA_LANTERN.defaultBlockState());
        s.fill(x1 + 1, y + 1, z, x2 - 1, y + 4, z, c[0].defaultBlockState());
        s.fill(x1 + 1, y + 5, z, x2 - 1, y + 7, z, c[1].defaultBlockState());
        s.fill(x1 + 2, y + 6, z, x2 - 2, y + 6, z, Blocks.WHITE_CONCRETE.defaultBlockState());
    }

    private static void streetLamp(SetPiece s, int x, int z, Direction toRoad) {
        BlockState pole = Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState();
        s.fill(x, 0, z, x, 5, z, pole);
        s.set(x, 6, z, Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState());
        s.set(x, 6, z + toRoad.getStepZ(), Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState());
        s.set(x, 5, z + toRoad.getStepZ(), Blocks.SEA_LANTERN.defaultBlockState());
    }

    private static void streetLampZ(SetPiece s, int x, int z) {
        s.fill(x, 0, z, x, 5, z, Blocks.POLISHED_BLACKSTONE_WALL.defaultBlockState());
        s.set(x, 6, z, Blocks.SEA_LANTERN.defaultBlockState());
    }

    private static void streetTree(SetPiece s, RandomSource rnd, int x, int z) {
        s.set(x, -1, z, Blocks.COARSE_DIRT.defaultBlockState());
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 3; dy <= 5; dy++) {
                    if (!(Math.abs(dx) + Math.abs(dz) == 2 && dy != 4)) {
                        s.set(x + dx, dy, z + dz, leaves);
                    }
                }
            }
        }
        s.fill(x, 0, z, x, 4, z, Blocks.OAK_LOG.defaultBlockState());
    }

    private static final Block[] CAR_COLOURS = {Blocks.RED_CONCRETE, Blocks.BLUE_CONCRETE, Blocks.WHITE_CONCRETE,
            Blocks.BLACK_CONCRETE, Blocks.GRAY_CONCRETE, Blocks.GREEN_CONCRETE};

    /** A parked car (5 long, 2 wide) along the main street; taxis are yellow with a roof light. */
    private static void car(SetPiece s, RandomSource rnd, int x, int z, boolean taxi) {
        BlockState body = (taxi ? Blocks.YELLOW_CONCRETE : CAR_COLOURS[rnd.nextInt(CAR_COLOURS.length)]).defaultBlockState();
        int z2 = z + 1;
        s.fill(x, 0, z, x + 4, 0, z2, body);
        for (int wx : new int[]{x, x + 4}) {
            s.set(wx, 0, z, Blocks.BLACK_CONCRETE.defaultBlockState()).set(wx, 0, z2, Blocks.BLACK_CONCRETE.defaultBlockState());
        }
        s.fill(x + 1, 1, z, x + 3, 1, z2, Blocks.LIGHT_GRAY_STAINED_GLASS.defaultBlockState());
        s.fill(x + 2, 1, z, x + 2, 1, z2, body);
        if (taxi) {
            s.set(x + 2, 2, z, Blocks.WHITE_CARPET.defaultBlockState());
        }
    }

    private static void carZ(SetPiece s, RandomSource rnd, int x, int z, boolean taxi) {
        BlockState body = (taxi ? Blocks.YELLOW_CONCRETE : CAR_COLOURS[rnd.nextInt(CAR_COLOURS.length)]).defaultBlockState();
        s.fill(x, 0, z, x + 1, 0, z + 4, body);
        for (int wz : new int[]{z, z + 4}) {
            s.set(x, 0, wz, Blocks.BLACK_CONCRETE.defaultBlockState()).set(x + 1, 0, wz, Blocks.BLACK_CONCRETE.defaultBlockState());
        }
        s.fill(x, 1, z + 1, x + 1, 1, z + 3, Blocks.LIGHT_GRAY_STAINED_GLASS.defaultBlockState());
        if (taxi) {
            s.set(x, 2, z + 2, Blocks.WHITE_CARPET.defaultBlockState());
        }
    }

    private static void buildShop(SetPiece s) {
        BlockState wall = Blocks.BRICKS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState glass = Blocks.GLASS_PANE.defaultBlockState();
        int front = -9;
        int back = -25;
        s.walls(-10, 0, front, 10, 16, back, wall);
        s.fill(-10, 17, back, 10, 17, front, Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
        // Ground floor interior.
        s.fill(-9, 0, back + 1, 9, 4, front - 1, air);
        s.fill(-9, -1, back + 1, 9, -1, front - 1, Blocks.POLISHED_ANDESITE.defaultBlockState());
        s.fill(-9, 5, back + 1, 9, 5, front - 1, Blocks.SMOOTH_STONE.defaultBlockState());
        for (int x = -6; x <= 6; x += 4) {
            for (int z = -21; z <= -12; z += 4) {
                s.set(x, 5, z, Blocks.SEA_LANTERN.defaultBlockState());
            }
        }
        // Storefront: glass with a 3-wide doorway in the middle and a striped awning.
        s.fill(-8, 0, front, 8, 3, front, glass);
        s.fill(-1, 0, front, 1, 2, front, air);
        for (int x = -10; x <= 10; x++) {
            s.set(x, 4, front, Math.floorMod(x, 2) == 0 ? Blocks.RED_WOOL.defaultBlockState() : Blocks.WHITE_WOOL.defaultBlockState());
            s.set(x, 4, front + 1, Math.floorMod(x, 2) == 0 ? Blocks.RED_WOOL.defaultBlockState() : Blocks.WHITE_WOOL.defaultBlockState());
        }
        // Counter, register, shelves.
        s.fill(-8, 0, -16, -3, 0, -16, Blocks.SMOOTH_QUARTZ.defaultBlockState());
        s.set(-6, 1, -16, Blocks.STONE_BUTTON.defaultBlockState());
        s.fill(-9, 0, -24, 9, 2, -24, Blocks.BARREL.defaultBlockState());
        s.fill(9, 0, -22, 9, 2, -11, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        s.fill(2, 0, -19, 6, 1, -19, Blocks.SPRUCE_PLANKS.defaultBlockState());
        // Upper floors' windows.
        for (int x = -8; x <= 8; x += 3) {
            for (int y = 7; y <= 14; y += 4) {
                s.set(x, y, front, Blocks.BLACK_STAINED_GLASS.defaultBlockState());
            }
        }
    }

    public static void placeTrace(ServerLevel ny) {
        BlockPos p = ny(NY_TRACE);
        if (!ny.getBlockState(p).is(MarvelBlocks.FADING_TRACE.get())) {
            ny.setBlock(p, MarvelBlocks.FADING_TRACE.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    // ================================================================== TVA

    public static void ensureTva(ServerLevel level) {
        WorldSites sites = WorldSites.get(level.getServer());
        if (sites.hasFlag(MarvelIds.LAYOUT_V2_TVA)) {
            return;
        }
        buildTva(new SetPiece(level, TVA_ORIGIN, 1_000_000));
        sites.record(MarvelIds.SITE_TVA, TVA, TVA_ORIGIN);
        sites.setFlag(MarvelIds.LAYOUT_V2_TVA);
    }

    private static void buildTva(SetPiece s) {
        BlockState floorA = Blocks.POLISHED_GRANITE.defaultBlockState();
        BlockState floorB = Blocks.BROWN_TERRACOTTA.defaultBlockState();
        BlockState wall = Blocks.TERRACOTTA.defaultBlockState();
        BlockState panel = Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState();
        BlockState ceiling = Blocks.SMOOTH_SANDSTONE.defaultBlockState();
        BlockState light = Blocks.OCHRE_FROGLIGHT.defaultBlockState();
        BlockState amber = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState pillar = Blocks.STRIPPED_DARK_OAK_WOOD.defaultBlockState();

        // ---- Arrival hall
        s.room(-12, -1, -10, 12, 9, 10, floorA, wall, ceiling);
        checker(s, -11, -9, 11, 9, floorA, floorB);
        s.walls(-11, 0, -9, 11, 1, 9, panel);
        s.walls(-12, 5, -10, 12, 5, 10, amber);
        for (int x = -8; x <= 8; x += 4) {
            for (int z = -6; z <= 6; z += 4) {
                s.set(x, 9, z, light);
            }
        }
        // The orange time door the player came through (decorative), on the south wall.
        s.fill(-3, 0, 10, 3, 6, 10, Blocks.ORANGE_CONCRETE.defaultBlockState());
        s.fill(-2, 0, 10, 2, 5, 10, Blocks.ORANGE_STAINED_GLASS.defaultBlockState());
        s.fill(-2, 0, 9, 2, 1, 9, air);
        // Benches and check-in desks.
        for (int x : new int[]{-10, 10}) {
            s.fill(x, 0, -6, x, 0, 6, Blocks.SPRUCE_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, x < 0 ? Direction.WEST : Direction.EAST));
        }
        for (int x : new int[]{-7, 7}) {
            s.fill(x - 2, 0, -4, x + 2, 0, -4, Blocks.SPRUCE_PLANKS.defaultBlockState());
            s.set(x, 1, -4, Blocks.BLACK_STAINED_GLASS.defaultBlockState());
        }

        // ---- Corridor, then a big clock on the hall's north wall over the doorway.
        s.room(-4, -1, -16, 4, 6, -10, floorA, wall, ceiling);
        checker(s, -3, -15, 3, -11, floorA, floorB);
        s.set(0, 6, -13, light);
        clockFace(s, 0, 6, -10);
        s.fill(-2, 0, -10, 2, 3, -10, air);
        s.fill(-2, 0, -9, 2, 1, -9, air);

        // ---- Atrium: two storeys, colonnades, balconies, rows of desks.
        int h = 18;
        s.room(-22, -1, -52, 22, h, -16, floorA, wall, ceiling);
        checker(s, -21, -51, 21, -17, floorA, floorB);
        s.fill(-2, 0, -16, 2, 3, -16, air);
        s.walls(-21, 0, -51, 21, 1, -17, panel);
        s.walls(-22, 6, -52, 22, 6, -16, amber);
        s.walls(-22, 15, -52, 22, 15, -16, amber);
        s.fill(-2, 0, -17, 2, 1, -17, air);
        for (int x : new int[]{-12, -11, 11, 12}) {
            for (int z = -46; z <= -22; z += 8) {
                s.fill(x, 0, z, x, h - 1, z, pillar);
                s.fill(x, 0, z + 1, x, h - 1, z + 1, pillar);
            }
        }
        // Balconies along the long walls with railings.
        for (int side : new int[]{-1, 1}) {
            int inner = side * 18;
            int outer = side * 21;
            s.fill(inner, 7, -50, outer, 7, -18, Blocks.SPRUCE_PLANKS.defaultBlockState());
            s.fill(inner, 8, -50, inner, 8, -18, Blocks.SPRUCE_FENCE.defaultBlockState());
            for (int z = -48; z <= -20; z += 7) {
                s.set(inner + side, 8, z, Blocks.BARREL.defaultBlockState());
                s.set(inner + side * 2, 8, z, Blocks.BARREL.defaultBlockState());
            }
        }
        for (int x = -18; x <= 18; x += 6) {
            for (int z = -48; z <= -20; z += 6) {
                s.set(x, h, z, light);
            }
        }
        // Desk rows with chairs and screens in the side aisles.
        for (int x : new int[]{-17, -14, 14, 17}) {
            for (int z = -46; z <= -22; z += 4) {
                s.set(x, 0, z, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
                s.set(x, 1, z, Blocks.BLACK_STAINED_GLASS.defaultBlockState());
                s.set(x, 0, z + 1, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
            }
        }
        // Filing cabinets along the end walls.
        s.fill(-21, 0, -51, -14, 3, -51, Blocks.BARREL.defaultBlockState());
        s.fill(14, 0, -51, 21, 3, -51, Blocks.BARREL.defaultBlockState());
        // ---- Records room
        s.room(-14, -1, -72, 14, 9, -52, floorA, wall, ceiling);
        checker(s, -13, -71, 13, -53, floorA, floorB);
        s.fill(-3, 0, -52, 3, 4, -52, air);
        s.fill(-3, 0, -51, 3, 1, -51, air);
        // The branching-timeline display above the records doorway, facing the atrium.
        s.fill(-8, 8, -52, 8, 13, -52, Blocks.BLACK_CONCRETE.defaultBlockState());
        s.fill(-7, 10, -52, 7, 10, -52, Blocks.ORANGE_STAINED_GLASS.defaultBlockState());
        for (int x = -7; x <= 7; x++) {
            int y = 10 + (int) Math.round(Math.sin(x * 0.7) * 1.4);
            s.set(x, y, -52, Blocks.ORANGE_STAINED_GLASS.defaultBlockState());
        }
        for (int x = -9; x <= 9; x += 6) {
            for (int z = -68; z <= -56; z += 6) {
                s.set(x, 9, z, light);
            }
        }
        for (int x : new int[]{-12, -9, 9, 12}) {
            s.fill(x, 0, -70, x, 3, -55, Blocks.BARREL.defaultBlockState());
        }
        s.fill(-13, 0, -71, 13, 4, -71, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        s.fill(-2, 0, -67, 2, 0, -67, Blocks.SPRUCE_PLANKS.defaultBlockState());
        s.set(TVA_TERMINAL.getX(), TVA_TERMINAL.getY(), TVA_TERMINAL.getZ(), MarvelBlocks.TVA_TERMINAL.get().defaultBlockState());
        s.fill(-4, 1, -71, 4, 6, -71, amber);
        s.fill(-3, 2, -71, 3, 5, -71, Blocks.ORANGE_STAINED_GLASS.defaultBlockState());
        s.settle();
    }

    /** A round clock: amber ring, dark face, two hands. */
    private static void clockFace(SetPiece s, int cx, int cy, int z) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d <= 3.3) {
                    s.set(cx + dx, cy + dy, z, d >= 2.5 ? Blocks.ORANGE_TERRACOTTA.defaultBlockState() : Blocks.BLACK_TERRACOTTA.defaultBlockState());
                }
            }
        }
        s.fill(cx, cy, z, cx, cy + 2, z, Blocks.GOLD_BLOCK.defaultBlockState());
        s.fill(cx, cy, z, cx + 1, cy, z, Blocks.GOLD_BLOCK.defaultBlockState());
    }

    private static void checker(SetPiece s, int x1, int z1, int x2, int z2, BlockState a, BlockState b) {
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                s.set(x, -1, z, Math.floorMod(x + z, 2) == 0 ? a : b);
            }
        }
    }
}
