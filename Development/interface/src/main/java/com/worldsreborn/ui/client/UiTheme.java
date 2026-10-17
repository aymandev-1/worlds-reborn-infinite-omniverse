package com.worldsreborn.ui.client;

import net.minecraft.client.gui.GuiGraphics;

/**
 * The Worlds Reborn look: dark translucent panels, white text, emerald and cyan highlights, gold quest markers.
 * Every screen and HUD element draws through here so the style stays consistent.
 */
public final class UiTheme {
    public static final int TEXT = 0xFFFFFFFF;
    public static final int TEXT_DIM = 0xFFB4BCC4;
    public static final int TEXT_MUTED = 0xFF7A848E;
    public static final int EMERALD = 0xFF2ECC71;
    public static final int CYAN = 0xFF3FD0E0;
    public static final int GOLD = 0xFFF2C94C;
    public static final int DANGER = 0xFFE5534B;
    public static final int SILVER_BLUE = 0xFFB8D8F0;
    public static final int TVA_AMBER = 0xFFE8A33D;

    private UiTheme() {
    }

    public static int panelColor() {
        return UiConfig.HIGH_CONTRAST.getAsBoolean() ? 0xF0080A0D : 0xB0101418;
    }

    /** Dark panel with a thin accent line along the top. */
    public static void panel(GuiGraphics g, int x, int y, int w, int h, int accent) {
        g.fill(x, y, x + w, y + h, panelColor());
        g.fill(x, y, x + w, y + 1, accent);
    }

    /** Dark panel with a full one-pixel border. */
    public static void framed(GuiGraphics g, int x, int y, int w, int h, int border) {
        g.fill(x, y, x + w, y + h, panelColor());
        g.renderOutline(x, y, w, h, border);
    }

    public static int withAlpha(int color, float alpha) {
        int a = Math.max(0, Math.min(255, (int) (alpha * 255)));
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
