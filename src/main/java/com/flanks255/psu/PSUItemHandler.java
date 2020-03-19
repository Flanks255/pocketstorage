package com.flanks255.psu;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;


import javax.annotation.Nonnull;

public class PSUItemHandler implements IItemHandler, IItemHandlerModifiable {
    public PSUItemHandler(ItemStack stack, int slotsCount, int capacity) {
        slotCount = slotsCount;
        slotCapacity = capacity;
        slots = NonNullList.withSize(slotsCount, PSUSlot.EMPTY);
        this.itemStack = stack;
        load();
    }

    private NonNullList<PSUSlot> slots;
    private ItemStack itemStack;
    private int slotCount;
    private int slotCapacity;

    void checkIndex(int slot) {
        if (slot < 0 || slot >= slots.size())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + slots.size() + ")");
    }

    @Override
    public int getSlots() {
        return slotCount;
    }

    @Override
    public int getSlotLimit(int slot) {
        return slotCapacity;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        checkIndex(slot);
        ItemStack tmp;
        if (!slots.get(slot).isEmpty()) {
            if (slots.get(slot).getCount() > 0) {
                tmp = slots.get(slot).getStack();
                tmp.setCount(slots.get(slot).getCount());
                return tmp;
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean hasItem(ItemStack stack) {
        //load();
        for (int i = 0; i < slots.size(); i++) {
            if (ItemHandlerHelper.canItemStacksStack(slots.get(i).getStack(), stack))
                return true;
        }
        return false;
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        checkIndex(slot);
        slots.set(slot, new PSUSlot(stack));
        save();
    }

    public ItemStack insertItemSlotless(@Nonnull ItemStack stack, boolean allowEmpty) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;
        load();
        for (int i = 0; i < slots.size(); i++) {
            if (ItemHandlerHelper.canItemStacksStack(slots.get(i).getStack(),stack)) {
                //Found matching item, insert it.
                PSUSlot tmp = slots.get(i);
                tmp.setCount(Math.min(tmp.getCount() + stack.getCount(), slotCapacity));
                save();
                ItemStack tmpstack = stack.copy();
                stack.setCount(0);
                return tmpstack;
            }
        }
        if (allowEmpty) {
            //No matching slots found, find an empty one.
            for (int n = 0; n < slots.size(); n++) {
                if (slots.get(n).isEmpty()) {
                    slots.set(n, new PSUSlot(stack));
                    save();
                    ItemStack tmpstack = stack.copy();
                    stack.setCount(0);
                    return tmpstack;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        checkIndex(slot);
        if (!isItemValid(slot, stack))
            return stack;

        load();

        //Slot is empty, fire away!
        if (slots.get(slot).isEmpty()) {
            if (!simulate) {
                slots.set(slot, new PSUSlot(stack));
                save();
            }
            return ItemStack.EMPTY;
        }
        else {
            if (slots.get(slot).registryName.equals(stack.getItem().getRegistryName())) {
                if (!simulate) {
                    PSUSlot tmp = slots.get(slot);
                    tmp.incrementCount(stack.getCount(), slotCapacity);
                    save();
                }
                return ItemStack.EMPTY;
            }
            else {
                return stack;
            }
        }

    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return ItemStack.EMPTY;
        checkIndex(slot);

        PSUSlot tmp = slots.get(slot);
        if (tmp.isEmpty())
            return ItemStack.EMPTY;

        ItemStack item = tmp.getStack();
        int extract = Math.min(amount, Math.min(tmp.getCount() ,item.getMaxStackSize()));
        item.setCount(extract);
        if (tmp.getCount() <= extract) {
            if (!simulate) {
                slots.set(slot, PSUSlot.EMPTY);
                save();
            }
        }
        else {
            if (!simulate) {
                tmp.decrementCount(extract);
                save();
            }
        }
        return item;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return !stack.hasTag();
    }

    public void save() {
        ListNBT tagList = new ListNBT();

        for (PSUSlot slot : slots){
            if (!slot.isEmpty()) {
                tagList.add(slot.writeNBT());
            }
            else {
                CompoundNBT tmp = new CompoundNBT();
                tmp.putString("Item", "");
                tmp.putInt("Count", 0);
                tagList.add(tmp);
            }
        }
        CompoundNBT nbt = itemStack.getOrCreateTag();
        nbt.put("Slots", tagList);

        itemStack.setTag(nbt);
    }
    public void load() {
        if (itemStack.hasTag())
            load(itemStack.getTag());
    }

    public void load(@Nonnull CompoundNBT nbt) {
        if (nbt.contains("Slots")) {
            ListNBT tagList = nbt.getList("Slots", Constants.NBT.TAG_COMPOUND);

            for (int i = 0; i < tagList.size(); i++) {
                CompoundNBT itemTag = tagList.getCompound(i);
                if (i < slots.size()) {
                    PSUSlot tmp = new PSUSlot(itemTag);
                    slots.set(i, tmp);
                }
            }
        }
    }
}
