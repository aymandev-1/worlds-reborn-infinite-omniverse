package com.worldsreborn.ui.client.cinematic;

import com.worldsreborn.ui.client.UiConfig;
import com.worldsreborn.ui.client.UiTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client registry of cinematic presentations, plus the shared building-block renderers. Campaign mods register their
 * own cinematics from their client setup.
 */
public final class Cinematics {
    private static final Map<String, CinematicDef> DEFS = new ConcurrentHashMap<>();

    /** Black bars top and bottom; the world stays visible in between. */
    public static final CinematicRenderer LETTERBOX = (g, w, h, tick, progress, arg) -> letterbox(g, w, h, fadeIn(tick));

    /** Full black, for scene changes and narration cards. */
    public static final CinematicRenderer BLACK = (g, w, h, tick, progress, arg) -> g.fill(0, 0, w, h, 0xFF000000);

    /** Generic multiverse transit: dark void with silver-blue rings rushing outwards. */
    public static final CinematicRenderer TRAVEL = (g, w, h, tick, progress, arg) -> {
        g.fill(0, 0, w, h, 0xFF05070C);
        rings(g, w, h, tick, UiTheme.SILVER_BLUE);
        if (arg != null && !arg.isEmpty()) {
            var font = Minecraft.getInstance().font;
            Component line = Component.translatable("cinematic.wrinterface.travelling", Component.translatable(arg));
            g.drawCenteredString(font, line, w / 2, h - 40, UiTheme.withAlpha(UiTheme.TEXT, Mth.clamp(tick / 20f, 0, 1)));
        }
    };

    static {
        register(new CinematicDef("wrtravel:loading", TRAVEL, null, List.of()));
    }

    private Cinematics() {
    }

    public static void register(CinematicDef def) {
        DEFS.put(def.id(), def);
    }

    /** Unknown ids fall back to a plain black card so the timer still runs and the player is never stuck. */
    public static CinematicDef get(String id) {
        return DEFS.getOrDefault(id, new CinematicDef(id, BLACK, null, List.of()));
    }

    public static float fadeIn(float tick) {
        return Mth.clamp(tick / 10f, 0f, 1f);
    }

    public static void letterbox(GuiGraphics g, int w, int h, float amount) {
        int bar = (int) (h * 0.12f * amount);
        g.fill(0, 0, w, bar, 0xFF000000);
        g.fill(0, h - bar, w, h, 0xFF000000);
    }

    /** Expanding concentric rings. Drawn as thin outlines so it stays cheap; static when Reduce Motion is on. */
    public static void rings(GuiGraphics g, int w, int h, float tick, int color) {
        int cx = w / 2;
        int cy = h / 2;
        int max = Math.max(w, h);
        boolean still = UiConfig.REDUCE_MOTION.getAsBoolean();
        for (int i = 0; i < 9; i++) {
            float phase = still ? i / 9f : ((tick * 0.02f) + i / 9f) % 1f;
            int r = (int) (phase * phase * max * 0.75f) + 2;
            float alpha = (1f - phase) * 0.85f;
            g.renderOutline(cx - r, cy - r * 3 / 5, r * 2, r * 6 / 5, UiTheme.withAlpha(color, alpha));
        }
        g.fill(cx - 2, cy - 2, cx + 2, cy + 2, UiTheme.withAlpha(0xFFFFFF, 0.9f));
    }
}
