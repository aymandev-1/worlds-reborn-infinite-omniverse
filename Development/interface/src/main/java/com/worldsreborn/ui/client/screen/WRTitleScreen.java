package com.worldsreborn.ui.client.screen;

import com.worldsreborn.ui.client.ThemedButton;
import com.worldsreborn.ui.client.UiTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.neoforged.neoforge.client.gui.ModListScreen;

import java.util.Comparator;

/**
 * Worlds Reborn title screen: Continue, Singleplayer, Multiplayer, Settings, Credits, Mods and Quit, on the left so
 * the panorama stays visible. Continue opens the most recently played world and is disabled when there is none.
 * Turn it off with Settings > Worlds Reborn > replaceTitleScreen to get the vanilla screen back.
 */
public class WRTitleScreen extends Screen {
    private ThemedButton continueButton;
    private String continueLevelId;
    private Component continueLabel = Component.translatable("menu.wrinterface.continue");

    public WRTitleScreen() {
        super(Component.translatable("menu.wrinterface.title"));
    }

    @Override
    protected void init() {
        int x = 28;
        int w = 150;
        int y = height / 2 - 62;
        int step = 24;
        continueButton = addRenderableWidget(new ThemedButton(x, y, w, 20, continueLabel, b -> continueWorld(), UiTheme.GOLD));
        continueButton.active = continueLevelId != null;
        addRenderableWidget(new ThemedButton(x, y += step, w, 20, Component.translatable("menu.singleplayer"),
                b -> minecraft.setScreen(new SelectWorldScreen(this))));
        addRenderableWidget(new ThemedButton(x, y += step, w, 20, Component.translatable("menu.multiplayer"),
                b -> minecraft.setScreen(new JoinMultiplayerScreen(this))));
        addRenderableWidget(new ThemedButton(x, y += step, w, 20, Component.translatable("menu.wrinterface.settings"),
                b -> minecraft.setScreen(new SettingsHubScreen(this))));
        addRenderableWidget(new ThemedButton(x, y += step, w, 20, Component.translatable("menu.wrinterface.credits"),
                b -> minecraft.setScreen(new CreditsScreen(this))));
        addRenderableWidget(new ThemedButton(x, y += step, w / 2 - 2, 20, Component.translatable("fml.menu.mods"),
                b -> minecraft.setScreen(new ModListScreen(this))));
        addRenderableWidget(new ThemedButton(x + w / 2 + 2, y, w / 2 - 2, 20, Component.translatable("menu.quit"),
                b -> minecraft.stop(), UiTheme.DANGER));
        findLatestWorld();
    }

    private void findLatestWorld() {
        try {
            LevelStorageSource source = minecraft.getLevelSource();
            source.loadLevelSummaries(source.findLevelCandidates()).thenAccept(list -> minecraft.execute(() -> {
                LevelSummary latest = list.stream()
                        .filter(s -> !s.isLocked() && !s.requiresManualConversion())
                        .max(Comparator.comparingLong(LevelSummary::getLastPlayed)).orElse(null);
                continueLevelId = latest == null ? null : latest.getLevelId();
                continueLabel = latest == null ? Component.translatable("menu.wrinterface.continue")
                        : Component.translatable("menu.wrinterface.continue_world", latest.getLevelName());
                if (continueButton != null) {
                    continueButton.setMessage(continueLabel);
                    continueButton.active = continueLevelId != null;
                }
            }));
        } catch (Exception e) {
            continueLevelId = null;
        }
    }

    private void continueWorld() {
        if (continueLevelId != null) {
            minecraft.createWorldOpenFlows().openWorld(continueLevelId, () -> minecraft.setScreen(this));
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderPanorama(g, partialTick);
        // Soft dark band behind the menu, fading out toward the middle of the screen.
        int band = 230;
        for (int i = 0; i < 24; i++) {
            int a = (int) (0xC0 * (1 - i / 24f));
            g.fill(band + i * 4, 0, band + i * 4 + 4, height, a << 24);
        }
        g.fill(0, 0, band, height, 0xC0000000);
        g.fillGradient(0, height - 60, width, height, 0x00000000, 0x90000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int x = 28;
        int y = height / 2 - 104;
        Component title = Component.translatable("menu.wrinterface.title");
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(2.5f, 2.5f, 1f);
        // Emerald glow behind the white title.
        for (int[] o : new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
            g.drawString(font, title, o[0], o[1], UiTheme.withAlpha(UiTheme.EMERALD, 0.35f), false);
        }
        g.drawString(font, title, 0, 0, UiTheme.TEXT, true);
        g.pose().popPose();
        float t = (System.currentTimeMillis() % 4000L) / 4000f;
        int sweep = (int) (150 * t);
        g.fillGradient(x, y + 26, x + 150, y + 27, UiTheme.EMERALD, UiTheme.EMERALD);
        g.fill(x + Math.max(0, sweep - 20), y + 26, x + sweep, y + 27, UiTheme.CYAN);
        g.drawString(font, Component.translatable("menu.wrinterface.subtitle"), x, y + 31, UiTheme.CYAN, false);
        Component ver = Component.translatable("menu.wrinterface.version", "0.1.0");
        g.drawString(font, ver, 4, height - 10, UiTheme.TEXT_MUTED, false);
    }
}
