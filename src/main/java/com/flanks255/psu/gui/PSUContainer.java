package com.flanks255.psu.gui;

import com.flanks255.psu.PSUItemHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
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
        IItemHandler tmp = player.getHeldItemMainhand().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
        itemKey = player.getHeldItemMainhand().getTranslationKey();
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

    public void networkSlotClick(int slot, boolean shift, boolean ctrl, boolean rightClick) {
        if (slot >= 0 && slot <= handler.getSlots()) {
            if (!playerinv.getItemStack().isEmpty()) {
                ItemStack incoming = playerinv.getItemStack();
                if (incoming.hasTag() && playerinv.player.world.isRemote()) {
                    playerinv.player.sendMessage(new StringTextComponent(I18n.format("PocketStorage.nodataitems")));
                    return;
                }
                if (!ctrl) {
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
        return true;
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
