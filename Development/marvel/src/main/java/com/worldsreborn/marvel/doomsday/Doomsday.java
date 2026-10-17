package com.worldsreborn.marvel.doomsday;

import com.worldsreborn.characters.NpcSpawner;
import com.worldsreborn.characters.entity.HostileNpcEntity;
import com.worldsreborn.characters.entity.NpcInteractEvent;
import com.worldsreborn.characters.entity.StoryNpcEntity;
import com.worldsreborn.core.WRCore;
import com.worldsreborn.core.model.DialogueChoice;
import com.worldsreborn.core.model.DialogueDef;
import com.worldsreborn.core.model.DialogueLine;
import com.worldsreborn.core.model.Location;
import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.model.ProgressResult;
import com.worldsreborn.core.model.QuestDef;
import com.worldsreborn.core.model.StepDef;
import com.worldsreborn.core.model.WRContent;
import com.worldsreborn.core.profile.Profiles;
import com.worldsreborn.marvel.MarvelScript611;
import com.worldsreborn.marvel.MarvelSites;
import com.worldsreborn.marvel.WRMarvel;
import com.worldsreborn.marvel.block.MarvelBlocks;
import com.worldsreborn.marvel.item.MarvelItems;
import com.worldsreborn.story.StoryService;
import com.worldsreborn.story.event.CinematicFinishedEvent;
import com.worldsreborn.story.event.DialogueFinishedEvent;
import com.worldsreborn.story.event.QuestProgressEvent;
import com.worldsreborn.travel.TravelService;
import com.worldsreborn.travel.WRTravel;
import com.worldsreborn.travel.event.TravelArrivedEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.worldsreborn.marvel.MarvelIds.*;

/**
 * Runs Act 1 "Doomsday", Scenes 6-11 (WR1_006-WR1_011).
 * <p>
 * Like {@link com.worldsreborn.marvel.MarvelStory}, everything is driven from the player's saved quest step: each step
 * is named after the checkpoint it reaches, and {@link #setup} makes the world match the current step (people at
 * their posts, doors in the right state, targets). Finer progress inside a step (choices made, supplies delivered,
 * refugees through the gateway, Mobius's status...) is stored as player flags under {@link
 * com.worldsreborn.marvel.MarvelIds#FLAG_STATE_PREFIX}. Short timed beats (a door failing a moment after a line) are
 * kept in memory only; after a reload the step simply starts its beat again, so nothing is duplicated or lost.
 * <p>
 * Dialogue text is {@link MarvelScript611}; lines without a recording are shown as subtitles only (audio_missing).
 */
public final class Doomsday {
    private static final String NS = WRMarvel.MODID + ":";
    public static final String PETER_SKIN = NS + "textures/entity/tom_spiderman.png";

    private Doomsday() {
    }

    // ================================================================== dialogue from the script

    /** Dialogue id for a script group, e.g. {@code d06_search} -> {@code wrmarvel:d06_search}. */
    public static String dlg(String group) {
        return NS + group;
    }

    private static String voice(String lineId) {
        return MarvelScript611.MILLIS.getOrDefault(lineId, 0) > 0 ? NS + "voice." + lineId : null;
    }

    private static DialogueLine line(String id, boolean asReply) {
        String speaker = MarvelScript611.SPEAKER.get(id);
        boolean player = asReply && MarvelScript611.PLAYER_LINE.getOrDefault(id, false);
        return new DialogueLine("speaker.wrmarvel." + speaker, "line.wrmarvel." + id, false, voice(id),
                player ? "tone.wrmarvel." + id : null);
    }

    private static List<DialogueLine> lines(String... groups) {
        List<DialogueLine> out = new ArrayList<>();
        for (String g : groups) {
            for (String id : MarvelScript611.GROUPS.get(g)) {
                out.add(line(id, true));
            }
        }
        return out;
    }

    /**
     * Registers every conversation of Scenes 6-11. A group with options (#a/#b/#c) ends in that choice; each option
     * is followed by the group's rejoin lines (if any), so all choices come back to the same conversation.
     */
    public static void registerDialogues() {
        Map<String, String[]> combined = Map.of("d06_identify", new String[]{"d06_identify", "d06_sightings"});
        for (String group : MarvelScript611.GROUPS.keySet()) {
            if (!group.startsWith("d") || group.contains("#") || group.endsWith("_rejoin") || group.equals("d06_sightings")) {
                continue;
            }
            List<DialogueLine> body = lines(combined.getOrDefault(group, new String[]{group}));
            List<DialogueChoice> choices = new ArrayList<>();
            List<DialogueLine> rejoin = new ArrayList<>();
            for (String id : MarvelScript611.GROUPS.getOrDefault(group + "_rejoin", List.of())) {
                rejoin.add(line(id, false));
            }
            for (String opt : new String[]{"a", "b", "c"}) {
                List<String> ids = MarvelScript611.GROUPS.get(group + "#" + opt);
                if (ids != null) {
                    String id = ids.get(0);
                    choices.add(new DialogueChoice(opt, "tone.wrmarvel." + id, "line.wrmarvel." + id, voice(id), rejoin));
                }
            }
            WRContent.DIALOGUES.register(new DialogueDef(dlg(group), body, choices));
        }
    }

    /** Ticks the lines of a group take on screen: the recording's length, else the subtitle's reading time. */
    public static int speakingTicks(String group) {
        int ms = 0;
        for (String id : MarvelScript611.GROUPS.get(group)) {
            int rec = MarvelScript611.MILLIS.getOrDefault(id, 0);
            ms += (rec > 0 ? rec : 1800 + 55 * MarvelScript611.CHARS.getOrDefault(id, 30)) + 300;
        }
        return ms / 50;
    }

    /** Speaks a group of lines as subtitles while play continues, once per player. */
    private static boolean bark(ServerPlayer player, String group) {
        if (!Profiles.get(player).setFlag(FLAG_BARK_PREFIX + group)) {
            return false;
        }
        for (String id : MarvelScript611.GROUPS.get(group)) {
            StoryService.say(player, "speaker.wrmarvel." + MarvelScript611.SPEAKER.get(id), "line.wrmarvel." + id, voice(id));
        }
        return true;
    }

    // ================================================================== saved state and timed beats

    private static boolean state(ServerPlayer player, String key) {
        return Profiles.get(player).hasFlag(FLAG_STATE_PREFIX + key);
    }

    private static boolean setState(ServerPlayer player, String key) {
        boolean changed = Profiles.get(player).setFlag(FLAG_STATE_PREFIX + key);
        if (changed) {
            Profiles.sync(player);
        }
        return changed;
    }

    /** What happens in memory only: timed beats, and the beats already started this session. */
    private static final class Session {
        final Map<String, Integer> timers = new HashMap<>();
        final Set<String> started = new HashSet<>();
        final Map<String, Integer> counters = new HashMap<>();
    }

    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    private static Session session(ServerPlayer player) {
        return SESSIONS.computeIfAbsent(player.getUUID(), u -> new Session());
    }

    /** Starts a beat once per session; returns false if it was already started. */
    private static boolean once(ServerPlayer player, String beat) {
        return session(player).started.add(beat);
    }

    private static void after(ServerPlayer player, int ticks, String beat) {
        session(player).timers.put(beat, Math.max(1, ticks));
    }

    private static boolean pending(ServerPlayer player, String beat) {
        return session(player).timers.containsKey(beat);
    }

    // ================================================================== current scene

    private record Current(String quest, String step) {
        boolean is(String q, String s) {
            return quest.equals(q) && step.equals(s);
        }
    }

    private static final List<String> DOOMSDAY = List.of(S06, S07, S08, S09, S10, S11);

    @Nullable
    private static Current current(ServerPlayer player) {
        PlayerProgress p = Profiles.get(player);
        for (String id : DOOMSDAY) {
            if (p.isQuestActive(id)) {
                QuestDef q = StoryService.quest(id);
                StepDef s = p.currentStep(q);
                if (s != null) {
                    return new Current(id, s.id());
                }
            }
        }
        return null;
    }

    private static boolean on(ServerPlayer player, String quest, String step) {
        return StoryService.isOnStep(player, quest, step);
    }

    private static void progress(ServerPlayer player, String quest, String step) {
        StoryService.progress(player, quest, step, 1);
    }

    private static boolean in(ServerPlayer player, ResourceKey<Level> dim) {
        return player.level().dimension() == dim;
    }

    private static boolean near(ServerPlayer player, ResourceKey<Level> dim, BlockPos pos, double dist) {
        return in(player, dim) && player.position().distanceTo(Vec3.atBottomCenterOf(pos)) <= dist;
    }

    /** Nothing on screen that a new conversation would interrupt. */
    private static boolean idle(ServerPlayer player) {
        PlayerProgress p = Profiles.get(player);
        return p.pendingDialogue() == null && p.activeCinematic() == null && !p.inTransit();
    }

    private static void target(ServerPlayer player, ResourceKey<Level> dim, BlockPos pos, double radius) {
        Current c = current(player);
        if (c == null) {
            return;
        }
        var t = Profiles.get(player).target();
        if (t != null && t.questId().equals(c.quest) && t.stepId().equals(c.step)
                && t.location().dimension().equals(dim.location().toString())
                && (int) Math.floor(t.location().x()) == pos.getX() && (int) Math.floor(t.location().y()) == pos.getY()
                && (int) Math.floor(t.location().z()) == pos.getZ()) {
            return;
        }
        StoryService.setTarget(player, c.quest, c.step, dim, pos, radius);
    }

    /** Where the player comes back after dying: the group checkpoint of the current beat. */
    private static void checkpoint(ServerPlayer player, ResourceKey<Level> dim, BlockPos pos) {
        PlayerProgress p = Profiles.get(player);
        Location now = p.checkpoint();
        String d = dim.location().toString();
        if (now != null && now.dimension().equals(d) && (int) Math.floor(now.x()) == pos.getX() && (int) Math.floor(now.z()) == pos.getZ()) {
            return;
        }
        p.setCheckpoint(new Location(d, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, player.getYRot(), 0f));
        Profiles.sync(player);
    }

    private static void message(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }

    // ================================================================== people

    /** Per-player tag, for the people this player escorts and the attackers in their way. */
    private static String own(ServerPlayer player, String base) {
        return base + "_" + player.getUUID().toString().substring(0, 8);
    }

    private static final String TAG_STAFF = NS + "dd_staff";
    private static final String TAG_REFUGEE = NS + "dd_refugee";
    private static final String TAG_ATTACKER = NS + "dd_attacker";
    private static final String TAG_UATU = NS + "dd_uatu";
    private static final String TAG_WATCHER = NS + "dd_watcher";
    private static final String TAG_NY = NS + "dd_ny";
    private static final String TAG_HERO = NS + "dd_hero";

    // Role characters (registered in MarvelContent): the same skins with their job as the name.
    public static final String RECEPTION_WORKER = NS + "reception_worker";
    public static final String OPS_OFFICER = NS + "ops_officer";
    public static final String TECHNICIAN = NS + "technician";
    public static final String ATTENDANT = NS + "attendant";
    public static final String SECURITY_AGENT = NS + "security_agent";
    public static final String FIELD_AGENT = NS + "tva_agent";
    public static final String EVAC_WORKER = NS + "evac_worker";
    public static final String COORDINATOR = NS + "coordinator";
    public static final String CAR_OCCUPANT = NS + "car_occupant";
    public static final String PEDESTRIAN = NS + "pedestrian";

    /**
     * One NPC per tag near {@code pos}: spawns it if missing and removes duplicates. Returns null while the spot's
     * chunk is not entity-ticking (spawning then would make duplicates; setup runs again shortly).
     */
    @Nullable
    private static StoryNpcEntity npc(ServerLevel level, String characterId, String tag, BlockPos pos, float yaw) {
        if (!level.isPositionEntityTicking(pos)) {
            return null;
        }
        List<StoryNpcEntity> found = NpcSpawner.findStoryNpcs(level, tag, pos, 160);
        if (found.isEmpty()) {
            return NpcSpawner.spawnStoryNpc(level, characterId, tag, pos, yaw);
        }
        for (int i = 1; i < found.size(); i++) {
            found.get(i).discard();
        }
        return found.get(0);
    }

    /** An NPC that stays at a post: put back if it wandered far, otherwise left alone. */
    @Nullable
    private static StoryNpcEntity post(ServerLevel level, String characterId, String tag, BlockPos pos, float yaw, boolean ambient) {
        StoryNpcEntity n = npc(level, characterId, tag, pos, yaw);
        if (n != null) {
            if (n.isAmbient() != ambient) {
                n.setAmbient(ambient, 0);
            }
            if (!n.isWalking() && !n.blockPosition().closerThan(pos, 3)) {
                n.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0f);
            }
        }
        return n;
    }

    /** Sends an NPC towards a spot: walks if it is near enough, otherwise appears there. */
    private static void sendTo(@Nullable StoryNpcEntity n, BlockPos pos) {
        if (n == null) {
            return;
        }
        double d = n.position().distanceTo(Vec3.atBottomCenterOf(pos));
        if (d > 48) {
            n.stopWalking();
            n.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, n.getYRot(), 0f);
        } else if (d > 2.0 && !n.isWalking()) {
            n.walkTo(pos);
        }
    }

    private static void removeAll(ServerLevel level, String tag, BlockPos around, double radius) {
        NpcSpawner.findStoryNpcs(level, tag, around, radius).forEach(StoryNpcEntity::discard);
    }

    private static List<HostileNpcEntity> attackers(ServerLevel level, ServerPlayer player, String group, BlockPos around) {
        return NpcSpawner.findHostiles(level, own(player, TAG_ATTACKER + "_" + group), around, 200);
    }

    private static void clearAttackers(ServerLevel level, ServerPlayer player, String group, BlockPos around, boolean puff) {
        for (HostileNpcEntity a : attackers(level, player, group, around)) {
            if (puff) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, a.getX(), a.getY() + 1, a.getZ(), 20, 0.3, 0.8, 0.3, 0.02);
            }
            a.discard();
        }
    }

    /** Spawns a group of attackers if none of that group is around. They hold still until released or hit. */
    private static void ensureAttackers(ServerLevel level, ServerPlayer player, String group, List<BlockPos> spots, float yaw, int hold, int roam) {
        BlockPos first = spots.get(0);
        if (!level.isPositionEntityTicking(first) || !attackers(level, player, group, first).isEmpty()) {
            return;
        }
        UUID encounter = UUID.nameUUIDFromBytes((group + player.getUUID()).getBytes());
        for (BlockPos s : spots) {
            HostileNpcEntity a = NpcSpawner.spawnHostile(level, ATTACKER, own(player, TAG_ATTACKER + "_" + group), encounter, s, yaw);
            if (a != null) {
                a.hold(hold);
                a.restrictTo(s, roam);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, a.getX(), a.getY() + 1, a.getZ(), 20, 0.3, 0.8, 0.3, 0.02);
            }
        }
    }

    // ---- refugees

    private static final int ANA = 0, JONAH = 1, TEODOR = 2;

    @Nullable
    private static StoryNpcEntity refugee(ServerLevel tva, ServerPlayer player, int i, BlockPos spawnAt) {
        StoryNpcEntity r = npc(tva, REFUGEES.get(i), own(player, TAG_REFUGEE + "_" + i), spawnAt, 0f);
        if (r != null) {
            r.setWalkSpeed(i == TEODOR ? 0.45 : 0.75);
        }
        return r;
    }

    /** Where refugee {@code i} stands around a rally point, so the group doesn't stack on one block. */
    private static BlockPos around(BlockPos rally, int i) {
        int[][] off = {{0, 0}, {-2, 1}, {2, 1}, {-1, -2}, {1, 2}};
        return rally.offset(off[i][0], 0, off[i][1]);
    }

    private static boolean groupAt(ServerLevel tva, ServerPlayer player, BlockPos rally, double dist) {
        for (int i = 0; i < REFUGEES.size(); i++) {
            List<StoryNpcEntity> r = NpcSpawner.findStoryNpcs(tva, own(player, TAG_REFUGEE + "_" + i), rally, 200);
            if (r.isEmpty() || r.get(0).position().distanceTo(Vec3.atBottomCenterOf(rally)) > dist) {
                return false;
            }
        }
        return true;
    }

    /**
     * Moves the group towards a rally point. Anyone who fell (into the trench or the chasm) is brought back to the
     * group's checkpoint: nobody is ever lost, and the escort just carries on.
     */
    private static void moveGroup(ServerLevel tva, ServerPlayer player, BlockPos rally, BlockPos recovery, boolean low) {
        for (int i = 0; i < REFUGEES.size(); i++) {
            moveRefugee(tva, player, i, around(rally, i), recovery, low);
        }
    }

    private static void moveRefugee(ServerLevel tva, ServerPlayer player, int i, BlockPos to, BlockPos recovery, boolean low) {
        StoryNpcEntity r = refugee(tva, player, i, MarvelSites.tva(recovery));
        if (r == null) {
            return;
        }
        BlockPos dest = MarvelSites.tva(to);
        if (r.getY() < MarvelSites.TVA_ORIGIN.getY() - 2.5) {
            BlockPos back = MarvelSites.tva(around(recovery, i));
            r.stopWalking();
            r.moveTo(back.getX() + 0.5, back.getY(), back.getZ() + 0.5, r.getYRot(), 0f);
            tva.sendParticles(ParticleTypes.CLOUD, r.getX(), r.getY() + 1, r.getZ(), 10, 0.3, 0.5, 0.3, 0.02);
            message(player, "message.wrmarvel.refugee_recovered");
            return;
        }
        if (r.isCowering() != low) {
            r.setCowering(low);
        }
        sendTo(r, dest);
    }

    // ================================================================== setup: the world matches the step

    /** Called a few times a second from the tick, and on login, respawn and dimension change. */
    public static void setup(ServerPlayer player) {
        Current c = current(player);
        if (c == null) {
            return;
        }
        var server = player.server;
        switch (c.quest) {
            case S06, S07, S08, S09 -> {
                ServerLevel tva = server.getLevel(MarvelSites.TVA);
                if (tva == null) {
                    return;
                }
                if (!in(player, MarvelSites.TVA)) {
                    // Somewhere else (an old save, or free travel): the scene is in the TVA.
                    target(player, MarvelSites.TVA, MarvelSites.tva(MarvelSites.TVA_ARRIVAL), 4);
                    return;
                }
                DoomsdaySites.ensureTvaWing(tva);
                switch (c.quest) {
                    case S06 -> setupS06(player, tva, c.step);
                    case S07 -> setupS07(player, tva, c.step);
                    case S08 -> setupS08(player, tva, c.step);
                    default -> setupS09(player, tva, c.step);
                }
            }
            case S10 -> {
                ServerLevel obs = server.getLevel(DoomsdaySites.WATCHERS);
                if (obs != null && in(player, DoomsdaySites.WATCHERS)) {
                    setupS10(player, obs, c.step);
                }
            }
            case S11 -> {
                ServerLevel ny = server.getLevel(MarvelSites.NEW_YORK);
                if (ny != null && in(player, MarvelSites.NEW_YORK)) {
                    setupS11(player, ny, c.step);
                }
            }
            default -> {
            }
        }
    }

    private static BlockPos t(BlockPos offset) {
        return MarvelSites.tva(offset);
    }

    // ---------------------------------------------------------------- Scene 6, Beyond the Records

    private static void setupS06(ServerPlayer player, ServerLevel tva, String step) {
        StoryNpcEntity mobius = npc(tva, MOBIUS, TAG_MOBIUS_TVA, t(DoomsdaySites.MOBIUS_SEARCH), 180f);
        // Operations staff at their desks, and the officer on the platform.
        for (int i = 0; i < DoomsdaySites.OPS_STAFF.size(); i++) {
            post(tva, TVA_STAFF, TAG_STAFF + "_ops_" + i, t(DoomsdaySites.OPS_STAFF.get(i)), 90f, true);
        }
        post(tva, OPS_OFFICER, TAG_STAFF + "_officer", t(DoomsdaySites.OPS_OFFICER), 90f, false);
        switch (step) {
            case S06_SEARCH -> {
                sendTo(mobius, t(DoomsdaySites.MOBIUS_SEARCH));
                target(player, MarvelSites.TVA, t(DoomsdaySites.MOBIUS_SEARCH), 3);
                if (near(player, MarvelSites.TVA, t(DoomsdaySites.MOBIUS_SEARCH), 6) && mobius != null
                        && mobius.position().distanceTo(Vec3.atBottomCenterOf(t(DoomsdaySites.MOBIUS_SEARCH))) < 4) {
                    autoOpen(player, "d06_search", 20);
                }
            }
            case S06_CROSSING -> {
                sendTo(mobius, t(DoomsdaySites.MOBIUS_TABLE));
                target(player, MarvelSites.TVA, t(DoomsdaySites.INTERVIEW_TABLE), 3);
                if (near(player, MarvelSites.TVA, t(DoomsdaySites.INTERVIEW_TABLE), 7)) {
                    autoOpen(player, "d06_crossing", 30);
                }
            }
            case S06_IDENTIFY -> {
                sendTo(mobius, t(DoomsdaySites.MOBIUS_TABLE));
                target(player, MarvelSites.TVA, t(DoomsdaySites.DISPLAY), 2);
            }
            case S06_OPERATIONS -> {
                if (mobius != null && !mobius.isWalking() && !mobius.blockPosition().closerThan(t(DoomsdaySites.OPS_MOBIUS), 3)) {
                    mobius.walkTo(t(DoomsdaySites.OPS_MOBIUS));
                }
                target(player, MarvelSites.TVA, t(DoomsdaySites.OPS_ENTER), 4);
                if (once(player, "s06_alert")) {
                    alarm(tva, t(DoomsdaySites.CORRIDOR));
                }
            }
            case S06_FEED -> {
                sendTo(mobius, t(DoomsdaySites.OPS_MOBIUS));
                target(player, MarvelSites.TVA, t(DoomsdaySites.OPS_SCREEN_VIEW), 4);
                checkpoint(player, MarvelSites.TVA, t(DoomsdaySites.OPS_ENTER));
                if (!state(player, "s06_feed_seen") && idle(player) && !pending(player, "cin_s06_feed")
                        && near(player, MarvelSites.TVA, t(DoomsdaySites.OPS_SCREEN_VIEW), 16)) {
                    after(player, 30, "cin_s06_feed");
                }
            }
            default -> {
            }
        }
    }

    private static void alarm(ServerLevel level, BlockPos at) {
        level.playSound(null, at, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2f, 0.6f);
        level.playSound(null, at, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 2f, 0.5f);
    }

    /** Opens a conversation by itself after a short pause, once per session, when nothing else is on screen. */
    private static void autoOpen(ServerPlayer player, String group, int ticks) {
        if (once(player, "open:" + group)) {
            after(player, ticks, "open:" + group);
        }
    }

    // ---------------------------------------------------------------- Scene 7, Those Who Escaped

    private static void setupS07(ServerPlayer player, ServerLevel tva, String step) {
        StoryNpcEntity mobius = npc(tva, MOBIUS, TAG_MOBIUS_TVA, t(DoomsdaySites.RECEPTION_MOBIUS), 180f);
        sendTo(mobius, t(DoomsdaySites.RECEPTION_MOBIUS));
        StoryNpcEntity worker = npc(tva, RECEPTION_WORKER, TAG_STAFF + "_reception", t(DoomsdaySites.RECEPTION_WORKER), 90f);
        post(tva, TVA_MEDIC, TAG_STAFF + "_medic", t(DoomsdaySites.MEDIC), -90f, false);
        post(tva, ATTENDANT, TAG_STAFF + "_attendant", t(DoomsdaySites.ATTENDANT), -90f, false);
        post(tva, TECHNICIAN, TAG_STAFF + "_technician", t(DoomsdaySites.TECHNICIAN), 90f, true);
        post(tva, SECURITY_AGENT, TAG_STAFF + "_security", t(DoomsdaySites.SECURITY), -90f, true);
        checkpoint(player, MarvelSites.TVA, t(DoomsdaySites.RECEPTION_MOBIUS).offset(0, 0, 2));

        boolean reunited = state(player, "family_reunited");
        for (int i = 0; i < REFUGEES.size(); i++) {
            BlockPos home = t(DoomsdaySites.REFUGEE_RECEPTION.get(i));
            StoryNpcEntity r = refugee(tva, player, i, home);
            if (r == null) {
                continue;
            }
            if (i == ANA && (step.equals(S07_REUNITE) && state(player, "survivor_following") || reunited)) {
                continue; // following Steve, or with her partner (handled below)
            }
            if (i != JONAH || !reunited) {
                sendTo(r, home);
            }
        }
        if (worker != null && !state(player, "walker_crossing") && !step.equals(S07_ROUTE)) {
            sendTo(worker, t(DoomsdaySites.RECEPTION_WORKER));
        }

        switch (step) {
            case S07_OFFER -> {
                target(player, MarvelSites.TVA, t(DoomsdaySites.RECEPTION_MOBIUS), 3);
                if (near(player, MarvelSites.TVA, t(DoomsdaySites.RECEPTION_MOBIUS), 6)) {
                    autoOpen(player, "d07_offer", 20);
                }
            }
            case S07_ROUTE -> {
                if (!state(player, "tasks_given")) {
                    target(player, MarvelSites.TVA, t(DoomsdaySites.RECEPTION_WORKER), 3);
                    if (worker != null) {
                        sendTo(worker, t(DoomsdaySites.RECEPTION_WORKER));
                    }
                    if (near(player, MarvelSites.TVA, t(DoomsdaySites.RECEPTION_WORKER), 6)) {
                        autoOpen(player, "d07_tasks", 10);
                    }
                    return;
                }
                target(player, MarvelSites.TVA, t(DoomsdaySites.WALKWAY_NORTH), 4);
                ensureItems(player, MarvelItems.REPAIR_PANEL.get(), 12);
                if (state(player, "walker_crossing")) {
                    // The reception worker tries the walkway; when she is across, the route is clear.
                    if (worker != null && worker.blockPosition().closerThan(t(DoomsdaySites.WALKWAY_SOUTH), 2.5)) {
                        Profiles.get(player).clearFlag(FLAG_STATE_PREFIX + "walker_crossing");
                        worker.walkTo(t(DoomsdaySites.RECEPTION_WORKER));
                        progress(player, S07, S07_ROUTE);
                    } else if (worker != null && !worker.isWalking()) {
                        worker.walkTo(t(DoomsdaySites.WALKWAY_SOUTH));
                    }
                    int waited = session(player).counters.merge("walker", 1, Integer::sum);
                    if (waited > 80 && worker != null && DoomsdaySites.walkwayRepaired(tva)) {
                        // The path-finder gave up although the walkway is sound: she gets across anyway.
                        BlockPos s = t(DoomsdaySites.WALKWAY_SOUTH);
                        worker.stopWalking();
                        worker.moveTo(s.getX() + 0.5, s.getY(), s.getZ() + 0.5, 0f, 0f);
                    }
                    if (waited > 60 && !DoomsdaySites.walkwayRepaired(tva)) {
                        // Someone broke the walkway again while she was on it: back to the repairs.
                        Profiles.get(player).clearFlag(FLAG_STATE_PREFIX + "walker_crossing");
                        session(player).started.remove("open:d07_route");
                        message(player, "message.wrmarvel.walkway_not_safe");
                    }
                } else if (!DoomsdaySites.cratesRemain(tva) && DoomsdaySites.walkwayRepaired(tva)) {
                    autoOpen(player, "d07_route", 10);
                }
            }
            case S07_SUPPLIES -> {
                boolean water = state(player, "delivered_water");
                boolean blankets = state(player, "delivered_blankets");
                boolean medical = state(player, "delivered_medical");
                if (water && blankets && medical) {
                    progress(player, S07, S07_SUPPLIES);
                    return;
                }
                boolean carrying = has(player, MarvelItems.WATER.get()) || has(player, MarvelItems.BLANKETS.get()) || has(player, MarvelItems.MEDICAL.get());
                BlockPos next = !carrying ? DoomsdaySites.SHELF : has(player, MarvelItems.WATER.get()) ? DoomsdaySites.RECEPTION_WORKER
                        : has(player, MarvelItems.BLANKETS.get()) ? DoomsdaySites.ATTENDANT : DoomsdaySites.MEDIC;
                target(player, MarvelSites.TVA, t(next), 3);
            }
            case S07_REUNITE -> {
                StoryNpcEntity ana = refugee(tva, player, ANA, t(DoomsdaySites.POINT_A));
                StoryNpcEntity jonah = refugee(tva, player, JONAH, t(DoomsdaySites.POINT_B));
                if (!state(player, "survivor_following")) {
                    target(player, MarvelSites.TVA, t(DoomsdaySites.POINT_A), 3);
                } else {
                    target(player, MarvelSites.TVA, t(DoomsdaySites.POINT_B), 3);
                    follow(tva, player, ana, t(DoomsdaySites.POINT_A));
                    if (ana != null && jonah != null && ana.position().distanceTo(jonah.position()) < 4.5) {
                        ana.stopWalking();
                        autoOpen(player, "d07_reunion", 10);
                    }
                }
            }
            case S07_HOME -> {
                keepTogether(tva, player);
                target(player, MarvelSites.TVA, t(DoomsdaySites.POINT_B), 3);
                autoOpen(player, "d07_home", 30);
            }
            case S07_PENDING -> {
                keepTogether(tva, player);
                target(player, MarvelSites.TVA, t(DoomsdaySites.RECEPTION_MOBIUS), 3);
                if (near(player, MarvelSites.TVA, t(DoomsdaySites.RECEPTION_MOBIUS), 5)) {
                    autoOpen(player, "d07_pending", 10);
                }
            }
            case S07_DOORS -> {
                keepTogether(tva, player);
                if (once(player, "s07_doors")) {
                    after(player, 40, "s07_doors_fail");
                }
            }
            default -> {
            }
        }
    }

    /** After the reunion the two stay side by side at reception point B. */
    private static void keepTogether(ServerLevel tva, ServerPlayer player) {
        StoryNpcEntity ana = refugee(tva, player, ANA, t(DoomsdaySites.POINT_B));
        sendTo(ana, t(DoomsdaySites.POINT_B).offset(1, 0, 0));
        sendTo(refugee(tva, player, JONAH, t(DoomsdaySites.POINT_B)), t(DoomsdaySites.POINT_B));
    }

    /** Someone following Steve: keeps a few blocks behind him; brought back if they get lost or fall. */
    private static void follow(ServerLevel level, ServerPlayer player, @Nullable StoryNpcEntity n, BlockPos lostReturn) {
        if (n == null) {
            return;
        }
        double d = n.distanceTo(player);
        if (n.getY() < MarvelSites.TVA_ORIGIN.getY() - 2.5 || d > 40) {
            BlockPos back = Placementish.nearPlayer(level, player);
            n.stopWalking();
            n.moveTo(back.getX() + 0.5, back.getY(), back.getZ() + 0.5, n.getYRot(), 0f);
            return;
        }
        if (d > 3.5) {
            n.walkTo(player.blockPosition());
        } else if (n.isWalking()) {
            n.stopWalking();
        }
    }

    /** A safe spot just behind the player (for someone who fell behind or into the trench). */
    private static final class Placementish {
        static BlockPos nearPlayer(ServerLevel level, ServerPlayer player) {
            Vec3 back = player.position().subtract(player.getLookAngle().multiply(2, 0, 2));
            return com.worldsreborn.worlds.Placement.findSafe(level, BlockPos.containing(back), 4);
        }
    }

    // ---------------------------------------------------------------- Scene 8, The Breach

    private static final List<BlockPos> STANDOFF = List.of(new BlockPos(63, 0, -35), new BlockPos(63, 0, -33),
            new BlockPos(64, 0, -34));
    private static final List<BlockPos> SIDE_ATTACKERS = List.of(new BlockPos(82, 0, 50), new BlockPos(82, 0, 52));

    private static void setupS08(ServerPlayer player, ServerLevel tva, String step) {
        // The standoff at the unauthorised door stays where it is: security holds the attackers back.
        ensureAttackers(tva, player, "standoff", STANDOFF, -90f, -1, 5);
        for (int i = 0; i < 2; i++) {
            post(tva, SECURITY_AGENT, TAG_STAFF + "_standoff_" + i, t(new BlockPos(68, 0, -36 + i * 4)), 90f, true);
        }
        post(tva, SECURITY_AGENT, TAG_STAFF + "_security", t(DoomsdaySites.SECURITY), 90f, true);
        StoryNpcEntity mobius = NpcSpawner.findStoryNpcs(tva, TAG_MOBIUS_TVA, t(DoomsdaySites.RECEPTION_MOBIUS), 200)
                .stream().findFirst().orElse(null);
        switch (step) {
            case S08_EVACUATE -> {
                checkpoint(player, MarvelSites.TVA, t(DoomsdaySites.RALLY_EXIT));
                if (mobius == null) {
                    mobius = npc(tva, MOBIUS, TAG_MOBIUS_TVA, t(new BlockPos(70, 0, -30)), -90f);
                }
                sendTo(mobius, t(new BlockPos(70, 0, -30)));
                if (once(player, "s08_entry")) {
                    bark(player, "b08_entry");
                    after(player, speakingTicks("b08_entry"), "s08_evacuate_call");
                }
                target(player, MarvelSites.TVA, t(DoomsdaySites.RALLY_1), 3);
                if (state(player, "evacuation_called")) {
                    moveGroup(tva, player, DoomsdaySites.RALLY_1, DoomsdaySites.RALLY_EXIT, false);
                    if (groupAt(tva, player, t(DoomsdaySites.RALLY_1), 5) && near(player, MarvelSites.TVA, t(DoomsdaySites.RALLY_1), 12)) {
                        progress(player, S08, S08_EVACUATE);
                    }
                } else {
                    moveGroup(tva, player, DoomsdaySites.RALLY_EXIT, DoomsdaySites.RALLY_EXIT, false);
                }
            }
            case S08_COVER -> {
                checkpoint(player, MarvelSites.TVA, t(DoomsdaySites.RALLY_1));
                ensureAttackers(tva, player, "gallery", DoomsdaySites.GALLERY_SHOOTERS, 90f, -1, 1);
                ensureItems(player, MarvelItems.REPAIR_PANEL.get(), 26);
                boolean built = state(player, "cover_built") || DoomsdaySites.coverColumns(tva) >= DoomsdaySites.coverNeeded();
                if (built && setState(player, "cover_built")) {
                    bark(player, "b08_cover");
                }
                if (!built) {
                    target(player, MarvelSites.TVA, t(new BlockPos(89, 0, 18)), 4);
                    moveGroup(tva, player, DoomsdaySites.RALLY_1, DoomsdaySites.RALLY_1, false);
                    if (once(player, "s08_cover_hint")) {
                        message(player, "message.wrmarvel.build_cover");
                    }
                } else {
                    target(player, MarvelSites.TVA, t(DoomsdaySites.RALLY_2), 3);
                    moveGroup(tva, player, DoomsdaySites.RALLY_2, DoomsdaySites.RALLY_1, true);
                    if (groupAt(tva, player, t(DoomsdaySites.RALLY_2), 5)) {
                        progress(player, S08, S08_COVER);
                    }
                }
            }
            case S08_BRIDGE -> {
                checkpoint(player, MarvelSites.TVA, t(DoomsdaySites.RALLY_2));
                ensureAttackers(tva, player, "gallery", DoomsdaySites.GALLERY_SHOOTERS, 90f, -1, 1);
                if (!state(player, "bridge_collapsed")) {
                    // The floor ahead gives way once nobody is standing on it.
                    BlockPos rel = player.blockPosition().subtract(MarvelSites.TVA_ORIGIN);
                    boolean onIt = rel.getZ() >= DoomsdaySites.BRIDGE_Z1 - 1 && rel.getZ() <= DoomsdaySites.BRIDGE_Z2 + 1
                            && rel.getX() >= DoomsdaySites.BRIDGE_X1 - 1 && rel.getX() <= DoomsdaySites.BRIDGE_X2 + 1;
                    if (!onIt) {
                        DoomsdaySites.collapseBridge(tva);
                        BlockPos mid = t(new BlockPos(86, -1, 34));
                        tva.playSound(null, mid, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.4f, 0.6f);
                        tva.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, mid.getX(), mid.getY(), mid.getZ(), 30, 4, 0.5, 2, 0.01);
                        setState(player, "bridge_collapsed");
                        message(player, "message.wrmarvel.bridge_gap");
                    }
                }
                ensureItems(player, MarvelItems.REPAIR_PANEL.get(), 20);
                moveGroup(tva, player, DoomsdaySites.RALLY_2, DoomsdaySites.RALLY_2, false);
                if (state(player, "bridge_collapsed") && DoomsdaySites.bridgeBuilt(tva)) {
                    if (setState(player, "bridge_built")) {
                        bark(player, "b08_bridge");
                    }
                }
                if (state(player, "bridge_built")) {
                    target(player, MarvelSites.TVA, t(DoomsdaySites.RALLY_3), 3);
                    // One at a time: each waits until the one before is across.
                    for (int i = 0; i < REFUGEES.size(); i++) {
                        boolean go = i == 0 || refugeePast(tva, player, i - 1, DoomsdaySites.BRIDGE_Z2 + 1);
                        moveRefugee(tva, player, i, go ? around(DoomsdaySites.RALLY_3, i) : around(DoomsdaySites.RALLY_2, i),
                                DoomsdaySites.RALLY_2, false);
                    }
                    if (groupAt(tva, player, t(DoomsdaySites.RALLY_3), 5)) {
                        progress(player, S08, S08_BRIDGE);
                    }
                } else {
                    target(player, MarvelSites.TVA, t(new BlockPos(86, 0, 34)), 4);
                }
            }
            case S08_INJURED -> {
                checkpoint(player, MarvelSites.TVA, t(DoomsdaySites.RALLY_3));
                clearAttackers(tva, player, "gallery", t(DoomsdaySites.RALLY_2), false);
                StoryNpcEntity worker = post(tva, EVAC_WORKER, TAG_STAFF + "_evac", t(DoomsdaySites.RALLY_4).offset(2, 0, 2), 180f, false);
                for (int i = 0; i < REFUGEES.size(); i++) {
                    if (i != TEODOR) {
                        moveRefugee(tva, player, i, around(DoomsdaySites.RALLY_4, i), DoomsdaySites.RALLY_3, false);
                    }
                }
                StoryNpcEntity teodor = refugee(tva, player, TEODOR, t(DoomsdaySites.RALLY_3));
                if (teodor == null) {
                    return;
                }
                target(player, MarvelSites.TVA, teodor.blockPosition(), 3);
                if (teodor.getZ() > t(DoomsdaySites.SIDE_RIFT).getZ() - 6 && setState(player, "side_rift_open")) {
                    DoomsdaySites.setSideRift(tva, true);
                    tva.playSound(null, t(DoomsdaySites.SIDE_RIFT), SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.8f, 1.4f);
                }
                if (state(player, "side_rift_open") && !state(player, "injured_safe")) {
                    DoomsdaySites.setSideRift(tva, true);
                    ensureAttackers(tva, player, "side", SIDE_ATTACKERS, 90f, 40, 12);
                }
                boolean supported = teodor.distanceTo(player) <= 6;
                boolean threatened = attackers(tva, player, "side", t(DoomsdaySites.SIDE_RIFT)).stream()
                        .anyMatch(a -> a.distanceTo(teodor) < 5) && teodor.distanceTo(player) > 3;
                if (teodor.blockPosition().closerThan(t(around(DoomsdaySites.RALLY_4, TEODOR)), 2.5)) {
                    teodor.stopWalking();
                    teodor.setCowering(false);
                    if (setState(player, "injured_safe")) {
                        if (worker != null) {
                            worker.getLookControl().setLookAt(teodor);
                        }
                        bark(player, "b08_injured");
                        clearAttackers(tva, player, "side", t(DoomsdaySites.SIDE_RIFT), true);
                        DoomsdaySites.setSideRift(tva, false);
                        after(player, speakingTicks("b08_injured"), "s08_injured_done");
                    }
                } else if (supported && !threatened) {
                    teodor.setCowering(false);
                    moveRefugee(tva, player, TEODOR, around(DoomsdaySites.RALLY_4, TEODOR), DoomsdaySites.RALLY_3, false);
                } else {
                    teodor.stopWalking();
                    teodor.setCowering(threatened);
                    if (!supported && session(player).counters.merge("support_hint", 1, Integer::sum) % 8 == 1) {
                        message(player, "message.wrmarvel.stay_close_injured");
                    }
                }
            }
            case S08_MOBIUS -> {
                checkpoint(player, MarvelSites.TVA, t(DoomsdaySites.RALLY_4));
                moveGroup(tva, player, DoomsdaySites.RALLY_4, DoomsdaySites.RALLY_4, false);
                post(tva, EVAC_WORKER, TAG_STAFF + "_evac", t(DoomsdaySites.RALLY_4).offset(2, 0, 2), 180f, false);
                if (state(player, "mobius_leaving")) {
                    return;
                }
                StoryNpcEntity m = npc(tva, MOBIUS, TAG_MOBIUS_TVA, t(new BlockPos(84, 0, 50)), 180f);
                StoryNpcEntity agent = npc(tva, FIELD_AGENT, TAG_STAFF + "_agent", t(new BlockPos(85, 0, 49)), 180f);
                sendTo(m, t(DoomsdaySites.AGENT_REPORT).offset(1, 0, -1));
                sendTo(agent, t(DoomsdaySites.AGENT_REPORT));
                target(player, MarvelSites.TVA, t(DoomsdaySites.AGENT_REPORT), 3);
                if (m != null && m.blockPosition().closerThan(t(DoomsdaySites.AGENT_REPORT), 4)
                        && near(player, MarvelSites.TVA, t(DoomsdaySites.AGENT_REPORT), 8)) {
                    autoOpen(player, "d08_mobius", 10);
                }
            }
            default -> {
            }
        }
    }

    private static boolean refugeePast(ServerLevel tva, ServerPlayer player, int i, int z) {
        List<StoryNpcEntity> r = NpcSpawner.findStoryNpcs(tva, own(player, TAG_REFUGEE + "_" + i), t(DoomsdaySites.RALLY_2), 200);
        return !r.isEmpty() && r.get(0).getZ() > MarvelSites.TVA_ORIGIN.getZ() + z;
    }

    private static final DustParticleOptions BOLT = new DustParticleOptions(new Vector3f(1f, 0.25f, 0.2f), 1.0f);

    /** The gallery attackers fire at anyone exposed on the open side; solid cover in the way stops the shot. */
    private static void galleryFire(ServerPlayer player, ServerLevel tva) {
        if (!(on(player, S08, S08_COVER) || on(player, S08, S08_BRIDGE)) || !in(player, MarvelSites.TVA)
                || player.isCreative() || player.isSpectator()) {
            return;
        }
        BlockPos rel = player.blockPosition().subtract(MarvelSites.TVA_ORIGIN);
        if (rel.getX() < 80 || rel.getX() > 97 || rel.getZ() < DoomsdaySites.COVER_Z1 - 4 || rel.getZ() > DoomsdaySites.COVER_Z2 + 4) {
            return;
        }
        for (HostileNpcEntity a : attackers(tva, player, "gallery", t(DoomsdaySites.GALLERY_SHOOTERS.get(1)))) {
            if (tva.random.nextInt(3) != 0) {
                continue;
            }
            Vec3 from = a.getEyePosition();
            Vec3 to = player.getEyePosition().add(0, player.isCrouching() ? -0.2 : 0, 0);
            BlockHitResult hit = tva.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, a));
            boolean blocked = hit.getType() == HitResult.Type.BLOCK;
            Vec3 end = blocked ? hit.getLocation() : to;
            Vec3 d = end.subtract(from);
            int n = (int) Math.min(60, d.length() * 2);
            for (int i = 1; i <= n; i++) {
                Vec3 p = from.add(d.scale(i / (double) n));
                tva.sendParticles(BOLT, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }
            tva.playSound(null, a.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.7f, 1.6f);
            if (blocked) {
                tva.sendParticles(ParticleTypes.SMOKE, end.x, end.y, end.z, 4, 0.1, 0.1, 0.1, 0.01);
            } else {
                player.hurt(tva.damageSources().mobAttack(a), 3f);
            }
        }
    }

    // ---------------------------------------------------------------- Scene 9, The Last Door

    private static final List<BlockPos> SAFE_SPOTS = List.of(new BlockPos(85, 0, 119), new BlockPos(88, 0, 121),
            new BlockPos(91, 0, 119), new BlockPos(93, 0, 122), new BlockPos(86, 0, 123));

    private static void setupS09(ServerPlayer player, ServerLevel tva, String step) {
        clearAttackers(tva, player, "standoff", t(DoomsdaySites.RECEPTION_MOBIUS), false);
        post(tva, COORDINATOR, TAG_STAFF + "_coordinator", t(DoomsdaySites.COORDINATOR), 180f, false);
        for (int i = 0; i < DoomsdaySites.SAFE_ROOM_STAFF.size(); i++) {
            post(tva, TVA_STAFF, TAG_STAFF + "_safe_" + i, t(DoomsdaySites.SAFE_ROOM_STAFF.get(i)), 0f, true);
        }
        rumble(player, tva);
        switch (step) {
            case S09_REFUGEES -> {
                checkpoint(player, MarvelSites.TVA, t(DoomsdaySites.RALLY_5));
                boolean inChamber = player.getZ() > MarvelSites.TVA_ORIGIN.getZ() + 82;
                if (inChamber && bark(player, "b09_route")) {
                    after(player, speakingTicks("b09_route"), "s09_together");
                }
                // Steve does not go through before them.
                if (player.getZ() > MarvelSites.TVA_ORIGIN.getZ() + DoomsdaySites.GATEWAY_Z - 0.5) {
                    BlockPos back = t(DoomsdaySites.GATEWAY_FRONT).offset(0, 0, -3);
                    player.teleportTo(back.getX() + 0.5, back.getY(), back.getZ() + 0.5);
                    message(player, "message.wrmarvel.refugees_first");
                }
                if (!DoomsdaySites.debrisGap(tva)) {
                    target(player, MarvelSites.TVA, t(new BlockPos(89, 0, 104)), 3);
                    moveGroup(tva, player, inChamber ? DoomsdaySites.CHAMBER_CENTRE : DoomsdaySites.RALLY_5, DoomsdaySites.RALLY_5, false);
                    return;
                }
                target(player, MarvelSites.TVA, t(DoomsdaySites.GATEWAY_FRONT), 3);
                int safe = 0;
                for (int i = 0; i < REFUGEES.size(); i++) {
                    StoryNpcEntity r = refugee(tva, player, i, t(DoomsdaySites.RALLY_5));
                    if (state(player, "refugee_safe_" + i)) {
                        safe++;
                        if (r != null) {
                            sendTo(r, t(SAFE_SPOTS.get(i)));
                        }
                        continue;
                    }
                    if (r == null) {
                        continue;
                    }
                    boolean turn = i == 0 || state(player, "refugee_safe_" + (i - 1));
                    if (!turn) {
                        moveRefugee(tva, player, i, around(DoomsdaySites.CHAMBER_CENTRE, i), DoomsdaySites.RALLY_5, false);
                        continue;
                    }
                    if (i == REFUGEES.size() - 1) {
                        bark(player, "b09_last");
                    }
                    if (r.getZ() > MarvelSites.TVA_ORIGIN.getZ() + DoomsdaySites.GATEWAY_Z + 0.5) {
                        setState(player, "refugee_safe_" + i);
                        sendTo(r, t(SAFE_SPOTS.get(i)));
                        continue;
                    }
                    int tries = session(player).counters.merge("cross_" + i, 1, Integer::sum);
                    if (r.blockPosition().closerThan(t(DoomsdaySites.GATEWAY_FRONT), 3) && tries > 16 || tries > 90) {
                        // Through the doorway (if the path-finder balks at the light, step them through).
                        BlockPos s = t(SAFE_SPOTS.get(i));
                        tva.sendParticles(ParticleTypes.END_ROD, r.getX(), r.getY() + 1, r.getZ(), 12, 0.3, 0.8, 0.3, 0.02);
                        r.stopWalking();
                        r.moveTo(s.getX() + 0.5, s.getY(), s.getZ() + 0.5, 0f, 0f);
                    } else {
                        r.setCowering(false);
                        if (!r.isWalking()) {
                            r.walkTo(t(SAFE_SPOTS.get(i)));
                        }
                    }
                }
                if (safe == REFUGEES.size()) {
                    // Everyone is through. The doorway fails before Steve can follow; they stay safe on the other side.
                    DoomsdaySites.setGateway(tva, Blocks.ORANGE_CONCRETE.defaultBlockState());
                    BlockPos g = t(DoomsdaySites.GATEWAY_FRONT).offset(0, 1, 2);
                    tva.playSound(null, g, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.6f, 0.5f);
                    tva.playSound(null, g, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1.6f, 0.5f);
                    setState(player, "refugees_safe");
                    bark(player, "b09_door_fails");
                    progress(player, S09, S09_REFUGEES);
                }
            }
            case S09_BACKUP -> {
                DoomsdaySites.setGateway(tva, Blocks.ORANGE_CONCRETE.defaultBlockState());
                checkpoint(player, MarvelSites.TVA, t(DoomsdaySites.GATEWAY_FRONT).offset(0, 0, -3));
                ensureItems(player, MarvelItems.REPAIR_PANEL.get(), 10);
                target(player, MarvelSites.TVA, t(DoomsdaySites.BACKUP_CONTROL), 2);
                if (once(player, "s09_backup_hint")) {
                    message(player, "message.wrmarvel.backup_control");
                }
            }
            case S09_DOORWAY -> {
                DoomsdaySites.setGateway(tva, Blocks.ORANGE_CONCRETE.defaultBlockState());
                DoomsdaySites.setEmergencyDoor(tva, true);
                target(player, MarvelSites.TVA, t(DoomsdaySites.EMERGENCY_DOOR), 2);
                BlockPos at = player.blockPosition();
                if (tva.getBlockState(at).is(MarvelBlocks.TVA_DOORWAY.get())
                        && at.getX() - MarvelSites.TVA_ORIGIN.getX() <= DoomsdaySites.EMERGENCY_DOOR.getX()
                        && TravelService.beginTransit(player, DEST_WATCHERS, CIN_S09_DOORWAY, CIN_S09_DOORWAY_TICKS)) {
                    progress(player, S09, S09_DOORWAY);
                }
            }
            default -> {
            }
        }
    }

    /** The TVA coming apart around the chamber: distant impacts and dust from the ceiling. */
    private static void rumble(ServerPlayer player, ServerLevel tva) {
        if (tva.random.nextInt(4) != 0) {
            return;
        }
        BlockPos p = player.blockPosition().offset(tva.random.nextInt(17) - 8, 6, tva.random.nextInt(17) - 8);
        tva.playSound(null, p, tva.random.nextBoolean() ? SoundEvents.DEEPSLATE_BREAK : SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.AMBIENT, 0.6f, 0.4f + tva.random.nextFloat() * 0.3f);
        tva.sendParticles(ParticleTypes.ASH, p.getX(), p.getY(), p.getZ(), 20, 3, 0.5, 3, 0.01);
    }

    // ---------------------------------------------------------------- Scene 10, Sanctuary of the Watchers

    private static BlockPos o(BlockPos offset) {
        return DoomsdaySites.obs(offset);
    }

    private static void setupS10(ServerPlayer player, ServerLevel obs, String step) {
        DoomsdaySites.ensureObservatory(obs);
        checkpoint(player, DoomsdaySites.WATCHERS, o(DoomsdaySites.OBS_ARRIVAL));
        StoryNpcEntity uatu = post(obs, UATU, TAG_UATU, o(DoomsdaySites.OBS_UATU_GREET), 180f, false);
        for (int i = 0; i < DoomsdaySites.OBS_WATCHERS.size(); i++) {
            post(obs, WATCHER, TAG_WATCHER + "_" + i, o(DoomsdaySites.OBS_WATCHERS.get(i)), 180f, i != 0);
        }
        target(player, DoomsdaySites.WATCHERS, o(DoomsdaySites.OBS_UATU_GREET), 3);
        if (uatu == null || !idle(player) || !near(player, DoomsdaySites.WATCHERS, o(DoomsdaySites.OBS_UATU_GREET), 10)) {
            return;
        }
        switch (step) {
            case S10_REFUGEES -> {
                if (!state(player, "uatu_met")) {
                    autoOpen(player, "d10_visitor", 40);
                } else if (!state(player, "watchers_debated")) {
                    autoOpen(player, "d10_debate", 20);
                } else {
                    autoOpen(player, "d10_refugees_ask", 20);
                }
            }
            case S10_FALLS -> {
                if (once(player, "cin_s10_falls")) {
                    after(player, 30, "cin_s10_falls");
                }
            }
            case S10_SANCTUARY -> autoOpen(player, "d10_conditions", 30);
            case S10_HANDOVER -> {
                if (!state(player, "peter_seen")) {
                    autoOpen(player, "d10_peter_ask", 30);
                } else if (once(player, "cin_s10_handover")) {
                    after(player, 20, "cin_s10_handover");
                }
            }
            default -> {
            }
        }
    }

    // ---------------------------------------------------------------- Scene 11, The Missing Traveller

    private static BlockPos n(BlockPos offset) {
        return MarvelSites.ny(offset);
    }

    private static final String[] INSPECT = {"inspected_wall", "inspected_pavement", "inspected_exit"};

    private static BlockPos[] inspectSpots() {
        return new BlockPos[]{DoomsdaySites.ALLEY_WALL, DoomsdaySites.ALLEY_PAVEMENT, DoomsdaySites.ALLEY_EXIT};
    }

    private static void setupS11(ServerPlayer player, ServerLevel ny, String step) {
        DoomsdaySites.ensureNewYorkS11(ny);
        if (state(player, "perspective_peter")) {
            // Spider-Man: a lighter step and a stronger jump (falls are handled by the web-shooters).
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 140, 1, true, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 140, 0, true, false, false));
        }
        switch (step) {
            case S11_ALLEY -> {
                checkpoint(player, MarvelSites.NEW_YORK, n(MarvelSites.NY_ARRIVAL));
                if (bark(player, "b11_alley")) {
                    after(player, speakingTicks("b11_alley") + 20, "b11_wait");
                }
                BlockPos[] spots = inspectSpots();
                int done = 0;
                BlockPos next = null;
                for (int i = 0; i < spots.length; i++) {
                    BlockPos p = n(spots[i]);
                    if (state(player, INSPECT[i])) {
                        done++;
                        continue;
                    }
                    if (next == null) {
                        next = p;
                    }
                    ny.sendParticles(ParticleTypes.WAX_OFF, p.getX() + 0.5, p.getY() + 0.6, p.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.0);
                    if (player.position().distanceTo(Vec3.atBottomCenterOf(p)) < 2.5 && setState(player, INSPECT[i])) {
                        message(player, "message.wrmarvel." + INSPECT[i]);
                        ny.playSound(null, p, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8f, 1.2f);
                        done++;
                    }
                }
                if (next != null) {
                    target(player, MarvelSites.NEW_YORK, next, 2);
                }
                if (done == spots.length) {
                    progress(player, S11, S11_ALLEY);
                    message(player, "message.wrmarvel.use_scanner_pavement");
                }
            }
            case S11_SCAN -> target(player, MarvelSites.NEW_YORK, n(DoomsdaySites.ALLEY_PAVEMENT), 2);
            case S11_ACCIDENT -> {
                checkpoint(player, MarvelSites.NEW_YORK, n(DoomsdaySites.PAVEMENT_SAFE));
                target(player, MarvelSites.NEW_YORK, n(DoomsdaySites.ACCIDENT), 6);
                if (player.position().distanceTo(Vec3.atBottomCenterOf(n(DoomsdaySites.ACCIDENT))) < 40) {
                    accident(player, ny);
                }
            }
            case S11_SKY -> {
                accidentAftermath(player, ny);
                checkpoint(player, MarvelSites.NEW_YORK, n(DoomsdaySites.PAVEMENT_SAFE));
                target(player, MarvelSites.NEW_YORK, n(DoomsdaySites.OBSERVATION_ROOF), 3);
                if (near(player, MarvelSites.NEW_YORK, n(DoomsdaySites.OBSERVATION_ROOF), 6) && once(player, "s11_sky_hint")) {
                    message(player, "message.wrmarvel.record_sky");
                }
            }
            case S11_MEETING -> {
                accidentAftermath(player, ny);
                if (!state(player, "daredevil_called")) {
                    autoOpen(player, "d11_call", 40);
                    return;
                }
                target(player, MarvelSites.NEW_YORK, n(DoomsdaySites.HK_ROOF), 3);
                heroes(ny);
                if (near(player, MarvelSites.NEW_YORK, n(DoomsdaySites.HK_ROOF), 7)) {
                    checkpoint(player, MarvelSites.NEW_YORK, n(DoomsdaySites.HK_PETER));
                    progress(player, S11, S11_MEETING);
                }
            }
            case S11_SHARE -> {
                checkpoint(player, MarvelSites.NEW_YORK, n(DoomsdaySites.HK_PETER));
                heroes(ny);
                target(player, MarvelSites.NEW_YORK, n(DoomsdaySites.HK_HEROES.get(0)), 3);
                if (!state(player, "meeting_held") && near(player, MarvelSites.NEW_YORK, n(DoomsdaySites.HK_ROOF), 8)) {
                    autoOpen(player, "d11_meeting", 20);
                }
            }
            default -> {
            }
        }
    }

    /** The five at the Hell's Kitchen meeting, spread around the roof, facing the middle. */
    private static void heroes(ServerLevel ny) {
        String[] who = {DAREDEVIL, JESSICA, LUKE, DANNY, FRANK};
        float[] yaw = {0f, 90f, 135f, -135f, -70f};
        for (int i = 0; i < who.length; i++) {
            post(ny, who[i], TAG_HERO + "_" + i, n(DoomsdaySites.HK_HEROES.get(i)), yaw[i], false);
        }
    }

    private static void accident(ServerPlayer player, ServerLevel ny) {
        boolean secured = DoomsdaySites.allWebbed(ny, DoomsdaySites.CAR_ANCHORS);
        boolean barrier = DoomsdaySites.allWebbed(ny, DoomsdaySites.BARRIER_POSTS);
        // The two people in the car, a witness on the pavement, and pedestrians drifting towards the hole.
        for (int i = 0; i < DoomsdaySites.OCCUPANTS.size(); i++) {
            StoryNpcEntity o = npc(ny, i == 0 ? CAR_OCCUPANT : BYSTANDER, own(player, TAG_NY + "_occupant_" + i), n(DoomsdaySites.OCCUPANTS.get(i)), 180f);
            if (o == null) {
                continue;
            }
            if (state(player, "occupant_out_" + i)) {
                o.setCowering(false);
                if (o.blockPosition().closerThan(n(DoomsdaySites.PAVEMENT_SAFE), 3)) {
                    setState(player, "occupant_safe_" + i);
                } else {
                    int tries = session(player).counters.merge("occ_" + i, 1, Integer::sum);
                    if (tries > 50) {
                        BlockPos s = n(DoomsdaySites.PAVEMENT_SAFE).offset(i, 0, 0);
                        o.stopWalking();
                        o.moveTo(s.getX() + 0.5, s.getY(), s.getZ() + 0.5, 0f, 0f);
                    } else if (!o.isWalking()) {
                        o.walkTo(o.getZ() > n(DoomsdaySites.OCCUPANT_EXIT).getZ() + 1 ? n(DoomsdaySites.OCCUPANT_EXIT) : n(DoomsdaySites.PAVEMENT_SAFE).offset(i, 0, 0));
                    }
                }
            } else {
                o.setCowering(true);
                if (!o.blockPosition().closerThan(n(DoomsdaySites.OCCUPANTS.get(i)), 1.5)) {
                    BlockPos s = n(DoomsdaySites.OCCUPANTS.get(i));
                    o.moveTo(s.getX() + 0.5, s.getY(), s.getZ() + 0.5, 180f, 0f);
                }
            }
        }
        post(ny, BYSTANDER, own(player, TAG_NY + "_witness"), n(DoomsdaySites.WITNESS), 180f, false);
        for (int i = 0; i < DoomsdaySites.PEDESTRIANS.size(); i++) {
            StoryNpcEntity p = npc(ny, PEDESTRIAN, own(player, TAG_NY + "_pedestrian_" + i), n(DoomsdaySites.PEDESTRIANS.get(i)), 180f);
            if (p == null) {
                continue;
            }
            BlockPos held = n(DoomsdaySites.PEDESTRIANS_HELD).offset(i * 2 - 2, 0, 0);
            if (barrier) {
                sendTo(p, held);
            } else if (p.blockPosition().closerThan(n(DoomsdaySites.PEDESTRIANS_DANGER), 3.5)) {
                // Too close to the edge: they get a fright and back off, then drift forward again.
                p.stopWalking();
                BlockPos s = n(DoomsdaySites.PEDESTRIANS.get(i));
                p.walkTo(s);
                message(player, "message.wrmarvel.keep_people_back");
            } else if (!p.isWalking() && player.position().distanceTo(Vec3.atBottomCenterOf(n(DoomsdaySites.ACCIDENT))) < 24) {
                p.walkTo(n(DoomsdaySites.PEDESTRIANS_DANGER).offset(i * 2 - 2, 0, 0));
            }
        }
        if (!secured && ny.random.nextInt(6) == 0) {
            ny.playSound(null, n(DoomsdaySites.ACCIDENT).offset(0, 0, 5), SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 1.2f, 0.5f);
        }
        boolean out = state(player, "occupant_safe_0") && state(player, "occupant_safe_1");
        if (secured && barrier && out && setState(player, "accident_done")) {
            progress(player, S11, S11_ACCIDENT);
            // Another Earth, faint in the sky.
            setState(player, "other_earth_visible");
            ny.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.AMBIENT, 1.5f, 0.4f);
            bark(player, "b11_sky");
            after(player, speakingTicks("b11_sky"), "b11_clear_road");
        }
    }

    /** After the rescue the people stay safe where they were left; the danger area stays closed off. */
    private static void accidentAftermath(ServerPlayer player, ServerLevel ny) {
        for (int i = 0; i < DoomsdaySites.OCCUPANTS.size(); i++) {
            post(ny, i == 0 ? CAR_OCCUPANT : BYSTANDER, own(player, TAG_NY + "_occupant_" + i),
                    n(DoomsdaySites.PAVEMENT_SAFE).offset(i, 0, 0), 0f, true);
        }
        for (int i = 0; i < DoomsdaySites.PEDESTRIANS.size(); i++) {
            post(ny, PEDESTRIAN, own(player, TAG_NY + "_pedestrian_" + i), n(DoomsdaySites.PEDESTRIANS_HELD).offset(i * 2 - 2, 0, 0), 180f, true);
        }
        post(ny, BYSTANDER, own(player, TAG_NY + "_witness"), n(DoomsdaySites.WITNESS), 180f, true);
    }

    // ================================================================== timed beats

    private static void fire(ServerPlayer player, String beat) {
        ServerLevel level = player.serverLevel();
        if (beat.startsWith("open:")) {
            String group = beat.substring(5);
            if (!idle(player)) {
                after(player, 10, beat); // never over another conversation or a cinematic
                return;
            }
            if (stillWanted(player, group)) {
                StoryService.openDialogue(player, dlg(group));
            } else {
                session(player).started.remove(beat);
            }
            return;
        }
        switch (beat) {
            case "cin_s06_feed" -> {
                if (on(player, S06, S06_FEED) && idle(player)) {
                    StoryService.playCinematic(player, CIN_S06_FEED, CIN_S06_FEED_TICKS);
                } else if (on(player, S06, S06_FEED)) {
                    after(player, 10, beat);
                }
            }
            case "s07_doors_fail" -> {
                if (!on(player, S07, S07_DOORS) || !in(player, MarvelSites.TVA)) {
                    return;
                }
                // The working evacuation doorway goes dark.
                DoomsdaySites.setEvacGateway(level, false);
                BlockPos g = t(new BlockPos(96, 1, -33));
                level.playSound(null, g, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.5f, 0.6f);
                bark(player, "b07_doors");
                after(player, speakingTicks("b07_doors") + 10, "s07_rift");
            }
            case "s07_rift" -> {
                if (!on(player, S07, S07_DOORS) || !in(player, MarvelSites.TVA)) {
                    return;
                }
                // A door nobody scheduled opens on the far wall.
                DoomsdaySites.setRiftDoor(level, true);
                BlockPos d = t(new BlockPos(62, 2, -34));
                level.playSound(null, d, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1f, 0.7f);
                level.playSound(null, d, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.2f, 0.5f);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, d.getX(), d.getY(), d.getZ(), 60, 0.4, 1.5, 1.5, 0.05);
                bark(player, "b07_door_unscheduled");
                after(player, speakingTicks("b07_door_unscheduled"), "s07_done");
            }
            case "s07_done" -> {
                if (on(player, S07, S07_DOORS)) {
                    progress(player, S07, S07_DOORS);
                }
            }
            case "s08_evacuate_call" -> {
                if (on(player, S08, S08_EVACUATE)) {
                    bark(player, "b08_evacuate");
                    setState(player, "evacuation_called");
                    message(player, "message.wrmarvel.lead_group");
                }
            }
            case "s08_injured_done" -> {
                if (on(player, S08, S08_INJURED)) {
                    progress(player, S08, S08_INJURED);
                }
            }
            case "s08_mobius_gone" -> {
                if (!on(player, S08, S08_MOBIUS)) {
                    return;
                }
                // The ceiling comes down across the side passage behind him.
                DoomsdaySites.blockMobiusRoute(level);
                BlockPos door = t(DoomsdaySites.MOBIUS_DOOR);
                level.playSound(null, door, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.6f, 0.5f);
                level.playSound(null, door, SoundEvents.DEEPSLATE_BREAK, SoundSource.BLOCKS, 1.6f, 0.6f);
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, door.getX(), door.getY() + 1, door.getZ(), 30, 1, 1, 1.5, 0.01);
                removeAll(level, TAG_MOBIUS_TVA, door, 64);
                removeAll(level, TAG_STAFF + "_agent", door, 64);
                setState(player, "mobius_status_separated_unknown");
                bark(player, "b08_mobius_gone");
                after(player, speakingTicks("b08_mobius_gone"), "s08_done");
            }
            case "s08_done" -> {
                if (on(player, S08, S08_MOBIUS)) {
                    progress(player, S08, S08_MOBIUS);
                }
            }
            case "s09_together" -> bark(player, "b09_together");
            case "s09_all_right" -> bark(player, "b09_all_right");
            case "s09_where" -> {
                bark(player, "b09_where");
                after(player, speakingTicks("b09_where"), "s09_arrived");
            }
            case "s09_arrived" -> {
                if (on(player, S09, S09_ARRIVE)) {
                    progress(player, S09, S09_ARRIVE);
                }
            }
            case "cin_s10_falls" -> {
                if (on(player, S10, S10_FALLS)) {
                    if (idle(player)) {
                        StoryService.playCinematic(player, CIN_S10_FALLS, CIN_S10_FALLS_TICKS);
                    } else {
                        after(player, 10, beat);
                    }
                }
            }
            case "s10_falls_done" -> {
                if (on(player, S10, S10_FALLS)) {
                    progress(player, S10, S10_FALLS);
                }
            }
            case "cin_s10_handover" -> {
                if (on(player, S10, S10_HANDOVER)) {
                    if (idle(player)) {
                        StoryService.playCinematic(player, CIN_S10_HANDOVER, CIN_S10_HANDOVER_TICKS);
                    } else {
                        after(player, 10, beat);
                    }
                }
            }
            case "b11_wait" -> bark(player, "b11_wait");
            case "b11_clear_road" -> bark(player, "b11_clear_road");
            case "scan_1" -> {
                message(player, "message.wrmarvel.scan_sampling");
                level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.PLAYERS, 0.6f, 1.4f);
                after(player, 30, "scan_2");
            }
            case "scan_2" -> {
                message(player, "message.wrmarvel.scan_comparing");
                level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.PLAYERS, 0.6f, 1.7f);
                after(player, 30, "scan_3");
            }
            case "scan_3" -> {
                if (!on(player, S11, S11_SCAN)) {
                    return;
                }
                StoryService.infoPanel(player, "scanner", "panel.wrmarvel.peter_scan.title", List.of(
                        "panel.wrmarvel.peter_scan.original", "panel.wrmarvel.peter_scan.sample",
                        "panel.wrmarvel.peter_scan.compare", "panel.wrmarvel.peter_scan.second"));
                setState(player, "peter_second_signature_recorded");
                bark(player, "b11_different");
                progress(player, S11, S11_SCAN);
            }
            case "s11_meeting_call" -> autoOpen(player, "d11_call", 10);
            case "cin_s11_watching" -> {
                if (on(player, S11, S11_SHARE)) {
                    if (idle(player)) {
                        // A brief look back at the observatory, where Steve is watching.
                        StoryService.playCinematic(player, CIN_S11_WATCHING, CIN_S11_WATCHING_TICKS);
                    } else {
                        after(player, 10, beat);
                    }
                }
            }
            default -> {
            }
        }
    }

    /** Whether an automatic conversation still belongs to the player's current step. */
    private static boolean stillWanted(ServerPlayer player, String group) {
        return switch (group) {
            case "d06_search" -> on(player, S06, S06_SEARCH);
            case "d06_crossing" -> on(player, S06, S06_CROSSING);
            case "d06_identify" -> on(player, S06, S06_IDENTIFY);
            case "d06_people" -> on(player, S06, S06_FEED);
            case "d07_offer" -> on(player, S07, S07_OFFER);
            case "d07_tasks" -> on(player, S07, S07_ROUTE) && !state(player, "tasks_given");
            case "d07_route" -> on(player, S07, S07_ROUTE) && state(player, "tasks_given") && !state(player, "walker_crossing");
            case "d07_survivor" -> on(player, S07, S07_REUNITE) && !state(player, "survivor_following");
            case "d07_reunion" -> on(player, S07, S07_REUNITE);
            case "d07_home" -> on(player, S07, S07_HOME);
            case "d07_pending" -> on(player, S07, S07_PENDING);
            case "d08_mobius" -> on(player, S08, S08_MOBIUS) && !state(player, "mobius_leaving");
            case "d10_visitor" -> on(player, S10, S10_REFUGEES) && !state(player, "uatu_met");
            case "d10_debate" -> on(player, S10, S10_REFUGEES) && !state(player, "watchers_debated");
            case "d10_refugees_ask", "d10_refugees_safe" -> on(player, S10, S10_REFUGEES);
            case "d10_conditions" -> on(player, S10, S10_SANCTUARY);
            case "d10_peter_ask", "d10_peter_watch" -> on(player, S10, S10_HANDOVER) && !state(player, "steve_message_blocked");
            case "d11_call" -> on(player, S11, S11_MEETING) && !state(player, "daredevil_called");
            case "d11_meeting" -> on(player, S11, S11_SHARE) && !state(player, "meeting_held");
            default -> false;
        };
    }

    // ================================================================== events

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % 10 == 0) {
            Session s = SESSIONS.get(player.getUUID());
            if (s != null && !s.timers.isEmpty()) {
                List<String> due = new ArrayList<>();
                for (Map.Entry<String, Integer> e : s.timers.entrySet()) {
                    e.setValue(e.getValue() - 10);
                    if (e.getValue() <= 0) {
                        due.add(e.getKey());
                    }
                }
                for (String beat : due) {
                    s.timers.remove(beat);
                    fire(player, beat);
                }
            }
        }
        if (player.tickCount % 20 == 5 && in(player, MarvelSites.TVA)) {
            galleryFire(player, player.serverLevel());
        }
        if (player.tickCount % 10 == 3 && current(player) != null) {
            setup(player);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        SESSIONS.remove(player.getUUID());
        PlayerProgress p = Profiles.get(player);
        // Saves from before Scenes 6-11 existed finished at Scene 5 with free travel as a stopgap: carry on to Scene 6.
        if (p.isQuestCompleted(S05) && !p.isQuestStarted(S06)) {
            p.clearFlag(WRTravel.FREE_TRAVEL_FLAG);
            StoryService.start(player, S06);
            if (!in(player, MarvelSites.TVA)) {
                TravelService.beginTransit(player, DEST_TVA, null, 40);
            }
        }
        // A Scene 11 save always comes back as Spider-Man; anything else as Steve.
        if (p.isQuestActive(S11) && !state(player, "perspective_peter")) {
            Perspective.becomePeter(player);
        } else if (!p.isQuestActive(S11) && state(player, "perspective_peter")) {
            Perspective.becomeSteve(player);
        }
        setup(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SESSIONS.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Current c = current(player);
        if (c != null && c.is(S08, S08_INJURED) && player.server.getLevel(MarvelSites.TVA) instanceof ServerLevel tva) {
            // Back at the group checkpoint: the attackers withdraw and come again, the injured survivor waits.
            clearAttackers(tva, player, "side", t(DoomsdaySites.SIDE_RIFT), false);
        }
        if (c != null && c.quest.equals(S11)) {
            Perspective.restoreKit(player);
        }
        setup(player);
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && state(player, "perspective_peter")) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onQuestProgress(QuestProgressEvent event) {
        ServerPlayer player = event.getPlayer();
        String q = event.getQuest().id();
        if (!DOOMSDAY.contains(q)) {
            return;
        }
        ServerLevel tva = player.server.getLevel(MarvelSites.TVA);
        if (event.isQuestStart()) {
            WRCore.LOGGER.info("[Worlds Reborn] {} started Marvel {}", player.getScoreboardName(), q);
            switch (q) {
                case S06 -> {
                    if (tva != null) {
                        DoomsdaySites.ensureTvaWing(tva);
                    }
                    // Mobius heads for the search terminal in the interview room next door.
                    if (tva != null) {
                        NpcSpawner.findStoryNpcs(tva, TAG_MOBIUS_TVA, t(MarvelSites.TVA_MOBIUS), 96)
                                .forEach(m -> m.walkTo(t(DoomsdaySites.MOBIUS_SEARCH)));
                    }
                    message(player, "message.wrmarvel.follow_mobius_interview");
                }
                case S10 -> {
                    ServerLevel ny = player.server.getLevel(MarvelSites.NEW_YORK);
                    if (ny != null) {
                        DoomsdaySites.ensureNewYorkS11(ny); // built ahead of the handover
                    }
                }
                default -> {
                }
            }
        }
        if (event.getResult() == ProgressResult.QUEST_COMPLETED && q.equals(S11)) {
            finishMilestone(player);
        }
        if (q.equals(S08) && S08_COVER.equals(event.getStepId()) && event.getResult() == ProgressResult.STEP_COMPLETED) {
            message(player, "message.wrmarvel.bridge_gap");
        }
        if (q.equals(S09) && S09_ARRIVE.equals(event.getStepId())) {
            return;
        }
        if (q.equals(S11) && S11_SKY.equals(event.getStepId()) && event.getResult() == ProgressResult.STEP_COMPLETED) {
            after(player, speakingTicks("b11_projection") + 40, "s11_meeting_call");
        }
        setup(player);
    }

    @SubscribeEvent
    public static void onArrived(TravelArrivedEvent event) {
        ServerPlayer player = event.getPlayer();
        if (DEST_WATCHERS.equals(event.getDestination().id()) && on(player, S09, S09_ARRIVE)) {
            // Silence. No one there to fight; Steve takes in where he is.
            after(player, 40, "s09_where");
        }
    }

    @SubscribeEvent
    public static void onNpcInteract(NpcInteractEvent event) {
        ServerPlayer player = event.getPlayer();
        String who = event.getCharacterId();
        Current c = current(player);
        if (c == null) {
            return;
        }
        if (MOBIUS.equals(who)) {
            switch (c.step) {
                case S06_SEARCH -> open(player, "d06_search");
                case S06_CROSSING -> open(player, "d06_crossing");
                case S06_IDENTIFY -> message(player, "message.wrmarvel.use_display");
                case S07_OFFER -> open(player, "d07_offer");
                case S07_PENDING -> open(player, "d07_pending");
                case S08_MOBIUS -> open(player, "d08_mobius");
                default -> {
                }
            }
        } else if (RECEPTION_WORKER.equals(who) && c.quest.equals(S07)) {
            if (c.is(S07, S07_ROUTE) && !state(player, "tasks_given")) {
                open(player, "d07_tasks");
            } else if (c.is(S07, S07_SUPPLIES)) {
                deliver(player, MarvelItems.WATER.get(), "delivered_water", null);
            } else if (c.is(S07, S07_ROUTE)) {
                message(player, DoomsdaySites.cratesRemain(player.serverLevel()) ? "message.wrmarvel.move_supplies" : "message.wrmarvel.repair_walkway");
            }
        } else if (ATTENDANT.equals(who) && c.is(S07, S07_SUPPLIES)) {
            deliver(player, MarvelItems.BLANKETS.get(), "delivered_blankets", null);
        } else if (TVA_MEDIC.equals(who) && c.is(S07, S07_SUPPLIES)) {
            deliver(player, MarvelItems.MEDICAL.get(), "delivered_medical", "b07_medic");
        } else if (REFUGEES.get(ANA).equals(who) && c.is(S07, S07_REUNITE) && !state(player, "survivor_following")) {
            open(player, "d07_survivor");
        } else if (REFUGEES.get(JONAH).equals(who) && c.is(S07, S07_REUNITE)) {
            message(player, state(player, "survivor_following") ? "message.wrmarvel.bring_survivor" : "message.wrmarvel.partner_waiting");
        } else if (UATU.equals(who) && c.quest.equals(S10)) {
            setupS10(player, player.serverLevel(), c.step);
        } else if ((CAR_OCCUPANT.equals(who) || BYSTANDER.equals(who)) && c.is(S11, S11_ACCIDENT)) {
            String tag = event.getNpc().getStoryTag();
            for (int i = 0; i < DoomsdaySites.OCCUPANTS.size(); i++) {
                if (tag.equals(own(player, TAG_NY + "_occupant_" + i))) {
                    if (!DoomsdaySites.allWebbed(player.serverLevel(), DoomsdaySites.CAR_ANCHORS)) {
                        message(player, "message.wrmarvel.secure_car_first");
                    } else if (setState(player, "occupant_out_" + i)) {
                        DoomsdaySites.openCarDoors(player.serverLevel());
                        bark(player, "b11_back_seat");
                        event.getNpc().walkTo(n(DoomsdaySites.OCCUPANT_EXIT));
                    }
                }
            }
        } else if (DAREDEVIL.equals(who) && c.is(S11, S11_SHARE) && !state(player, "meeting_held")) {
            open(player, "d11_meeting");
        } else if (c.is(S11, S11_SHARE) && state(player, "meeting_held") && List.of(DAREDEVIL, JESSICA, LUKE, DANNY, FRANK).contains(who)) {
            message(player, "message.wrmarvel.share_recording");
        }
    }

    /** Opens one of these scenes' conversations now (talking to someone), unless another is already showing. */
    private static void open(ServerPlayer player, String group) {
        if (!stillWanted(player, group)) {
            return;
        }
        String showing = Profiles.get(player).pendingDialogue();
        if (showing != null && !showing.equals(dlg(group))) {
            return;
        }
        session(player).timers.remove("open:" + group);
        session(player).started.add("open:" + group);
        StoryService.openDialogue(player, dlg(group));
    }

    private static void deliver(ServerPlayer player, Item item, String key, @Nullable String barkGroup) {
        if (state(player, key)) {
            message(player, "message.wrmarvel.already_delivered");
            return;
        }
        if (!take(player, item)) {
            message(player, "message.wrmarvel.nothing_to_deliver");
            return;
        }
        setState(player, key);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8f, 1f);
        message(player, "message.wrmarvel." + key);
        if (barkGroup != null) {
            bark(player, barkGroup);
        }
        setup(player);
    }

    @SubscribeEvent
    public static void onDialogueFinished(DialogueFinishedEvent event) {
        ServerPlayer player = event.getPlayer();
        String id = event.getDialogueId();
        if (!id.startsWith(NS + "d0") && !id.startsWith(NS + "d1")) {
            return;
        }
        String group = id.substring(NS.length());
        String choice = event.getChoiceId();
        ServerLevel level = player.serverLevel();
        session(player).started.remove("open:" + group);
        switch (group) {
            case "d06_search" -> {
                if (on(player, S06, S06_SEARCH)) {
                    progress(player, S06, S06_SEARCH);
                    NpcSpawner.findStoryNpcs(level, TAG_MOBIUS_TVA, player.blockPosition(), 48)
                            .forEach(m -> m.walkTo(t(DoomsdaySites.MOBIUS_TABLE)));
                }
            }
            case "d06_crossing" -> {
                if (on(player, S06, S06_CROSSING)) {
                    setState(player, "crossing_" + (choice == null ? "a" : choice));
                    progress(player, S06, S06_CROSSING);
                    message(player, "message.wrmarvel.use_display");
                }
            }
            case "d06_identify" -> {
                if (on(player, S06, S06_IDENTIFY)) {
                    setState(player, "traveller_identified");
                    progress(player, S06, S06_IDENTIFY);
                    // Priority alert: Mobius leads the way to operations.
                    alarm(level, player.blockPosition());
                    bark(player, "b06_alert");
                    NpcSpawner.findStoryNpcs(level, TAG_MOBIUS_TVA, player.blockPosition(), 48)
                            .forEach(m -> m.walkTo(t(DoomsdaySites.OPS_MOBIUS)));
                }
            }
            case "d06_people" -> {
                if (on(player, S06, S06_FEED)) {
                    progress(player, S06, S06_FEED);
                }
            }
            case "d07_offer" -> progress(player, S07, S07_OFFER);
            case "d07_tasks" -> {
                if (on(player, S07, S07_ROUTE) && setState(player, "tasks_given")) {
                    StoryService.grantOnce(player, NS + "wr1_panels", () -> giveItem(player, new ItemStack(MarvelItems.REPAIR_PANEL.get(), 24)));
                    message(player, "message.wrmarvel.tasks_given");
                }
            }
            case "d07_route" -> {
                if (on(player, S07, S07_ROUTE)) {
                    setState(player, "walker_crossing");
                    session(player).counters.put("walker", 0);
                    NpcSpawner.findStoryNpcs(level, TAG_STAFF + "_reception", player.blockPosition(), 64)
                            .forEach(w -> w.walkTo(t(DoomsdaySites.WALKWAY_SOUTH)));
                }
            }
            case "d07_survivor" -> {
                if (on(player, S07, S07_REUNITE)) {
                    setState(player, "survivor_following");
                    message(player, "message.wrmarvel.bring_survivor");
                }
            }
            case "d07_reunion" -> {
                if (on(player, S07, S07_REUNITE)) {
                    setState(player, "family_reunited");
                    progress(player, S07, S07_REUNITE);
                }
            }
            case "d07_home" -> {
                if (on(player, S07, S07_HOME)) {
                    setState(player, "home_" + (choice == null ? "a" : choice));
                    progress(player, S07, S07_HOME);
                }
            }
            case "d07_pending" -> {
                if (on(player, S07, S07_PENDING)) {
                    setState(player, "peter_contact_request_pending");
                    progress(player, S07, S07_PENDING);
                }
            }
            case "d08_mobius" -> {
                if (on(player, S08, S08_MOBIUS) && setState(player, "mobius_leaving")) {
                    // He turns back for the others.
                    NpcSpawner.findStoryNpcs(level, TAG_MOBIUS_TVA, t(DoomsdaySites.AGENT_REPORT), 32)
                            .forEach(m -> m.walkTo(t(DoomsdaySites.MOBIUS_BEYOND)));
                    NpcSpawner.findStoryNpcs(level, TAG_STAFF + "_agent", t(DoomsdaySites.AGENT_REPORT), 32)
                            .forEach(m -> m.walkTo(t(DoomsdaySites.MOBIUS_BEYOND).offset(0, 0, 2)));
                    after(player, 80, "s08_mobius_gone");
                }
            }
            case "d10_visitor" -> {
                if (setState(player, "uatu_met")) {
                    // The other Watchers draw closer.
                    autoOpen(player, "d10_debate", 30);
                }
            }
            case "d10_debate" -> {
                if (setState(player, "watchers_debated")) {
                    autoOpen(player, "d10_refugees_ask", 30);
                }
            }
            case "d10_refugees_ask" -> {
                if (on(player, S10, S10_REFUGEES)) {
                    StoryService.playCinematic(player, CIN_S10_REFUGEES, CIN_S10_REFUGEES_TICKS);
                }
            }
            case "d10_refugees_safe" -> {
                if (on(player, S10, S10_REFUGEES)) {
                    setState(player, "refugees_confirmed");
                    progress(player, S10, S10_REFUGEES);
                }
            }
            case "d10_conditions" -> {
                if (on(player, S10, S10_SANCTUARY)) {
                    setState(player, "conditions_" + (choice == null ? "a" : choice));
                    setState(player, "sanctuary_accepted");
                    progress(player, S10, S10_SANCTUARY);
                }
            }
            case "d10_peter_ask" -> {
                if (on(player, S10, S10_HANDOVER)) {
                    StoryService.playCinematic(player, CIN_S10_PETER, CIN_S10_PETER_TICKS);
                }
            }
            case "d10_peter_watch" -> {
                if (on(player, S10, S10_HANDOVER)) {
                    // Steve may not send a message while he accepts the sanctuary.
                    setState(player, "steve_message_blocked");
                    setState(player, "peter_seen");
                    after(player, 20, "cin_s10_handover");
                    session(player).started.add("cin_s10_handover");
                }
            }
            case "d11_call" -> {
                if (on(player, S11, S11_MEETING) && setState(player, "daredevil_called")) {
                    message(player, "message.wrmarvel.hk_location");
                }
            }
            case "d11_meeting" -> {
                if (on(player, S11, S11_SHARE) && setState(player, "meeting_held")) {
                    message(player, "message.wrmarvel.share_recording");
                }
            }
            default -> {
            }
        }
    }

    @SubscribeEvent
    public static void onCinematicFinished(CinematicFinishedEvent event) {
        ServerPlayer player = event.getPlayer();
        String id = event.getCinematicId();
        if (CIN_S06_FEED.equals(id) && on(player, S06, S06_FEED)) {
            setState(player, "s06_feed_seen");
            open(player, "d06_people");
        } else if (CIN_S10_REFUGEES.equals(id) && on(player, S10, S10_REFUGEES)) {
            open(player, "d10_refugees_safe");
        } else if (CIN_S10_FALLS.equals(id) && on(player, S10, S10_FALLS)) {
            setState(player, "tva_falls_seen");
            bark(player, "b10_falls");
            after(player, speakingTicks("b10_falls"), "s10_falls_done");
        } else if (CIN_S10_PETER.equals(id) && on(player, S10, S10_HANDOVER)) {
            open(player, "d10_peter_watch");
        } else if (CIN_S10_HANDOVER.equals(id) && on(player, S10, S10_HANDOVER)) {
            // The story follows Spider-Man for a while.
            Perspective.becomePeter(player);
            progress(player, S10, S10_HANDOVER);
        } else if (CIN_S11_WATCHING.equals(id) && on(player, S11, S11_SHARE)) {
            Perspective.becomeSteve(player);
            progress(player, S11, S11_SHARE);
        }
    }

    // ================================================================== blocks and items

    public static void onCrateMoved(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.serverLevel();
        boolean crate = level.getBlockState(pos).is(MarvelBlocks.FALLEN_CRATE.get());
        boolean allowed = crate ? on(player, S07, S07_ROUTE) && state(player, "tasks_given") : on(player, S09, S09_REFUGEES);
        if (!allowed) {
            message(player, "message.wrmarvel.not_now");
            return;
        }
        // Lift it clear: whatever sat on top comes down with it.
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockState(pos.above()).getBlock() instanceof FallenCrateBlock) {
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        level.playSound(null, pos, crate ? SoundEvents.WOOD_BREAK : SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.8f, 0.8f);
        level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.01);
    }

    public static void onDisplayUsed(ServerPlayer player) {
        if (on(player, S06, S06_IDENTIFY)) {
            PacketDistributor.sendToPlayer(player, new MarvelPayloads.OpenScreen("recording"));
            setState(player, "recording_open");
        } else {
            message(player, "message.wrmarvel.display_idle");
        }
    }

    public static void onScreenAction(ServerPlayer player, String action) {
        if ("recording_identified".equals(action) && on(player, S06, S06_IDENTIFY) && state(player, "recording_open")) {
            open(player, "d06_identify");
        }
    }

    public static void onSupplyShelf(ServerPlayer player) {
        if (!on(player, S07, S07_SUPPLIES)) {
            message(player, "message.wrmarvel.not_now");
            return;
        }
        boolean gave = false;
        gave |= supply(player, MarvelItems.WATER.get(), "delivered_water");
        gave |= supply(player, MarvelItems.BLANKETS.get(), "delivered_blankets");
        gave |= supply(player, MarvelItems.MEDICAL.get(), "delivered_medical");
        message(player, gave ? "message.wrmarvel.supplies_taken" : "message.wrmarvel.supplies_have");
    }

    /** One package of each kind not yet delivered and not already carried: never more than one. */
    private static boolean supply(ServerPlayer player, Item item, String delivered) {
        if (state(player, delivered) || has(player, item)) {
            return false;
        }
        giveItem(player, new ItemStack(item));
        return true;
    }

    public static void onWebAnchor(ServerPlayer player, BlockPos pos) {
        ServerLevel ny = player.serverLevel();
        BlockState st = ny.getBlockState(pos);
        if (!on(player, S11, S11_ACCIDENT) || st.getValue(WebAnchorBlock.WEBBED)) {
            return;
        }
        ny.setBlock(pos, st.setValue(WebAnchorBlock.WEBBED, true), Block.UPDATE_ALL);
        ny.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.PLAYERS, 1f, 1.4f);
        BlockPos rel = pos.subtract(MarvelSites.NY_ORIGIN);
        if (DoomsdaySites.CAR_ANCHORS.contains(rel)) {
            bark(player, "b11_car");
            if (DoomsdaySites.allWebbed(ny, DoomsdaySites.CAR_ANCHORS)) {
                ny.playSound(null, n(DoomsdaySites.ACCIDENT).offset(0, 0, 5), SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 1.2f, 0.7f);
                message(player, "message.wrmarvel.car_secured");
            }
        } else if (DoomsdaySites.BARRIER_POSTS.contains(rel) && DoomsdaySites.allWebbed(ny, DoomsdaySites.BARRIER_POSTS)) {
            DoomsdaySites.webBarrier(ny);
            message(player, "message.wrmarvel.barrier_up");
        }
        setup(player);
    }

    public static void onWebbedEntity(ServerPlayer player, LivingEntity target) {
        // Webbing an attacker or a robber just holds them; it never counts towards a scene.
        if (target instanceof HostileNpcEntity h) {
            h.hold(60);
        }
    }

    public static void onScannerUsed(ServerPlayer player) {
        Current c = current(player);
        if (c == null || !in(player, MarvelSites.NEW_YORK)) {
            message(player, "message.wrmarvel.scanner_idle");
            return;
        }
        if (c.is(S11, S11_SCAN)) {
            if (!near(player, MarvelSites.NEW_YORK, n(DoomsdaySites.ALLEY_PAVEMENT), 5)) {
                message(player, "message.wrmarvel.scanner_nothing");
            } else if (once(player, "scan")) {
                message(player, "message.wrmarvel.scan_loading");
                player.level().playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.PLAYERS, 0.6f, 1.1f);
                after(player, 30, "scan_1");
            }
        } else if (c.is(S11, S11_SKY)) {
            if (player.getY() < MarvelSites.NY_ORIGIN.getY() + 14 || !state(player, "other_earth_visible")) {
                message(player, "message.wrmarvel.record_higher");
            } else if (setState(player, "other_earth_recorded")) {
                player.level().playSound(null, player.blockPosition(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1f, 1.2f);
                message(player, "message.wrmarvel.sky_recorded");
                bark(player, "b11_projection");
                progress(player, S11, S11_SKY);
            }
        } else if (c.is(S11, S11_SHARE) && state(player, "meeting_held")) {
            if (near(player, MarvelSites.NEW_YORK, n(DoomsdaySites.HK_ROOF), 10) && setState(player, "recording_shared")) {
                StoryService.infoPanel(player, "scanner", "panel.wrmarvel.share.title",
                        List.of("panel.wrmarvel.share.sky", "panel.wrmarvel.share.signature", "panel.wrmarvel.share.sent"));
                after(player, 80, "cin_s11_watching");
            }
        } else {
            message(player, "message.wrmarvel.scanner_idle");
        }
    }

    /** The TVA terminals of Scenes 6 and 9. Returns false if this terminal is not part of them. */
    public static boolean onTerminalUsed(ServerPlayer player, BlockPos pos) {
        if (!in(player, MarvelSites.TVA)) {
            return false;
        }
        BlockPos rel = pos.subtract(MarvelSites.TVA_ORIGIN);
        if (rel.equals(DoomsdaySites.SEARCH_TERMINAL)) {
            if (on(player, S06, S06_SEARCH)) {
                open(player, "d06_search");
            } else {
                StoryService.infoPanel(player, "tva", "panel.wrmarvel.search.title",
                        List.of("panel.wrmarvel.search.coordinates", "panel.wrmarvel.search.match"));
            }
            return true;
        }
        if (rel.equals(DoomsdaySites.BACKUP_CONTROL)) {
            if (on(player, S09, S09_BACKUP)) {
                ServerLevel tva = player.serverLevel();
                DoomsdaySites.setEmergencyDoor(tva, true);
                BlockPos d = t(DoomsdaySites.EMERGENCY_DOOR);
                tva.playSound(null, d, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.5f, 0.7f);
                tva.playSound(null, d, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.2f, 0.6f);
                StoryService.infoPanel(player, "tva", "panel.wrmarvel.emergency.title", List.of(
                        "panel.wrmarvel.emergency.status", "panel.wrmarvel.emergency.destination"));
                checkpoint(player, MarvelSites.TVA, t(new BlockPos(80, 0, 96)));
                progress(player, S09, S09_BACKUP);
                after(player, 50, "s09_all_right");
            } else {
                message(player, "message.wrmarvel.terminal_locked");
            }
            return true;
        }
        if (rel.equals(DoomsdaySites.GATEWAY_CONTROLS)) {
            message(player, on(player, S09, S09_BACKUP) ? "message.wrmarvel.gateway_dead" : "message.wrmarvel.gateway_controls");
            return true;
        }
        return false;
    }

    // ================================================================== inventory helpers

    static boolean has(ServerPlayer player, Item item) {
        return player.getInventory().contains(new ItemStack(item));
    }

    private static boolean take(ServerPlayer player, Item item) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(item)) {
                inv.getItem(i).shrink(1);
                return true;
            }
        }
        return false;
    }

    /** Tops up a story tool to at least {@code count} (repair panels: the crew hands out more as needed). */
    private static void ensureItems(ServerPlayer player, Item item, int count) {
        if (player.isCreative()) {
            return;
        }
        int have = 0;
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(item)) {
                have += inv.getItem(i).getCount();
            }
        }
        if (have < count && once(player, "topup:" + item + ":" + count + ":" + (player.tickCount / 600))) {
            giveItem(player, new ItemStack(item, count - have));
        }
    }

    static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    // ================================================================== milestone and testing

    private static void finishMilestone(ServerPlayer player) {
        PlayerProgress p = Profiles.get(player);
        p.setFlag(FLAG_MILESTONE_2);
        // Scene 12 is not built yet: free travel so nobody is stranded in the observatory.
        p.setFlag(WRTravel.FREE_TRAVEL_FLAG);
        StoryService.toast(player, "info", "toast.wrmarvel.milestone2.title", "toast.wrmarvel.milestone2.detail", "");
        WRCore.LOGGER.info("[Worlds Reborn] {} finished Marvel Act 1 Scenes 6-11", player.getScoreboardName());
        Profiles.sync(player);
    }

    /**
     * Developer jump to the start of Scene 6-11: earlier scenes are already marked complete by the caller. Resets the
     * changing parts of the sites and puts the player where the scene begins.
     */
    public static void jumpTo(ServerPlayer player, int scene) {
        PlayerProgress p = Profiles.get(player);
        leavePerspective(player);
        p.flags().removeIf(f -> f.startsWith(FLAG_STATE_PREFIX));
        p.flags().removeIf(f -> f.startsWith(FLAG_BARK_PREFIX + "b") || f.startsWith(FLAG_BARK_PREFIX + "d"));
        SESSIONS.remove(player.getUUID());
        var server = player.server;
        ServerLevel tva = server.getLevel(MarvelSites.TVA);
        if (tva != null) {
            DoomsdaySites.ensureTvaWing(tva);
            DoomsdaySites.resetTvaWing(tva);
            DoomsdaySites.prebuildRepairs(tva, scene >= 8, scene >= 9);
            for (String tag : List.of(TAG_MOBIUS_TVA, TAG_STAFF + "_agent")) {
                removeAll(tva, tag, t(new BlockPos(60, 0, 0)), 200);
            }
            for (int i = 0; i < REFUGEES.size(); i++) {
                removeAll(tva, own(player, TAG_REFUGEE + "_" + i), t(new BlockPos(60, 0, 0)), 220);
            }
            for (String g : List.of("standoff", "gallery", "side")) {
                clearAttackers(tva, player, g, t(new BlockPos(80, 0, 0)), false);
            }
        }
        ServerLevel ny = server.getLevel(MarvelSites.NEW_YORK);
        if (ny != null && scene >= 10) {
            DoomsdaySites.ensureNewYorkS11(ny);
            DoomsdaySites.resetAccident(ny);
        }
        switch (scene) {
            case 6, 7, 8, 9 -> {
                if (tva != null) {
                    BlockPos start = switch (scene) {
                        case 6 -> t(MarvelSites.TVA_MOBIUS).offset(0, 0, 3);
                        case 7 -> t(DoomsdaySites.RECEPTION_MOBIUS).offset(0, 0, 3);
                        case 8 -> t(DoomsdaySites.RECEPTION_MOBIUS).offset(2, 0, 5);
                        default -> t(DoomsdaySites.RALLY_4).offset(0, 0, 3);
                    };
                    if (scene == 6) {
                        npc(tva, MOBIUS, TAG_MOBIUS_TVA, t(MarvelSites.TVA_MOBIUS), 90f);
                    }
                    if (scene == 9) {
                        for (int i = 0; i < REFUGEES.size(); i++) {
                            BlockPos r = t(around(DoomsdaySites.RALLY_4, i));
                            NpcSpawner.spawnStoryNpc(tva, REFUGEES.get(i), own(player, TAG_REFUGEE + "_" + i), r, 180f);
                        }
                        DoomsdaySites.blockMobiusRoute(tva);
                        p.setFlag(FLAG_STATE_PREFIX + "mobius_status_separated_unknown");
                    }
                    if (scene >= 8) {
                        DoomsdaySites.setEvacGateway(tva, false);
                        DoomsdaySites.setRiftDoor(tva, true);
                        p.setFlag(FLAG_STATE_PREFIX + "peter_contact_request_pending");
                    }
                    player.teleportTo(tva, start.getX() + 0.5, start.getY(), start.getZ() + 0.5, 180f, 0f);
                }
            }
            case 10 -> {
                ServerLevel obs = server.getLevel(DoomsdaySites.WATCHERS);
                if (obs != null) {
                    Vec3 a = DoomsdaySites.watchersArrival(obs);
                    player.teleportTo(obs, a.x, a.y, a.z, 180f, 0f);
                }
                p.setFlag(FLAG_STATE_PREFIX + "mobius_status_separated_unknown");
                p.setFlag(FLAG_STATE_PREFIX + "refugees_safe");
            }
            default -> {
                p.setFlag(FLAG_STATE_PREFIX + "mobius_status_separated_unknown");
                p.setFlag(FLAG_STATE_PREFIX + "refugees_safe");
                p.setFlag(FLAG_STATE_PREFIX + "sanctuary_accepted");
                p.setFlag(FLAG_STATE_PREFIX + "steve_message_blocked");
            }
        }
        StoryService.start(player, DOOMSDAY.get(scene - 6));
        if (scene == 11) {
            Perspective.becomePeter(player);
        }
        Profiles.sync(player);
    }

    /** Used when jumping to an earlier scene: back to Steve, with his inventory, if playing as Spider-Man. */
    public static void leavePerspective(ServerPlayer player) {
        if (Profiles.get(player).perspectiveSkin() != null || state(player, "perspective_peter")) {
            Perspective.becomeSteve(player);
        }
    }

    // ================================================================== playing as Spider-Man (Scene 11)

    /**
     * The perspective switch to Spider-Man: Steve's inventory is put aside (saved with the world, so it survives a
     * reload or crash), the player gets Peter's web-shooters and scanner and is shown in his suit, and arrives in New
     * York. {@link #becomeSteve} reverses it exactly once. Both are safe to call repeatedly.
     */
    static final class Perspective {
        private Perspective() {
        }

        static void becomePeter(ServerPlayer player) {
            ServerLevel ny = player.server.getLevel(MarvelSites.NEW_YORK);
            if (ny == null) {
                return;
            }
            PerspectiveStash.stash(player);
            Profiles.get(player).setPerspectiveSkin(PETER_SKIN);
            setState(player, "perspective_peter");
            restoreKit(player);
            DoomsdaySites.ensureNewYorkS11(ny);
            BlockPos a = MarvelSites.ny(MarvelSites.NY_ARRIVAL).offset(0, 0, -2);
            player.teleportTo(ny, a.getX() + 0.5, a.getY(), a.getZ() + 0.5, 180f, 0f);
            player.fallDistance = 0;
            checkpoint(player, MarvelSites.NEW_YORK, a);
            Profiles.sync(player);
        }

        /** Peter's kit: web-shooters and scanner, given again if lost (never duplicated). */
        static void restoreKit(ServerPlayer player) {
            if (!state(player, "perspective_peter")) {
                return;
            }
            if (!has(player, MarvelItems.WEB_SHOOTERS.get())) {
                giveItem(player, new ItemStack(MarvelItems.WEB_SHOOTERS.get()));
            }
            if (!has(player, MarvelItems.PETER_SCANNER.get())) {
                giveItem(player, new ItemStack(MarvelItems.PETER_SCANNER.get()));
            }
        }

        static void becomeSteve(ServerPlayer player) {
            PlayerProgress p = Profiles.get(player);
            p.setPerspectiveSkin(null);
            p.clearFlag(FLAG_STATE_PREFIX + "perspective_peter");
            player.removeEffect(MobEffects.JUMP);
            player.removeEffect(MobEffects.MOVEMENT_SPEED);
            PerspectiveStash.restore(player);
            ServerLevel obs = player.server.getLevel(DoomsdaySites.WATCHERS);
            if (obs != null) {
                Vec3 a = DoomsdaySites.watchersArrival(obs);
                player.teleportTo(obs, a.x, a.y, a.z, 180f, 0f);
                checkpoint(player, DoomsdaySites.WATCHERS, BlockPos.containing(a));
            }
            Profiles.sync(player);
        }
    }
}
