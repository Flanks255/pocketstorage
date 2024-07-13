package com.flanks255.psu.inventory;

import com.flanks255.psu.PocketStorage;
import com.flanks255.psu.items.PSUTier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

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
            return ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(new Factory<>(StorageManager::new, StorageManager::load), NAME);
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

    public Optional<IItemHandler> getCapability(UUID uuid) {
        if (data.containsKey(uuid))
            return data.get(uuid).getOptional();

        return Optional.empty();
    }

    public Optional<PSUItemHandler> getHandler(ItemStack stack) {
        if (stack.has(PocketStorage.PSU_UUID.get())) {
            UUID uuid = stack.get(PocketStorage.PSU_UUID.get());
            if (data.containsKey(uuid))
                return Optional.of(data.get(uuid).getHandler());
        }

        return Optional.empty();
    }

    public IItemHandler getCapability(ItemStack stack) {
        if (stack.has(PocketStorage.PSU_UUID.get())) {
            UUID uuid = stack.get(PocketStorage.PSU_UUID.get());
            if (data.containsKey(uuid))
                return data.get(uuid).getHandler();
        }

        return null;
    }

    public static StorageManager load(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        if (nbt.contains("PSUS")) {
            ListTag list = nbt.getList("PSUS", Tag.TAG_COMPOUND);
            list.forEach((psuNBT) -> PSUData.fromNBT((CompoundTag) psuNBT).ifPresent((psu) -> data.put(psu.getUuid(), psu)));
        }
        return new StorageManager();
    }

    @Override
    @Nonnull
    public CompoundTag save(CompoundTag compound,  HolderLookup.Provider pRegistries) {
        ListTag backpacks = new ListTag();
        data.forEach(((uuid, backpackData) -> backpacks.add(backpackData.toNBT())));
        compound.put("PSUS", backpacks);
        return compound;
    }
}
