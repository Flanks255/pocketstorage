package com.flanks255.psu.gui;

import com.flanks255.psu.inventory.PSUItemHandler;
import com.flanks255.psu.PocketStorage;
import com.flanks255.psu.items.PSUTier;
import com.flanks255.psu.util.PSUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Optional;
import java.util.UUID;

public class PSUContainer extends AbstractContainerMenu {
    public PSUItemHandler handler;
    private final Inventory playerInv;
    private final UUID uuid;


    public static PSUContainer fromNetwork(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        CompoundTag nbt = data.readAnySizeNbt();
        UUID uuidIn = data.readUUID();
        PSUTier tier = PSUTier.values()[data.readInt()];
        PSUItemHandler handler = new PSUItemHandler(tier);
        handler.deserializeNBT(nbt);
        return new PSUContainer(windowId, playerInventory, uuidIn, handler);
    }
    public PSUContainer(final int windowId, final Inventory playerInventory, UUID uuidIn, PSUItemHandler handlerIn) {
        super(PocketStorage.PSUCONTAINER.get(), windowId);

        uuid = uuidIn;
        playerInv = playerInventory;
        handler = handlerIn;

        addPlayerSlots(playerInventory);
    }

    @Override
    public void clicked(int slot, int dragType, ClickType clickTypeIn, Player player) {
        if (clickTypeIn == ClickType.SWAP)
            return;

        if (slot >= 0) getSlot(slot).container.setChanged();
        super.clicked(slot, dragType, clickTypeIn, player);
    }

    public void networkSlotClick(int slot, boolean shift, boolean ctrl, boolean rightClick) {
        if (slot >= 0 && slot <= handler.getSlots()) {
            if (!getCarried().isEmpty()) {
                ItemStack incoming = getCarried();
                if (incoming.hasTag() && playerInv.player.level.isClientSide()) {
                    playerInv.player.sendSystemMessage(Component.translatable("pocketstorage.util.no_data_items"));
                    return;
                }
                if (rightClick) {
                    ItemStack single = incoming.split(1);
                    ItemStack remainder = handler.insertItem(slot, single, false);
                    if (!remainder.isEmpty()) {
                        incoming.grow(1);
                    }
                    setCarried(incoming);
                } else if (!ctrl) {
                    setCarried(handler.insertItem(slot, incoming, false));
                }
                else {
                    if (incoming.getCount() < incoming.getMaxStackSize() && incoming.sameItem(handler.getStackInSlot(slot))) {
                        ItemStack tmp = handler.extractItem(slot, 1, false);
                        if (!tmp.isEmpty()) {
                            incoming.setCount(incoming.getCount()+1);
                            setCarried(incoming);
                        }
                    }
                }

            }
            else {
                int extract = ctrl ? 1 : 64;
                if (rightClick)
                    extract = Math.min(handler.getStackInSlot(slot).getCount() / 2, 32);
                ItemStack tmp = handler.extractItem(slot, extract, false);
                if (!shift) {
                    if (!tmp.isEmpty()) {
                        setCarried(tmp);
                    }
                }
                else {
                    if (!tmp.isEmpty()) {
                        ItemHandlerHelper.giveItemToPlayer(playerInv.player, tmp);
                    }
                }
            }
        }
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            return handler.insertItemSlotless(slot.getItem(), true, true);
        }
        return ItemStack.EMPTY;
    }

    private void addPlayerSlots(Inventory playerInventory) {
        int originX = 7;
        int originY = 97;

        //Hotbar
        for (int col = 0; col < 9; col++) {
            int x = originX + col * 18;
            int y = originY + 58;
            Optional<UUID> uuidOptional = PSUtils.getUUID(playerInventory.items.get(col));
            boolean lockMe = uuidOptional.map(id -> id.compareTo(this.uuid) == 0).orElse(false);
            this.addSlot(new LockableSlot(playerInventory, col, x+1, y+1, lockMe));
        }

        //Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int x = originX + col * 18;
                int y = originY + row * 18;
                int index = (col + row * 9) + 9;
                Optional<UUID> uuidOptional = PSUtils.getUUID(playerInventory.items.get(index));
                boolean lockMe = uuidOptional.map(id -> id.compareTo(this.uuid) == 0).orElse(false);
                this.addSlot(new LockableSlot(playerInventory, index, x+1, y+1, lockMe));
            }
        }
    }

}
