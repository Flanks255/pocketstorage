package com.flanks255.psu.items;

import com.flanks255.psu.PSUItemHandler;
import com.flanks255.psu.gui.PSUContainer;
import com.flanks255.psu.PocketStorage;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class PocketStorageUnit extends Item {
    public PocketStorageUnit(String name, int size, int capacity, Rarity rarity) {
        super(new Item.Properties().maxStackSize(1).group(ItemGroup.TOOLS));
        this.name = name;
        this.size = size;
        this.rarity = rarity;
        this.capacity = capacity;
    }

    private int size;
    private Rarity rarity;
    private String name;
    private int capacity;

    public PocketStorageUnit setName() {
        setRegistryName(new ResourceLocation(PocketStorage.MODID, name));
        return this;
    }

    @Override
    public Rarity getRarity(ItemStack p_77613_1_) {
        return rarity;
    }

    private boolean hasTranslation(String key) {
        return !I18n.format(key).equals(key);
    }

    private String fallbackString(String key, String fallback) {
        String tmp = I18n.format(key);
        return tmp.equals(key)?fallback:tmp;
    }


    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        String translationKey = getTranslationKey();

        if (Screen.hasShiftDown()) {
            tooltip.add(new StringTextComponent(I18n.format( translationKey + ".info", capacity, size)));
            if (hasTranslation(translationKey + ".info2"))
                tooltip.add(new StringTextComponent( I18n.format(translationKey + ".info2")));
            if (hasTranslation(translationKey + ".info3"))
                tooltip.add(new StringTextComponent( I18n.format(translationKey + ".info3")));
        }
        else {
            tooltip.add(new StringTextComponent( fallbackString("pocketstorage.shift", "Press <§6§oShift§r> for info.") ));
        }
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new PSUCaps(stack);
    }

    public boolean pickupEvent(EntityItemPickupEvent event, ItemStack stack) {
        CompoundNBT nbt = stack.getTag();
        if (nbt == null)
            return false;

        LazyOptional<IItemHandler> optional = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        if (!optional.isPresent())
            return false;

        IItemHandler handler = optional.orElse(null);
        if (handler == null || !(handler instanceof PSUItemHandler))
            return false;
        ((PSUItemHandler)handler).load();

        ItemStack pickedUp = event.getItem().getItem();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack slot = handler.getStackInSlot(i);
            if (ItemHandlerHelper.canItemStacksStack(slot, pickedUp)) {
                handler.insertItem(i, pickedUp, false);
                pickedUp.setCount(0);
                return true;
            }
        }
        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        openGUI(worldIn,playerIn,handIn);
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (!context.getWorld().isRemote) {
            World world = context.getWorld();
            BlockState bs = world.getBlockState(context.getPos());
            if (bs.hasTileEntity()) {
                TileEntity te = world.getTileEntity(context.getPos());
                LazyOptional<IItemHandler> chestOptional = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                LazyOptional<IItemHandler> myOptional = context.getItem().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                if (chestOptional.isPresent() && myOptional.isPresent()) {
                        myOptional.ifPresent((my) -> {
                            chestOptional.ifPresent((chest) -> {
                                if (my instanceof PSUItemHandler) {
                                    ((PSUItemHandler) my).load();
                                    for (int i = 0; i < chest.getSlots(); i++) {
                                        ItemStack stack = chest.getStackInSlot(i);
                                        if (stack.isEmpty())
                                            continue;
                                        if (((PSUItemHandler) my).hasItem(stack)) {
                                            ItemStack newstack = chest.extractItem(i, stack.getCount(), false);
                                                ((PSUItemHandler) my).insertItemSlotless(newstack, false);
                                        }
                                    }
                                }
                            });
                        });
                }
            } else
                openGUI(context.getWorld(), context.getPlayer(), context.getHand());
        }
        return ActionResultType.FAIL;
    }

    private void openGUI(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!worldIn.isRemote) {
            playerIn.openContainer((new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName() {
                    return playerIn.getHeldItem(handIn).getDisplayName();
                }

                @Nullable
                @Override
                public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
                    return new PSUContainer(p_createMenu_1_, p_createMenu_3_.world, p_createMenu_3_.getPosition(), p_createMenu_2_, p_createMenu_3_);
                }
            }));
        }
    }


    class PSUCaps implements ICapabilityProvider {
        public PSUCaps(ItemStack stack) {
            this.itemstack = stack;
            inventory = new PSUItemHandler(itemstack, size, capacity);
            lazyOptional = LazyOptional.of(() -> inventory);
        }

        private ItemStack itemstack;

        private PSUItemHandler inventory;
        private LazyOptional<IItemHandler> lazyOptional;

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                return lazyOptional.cast();
            }
            else
                return LazyOptional.empty();
        }
    }
}
