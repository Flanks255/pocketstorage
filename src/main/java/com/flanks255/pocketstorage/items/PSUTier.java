package com.flanks255.pocketstorage.items;

import com.flanks255.pocketstorage.PocketStorage;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;

public enum PSUTier {
    TIER1("Tier 1", Rarity.COMMON, 8, 0xFF, PocketStorage.PSU1),
    TIER2("Tier 2", Rarity.UNCOMMON, 16, 0xFFF, PocketStorage.PSU2),
    TIER3("Tier 3", Rarity.RARE, 32, 0xFFFF, PocketStorage.PSU3),
    TIER4("Tier 4", Rarity.EPIC, 64, 0xFFFFF, PocketStorage.PSU4);

    public final String name;
    public final Rarity rarity;
    public final int slots;
    public final int capacity;
    public final DeferredItem<Item> item;

    PSUTier(String name, Rarity rarity, int slots, int capacity, DeferredItem<Item> item) {
        this.name = name;
        this.rarity = rarity;
        this.slots = slots;
        this.capacity = capacity;
        this.item = item;
    }
}
