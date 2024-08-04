package com.flanks255.psu.inventory;

import com.flanks255.psu.items.PSUTier;
import com.flanks255.psu.items.PocketStorageUnit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;

public class PSUItemHandler implements IItemHandler, INBTSerializable<CompoundTag> {
    public PSUItemHandler(PSUTier tier) {
        slotCount = tier.slots;
        slotCapacity = tier.capacity;
        slots = NonNullList.withSize(tier.slots, PSUSlot.EMPTY);
    }

    private NonNullList<PSUSlot> slots;
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

    public PSUSlot getSlot(int slot) {
        checkIndex(slot);
        return slots.get(slot);
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
        for (PSUSlot slot : slots) {
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot.getStack(), stack))
                return true;
        }
        return false;
    }

    public void upgrade(PSUTier tier) {
        if (tier.slots <= this.slots.size())
            return;
        NonNullList<PSUSlot> oldStacks = this.slots;
        this.slots = NonNullList.withSize(tier.slots, PSUSlot.EMPTY);
        for (int i = 0; i < oldStacks.size(); i++) {
            this.slots.set(i, oldStacks.get(i));
        }
        slotCount = tier.slots;
        slotCapacity = tier.capacity;
    }

    public List<PSUSlot> getMatchingSlots(@Nonnull ItemStack stack) {
        return slots.stream().filter(s -> ItemStack.isSameItemSameComponents(s.getStack(), stack)).toList();
    }

    // Inserts an item into the first slot that matches the item.
    // Returns the remainder of the stack if it could not be fully inserted.
    public ItemStack insertItemSlotless(@Nonnull ItemStack stack, boolean allowEmpty, boolean allowVoid) {
        if (stack.isEmpty() || !isItemValid(0, stack))
            return ItemStack.EMPTY;

        boolean foundAny = false;
        int count = stack.getCount();

        for (PSUSlot slot : slots) {
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot.getStack(), stack)) {
                //Found matching item, insert it.
                foundAny = true;

                int remainder = allowVoid ? 0 : Math.max(slot.getCount() + stack.getCount() - slotCapacity, 0);
                slot.setCount(Math.min(slot.getCount() + stack.getCount(), slotCapacity));
                onContentsChanged();
                ItemStack returnStack = stack.copy();
                returnStack.setCount(remainder);
                return returnStack;
            }
        }


        if (allowEmpty) {
            //No matching slots found, find an empty one.
            for (int n = 0; n < slots.size(); n++) {
                if (slots.get(n).isEmpty()) {
                    slots.set(n, new PSUSlot(stack));
                    onContentsChanged();
                    return ItemStack.EMPTY;
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

        //Slot is empty, fire away!
        if (slots.get(slot).isEmpty()) {
            if (!simulate) {
                slots.set(slot, new PSUSlot(stack));
                onContentsChanged();
            }
            return ItemStack.EMPTY;
        }
        else {
            if (slots.get(slot).checkItem(stack)) {
                if (!simulate) {
                    PSUSlot tmp = slots.get(slot);
                    tmp.incrementCount(stack.getCount(), slotCapacity);
                    onContentsChanged();
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
                onContentsChanged();
            }
        }
        else {
            if (!simulate) {
                tmp.decrementCount(extract);
                onContentsChanged();
            }
        }
        return item;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return !stack.has(DataComponents.CUSTOM_DATA) &&
                !(stack.getItem() instanceof PocketStorageUnit) &&
                isStandardItem(stack);
    }

    public static boolean isStandardItem(ItemStack stack) {
        return stack.isComponentsPatchEmpty();
        //return ItemStack.isSameItemSameComponents(stack, stack.getItem().getDefaultInstance());
    }

    private void onContentsChanged() {
        StorageManager.get().setDirty();
    }
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        ListTag tagList = new ListTag();

        for (PSUSlot slot : slots){
            if (!slot.isEmpty()) {
                tagList.add(slot.writeNBT());
            }
            else {
                CompoundTag tmp = new CompoundTag();
                tmp.putString("Item", "");
                tmp.putInt("Count", 0);
                tagList.add(tmp);
            }
        }
        CompoundTag nbt = new CompoundTag();
        nbt.put("Slots", tagList);

        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains("Slots")) {
            ListTag tagList = nbt.getList("Slots", Tag.TAG_COMPOUND);

            for (int i = 0; i < tagList.size(); i++) {
                CompoundTag itemTag = tagList.getCompound(i);
                if (i < slots.size()) {
                    PSUSlot tmp = new PSUSlot(itemTag);
                    slots.set(i, tmp);
                }
            }
        }
    }
}
