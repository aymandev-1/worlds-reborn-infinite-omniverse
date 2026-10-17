# Worlds Reborn — status (milestone 1: Marvel Act 1, Scenes 1–5)

Last updated 2026-10-16. Status words: IMPLEMENTED (code written), COMPILED, TESTED, VERIFIED IN GAME.

## What has actually been checked

- Core save/quest logic: 11 unit tests pass.
- All eight mods COMPILED on Ayman's PC (`gradlew build`, 2026-10-16) and load together in the NeoForge 1.21.1 test client; content registers (2 universes, 5 quests, 5 dialogues, 3 destinations). New York and TVA dimensions load.
- First play session found robbers refilling and wandering out of the shop; fixed (spawn only when none are left, robbers return to their post, slightly weaker). Fix compiles; not re-played yet.
- Scenes 1-5 have not yet been played start to finish.

## Implemented (code written)

**Core**: content registries; per-player profile saved with the player (quests, flags, unlocks, abilities, cooldowns, pending dialogue, cinematic and transit state, objective target, checkpoint, return point) with a data version and migration hook; validated network messages; `/wr profile`, `/wr content`.

**Interface**
- Title screen: Continue (latest world), Singleplayer, Multiplayer, Settings, Credits, Mods, Quit. Can be switched back to vanilla in settings.
- HUD: tracked quest top-left with distance; quest banners top-centre; "[Right-click] Talk to …" prompts under the crosshair; ability slots beside the hotbar (only once one is equipped). Hidden with F1.
- Quest journal (J): main, optional, completed quests; current step; future steps hidden; Track button; Abilities tab.
- Multiverse map (M): destinations, locked/unlocked/here, hidden ones as ???, travel when allowed, future franchises listed as planned.
- Dialogue: bottom panel, typed text, speaker name, tone-labelled choices (number keys work), Esc skips.
- Cinematics: letterbox/black/travel visuals, timed captions, Esc to skip (setting), server-timed so they can't trap you.
- Info panels: scanner, TVA, map styles.
- Settings: quest tracker, distance, prompts, dialogue speed, cinematic skip, text size, high contrast, reduce motion.

**Story / Characters / Abilities / Worlds / Travel**: as described in README. Spider-free: spiders and cave spiders don't spawn (setting, on by default).

**Marvel, Act 1**
1. The Map: a village is built near spawn; reach it; talk to the cartographer; map panel (Before/Today); the hooded traveller appears at the village edge and vanishes when looked at; head to the ruins.
2. Beneath the Ruins: stairs to a buried chamber; fill three gaps in a redstone line (redstone in the chest) and pull the lever; the silver-blue portal opens; step in; "falling through realities" cinematic with the traveller's line "And it starts."
3. A Stranger in New York: night street in MU-616 (own dimension); stop three robbers in the shop while customers cower; Spider-Man drops in; talk.
4. The Fading Trace: walk Peter to the alley; he scans the trace (scanner panel); he gets an emergency and leaves.
5. An Unrecorded Traveller: Mobius appears; orange time-door cinematic to the TVA (own dimension); records terminal shows "ORIGIN UNIVERSE: UNREGISTERED".
End of milestone: "To be continued" banner. Free travel is switched on as a temporary measure so you aren't stuck in the TVA.

## Placeholders (must be replaced)

- **All dialogue** except "And it starts." is a one-line placeholder describing the scene beat, shown with a [TEMP] tag. The approved scripts haven't been written.
- Map, scanner and TVA read-out lines are placeholder (marked with a [TEMP] note), except "ORIGIN UNIVERSE: UNREGISTERED".
- Character skins are simple generated stand-ins (Spider-Man, Mobius, hooded traveller, robber, three customers). The cartographer uses the vanilla villager texture. Final models are still to be made.
- Block textures (portal, core, trace, terminal) are generated stand-ins.
- Buildings are built from code with vanilla blocks.

## Known issues / risks

- First join builds the village and ruins at once, which can cause a short pause.
- Free travel at the end of Scene 5 is temporary.
- Multiplayer: scenes work per player, but shared NPCs (robbers, Spider-Man) aren't co-ordinated between several players on different steps.

## Planned (not built)

Scenes 6–160; vehicles and spacecraft; multiplayer party travel; character/equipment screen; DC, Ben 10, Video Games and the rest; CHRONOVERSE.

## Next milestone

Compile and fix on Ayman's PC, play Scenes 1–5 start to finish, then Scenes 6–10 (Beyond the Records → Sanctuary of the Watchers) once their scripts are approved.
