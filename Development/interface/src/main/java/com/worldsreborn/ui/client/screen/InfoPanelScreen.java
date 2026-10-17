package com.worldsreborn.ui.client.screen;

import com.worldsreborn.ui.client.ThemedButton;
import com.worldsreborn.ui.client.UiConfig;
import com.worldsreborn.ui.client.UiTheme;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * A read-out panel: Peter's scanner, TVA records and similar. Lines appear one after another like a terminal.
 * Style "tva" is amber on brown, "map" is gold on parchment-brown; anything else ("scanner") is cyan on dark blue.
 * A style of the form {@code map:<texture>} uses the map colours and shows that 256x256 picture above the lines.
 */
public class InfoPanelScreen extends Screen {
    private static final long LINE_MS = 350;
    private final String style;
    private final List<String> lineKeys;
    private final long opened = Util.getMillis();
    private final net.minecraft.resources.ResourceLocation image;

    public InfoPanelScreen(String style, String titleKey, List<String> lineKeys) {
        super(Component.translatable(titleKey));
        int colon = style.indexOf(':');
        this.image = colon > 0 ? net.minecraft.resources.ResourceLocation.tryParse(style.substring(colon + 1)) : null;
        this.style = colon > 0 ? style.substring(0, colon) : style;
        this.lineKeys = List.copyOf(lineKeys);
    }

    private int accent() {
        return switch (style) {
            case "tva" -> UiTheme.TVA_AMBER;
            case "map" -> UiTheme.GOLD;
            default -> UiTheme.CYAN;
        };
    }

    private int fill() {
        return switch (style) {
            case "tva" -> 0xF02A1A0C;
            case "map" -> 0xF03A2C1A;
            default -> 0xF00A1420;
        };
    }

    private int panelW() {
        return Math.min(320, width - 20);
    }

    private int imageSize() {
        return image == null ? 0 : Math.max(64, Math.min(180, height - 120 - lineKeys.size() * 14));
    }

    private int panelH() {
        return 44 + (image == null ? 0 : imageSize() + 8) + lineKeys.size() * 14 + 28;
    }

    @Override
    protected void init() {
        int w = panelW();
        int x = (width - w) / 2;
        int y = (height - panelH()) / 2;
        addRenderableWidget(new ThemedButton(x + w - 70, y + panelH() - 26, 62, 18, CommonComponents.GUI_DONE, b -> onClose(), accent()));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0x80000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int w = panelW();
        int h = panelH();
        int x = (width - w) / 2;
        int y = (height - h) / 2;
        g.fill(x, y, x + w, y + h, fill());
        g.renderOutline(x, y, w, h, accent());
        g.fill(x, y + 22, x + w, y + 23, UiTheme.withAlpha(accent(), 0.5f));
        g.drawString(font, title, x + 10, y + 8, accent(), false);

        long elapsed = UiConfig.REDUCE_MOTION.getAsBoolean() ? Long.MAX_VALUE / 2 : Util.getMillis() - opened;
        int shown = (int) Math.min(lineKeys.size(), elapsed / LINE_MS + 1);
        int ly = y + 32;
        if (image != null) {
            int s = imageSize();
            g.blit(image, x + (w - s) / 2, ly, s, s, 0, 0, 256, 256, 256, 256);
            ly += s + 8;
        }
        for (int i = 0; i < shown; i++) {
            g.drawString(font, Component.translatable(lineKeys.get(i)), x + 12, ly, UiTheme.TEXT, false);
            ly += 14;
        }
        if (shown < lineKeys.size() && (Util.getMillis() / 300) % 2 == 0) {
            g.fill(x + 12, ly, x + 18, ly + 8, accent());
        }
    }
}
