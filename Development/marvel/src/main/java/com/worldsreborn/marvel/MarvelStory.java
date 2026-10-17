package com.worldsreborn.marvel;

import com.worldsreborn.characters.NpcSpawner;
import com.worldsreborn.characters.entity.HostileNpcEntity;
import com.worldsreborn.characters.entity.NpcInteractEvent;
import com.worldsreborn.characters.entity.StoryNpcEntity;
import com.worldsreborn.core.WRCore;
import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.model.ProgressResult;
import com.worldsreborn.core.model.QuestDef;
import com.worldsreborn.core.model.QuestState;
import com.worldsreborn.core.model.StepDef;
import com.worldsreborn.core.model.UniverseInfo;
import com.worldsreborn.core.model.WRContent;
import com.worldsreborn.core.profile.Profiles;
import com.worldsreborn.marvel.item.MarvelItems;
import com.worldsreborn.story.StoryService;
import com.worldsreborn.story.event.DialogueFinishedEvent;
import com.worldsreborn.story.event.QuestProgressEvent;
import com.worldsreborn.travel.TravelService;
import com.worldsreborn.travel.WRTravel;
import com.worldsreborn.travel.event.PortalContactEvent;
import com.worldsreborn.travel.event.TravelArrivedEvent;
import com.worldsreborn.worlds.WorldSites;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.worldsreborn.marvel.MarvelIds.*;

/**
 * Runs Act 1, Scenes 1-5. Every handler is idempotent: {@link #setup} looks at the player's current scene and step and
 * makes the world match it (targets, characters), so logging out, dying or reloading at any point resumes cleanly.
 */
public final class MarvelStory {
    private static final UUID ROBBERY = UUID.nameUUIDFromBytes("wrmarvel:shop_robbery".getBytes());
    /** Players waiting for the opening cinematic (sent a moment after joining so the client is ready). */
    private static final Map<UUID, Integer> INTRO_DELAY = new ConcurrentHashMap<>();
    private static final int NEARBY = 80;
    /** Short pauses before a dialogue opens by itself (someone arriving, a door opening): player -> what and when. */
    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();

    private record Pending(int ticks, String dialogueId, String questId, String stepId) {
    }

    private MarvelStory() {
    }

    // ================================================================== current scene

    private record Current(QuestDef quest, StepDef step, QuestState state) {
        boolean is(String questId, String stepId) {
            return quest.id().equals(questId) && step.id().equals(stepId);
        }
    }

    private static Current current(ServerPlayer player) {
        PlayerProgress p = Profiles.get(player);
        for (String id : SCENES) {
            if (p.isQuestActive(id)) {
                QuestDef q = StoryService.quest(id);
                StepDef s = p.currentStep(q);
                if (s != null) {
                    return new Current(q, s, p.questState(id));
                }
            }
        }
        return null;
    }

    private static boolean on(ServerPlayer player, String questId, String stepId) {
        return StoryService.isOnStep(player, questId, stepId);
    }

    private static boolean in(ServerPlayer player, ResourceKey<Level> dim) {
        return player.level().dimension() == dim;
    }

    private static boolean near(ServerPlayer player, ResourceKey<Level> dim, BlockPos pos) {
        return in(player, dim) && player.blockPosition().closerThan(pos, NEARBY);
    }

    // ================================================================== setup

    /** Makes targets and characters match the player's current step. Safe to call any time. */
    public static void setup(ServerPlayer player) {
        Current c = current(player);
        if (c == null) {
            return;
        }
        var server = player.server;
        ServerLevel overworld = server.overworld();
        String q = c.quest.id();
        String step = c.step.id();
        ensureVillagers(player);

        if (q.equals(S01)) {
            BlockPos cartographer = MarvelSites.village(server, MarvelSites.V_CARTOGRAPHER);
            if (near(player, Level.OVERWORLD, cartographer)) {
                ensureNpc(overworld, CARTOGRAPHER, TAG_CARTOGRAPHER, cartographer, 90f);
            }
            switch (step) {
                case S01_RETURN_VILLAGE -> target(player, Level.OVERWORLD, MarvelSites.villageOrigin(server), 7);
                case S01_TALK_CARTOGRAPHER -> target(player, Level.OVERWORLD, cartographer, 3);
                case S01_REACH_RUINS -> target(player, Level.OVERWORLD, MarvelSites.ruins(server, MarvelSites.R_ENTRANCE), 6);
                default -> {
                }
            }
        } else if (q.equals(S02)) {
            BlockPos ruins = MarvelSites.ruinsOrigin(server);
            switch (step) {
                case S02_ENTER_CHAMBER -> target(player, Level.OVERWORLD, ruins.offset(MarvelSites.R_CHAMBER), 4);
                case S02_REPAIR_REDSTONE -> target(player, Level.OVERWORLD, ruins.offset(MarvelSites.R_CORE), 2);
                case S02_ENTER_PORTAL -> {
                    if (!MarvelSites.portalIsLit(overworld, ruins)) {
                        MarvelSites.lightPortal(overworld, ruins);
                    }
                    target(player, Level.OVERWORLD, ruins.offset(MarvelSites.R_PORTAL), 1);
                }
                default -> {
                }
            }
        } else if (q.equals(S03)) {
            ServerLevel ny = server.getLevel(MarvelSites.NEW_YORK);
            if (ny == null) {
                return;
            }
            target(player, MarvelSites.NEW_YORK, MarvelSites.ny(MarvelSites.NY_SHOP_DOOR), 4);
            if (near(player, MarvelSites.NEW_YORK, MarvelSites.ny(MarvelSites.NY_SHOP_DOOR))) {
                MarvelSites.ensureNewYork(ny);
                MarvelSites.placeTrace(ny);
                boolean robbery = step.equals(S03_STOP_ROBBERS);
                ensureCustomers(ny, robbery);
                if (robbery) {
                    ensureRobbers(ny, c.step.required() - c.state.count);
                    if (c.state.count > 0) {
                        // After the first robber goes down, Spider-Man drops in and joins the fight.
                        StoryNpcEntity spidey = ensureNpc(ny, SPIDERMAN, TAG_SPIDERMAN, MarvelSites.ny(MarvelSites.NY_SPIDERMAN_DROP), 180f);
                        if (spidey != null) {
                            spidey.setFighter(true);
                        }
                    }
                } else {
                    StoryNpcEntity spidey = ensureNpc(ny, SPIDERMAN, TAG_SPIDERMAN, MarvelSites.ny(MarvelSites.NY_SPIDERMAN_DROP), 180f);
                    if (spidey != null) {
                        spidey.setFighter(false);
                    }
                }
            }
        } else if (q.equals(S04)) {
            ServerLevel ny = server.getLevel(MarvelSites.NEW_YORK);
            if (ny == null) {
                return;
            }
            BlockPos trace = MarvelSites.ny(MarvelSites.NY_ROOF_TRACE);
            target(player, MarvelSites.NEW_YORK, step.equals(S04_BACK_TO_TRACE) ? trace : MarvelSites.ny(MarvelSites.NY_ROOF_SPIDEY), 3);
            if (near(player, MarvelSites.NEW_YORK, trace)) {
                MarvelSites.ensureRooftop(ny);
                ensureCustomers(ny, false);
                if (!step.equals(S04_PETER_LEAVES)) {
                    MarvelSites.placeRoofTrace(ny);
                }
                rooftopSpiderMan(ny);
                if (step.equals(S04_PETER_LEAVES) && Profiles.get(player).hasFlag(FLAG_S04_DOOR)) {
                    openTvaDoor(ny);
                    ensureAgents(ny);
                }
            }
        } else if (q.equals(S05)) {
            switch (step) {
                case S05_MEET_MOBIUS -> {
                    ServerLevel ny = server.getLevel(MarvelSites.NEW_YORK);
                    BlockPos mobius = MarvelSites.ny(MarvelSites.NY_ROOF_MOBIUS);
                    target(player, MarvelSites.NEW_YORK, mobius, 3);
                    if (ny != null && near(player, MarvelSites.NEW_YORK, mobius)) {
                        MarvelSites.ensureRooftop(ny);
                        openTvaDoor(ny);
                        ensureAgents(ny);
                        rooftopSpiderMan(ny);
                        if (ny.isPositionEntityTicking(mobius) && NpcSpawner.findStoryNpcs(ny, TAG_MOBIUS, mobius, 96).isEmpty()) {
                            // Mobius steps out of the time door.
                            ny.sendParticles(ParticleTypes.FLAME, mobius.getX() + 0.5, mobius.getY() + 1, mobius.getZ() + 0.5, 40, 0.4, 1.0, 0.4, 0.02);
                            if (ensureNpc(ny, MOBIUS, TAG_MOBIUS, mobius, 0f) != null) {
                                PENDING.put(player.getUUID(), new Pending(30, D05_MOBIUS, S05, S05_MEET_MOBIUS));
                            }
                        } else {
                            ensureNpc(ny, MOBIUS, TAG_MOBIUS, mobius, 0f);
                        }
                    }
                }
                case S05_FIND_RECORDS, S05_READ_RECORDS -> {
                    ServerLevel tva = server.getLevel(MarvelSites.TVA);
                    BlockPos records = MarvelSites.tva(step.equals(S05_FIND_RECORDS) ? MarvelSites.TVA_RECORDS : MarvelSites.TVA_TERMINAL);
                    target(player, MarvelSites.TVA, records, 3);
                    if (tva != null && near(player, MarvelSites.TVA, records)) {
                        MarvelSites.ensureTva(tva);
                        ensureNpc(tva, MOBIUS, TAG_MOBIUS_TVA, MarvelSites.tva(MarvelSites.TVA_MOBIUS), 90f);
                        ensureGuards(tva);
                    } else if (tva != null && step.equals(S05_FIND_RECORDS) && in(player, MarvelSites.TVA)
                            && !Profiles.get(player).hasFlag(FLAG_S05_ARRIVAL)) {
                        // Mobius meets Steve in the arrival hall, then walks him to the records room.
                        MarvelSites.ensureTva(tva);
                        BlockPos greet = MarvelSites.tva(MarvelSites.TVA_MOBIUS_GREET);
                        StoryNpcEntity mobius = ensureNpc(tva, MOBIUS, TAG_MOBIUS_TVA, greet, 180f);
                        if (mobius != null && !mobius.blockPosition().closerThan(greet, 4)) {
                            mobius.moveTo(greet.getX() + 0.5, greet.getY(), greet.getZ() + 0.5, 180f, 0f);
                        }
                        if (mobius != null && !PENDING.containsKey(player.getUUID())) {
                            PENDING.put(player.getUUID(), new Pending(30, D05_ARRIVAL, S05, S05_FIND_RECORDS));
                        }
                    }
                }
                default -> {
                }
            }
        }
    }

    private static void target(ServerPlayer player, ResourceKey<Level> dim, BlockPos pos, double radius) {
        Current c = current(player);
        if (c == null) {
            return;
        }
        var t = Profiles.get(player).target();
        if (t != null && t.questId().equals(c.quest.id()) && t.stepId().equals(c.step.id())
                && t.location().dimension().equals(dim.location().toString())
                && (int) Math.floor(t.location().x()) == pos.getX() && (int) Math.floor(t.location().z()) == pos.getZ()) {
            return;
        }
        StoryService.setTarget(player, c.quest.id(), c.step.id(), dim, pos, radius);
    }

    /**
     * One NPC per tag near {@code pos}: spawns it if missing, removes duplicates. Returns null while the spot's chunk
     * isn't fully loaded yet: entities there can't be seen, so spawning would make duplicates (setup runs again soon).
     */
    @org.jetbrains.annotations.Nullable
    private static StoryNpcEntity ensureNpc(ServerLevel level, String characterId, String tag, BlockPos pos, float yaw) {
        if (!level.isPositionEntityTicking(pos)) {
            return null;
        }
        List<StoryNpcEntity> found = NpcSpawner.findStoryNpcs(level, tag, pos, 96);
        if (found.isEmpty()) {
            return NpcSpawner.spawnStoryNpc(level, characterId, tag, pos, yaw);
        }
        for (int i = 1; i < found.size(); i++) {
            found.get(i).discard();
        }
        StoryNpcEntity npc = found.get(0);
        if (!npc.isFighter() && !npc.isWalking() && !npc.blockPosition().closerThan(pos, 24)) {
            // Left over from an older layout of the site: move it to its new spot.
            npc.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0f);
        }
        return npc;
    }

    /** Background villagers going about their day while a player is near the village. */
    private static void ensureVillagers(ServerPlayer player) {
        WorldSites sites = WorldSites.get(player.server);
        if (!in(player, Level.OVERWORLD) || !sites.has(SITE_VILLAGE)) {
            return;
        }
        BlockPos village = MarvelSites.villageOrigin(player.server);
        if (!near(player, Level.OVERWORLD, village)) {
            return;
        }
        ServerLevel overworld = player.server.overworld();
        for (int i = 0; i < VILLAGERS.size(); i++) {
            BlockPos home = village.offset(MarvelSites.V_VILLAGERS.get(i));
            String tag = TAG_VILLAGER + "_" + i;
            if (overworld.isPositionEntityTicking(home) && NpcSpawner.findStoryNpcs(overworld, tag, home, 48).isEmpty()) {
                NpcSpawner.spawnStoryNpc(overworld, VILLAGERS.get(i)[0], tag, home, i * 60f).setAmbient(true, 8);
            }
        }
    }

    /** TVA guards standing at their posts. */
    private static void ensureGuards(ServerLevel tva) {
        for (int i = 0; i < MarvelSites.TVA_GUARDS.size(); i++) {
            int[] g = MarvelSites.TVA_GUARDS.get(i);
            BlockPos post = MarvelSites.tva(new BlockPos(g[0], 0, g[1]));
            StoryNpcEntity guard = ensureNpc(tva, TVA_GUARD, TAG_TVA_GUARD + "_" + i, post, g[2]);
            if (guard != null && !guard.isAmbient()) {
                guard.setAmbient(true, 0);
            }
        }
    }

    private static void ensureCustomers(ServerLevel ny, boolean cowering) {
        for (int i = 0; i < CIVILIANS.size(); i++) {
            StoryNpcEntity npc = ensureNpc(ny, CIVILIANS.get(i), TAG_CUSTOMER + "_" + i, MarvelSites.ny(MarvelSites.NY_CUSTOMERS.get(i)), 0f);
            if (npc != null && npc.isCowering() != cowering) {
                npc.setCowering(cowering);
            }
        }
    }

    /** Spider-Man waits on the shop roof in Scenes 4-5; if he is still down on the street he web-zips up. */
    private static void rooftopSpiderMan(ServerLevel ny) {
        BlockPos spot = MarvelSites.ny(MarvelSites.NY_ROOF_SPIDEY);
        StoryNpcEntity peter = ensureNpc(ny, SPIDERMAN, TAG_SPIDERMAN, spot, 200f);
        if (peter == null) {
            return;
        }
        peter.setFighter(false);
        if (!peter.blockPosition().closerThan(spot, 3)) {
            ny.sendParticles(ParticleTypes.CLOUD, peter.getX(), peter.getY() + 1, peter.getZ(), 12, 0.3, 0.6, 0.3, 0.02);
            peter.moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, 200f, 0f);
            ny.playSound(null, spot, SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 1f, 0.7f);
        }
    }

    /** The two TVA agents who step out of the time door. */
    private static void ensureAgents(ServerLevel ny) {
        for (int i = 0; i < MarvelSites.NY_ROOF_AGENTS.size(); i++) {
            StoryNpcEntity agent = ensureNpc(ny, TVA_GUARD, TAG_TVA_AGENT + "_" + i, MarvelSites.ny(MarvelSites.NY_ROOF_AGENTS.get(i)), 20f - i * 40f);
            if (agent != null && !agent.isAmbient()) {
                agent.setAmbient(true, 0);
            }
        }
    }

    private static void openTvaDoor(ServerLevel ny) {
        if (MarvelSites.setTvaDoor(ny, true)) {
            BlockPos door = MarvelSites.ny(MarvelSites.NY_ROOF_DOOR);
            ny.playSound(null, door, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.2f, 0.6f);
            ny.playSound(null, door, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1f, 0.8f);
            ny.sendParticles(ParticleTypes.FLAME, door.getX() + 0.5, door.getY() + 2, door.getZ() + 0.5, 60, 1.2, 1.5, 0.3, 0.02);
        }
    }

    private static void closeTvaRooftop(ServerLevel ny) {
        BlockPos door = MarvelSites.ny(MarvelSites.NY_ROOF_DOOR);
        MarvelSites.setTvaDoor(ny, false);
        for (int i = 0; i < MarvelSites.NY_ROOF_AGENTS.size(); i++) {
            NpcSpawner.findStoryNpcs(ny, TAG_TVA_AGENT + "_" + i, door, 48).forEach(StoryNpcEntity::vanish);
        }
        NpcSpawner.findStoryNpcs(ny, TAG_MOBIUS, door, 48).forEach(StoryNpcEntity::vanish);
    }

    // ================================================================== spoken lines

    /** Script line id ("S002_005") for scene and line numbers. */
    private static String lineId(int scene, int line) {
        return String.format(Locale.ROOT, "S%03d_%03d", scene, line);
    }

    /**
     * Speaks script lines {@code from..to} of a scene as voiced subtitles, in order. Each run plays once per player
     * (remembered by a flag on its first line), so walking back and forth doesn't repeat it.
     */
    static boolean bark(ServerPlayer player, int scene, int from, int to) {
        if (!Profiles.get(player).setFlag(FLAG_BARK_PREFIX + lineId(scene, from))) {
            return false;
        }
        for (int i = from; i <= to; i++) {
            String id = lineId(scene, i);
            StoryService.say(player, "speaker.wrmarvel." + MarvelScript.SPEAKER.get(id), "line.wrmarvel." + id.toLowerCase(Locale.ROOT),
                    MarvelContent.voice(id));
        }
        return true;
    }

    /** Ticks it takes to speak lines {@code from..to} of a scene, with the short gaps between subtitles. */
    private static int speakingTicks(int scene, int from, int to) {
        int ms = 0;
        for (int i = from; i <= to; i++) {
            ms += MarvelScript.MILLIS.getOrDefault(lineId(scene, i), 2000) + 300;
        }
        return ms / 50;
    }

    /** Called when a survey map is used: Steve's line about it, then the map itself. */
    public static void onSurveyRead(ServerPlayer player, boolean newSurvey) {
        PlayerProgress p = Profiles.get(player);
        if (newSurvey) {
            // The route line follows the first look at the new survey.
            p.clearFlag(FLAG_BARK_PREFIX + lineId(1, 25));
            bark(player, 1, 25, 26);
            StoryService.infoPanel(player, "map:wrmarvel:textures/gui/new_survey.png", "item.wrmarvel.new_survey",
                    List.of("panel.wrmarvel.survey.new"));
        } else {
            p.clearFlag(FLAG_BARK_PREFIX + lineId(1, 24));
            bark(player, 1, 24, 24);
            StoryService.infoPanel(player, "map:wrmarvel:textures/gui/old_survey.png", "item.wrmarvel.old_survey",
                    List.of("panel.wrmarvel.survey.old"));
        }
    }

    /** Lines Steve says on his own as he reaches places (Scenes 2-3). Checked twice a second. */
    private static void placeBarks(ServerPlayer player) {
        Current c = current(player);
        if (c == null) {
            return;
        }
        var server = player.server;
        if (c.quest.id().equals(S02) && in(player, Level.OVERWORLD)) {
            BlockPos ruins = MarvelSites.ruinsOrigin(server);
            BlockPos at = player.blockPosition();
            if (c.step.id().equals(S02_ENTER_CHAMBER) && at.closerThan(ruins.offset(0, 0, 22), 5)) {
                bark(player, 2, 3, 4);
            } else if (c.step.id().equals(S02_REPAIR_REDSTONE) && at.closerThan(ruins.offset(MarvelSites.R_CORE), 5)) {
                bark(player, 2, 7, 9);
            } else if (c.step.id().equals(S02_ENTER_PORTAL) && at.closerThan(ruins.offset(MarvelSites.R_PORTAL), 6)) {
                bark(player, 2, 13, 15);
            }
        } else if (c.quest.id().equals(S03) && c.step.id().equals(S03_STOP_ROBBERS) && in(player, MarvelSites.NEW_YORK)) {
            BlockPos at = player.blockPosition();
            BlockPos origin = MarvelSites.NY_ORIGIN;
            if (at.getZ() < origin.getZ() + 9 && !Profiles.get(player).hasFlag(FLAG_BARK_PREFIX + lineId(3, 4))) {
                // Out of the alley and onto the street: a car horn.
                player.serverLevel().playSound(null, at.offset(-6, 0, 0), SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.NEUTRAL, 2f, 1.2f);
                player.serverLevel().playSound(null, at.offset(-6, 0, 0), SoundEvents.NOTE_BLOCK_BIT.value(), SoundSource.NEUTRAL, 2f, 0.6f);
                bark(player, 3, 4, 7);
            }
            BlockPos shop = MarvelSites.ny(MarvelSites.NY_SHOP_DOOR);
            List<HostileNpcEntity> robbers = NpcSpawner.findHostiles(player.serverLevel(), TAG_ROBBER, shop, 64);
            if (at.closerThan(shop, 12) && bark(player, 3, 8, 15)) {
                // The robbers hold off until the whole exchange has been said ("Get him!" ... "Bad idea.").
                int ticks = speakingTicks(3, 8, 15) + 10;
                robbers.forEach(r -> r.hold(ticks));
            } else if (Profiles.get(player).hasFlag(FLAG_BARK_PREFIX + lineId(3, 8))) {
                // Already said (e.g. after a reload): nothing to wait for.
                robbers.stream().filter(HostileNpcEntity::isHeldUntilReleased).forEach(HostileNpcEntity::release);
            }
        }
    }

    /**
     * Spawns the robbery only when no robber is left anywhere nearby (fresh start, or after a reload lost them).
     * Robbers that chased the player out of the shop still count, so the fight never refills mid-way.
     */
    private static void ensureRobbers(ServerLevel ny, int remaining) {
        BlockPos shop = MarvelSites.ny(MarvelSites.NY_SHOP_DOOR);
        if (remaining <= 0 || !ny.isPositionEntityTicking(shop) || !NpcSpawner.findHostiles(ny, TAG_ROBBER, shop, 200).isEmpty()) {
            return;
        }
        for (int i = 0; i < remaining && i < MarvelSites.NY_ROBBERS.size(); i++) {
            HostileNpcEntity robber = NpcSpawner.spawnHostile(ny, ROBBER, TAG_ROBBER, ROBBERY, MarvelSites.ny(MarvelSites.NY_ROBBERS.get(i)), 180f);
            if (robber != null) {
                robber.hold(-1); // busy with the shop until Steve walks in and the exchange is over
            }
        }
    }

    // ================================================================== events

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PlayerProgress p = Profiles.get(player);
        if (!p.isQuestStarted(S01)) {
            StoryService.start(player, S01);
        } else {
            setup(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            setup(player);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            setup(player);
        }
    }

    @SubscribeEvent
    public static void onQuestProgress(QuestProgressEvent event) {
        ServerPlayer player = event.getPlayer();
        QuestDef q = event.getQuest();
        if (!SCENES.contains(q.id())) {
            return;
        }
        if (event.isQuestStart() && q.id().equals(S01)) {
            MarvelSites.villageOrigin(player.server);
            INTRO_DELAY.put(player.getUUID(), 40);
        }
        // Build the big places ahead of time, while the player is busy elsewhere, so arriving doesn't stall.
        if (event.isQuestStart() && q.id().equals(S02)) {
            ServerLevel ny = player.server.getLevel(MarvelSites.NEW_YORK);
            if (ny != null) {
                MarvelSites.ensureNewYork(ny);
            }
        }
        if (event.isQuestStart() && q.id().equals(S05)) {
            ServerLevel tva = player.server.getLevel(MarvelSites.TVA);
            if (tva != null) {
                MarvelSites.ensureTva(tva);
            }
        }
        if (event.isQuestStart() && q.id().equals(S02)) {
            bark(player, 2, 1, 2);
        }
        if (q.id().equals(S02) && S02_ENTER_CHAMBER.equals(event.getStepId()) && event.getResult() == ProgressResult.STEP_COMPLETED) {
            bark(player, 2, 5, 6);
        }
        if (q.id().equals(S03) && S03_STOP_ROBBERS.equals(event.getStepId())) {
            if (event.getResult() == ProgressResult.STEP_COMPLETED) {
                // Robbers stopped.
                ServerLevel ny = player.server.getLevel(MarvelSites.NEW_YORK);
                if (ny != null) {
                    ensureCustomers(ny, false);
                    ny.playSound(null, MarvelSites.ny(MarvelSites.NY_SHOP_DOOR), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL, 1f, 0.6f);
                }
                bark(player, 3, 21, 21);
            } else {
                // First robber down: Spider-Man swings in.
                bark(player, 3, 16, 20);
            }
        }
        if (q.id().equals(S04) && event.isQuestStart()) {
            player.displayClientMessage(Component.translatable("message.wrmarvel.climb_to_roof"), true);
        }
        if (q.id().equals(S05) && S05_FIND_RECORDS.equals(event.getStepId()) && event.getResult() == ProgressResult.STEP_COMPLETED
                && !Profiles.get(player).hasFlag(FLAG_S05_INTAKE)) {
            PENDING.put(player.getUUID(), new Pending(20, D05_INTAKE, S05, S05_READ_RECORDS));
        }
        if (q.id().equals(S05) && event.getResult() == ProgressResult.QUEST_COMPLETED) {
            finishMilestone(player);
        }
        setup(player);
    }

    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 10 != 0) {
            return;
        }
        Integer delay = INTRO_DELAY.get(player.getUUID());
        if (delay != null) {
            if (delay <= 10) {
                INTRO_DELAY.remove(player.getUUID());
                wakeInBed(player);
            } else {
                INTRO_DELAY.put(player.getUUID(), delay - 10);
            }
        }
        if (on(player, S02, S02_REPAIR_REDSTONE) && in(player, Level.OVERWORLD)) {
            checkRedstone(player);
        }
        placeBarks(player);
        Pending pending = PENDING.get(player.getUUID());
        if (pending != null) {
            PlayerProgress prog = Profiles.get(player);
            if (!on(player, pending.questId(), pending.stepId())) {
                PENDING.remove(player.getUUID());
            } else if (prog.pendingDialogue() != null || prog.activeCinematic() != null) {
                // Wait: never open over another conversation or a cinematic.
            } else if (pending.ticks() <= 10) {
                PENDING.remove(player.getUUID());
                StoryService.openDialogue(player, pending.dialogueId());
            } else {
                PENDING.put(player.getUUID(), new Pending(pending.ticks() - 10, pending.dialogueId(), pending.questId(), pending.stepId()));
            }
        }
        if (player.tickCount % 100 == 0) {
            setup(player);
        }
    }

    /** Scene 1 opening: Steve is put in his own bed and dreams; the dream ends with him waking up there. */
    private static void wakeInBed(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        BlockPos village = MarvelSites.villageOrigin(player.server);
        BlockPos wake = village.offset(MarvelSites.V_STEVE_WAKE);
        player.teleportTo(overworld, wake.getX() + 0.5, wake.getY(), wake.getZ() + 0.5, -90f, 10f);
        player.setRespawnPosition(Level.OVERWORLD, village.offset(MarvelSites.V_STEVE_BED), -90f, true, false);
        StoryService.playCinematic(player, CIN_DREAM, CIN_DREAM_TICKS);
    }

    @SubscribeEvent
    public static void onCinematicFinished(com.worldsreborn.story.event.CinematicFinishedEvent event) {
        if (CIN_DREAM.equals(event.getCinematicId())) {
            ServerPlayer player = event.getPlayer();
            player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 0.6f, 0.8f);
            StoryService.playCinematic(player, CIN_INTRO, CIN_INTRO_TICKS);
        }
    }

    private static void checkRedstone(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        BlockPos ruins = MarvelSites.ruinsOrigin(player.server);
        if (!player.blockPosition().closerThan(ruins.offset(MarvelSites.R_CORE), 32)) {
            return;
        }
        WorldSites sites = WorldSites.get(player.server);
        boolean alreadyLit = sites.hasFlag(WORLD_FLAG_PORTAL_LIT);
        if (!alreadyLit && !MarvelSites.coreIsPowered(overworld, ruins)) {
            return;
        }
        if (!MarvelSites.portalIsLit(overworld, ruins)) {
            MarvelSites.lightPortal(overworld, ruins);
            BlockPos portal = ruins.offset(MarvelSites.R_PORTAL);
            overworld.playSound(null, portal, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.8f, 1.4f);
            overworld.sendParticles(ParticleTypes.END_ROD, portal.getX() + 0.5, portal.getY() + 0.5, portal.getZ() + 0.5, 60, 1.2, 1.2, 0.3, 0.05);
        }
        sites.setFlag(WORLD_FLAG_PORTAL_LIT);
        bark(player, 2, 10, 12);
        StoryService.progress(player, S02, S02_REPAIR_REDSTONE, 1);
    }

    @SubscribeEvent
    public static void onNpcInteract(NpcInteractEvent event) {
        ServerPlayer player = event.getPlayer();
        String character = event.getCharacterId();
        if (CARTOGRAPHER.equals(character) && on(player, S01, S01_TALK_CARTOGRAPHER)) {
            StoryService.openDialogue(player, D01_CARTOGRAPHER);
        } else if (SPIDERMAN.equals(character)) {
            if (on(player, S03, S03_MEET_SPIDERMAN)) {
                StoryService.openDialogue(player, D03_SPIDERMAN);
            } else if (on(player, S04, S04_SCAN_TRACE)) {
                StoryService.openDialogue(player, D04_SCAN);
            } else if (on(player, S04, S04_PETER_LEAVES)) {
                open(player, Profiles.get(player).hasFlag(FLAG_S04_DOOR) ? D04_TVA : D04_HEAR);
            } else if (on(player, S04, S04_BACK_TO_TRACE)) {
                player.displayClientMessage(Component.translatable("message.wrmarvel.climb_to_roof"), true);
            }
        } else if (TVA_GUARD.equals(character) && on(player, S04, S04_PETER_LEAVES) && Profiles.get(player).hasFlag(FLAG_S04_DOOR)) {
            open(player, D04_TVA);
        } else if (MOBIUS.equals(character)) {
            if (on(player, S05, S05_MEET_MOBIUS)) {
                open(player, D05_MOBIUS);
            } else if (on(player, S05, S05_FIND_RECORDS) && !Profiles.get(player).hasFlag(FLAG_S05_ARRIVAL)) {
                open(player, D05_ARRIVAL);
            } else if (on(player, S05, S05_READ_RECORDS) && !Profiles.get(player).hasFlag(FLAG_S05_INTAKE)) {
                open(player, D05_INTAKE);
            }
        }
    }

    /** Opens a story conversation unless one is already showing; it replaces any matching auto-open. */
    private static void open(ServerPlayer player, String dialogueId) {
        Pending pending = PENDING.get(player.getUUID());
        if (pending != null && pending.dialogueId().equals(dialogueId)) {
            PENDING.remove(player.getUUID());
        }
        String showing = Profiles.get(player).pendingDialogue();
        if (showing != null && !showing.equals(dialogueId)) {
            return;
        }
        StoryService.openDialogue(player, dialogueId);
    }

    @SubscribeEvent
    public static void onDialogueFinished(DialogueFinishedEvent event) {
        ServerPlayer player = event.getPlayer();
        String id = event.getDialogueId();
        if (id.equals(D01_CARTOGRAPHER)) {
            // The cartographer hands over both surveys; using one unrolls it.
            StoryService.grantOnce(player, REWARD_SURVEYS, () -> {
                giveItem(player, new ItemStack(MarvelItems.OLD_SURVEY.get()));
                giveItem(player, new ItemStack(MarvelItems.NEW_SURVEY.get()));
            });
            player.displayClientMessage(Component.translatable("message.wrmarvel.surveys_given"), false);
            StoryService.progress(player, S01, S01_TALK_CARTOGRAPHER, 1);
            // The hooded traveller watches from the edge of the village, gone the moment Steve looks.
            spawnTraveller(player, MarvelSites.village(player.server, MarvelSites.V_TRAVELLER));
        } else if (id.equals(D03_SPIDERMAN)) {
            StoryService.progress(player, S03, S03_MEET_SPIDERMAN, 1);
        } else if (id.equals(D04_SCAN)) {
            if (!on(player, S04, S04_SCAN_TRACE)) {
                return;
            }
            // The signal is gone: the trace fades out.
            ServerLevel ny = player.serverLevel();
            BlockPos trace = MarvelSites.ny(MarvelSites.NY_ROOF_TRACE);
            if (in(player, MarvelSites.NEW_YORK)) {
                MarvelSites.removeRoofTrace(ny);
                ny.sendParticles(ParticleTypes.REVERSE_PORTAL, trace.getX() + 0.5, trace.getY() + 0.5, trace.getZ() + 0.5, 50, 0.4, 0.6, 0.4, 0.02);
                ny.playSound(null, trace, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.8f, 1.3f);
            }
            StoryService.infoPanel(player, "scanner", "panel.wrmarvel.scanner.title", List.of(
                    "panel.wrmarvel.scanner.target", "panel.wrmarvel.scanner.signal", "panel.wrmarvel.scanner.source"));
            StoryService.progress(player, S04, S04_SCAN_TRACE, 1);
            // A low hum from across the roof: Steve hears it once the scanner read-out is closed.
            BlockPos door = MarvelSites.ny(MarvelSites.NY_ROOF_DOOR);
            ny.playSound(null, door, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 1.5f, 0.5f);
            ny.playSound(null, door, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.6f, 0.6f);
            PENDING.put(player.getUUID(), new Pending(20, D04_HEAR, S04, S04_PETER_LEAVES));
        } else if (id.equals(D04_HEAR)) {
            if (!on(player, S04, S04_PETER_LEAVES) || !in(player, MarvelSites.NEW_YORK)) {
                return;
            }
            // "Another portal?!": the time door opens and the agents step through, then they speak.
            Profiles.get(player).setFlag(FLAG_S04_DOOR);
            ServerLevel ny = player.serverLevel();
            openTvaDoor(ny);
            ensureAgents(ny);
            PENDING.put(player.getUUID(), new Pending(30, D04_TVA, S04, S04_PETER_LEAVES));
        } else if (id.equals(D04_TVA)) {
            StoryService.progress(player, S04, S04_PETER_LEAVES, 1);
        } else if (id.equals(D05_MOBIUS)) {
            if (on(player, S05, S05_MEET_MOBIUS)
                    && TravelService.beginTransit(player, DEST_TVA, CIN_TVA_DOOR, CIN_TVA_DOOR_TICKS)) {
                closeTvaRooftop(player.serverLevel());
            }
        } else if (id.equals(D05_ARRIVAL)) {
            if (!Profiles.get(player).setFlag(FLAG_S05_ARRIVAL) || !in(player, MarvelSites.TVA)) {
                return;
            }
            // Mobius leads the way to the records room.
            NpcSpawner.findStoryNpcs(player.serverLevel(), TAG_MOBIUS_TVA, player.blockPosition(), 48)
                    .forEach(m -> m.walkTo(MarvelSites.tva(MarvelSites.TVA_MOBIUS)));
            Profiles.sync(player);
        } else if (id.equals(D05_INTAKE)) {
            Profiles.get(player).setFlag(FLAG_S05_INTAKE);
            Profiles.sync(player);
            player.displayClientMessage(Component.translatable("message.wrmarvel.use_terminal"), true);
        } else if (id.equals(D05_ANALYSIS)) {
            if (on(player, S05, S05_READ_RECORDS)) {
                UiPanel.tvaScan(player);
                StoryService.progress(player, S05, S05_READ_RECORDS, 1);
            }
        }
    }

    private static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static void spawnTraveller(ServerPlayer player, BlockPos pos) {
        ServerLevel overworld = player.server.overworld();
        if (!in(player, Level.OVERWORLD)) {
            return;
        }
        NpcSpawner.findStoryNpcs(overworld, TAG_TRAVELLER, pos, 128).forEach(StoryNpcEntity::discard);
        BlockPos ground = com.worldsreborn.worlds.Placement.findSafe(overworld, pos, 4);
        StoryNpcEntity traveller = NpcSpawner.spawnStoryNpc(overworld, TRAVELLER, TAG_TRAVELLER, ground, 0f);
        traveller.setShy(true);
    }

    @SubscribeEvent
    public static void onPortalContact(PortalContactEvent event) {
        if (!DEST_NEW_YORK.equals(event.getDestinationId())) {
            return;
        }
        event.setHandled();
        ServerPlayer player = event.getPlayer();
        if (on(player, S02, S02_ENTER_PORTAL)) {
            TravelService.beginTransit(player, DEST_NEW_YORK, CIN_FALLING, CIN_FALLING_TICKS);
        } else if (TravelService.isUnlocked(player, TravelService.destination(DEST_NEW_YORK))) {
            TravelService.beginTransit(player, DEST_NEW_YORK, null, 40);
        } else {
            player.displayClientMessage(Component.translatable("message.wrmarvel.portal_unresponsive"), true);
        }
    }

    @SubscribeEvent
    public static void onArrived(TravelArrivedEvent event) {
        ServerPlayer player = event.getPlayer();
        String dest = event.getDestination().id();
        if (dest.equals(DEST_NEW_YORK)) {
            MarvelSites.placeTrace(player.serverLevel());
            if (on(player, S02, S02_ENTER_PORTAL)) {
                bark(player, 3, 1, 3);
            }
            StoryService.progress(player, S02, S02_ENTER_PORTAL, 1);
        } else if (dest.equals(DEST_TVA)) {
            StoryService.progress(player, S05, S05_MEET_MOBIUS, 1);
        }
        setup(player);
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof HostileNpcEntity robber) || !TAG_ROBBER.equals(robber.getStoryTag())
                || !(robber.level() instanceof ServerLevel level)) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(robber) < 48 * 48 && on(player, S03, S03_STOP_ROBBERS)) {
                StoryService.progress(player, S03, S03_STOP_ROBBERS, 1);
            }
        }
    }

    /** Called by the TVA records terminal. */
    public static void onTerminalUsed(ServerPlayer player) {
        PlayerProgress p = Profiles.get(player);
        boolean reading = on(player, S05, S05_READ_RECORDS);
        if (!reading && !p.isQuestCompleted(S05)) {
            player.displayClientMessage(Component.translatable("message.wrmarvel.terminal_locked"), true);
            return;
        }
        if (!reading) {
            UiPanel.tvaScan(player);
        } else if (!p.hasFlag(FLAG_S05_INTAKE)) {
            StoryService.openDialogue(player, D05_INTAKE);
        } else {
            StoryService.openDialogue(player, D05_ANALYSIS);
        }
    }

    /** End of the first milestone (Scenes 1-5). */
    private static void finishMilestone(ServerPlayer player) {
        PlayerProgress p = Profiles.get(player);
        p.setFlag(FLAG_MILESTONE_1);
        // Scene 6 follows straight on (the quest's next); free travel waits until the end of Scene 11.
        StoryService.toast(player, "info", "toast.wrmarvel.milestone.title", "toast.wrmarvel.milestone.detail", "");
        WRCore.LOGGER.info("[Worlds Reborn] {} finished Marvel Act 1 Scenes 1-5", player.getScoreboardName());
        Profiles.sync(player);
    }

    /** TVA records read-out. The origin line comes from the universe registry: MC-616 is not in the TVA's records. */
    static final class UiPanel {
        private UiPanel() {
        }

        static void tvaScan(ServerPlayer player) {
            UniverseInfo home = WRContent.universe(WRCore.MC_616);
            boolean registered = home != null && home.tvaRegistered();
            List<String> lines = new ArrayList<>(List.of("panel.wrmarvel.tva.subject", "panel.wrmarvel.tva.classification",
                    registered ? "panel.wrmarvel.tva.origin_registered" : "panel.wrmarvel.tva.origin_unregistered"));
            StoryService.infoPanel(player, "tva", "panel.wrmarvel.tva.title", lines);
        }
    }

    /** Developer helper: jump to the start of a scene (1-11). Earlier scenes are marked complete. */
    public static void jumpToScene(ServerPlayer player, int scene) {
        PlayerProgress p = Profiles.get(player);
        for (String id : SCENES) {
            p.removeQuest(id);
        }
        p.clearFlag(FLAG_MILESTONE_1);
        p.clearFlag(FLAG_MILESTONE_2);
        p.clearFlag(FLAG_MILESTONE_3);
        p.clearFlag(WRTravel.FREE_TRAVEL_FLAG);
        com.worldsreborn.marvel.doomsday.Doomsday.leavePerspective(player);
        p.clearFlag(FLAG_S05_INTAKE);
        p.clearFlag(FLAG_S05_ARRIVAL);
        p.clearFlag(FLAG_S04_DOOR);
        p.flags().removeIf(f -> f.startsWith(FLAG_BARK_PREFIX));
        PENDING.remove(player.getUUID());
        p.setTarget(null);
        for (int i = 0; i < scene - 1; i++) {
            p.forceComplete(StoryService.quest(SCENES.get(i)));
        }
        var server = player.server;
        if (scene >= 6) {
            com.worldsreborn.marvel.doomsday.Doomsday.jumpTo(player, scene);
            return;
        }
        switch (scene) {
            case 2 -> {
                BlockPos e = MarvelSites.ruins(server, MarvelSites.R_ENTRANCE);
                teleport(player, server.overworld(), Vec3.atBottomCenterOf(e));
            }
            case 3, 4, 5 -> {
                ServerLevel ny = server.getLevel(MarvelSites.NEW_YORK);
                if (ny != null) {
                    teleport(player, ny, MarvelSites.nyArrival(ny));
                    MarvelSites.placeTrace(ny);
                    if (scene > 3) {
                        MarvelSites.ensureRooftop(ny);
                        teleport(player, ny, Vec3.atBottomCenterOf(MarvelSites.ny(MarvelSites.NY_ROOF_TRACE).offset(0, 0, 3)));
                        NpcSpawner.findHostiles(ny, TAG_ROBBER, MarvelSites.ny(MarvelSites.NY_SHOP_DOOR), 64).forEach(HostileNpcEntity::discard);
                    }
                }
            }
            default -> {
                BlockPos v = MarvelSites.villageOrigin(server);
                teleport(player, server.overworld(), Vec3.atBottomCenterOf(v.offset(0, 0, -12)));
            }
        }
        StoryService.start(player, SCENES.get(scene - 1));
        Profiles.sync(player);
    }

    private static void teleport(ServerPlayer player, ServerLevel level, Vec3 pos) {
        BlockPos safe = com.worldsreborn.worlds.Placement.findSafe(level, BlockPos.containing(pos), 6);
        player.teleportTo(level, safe.getX() + 0.5, safe.getY(), safe.getZ() + 0.5, player.getYRot(), player.getXRot());
    }
}
