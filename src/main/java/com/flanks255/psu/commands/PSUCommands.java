package com.flanks255.psu.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class PSUCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> commandNode = dispatcher.register(
            Commands.literal("pocketstorage")
                .then(List.register())
                .then(Recover.register())
                .then(Open.register())
        );

        dispatcher.register(Commands.literal("ps").redirect(commandNode));
    }
}
