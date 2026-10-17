# Worlds Reborn

A Minecraft mod series. This folder builds **eight separate mods** (one jar each) for **Minecraft 1.21.1 + NeoForge**.

| Mod | Folder | Jar | What it does |
|---|---|---|---|
| Worlds Reborn: Core | `core` | `worldsreborn-core-1.21.1-0.1.0.jar` | Shared registries, player save profile (with data versions), networking, config, `/wr` dev commands |
| Worlds Reborn: Interface | `interface` | `worldsreborn-interface-…` | Title screen, HUD, quest journal (J), multiverse map (M), dialogue, cinematics, info panels, settings |
| Worlds Reborn: Story | `story` | `worldsreborn-story-…` | Quests, dialogue and cinematic flow, server-side timers and recovery |
| Worlds Reborn: Characters | `characters` | `worldsreborn-characters-…` | Story NPCs, hostile humanoids, character skins |
| Worlds Reborn: Abilities | `abilities` | `worldsreborn-abilities-…` | Ability unlocks, 3 slots (Z/X/C), cooldowns |
| Worlds Reborn: Worlds | `worlds` | `worldsreborn-worlds-…` | Set-piece builder, site records, safe placement, spider-free rule |
| Worlds Reborn: Travel | `travel` | `worldsreborn-travel-…` | Destinations, portals, transit, arrival, checkpoints |
| Worlds Reborn: Marvel | `marvel` | `worldsreborn-marvel-…` | Marvel campaign. This build: Act 1, Scenes 1–16 |

Marvel needs all the others. The other seven don't need Marvel.

## Build

Needs Java 21 (Gradle downloads it automatically if missing).

```
gradlew build                 (Windows: gradlew.bat build)
gradlew :marvel:collectJars   copies all eight jars to build/release
```

## Play from the source (test client)

```
gradlew :marvel:runClient
```

## Install into the normal Minecraft launcher

1. Install NeoForge for Minecraft 1.21.1 (21.1.x).
2. Copy all eight jars from `build/release` into your `.minecraft/mods` folder.
3. Start the NeoForge profile and create a new world.

## Developer commands (need cheats / op)

- `/wr marvel scene <1-16>` jump to the start of a scene
- `/wr marvel restart` start Scene 1 again
- `/wr story status` show your quest state
- `/wr profile show` / `/wr profile reset` show or wipe your Worlds Reborn save
- `/wr travel list` / `/wr travel to <id>` list destinations or travel
- `/wr worlds sites` list built areas

## Regenerating the placeholder art

`python3 tools/gen_textures.py` rewrites the temporary skins and block textures.

See `STATUS.md` for what's done, what's placeholder, and what still needs testing.
