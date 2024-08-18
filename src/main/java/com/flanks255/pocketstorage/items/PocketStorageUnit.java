package com.flanks255.pocketstorage.items;

import com.flanks255.pocketstorage.PocketStorage;
import com.flanks255.pocketstorage.gui.PSUContainer;
import com.flanks255.pocketstorage.inventory.PSUData;
import com.flanks255.pocketstorage.inventory.PSUItemHandler;
import com.flanks255.pocketstorage.inventory.StorageManager;
import com.flanks255.pocketstorage.util.CapHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
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
        super(new Item.Properties().stacksTo(1).rarity(tierIn.rarity));
        this.tier = tierIn;
    }

    private boolean hasTranslation(String key) {
        return !I18n.get(key).equals(key);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull Item.TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
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

        if (flagIn.isAdvanced() && stack.has(PocketStorage.PSU_UUID.get())) {
            UUID uuid = stack.get(PocketStorage.PSU_UUID.get());
            tooltip.add(Component.literal("ID: " + uuid.toString().substring(0,8)).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else if (flagIn.isAdvanced() && stack.has(DataComponents.CUSTOM_DATA) && stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains("UUID")) {
            UUID uuid = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe().getUUID("UUID");
            tooltip.add(Component.literal("ID: " + uuid.toString().substring(0,8)).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    public boolean pickupEvent(ItemEntityPickupEvent.Pre event, ItemStack stack) {
        Optional<PSUItemHandler> handlerOpt = StorageManager.get().getHandler(stack);

        return handlerOpt.map(handler -> {
            ItemEntity itemEntity = event.getItemEntity();
            ItemStack pickedUp = itemEntity.getItem();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack slot = handler.getStackInSlot(i);
                if (ItemStack.isSameItemSameComponents(slot, pickedUp)) {
                    handler.insertItem(i, pickedUp, false);
                    pickedUp.setCount(0);
                    if(itemEntity.getAge() > 0)
                        event.getPlayer().level().playSound(null, event.getPlayer().blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);

                    event.setCanPickup(TriState.FALSE);
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

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide) {
            Level world = context.getLevel();
            if (world.getBlockState(context.getClickedPos()).hasBlockEntity() && context.getPlayer() != null && context.getPlayer().isCrouching()) {
                Optional<PSUItemHandler> handler = StorageManager.get().getHandler(context.getItemInHand());
                Optional<IItemHandler> chestOptional = CapHelper.getItemHandler(world, context.getClickedPos(), context.getClickedFace());
                handler.ifPresent(myItems -> chestOptional.ifPresent(chestHandler -> {
                    if (giveItems(myItems, chestHandler)) {
                        playSound(context.getLevel(), context.getClickedPos());
                        context.getPlayer().swing(context.getHand(), true);
                    }
                }));
            } else
                openGUI(context.getLevel(), context.getPlayer(), context.getHand());
        }
        return InteractionResult.FAIL;
    }

    private boolean giveItems(PSUItemHandler from, IItemHandler to) {
        boolean movedItems = false;
        for (int i = 0; i < from.getSlots(); i++) {
            ItemStack stack = from.getStackInSlot(i);
            if (stack.isEmpty())
                continue;
            ItemStack backup = from.getStackInSlot(i);
            backup.setCount(1);
            stack.setCount(stack.getCount()-1);
            ItemStack remainder = ItemHandlerHelper.insertItemStacked(to, stack, false );
            movedItems = true;
            if (remainder.isEmpty())
                from.getSlot(i).setCount(1);
            else
                from.getSlot(i).setCount(remainder.getCount()+1);
        }
        return movedItems;
    }

    private void playSound(Level level, BlockPos blockPos) {
        level.playSound(null, blockPos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5F, 0.5F + (random.nextFloat() * 0.5F));
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
            if (world.getBlockState(event.getPos()).hasBlockEntity()) {
                Optional<IItemHandler> chestOptional = CapHelper.getItemHandler(world, event.getPos(), event.getFace());
                Optional<PSUItemHandler> handler = StorageManager.get().getHandler(event.getEntity().getMainHandItem());
                handler.ifPresent(myItems -> chestOptional.ifPresent(chestHandler -> {
                    if (takeItems(chestHandler, myItems))
                        playSound(event.getLevel(), event.getPos());
                }));
            }
        }
    }

    private boolean takeItems(IItemHandler from, PSUItemHandler to) {
        boolean movedItems = false;
        for (int i = 0; i < from.getSlots(); i++) {
            ItemStack stack = from.getStackInSlot(i);
            if (stack.isEmpty())
                continue;
            if (to.hasItem(stack)) {
                ItemStack newStack = from.extractItem(i, stack.getCount(), false);
                from.insertItem(i, to.insertItemSlotless(newStack, false, false), false);
                movedItems = true;
            }
        }
        return movedItems;
    }

    public static PSUData getData(ItemStack stack) {
        if (!(stack.getItem() instanceof PocketStorageUnit))
            return null;
        UUID uuid;

        if (stack.has(PocketStorage.PSU_UUID.get())) {
            uuid = stack.get(PocketStorage.PSU_UUID.get());
        }
        else if (stack.has(DataComponents.CUSTOM_DATA)) { //Legacy support
            CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
            if (tag.contains("UUID")) {
                uuid = tag.getUUID("UUID");
                stack.set(PocketStorage.PSU_UUID.get(), uuid);
                stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, $ -> $.update(compoundTag -> compoundTag.remove("UUID")));
            }
            else {
                uuid = UUID.randomUUID();
                stack.set(PocketStorage.PSU_UUID.get(), uuid);
            }
        }
        else {
            uuid = UUID.randomUUID();
            stack.set(PocketStorage.PSU_UUID.get(), uuid);
        }

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
                packetBuffer -> packetBuffer.writeNbt(data.getHandler().serializeNBT(worldIn.registryAccess())).writeUUID(uuid).writeInt(data.getTier().ordinal())
            );
        }
    }
}
