package com.worldsreborn.ui.client.screen;

import com.worldsreborn.ui.WRInterface;
import com.worldsreborn.ui.client.ThemedButton;
import com.worldsreborn.ui.client.UiTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

/** Settings entry point from the title screen: vanilla game options, or Worlds Reborn's own HUD/story/accessibility options. */
public class SettingsHubScreen extends Screen {
    private final Screen parent;

    public SettingsHubScreen(Screen parent) {
        super(Component.translatable("menu.wrinterface.settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = width / 2 - 100;
        int y = height / 2 - 34;
        addRenderableWidget(new ThemedButton(x, y, 200, 20, Component.translatable("menu.wrinterface.game_options"),
                b -> minecraft.setScreen(new OptionsScreen(this, minecraft.options))));
        addRenderableWidget(new ThemedButton(x, y + 24, 200, 20, Component.translatable("menu.wrinterface.wr_options"),
                b -> ModList.get().getModContainerById(WRInterface.MODID)
                        .ifPresent(c -> minecraft.setScreen(new ConfigurationScreen(c, this)))));
        addRenderableWidget(new ThemedButton(x, y + 60, 200, 20, CommonComponents.GUI_BACK, b -> onClose(), UiTheme.TEXT_DIM));
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(font, title, width / 2, height / 2 - 56, UiTheme.CYAN);
    }
}
