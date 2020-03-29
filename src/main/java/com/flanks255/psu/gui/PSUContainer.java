package com.flanks255.psu.gui;

import com.flanks255.psu.PSUItemHandler;
import com.flanks255.psu.items.PocketStorageUnit;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class PSUContainer extends Container {

    public PSUContainer(final int windowId, final PlayerInventory playerInventory) {
        this(windowId, playerInventory.player.world, playerInventory.player.getPosition(), playerInventory, playerInventory.player);
    }
    public PSUContainer(int windowId, World world, BlockPos pos, PlayerInventory playerInventory, PlayerEntity player) {
        super(type, windowId);

        playerinv = playerInventory;
        ItemStack stack = findPSU(player);
        if (stack == null || stack.isEmpty()) {
            player.closeScreen();
            return;
        }

        IItemHandler tmp = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
        itemKey = stack.getTranslationKey();
        if (tmp instanceof PSUItemHandler) {
            handler = (PSUItemHandler) tmp;
            ((PSUItemHandler) tmp).load();

        } else
            player.closeScreen();
        addPlayerSlots(playerInventory);
    }

    public static final ContainerType type = new ContainerType<>(PSUContainer::new).setRegistryName("psu_container");
    public String itemKey;
    public PSUItemHandler handler;
    private PlayerInventory playerinv;
    protected int slotID;

    public ItemStack findPSU(PlayerEntity playerIn) {
        PlayerInventory playerInventory = playerIn.inventory;

        if (playerIn.getHeldItemMainhand().getItem() instanceof PocketStorageUnit) {
            for (int i = 0; i <= 8; i++) {
                ItemStack stack = playerInventory.getStackInSlot(i);
                if (stack == playerIn.getHeldItemMainhand()) {
                    slotID = i;
                    return stack;
                }
            }
        } else if (playerIn.getHeldItemOffhand().getItem() instanceof PocketStorageUnit) {
            slotID = -106;
            return playerIn.getHeldItemOffhand();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack slotClick(int slot, int dragType, ClickType clickTypeIn, PlayerEntity player) {
        if (slot >= 0) {
            if (getSlot(slot).getStack().getItem() instanceof PocketStorageUnit)
                return ItemStack.EMPTY;
        }
        if (clickTypeIn == ClickType.SWAP)
            return ItemStack.EMPTY;

        if (slot >= 0) getSlot(slot).inventory.markDirty();
        return super.slotClick(slot, dragType, clickTypeIn, player);
    }

    public void networkSlotClick(int slot, boolean shift, boolean ctrl, boolean rightClick) {
        if (slot >= 0 && slot <= handler.getSlots()) {
            if (!playerinv.getItemStack().isEmpty()) {
                ItemStack incoming = playerinv.getItemStack();
                if (incoming.hasTag() && playerinv.player.world.isRemote()) {
                    playerinv.player.sendMessage(new StringTextComponent(I18n.format("pocketstorage.nodataitems")));
                    return;
                }
                if (rightClick) {
                    ItemStack single = incoming.split(1);
                    ItemStack remainder = handler.insertItem(slot, single, false);
                    if (!remainder.isEmpty()) {
                        incoming.grow(1);
                    }
                    playerinv.setItemStack(incoming);
                } else if (!ctrl) {
                    playerinv.setItemStack(handler.insertItem(slot, incoming, false));
                }
                else {
                    if (incoming.getCount() < incoming.getMaxStackSize() && incoming.isItemEqual(handler.getStackInSlot(slot))) {
                        ItemStack tmp = handler.extractItem(slot, 1, false);
                        if (!tmp.isEmpty()) {
                            incoming.setCount(incoming.getCount()+1);
                            playerinv.setItemStack(incoming);
                        }
                    }
                }

            }
            else {
                int extract = ctrl ? 1 : 64;
                if (rightClick)
                    extract = Math.min(handler.getStackInSlot(slot).getCount() / 2, 32);
                if (!shift) {
                    ItemStack tmp = handler.extractItem(slot, extract, false);
                    if (!tmp.isEmpty()) {
                        playerinv.setItemStack(tmp);
                    }

                    return;
                }
                if (shift) {
                    ItemStack tmp = handler.extractItem(slot, extract, false);
                    if (!tmp.isEmpty()) {
                        ItemHandlerHelper.giveItemToPlayer(playerinv.player, tmp);
                    }
                    return;
                }
            }
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        if (slotID == -106)
            return true; //offhand, cant move it anyway...
        return !playerIn.inventory.getStackInSlot(slotID).isEmpty();
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
            Slot slot = this.inventorySlots.get(index);

            if (slot != null && slot.getHasStack()) {
                return handler.insertItemSlotless(slot.getStack(), true);
            }
            return ItemStack.EMPTY;
        }

    private void addPlayerSlots(PlayerInventory playerInventory) {
        int originX = 7;
        int originY = 97;

        //Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int x = originX + col * 18;
                int y = originY + row * 18;
                this.addSlot(new Slot(playerInventory, (col + row * 9) + 9, x+1, y+1));
            }
        }

        //Hotbar
        for (int col = 0; col < 9; col++) {
            int x = originX + col * 18;
            int y = originY + 58;
            this.addSlot(new Slot(playerInventory, col, x+1, y+1));
        }
    }

}
