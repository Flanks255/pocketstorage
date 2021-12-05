package com.flanks255.psu;

import com.flanks255.psu.data.Generator;
import com.flanks255.psu.gui.PSUContainer;
import com.flanks255.psu.gui.PSUGui;
import com.flanks255.psu.items.PocketStorageUnit;
import com.flanks255.psu.network.PSUNetwork;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("pocketstorage")
public class PocketStorage
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "pocketstorage";
    public static SimpleChannel network;

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MODID);
    private static final DeferredRegister<IRecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);

    public static final RegistryObject<Item> PSU1 = ITEMS.register("psu_1", () -> new PocketStorageUnit(8, 0xFF, Rarity.COMMON));
    public static final RegistryObject<Item> PSU2 = ITEMS.register("psu_2", () -> new PocketStorageUnit( 16, 0xFFF, Rarity.UNCOMMON));
    public static final RegistryObject<Item> PSU3 = ITEMS.register("psu_3", () -> new PocketStorageUnit( 32, 0xFFFF, Rarity.RARE));
    public static final RegistryObject<Item> PSU4 = ITEMS.register("psu_4", () -> new PocketStorageUnit( 64, 0xFFFFF, Rarity.EPIC));

    public static final RegistryObject<ContainerType<PSUContainer>> PSUCONTAINER = CONTAINERS.register("psu_container", () -> IForgeContainerType.create(PSUContainer::new));

    public static final RegistryObject<IRecipeSerializer<CopyDataRecipe>> UPGRADE_RECIPE = RECIPES.register("data_upgrade", CopyDataRecipe.Serializer::new);


    public PocketStorage() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(bus);
        CONTAINERS.register(bus);
        RECIPES.register(bus);

        bus.addListener(Generator::gatherData);

        bus.addListener(this::setup);
        bus.addListener(this::doClientStuff);

        MinecraftForge.EVENT_BUS.addListener(this::pickupEvent);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::interactEvent);
    }

    private void pickupEvent(EntityItemPickupEvent event) {
        if (event.getPlayer().containerMenu instanceof PSUContainer || event.getPlayer().isShiftKeyDown())
            return;
        PlayerInventory playerInv = event.getPlayer().inventory;
        for (int i = 0; i <= 35; i++) {
            ItemStack stack = playerInv.getItem(i);
            if (stack.getItem() instanceof PocketStorageUnit && ((PocketStorageUnit) stack.getItem()).pickupEvent(event, stack)) {
                event.setResult(Event.Result.ALLOW);
                return;
            }
        }
    }

    private void interactEvent(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getItemStack().getItem() instanceof PocketStorageUnit && event.getPlayer().isShiftKeyDown()) {
            if (event.getSide() == LogicalSide.SERVER)
                ((PocketStorageUnit) event.getItemStack().getItem()).onLeftClickEvent(event);
            event.setCanceled(true);
        }
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        network = PSUNetwork.getNetworkChannel();
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        ScreenManager.register(PSUCONTAINER.get(), PSUGui::new);
    }
}
