package com.worldsreborn.ui.client.screen;

import com.worldsreborn.ui.client.ThemedButton;
import com.worldsreborn.ui.client.UiTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Plain credits list. Text lives in the language file so it can be edited without code changes. */
public class CreditsScreen extends Screen {
    private static final int LINES = 8;
    private final Screen parent;

    public CreditsScreen(Screen parent) {
        super(Component.translatable("menu.wrinterface.credits"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addRenderableWidget(new ThemedButton(width / 2 - 50, height - 34, 100, 20, CommonComponents.GUI_BACK, b -> onClose(), UiTheme.TEXT_DIM));
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int w = Math.min(320, width - 20);
        int x = (width - w) / 2;
        int y = 30;
        UiTheme.panel(g, x, y, w, LINES * 14 + 30, UiTheme.EMERALD);
        g.drawCenteredString(font, title, width / 2, y + 8, UiTheme.EMERALD);
        for (int i = 0; i < LINES; i++) {
            g.drawCenteredString(font, Component.translatable("credits.wrinterface.line" + (i + 1)), width / 2, y + 26 + i * 14,
                    i % 2 == 0 ? UiTheme.TEXT : UiTheme.TEXT_DIM);
        }
    }
}
