package com.flanks255.psu;

import com.flanks255.psu.commands.PSUCommands;
import com.flanks255.psu.crafting.UpgradeRecipe;
import com.flanks255.psu.data.Generator;
import com.flanks255.psu.gui.PSUContainer;
import com.flanks255.psu.gui.PSUGui;
import com.flanks255.psu.inventory.StorageManager;
import com.flanks255.psu.items.PSUTier;
import com.flanks255.psu.items.PocketStorageUnit;
import com.flanks255.psu.network.PSUNetwork;
import com.flanks255.psu.util.RecipeUnlocker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.EntityItemPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mod("pocketstorage")
public class PocketStorage
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "pocketstorage";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, MODID);

    public static final DeferredItem<Item> PSU1 = ITEMS.register("psu_1", () -> new PocketStorageUnit(PSUTier.TIER1));
    public static final DeferredItem<Item> PSU2 = ITEMS.register("psu_2", () -> new PocketStorageUnit( PSUTier.TIER2));
    public static final DeferredItem<Item> PSU3 = ITEMS.register("psu_3", () -> new PocketStorageUnit( PSUTier.TIER3));
    public static final DeferredItem<Item> PSU4 = ITEMS.register("psu_4", () -> new PocketStorageUnit( PSUTier.TIER4));

    public static final DeferredHolder<MenuType<?>, MenuType<PSUContainer>> PSUCONTAINER = CONTAINERS.register("psu_container", () -> IMenuTypeExtension.create(PSUContainer::fromNetwork));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<UpgradeRecipe>> UPGRADE_RECIPE = RECIPES.register("upgrade", UpgradeRecipe.Serializer::new);


    public PocketStorage(IEventBus bus) {
        IEventBus neoBus = NeoForge.EVENT_BUS;

        ITEMS.register(bus);
        CONTAINERS.register(bus);
        RECIPES.register(bus);

        bus.addListener(Generator::gatherData);
        neoBus.addListener(this::onCommandsRegister);
        bus.addListener(this::registerCaps);
        bus.addListener(PSUNetwork::register);

        bus.addListener(this::setup);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            bus.addListener(this::menuScreenEvent);
            bus.addListener(this::creativeTabEvent);
        }

        neoBus.addListener(this::pickupEvent);
        neoBus.addListener(this::interactEvent);

        RecipeUnlocker.register(MODID, neoBus, 1);
    }

    private void pickupEvent(EntityItemPickupEvent event) {
        if (event.getEntity().containerMenu instanceof PSUContainer || event.getEntity().isShiftKeyDown())
            return;
        Inventory playerInv = event.getEntity().getInventory();
        for (int i = 0; i <= 35; i++) {
            ItemStack stack = playerInv.getItem(i);
            if (stack.getItem() instanceof PocketStorageUnit && ((PocketStorageUnit) stack.getItem()).pickupEvent(event, stack)) {
                event.setResult(Event.Result.ALLOW);
                return;
            }
        }
    }

    private void interactEvent(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getItemStack().getItem() instanceof PocketStorageUnit && event.getEntity().isShiftKeyDown()) {
            if (event.getSide() == LogicalSide.SERVER)
                ((PocketStorageUnit) event.getItemStack().getItem()).onLeftClickEvent(event);
            event.setCanceled(true);
        }
    }

    private void onCommandsRegister(RegisterCommandsEvent event) {
        PSUCommands.register(event.getDispatcher());
    }

    private void setup(final FMLCommonSetupEvent event) {
    }

    private void menuScreenEvent(final RegisterMenuScreensEvent event) {
        event.register(PSUCONTAINER.get(), PSUGui::new);
    }

    private void creativeTabEvent(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().compareTo(CreativeModeTabs.TOOLS_AND_UTILITIES) == 0) {
            event.accept(PSU1.get());
            event.accept(PSU2.get());
            event.accept(PSU3.get());
            event.accept(PSU4.get());
        }
    }

    private void registerCaps(final RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.ItemHandler.ITEM, (stack, ctx) -> StorageManager.get().getCapability(stack)
                , PSU1, PSU2, PSU3, PSU4);
    }
}
