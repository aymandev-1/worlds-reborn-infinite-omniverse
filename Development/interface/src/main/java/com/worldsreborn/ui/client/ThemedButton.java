package com.worldsreborn.ui.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * A flat button in the Worlds Reborn style: dark fill, emerald edge when hovered, grey when disabled. On hover it
 * eases a few pixels to the right and an accent glow fills in from the left (instant with reduceMotion).
 */
public class ThemedButton extends Button {
    private final int accent;
    private float hover;

    public ThemedButton(int x, int y, int w, int h, Component message, OnPress onPress) {
        this(x, y, w, h, message, onPress, UiTheme.EMERALD);
    }

    public ThemedButton(int x, int y, int w, int h, Component message, OnPress onPress, int accent) {
        super(x, y, w, h, message, onPress, DEFAULT_NARRATION);
        this.accent = accent;
    }

    @Override
    public void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        boolean hot = active && isHoveredOrFocused();
        float target = hot ? 1f : 0f;
        hover = UiConfig.REDUCE_MOTION.getAsBoolean() ? target : hover + (target - hover) * 0.25f;
        int x = getX() + Math.round(hover * 4);
        int y = getY();
        g.fill(x, y, x + width, y + height, UiTheme.panelColor());
        if (hover > 0.01f) {
            int glow = (int) (width * 0.7f * hover);
            g.fillGradient(x, y, x + glow, y + height, UiTheme.withAlpha(accent, 0.28f * hover), UiTheme.withAlpha(accent, 0.10f * hover));
        }
        g.renderOutline(x, y, width, height, !active ? 0xFF3A4048 : hot ? accent : 0xFF4A5560);
        if (hover > 0.01f) {
            g.fill(x, y, x + 2, y + height, UiTheme.withAlpha(accent, hover));
        }
        int color = active ? (hot ? UiTheme.TEXT : UiTheme.TEXT_DIM) : UiTheme.TEXT_MUTED;
        var font = Minecraft.getInstance().font;
        g.drawCenteredString(font, getMessage(), x + width / 2, y + (height - 8) / 2, color);
    }
}
