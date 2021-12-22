package com.flanks255.psu.util;

import com.flanks255.psu.inventory.StorageManager;
import com.flanks255.psu.items.PocketStorageUnit;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PSUtils {
    @Nonnull
    public static Optional<UUID> getUUID(@Nonnull ItemStack stack) {
        if (stack.getItem() instanceof PocketStorageUnit && stack.hasTag() && stack.getTag().contains("UUID"))
            return Optional.of(stack.getTag().getUUID("UUID"));
        else
            return Optional.empty();
    }

    public static Set<String> getUUIDSuggestions(CommandContext<CommandSource> commandSource) {
        StorageManager manager = StorageManager.get();
        Set<String> list = new HashSet<>();

        manager.getMap().forEach((uuid, data) -> list.add(uuid.toString()));

        return list;
    }
}
