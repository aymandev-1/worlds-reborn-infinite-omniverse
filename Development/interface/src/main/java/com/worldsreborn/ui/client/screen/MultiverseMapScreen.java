package com.worldsreborn.ui.client.screen;

import com.worldsreborn.core.api.ClientState;
import com.worldsreborn.core.model.Destination;
import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.model.WRContent;
import com.worldsreborn.core.network.TravelRequestPayload;
import com.worldsreborn.ui.client.ThemedButton;
import com.worldsreborn.ui.client.UiTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Multiverse map (default key M). Shows every registered destination as a node: discovered places by name,
 * undiscovered hidden ones as "???", locked ones greyed. Travel from here only works once the story grants free
 * travel; the server checks again either way. Franchises that aren't built yet are listed as PLANNED.
 */
public class MultiverseMapScreen extends Screen {
    /** Upcoming franchises in the locked release order. Display only. */
    private static final List<String> PLANNED = List.of("dc", "ben10", "video_games", "anime", "film_tv", "literature",
            "original_worlds", "chronoverse");
    private static final String FREE_TRAVEL_FLAG = "wrtravel:free_travel";

    private final List<Destination> nodes = new ArrayList<>();
    private Destination selected;
    private long seenVersion = -1;

    public MultiverseMapScreen() {
        super(Component.translatable("screen.wrinterface.map"));
    }

    private boolean visible(PlayerProgress p, Destination d) {
        return !d.hiddenUntilDiscovered() || p.discoveredDestinations().contains(d.id());
    }

    private boolean isHere(Destination d) {
        return minecraft != null && minecraft.level != null
                && minecraft.level.dimension().location().toString().equals(d.dimension());
    }

    @Override
    protected void init() {
        clearWidgets();
        seenVersion = ClientState.version();
        nodes.clear();
        nodes.addAll(WRContent.DESTINATIONS.all());
        PlayerProgress p = ClientState.progress();

        int n = Math.max(1, nodes.size());
        int nodeW = 110;
        int gap = 12;
        int total = n * nodeW + (n - 1) * gap;
        int scale = total > width - 20 ? (width - 20 - (n - 1) * 4) / n : nodeW;
        int x = (width - (n * scale + (n - 1) * (scale == nodeW ? gap : 4))) / 2;
        int y = height / 2 - 40;
        for (Destination d : nodes) {
            boolean show = visible(p, d);
            boolean unlocked = p.isDestinationUnlocked(d);
            Component name = show ? Component.translatable(d.nameKey()) : Component.literal("???");
            int accent = isHere(d) ? UiTheme.EMERALD : unlocked ? UiTheme.CYAN : UiTheme.TEXT_MUTED;
            ThemedButton b = new ThemedButton(x, y, scale, 30, name, btn -> {
                selected = d;
                init();
            }, accent);
            b.active = d != selected;
            addRenderableWidget(b);
            x += scale + (scale == nodeW ? gap : 4);
        }

        if (selected != null) {
            boolean canTravel = p.hasFlag(FREE_TRAVEL_FLAG) && p.isDestinationUnlocked(selected) && !isHere(selected);
            ThemedButton travel = new ThemedButton(width / 2 - 50, height / 2 + 62, 100, 20,
                    Component.translatable("map.wrinterface.travel"), b -> {
                        PacketDistributor.sendToServer(new TravelRequestPayload(selected.id()));
                        onClose();
                    });
            travel.active = canTravel;
            addRenderableWidget(travel);
        }
    }

    @Override
    public void tick() {
        if (seenVersion != ClientState.version()) {
            init();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0xE005070C);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        g.drawCenteredString(font, title, width / 2, 14, UiTheme.CYAN);
        g.drawCenteredString(font, Component.translatable("map.wrinterface.hint"), width / 2, 26, UiTheme.TEXT_MUTED);

        // Thread connecting the nodes.
        if (nodes.size() > 1) {
            int y = height / 2 - 25;
            g.fill(20, y, width - 20, y + 1, 0x403FD0E0);
        }
        for (var r : renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }

        PlayerProgress p = ClientState.progress();
        if (selected != null) {
            int w = Math.min(300, width - 20);
            int x = (width - w) / 2;
            int y = height / 2 - 2;
            UiTheme.panel(g, x, y, w, 58, UiTheme.CYAN);
            boolean show = visible(p, selected);
            String universe = selected.universe() == null ? "" : selected.universe();
            g.drawString(font, Component.literal(universe), x + 8, y + 6, UiTheme.CYAN, false);
            Component status = Component.translatable(isHere(selected) ? "map.wrinterface.here"
                    : p.isDestinationUnlocked(selected) ? "map.wrinterface.unlocked" : "map.wrinterface.locked");
            g.drawString(font, status, x + w - 8 - font.width(status), y + 6, UiTheme.GOLD, false);
            Component desc = show ? Component.translatable(selected.descriptionKey()) : Component.translatable("map.wrinterface.unknown");
            int ly = y + 20;
            for (FormattedCharSequence line : font.split(FormattedText.composite(desc), w - 16)) {
                g.drawString(font, line, x + 8, ly, UiTheme.TEXT_DIM, false);
                ly += 10;
            }
            if (!p.hasFlag(FREE_TRAVEL_FLAG)) {
                g.drawCenteredString(font, Component.translatable("map.wrinterface.no_free_travel"), width / 2, height / 2 + 86, UiTheme.TEXT_MUTED);
            }
        }

        int py = height - 36;
        g.drawCenteredString(font, Component.translatable("map.wrinterface.planned"), width / 2, py, UiTheme.TEXT_MUTED);
        List<Component> parts = new ArrayList<>();
        for (String key : PLANNED) {
            parts.add(Component.translatable("franchise.wrinterface." + key));
        }
        Component line = Component.empty();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                line = line.copy().append("  ·  ");
            }
            line = line.copy().append(parts.get(i));
        }
        int lw = font.width(line);
        if (lw > width - 20) {
            for (FormattedCharSequence l : font.split(FormattedText.composite(line), width - 20)) {
                py += 11;
                g.drawCenteredString(font, l, width / 2, py, UiTheme.TEXT_MUTED);
            }
        } else {
            g.drawCenteredString(font, line, width / 2, py + 12, UiTheme.TEXT_MUTED);
        }
    }
}
