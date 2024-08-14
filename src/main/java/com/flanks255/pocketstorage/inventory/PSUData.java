package com.flanks255.pocketstorage.inventory;

import com.flanks255.pocketstorage.items.PSUTier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Optional;
import java.util.UUID;

public class PSUData {
    private final UUID uuid;
    private PSUTier tier;
    private final PSUItemHandler inventory;
    private final Optional<IItemHandler> optional;
    public final Metadata meta = new Metadata();

    public PSUData(UUID uuidIn, PSUTier tierIn) {
        uuid = uuidIn;
        tier = tierIn;

        inventory = new PSUItemHandler(tier);
        optional = Optional.of(inventory);
    }

    //create from nbt
    public PSUData(UUID uuidIn, CompoundTag incomingNBT, HolderLookup.Provider registryAccess) {
        uuid = uuidIn;
        tier = PSUTier.values()[Math.min(incomingNBT.getInt("Tier"), PSUTier.TIER4.ordinal())];

        inventory = new PSUItemHandler(tier);
        inventory.deserializeNBT(registryAccess, incomingNBT.getCompound("Inventory"));
        optional = Optional.of(inventory);

        if (incomingNBT.contains("Metadata"))
            meta.deserializeNBT(registryAccess, incomingNBT.getCompound("Metadata"));
    }

    public UUID getUuid() {
        return uuid;
    }

    public Optional<IItemHandler> getOptional() {
        return optional;
    }

    public PSUItemHandler getHandler() {
        return inventory;
    }

    public void upgrade(PSUTier newTier) {
        if (newTier.ordinal() > tier.ordinal()) {
            tier = newTier;
            inventory.upgrade(tier);
        }
    }

    public void updateAccessRecords(String player, long time) {
        if (meta.firstAccessedTime == 0) {
            //new item, set creation data
            meta.firstAccessedTime = time;
            meta.firstAccessedPlayer = player;
        }

        meta.setLastAccessedTime(time);
        meta.setLastAccessedPlayer(player);
    }
    public PSUTier getTier() {
        return tier;
    }

    public static Optional<PSUData> fromNBT(CompoundTag nbt, HolderLookup.Provider registryAccess) {
        if (nbt.contains("UUID")) {
            UUID uuid = nbt.getUUID("UUID");
            return Optional.of(new PSUData(uuid, nbt, registryAccess));
        }
        return Optional.empty();
    }


    public CompoundTag toNBT(HolderLookup.Provider registryAccess) {
        CompoundTag nbt = new CompoundTag();

        nbt.putUUID("UUID", uuid);
        nbt.putString("StringUUID", uuid.toString());
        nbt.putInt("Tier", tier.ordinal());

        nbt.put("Inventory", inventory.serializeNBT(registryAccess));

        nbt.put("Metadata", meta.serializeNBT(registryAccess));

        return nbt;
    }



    public static class Metadata implements INBTSerializable<CompoundTag> {
        private String firstAccessedPlayer = "";

        private long firstAccessedTime = 0;
        private String lastAccessedPlayer = "";
        private long lastAccessedTime = 0;
        public long getLastAccessedTime() {
            return lastAccessedTime;
        }

        public void setLastAccessedTime(long lastAccessedTime) {
            this.lastAccessedTime = lastAccessedTime;
        }

        public String getLastAccessedPlayer() {
            return lastAccessedPlayer;
        }

        public void setLastAccessedPlayer(String lastAccessedPlayer) {
            this.lastAccessedPlayer = lastAccessedPlayer;
        }

        public long getFirstAccessedTime() {
            return firstAccessedTime;
        }

        public String getFirstAccessedPlayer() {
            return firstAccessedPlayer;
        }

        @Override
        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            CompoundTag nbt = new CompoundTag();

            nbt.putString("firstPlayer", firstAccessedPlayer);
            nbt.putLong("firstTime", firstAccessedTime);
            nbt.putString("lastPlayer", lastAccessedPlayer);
            nbt.putLong("lastTime", lastAccessedTime);

            return nbt;
        }

        @Override
        public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
            firstAccessedPlayer = nbt.getString("firstPlayer");
            firstAccessedTime = nbt.getLong("firstTime");
            lastAccessedPlayer = nbt.getString("lastPlayer");
            lastAccessedTime = nbt.getLong("lastTime");
        }
    }
}
