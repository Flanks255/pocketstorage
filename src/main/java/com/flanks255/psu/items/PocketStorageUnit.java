package com.flanks255.psu.items;

import com.flanks255.psu.inventory.PSUData;
import com.flanks255.psu.inventory.PSUItemHandler;
import com.flanks255.psu.gui.PSUContainer;
import com.flanks255.psu.inventory.StorageManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
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
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.network.NetworkHooks;

public class PocketStorageUnit extends Item {
    private final PSUTier tier;
    private static final Random random = new Random();

    private long lastInteractMills = 0;
    private BlockPos lastInteractPos = new BlockPos(0,0,0);

    public PocketStorageUnit(PSUTier tierIn) {
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_TOOLS));
        this.tier = tierIn;
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return tier.rarity;
    }

    private boolean hasTranslation(String key) {
        return !I18n.get(key).equals(key);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level worldIn, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        String translationKey = getDescriptionId();

        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent( translationKey + ".info", new TextComponent(String.valueOf(tier.slots)).withStyle(ChatFormatting.GOLD), new TextComponent(String.valueOf(tier.capacity)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
            if (hasTranslation(translationKey + ".info2"))
                tooltip.add(new TranslatableComponent( translationKey + ".info2").withStyle(ChatFormatting.GRAY));
            if (hasTranslation(translationKey + ".info3"))
                tooltip.add(new TranslatableComponent( translationKey + ".info3").withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("pocketstorage.util.deposit", new TranslatableComponent("pocketstorage.util.sneak_right").withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
            tooltip.add(new TranslatableComponent("pocketstorage.util.withdraw", new TranslatableComponent("pocketstorage.util.sneak_left").withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
        }
        else {
            tooltip.add(new TranslatableComponent("pocketstorage.util.shift", new TranslatableComponent("pocketstorage.util.key_shift").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC)).withStyle(ChatFormatting.GRAY));
        }

        if (flagIn.isAdvanced() && stack.getTag() != null && stack.getTag().contains("UUID")) {
            UUID uuid = stack.getTag().getUUID("UUID");
            tooltip.add(new TextComponent("ID: " + uuid.toString().substring(0,8)).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new PSUCaps(stack);
    }

    public boolean pickupEvent(EntityItemPickupEvent event, ItemStack stack) {
        Optional<PSUItemHandler> handlerOpt = StorageManager.get().getHandler(stack);

        return handlerOpt.map(handler -> {
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
        }).orElse(false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        openGUI(worldIn,playerIn,handIn);
        return InteractionResultHolder.success(playerIn.getItemInHand(handIn));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide) {
            Level world = context.getLevel();
            BlockState bs = world.getBlockState(context.getClickedPos());
            if (bs.hasBlockEntity()) {
                BlockEntity te = world.getBlockEntity(context.getClickedPos());
                LazyOptional<IItemHandler> chestOptional = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                Optional<PSUItemHandler> handler = StorageManager.get().getHandler(context.getItemInHand());
                handler.ifPresent((my) -> chestOptional.ifPresent((chest) -> {
                    boolean movedItems = false;
                    for (int i = 0; i < my.getSlots(); i++) {
                        ItemStack stack = my.getStackInSlot(i);
                        if (stack.isEmpty())
                            continue;
                        ItemStack backup = my.getStackInSlot(i);
                        backup.setCount(1);
                        stack.setCount(stack.getCount()-1);
                        ItemStack remainder = ItemHandlerHelper.insertItemStacked(chest, stack, false );
                        movedItems = true;
                        if (remainder.isEmpty())
                            my.getSlot(i).setCount(1);
                        else
                            my.getSlot(i).setCount(remainder.getCount()+1);
                    }
                    if (movedItems) {
                        context.getLevel().playSound(null, context.getClickedPos(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5F, 0.5F + (random.nextFloat() * 0.5F));
                        context.getPlayer().swing(context.getHand(), true);
                    }
                }));
            } else
                openGUI(context.getLevel(), context.getPlayer(), context.getHand());
        }
        return InteractionResult.FAIL;
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
            Level world = event.getWorld();
            BlockState bs = world.getBlockState(event.getPos());
            if (bs.hasBlockEntity()) {
                BlockEntity te = world.getBlockEntity(event.getPos());
                LazyOptional<IItemHandler> chestOptional = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
                Optional<PSUItemHandler> handler = StorageManager.get().getHandler(event.getPlayer().getMainHandItem());
                handler.ifPresent((my) -> chestOptional.ifPresent((chest) -> {
                    boolean movedItems = false;
                    for (int i = 0; i < chest.getSlots(); i++) {
                        ItemStack stack = chest.getStackInSlot(i);
                        if (stack.isEmpty())
                            continue;
                        if (my.hasItem(stack)) {
                            ItemStack newStack = chest.extractItem(i, stack.getCount(), false);
                            my.insertItemSlotless(newStack, false, false);
                            movedItems = true;
                        }
                    }
                    if (movedItems)
                        event.getWorld().playSound(null, event.getPos(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5F, 0.5F + (random.nextFloat() * 0.5F));
                }));
            }
        }
    }

    public static PSUData getData(ItemStack stack) {
        if (!(stack.getItem() instanceof PocketStorageUnit))
            return null;
        UUID uuid;
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("UUID")) {
            uuid = UUID.randomUUID();
            tag.putUUID("UUID", uuid);
        } else
            uuid = tag.getUUID("UUID");
        return StorageManager.get().getOrCreateStorage(uuid, ((PocketStorageUnit) stack.getItem()).tier);
    }

    private void openGUI(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (!worldIn.isClientSide && playerIn instanceof ServerPlayer && stack.getItem() instanceof PocketStorageUnit) {
            PSUData data = PocketStorageUnit.getData(stack);

            PSUTier tier = ((PocketStorageUnit) stack.getItem()).tier;
            UUID uuid = data.getUuid();

            // Old PSU Migration Time
            if(stack.getOrCreateTag().contains("Slots")) {
                data.getHandler().deserializeNBT(stack.getTag());
                stack.getTag().remove("Slots");
                StorageManager.get().setDirty();
                playerIn.sendMessage(new TranslatableComponent("pocketstorage.util.migration"), Util.NIL_UUID);
            }

            data.updateAccessRecords(playerIn.getName().getString(), System.currentTimeMillis());

            // Upgrade Time
            if (data.getTier().ordinal() < tier.ordinal()) {
                data.upgrade(tier);
                playerIn.sendMessage(new TranslatableComponent("pocketstorage.util.upgrade"), Util.NIL_UUID);
            }

            NetworkHooks.openGui((ServerPlayer) playerIn, new SimpleMenuProvider((windowId, playerInventory, playerEntity) ->
                    new PSUContainer(windowId, playerInventory, uuid, data.getHandler()), stack.getHoverName()),
                packetBuffer -> packetBuffer.writeNbt(data.getHandler().serializeNBT()).writeUUID(uuid).writeInt(data.getTier().ordinal())
            );
        }
    }


    static class PSUCaps implements ICapabilityProvider {
        public PSUCaps(ItemStack stack) {
            this.stack = stack;
        }
        private final ItemStack stack;
        private LazyOptional<IItemHandler> lazyOptional = LazyOptional.empty();

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                if(!lazyOptional.isPresent())
                    lazyOptional = StorageManager.get().getCapability(stack);
                return lazyOptional.cast();
            }
            else
                return LazyOptional.empty();
        }
    }
}
