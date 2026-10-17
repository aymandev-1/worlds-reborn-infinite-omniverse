package com.worldsreborn.story;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.worldsreborn.core.model.PlayerProgress;
import com.worldsreborn.core.model.QuestDef;
import com.worldsreborn.core.model.QuestState;
import com.worldsreborn.core.model.WRContent;
import com.worldsreborn.core.profile.Profiles;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** /wr story ... developer tools for testing progression. */
public final class StoryCommands {
    private static final SuggestionProvider<CommandSourceStack> QUESTS = (c, b) ->
            SharedSuggestionProvider.suggest(WRContent.QUESTS.all().stream().map(QuestDef::id), b);

    private StoryCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("wr").requires(s -> s.hasPermission(2))
                .then(Commands.literal("story")
                        .then(Commands.literal("status").executes(c -> status(c.getSource(), c.getSource().getPlayerOrException())))
                        .then(Commands.literal("start").then(Commands.argument("quest", StringArgumentType.greedyString()).suggests(QUESTS)
                                .executes(c -> {
                                    ServerPlayer p = c.getSource().getPlayerOrException();
                                    String q = StringArgumentType.getString(c, "quest");
                                    StoryService.quest(q);
                                    boolean started = StoryService.start(p, q);
                                    c.getSource().sendSuccess(() -> Component.literal(started ? "Started " + q : q + " was already started"), true);
                                    return 1;
                                })))
                        .then(Commands.literal("complete").then(Commands.argument("quest", StringArgumentType.greedyString()).suggests(QUESTS)
                                .executes(c -> {
                                    ServerPlayer p = c.getSource().getPlayerOrException();
                                    QuestDef q = StoryService.quest(StringArgumentType.getString(c, "quest"));
                                    // Walk every remaining step through the normal path so campaign reactions still run.
                                    PlayerProgress progress = Profiles.get(p);
                                    progress.startQuest(q.id());
                                    int guard = 0;
                                    while (!progress.isQuestCompleted(q.id()) && guard++ < 64) {
                                        var step = progress.currentStep(q);
                                        StoryService.progress(p, q.id(), step.id(), step.required());
                                    }
                                    c.getSource().sendSuccess(() -> Component.literal("Completed " + q.id()), true);
                                    return 1;
                                })))
                        .then(Commands.literal("setstep").then(Commands.argument("step", IntegerArgumentType.integer(0))
                                .then(Commands.argument("quest", StringArgumentType.greedyString()).suggests(QUESTS)
                                        .executes(c -> {
                                            ServerPlayer p = c.getSource().getPlayerOrException();
                                            QuestDef q = StoryService.quest(StringArgumentType.getString(c, "quest"));
                                            Profiles.get(p).setQuestStep(q, IntegerArgumentType.getInteger(c, "step"));
                                            Profiles.sync(p);
                                            c.getSource().sendSuccess(() -> Component.literal("Set " + q.id() + " to step "
                                                    + IntegerArgumentType.getInteger(c, "step")), true);
                                            return 1;
                                        }))))));
    }

    private static int status(CommandSourceStack source, ServerPlayer player) {
        PlayerProgress p = Profiles.get(player);
        source.sendSuccess(() -> Component.literal("Story status for " + player.getScoreboardName()), false);
        for (QuestDef q : WRContent.QUESTS.all()) {
            QuestState s = p.questState(q.id());
            String state = s == null ? "not started" : s.completed ? "COMPLETE"
                    : "step " + (s.stepIndex + 1) + "/" + q.steps().size() + " (" + q.step(s.stepIndex).id() + ") count " + s.count;
            source.sendSuccess(() -> Component.literal(" " + q.sceneLabel() + "  " + q.id() + ": " + state), false);
        }
        return 1;
    }
}
