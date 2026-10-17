package com.worldsreborn.marvel.doomsday;

import com.worldsreborn.marvel.MarvelIds;
import com.worldsreborn.marvel.MarvelSites;
import com.worldsreborn.marvel.block.MarvelBlocks;
import com.worldsreborn.worlds.SetPiece;
import com.worldsreborn.worlds.WorldSites;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * The places of Act 1 Scenes 6-11: a new wing of the TVA (interview room, operations, reception, the evacuation
 * corridors and the emergency gateway chamber), the Watchers' observatory, and the Scene 11 sites in New York (the
 * traffic accident, an observation rooftop and the Hell's Kitchen meeting rooftop).
 * <p>
 * All positions are offsets: TVA ones from {@link MarvelSites#TVA_ORIGIN}, New York ones from
 * {@link MarvelSites#NY_ORIGIN}, Watchers ones from {@link #WATCHERS_ORIGIN}. Parts of the TVA change during the
 * scenes (crates moved, the walkway collapsing, doors failing); {@link #resetTvaWing} puts them back for a replay.
 */
public final class DoomsdaySites {
    public static final ResourceKey<Level> WATCHERS = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(MarvelIds.DEST_WATCHERS));
    public static final BlockPos WATCHERS_ORIGIN = new BlockPos(0, 100, 0);

    // ---- Scene 6: interview room, corridor, operations
    public static final BlockPos INTERVIEW_TABLE = new BlockPos(24, 0, -62);
    public static final BlockPos SEARCH_TERMINAL = new BlockPos(24, 0, -69);
    public static final BlockPos MOBIUS_SEARCH = new BlockPos(22, 0, -67);
    public static final BlockPos MOBIUS_TABLE = new BlockPos(24, 0, -59);
    public static final BlockPos DISPLAY = new BlockPos(32, 2, -67);
    public static final BlockPos CORRIDOR = new BlockPos(47, 0, -62);
    public static final BlockPos OPS_ENTER = new BlockPos(66, 0, -62);
    public static final BlockPos OPS_SCREEN_VIEW = new BlockPos(84, 0, -62);
    public static final BlockPos OPS_OFFICER = new BlockPos(90, 0, -66);
    public static final BlockPos OPS_MOBIUS = new BlockPos(82, 0, -59);
    public static final List<BlockPos> OPS_STAFF = List.of(new BlockPos(70, 0, -72), new BlockPos(76, 0, -72),
            new BlockPos(70, 0, -52), new BlockPos(76, 0, -52));

    // ---- Scene 7: reception (south of operations)
    public static final BlockPos RECEPTION_MOBIUS = new BlockPos(79, 0, -38);
    public static final BlockPos RECEPTION_WORKER = new BlockPos(86, 0, -30);
    public static final BlockPos MEDIC = new BlockPos(68, 0, -31);
    public static final BlockPos ATTENDANT = new BlockPos(68, 0, -9);
    public static final BlockPos TECHNICIAN = new BlockPos(93, 0, -38);
    public static final BlockPos SECURITY = new BlockPos(70, 0, -36);
    public static final BlockPos SHELF = new BlockPos(63, 0, -40);
    public static final BlockPos POINT_A = new BlockPos(89, 0, -34);
    public static final BlockPos POINT_B = new BlockPos(89, 0, -9);
    public static final BlockPos WALKWAY_NORTH = new BlockPos(78, 0, -28);
    public static final BlockPos WALKWAY_SOUTH = new BlockPos(78, 0, -19);
    public static final BlockPos STEP_AWAY = new BlockPos(84, 0, -26);
    /** Where each refugee waits in reception, in the order of {@link MarvelIds#REFUGEES}. */
    public static final List<BlockPos> REFUGEE_RECEPTION = List.of(POINT_A, POINT_B, new BlockPos(70, 0, -28),
            new BlockPos(86, 0, -36), new BlockPos(91, 0, -36));

    // ---- Scene 8: evacuation corridor (south of reception)
    public static final BlockPos RALLY_EXIT = new BlockPos(86, 0, -5);
    public static final BlockPos RALLY_1 = new BlockPos(84, 0, 6);
    public static final BlockPos RALLY_2 = new BlockPos(84, 0, 28);
    public static final BlockPos RALLY_3 = new BlockPos(84, 0, 42);
    public static final BlockPos RALLY_4 = new BlockPos(84, 0, 60);
    public static final BlockPos RALLY_5 = new BlockPos(84, 0, 76);
    public static final List<BlockPos> GALLERY_SHOOTERS = List.of(new BlockPos(100, 0, 14), new BlockPos(100, 0, 18),
            new BlockPos(100, 0, 22));
    public static final BlockPos SIDE_RIFT = new BlockPos(81, 0, 51);
    public static final BlockPos MOBIUS_DOOR = new BlockPos(80, 0, 65);
    public static final BlockPos MOBIUS_BEYOND = new BlockPos(73, 0, 65);
    public static final BlockPos AGENT_REPORT = new BlockPos(82, 0, 65);
    /** Cover zone along the exposed east side; each z column needs cover at standing height. */
    public static final int COVER_X1 = 87, COVER_X2 = 91, COVER_Z1 = 12, COVER_Z2 = 24;
    public static final int BRIDGE_X1 = 81, BRIDGE_X2 = 91, BRIDGE_Z1 = 32, BRIDGE_Z2 = 37;

    // ---- Scene 9: emergency gateway chamber
    public static final BlockPos COORDINATOR = new BlockPos(82, 0, 108);
    public static final BlockPos GATEWAY_FRONT = new BlockPos(89, 0, 111);
    public static final BlockPos GATEWAY_CONTROLS = new BlockPos(95, 0, 111);
    public static final int GATEWAY_X1 = 86, GATEWAY_X2 = 92, GATEWAY_Z = 114;
    public static final BlockPos BACKUP_CONTROL = new BlockPos(103, 9, 86);
    public static final BlockPos EMERGENCY_DOOR = new BlockPos(77, 0, 96);
    public static final List<BlockPos> SAFE_ROOM_STAFF = List.of(new BlockPos(86, 0, 120), new BlockPos(92, 0, 121));
    public static final BlockPos CHAMBER_CENTRE = new BlockPos(92, 0, 96);

    // ---- Scene 10: observatory (from WATCHERS_ORIGIN)
    public static final BlockPos OBS_ARRIVAL = new BlockPos(0, 0, 18);
    public static final BlockPos OBS_UATU = new BlockPos(0, 0, -4);
    public static final BlockPos OBS_UATU_GREET = new BlockPos(0, 0, 13);
    /** The other Watchers; the first one is the one who speaks. */
    public static final List<BlockPos> OBS_WATCHERS = List.of(new BlockPos(-6, 0, 7), new BlockPos(6, 0, 7),
            new BlockPos(0, 0, 3));
    /** Debris in a U around the front of the refugee gateway (Scene 9): a gap must be cleared to reach it. */
    public static final List<BlockPos> DEBRIS_U;

    static {
        java.util.ArrayList<BlockPos> u = new java.util.ArrayList<>();
        for (int x = 84; x <= 94; x++) {
            u.add(new BlockPos(x, 0, 104));
        }
        for (int z = 105; z <= 113; z++) {
            u.add(new BlockPos(84, 0, z));
            u.add(new BlockPos(94, 0, z));
        }
        DEBRIS_U = List.copyOf(u);
    }

    // ---- Scene 11: New York (from NY_ORIGIN)
    public static final BlockPos ALLEY_WALL = new BlockPos(4, 0, 17);
    public static final BlockPos ALLEY_PAVEMENT = MarvelSites.NY_TRACE;
    public static final BlockPos ALLEY_EXIT = new BlockPos(6, 0, 10);
    public static final BlockPos ACCIDENT = new BlockPos(48, 0, 30);
    public static final List<BlockPos> CAR_ANCHORS = List.of(new BlockPos(39, 4, 35), new BlockPos(48, 2, 35),
            new BlockPos(57, 4, 35));
    public static final List<BlockPos> BARRIER_POSTS = List.of(new BlockPos(42, 1, 27), new BlockPos(54, 1, 27));
    public static final List<BlockPos> OCCUPANTS = List.of(new BlockPos(46, 0, 35), new BlockPos(50, 0, 35));
    public static final BlockPos OCCUPANT_EXIT = new BlockPos(48, 0, 31);
    public static final BlockPos PAVEMENT_SAFE = new BlockPos(42, 0, 22);
    public static final BlockPos WITNESS = new BlockPos(44, 0, 24);
    public static final List<BlockPos> PEDESTRIANS = List.of(new BlockPos(47, 0, 12), new BlockPos(50, 0, 14),
            new BlockPos(45, 0, 16));
    public static final BlockPos PEDESTRIANS_HELD = new BlockPos(48, 0, 23);
    public static final BlockPos PEDESTRIANS_DANGER = new BlockPos(48, 0, 30);
    public static final BlockPos OBSERVATION_ROOF = new BlockPos(64, 21, 49);
    public static final BlockPos HK_ROOF = new BlockPos(-67, 15, 47);
    public static final BlockPos HK_PETER = new BlockPos(-67, 15, 50);
    /** Meeting positions: Daredevil, Jessica Jones, Luke Cage, Danny Rand, Frank Castle (spread around the roof). */
    public static final List<BlockPos> HK_HEROES = List.of(new BlockPos(-67, 15, 43), new BlockPos(-62, 15, 45),
            new BlockPos(-62, 15, 51), new BlockPos(-71, 15, 51), new BlockPos(-73, 15, 44));

    private DoomsdaySites() {
    }

    public static BlockPos tva(BlockPos offset) {
        return MarvelSites.tva(offset);
    }

    public static BlockPos ny(BlockPos offset) {
        return MarvelSites.ny(offset);
    }

    public static BlockPos obs(BlockPos offset) {
        return WATCHERS_ORIGIN.offset(offset);
    }

    // ================================================================== construction zones

    private record Zone(int x1, int y1, int z1, int x2, int y2, int z2) {
        boolean contains(BlockPos p) {
            return p.getX() >= x1 && p.getX() <= x2 && p.getY() >= y1 && p.getY() <= y2 && p.getZ() >= z1 && p.getZ() <= z2;
        }
    }

    /** Where repair panels may be placed, as TVA offsets: the reception walkway, the cover line, the bridge, the climb. */
    private static final List<Zone> ZONES = List.of(
            new Zone(76, -3, -25, 80, -1, -22),
            new Zone(COVER_X1, 0, COVER_Z1 - 1, COVER_X2, 2, COVER_Z2 + 1),
            new Zone(BRIDGE_X1, -3, BRIDGE_Z1, BRIDGE_X2, -1, BRIDGE_Z2),
            new Zone(102, 0, 86, 107, 9, 111));

    public static boolean inConstructionZone(Level level, BlockPos pos) {
        if (!level.dimension().equals(MarvelSites.TVA)) {
            return false;
        }
        BlockPos rel = pos.subtract(MarvelSites.TVA_ORIGIN);
        return ZONES.stream().anyMatch(z -> z.contains(rel));
    }

    // ================================================================== TVA wing

    public static void ensureTvaWing(ServerLevel tva) {
        MarvelSites.ensureTva(tva);
        WorldSites sites = WorldSites.get(tva.getServer());
        if (sites.hasFlag(MarvelIds.LAYOUT_TVA_WING)) {
            return;
        }
        SetPiece s = new SetPiece(tva, MarvelSites.TVA_ORIGIN, 2_500_000);
        buildWing(s);
        resetTvaWing(tva);
        sites.setFlag(MarvelIds.LAYOUT_TVA_WING);
    }

    private static final BlockState FLOOR_A = Blocks.POLISHED_GRANITE.defaultBlockState();
    private static final BlockState FLOOR_B = Blocks.BROWN_TERRACOTTA.defaultBlockState();
    private static final BlockState WALL = Blocks.TERRACOTTA.defaultBlockState();
    private static final BlockState PANEL = Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState();
    private static final BlockState CEILING = Blocks.SMOOTH_SANDSTONE.defaultBlockState();
    private static final BlockState LIGHT = Blocks.OCHRE_FROGLIGHT.defaultBlockState();
    private static final BlockState AMBER = Blocks.ORANGE_TERRACOTTA.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private static void checker(SetPiece s, int x1, int z1, int x2, int z2) {
        for (int x = x1; x <= x2; x++) {
            for (int z = z1; z <= z2; z++) {
                s.set(x, -1, z, Math.floorMod(x + z, 2) == 0 ? FLOOR_A : FLOOR_B);
            }
        }
    }

    private static void lights(SetPiece s, int x1, int z1, int x2, int z2, int y, int step) {
        for (int x = x1; x <= x2; x += step) {
            for (int z = z1; z <= z2; z += step) {
                s.set(x, y, z, LIGHT);
            }
        }
    }

    private static void desk(SetPiece s, int x, int z) {
        s.set(x, 0, z, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        s.set(x + 1, 0, z, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        s.set(x, 1, z, Blocks.BLACK_STAINED_GLASS.defaultBlockState());
        s.set(x + 1, 1, z, Blocks.ORANGE_STAINED_GLASS_PANE.defaultBlockState());
    }

    private static void buildWing(SetPiece s) {
        // ---------------------------------------------------------- interview room (east of the records room)
        s.room(15, -1, -71, 33, 9, -53, FLOOR_A, WALL, CEILING);
        checker(s, 16, -70, 32, -54);
        s.walls(16, 0, -70, 32, 1, -54, PANEL);
        s.walls(15, 5, -71, 33, 5, -53, AMBER);
        lights(s, 18, -68, 30, -56, 9, 6);
        // Interview table and chairs.
        s.fill(22, 0, -63, 26, 0, -61, Blocks.DARK_OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        s.set(24, 1, -62, Blocks.LANTERN.defaultBlockState());
        for (int x : new int[]{23, 25}) {
            s.set(x, 0, -64, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
            s.set(x, 0, -60, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
        }
        // The search terminal on the north wall, with files beside it.
        s.set(SEARCH_TERMINAL.getX(), 0, SEARCH_TERMINAL.getZ(), MarvelBlocks.TVA_TERMINAL.get().defaultBlockState());
        s.fill(19, 0, -70, 21, 2, -70, Blocks.BARREL.defaultBlockState());
        s.fill(27, 0, -70, 29, 2, -70, Blocks.CHISELED_BOOKSHELF.defaultBlockState());
        // The display wall on the east side, north of the corridor door: a framed 3x3 screen.
        s.fill(32, 0, -69, 32, 4, -65, Blocks.BLACK_CONCRETE.defaultBlockState());
        s.fill(32, 1, -68, 32, 3, -66, MarvelBlocks.RECORDING_DISPLAY.get().defaultBlockState());
        // A doorway from the records room (west) into the interview room, through the filing rows.
        s.fill(9, 0, -63, 15, 3, -61, AIR);

        // ---------------------------------------------------------- corridor
        s.room(33, -1, -65, 61, 6, -59, FLOOR_A, WALL, CEILING);
        checker(s, 34, -64, 60, -60);
        s.walls(34, 0, -64, 60, 0, -60, PANEL);
        lights(s, 36, -62, 60, -62, 6, 6);
        for (int x = 38; x <= 58; x += 10) {
            s.fill(x, 1, -64, x, 3, -64, Blocks.ORANGE_STAINED_GLASS.defaultBlockState());
            s.fill(x, 1, -60, x, 3, -60, Blocks.ORANGE_STAINED_GLASS.defaultBlockState());
        }
        s.fill(33, 0, -63, 33, 3, -61, AIR); // from the interview room

        // ---------------------------------------------------------- operations
        s.room(61, -1, -82, 97, 15, -42, FLOOR_A, WALL, CEILING);
        checker(s, 62, -81, 96, -43);
        s.walls(61, 7, -82, 97, 7, -42, AMBER);
        lights(s, 64, -79, 94, -45, 15, 6);
        // The big screen on the east wall.
        s.fill(96, 1, -76, 96, 13, -48, Blocks.ORANGE_TERRACOTTA.defaultBlockState());
        s.fill(96, 2, -75, 96, 12, -49, Blocks.BLACK_CONCRETE.defaultBlockState());
        for (int z = -74; z <= -50; z += 2) {
            int y = 7 + (int) Math.round(Math.sin(z * 0.5) * 3);
            s.set(96, y, z, Blocks.ORANGE_STAINED_GLASS.defaultBlockState());
            s.set(96, y + 1, z + 1, Blocks.YELLOW_STAINED_GLASS.defaultBlockState());
        }
        // Rows of desks facing the screen, and a raised officer's platform.
        for (int x = 68; x <= 80; x += 6) {
            for (int z = -76; z <= -48; z += 4) {
                if (z >= -64 && z <= -60) {
                    continue; // aisle
                }
                desk(s, x, z);
            }
        }
        s.fill(88, 0, -70, 93, 0, -64, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        s.fill(88, 1, -70, 93, 1, -70, Blocks.SPRUCE_FENCE.defaultBlockState());
        s.fill(61, 0, -63, 61, 3, -61, AIR); // from the corridor

        // ---------------------------------------------------------- reception (south of operations)
        s.room(61, -1, -42, 97, 11, 0, FLOOR_A, WALL, CEILING);
        checker(s, 62, -41, 96, -1);
        s.walls(61, 5, -42, 97, 5, 0, AMBER);
        lights(s, 64, -40, 94, -2, 11, 6);
        s.fill(75, 0, -42, 83, 5, -42, AIR); // wide opening to operations
        // The service trench across the room; the only way across is the walkway at x 77..79.
        s.fill(62, -6, -25, 96, -1, -22, AIR);
        s.fill(62, -7, -25, 96, -7, -22, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        s.fill(62, -6, -26, 96, -1, -26, Blocks.SMOOTH_SANDSTONE.defaultBlockState());
        s.fill(62, -6, -21, 96, -1, -21, Blocks.SMOOTH_SANDSTONE.defaultBlockState());
        s.fill(61, -7, -26, 61, -1, -21, Blocks.SMOOTH_SANDSTONE.defaultBlockState());
        s.fill(97, -7, -26, 97, -1, -21, Blocks.SMOOTH_SANDSTONE.defaultBlockState());
        for (int z : new int[]{-25, -22}) {
            // Recovery ladders at the west end, one up each side.
            Direction facing = z == -25 ? Direction.SOUTH : Direction.NORTH;
            s.fill(63, -6, z, 63, -1, z, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, facing));
        }
        s.fill(62, 0, -26, 96, 0, -26, Blocks.SPRUCE_FENCE.defaultBlockState());
        s.fill(62, 0, -21, 96, 0, -21, Blocks.SPRUCE_FENCE.defaultBlockState());
        s.fill(76, 0, -26, 80, 0, -26, AIR);
        s.fill(76, 0, -21, 80, 0, -21, AIR);
        s.fill(63, 0, -26, 63, 0, -26, AIR);
        s.fill(63, 0, -21, 63, 0, -21, AIR);
        // Supply shelf behind a trolley.
        s.set(SHELF.getX(), 0, SHELF.getZ(), MarvelBlocks.SUPPLY_SHELF.get().defaultBlockState());
        s.fill(62, 1, -41, 64, 2, -41, Blocks.BARREL.defaultBlockState());
        s.fill(64, 0, -38, 66, 0, -38, Blocks.IRON_BLOCK.defaultBlockState());
        s.fill(64, 1, -38, 66, 1, -38, Blocks.IRON_BARS.defaultBlockState());
        // Medical station and accommodation cots.
        for (int z = -34; z <= -28; z += 3) {
            s.fill(64, 0, z, 65, 0, z, Blocks.WHITE_WOOL.defaultBlockState());
        }
        for (int x = 64; x <= 74; x += 3) {
            for (int z = -6; z <= -2; z += 4) {
                s.fill(x, 0, z, x + 1, 0, z, Blocks.LIGHT_BLUE_WOOL.defaultBlockState());
            }
        }
        // Reception desks at points A and B.
        s.fill(91, 0, -32, 94, 0, -32, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        s.fill(91, 0, -12, 94, 0, -12, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
        // The working evacuation gateway on the east wall.
        doorFrame(s, 97, -36, -30, Blocks.ORANGE_CONCRETE.defaultBlockState());
        // A sealed side door on the west wall (unscheduled activation in Scene 7).
        doorFrame(s, 61, -37, -31, Blocks.GRAY_CONCRETE.defaultBlockState());
        // Emergency exit south.
        s.fill(84, 0, 0, 88, 3, 0, AIR);
        s.fill(83, 4, 0, 89, 4, 0, Blocks.LIME_CONCRETE.defaultBlockState());

        // ---------------------------------------------------------- evacuation corridor
        s.room(80, -1, 0, 92, 7, 80, FLOOR_A, WALL, CEILING);
        checker(s, 81, 1, 91, 79);
        lights(s, 83, 3, 89, 77, 7, 6);
        s.fill(84, 0, 0, 88, 3, 0, AIR);
        // Emergency signs every few metres.
        for (int z = 6; z <= 76; z += 10) {
            s.set(81, 3, z, Blocks.LIME_STAINED_GLASS.defaultBlockState());
        }
        // Exposed section: the east wall is gone; beyond a drop, the gallery the attackers hold.
        s.fill(92, 0, COVER_Z1, 92, 6, COVER_Z2, AIR);
        s.fill(93, -12, COVER_Z1 - 2, 97, 6, COVER_Z2 + 2, AIR);
        s.fill(93, -13, COVER_Z1 - 2, 97, -13, COVER_Z2 + 2, Blocks.DEEPSLATE_TILES.defaultBlockState());
        s.room(97, -1, COVER_Z1 - 3, 104, 7, COVER_Z2 + 3, Blocks.POLISHED_BLACKSTONE.defaultBlockState(),
                Blocks.BLACKSTONE.defaultBlockState(), Blocks.BLACKSTONE.defaultBlockState());
        s.fill(97, 0, COVER_Z1 - 2, 97, 6, COVER_Z2 + 2, AIR);
        s.fill(97, -1, COVER_Z1 - 2, 97, -1, COVER_Z2 + 2, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        // The drop is walled in (the TVA floats in a void) and has a ladder back up.
        BlockState pit = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        s.fill(92, -13, COVER_Z1 - 3, 97, -2, COVER_Z1 - 3, pit);
        s.fill(92, -13, COVER_Z2 + 3, 97, -2, COVER_Z2 + 3, pit);
        s.fill(92, -13, COVER_Z1 - 2, 92, -2, COVER_Z2 + 2, pit);
        s.fill(97, -13, COVER_Z1 - 2, 97, -2, COVER_Z2 + 2, pit);
        s.fill(93, -12, 18, 93, -1, 18, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST));
        // Chasm under the bridge section, with a recovery ladder on the north face.
        s.fill(BRIDGE_X1, -12, BRIDGE_Z1, BRIDGE_X2, -2, BRIDGE_Z2, AIR);
        s.fill(BRIDGE_X1, -13, BRIDGE_Z1, BRIDGE_X2, -13, BRIDGE_Z2, Blocks.DEEPSLATE_TILES.defaultBlockState());
        s.fill(BRIDGE_X1 - 1, -12, BRIDGE_Z1 - 1, BRIDGE_X2 + 1, -2, BRIDGE_Z1 - 1, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        s.fill(BRIDGE_X1 - 1, -12, BRIDGE_Z2 + 1, BRIDGE_X2 + 1, -2, BRIDGE_Z2 + 1, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        s.fill(BRIDGE_X1 - 1, -13, BRIDGE_Z1, BRIDGE_X1 - 1, -2, BRIDGE_Z2, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        s.fill(BRIDGE_X2 + 1, -13, BRIDGE_Z1, BRIDGE_X2 + 1, -2, BRIDGE_Z2, Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        s.fill(82, -12, BRIDGE_Z1, 82, -2, BRIDGE_Z1, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
        // Side doors on the west wall: the rift attackers come through, and the route Mobius takes back.
        s.fill(80, 0, MOBIUS_DOOR.getZ() - 1, 80, 2, MOBIUS_DOOR.getZ() + 1, AIR);
        s.room(66, -1, MOBIUS_DOOR.getZ() - 3, 80, 6, MOBIUS_DOOR.getZ() + 3, FLOOR_A, WALL, CEILING);
        s.fill(80, 0, MOBIUS_DOOR.getZ() - 1, 80, 2, MOBIUS_DOOR.getZ() + 1, AIR);
        doorFrameZ(s, 80, SIDE_RIFT.getZ() - 2, SIDE_RIFT.getZ() + 2, Blocks.GRAY_CONCRETE.defaultBlockState());
        s.fill(84, 0, 80, 88, 3, 80, AIR);

        // ---------------------------------------------------------- emergency gateway chamber
        s.room(76, -1, 80, 108, 16, 114, FLOOR_A, WALL, CEILING);
        checker(s, 77, 81, 107, 113);
        s.walls(76, 8, 80, 108, 8, 114, AMBER);
        lights(s, 79, 83, 105, 111, 16, 6);
        s.fill(84, 0, 80, 88, 3, 80, AIR);
        // The refugee gateway (south wall) and the intact reception behind it.
        s.room(82, -1, 114, 96, 8, 128, FLOOR_A, WALL, CEILING);
        checker(s, 83, 115, 95, 127);
        lights(s, 85, 117, 93, 125, 8, 4);
        s.fill(84, 0, 125, 86, 0, 125, Blocks.LIGHT_BLUE_WOOL.defaultBlockState());
        s.fill(90, 0, 125, 92, 0, 125, Blocks.LIGHT_BLUE_WOOL.defaultBlockState());
        s.fill(GATEWAY_X1 - 1, 0, GATEWAY_Z, GATEWAY_X2 + 1, 6, GATEWAY_Z, Blocks.ORANGE_CONCRETE.defaultBlockState());
        // Controls and the coordinator's post.
        s.set(GATEWAY_CONTROLS.getX(), 0, GATEWAY_CONTROLS.getZ(), MarvelBlocks.TVA_TERMINAL.get().defaultBlockState());
        // Backup control platform, reached by a stair that has partly fallen away.
        s.fill(100, 8, 83, 107, 8, 89, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        s.fill(100, 9, 83, 107, 9, 83, Blocks.IRON_BARS.defaultBlockState());
        s.fill(100, 9, 83, 100, 9, 89, Blocks.IRON_BARS.defaultBlockState());
        s.set(BACKUP_CONTROL.getX(), BACKUP_CONTROL.getY(), BACKUP_CONTROL.getZ(), MarvelBlocks.TVA_TERMINAL.get().defaultBlockState());
        // Emergency doorway frame on the west wall (inactive until the backup control is used).
        doorFrameZ(s, 76, EMERGENCY_DOOR.getZ() - 3, EMERGENCY_DOOR.getZ() + 3, Blocks.ORANGE_CONCRETE.defaultBlockState());
        // Dark backing behind every doorway set in an outer wall, so nobody can walk out into the void.
        BlockState back = Blocks.BLACK_CONCRETE.defaultBlockState();
        s.fill(98, 0, -36, 98, 6, -30, back);
        s.fill(60, 0, -37, 60, 6, -31, back);
        s.fill(79, 0, SIDE_RIFT.getZ() - 2, 79, 6, SIDE_RIFT.getZ() + 2, back);
        s.fill(75, 0, EMERGENCY_DOOR.getZ() - 3, 75, 6, EMERGENCY_DOOR.getZ() + 3, back);
        // Machinery along the east wall.
        for (int z = 92; z <= 110; z += 6) {
            s.fill(107, 0, z, 107, 5, z + 2, Blocks.COPPER_BULB.defaultBlockState());
        }
        s.settle();
    }

    /** A 7-wide, 7-high door frame in a wall at x, spanning z1..z2 (opening z1+1..z2-1, y 0..4). */
    private static void doorFrame(SetPiece s, int x, int z1, int z2, BlockState frame) {
        s.fill(x, 0, z1, x, 6, z2, frame);
    }

    private static void doorFrameZ(SetPiece s, int x, int z1, int z2, BlockState frame) {
        s.fill(x, 0, z1, x, 6, z2, frame);
    }

    /** Fills a door opening (inside a frame on a wall at x) with the given state. */
    public static void setDoorX(ServerLevel tva, int x, int z1, int z2, BlockState state) {
        for (int z = z1; z <= z2; z++) {
            for (int y = 0; y <= 4; y++) {
                tva.setBlock(tva(new BlockPos(x, y, z)), state, Block.UPDATE_CLIENTS);
            }
        }
    }

    /** Fills the refugee gateway opening (south wall). */
    public static void setGateway(ServerLevel tva, BlockState state) {
        for (int x = GATEWAY_X1; x <= GATEWAY_X2; x++) {
            for (int y = 0; y <= 4; y++) {
                tva.setBlock(tva(new BlockPos(x, y, GATEWAY_Z)), state, Block.UPDATE_CLIENTS);
            }
        }
    }

    public static void setEvacGateway(ServerLevel tva, boolean working) {
        setDoorX(tva, 97, -35, -31, working ? MarvelBlocks.TVA_DOORWAY.get().defaultBlockState()
                : Blocks.ORANGE_CONCRETE.defaultBlockState());
    }

    public static void setRiftDoor(ServerLevel tva, boolean open) {
        setDoorX(tva, 61, -36, -32, open ? MarvelBlocks.RIFT_DOORWAY.get().defaultBlockState()
                : Blocks.GRAY_CONCRETE.defaultBlockState());
    }

    public static void setSideRift(ServerLevel tva, boolean open) {
        setDoorX(tva, 80, SIDE_RIFT.getZ() - 1, SIDE_RIFT.getZ() + 1, open ? MarvelBlocks.RIFT_DOORWAY.get().defaultBlockState()
                : Blocks.GRAY_CONCRETE.defaultBlockState());
    }

    public static void setEmergencyDoor(ServerLevel tva, boolean open) {
        setDoorX(tva, 76, EMERGENCY_DOOR.getZ() - 2, EMERGENCY_DOOR.getZ() + 2, open ? MarvelBlocks.TVA_DOORWAY.get().defaultBlockState()
                : Blocks.ORANGE_CONCRETE.defaultBlockState());
    }

    /** True once the bridge section has collapsed (any of its floor is gone). */
    public static void collapseBridge(ServerLevel tva) {
        for (int x = BRIDGE_X1; x <= BRIDGE_X2; x++) {
            for (int z = BRIDGE_Z1; z <= BRIDGE_Z2; z++) {
                tva.setBlock(tva(new BlockPos(x, -1, z)), AIR, Block.UPDATE_ALL);
            }
        }
    }

    public static void blockMobiusRoute(ServerLevel tva) {
        for (int z = MOBIUS_DOOR.getZ() - 1; z <= MOBIUS_DOOR.getZ() + 1; z++) {
            for (int y = 0; y <= 2; y++) {
                BlockState rubble = (y + z) % 2 == 0 ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.ANDESITE.defaultBlockState();
                tva.setBlock(tva(new BlockPos(80, y, z)), rubble, Block.UPDATE_ALL);
            }
            tva.setBlock(tva(new BlockPos(81, 0, z)), Blocks.GRAVEL.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /**
     * Puts every part the scenes change back to its starting state: supplies on the walkway, the walkway gap, an empty
     * cover line, the intact bridge floor, open side route, gateways working, the emergency doorway closed and the
     * damaged backup stair. Repair panels left in any construction zone are removed.
     */
    public static void resetTvaWing(ServerLevel tva) {
        SetPiece s = new SetPiece(tva, MarvelSites.TVA_ORIGIN, 200_000);
        // Construction zones cleared of panels.
        for (Zone z : ZONES) {
            for (int x = z.x1; x <= z.x2; x++) {
                for (int y = z.y1; y <= z.y2; y++) {
                    for (int zz = z.z1; zz <= z.z2; zz++) {
                        if (s.get(x, y, zz).is(MarvelBlocks.REPAIR_PANEL.get())) {
                            s.set(x, y, zz, AIR);
                        }
                    }
                }
            }
        }
        // Reception: the walkway with its broken middle and the fallen supplies on the north approach.
        s.fill(76, -1, -25, 80, -1, -22, AIR);
        s.fill(76, -1, -26, 80, -1, -26, Blocks.SPRUCE_PLANKS.defaultBlockState());
        s.fill(76, -1, -21, 80, -1, -21, Blocks.SPRUCE_PLANKS.defaultBlockState());
        s.set(76, -1, -25, Blocks.SPRUCE_PLANKS.defaultBlockState());
        s.set(80, -1, -22, Blocks.SPRUCE_PLANKS.defaultBlockState());
        for (int x = 76; x <= 80; x++) {
            s.set(x, 0, -27, MarvelBlocks.FALLEN_CRATE.get().defaultBlockState());
        }
        s.set(77, 1, -27, MarvelBlocks.FALLEN_CRATE.get().defaultBlockState());
        s.set(79, 1, -27, MarvelBlocks.FALLEN_CRATE.get().defaultBlockState());
        // Doors.
        setEvacGateway(tva, true);
        setRiftDoor(tva, false);
        setSideRift(tva, false);
        setEmergencyDoor(tva, false);
        setGateway(tva, MarvelBlocks.TVA_DOORWAY.get().defaultBlockState());
        // Bridge floor intact (it collapses during Scene 8).
        s.fill(BRIDGE_X1, -1, BRIDGE_Z1, BRIDGE_X2, -1, BRIDGE_Z2, FLOOR_B);
        // Mobius's route open again.
        s.fill(80, 0, MOBIUS_DOOR.getZ() - 1, 80, 2, MOBIUS_DOOR.getZ() + 1, AIR);
        s.fill(81, 0, MOBIUS_DOOR.getZ() - 1, 81, 0, MOBIUS_DOOR.getZ() + 1, AIR);
        // Debris across the gateway approach.
        for (BlockPos d : DEBRIS_U) {
            s.fill(d.getX(), 0, d.getZ(), d.getX(), 1, d.getZ(), MarvelBlocks.DEBRIS.get().defaultBlockState());
        }
        // The stair to the backup platform: stairs up the east side with a fallen middle section.
        for (int y = 0; y <= 8; y++) {
            int z = 110 - y;
            s.fill(106, y, z, 107, y, z, y >= 3 && y <= 5 ? AIR
                    : Blocks.POLISHED_DEEPSLATE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
            if (y > 0) {
                s.fill(106, 0, z, 107, y - 1, z, y >= 3 && y <= 5 ? AIR : Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            }
        }
        s.fill(106, 8, 90, 107, 8, 101, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        s.fill(106, 8, 94, 107, 8, 96, AIR);
        s.settle();
    }

    // ================================================================== cover and bridge checks

    private static boolean solid(ServerLevel tva, int x, int y, int z) {
        BlockPos p = tva(new BlockPos(x, y, z));
        return tva.getBlockState(p).isCollisionShapeFullBlock(tva, p);
    }

    /**
     * Cover is useful when nearly every metre of the exposed side has something solid at both crouching and standing
     * height between the corridor and the gallery. Any arrangement counts; no exact pattern is required.
     */
    public static int coverColumns(ServerLevel tva) {
        int covered = 0;
        for (int z = COVER_Z1; z <= COVER_Z2; z++) {
            boolean low = false;
            boolean high = false;
            for (int x = COVER_X1; x <= COVER_X2; x++) {
                low |= solid(tva, x, 0, z);
                high |= solid(tva, x, 1, z);
            }
            if (low && high) {
                covered++;
            }
        }
        return covered;
    }

    public static int coverNeeded() {
        return COVER_Z2 - COVER_Z1 - 1;
    }

    /**
     * Whether a person can walk across the gap: a breadth-first search over standing cells (solid footing, two blocks
     * of headroom) from the north edge of the gap to the south edge. Any bridge shape that can be walked counts.
     */
    public static boolean walkable(ServerLevel tva, int x1, int x2, int zFrom, int zTo, int floorY) {
        java.util.ArrayDeque<int[]> queue = new java.util.ArrayDeque<>();
        java.util.Set<Long> seen = new java.util.HashSet<>();
        for (int x = x1; x <= x2; x++) {
            if (standable(tva, x, floorY, zFrom - 1)) {
                queue.add(new int[]{x, zFrom - 1});
            }
        }
        while (!queue.isEmpty()) {
            int[] c = queue.poll();
            if (!seen.add(((long) c[0] << 32) ^ (c[1] & 0xffffffffL))) {
                continue;
            }
            if (c[1] >= zTo + 1) {
                return true;
            }
            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                int nx = c[0] + d[0], nz = c[1] + d[1];
                if (nx < x1 || nx > x2 || nz < zFrom - 1 || nz > zTo + 1) {
                    continue;
                }
                if (standable(tva, nx, floorY, nz)) {
                    queue.add(new int[]{nx, nz});
                }
            }
        }
        return false;
    }

    private static boolean standable(ServerLevel tva, int x, int floorY, int z) {
        BlockPos foot = tva(new BlockPos(x, floorY, z));
        BlockState below = tva.getBlockState(foot);
        return below.isFaceSturdy(tva, foot, Direction.UP)
                && tva.getBlockState(foot.above()).getCollisionShape(tva, foot.above()).isEmpty()
                && tva.getBlockState(foot.above(2)).getCollisionShape(tva, foot.above(2)).isEmpty();
    }

    public static boolean walkwayRepaired(ServerLevel tva) {
        return walkable(tva, 76, 80, -25, -22, -1);
    }

    public static boolean bridgeBuilt(ServerLevel tva) {
        return walkable(tva, BRIDGE_X1, BRIDGE_X2, BRIDGE_Z1, BRIDGE_Z2, -1);
    }

    // ================================================================== Watchers' observatory

    public static Vec3 watchersArrival(ServerLevel level) {
        ensureObservatory(level);
        return Vec3.atBottomCenterOf(obs(OBS_ARRIVAL));
    }

    public static void ensureObservatory(ServerLevel level) {
        WorldSites sites = WorldSites.get(level.getServer());
        if (sites.hasFlag(MarvelIds.LAYOUT_WATCHERS)) {
            return;
        }
        buildObservatory(new SetPiece(level, WATCHERS_ORIGIN, 600_000));
        sites.setFlag(MarvelIds.LAYOUT_WATCHERS);
    }

    private static void buildObservatory(SetPiece s) {
        BlockState calcite = Blocks.CALCITE.defaultBlockState();
        BlockState quartz = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState gold = Blocks.GOLD_BLOCK.defaultBlockState();
        BlockState deep = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        BlockState glass = Blocks.BLACK_STAINED_GLASS.defaultBlockState();
        BlockState pillar = Blocks.QUARTZ_PILLAR.defaultBlockState();
        int r = 26;
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                double d = Math.sqrt(x * x + z * z);
                if (d > r + 0.5) {
                    continue;
                }
                BlockState top = d > r - 1.5 ? gold : Math.abs(d - 17) < 0.6 ? gold : d < 6 ? quartz
                        : (int) (Math.atan2(z, x) * 8 / Math.PI + 16) % 2 == 0 ? calcite : deep;
                s.set(x, -1, z, top);
                // An underside tapering into the void.
                int depth = (int) Math.max(1, (r - d) * 0.6);
                s.fill(x, -1 - depth, z, x, -2, z, deep);
            }
        }
        // Pillars around the rim, with a soft light on top.
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6;
            int x = (int) Math.round(Math.cos(a) * (r - 2));
            int z = (int) Math.round(Math.sin(a) * (r - 2));
            if (z > 15 && Math.abs(x) < 6) {
                continue; // keep the arrival side open
            }
            s.fill(x, 0, z, x, 12, z, pillar);
            s.set(x, 13, z, gold);
            s.set(x, 14, z, Blocks.END_ROD.defaultBlockState());
        }
        // Three great viewing windows: north, west and east.
        window(s, -7, 7, -24, true, glass, gold);
        window(s, -7, 7, -24, false, glass, gold);
        // Steps up to the centre and the arrival frame on the south edge.
        s.fill(-3, -1, 20, 3, -1, 23, quartz);
        s.fill(-3, 0, 22, -3, 5, 22, gold);
        s.fill(3, 0, 22, 3, 5, 22, gold);
        s.fill(-3, 6, 22, 3, 6, 22, gold);
        s.settle();
    }

    /** A window frame of gold with dark glass: north side (axisZ) or both side windows. */
    private static void window(SetPiece s, int a1, int a2, int at, boolean north, BlockState glass, BlockState frame) {
        if (north) {
            s.fill(a1 - 1, 0, at - 1, a2 + 1, 12, at - 1, frame);
            s.fill(a1, 1, at - 1, a2, 11, at - 1, glass);
        } else {
            for (int side : new int[]{-1, 1}) {
                int x = side * (-at) - side;
                s.fill(x, 0, a1 - 1, x, 10, a2 + 1, frame);
                s.fill(x, 1, a1, x, 9, a2, glass);
            }
        }
    }

    // ================================================================== New York, Scene 11

    public static void ensureNewYorkS11(ServerLevel ny) {
        MarvelSites.ensureNewYork(ny);
        WorldSites sites = WorldSites.get(ny.getServer());
        if (sites.hasFlag(MarvelIds.LAYOUT_NY_S11)) {
            return;
        }
        SetPiece s = new SetPiece(ny, MarvelSites.NY_ORIGIN, 900_000);
        buildAccident(s);
        buildObservationBuilding(s);
        buildHellsKitchen(s);
        s.settle();
        sites.setFlag(MarvelIds.LAYOUT_NY_S11);
    }

    private static void buildAccident(SetPiece s) {
        // A sinkhole across the east avenue.
        s.fill(43, -7, 36, 53, -1, 42, AIR);
        s.fill(43, -8, 36, 53, -8, 42, Blocks.COBBLED_DEEPSLATE.defaultBlockState());
        for (int x = 42; x <= 54; x++) {
            s.set(x, -1, 35, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
            s.set(x, -1, 43, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
        }
        s.fill(44, -6, 37, 46, -5, 38, Blocks.GRAVEL.defaultBlockState());
        // The car teetering on the edge, its front over the hole.
        BlockState body = Blocks.RED_CONCRETE.defaultBlockState();
        BlockState glass = Blocks.LIGHT_GRAY_STAINED_GLASS.defaultBlockState();
        s.fill(45, 0, 34, 51, 0, 36, body);
        s.fill(46, 0, 35, 50, 1, 35, AIR);
        s.fill(45, 1, 34, 51, 1, 34, glass);
        s.fill(45, 1, 36, 51, 1, 36, glass);
        s.fill(45, 1, 35, 45, 1, 35, glass);
        s.fill(51, 1, 35, 51, 1, 35, glass);
        s.fill(45, 2, 34, 51, 2, 36, body);
        for (int x : new int[]{45, 51}) {
            s.set(x, -1, 34, Blocks.BLACK_CONCRETE.defaultBlockState());
        }
        s.set(45, -1, 36, Blocks.BLACK_CONCRETE.defaultBlockState());
        // Danger area tape on the road.
        for (int x = 42; x <= 54; x += 2) {
            s.set(x, -1, 29, Blocks.YELLOW_CONCRETE.defaultBlockState());
        }
        resetAccident(s);
    }

    /** The accident's movable parts: unwebbed anchors on the facades, car roof and two posts. */
    public static void resetAccident(SetPiece s) {
        for (BlockPos a : CAR_ANCHORS) {
            s.set(a.getX(), a.getY(), a.getZ(), MarvelBlocks.WEB_ANCHOR.get().defaultBlockState());
        }
        for (BlockPos p : BARRIER_POSTS) {
            s.fill(p.getX(), 0, p.getZ(), p.getX(), p.getY() - 1, p.getZ(), Blocks.IRON_BARS.defaultBlockState());
            s.set(p.getX(), p.getY(), p.getZ(), MarvelBlocks.WEB_ANCHOR.get().defaultBlockState());
        }
        for (int x = 43; x <= 53; x++) {
            s.set(x, 0, 27, AIR);
            s.set(x, 1, 27, AIR);
        }
        // Car doors shut (they open once the car is secured).
        s.fill(46, 0, 34, 47, 0, 34, Blocks.RED_CONCRETE.defaultBlockState());
        s.fill(46, 1, 34, 47, 1, 34, Blocks.LIGHT_GRAY_STAINED_GLASS.defaultBlockState());
    }

    /** Opens the car's pavement-side doors so the occupants can climb out. */
    public static void openCarDoors(ServerLevel ny) {
        for (int x = 46; x <= 47; x++) {
            for (int y = 0; y <= 1; y++) {
                ny.setBlock(ny(new BlockPos(x, y, 34)), AIR, Block.UPDATE_ALL);
            }
        }
    }

    /** True when every web anchor in the list is webbed. */
    public static boolean allWebbed(ServerLevel ny, List<BlockPos> anchors) {
        for (BlockPos a : anchors) {
            BlockState st = ny.getBlockState(ny(a));
            if (!st.is(MarvelBlocks.WEB_ANCHOR.get()) || !st.getValue(WebAnchorBlock.WEBBED)) {
                return false;
            }
        }
        return true;
    }

    /** A gap has been cleared in the debris around the gateway: some column of it is open at both heights. */
    public static boolean debrisGap(ServerLevel tva) {
        for (BlockPos d : DEBRIS_U) {
            if (!tva.getBlockState(tva(d)).is(MarvelBlocks.DEBRIS.get()) && !tva.getBlockState(tva(d.above())).is(MarvelBlocks.DEBRIS.get())) {
                return true;
            }
        }
        return false;
    }

    /** True while any fallen supplies still block the reception walkway's north approach. */
    public static boolean cratesRemain(ServerLevel tva) {
        for (int x = 76; x <= 80; x++) {
            for (int y = 0; y <= 1; y++) {
                if (tva.getBlockState(tva(new BlockPos(x, y, -27))).is(MarvelBlocks.FALLEN_CRATE.get())) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Lays a plain repaired walkway and bridge (used when jumping straight to a later scene for testing). */
    public static void prebuildRepairs(ServerLevel tva, boolean walkway, boolean bridge) {
        SetPiece s = new SetPiece(tva, MarvelSites.TVA_ORIGIN, 10_000);
        if (walkway) {
            s.fill(76, 0, -27, 80, 1, -27, AIR);
            s.fill(77, -1, -25, 79, -1, -22, MarvelBlocks.REPAIR_PANEL.get().defaultBlockState());
        }
        if (bridge) {
            s.fill(83, -1, BRIDGE_Z1, 85, -1, BRIDGE_Z2, MarvelBlocks.REPAIR_PANEL.get().defaultBlockState());
        }
        s.settle();
    }

    public static void resetAccident(ServerLevel ny) {
        resetAccident(new SetPiece(ny, MarvelSites.NY_ORIGIN, 200));
    }

    /** Webs strung between the two posts: pedestrians cannot pass into the danger area. */
    public static void webBarrier(ServerLevel ny) {
        for (int x = BARRIER_POSTS.get(0).getX() + 1; x < BARRIER_POSTS.get(1).getX(); x++) {
            ny.setBlock(ny(new BlockPos(x, 0, 27)), Blocks.COBWEB.defaultBlockState(), Block.UPDATE_CLIENTS);
            ny.setBlock(ny(new BlockPos(x, 1, 27)), Blocks.COBWEB.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static void buildObservationBuilding(SetPiece s) {
        s.fill(58, 0, 43, 72, 45, 55, AIR);
        s.fill(58, -1, 43, 72, -1, 55, Blocks.SMOOTH_STONE.defaultBlockState());
        s.fill(59, 0, 44, 69, 19, 54, Blocks.BRICKS.defaultBlockState());
        s.fill(60, 0, 45, 68, 19, 53, AIR);
        for (int y = 2; y <= 18; y += 4) {
            for (int z = 46; z <= 52; z += 2) {
                s.set(59, y, z, Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState());
            }
            s.fill(60, y - 2, 45, 68, y - 2, 53, Blocks.SPRUCE_PLANKS.defaultBlockState());
        }
        s.fill(59, 20, 44, 69, 20, 54, Blocks.SMOOTH_STONE.defaultBlockState());
        s.fill(59, 21, 44, 69, 21, 44, Blocks.BRICK_WALL.defaultBlockState());
        s.fill(59, 21, 54, 69, 21, 54, Blocks.BRICK_WALL.defaultBlockState());
        s.fill(59, 21, 44, 59, 21, 54, Blocks.BRICK_WALL.defaultBlockState());
        s.fill(69, 21, 44, 69, 21, 54, Blocks.BRICK_WALL.defaultBlockState());
        s.fill(66, 21, 46, 67, 22, 47, Blocks.IRON_BLOCK.defaultBlockState());
        // A fire-escape ladder on the avenue side.
        s.fill(58, 0, 49, 58, 20, 49, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST));
    }

    private static void buildHellsKitchen(SetPiece s) {
        s.fill(-76, 0, 38, -58, 45, 56, AIR);
        s.fill(-76, -1, 38, -58, -1, 56, Blocks.SMOOTH_STONE.defaultBlockState());
        s.fill(-75, 0, 40, -59, 13, 54, Blocks.BRICKS.defaultBlockState());
        s.fill(-74, 0, 41, -60, 13, 53, AIR);
        for (int y = 2; y <= 12; y += 4) {
            for (int x = -73; x <= -61; x += 3) {
                s.set(x, y, 40, Blocks.YELLOW_STAINED_GLASS.defaultBlockState());
                s.set(x, y, 54, Blocks.YELLOW_STAINED_GLASS.defaultBlockState());
            }
            s.fill(-74, y - 2, 41, -60, y - 2, 53, Blocks.SPRUCE_PLANKS.defaultBlockState());
        }
        s.fill(-75, 14, 40, -59, 14, 54, Blocks.SMOOTH_STONE.defaultBlockState());
        s.fill(-75, 15, 40, -59, 15, 40, Blocks.BRICK_WALL.defaultBlockState());
        s.fill(-75, 15, 54, -59, 15, 54, Blocks.BRICK_WALL.defaultBlockState());
        s.fill(-75, 15, 40, -75, 15, 54, Blocks.BRICK_WALL.defaultBlockState());
        s.fill(-59, 15, 40, -59, 15, 54, Blocks.BRICK_WALL.defaultBlockState());
        // Water tower, chimney, an air-conditioning unit and a skylight.
        s.fill(-73, 15, 52, -71, 15, 52, Blocks.SPRUCE_FENCE.defaultBlockState());
        s.fill(-74, 18, 50, -70, 21, 53, Blocks.SPRUCE_PLANKS.defaultBlockState());
        for (int[] leg : new int[][]{{-74, 50}, {-70, 50}, {-74, 53}, {-70, 53}}) {
            s.fill(leg[0], 15, leg[1], leg[0], 17, leg[1], Blocks.SPRUCE_FENCE.defaultBlockState());
        }
        s.fill(-61, 15, 42, -60, 18, 42, Blocks.BRICKS.defaultBlockState());
        s.fill(-64, 15, 41, -63, 16, 42, Blocks.IRON_BLOCK.defaultBlockState());
        s.fill(-68, 14, 46, -66, 14, 48, Blocks.GLASS.defaultBlockState());
        s.set(-67, 15, 46, Blocks.LANTERN.defaultBlockState());
        // Fire escape.
        s.fill(-58, 0, 47, -58, 14, 47, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST));
    }
}
