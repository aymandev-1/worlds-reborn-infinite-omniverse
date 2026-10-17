package com.worldsreborn.ui.client;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.Deque;

/** Quest and item banners shown at the top of the screen, one at a time. */
public final class ToastQueue {
    public static final long SHOW_MS = 4000;
    private static final int MAX_QUEUED = 8;
    private static final Deque<Toast> QUEUE = new ArrayDeque<>();
    private static Toast current;
    private static long shownAt;

    public record Toast(String style, Component title, Component detail) {
        public Component label() {
            return Component.translatable("toast.wrinterface." + style);
        }

        public int accent() {
            return switch (style) {
                case "quest_completed" -> UiTheme.GOLD;
                case "quest_updated" -> UiTheme.EMERALD;
                case "item" -> UiTheme.TEXT;
                default -> UiTheme.CYAN;
            };
        }
    }

    private ToastQueue() {
    }

    public static void push(Toast toast) {
        if (QUEUE.size() < MAX_QUEUED) {
            QUEUE.add(toast);
        }
    }

    /** @return the toast to draw now, or null; {@code progress} is 0..1 through its display time. */
    public static Toast current() {
        long now = Util.getMillis();
        if (current != null && now - shownAt > SHOW_MS) {
            current = null;
        }
        if (current == null && !QUEUE.isEmpty()) {
            current = QUEUE.poll();
            shownAt = now;
        }
        return current;
    }

    public static float progress() {
        return current == null ? 1f : Math.min(1f, (Util.getMillis() - shownAt) / (float) SHOW_MS);
    }

    public static void clear() {
        QUEUE.clear();
        current = null;
    }
}
