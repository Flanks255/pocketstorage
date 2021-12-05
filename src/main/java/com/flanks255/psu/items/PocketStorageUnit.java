package com.flanks255.psu.items;

import com.flanks255.psu.PSUItemHandler;
import com.flanks255.psu.gui.PSUContainer;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class PocketStorageUnit extends Item {
    private final int size;
    private final Rarity rarity;
    private String name;
    private final int capacity;

    private long lastInteractMills = 0;
    private BlockPos lastInteractPos = new BlockPos(0,0,0);

    public PocketStorageUnit(int size, int capacity, Rarity rarity) {
        super(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_TOOLS));
        this.size = size;
        this.rarity = rarity;
        this.capacity = capacity;
    }
    @Override
    public Rarity getRarity(ItemStack p_77613_1_) {
        return rarity;
    }

    private boolean hasTranslation(String key) {
        return !I18n.get(key).equals(key);
    }

    private String fallbackString(String key, String fallback) {
        String tmp = I18n.get(key);
        return tmp.equals(key)?fallback:tmp;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        String translationKey = getDescriptionId();

        if (Screen.hasShiftDown()) {
            tooltip.add(new StringTextComponent(I18n.get( translationKey + ".info", size, capacity)));
            if (hasTranslation(translationKey + ".info2"))
                tooltip.add(new StringTextComponent( I18n.get(translationKey + ".info2")));
            if (hasTranslation(translationKey + ".info3"))
                tooltip.add(new StringTextComponent( I18n.get(translationKey + ".info3")));
            tooltip.add(new StringTextComponent(I18n.get("pocketstorage.deposit")));
            tooltip.add(new StringTextComponent(I18n.get("pocketstorage.withdraw")));
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
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        openGUI(worldIn,playerIn,handIn);
        return ActionResult.success(playerIn.getItemInHand(handIn));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public ActionResultType useOn(ItemUseContext context) {
        if (!context.getLevel().isClientSide) {
            World world = context.getLevel();
            BlockState bs = world.getBlockState(context.getClickedPos());
            if (bs.hasTileEntity()) {
                TileEntity te = world.getBlockEntity(context.getClickedPos());
                LazyOptional<IItemHandler> chestOptional = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                LazyOptional<IItemHandler> myOptional = context.getItemInHand().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                    myOptional.ifPresent((my) -> {
                        chestOptional.ifPresent((chest) -> {
                            if (my instanceof PSUItemHandler) {
                                ((PSUItemHandler) my).load();
                                for (int i = 0; i < my.getSlots(); i++) {
                                    ItemStack stack = my.getStackInSlot(i);
                                    if (stack.isEmpty())
                                        continue;
                                    ItemStack backup = my.getStackInSlot(i);
                                    backup.setCount(1);
                                    stack.setCount(stack.getCount()-1);
                                    ItemStack remainder = ItemHandlerHelper.insertItemStacked(chest, stack, false );
                                    if (remainder.isEmpty()) {
                                        ((PSUItemHandler) my).setStackInSlot(i, backup);
                                    }
                                    else {
                                        remainder.setCount(remainder.getCount()+1);
                                        ((PSUItemHandler) my).setStackInSlot(i, remainder);
                                    }
                                }
                            }
                        });
                    });
            } else
                openGUI(context.getLevel(), context.getPlayer(), context.getHand());
        }
        return ActionResultType.FAIL;
    }

    public void onLeftClickEvent(PlayerInteractEvent.LeftClickBlock event) {
        if (lastInteractPos.compareTo(event.getPos()) != 0)
            onLeftClick(event);
        else if (System.currentTimeMillis() - lastInteractMills > 1000)
            onLeftClick(event);
        lastInteractMills = System.currentTimeMillis();
        lastInteractPos = event.getPos();
    }

    private void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getWorld().isClientSide) {
            World world = event.getWorld();
            BlockState bs = world.getBlockState(event.getPos());
            if (bs.hasTileEntity()) {
                TileEntity te = world.getBlockEntity(event.getPos());
                LazyOptional<IItemHandler> chestOptional = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                LazyOptional<IItemHandler> myOptional = event.getPlayer().getMainHandItem().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
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
                                    ((PSUItemHandler) my).insertItemSlotless(newstack, false, false);
                                }
                            }
                        }
                    });
                });
            }
        }
    }

    private void openGUI(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!worldIn.isClientSide && playerIn.getItemInHand(handIn).getItem() instanceof PocketStorageUnit) {
            playerIn.openMenu(new SimpleNamedContainerProvider((windowId, playerInventory, playerEntity) ->
                    new PSUContainer(windowId, playerInventory, null), playerIn.getItemInHand(handIn).getHoverName()));
        }
    }


    class PSUCaps implements ICapabilityProvider {
        public PSUCaps(ItemStack stack) {
            inventory = new PSUItemHandler(stack, size, capacity);
            lazyOptional = LazyOptional.of(() -> inventory);
        }

        private final PSUItemHandler inventory;
        private final LazyOptional<IItemHandler> lazyOptional;

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
