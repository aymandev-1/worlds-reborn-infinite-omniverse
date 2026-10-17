package com.worldsreborn.ui.client.cinematic;

import java.util.List;

/**
 * Client-side presentation of a cinematic. The server only sends the id and duration and runs its own timer, so a
 * missing or broken definition can never trap the player.
 *
 * @param titleKey optional heading shown during the first seconds, or null
 */
public record CinematicDef(String id, CinematicRenderer renderer, String titleKey, List<Caption> captions) {
    public CinematicDef {
        captions = captions == null ? List.of() : List.copyOf(captions);
    }
}
