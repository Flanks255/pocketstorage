package com.flanks255.psu;

import com.flanks255.psu.commands.PSUCommands;
import com.flanks255.psu.crafting.CopyDataRecipe;
import com.flanks255.psu.crafting.TargetNBTIngredient;
import com.flanks255.psu.data.Generator;
import com.flanks255.psu.gui.PSUContainer;
import com.flanks255.psu.gui.PSUGui;
import com.flanks255.psu.items.PSUTier;
import com.flanks255.psu.items.PocketStorageUnit;
import com.flanks255.psu.network.PSUNetwork;
import com.flanks255.psu.util.RecipeUnlocker;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mod("pocketstorage")
public class PocketStorage
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "pocketstorage";
    public static SimpleChannel NETWORK;

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);

    public static final RegistryObject<Item> PSU1 = ITEMS.register("psu_1", () -> new PocketStorageUnit(PSUTier.TIER1));
    public static final RegistryObject<Item> PSU2 = ITEMS.register("psu_2", () -> new PocketStorageUnit( PSUTier.TIER2));
    public static final RegistryObject<Item> PSU3 = ITEMS.register("psu_3", () -> new PocketStorageUnit( PSUTier.TIER3));
    public static final RegistryObject<Item> PSU4 = ITEMS.register("psu_4", () -> new PocketStorageUnit( PSUTier.TIER4));

    public static final RegistryObject<MenuType<PSUContainer>> PSUCONTAINER = CONTAINERS.register("psu_container", () -> IForgeMenuType.create(PSUContainer::fromNetwork));

    public static final RegistryObject<RecipeSerializer<CopyDataRecipe>> UPGRADE_RECIPE = RECIPES.register("data_upgrade", CopyDataRecipe.Serializer::new);


    public PocketStorage() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(bus);
        CONTAINERS.register(bus);
        RECIPES.register(bus);

        bus.addListener(Generator::gatherData);

        MinecraftForge.EVENT_BUS.addListener(this::onCommandsRegister);

        bus.addListener(this::setup);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            bus.addListener(this::doClientStuff);
            bus.addListener(this::creativeTabEvent);
        }

        MinecraftForge.EVENT_BUS.addListener(this::pickupEvent);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::interactEvent);

        RecipeUnlocker.register(MODID, MinecraftForge.EVENT_BUS, 1);
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
        event.enqueueWork(() ->
            CraftingHelper.register(TargetNBTIngredient.Serializer.NAME, TargetNBTIngredient.SERIALIZER));
        NETWORK = PSUNetwork.getNetworkChannel();
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        MenuScreens.register(PSUCONTAINER.get(), PSUGui::new);
    }

    private void creativeTabEvent(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().compareTo(CreativeModeTabs.TOOLS_AND_UTILITIES) == 0) {
            event.accept(PSU1.get());
            event.accept(PSU2.get());
            event.accept(PSU3.get());
            event.accept(PSU4.get());
        }
    }
}
