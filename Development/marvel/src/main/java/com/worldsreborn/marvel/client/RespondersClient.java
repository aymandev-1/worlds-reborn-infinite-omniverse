package com.worldsreborn.marvel.client;

import com.worldsreborn.marvel.MarvelIds;
import com.worldsreborn.ui.client.UiConfig;
import com.worldsreborn.ui.client.UiTheme;
import com.worldsreborn.ui.client.cinematic.CinematicDef;
import com.worldsreborn.ui.client.cinematic.Cinematics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.List;

/**
 * Client presentation of Doomsday Scenes 12-16: the card that hands the story to each scene's hero, and the short
 * travel fades of Scene 16. Everything else in these scenes is played in the world.
 */
final class RespondersClient {
    private static final ResourceLocation SKYLINE = MarvelClient.tex("skyline");
    private static final ResourceLocation OTHER_EARTH = MarvelClient.tex("other_earth");

    private RespondersClient() {
    }

    /** One hero card: which scene, whose story, where, the colours of their suit, and the backdrop. */
    private record Card(String scene, String skin, int accent, int skyTop, int skyBottom, boolean city, boolean signalLost) {
    }

    static void setup() {
        title(MarvelIds.CIN_S12_TITLE, new Card("s12", "sam_wilson", 0xFF2E5FB8, 0xFF050A1C, 0xFF16213F, true, false));
        title(MarvelIds.CIN_S13_TITLE, new Card("s13", "bucky_barnes", 0xFF8A2A2A, 0xFF07080C, 0xFF262019, true, false));
        title(MarvelIds.CIN_S14_TITLE, new Card("s14", "sam_wilson", 0xFF2E5FB8, 0xFF050A1C, 0xFF16213F, true, false));
        title(MarvelIds.CIN_S15_TITLE, new Card("s15", "sue_storm", 0xFF2F6FD0, 0xFF0B1B30, 0xFF5D86B5, false, true));
        title(MarvelIds.CIN_S16_TITLE, new Card("s16", "cyclops", 0xFFE0B020, 0xFF5A9BE0, 0xFFB9DAF5, false, false));
        Cinematics.register(new CinematicDef(MarvelIds.CIN_S16_STATION, (g, w, h, tick, progress, arg) ->
                travel(g, w, h, tick, "cinematic.wrmarvel.s16_station"), null, List.of()));
        Cinematics.register(new CinematicDef(MarvelIds.CIN_S16_RETURN, (g, w, h, tick, progress, arg) ->
                travel(g, w, h, tick, "cinematic.wrmarvel.s16_return"), null, List.of()));
    }

    private static void title(String id, Card card) {
        Cinematics.register(new CinematicDef(id, (g, w, h, tick, progress, arg) -> card(g, w, h, tick, card), null, List.of()));
    }

    private static boolean still() {
        return UiConfig.REDUCE_MOTION.getAsBoolean();
    }

    private static boolean noFlash() {
        return UiConfig.REDUCE_FLASHING.getAsBoolean();
    }

    /**
     * The hand-over card. Scene 15 opens on Reed's side of the failed connection (the static the Avengers just lost)
     * before the card; Scene 16 is day on the X-Men's Earth.
     */
    private static void card(GuiGraphics g, int w, int h, float tick, Card card) {
        Font font = Minecraft.getInstance().font;
        for (int i = 0; i < 16; i++) {
            g.fill(0, h * i / 16, w, h * (i + 1) / 16, 0xFF000000 | MarvelClient.lerpRgb(card.skyTop(), card.skyBottom(), i / 15f));
        }
        float drift = still() ? 0 : tick * 0.25f;
        if (card.city()) {
            MarvelClient.sprite(g, OTHER_EARTH, w * 0.78f, h * 0.24f, h * 0.22f, h * 0.22f, 0, 0.4f, 256, 256);
            MarvelClient.sprite(g, SKYLINE, w / 2f - drift, h * 0.82f, w * 1.4f, w * 0.5f, 0, 1f, 256, 96);
        } else if (card.signalLost()) {
            // The lab: benches and the receiving ring, simply drawn.
            g.fill(0, (int) (h * 0.78f), w, h, 0xFFDCE4EC);
            int cx = (int) (w * 0.7f), cy = (int) (h * 0.5f), r = (int) (h * 0.22f);
            for (int a = 0; a < 48; a++) {
                double t = a * Math.PI / 24;
                int x = cx + (int) (Math.cos(t) * r), y = cy + (int) (Math.sin(t) * r);
                g.fill(x - 3, y - 3, x + 3, y + 3, 0xFF9AA4AE);
            }
        } else {
            // The school grounds in daylight: lawn, the mansion's roofline, trees.
            g.fill(0, (int) (h * 0.72f), w, h, 0xFF5E9E3A);
            g.fill((int) (w * 0.25f), (int) (h * 0.46f), (int) (w * 0.75f), (int) (h * 0.72f), 0xFF8A4B32);
            g.fill((int) (w * 0.23f), (int) (h * 0.42f), (int) (w * 0.77f), (int) (h * 0.47f), 0xFF3B2A22);
            for (int i = 0; i < 6; i++) {
                int x = (int) (w * (0.3f + i * 0.08f));
                g.fill(x, (int) (h * 0.52f), x + (int) (w * 0.025f), (int) (h * 0.6f), 0xFFBFD8EE);
            }
            for (int i = 0; i < 4; i++) {
                int x = (int) (w * (0.05f + i * 0.06f));
                g.fill(x - 10, (int) (h * 0.55f), x + 10, (int) (h * 0.7f), 0xFF2F6B2A);
            }
        }
        // The hero, front on.
        float px = h / 70f;
        MarvelClient.figure(g, w * 0.26f, h * 0.95f, px, DoomsdayClient.skin(card.skin()), null, 1f, 1f, 1f,
                Mth.clamp((tick - 8) / 15f, 0f, 1f), 0);
        // Scene, hero, place.
        float a = Mth.clamp((tick - 18) / 15f, 0f, 1f);
        if (a > 0) {
            int x = (int) (w * 0.42f), y = (int) (h * 0.36f);
            g.fill(x - 10, y - 6, x - 6, y + 46, UiTheme.withAlpha(card.accent(), a));
            g.pose().pushPose();
            g.pose().translate(x, y, 0);
            g.pose().scale(1.6f, 1.6f, 1f);
            g.drawString(font, Component.translatable("cinematic.wrmarvel." + card.scene() + "_title.scene"), 0, 0, UiTheme.withAlpha(UiTheme.TEXT, a));
            g.pose().popPose();
            g.drawString(font, Component.translatable("cinematic.wrmarvel." + card.scene() + "_title.who"), x, y + 22, UiTheme.withAlpha(UiTheme.GOLD, a));
            g.drawString(font, Component.translatable("cinematic.wrmarvel." + card.scene() + "_title.where"), x, y + 36, UiTheme.withAlpha(UiTheme.TEXT_DIM, a));
        }
        if (card.signalLost()) {
            // The first moments: the connection breaking up from Reed's side.
            float lost = Mth.clamp(1 - (tick - 30) / 15f, 0f, 1f);
            if (lost > 0) {
                noise(g, w, h, tick, lost);
                g.fill(0, 0, w, h, UiTheme.withAlpha(0x000000, lost * 0.4f));
                g.drawCenteredString(font, Component.translatable("cinematic.wrmarvel.s15_title.lost"), w / 2, h / 2 - 4,
                        UiTheme.withAlpha(UiTheme.DANGER, lost));
            }
        }
        fade(g, w, h, tick, MarvelIds.CIN_TITLE_TICKS);
    }

    /** Scene 16's short trips between the school and the station: a fade with the place named. */
    private static void travel(GuiGraphics g, int w, int h, float tick, String key) {
        g.fill(0, 0, w, h, 0xFF000000);
        float a = Mth.clamp(Math.min(tick - 10, MarvelIds.CIN_S16_TRAVEL_TICKS - 10 - tick) / 12f, 0f, 1f);
        g.drawCenteredString(Minecraft.getInstance().font, Component.translatable(key), w / 2, h / 2 - 4, UiTheme.withAlpha(UiTheme.TEXT_DIM, a));
    }

    private static void noise(GuiGraphics g, int w, int h, float tick, float amount) {
        RandomSource rnd = RandomSource.create(noFlash() ? 7 : (long) tick);
        int cell = Math.max(3, w / 120);
        for (int y = 0; y < h; y += cell) {
            for (int x = 0; x < w; x += cell) {
                if (rnd.nextFloat() < 0.5f) {
                    int v = 60 + rnd.nextInt(noFlash() ? 60 : 190);
                    g.fill(x, y, x + cell, y + cell, UiTheme.withAlpha((v << 16) | (v << 8) | v, amount * 0.85f));
                }
            }
        }
    }

    private static void fade(GuiGraphics g, int w, int h, float tick, int total) {
        float a = Math.max(Mth.clamp(1 - tick / 12f, 0f, 1f), Mth.clamp((tick - (total - 14)) / 14f, 0f, 1f));
        if (a > 0) {
            g.fill(0, 0, w, h, UiTheme.withAlpha(0x000000, a));
        }
    }
}
