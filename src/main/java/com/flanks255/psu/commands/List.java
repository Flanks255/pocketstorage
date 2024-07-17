package com.flanks255.psu.commands;

import com.flanks255.psu.inventory.PSUData;
import com.flanks255.psu.inventory.StorageManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class List {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("list").requires(cs -> cs.hasPermission(1)).executes(List::list)
            .then(Commands.literal("firstOpenedBy").then(Commands.argument("PlayerName", StringArgumentType.string())
                .suggests((cs, builder) -> SharedSuggestionProvider.suggest(getPlayerSuggestions(cs), builder))
                .executes(cs -> first(cs, StringArgumentType.getString(cs, "PlayerName")))))
            .then(Commands.literal("lastOpenedBy").then(Commands.argument("PlayerName", StringArgumentType.string())
                .suggests((cs, builder) -> SharedSuggestionProvider.suggest(getPlayerSuggestions(cs), builder))
                .executes(cs -> last(cs, StringArgumentType.getString(cs, "PlayerName")))));
    }

    public static Set<String> getPlayerSuggestions(CommandContext<CommandSourceStack> commandSource) {
        Set<String> list = new HashSet<>();
        commandSource.getSource().getServer().getPlayerList().getPlayers().forEach( serverPlayerEntity -> list.add(serverPlayerEntity.getName().getString()));

        return list;
    }

    public static int list(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        StorageManager storageManager = StorageManager.get();
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        if (storageManager.getMap().size() == 0) {
            ctx.getSource().sendSuccess(() -> Component.literal("[ ]"), false);
            return 0;
        }

        storageManager.getMap().forEach( (uuid, psuData) -> sendPS(player, psuData));

        return 0;
    }

    public static void sendPS(Player player, PSUData data) {
        player.sendSystemMessage(Component.literal("===========================").withStyle(ChatFormatting.DARK_GRAY));
        player.sendSystemMessage(Component.literal(
                data.getUuid().toString().substring(0,8) + "...\nFirst: " + data.meta.getFirstAccessedPlayer() + "\n" + SDF.format(new Date(data.meta.getFirstAccessedTime())) +
                        "\nLast: " + data.meta.getLastAccessedPlayer() + "\n" + SDF.format(new Date(data.meta.getLastAccessedTime()))
        ));


        var open_link = Component.literal("Open");
        open_link.withStyle(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ps open " + data.getUuid().toString()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Open Pocket Storage")))
                .withColor(ChatFormatting.BLUE)
                .withUnderlined(true));
        var recover_link = Component.literal("Recover");
        recover_link.withStyle(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ps recover " + data.getUuid().toString()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Recover Pocket Storage")))
                .withColor(ChatFormatting.GREEN)
                .withUnderlined(true));

        player.sendSystemMessage(Component.literal("[").append(open_link).append("] - [").append(recover_link).append("]"));
    }

    public static int first(CommandContext<CommandSourceStack> ctx, String playerName) throws CommandSyntaxException {
        StorageManager storageManager = StorageManager.get();
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        if (storageManager.getMap().size() == 0) {
            ctx.getSource().sendSuccess(() -> Component.literal("[ ]"), false);
            return 0;
        }

        storageManager.getMap().forEach( (uuid, psuData) -> {
            if (psuData.meta.getFirstAccessedPlayer().equalsIgnoreCase(playerName)) {
                sendPS(player, psuData);
            }
        });

        return 0;
    }

    public static int last(CommandContext<CommandSourceStack> ctx, String playerName) throws CommandSyntaxException {
        StorageManager storageManager = StorageManager.get();
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        if (storageManager.getMap().size() == 0) {
            ctx.getSource().sendSuccess(() -> Component.literal("[ ]"), false);
            return 0;
        }

        storageManager.getMap().forEach( (uuid, psuData) -> {
            if (psuData.meta.getLastAccessedPlayer().equalsIgnoreCase(playerName)) {
                sendPS(player, psuData);
            }
        });

        return 0;
    }
}
