package com.flanks255.psu.inventory;

import com.flanks255.psu.PocketStorage;
import com.flanks255.psu.items.PSUTier;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class StorageManager extends SavedData {
    private static final String NAME = PocketStorage.MODID + "_data";

    private static final HashMap<UUID, PSUData> data = new HashMap<>();

    public static final StorageManager blankClient = new StorageManager();

    public static StorageManager get() {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
            return ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(StorageManager::load, StorageManager::new, NAME);
        else
            return blankClient;
    }

    public HashMap<UUID, PSUData> getMap() {
        return data;
    }

    public Optional<PSUData> getStorage(UUID uuid) {
        if (data.containsKey(uuid))
            return Optional.of(data.get(uuid));
        return Optional.empty();
    }

    public PSUData getOrCreateStorage(UUID uuid, PSUTier tier) {
        return data.computeIfAbsent(uuid, id ->{
            setDirty();
            return new PSUData(id, tier);
        });
    }

    public LazyOptional<IItemHandler> getCapability(UUID uuid) {
        if (data.containsKey(uuid))
            return data.get(uuid).getOptional();

        return LazyOptional.empty();
    }

    public Optional<PSUItemHandler> getHandler(ItemStack stack) {
        if (stack.getOrCreateTag().contains("UUID")) {
            UUID uuid = stack.getTag().getUUID("UUID");
            if (data.containsKey(uuid))
                return Optional.of(data.get(uuid).getHandler());
        }

        return Optional.empty();
    }

    public LazyOptional<IItemHandler> getCapability(ItemStack stack) {
        if (stack.getOrCreateTag().contains("UUID")) {
            UUID uuid = stack.getTag().getUUID("UUID");
            if (data.containsKey(uuid))
                return data.get(uuid).getOptional();
        }

        return LazyOptional.empty();
    }

    public static StorageManager load(CompoundTag nbt) {
        if (nbt.contains("PSUS")) {
            ListTag list = nbt.getList("PSUS", Tag.TAG_COMPOUND);
            list.forEach((psuNBT) -> PSUData.fromNBT((CompoundTag) psuNBT).ifPresent((psu) -> data.put(psu.getUuid(), psu)));
        }
        return new StorageManager();
    }

    @Override
    @Nonnull
    public CompoundTag save(CompoundTag compound) {
        ListTag backpacks = new ListTag();
        data.forEach(((uuid, backpackData) -> backpacks.add(backpackData.toNBT())));
        compound.put("PSUS", backpacks);
        return compound;
    }
}
