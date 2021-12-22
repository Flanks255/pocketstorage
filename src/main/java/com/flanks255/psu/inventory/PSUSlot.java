package com.flanks255.psu.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class PSUSlot {
    public static final PSUSlot EMPTY = new PSUSlot();
    public ResourceLocation registryName;
    private int count;

    public PSUSlot() {
        count = 0;
        registryName = new ResourceLocation("minecraft:air");
    }
    public PSUSlot(ResourceLocation itemIn, int countIn) {
        count = countIn;
        registryName = itemIn;
    }
    public PSUSlot(ItemStack stack) {
        registryName = stack.getItem().getRegistryName();
        count = stack.getCount();
    }
    public PSUSlot(CompoundNBT tag) {
        readNBT(tag);
    }

    public void setCount(int count) {
        this.count = count;
    }
    public int getCount() {
        return count;
    }
    public void decrementCount(int dec) {
        count -= dec;
    }
    public void incrementCount(int inc, int cap) {
        count = Math.min(count + inc, cap);
    }
    public int incrementCount(int inc) {
        count += inc;
        return count;
    }

    public boolean isEmpty() {
            return count <= 0;
    }

    public ItemStack getStack() {
        return new ItemStack(ForgeRegistries.ITEMS.getValue(registryName));
    }

    public CompoundNBT writeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putString("Item", registryName.toString());
        tag.putInt("Count", count);
        return tag;
    }

    public void readNBT(CompoundNBT tag) {
        if (tag.contains("Item"))
            registryName = new ResourceLocation(tag.getString("Item"));
        else {
            return;
        }
        if (tag.contains("Count"))
            count = tag.getInt("Count");
        else
            count = 0;
    }
}