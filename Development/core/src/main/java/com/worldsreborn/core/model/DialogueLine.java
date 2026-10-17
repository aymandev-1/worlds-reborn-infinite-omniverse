package com.worldsreborn.core.model;

/**
 * @param placeholder true when the text is temporary and must be replaced by the approved script
 * @param voice       sound event id of the voice-over for this line, or null
 * @param toneKey     non-null for a line the player's character says: the conversation pauses and offers it as a
 *                    reply, labelled with this tone, until the player picks it
 * @param images      textures shown above the conversation while this line is on screen (for example the maps being
 *                    talked about), as resource locations separated by {@code |}; null for none
 */
public record DialogueLine(String speakerKey, String textKey, boolean placeholder, String voice, String toneKey, String images) {
    public DialogueLine(String speakerKey, String textKey, boolean placeholder) {
        this(speakerKey, textKey, placeholder, null, null, null);
    }

    public DialogueLine(String speakerKey, String textKey, boolean placeholder, String voice) {
        this(speakerKey, textKey, placeholder, voice, null, null);
    }

    public DialogueLine(String speakerKey, String textKey, boolean placeholder, String voice, String toneKey) {
        this(speakerKey, textKey, placeholder, voice, toneKey, null);
    }

    public boolean isPlayerReply() {
        return toneKey != null;
    }

    public DialogueLine withImages(String images) {
        return new DialogueLine(speakerKey, textKey, placeholder, voice, toneKey, images);
    }
}
