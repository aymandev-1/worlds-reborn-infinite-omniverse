package com.worldsreborn.marvel;

import java.util.List;

/** Every stable id used by the Marvel campaign. Never rename or reuse one: saves refer to them. */
public final class MarvelIds {
    private static final String NS = WRMarvel.MODID + ":";

    // Quests, one per scene. Steps are local ids inside each quest.
    public static final String S01 = NS + "s01_the_map";
    public static final String S01_RETURN_VILLAGE = "return_village";
    public static final String S01_TALK_CARTOGRAPHER = "talk_cartographer";
    public static final String S01_REACH_RUINS = "reach_ruins";

    public static final String S02 = NS + "s02_beneath_the_ruins";
    public static final String S02_ENTER_CHAMBER = "enter_chamber";
    public static final String S02_REPAIR_REDSTONE = "repair_redstone";
    public static final String S02_ENTER_PORTAL = "enter_portal";

    public static final String S03 = NS + "s03_stranger_in_new_york";
    public static final String S03_STOP_ROBBERS = "stop_robbers";
    public static final String S03_MEET_SPIDERMAN = "meet_spiderman";

    public static final String S04 = NS + "s04_the_fading_trace";
    public static final String S04_BACK_TO_TRACE = "back_to_trace";
    public static final String S04_SCAN_TRACE = "scan_trace";
    public static final String S04_PETER_LEAVES = "peter_leaves";

    public static final String S05 = NS + "s05_unrecorded_traveller";
    public static final String S05_MEET_MOBIUS = "meet_mobius";
    public static final String S05_FIND_RECORDS = "find_records";
    public static final String S05_READ_RECORDS = "read_records";

    // Act 1 "Doomsday", Scenes 6-11 (authored ids WR1_006..WR1_011). Each step is named after the checkpoint it
    // reaches when completed, so the saved step index is the scene's last checkpoint.
    public static final String S06 = NS + "wr1_006";
    public static final String S06_SEARCH = "s06_search_explained";
    public static final String S06_CROSSING = "s06_crossing_account_recorded";
    public static final String S06_IDENTIFY = "s06_traveller_identified";
    public static final String S06_OPERATIONS = "s06_operations_entered";
    public static final String S06_FEED = "s06_complete";

    public static final String S07 = NS + "wr1_007";
    public static final String S07_OFFER = "s07_help_offered";
    public static final String S07_ROUTE = "s07_route_cleared";
    public static final String S07_SUPPLIES = "s07_supplies_delivered";
    public static final String S07_REUNITE = "s07_family_reunited";
    public static final String S07_HOME = "s07_home_answered";
    public static final String S07_PENDING = "s07_message_pending";
    public static final String S07_DOORS = "s07_complete";

    public static final String S08 = NS + "wr1_008";
    public static final String S08_EVACUATE = "s08_evacuation_started";
    public static final String S08_COVER = "s08_cover_built";
    public static final String S08_BRIDGE = "s08_bridge_crossed";
    public static final String S08_INJURED = "s08_injured_survivor_safe";
    public static final String S08_MOBIUS = "s08_complete";

    public static final String S09 = NS + "wr1_009";
    public static final String S09_REFUGEES = "s09_refugees_safe";
    public static final String S09_BACKUP = "s09_backup_reached";
    public static final String S09_DOORWAY = "s09_unknown_gateway_entered";
    public static final String S09_ARRIVE = "s09_complete";

    public static final String S10 = NS + "wr1_010";
    public static final String S10_REFUGEES = "s10_refugees_confirmed";
    public static final String S10_FALLS = "s10_tva_falls_seen";
    public static final String S10_SANCTUARY = "s10_sanctuary_accepted";
    public static final String S10_HANDOVER = "s10_complete";

    public static final String S11 = NS + "wr1_011";
    public static final String S11_ALLEY = "s11_alley_inspected";
    public static final String S11_SCAN = "s11_scan_saved";
    public static final String S11_ACCIDENT = "s11_accident_rescue_complete";
    public static final String S11_SKY = "s11_sky_recorded";
    public static final String S11_MEETING = "s11_meeting_reached";
    public static final String S11_SHARE = "s11_complete";

    /** Scene order. The first active quest in this list is the player's current scene. */
    public static final List<String> SCENES = List.of(S01, S02, S03, S04, S05, S06, S07, S08, S09, S10, S11);

    // Dialogues
    public static final String D01_CARTOGRAPHER = NS + "d01_cartographer";
    public static final String D03_SPIDERMAN = NS + "d03_spiderman";
    public static final String D04_SCAN = NS + "d04_scan";
    public static final String D04_HEAR = NS + "d04_hear";
    public static final String D04_TVA = NS + "d04_tva";
    public static final String D05_MOBIUS = NS + "d05_mobius";
    public static final String D05_ARRIVAL = NS + "d05_arrival";
    public static final String D05_INTAKE = NS + "d05_intake";
    public static final String D05_ANALYSIS = NS + "d05_analysis";

    // Characters
    public static final String CARTOGRAPHER = NS + "cartographer";
    public static final String TRAVELLER = NS + "hooded_traveller";
    public static final String SPIDERMAN = NS + "tom_spiderman";
    public static final String MOBIUS = NS + "mobius";
    public static final String ROBBER = NS + "robber";
    public static final List<String> CIVILIANS = List.of(NS + "civilian_1", NS + "civilian_2", NS + "civilian_3");
    public static final String TVA_GUARD = NS + "tva_guard";
    public static final String TVA_STAFF = NS + "tva_staff";
    public static final String TVA_MEDIC = NS + "tva_medic";
    public static final String ATTACKER = NS + "unknown_attacker";
    public static final String UATU = NS + "uatu";
    public static final String WATCHER = NS + "watcher";
    public static final String DAREDEVIL = NS + "daredevil";
    public static final String JESSICA = NS + "jessica_jones";
    public static final String LUKE = NS + "luke_cage";
    public static final String DANNY = NS + "danny_rand";
    public static final String FRANK = NS + "frank_castle";
    public static final String BYSTANDER = NS + "bystander";
    /** The refugee group Steve escorts in Scenes 7-9 (same people again in Scene 10's view). */
    public static final List<String> REFUGEES = List.of(NS + "refugee_ana", NS + "refugee_jonah", NS + "refugee_teodor",
            NS + "refugee_mara", NS + "refugee_kip");
    /** Background villagers, each wearing a vanilla profession's clothes: {character id, profession}. */
    public static final List<String[]> VILLAGERS = List.of(
            new String[]{NS + "villager_farmer", "minecraft:farmer"},
            new String[]{NS + "villager_shepherd", "minecraft:shepherd"},
            new String[]{NS + "villager_librarian", "minecraft:librarian"},
            new String[]{NS + "villager_mason", "minecraft:mason"},
            new String[]{NS + "villager_fisherman", "minecraft:fisherman"},
            new String[]{NS + "villager_butcher", "minecraft:butcher"});

    // Story tags on spawned NPCs (used to find them again)
    public static final String TAG_CARTOGRAPHER = NS + "cartographer";
    public static final String TAG_TRAVELLER = NS + "traveller";
    public static final String TAG_CUSTOMER = NS + "customer";
    public static final String TAG_ROBBER = NS + "robber";
    public static final String TAG_SPIDERMAN = NS + "spiderman";
    public static final String TAG_MOBIUS = NS + "mobius";
    public static final String TAG_MOBIUS_TVA = NS + "mobius_tva";
    public static final String TAG_VILLAGER = NS + "villager";
    public static final String TAG_TVA_GUARD = NS + "tva_guard";
    public static final String TAG_TVA_AGENT = NS + "tva_agent";

    // Destinations
    public static final String DEST_HOME = NS + "mc616_home";
    public static final String DEST_NEW_YORK = NS + "mu616_new_york";
    public static final String DEST_TVA = NS + "tva";
    public static final String DEST_WATCHERS = NS + "watchers_observatory";

    // Cinematics (presentation lives in client.MarvelClient)
    public static final String CIN_DREAM = NS + "s01_dream";
    public static final int CIN_DREAM_TICKS = 320;
    public static final String CIN_INTRO = NS + "s01_intro";
    public static final String CIN_FALLING = NS + "falling_through_realities";
    public static final String CIN_TVA_DOOR = NS + "tva_door";
    public static final int CIN_INTRO_TICKS = 140;
    public static final int CIN_FALLING_TICKS = 550;
    public static final int CIN_TVA_DOOR_TICKS = 140;
    public static final String CIN_S06_FEED = NS + "s06_evacuation_feed";
    public static final int CIN_S06_FEED_TICKS = 420;
    public static final String CIN_S09_DOORWAY = NS + "s09_unknown_doorway";
    public static final int CIN_S09_DOORWAY_TICKS = 160;
    public static final String CIN_S10_REFUGEES = NS + "s10_view_refugees";
    public static final int CIN_S10_REFUGEES_TICKS = 160;
    public static final String CIN_S10_FALLS = NS + "s10_view_tva_falls";
    public static final int CIN_S10_FALLS_TICKS = 280;
    public static final String CIN_S10_PETER = NS + "s10_view_peter";
    public static final int CIN_S10_PETER_TICKS = 180;
    public static final String CIN_S10_HANDOVER = NS + "s10_handover";
    public static final int CIN_S10_HANDOVER_TICKS = 100;
    public static final String CIN_S11_WATCHING = NS + "s11_steve_watching";
    public static final int CIN_S11_WATCHING_TICKS = 140;

    // World sites and flags
    public static final String SITE_VILLAGE = NS + "village";
    public static final String SITE_RUINS = NS + "ruins";
    public static final String SITE_NEW_YORK = NS + "new_york";
    public static final String SITE_TVA = NS + "tva";
    /** Set once a site has been (re)built with the current layout; older worlds rebuild in place. */
    public static final String LAYOUT_V2_VILLAGE = NS + "layout_v2_village";
    public static final String LAYOUT_V2_NEW_YORK = NS + "layout_v2_new_york";
    public static final String LAYOUT_V2_TVA = NS + "layout_v2_tva";
    public static final String LAYOUT_V4_RUINS = NS + "layout_v4_ruins";
    public static final String LAYOUT_V5_PORTAL = NS + "layout_v5_portal";
    public static final String LAYOUT_V3_ROOFTOP = NS + "layout_v4_rooftop";
    public static final String WORLD_FLAG_PORTAL_LIT = NS + "portal_lit";
    public static final String LAYOUT_TVA_WING = NS + "layout_tva_wing_v1";
    public static final String LAYOUT_WATCHERS = NS + "layout_watchers_v1";
    public static final String LAYOUT_NY_S11 = NS + "layout_ny_s11_v1";

    /** Player flag set when the Scenes 1-5 milestone is finished. */
    public static final String FLAG_MILESTONE_1 = NS + "milestone_scenes_1_5";
    /** Prefix of the player flags that mark a spoken script line (bark) as already heard. */
    public static final String FLAG_BARK_PREFIX = NS + "heard/";
    /** Player flag: Mobius has asked Steve where he comes from (Scene 5 intake). */
    public static final String FLAG_S05_INTAKE = NS + "s05_intake_done";
    /** Player flag: Mobius has welcomed Steve in the TVA arrival hall (Scene 5). */
    public static final String FLAG_S05_ARRIVAL = NS + "s05_arrival_done";
    /** Player flag: the TVA time door has opened on the rooftop (Scene 4). */
    public static final String FLAG_S04_DOOR = NS + "s04_door_open";
    /** Player flag set when Scene 11 is finished. */
    public static final String FLAG_MILESTONE_2 = NS + "milestone_scenes_6_11";
    /** Prefix of the per-beat checkpoint and state flags of Scenes 6-11 (see Doomsday). */
    public static final String FLAG_STATE_PREFIX = NS + "wr1/";
    /** Reward id for the cartographer's two surveys. */
    public static final String REWARD_SURVEYS = NS + "surveys";

    private MarvelIds() {
    }
}
