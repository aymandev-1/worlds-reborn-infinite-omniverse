"""Worlds Reborn: Marvel, Act 1 "Doomsday", Scenes 012-016: the approved dialogue.

Source: the user's handoff "WORLDS REBORN - DOOMSDAY SCENES 12-16" (2026-10-17), kept in
docs/story/scenes_12_16/. Every line, its stable id and its speaker come from VOICE_AND_SUBTITLE_MANIFEST.csv, word for
word; this file only says which conversation (d..), choice option (..#a/#b/#c) or spoken-during-play group (b..) each
line belongs to, following the beats of SCENES_12-16_APPROVED_DIALOGUE_AND_DIRECTION.md.

Run this file to regenerate:
  - lang entries (line/speaker/tone keys) in the wrmarvel en_us.json
  - sounds.json entries for every line that has a recording in sounds/voice/<id>.ogg (none yet: all audio_missing)
  - marvel/.../MarvelScript1216.java (speakers, groups, recording lengths)
"""
import csv
import json
import os
import subprocess
from collections import OrderedDict

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
ASSETS = os.path.join(ROOT, "marvel/src/main/resources/assets/wrmarvel")
JAVA = os.path.join(ROOT, "marvel/src/main/java/com/worldsreborn/marvel/MarvelScript1216.java")
MANIFEST = os.path.join(ROOT, "../docs/story/scenes_12_16/VOICE_AND_SUBTITLE_MANIFEST.csv")

# The playable character of each scene: their lines are only offered as reply buttons where the script gives a choice.
PLAYER = {12: "sam", 13: "bucky", 14: "sam", 15: "sue", 16: "cyclops"}

# Line id -> group, in script order. Conversations open on screen (d..); barks are spoken as subtitles during play (b..).
GROUPS = {
    # ---------------------------------------------------------------- Scene 12, A World on Alert
    "d12_reports": ["wr_d012_sam_001", "wr_d012_shuri_001", "wr_d012_scott_001", "wr_d012_shuri_002",
                    "wr_d012_sam_002", "wr_d012_shang_chi_001", "wr_d012_sam_003"],
    "b12_first_sample": ["wr_d012_joaquin_001", "wr_d012_shuri_003"],
    "b12_closer": ["wr_d012_sam_004"],
    "b12_distress": ["wr_d012_pilot_001", "wr_d012_joaquin_002", "wr_d012_sam_005"],
    "b12_approach": ["wr_d012_sam_006", "wr_d012_passenger_001", "wr_d012_sam_007"],
    "b12_landing_clear": ["wr_d012_joaquin_003", "wr_d012_shang_chi_002", "wr_d012_scott_002"],
    "d12_landed": ["wr_d012_sam_008", "wr_d012_joaquin_004", "wr_d012_shuri_004"],
    "d12_population": ["wr_d012_shuri_005", "wr_d012_sam_009", "wr_d012_shuri_006", "wr_d012_sam_010"],
    # ---------------------------------------------------------------- Scene 13, Another Response
    "d13_distress": ["wr_d013_yelena_001", "wr_d013_bucky_001", "wr_d013_walker_001", "wr_d013_ghost_001",
                     "wr_d013_sentry_001"],
    "d13_clint_kate": ["wr_d013_kate_001", "wr_d013_clint_001", "wr_d013_yelena_002", "wr_d013_clint_002"],
    "b13_this_way": ["wr_d013_bucky_002"],
    "b13_room_clear": ["wr_d013_yelena_003"],
    "b13_cable": ["wr_d013_kate_002", "wr_d013_clint_003"],
    "b13_exit": ["wr_d013_red_guardian_001", "wr_d013_walker_002"],
    "b13_sentry": ["wr_d013_sentry_002"],
    "d13_recorder": ["wr_d013_scientist_001", "wr_d013_bucky_003", "wr_d013_scientist_002"],
    "d13_intervals": ["wr_d013_scientist_003", "wr_d013_yelena_004", "wr_d013_scientist_004", "wr_d013_bucky_004",
                      "wr_d013_bucky_005", "wr_d013_sam_001"],
    # ---------------------------------------------------------------- Scene 14, First Contact
    "d14_briefing": ["wr_d014_sam_001", "wr_d014_yelena_001", "wr_d014_bucky_001", "wr_d014_shuri_001",
                     "wr_d014_shuri_002"],
    "d14_briefing#a": ["wr_d014_sam_002"],
    "d14_briefing#b": ["wr_d014_sam_003"],
    "d14_briefing#c": ["wr_d014_sam_004"],
    "d14_briefing_rejoin": ["wr_d014_clint_001", "wr_d014_sam_005"],
    "d14_window": ["wr_d014_shuri_003", "wr_d014_scott_001", "wr_d014_shuri_004"],
    "d14_contact": ["wr_d014_reed_001", "wr_d014_sam_006", "wr_d014_reed_002", "wr_d014_sam_007",
                    "wr_d014_shuri_005", "wr_d014_reed_003"],
    "d14_same_boundary": ["wr_d014_shuri_006", "wr_d014_reed_004", "wr_d014_sam_008", "wr_d014_reed_005"],
    "b14_deteriorates": ["wr_d014_reed_006"],
    # ---------------------------------------------------------------- Scene 15, Across the Divide
    "d15_after": ["wr_d015_johnny_001", "wr_d015_ben_001", "wr_d015_reed_001", "wr_d015_sue_001"],
    "b15_aisle": ["wr_d015_sue_002", "wr_d015_reed_002", "wr_d015_sue_003"],
    "b15_sensor": ["wr_d015_johnny_002", "wr_d015_sue_004"],
    "b15_sensor_done": ["wr_d015_johnny_003"],
    "b15_assembly": ["wr_d015_ben_002", "wr_d015_reed_003", "wr_d015_ben_003"],
    "d15_restored": ["wr_d015_reed_004", "wr_d015_shuri_001", "wr_d015_sam_001", "wr_d015_sue_005"],
    "d15_more_worlds": ["wr_d015_reed_005", "wr_d015_shuri_002", "wr_d015_reed_006", "wr_d015_sam_002",
                        "wr_d015_reed_007", "wr_d015_reed_008"],
    # ---------------------------------------------------------------- Scene 16, The Other Side
    "d16_briefing": ["wr_d016_beast_001", "wr_d016_cyclops_001", "wr_d016_beast_002", "wr_d016_xavier_001",
                     "wr_d016_magneto_001", "wr_d016_cyclops_002"],
    "d16_arrival": ["wr_d016_nightcrawler_001", "wr_d016_jean_001", "wr_d016_cyclops_003"],
    "b16_beam": ["wr_d016_jean_002", "wr_d016_cyclops_004"],
    "b16_extract": ["wr_d016_nightcrawler_002", "wr_d016_cyclops_005"],
    "b16_lower_recorder": ["wr_d016_scientist_001", "wr_d016_beast_003"],
    "b16_recorder": ["wr_d016_beast_004", "wr_d016_cyclops_006"],
    "d16_landscape": ["wr_d016_cyclops_007", "wr_d016_beast_005", "wr_d016_jean_003", "wr_d016_beast_006",
                      "wr_d016_cyclops_008"],
    "d16_school": ["wr_d016_xavier_002", "wr_d016_magneto_002", "wr_d016_cyclops_009", "wr_d016_magneto_003",
                   "wr_d016_cyclops_010"],
}

# Reply-button labels for the one scripted choice (14.2, "Define priorities"). Labels only; they are not spoken.
DIRECTIONS = {
    "wr_d014_sam_002": "Protect people",
    "wr_d014_sam_003": "Contact first",
    "wr_d014_sam_004": "Share the evidence",
}


def scene_of(row):
    return int(row["scene"].split("_")[1])


def speaker_key(row):
    return row["speaker"].lower().replace("-", "_").replace(" ", "_")


def speaker_label(row):
    return row["speaker"].title()


def duration_ms(lid):
    path = os.path.join(ASSETS, "sounds/voice", lid + ".ogg")
    if not os.path.exists(path):
        return 0
    out = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", path],
                         capture_output=True, text=True).stdout.strip()
    return int(float(out) * 1000) if out else 0


def main():
    with open(MANIFEST, encoding="utf-8-sig") as f:
        rows = {r["id"]: r for r in csv.DictReader(f)}
    group_of = {}
    for g, ids in GROUPS.items():
        for lid in ids:
            if lid not in rows:
                raise SystemExit(f"{lid} is not in the manifest")
            if lid in group_of:
                raise SystemExit(f"{lid} is in two groups")
            group_of[lid] = g
    missing = [lid for lid in rows if lid not in group_of]
    if missing:
        raise SystemExit(f"manifest lines without a group: {missing}")

    lang_path = os.path.join(ASSETS, "lang/en_us.json")
    lang = json.load(open(lang_path, encoding="utf-8"))
    for lid, r in rows.items():
        lang.setdefault("speaker.wrmarvel." + speaker_key(r), speaker_label(r))
        lang["line.wrmarvel." + lid] = r["text"]
        lang["subtitles.wrmarvel." + lid] = speaker_label(r) + " speaks"
        if lid in DIRECTIONS:
            lang["tone.wrmarvel." + lid] = DIRECTIONS[lid]
    json.dump(lang, open(lang_path, "w", encoding="utf-8"), indent=2, ensure_ascii=False)

    sounds_path = os.path.join(ASSETS, "sounds.json")
    sounds = {k: v for k, v in json.load(open(sounds_path)).items()
              if not k.startswith(("voice.wr_d012_", "voice.wr_d013_", "voice.wr_d014_", "voice.wr_d015_", "voice.wr_d016_"))}
    recorded = 0
    for lid in rows:
        if os.path.exists(os.path.join(ASSETS, "sounds/voice", lid + ".ogg")):
            recorded += 1
            sounds["voice." + lid] = {"category": "voice", "subtitle": "subtitles.wrmarvel." + lid,
                                      "sounds": [{"name": "wrmarvel:voice/" + lid, "stream": False}]}
    json.dump(sounds, open(sounds_path, "w"), indent=2)

    order = [lid for g in GROUPS.values() for lid in g]
    q = chr(34)
    speaker_rows = ",\n".join(f'            Map.entry("{lid}", "{speaker_key(rows[lid])}")' for lid in order)
    player_rows = ",\n".join(
        f'            Map.entry("{lid}", {str(lid in DIRECTIONS and speaker_key(rows[lid]) == PLAYER[scene_of(rows[lid])]).lower()})'
        for lid in order)
    ms_rows = ",\n".join(f'            Map.entry("{lid}", {duration_ms(lid)})' for lid in order)
    char_rows = ",\n".join(f'            Map.entry("{lid}", {len(rows[lid]["text"])})' for lid in order)
    group_rows = ",\n".join(f'            Map.entry("{g}", List.of({", ".join(q + i + q for i in v)}))'
                            for g, v in GROUPS.items())
    with open(JAVA, "w", encoding="utf-8") as f:
        f.write(f"""package com.worldsreborn.marvel;

import java.util.List;
import java.util.Map;

/**
 * Dialogue of Act 1 Scenes 012-016 (the user's approved Scenes 12-16 handoff). GENERATED by tools/script_12_16.py from
 * docs/story/scenes_12_16/VOICE_AND_SUBTITLE_MANIFEST.csv; edit there, not here. Text is in lang
 * ({{@code line.wrmarvel.<id>}}), audio in {{@code sounds/voice/<id>.ogg}} when recorded (a length of 0 means the line is
 * audio_missing: subtitles only).
 */
public final class MarvelScript1216 {{
    public static final Map<String, String> SPEAKER = Map.ofEntries(
{speaker_rows});

    /** Lines the player speaks as a reply button. */
    public static final Map<String, Boolean> PLAYER_LINE = Map.ofEntries(
{player_rows});

    /** Length of each line's recording in milliseconds; 0 = not recorded (audio_missing). */
    public static final Map<String, Integer> MILLIS = Map.ofEntries(
{ms_rows});

    /** Length of each line's text, for timing subtitles of lines that have no recording. */
    public static final Map<String, Integer> CHARS = Map.ofEntries(
{char_rows});

    /** Conversations, choice options (#a/#b/#c) and spoken-during-play groups, each in script order. */
    public static final Map<String, List<String>> GROUPS = Map.ofEntries(
{group_rows});

    private MarvelScript1216() {{
    }}
}}
""")
    print(f"{len(rows)} lines in {len(GROUPS)} groups, {recorded} recorded")


if __name__ == "__main__":
    main()
