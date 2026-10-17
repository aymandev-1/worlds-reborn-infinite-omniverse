# WORLDS REBORN: INFINITE OMNIVERSE

> **A story-driven Minecraft multiverse adventure where realities collide, hidden records surface, and every world is connected.**

**WORLDS REBORN: INFINITE OMNIVERSE** is an ambitious, modular Minecraft project built around an expanding narrative: begin with a mystery in a quiet village, cross into Marvel's universe, uncover an impossible origin, and eventually follow the trail across an entire omniverse.

The project is being developed as eight cooperating NeoForge mods rather than one monolithic jar. That structure keeps shared systems reusable while allowing each campaign and universe to grow independently. The current playable foundation is the opening Marvel campaign milestone: **Act 1, Scenes 1-11** are implemented and compiled, with full start-to-finish playtesting still in progress.

## Repository description

> WORLDS REBORN: INFINITE OMNIVERSE is a story-driven Minecraft NeoForge project spanning Marvel and a planned multiverse of worlds, quests, characters, abilities, portals, and cinematic adventures.

## Current status

| Area | Status |
| --- | --- |
| Minecraft target | 1.21.1 |
| Mod loader | NeoForge 21.1.77 |
| Java target | Java 21 |
| Build system | Gradle wrapper 8.14.3 |
| Architecture | Eight cooperating modules, one jar per module |
| Current milestone | Marvel Act 1, Scenes 1-11 |
| Implementation state | Scenes 1-11 are implemented and compiled |
| Playthrough state | Scenes 1-11 have not yet been played from start to finish |

The latest project status, known issues, placeholders, and testing notes are maintained in [`Development/STATUS.md`](Development/STATUS.md).

## What is implemented

### Eight-module foundation

| Module | Responsibility |
| --- | --- |
| `core` | Shared registries, player save profiles, data versions, networking, configuration, and `/wr` development commands |
| `interface` | Title screen, HUD, quest journal, multiverse map, dialogue, cinematics, information panels, and settings |
| `story` | Quests, dialogue and cinematic flow, server-side timers, and recovery |
| `characters` | Story NPCs, hostile humanoids, and character skins |
| `abilities` | Ability unlocks, three ability slots (`Z`/`X`/`C`), and cooldowns |
| `worlds` | Set-piece building, site records, safe placement, and the spider-free rule |
| `travel` | Destinations, portals, transit, arrivals, and checkpoints |
| `marvel` | The first campaign, including Marvel Act 1 Scenes 1-11 |

### Marvel Act 1: Scenes 1-5

1. **The Map** - A village, cartographer, map panel, hooded traveller, and ruins establish the mystery.
2. **Beneath the Ruins** - A buried chamber, redstone mechanism, silver-blue portal, and reality-falling cinematic open the path forward.
3. **A Stranger in New York** - The player reaches the MU-616 dimension, confronts three robbers, and meets Spider-Man.
4. **The Fading Trace** - Peter is escorted to an alley where a scanner reveals a trace before an emergency pulls him away.
5. **An Unrecorded Traveller** - Mobius arrives, an orange time-door leads to the TVA, and a records terminal reports `ORIGIN UNIVERSE: UNREGISTERED`.

### Marvel Act 1 "Doomsday": Scenes 6-11

6. **Beyond the Records** - Mobius questions Steve, the Hooded Traveller is spotted in arrival footage, and an incursion evacuation fails.
7. **Those Who Escaped** - Steve helps TVA refugees: clears a route, delivers supplies and reunites a family.
8. **The Breach** - Attackers hit the TVA; Steve builds cover and escorts refugees while Mobius is separated.
9. **The Last Door** - The refugees reach safety, Steve's gateway fails, and an unknown doorway carries him away.
10. **Sanctuary of the Watchers** - Uatu shelters Steve and he watches the TVA fall.
11. **The Missing Traveller** - Play as Spider-Man: scan the alley, rescue accident victims, record another Earth in the sky and meet the Defenders.

Free travel is temporarily enabled at the end of Scene 11 so players are not stranded while Scene 12 is in development.

## Planned features and future scope

The following items are planned, not complete:

- Marvel Scenes 6-160 and the continuation of the first campaign.
- Final approved dialogue, map text, scanner text, and TVA read-outs.
- Final character models and skins to replace generated stand-ins.
- Final block and set-piece art to replace temporary generated textures and code-built vanilla-block structures.
- Vehicles and spacecraft.
- Multiplayer party travel and better coordination for shared story NPCs.
- Character and equipment screens.
- Additional franchise campaigns and their connected worlds.
- The larger CHRONOVERSE storyline.

Future franchises and features should be understood as roadmap scope, not current completion claims.

## Story order

The planned narrative is designed to expand from a focused first campaign into a connected omniverse. Only the Marvel opening milestone is currently implemented.

| Order | Campaign / story phase | Status | Scope |
| ---: | --- | --- | --- |
| 1 | **Marvel** | **Implemented milestone** | Act 1, Scenes 1-11 are implemented and compiled; Scenes 12-160 are planned |
| 2 | **DC** | Planned | Future franchise campaign |
| 3 | **Ben 10** | Planned | Future franchise campaign |
| 4 | **Video Games** | Planned | Future franchise campaign |
| 5 | **Additional universes** | Planned | Further worlds and franchises as the project expands |
| 6 | **CHRONOVERSE** | Planned | The larger crossover and omniverse phase |

This order is a high-level roadmap. It does not imply that planned campaigns are already built or playable.

## Project structure

```text
Worlds Reborn/
├── Development/
│   ├── core/
│   ├── interface/
│   ├── story/
│   ├── characters/
│   ├── abilities/
│   ├── worlds/
│   ├── travel/
│   ├── marvel/
│   ├── build.gradle
│   ├── gradle.properties
│   ├── gradlew
│   ├── gradlew.bat
│   ├── settings.gradle
│   ├── README.md
│   └── STATUS.md
├── Reference Images/
└── README.md
```

The Gradle project lives under `Development`. Each module produces its own jar; the `marvel` module depends on the other seven modules and provides the development client run configuration.

## Build and run

Run these commands from the `Development` directory.

### Build all modules

```text
gradlew.bat build
```

This builds the eight-module project with the configured Minecraft 1.21.1 and NeoForge 21.1.77 setup.

### Collect the release jars

```text
gradlew.bat :marvel:collectJars
```

The task copies all eight module jars into `Development/build/release`.

### Launch the source test client

```text
gradlew.bat :marvel:runClient
```

The development client is configured by the `marvel` module and loads the cooperating modules together.

### Install into a normal Minecraft launcher

1. Install NeoForge for Minecraft 1.21.1, using a compatible 21.1.x release.
2. Run `gradlew.bat :marvel:collectJars`.
3. Copy all eight jars from `Development/build/release` into the launcher's `.minecraft/mods` folder.
4. Start the NeoForge profile and create a new world.

This repository does not currently publish a packaged download link.

## Active development

The active priority is to compile and fix the current build on the development machine, play Marvel Act 1 Scenes 1-11 from start to finish, and then move into Scenes 12-16 once their approved scripts are added. The full roadmap is in [`docs/MASTER_PLAN.md`](docs/MASTER_PLAN.md) and the 160-scene Marvel outline (with approved vs proposed scenes marked) is in [`docs/story/MARVEL_CAMPAIGN_OUTLINE.md`](docs/story/MARVEL_CAMPAIGN_OUTLINE.md).

Known development considerations include:

- The first join builds the village and ruins together, which can cause a short pause.
- Free travel at the end of Scene 11 is temporary.
- Multiplayer scenes are per-player, while shared NPC behavior is not yet coordinated between players on different story steps.
- Most dialogue and several information read-outs remain one-line placeholders marked for replacement.

For the detailed implementation inventory and test history, see [`Development/STATUS.md`](Development/STATUS.md). For module-specific build notes, see [`Development/README.md`](Development/README.md).

## Developer commands

When cheats or operator permissions are available in-game, the current development commands include:

```text
/wr marvel scene <1-11>
/wr marvel restart
/wr story status
/wr profile show
/wr profile reset
/wr travel list
/wr travel to <id>
/wr worlds sites
```

These are development and testing tools, not a finalized player-facing command set.

## Contributing and development notes

Worlds Reborn is an active personal development project. Contributions, technical feedback, testing notes, and careful issue reports are welcome as the project evolves.

When working on the codebase:

- Keep shared systems in the appropriate module instead of adding campaign-specific behavior to `core`.
- Preserve the eight-module dependency direction; the Marvel campaign consumes the shared modules.
- Treat `Development/STATUS.md` as the source of truth for what is implemented, compiled, tested, verified in-game, placeholder, or planned.
- Do not describe future franchises, scenes, assets, or features as complete unless the status documentation confirms them.
- Test changes against the configured Minecraft 1.21.1, NeoForge 21.1.77, and Java 21 toolchain.

## Topics / tags

`minecraft` `minecraft-mod` `neoforge` `java` `gradle` `multiverse` `omniverse` `story-driven` `quests` `cinematics` `marvel` `worldbuilding` `modular`

## Disclaimer

**WORLDS REBORN: INFINITE OMNIVERSE is an unofficial, fan-made Minecraft project.** It is not affiliated with, endorsed by, sponsored by, or approved by Mojang Studios, Microsoft, Marvel, DC, Cartoon Network, or any other referenced rights holder. All referenced third-party characters, settings, names, and trademarks remain the property of their respective owners. This project is intended for creative, non-commercial fan development and personal testing.
