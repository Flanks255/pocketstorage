package com.flanks255.psu.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class PSUCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralCommandNode<CommandSource> commandNode = dispatcher.register(
            Commands.literal("pocketstorage")
                .then(List.register())
                .then(Recover.register())
                .then(Open.register())
        );

        dispatcher.register(Commands.literal("ps").redirect(commandNode));
    }
}
