package com.worldsreborn.ui.client.hud;

import com.worldsreborn.core.api.BlockPromptProvider;
import com.worldsreborn.core.api.ClientState;
import com.worldsreborn.core.api.EntityPromptProvider;
import com.worldsreborn.core.model.AbilityDef;
import com.worldsreborn.core.model.Location;
import com.worldsreborn.core.model.ObjectiveTarget;
import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.model.QuestDef;
import com.worldsreborn.core.model.QuestState;
import com.worldsreborn.core.model.StepDef;
import com.worldsreborn.core.model.StepType;
import com.worldsreborn.core.model.WRContent;
import com.worldsreborn.ui.client.ToastQueue;
import com.worldsreborn.ui.client.UiConfig;
import com.worldsreborn.ui.client.UiTheme;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * The Worlds Reborn HUD: tracked quest (top left), banners (top centre), interaction prompt (under the crosshair)
 * and ability slots (right of the hotbar, only once an ability is equipped). Nothing is drawn in the middle of the
 * view, and everything hides with F1.
 */
public final class HudLayers {
    private static final int TRACKER_W = 170;

    private HudLayers() {
    }

    private static boolean hidden(Minecraft mc) {
        return mc.options.hideGui || mc.player == null;
    }

    public static void questTracker(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (hidden(mc) || !UiConfig.SHOW_QUEST_TRACKER.getAsBoolean() || mc.getDebugOverlay().showDebugScreen()) {
            return;
        }
        PlayerProgress p = ClientState.progress();
        String tracked = p.trackedQuest();
        QuestDef quest = tracked == null ? null : WRContent.QUESTS.getOrNull(tracked);
        QuestState state = tracked == null ? null : p.questState(tracked);
        if (quest == null || state == null || state.completed) {
            return;
        }
        StepDef step = quest.step(state.stepIndex);
        if (step == null) {
            return;
        }
        Font font = mc.font;
        float scale = UiConfig.textScale();
        int innerW = TRACKER_W - 16;
        Component stepText = Component.translatable(step.descriptionKey());
        if (step.type() == StepType.COUNT) {
            stepText = Component.empty().append(stepText).append(" (" + state.count + "/" + step.required() + ")");
        }
        List<FormattedCharSequence> lines = font.split(FormattedText.composite(stepText), innerW - 10);
        String distance = distanceText(mc.player, p.target(), quest.id());
        int h = 30 + lines.size() * 10 + (distance != null ? 11 : 0);

        g.pose().pushPose();
        g.pose().translate(6, 6, 0);
        g.pose().scale(scale, scale, 1f);
        UiTheme.panel(g, 0, 0, TRACKER_W, h, UiTheme.GOLD);
        g.drawString(font, quest.sceneLabel(), 8, 5, UiTheme.CYAN, false);
        g.drawString(font, Component.translatable(quest.titleKey()), 8, 16, UiTheme.TEXT, false);
        int y = 28;
        g.drawString(font, "◆", 8, y, UiTheme.GOLD, false);
        for (FormattedCharSequence l : lines) {
            g.drawString(font, l, 18, y, UiTheme.TEXT_DIM, false);
            y += 10;
        }
        if (distance != null) {
            g.drawString(font, distance, 18, y + 1, UiTheme.GOLD, false);
        }
        g.pose().popPose();
    }

    private static String distanceText(LocalPlayer player, ObjectiveTarget target, String questId) {
        if (!UiConfig.SHOW_OBJECTIVE_DISTANCE.getAsBoolean() || target == null || !questId.equals(target.questId())) {
            return null;
        }
        Location loc = target.location();
        if (!player.level().dimension().location().toString().equals(loc.dimension())) {
            return null;
        }
        double d = Math.sqrt(player.distanceToSqr(loc.x(), loc.y(), loc.z()));
        if (d <= target.radius()) {
            return Component.translatable("hud.wrinterface.here").getString();
        }
        return Component.translatable("hud.wrinterface.distance", (int) d).getString();
    }

    public static void toasts(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (hidden(mc)) {
            return;
        }
        ToastQueue.Toast t = ToastQueue.current();
        if (t == null) {
            return;
        }
        Font font = mc.font;
        float p = ToastQueue.progress();
        float slide = UiConfig.REDUCE_MOTION.getAsBoolean() ? 1f : Mth.clamp(Math.min(p / 0.06f, (1f - p) / 0.06f), 0f, 1f);
        int w = Math.max(160, Math.max(font.width(t.title()), font.width(t.detail())) + 20);
        int x = (g.guiWidth() - w) / 2;
        int y = (int) (-40 + 46 * slide);
        UiTheme.panel(g, x, y, w, 36, t.accent());
        g.drawCenteredString(font, t.label(), g.guiWidth() / 2, y + 4, t.accent());
        g.drawCenteredString(font, t.title(), g.guiWidth() / 2, y + 14, UiTheme.TEXT);
        g.drawCenteredString(font, t.detail(), g.guiWidth() / 2, y + 25, UiTheme.TEXT_DIM);
    }

    /** Spoken lines during play, bottom centre above the hotbar. */
    public static void subtitles(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen instanceof com.worldsreborn.ui.client.screen.DialogueScreen) {
            return;
        }
        com.worldsreborn.ui.client.Subtitles.Line line = com.worldsreborn.ui.client.Subtitles.current();
        if (line == null) {
            return;
        }
        Font font = mc.font;
        int maxW = Math.min(340, g.guiWidth() - 40);
        List<FormattedCharSequence> rows = font.split(line.text(), maxW - 12);
        int w = 12 + Math.max(font.width(line.speaker()), rows.stream().mapToInt(font::width).max().orElse(0));
        int h = 16 + rows.size() * (font.lineHeight + 1);
        int x = (g.guiWidth() - w) / 2;
        int y = g.guiHeight() - 62 - h;
        g.fill(x, y, x + w, y + h, 0xA0000000);
        g.drawString(font, line.speaker(), x + 6, y + 4, UiTheme.EMERALD, false);
        int ty = y + 14;
        for (FormattedCharSequence r : rows) {
            g.drawString(font, r, x + 6, ty, UiTheme.TEXT, false);
            ty += font.lineHeight + 1;
        }
    }

    public static void prompt(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (hidden(mc) || mc.screen != null || !UiConfig.SHOW_INTERACTION_PROMPTS.getAsBoolean() || mc.level == null) {
            return;
        }
        HitResult hit = mc.hitResult;
        Component prompt = null;
        if (hit instanceof EntityHitResult eh && eh.getEntity() instanceof EntityPromptProvider pp) {
            prompt = pp.getInteractionPrompt(mc.player);
        } else if (hit instanceof BlockHitResult bh && hit.getType() == HitResult.Type.BLOCK) {
            BlockState state = mc.level.getBlockState(bh.getBlockPos());
            if (state.getBlock() instanceof BlockPromptProvider pp) {
                prompt = pp.getInteractionPrompt(mc.player, state, bh.getBlockPos());
            }
        }
        if (prompt == null) {
            return;
        }
        Font font = mc.font;
        KeyMapping use = mc.options.keyUse;
        Component key = Component.literal("[").append(use.getTranslatedKeyMessage()).append("] ");
        Component full = Component.empty().append(key.copy().withStyle(s -> s.withColor(UiTheme.GOLD))).append(prompt);
        int w = font.width(full) + 10;
        int x = (g.guiWidth() - w) / 2;
        int y = g.guiHeight() / 2 + 14;
        g.fill(x, y, x + w, y + 13, UiTheme.panelColor());
        g.drawString(font, full, x + 5, y + 3, UiTheme.TEXT, false);
    }

    public static void abilities(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (hidden(mc) || mc.level == null) {
            return;
        }
        PlayerProgress p = ClientState.progress();
        if (!p.hasAnyEquippedAbility()) {
            return;
        }
        Font font = mc.font;
        int size = 20;
        int x = g.guiWidth() / 2 + 91 + 8;
        int y = g.guiHeight() - size - 2;
        String[] keys = {"Z", "X", "C"};
        long now = mc.level.getGameTime();
        for (int i = 0; i < PlayerProgress.ABILITY_SLOTS; i++) {
            String id = p.equipped(i);
            AbilityDef def = id == null ? null : WRContent.ABILITIES.getOrNull(id);
            UiTheme.framed(g, x, y, size, size, def == null ? 0xFF3A4048 : UiTheme.CYAN);
            if (def != null) {
                String initial = Component.translatable(def.nameKey()).getString();
                initial = initial.isEmpty() ? "?" : initial.substring(0, 1);
                g.drawCenteredString(font, initial, x + size / 2, y + 6, UiTheme.TEXT);
                long left = p.cooldownRemaining(id, now);
                if (left > 0 && def.cooldownTicks() > 0) {
                    int cover = (int) (size * Math.min(1f, left / (float) def.cooldownTicks()));
                    g.fill(x + 1, y + size - cover, x + size - 1, y + size - 1, 0xA0000000);
                }
            }
            g.drawString(font, keys[i], x + 2, y - 9, UiTheme.TEXT_MUTED, false);
            x += size + 3;
        }
    }
}
