package com.worldsreborn.marvel.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.worldsreborn.core.api.ClientState;
import com.worldsreborn.marvel.MarvelIds;
import com.worldsreborn.marvel.MarvelScript611;
import com.worldsreborn.marvel.MarvelSites;
import com.worldsreborn.marvel.WRMarvel;
import com.worldsreborn.marvel.doomsday.MarvelPayloads;
import com.worldsreborn.ui.client.UiConfig;
import com.worldsreborn.ui.client.UiTheme;
import com.worldsreborn.ui.client.cinematic.Caption;
import com.worldsreborn.ui.client.cinematic.CinematicDef;
import com.worldsreborn.ui.client.cinematic.Cinematics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/** Client presentation of Doomsday Scenes 6-11: the cinematics, the recording screen and the other Earth in the sky. */
final class DoomsdayClient {
    private static final ResourceLocation OPS_FEED = MarvelClient.tex("ops_feed");
    private static final ResourceLocation OBSERVATORY = MarvelClient.tex("observatory_view");
    private static final ResourceLocation OTHER_EARTH = MarvelClient.tex("other_earth");
    private static final ResourceLocation SKYLINE = MarvelClient.tex("skyline");
    private static final ResourceLocation STEVE = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
    private static final ResourceLocation PETER = skin("tom_spiderman");
    private static final ResourceLocation WATCHER = skin("watcher");
    private static final ResourceLocation[] REFUGEES = {skin("refugee_mara"), skin("refugee_jonah"), skin("refugee_ana"),
            skin("refugee_kip"), skin("refugee_teodor")};
    private static final String OTHER_EARTH_FLAG = MarvelIds.FLAG_STATE_PREFIX + "other_earth_visible";

    private DoomsdayClient() {
    }

    static ResourceLocation skin(String name) {
        return ResourceLocation.fromNamespaceAndPath(WRMarvel.MODID, "textures/entity/" + name + ".png");
    }

    private static boolean noFlash() {
        return UiConfig.REDUCE_FLASHING.getAsBoolean();
    }

    private static boolean still() {
        return UiConfig.REDUCE_MOTION.getAsBoolean();
    }

    static void setup() {
        // Scene 6: the evacuation feed, four radio lines, then the feed breaks off.
        List<Caption> feed = new ArrayList<>();
        int[][] feedTimes = {{20, 110}, {116, 196}, {202, 262}, {300, 390}};
        List<String> feedLines = MarvelScript611.GROUPS.get("b06_feed");
        for (int i = 0; i < feedLines.size() && i < feedTimes.length; i++) {
            String id = feedLines.get(i);
            int ms = MarvelScript611.MILLIS.getOrDefault(id, 0);
            feed.add(new Caption(feedTimes[i][0], feedTimes[i][1], "speaker.wrmarvel." + MarvelScript611.SPEAKER.get(id),
                    "line.wrmarvel." + id, false, ms > 0 ? WRMarvel.MODID + ":voice." + id : null));
        }
        Cinematics.register(new CinematicDef(MarvelIds.CIN_S06_FEED, DoomsdayClient::opsFeed, null, feed));
        Cinematics.register(new CinematicDef(MarvelIds.CIN_S09_DOORWAY, DoomsdayClient::doorway, null, List.of()));
        Cinematics.register(new CinematicDef(MarvelIds.CIN_S10_REFUGEES, DoomsdayClient::refugees, null, List.of()));
        Cinematics.register(new CinematicDef(MarvelIds.CIN_S10_FALLS, DoomsdayClient::tvaFalls, null, List.of()));
        Cinematics.register(new CinematicDef(MarvelIds.CIN_S10_PETER, DoomsdayClient::peterView, null, List.of()));
        Cinematics.register(new CinematicDef(MarvelIds.CIN_S10_HANDOVER, DoomsdayClient::handover, null, List.of()));
        Cinematics.register(new CinematicDef(MarvelIds.CIN_S11_WATCHING, DoomsdayClient::steveWatching, null, List.of()));

        MarvelPayloads.clientScreenOpener = name -> {
            if ("recording".equals(name)) {
                Minecraft.getInstance().setScreen(new RecordingScreen());
            }
        };
        NeoForge.EVENT_BUS.addListener(DoomsdayClient::renderSky);
    }

    // ================================================================== cinematics

    private static void fitted(GuiGraphics g, ResourceLocation tex, int w, int h, float zoom, float alpha) {
        float s = Math.max(w / 256f, h / 144f) * zoom;
        MarvelClient.sprite(g, tex, w / 2f, h / 2f, 256 * s, 144 * s, 0, alpha, 256, 144);
    }

    /** Static noise; with reduced flashing it is a steady grey grain instead of a flicker. */
    private static void noise(GuiGraphics g, int w, int h, float tick, float amount) {
        if (amount <= 0) {
            return;
        }
        RandomSource rnd = RandomSource.create(noFlash() ? 7 : (long) tick);
        int cell = Math.max(3, w / 120);
        for (int y = 0; y < h; y += cell) {
            for (int x = 0; x < w; x += cell) {
                if (rnd.nextFloat() < 0.55f) {
                    int v = 60 + rnd.nextInt(noFlash() ? 60 : 190);
                    g.fill(x, y, x + cell, y + cell, UiTheme.withAlpha((v << 16) | (v << 8) | v, amount * (noFlash() ? 0.6f : 0.9f)));
                }
            }
        }
    }

    /** A TVA camera feed of the evacuation corridor: a portal holding, figures moving through, then lost signal. */
    private static void opsFeed(GuiGraphics g, int w, int h, float tick, float progress, String arg) {
        g.fill(0, 0, w, h, 0xFF000000);
        float jitter = still() || noFlash() ? 0 : Mth.sin(tick * 1.7f) * (tick > 240 ? 3 : 0.6f);
        g.pose().pushPose();
        g.pose().translate(jitter, 0, 0);
        fitted(g, OPS_FEED, w, h, 1.05f, 1f);
        // People hurrying through the doorway, small and dark.
        for (int i = 0; i < 6; i++) {
            float t = ((tick + i * 37) % 160) / 160f;
            float x = w * (0.15f + 0.35f * t) + i * 3;
            if (tick < 290) {
                MarvelClient.figure(g, x, h * 0.78f, h / 220f, REFUGEES[i % REFUGEES.length], null, 0.25f, 0.2f, 0.18f, 0.9f, 0);
            }
        }
        g.pose().popPose();
        // Scan lines and the feed label.
        for (int y = 0; y < h; y += 4) {
            g.fill(0, y, w, y + 1, 0x22000000);
        }
        g.drawString(Minecraft.getInstance().font, Component.translatable("cinematic.wrmarvel.s06_feed.label"), 12, 12, UiTheme.TVA_AMBER);
        if (((int) tick / 10) % 2 == 0 || noFlash()) {
            g.fill(w - 22, 13, w - 14, 21, UiTheme.withAlpha(0xE5534B, 0.9f));
        }
        // "We're losing the-" and the feed breaks up.
        float lost = Mth.clamp((tick - 240) / 50f, 0f, 1f);
        noise(g, w, h, tick, lost * (tick > 290 ? 1f : 0.5f));
        if (tick > 290) {
            g.fill(0, 0, w, h, UiTheme.withAlpha(0x000000, 0.5f));
            g.drawCenteredString(Minecraft.getInstance().font, Component.translatable("cinematic.wrmarvel.s06_feed.lost"),
                    w / 2, h / 2 - 4, UiTheme.TVA_AMBER);
        }
    }

    /** Scene 9: through the failing doorway. The destination readout gives up, then the light swallows the view. */
    private static void doorway(GuiGraphics g, int w, int h, float tick, float progress, String arg) {
        for (int i = 0; i < 8; i++) {
            g.fill(0, h * i / 8, w, h * (i + 1) / 8, 0xFF000000 | MarvelClient.lerpRgb(0x1A0F06, 0x3A2010, i / 7f));
        }
        float grow = Mth.clamp(tick / 110f, 0f, 1f);
        int dh = (int) (h * (0.4f + grow * grow * 1.6f));
        int dw = (int) (dh * 0.55f);
        int cx = w / 2, cy = h / 2;
        float wobble = still() ? 0 : Mth.sin(tick * 0.4f) * 3;
        g.fill(cx - dw / 2 - 5, cy - dh / 2 - 5, cx + dw / 2 + 5, cy + dh / 2 + 5, UiTheme.withAlpha(0xC06018, 1f));
        for (int i = 0; i < 6; i++) {
            float k = i / 6f;
            int col = MarvelClient.lerpRgb(0xFFB45A, 0x9FD8FF, (Mth.sin(tick * 0.08f + i) + 1) / 2 * (still() ? 0 : 1));
            int iw = (int) (dw * (1 - k)), ih = (int) (dh * (1 - k));
            g.fill(cx - iw / 2 + (int) wobble, cy - ih / 2, cx + iw / 2 + (int) wobble, cy + ih / 2, UiTheme.withAlpha(col, 0.35f + k * 0.4f));
        }
        if (tick > 20 && tick < 120) {
            boolean show = noFlash() || ((int) tick / 6) % 3 != 0;
            if (show) {
                Component text = Component.translatable("cinematic.wrmarvel.s09_doorway.unavailable");
                int tw = Minecraft.getInstance().font.width(text);
                g.fill(cx - tw / 2 - 8, 20, cx + tw / 2 + 8, 36, 0xC0200C04);
                g.drawCenteredString(Minecraft.getInstance().font, text, cx, 24, UiTheme.DANGER);
            }
        }
        float white = Mth.clamp((tick - 110) / (noFlash() ? 40f : 15f), 0f, 1f);
        if (white > 0) {
            g.fill(0, 0, w, h, UiTheme.withAlpha(noFlash() ? 0xC8D8E8 : 0xFFF4E6, white));
        }
    }

    private static void observatory(GuiGraphics g, int w, int h, float zoom) {
        g.fill(0, 0, w, h, 0xFF02030A);
        fitted(g, OBSERVATORY, w, h, zoom, 1f);
    }

    /** A round window in the observatory floor or wall, showing {@code inside}. */
    private static void windowFrame(GuiGraphics g, int cx, int cy, int r, int ring) {
        for (int i = 0; i < 3; i++) {
            int rr = r + i * 2;
            g.fill(cx - rr, cy - rr, cx + rr, cy - rr + 2, ring);
            g.fill(cx - rr, cy + rr - 2, cx + rr, cy + rr, ring);
            g.fill(cx - rr, cy - rr, cx - rr + 2, cy + rr, ring);
            g.fill(cx + rr - 2, cy - rr, cx + rr, cy + rr, ring);
        }
    }

    /** Scene 10: the five refugees standing safe on the observatory floor. */
    private static void refugees(GuiGraphics g, int w, int h, float tick, float progress, String arg) {
        observatory(g, w, h, 1f + (still() ? 0 : progress * 0.06f));
        g.fill(0, (int) (h * 0.8f), w, h, 0xFF0B0D16);
        float px = h / 110f;
        for (int i = 0; i < REFUGEES.length; i++) {
            float a = Mth.clamp((tick - i * 12) / 20f, 0f, 1f);
            MarvelClient.figure(g, w * (0.25f + i * 0.125f), h * 0.86f, px, REFUGEES[i], null, 1f, 1f, 1f, a, 0);
        }
        fadeEdges(g, w, h, tick, 160);
    }

    /** Scene 10: through an observation window, the TVA's amber bubble breaks apart and goes dark. */
    private static void tvaFalls(GuiGraphics g, int w, int h, float tick, float progress, String arg) {
        observatory(g, w, h, 1.1f);
        int cx = w / 2, cy = (int) (h * 0.42f);
        int r = (int) (h * 0.3f);
        g.fill(cx - r, cy - r, cx + r, cy + r, 0xF0050610);
        windowFrame(g, cx, cy, r, 0xFF6B7690);
        float crack = Mth.clamp((tick - 60) / 120f, 0f, 1f);
        float dark = Mth.clamp((tick - 170) / 60f, 0f, 1f);
        // The TVA: an amber city in its bubble.
        int core = (int) (r * 0.55f);
        g.fill(cx - core, cy - core / 2, cx + core, cy + core / 2, UiTheme.withAlpha(0xE8A33D, 0.85f * (1 - dark)));
        for (int i = 0; i < 7; i++) {
            int bx = cx - core + 6 + i * (core * 2 - 12) / 7;
            int bh = 8 + (i * 13) % 20;
            g.fill(bx, cy - core / 2 - bh, bx + 6, cy - core / 2, UiTheme.withAlpha(0xC07020, 0.9f * (1 - dark)));
        }
        // Cracks spreading over it, pieces drifting away.
        RandomSource rnd = RandomSource.create(42);
        for (int i = 0; i < 12; i++) {
            float ang = rnd.nextFloat() * Mth.TWO_PI;
            float len = core * 1.2f * Mth.clamp(crack * 1.4f - i * 0.05f, 0f, 1f);
            if (len > 1) {
                MarvelClient.line(g, cx, cy, cx + Mth.cos(ang) * len, cy + Mth.sin(ang) * len, 1,
                        UiTheme.withAlpha(0xFFF0C8, 0.8f * (1 - dark)));
            }
            float drift = still() ? crack * 4 : crack * crack * r * 0.6f;
            int dx = (int) (cx + Mth.cos(ang) * (core * 0.6f + drift));
            int dy = (int) (cy + Mth.sin(ang) * (core * 0.3f + drift));
            if (crack > 0.3f) {
                g.fill(dx - 3, dy - 2, dx + 3, dy + 2, UiTheme.withAlpha(0xD08A30, (1 - dark) * 0.9f));
            }
        }
        if (crack > 0.4f && !noFlash() && tick > 150 && tick < 165) {
            g.fill(0, 0, w, h, UiTheme.withAlpha(0xFFE6B8, 0.25f));
        }
        // Steve and a Watcher in silhouette, watching it happen.
        float px = h / 90f;
        MarvelClient.figure(g, w * 0.2f, h * 1.02f, px, STEVE, null, 0.12f, 0.12f, 0.16f, 1f, 0);
        MarvelClient.figure(g, w * 0.82f, h * 1.05f, px * 1.25f, WATCHER, null, 0.1f, 0.1f, 0.14f, 1f, 0);
        fadeEdges(g, w, h, tick, 280);
    }

    /** Scene 10: through the window, New York, and a red-and-blue figure on a rooftop. */
    private static void peterView(GuiGraphics g, int w, int h, float tick, float progress, String arg) {
        observatory(g, w, h, 1.1f);
        int cx = w / 2, cy = (int) (h * 0.45f);
        int r = (int) (h * 0.32f);
        // Inside the window: night sky, skyline, Spider-Man perched.
        g.fill(cx - r, cy - r, cx + r, cy + r, 0xFF0A1028);
        float zoom = 1 + (still() ? 0 : progress * 0.35f);
        g.enableScissor(cx - r, cy - r, cx + r, cy + r);
        MarvelClient.sprite(g, SKYLINE, cx, cy + r * 0.45f, r * 2.4f * zoom, r * 0.9f * zoom, 0, 1f, 256, 96);
        MarvelClient.figure(g, cx + r * 0.2f, cy + r * 0.12f, r / 70f * zoom, PETER, null, 1f, 1f, 1f, 1f, 0);
        g.disableScissor();
        windowFrame(g, cx, cy, r, 0xFF6B7690);
        float px = h / 90f;
        MarvelClient.figure(g, w * 0.2f, h * 1.02f, px, STEVE, null, 0.12f, 0.12f, 0.16f, 1f, 0);
        fadeEdges(g, w, h, tick, 180);
    }

    /** Scene 10 to 11: the window fills the view and the story follows Spider-Man into the city. */
    private static void handover(GuiGraphics g, int w, int h, float tick, float progress, String arg) {
        g.fill(0, 0, w, h, 0xFF0A1028);
        float zoom = 1 + (still() ? progress * 0.2f : progress * progress * 2.5f);
        MarvelClient.sprite(g, SKYLINE, w / 2f, h * 0.75f, w * zoom * 1.2f, w * zoom * 0.45f, 0, 1f, 256, 96);
        float a = Mth.clamp((70 - tick) / 30f, 0f, 1f);
        if (a > 0) {
            observatory(g, w, h, 1f + progress * 2);
            g.fill(0, 0, w, h, UiTheme.withAlpha(0x02030A, a * 0.2f));
        }
        float out = Mth.clamp((tick - 80) / 20f, 0f, 1f);
        g.fill(0, 0, w, h, UiTheme.withAlpha(0x000000, out));
    }

    /** Scene 11 ending: back in the observatory, Steve watching New York through the window. */
    private static void steveWatching(GuiGraphics g, int w, int h, float tick, float progress, String arg) {
        observatory(g, w, h, 1.05f);
        int cx = (int) (w * 0.58f), cy = (int) (h * 0.42f);
        int r = (int) (h * 0.28f);
        g.fill(cx - r, cy - r, cx + r, cy + r, 0xFF0A1028);
        g.enableScissor(cx - r, cy - r, cx + r, cy + r);
        MarvelClient.sprite(g, SKYLINE, cx, cy + r * 0.45f, r * 2.4f, r * 0.9f, 0, 1f, 256, 96);
        MarvelClient.sprite(g, OTHER_EARTH, cx - r * 0.35f, cy - r * 0.45f, r * 0.7f, r * 0.7f, 0, 0.35f, 256, 256);
        g.disableScissor();
        windowFrame(g, cx, cy, r, 0xFF6B7690);
        float px = h / 80f;
        MarvelClient.figure(g, w * 0.26f, h * 1.0f, px, STEVE, null, 1f, 1f, 1f, 1f, 0);
        fadeEdges(g, w, h, tick, 140);
    }

    private static void fadeEdges(GuiGraphics g, int w, int h, float tick, int total) {
        float a = Math.max(Mth.clamp(1 - tick / 15f, 0f, 1f), Mth.clamp((tick - (total - 15)) / 15f, 0f, 1f));
        if (a > 0) {
            g.fill(0, 0, w, h, UiTheme.withAlpha(0x000000, a));
        }
    }

    // ================================================================== the other Earth in New York's sky

    private static void renderSky(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_SKY) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimension().equals(MarvelSites.NEW_YORK)
                || !ClientState.progress().hasFlag(OTHER_EARTH_FLAG)) {
            return;
        }
        PoseStack pose = new PoseStack();
        pose.mulPose(event.getModelViewMatrix());
        // High in the south-western sky, faint.
        pose.mulPose(Axis.YP.rotationDegrees(-35f));
        pose.mulPose(Axis.XP.rotationDegrees(40f));
        Matrix4f m = pose.last().pose();
        float size = 34f;
        float dist = 100f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, OTHER_EARTH);
        float pulse = still() ? 0f : Mth.sin((mc.level.getGameTime() % 2400) / 2400f * Mth.TWO_PI) * 0.05f;
        RenderSystem.setShaderColor(1f, 1f, 1f, 0.45f + pulse);
        BufferBuilder buf = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.addVertex(m, -size, dist, -size).setUv(0f, 0f);
        buf.addVertex(m, size, dist, -size).setUv(1f, 0f);
        buf.addVertex(m, size, dist, size).setUv(1f, 1f);
        buf.addVertex(m, -size, dist, size).setUv(0f, 1f);
        BufferUploader.drawWithShader(buf.buildOrThrow());
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
