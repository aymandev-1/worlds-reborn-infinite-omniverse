package com.worldsreborn.marvel.client;

import com.worldsreborn.marvel.doomsday.MarvelPayloads;
import com.worldsreborn.ui.client.ThemedButton;
import com.worldsreborn.ui.client.UiConfig;
import com.worldsreborn.ui.client.UiTheme;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Scene 6: the TVA's recording of Steve's crossing through the silver-blue passage. It loops; the player can pause and
 * scrub it, and points out the figure watching from the edge of the passage (visible from 3 to 6 seconds). If they
 * haven't found him after a while, he is highlighted and a button points him out for them, so nobody gets stuck.
 */
final class RecordingScreen extends Screen {
    private static final float LENGTH = 240f; // 12 s loop, in ticks
    private static final float SEEN_FROM = 60f;
    private static final float SEEN_TO = 120f;
    private static final long HELP_AFTER_MS = 20_000;
    private static final ResourceLocation STEVE = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
    private static final ResourceLocation TRAVELLER = DoomsdayClient.skin("hooded_traveller");

    private final long opened = Util.getMillis();
    private float time;
    private long last = Util.getMillis();
    private boolean paused;
    private boolean dragging;
    private boolean found;
    private long foundAt;
    private ThemedButton pauseButton;
    private ThemedButton helpButton;

    RecordingScreen() {
        super(Component.translatable("screen.wrmarvel.recording.title"));
    }

    private int vx() {
        return (width - vw()) / 2;
    }

    private int vy() {
        return 34;
    }

    private int vw() {
        return Math.min(width - 40, (int) ((height - 110) * 16f / 9f));
    }

    private int vh() {
        return vw() * 9 / 16;
    }

    private int barY() {
        return vy() + vh() + 8;
    }

    @Override
    protected void init() {
        int y = barY() + 14;
        pauseButton = addRenderableWidget(new ThemedButton(vx(), y, 80, 18, pauseLabel(), b -> {
            paused = !paused;
            b.setMessage(pauseLabel());
        }, UiTheme.TVA_AMBER));
        helpButton = addRenderableWidget(new ThemedButton(vx() + vw() - 120, y, 120, 18,
                Component.translatable("screen.wrmarvel.recording.point_out"), b -> identify(), UiTheme.TVA_AMBER));
        helpButton.visible = false;
    }

    private Component pauseLabel() {
        return Component.translatable(paused ? "screen.wrmarvel.recording.play" : "screen.wrmarvel.recording.pause");
    }

    private boolean helping() {
        return Util.getMillis() - opened > HELP_AFTER_MS;
    }

    private boolean travellerVisible() {
        return time >= SEEN_FROM && time <= SEEN_TO;
    }

    /** The traveller's on-screen box: half in shadow at the right-hand edge of the passage. */
    private int[] travellerBox() {
        float px = vh() / 85f;
        int cx = vx() + (int) (vw() * 0.86f);
        int foot = vy() + (int) (vh() * 0.86f);
        return new int[]{(int) (cx - 8 * px), (int) (foot - 32 * px), (int) (cx + 8 * px), foot};
    }

    private void identify() {
        if (!found) {
            found = true;
            foundAt = Util.getMillis();
            paused = true;
            time = Mth.clamp(time, SEEN_FROM + 10, SEEN_TO - 10);
        }
    }

    @Override
    public void tick() {
        if (found && Util.getMillis() - foundAt > 2200) {
            PacketDistributor.sendToServer(new MarvelPayloads.ScreenAction("recording_identified"));
            onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button == 0 && !found) {
            int[] box = travellerBox();
            if (travellerVisible() && mx >= box[0] - 6 && mx <= box[2] + 6 && my >= box[1] - 6 && my <= box[3] + 6) {
                identify();
                return true;
            }
            if (onBar(mx, my)) {
                dragging = true;
                scrub(mx);
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragging) {
            scrub(mx);
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragging = false;
        return super.mouseReleased(mx, my, button);
    }

    private boolean onBar(double mx, double my) {
        return mx >= vx() && mx <= vx() + vw() && my >= barY() - 3 && my <= barY() + 7;
    }

    private void scrub(double mx) {
        time = Mth.clamp((float) ((mx - vx()) / vw()), 0f, 1f) * (LENGTH - 1);
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xE0080604);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long now = Util.getMillis();
        if (!paused && !dragging) {
            time = (time + (now - last) / 50f) % LENGTH;
        }
        last = now;
        super.render(g, mouseX, mouseY, partialTick);
        helpButton.visible = helping() && !found;

        g.drawString(font, title, vx(), 14, UiTheme.TVA_AMBER);
        int x0 = vx(), y0 = vy(), w = vw(), h = vh();
        UiTheme.framed(g, x0 - 2, y0 - 2, w + 4, h + 4, UiTheme.TVA_AMBER);
        g.enableScissor(x0, y0, x0 + w, y0 + h);
        drawPassage(g, x0, y0, w, h);
        g.disableScissor();

        // Scrub bar with the play head.
        int by = barY();
        g.fill(x0, by, x0 + w, by + 4, 0xFF3A2C1A);
        g.fill(x0, by, x0 + (int) (w * time / LENGTH), by + 4, UiTheme.TVA_AMBER);
        int head = x0 + (int) (w * time / LENGTH);
        g.fill(head - 2, by - 3, head + 2, by + 7, 0xFFFFFFFF);
        g.drawString(font, String.format("00:%02d / 00:%02d", (int) (time / 20), (int) (LENGTH / 20)), x0 + 90, by + 19, UiTheme.TEXT_DIM);

        Component hint = found ? Component.translatable("screen.wrmarvel.recording.found")
                : Component.translatable("screen.wrmarvel.recording.hint");
        g.drawCenteredString(font, hint, width / 2, by + 40, found ? UiTheme.GOLD : UiTheme.TEXT_DIM);
    }

    private void drawPassage(GuiGraphics g, int x0, int y0, int w, int h) {
        boolean still = UiConfig.REDUCE_MOTION.getAsBoolean();
        // Silver-blue passage walls, receding.
        for (int i = 0; i < 10; i++) {
            float k = i / 10f;
            int inset = (int) (Math.min(w, h) * 0.45f * k);
            int col = MarvelClient.lerpRgb(0x1A2A40, 0xB8D8F0, k);
            g.fill(x0 + inset, y0 + inset / 2, x0 + w - inset, y0 + h - inset / 2, 0xFF000000 | col);
        }
        if (!still) {
            for (int i = 0; i < 5; i++) {
                float t = ((time * 2 + i * 50) % LENGTH) / LENGTH;
                int ly = y0 + (int) (h * t);
                g.fill(x0, ly, x0 + w, ly + 1, UiTheme.withAlpha(0xFFFFFF, 0.12f));
            }
        }
        // Steve walking through the middle of the passage, coming closer.
        float walk = time / LENGTH;
        float px = h / 120f * (0.7f + walk * 0.8f);
        MarvelClient.figure(g, x0 + w * 0.45f, y0 + h * (0.72f + walk * 0.18f), px, STEVE, null, 0.9f, 0.95f, 1f, 1f, 0);
        // The watcher at the edge: fades in at 3 s, fades out by 6 s, mostly in shadow.
        float seen = MarvelClient.window(time, SEEN_FROM, SEEN_TO, 12f);
        int[] box = travellerBox();
        g.fill(box[0] - (box[2] - box[0]), y0, x0 + w, y0 + h, 0x60000000);
        if (seen > 0) {
            float tpx = h / 85f;
            MarvelClient.figure(g, (box[0] + box[2]) / 2f, box[3], tpx, TRAVELLER, null, 0.4f, 0.42f, 0.5f, seen * 0.85f, 0);
        }
        if ((helping() || found) && seen > 0) {
            int c = found ? UiTheme.GOLD : UiTheme.withAlpha(0xF2C94C, 0.6f + 0.4f * Mth.sin(Util.getMillis() / 200f));
            g.renderOutline(box[0] - 4, box[1] - 4, box[2] - box[0] + 8, box[3] - box[1] + 8, c);
            g.renderOutline(box[0] - 5, box[1] - 5, box[2] - box[0] + 10, box[3] - box[1] + 10, c);
        }
        // Timestamp overlay, like a security feed.
        g.drawString(font, Component.translatable("screen.wrmarvel.recording.label"), x0 + 6, y0 + 6, UiTheme.TVA_AMBER);
    }
}
