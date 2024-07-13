package com.flanks255.psu.util;

import com.flanks255.psu.PocketStorage;
import com.flanks255.psu.inventory.StorageManager;
import com.flanks255.psu.items.PocketStorageUnit;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PSUtils {
    @Nonnull
    public static Optional<UUID> getUUID(@Nonnull ItemStack stack) {
        if (stack.has(PocketStorage.PSU_UUID.get()))
            return Optional.ofNullable(stack.get(PocketStorage.PSU_UUID.get()));
        else if (stack.getItem() instanceof PocketStorageUnit && stack.has(DataComponents.CUSTOM_DATA) && stack.get(DataComponents.CUSTOM_DATA).contains("UUID"))
            return Optional.of(stack.get(DataComponents.CUSTOM_DATA).copyTag().getUUID("UUID"));
        else
            return Optional.empty();
    }

    public static Set<String> getUUIDSuggestions(CommandContext<CommandSourceStack> commandSource) {
        StorageManager manager = StorageManager.get();
        Set<String> list = new HashSet<>();

        manager.getMap().forEach((uuid, data) -> list.add(uuid.toString()));

        return list;
    }
}
