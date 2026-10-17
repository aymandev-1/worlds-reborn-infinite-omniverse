# Worlds Reborn: Infinite Omniverse — Master Development Plan

Recorded 2026-10-17 from the project owner's plan. This is the roadmap; [`Development/STATUS.md`](../Development/STATUS.md)
remains the source of truth for what is actually built.

## 1. Campaign order (narrative scope, not a build order)

| # | Project | Scope |
|---:|---|---|
| 01 | **Marvel: The Multiversal Saga** | Steve travels from MC-616 into MU-616: alliances, incursions, Doomsday, Secret Wars, the Ultimate Reset. **Current production priority.** See [`story/MARVEL_CAMPAIGN_OUTLINE.md`](story/MARVEL_CAMPAIGN_OUTLINE.md). |
| 02 | DC Universe | Steve enters Gotham through the blue portal after the Marvel reset. Batman and the Bat-Family; 150-scene main campaign plus optional character storylines. |
| 03 | Ben 10 | Omniverse-era setting after DC. Ben and Rook are the first contacts; 120-scene crossover with Omnitrix transformations, alien abilities, exploration. |
| 04 | Video Games | Separate adaptations: Sonic, GTA, Zelda, Pokémon, Red Dead Redemption, Assassin's Creed and others. |
| 05 | Anime & Manga | Dragon Ball, Naruto/Boruto, One Piece, Bleach, Jujutsu Kaisen, Demon Slayer — each keeps its own powers, progression and continuity. |
| 06 | Film & TV | Star Wars, Doctor Who, Star Trek, Transformers, Jurassic Park/World, The Matrix. |
| 07 | Literature | Middle-earth, Wizarding World, Narnia, Percy Jackson, Dune, His Dark Materials. |
| 08 | Original Worlds | Original universes with their own protagonists, civilisations, powers and factions, gradually linking into the Omniverse. |
| 09 | CHRONOVERSE: Infinite Omniverse | Multiple distinct Steves, Alex, Herobrine, ancient civilisations, timeline fractures. MC-616, MC-100, MC-101 etc. stay distinct; the ultimate origin of the Omniverse stays intentionally unresolved. |

## 2. Shared technical foundation

Every universe reuses the same core systems where possible, while keeping its own gameplay, look, characters and history.

| System | Covers | Module today |
|---|---|---|
| Story & Campaigns | Scenes, missions, dialogue, checkpoints | `story` |
| Characters | Rigs, costumes, NPCs, factions | `characters` |
| Abilities & Combat | Powers, combos, transformations | `abilities` |
| Worlds & Travel | Portals, dimensions, environments | `worlds`, `travel` |
| Cinematics & Audio | Camera, animation, subtitles, voice | `interface` (+ `story` timing) |
| Progression & Saving | Persistent state, multiplayer recovery | `core` |
| Vehicles | Road, air, space, special travel | not started |
| Interface | Main menu, HUD, journal, map | `interface` |

Verified target (from the repository, 2026-10-17): **Minecraft 1.21.1, NeoForge 21.1.77, Java 21, Gradle 8.14.3**.
Earlier handoffs mentioning Minecraft 1.20.1 / Forge 47.2.0 are out of date.

## 3. Roadmap

1. **Audit and repair** — preserve existing work, verify versions, compile, find what actually runs in game.
   *Done when:* the project builds, launches, and loads existing saves without losing progress.
2. **Finish the shared gameplay systems** — cutscenes, quest triggers, actor movement, dialogue, subtitles, camera,
   combat, NPC navigation, saving, portals, character switching.
   *Done when:* one complete mission exercises every system, including save/reload and failure recovery.
3. **Marvel Scenes 001–016** per the approved scripts (opening, TVA, evacuation, Watchers, Spider-Man, Avengers,
   New Avengers, Fantastic Four, X-Men).
   *Done when:* all 16 scenes play consecutively with correct characters, events, checkpoints and transitions.
4. **The rest of Marvel** — Gathering Crisis, Doomsday, Secret Wars, the ten-scene Ultimate Reset.
   *Done when:* playable from Steve's village to the blue portal to Gotham.
5. **DC and Ben 10** — DC's 150 scenes and Ben 10's 120 on the finished systems.
6. **Remaining franchise categories** — self-contained campaigns reusing the core systems.
7. **CHRONOVERSE integration.**
8. **Final QA and release preparation** — performance, multiplayer, continuity, model/audio quality, third-party asset
   permissions, appropriately scoped distribution.

## 4. Immediate focus: Marvel Scenes 006–016

| Scene | Playable character | Primary feature |
|---|---|---|
| 006–010 | Steve | TVA investigations, evacuations, breach, Watchers |
| 011 | Spider-Man | Scanner, rescue, Defenders meeting |
| 012 | Sam Wilson | Flight mechanics, helicopter rescue |
| 013 | Bucky Barnes | New Avengers team rescue |
| 014 | Sam Wilson | Joint briefing, inter-universe communication |
| 015 | Sue Storm | Force fields, Fantastic Four laboratory |
| 016 | Cyclops | Optic blasts, X-Men monitoring-station rescue |

## 5. Asset pipeline (every major character)

Reference → Skin/Model → Rig → Animation → Abilities → Voice & Audio → Integration → In-game QA.

Tools: GitHub + Claude Code (source, implementation); Blockbench and Blender (models, rigs, animation); texture
tools (costumes); a properly licensed voice-recording workflow; Minecraft itself for final verification.
An AI-generated reference image is not a finished skin, and a compiled mod is not a polished cinematic — both need
separate approval.

## 6. Release-quality checklist (every finished scene)

- Correct characters with recognisable, distinct costumes.
- Environment matches the script and visual references.
- Cutscenes use intentional camera work and character animation.
- Dialogue and subtitles in sync; missing voice recordings explicitly tracked.
- Player actions have consequences and genuine objectives.
- Rescue missions verify the people actually reach safety.
- Abilities, combat and traversal behave consistently.
- Saving, reloading, skipping cutscenes and multiplayer don't break progression.
- Performance acceptable on target hardware.
- The scene connects correctly to the next one.

For any public release: franchise characters, community skins, music and voice performances cannot be redistributed
just because they are accessible online.
