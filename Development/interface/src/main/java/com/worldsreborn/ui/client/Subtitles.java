package com.worldsreborn.ui.client;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Lines spoken during play (barks), shown one at a time near the bottom of the screen. Each stays up for its
 * voice-over, or for a reading time based on its length when there is no recording.
 */
public final class Subtitles {
    public record Line(Component speaker, Component text, String voice) {
    }

    private static final Deque<Line> QUEUE = new ArrayDeque<>();
    private static Line current;
    private static long shownAt;
    private static long minMillis;

    private Subtitles() {
    }

    public static void push(Line line) {
        QUEUE.add(line);
        if (current == null) {
            next();
        }
    }

    private static void next() {
        current = QUEUE.poll();
        if (current != null) {
            shownAt = Util.getMillis();
            Voice.play(current.voice());
            // Voiced lines move on shortly after the recording ends; others stay up long enough to read.
            minMillis = Voice.playing() ? 500L : 1800L + 55L * current.text().getString().length();
        }
    }

    /** The line to show now, or null. */
    public static Line current() {
        if (current != null && Util.getMillis() - shownAt > minMillis && !Voice.playing()) {
            next();
        }
        return current;
    }

    public static void clear() {
        QUEUE.clear();
        current = null;
    }
}
