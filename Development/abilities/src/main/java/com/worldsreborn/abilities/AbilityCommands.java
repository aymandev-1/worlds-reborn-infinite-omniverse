package com.worldsreborn.abilities;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.worldsreborn.core.model.AbilityDef;
import com.worldsreborn.core.model.WRContent;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** /wr abilities unlock|equip ... developer tools. */
final class AbilityCommands {
    private AbilityCommands() {
    }

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("wr").requires(s -> s.hasPermission(2))
                .then(Commands.literal("abilities")
                        .then(Commands.literal("unlock").then(Commands.argument("ability", StringArgumentType.greedyString())
                                .suggests((c, b) -> SharedSuggestionProvider.suggest(WRContent.ABILITIES.all().stream().map(AbilityDef::id), b))
                                .executes(c -> {
                                    String id = StringArgumentType.getString(c, "ability");
                                    boolean ok = AbilityService.unlock(c.getSource().getPlayerOrException(), id);
                                    c.getSource().sendSuccess(() -> Component.literal(ok ? "Unlocked " + id : "Unknown or already unlocked: " + id), false);
                                    return ok ? 1 : 0;
                                })))
                        .then(Commands.literal("equip").then(Commands.argument("slot", IntegerArgumentType.integer(1, 3))
                                .then(Commands.argument("ability", StringArgumentType.greedyString())
                                        .suggests((c, b) -> SharedSuggestionProvider.suggest(WRContent.ABILITIES.all().stream().map(AbilityDef::id), b))
                                        .executes(c -> {
                                            int slot = IntegerArgumentType.getInteger(c, "slot");
                                            String id = StringArgumentType.getString(c, "ability");
                                            boolean ok = AbilityService.equip(c.getSource().getPlayerOrException(), slot - 1, id);
                                            c.getSource().sendSuccess(() -> Component.literal(ok ? "Equipped " + id + " in slot " + slot
                                                    : "Unlock " + id + " first"), false);
                                            return ok ? 1 : 0;
                                        }))))));
    }
}
