package com.flanks255.psu.items;

import com.flanks255.psu.gui.PSUContainer;
import com.flanks255.psu.inventory.PSUData;
import com.flanks255.psu.inventory.PSUItemHandler;
import com.flanks255.psu.inventory.StorageManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.EntityItemPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class PocketStorageUnit extends Item {
    private final PSUTier tier;
    private static final Random random = new Random();

    private long lastInteractMills = 0;
    private BlockPos lastInteractPos = new BlockPos(0,0,0);

    public PocketStorageUnit(PSUTier tierIn) {
        super(new Item.Properties().stacksTo(1));
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
            tooltip.add(Component.translatable( translationKey + ".info", Component.literal(String.valueOf(tier.slots)).withStyle(ChatFormatting.GOLD), Component.literal(String.valueOf(tier.capacity)).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
            if (hasTranslation(translationKey + ".info2"))
                tooltip.add(Component.translatable( translationKey + ".info2").withStyle(ChatFormatting.GRAY));
            if (hasTranslation(translationKey + ".info3"))
                tooltip.add(Component.translatable( translationKey + ".info3").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("pocketstorage.util.deposit", Component.translatable("pocketstorage.util.sneak_right").withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("pocketstorage.util.withdraw", Component.translatable("pocketstorage.util.sneak_left").withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
        }
        else {
            tooltip.add(Component.translatable("pocketstorage.util.shift", Component.translatable("pocketstorage.util.key_shift").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC)).withStyle(ChatFormatting.GRAY));
        }

        if (flagIn.isAdvanced() && stack.getTag() != null && stack.getTag().contains("UUID")) {
            UUID uuid = stack.getTag().getUUID("UUID");
            tooltip.add(Component.literal("ID: " + uuid.toString().substring(0,8)).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
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
            BlockState blockState = world.getBlockState(context.getClickedPos());
            if (blockState.hasBlockEntity() && context.getPlayer() != null && context.getPlayer().isCrouching()) {
                Optional<PSUItemHandler> handler = StorageManager.get().getHandler(context.getItemInHand());
                BlockEntity blockEntity = world.getBlockEntity(context.getClickedPos());
                Optional<IItemHandler> chestOptional = Optional.ofNullable(world.getCapability(Capabilities.ItemHandler.BLOCK, context.getClickedPos(), blockState, blockEntity, context.getClickedFace()));
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
        if (!event.getEntity().isCrouching() || event.getLevel().isClientSide)
            return;

        if (lastInteractPos.compareTo(event.getPos()) != 0)
            onLeftClick(event);
        else if (System.currentTimeMillis() - lastInteractMills > 1000)
            onLeftClick(event);
        lastInteractMills = System.currentTimeMillis();
        lastInteractPos = event.getPos();
    }

    private void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getLevel().isClientSide) {
            Level world = event.getLevel();
            BlockState blockState = world.getBlockState(event.getPos());
            if (blockState.hasBlockEntity()) {
                BlockEntity blockEntity = world.getBlockEntity(event.getPos());
                Optional<IItemHandler> chestOptional = Optional.ofNullable(world.getCapability(Capabilities.ItemHandler.BLOCK, event.getPos(), blockState, blockEntity, event.getFace()));
                Optional<PSUItemHandler> handler = StorageManager.get().getHandler(event.getEntity().getMainHandItem());
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
                        event.getLevel().playSound(null, event.getPos(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5F, 0.5F + (random.nextFloat() * 0.5F));
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

            data.updateAccessRecords(playerIn.getName().getString(), System.currentTimeMillis());

            // Upgrade Time
            if (data.getTier().ordinal() < tier.ordinal()) {
                data.upgrade(tier);
                playerIn.sendSystemMessage(Component.translatable("pocketstorage.util.upgrade"));
            }

            playerIn.openMenu(new SimpleMenuProvider((windowId, playerInventory, playerEntity) ->
                    new PSUContainer(windowId, playerInventory, uuid, data.getHandler()), stack.getHoverName()),
                packetBuffer -> packetBuffer.writeNbt(data.getHandler().serializeNBT()).writeUUID(uuid).writeInt(data.getTier().ordinal())
            );
        }
    }


/*    static class PSUCaps implements ICapabilityProvider {
        public PSUCaps(ItemStack stack) {
            this.stack = stack;
        }
        private final ItemStack stack;
        private LazyOptional<IItemHandler> lazyOptional = LazyOptional.empty();

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap == ForgeCapabilities.ITEM_HANDLER) {
                if(!lazyOptional.isPresent())
                    lazyOptional = StorageManager.get().getCapability(stack);
                return lazyOptional.cast();
            }
            else
                return LazyOptional.empty();
        }
    }*/
}
