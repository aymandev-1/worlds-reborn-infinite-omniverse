package com.worldsreborn.ui.client;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Client settings, editable in game from Mods > Worlds Reborn: Interface > Config, or the title screen's Settings. */
public final class UiConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue REPLACE_TITLE_SCREEN = B
            .comment("Use the Worlds Reborn title screen instead of the vanilla one")
            .define("replaceTitleScreen", true);

    public static final ModConfigSpec.BooleanValue PLAY_AS_STEVE = B
            .comment("Draw every player with Steve's skin (Worlds Reborn's hero is MC-616 Steve)")
            .define("playAsSteve", true);
    public static final ModConfigSpec.BooleanValue SKIP_VANILLA_TUTORIAL = B
            .comment("Turn off the vanilla 'punch a tree' tutorial pop-ups")
            .define("skipVanillaTutorial", true);

    static {
        B.push("hud");
    }

    public static final ModConfigSpec.BooleanValue SHOW_QUEST_TRACKER = B
            .comment("Show the tracked quest in the top-left corner")
            .define("showQuestTracker", true);
    public static final ModConfigSpec.BooleanValue SHOW_OBJECTIVE_DISTANCE = B
            .comment("Show the distance to the current objective")
            .define("showObjectiveDistance", true);
    public static final ModConfigSpec.BooleanValue SHOW_INTERACTION_PROMPTS = B
            .comment("Show prompts such as 'Talk' under the crosshair")
            .define("showInteractionPrompts", true);

    static {
        B.pop();
        B.push("story");
    }

    public static final ModConfigSpec.IntValue DIALOGUE_SPEED = B
            .comment("Dialogue letters per second. 0 shows each line instantly")
            .defineInRange("dialogueSpeed", 45, 0, 200);
    public static final ModConfigSpec.BooleanValue ALLOW_CINEMATIC_SKIP = B
            .comment("Allow Esc to skip cinematics")
            .define("allowCinematicSkip", true);

    static {
        B.pop();
        B.push("accessibility");
    }

    public static final ModConfigSpec.DoubleValue TEXT_SCALE = B
            .comment("Scale of dialogue, captions and the quest tracker")
            .defineInRange("textScale", 1.0, 1.0, 2.0);
    public static final ModConfigSpec.BooleanValue HIGH_CONTRAST = B
            .comment("Opaque panels behind all Worlds Reborn text")
            .define("highContrast", false);
    public static final ModConfigSpec.BooleanValue REDUCE_MOTION = B
            .comment("Turn off swirling, sliding and flashing effects")
            .define("reduceMotion", false);
    public static final ModConfigSpec.BooleanValue REDUCE_FLASHING = B
            .comment("Replace bright flashes and flickering static with gentle fades")
            .define("reduceFlashing", false);

    static {
        B.pop();
    }

    public static final ModConfigSpec SPEC = B.build();

    private UiConfig() {
    }

    public static float textScale() {
        return (float) TEXT_SCALE.getAsDouble();
    }
}
