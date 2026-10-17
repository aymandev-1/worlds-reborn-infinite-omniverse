package com.worldsreborn.marvel;

import com.worldsreborn.characters.CharacterDef;
import com.worldsreborn.characters.Characters;
import com.worldsreborn.core.WRCore;
import com.worldsreborn.core.model.Destination;
import com.worldsreborn.core.model.DialogueChoice;
import com.worldsreborn.core.model.DialogueDef;
import com.worldsreborn.core.model.DialogueLine;
import com.worldsreborn.core.model.QuestCategory;
import com.worldsreborn.core.model.QuestDef;
import com.worldsreborn.core.model.StepDef;
import com.worldsreborn.core.model.UniverseId;
import com.worldsreborn.core.model.UniverseInfo;
import com.worldsreborn.core.model.WRContent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import static com.worldsreborn.marvel.MarvelIds.*;

/**
 * Registers the Marvel campaign's content for Act 1: Scenes 1-5 here, Scenes 6-16 through the doomsday package.
 * <p>
 * Dialogue is the user's Scenes 001-005 voice acting script (Draft 1), word for word, every line voiced; see
 * {@link MarvelScript} and tools/voice_script.py.
 */
public final class MarvelContent {
    public static final UniverseId MU_616 = UniverseId.parse("MU-616");
    private static final String CAMPAIGN = WRMarvel.MODID;

    private MarvelContent() {
    }

    public static void register() {
        WRContent.UNIVERSES.register(new UniverseInfo(MU_616, "universe.wrmarvel.mu616", "marvel", true));
        destinations();
        com.worldsreborn.marvel.doomsday.Responders.registerAbilities();
        quests();
        dialogues();
        characters();
    }

    private static void destinations() {
        WRContent.DESTINATIONS.register(new Destination(DEST_HOME, WRCore.MC_616.toString(), "minecraft:overworld",
                "destination.wrmarvel.mc616_home", "destination.wrmarvel.mc616_home.desc", null, true, false, 0, 0, 0));
        WRContent.DESTINATIONS.register(new Destination(DEST_NEW_YORK, MU_616.toString(), DEST_NEW_YORK,
                "destination.wrmarvel.mu616_new_york", "destination.wrmarvel.mu616_new_york.desc", S02, false, true, 0, 0, 0));
        WRContent.DESTINATIONS.register(new Destination(DEST_TVA, null, DEST_TVA,
                "destination.wrmarvel.tva", "destination.wrmarvel.tva.desc", S05, false, true, 0, 0, 0));
        WRContent.DESTINATIONS.register(new Destination(DEST_WATCHERS, null, DEST_WATCHERS,
                "destination.wrmarvel.watchers_observatory", "destination.wrmarvel.watchers_observatory.desc", S10, false, true, 0, 0, 0));
    }

    private static QuestDef quest(String id, int scene, List<StepDef> steps, String next) {
        String key = "quest." + id.replace(':', '.');
        return WRContent.QUESTS.register(new QuestDef(id, CAMPAIGN, "Act 1 · Scene " + scene, QuestCategory.MAIN,
                key, key + ".summary", steps, List.of(), next));
    }

    private static StepDef reach(String quest, String step) {
        return StepDef.reach(step, stepKey(quest, step));
    }

    private static StepDef action(String quest, String step) {
        return StepDef.action(step, stepKey(quest, step));
    }

    public static String stepKey(String quest, String step) {
        return "quest." + quest.replace(':', '.') + "." + step;
    }

    private static void quests() {
        quest(S01, 1, List.of(reach(S01, S01_RETURN_VILLAGE), action(S01, S01_TALK_CARTOGRAPHER), reach(S01, S01_REACH_RUINS)), S02);
        quest(S02, 2, List.of(reach(S02, S02_ENTER_CHAMBER), action(S02, S02_REPAIR_REDSTONE), action(S02, S02_ENTER_PORTAL)), S03);
        quest(S03, 3, List.of(StepDef.count(S03_STOP_ROBBERS, stepKey(S03, S03_STOP_ROBBERS), 3), action(S03, S03_MEET_SPIDERMAN)), S04);
        quest(S04, 4, List.of(reach(S04, S04_BACK_TO_TRACE), action(S04, S04_SCAN_TRACE), action(S04, S04_PETER_LEAVES)), S05);
        quest(S05, 5, List.of(action(S05, S05_MEET_MOBIUS), reach(S05, S05_FIND_RECORDS), action(S05, S05_READ_RECORDS)), S06);
        // Act 1 "Doomsday", Scenes 6-11 (see doomsday.Doomsday). Each step is named after the checkpoint it reaches.
        quest(S06, 6, List.of(action(S06, S06_SEARCH), action(S06, S06_CROSSING), action(S06, S06_IDENTIFY),
                reach(S06, S06_OPERATIONS), action(S06, S06_FEED)), S07);
        quest(S07, 7, List.of(action(S07, S07_OFFER), action(S07, S07_ROUTE), action(S07, S07_SUPPLIES), action(S07, S07_REUNITE),
                action(S07, S07_HOME), action(S07, S07_PENDING), action(S07, S07_DOORS)), S08);
        quest(S08, 8, List.of(action(S08, S08_EVACUATE), action(S08, S08_COVER), action(S08, S08_BRIDGE), action(S08, S08_INJURED),
                action(S08, S08_MOBIUS)), S09);
        quest(S09, 9, List.of(action(S09, S09_REFUGEES), action(S09, S09_BACKUP), action(S09, S09_DOORWAY), action(S09, S09_ARRIVE)), S10);
        quest(S10, 10, List.of(action(S10, S10_REFUGEES), action(S10, S10_FALLS), action(S10, S10_SANCTUARY), action(S10, S10_HANDOVER)), S11);
        quest(S11, 11, List.of(action(S11, S11_ALLEY), action(S11, S11_SCAN), action(S11, S11_ACCIDENT), action(S11, S11_SKY),
                action(S11, S11_MEETING), action(S11, S11_SHARE)), S12);
        // Act 1 "Doomsday", Scenes 12-16 (see doomsday.Responders): the approved checkpoints, in order.
        quest(S12, 12, List.of(action(S12, S12_RESPONSE), action(S12, S12_SURVEY), action(S12, S12_PASSENGERS), action(S12, S12_DONE)), S13);
        quest(S13, 13, List.of(action(S13, S13_ENTRY), action(S13, S13_FIRST_GROUP), action(S13, S13_STAFF), action(S13, S13_RECORDER),
                action(S13, S13_DONE)), S14);
        quest(S14, 14, List.of(action(S14, S14_PLAN), action(S14, S14_ARRAY), action(S14, S14_DONE)), S15);
        quest(S15, 15, List.of(action(S15, S15_TEAM), action(S15, S15_LAB), action(S15, S15_SENSORS), action(S15, S15_ASSEMBLY),
                action(S15, S15_DONE)), S16);
        quest(S16, 16, List.of(action(S16, S16_BRIEFING), action(S16, S16_ACCESS), action(S16, S16_STAFF), action(S16, S16_DATA),
                action(S16, S16_DONE)), null);
    }

    /** One numbered line of the voice acting script: its text, its recording and, for Steve, a reply prompt. */
    public static DialogueLine line(String id) {
        String speaker = MarvelScript.SPEAKER.get(id);
        if (speaker == null) {
            throw new IllegalArgumentException("No script line " + id);
        }
        String key = id.toLowerCase(java.util.Locale.ROOT);
        return new DialogueLine("speaker.wrmarvel." + speaker, "line.wrmarvel." + key, false, voice(id),
                "steve".equals(speaker) ? "tone.wrmarvel." + key : null);
    }

    public static String voice(String id) {
        return WRMarvel.MODID + ":voice." + id.toLowerCase(java.util.Locale.ROOT);
    }

    /** Lines {@code scene_from} to {@code scene_to} of one scene, e.g. {@code lines(1, 1, 23)}. */
    public static List<DialogueLine> lines(int scene, int from, int to) {
        List<DialogueLine> out = new java.util.ArrayList<>();
        for (int i = from; i <= to; i++) {
            out.add(line(String.format("S%03d_%03d", scene, i)));
        }
        return out;
    }

    private static void dialogue(String id, List<DialogueLine> lines) {
        WRContent.DIALOGUES.register(new DialogueDef(id, lines, List.of()));
    }

    /**
     * Conversations from the user's Scenes 001-005 voice acting script, in order. Steve's lines are offered to the
     * player as replies. Lines spoken during play (on arrival, in the fight, at the maps) are barks, see MarvelStory.
     */
    private static void dialogues() {
        // The two surveys are on screen from "What's this?" until the cartographer asks for help.
        List<DialogueLine> cartographer = new java.util.ArrayList<>(lines(1, 1, 23));
        String maps = WRMarvel.MODID + ":textures/gui/old_survey.png|" + WRMarvel.MODID + ":textures/gui/new_survey.png";
        for (int i = 5; i <= 16; i++) {
            cartographer.set(i, cartographer.get(i).withImages(maps));
        }
        dialogue(D01_CARTOGRAPHER, cartographer);
        dialogue(D03_SPIDERMAN, lines(3, 22, 40));
        dialogue(D04_SCAN, lines(4, 1, 12));
        dialogue(D04_HEAR, lines(4, 13, 14));
        dialogue(D04_TVA, lines(4, 15, 22));
        dialogue(D05_MOBIUS, lines(5, 1, 7));
        dialogue(D05_ARRIVAL, lines(5, 8, 11));
        dialogue(D05_INTAKE, lines(5, 12, 18));
        dialogue(D05_ANALYSIS, lines(5, 19, 37));
        com.worldsreborn.marvel.doomsday.Doomsday.registerDialogues();
        com.worldsreborn.marvel.doomsday.Responders.registerDialogues();
    }

    private static void character(String id, CharacterDef.Model model, ResourceLocation texture) {
        Characters.register(new CharacterDef(id, "character." + id.replace(':', '.'), texture, model, true));
    }

    private static ResourceLocation skin(String id) {
        return WRMarvel.id("textures/entity/" + id.substring(id.indexOf(':') + 1) + ".png");
    }

    private static void characters() {
        // Villagers use the vanilla villager body with their profession's clothes on top.
        ResourceLocation villager = ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");
        Characters.register(new CharacterDef(CARTOGRAPHER, "character.wrmarvel.cartographer", villager,
                CharacterDef.Model.VILLAGER, true, "minecraft:cartographer", null));
        for (String[] v : VILLAGERS) {
            Characters.register(new CharacterDef(v[0], "character." + v[0].replace(':', '.'), villager,
                    CharacterDef.Model.VILLAGER, true, v[1], null));
        }
        Characters.register(new CharacterDef(TVA_GUARD, "character.wrmarvel.tva_guard", skin(TVA_GUARD),
                CharacterDef.Model.HUMANOID, true, null, ResourceLocation.withDefaultNamespace("blaze_rod")));
        for (String id : List.of(TRAVELLER, SPIDERMAN, MOBIUS, ROBBER)) {
            character(id, CharacterDef.Model.HUMANOID, skin(id));
        }
        for (String id : CIVILIANS) {
            character(id, CharacterDef.Model.HUMANOID, skin(id));
        }
        // Scenes 6-11.
        for (String id : List.of(TVA_STAFF, TVA_MEDIC, ATTACKER, UATU, WATCHER, DAREDEVIL, JESSICA, LUKE, DANNY, FRANK, BYSTANDER)) {
            character(id, CharacterDef.Model.HUMANOID, skin(id));
        }
        for (String id : REFUGEES) {
            character(id, CharacterDef.Model.HUMANOID, skin(id));
        }
        // People named by their job, wearing a shared uniform: {character, skin}.
        String[][] roles = {
                {com.worldsreborn.marvel.doomsday.Doomsday.RECEPTION_WORKER, TVA_STAFF},
                {com.worldsreborn.marvel.doomsday.Doomsday.OPS_OFFICER, TVA_STAFF},
                {com.worldsreborn.marvel.doomsday.Doomsday.TECHNICIAN, TVA_STAFF},
                {com.worldsreborn.marvel.doomsday.Doomsday.ATTENDANT, TVA_STAFF},
                {com.worldsreborn.marvel.doomsday.Doomsday.COORDINATOR, TVA_STAFF},
                {com.worldsreborn.marvel.doomsday.Doomsday.EVAC_WORKER, TVA_MEDIC},
                {com.worldsreborn.marvel.doomsday.Doomsday.SECURITY_AGENT, TVA_GUARD},
                {com.worldsreborn.marvel.doomsday.Doomsday.FIELD_AGENT, TVA_GUARD},
                {com.worldsreborn.marvel.doomsday.Doomsday.CAR_OCCUPANT, CIVILIANS.get(0)},
                {com.worldsreborn.marvel.doomsday.Doomsday.PEDESTRIAN, CIVILIANS.get(2)}};
        for (String[] r : roles) {
            character(r[0], CharacterDef.Model.HUMANOID, skin(r[1]));
        }
        // Scenes 12-16: each character wears their own (generated stand-in) skin.
        for (String id : com.worldsreborn.marvel.doomsday.Responders.CHARACTERS) {
            character(id, CharacterDef.Model.HUMANOID, skin(id));
        }
    }
}
