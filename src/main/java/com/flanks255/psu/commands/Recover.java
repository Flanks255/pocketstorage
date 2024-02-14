package com.flanks255.psu.commands;

import com.flanks255.psu.inventory.PSUData;
import com.flanks255.psu.inventory.StorageManager;
import com.flanks255.psu.util.PSUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.Optional;
import java.util.UUID;

public class Recover {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("recover")
            .requires(cs -> cs.hasPermission(1))
            .then(Commands.argument("UUID", StringArgumentType.string()).suggests(((context, builder) -> SharedSuggestionProvider.suggest(PSUtils.getUUIDSuggestions(context), builder))).executes(cs -> recover(cs, StringArgumentType.getString(cs, "UUID"))));
    }

    public static int recover(CommandContext<CommandSourceStack> ctx, String stringUUID) throws CommandSyntaxException {
        UUID uuid;
        try {
            uuid = UUID.fromString(stringUUID);
        }
        catch(IllegalArgumentException e){
            return 0;
        }
        StorageManager storage = StorageManager.get();

        if (storage.getMap().containsKey(uuid)) {
            ServerPlayer player = ctx.getSource().getPlayerOrException();

            Optional<PSUData> data = storage.getStorage(uuid);

            data.ifPresent(psu -> {
                ItemStack stack = new ItemStack(psu.getTier().item.get());
                stack.getOrCreateTag().putUUID("UUID", psu.getUuid());

                ItemHandlerHelper.giveItemToPlayer(player, stack);
            });
        } else
            ctx.getSource().sendFailure(Component.translatable("pocketstorage.util.invalid_uuid"));
        return 0;
    }
}
