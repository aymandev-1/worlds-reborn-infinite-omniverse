package com.worldsreborn.core.model;

import java.util.List;

/**
 * One reply option. {@code toneKey} is the short tone label (Practical, Kind, Sarcastic...), {@code textKey} the line
 * the player's character says. {@code voice} is that line's voice-over (or null), and {@code reply} the lines the
 * other character answers with before the conversation ends (may be empty).
 */
public record DialogueChoice(String id, String toneKey, String textKey, String voice, List<DialogueLine> reply) {
    public DialogueChoice {
        reply = reply == null ? List.of() : List.copyOf(reply);
    }

    public DialogueChoice(String id, String toneKey, String textKey) {
        this(id, toneKey, textKey, null, List.of());
    }
}
