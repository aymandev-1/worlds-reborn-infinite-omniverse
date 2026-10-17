package com.worldsreborn.ui.client.screen;

import com.worldsreborn.core.api.ClientState;
import com.worldsreborn.core.model.AbilityDef;
import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.model.QuestCategory;
import com.worldsreborn.core.model.QuestDef;
import com.worldsreborn.core.model.QuestState;
import com.worldsreborn.core.model.StepDef;
import com.worldsreborn.core.model.StepType;
import com.worldsreborn.core.model.WRContent;
import com.worldsreborn.core.network.TrackQuestPayload;
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
import java.util.Map;

/**
 * Quest journal (default key J). Left: active main quests, optional quests, then completed ones. Right: the selected
 * quest's scene, summary and steps. Future steps stay hidden so the journal never spoils the story.
 * A second tab lists unlocked abilities and equipped slots.
 */
public class JournalScreen extends Screen {
    private enum Tab { QUESTS, ABILITIES }

    private Tab tab = Tab.QUESTS;
    private final List<QuestDef> entries = new ArrayList<>();
    private QuestDef selected;
    private long seenVersion = -1;

    public JournalScreen() {
        super(Component.translatable("screen.wrinterface.journal"));
    }

    private void reload() {
        seenVersion = ClientState.version();
        PlayerProgress p = ClientState.progress();
        entries.clear();
        List<QuestDef> main = new ArrayList<>();
        List<QuestDef> optional = new ArrayList<>();
        List<QuestDef> done = new ArrayList<>();
        for (Map.Entry<String, QuestState> e : p.quests().entrySet()) {
            QuestDef q = WRContent.QUESTS.getOrNull(e.getKey());
            if (q == null) {
                continue;
            }
            if (e.getValue().completed) {
                done.add(0, q);
            } else if (q.category() == QuestCategory.OPTIONAL) {
                optional.add(q);
            } else {
                main.add(q);
            }
        }
        entries.addAll(main);
        entries.addAll(optional);
        entries.addAll(done);
        if (selected == null || !entries.contains(selected)) {
            String tracked = p.trackedQuest();
            selected = tracked != null ? WRContent.QUESTS.getOrNull(tracked) : null;
            if (selected == null && !entries.isEmpty()) {
                selected = entries.get(0);
            }
        }
    }

    private int left() {
        return (width - frameW()) / 2;
    }

    private int top() {
        return (height - frameH()) / 2;
    }

    private int frameW() {
        return Math.min(440, width - 20);
    }

    private int frameH() {
        return Math.min(260, height - 20);
    }

    @Override
    protected void init() {
        clearWidgets();
        reload();
        int x = left();
        int y = top();
        addRenderableWidget(new ThemedButton(x, y, 80, 16, Component.translatable("journal.wrinterface.tab.quests"),
                b -> switchTab(Tab.QUESTS), UiTheme.GOLD));
        addRenderableWidget(new ThemedButton(x + 84, y, 80, 16, Component.translatable("journal.wrinterface.tab.abilities"),
                b -> switchTab(Tab.ABILITIES), UiTheme.CYAN));
        if (tab == Tab.QUESTS) {
            int ly = y + 22;
            for (QuestDef q : entries) {
                if (ly > y + frameH() - 20) {
                    break;
                }
                boolean completed = ClientState.progress().isQuestCompleted(q.id());
                Component label = Component.translatable(q.titleKey());
                ThemedButton b = new ThemedButton(x + 4, ly, 150, 18, label, btn -> {
                    selected = q;
                    init();
                }, completed ? UiTheme.TEXT_MUTED : q.category() == QuestCategory.MAIN ? UiTheme.GOLD : UiTheme.CYAN);
                b.active = q != selected;
                addRenderableWidget(b);
                ly += 20;
            }
            if (selected != null && ClientState.progress().isQuestActive(selected.id())) {
                boolean tracked = selected.id().equals(ClientState.progress().trackedQuest());
                addRenderableWidget(new ThemedButton(x + frameW() - 92, y + frameH() - 24, 86, 18,
                        Component.translatable(tracked ? "journal.wrinterface.untrack" : "journal.wrinterface.track"),
                        b -> PacketDistributor.sendToServer(new TrackQuestPayload(tracked ? "" : selected.id())), UiTheme.GOLD));
            }
        }
    }

    private void switchTab(Tab t) {
        tab = t;
        init();
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
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        int x = left();
        int y = top();
        int w = frameW();
        int h = frameH();
        // Background (blur) is drawn by super.render(); draw the frame first, then widgets on top.
        renderBackground(g, mouseX, mouseY, partialTick);
        UiTheme.panel(g, x, y + 18, w, h - 18, tab == Tab.QUESTS ? UiTheme.GOLD : UiTheme.CYAN);
        for (var r : renderables) {
            r.render(g, mouseX, mouseY, partialTick);
        }
        if (tab == Tab.QUESTS) {
            renderQuests(g, x, y, w, h);
        } else {
            renderAbilities(g, x, y, w);
        }
    }

    private void renderQuests(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x + 158, y + 22, x + 159, y + h - 4, 0x60FFFFFF);
        if (entries.isEmpty()) {
            g.drawString(font, Component.translatable("journal.wrinterface.empty"), x + 10, y + 28, UiTheme.TEXT_DIM, false);
            return;
        }
        if (selected == null) {
            return;
        }
        int dx = x + 166;
        int dw = w - 174;
        int dy = y + 26;
        PlayerProgress p = ClientState.progress();
        QuestState state = p.questState(selected.id());
        boolean completed = state != null && state.completed;

        g.drawString(font, selected.sceneLabel(), dx, dy, UiTheme.CYAN, false);
        dy += 11;
        g.drawString(font, Component.translatable(selected.titleKey()), dx, dy, completed ? UiTheme.TEXT_DIM : UiTheme.GOLD, false);
        dy += 14;
        for (FormattedCharSequence line : font.split(FormattedText.composite(Component.translatable(selected.summaryKey())), dw)) {
            g.drawString(font, line, dx, dy, UiTheme.TEXT_DIM, false);
            dy += 10;
        }
        dy += 6;
        int current = state == null ? 0 : state.stepIndex;
        for (int i = 0; i < selected.steps().size(); i++) {
            StepDef s = selected.steps().get(i);
            boolean done = completed || i < current;
            Component text;
            int color;
            String mark;
            if (done) {
                text = Component.translatable(s.descriptionKey());
                color = UiTheme.TEXT_MUTED;
                mark = "✔";
            } else if (i == current) {
                text = Component.translatable(s.descriptionKey());
                if (s.type() == StepType.COUNT) {
                    text = Component.empty().append(text).append(" (" + state.count + "/" + s.required() + ")");
                }
                color = UiTheme.TEXT;
                mark = "◆";
            } else {
                text = Component.translatable("journal.wrinterface.hidden_step");
                color = UiTheme.TEXT_MUTED;
                mark = "◇";
            }
            g.drawString(font, mark, dx, dy, done ? UiTheme.EMERALD : UiTheme.GOLD, false);
            for (FormattedCharSequence line : font.split(FormattedText.composite(text), dw - 12)) {
                g.drawString(font, line, dx + 12, dy, color, false);
                dy += 10;
            }
            dy += 3;
            if (i > current && !completed) {
                break;
            }
        }
        if (completed) {
            g.drawString(font, Component.translatable("journal.wrinterface.completed"), dx, y + h - 18, UiTheme.EMERALD, false);
        }
    }

    private void renderAbilities(GuiGraphics g, int x, int y, int w) {
        PlayerProgress p = ClientState.progress();
        int dy = y + 28;
        g.drawString(font, Component.translatable("journal.wrinterface.equipped"), x + 10, dy, UiTheme.CYAN, false);
        dy += 14;
        for (int i = 0; i < PlayerProgress.ABILITY_SLOTS; i++) {
            String id = p.equipped(i);
            AbilityDef def = id == null ? null : WRContent.ABILITIES.getOrNull(id);
            Component name = def == null ? Component.translatable("journal.wrinterface.empty_slot") : Component.translatable(def.nameKey());
            g.drawString(font, Component.literal((i + 1) + ". ").append(name), x + 14, dy, def == null ? UiTheme.TEXT_MUTED : UiTheme.TEXT, false);
            dy += 12;
        }
        dy += 8;
        g.drawString(font, Component.translatable("journal.wrinterface.unlocked"), x + 10, dy, UiTheme.CYAN, false);
        dy += 14;
        if (p.unlockedAbilities().isEmpty()) {
            g.drawString(font, Component.translatable("journal.wrinterface.no_abilities"), x + 14, dy, UiTheme.TEXT_DIM, false);
        }
        for (String id : p.unlockedAbilities()) {
            AbilityDef def = WRContent.ABILITIES.getOrNull(id);
            if (def == null) {
                continue;
            }
            g.drawString(font, Component.translatable(def.nameKey()), x + 14, dy, UiTheme.TEXT, false);
            dy += 10;
            g.drawString(font, Component.translatable(def.descriptionKey()), x + 20, dy, UiTheme.TEXT_DIM, false);
            dy += 14;
        }
    }
}
