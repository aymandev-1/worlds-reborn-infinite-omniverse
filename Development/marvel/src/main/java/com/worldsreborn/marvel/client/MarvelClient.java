package com.worldsreborn.marvel.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import com.worldsreborn.marvel.MarvelContent;
import com.worldsreborn.marvel.MarvelIds;
import com.worldsreborn.marvel.MarvelScript;
import com.worldsreborn.marvel.WRMarvel;
import com.worldsreborn.ui.client.UiConfig;
import com.worldsreborn.ui.client.UiTheme;
import com.worldsreborn.ui.client.cinematic.Caption;
import com.worldsreborn.ui.client.cinematic.CinematicDef;
import com.worldsreborn.ui.client.cinematic.Cinematics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Client presentation of the Marvel cinematics. */
@Mod(value = WRMarvel.MODID, dist = Dist.CLIENT)
public final class MarvelClient {
    public MarvelClient(IEventBus modBus) {
        modBus.addListener(this::setup);
    }

    private static final ResourceLocation VORTEX = tex("vortex");
    private static final ResourceLocation WATCHER = tex("watcher");
    private static final ResourceLocation SKYLINE = tex("skyline");
    private static final ResourceLocation TVA_HALL = tex("tva_hall");
    private static final ResourceLocation[] WORLDS = new ResourceLocation[6];

    static {
        for (int i = 0; i < WORLDS.length; i++) {
            WORLDS[i] = tex("world_" + i);
        }
    }

    static ResourceLocation tex(String name) {
        return ResourceLocation.fromNamespaceAndPath(WRMarvel.MODID, "textures/gui/cinematic/" + name + ".png");
    }

    /** Falling-cinematic captions: script line, first tick, last tick. Timed to the recorded takes. */
    private static final int[][] FALLING_LINES = {
            {16, 10, 60}, {17, 64, 92}, {18, 96, 152}, {19, 156, 224}, {20, 230, 422},
            {21, 424, 446}, {22, 450, 480}, {23, 486, 518}, {24, 520, 545}};

    private void setup(FMLClientSetupEvent event) {
        // Scene 1: Steve's dream before he wakes in his bed.
        Cinematics.register(new CinematicDef(MarvelIds.CIN_DREAM, MarvelClient::dream, null, List.of()));

        // Scene 1 opening card over the village.
        Cinematics.register(new CinematicDef(MarvelIds.CIN_INTRO, Cinematics.LETTERBOX, "cinematic.wrmarvel.intro.title",
                List.of(Caption.text(25, 130, "cinematic.wrmarvel.intro.scene"))));

        // Scene 2: falling through realities, lines S002_016-024 of the script.
        List<Caption> falling = new ArrayList<>();
        for (int[] l : FALLING_LINES) {
            String id = String.format(Locale.ROOT, "S002_%03d", l[0]);
            falling.add(Caption.voiced(l[1], l[2], "speaker.wrmarvel." + MarvelScript.SPEAKER.get(id),
                    "line.wrmarvel." + id.toLowerCase(Locale.ROOT), MarvelContent.voice(id)));
        }
        Cinematics.register(new CinematicDef(MarvelIds.CIN_FALLING, MarvelClient::falling, null, falling));

        // Scene 5: through the TVA time door.
        Cinematics.register(new CinematicDef(MarvelIds.CIN_TVA_DOOR, MarvelClient::tvaDoor, null, List.of()));

        // Scenes 6-11.
        DoomsdayClient.setup();
    }

    private static final ResourceLocation STEVE = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");

    /** A flat front view of Steve, feet at (cx, footY), {@code px} screen pixels per skin pixel, tinted. */
    private static ResourceLocation vanilla(String path) {
        return ResourceLocation.withDefaultNamespace("textures/" + path + ".png");
    }

    /** A dream echo: a skin, an optional armour set worn over it, a tint and glowing eyes (0 = none). */
    private record Echo(float slot, float delay, ResourceLocation skin, ResourceLocation armor, float r, float g, float b, int eyes) {
    }

    private static final Echo[] ECHOES = {
            new Echo(0, 0, STEVE, null, 1f, 1f, 1f, 0),
            new Echo(-1, 1, vanilla("entity/player/wide/alex"), null, 1f, 1f, 1f, 0),
            new Echo(4, 2, STEVE, vanilla("models/armor/iron_layer_1"), 1f, 1f, 1f, 0),
            new Echo(-2, 3, vanilla("entity/zombie/zombie"), null, 1f, 1f, 1f, 0),
            new Echo(2, 4, STEVE, vanilla("models/armor/gold_layer_1"), 1f, 0.92f, 0.7f, 0),
            new Echo(-3, 5, STEVE, vanilla("models/armor/diamond_layer_1"), 0.8f, 0.9f, 1f, 0),
            new Echo(3, 6, STEVE, null, 0.1f, 0.07f, 0.16f, 0xB070FF),
            new Echo(-4, 7, STEVE, vanilla("models/armor/netherite_layer_1"), 1f, 1f, 1f, 0),
            // Herobrine appears last, close by, staring with white eyes.
            new Echo(1, 9, STEVE, null, 0.92f, 0.92f, 0.95f, 0xFFFFFF),
    };

    /**
     * Draws a flat front-facing player figure from a 64x64 skin (with its outer layers), optionally wearing a 64x32
     * armour texture, with optional glowing eyes.
     */
    static void figure(GuiGraphics g, float cx, float footY, float px, ResourceLocation skin, ResourceLocation armor,
                               float r, float gr, float b, float a, int eyes) {
        if (a <= 0.01f) {
            return;
        }
        float alpha = Mth.clamp(a, 0f, 1f);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(r, gr, b, alpha);
        g.pose().pushPose();
        g.pose().translate(cx - 8 * px, footY - 32 * px, 0);
        g.pose().scale(px, px, 1f);
        // {screen x, screen y, u, v, w, h} in skin pixels: head, body, arms, legs, then the outer layers
        int[][] parts = {{4, 0, 8, 8, 8, 8}, {4, 8, 20, 20, 8, 12}, {0, 8, 44, 20, 4, 12}, {12, 8, 36, 52, 4, 12},
                {4, 20, 4, 20, 4, 12}, {8, 20, 20, 52, 4, 12},
                {4, 0, 40, 8, 8, 8}, {4, 8, 20, 36, 8, 12}, {0, 8, 44, 36, 4, 12}, {12, 8, 52, 52, 4, 12},
                {4, 20, 4, 36, 4, 12}, {8, 20, 4, 52, 4, 12}};
        for (int[] p : parts) {
            g.blit(skin, p[0], p[1], p[4], p[5], p[2], p[3], p[4], p[5], 64, 64);
        }
        if (armor != null) {
            RenderSystem.setShaderColor(Math.max(r, 0.85f), Math.max(gr, 0.85f), Math.max(b, 0.85f), alpha);
            // Armour sits a little proud of the body: helmet, chestplate, arms, boots.
            int[][] plates = {{3, -1, 8, 8, 10, 9, 8, 8}, {3, 7, 20, 20, 10, 13, 8, 12}, {-1, 7, 44, 20, 5, 13, 4, 12},
                    {12, 7, 44, 20, 5, 13, 4, 12}, {3, 25, 4, 26, 5, 7, 4, 6}, {8, 25, 4, 26, 5, 7, 4, 6}};
            for (int[] p : plates) {
                g.blit(armor, p[0], p[1], p[4], p[5], p[2], p[3], p[6], p[7], 64, 32);
            }
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        if (eyes != 0) {
            // Glowing eyes with a soft halo.
            g.fill(4, 3, 12, 6, UiTheme.withAlpha(eyes, 0.18f * alpha));
            g.fill(5, 4, 7, 5, UiTheme.withAlpha(eyes, alpha));
            g.fill(9, 4, 11, 5, UiTheme.withAlpha(eyes, alpha));
        }
        g.pose().popPose();
        RenderSystem.disableBlend();
    }

    /**
     * Scene 1 opening: Steve's dream. Out of the dark, more and more Steves appear around him: Alex, armoured Steves,
     * a zombie, a shadow and, last of all, Herobrine; then glowing timelines start branching out from him, flare
     * white, and his eyes open on his bedroom.
     */
    private static void dream(GuiGraphics g, int w, int h, float tick, float progress, String arg) {
        boolean still = UiConfig.REDUCE_MOTION.getAsBoolean();
        float open = Mth.clamp((tick - 292) / 26f, 0f, 1f); // eyes opening at the end
        if (open < 1f) {
            float bgA = 1f - open;
            for (int i = 0; i < 8; i++) {
                int col = lerpRgb(0x07030F, 0x1B1033, i / 7f);
                g.fill(0, h * i / 8, w, h * (i + 1) / 8, UiTheme.withAlpha(col, open > 0 ? 0f : 1f));
            }
            if (open == 0) {
                RandomSource rnd = RandomSource.create(1L);
                for (int i = 0; i < 70; i++) {
                    int sx = rnd.nextInt(Math.max(1, w));
                    int sy = (int) ((rnd.nextInt(Math.max(1, h)) + (still ? 0 : tick * (0.1f + rnd.nextFloat() * 0.3f))) % h);
                    g.fill(sx, sy, sx + 1, sy + 1, UiTheme.withAlpha(0xD8D0FF, 0.3f + 0.5f * rnd.nextFloat()));
                }
                // Steves: one in the middle, then echoes appearing around him.
                float base = h / 110f;
                float timelines = Mth.clamp((tick - 170) / 90f, 0f, 1f);
                for (int i = ECHOES.length - 1; i >= 0; i--) {
                    Echo e = ECHOES[i];
                    float appear = Mth.clamp((tick - 15 - e.delay() * 15) / 20f, 0f, 1f);
                    float fadeForLines = i == 0 ? 1f : 1f - timelines * 0.75f;
                    float bob = still ? 0 : Mth.sin((tick + i * 13) * 0.05f) * 3;
                    float scale = base * (1f - Math.abs(e.slot()) * 0.1f);
                    float cx = w / 2f + e.slot() * w * 0.105f;
                    float foot = h * 0.72f - Math.abs(e.slot()) * h * 0.04f + bob;
                    float flicker = i == 0 || still ? 1f : 0.8f + 0.2f * Mth.sin(tick * 0.3f + i);
                    figure(g, cx, foot, scale, e.skin(), e.armor(), e.r(), e.g(), e.b(),
                            appear * fadeForLines * flicker * (i == 0 ? 1f : 0.85f), e.eyes());
                }
                // Timelines branching out from Steve.
                if (timelines > 0) {
                    float ox = w / 2f;
                    float oy = h * 0.72f - 16 * base;
                    RandomSource tr = RandomSource.create(616L);
                    for (int i = 0; i < 14; i++) {
                        float ang = -Mth.PI / 2 + (i - 6.5f) * 0.22f;
                        branch(g, tr, ox, oy, ang, Math.max(w, h) * 0.55f * timelines, 3, tick, i % 3 == 0 ? 0xF2C94C : 0xB8D8F0);
                    }
                }
                float flash = window(tick, 255, 300, 18);
                if (flash > 0) {
                    g.fill(0, 0, w, h, UiTheme.withAlpha(0xFFFFFF, flash));
                }
            }
        }
        if (open > 0) {
            // Eyelids opening on the bedroom: black bars sliding apart, with a blink.
            float o = open < 0.35f ? open / 0.35f * 0.4f : open < 0.5f ? 0.4f - (open - 0.35f) / 0.15f * 0.3f
                    : 0.1f + (open - 0.5f) / 0.5f * 0.9f;
            int lid = (int) (h / 2f * (1 - o));
            g.fill(0, 0, w, lid, 0xFF000000);
            g.fill(0, h - lid, w, h, 0xFF000000);
            g.fill(0, 0, w, h, UiTheme.withAlpha(0x000000, (1 - open) * 0.6f));
        }
    }

    /** One timeline: a glowing line that forks as it grows. */
    private static void branch(GuiGraphics g, RandomSource rnd, float x, float y, float angle, float length, int depth, float tick, int color) {
        if (length < 6 || depth < 0) {
            return;
        }
        float seg = length * 0.45f;
        float ex = x + Mth.cos(angle) * seg;
        float ey = y + Mth.sin(angle) * seg;
        line(g, x, y, ex, ey, depth + 1, UiTheme.withAlpha(color, 0.35f + 0.15f * depth));
        float spread = 0.25f + rnd.nextFloat() * 0.3f;
        branch(g, rnd, ex, ey, angle - spread, length - seg, depth - 1, tick, color);
        if (rnd.nextFloat() < 0.75f) {
            branch(g, rnd, ex, ey, angle + spread, length - seg, depth - 1, tick, color);
        }
    }

    static void line(GuiGraphics g, float x1, float y1, float x2, float y2, int thick, int argb) {
        float len = (float) Math.hypot(x2 - x1, y2 - y1);
        RenderSystem.enableBlend();
        g.pose().pushPose();
        g.pose().translate(x1, y1, 0);
        g.pose().mulPose(Axis.ZP.rotation((float) Math.atan2(y2 - y1, x2 - x1)));
        g.fill(0, -thick / 2 - 1, (int) len, thick / 2 + 1, argb);
        g.fill(0, -thick / 2 - 3, (int) len, thick / 2 + 3, UiTheme.withAlpha(argb & 0xFFFFFF, 0.08f));
        g.pose().popPose();
        RenderSystem.disableBlend();
    }

    /** Draws a texture centred on (cx, cy), scaled and rotated, with an alpha. */
    static void sprite(GuiGraphics g, ResourceLocation tex, float cx, float cy, float w, float h, float degrees, float alpha,
                               int texW, int texH) {
        if (alpha <= 0.01f) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, Mth.clamp(alpha, 0f, 1f));
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        if (degrees != 0) {
            g.pose().mulPose(Axis.ZP.rotationDegrees(degrees));
        }
        g.pose().scale(w / texW, h / texH, 1f);
        g.blit(tex, -texW / 2, -texH / 2, 0, 0, texW, texH, texW, texH);
        g.pose().popPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    static float window(float t, float from, float to, float fade) {
        return Mth.clamp(Math.min((t - from) / fade, (to - t) / fade), 0f, 1f);
    }

    private static final ResourceLocation NEBULA = tex("nebula");
    private static final ResourceLocation[] BUBBLES = new ResourceLocation[8];
    private static final ResourceLocation[] BURNING = new ResourceLocation[8];

    static {
        for (int i = 0; i < BUBBLES.length; i++) {
            BUBBLES[i] = tex("bubble_" + i);
            BURNING[i] = tex("bubble_burn_" + i);
        }
    }

    /**
     * Scene 2: the fall through the multiverse, in depth. A slowly turning nebula, star streaks rushing past, a
     * silver-blue tunnel, and whole universes (glass spheres with a world inside) flying out of the dark towards
     * Steve and past him. From "what's happening to those worlds?" they crack and burn. A vast watcher looms while he
     * speaks; the fall speeds up and shakes as Steve loses control; the light drains for "And it starts."; then a
     * white flash as he falls out.
     */
    private static void falling(GuiGraphics g, int w, int h, float tick, float progress, String arg) {
        boolean still = UiConfig.REDUCE_MOTION.getAsBoolean();
        boolean losing = tick > 424 && tick < 486;
        float speed = losing ? 2.4f : tick < 40 ? 0.4f + tick / 40f * 0.6f : 1f;
        float cx = w / 2f;
        float cy = h / 2f;
        float big = Math.max(w, h);
        g.fill(0, 0, w, h, 0xFF000000);

        g.pose().pushPose();
        if (!still) {
            // Tumbling: the whole view rolls slowly, and shakes when Steve loses control.
            float shake = losing ? Mth.sin(tick * 2.3f) * 4f : 0f;
            g.pose().translate(cx + shake, cy + shake * 0.6f, 0);
            g.pose().mulPose(Axis.ZP.rotationDegrees(Mth.sin(tick * 0.012f) * 18f + tick * 0.05f));
            g.pose().translate(-cx, -cy, 0);
        }

        // Deep space behind everything, slowly zooming in.
        float neb = big * (1.6f + (still ? 0 : (tick % 600) / 600f * 0.5f));
        sprite(g, NEBULA, cx, cy, neb, neb, still ? 0 : -tick * 0.08f, 1f, 512, 512);

        // Star streaks rushing outward from the centre (perspective).
        RenderSystem.enableBlend();
        RandomSource rnd = RandomSource.create(2024L);
        for (int i = 0; i < 160; i++) {
            float ang = rnd.nextFloat() * Mth.TWO_PI;
            float z0 = rnd.nextFloat();
            float z = still ? z0 : (z0 + tick * 0.012f * speed) % 1f; // 0 far .. 1 near
            float r1 = big * 0.03f / (1.05f - z);
            float r2 = big * 0.03f / (1.05f - Math.max(0f, z - 0.04f * speed));
            if (r1 > big) {
                continue;
            }
            float a = Mth.clamp(z * 1.4f, 0f, 1f);
            line(g, cx + Mth.cos(ang) * r2, cy + Mth.sin(ang) * r2, cx + Mth.cos(ang) * r1, cy + Mth.sin(ang) * r1,
                    z > 0.8f ? 2 : 1, UiTheme.withAlpha(i % 5 == 0 ? 0xC8B4FF : 0xDDEEFF, a));
        }
        RenderSystem.disableBlend();

        // The tunnel: two counter-rotating layers of light.
        float spin = still ? 0 : tick * 1.3f * speed;
        sprite(g, VORTEX, cx, cy, big * 1.5f, big * 1.5f, spin, 0.55f, 512, 512);
        sprite(g, VORTEX, cx, cy, big * 0.7f, big * 0.7f, -spin * 1.5f, 0.45f, 512, 512);

        // Universes flying past, drawn far to near.
        if (tick > 50 && tick < 450) {
            int count = 12;
            float[][] list = new float[count][];
            for (int i = 0; i < count; i++) {
                float period = 140f;
                float z = still ? (i + 0.5f) / count : ((tick - 50) / period * speed + i / (float) count) % 1f;
                list[i] = new float[]{i, z};
            }
            java.util.Arrays.sort(list, (a, b) -> Float.compare(a[1], b[1]));
            boolean burning = tick > 156 && tick < 240;
            for (float[] e : list) {
                int i = (int) e[0];
                float z = e[1];
                float ang = i * 2.39996f;
                float spread = 0.12f + 0.9f * z * z;
                float bx = cx + Mth.cos(ang) * big * 0.42f * spread;
                float by = cy + Mth.sin(ang) * big * 0.32f * spread;
                float size = big * (0.03f + 0.5f * z * z * z);
                float a = Mth.clamp(z * 5f, 0f, 1f) * Mth.clamp((1f - z) * 6f, 0f, 1f);
                if (tick > 230) {
                    a *= 0.45f; // the watcher takes over
                }
                boolean burn = burning && i % 4 != 3;
                float wobble = burn && !still ? Mth.sin(tick * 1.7f + i) * 2f : 0f;
                sprite(g, burn ? BURNING[i % 8] : BUBBLES[i % 8], bx + wobble, by, size, size, still ? 0 : i * 23f + tick * 0.2f, a, 160, 160);
            }
        }

        // The watcher, while S002_020 plays.
        float watcher = window(tick, 225, 430, 40);
        if (watcher > 0) {
            float s = h * 1.15f;
            sprite(g, WATCHER, cx, h * 0.55f, s, s, 0, watcher * 0.85f, 192, 192);
        }
        g.pose().popPose();

        // Edge darkening, like a lens.
        for (int i = 0; i < 6; i++) {
            int m = (int) (big * 0.02f * i);
            float a = 0.12f;
            g.fill(0, 0, w, m, UiTheme.withAlpha(0x000000, a));
            g.fill(0, h - m, w, h, UiTheme.withAlpha(0x000000, a));
            g.fill(0, 0, m, h, UiTheme.withAlpha(0x000000, a));
            g.fill(w - m, 0, w, h, UiTheme.withAlpha(0x000000, a));
        }

        // "And it starts.": the light drains away.
        float dark = window(tick, 482, 522, 10);
        if (dark > 0) {
            g.fill(0, 0, w, h, UiTheme.withAlpha(0x000000, dark * 0.8f));
        }
        float flashIn = Mth.clamp(1 - tick / 25f, 0f, 1f);
        float flashOut = Mth.clamp((tick - 522) / 20f, 0f, 1f);
        float flash = Math.max(flashIn, flashOut);
        if (flash > 0) {
            g.fill(0, 0, w, h, UiTheme.withAlpha(0xDDEEFF, flash));
        }
    }

    /**
     * Scene 5: the rooftop at night, the orange time door swinging open with light pouring out, a walk into the
     * light, and the amber TVA lobby on the other side.
     */
    private static void tvaDoor(GuiGraphics g, int w, int h, float tick, float progress, String arg) {
        // Night sky and rooftops.
        for (int i = 0; i < 8; i++) {
            int y0 = h * i / 8;
            g.fill(0, y0, w, h * (i + 1) / 8, 0xFF000000 | lerpRgb(0x050814, 0x1A2140, i / 7f));
        }
        float skyH = h * 0.45f;
        sprite(g, SKYLINE, w / 4f, h - skyH / 2f, w / 2f + 2, skyH, 0, 1f, 256, 96);
        sprite(g, SKYLINE, w * 3 / 4f, h - skyH / 2f, w / 2f + 2, skyH, 0, 1f, 256, 96);
        g.fill(0, (int) (h * 0.86f), w, h, 0xFF1A1A1F);

        // The door grows as Steve walks towards it.
        float walk = Mth.clamp((tick - 45) / 50f, 0f, 1f);
        float zoom = 1 + walk * walk * 9f;
        float open = Mth.clamp((tick - 5) / 30f, 0f, 1f);
        int dh = (int) (h * 0.42f * zoom);
        int dw = (int) (dh * 0.55f);
        int cx = w / 2;
        int bottom = (int) (h * 0.86f + walk * h * 0.6f);
        int top = bottom - dh;
        int inner = (int) (dw * open);
        // Light rays fanning out from the doorway.
        if (open > 0.2f) {
            RenderSystem.enableBlend();
            for (int i = 0; i < 9; i++) {
                float ang = -60 + i * 15 + (UiConfig.REDUCE_MOTION.getAsBoolean() ? 0 : Mth.sin(tick * 0.05f + i) * 3);
                g.pose().pushPose();
                g.pose().translate(cx, top + dh / 2f, 0);
                g.pose().mulPose(Axis.ZP.rotationDegrees(ang + 180));
                g.fill(-3 - i % 3, 0, 3 + i % 3, (int) (h * 1.2f), UiTheme.withAlpha(0xFFB45A, 0.10f * open));
                g.pose().popPose();
            }
            RenderSystem.disableBlend();
        }
        g.fill(cx - dw / 2 - 4, top - 4, cx + dw / 2 + 4, bottom, UiTheme.withAlpha(0xC06018, 1f));
        g.fill(cx - dw / 2, top, cx + dw / 2, bottom, 0xFF120A04);
        g.fill(cx - inner / 2, top, cx + inner / 2, bottom, UiTheme.withAlpha(0xFFD9A0, 0.95f));
        g.fill(cx - inner / 4, top, cx + inner / 4, bottom, UiTheme.withAlpha(0xFFF4DE, 0.9f));

        // Through the door: a white-gold flash, then the TVA lobby.
        float through = window(tick, 88, 112, 12);
        float hall = Mth.clamp((tick - 100) / 15f, 0f, 1f);
        if (hall > 0) {
            float s = Math.max(w / 256f, h / 144f) * (1.15f - 0.1f * Mth.clamp((tick - 100) / 40f, 0f, 1f));
            sprite(g, TVA_HALL, w / 2f, h / 2f, 256 * s, 144 * s, 0, hall, 256, 144);
        }
        if (through > 0) {
            g.fill(0, 0, w, h, UiTheme.withAlpha(0xFFE6B8, through));
        }
        float out = Mth.clamp((tick - 125) / 15f, 0f, 1f);
        if (out > 0) {
            g.fill(0, 0, w, h, UiTheme.withAlpha(0x000000, out));
        }
    }

    static int lerpRgb(int a, int b, float t) {
        int r = (int) Mth.lerp(t, (a >> 16) & 255, (b >> 16) & 255);
        int gr = (int) Mth.lerp(t, (a >> 8) & 255, (b >> 8) & 255);
        int bl = (int) Mth.lerp(t, a & 255, b & 255);
        return (r << 16) | (gr << 8) | bl;
    }
}
