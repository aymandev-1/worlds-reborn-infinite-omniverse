package com.worldsreborn.ui.client.screen;

import com.worldsreborn.core.model.DialogueChoice;
import com.worldsreborn.core.model.DialogueDef;
import com.worldsreborn.core.model.DialogueLine;
import com.worldsreborn.core.network.DialogueResponsePayload;
import com.worldsreborn.ui.client.ThemedButton;
import com.worldsreborn.ui.client.UiConfig;
import com.worldsreborn.ui.client.UiTheme;
import com.worldsreborn.ui.client.Voice;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Conversation panel along the bottom of the screen, keeping the centre of the view clear. Lines type out and their
 * voice-over plays; click, Space or Enter advances. When it is the player's character's turn, their line is offered
 * as a tone-labelled reply and the conversation waits for it. If the dialogue ends in a choice, the replies appear;
 * the player's character then speaks the chosen line and the other character answers before the conversation
 * closes. Esc skips the whole conversation (the server then applies its default choice).
 */
public class DialogueScreen extends Screen {
    private static final int PANEL_MAX_W = 420;

    private static final String PLAYER_SPEAKER = "speaker.wrinterface.player";

    private final DialogueDef def;
    /** Lines being played: the dialogue's own, then the chosen line and its reply. */
    private List<DialogueLine> script;
    private String chosen;
    private int lineIndex;
    private long lineStart = Util.getMillis();
    private boolean sent;

    public DialogueScreen(DialogueDef def) {
        super(Component.translatable("screen.wrinterface.dialogue"));
        this.def = def;
        this.script = def.lines();
        if (script.get(0).isPlayerReply()) {
            lineIndex = -1; // the player speaks first
        } else {
            Voice.play(script.get(0).voice());
        }
    }

    public String dialogueId() {
        return def.id();
    }

    @Override
    protected void init() {
        clearWidgets();
        if (replyPending()) {
            // The reply is drawn as a wrapped card in render(), so long lines never run off the screen.
            return;
        } else if (atChoices()) {
            List<DialogueChoice> choices = def.choices();
            int w = Math.min(PANEL_MAX_W, width - 20);
            int x = (width - w) / 2;
            int bh = 20;
            int y = height - 12 - choices.size() * (bh + 4);
            for (DialogueChoice c : choices) {
                Component label = Component.translatable(c.toneKey()).withStyle(s -> s.withColor(UiTheme.GOLD))
                        .append(Component.literal("  ")).append(Component.translatable(c.textKey()).withStyle(s -> s.withColor(UiTheme.TEXT)));
                addRenderableWidget(new ThemedButton(x, y, w, bh, label, b -> choose(c)));
                y += bh + 4;
            }
        }
    }

    /** The player's character speaks next: their line waits as a reply button. */
    private boolean replyPending() {
        int next = lineIndex + 1;
        return next < script.size() && script.get(next).isPlayerReply() && (lineIndex < 0 || fullyShown());
    }

    /** Wrapped lines of the pending reply card: the tone in gold on its own line, then what Steve will say. */
    private List<FormattedCharSequence> replyLines(int innerW) {
        DialogueLine reply = script.get(lineIndex + 1);
        List<FormattedCharSequence> out = new java.util.ArrayList<>();
        out.add(Component.literal("[1] ").withStyle(s -> s.withColor(UiTheme.TEXT_MUTED))
                .append(Component.translatable(reply.toneKey()).withStyle(s -> s.withColor(UiTheme.GOLD).withItalic(true)))
                .getVisualOrderText());
        out.addAll(font.split(Component.translatable(reply.textKey()).withStyle(s -> s.withColor(UiTheme.TEXT)), innerW));
        return out;
    }

    /** Screen rectangle of the reply card: x, y, w, h. */
    private int[] replyRect() {
        float scale = UiConfig.textScale();
        int w = Math.min(PANEL_MAX_W, width - 20);
        int innerW = (int) ((w - 16) / scale);
        int h = (int) (replyLines(innerW).size() * (font.lineHeight + 2) * scale) + 12;
        return new int[]{(width - w) / 2, height - 12 - h, w, h};
    }

    private void sayReply() {
        lineIndex++;
        lineStart = Util.getMillis();
        Voice.play(line().voice());
        init();
    }

    private boolean atChoices() {
        return !replyPending() && chosen == null && lineIndex >= script.size() - 1 && fullyShown() && !def.choices().isEmpty();
    }

    private DialogueLine line() {
        return script.get(Math.max(0, Math.min(lineIndex, script.size() - 1)));
    }

    /** The player picked a reply: speak it, play the answer, then report the choice. */
    private void choose(DialogueChoice c) {
        chosen = c.id();
        List<DialogueLine> after = new java.util.ArrayList<>();
        after.add(new DialogueLine(PLAYER_SPEAKER, c.textKey(), false, c.voice()));
        after.addAll(c.reply());
        script = after;
        lineIndex = 0;
        lineStart = Util.getMillis();
        Voice.play(line().voice());
        init();
    }

    private String lineText() {
        return Component.translatable(line().textKey()).getString();
    }

    private int visibleChars() {
        int speed = UiConfig.DIALOGUE_SPEED.getAsInt();
        if (speed <= 0) {
            return Integer.MAX_VALUE;
        }
        return (int) ((Util.getMillis() - lineStart) * speed / 1000L);
    }

    private boolean fullyShown() {
        return visibleChars() >= lineText().length();
    }

    private void advance() {
        if (replyPending()) {
            sayReply();
            return;
        }
        if (!fullyShown()) {
            lineStart = 0;
            init();
            return;
        }
        if (lineIndex < script.size() - 1) {
            lineIndex++;
            lineStart = Util.getMillis();
            Voice.play(line().voice());
            init();
        } else if (chosen != null) {
            respond(chosen, false);
        } else if (def.choices().isEmpty()) {
            respond("", false);
        }
    }

    private void respond(String choiceId, boolean skipped) {
        if (sent) {
            return;
        }
        sent = true;
        Voice.stop();
        PacketDistributor.sendToServer(new DialogueResponsePayload(def.id(), choiceId, skipped));
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }

    @Override
    public void tick() {
        // Rebuild once typing finishes so choice buttons appear without an extra click.
        if ((atChoices() || replyPending()) && children().isEmpty()) {
            init();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (replyPending()) {
            int[] r = replyRect();
            if (mouseX >= r[0] && mouseX < r[0] + r[2] && mouseY >= r[1] && mouseY < r[1] + r[3]) {
                sayReply();
            }
            return true;
        }
        if (!atChoices()) {
            advance();
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            respond("", true);
            return true;
        }
        if (!atChoices() && (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            advance();
            return true;
        }
        if (replyPending() && (keyCode == GLFW.GLFW_KEY_1 || keyCode == GLFW.GLFW_KEY_KP_1)) {
            sayReply();
            return true;
        }
        if (atChoices() && keyCode >= GLFW.GLFW_KEY_1 && keyCode < GLFW.GLFW_KEY_1 + def.choices().size()) {
            choose(def.choices().get(keyCode - GLFW.GLFW_KEY_1));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Pictures the line is about (the maps), side by side above the conversation panel. */
    private void drawImages(GuiGraphics g, int panelTop) {
        String images = line().images();
        if (images == null || images.isEmpty()) {
            return;
        }
        String[] list = images.split("\\|");
        int gap = 10;
        int size = Math.min(170, Math.min(panelTop - 24, (Math.min(PANEL_MAX_W, width - 20) - gap * (list.length - 1)) / list.length));
        if (size < 48) {
            return;
        }
        int total = size * list.length + gap * (list.length - 1);
        int x = (width - total) / 2;
        int y = Math.max(20, panelTop - size - 8);
        for (String id : list) {
            ResourceLocation tex = ResourceLocation.tryParse(id);
            if (tex != null) {
                g.fill(x - 2, y - 2, x + size + 2, y + size + 2, 0xC0000000);
                g.blit(tex, x, y, size, size, 0, 0, 256, 256, 256, 256);
            }
            x += size + gap;
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fillGradient(0, height / 2, width, height, 0x00000000, 0x90000000);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        float scale = UiConfig.textScale();
        int w = Math.min(PANEL_MAX_W, width - 20);
        int x = (width - w) / 2;
        int innerW = (int) ((w - 16) / scale);
        Component hint = Component.translatable("dialogue.wrinterface.skip_hint");
        g.drawString(font, hint, width - font.width(hint) - 6, 6, UiTheme.TEXT_MUTED, false);
        int replyH = 0;
        if (replyPending()) {
            int[] r = replyRect();
            replyH = r[3];
            boolean hover = mouseX >= r[0] && mouseX < r[0] + r[2] && mouseY >= r[1] && mouseY < r[1] + r[3];
            UiTheme.panel(g, r[0], r[1], r[2], r[3], hover ? UiTheme.TEXT : UiTheme.GOLD);
            g.pose().pushPose();
            g.pose().translate(r[0] + 8, r[1] + 6, 0);
            g.pose().scale(scale, scale, 1f);
            int ry = 0;
            for (FormattedCharSequence l : replyLines(innerW)) {
                g.drawString(font, l, 0, ry, UiTheme.TEXT, false);
                ry += font.lineHeight + 2;
            }
            g.pose().popPose();
        }
        if (lineIndex < 0) {
            return; // only the player's opening reply is on screen
        }

        String full = lineText();
        String shown = full.substring(0, Math.min(full.length(), visibleChars()));
        Component text = Component.literal(shown);
        if (line().placeholder()) {
            text = Component.translatable("dialogue.wrinterface.temp").withStyle(s -> s.withColor(UiTheme.TEXT_MUTED))
                    .append(" ").append(text);
        }
        List<FormattedCharSequence> lines = font.split(FormattedText.composite(text), innerW);
        int lineH = font.lineHeight + 2;
        int choicesH = replyPending() ? replyH : atChoices() ? def.choices().size() * 24 : 0;
        int textH = (int) ((lineH * (Math.max(2, lines.size()) + 1) + 6) * scale);
        int panelH = textH + 12;
        int y = height - 12 - choicesH - panelH - (choicesH > 0 ? 4 : 0);

        UiTheme.panel(g, x, y, w, panelH, UiTheme.EMERALD);
        drawImages(g, y);
        g.pose().pushPose();
        g.pose().translate(x + 8, y + 8, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, Component.translatable(line().speakerKey()), 0, 0,
                line().isPlayerReply() || PLAYER_SPEAKER.equals(line().speakerKey()) ? UiTheme.GOLD : UiTheme.EMERALD, false);
        int ty = lineH + 4;
        for (FormattedCharSequence l : lines) {
            g.drawString(font, l, 0, ty, UiTheme.TEXT, false);
            ty += lineH;
        }
        g.pose().popPose();

        if (!atChoices() && !replyPending()) {
            boolean blink = (Util.getMillis() / 500) % 2 == 0;
            if (fullyShown() && blink) {
                g.drawString(font, "▼", x + w - 12, y + panelH - 12, UiTheme.GOLD, false);
            }
        }
    }
}
