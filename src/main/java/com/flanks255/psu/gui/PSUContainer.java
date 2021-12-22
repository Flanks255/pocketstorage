package com.flanks255.psu.gui;

import com.flanks255.psu.inventory.PSUItemHandler;
import com.flanks255.psu.PocketStorage;
import com.flanks255.psu.items.PSUTier;
import com.flanks255.psu.util.PSUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Optional;
import java.util.UUID;

public class PSUContainer extends Container {
    public PSUItemHandler handler;
    private final PlayerInventory playerInv;
    private final UUID uuid;


    public static PSUContainer fromNetwork(int windowId, PlayerInventory playerInventory, PacketBuffer data) {
        CompoundNBT nbt = data.readAnySizeNbt();
        UUID uuidIn = data.readUUID();
        PSUTier tier = PSUTier.values()[data.readInt()];
        PSUItemHandler handler = new PSUItemHandler(tier);
        handler.deserializeNBT(nbt);
        return new PSUContainer(windowId, playerInventory, uuidIn, handler);
    }
    public PSUContainer(final int windowId, final PlayerInventory playerInventory, UUID uuidIn, PSUItemHandler handlerIn) {
        super(PocketStorage.PSUCONTAINER.get(), windowId);

        uuid = uuidIn;
        playerInv = playerInventory;
        handler = handlerIn;

        addPlayerSlots(playerInventory);
    }

    @Override
    public ItemStack clicked(int slot, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (clickTypeIn == ClickType.SWAP)
            return ItemStack.EMPTY;

        if (slot >= 0) getSlot(slot).container.setChanged();
        return super.clicked(slot, dragType, clickTypeIn, player);
    }

    public void networkSlotClick(int slot, boolean shift, boolean ctrl, boolean rightClick) {
        if (slot >= 0 && slot <= handler.getSlots()) {
            if (!playerInv.getCarried().isEmpty()) {
                ItemStack incoming = playerInv.getCarried();
                if (incoming.hasTag() && playerInv.player.level.isClientSide()) {
                    playerInv.player.sendMessage(new TranslationTextComponent("pocketstorage.util.no_data_items"), Util.NIL_UUID);
                    return;
                }
                if (rightClick) {
                    ItemStack single = incoming.split(1);
                    ItemStack remainder = handler.insertItem(slot, single, false);
                    if (!remainder.isEmpty()) {
                        incoming.grow(1);
                    }
                    playerInv.setCarried(incoming);
                } else if (!ctrl) {
                    playerInv.setCarried(handler.insertItem(slot, incoming, false));
                }
                else {
                    if (incoming.getCount() < incoming.getMaxStackSize() && incoming.sameItem(handler.getStackInSlot(slot))) {
                        ItemStack tmp = handler.extractItem(slot, 1, false);
                        if (!tmp.isEmpty()) {
                            incoming.setCount(incoming.getCount()+1);
                            playerInv.setCarried(incoming);
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
                        playerInv.setCarried(tmp);
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
    public boolean stillValid(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity playerIn, int index) {
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            return handler.insertItemSlotless(slot.getItem(), true, true);
        }
        return ItemStack.EMPTY;
    }

    private void addPlayerSlots(PlayerInventory playerInventory) {
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
