"""Worlds Reborn: Marvel, Act 1 "Doomsday", Scenes 006-011: the baseline dialogue.

Source: the user's implementation spec "WORLDS REBORN: MARVEL - IMPLEMENT DOOMSDAY SCENES 6-11" (2026-10-17), which
replaces the earlier provisional Scenes 6-11 script (SCENES_006_011_SCRIPT_AND_STORY_DRAFT1.md). Text is word for word.
Directions are only labels for the player's reply buttons; they are not spoken.

Every line has a stable id wr_d<scene>_<speaker>_<nnn> (numbered per speaker within a scene) and belongs to a group:
a conversation (d..), a choice option (..#a/#b/#c) or a spoken line during play (b..). Run this file to regenerate:
  - lang entries (line/speaker/tone keys) in the wrmarvel en_us.json
  - sounds.json entries for every line that has a recording in sounds/voice/<id>.ogg
  - marvel/.../MarvelScript611.java (speakers, groups, recording lengths)
  - canon/marvel/SCENES_006_011_LINE_MANIFEST.md and .json (text, speaker, scene, beat, file, recording status)
"""
import json
import os
import subprocess
from collections import OrderedDict, defaultdict

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
ASSETS = os.path.join(ROOT, "marvel/src/main/resources/assets/wrmarvel")
JAVA = os.path.join(ROOT, "marvel/src/main/java/com/worldsreborn/marvel/MarvelScript611.java")
CANON = "/mnt/project-files/canon/marvel"

# speaker key -> subtitle label. Scene 11's player is Spider-Man; his subtitles never show his civilian name.
SPEAKERS = {
    "steve": "Steve",
    "mobius": "Mobius",
    "ops_officer": "Operations Officer",
    "field_agent": "Field Agent",
    "reception_worker": "Reception Worker",
    "medic": "Medic",
    "survivor": "Survivor",
    "technician": "Technician",
    "security_agent": "Security Agent",
    "agent": "Agent",
    "evac_worker": "Evacuation Worker",
    "coordinator": "Coordinator",
    "uatu": "Uatu",
    "other_watcher": "Watcher",
    "spiderman": "Spider-Man",
    "civilian": "Civilian",
    "bystander": "Bystander",
    "daredevil": "Daredevil",
    "jessica": "Jessica Jones",
    "luke": "Luke Cage",
    "danny": "Danny Rand",
    "frank": "Frank Castle",
}

# (scene, beat, group, speaker, direction for reply buttons, text)
L = [
    # ---------------------------------------------------------------- Scene 6, Beyond the Records
    (6, "6.1", "d06_search", "mobius", "", "I checked the coordinates again. Still no match."),
    (6, "6.1", "d06_search", "steve", "Flat", "So you don't know where it is."),
    (6, "6.1", "d06_search", "mobius", "", "That's different from knowing it's gone."),
    (6, "6.2", "d06_crossing", "mobius", "", "Tell me about home. Before the ruins."),
    (6, "6.2", "d06_crossing", "steve", "Remembering", "The Overworld. The Nether. The End. I thought that was everything."),
    (6, "6.2", "d06_crossing", "mobius", "", "Connected dimensions. What you reached in New York was another universe."),
    (6, "6.2", "d06_crossing", "steve", "Unsure", "Another time?"),
    (6, "6.2", "d06_crossing", "mobius", "", "Not necessarily. Different place, different history. We haven't established that you travelled through time."),
    (6, "6.2", "d06_crossing#a", "steve", "Practical", "There were worlds around me. More than I could count."),
    (6, "6.2", "d06_crossing#b", "steve", "Concerned", "Some of them were breaking apart."),
    (6, "6.2", "d06_crossing#c", "steve", "Focused on the traveller", "There was someone in the passage. He was watching me."),
    (6, "6.2", "d06_crossing_rejoin", "steve", "", "I saw different worlds. Earths overlapping. Lines of light breaking."),
    (6, "6.2", "d06_crossing_rejoin", "mobius", "", "Let's compare that with your arrival."),
    (6, "6.3", "d06_identify", "steve", "Sharp", "There. Stop it."),
    (6, "6.3", "d06_identify", "mobius", "", "You know him?"),
    (6, "6.3", "d06_identify", "steve", "Certain", "I saw him in the passage."),
    (6, "6.3", "d06_identify", "mobius", "", "Before New York?"),
    (6, "6.3", "d06_identify", "steve", "Quiet", "While I was falling."),
    (6, "6.3", "d06_sightings", "mobius", "", "Different places. Same figure. No identity we can verify."),
    (6, "6.3", "d06_sightings", "steve", "Remembering", "He said something."),
    (6, "6.3", "d06_sightings", "mobius", "", "What?"),
    (6, "6.3", "d06_sightings", "steve", "Quoting", "'And it starts.'"),
    (6, "6.4", "b06_alert", "ops_officer", "", "Incursion response. Extraction teams require immediate gateway support."),
    (6, "6.4", "b06_alert", "mobius", "", "Come with me."),
    (6, "6.5", "b06_feed", "field_agent", "", "We have another group approaching. Hold the exit."),
    (6, "6.5", "b06_feed", "ops_officer", "", "Your doorway is holding. Keep moving."),
    (6, "6.5", "b06_feed", "field_agent", "", "We're losing the—"),
    (6, "6.5", "b06_feed", "ops_officer", "", "Extraction team, respond."),
    (6, "6.5", "d06_people", "steve", "Shaken", "Those places I saw… there were people there."),
    (6, "6.5", "d06_people", "mobius", "", "Yes."),
    # ---------------------------------------------------------------- Scene 7, Those Who Escaped
    (7, "7.1", "d07_offer", "steve", "Urgent", "Can you get anyone else out?"),
    (7, "7.1", "d07_offer", "mobius", "", "The teams are checking every route that still answers."),
    (7, "7.1", "d07_offer", "steve", "Ready", "What can I do?"),
    (7, "7.1", "d07_offer", "mobius", "", "Help us keep this area moving. People coming through need somewhere to go."),
    (7, "7.1", "d07_tasks", "reception_worker", "", "The walkway's blocked. Supplies are behind that trolley. And we've got people separated between reception points."),
    (7, "7.2", "d07_route", "reception_worker", "", "That will hold?"),
    (7, "7.2", "d07_route", "steve", "Confident", "It'll hold. Bring them across."),
    (7, "7.3", "b07_medic", "medic", "", "Leave those here. Thank you."),
    (7, "7.4", "d07_survivor", "survivor", "", "They came through before me. I couldn't see where they went."),
    (7, "7.4", "d07_survivor", "steve", "Gentle", "Tell me their name."),
    (7, "7.4", "d07_reunion", "survivor", "", "You found them."),
    (7, "7.4", "d07_reunion", "steve", "Kind", "Stay together. The reception team will help you from here."),
    (7, "7.5", "d07_home", "survivor", "", "When can we go back?"),
    (7, "7.5", "d07_home#a", "steve", "Honest", "I don't know yet."),
    (7, "7.5", "d07_home#b", "steve", "Reassuring", "I don't know. Let's get you somewhere safe first."),
    (7, "7.5", "d07_home#c", "steve", "Careful", "They're still checking the routes. I won't promise something I can't know."),
    (7, "7.6", "d07_pending", "steve", "Worried", "Did anyone tell Spider-Man where I went?"),
    (7, "7.6", "d07_pending", "mobius", "", "Your request is still pending. The gateway teams are handling the extractions."),
    (7, "7.6", "d07_pending", "steve", "Quiet", "He'll be looking."),
    (7, "7.6", "d07_pending", "mobius", "", "I haven't forgotten."),
    (7, "7.7", "b07_doors", "technician", "", "We've lost three routes."),
    (7, "7.7", "b07_doors", "reception_worker", "", "Overload?"),
    (7, "7.7", "b07_doors", "technician", "", "Checking."),
    (7, "7.7", "b07_door_unscheduled", "security_agent", "", "That door isn't scheduled. Everyone back."),
    # ---------------------------------------------------------------- Scene 8, The Breach
    (8, "8.1", "b08_entry", "security_agent", "", "Identify yourselves. Stop where you are."),
    (8, "8.1", "b08_evacuate", "mobius", "", "Steve! Get them away from the doors. Emergency route, now."),
    (8, "8.2", "b08_cover", "steve", "", "Behind the wall. Keep low."),
    (8, "8.3", "b08_bridge", "steve", "", "One at a time. Keep moving."),
    (8, "8.4", "b08_injured", "evac_worker", "", "I've got them. Give us a moment."),
    (8, "8.4", "b08_injured", "steve", "", "You've got it."),
    (8, "8.5", "d08_mobius", "agent", "", "We've still got people behind the reception doors."),
    (8, "8.5", "d08_mobius", "mobius", "", "Take this group ahead. Follow the emergency signs."),
    (8, "8.5", "d08_mobius", "steve", "Insistent", "Come with us."),
    (8, "8.5", "d08_mobius", "mobius", "", "I'm going back for the others."),
    (8, "8.5", "b08_mobius_gone", "steve", "", "Mobius?"),
    # ---------------------------------------------------------------- Scene 9, The Last Door
    (9, "9.1", "b09_route", "coordinator", "", "This route is still open. Bring them through."),
    (9, "9.1", "b09_together", "steve", "", "Everyone stays together."),
    (9, "9.2", "b09_last", "steve", "", "Keep going. I'm right behind you."),
    (9, "9.3", "b09_door_fails", "steve", "", "No. Open it again."),
    (9, "9.4", "b09_all_right", "steve", "", "All right."),
    (9, "9.5", "b09_where", "steve", "", "Where…?"),
    # ---------------------------------------------------------------- Scene 10, Sanctuary of the Watchers
    (10, "10.1", "d10_visitor", "uatu", "", "How did you enter this place?"),
    (10, "10.1", "d10_visitor", "steve", "Dazed", "A doorway. The building was coming down."),
    (10, "10.1", "d10_visitor", "uatu", "", "The Time Variance Authority."),
    (10, "10.1", "d10_visitor", "steve", "Urgent", "You know what happened?"),
    (10, "10.1", "d10_visitor", "uatu", "", "We are watching it."),
    (10, "10.2", "d10_debate", "other_watcher", "", "He cannot remain without conditions."),
    (10, "10.2", "d10_debate", "steve", "Defensive", "I wasn't trying to come here."),
    (10, "10.2", "d10_debate", "uatu", "", "He sought an escape. I will take responsibility for his presence."),
    (10, "10.2", "d10_debate", "other_watcher", "", "Then he observes. He does not interfere."),
    (10, "10.3", "d10_refugees_ask", "steve", "Anxious", "The people I sent through. Did they make it?"),
    (10, "10.3", "d10_refugees_safe", "uatu", "", "These people reached safety."),
    (10, "10.3", "d10_refugees_safe", "steve", "Hopeful", "Mobius?"),
    (10, "10.3", "d10_refugees_safe", "uatu", "", "We have not established what became of him."),
    (10, "10.4", "b10_falls", "steve", "", "There were still people inside."),
    (10, "10.5", "d10_conditions", "uatu", "", "You may remain here while we consider your situation."),
    (10, "10.5", "d10_conditions", "steve", "Wary", "And watch?"),
    (10, "10.5", "d10_conditions", "uatu", "", "You may observe and speak with me. You may not enter those realities or send messages into them while accepting this sanctuary."),
    (10, "10.5", "d10_conditions", "steve", "Hopeful", "Can you find my home?"),
    (10, "10.5", "d10_conditions", "uatu", "", "I cannot promise you that."),
    (10, "10.5", "d10_conditions#a", "steve", "Guarded", "Then I'll stay. For now."),
    (10, "10.5", "d10_conditions#b", "steve", "Practical", "I need somewhere to start. I'll stay."),
    (10, "10.5", "d10_conditions#c", "steve", "Firm", "I'll respect your conditions. But I still want answers."),
    (10, "10.6", "d10_peter_ask", "steve", "Worried", "Can you show me New York? Spider-Man."),
    (10, "10.6", "d10_peter_watch", "steve", "Pleading", "He's looking for me. Just let me tell him I'm alive."),
    (10, "10.6", "d10_peter_watch", "uatu", "", "I know what I'm asking of you. But that is their condition."),
    # ---------------------------------------------------------------- Scene 11, The Missing Traveller
    (11, "11.1", "b11_alley", "spiderman", "", "Steve?"),
    (11, "11.1", "b11_wait", "spiderman", "", "You said you'd wait."),
    (11, "11.2", "b11_different", "spiderman", "", "That's different."),
    (11, "11.2", "b11_different", "spiderman", "", "Something else opened here after I left."),
    (11, "11.3", "b11_car", "spiderman", "", "Stay still. I've got the car."),
    (11, "11.3", "b11_back_seat", "civilian", "", "There's someone in the back."),
    (11, "11.3", "b11_back_seat", "spiderman", "", "I see them."),
    (11, "11.4", "b11_sky", "bystander", "", "Is everybody seeing that?"),
    (11, "11.4", "b11_clear_road", "spiderman", "", "Get back from the road. Give the emergency crews space."),
    (11, "11.5", "b11_projection", "spiderman", "", "Please tell me that's a projection."),
    (11, "11.6", "d11_call", "daredevil", "", "You're seeing it too?"),
    (11, "11.6", "d11_call", "spiderman", "Wry", "Hard to miss."),
    (11, "11.6", "d11_call", "daredevil", "", "We're comparing reports. Hell's Kitchen. I'll send the location."),
    (11, "11.6", "d11_call", "spiderman", "Ready", "On my way."),
    (11, "11.7", "d11_meeting", "daredevil", "", "Start with what you know."),
    (11, "11.7", "d11_meeting", "spiderman", "Explaining", "A man came through a portal. Called himself Steve. He helped me stop a robbery."),
    (11, "11.7", "d11_meeting", "jessica", "", "And now?"),
    (11, "11.7", "d11_meeting", "spiderman", "Worried", "He's missing. Something else happened where he arrived."),
    (11, "11.7", "d11_meeting", "frank", "", "Someone sent him?"),
    (11, "11.7", "d11_meeting", "spiderman", "Honest", "I don't know."),
    (11, "11.7", "d11_meeting", "daredevil", "", "Then we don't build the explanation around that."),
    (11, "11.7", "d11_meeting", "luke", "", "People downstairs are watching another planet in the sky. We need somewhere safe to direct them if this gets worse."),
    (11, "11.7", "d11_meeting", "danny", "", "I'll check whether anyone recognises the phenomenon. I'm not calling it magic without evidence."),
    (11, "11.7", "d11_meeting", "jessica", "", "I'll compare the sightings. See whether everyone's recording the same thing."),
    (11, "11.7", "d11_meeting", "frank", "", "I'll watch for anyone using the panic."),
    (11, "11.7", "d11_meeting", "daredevil", "", "And we share what we can verify."),
    (11, "11.7", "d11_meeting", "spiderman", "Earnest", "If you find Steve, contact me."),
]

# Whose lines are offered to the player as reply buttons in each scene.
PLAYER = {6: "steve", 7: "steve", 8: "steve", 9: "steve", 10: "steve", 11: "spiderman"}


def ids():
    counters = defaultdict(int)
    out = []
    for scene, beat, group, speaker, direction, text in L:
        counters[(scene, speaker)] += 1
        out.append((f"wr_d{scene:03d}_{speaker}_{counters[(scene, speaker)]:03d}", scene, beat, group, speaker, direction, text))
    return out


def duration_ms(lid):
    path = os.path.join(ASSETS, "sounds/voice", lid + ".ogg")
    if not os.path.exists(path):
        return 0
    out = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", path],
                         capture_output=True, text=True).stdout.strip()
    return int(float(out) * 1000) if out else 0


def main():
    rows = ids()
    lang_path = os.path.join(ASSETS, "lang/en_us.json")
    lang = json.load(open(lang_path, encoding="utf-8"))
    for key, label in SPEAKERS.items():
        lang.setdefault("speaker.wrmarvel." + key, label)
    for lid, scene, beat, group, speaker, direction, text in rows:
        lang["line.wrmarvel." + lid] = text
        lang["subtitles.wrmarvel." + lid] = SPEAKERS[speaker] + " speaks"
        if direction:
            lang["tone.wrmarvel." + lid] = direction
    json.dump(lang, open(lang_path, "w", encoding="utf-8"), indent=2, ensure_ascii=False)

    sounds_path = os.path.join(ASSETS, "sounds.json")
    sounds = {k: v for k, v in json.load(open(sounds_path)).items() if not k.startswith(("voice.wr_d00", "voice.wr_d010_", "voice.wr_d011_"))}
    recorded = 0
    for lid, *_ in rows:
        if os.path.exists(os.path.join(ASSETS, "sounds/voice", lid + ".ogg")):
            recorded += 1
            sounds["voice." + lid] = {"category": "voice", "subtitle": "subtitles.wrmarvel." + lid,
                                      "sounds": [{"name": "wrmarvel:voice/" + lid, "stream": False}]}
    json.dump(sounds, open(sounds_path, "w"), indent=2)

    groups = OrderedDict()
    for lid, scene, beat, group, *_ in rows:
        groups.setdefault(group, []).append(lid)
    speaker_rows = ",\n".join(f'            Map.entry("{r[0]}", "{r[4]}")' for r in rows)
    tone_rows = ",\n".join(f'            Map.entry("{r[0]}", {str(bool(r[5]) and r[4] == PLAYER[r[1]]).lower()})' for r in rows)
    ms_rows = ",\n".join(f'            Map.entry("{r[0]}", {duration_ms(r[0])})' for r in rows)
    char_rows = ",\n".join(f'            Map.entry("{r[0]}", {len(r[6])})' for r in rows)
    group_rows = ",\n".join(f'            Map.entry("{g}", List.of({", ".join(chr(34) + i + chr(34) for i in v)}))'
                            for g, v in groups.items())
    with open(JAVA, "w", encoding="utf-8") as f:
        f.write(f"""package com.worldsreborn.marvel;

import java.util.List;
import java.util.Map;

/**
 * Dialogue of Act 1 Scenes 006-011 (the user's Doomsday implementation spec). GENERATED by tools/script_6_11.py;
 * edit the script there, not here. Text is in lang ({{@code line.wrmarvel.<id>}}), audio in
 * {{@code sounds/voice/<id>.ogg}} when recorded (a length of 0 means the line is audio_missing: subtitles only).
 */
public final class MarvelScript611 {{
    public static final Map<String, String> SPEAKER = Map.ofEntries(
{speaker_rows});

    /** Lines the player speaks as a reply button. */
    public static final Map<String, Boolean> PLAYER_LINE = Map.ofEntries(
{tone_rows});

    /** Length of each line's recording in milliseconds; 0 = not recorded (audio_missing). */
    public static final Map<String, Integer> MILLIS = Map.ofEntries(
{ms_rows});

    /** Length of each line's text, for timing subtitles of lines that have no recording. */
    public static final Map<String, Integer> CHARS = Map.ofEntries(
{char_rows});

    /** Conversations, choice options (#a/#b/#c) and spoken-during-play groups, each in script order. */
    public static final Map<String, List<String>> GROUPS = Map.ofEntries(
{group_rows});

    private MarvelScript611() {{
    }}
}}
""")

    manifest = []
    for lid, scene, beat, group, speaker, direction, text in rows:
        rec = os.path.exists(os.path.join(ASSETS, "sounds/voice", lid + ".ogg"))
        manifest.append({"id": lid, "scene": f"WR1_{scene:03d}", "beat": beat, "group": group,
                         "speaker": SPEAKERS[speaker], "text": text,
                         "file": f"marvel/src/main/resources/assets/wrmarvel/sounds/voice/{lid}.ogg",
                         "status": "recorded" if rec else "audio_missing"})
    if os.path.isdir(CANON):
        json.dump(manifest, open(os.path.join(CANON, "SCENES_006_011_LINE_MANIFEST.json"), "w"), indent=1, ensure_ascii=False)
        with open(os.path.join(CANON, "SCENES_006_011_LINE_MANIFEST.md"), "w", encoding="utf-8") as md:
            md.write("# Scenes 006–011 line manifest\n\nGenerated by `tools/script_6_11.py` from the user's Doomsday "
                     "implementation spec (2026-10-17). Status is `recorded` only when the .ogg exists in the mod.\n\n")
            md.write("| ID | Scene | Beat | Speaker | Text | Status |\n|---|---|---|---|---|---|\n")
            for m in manifest:
                md.write(f"| {m['id']} | {m['scene']} | {m['beat']} | {m['speaker']} | {m['text'].replace('|', '/')} | {m['status']} |\n")
    print(f"{len(rows)} lines in {len(groups)} groups, {recorded} recorded")


if __name__ == "__main__":
    main()
