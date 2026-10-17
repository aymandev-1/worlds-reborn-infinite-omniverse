package com.worldsreborn.ui.client.cinematic;

import net.minecraft.client.gui.GuiGraphics;

/** Draws a cinematic's visuals. Captions and skip hints are drawn by the screen on top. */
@FunctionalInterface
public interface CinematicRenderer {
    /**
     * @param tick     ticks since the cinematic started, including partial ticks
     * @param progress 0..1 through the cinematic
     * @param argument free text from the server, e.g. a destination name key
     */
    void render(GuiGraphics g, int width, int height, float tick, float progress, String argument);
}
