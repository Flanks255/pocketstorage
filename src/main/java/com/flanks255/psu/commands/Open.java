package com.flanks255.psu.commands;

import com.flanks255.psu.util.PSUtils;
import com.flanks255.psu.gui.PSUContainer;
import com.flanks255.psu.inventory.PSUData;
import com.flanks255.psu.inventory.StorageManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

import java.util.Optional;
import java.util.UUID;

public class Open {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("open")
            .requires(cs -> cs.hasPermission(1))
            .then(Commands.argument("UUID", StringArgumentType.string()).suggests(((context, builder) -> SharedSuggestionProvider.suggest(PSUtils.getUUIDSuggestions(context), builder))).executes(cs -> open(cs, StringArgumentType.getString(cs, "UUID"))));
    }

    public static int open(CommandContext<CommandSourceStack> ctx, String stringUUID) throws CommandSyntaxException {
        UUID uuid;
        try {
            uuid = UUID.fromString(stringUUID);
        }
        catch(IllegalArgumentException e){
            return 0;
        }
        StorageManager storageManager = StorageManager.get();

        if (storageManager.getMap().containsKey(uuid)) {
            ServerPlayer player = ctx.getSource().getPlayerOrException();

            Optional<PSUData> data = storageManager.getStorage(uuid);

            data.ifPresent(psu -> player.openMenu(new SimpleMenuProvider((windowId, playerInventory, playerEntity) ->
                    new PSUContainer(windowId, playerInventory, uuid, psu.getHandler()), Component.literal(psu.getTier().name)),
                packetBuffer -> packetBuffer.writeNbt(psu.getHandler().serializeNBT(RegistryAccess.EMPTY)).writeUUID(uuid).writeInt(psu.getTier().ordinal())));
        } else
            ctx.getSource().sendFailure(Component.translatable("pocketstorage.util.invalid_uuid"));
        return 0;
    }
}
