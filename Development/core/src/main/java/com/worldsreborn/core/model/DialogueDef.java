package com.worldsreborn.core.model;

import java.util.List;

/** A linear conversation, optionally ending in a choice. */
public record DialogueDef(String id, List<DialogueLine> lines, List<DialogueChoice> choices) {
    public DialogueDef {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Dialogue " + id + " needs at least one line");
        }
        lines = List.copyOf(lines);
        choices = choices == null ? List.of() : List.copyOf(choices);
    }
}
