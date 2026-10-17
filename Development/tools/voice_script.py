"""Worlds Reborn: Marvel, Scenes 001-005 voice acting script, Draft 1.

Provided verbatim by the user on 2026-10-17 (it replaces Claude's earlier draft). One entry per numbered line:
(line id, speaker, direction, text). Directions are not spoken. Run this file to regenerate the lang entries,
sounds.json and the readable copy in canon/marvel.
"""
import json
import os
import subprocess
import sys

LINES = [
    # SCENE 001 - THE MAP (MC-616, Village Cartographer's Workshop)
    ("S001_001", "CARTOGRAPHER", "Warm, pleasantly surprised", "Steve! You're back! I was beginning to think you'd found another dimension to get lost in."),
    ("S001_002", "STEVE", "Amused, relaxed", "Not this time. Just a few mountains, some caves, and more skeletons than I care to remember."),
    ("S001_003", "CARTOGRAPHER", "Laughing lightly", "Sounds like another ordinary adventure for you."),
    ("S001_004", "STEVE", "Friendly", "So, what's new around here?"),
    ("S001_005", "CARTOGRAPHER", "Tone becoming serious", "Actually, there's something I've been meaning to show you."),
    ("S001_006", "STEVE", "Curious", "What's this?"),
    ("S001_007", "CARTOGRAPHER", "Explaining", "Two maps. Same valley. Same coordinates. Surveyed at different times."),
    ("S001_008", "STEVE", "Observant", "They look almost identical."),
    ("S001_009", "CARTOGRAPHER", "Insistent", "Almost. Look closer."),
    ("S001_010", "STEVE", "Noticing something", "Wait. What's that structure?"),
    ("S001_011", "CARTOGRAPHER", "Quietly troubled", "That's exactly the problem."),
    ("S001_012", "STEVE", "Confused", "What problem?"),
    ("S001_013", "CARTOGRAPHER", "Serious", "The earlier survey shows nothing there. No ruins. No foundations. Nothing."),
    ("S001_014", "STEVE", "Sceptical", "Maybe whoever made the first map missed it."),
    ("S001_015", "CARTOGRAPHER", "Firm", "I made the first map, Steve. I walked that valley myself."),
    ("S001_016", "STEVE", "Surprised", "Then how did an entire building appear out of nowhere?"),
    ("S001_017", "CARTOGRAPHER", "Concerned", "I was hoping you might help me answer that."),
    ("S001_018", "STEVE", "Determined", "Well, there's only one way to find out."),
    ("S001_019", "CARTOGRAPHER", "Concerned", "Steve, please be careful. Something about this doesn't feel right."),
    ("S001_020", "STEVE", "Reassuring", "I've handled worse than an abandoned building."),
    ("S001_021", "CARTOGRAPHER", "Under his breath", "That's what worries me."),
    ("S001_022", "STEVE", "Heading out", "I'll take a look. If I find anything interesting, you'll be the first to know."),
    ("S001_023", "CARTOGRAPHER", "Calling after him", "Just make sure you come back!"),
    ("S001_024", "STEVE", "Examining old map", "Nothing unusual here."),
    ("S001_025", "STEVE", "Examining new map", "Same valley... but that definitely wasn't there before."),
    ("S001_026", "STEVE", "Looking at the route", "Looks like I've got a bit of a walk ahead of me."),
    # SCENE 002 - BENEATH THE RUINS (MC-616, Ancient Valley and Underground Structure)
    ("S002_001", "STEVE", "Looking around", "This should be the place."),
    ("S002_002", "STEVE", "Surprised", "Okay... that's definitely not supposed to be here."),
    ("S002_003", "STEVE", "Studying ruins", "These blocks... I've never seen anything built like this."),
    ("S002_004", "STEVE", "Cautious", "Someone went through a lot of trouble to hide this place."),
    ("S002_005", "STEVE", "Entering the underground chamber", "Hello? Anyone down here?"),
    ("S002_006", "STEVE", "Listening", "Great. Just me and a creepy underground ruin."),
    ("S002_007", "STEVE", "Examining machinery", "Redstone... but not like any redstone I've worked with."),
    ("S002_008", "STEVE", "Discovering symbols", "What kind of language is this?"),
    ("S002_009", "STEVE", "Thinking aloud", "There's got to be a way to activate this."),
    ("S002_010", "STEVE", "Interacting with mechanism", "All right. Here goes nothing."),
    ("S002_011", "STEVE", "Startled", "Whoa!"),
    ("S002_012", "STEVE", "Amazed", "That... is not a Nether portal."),
    ("S002_013", "STEVE", "Tentative", "Where does it lead?"),
    ("S002_014", "STEVE", "Nervous laugh", "I've really got to stop jumping into things I don't understand."),
    ("S002_015", "STEVE", "Resolute", "One look. Then I'm coming straight back."),
    ("S002_016", "STEVE", "Shouting, falling", "WHOA! WHAT IS HAPPENING?!"),
    ("S002_017", "STEVE", "Disoriented", "Where am I?!"),
    ("S002_018", "STEVE", "Overwhelmed by visions", "Those places... what are they?"),
    ("S002_019", "STEVE", "Seeing destruction", "No... what's happening to those worlds?"),
    ("S002_020", "THE WATCHER", "Deep, solemn, distant", "Countless realities. Countless lives. And events whose consequences reach farther than any one world."),
    ("S002_021", "STEVE", "Trying to focus", "Who's there?!"),
    ("S002_022", "STEVE", "Losing control", "I can't stop!"),
    ("S002_023", "UNKNOWN VOICE", "Very quiet, ominous", "And it starts."),
    ("S002_024", "STEVE", "Screaming as he falls out", "AAAAH!"),
    # SCENE 003 - STRANGER IN NEW YORK (MU-616, New York City)
    ("S003_001", "STEVE", "Groaning after landing", "Ow... okay. That hurt."),
    ("S003_002", "STEVE", "Looking at skyscrapers", "What... is this place?"),
    ("S003_003", "STEVE", "Astonished", "How tall are those buildings?!"),
    ("S003_004", "STEVE", "Startled by car horn", "HEY! Watch it!"),
    ("S003_005", "CIVILIAN 1", "Annoyed, passing by", "Move it, buddy!"),
    ("S003_006", "STEVE", "Confused", "Buddy?"),
    ("S003_007", "STEVE", "Quietly", "I'm definitely not home."),
    ("S003_008", "ROBBER 1", "Threatening", "Nobody move! Hands where I can see them!"),
    ("S003_009", "CIVILIAN 2", "Terrified", "Please! Take whatever you want!"),
    ("S003_010", "ROBBER 2", "Aggressive", "Give me the bag! Now!"),
    ("S003_011", "STEVE", "Firm", "Hey. Leave them alone."),
    ("S003_012", "ROBBER 1", "Mocking", "What are you supposed to be? Some kind of video game character?"),
    ("S003_013", "STEVE", "Dry humour", "Funny. I was about to ask you the same thing."),
    ("S003_014", "ROBBER 2", "Angry", "Get him!"),
    ("S003_015", "STEVE", "Ready to fight", "Bad idea."),
    ("S003_016", "SPIDER-MAN", "Swinging into action", "Okay! Pretty sure this isn't how we ask people for their belongings!"),
    ("S003_017", "ROBBER 1", "Shocked", "Spider-Man?!"),
    ("S003_018", "SPIDER-MAN", "Playful", "Yep! That's me. Hi."),
    ("S003_019", "STEVE", "Confused", "Spider... Man?"),
    ("S003_020", "SPIDER-MAN", "Mid-fight", "Give me one second! Kind of busy!"),
    ("S003_021", "SPIDER-MAN", "After webbing robber", "And... there we go!"),
    ("S003_022", "STEVE", "Examining webbing", "What is that stuff?"),
    ("S003_023", "SPIDER-MAN", "Friendly", "Webbing. Long story. You're not from around here, are you?"),
    ("S003_024", "STEVE", "Matter-of-fact", "That obvious?"),
    ("S003_025", "SPIDER-MAN", "Joking", "The sword, the clothes, the whole... square thing? Yeah, a little."),
    ("S003_026", "SPIDER-MAN", "Gentle", "Okay, start from the beginning. What's your name?"),
    ("S003_027", "STEVE", "Simple", "Steve."),
    ("S003_028", "SPIDER-MAN", "Friendly", "Nice to meet you, Steve. You can call me Spider-Man."),
    ("S003_029", "STEVE", "Puzzled", "That's really your name?"),
    ("S003_030", "SPIDER-MAN", "Lightly joking", "Well, not the one on my birth certificate."),
    ("S003_031", "SPIDER-MAN", "Curious", "So where are you from?"),
    ("S003_032", "STEVE", "Thinking", "A village. Lots of forests, mountains, caves. Not many... whatever those metal things on the roads are."),
    ("S003_033", "SPIDER-MAN", "Surprised", "Cars? You've never seen a car?"),
    ("S003_034", "STEVE", "Genuine", "Nope."),
    ("S003_035", "SPIDER-MAN", "Processing", "Okay. Wow. That's new."),
    ("S003_036", "STEVE", "Serious", "I went through some kind of portal. I saw other places. Worlds, I think. Then I ended up here."),
    ("S003_037", "SPIDER-MAN", "Concerned", "Other worlds?"),
    ("S003_038", "STEVE", "Honest", "I don't understand it either."),
    ("S003_039", "SPIDER-MAN", "Reassuring", "All right. We'll figure something out."),
    ("S003_040", "STEVE", "Grateful", "Thanks. I appreciate it."),
    # SCENE 004 - FADING TRACE (MU-616, New York Rooftop and Portal Arrival Area)
    ("S004_001", "SPIDER-MAN", "Focused", "Okay, I've got something. There's still a little energy left from where you appeared."),
    ("S004_002", "STEVE", "Hopeful", "Can it tell us how to get home?"),
    ("S004_003", "SPIDER-MAN", "Uncertain", "Maybe. But I've never seen readings like these."),
    ("S004_004", "STEVE", "Concerned", "Is that bad?"),
    ("S004_005", "SPIDER-MAN", "Trying to stay positive", "Not necessarily. Just... incredibly weird."),
    ("S004_006", "STEVE", "Dry", "That's reassuring."),
    ("S004_007", "SPIDER-MAN", "Concentrating", "Come on. Come on..."),
    ("S004_008", "SPIDER-MAN", "Frustrated", "No, no, no. It's fading."),
    ("S004_009", "STEVE", "Alarmed", "What's fading?"),
    ("S004_010", "SPIDER-MAN", "Serious", "The signal. Whatever brought you here, it's almost completely gone."),
    ("S004_011", "STEVE", "Quietly worried", "So... I'm stuck here?"),
    ("S004_012", "SPIDER-MAN", "Reassuring", "Hey. We don't know that yet."),
    ("S004_013", "STEVE", "Hearing something", "Peter... did you hear that?"),
    ("S004_014", "SPIDER-MAN", "Alert", "Yeah. I heard it."),
    ("S004_015", "STEVE", "Startled", "Another portal?!"),
    ("S004_016", "SPIDER-MAN", "Guarded", "Okay, that's definitely not mine."),
    ("S004_017", "TVA AGENT 1", "Commanding", "Step away from the anomaly."),
    ("S004_018", "STEVE", "Defensive", "Anomaly? I'm standing right here!"),
    ("S004_019", "TVA AGENT 2", "Into communicator", "Subject located. Awaiting intake instructions."),
    ("S004_020", "SPIDER-MAN", "Protective", "Okay, hold on. Who exactly are you guys?"),
    ("S004_021", "TVA AGENT 1", "Firm", "Time Variance Authority. Do not interfere."),
    ("S004_022", "STEVE", "Confused", "Time... what?"),
    # SCENE 005 - UNRECORDED TRAVELLER (MU-616, TVA Gateway and Intake Headquarters)
    ("S005_001", "MOBIUS", "Calm, diplomatic", "All right, everybody. Let's not make this more complicated than it needs to be."),
    ("S005_002", "STEVE", "Suspicious", "And you are?"),
    ("S005_003", "MOBIUS", "Friendly", "Mobius. I work with the TVA."),
    ("S005_004", "STEVE", "Dry", "That doesn't explain much."),
    ("S005_005", "MOBIUS", "Amused", "Yeah, I get that a lot."),
    ("S005_006", "MOBIUS", "Professional", "We just need to ask you a few questions about how you got here."),
    ("S005_007", "STEVE", "Frustrated", "I'd love to know that myself."),
    ("S005_008", "STEVE", "In awe", "What is this place?"),
    ("S005_009", "MOBIUS", "Casual", "Welcome to the Time Variance Authority."),
    ("S005_010", "STEVE", "Confused", "Is this another world?"),
    ("S005_011", "MOBIUS", "Considering his answer", "Let's just say it's a little more complicated."),
    ("S005_012", "TVA AGENT 1", "Procedural", "Prepare the origin analysis."),
    ("S005_013", "TVA AGENT 2", "Confirming", "Scanner ready."),
    ("S005_014", "MOBIUS", "Conversational", "Okay, Steve. Where do you come from?"),
    ("S005_015", "STEVE", "Straightforward", "My world. A village near the mountains."),
    ("S005_016", "MOBIUS", "Patient", "Right. And do you know which universe that is?"),
    ("S005_017", "STEVE", "Baffled", "Which... universe?"),
    ("S005_018", "MOBIUS", "Recognising the problem", "Okay. That's a no."),
    ("S005_019", "TVA AGENT 2", "Reading scanner", "Beginning dimensional origin analysis."),
    ("S005_020", "TVA AGENT 2", "Uneasy", "No matching classification."),
    ("S005_021", "TVA AGENT 1", "Surprised", "Run it again."),
    ("S005_022", "TVA AGENT 2", "Focused", "Second scan complete. Same result."),
    ("S005_023", "MOBIUS", "Serious", "What's it saying?"),
    ("S005_024", "TVA AGENT 2", "Clearly", "Origin universe: unregistered."),
    ("S005_025", "MOBIUS", "Quiet surprise", "Unregistered?"),
    ("S005_026", "STEVE", "Concerned", "What does that mean?"),
    ("S005_027", "MOBIUS", "Choosing his words", "It means we can't find your home universe in our records."),
    ("S005_028", "STEVE", "Alarmed", "Wait. You mean my home is gone?"),
    ("S005_029", "MOBIUS", "Reassuring", "No. That's not what I'm saying. We just don't know where it is."),
    ("S005_030", "STEVE", "Determined", "Then I need to find it."),
    ("S005_031", "MOBIUS", "Thoughtful", "And I think we need to understand how you got here in the first place."),
    ("S005_032", "STEVE", "Tired, frustrated", "Yesterday, I was looking at a map. Now I'm being told nobody knows where my universe is."),
    ("S005_033", "MOBIUS", "Quietly", "Well, Steve... you certainly know how to make an entrance."),
    ("S005_034", "STEVE", "Dry humour", "Believe me. I wasn't trying to."),
    ("S005_035", "MOBIUS", "To the agents", "Keep the file open. I want a full review of that arrival signature."),
    ("S005_036", "TVA AGENT 1", "Acknowledging", "Understood."),
    ("S005_037", "STEVE", "Quietly to himself", "I've got to get home."),
]

# Speaker label (shown above subtitles) for each script speaker, as lang key suffixes.
SPEAKERS = {
    "STEVE": ("steve", "Steve"),
    "CARTOGRAPHER": ("cartographer", "Cartographer"),
    "THE WATCHER": ("watcher", "The Watcher"),
    "UNKNOWN VOICE": ("unknown_voice", "Unknown Voice"),
    "CIVILIAN 1": ("civilian_1", "Civilian"),
    "CIVILIAN 2": ("civilian_2", "Civilian"),
    "ROBBER 1": ("robber_1", "Robber"),
    "ROBBER 2": ("robber_2", "Robber"),
    "SPIDER-MAN": ("spiderman", "Spider-Man"),
    "TVA AGENT 1": ("tva_agent_1", "TVA Agent"),
    "TVA AGENT 2": ("tva_agent_2", "TVA Agent"),
    "MOBIUS": ("mobius", "Mobius"),
}

# Short reply labels where the script's direction describes an action rather than a tone.
DIRECTIONS = {}

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "marvel/src/main/resources/assets/wrmarvel")


def sound_id(line_id):
    return "voice." + line_id.lower()


JAVA = os.path.join(ROOT, "marvel/src/main/java/com/worldsreborn/marvel/MarvelScript.java")


def duration_ms(lid):
    """Length of a line's recording in milliseconds (0 if not recorded yet)."""
    path = os.path.join(ASSETS, "sounds/voice", lid.lower() + ".ogg")
    if not os.path.exists(path):
        return 0
    out = subprocess.run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", path],
                         capture_output=True, text=True).stdout.strip()
    return int(float(out) * 1000) if out else 0


def write_java():
    """Speaker and recording length of every line, so the game can build conversations and time scenes."""
    rows = ",\n".join(f'            Map.entry("{lid}", "{SPEAKERS[sp][0]}")' for lid, sp, _, _ in LINES)
    lengths = ",\n".join(f'            Map.entry("{lid}", {duration_ms(lid)})' for lid, _, _, _ in LINES)
    with open(JAVA, "w", encoding="utf-8") as f:
        f.write(f"""package com.worldsreborn.marvel;

import java.util.Map;

/**
 * Speakers of the user's Scenes 001-005 voice acting script (Draft 1). GENERATED by tools/voice_script.py from the
 * script text; edit the script there, not here. Text is in lang ({{@code line.wrmarvel.<id>}}), audio in
 * {{@code sounds/voice/<id>.ogg}}.
 */
public final class MarvelScript {{
    public static final Map<String, String> SPEAKER = Map.ofEntries(
{rows});

    /** Length of each line's recording in milliseconds. */
    public static final Map<String, Integer> MILLIS = Map.ofEntries(
{lengths});

    private MarvelScript() {{
    }}
}}
""")


def main():
    lang_path = os.path.join(ASSETS, "lang/en_us.json")
    lang = json.load(open(lang_path, encoding="utf-8"))
    for key, (suffix, label) in SPEAKERS.items():
        lang["speaker.wrmarvel." + suffix] = label
    for lid, speaker, direction, text in LINES:
        lang["line.wrmarvel." + lid.lower()] = text
        lang["subtitles.wrmarvel." + lid.lower()] = SPEAKERS[speaker][1] + " speaks"
        if speaker == "STEVE":
            # Steve's lines are offered to the player as replies, labelled with the script's direction.
            lang["tone.wrmarvel." + lid.lower()] = DIRECTIONS.get(lid, direction)
    json.dump(lang, open(lang_path, "w", encoding="utf-8"), indent=2, ensure_ascii=False)

    sounds_path = os.path.join(ASSETS, "sounds.json")
    sounds = {k: v for k, v in json.load(open(sounds_path)).items() if not k.startswith("voice.s0")}
    voice_dir = os.path.join(ASSETS, "sounds/voice")
    recorded = 0
    for lid, _, _, _ in LINES:
        f = os.path.join(voice_dir, lid.lower() + ".ogg")
        if os.path.exists(f):
            recorded += 1
            sounds[sound_id(lid)] = {"category": "voice", "subtitle": "subtitles.wrmarvel." + lid.lower(),
                                     "sounds": [{"name": "wrmarvel:voice/" + lid.lower(), "stream": False}]}
    json.dump(sounds, open(sounds_path, "w"), indent=2)
    print(f"{len(LINES)} lines, {recorded} recorded")
    write_java()

    if len(sys.argv) > 1 and sys.argv[1] == "--markdown":
        out = sys.argv[2]
        with open(out, "w", encoding="utf-8") as f:
            f.write("# Worlds Reborn: Marvel · Scenes 001–005 · Voice acting script, Draft 1\n\n")
            f.write("Provided by the user on 2026-10-17. Replaces Claude's earlier draft (SCENES_001_005_DRAFT_SCRIPT.md). ")
            f.write("One numbered line per audio file; bracketed directions are not spoken.\n\n")
            scene = None
            for lid, speaker, direction, text in LINES:
                if lid[:4] != scene:
                    scene = lid[:4]
                    f.write(f"\n## Scene {scene[1:]}\n\n")
                f.write(f"- **{lid}** {speaker} [{direction}] \"{text}\"\n")
            f.write("\n## Continuity restrictions\n\n"
                    "- No disclosure of Caelum's identity in Scene 001.\n"
                    "- No full explanation of the multiverse from Steve's perspective in Scene 002.\n"
                    "- Tom's Spider-Man is Steve's initial Marvel hero contact.\n"
                    "- The residual dimensional trace fades in Scene 004.\n"
                    "- The TVA reports that Steve's home universe is unregistered, not destroyed.\n"
                    "- The later incursion briefing, hooded traveller identification and TVA attack must not be moved into Scenes 001–005.\n"
                    "- Preserve all existing Marvel Story Reference Board events and update this draft if an original approved line differs.\n"
                    "- S002_023: keep this voice unidentified until the authoritative story board assigns the speaker. Do not automatically credit it to Caelum, Vaelith or the Watcher.\n")


if __name__ == "__main__":
    main()
