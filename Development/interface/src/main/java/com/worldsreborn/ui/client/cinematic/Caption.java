package com.worldsreborn.ui.client.cinematic;

/**
 * One timed line of cinematic text.
 *
 * @param speakerKey  translation key of the speaker, or null for narration/on-screen text
 * @param placeholder true while the line is temporary and awaits the approved script; shown with a [TEMP] tag
 * @param voice       sound event id of the voice-over played when the caption appears, or null
 */
public record Caption(int fromTick, int toTick, String speakerKey, String textKey, boolean placeholder, String voice) {
    public Caption(int fromTick, int toTick, String speakerKey, String textKey, boolean placeholder) {
        this(fromTick, toTick, speakerKey, textKey, placeholder, null);
    }

    public static Caption text(int from, int to, String textKey) {
        return new Caption(from, to, null, textKey, false);
    }

    public static Caption line(int from, int to, String speakerKey, String textKey) {
        return new Caption(from, to, speakerKey, textKey, false);
    }

    /** A spoken line: caption plus voice-over. */
    public static Caption voiced(int from, int to, String speakerKey, String textKey, String voice) {
        return new Caption(from, to, speakerKey, textKey, false, voice);
    }

    public boolean visibleAt(float tick) {
        return tick >= fromTick && tick < toTick;
    }
}
