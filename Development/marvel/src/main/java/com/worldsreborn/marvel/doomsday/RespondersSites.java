package com.worldsreborn.marvel.doomsday;

import com.worldsreborn.marvel.MarvelIds;
import com.worldsreborn.marvel.MarvelSites;
import com.worldsreborn.marvel.block.MarvelBlocks;
import com.worldsreborn.marvel.doomsday.StoryControlBlock.Kind;
import com.worldsreborn.worlds.SetPiece;
import com.worldsreborn.worlds.WorldSites;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * The places of Act 1 Scenes 12-16.
 * <ul>
 * <li>MU-616 New York (offsets from {@link MarvelSites#NY_ORIGIN}, east and west of the city): the Avengers' response
 * base with its briefing room, launch pad and communications array (Scenes 12 and 14); an emergency landing site west of
 * the city (Scene 12); a damaged research facility south-east of the base (Scene 13).</li>
 * <li>The Fantastic Four's Earth (offsets from {@link #FF_ORIGIN}): their laboratory, its safe room and rooftop
 * sensor deck (Scene 15).</li>
 * <li>The X-Men's Earth (offsets from {@link #XS_ORIGIN}): Xavier's school and grounds, and the damaged monitoring
 * station 300 blocks south (Scene 16).</li>
 * </ul>
 * Parts change during play (debris cleared, doors braced, a cable crossing set, fields placed); {@link #reset} puts a
 * scene's parts back for a replay.
 */
public final class RespondersSites {
    public static final ResourceKey<Level> FF_WORLD = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("wrmarvel:ff_world"));
    public static final ResourceKey<Level> XMEN_WORLD = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse("wrmarvel:xmen_world"));
    public static final BlockPos FF_ORIGIN = new BlockPos(0, 64, 0);
    public static final BlockPos XS_ORIGIN = new BlockPos(0, 64, 0);
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    // ================================================================== New York: the response base (Scenes 12, 14)

    public static final BlockPos BASE_SAM_START = new BlockPos(193, 0, 5);
    public static final BlockPos ROUTE_TABLE = new BlockPos(189, 0, 0);
    public static final BlockPos BASE_DISPLAY = new BlockPos(179, 2, 0);
    public static final BlockPos BASE_DISPLAY_VIEW = new BlockPos(184, 0, 0);
    public static final BlockPos BASE_DOOR = new BlockPos(200, 0, 0);
    public static final BlockPos LAUNCH_PAD = new BlockPos(210, 0, 0);
    public static final BlockPos BASE_SHANG_CHI = new BlockPos(185, 0, -6);
    public static final BlockPos BASE_SCOTT = new BlockPos(187, 0, -7);
    public static final BlockPos BASE_JOAQUIN = new BlockPos(212, 0, 3);
    public static final BlockPos ARRAY_POWER = new BlockPos(217, 0, -12);
    public static final BlockPos ARRAY_DISH = new BlockPos(220, 0, -12);
    public static final BlockPos ARRAY_TEST = new BlockPos(223, 0, -12);
    public static final BlockPos ARRAY_VIEW = new BlockPos(220, 0, -9);
    public static final BlockPos TRANSMIT = new BlockPos(180, 0, 4);
    /** Scene 14: the responders in small groups around the briefing room (Joaquin's, Bucky's, the Sentry's, Clint's). */
    public static final List<BlockPos> BRIEFING_SPOTS = List.of(
            new BlockPos(183, 0, -8), new BlockPos(185, 0, -9), new BlockPos(187, 0, -8),
            new BlockPos(195, 0, -6), new BlockPos(196, 0, -4), new BlockPos(197, 0, -7),
            new BlockPos(195, 0, 6), new BlockPos(197, 0, 5), new BlockPos(196, 0, 8),
            new BlockPos(183, 0, 8), new BlockPos(185, 0, 9));

    // ---- Scene 12 airspace and the emergency landing site (west of the city)
    /** Survey positions high above the towers, east to west; the samples are taken where Sam holds steady. */
    public static final List<BlockPos> SURVEY = List.of(new BlockPos(95, 112, -40), new BlockPos(10, 114, 48),
            new BlockPos(-85, 112, -38));
    /** Above this height Sam turns back: the survey is not a climb towards the other Earth. */
    public static final int SURVEY_CEILING = 140;
    public static final BlockPos HELI_HOLD = new BlockPos(-150, 58, -6);
    public static final BlockPos LANDING = new BlockPos(-176, 0, 18);
    public static final BlockPos SAFE_AREA = new BlockPos(-183, 0, 31);
    public static final List<BlockPos> CREW_POSTS = List.of(new BlockPos(-186, 0, 28), new BlockPos(-180, 0, 28));
    public static final BlockPos SITE_SHANG_CHI = new BlockPos(-170, 0, 29);
    public static final BlockPos SITE_SCOTT = new BlockPos(-168, 0, 27);
    public static final BlockPos SITE_JOAQUIN = new BlockPos(-171, 0, 24);
    /** Where each rescued passenger, then the pilot, waits with the crews. */
    public static final List<BlockPos> SAFE_SPOTS = List.of(new BlockPos(-184, 0, 32), new BlockPos(-182, 0, 33),
            new BlockPos(-180, 0, 32));

    // ================================================================== New York: the research facility (Scene 13)

    /** The facility's front door; everything below is relative to it (the building runs north from here). */
    public static final BlockPos FAC = new BlockPos(220, 0, 160);
    public static final BlockPos FAC_ARRIVAL = fac(0, 0, -16);
    public static final BlockPos EXTRACTION = fac(0, 0, -10);
    public static final List<BlockPos> RESPONDERS = List.of(fac(-4, 0, -12), fac(4, 0, -12));
    public static final BlockPos FAC_YELENA = fac(-2, 0, -6);
    public static final List<BlockPos> FAC_TEAM = List.of(fac(-3, 0, -3), fac(3, 0, -3), fac(5, 0, -7), fac(-6, 0, -7));
    public static final BlockPos FAC_CLINT = fac(-12, 0, 5);
    public static final BlockPos FAC_KATE = fac(-10, 0, 4);
    public static final BlockPos OBSTRUCTION = fac(-12, 0, 15);
    public static final BlockPos BRACE = fac(-14, 0, 14);
    public static final BlockPos ROOM_1 = fac(-12, 0, 22);
    public static final List<BlockPos> GROUP_1 = List.of(fac(-14, 0, 22), fac(-12, 0, 25), fac(-10, 0, 22));
    public static final BlockPos LOBBY = fac(0, 0, 5);
    public static final List<BlockPos> SIDE_ROOMS = List.of(fac(-3, 0, 13), fac(3, 0, 13));
    public static final BlockPos FAC_DOOR = fac(0, 0, 0);
    public static final int GAP_Z1 = 13, GAP_Z2 = 17;
    public static final BlockPos CROSSING_SOUTH = fac(12, 0, 11);
    public static final BlockPos CROSSING_NORTH = fac(12, 0, 19);
    public static final BlockPos SEALED_DOOR = fac(12, 0, 24);
    public static final List<BlockPos> GROUP_2 = List.of(fac(9, 0, 27), fac(11, 0, 28), fac(13, 0, 27), fac(15, 0, 28));
    public static final BlockPos SENTRY_HOLD = fac(12, 3, 15);
    public static final BlockPos FAC_RECORDER = fac(16, 0, 21);
    /** The unsafe way round to the recorder: a failing strip along the east wall. */
    public static final int UNSAFE_X = 17;

    public static BlockPos fac(int x, int y, int z) {
        return FAC.offset(x, y, z);
    }

    // ================================================================== the Fantastic Four's laboratory (Scene 15)

    public static final BlockPos FF_SUE_START = new BlockPos(0, 0, 6);
    public static final BlockPos FF_CONSOLE = new BlockPos(0, 0, -3);
    public static final BlockPos FF_REED = new BlockPos(-2, 0, -3);
    public static final BlockPos FF_JOHNNY = new BlockPos(3, 0, -1);
    public static final BlockPos FF_BEN = new BlockPos(2, 0, -8);
    public static final BlockPos CONNECTION = new BlockPos(0, 1, -7);
    /** Lab staff waiting at the west benches, then the aisle points they cross, then the safe room. */
    public static final List<BlockPos> FF_STAFF = List.of(new BlockPos(-15, 0, 5), new BlockPos(-15, 0, 7), new BlockPos(-13, 0, 6));
    public static final List<BlockPos> AISLE = List.of(new BlockPos(-8, 0, 6), new BlockPos(0, 0, 6), new BlockPos(8, 0, 6));
    public static final BlockPos SAFE_ROOM = new BlockPos(22, 0, 6);
    /** Field anchors: Sue's fields snap to these when aimed near them (canopies at y 5, platform and guard walls). */
    public static final List<BlockPos> CANOPIES = List.of(new BlockPos(-8, 5, 6), new BlockPos(0, 5, 6), new BlockPos(8, 5, 6),
            new BlockPos(0, 5, -3));
    public static final BlockPos ROOF_PLATFORM = new BlockPos(21, 11, 0);
    public static final BlockPos SENSOR_1 = new BlockPos(-15, 12, -11);
    public static final BlockPos SENSOR_2 = new BlockPos(22, 12, 0);
    public static final BlockPos ROOF_JOHNNY = new BlockPos(15, 13, 0);
    public static final BlockPos ROOF_LADDER_TOP = new BlockPos(16, 12, -12);
    /** Guard walls around the connection: west and east (planes x = -3 / 3) and south (plane z = -4). */
    public static final List<BlockPos> GUARDS = List.of(new BlockPos(-3, 1, -7), new BlockPos(3, 1, -7), new BlockPos(0, 1, -4));
    public static final int FF_CEILING = 10;

    // ================================================================== the X-Men's Earth (Scene 16)

    public static final BlockPos XS_CYCLOPS_START = new BlockPos(0, 0, 8);
    public static final BlockPos XS_BRIEFING = new BlockPos(0, 0, -30);
    public static final BlockPos XS_BEAST = new BlockPos(-3, 0, -33);
    public static final BlockPos XS_XAVIER = new BlockPos(0, 0, -36);
    public static final BlockPos XS_MAGNETO = new BlockPos(3, 0, -33);
    public static final BlockPos XS_JEAN = new BlockPos(-5, 0, -30);
    public static final BlockPos XS_KURT = new BlockPos(5, 0, -30);
    public static final List<BlockPos> STUDENTS = List.of(new BlockPos(-10, 0, 0), new BlockPos(-7, 0, 2), new BlockPos(9, 0, -4),
            new BlockPos(12, 0, 6));
    /** The monitoring station, 300 blocks south of the school. */
    public static final BlockPos ST = new BlockPos(0, 0, 300);
    public static final BlockPos ST_ARRIVAL = st(8, 0, -9);
    public static final BlockPos ST_TEAM_WAIT = st(10, 0, -6);
    public static final BlockPos ST_MAIN_DOOR = st(0, 0, 0);
    public static final BlockPos ST_SIDE_DOOR = st(12, 0, 5);
    public static final BlockPos ST_EXTRACT = st(19, 0, -2);
    /** Debris to clear with controlled blasts: the side door, then the corridor past the two props. */
    public static final List<BlockPos> ACCESS_DEBRIS = List.of(
            st(12, 0, 4), st(12, 0, 5), st(12, 0, 6), st(12, 1, 4), st(12, 1, 5), st(12, 1, 6), st(12, 2, 5),
            st(7, 0, 4), st(7, 0, 5), st(7, 0, 6), st(7, 1, 4), st(7, 1, 5), st(7, 1, 6), st(7, 2, 5), st(8, 0, 5), st(8, 1, 5));
    /** The corridor part of {@link #ACCESS_DEBRIS}, which comes down again after a hit prop. */
    public static final int CORRIDOR_DEBRIS_FROM = 7;
    /** Load-bearing props: either side of the corridor debris, and either side of the hall. Blasting one is a setback. */
    public static final List<BlockPos> SUPPORTS = List.of(st(9, 0, 4), st(9, 0, 6), st(-3, 0, 12), st(3, 0, 12));
    public static final List<BlockPos> TELEPORT_SPOTS = List.of(st(-6, 0, 12), st(6, 0, 12));
    public static final List<BlockPos> TELEPORT_DEBRIS = List.of(st(-6, 0, 12), st(-6, 1, 12), st(6, 0, 12), st(6, 1, 12));
    /** Three isolated pairs of staff on the cut-off upper floor. */
    public static final List<BlockPos> ST_STAFF = List.of(st(-8, 5, 19), st(-7, 5, 20), st(8, 5, 19), st(7, 5, 20),
            st(0, 5, 22), st(1, 5, 22));
    public static final BlockPos ST_JEAN = st(0, 0, 14);
    public static final BlockPos ST_KURT = st(2, 0, 12);
    public static final BlockPos CORRIDOR_HAZARD = st(6, 0, 5);
    public static final BlockPos ST_REC_POWER = st(-10, 0, 21);
    public static final BlockPos ST_REC_CLAMP = st(-9, 0, 22);
    public static final BlockPos ST_RECORDER = st(-8, 0, 22);
    public static final BlockPos ST_REC_VIEW = st(-8, 0, 19);
    public static final BlockPos ST_BEAST_REC = st(-7, 0, 20);

    public static BlockPos st(int x, int y, int z) {
        return ST.offset(x, y, z);
    }

    public static final BlockState SUPPORT = Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();

    private RespondersSites() {
    }

    private static BlockState ctl(Kind kind) {
        return MarvelBlocks.STORY_CONTROL.get().defaultBlockState().setValue(StoryControlBlock.KIND, kind);
    }

    // ================================================================== ensure (built once per world)

    public static void ensureNewYork(ServerLevel ny) {
        MarvelSites.ensureNewYork(ny);
        WorldSites sites = WorldSites.get(ny.getServer());
        if (!sites.hasFlag(MarvelIds.LAYOUT_RESPONSE_BASE)) {
            SetPiece s = new SetPiece(ny, MarvelSites.NY_ORIGIN, 600_000);
            buildBase(s);
            buildLandingSite(s);
            s.settle();
            sites.setFlag(MarvelIds.LAYOUT_RESPONSE_BASE);
        }
        if (!sites.hasFlag(MarvelIds.LAYOUT_FACILITY)) {
            SetPiece s = new SetPiece(ny, MarvelSites.NY_ORIGIN.offset(FAC), 300_000);
            buildFacility(s);
            s.settle();
            sites.setFlag(MarvelIds.LAYOUT_FACILITY);
        }
    }

    public static void ensureFfLab(ServerLevel ff) {
        WorldSites sites = WorldSites.get(ff.getServer());
        if (!sites.hasFlag(MarvelIds.LAYOUT_FF_LAB)) {
            SetPiece s = new SetPiece(ff, FF_ORIGIN, 600_000);
            buildFfLab(s);
            s.settle();
            sites.setFlag(MarvelIds.LAYOUT_FF_LAB);
        }
    }

    public static void ensureXmen(ServerLevel xw) {
        WorldSites sites = WorldSites.get(xw.getServer());
        if (!sites.hasFlag(MarvelIds.LAYOUT_XMEN)) {
            SetPiece s = new SetPiece(xw, XS_ORIGIN, 900_000);
            buildSchool(s);
            buildStation(s);
            s.settle();
            sites.setFlag(MarvelIds.LAYOUT_XMEN);
        }
    }

    // ================================================================== the response base

    private static void buildBase(SetPiece s) {
        BlockState pad = Blocks.GRAY_CONCRETE.defaultBlockState();
        BlockState line = Blocks.YELLOW_CONCRETE.defaultBlockState();
        BlockState wall = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        BlockState glass = Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
        BlockState floor = Blocks.POLISHED_ANDESITE.defaultBlockState();
        s.fill(172, -1, -28, 238, -1, 28, pad);
        s.clear(172, 0, -28, 238, 20, 28);
        // Briefing room.
        s.fill(177, -1, -13, 201, -1, 13, floor);
        s.walls(177, 0, -13, 201, 6, 13, wall);
        s.fill(177, 7, -13, 201, 7, 13, Blocks.SMOOTH_STONE.defaultBlockState());
        for (int z = -10; z <= 10; z += 4) {
            s.fill(201, 2, z, 201, 4, z + 1, glass);
        }
        for (int x = 181; x <= 197; x += 4) {
            s.fill(x, 2, -13, x + 1, 4, -13, glass);
            s.fill(x, 2, 13, x + 1, 4, 13, glass);
        }
        s.clear(201, 0, -1, 201, 2, 1);
        for (int x = 181; x <= 197; x += 8) {
            for (int z = -9; z <= 9; z += 9) {
                s.set(x, 6, z, Blocks.SEA_LANTERN.defaultBlockState());
            }
        }
        // The display wall: three panels (flight readings, facility records, public sky imagery in Scene 14).
        s.fill(178, 1, -6, 178, 4, 6, Blocks.BLACK_STAINED_GLASS.defaultBlockState());
        s.fill(178, 0, -6, 178, 0, 6, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        for (int z : new int[]{-3, 3}) {
            s.fill(178, 1, z, 178, 4, z, Blocks.GRAY_CONCRETE.defaultBlockState());
        }
        // The table with the route display in the middle.
        s.fill(186, 0, -2, 192, 0, 2, Blocks.WHITE_CONCRETE.defaultBlockState());
        s.set(ROUTE_TABLE.getX(), 0, ROUTE_TABLE.getZ(), ctl(Kind.ROUTE_TABLE));
        s.set(TRANSMIT.getX(), 0, TRANSMIT.getZ(), ctl(Kind.TRANSMIT));
        // Launch pad: a yellow ring outside the door.
        ring(s, LAUNCH_PAD.getX(), LAUNCH_PAD.getZ(), 4, line);
        // Communications array: a mast and dish on a plinth, with its three controls in front.
        s.fill(218, 0, -18, 222, 0, -14, Blocks.IRON_BLOCK.defaultBlockState());
        s.fill(220, 1, -16, 220, 7, -16, Blocks.IRON_BARS.defaultBlockState());
        s.fill(218, 8, -17, 222, 10, -17, Blocks.WHITE_CONCRETE.defaultBlockState());
        s.set(220, 9, -16, Blocks.LIGHTNING_ROD.defaultBlockState());
        s.set(ARRAY_POWER.getX(), 0, ARRAY_POWER.getZ(), ctl(Kind.POWER));
        s.set(ARRAY_DISH.getX(), 0, ARRAY_DISH.getZ(), ctl(Kind.DISH));
        s.set(ARRAY_TEST.getX(), 0, ARRAY_TEST.getZ(), ctl(Kind.TEST));
        setArrayLamps(s, false);
        // A few parked response vehicles.
        for (int x : new int[]{214, 222, 230}) {
            s.fill(x, 0, 18, x + 4, 1, 21, Blocks.BLACK_CONCRETE.defaultBlockState());
            s.fill(x + 1, 1, 18, x + 3, 1, 18, Blocks.TINTED_GLASS.defaultBlockState());
        }
    }

    /** The two lamps on the array plinth: off until power is routed. */
    public static void setArrayLamps(SetPiece s, boolean lit) {
        BlockState lamp = Blocks.REDSTONE_LAMP.defaultBlockState().setValue(RedstoneLampBlock.LIT, lit);
        s.set(218, 1, -14, lamp);
        s.set(222, 1, -14, lamp);
    }

    public static void setArrayLamps(ServerLevel ny, boolean lit) {
        setArrayLamps(new SetPiece(ny, MarvelSites.NY_ORIGIN, 10), lit);
    }

    private static void ring(SetPiece s, int cx, int cz, int r, BlockState state) {
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                double d = Math.sqrt(x * x + z * z);
                if (Math.abs(d - r) < 0.5) {
                    s.set(cx + x, -1, cz + z, state);
                }
            }
        }
    }

    private static void buildLandingSite(SetPiece s) {
        // A clear field with a marked pad, the crews' area and an ambulance.
        s.fill(-196, -1, 4, -158, -1, 40, Blocks.GRASS_BLOCK.defaultBlockState());
        s.clear(-196, 0, 4, -158, 12, 40);
        s.fill(-182, -1, 12, -170, -1, 24, Blocks.GRAY_CONCRETE.defaultBlockState());
        ring(s, LANDING.getX(), LANDING.getZ(), 5, Blocks.YELLOW_CONCRETE.defaultBlockState());
        for (int z = -2; z <= 2; z++) {
            s.set(LANDING.getX() - 2, -1, LANDING.getZ() + z, Blocks.WHITE_CONCRETE.defaultBlockState());
            s.set(LANDING.getX() + 2, -1, LANDING.getZ() + z, Blocks.WHITE_CONCRETE.defaultBlockState());
        }
        for (int x = -1; x <= 1; x++) {
            s.set(LANDING.getX() + x, -1, LANDING.getZ(), Blocks.WHITE_CONCRETE.defaultBlockState());
        }
        s.fill(-190, -1, 26, -176, -1, 36, Blocks.SMOOTH_STONE.defaultBlockState());
        s.fill(-191, 0, 34, -187, 2, 37, Blocks.WHITE_CONCRETE.defaultBlockState());
        s.fill(-191, 2, 34, -191, 2, 37, Blocks.RED_CONCRETE.defaultBlockState());
        s.set(-189, 3, 35, Blocks.REDSTONE_LAMP.defaultBlockState().setValue(RedstoneLampBlock.LIT, true));
        for (int[] c : new int[][]{{-176, 28}, {-176, 36}, {-190, 26}}) {
            s.set(c[0], 0, c[1], Blocks.ORANGE_CONCRETE.defaultBlockState());
            s.set(c[0], 1, c[1], Blocks.END_ROD.defaultBlockState());
        }
    }

    // ================================================================== the research facility

    private static void buildFacility(SetPiece s) {
        RandomSource rnd = RandomSource.create(1313L);
        BlockState wall = Blocks.WHITE_CONCRETE.defaultBlockState();
        BlockState trim = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        BlockState floor = Blocks.SMOOTH_STONE.defaultBlockState();
        BlockState crack = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        s.clear(-24, 0, -22, 24, 14, 34);
        s.fill(-24, -1, -22, 24, -1, -1, Blocks.GRAY_CONCRETE.defaultBlockState());
        // Shell: walls, floor and a roof with holes torn in it.
        s.fill(-18, -1, 0, 18, -1, 30, floor);
        s.walls(-18, 0, 0, 18, 8, 30, wall);
        s.fill(-18, 9, 0, 18, 9, 30, trim);
        for (int i = 0; i < 18; i++) {
            int x = -16 + rnd.nextInt(33), z = 2 + rnd.nextInt(27);
            s.fill(x, 9, z, x + rnd.nextInt(3), 9, z + rnd.nextInt(3), AIR);
        }
        for (int x = -15; x <= 15; x += 5) {
            s.fill(x, 3, 0, x + 2, 5, 0, Blocks.GLASS.defaultBlockState());
        }
        // Front door (Walker and Red Guardian keep it open in 13.4).
        s.clear(-1, 0, 0, 1, 2, 0);
        // Interior partitions: west wing, the sealed north-west room, the lobby's side rooms, the east wing.
        s.fill(-7, 0, 1, -7, 7, 14, trim);
        s.clear(-7, 0, 4, -7, 2, 6);
        s.fill(-17, 0, 15, -7, 7, 15, trim);
        s.fill(-13, 2, 15, -11, 3, 15, crack);
        s.fill(-6, 0, 10, 6, 7, 10, trim);
        s.walls(-6, 0, 10, -1, 7, 15, trim);
        s.walls(1, 0, 10, 6, 7, 15, trim);
        s.clear(-3, 0, 10, -3, 2, 10);
        s.clear(3, 0, 10, 3, 2, 10);
        s.fill(7, 0, 1, 7, 7, 29, trim);
        s.clear(7, 0, 4, 7, 2, 6);
        // East wing: the floor has fallen through across z 13-17 (the broken section).
        s.fill(8, -6, GAP_Z1, 17, -1, GAP_Z2, AIR);
        s.fill(8, -7, GAP_Z1, 17, -7, GAP_Z2, Blocks.COBBLED_DEEPSLATE.defaultBlockState());
        // A failing strip along the east wall: the unsafe way round (it gives way under anyone who tries it).
        s.fill(17, -1, GAP_Z1, 17, -1, GAP_Z2, crack);
        for (int x = 9; x <= 17; x++) {
            s.set(x, -1, GAP_Z1 - 1, crack);
            s.set(x, -1, GAP_Z2 + 1, crack);
        }
        // The sagging ceiling over the crossing (Sentry holds it).
        s.fill(9, 8, 13, 16, 8, 17, Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState());
        // The sealed section where the second group is trapped.
        s.fill(8, 0, 24, 17, 7, 24, trim);
        // Benches, cabinets and the monitoring array along the walls.
        for (int z = 2; z <= 12; z += 5) {
            s.fill(-16, 0, z, -15, 0, z + 2, Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
        }
        for (int x = 9; x <= 16; x += 3) {
            s.set(x, 0, 2, Blocks.IRON_BLOCK.defaultBlockState());
            s.set(x, 1, 2, Blocks.DAYLIGHT_DETECTOR.defaultBlockState());
        }
        s.fill(14, 0, 20, 17, 2, 22, Blocks.IRON_BLOCK.defaultBlockState());
        s.fill(15, 0, 21, 17, 1, 21, AIR);
        for (int i = 0; i < 26; i++) {
            int x = -16 + rnd.nextInt(33), z = 1 + rnd.nextInt(29);
            if (!s.get(x, 0, z).isAir() || Math.abs(x) < 3 || (x > 7 && z >= GAP_Z1 - 1 && z <= GAP_Z2 + 1)) {
                continue;
            }
            s.set(x, 0, z, rnd.nextBoolean() ? Blocks.ANDESITE_SLAB.defaultBlockState() : Blocks.COBBLESTONE_SLAB.defaultBlockState());
        }
        // The cable route Clint and Kate rigged: chains under the ceiling from the west wing to the blocked door.
        for (int z = 5; z <= 14; z++) {
            s.set(-12, 7, z, Blocks.CHAIN.defaultBlockState());
        }
        for (int x = -6; x >= -12; x--) {
            s.set(x, 7, 5, Blocks.CHAIN.defaultBlockState().setValue(net.minecraft.world.level.block.ChainBlock.AXIS, Direction.Axis.X));
        }
        // Responders' area outside: an ambulance and a cordon.
        s.fill(-9, 0, -16, -6, 2, -13, Blocks.WHITE_CONCRETE.defaultBlockState());
        s.fill(-9, 2, -16, -9, 2, -13, Blocks.RED_CONCRETE.defaultBlockState());
        for (int x = -12; x <= 12; x += 3) {
            s.set(x, 0, -19, Blocks.ORANGE_CONCRETE.defaultBlockState());
        }
        resetFacility(s);
    }

    /** The parts of Scene 13 that change: the obstruction, the brace, the exit rubble, the seal and the crossing. */
    public static void resetFacility(SetPiece s) {
        BlockState debris = MarvelBlocks.DEBRIS.get().defaultBlockState();
        s.fill(-13, 0, 15, -11, 1, 15, debris);
        s.set(BRACE.getX() - FAC.getX(), 0, BRACE.getZ() - FAC.getZ(), ctl(Kind.BRACE));
        s.clear(-14, 1, 14, -14, 2, 14);
        s.clear(-10, 0, 14, -10, 2, 14);
        s.fill(17, -1, GAP_Z1, 17, -1, GAP_Z2, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
        s.clear(-1, 0, 0, 1, 2, 0);
        s.fill(11, 0, 24, 12, 2, 24, Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState());
        s.fill(12, -1, GAP_Z1, 12, -1, GAP_Z2, AIR);
        s.fill(9, 6, GAP_Z1 - 1, 9, 6, GAP_Z2 + 1, AIR);
        s.fill(15, 6, GAP_Z1 - 1, 15, 6, GAP_Z2 + 1, AIR);
        s.set(FAC_RECORDER.getX() - FAC.getX(), 0, FAC_RECORDER.getZ() - FAC.getZ(), ctl(Kind.RECORDER));
    }

    public static void resetFacility(ServerLevel ny) {
        resetFacility(new SetPiece(ny, MarvelSites.NY_ORIGIN.offset(FAC), 2_000));
    }

    private static SetPiece fac(ServerLevel ny) {
        return new SetPiece(ny, MarvelSites.NY_ORIGIN.offset(FAC), 500);
    }

    public static boolean obstructionCleared(ServerLevel ny) {
        return !fac(ny).get(-12, 0, 15).is(MarvelBlocks.DEBRIS.get()) && !fac(ny).get(-12, 1, 15).is(MarvelBlocks.DEBRIS.get());
    }

    /** Bucky's arm: the obstruction comes out as one piece. */
    public static void liftObstruction(ServerLevel ny) {
        fac(ny).fill(-13, 0, 15, -11, 1, 15, AIR);
    }

    /** Props either side of the unstable doorway. */
    public static void braceDoor(ServerLevel ny) {
        SetPiece s = fac(ny);
        BlockState prop = Blocks.STRIPPED_OAK_LOG.defaultBlockState();
        s.fill(-14, 0, 14, -14, 2, 14, prop);
        s.fill(-10, 0, 14, -10, 2, 14, prop);
    }

    public static boolean doorBraced(ServerLevel ny) {
        return fac(ny).get(-14, 1, 14).is(Blocks.STRIPPED_OAK_LOG);
    }

    /** Part of the front fails as the disturbance returns: rubble across the exit. */
    public static void dropExitRubble(ServerLevel ny) {
        fac(ny).fill(-1, 0, 0, 1, 1, 0, MarvelBlocks.DEBRIS.get().defaultBlockState());
    }

    public static boolean exitClear(ServerLevel ny) {
        SetPiece s = fac(ny);
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                if (s.get(x, y, 0).is(MarvelBlocks.DEBRIS.get())) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Ghost opens the seal from the inside. */
    public static void openSeal(ServerLevel ny) {
        fac(ny).fill(11, 0, 24, 12, 2, 24, AIR);
    }

    /** Clint and Kate's cable crossing: a cable walkway with guide lines either side. */
    public static void setCable(ServerLevel ny) {
        SetPiece s = fac(ny);
        s.fill(12, -1, GAP_Z1, 12, -1, GAP_Z2, Blocks.SPRUCE_SLAB.defaultBlockState()
                .setValue(net.minecraft.world.level.block.SlabBlock.TYPE, net.minecraft.world.level.block.state.properties.SlabType.TOP));
        BlockState chain = Blocks.CHAIN.defaultBlockState().setValue(net.minecraft.world.level.block.ChainBlock.AXIS, Direction.Axis.Z);
        s.fill(9, 6, GAP_Z1 - 1, 9, 6, GAP_Z2 + 1, chain);
        s.fill(15, 6, GAP_Z1 - 1, 15, 6, GAP_Z2 + 1, chain);
    }

    public static boolean cableSet(ServerLevel ny) {
        return !fac(ny).get(12, -1, 15).isAir();
    }

    // ================================================================== the Fantastic Four's laboratory

    private static void buildFfLab(SetPiece s) {
        RandomSource rnd = RandomSource.create(1515L);
        BlockState white = Blocks.WHITE_CONCRETE.defaultBlockState();
        BlockState blue = Blocks.BLUE_CONCRETE.defaultBlockState();
        BlockState floor = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState glass = Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
        // A plaza and a ring of towers so the lab stands in a city.
        s.fill(-60, -1, -60, 60, -1, 60, Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState());
        s.clear(-60, 0, -60, 60, 30, 60);
        for (int i = 0; i < 16; i++) {
            double a = i * Math.PI / 8 + 0.2;
            int cx = (int) (Math.cos(a) * 48), cz = (int) (Math.sin(a) * 48);
            int h = 20 + rnd.nextInt(40);
            BlockState c = rnd.nextBoolean() ? Blocks.GRAY_CONCRETE.defaultBlockState() : Blocks.CYAN_TERRACOTTA.defaultBlockState();
            s.fill(cx - 4, 0, cz - 4, cx + 4, h, cz + 4, c);
            for (int y = 2; y < h; y += 3) {
                s.fill(cx - 4, y, cz - 4, cx + 4, y, cz + 4, Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState());
            }
        }
        // The laboratory: one tall hall with benches, the receiving machine, Reed's console and a safe room annex.
        s.fill(-18, -1, -14, 26, -1, 14, floor);
        s.walls(-18, 0, -14, 18, FF_CEILING, 14, white);
        s.fill(-18, FF_CEILING + 1, -14, 18, FF_CEILING + 1, 14, Blocks.SMOOTH_STONE.defaultBlockState());
        s.fill(-18, 4, -14, 18, 4, -14, blue);
        s.fill(-18, 4, 14, 18, 4, 14, blue);
        for (int x = -14; x <= 14; x += 6) {
            s.fill(x, 5, -14, x + 2, 8, -14, glass);
            s.fill(x, 5, 14, x + 2, 8, 14, glass);
        }
        for (int x = -12; x <= 12; x += 8) {
            for (int z = -8; z <= 8; z += 8) {
                s.set(x, FF_CEILING, z, Blocks.SEA_LANTERN.defaultBlockState());
            }
        }
        // Safe room through the east wall.
        s.walls(18, 0, 1, 26, 6, 11, white);
        s.fill(18, 7, 1, 26, 7, 11, Blocks.SMOOTH_STONE.defaultBlockState());
        s.clear(18, 0, 5, 18, 2, 7);
        s.fill(19, 0, 10, 25, 0, 10, Blocks.BLUE_CONCRETE.defaultBlockState());
        // Benches along the west end (where the staff start) and the aisle markings at z 6.
        for (int z : new int[]{2, 10}) {
            s.fill(-16, 0, z, -10, 0, z, Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
        }
        for (int x = -16; x <= 17; x++) {
            s.set(x, -1, 4, Blocks.YELLOW_CONCRETE.defaultBlockState());
            s.set(x, -1, 8, Blocks.YELLOW_CONCRETE.defaultBlockState());
        }
        // The receiving machine: a ring on a base, the connection at its foot, Reed's console in front.
        BlockState iron = Blocks.IRON_BLOCK.defaultBlockState();
        for (int a = 0; a < 32; a++) {
            double t = a * Math.PI / 16;
            int x = (int) Math.round(Math.cos(t) * 4), y = (int) Math.round(Math.sin(t) * 4) + 5;
            s.set(x, y, -9, iron);
        }
        s.fill(-2, 0, -10, 2, 0, -8, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        s.set(CONNECTION.getX(), CONNECTION.getY(), CONNECTION.getZ(), Blocks.LIGHTNING_ROD.defaultBlockState());
        s.set(0, 0, -7, Blocks.COPPER_BLOCK.defaultBlockState());
        s.fill(-1, 0, -3, 1, 0, -3, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        s.set(FF_CONSOLE.getX(), 1, FF_CONSOLE.getZ(), Blocks.DAYLIGHT_DETECTOR.defaultBlockState());
        // Ladder to the roof in the north-east corner, and the roof deck with its parapet and first sensor mast.
        for (int y = 0; y <= FF_CEILING + 1; y++) {
            s.set(16, y, -13, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH));
        }
        s.walls(-18, FF_CEILING + 2, -14, 18, FF_CEILING + 2, 14, Blocks.WHITE_CONCRETE_POWDER.defaultBlockState());
        s.fill(-15, FF_CEILING + 2, -11, -15, FF_CEILING + 3, -11, Blocks.IRON_BARS.defaultBlockState());
        s.set(SENSOR_1.getX(), SENSOR_1.getY() + 2, SENSOR_1.getZ(), Blocks.END_ROD.defaultBlockState());
        // The second sensor goes on a bracket out past the east parapet, over the drop.
        s.fill(19, FF_CEILING + 2, 0, 20, FF_CEILING + 2, 0, Blocks.IRON_BARS.defaultBlockState());
        resetFfLab(s);
    }

    public static void resetFfLab(SetPiece s) {
        s.clear(SENSOR_2.getX(), SENSOR_2.getY(), SENSOR_2.getZ(), SENSOR_2.getX(), SENSOR_2.getY() + 1, SENSOR_2.getZ());
    }

    public static void installSensor(ServerLevel ff) {
        new SetPiece(ff, FF_ORIGIN, 10).set(SENSOR_2.getX(), SENSOR_2.getY(), SENSOR_2.getZ(), Blocks.END_ROD.defaultBlockState());
    }

    // ================================================================== Xavier's school and the monitoring station

    private static void buildSchool(SetPiece s) {
        RandomSource rnd = RandomSource.create(1616L);
        BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
        s.fill(-70, -1, -70, 70, -1, 50, grass);
        s.clear(-70, 0, -70, 70, 24, 50);
        // Paths across the lawn to the front door.
        s.fill(-1, -1, -19, 1, -1, 30, Blocks.DIRT_PATH.defaultBlockState());
        s.fill(-20, -1, -1, 20, -1, 1, Blocks.DIRT_PATH.defaultBlockState());
        // Trees around the grounds.
        for (int i = 0; i < 22; i++) {
            int x = -64 + rnd.nextInt(129), z = -64 + rnd.nextInt(110);
            if (Math.abs(x) < 26 && z > -44 && z < 24) {
                continue;
            }
            tree(s, rnd, x, z);
        }
        // The mansion: brick walls, stone trim, tall windows, two floors.
        BlockState brick = Blocks.BRICKS.defaultBlockState();
        BlockState stone = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState glass = Blocks.GLASS.defaultBlockState();
        s.fill(-22, -1, -42, 22, -1, -20, Blocks.SPRUCE_PLANKS.defaultBlockState());
        s.walls(-22, 0, -42, 22, 12, -20, brick);
        s.fill(-22, 6, -42, 22, 6, -20, Blocks.SPRUCE_PLANKS.defaultBlockState());
        s.fill(-22, 13, -42, 22, 13, -20, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        s.fill(-20, 14, -40, 20, 14, -22, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        for (int x = -18; x <= 18; x += 6) {
            s.fill(x, 2, -20, x + 1, 4, -20, glass);
            s.fill(x, 8, -20, x + 1, 10, -20, glass);
        }
        s.fill(-22, 5, -42, 22, 5, -42, stone);
        s.fill(-22, 5, -20, 22, 5, -20, stone);
        s.clear(-1, 0, -20, 1, 3, -20);
        // Briefing room: a long table, chairs of light, a map wall.
        s.walls(-9, 0, -39, 9, 5, -26, stone);
        s.clear(-1, 0, -26, 1, 2, -26);
        s.fill(-4, 0, -34, 4, 0, -32, Blocks.DARK_OAK_PLANKS.defaultBlockState());
        s.fill(-8, 1, -39, 8, 4, -39, Blocks.BLACK_STAINED_GLASS.defaultBlockState());
        for (int x = -6; x <= 6; x += 4) {
            s.set(x, 5, -32, Blocks.SEA_LANTERN.defaultBlockState());
        }
        // Benches on the lawn.
        for (int x : new int[]{-12, 12}) {
            s.fill(x - 1, 0, 4, x + 1, 0, 4, Blocks.SPRUCE_SLAB.defaultBlockState());
        }
    }

    private static void tree(SetPiece s, RandomSource rnd, int x, int z) {
        int h = 4 + rnd.nextInt(3);
        s.fill(x, 0, z, x, h, z, Blocks.OAK_LOG.defaultBlockState());
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = h - 1; dy <= h + 1; dy++) {
                    if (Math.abs(dx) + Math.abs(dz) + Math.max(0, dy - h) * 2 <= 3 && !(dx == 0 && dz == 0 && dy <= h)) {
                        s.set(x + dx, dy, z + dz, leaves);
                    }
                }
            }
        }
    }

    private static void buildStation(SetPiece s) {
        RandomSource rnd = RandomSource.create(1661L);
        int ox = ST.getX(), oz = ST.getZ();
        BlockState wall = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
        BlockState floor = Blocks.POLISHED_ANDESITE.defaultBlockState();
        s.fill(ox - 40, -1, oz - 30, ox + 40, -1, oz + 40, Blocks.GRASS_BLOCK.defaultBlockState());
        s.clear(ox - 40, 0, oz - 30, ox + 40, 20, oz + 40);
        // Station shell, two floors; the upper floor's stairs have gone.
        s.fill(ox - 12, -1, oz, ox + 12, -1, oz + 24, floor);
        s.walls(ox - 12, 0, oz, ox + 12, 9, oz + 24, wall);
        s.fill(ox - 12, 4, oz + 8, ox + 12, 4, oz + 24, floor);
        s.fill(ox - 12, 10, oz, ox + 12, 10, oz + 24, Blocks.SMOOTH_STONE.defaultBlockState());
        s.clear(ox - 3, 4, oz + 8, ox + 3, 4, oz + 10);
        for (int i = 0; i < 10; i++) {
            int x = ox - 10 + rnd.nextInt(21), z = oz + 1 + rnd.nextInt(23);
            s.fill(x, 10, z, x + 1, 10, z + 1, AIR);
        }
        // Main entrance with its unstable upper section; a cordon of hazard blocks in front.
        s.clear(ox - 2, 0, oz, ox + 2, 3, oz);
        s.fill(ox - 3, 4, oz, ox + 3, 6, oz + 1, Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
        for (int x = -3; x <= 3; x += 2) {
            s.set(ox + x, 0, oz - 3, Blocks.YELLOW_CONCRETE.defaultBlockState());
        }
        // Side service door on the east wall and the corridor running west from it.
        s.clear(ox + 12, 0, oz + 4, ox + 12, 2, oz + 6);
        s.clear(ox + 11, 0, oz + 4, ox + 11, 3, oz + 6);
        s.fill(ox - 11, 0, oz + 3, ox + 11, 3, oz + 3, wall);
        s.fill(ox - 11, 0, oz + 7, ox - 1, 3, oz + 7, wall);
        s.fill(ox + 3, 0, oz + 7, ox + 11, 3, oz + 7, wall);
        s.fill(ox - 11, 4, oz + 3, ox + 11, 4, oz + 7, floor);
        // The hall north of the corridor, and the recorder room in its west corner.
        s.walls(ox - 11, 0, oz + 18, ox - 5, 3, oz + 23, wall);
        s.clear(ox - 8, 0, oz + 18, ox - 7, 2, oz + 18);
        s.fill(ox - 11, 0, oz + 23, ox - 6, 0, oz + 23, Blocks.IRON_BLOCK.defaultBlockState());
        for (int i = 0; i < 6; i++) {
            int x = ox - 9 + rnd.nextInt(19), z = oz + 9 + rnd.nextInt(8);
            s.set(x, 0, z, rnd.nextBoolean() ? Blocks.ANDESITE_SLAB.defaultBlockState() : Blocks.COBBLESTONE_SLAB.defaultBlockState());
        }
        // Instrument racks upstairs where the staff are cut off.
        for (int x = -10; x <= 10; x += 5) {
            s.set(ox + x, 5, oz + 23, Blocks.IRON_BLOCK.defaultBlockState());
            s.set(ox + x, 6, oz + 23, Blocks.DAYLIGHT_DETECTOR.defaultBlockState());
        }
        // A survey mast outside.
        s.fill(ox + 16, 0, oz + 14, ox + 16, 14, oz + 14, Blocks.IRON_BARS.defaultBlockState());
        s.set(ox + 16, 15, oz + 14, Blocks.LIGHTNING_ROD.defaultBlockState());
        resetStation(s);
    }

    /** The parts of Scene 16 that change: debris to clear, the props, the arrival spots and the recorder. */
    public static void resetStation(SetPiece s) {
        BlockState debris = MarvelBlocks.DEBRIS.get().defaultBlockState();
        for (BlockPos p : ACCESS_DEBRIS) {
            s.set(p.getX(), p.getY(), p.getZ(), debris);
        }
        for (BlockPos p : TELEPORT_DEBRIS) {
            s.set(p.getX(), p.getY(), p.getZ(), debris);
        }
        for (BlockPos p : SUPPORTS) {
            s.fill(p.getX(), 0, p.getZ(), p.getX(), 3, p.getZ(), SUPPORT.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
        }
        s.fill(CORRIDOR_HAZARD.getX(), 0, CORRIDOR_HAZARD.getZ() - 1, CORRIDOR_HAZARD.getX(), 1, CORRIDOR_HAZARD.getZ() + 1, AIR);
        s.set(ST_REC_POWER.getX(), 0, ST_REC_POWER.getZ(), ctl(Kind.POWER));
        s.set(ST_REC_CLAMP.getX(), 0, ST_REC_CLAMP.getZ(), ctl(Kind.CLAMP));
        s.set(ST_RECORDER.getX(), 0, ST_RECORDER.getZ(), ctl(Kind.RECORDER));
    }

    public static void resetStation(ServerLevel xw) {
        resetStation(new SetPiece(xw, XS_ORIGIN, 1_000));
    }

    public static boolean allCleared(ServerLevel xw, List<BlockPos> debris) {
        for (BlockPos p : debris) {
            if (xw.getBlockState(XS_ORIGIN.offset(p)).is(MarvelBlocks.DEBRIS.get())) {
                return false;
            }
        }
        return true;
    }

    /** A recoverable setback: a hit prop cracks and part of the corridor comes down again. */
    public static void corridorSetback(ServerLevel xw) {
        BlockState debris = MarvelBlocks.DEBRIS.get().defaultBlockState();
        for (BlockPos p : ACCESS_DEBRIS.subList(CORRIDOR_DEBRIS_FROM, ACCESS_DEBRIS.size())) {
            xw.setBlock(XS_ORIGIN.offset(p), debris, Block.UPDATE_ALL);
        }
    }

    public static void dropCorridorHazard(ServerLevel xw) {
        for (int z = -1; z <= 1; z++) {
            for (int y = 0; y <= 1; y++) {
                xw.setBlock(XS_ORIGIN.offset(CORRIDOR_HAZARD).offset(0, y, z), MarvelBlocks.DEBRIS.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    // ================================================================== resetting for a replay

    /** Puts the changing parts of a scene back (developer scene jumps). */
    public static void reset(net.minecraft.server.MinecraftServer server, int scene) {
        if (scene == 13 && server.getLevel(MarvelSites.NEW_YORK) instanceof ServerLevel ny) {
            ensureNewYork(ny);
            resetFacility(ny);
        }
        if (scene == 14 && server.getLevel(MarvelSites.NEW_YORK) instanceof ServerLevel ny) {
            ensureNewYork(ny);
            setArrayLamps(ny, false);
        }
        if (scene == 15 && server.getLevel(FF_WORLD) instanceof ServerLevel ff) {
            ensureFfLab(ff);
            resetFfLab(new SetPiece(ff, FF_ORIGIN, 100));
        }
        if (scene == 16 && server.getLevel(XMEN_WORLD) instanceof ServerLevel xw) {
            ensureXmen(xw);
            resetStation(xw);
        }
    }
}
