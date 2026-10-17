"""Static checks for Act 1 Scenes 12-16: run after editing the scene code, the script or the lang file.

- Every lang key the scene code uses (messages, panels, toasts, prompts, cinematic text, quests, abilities) exists.
- Every dialogue / bark group the code opens or speaks is in MarvelScript1216, and every conversation in the script is
  used somewhere (no approved line silently left out).
- Every character and hero has a skin; every block state and item has its model and texture.
- Every scene line in lang matches the approved manifest word for word.
Exits non-zero on any problem. Run from the project root: python3 tools/check_12_16.py
"""
import csv
import json
import os
import re
import sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
J = os.path.join(ROOT, "marvel/src/main/java/com/worldsreborn/marvel")
A = os.path.join(ROOT, "marvel/src/main/resources/assets/wrmarvel")
SOURCES = [os.path.join(J, "doomsday/Responders.java"), os.path.join(J, "doomsday/RespondersSites.java"),
           os.path.join(J, "doomsday/StoryControlBlock.java"), os.path.join(J, "client/RespondersClient.java"),
           os.path.join(J, "MarvelContent.java")]
MANIFEST = os.path.join(ROOT, "../docs/story/scenes_12_16/VOICE_AND_SUBTITLE_MANIFEST.csv")

problems = []
lang = json.load(open(os.path.join(A, "lang/en_us.json"), encoding="utf-8"))
code = "\n".join(open(p, encoding="utf-8").read() for p in SOURCES)
script = open(os.path.join(J, "MarvelScript1216.java"), encoding="utf-8").read()
groups = set(re.findall(r'Map\.entry\("([a-z0-9_#]+)", List\.of', script))

# Literal lang keys.
for key in sorted(set(re.findall(r'"((?:message|panel|toast|cinematic)\.wrmarvel\.[a-z0-9_.]+)"', code))):
    if key.endswith("."):
        continue  # a prefix completed in code; checked below
    if key not in lang:
        problems.append("missing lang: " + key)
# Keys built from a prefix in the client cards and the aim read-out.
for scene in ("s12", "s13", "s14", "s15", "s16"):
    for part in ("scene", "who", "where"):
        if f"cinematic.wrmarvel.{scene}_title.{part}" not in lang:
            problems.append(f"missing lang: cinematic.wrmarvel.{scene}_title.{part}")
for aim in ("none", "debris", "support", "person"):
    if "message.wrmarvel.r.aim." + aim not in lang:
        problems.append("missing lang: message.wrmarvel.r.aim." + aim)
for kind in re.search(r"enum Kind implements StringRepresentable \{\s*([A-Z_, ]+);", code).group(1).split(","):
    k = kind.strip().lower()
    if "prompt.wrmarvel.control." + k not in lang:
        problems.append("missing prompt: " + k)
    if not os.path.exists(os.path.join(A, "textures/block/story_control_" + k + ".png")):
        problems.append("missing control texture: " + k)

# Quests and steps.
ids = open(os.path.join(J, "MarvelIds.java"), encoding="utf-8").read()
for n in range(12, 17):
    q = f"quest.wrmarvel.wr1_0{n}"
    for k in (q, q + ".summary"):
        if k not in lang:
            problems.append("missing lang: " + k)
    for step in re.findall(rf'S{n}_[A-Z_]+ = "([a-z0-9_]+)"', ids):
        if f"{q}.{step}" not in lang:
            problems.append(f"missing step lang: {q}.{step}")

# Abilities.
for a in re.findall(r'public static final String (?:SAM_FLIGHT|BUCKY_LIFT|SUE_FIELD|SUE_RELEASE|OPTIC_BLAST) = NS \+ "([a-z_]+)"', code):
    for k in (f"ability.wrmarvel.{a}", f"ability.wrmarvel.{a}.desc"):
        if k not in lang:
            problems.append("missing lang: " + k)

# Dialogue groups used by the code exist; every conversation and bark in the script is used.
used = set(re.findall(r'(?:bark|autoOpen|open|speakingTicks|heard)\(player, "([a-z0-9_]+)"', code))
used |= set(re.findall(r'"open:([a-z0-9_]+)"', code))
used |= set(re.findall(r'\? "([db]1[2-6][a-z0-9_]+)" : "([db]1[2-6][a-z0-9_]+)"', code) and
            [g for pair in re.findall(r'\? "([db]1[2-6][a-z0-9_]+)" : "([db]1[2-6][a-z0-9_]+)"', code) for g in pair])
used |= set(re.findall(r'case "([db]1[2-6][a-z0-9_]+)"', code))
for g in sorted(used):
    if g not in groups:
        problems.append("code uses unknown group: " + g)
for g in sorted(groups):
    if "#" in g or g.endswith("_rejoin"):
        continue
    if g not in used:
        problems.append("script group never used: " + g)

# Characters and hero skins.
names = dict(re.findall(r'public static final String ([A-Z_]+) = NS \+ "([a-z0-9_]+)";', code))
lists = re.findall(r'List\.of\(NS \+ "([a-z0-9_]+)", NS \+ "([a-z0-9_]+)"\)', code)
chars = set(names.values()) | {n for pair in lists for n in pair}
skins = set(re.findall(r'[A-Z_]+\("([a-z_]+)", List\.of', code))
for c in sorted(chars):
    if c.startswith(("sam_flight", "bucky_lift", "sue_field", "sue_release", "optic_blast")) or c == "r_heli":
        continue
    if not os.path.exists(os.path.join(A, "textures/entity", c + ".png")):
        problems.append("missing skin: " + c)
    if c not in skins and "character.wrmarvel." + c not in lang and c not in ("sam_wilson", "sue_storm", "cyclops"):
        problems.append("missing character name: " + c)
for s in skins:
    if not os.path.exists(os.path.join(A, "textures/entity", s + ".png")):
        problems.append("missing hero skin: " + s)

# Pictures, blocks, items.
for pic in re.findall(r'GUI \+ "([a-z_]+\.png)"', code):
    if not os.path.exists(os.path.join(A, "textures/gui/cinematic", pic)):
        problems.append("missing picture: " + pic)
for name in ("story_control", "force_field"):
    if not os.path.exists(os.path.join(A, "blockstates", name + ".json")):
        problems.append("missing blockstate: " + name)
if not os.path.exists(os.path.join(A, "models/item/monitoring_recorder.json")):
    problems.append("missing item model: monitoring_recorder")

# Lines match the approved manifest.
with open(MANIFEST, encoding="utf-8-sig") as f:
    for r in csv.DictReader(f):
        if lang.get("line.wrmarvel." + r["id"]) != r["text"]:
            problems.append("line differs from manifest: " + r["id"])

for p in problems:
    print(p)
print(f"{len(problems)} problem(s)")
sys.exit(1 if problems else 0)
