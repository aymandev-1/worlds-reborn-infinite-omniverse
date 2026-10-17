package com.worldsreborn.marvel.doomsday;

import com.worldsreborn.abilities.AbilityUsedEvent;
import com.worldsreborn.characters.NpcSpawner;
import com.worldsreborn.characters.entity.StoryNpcEntity;
import com.worldsreborn.core.WRCore;
import com.worldsreborn.core.model.AbilityDef;
import com.worldsreborn.core.model.DialogueChoice;
import com.worldsreborn.core.model.DialogueDef;
import com.worldsreborn.core.model.DialogueLine;
import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.model.WRContent;
import com.worldsreborn.core.profile.Profiles;
import com.worldsreborn.marvel.MarvelScript1216;
import com.worldsreborn.marvel.MarvelSites;
import com.worldsreborn.marvel.WRMarvel;
import com.worldsreborn.marvel.block.MarvelBlocks;
import com.worldsreborn.marvel.doomsday.Doomsday.Current;
import com.worldsreborn.marvel.doomsday.StoryControlBlock.Kind;
import com.worldsreborn.marvel.item.MarvelItems;
import com.worldsreborn.story.StoryService;
import com.worldsreborn.travel.WRTravel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.worldsreborn.marvel.MarvelIds.*;
import static com.worldsreborn.marvel.MarvelSites.NEW_YORK;
import static com.worldsreborn.marvel.doomsday.Doomsday.*;
import static com.worldsreborn.marvel.doomsday.RespondersSites.*;

/**
 * Runs Act 1 "Doomsday", Scenes 12-16 (WR1_012-WR1_016): Earth's heroes respond, and the story follows a different hero
 * in each scene while Steve stays an observer.
 * <ul>
 * <li>12, Sam Wilson: flight survey over New York, a helicopter rescue, Shuri's imagery of an inhabited Earth.</li>
 * <li>13, Bucky Barnes: the New Avengers, Clint and Kate evacuate a damaged research facility and recover its recorder.</li>
 * <li>14, Sam Wilson: one joint briefing, the communications array, first contact with Reed Richards.</li>
 * <li>15, Sue Storm: the Fantastic Four keep the channel open; force fields over the staff, the roof sensor and the
 * machine.</li>
 * <li>16, Cyclops: the X-Men rescue a monitoring station's staff with controlled optic blasts and find the overlap.</li>
 * </ul>
 * Same rules as {@link Doomsday}, whose helpers it uses: the saved quest step says where the scene is, {@link #setup}
 * makes the world match it, finer progress is saved as player flags under {@link
 * com.worldsreborn.marvel.MarvelIds#FLAG_STATE_PREFIX}, and short timed beats live in memory and simply start again after
 * a reload. Dialogue is {@link MarvelScript1216}; lines without a recording are subtitles only (audio_missing).
 * <p>
 * Each scene's hero is a full perspective switch: Steve's inventory stays put aside (as in Scene 11), the player gets
 * that hero's look and powers in the ability slots and nothing else, and no power or item crosses to the next hero.
 */
public final class Responders {
    private static final String NS = WRMarvel.MODID + ":";

    private Responders() {
    }

    // ================================================================== characters and powers

    public static final String SAM = NS + "sam_wilson";
    public static final String JOAQUIN = NS + "joaquin_torres";
    public static final String SHANG_CHI = NS + "shang_chi";
    public static final String SCOTT = NS + "scott_lang";
    public static final String PILOT = NS + "heli_pilot";
    public static final List<String> PASSENGERS = List.of(NS + "passenger_1", NS + "passenger_2");
    public static final String GROUND_CREW = NS + "ground_crew";
    public static final String BUCKY = NS + "bucky_barnes";
    public static final String YELENA = NS + "yelena_belova";
    public static final String WALKER = NS + "john_walker";
    public static final String RED_GUARDIAN = NS + "red_guardian";
    public static final String GHOST = NS + "ghost";
    public static final String SENTRY = NS + "sentry";
    public static final String CLINT = NS + "clint_barton";
    public static final String KATE = NS + "kate_bishop";
    public static final String SCIENTIST = NS + "facility_scientist";
    public static final List<String> FACILITY_STAFF = List.of(NS + "facility_staff_1", NS + "facility_staff_2");
    public static final String EMERGENCY_RESPONDER = NS + "emergency_responder";
    public static final String REED = NS + "reed_richards";
    public static final String SUE = NS + "sue_storm";
    public static final String JOHNNY = NS + "johnny_storm";
    public static final String BEN = NS + "ben_grimm";
    public static final String LAB_STAFF = NS + "ff_lab_staff";
    public static final String CYCLOPS = NS + "cyclops";
    public static final String JEAN = NS + "jean_grey";
    public static final String BEAST = NS + "beast";
    public static final String NIGHTCRAWLER = NS + "nightcrawler";
    public static final String XAVIER = NS + "charles_xavier";
    public static final String MAGNETO = NS + "magneto";
    public static final String STATION_STAFF = NS + "station_staff";
    public static final String STATION_SCIENTIST = NS + "station_scientist";
    public static final List<String> STUDENTS = List.of(NS + "student_1", NS + "student_2");

    /** Everyone in Scenes 12-16 who appears as a person in the world (their skin is named after the id). */
    public static final List<String> CHARACTERS = List.of(JOAQUIN, SHANG_CHI, SCOTT, PILOT, PASSENGERS.get(0), PASSENGERS.get(1),
            GROUND_CREW, BUCKY, YELENA, WALKER, RED_GUARDIAN, GHOST, SENTRY, CLINT, KATE, SCIENTIST, FACILITY_STAFF.get(0),
            FACILITY_STAFF.get(1), EMERGENCY_RESPONDER, REED, JOHNNY, BEN, LAB_STAFF, JEAN, BEAST, NIGHTCRAWLER, XAVIER, MAGNETO,
            STATION_STAFF, STATION_SCIENTIST, STUDENTS.get(0), STUDENTS.get(1));

    public static final String SAM_FLIGHT = NS + "sam_flight";
    public static final String BUCKY_LIFT = NS + "bucky_lift";
    public static final String SUE_FIELD = NS + "sue_field";
    public static final String SUE_RELEASE = NS + "sue_release";
    public static final String OPTIC_BLAST = NS + "optic_blast";

    /** The playable heroes of Scenes 12-16: who they are, how they look and the powers in their slots (Z, X, C). */
    enum Hero {
        SAM_WILSON("sam_wilson", List.of(SAM_FLIGHT)),
        BUCKY_BARNES("bucky_barnes", List.of(BUCKY_LIFT)),
        SUE_STORM("sue_storm", List.of(SUE_FIELD, SUE_RELEASE)),
        SCOTT_SUMMERS("cyclops", List.of(OPTIC_BLAST));

        final String skin;
        final List<String> abilities;

        Hero(String skin, List<String> abilities) {
            this.skin = skin;
            this.abilities = abilities;
        }

        String flag() {
            return "hero_" + skin;
        }

        String skinPath() {
            return NS + "textures/entity/" + skin + ".png";
        }
    }

    static final List<String> SCENES_12_16 = List.of(S12, S13, S14, S15, S16);

    static Hero heroFor(String quest) {
        return switch (quest) {
            case S12, S14 -> Hero.SAM_WILSON;
            case S13 -> Hero.BUCKY_BARNES;
            case S15 -> Hero.SUE_STORM;
            default -> Hero.SCOTT_SUMMERS;
        };
    }

    /** Registers the powers of Scenes 12-16 (called while the Marvel content is registered). */
    public static void registerAbilities() {
        ability(SAM_FLIGHT, 10);
        ability(BUCKY_LIFT, 15);
        ability(SUE_FIELD, 8);
        ability(SUE_RELEASE, 10);
        ability(OPTIC_BLAST, 20);
    }

    private static void ability(String id, int cooldown) {
        String key = "ability." + id.replace(':', '.');
        WRContent.ABILITIES.register(new AbilityDef(id, key, key + ".desc", cooldown, WRMarvel.MODID));
    }

    // ================================================================== dialogue from the script

    private static final String GUI = NS + "textures/gui/cinematic/";
    /** Pictures on screen while a conversation is about them (Shuri's imagery, the joint display, Reed's events...). */
    private static final Map<String, String> IMAGES = Map.of(
            "d12_population", GUI + "shuri_imagery.png",
            "d14_briefing", GUI + "joint_display.png",
            "d15_more_worlds", GUI + "detected_events.png",
            "d16_landscape", GUI + "overlap_terrain.png");

    private static String voice(String lineId) {
        return MarvelScript1216.MILLIS.getOrDefault(lineId, 0) > 0 ? NS + "voice." + lineId : null;
    }

    private static DialogueLine line(String id, boolean asReply, @Nullable String images) {
        String speaker = MarvelScript1216.SPEAKER.get(id);
        boolean player = asReply && MarvelScript1216.PLAYER_LINE.getOrDefault(id, false);
        DialogueLine l = new DialogueLine("speaker.wrmarvel." + speaker, "line.wrmarvel." + id, false, voice(id),
                player ? "tone.wrmarvel." + id : null);
        return images == null ? l : l.withImages(images);
    }

    /** Registers every conversation of Scenes 12-16; a group with options ends in that choice, then its rejoin lines. */
    public static void registerDialogues() {
        for (String group : MarvelScript1216.GROUPS.keySet()) {
            if (!group.startsWith("d") || group.contains("#") || group.endsWith("_rejoin")) {
                continue;
            }
            String images = IMAGES.get(group);
            List<DialogueLine> body = new ArrayList<>();
            for (String id : MarvelScript1216.GROUPS.get(group)) {
                body.add(line(id, true, images));
            }
            List<DialogueLine> rejoin = new ArrayList<>();
            for (String id : MarvelScript1216.GROUPS.getOrDefault(group + "_rejoin", List.of())) {
                rejoin.add(line(id, false, null));
            }
            List<DialogueChoice> choices = new ArrayList<>();
            for (String opt : new String[]{"a", "b", "c"}) {
                List<String> ids = MarvelScript1216.GROUPS.get(group + "#" + opt);
                if (ids != null) {
                    String id = ids.get(0);
                    choices.add(new DialogueChoice(opt, "tone.wrmarvel." + id, "line.wrmarvel." + id, voice(id), rejoin));
                }
            }
            WRContent.DIALOGUES.register(new DialogueDef(dlg(group), body, choices));
        }
    }

    static int speakingTicks(String group) {
        int ms = 0;
        for (String id : MarvelScript1216.GROUPS.get(group)) {
            int rec = MarvelScript1216.MILLIS.getOrDefault(id, 0);
            ms += (rec > 0 ? rec : 1800 + 55 * MarvelScript1216.CHARS.getOrDefault(id, 30)) + 300;
        }
        return ms / 50;
    }

    /** Speaks a group of lines as subtitles while play continues, once per player. */
    static boolean bark(ServerPlayer player, String group) {
        if (!Profiles.get(player).setFlag(FLAG_BARK_PREFIX + group)) {
            return false;
        }
        for (String id : MarvelScript1216.GROUPS.get(group)) {
            StoryService.say(player, "speaker.wrmarvel." + MarvelScript1216.SPEAKER.get(id), "line.wrmarvel." + id, voice(id));
        }
        return true;
    }

    private static boolean heard(ServerPlayer player, String group) {
        return Profiles.get(player).hasFlag(FLAG_BARK_PREFIX + group);
    }

    // ================================================================== small helpers

    private static void bar(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args), true);
    }

    /** A one-time hint on the action bar (once per session, so it comes back after a reload). */
    private static void hint(ServerPlayer player, String id, String key) {
        if (once(player, "hint:" + id)) {
            message(player, key);
        }
    }

    private static String tag(ServerPlayer player, String name) {
        return own(player, NS + "r_" + name);
    }

    private static BlockPos ff(BlockPos offset) {
        return FF_ORIGIN.offset(offset);
    }

    private static BlockPos xs(BlockPos offset) {
        return XS_ORIGIN.offset(offset);
    }

    private static Vec3 c(BlockPos p) {
        return Vec3.atBottomCenterOf(p);
    }

    private static int counter(ServerPlayer player, String key) {
        return session(player).counters.getOrDefault(key, 0);
    }

    private static void setCounter(ServerPlayer player, String key, int value) {
        session(player).counters.put(key, value);
    }

    /** Where an NPC should stand among a group around a spot, so they don't stack on one block. */
    private static BlockPos spread(BlockPos at, int i) {
        int[][] off = {{0, 0}, {2, 0}, {-2, 0}, {0, 2}, {2, 2}, {-2, 2}, {0, -2}};
        return at.offset(off[i % off.length][0], 0, off[i % off.length][1]);
    }

    /**
     * Walks an NPC along waypoints, one after another; true once it stands at the last. If it has made no progress for a
     * while (the path-finder balks at a narrow crossing or a doorway) and the player is near, it is stepped along.
     */
    private static boolean walkRoute(ServerPlayer player, @Nullable StoryNpcEntity n, String key, List<BlockPos> route) {
        if (n == null) {
            return false;
        }
        Map<String, Integer> cnt = session(player).counters;
        Integer stage = cnt.get(key);
        if (stage == null) {
            stage = 0;
            double best = Double.MAX_VALUE;
            for (int i = 0; i < route.size(); i++) {
                double d = n.position().distanceTo(c(route.get(i)));
                if (d < best) {
                    best = d;
                    stage = i;
                }
            }
            cnt.put(key, stage);
        }
        if (stage >= route.size()) {
            return true;
        }
        BlockPos wp = route.get(stage);
        if (n.position().distanceTo(c(wp)) < 1.7) {
            cnt.put(key, stage + 1);
            cnt.put(key + "#t", 0);
            n.stopWalking();
            return stage + 1 >= route.size();
        }
        int t = cnt.merge(key + "#t", 1, Integer::sum);
        if (t > 10 && player.distanceTo(n) < 18) {
            Vec3 d = c(wp).subtract(n.position());
            Vec3 step = d.horizontalDistance() > 1.2 ? new Vec3(d.x, 0, d.z).normalize().scale(1.2) : new Vec3(d.x, 0, d.z);
            n.stopWalking();
            // Only into open space: never into rubble that has come down across the way.
            if (n.level().noCollision(n, n.getBoundingBox().move(step.x, wp.getY() - n.getY(), step.z))) {
                n.moveTo(n.getX() + step.x, wp.getY(), n.getZ() + step.z, n.getYRot(), 0f);
            }
        } else if (!n.isWalking()) {
            n.walkTo(wp);
        }
        return false;
    }

    private static int routeStage(ServerPlayer player, String key) {
        return session(player).counters.getOrDefault(key, -1);
    }

    private static void puff(ServerLevel level, Vec3 at, ParticleOptions p, int count) {
        level.sendParticles(p, at.x, at.y + 1, at.z, count, 0.3, 0.7, 0.3, 0.02);
    }

    private static final DustParticleOptions MARKER = new DustParticleOptions(new Vector3f(0.35f, 0.8f, 1f), 1.6f);
    private static final DustParticleOptions OPTIC = new DustParticleOptions(new Vector3f(1f, 0.15f, 0.12f), 1.2f);
    private static final DustParticleOptions FIELD = new DustParticleOptions(new Vector3f(0.55f, 0.85f, 1f), 0.9f);

    /** A line of particles from a to b, sent to everyone near. */
    private static void beam(ServerLevel level, Vec3 a, Vec3 b, ParticleOptions p) {
        Vec3 d = b.subtract(a);
        int n = (int) Math.min(80, d.length() * 3);
        for (int i = 0; i <= n; i++) {
            Vec3 q = a.add(d.scale(i / (double) Math.max(1, n)));
            level.sendParticles(p, q.x, q.y, q.z, 1, 0, 0, 0, 0);
        }
    }

    // ================================================================== who the player is: hero perspectives

    @Nullable
    static Hero currentHero(ServerPlayer player) {
        for (Hero h : Hero.values()) {
            if (state(player, h.flag())) {
                return h;
            }
        }
        return null;
    }

    /**
     * Puts the player in a hero's place: Steve's inventory is stashed the first time (a previous hero's things are just
     * dropped from the story), the hero's skin and powers replace whatever came before, and they arrive at the scene.
     */
    static void becomeHero(ServerPlayer player, Hero hero, ServerLevel level, BlockPos at, float yaw) {
        PlayerProgress p = Profiles.get(player);
        boolean inPerspective = currentHero(player) != null || state(player, "perspective_peter") || PerspectiveStash.holds(player);
        if (inPerspective) {
            player.getInventory().clearContent();
        } else {
            PerspectiveStash.stash(player);
            for (int i = 0; i < 3; i++) {
                String id = p.equipped(i);
                if (id != null) {
                    p.setFlag(FLAG_STATE_PREFIX + "steve_slot_" + i + "=" + id);
                }
            }
        }
        for (Hero h : Hero.values()) {
            p.clearFlag(FLAG_STATE_PREFIX + h.flag());
            h.abilities.forEach(p.unlockedAbilities()::remove);
        }
        p.clearFlag(FLAG_STATE_PREFIX + "perspective_peter");
        for (int i = 0; i < 3; i++) {
            p.equip(i, null);
        }
        for (int i = 0; i < hero.abilities.size(); i++) {
            p.unlockedAbilities().add(hero.abilities.get(i));
            p.equip(i, hero.abilities.get(i));
        }
        p.setFlag(FLAG_STATE_PREFIX + hero.flag());
        p.setPerspectiveSkin(hero.skinPath());
        setFlight(player, false);
        player.teleportTo(level, at.getX() + 0.5, at.getY(), at.getZ() + 0.5, yaw, 0f);
        player.fallDistance = 0;
        checkpoint(player, level.dimension(), at);
        Profiles.sync(player);
    }

    /**
     * Takes away every hero's look and powers and gives Steve back his own ability slots. Steve's inventory is restored
     * by the caller ({@link Doomsday.Perspective#becomeSteve}).
     */
    static void clearHero(ServerPlayer player) {
        PlayerProgress p = Profiles.get(player);
        for (Hero h : Hero.values()) {
            p.clearFlag(FLAG_STATE_PREFIX + h.flag());
            h.abilities.forEach(p.unlockedAbilities()::remove);
        }
        for (int i = 0; i < 3; i++) {
            String id = p.equipped(i);
            if (id != null && !p.unlockedAbilities().contains(id)) {
                p.equip(i, null);
            }
        }
        String prefix = FLAG_STATE_PREFIX + "steve_slot_";
        for (String f : List.copyOf(p.flags())) {
            if (f.startsWith(prefix)) {
                String[] kv = f.substring(prefix.length()).split("=", 2);
                if (kv.length == 2) {
                    p.equip(Integer.parseInt(kv[0]), kv[1]);
                }
                p.clearFlag(f);
            }
        }
        setFlight(player, false);
        LIVE.remove(player.getUUID());
    }

    /** Sam's flight: available in Scene 12 once he has launched; switched off everywhere else. */
    private static void setFlight(ServerPlayer player, boolean allowed) {
        var a = player.getAbilities();
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (a.mayfly != allowed || (!allowed && a.flying)) {
            a.mayfly = allowed;
            if (!allowed) {
                a.flying = false;
            }
            a.setFlyingSpeed(allowed ? 0.075f : 0.05f);
            player.onUpdateAbilities();
        }
    }

    // ================================================================== what lives only in memory

    /** Moving things of the current scene: the helicopter and its people, Joaquin's flight, Sue's fields, hazards. */
    private static final class Live {
        Vec3 lastPos;
        Vec3 velocity = Vec3.ZERO;
        // Scene 12
        Vec3 heliPos;
        Vec3 heliVel = Vec3.ZERO;
        int heliT;
        final List<Display.BlockDisplay> parts = new ArrayList<>();
        int carried = -1;
        int approach;
        double joaquinAngle;
        final Map<String, StoryNpcEntity> people = new HashMap<>();
        // Scene 15
        final ArrayDeque<List<BlockPos>> fields = new ArrayDeque<>();
        int hazardTimer = 80;
        @Nullable
        BlockPos hazardTarget;
        @Nullable
        Vec3 hazardFrom;
        int hazardWarn;
        // Scene 16
        String lastAim = "";
    }

    private static final Map<UUID, Live> LIVE = new ConcurrentHashMap<>();

    private static Live live(ServerPlayer player) {
        return LIVE.computeIfAbsent(player.getUUID(), u -> new Live());
    }

    static void forget(UUID id) {
        LIVE.remove(id);
    }

    /** A person kept in memory for per-tick movement; found again (or spawned) when the reference goes stale. */
    @Nullable
    private static StoryNpcEntity person(Live l, ServerLevel level, String characterId, String tag, BlockPos pos, float yaw) {
        StoryNpcEntity n = l.people.get(tag);
        if (n != null && n.isAlive() && !n.isRemoved() && n.level() == level) {
            return n;
        }
        n = npc(level, characterId, tag, pos, yaw);
        if (n != null) {
            l.people.put(tag, n);
        }
        return n;
    }

    // ================================================================== scene start: the story moves to the next hero

    static void onQuestStart(ServerPlayer player, String quest) {
        WRCore.LOGGER.info("[Worlds Reborn] {} started Marvel {}", player.getScoreboardName(), quest);
        LIVE.remove(player.getUUID());
        cleanUpBefore(player, quest);
        after(player, quest.equals(S12) ? 60 : 30, "title:" + quest);
        session(player).started.add("title:" + quest);
    }

    /** The people of the previous scene go home: nobody is left standing around a site the story has moved on from. */
    private static void cleanUpBefore(ServerPlayer player, String quest) {
        var server = player.server;
        if (server.getLevel(NEW_YORK) instanceof ServerLevel ny) {
            if (quest.equals(S13) || quest.equals(S14)) {
                for (String who : List.of("s12_shang_chi", "s12_scott", "s12_joaquin", "s12_pilot", "s12_passenger_0",
                        "s12_passenger_1", "s12_crew_0", "s12_crew_1", "s12_site_shang_chi", "s12_site_scott")) {
                    removeAll(ny, tag(player, who), n(LANDING), 400);
                }
                removeHeli(ny, player);
            }
            if (quest.equals(S14) || quest.equals(S15)) {
                for (String who : List.of("s13_yelena", "s13_walker", "s13_red_guardian", "s13_ghost", "s13_sentry", "s13_clint",
                        "s13_kate", "s13_responder_0", "s13_responder_1", "s13_g1_0", "s13_g1_1", "s13_g1_2", "s13_g2_0",
                        "s13_g2_1", "s13_g2_2", "s13_g2_3")) {
                    removeAll(ny, tag(player, who), n(FAC), 120);
                }
            }
            if (quest.equals(S15)) {
                for (int i = 0; i < BRIEFING_SPOTS.size(); i++) {
                    removeAll(ny, tag(player, "s14_team_" + i), n(BASE_SAM_START), 80);
                }
            }
        }
    }

    /** Plays a scene's title card, then puts the player in that scene's hero's place. */
    private static void title(ServerPlayer player, String quest) {
        if (!Profiles.get(player).isQuestActive(quest)) {
            return;
        }
        if (!idle(player)) {
            after(player, 10, "title:" + quest);
            return;
        }
        String cin = switch (quest) {
            case S12 -> CIN_S12_TITLE;
            case S13 -> CIN_S13_TITLE;
            case S14 -> CIN_S14_TITLE;
            case S15 -> CIN_S15_TITLE;
            default -> CIN_S16_TITLE;
        };
        StoryService.playCinematic(player, cin, CIN_TITLE_TICKS);
    }

    /** Arrives as the hero of {@code quest}, at the scene's start. */
    private static void arrive(ServerPlayer player, String quest) {
        var server = player.server;
        Hero hero = heroFor(quest);
        switch (quest) {
            case S12, S13, S14 -> {
                if (server.getLevel(NEW_YORK) instanceof ServerLevel ny) {
                    RespondersSites.ensureNewYork(ny);
                    BlockPos at = switch (quest) {
                        case S12 -> n(BASE_SAM_START);
                        case S13 -> n(FAC_ARRIVAL);
                        default -> n(BASE_SAM_START);
                    };
                    becomeHero(player, hero, ny, at, quest.equals(S13) ? 0f : -90f);
                }
            }
            case S15 -> {
                if (server.getLevel(FF_WORLD) instanceof ServerLevel ffw) {
                    RespondersSites.ensureFfLab(ffw);
                    becomeHero(player, hero, ffw, ff(FF_SUE_START), 180f);
                }
            }
            default -> {
                if (server.getLevel(XMEN_WORLD) instanceof ServerLevel xw) {
                    RespondersSites.ensureXmen(xw);
                    becomeHero(player, hero, xw, xs(XS_CYCLOPS_START), 180f);
                }
            }
        }
    }

    /** On login: the player comes back as the hero of the scene they are on, or as Steve outside these scenes. */
    static void onLogin(ServerPlayer player) {
        Current c = current(player);
        Hero want = c != null && SCENES_12_16.contains(c.quest()) ? heroFor(c.quest()) : null;
        Hero have = currentHero(player);
        if (want == null && have != null) {
            Perspective.becomeSteve(player);
        } else if (want != null && have != want) {
            // Between the end of the last scene and this hero's arrival: run the title card again.
            after(player, 40, "title:" + c.quest());
            session(player).started.add("title:" + c.quest());
        }
    }

    /** Developer scene jump to Scene 12-16: the earlier scenes are complete; reset this scene's site and carry flags. */
    static void prepareJump(ServerPlayer player, int scene) {
        LIVE.remove(player.getUUID());
        RespondersSites.reset(player.server, scene);
        if (player.server.getLevel(NEW_YORK) instanceof ServerLevel ny) {
            RespondersSites.ensureNewYork(ny);
            removeHeli(ny, player);
        }
        PlayerProgress p = Profiles.get(player);
        p.setFlag(FLAG_STATE_PREFIX + "other_earth_visible");
        if (scene >= 15) {
            p.setFlag(FLAG_STATE_PREFIX + "reed_contact_established");
        }
    }

    // ================================================================== setup: the world matches the step

    static void setup(ServerPlayer player, Current c) {
        Hero hero = heroFor(c.quest());
        if (currentHero(player) != hero) {
            // Still Steve (or the previous hero) until the title card has played.
            if (!pending(player, "title:" + c.quest()) && once(player, "title:" + c.quest())) {
                after(player, 40, "title:" + c.quest());
            }
            return;
        }
        var server = player.server;
        switch (c.quest()) {
            case S12, S13, S14 -> {
                ServerLevel ny = server.getLevel(NEW_YORK);
                if (ny == null || !in(player, NEW_YORK)) {
                    return;
                }
                RespondersSites.ensureNewYork(ny);
                switch (c.quest()) {
                    case S12 -> setupS12(player, ny, c.step());
                    case S13 -> setupS13(player, ny, c.step());
                    default -> setupS14(player, ny, c.step());
                }
            }
            case S15 -> {
                if (server.getLevel(FF_WORLD) instanceof ServerLevel ffw && in(player, FF_WORLD)) {
                    RespondersSites.ensureFfLab(ffw);
                    setupS15(player, ffw, c.step());
                }
            }
            default -> {
                if (server.getLevel(XMEN_WORLD) instanceof ServerLevel xw && in(player, XMEN_WORLD)) {
                    RespondersSites.ensureXmen(xw);
                    setupS16(player, xw, c.step());
                }
            }
        }
    }

    // ---------------------------------------------------------------- Scene 12, A World on Alert

    private static void setupS12(ServerPlayer player, ServerLevel ny, String step) {
        setFlight(player, !step.equals(S12_RESPONSE) || state(player, "launched"));
        switch (step) {
            case S12_RESPONSE -> {
                checkpoint(player, NEW_YORK, n(BASE_SAM_START));
                post(ny, SHANG_CHI, tag(player, "s12_shang_chi"), n(BASE_SHANG_CHI), 90f, false);
                post(ny, SCOTT, tag(player, "s12_scott"), n(BASE_SCOTT), 90f, false);
                post(ny, JOAQUIN, tag(player, "s12_joaquin"), n(BASE_JOAQUIN), -90f, false);
                if (!state(player, "s12_briefed")) {
                    target(player, NEW_YORK, n(BASE_DISPLAY_VIEW), 3);
                    if (near(player, NEW_YORK, n(BASE_DISPLAY_VIEW), 9)) {
                        autoOpen(player, "d12_reports", 30);
                    }
                } else if (!state(player, "route_reviewed")) {
                    target(player, NEW_YORK, n(ROUTE_TABLE), 2);
                    hint(player, "s12_route", "message.wrmarvel.r.review_route");
                } else {
                    target(player, NEW_YORK, n(LAUNCH_PAD), 3);
                    hint(player, "s12_launch", "message.wrmarvel.r.launch");
                }
            }
            case S12_SURVEY -> {
                checkpoint(player, NEW_YORK, n(LAUNCH_PAD));
                landingSite(player, ny, false);
                int i = nextSample(player);
                if (i >= 0) {
                    target(player, NEW_YORK, n(SURVEY.get(i)), 7);
                }
                hint(player, "s12_fly", "message.wrmarvel.r.fly");
            }
            case S12_PASSENGERS -> {
                checkpoint(player, NEW_YORK, n(LANDING).offset(0, 0, 8));
                landingSite(player, ny, false);
                Live l = live(player);
                if (l.carried >= 0) {
                    target(player, NEW_YORK, n(SAFE_AREA), 5);
                } else if (l.heliPos != null) {
                    target(player, NEW_YORK, BlockPos.containing(l.heliPos), 4);
                } else {
                    target(player, NEW_YORK, n(HELI_HOLD), 6);
                }
            }
            case S12_DONE -> {
                checkpoint(player, NEW_YORK, n(LANDING).offset(0, 0, 8));
                landingSite(player, ny, state(player, "heli_landed"));
                target(player, NEW_YORK, n(LANDING), 5);
                if (state(player, "heli_landed") && near(player, NEW_YORK, n(LANDING), 16) && !player.getAbilities().flying) {
                    autoOpen(player, state(player, "s12_accounted") ? "d12_population" : "d12_landed", 20);
                } else if (state(player, "heli_landed")) {
                    hint(player, "s12_land", "message.wrmarvel.r.land_with_them");
                }
            }
            default -> {
            }
        }
    }

    /** The emergency landing site: crews, Shang-Chi and Scott helping below, and the people already brought down. */
    private static void landingSite(ServerPlayer player, ServerLevel ny, boolean pilotDown) {
        for (int i = 0; i < CREW_POSTS.size(); i++) {
            post(ny, GROUND_CREW, tag(player, "s12_crew_" + i), n(CREW_POSTS.get(i)), 0f, false);
        }
        post(ny, SHANG_CHI, tag(player, "s12_site_shang_chi"), n(SITE_SHANG_CHI), 90f, false);
        post(ny, SCOTT, tag(player, "s12_site_scott"), n(SITE_SCOTT), 90f, false);
        for (int i = 0; i < PASSENGERS.size(); i++) {
            if (state(player, "passenger_safe_" + i)) {
                StoryNpcEntity pass = npc(ny, PASSENGERS.get(i), tag(player, "s12_passenger_" + i), n(SAFE_SPOTS.get(i)), 0f);
                if (pass != null) {
                    pass.setNoGravity(false);
                    sendTo(pass, n(SAFE_SPOTS.get(i)));
                }
            }
        }
        if (pilotDown) {
            StoryNpcEntity pilot = npc(ny, PILOT, tag(player, "s12_pilot"), n(LANDING).offset(2, 0, 0), 0f);
            if (pilot != null) {
                pilot.setNoGravity(false);
                sendTo(pilot, n(SAFE_SPOTS.get(2)));
            }
        }
    }

    private static int nextSample(ServerPlayer player) {
        for (int i = 0; i < SURVEY.size(); i++) {
            if (!state(player, "sample_" + i)) {
                return i;
            }
        }
        return -1;
    }

    /** Every tick in Scene 12: survey sampling, the helicopter and its people, Joaquin's flight, the altitude limit. */
    private static void tickS12(ServerPlayer player, ServerLevel ny, String step, Live l) {
        if (step.equals(S12_RESPONSE)) {
            return;
        }
        // Not a race towards the other Earth: above the survey ceiling Sam turns back.
        if (player.getY() > MarvelSites.NY_ORIGIN.getY() + SURVEY_CEILING) {
            player.setDeltaMovement(player.getDeltaMovement().x * 0.5, -0.8, player.getDeltaMovement().z * 0.5);
            player.hurtMarked = true;
            if (player.tickCount % 20 == 0) {
                message(player, "message.wrmarvel.r.ceiling");
            }
            bark(player, "b12_closer");
        }
        if (step.equals(S12_SURVEY)) {
            survey(player, ny, l);
            joaquinSurvey(player, ny, l);
            return;
        }
        heli(player, ny, step, l);
    }

    private static void survey(ServerPlayer player, ServerLevel ny, Live l) {
        int i = nextSample(player);
        if (i < 0) {
            return;
        }
        Vec3 point = c(n(SURVEY.get(i))).add(0, 0.5, 0);
        if (player.tickCount % 4 == 0) {
            // The marker: a ring of light at the survey position, seen from far away.
            for (int k = 0; k < 20; k++) {
                double a = k * Math.PI / 10 + player.tickCount * 0.02;
                ny.sendParticles(player, MARKER, true, point.x + Math.cos(a) * 6, point.y, point.z + Math.sin(a) * 6, 1, 0, 0, 0, 0);
            }
        }
        double dist = player.position().distanceTo(point);
        boolean steady = player.getAbilities().flying && dist < 8 && l.velocity.length() < 0.55;
        int sample = Math.max(0, counter(player, "sample") + (steady ? 1 : dist < 8 ? -1 : -2));
        setCounter(player, "sample", sample);
        if (dist < 14 && player.tickCount % 10 == 0) {
            bar(player, steady ? "message.wrmarvel.r.sampling" : "message.wrmarvel.r.hold_steady", i + 1, SURVEY.size(),
                    Math.min(100, sample * 100 / 70));
        }
        if (sample >= 70 && setState(player, "sample_" + i)) {
            setCounter(player, "sample", 0);
            ny.playSound(null, player.blockPosition(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1f, 1.3f);
            ny.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1f, 1.6f);
            ny.sendParticles(player, ParticleTypes.END_ROD, true, point.x, point.y, point.z, 60, 3, 1, 3, 0.05);
            player.displayClientMessage(Component.translatable("message.wrmarvel.r.sample_done", i + 1, SURVEY.size()), true);
            if (i == 0) {
                bark(player, "b12_first_sample");
            } else if (i == 1) {
                bark(player, "b12_closer");
            } else {
                progress(player, S12, S12_SURVEY);
                after(player, 30, "s12_distress");
            }
        }
    }

    /** Joaquin flies his own complementary route: an orbit offset from Sam's current survey position. */
    private static void joaquinSurvey(ServerPlayer player, ServerLevel ny, Live l) {
        int i = Math.max(0, nextSample(player));
        Vec3 centre = c(n(SURVEY.get(i))).add(-16, 6, 16);
        l.joaquinAngle += 0.035;
        Vec3 at = centre.add(Math.cos(l.joaquinAngle) * 11, Math.sin(l.joaquinAngle * 2) * 1.5, Math.sin(l.joaquinAngle) * 11);
        fly(player, ny, l, JOAQUIN, "s12_joaquin", at);
    }

    /** Moves a flying person (Joaquin) smoothly; they hang in the air with no gravity while flying. */
    private static void fly(ServerPlayer player, ServerLevel ny, Live l, String who, String name, Vec3 to) {
        StoryNpcEntity n = person(l, ny, who, tag(player, name), BlockPos.containing(player.position().add(0, 2, 0)), 0f);
        if (n == null) {
            return;
        }
        n.setNoGravity(true);
        n.stopWalking();
        Vec3 d = to.subtract(n.position());
        Vec3 step = d.length() > 1.1 ? d.normalize().scale(1.1) : d;
        if (d.length() > 200) {
            step = d;
        }
        float yaw = (float) (Math.atan2(-step.x, step.z) * 180 / Math.PI);
        n.moveTo(n.getX() + step.x, n.getY() + step.y, n.getZ() + step.z, step.lengthSqr() > 0.01 ? yaw : n.getYRot(), 0f);
        n.setYHeadRot(n.getYRot());
        if (player.tickCount % 3 == 0) {
            ny.sendParticles(ParticleTypes.CLOUD, n.getX(), n.getY() + 0.6, n.getZ(), 1, 0.2, 0.1, 0.2, 0.0);
        }
    }

    // ---- the helicopter

    private static final String HELI_TAG = NS + "r_heli";

    private static String heliTag(ServerPlayer player) {
        return own(player, HELI_TAG);
    }

    /** Removes this player's helicopter parts wherever they are (and any left from before a reload). */
    private static void removeHeli(ServerLevel ny, ServerPlayer player) {
        String t = heliTag(player);
        List<Entity> old = new ArrayList<>();
        for (Entity e : ny.getAllEntities()) {
            if (e instanceof Display.BlockDisplay && e.getTags().contains(t)) {
                old.add(e);
            }
        }
        old.forEach(Entity::discard);
        Live l = LIVE.get(player.getUUID());
        if (l != null) {
            l.parts.clear();
        }
    }

    /** One part of the helicopter: a block drawn at an offset and scale from the helicopter's position. */
    private record Part(BlockState block, float tx, float ty, float tz, float sx, float sy, float sz) {
    }

    private static final List<Part> HELI_PARTS = List.of(
            new Part(Blocks.WHITE_CONCRETE.defaultBlockState(), -1.5f, 0f, -0.9f, 3f, 1.6f, 1.8f),
            new Part(Blocks.RED_CONCRETE.defaultBlockState(), -1.5f, 0.55f, -0.92f, 3f, 0.25f, 1.84f),
            new Part(Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState(), 1.5f, 0.2f, -0.8f, 0.8f, 1.2f, 1.6f),
            new Part(Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState(), -5.2f, 0.8f, -0.2f, 3.7f, 0.4f, 0.4f),
            new Part(Blocks.RED_CONCRETE.defaultBlockState(), -5.4f, 0.9f, -0.08f, 0.6f, 1.3f, 0.16f),
            new Part(Blocks.GRAY_CONCRETE.defaultBlockState(), -0.1f, 1.6f, -0.1f, 0.2f, 0.45f, 0.2f),
            new Part(Blocks.BLACK_CONCRETE.defaultBlockState(), -4.2f, 2.05f, -0.18f, 8.4f, 0.08f, 0.36f),
            new Part(Blocks.BLACK_CONCRETE.defaultBlockState(), -0.18f, 2.06f, -4.2f, 0.36f, 0.08f, 8.4f),
            new Part(Blocks.IRON_BLOCK.defaultBlockState(), -1.6f, -0.45f, -1.05f, 3.2f, 0.12f, 0.12f),
            new Part(Blocks.IRON_BLOCK.defaultBlockState(), -1.6f, -0.45f, 0.93f, 3.2f, 0.12f, 0.12f));

    private static ListTag floats(float... v) {
        ListTag t = new ListTag();
        for (float f : v) {
            t.add(FloatTag.valueOf(f));
        }
        return t;
    }

    @Nullable
    private static Display.BlockDisplay display(ServerLevel level, Part part, Vec3 at, String tagName) {
        Display.BlockDisplay d = EntityType.BLOCK_DISPLAY.create(level);
        if (d == null) {
            return null;
        }
        CompoundTag t = new CompoundTag();
        ListTag pos = new ListTag();
        pos.add(DoubleTag.valueOf(at.x));
        pos.add(DoubleTag.valueOf(at.y));
        pos.add(DoubleTag.valueOf(at.z));
        t.put("Pos", pos);
        t.put("block_state", NbtUtils.writeBlockState(part.block()));
        CompoundTag tr = new CompoundTag();
        tr.put("translation", floats(part.tx(), part.ty(), part.tz()));
        tr.put("scale", floats(part.sx(), part.sy(), part.sz()));
        tr.put("left_rotation", floats(0, 0, 0, 1));
        tr.put("right_rotation", floats(0, 0, 0, 1));
        t.put("transformation", tr);
        t.putInt("teleport_duration", 2);
        t.putFloat("view_range", 6f);
        d.load(t);
        d.addTag(tagName);
        level.addFreshEntity(d);
        return d;
    }

    private static void heli(ServerPlayer player, ServerLevel ny, String step, Live l) {
        boolean landed = state(player, "heli_landed");
        if (l.heliPos == null) {
            removeHeli(ny, player);
            l.heliPos = landed ? c(n(LANDING)).add(0, 0.45, 0) : c(n(HELI_HOLD));
        }
        if (l.parts.isEmpty() || l.parts.stream().anyMatch(Entity::isRemoved)) {
            removeHeli(ny, player);
            for (Part part : HELI_PARTS) {
                Display.BlockDisplay d = display(ny, part, l.heliPos, heliTag(player));
                if (d != null) {
                    l.parts.add(d);
                }
            }
        }
        Vec3 before = l.heliPos;
        l.heliT++;
        boolean landing = step.equals(S12_DONE) || state(player, "passengers_delivered");
        if (!landed && !landing) {
            // Struggling to hold position: a slow wander with jolts as the instruments fail.
            Vec3 hold = c(n(HELI_HOLD));
            Vec3 want = hold.add(6 * Math.sin(l.heliT * 0.021), 1.8 * Math.sin(l.heliT * 0.047), 5 * Math.cos(l.heliT * 0.017));
            if (l.heliT % 90 < 6) {
                want = want.add(0, -0.8, 1.2);
            }
            Vec3 d = want.subtract(l.heliPos);
            l.heliPos = l.heliPos.add(d.length() > 0.22 ? d.normalize().scale(0.22) : d);
        } else if (!landed) {
            // Joaquin guides the pilot down onto the marked pad.
            Vec3 pad = c(n(LANDING)).add(0, 0.45, 0);
            Vec3 above = pad.add(0, 10, 0);
            Vec3 aim = Math.abs(l.heliPos.x - pad.x) + Math.abs(l.heliPos.z - pad.z) > 1.0 ? above : pad;
            Vec3 d = aim.subtract(l.heliPos);
            double speed = aim == pad ? 0.09 : 0.3;
            l.heliPos = l.heliPos.add(d.length() > speed ? d.normalize().scale(speed) : d);
            if (l.heliPos.distanceTo(pad) < 0.05 && setState(player, "heli_landed")) {
                ny.playSound(null, BlockPos.containing(pad), SoundEvents.IRON_GOLEM_STEP, SoundSource.NEUTRAL, 1.5f, 0.5f);
            }
        }
        l.heliVel = l.heliPos.subtract(before);
        for (Display.BlockDisplay d : l.parts) {
            d.moveTo(l.heliPos.x, l.heliPos.y, l.heliPos.z, 0f, 0f);
        }
        if (!landed && l.heliT % 5 == 0) {
            ny.sendParticles(ParticleTypes.SMOKE, l.heliPos.x - 1.2, l.heliPos.y + 1.4, l.heliPos.z, 2, 0.2, 0.1, 0.2, 0.01);
            for (int k = 0; k < 6; k++) {
                double a = l.heliT * 0.9 + k * Math.PI / 3;
                ny.sendParticles(ParticleTypes.CLOUD, l.heliPos.x + Math.cos(a) * 3.8, l.heliPos.y + 2.1, l.heliPos.z + Math.sin(a) * 3.8,
                        1, 0, 0, 0, 0);
            }
        }
        if (!landed && l.heliT % 30 == 0) {
            ny.playSound(null, BlockPos.containing(l.heliPos), SoundEvents.ELYTRA_FLYING, SoundSource.NEUTRAL, 1.6f, 0.45f);
        }
        heliPeople(player, ny, step, l, landed);
        // Joaquin stays off the helicopter's nose, guiding the pilot; once it is down he lands by the crews.
        if (!landed) {
            fly(player, ny, l, JOAQUIN, "s12_joaquin", l.heliPos.add(5, 1.5, -3));
        } else {
            StoryNpcEntity j = person(l, ny, JOAQUIN, tag(player, "s12_joaquin"), n(SITE_JOAQUIN), 0f);
            if (j != null && j.isNoGravity()) {
                j.setNoGravity(false);
                j.moveTo(n(SITE_JOAQUIN).getX() + 0.5, n(SITE_JOAQUIN).getY(), n(SITE_JOAQUIN).getZ() + 0.5, 0f, 0f);
            }
        }
    }

    private static Vec3 door(Live l) {
        return l.heliPos.add(0, 0.1, 1.9);
    }

    /** The pilot in the cockpit, the passengers at the side door, the one Sam is carrying, and the approach. */
    private static void heliPeople(ServerPlayer player, ServerLevel ny, String step, Live l, boolean landed) {
        if (!landed) {
            StoryNpcEntity pilot = person(l, ny, PILOT, tag(player, "s12_pilot"), BlockPos.containing(l.heliPos), 90f);
            if (pilot != null) {
                pilot.setNoGravity(true);
                pilot.stopWalking();
                pilot.moveTo(l.heliPos.x + 1.6, l.heliPos.y, l.heliPos.z, -90f, 0f);
            }
        }
        for (int i = 0; i < PASSENGERS.size(); i++) {
            if (state(player, "passenger_safe_" + i)) {
                continue;
            }
            StoryNpcEntity p = person(l, ny, PASSENGERS.get(i), tag(player, "s12_passenger_" + i), BlockPos.containing(door(l)), 0f);
            if (p == null) {
                continue;
            }
            p.setNoGravity(true);
            p.stopWalking();
            if (l.carried == i) {
                // Held by Sam, below and in front of him.
                Vec3 at = player.position().add(player.getLookAngle().multiply(0.6, 0, 0.6)).add(0, -1.7, 0);
                p.moveTo(at.x, at.y, at.z, player.getYRot(), 0f);
                p.setCowering(false);
            } else {
                p.moveTo(door(l).x + (i - 0.5) * 1.2, door(l).y, door(l).z - 0.4, 0f, 0f);
                p.setCowering(!landed);
            }
        }
        if (landed || step.equals(S12_DONE)) {
            return;
        }
        Vec3 door = door(l);
        double dist = player.position().distanceTo(door);
        // The rotor: too close above the cabin and the downwash throws Sam clear.
        double flat = Math.hypot(player.getX() - l.heliPos.x, player.getZ() - l.heliPos.z);
        double up = player.getY() - l.heliPos.y;
        if (flat < 4.6 && up > 1.2 && up < 4.2) {
            Vec3 away = new Vec3(player.getX() - l.heliPos.x, 0, player.getZ() - l.heliPos.z).normalize();
            player.setDeltaMovement(away.x * 0.9, -0.4, away.z * 0.9);
            player.hurtMarked = true;
            if (player.tickCount % 20 == 0) {
                message(player, "message.wrmarvel.r.rotor");
            }
            l.approach = 0;
            return;
        }
        double rel = l.velocity.subtract(l.heliVel).length();
        if (dist < 4 && rel < 0.22) {
            l.approach = Math.min(40, l.approach + 1);
            if (l.approach == 20) {
                message(player, "message.wrmarvel.r.approach_ok");
                ny.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 0.7f, 1.5f);
            }
        } else if (dist > 9) {
            l.approach = 0;
        } else if (dist < 6 && rel > 0.45) {
            l.approach = Math.max(0, l.approach - 2);
            if (player.tickCount % 20 == 0) {
                message(player, "message.wrmarvel.r.match_speed");
            }
        }
        if (dist < 7 && l.carried < 0 && l.approach < 20 && player.tickCount % 40 == 0) {
            message(player, "message.wrmarvel.r.approach_hint");
        }
        // Setting someone down with the crews.
        if (l.carried >= 0) {
            Vec3 safe = c(n(SAFE_AREA));
            if (Math.hypot(player.getX() - safe.x, player.getZ() - safe.z) < 8 && player.getY() < safe.y + 4) {
                int i = l.carried;
                l.carried = -1;
                StoryNpcEntity p = person(l, ny, PASSENGERS.get(i), tag(player, "s12_passenger_" + i), n(SAFE_SPOTS.get(i)), 0f);
                if (p != null) {
                    BlockPos s = n(SAFE_SPOTS.get(i));
                    p.setNoGravity(false);
                    p.moveTo(s.getX() + 0.5, s.getY(), s.getZ() + 0.5, 0f, 0f);
                }
                setState(player, "passenger_safe_" + i);
                message(player, "message.wrmarvel.r.passenger_safe");
                bark(player, "b12_landing_clear");
                if (state(player, "passenger_safe_0") && state(player, "passenger_safe_1") && setState(player, "passengers_delivered")) {
                    progress(player, S12, S12_PASSENGERS);
                }
            }
        }
    }

    // ---------------------------------------------------------------- Scene 13, Another Response

    private static final List<BlockPos> G1_IN = List.of(fac(-12, 0, 19), fac(-12, 0, 13), fac(-12, 0, 8), fac(-9, 0, 5), LOBBY);
    private static final List<BlockPos> G1_OUT = List.of(fac(0, 0, 3), fac(0, 0, -2), EXTRACTION);
    private static final List<BlockPos> G2_TO_CROSSING = List.of(fac(12, 0, 22), CROSSING_NORTH);
    private static final List<BlockPos> G2_CROSS = List.of(fac(12, 0, 15), CROSSING_SOUTH);
    private static final List<BlockPos> G2_TO_LOBBY = List.of(fac(12, 0, 6), fac(8, 0, 5), fac(4, 0, 5), LOBBY);

    private static BlockPos extractionSpot(int i) {
        return spread(EXTRACTION, i).offset(0, 0, -1);
    }

    private static String g1Char(int i) {
        return i == 0 ? SCIENTIST : FACILITY_STAFF.get(i % 2);
    }

    private static void setupS13(ServerPlayer player, ServerLevel ny, String step) {
        // The team around the facility, and the emergency responders at the perimeter.
        for (int i = 0; i < RESPONDERS.size(); i++) {
            post(ny, EMERGENCY_RESPONDER, tag(player, "s13_responder_" + i), n(RESPONDERS.get(i)), 0f, true);
        }
        boolean entered = !step.equals(S13_ENTRY);
        post(ny, WALKER, tag(player, "s13_walker"), n(FAC_TEAM.get(0)), 0f, false);
        post(ny, RED_GUARDIAN, tag(player, "s13_red_guardian"), n(FAC_TEAM.get(1)), 0f, false);
        if (!entered) {
            post(ny, GHOST, tag(player, "s13_ghost"), n(FAC_TEAM.get(2)), 0f, false);
            post(ny, SENTRY, tag(player, "s13_sentry"), n(FAC_TEAM.get(3)), 0f, false);
        }
        StoryNpcEntity yelena = npc(ny, YELENA, tag(player, "s13_yelena"), n(FAC_YELENA), 0f);
        // Ground floor recovery: anyone who drops into the basement through the broken section is brought back.
        if (player.getY() < MarvelSites.NY_ORIGIN.getY() - 2.5 && player.position().distanceTo(c(n(FAC))) < 60) {
            BlockPos back = n(CROSSING_SOUTH);
            player.teleportTo(back.getX() + 0.5, back.getY(), back.getZ() + 0.5);
            player.fallDistance = 0;
            message(player, "message.wrmarvel.r.fell_through");
        }
        switch (step) {
            case S13_ENTRY -> {
                checkpoint(player, NEW_YORK, n(FAC_ARRIVAL));
                sendTo(yelena, n(FAC_YELENA));
                target(player, NEW_YORK, n(FAC_YELENA), 3);
                if (near(player, NEW_YORK, n(FAC_YELENA), 8)) {
                    autoOpen(player, "d13_distress", 30);
                }
            }
            case S13_FIRST_GROUP -> firstGroup(player, ny, yelena);
            case S13_STAFF -> staffEvacuation(player, ny, yelena);
            case S13_RECORDER -> {
                checkpoint(player, NEW_YORK, n(EXTRACTION));
                evacuated(player, ny);
                sendTo(yelena, n(EXTRACTION).offset(3, 0, 0));
                staffEvacTeam(player, ny);
                if (!state(player, "recorder_talk")) {
                    target(player, NEW_YORK, n(EXTRACTION), 3);
                    if (near(player, NEW_YORK, n(EXTRACTION), 7)) {
                        autoOpen(player, "d13_recorder", 20);
                    }
                } else if (!has(player, MarvelItems.MONITORING_RECORDER.get()) && !state(player, "recorder_taken")) {
                    target(player, NEW_YORK, n(FAC_RECORDER), 2);
                    unsafeStrip(player, ny);
                } else if (has(player, MarvelItems.MONITORING_RECORDER.get())) {
                    target(player, NEW_YORK, n(extractionSpot(0)), 3);
                    unsafeStrip(player, ny);
                    if (near(player, NEW_YORK, n(extractionSpot(0)), 4)) {
                        handInRecorder(player);
                    }
                } else {
                    // Taken before a reload but no longer carried: the crew finds it again on the recorder rack.
                    Profiles.get(player).clearFlag(FLAG_STATE_PREFIX + "recorder_taken");
                }
            }
            case S13_DONE -> {
                evacuated(player, ny);
                sendTo(yelena, n(EXTRACTION).offset(3, 0, 0));
                staffEvacTeam(player, ny);
                target(player, NEW_YORK, n(extractionSpot(0)), 3);
                if (near(player, NEW_YORK, n(EXTRACTION), 8)) {
                    autoOpen(player, "d13_intervals", 20);
                }
            }
            default -> {
            }
        }
    }

    private static void firstGroup(ServerPlayer player, ServerLevel ny, @Nullable StoryNpcEntity yelena) {
        checkpoint(player, NEW_YORK, n(FAC_ARRIVAL).offset(0, 0, 6));
        post(ny, CLINT, tag(player, "s13_clint"), n(FAC_CLINT), 90f, false);
        post(ny, KATE, tag(player, "s13_kate"), n(FAC_KATE), 90f, false);
        boolean moving = state(player, "group1_moving");
        List<StoryNpcEntity> group = new ArrayList<>();
        for (int i = 0; i < GROUP_1.size(); i++) {
            StoryNpcEntity s = npc(ny, g1Char(i), tag(player, "s13_g1_" + i), n(GROUP_1.get(i)), 180f);
            if (s != null) {
                group.add(s);
                s.setCowering(!moving);
            }
        }
        if (!state(player, "ck_met")) {
            sendTo(yelena, n(LOBBY).offset(-3, 0, 0));
            target(player, NEW_YORK, n(FAC_CLINT), 3);
            if (near(player, NEW_YORK, n(FAC_CLINT), 6)) {
                autoOpen(player, "d13_clint_kate", 10);
            }
            return;
        }
        if (!RespondersSites.obstructionCleared(ny)) {
            sendTo(yelena, n(OBSTRUCTION).offset(2, 0, -3));
            target(player, NEW_YORK, n(OBSTRUCTION), 2);
            // The cable route Clint and Kate rigged, picked out on the way.
            if (player.tickCount % 20 == 3) {
                for (int z = 5; z <= 14; z += 3) {
                    BlockPos p = n(fac(-12, 6, z));
                    ny.sendParticles(player, ParticleTypes.WAX_OFF, false, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 2, 0.2, 0.2, 0.2, 0);
                }
            }
            if (near(player, NEW_YORK, n(OBSTRUCTION), 6)) {
                hint(player, "s13_lift", "message.wrmarvel.r.lift_hint");
            }
            return;
        }
        if (!RespondersSites.doorBraced(ny)) {
            target(player, NEW_YORK, n(BRACE), 2);
            hint(player, "s13_brace", "message.wrmarvel.r.brace_hint");
            return;
        }
        if (!moving) {
            target(player, NEW_YORK, n(ROOM_1), 3);
            if (near(player, NEW_YORK, n(ROOM_1), 6) && setState(player, "group1_moving")) {
                bark(player, "b13_this_way");
                message(player, "message.wrmarvel.r.lead_group");
            }
            return;
        }
        // Escort: in a line behind Bucky to the lobby; Yelena checks the two side rooms while he covers her; then out.
        boolean roomsClear = state(player, "room_b_clear");
        boolean allIn = true;
        for (int i = 0; i < group.size(); i++) {
            List<BlockPos> route = new ArrayList<>(G1_IN);
            route.set(route.size() - 1, spread(LOBBY, i));
            if (!roomsClear) {
                allIn &= walkRoute(player, group.get(i), "g1in_" + i, nRoute(route));
            } else {
                List<BlockPos> out = new ArrayList<>(G1_OUT);
                out.set(out.size() - 1, extractionSpot(i));
                walkRoute(player, group.get(i), "g1out_" + i, nRoute(out));
            }
        }
        if (!roomsClear) {
            target(player, NEW_YORK, n(LOBBY), 3);
            if (allIn && near(player, NEW_YORK, n(LOBBY), 10)) {
                checkRooms(player, ny, yelena);
            } else {
                sendTo(yelena, n(LOBBY).offset(-2, 0, 2));
            }
            return;
        }
        sendTo(yelena, n(EXTRACTION).offset(3, 0, 0));
        target(player, NEW_YORK, n(EXTRACTION), 3);
        boolean out = true;
        for (int i = 0; i < group.size(); i++) {
            out &= group.get(i).position().distanceTo(c(n(extractionSpot(i)))) < 2.5;
        }
        if (out && group.size() == GROUP_1.size()) {
            progress(player, S13, S13_FIRST_GROUP);
        }
    }

    private static List<BlockPos> nRoute(List<BlockPos> route) {
        List<BlockPos> out = new ArrayList<>(route.size());
        for (BlockPos p : route) {
            out.add(n(p));
        }
        return out;
    }

    /** Yelena checks each adjoining room while Bucky covers her from the lobby. She waits if he wanders off. */
    private static void checkRooms(ServerPlayer player, ServerLevel ny, @Nullable StoryNpcEntity yelena) {
        if (yelena == null) {
            return;
        }
        int room = state(player, "room_a_clear") ? 1 : 0;
        BlockPos inside = n(SIDE_ROOMS.get(room));
        boolean covered = player.distanceTo(yelena) < 10;
        if (!covered) {
            yelena.stopWalking();
            if (player.tickCount % 40 == 3) {
                message(player, "message.wrmarvel.r.cover_yelena");
            }
            return;
        }
        if (!yelena.blockPosition().closerThan(inside, 1.8)) {
            sendTo(yelena, inside);
            setCounter(player, "room_check", 0);
            return;
        }
        int t = counter(player, "room_check") + 1;
        setCounter(player, "room_check", t);
        if (t >= 4) {
            setCounter(player, "room_check", 0);
            if (room == 0) {
                setState(player, "room_a_clear");
            } else if (setState(player, "room_b_clear")) {
                bark(player, "b13_room_clear");
                message(player, "message.wrmarvel.r.take_them_out");
            }
        }
    }

    /** 13.4: Ghost, Clint and Kate, Walker and Red Guardian, the Sentry, and the second group across the crossing. */
    private static void staffEvacuation(ServerPlayer player, ServerLevel ny, @Nullable StoryNpcEntity yelena) {
        checkpoint(player, NEW_YORK, n(LOBBY));
        evacuated(player, ny);
        sendTo(yelena, n(CROSSING_SOUTH).offset(-2, 0, -2));
        if (once(player, "s13_staff_start") && !state(player, "exit_rubble_dropped")) {
            // The disturbance returns: part of the front comes down across the exit.
            RespondersSites.dropExitRubble(ny);
            setState(player, "exit_rubble_dropped");
            ny.playSound(null, n(FAC_DOOR), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.2f, 0.6f);
            ny.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, n(FAC_DOOR).getX() + 0.5, n(FAC_DOOR).getY() + 1, n(FAC_DOOR).getZ(), 20, 1.5, 1, 0.5, 0.01);
            bark(player, "b13_exit");
        }
        // Walker and Red Guardian keep the exit accessible.
        post(ny, WALKER, tag(player, "s13_walker"), n(FAC_DOOR).offset(-2, 0, -1), 0f, false);
        post(ny, RED_GUARDIAN, tag(player, "s13_red_guardian"), n(FAC_DOOR).offset(2, 0, -1), 0f, false);
        // Ghost passes through the seal to the trapped staff and opens it from their side.
        StoryNpcEntity ghost = npc(ny, GHOST, tag(player, "s13_ghost"), n(SEALED_DOOR).offset(0, 0, 2), 180f);
        if (!state(player, "seal_open")) {
            if (ghost != null && once(player, "s13_ghost_phase")) {
                ny.sendParticles(ParticleTypes.REVERSE_PORTAL, ghost.getX(), ghost.getY() + 1, ghost.getZ(), 40, 0.3, 0.9, 0.3, 0.05);
                after(player, 60, "s13_seal");
            }
        }
        // Clint and Kate at the crossing; the cable goes across when Bucky gets there.
        StoryNpcEntity clint = npc(ny, CLINT, tag(player, "s13_clint"), n(CROSSING_SOUTH).offset(-2, 0, -1), 0f);
        StoryNpcEntity kate = npc(ny, KATE, tag(player, "s13_kate"), n(CROSSING_SOUTH).offset(2, 0, -1), 0f);
        sendTo(clint, n(CROSSING_SOUTH).offset(-2, 0, -1));
        sendTo(kate, n(CROSSING_SOUTH).offset(2, 0, -1));
        boolean cable = RespondersSites.cableSet(ny);
        if (!cable && near(player, NEW_YORK, n(CROSSING_SOUTH), 9) && once(player, "s13_cable")) {
            after(player, 30, "s13_cable_set");
            if (kate != null) {
                beam(ny, kate.getEyePosition(), c(n(CROSSING_NORTH)).add(0, 2, 0), ParticleTypes.CRIT);
            }
            ny.playSound(null, n(CROSSING_SOUTH), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1f, 1.1f);
        }
        // The Sentry holds the sagging ceiling over the crossing.
        StoryNpcEntity sentry = npc(ny, SENTRY, tag(player, "s13_sentry"), n(SENTRY_HOLD), 0f);
        if (sentry != null) {
            sentry.setNoGravity(true);
            sentry.stopWalking();
            BlockPos h = n(SENTRY_HOLD);
            if (!sentry.blockPosition().closerThan(h, 1)) {
                sentry.moveTo(h.getX() + 0.5, h.getY(), h.getZ() + 0.5, 0f, 0f);
            }
            ny.sendParticles(ParticleTypes.END_ROD, h.getX() + 0.5, h.getY() + 2.6, h.getZ() + 0.5, 3, 1.5, 0.2, 1.5, 0.01);
            if (cable && near(player, NEW_YORK, n(CROSSING_SOUTH), 10)) {
                bark(player, "b13_sentry");
            }
        }
        if (!state(player, "seal_open")) {
            target(player, NEW_YORK, n(CROSSING_SOUTH), 3);
            return;
        }
        if (!cable) {
            target(player, NEW_YORK, n(CROSSING_SOUTH), 3);
            return;
        }
        // The second group: one at a time across the cable; Bucky keeps off it while someone is on it.
        boolean exitClear = RespondersSites.exitClear(ny);
        if (!exitClear) {
            target(player, NEW_YORK, n(FAC_DOOR), 2);
            hint(player, "s13_exit", "message.wrmarvel.r.clear_exit");
        } else {
            target(player, NEW_YORK, n(EXTRACTION), 3);
        }
        BlockPos rel = player.blockPosition().subtract(MarvelSites.NY_ORIGIN.offset(FAC));
        boolean bucyOnCrossing = rel.getX() >= 8 && rel.getX() <= 17 && rel.getZ() >= GAP_Z1 && rel.getZ() <= GAP_Z2;
        int crossing = counter(player, "crossing") - 1;
        int safe = 0;
        for (int i = 0; i < GROUP_2.size(); i++) {
            StoryNpcEntity s = npc(ny, FACILITY_STAFF.get(i % 2), tag(player, "s13_g2_" + i), n(GROUP_2.get(i)), 180f);
            if (s == null) {
                continue;
            }
            s.setCowering(false);
            if (s.getY() < MarvelSites.NY_ORIGIN.getY() - 2.5) {
                BlockPos back = n(CROSSING_NORTH).offset(i - 2, 0, 2);
                s.stopWalking();
                s.moveTo(back.getX() + 0.5, back.getY(), back.getZ() + 0.5, 180f, 0f);
                session(player).counters.remove("g2x_" + i);
                setCounter(player, "crossing", 0);
                continue;
            }
            if (state(player, "g2_out_" + i)) {
                List<BlockPos> route = new ArrayList<>(G2_TO_LOBBY);
                if (exitClear) {
                    route.addAll(G1_OUT);
                    route.set(route.size() - 1, extractionSpot(GROUP_1.size() + i));
                } else {
                    route.set(route.size() - 1, spread(LOBBY, i));
                }
                if (walkRoute(player, s, (exitClear ? "g2out_" : "g2lob_") + i, nRoute(route)) && exitClear) {
                    safe++;
                }
                continue;
            }
            if (crossing == i) {
                if (bucyOnCrossing) {
                    // Too much weight on the cable: they step back and wait.
                    s.stopWalking();
                    BlockPos back = n(CROSSING_NORTH);
                    s.moveTo(back.getX() + 0.5, back.getY(), back.getZ() + 0.5, 180f, 0f);
                    session(player).counters.remove("g2x_" + i);
                    setCounter(player, "crossing", 0);
                    message(player, "message.wrmarvel.r.one_at_a_time");
                    ny.playSound(null, n(CROSSING_NORTH), SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1f, 0.6f);
                } else if (walkRoute(player, s, "g2x_" + i, nRoute(G2_CROSS))) {
                    setState(player, "g2_out_" + i);
                    setCounter(player, "crossing", 0);
                }
                continue;
            }
            // Waiting their turn on the north side.
            if (walkRoute(player, s, "g2q_" + i, nRoute(List.of(G2_TO_CROSSING.get(0), CROSSING_NORTH.offset(i * 2 - 3, 0, 2))))
                    && crossing < 0 && !bucyOnCrossing && firstWaiting(player, i)) {
                setCounter(player, "crossing", i + 1);
            }
        }
        if (safe == GROUP_2.size()) {
            progress(player, S13, S13_STAFF);
        }
    }

    private static boolean firstWaiting(ServerPlayer player, int i) {
        for (int k = 0; k < i; k++) {
            if (!state(player, "g2_out_" + k)) {
                return false;
            }
        }
        return true;
    }

    /** Everyone already brought out waits safely with the responders. */
    private static void evacuated(ServerPlayer player, ServerLevel ny) {
        for (int i = 0; i < GROUP_1.size(); i++) {
            StoryNpcEntity s = npc(ny, g1Char(i), tag(player, "s13_g1_" + i), n(extractionSpot(i)), 180f);
            if (s != null) {
                s.setCowering(false);
                if (!Profiles.get(player).isOnStep(StoryService.quest(S13), S13_FIRST_GROUP)) {
                    sendTo(s, n(extractionSpot(i)));
                }
            }
        }
        Current c = current(player);
        if (c != null && !c.step().equals(S13_STAFF) && !c.step().equals(S13_FIRST_GROUP)) {
            for (int i = 0; i < GROUP_2.size(); i++) {
                StoryNpcEntity s = npc(ny, FACILITY_STAFF.get(i % 2), tag(player, "s13_g2_" + i), n(extractionSpot(GROUP_1.size() + i)), 180f);
                sendTo(s, n(extractionSpot(GROUP_1.size() + i)));
            }
        }
    }

    private static void staffEvacTeam(ServerPlayer player, ServerLevel ny) {
        post(ny, CLINT, tag(player, "s13_clint"), n(EXTRACTION).offset(-4, 0, 2), 0f, false);
        post(ny, KATE, tag(player, "s13_kate"), n(EXTRACTION).offset(-5, 0, 0), 0f, false);
        post(ny, GHOST, tag(player, "s13_ghost"), n(EXTRACTION).offset(5, 0, 2), 0f, false);
        StoryNpcEntity sentry = npc(ny, SENTRY, tag(player, "s13_sentry"), n(SENTRY_HOLD), 0f);
        if (sentry != null && player.tickCount % 20 == 3) {
            BlockPos h = n(SENTRY_HOLD);
            ny.sendParticles(ParticleTypes.END_ROD, h.getX() + 0.5, h.getY() + 2.6, h.getZ() + 0.5, 3, 1.5, 0.2, 1.5, 0.01);
        }
    }

    /** The failing strip along the east wall gives way under anyone who tries it instead of the cable. */
    private static void unsafeStrip(ServerPlayer player, ServerLevel ny) {
        BlockPos rel = player.blockPosition().subtract(MarvelSites.NY_ORIGIN.offset(FAC));
        if (rel.getX() == UNSAFE_X && rel.getZ() >= GAP_Z1 && rel.getZ() <= GAP_Z2 && rel.getY() >= -1) {
            for (int z = GAP_Z1; z <= GAP_Z2; z++) {
                ny.setBlock(n(fac(UNSAFE_X, -1, z)), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            ny.playSound(null, player.blockPosition(), SoundEvents.DEEPSLATE_BREAK, SoundSource.BLOCKS, 1.2f, 0.6f);
            message(player, "message.wrmarvel.r.unsafe_route");
        }
    }

    private static void handInRecorder(ServerPlayer player) {
        if (take(player, MarvelItems.MONITORING_RECORDER.get())) {
            player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8f, 1f);
            message(player, "message.wrmarvel.r.recorder_handed");
            progress(player, S13, S13_RECORDER);
        }
    }

    // ---------------------------------------------------------------- Scene 14, First Contact

    private static final String[] BRIEFING_TEAM = {JOAQUIN, SHANG_CHI, SCOTT, BUCKY, YELENA, WALKER, RED_GUARDIAN, GHOST, SENTRY, CLINT, KATE};

    private static void setupS14(ServerPlayer player, ServerLevel ny, String step) {
        // Everyone in small groups around the room; turned towards the display.
        for (int i = 0; i < BRIEFING_TEAM.length; i++) {
            post(ny, BRIEFING_TEAM[i], tag(player, "s14_team_" + i), n(BRIEFING_SPOTS.get(i)), 90f, true);
        }
        checkpoint(player, NEW_YORK, n(BASE_SAM_START));
        switch (step) {
            case S14_PLAN -> {
                target(player, NEW_YORK, n(BASE_DISPLAY_VIEW), 3);
                if (near(player, NEW_YORK, n(BASE_DISPLAY_VIEW), 9)) {
                    autoOpen(player, "d14_briefing", 30);
                }
            }
            case S14_ARRAY -> {
                if (!state(player, "window_told")) {
                    autoOpen(player, "d14_window", 20);
                    return;
                }
                RespondersSites.setArrayLamps(ny, state(player, "array_powered"));
                BlockPos next = !state(player, "array_powered") ? ARRAY_POWER : !state(player, "dish_aligned") ? ARRAY_DISH : ARRAY_TEST;
                target(player, NEW_YORK, n(next), 2);
            }
            case S14_DONE -> {
                RespondersSites.setArrayLamps(ny, true);
                if (!state(player, "contact_open")) {
                    target(player, NEW_YORK, n(TRANSMIT), 2);
                    if (once(player, "s14_static")) {
                        ny.playSound(null, player.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 1f, 0.5f);
                        message(player, "message.wrmarvel.r.static");
                        after(player, 60, "open:d14_contact");
                        session(player).started.add("open:d14_contact");
                    }
                } else if (!state(player, "packet_sent")) {
                    target(player, NEW_YORK, n(TRANSMIT), 2);
                    hint(player, "s14_send", "message.wrmarvel.r.send_packet");
                }
            }
            default -> {
            }
        }
    }

    /** Readable alignment: how far the dish is from the window Shuri found (0 is locked). */
    private static int dishTarget(ServerPlayer player) {
        return 3 + Math.floorMod(player.getUUID().hashCode(), 7);
    }

    // ---------------------------------------------------------------- Scene 15, Across the Divide

    private static void setupS15(ServerPlayer player, ServerLevel ffw, String step) {
        Live l = live(player);
        if (once(player, "s15_sweep")) {
            sweepFields(ffw, l);
        }
        checkpoint(player, FF_WORLD, ff(FF_SUE_START));
        post(ffw, REED, tag(player, "s15_reed"), ff(FF_REED), 180f, false);
        boolean onRoof = step.equals(S15_LAB) || step.equals(S15_SENSORS);
        if (!onRoof) {
            StoryNpcEntity johnny = post(ffw, JOHNNY, tag(player, "s15_johnny"), ff(FF_JOHNNY), 180f, false);
            if (johnny != null && johnny.isNoGravity()) {
                johnny.setNoGravity(false);
                BlockPos p = ff(FF_JOHNNY);
                johnny.moveTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, 180f, 0f);
            }
        }
        StoryNpcEntity ben = post(ffw, BEN, tag(player, "s15_ben"),
                step.equals(S15_TEAM) ? ff(FF_REED).offset(-3, 0, 2) : ff(FF_BEN), 180f, false);
        if (ben != null && !step.equals(S15_TEAM)) {
            sendTo(ben, ff(FF_BEN));
        }
        switch (step) {
            case S15_TEAM -> {
                target(player, FF_WORLD, ff(FF_REED), 3);
                for (int i = 0; i < FF_STAFF.size(); i++) {
                    post(ffw, LAB_STAFF, tag(player, "s15_staff_" + i), ff(FF_STAFF.get(i)), 90f, false);
                }
                if (near(player, FF_WORLD, ff(FF_REED), 9)) {
                    autoOpen(player, "d15_after", 30);
                }
            }
            case S15_LAB -> {
                johnnyOnRoof(player, ffw, l, false);
                bark(player, "b15_aisle");
                hint(player, "s15_fields", "message.wrmarvel.r.field_hint");
                labStaff(player, ffw);
                int cal = counter(player, "calibration");
                int safe = 0;
                for (int i = 0; i < FF_STAFF.size(); i++) {
                    safe += state(player, "ff_staff_safe_" + i) ? 1 : 0;
                }
                if (player.tickCount % 20 < 10) {
                    bar(player, "message.wrmarvel.r.lab_status", Math.min(100, cal), safe, FF_STAFF.size());
                }
                if (cal >= 100 && safe == FF_STAFF.size()) {
                    progress(player, S15, S15_LAB);
                }
            }
            case S15_SENSORS -> {
                for (int i = 0; i < FF_STAFF.size(); i++) {
                    post(ffw, LAB_STAFF, tag(player, "s15_staff_" + i), ff(spread(SAFE_ROOM, i)), 270f, true);
                }
                bark(player, "b15_sensor");
                BlockPos goal = player.getY() < FF_ORIGIN.getY() + FF_CEILING ? ROOF_LADDER_TOP : ROOF_PLATFORM.above();
                target(player, FF_WORLD, ff(goal), 3);
                if (!player.blockPosition().closerThan(ff(FF_SUE_START), 4) || player.getY() > FF_ORIGIN.getY() + FF_CEILING) {
                    hint(player, "s15_roof", "message.wrmarvel.r.roof_hint");
                }
                johnnyOnRoof(player, ffw, l, true);
            }
            case S15_ASSEMBLY -> {
                for (int i = 0; i < FF_STAFF.size(); i++) {
                    post(ffw, LAB_STAFF, tag(player, "s15_staff_" + i), ff(spread(SAFE_ROOM, i)), 270f, true);
                }
                bark(player, "b15_assembly");
                hint(player, "s15_guard", "message.wrmarvel.r.guard_hint");
                target(player, FF_WORLD, ff(CONNECTION), 2);
                if (ben != null) {
                    ben.setCowering(true);
                }
                if (player.tickCount % 20 < 10) {
                    bar(player, "message.wrmarvel.r.assembly_status", counter(player, "guarded"), 4);
                }
                if (counter(player, "guarded") >= 4) {
                    if (ben != null) {
                        ben.setCowering(false);
                    }
                    progress(player, S15, S15_ASSEMBLY);
                }
            }
            case S15_DONE -> {
                for (int i = 0; i < FF_STAFF.size(); i++) {
                    post(ffw, LAB_STAFF, tag(player, "s15_staff_" + i), ff(spread(SAFE_ROOM, i)), 270f, true);
                }
                if (ben != null) {
                    ben.setCowering(false);
                }
                target(player, FF_WORLD, ff(FF_REED), 3);
                if (near(player, FF_WORLD, ff(FF_REED), 9)) {
                    autoOpen(player, state(player, "channel_back") ? "d15_more_worlds" : "d15_restored", 30);
                }
            }
            default -> {
            }
        }
    }

    /** The lab staff cross the aisle behind Sue: the lead one moves on only when a field covers the next point. */
    private static void labStaff(ServerPlayer player, ServerLevel ffw) {
        boolean leadMoved = false;
        for (int i = 0; i < FF_STAFF.size(); i++) {
            StoryNpcEntity s = npc(ffw, LAB_STAFF, tag(player, "s15_staff_" + i), ff(FF_STAFF.get(i)), 90f);
            if (s == null) {
                continue;
            }
            if (state(player, "ff_staff_safe_" + i)) {
                sendTo(s, ff(spread(SAFE_ROOM, i)));
                s.setCowering(false);
                continue;
            }
            int stage = counter(player, "ff_stage_" + i);
            BlockPos at = stage == 0 ? FF_STAFF.get(i) : AISLE.get(stage - 1);
            if (leadMoved) {
                // Only one at a time under the fields; the rest wait at the benches.
                sendTo(s, ff(stage == 0 ? FF_STAFF.get(i) : at));
                s.setCowering(true);
                continue;
            }
            leadMoved = true;
            s.setCowering(false);
            if (!s.blockPosition().closerThan(ff(at), 1.8)) {
                sendTo(s, ff(at));
                continue;
            }
            if (stage == AISLE.size()) {
                sendTo(s, ff(spread(SAFE_ROOM, i)));
                if (s.blockPosition().closerThan(ff(SAFE_ROOM), 4) || counter(player, "ff_safe_wait_" + i) > 12) {
                    setState(player, "ff_staff_safe_" + i);
                } else {
                    session(player).counters.merge("ff_safe_wait_" + i, 1, Integer::sum);
                }
                continue;
            }
            BlockPos next = AISLE.get(stage);
            if (covered(ffw, ff(next))) {
                setCounter(player, "ff_stage_" + i, stage + 1);
                sendTo(s, ff(next));
            } else if (player.tickCount % 60 == 3) {
                message(player, "message.wrmarvel.r.cover_aisle");
            }
        }
    }

    /** Whether a field hangs over a floor point (any field cell in its column between the floor and the ceiling). */
    private static boolean covered(ServerLevel ffw, BlockPos floor) {
        for (int y = 1; y < FF_CEILING; y++) {
            if (ffw.getBlockState(floor.atY(FF_ORIGIN.getY() + y)).is(MarvelBlocks.FORCE_FIELD.get())) {
                return true;
            }
        }
        return false;
    }

    /** Johnny hovers on the roof; for the second sensor he waits for Sue's platform, then installs it. */
    private static void johnnyOnRoof(ServerPlayer player, ServerLevel ffw, Live l, boolean installing) {
        StoryNpcEntity j = person(l, ffw, JOHNNY, tag(player, "s15_johnny"), ff(ROOF_JOHNNY), 90f);
        if (j == null) {
            return;
        }
        if (!j.isNoGravity()) {
            ffw.sendParticles(ParticleTypes.FLAME, j.getX(), j.getY() + 1, j.getZ(), 20, 0.3, 0.8, 0.3, 0.05);
            j.setNoGravity(true);
        }
        j.stopWalking();
        boolean platform = platformUp(ffw);
        BlockPos spot = installing && platform ? ROOF_PLATFORM.above() : ROOF_JOHNNY;
        Vec3 to = c(ff(spot));
        Vec3 d = to.subtract(j.position());
        Vec3 step = d.length() > 0.4 ? d.normalize().scale(0.4) : d;
        j.moveTo(j.getX() + step.x, j.getY() + step.y, j.getZ() + step.z, 90f, 0f);
        if (player.tickCount % 4 == 0) {
            ffw.sendParticles(ParticleTypes.FLAME, j.getX(), j.getY() + 0.2, j.getZ(), 2, 0.15, 0.1, 0.15, 0.01);
        }
        if (!installing) {
            return;
        }
        if (platform && j.position().distanceTo(to) < 0.6) {
            int t = counter(player, "install") + 1;
            setCounter(player, "install", t);
            if (t == 8) {
                RespondersSites.installSensor(ffw);
                ffw.playSound(null, ff(SENSOR_2), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.6f, 1.6f);
                bark(player, "b15_sensor_done");
                after(player, speakingTicks("b15_sensor_done"), "s15_sensor_done");
            }
        } else {
            if (counter(player, "install") > 0 && counter(player, "install") < 8) {
                message(player, "message.wrmarvel.r.platform_gone");
            }
            if (counter(player, "install") < 8) {
                setCounter(player, "install", 0);
            }
        }
    }

    private static boolean platformUp(ServerLevel ffw) {
        return ffw.getBlockState(ff(ROOF_PLATFORM)).is(MarvelBlocks.FORCE_FIELD.get());
    }

    /** Clears field cells left in the lab from an earlier session (memory of them is gone after a reload). */
    private static void sweepFields(ServerLevel ffw, Live l) {
        l.fields.clear();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int x = -18; x <= 24; x++) {
            for (int y = 0; y <= FF_CEILING + 4; y++) {
                for (int z = -14; z <= 14; z++) {
                    BlockPos p = FF_ORIGIN.offset(x, y, z);
                    if (ffw.getBlockState(p).is(MarvelBlocks.FORCE_FIELD.get())) {
                        ffw.setBlock(p, air, Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    /** Every tick in Scene 15: the falling debris over the lab (15.2), loose debris at the connection (15.4). */
    private static void tickS15(ServerPlayer player, ServerLevel ffw, String step, Live l) {
        boolean lab = step.equals(S15_LAB);
        boolean assembly = step.equals(S15_ASSEMBLY);
        if (lab && player.tickCount % 10 == 0 && covered(ffw, ff(FF_CONSOLE))) {
            // Reed's recalibration moves on only while the console is protected.
            setCounter(player, "calibration", counter(player, "calibration") + 1);
        }
        if (lab && player.tickCount % 30 == 0) {
            // The building shakes.
            ffw.playSound(null, player.blockPosition(), SoundEvents.DEEPSLATE_BREAK, SoundSource.AMBIENT, 0.5f, 0.4f);
            ffw.sendParticles(ParticleTypes.ASH, player.getX(), FF_ORIGIN.getY() + FF_CEILING - 0.5, player.getZ(), 30, 8, 0.2, 8, 0.0);
        }
        if (!lab && !assembly) {
            l.hazardTarget = null;
            return;
        }
        if (l.hazardTarget == null) {
            if (--l.hazardTimer > 0) {
                return;
            }
            if (lab) {
                int pick = ffw.random.nextInt(100);
                BlockPos t;
                if (pick < 40) {
                    t = FF_CONSOLE;
                } else {
                    int lead = 0;
                    for (int i = 0; i < FF_STAFF.size(); i++) {
                        if (!state(player, "ff_staff_safe_" + i)) {
                            lead = Math.min(AISLE.size() - 1, counter(player, "ff_stage_" + i));
                            break;
                        }
                    }
                    t = pick < 85 ? AISLE.get(lead) : AISLE.get(ffw.random.nextInt(AISLE.size()));
                }
                l.hazardTarget = ff(t);
                l.hazardFrom = null;
                l.hazardWarn = 40;
            } else {
                int side = ffw.random.nextInt(3);
                Vec3[] from = {c(ff(new BlockPos(-14, 2, -7))), c(ff(new BlockPos(14, 2, -7))), c(ff(new BlockPos(0, 2, 9)))};
                l.hazardTarget = ff(CONNECTION);
                l.hazardFrom = from[side].add(0, 0.6, 0);
                l.hazardWarn = 30;
            }
            return;
        }
        BlockPos t = l.hazardTarget;
        if (l.hazardFrom == null) {
            // A crack in the ceiling over the target, then the piece falls.
            if (l.hazardWarn % 5 == 0) {
                ffw.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SMOOTH_STONE.defaultBlockState()),
                        t.getX() + 0.5, FF_ORIGIN.getY() + FF_CEILING - 0.1, t.getZ() + 0.5, 8, 0.6, 0, 0.6, 0);
                ffw.playSound(null, t.atY(FF_ORIGIN.getY() + FF_CEILING), SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.BLOCKS, 0.8f, 0.6f);
            }
            if (--l.hazardWarn > 0) {
                return;
            }
            boolean blocked = covered(ffw, t);
            if (blocked) {
                int y = FF_ORIGIN.getY() + 5;
                ffw.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SMOOTH_STONE.defaultBlockState()),
                        t.getX() + 0.5, y + 1, t.getZ() + 0.5, 30, 0.8, 0.2, 0.8, 0.1);
                ffw.playSound(null, t.atY(y), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1.4f, 0.7f);
            } else {
                ffw.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SMOOTH_STONE.defaultBlockState()),
                        t.getX() + 0.5, t.getY() + 0.2, t.getZ() + 0.5, 40, 0.6, 0.2, 0.6, 0.1);
                ffw.playSound(null, t, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.8f, 1.2f);
                if (t.equals(ff(FF_CONSOLE))) {
                    setCounter(player, "calibration", Math.max(0, counter(player, "calibration") - 30));
                    message(player, "message.wrmarvel.r.console_hit");
                } else {
                    // Whoever was under it falls back to the last protected point.
                    for (int i = 0; i < FF_STAFF.size(); i++) {
                        if (state(player, "ff_staff_safe_" + i)) {
                            continue;
                        }
                        int stage = counter(player, "ff_stage_" + i);
                        BlockPos at = stage == 0 ? FF_STAFF.get(i) : AISLE.get(stage - 1);
                        if (stage > 0 && ff(at).closerThan(t, 2.5)) {
                            setCounter(player, "ff_stage_" + i, stage - 1);
                            message(player, "message.wrmarvel.r.staff_back");
                        }
                    }
                }
                if (player.blockPosition().closerThan(t, 1.5)) {
                    player.hurt(ffw.damageSources().fallingBlock(player), 3f);
                }
            }
            l.hazardTarget = null;
            l.hazardTimer = 50 + ffw.random.nextInt(30);
            return;
        }
        // Loose debris flung at the connection: a warning glint, then a streak; a field on its path stops it.
        Vec3 from = l.hazardFrom;
        Vec3 to = c(t).add(0, 0.5, 0);
        if (l.hazardWarn > 12) {
            if (l.hazardWarn % 4 == 0) {
                ffw.sendParticles(ParticleTypes.ELECTRIC_SPARK, from.x, from.y, from.z, 6, 0.3, 0.3, 0.3, 0.05);
                ffw.playSound(null, BlockPos.containing(from), SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 1f, 0.6f);
            }
            l.hazardWarn--;
            return;
        }
        double f0 = (12 - l.hazardWarn) / 12.0, f1 = (13 - l.hazardWarn) / 12.0;
        Vec3 a = from.add(to.subtract(from).scale(f0)), b = from.add(to.subtract(from).scale(Math.min(1, f1)));
        BlockHitResult hit = ffw.clip(new ClipContext(a, b, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        boolean stopped = hit.getType() == HitResult.Type.BLOCK && ffw.getBlockState(hit.getBlockPos()).is(MarvelBlocks.FORCE_FIELD.get());
        beam(ffw, a, stopped ? hit.getLocation() : b, new BlockParticleOption(ParticleTypes.BLOCK, Blocks.IRON_BLOCK.defaultBlockState()));
        if (stopped) {
            ffw.sendParticles(FIELD, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, 30, 0.5, 0.5, 0.5, 0.05);
            ffw.playSound(null, hit.getBlockPos(), SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 1.4f, 0.8f);
            setCounter(player, "guarded", counter(player, "guarded") + 1);
            l.hazardTarget = null;
            l.hazardTimer = 40 + ffw.random.nextInt(25);
            return;
        }
        if (--l.hazardWarn <= 0) {
            ffw.sendParticles(ParticleTypes.ELECTRIC_SPARK, to.x, to.y, to.z, 30, 0.4, 0.4, 0.4, 0.2);
            ffw.playSound(null, t, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.8f, 1.4f);
            setCounter(player, "guarded", Math.max(0, counter(player, "guarded") - 1));
            message(player, "message.wrmarvel.r.connection_hit");
            l.hazardTarget = null;
            l.hazardTimer = 40 + ffw.random.nextInt(25);
        }
    }

    // ---- Sue's fields

    /** Field anchors in the lab: canopies over the aisle and console, the roof platform, the three guard walls. */
    private static List<BlockPos> anchorCells(BlockPos anchor) {
        List<BlockPos> cells = new ArrayList<>();
        int gi = GUARDS.indexOf(anchor);
        if (gi >= 0) {
            for (int a = -1; a <= 1; a++) {
                for (int y = -1; y <= 1; y++) {
                    cells.add(gi < 2 ? ff(anchor.offset(0, y, a)) : ff(anchor.offset(a, y, 0)));
                }
            }
            return cells;
        }
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                cells.add(ff(anchor.offset(x, 0, z)));
            }
        }
        return cells;
    }

    private static void placeField(ServerPlayer player, ServerLevel ffw) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(16));
        BlockHitResult hit = ffw.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 aim = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
        // Snap to the nearest anchor within reach of where she's looking; otherwise a sheet facing her.
        List<BlockPos> anchors = new ArrayList<>(CANOPIES);
        anchors.add(ROOF_PLATFORM);
        anchors.addAll(GUARDS);
        BlockPos best = null;
        double bestD = 4.5;
        for (BlockPos a : anchors) {
            double d = c(ff(a)).add(0, 0.5, 0).distanceTo(aim);
            // A canopy is aimed at by looking at the floor under it as well.
            double dFloor = CANOPIES.contains(a) ? c(ff(a.atY(0))).distanceTo(aim) : Double.MAX_VALUE;
            d = Math.min(d, dFloor);
            if (d < bestD) {
                bestD = d;
                best = a;
            }
        }
        List<BlockPos> cells;
        if (best != null) {
            cells = anchorCells(best);
        } else if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos centre = hit.getBlockPos().relative(hit.getDirection());
            Direction.Axis axis = hit.getDirection().getAxis();
            cells = new ArrayList<>();
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    cells.add(switch (axis) {
                        case X -> centre.offset(0, a, b);
                        case Y -> centre.offset(a, 0, b);
                        default -> centre.offset(a, b, 0);
                    });
                }
            }
        } else {
            message(player, "message.wrmarvel.r.field_range");
            return;
        }
        Live l = live(player);
        List<BlockPos> placed = new ArrayList<>();
        for (BlockPos p : cells) {
            if (ffw.getBlockState(p).isAir() && player.getBoundingBox().intersects(new AABB(p)) == false) {
                ffw.setBlock(p, MarvelBlocks.FORCE_FIELD.get().defaultBlockState(), Block.UPDATE_ALL);
                placed.add(p);
            }
        }
        if (placed.isEmpty()) {
            message(player, "message.wrmarvel.r.field_blocked");
            return;
        }
        l.fields.addLast(placed);
        while (l.fields.size() > 3) {
            release(ffw, l.fields.removeFirst());
        }
        Vec3 mid = c(placed.get(placed.size() / 2));
        beam(ffw, eye.add(0, -0.3, 0), mid, FIELD);
        ffw.sendParticles(FIELD, mid.x, mid.y + 0.5, mid.z, 25, 1, 0.4, 1, 0.02);
        ffw.playSound(null, BlockPos.containing(mid), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2f, 1.4f);
        bar(player, "message.wrmarvel.r.fields_up", l.fields.size());
    }

    private static void release(ServerLevel ffw, List<BlockPos> cells) {
        for (BlockPos p : cells) {
            if (ffw.getBlockState(p).is(MarvelBlocks.FORCE_FIELD.get())) {
                ffw.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                ffw.sendParticles(FIELD, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 2, 0.3, 0.3, 0.3, 0.01);
            }
        }
    }

    private static void releaseAll(ServerPlayer player, ServerLevel ffw) {
        Live l = live(player);
        while (!l.fields.isEmpty()) {
            release(ffw, l.fields.removeFirst());
        }
        ffw.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 1f, 1.6f);
        bar(player, "message.wrmarvel.r.fields_up", 0);
    }

    // ---------------------------------------------------------------- Scene 16, The Other Side

    private static final List<BlockPos> STAFF_OUT = List.of(st(1, 0, 9), st(1, 0, 5), st(4, 0, 5), st(10, 0, 5), st(14, 0, 5));
    /** Index in {@link #STAFF_OUT} the staff wait at while the corridor hazard is down. */
    private static final int HAZARD_WAIT = 1;

    private static String stTag(ServerPlayer player, String who) {
        return tag(player, "s16_" + who);
    }

    private static void setupS16(ServerPlayer player, ServerLevel xw, String step) {
        boolean atStation = state(player, "at_station") && !state(player, "back_at_school");
        if (!atStation) {
            school(player, xw, step);
            return;
        }
        // The team at the station.
        StoryNpcEntity jean = npc(xw, JEAN, stTag(player, "st_jean"), xs(ST_TEAM_WAIT).offset(-2, 0, 0), 0f);
        StoryNpcEntity beast = npc(xw, BEAST, stTag(player, "st_beast"), xs(ST_TEAM_WAIT), 0f);
        StoryNpcEntity kurt = npc(xw, NIGHTCRAWLER, stTag(player, "st_kurt"), xs(ST_TEAM_WAIT).offset(2, 0, 0), 0f);
        mainEntrance(player, xw);
        switch (step) {
            case S16_ACCESS -> {
                checkpoint(player, XMEN_WORLD, xs(ST_ARRIVAL));
                sendTo(jean, xs(ST_TEAM_WAIT).offset(-2, 0, 0));
                sendTo(beast, xs(ST_TEAM_WAIT));
                sendTo(kurt, xs(ST_TEAM_WAIT).offset(2, 0, 0));
                if (!state(player, "s16_arrival_done")) {
                    target(player, XMEN_WORLD, xs(ST_TEAM_WAIT), 3);
                    autoOpen(player, "d16_arrival", 30);
                    return;
                }
                target(player, XMEN_WORLD, xs(ST_SIDE_DOOR).offset(2, 0, 0), 3);
                hint(player, "s16_blast", "message.wrmarvel.r.blast_hint");
                if (RespondersSites.allCleared(xw, ACCESS_DEBRIS)) {
                    progress(player, S16, S16_ACCESS);
                }
            }
            case S16_STAFF -> staffRescue(player, xw, jean, beast, kurt);
            case S16_DATA -> {
                checkpoint(player, XMEN_WORLD, xs(ST_SIDE_DOOR).offset(2, 0, 0));
                stationStaffSafe(player, xw);
                sendTo(jean, xs(ST_JEAN));
                jeanHolds(xw, jean);
                sendTo(kurt, xs(ST_EXTRACT).offset(-2, 0, 0));
                follow(xw, player, beast, xs(ST_EXTRACT));
                target(player, XMEN_WORLD, xs(ST_REC_VIEW), 2);
                if (beast != null && near(player, XMEN_WORLD, xs(ST_REC_VIEW), 4) && beast.distanceTo(player) < 6) {
                    bark(player, "b16_recorder");
                }
            }
            case S16_DONE -> {
                stationStaffSafe(player, xw);
                sendTo(jean, xs(ST_EXTRACT).offset(-2, 0, 2));
                sendTo(beast, xs(ST_EXTRACT).offset(0, 0, 2));
                sendTo(kurt, xs(ST_EXTRACT).offset(2, 0, 2));
                target(player, XMEN_WORLD, xs(ST_EXTRACT), 3);
                if (near(player, XMEN_WORLD, xs(ST_EXTRACT), 7)) {
                    autoOpen(player, "d16_landscape", 20);
                }
            }
            default -> {
            }
        }
    }

    /** Xavier's school: students on the lawn, the briefing (16.1), and the return briefing (16.7). */
    private static void school(ServerPlayer player, ServerLevel xw, String step) {
        for (int i = 0; i < RespondersSites.STUDENTS.size(); i++) {
            StoryNpcEntity s = npc(xw, STUDENTS.get(i % 2), stTag(player, "student_" + i), xs(RespondersSites.STUDENTS.get(i)), i * 90f);
            if (s != null && !s.isAmbient()) {
                s.setAmbient(true, 6);
            }
        }
        post(xw, XAVIER, stTag(player, "xavier"), xs(XS_XAVIER), 180f, false);
        post(xw, MAGNETO, stTag(player, "magneto"), xs(XS_MAGNETO), 135f, false);
        post(xw, BEAST, stTag(player, "beast"), xs(XS_BEAST), 225f, false);
        post(xw, JEAN, stTag(player, "jean"), xs(XS_JEAN), 180f, false);
        post(xw, NIGHTCRAWLER, stTag(player, "kurt"), xs(XS_KURT), 180f, false);
        checkpoint(player, XMEN_WORLD, xs(XS_CYCLOPS_START));
        if (step.equals(S16_BRIEFING)) {
            target(player, XMEN_WORLD, xs(XS_BRIEFING), 3);
            if (near(player, XMEN_WORLD, xs(XS_BRIEFING), 7)) {
                autoOpen(player, "d16_briefing", 20);
            }
        } else if (step.equals(S16_DONE) && state(player, "back_at_school")) {
            target(player, XMEN_WORLD, xs(XS_BRIEFING), 3);
            if (near(player, XMEN_WORLD, xs(XS_BRIEFING), 8)) {
                autoOpen(player, "d16_school", 20);
            }
        } else if (!state(player, "at_station") && once(player, "s16_go")) {
            // Briefing over before a reload: off to the station again.
            after(player, 20, "cin_s16_station");
        }
    }

    /** The main entrance's upper section is giving way: anyone going in that way is pushed back out. */
    private static void mainEntrance(ServerPlayer player, ServerLevel xw) {
        BlockPos rel = player.blockPosition().subtract(XS_ORIGIN.offset(ST));
        if (Math.abs(rel.getX()) <= 3 && rel.getZ() >= -1 && rel.getZ() <= 2 && rel.getY() >= 0 && rel.getY() <= 3) {
            player.setDeltaMovement(0, 0.25, -0.9);
            player.hurtMarked = true;
            BlockPos d = xs(ST_MAIN_DOOR);
            xw.sendParticles(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.STONE_BRICKS.defaultBlockState()),
                    d.getX() + 0.5, d.getY() + 4, d.getZ() + 0.5, 30, 2, 0.2, 1, 0);
            xw.playSound(null, d, SoundEvents.DEEPSLATE_BREAK, SoundSource.BLOCKS, 1f, 0.5f);
            if (player.tickCount % 20 < 10) {
                message(player, "message.wrmarvel.r.unstable_entrance");
            }
        }
    }

    private static void jeanHolds(ServerLevel xw, @Nullable StoryNpcEntity jean) {
        if (jean != null && xw.random.nextInt(2) == 0) {
            BlockPos h = xs(ST_JEAN);
            xw.sendParticles(ParticleTypes.ENCHANT, h.getX() + 0.5, h.getY() + 3.5, h.getZ() + 0.5, 12, 2.5, 0.3, 2.5, 0.2);
        }
    }

    /** 16.4: Kurt brings each cut-off pair down to a cleared spot; Cyclops keeps the corridor open; everyone out. */
    private static void staffRescue(ServerPlayer player, ServerLevel xw, @Nullable StoryNpcEntity jean,
                                    @Nullable StoryNpcEntity beast, @Nullable StoryNpcEntity kurt) {
        checkpoint(player, XMEN_WORLD, xs(ST_SIDE_DOOR).offset(2, 0, 0));
        bark(player, "b16_extract");
        sendTo(jean, xs(ST_JEAN));
        jeanHolds(xw, jean);
        sendTo(beast, xs(ST_SIDE_DOOR).offset(4, 0, -2));
        boolean hazard = xw.getBlockState(xs(RespondersSites.CORRIDOR_HAZARD)).is(MarvelBlocks.DEBRIS.get());
        int safe = 0;
        BlockPos nextSpot = null;
        for (int g = 0; g < 3; g++) {
            int spot = g == 1 ? 1 : 0;
            boolean out = state(player, "g" + g + "_out");
            for (int k = 0; k < 2; k++) {
                int i = g * 2 + k;
                String who = i == 0 ? STATION_SCIENTIST : STATION_STAFF;
                if (state(player, "st_safe_" + i)) {
                    safe++;
                    StoryNpcEntity s = npc(xw, who, stTag(player, "staff_" + i), xs(spread(ST_EXTRACT, i)), 0f);
                    sendTo(s, xs(spread(ST_EXTRACT, i)));
                    continue;
                }
                StoryNpcEntity s = npc(xw, who, stTag(player, "staff_" + i), xs(ST_STAFF.get(i)), 180f);
                if (s == null) {
                    continue;
                }
                if (!out) {
                    s.setCowering(true);
                    if (!s.blockPosition().closerThan(xs(ST_STAFF.get(i)), 2)) {
                        BlockPos h = xs(ST_STAFF.get(i));
                        s.moveTo(h.getX() + 0.5, h.getY(), h.getZ() + 0.5, 180f, 0f);
                    }
                    continue;
                }
                s.setCowering(false);
                List<BlockPos> route = new ArrayList<>();
                route.add(TELEPORT_SPOTS.get(spot).offset(k, 0, 0));
                route.addAll(hazard ? STAFF_OUT.subList(0, HAZARD_WAIT + 1) : STAFF_OUT);
                if (!hazard) {
                    route.add(spread(ST_EXTRACT, i));
                }
                List<BlockPos> abs = new ArrayList<>();
                route.forEach(p -> abs.add(xs(p)));
                String key = (hazard ? "st_wait_" : "st_out_") + i;
                if (walkRoute(player, s, key, abs) && !hazard) {
                    setState(player, "st_safe_" + i);
                }
                // The first pair in the corridor: debris drops across it ahead of them.
                if (!state(player, "hazard_dropped") && routeStage(player, key) >= HAZARD_WAIT + 1) {
                    setState(player, "hazard_dropped");
                    RespondersSites.dropCorridorHazard(xw);
                    BlockPos hz = xs(RespondersSites.CORRIDOR_HAZARD);
                    xw.playSound(null, hz, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1f, 0.7f);
                    xw.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, hz.getX() + 0.5, hz.getY() + 1, hz.getZ() + 0.5, 15, 0.5, 0.8, 1, 0.01);
                    message(player, "message.wrmarvel.r.corridor_blocked");
                }
            }
            if (!out && nextSpot == null) {
                // This pair is next: their arrival spot must be clear (and empty if another pair used it first).
                boolean clear = RespondersSites.allCleared(xw, TELEPORT_DEBRIS.subList(spot * 2, spot * 2 + 2));
                boolean empty = g != 2 || (state(player, "st_safe_0") || routeStage(player, "st_out_0") >= 2)
                        && (state(player, "st_safe_1") || routeStage(player, "st_out_1") >= 2);
                nextSpot = TELEPORT_SPOTS.get(spot);
                if (clear && empty && once(player, "bamf_" + g)) {
                    after(player, 30, "bamf_" + g);
                } else if (!clear && player.tickCount % 60 == 7) {
                    message(player, "message.wrmarvel.r.clear_spot");
                }
            }
        }
        sendTo(kurt, xs(ST_KURT));
        if (nextSpot != null) {
            target(player, XMEN_WORLD, xs(nextSpot), 2);
        } else if (hazard) {
            target(player, XMEN_WORLD, xs(RespondersSites.CORRIDOR_HAZARD), 2);
        } else {
            target(player, XMEN_WORLD, xs(ST_EXTRACT), 3);
        }
        if (safe == ST_STAFF.size()) {
            bark(player, "b16_lower_recorder");
            after(player, speakingTicks("b16_lower_recorder"), "s16_staff_done");
        }
    }

    private static void stationStaffSafe(ServerPlayer player, ServerLevel xw) {
        for (int i = 0; i < ST_STAFF.size(); i++) {
            String who = i == 0 ? STATION_SCIENTIST : STATION_STAFF;
            post(xw, who, stTag(player, "staff_" + i), xs(spread(ST_EXTRACT, i)), 0f, true);
        }
    }

    /** Kurt teleports a pair from the upper floor to their cleared spot. */
    private static void bamf(ServerPlayer player, int g) {
        if (!(player.level() instanceof ServerLevel xw) || !on(player, S16, S16_STAFF)) {
            return;
        }
        int spot = g == 1 ? 1 : 0;
        for (int k = 0; k < 2; k++) {
            int i = g * 2 + k;
            String who = i == 0 ? STATION_SCIENTIST : STATION_STAFF;
            StoryNpcEntity s = npc(xw, who, stTag(player, "staff_" + i), xs(ST_STAFF.get(i)), 180f);
            if (s == null) {
                after(player, 10, "bamf_" + g);
                return;
            }
            xw.sendParticles(ParticleTypes.LARGE_SMOKE, s.getX(), s.getY() + 1, s.getZ(), 25, 0.4, 0.8, 0.4, 0.02);
            BlockPos to = xs(TELEPORT_SPOTS.get(spot).offset(k, 0, 0));
            s.stopWalking();
            s.moveTo(to.getX() + 0.5, to.getY(), to.getZ() + 0.5, 90f, 0f);
            xw.sendParticles(ParticleTypes.LARGE_SMOKE, s.getX(), s.getY() + 1, s.getZ(), 25, 0.4, 0.8, 0.4, 0.02);
            session(player).counters.remove("st_out_" + i);
            session(player).counters.remove("st_wait_" + i);
        }
        xw.playSound(null, xs(TELEPORT_SPOTS.get(spot)), SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1f, 0.8f);
        setState(player, "g" + g + "_out");
        if (g == 0) {
            bark(player, "b16_extract");
        }
    }

    /** Every few ticks in Scene 16: what Cyclops is aiming at, so he never has to fire blind. */
    private static void tickS16(ServerPlayer player, ServerLevel xw, String step, Live l) {
        boolean aiming = state(player, "at_station") && (step.equals(S16_ACCESS) && state(player, "s16_arrival_done") || step.equals(S16_STAFF));
        if (!aiming || player.tickCount % 4 != 0) {
            return;
        }
        String aim = aimAt(player, xw);
        if (aim.equals("support")) {
            bark(player, "b16_beam");
        }
        if (!aim.isEmpty() || !l.lastAim.isEmpty()) {
            bar(player, aim.isEmpty() ? "message.wrmarvel.r.aim.none" : "message.wrmarvel.r.aim." + aim);
        }
        l.lastAim = aim;
    }

    /** "debris", "support", "person" or "" for what a blast fired now would hit first. */
    private static String aimAt(ServerPlayer player, ServerLevel xw) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(32));
        BlockHitResult hit = xw.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 stop = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
        EntityHitResult who = ProjectileUtil.getEntityHitResult(player, eye, stop, new AABB(eye, stop).inflate(1),
                e -> e instanceof StoryNpcEntity || e instanceof ServerPlayer, eye.distanceToSqr(stop));
        if (who != null) {
            return "person";
        }
        if (hit.getType() != HitResult.Type.BLOCK) {
            return "";
        }
        BlockState s = xw.getBlockState(hit.getBlockPos());
        if (s.is(MarvelBlocks.DEBRIS.get())) {
            return "debris";
        }
        if (s.is(SUPPORT.getBlock())) {
            return "support";
        }
        return "";
    }

    private static void opticBlast(ServerPlayer player, ServerLevel xw) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(32));
        String aim = aimAt(player, xw);
        if (aim.equals("person")) {
            message(player, "message.wrmarvel.r.aim.person");
            return;
        }
        BlockHitResult hit = xw.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 stop = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
        beam(xw, eye.add(0, -0.1, 0), stop, OPTIC);
        xw.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.8f);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        if (aim.equals("debris")) {
            // Controlled: just the damaged piece and what is wedged against it.
            List<BlockPos> cluster = new ArrayList<>();
            ArrayDeque<BlockPos> open = new ArrayDeque<>(List.of(pos));
            while (!open.isEmpty() && cluster.size() < 6) {
                BlockPos p = open.removeFirst();
                if (cluster.contains(p) || !xw.getBlockState(p).is(MarvelBlocks.DEBRIS.get()) || !p.closerThan(pos, 1.8)) {
                    continue;
                }
                cluster.add(p);
                for (Direction d : Direction.values()) {
                    open.addLast(p.relative(d));
                }
            }
            for (BlockPos p : cluster) {
                xw.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                xw.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBBLED_DEEPSLATE.defaultBlockState()),
                        p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.1);
            }
            xw.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.7f, 1.4f);
            Doomsday.setup(player);
        } else if (aim.equals("support") && state(player, "at_station")) {
            // A recoverable setback: the prop holds, but part of the corridor comes down again; Jean steadies it.
            xw.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, SUPPORT), stop.x, stop.y, stop.z, 20, 0.2, 0.4, 0.2, 0.1);
            xw.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.2f, 0.6f);
            RespondersSites.corridorSetback(xw);
            message(player, "message.wrmarvel.r.setback");
        } else {
            xw.sendParticles(ParticleTypes.SMOKE, stop.x, stop.y, stop.z, 6, 0.1, 0.1, 0.1, 0.01);
        }
    }

    // ================================================================== timed beats

    static void fire(ServerPlayer player, String beat) {
        if (beat.startsWith("title:")) {
            title(player, beat.substring(6));
            return;
        }
        if (beat.startsWith("bamf_")) {
            bamf(player, Integer.parseInt(beat.substring(5)));
            return;
        }
        switch (beat) {
            case "s12_distress" -> {
                if (on(player, S12, S12_PASSENGERS)) {
                    bark(player, "b12_distress");
                    message(player, "message.wrmarvel.r.switch_to_rescue");
                }
            }
            case "s13_seal" -> {
                if (on(player, S13, S13_STAFF) && player.level() instanceof ServerLevel ny) {
                    RespondersSites.openSeal(ny);
                    setState(player, "seal_open");
                    BlockPos d = n(SEALED_DOOR);
                    ny.playSound(null, d, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1f, 0.7f);
                    ny.sendParticles(ParticleTypes.REVERSE_PORTAL, d.getX() + 0.5, d.getY() + 1, d.getZ() + 0.5, 30, 1, 1, 0.3, 0.05);
                    message(player, "message.wrmarvel.r.seal_open");
                }
            }
            case "s13_cable_set" -> {
                if (on(player, S13, S13_STAFF) && player.level() instanceof ServerLevel ny) {
                    RespondersSites.setCable(ny);
                    ny.playSound(null, n(CROSSING_SOUTH), SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1f, 1f);
                    bark(player, "b13_cable");
                }
            }
            case "s15_sensor_done" -> {
                if (on(player, S15, S15_SENSORS)) {
                    progress(player, S15, S15_SENSORS);
                }
            }
            case "s16_staff_done" -> {
                if (on(player, S16, S16_STAFF)) {
                    progress(player, S16, S16_STAFF);
                }
            }
            case "cin_s16_station" -> {
                if (on(player, S16, S16_ACCESS) && !state(player, "at_station")) {
                    if (idle(player)) {
                        StoryService.playCinematic(player, CIN_S16_STATION, CIN_S16_TRAVEL_TICKS);
                    } else {
                        after(player, 10, beat);
                    }
                }
            }
            case "cin_s16_return" -> {
                if (on(player, S16, S16_DONE) && !state(player, "back_at_school")) {
                    if (idle(player)) {
                        StoryService.playCinematic(player, CIN_S16_RETURN, CIN_S16_TRAVEL_TICKS);
                    } else {
                        after(player, 10, beat);
                    }
                }
            }
            case "s14_transmit_1", "s14_transmit_2", "s14_transmit_3" -> {
                int k = beat.charAt(beat.length() - 1) - '0';
                bar(player, "message.wrmarvel.r.transmitting", k * 25);
                player.level().playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.PLAYERS, 0.6f, 1.2f + k * 0.15f);
                after(player, 25, k < 3 ? "s14_transmit_" + (k + 1) : "s14_received");
            }
            case "s14_received" -> {
                if (on(player, S14, S14_DONE) && setState(player, "packet_sent")) {
                    message(player, "message.wrmarvel.r.received");
                    player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1f, 1.2f);
                    autoOpen(player, "d14_same_boundary", 30);
                }
            }
            case "s14_lost" -> {
                if (on(player, S14, S14_DONE)) {
                    StoryService.infoPanel(player, "scanner", "panel.wrmarvel.r.contact.title", List.of(
                            "panel.wrmarvel.r.contact.lost", "panel.wrmarvel.r.contact.saved", "panel.wrmarvel.r.contact.reed"));
                    setState(player, "reed_contact_established");
                    progress(player, S14, S14_DONE);
                }
            }
            default -> {
            }
        }
    }

    /** Whether an automatic conversation of Scenes 12-16 still belongs to the player's current step. */
    static boolean stillWanted(ServerPlayer player, String group) {
        return switch (group) {
            case "d12_reports" -> on(player, S12, S12_RESPONSE) && !state(player, "s12_briefed");
            case "d12_landed" -> on(player, S12, S12_DONE) && !state(player, "s12_accounted");
            case "d12_population" -> on(player, S12, S12_DONE) && state(player, "s12_accounted");
            case "d13_distress" -> on(player, S13, S13_ENTRY);
            case "d13_clint_kate" -> on(player, S13, S13_FIRST_GROUP) && !state(player, "ck_met");
            case "d13_recorder" -> on(player, S13, S13_RECORDER) && !state(player, "recorder_talk");
            case "d13_intervals" -> on(player, S13, S13_DONE);
            case "d14_briefing" -> on(player, S14, S14_PLAN);
            case "d14_window" -> on(player, S14, S14_ARRAY) && !state(player, "window_told");
            case "d14_contact" -> on(player, S14, S14_DONE) && !state(player, "contact_open");
            case "d14_same_boundary" -> on(player, S14, S14_DONE) && state(player, "packet_sent") && !state(player, "boundary_agreed");
            case "d15_after" -> on(player, S15, S15_TEAM);
            case "d15_restored" -> on(player, S15, S15_DONE) && !state(player, "channel_back");
            case "d15_more_worlds" -> on(player, S15, S15_DONE) && state(player, "channel_back");
            case "d16_briefing" -> on(player, S16, S16_BRIEFING);
            case "d16_arrival" -> on(player, S16, S16_ACCESS) && !state(player, "s16_arrival_done");
            case "d16_landscape" -> on(player, S16, S16_DONE) && !state(player, "landscape_seen");
            case "d16_school" -> on(player, S16, S16_DONE) && state(player, "back_at_school");
            default -> false;
        };
    }

    // ================================================================== events (routed from Doomsday)

    static void tick(ServerPlayer player) {
        Current c = current(player);
        if (c == null || !SCENES_12_16.contains(c.quest()) || currentHero(player) != heroFor(c.quest())) {
            return;
        }
        Live l = live(player);
        Vec3 pos = player.position();
        l.velocity = l.lastPos == null ? Vec3.ZERO : pos.subtract(l.lastPos);
        l.lastPos = pos;
        if (c.quest().equals(S12) && in(player, NEW_YORK)) {
            tickS12(player, player.serverLevel(), c.step(), l);
        } else if (c.quest().equals(S15) && in(player, FF_WORLD)) {
            tickS15(player, player.serverLevel(), c.step(), l);
        } else if (c.quest().equals(S16) && in(player, XMEN_WORLD)) {
            tickS16(player, player.serverLevel(), c.step(), l);
        }
    }

    static void onRespawn(ServerPlayer player) {
        Live l = LIVE.get(player.getUUID());
        if (l != null) {
            // Whoever Sam was carrying goes back to the helicopter door, not into the ground.
            l.carried = -1;
            l.approach = 0;
        }
    }

    static boolean noFallDamage(ServerPlayer player) {
        return currentHero(player) == Hero.SAM_WILSON;
    }

    static void onDialogueFinished(ServerPlayer player, String group, @Nullable String choice) {
        switch (group) {
            case "d12_reports" -> {
                if (on(player, S12, S12_RESPONSE)) {
                    setState(player, "s12_briefed");
                }
            }
            case "d12_landed" -> {
                if (on(player, S12, S12_DONE) && setState(player, "s12_accounted")) {
                    autoOpen(player, "d12_population", 30);
                }
            }
            case "d12_population" -> {
                if (on(player, S12, S12_DONE)) {
                    StoryService.toast(player, "info", "toast.wrmarvel.r.findings.title", "toast.wrmarvel.r.findings.detail", "");
                    progress(player, S12, S12_DONE);
                }
            }
            case "d13_distress" -> progress(player, S13, S13_ENTRY);
            case "d13_clint_kate" -> {
                if (on(player, S13, S13_FIRST_GROUP) && setState(player, "ck_met")) {
                    message(player, "message.wrmarvel.r.follow_cable");
                }
            }
            case "d13_recorder" -> {
                if (on(player, S13, S13_RECORDER) && setState(player, "recorder_talk")) {
                    message(player, "message.wrmarvel.r.recorder_route");
                }
            }
            case "d13_intervals" -> progress(player, S13, S13_DONE);
            case "d14_briefing" -> {
                if (on(player, S14, S14_PLAN)) {
                    setState(player, "priority_" + (choice == null ? "a" : choice));
                    StoryService.infoPanel(player, "scanner", "panel.wrmarvel.r.plan.title", List.of("panel.wrmarvel.r.plan.civilians",
                            "panel.wrmarvel.r.plan.readings", "panel.wrmarvel.r.plan.contact", "panel.wrmarvel.r.plan.no_assumptions"));
                    progress(player, S14, S14_PLAN);
                }
            }
            case "d14_window" -> {
                if (on(player, S14, S14_ARRAY) && setState(player, "window_told")) {
                    message(player, "message.wrmarvel.r.array_hint");
                }
            }
            case "d14_contact" -> {
                if (on(player, S14, S14_DONE) && setState(player, "contact_open")) {
                    message(player, "message.wrmarvel.r.send_packet");
                }
            }
            case "d14_same_boundary" -> {
                if (on(player, S14, S14_DONE) && setState(player, "boundary_agreed")) {
                    // The connection deteriorates.
                    player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1f, 0.6f);
                    bark(player, "b14_deteriorates");
                    after(player, speakingTicks("b14_deteriorates") + 10, "s14_lost");
                }
            }
            case "d15_after" -> {
                if (on(player, S15, S15_TEAM)) {
                    progress(player, S15, S15_TEAM);
                }
            }
            case "d15_restored" -> {
                if (on(player, S15, S15_DONE) && setState(player, "channel_back")) {
                    StoryService.toast(player, "info", "toast.wrmarvel.r.channel.title", "toast.wrmarvel.r.channel.detail", "");
                    message(player, "message.wrmarvel.r.not_the_incursion");
                    autoOpen(player, "d15_more_worlds", 40);
                }
            }
            case "d15_more_worlds" -> {
                if (on(player, S15, S15_DONE)) {
                    progress(player, S15, S15_DONE);
                }
            }
            case "d16_briefing" -> {
                if (on(player, S16, S16_BRIEFING)) {
                    progress(player, S16, S16_BRIEFING);
                    after(player, 20, "cin_s16_station");
                    session(player).started.add("s16_go");
                }
            }
            case "d16_arrival" -> {
                if (on(player, S16, S16_ACCESS)) {
                    setState(player, "s16_arrival_done");
                }
            }
            case "d16_landscape" -> {
                if (on(player, S16, S16_DONE) && setState(player, "landscape_seen")) {
                    after(player, 20, "cin_s16_return");
                }
            }
            case "d16_school" -> {
                if (on(player, S16, S16_DONE)) {
                    progress(player, S16, S16_DONE);
                }
            }
            default -> {
            }
        }
    }

    static void onCinematicFinished(ServerPlayer player, String id) {
        String quest = switch (id) {
            case CIN_S12_TITLE -> S12;
            case CIN_S13_TITLE -> S13;
            case CIN_S14_TITLE -> S14;
            case CIN_S15_TITLE -> S15;
            case CIN_S16_TITLE -> S16;
            default -> null;
        };
        if (quest != null && Profiles.get(player).isQuestActive(quest) && currentHero(player) != heroFor(quest)) {
            arrive(player, quest);
            return;
        }
        if (CIN_S16_STATION.equals(id) && on(player, S16, S16_ACCESS) && player.server.getLevel(XMEN_WORLD) instanceof ServerLevel xw) {
            // The team leaves the school for the station.
            for (String who : List.of("jean", "beast", "kurt")) {
                removeAll(xw, stTag(player, who), xs(XS_BRIEFING), 80);
            }
            setState(player, "at_station");
            BlockPos a = xs(ST_ARRIVAL);
            player.teleportTo(xw, a.getX() + 0.5, a.getY(), a.getZ() + 0.5, 0f, 0f);
            checkpoint(player, XMEN_WORLD, a);
        } else if (CIN_S16_RETURN.equals(id) && on(player, S16, S16_DONE) && player.server.getLevel(XMEN_WORLD) instanceof ServerLevel xw) {
            for (String who : List.of("st_jean", "st_beast", "st_kurt")) {
                removeAll(xw, stTag(player, who), xs(ST_EXTRACT), 80);
            }
            setState(player, "back_at_school");
            BlockPos a = xs(XS_BRIEFING).offset(0, 0, 3);
            player.teleportTo(xw, a.getX() + 0.5, a.getY(), a.getZ() + 0.5, 180f, 0f);
            checkpoint(player, XMEN_WORLD, a);
        }
    }

    static void onNpcInteract(ServerPlayer player, Current c, String who, StoryNpcEntity npc) {
        if (c.quest().equals(S12) && c.step().equals(S12_PASSENGERS)) {
            Live l = live(player);
            for (int i = 0; i < PASSENGERS.size(); i++) {
                if (npc.getStoryTag().equals(tag(player, "s12_passenger_" + i)) && !state(player, "passenger_safe_" + i)) {
                    if (l.carried >= 0) {
                        message(player, "message.wrmarvel.r.one_at_a_time");
                    } else if (l.approach < 20) {
                        message(player, "message.wrmarvel.r.approach_hint");
                    } else {
                        l.carried = i;
                        bark(player, "b12_approach");
                        message(player, "message.wrmarvel.r.carrying");
                        player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 1f, 1f);
                    }
                }
            }
        } else if (c.quest().equals(S13) && (SCIENTIST.equals(who)) && c.step().equals(S13_RECORDER)) {
            if (!state(player, "recorder_talk")) {
                open(player, "d13_recorder");
            } else {
                handInRecorder(player);
            }
        } else if (c.quest().equals(S13) && (YELENA.equals(who)) && c.step().equals(S13_ENTRY)) {
            open(player, "d13_distress");
        } else if (c.quest().equals(S13) && (CLINT.equals(who) || KATE.equals(who)) && c.step().equals(S13_FIRST_GROUP) && !state(player, "ck_met")) {
            open(player, "d13_clint_kate");
        } else if (c.quest().equals(S15) && REED.equals(who)) {
            switch (c.step()) {
                case S15_TEAM -> open(player, "d15_after");
                case S15_DONE -> open(player, state(player, "channel_back") ? "d15_more_worlds" : "d15_restored");
                default -> message(player, "message.wrmarvel.r.reed_busy");
            }
        } else if (c.quest().equals(S16) && (BEAST.equals(who) || XAVIER.equals(who)) && c.step().equals(S16_BRIEFING)) {
            open(player, "d16_briefing");
        }
    }

    /** Debris used by hand (right-click). True if Scenes 12-16 handled it. */
    static boolean onDebrisUsed(ServerPlayer player, BlockPos pos) {
        Current c = current(player);
        if (c == null || !SCENES_12_16.contains(c.quest())) {
            return false;
        }
        if (c.quest().equals(S13) && currentHero(player) == Hero.BUCKY_BARNES) {
            lift(player, pos);
        } else if (c.quest().equals(S16)) {
            message(player, "message.wrmarvel.r.blast_hint");
        } else {
            message(player, "message.wrmarvel.not_now");
        }
        return true;
    }

    /** Bucky's arm: the heavy obstruction or the rubble across the exit comes out in one go. */
    private static void lift(ServerPlayer player, BlockPos pos) {
        ServerLevel ny = player.serverLevel();
        BlockPos rel = pos.subtract(MarvelSites.NY_ORIGIN.offset(FAC));
        boolean obstruction = rel.getZ() == 15 && rel.getX() >= -13 && rel.getX() <= -11;
        boolean rubble = rel.getZ() == 0 && Math.abs(rel.getX()) <= 1;
        if (obstruction && on(player, S13, S13_FIRST_GROUP) && state(player, "ck_met")) {
            RespondersSites.liftObstruction(ny);
        } else if (rubble && on(player, S13, S13_STAFF)) {
            for (int x = -1; x <= 1; x++) {
                for (int y = 0; y <= 1; y++) {
                    ny.setBlock(n(fac(x, y, 0)), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            message(player, "message.wrmarvel.r.exit_clear");
        } else {
            message(player, "message.wrmarvel.not_now");
            return;
        }
        ny.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.8f, 0.6f);
        ny.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1.2f, 0.6f);
        ny.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBBLED_DEEPSLATE.defaultBlockState()),
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 1, 0.6, 0.4, 0.1);
        Doomsday.setup(player);
    }

    /** The scene controls: route table, door brace, recorders, the array, the transmitter, clamps. */
    static void onControlUsed(ServerPlayer player, BlockPos pos, Kind kind) {
        Current c = current(player);
        if (c == null || !SCENES_12_16.contains(c.quest()) || currentHero(player) != heroFor(c.quest())) {
            message(player, "message.wrmarvel.not_now");
            return;
        }
        ServerLevel level = player.serverLevel();
        switch (kind) {
            case ROUTE_TABLE -> {
                if (c.quest().equals(S12)) {
                    StoryService.infoPanel(player, "map", "panel.wrmarvel.r.route.title", List.of("panel.wrmarvel.r.route.one",
                            "panel.wrmarvel.r.route.two", "panel.wrmarvel.r.route.three", "panel.wrmarvel.r.route.joaquin"));
                    if (on(player, S12, S12_RESPONSE) && state(player, "s12_briefed") && setState(player, "route_reviewed")) {
                        message(player, "message.wrmarvel.r.launch");
                    }
                }
            }
            case BRACE -> {
                if (!on(player, S13, S13_FIRST_GROUP) || !RespondersSites.obstructionCleared(level)) {
                    message(player, "message.wrmarvel.r.brace_first");
                } else if (!RespondersSites.doorBraced(level)) {
                    RespondersSites.braceDoor(level);
                    level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1f, 0.8f);
                    message(player, "message.wrmarvel.r.door_braced");
                }
            }
            case RECORDER -> {
                if (c.quest().equals(S13)) {
                    if (!on(player, S13, S13_RECORDER) || !state(player, "recorder_talk")) {
                        message(player, "message.wrmarvel.r.recorder_running");
                    } else if (!has(player, MarvelItems.MONITORING_RECORDER.get()) && setState(player, "recorder_taken")) {
                        giveItem(player, new ItemStack(MarvelItems.MONITORING_RECORDER.get()));
                        level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1f, 1.2f);
                        message(player, "message.wrmarvel.r.recorder_taken");
                    }
                } else if (c.quest().equals(S16)) {
                    if (!on(player, S16, S16_DATA)) {
                        message(player, "message.wrmarvel.r.recorder_running");
                    } else if (!state(player, "rec_power_off")) {
                        message(player, "message.wrmarvel.r.rec.power_first");
                    } else if (!state(player, "rec_clamps")) {
                        message(player, "message.wrmarvel.r.rec.clamps_first");
                    } else {
                        level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1f, 1.2f);
                        message(player, "message.wrmarvel.r.rec.removed");
                        progress(player, S16, S16_DATA);
                    }
                }
            }
            case POWER -> {
                if (c.quest().equals(S14)) {
                    if (!on(player, S14, S14_ARRAY) || !state(player, "window_told")) {
                        message(player, "message.wrmarvel.not_now");
                    } else if (setState(player, "array_powered")) {
                        RespondersSites.setArrayLamps(level, true);
                        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1f, 1f);
                        message(player, "message.wrmarvel.r.array_powered");
                    } else {
                        message(player, "message.wrmarvel.r.array_powered");
                    }
                } else if (c.quest().equals(S16)) {
                    if (!on(player, S16, S16_DATA)) {
                        message(player, "message.wrmarvel.not_now");
                    } else if (setState(player, "rec_power_off")) {
                        level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1f, 0.7f);
                        message(player, "message.wrmarvel.r.rec.power_off");
                    }
                }
            }
            case CLAMP -> {
                if (!on(player, S16, S16_DATA)) {
                    message(player, "message.wrmarvel.not_now");
                } else if (!state(player, "rec_power_off")) {
                    message(player, "message.wrmarvel.r.rec.power_first");
                } else if (setState(player, "rec_clamps")) {
                    level.playSound(null, pos, SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 1f, 1f);
                    message(player, "message.wrmarvel.r.rec.clamps_off");
                }
            }
            case DISH -> {
                if (!on(player, S14, S14_ARRAY)) {
                    message(player, "message.wrmarvel.not_now");
                } else if (!state(player, "array_powered")) {
                    message(player, "message.wrmarvel.r.no_power");
                } else if (state(player, "dish_aligned")) {
                    message(player, "message.wrmarvel.r.aligned");
                } else {
                    int aim = Math.floorMod(counter(player, "dish") + (player.isShiftKeyDown() ? -1 : 1), 12);
                    setCounter(player, "dish", aim);
                    int off = Math.abs(aim - dishTarget(player));
                    off = Math.min(off, 12 - off);
                    int strength = Math.max(0, 100 - off * 25);
                    level.playSound(null, pos, SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.BLOCKS, 0.8f, 0.6f + strength / 100f);
                    if (off == 0 && setState(player, "dish_aligned")) {
                        message(player, "message.wrmarvel.r.aligned");
                    } else {
                        bar(player, "message.wrmarvel.r.alignment", strength, aim * 30);
                    }
                }
            }
            case TEST -> {
                if (!on(player, S14, S14_ARRAY)) {
                    message(player, "message.wrmarvel.not_now");
                } else if (!state(player, "array_powered")) {
                    message(player, "message.wrmarvel.r.no_power");
                } else if (!state(player, "dish_aligned")) {
                    message(player, "message.wrmarvel.r.not_aligned");
                } else {
                    level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1f, 1.4f);
                    message(player, "message.wrmarvel.r.channel_clear");
                    progress(player, S14, S14_ARRAY);
                }
            }
            case TRANSMIT -> {
                if (!on(player, S14, S14_DONE) || !state(player, "contact_open")) {
                    message(player, on(player, S14, S14_DONE) ? "message.wrmarvel.r.static" : "message.wrmarvel.not_now");
                } else if (!state(player, "packet_sent") && once(player, "s14_transmit")) {
                    after(player, 10, "s14_transmit_1");
                }
            }
            default -> {
            }
        }
        Doomsday.setup(player);
    }

    @SubscribeEvent
    public static void onAbility(AbilityUsedEvent event) {
        ServerPlayer player = event.getPlayer();
        String id = event.getAbility().id();
        Current c = current(player);
        if (c == null || !SCENES_12_16.contains(c.quest())) {
            return;
        }
        ServerLevel level = player.serverLevel();
        switch (id) {
            case SAM_FLIGHT -> {
                var a = player.getAbilities();
                if (!c.quest().equals(S12)) {
                    message(player, "message.wrmarvel.r.no_flight_here");
                } else if (c.step().equals(S12_RESPONSE) && !state(player, "launched")) {
                    if (!state(player, "route_reviewed")) {
                        message(player, "message.wrmarvel.r.review_route");
                    } else if (!near(player, NEW_YORK, n(LAUNCH_PAD), 7)) {
                        message(player, "message.wrmarvel.r.launch");
                    } else {
                        setState(player, "launched");
                        launch(player, level);
                        removeAll(level, tag(player, "s12_shang_chi"), n(BASE_SAM_START), 60);
                        removeAll(level, tag(player, "s12_scott"), n(BASE_SAM_START), 60);
                        progress(player, S12, S12_RESPONSE);
                    }
                } else if (a.flying) {
                    a.flying = false;
                    player.onUpdateAbilities();
                } else {
                    launch(player, level);
                }
            }
            case BUCKY_LIFT -> {
                Vec3 eye = player.getEyePosition();
                BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(5)),
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                if (hit.getType() == HitResult.Type.BLOCK && level.getBlockState(hit.getBlockPos()).is(MarvelBlocks.DEBRIS.get())) {
                    lift(player, hit.getBlockPos());
                } else {
                    message(player, "message.wrmarvel.r.lift_nothing");
                }
            }
            case SUE_FIELD -> {
                if (in(player, FF_WORLD)) {
                    placeField(player, level);
                }
            }
            case SUE_RELEASE -> {
                if (in(player, FF_WORLD)) {
                    releaseAll(player, level);
                }
            }
            case OPTIC_BLAST -> {
                if (in(player, XMEN_WORLD)) {
                    opticBlast(player, level);
                }
            }
            default -> {
            }
        }
    }

    private static void launch(ServerPlayer player, ServerLevel level) {
        setFlight(player, true);
        var a = player.getAbilities();
        a.flying = true;
        player.onUpdateAbilities();
        player.setDeltaMovement(player.getDeltaMovement().add(0, 1.1, 0));
        player.hurtMarked = true;
        level.playSound(null, player.blockPosition(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 1f, 1.4f);
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 20, 0.6, 0.1, 0.6, 0.05);
    }

    // ================================================================== the end of Scenes 12-16

    static void finishMilestone(ServerPlayer player) {
        PlayerProgress p = Profiles.get(player);
        p.setFlag(FLAG_MILESTONE_3);
        if (player.server.getLevel(FF_WORLD) instanceof ServerLevel ffw) {
            Live l = LIVE.get(player.getUUID());
            if (l != null) {
                while (!l.fields.isEmpty()) {
                    release(ffw, l.fields.removeFirst());
                }
            }
        }
        // Back to Steve, watching from the observatory.
        Perspective.becomeSteve(player);
        // Scene 17 is not built yet: free travel so nobody is stranded in the observatory.
        p.setFlag(WRTravel.FREE_TRAVEL_FLAG);
        StoryService.toast(player, "info", "toast.wrmarvel.milestone3.title", "toast.wrmarvel.milestone3.detail", "");
        WRCore.LOGGER.info("[Worlds Reborn] {} finished Marvel Act 1 Scenes 12-16", player.getScoreboardName());
        Profiles.sync(player);
    }

    /** The dimension a scene is played in (for the developer jump and checkpoints). */
    static ResourceKey<Level> dimensionOf(String quest) {
        return switch (quest) {
            case S15 -> FF_WORLD;
            case S16 -> XMEN_WORLD;
            default -> NEW_YORK;
        };
    }
}
