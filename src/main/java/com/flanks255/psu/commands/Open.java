package com.flanks255.psu.commands;

import com.flanks255.psu.util.PSUtils;
import com.flanks255.psu.gui.PSUContainer;
import com.flanks255.psu.inventory.PSUData;
import com.flanks255.psu.inventory.StorageManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.Optional;
import java.util.UUID;

public class Open {
    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("open")
            .requires(cs -> cs.hasPermission(1))
            .then(Commands.argument("UUID", StringArgumentType.string()).suggests(((context, builder) -> ISuggestionProvider.suggest(PSUtils.getUUIDSuggestions(context), builder))).executes(cs -> open(cs, StringArgumentType.getString(cs, "UUID"))));
    }

    public static int open(CommandContext<CommandSource> ctx, String stringUUID) throws CommandSyntaxException {
        UUID uuid;
        try {
            uuid = UUID.fromString(stringUUID);
        }
        catch(IllegalArgumentException e){
            return 0;
        }
        StorageManager storageManager = StorageManager.get();

        if (storageManager.getMap().containsKey(uuid)) {
            ServerPlayerEntity player = ctx.getSource().getPlayerOrException();

            Optional<PSUData> data = storageManager.getStorage(uuid);

            data.ifPresent(psu -> NetworkHooks.openGui(player, new SimpleNamedContainerProvider((windowId, playerInventory, playerEntity) ->
                    new PSUContainer(windowId, playerInventory, uuid, psu.getHandler()), new StringTextComponent(psu.getTier().name)),
                packetBuffer -> packetBuffer.writeNbt(psu.getHandler().serializeNBT()).writeUUID(uuid).writeInt(psu.getTier().ordinal())));
        } else
            ctx.getSource().sendFailure(new TranslationTextComponent("pocketstorage.util.invalid_uuid"));
        return 0;
    }
}
