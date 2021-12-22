package com.flanks255.psu.inventory;

import com.flanks255.psu.PocketStorage;
import com.flanks255.psu.items.PSUTier;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class StorageManager extends WorldSavedData {
    private static final String NAME = PocketStorage.MODID + "_data";

    private static final HashMap<UUID, PSUData> data = new HashMap<>();

    public static final StorageManager blankClient = new StorageManager();

    public StorageManager() {
        super(NAME);
    }

    public static StorageManager get() {
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER)
            return ServerLifecycleHooks.getCurrentServer().getLevel(World.OVERWORLD).getDataStorage().computeIfAbsent(StorageManager::new, NAME);
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

    @Override
    public void load(CompoundNBT nbt) {
        if (nbt.contains("PSUS")) {
            ListNBT list = nbt.getList("PSUS", Constants.NBT.TAG_COMPOUND);
            list.forEach((psuNBT) -> PSUData.fromNBT((CompoundNBT) psuNBT).ifPresent((psu) -> data.put(psu.getUuid(), psu)));
        }
    }

    @Override
    @Nonnull
    public CompoundNBT save(CompoundNBT compound) {
        ListNBT backpacks = new ListNBT();
        data.forEach(((uuid, backpackData) -> backpacks.add(backpackData.toNBT())));
        compound.put("PSUS", backpacks);
        return compound;
    }
}
