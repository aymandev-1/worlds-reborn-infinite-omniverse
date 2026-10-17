package com.worldsreborn.ui.client.screen;

import com.worldsreborn.core.network.CinematicDonePayload;
import com.worldsreborn.ui.client.UiConfig;
import com.worldsreborn.ui.client.UiTheme;
import com.worldsreborn.ui.client.Voice;
import com.worldsreborn.ui.client.cinematic.Caption;
import com.worldsreborn.ui.client.cinematic.CinematicDef;
import com.worldsreborn.ui.client.cinematic.Cinematics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Plays a cinematic. Input is blocked, the game is not paused (the server keeps time), and when it ends or is
 * skipped the server is told. If the client never reports back, the server's own timer finishes the scene.
 */
public class CinematicScreen extends Screen {
    private final CinematicDef def;
    private final int duration;
    private final String argument;
    private int ticks;
    private boolean finished;

    public CinematicScreen(String cinematicId, int durationTicks, String argument) {
        super(Component.empty());
        this.def = Cinematics.get(cinematicId);
        this.duration = Math.max(1, durationTicks);
        this.argument = argument == null ? "" : argument;
    }

    public String cinematicId() {
        return def.id();
    }

    @Override
    public void tick() {
        for (Caption c : def.captions()) {
            if (c.voice() != null && c.fromTick() == ticks) {
                Voice.play(c.voice());
            }
        }
        if (++ticks >= duration) {
            finish(false);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && UiConfig.ALLOW_CINEMATIC_SKIP.getAsBoolean()) {
            finish(true);
            return true;
        }
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        float t = ticks + partialTick;
        def.renderer().render(g, width, height, t, Mth.clamp(t / duration, 0f, 1f), argument);

        if (def.titleKey() != null && t < 80) {
            float a = Mth.clamp(Math.min(t / 15f, (80 - t) / 15f), 0f, 1f);
            g.pose().pushPose();
            g.pose().translate(width / 2f, height * 0.2f, 0);
            g.pose().scale(2f, 2f, 1f);
            g.drawCenteredString(font, Component.translatable(def.titleKey()), 0, 0, UiTheme.withAlpha(UiTheme.TEXT, a));
            g.pose().popPose();
        }

        for (Caption c : def.captions()) {
            if (c.visibleAt(t)) {
                drawCaption(g, c);
            }
        }

        if (UiConfig.ALLOW_CINEMATIC_SKIP.getAsBoolean() && t > 20) {
            Component hint = Component.translatable("cinematic.wrinterface.skip");
            g.drawString(font, hint, width - font.width(hint) - 8, height - 14, UiTheme.TEXT_MUTED, false);
        }
    }

    private void drawCaption(GuiGraphics g, Caption c) {
        float scale = UiConfig.textScale();
        int maxWidth = (int) (Math.min(360, width - 40) / scale);
        Component text = Component.translatable(c.textKey());
        if (c.placeholder()) {
            text = Component.translatable("dialogue.wrinterface.temp").append(" ").append(text);
        }
        List<FormattedCharSequence> lines = font.split(FormattedText.composite(text), maxWidth);
        int lineH = font.lineHeight + 2;
        int blockH = lines.size() * lineH + (c.speakerKey() != null ? lineH : 0);
        g.pose().pushPose();
        g.pose().translate(width / 2f, height - 30 - blockH * scale, 0);
        g.pose().scale(scale, scale, 1f);
        int y = 0;
        if (c.speakerKey() != null) {
            g.drawCenteredString(font, Component.translatable(c.speakerKey()), 0, y, UiTheme.EMERALD);
            y += lineH;
        }
        for (FormattedCharSequence line : lines) {
            int w = font.width(line);
            g.fill(-w / 2 - 3, y - 2, w / 2 + 3, y + font.lineHeight, 0x90000000);
            g.drawString(font, line, -w / 2, y, UiTheme.TEXT, false);
            y += lineH;
        }
        g.pose().popPose();
    }

    private void finish(boolean skipped) {
        if (finished) {
            return;
        }
        finished = true;
        PacketDistributor.sendToServer(new CinematicDonePayload(def.id(), skipped));
        if (minecraft != null && minecraft.screen == this) {
            minecraft.setScreen(null);
        }
    }
}
