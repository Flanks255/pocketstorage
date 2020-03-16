package com.flanks255.psu;

import com.flanks255.psu.gui.PSUContainer;
import com.flanks255.psu.gui.PSUGui;
import com.flanks255.psu.items.PocketStorageUnit;
import com.flanks255.psu.network.PSUNetwork;
import net.minecraft.block.Block;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("pocketstorage")
public class PocketStorage
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "pocketstorage";
    public static SimpleChannel network;

    public static final PocketStorageUnit PSU1 = new PocketStorageUnit("psu_1", 8, 0xFF, Rarity.COMMON);
    public static final PocketStorageUnit PSU2 = new PocketStorageUnit("psu_2", 16, 0xFFF, Rarity.UNCOMMON);
    public static final PocketStorageUnit PSU3 = new PocketStorageUnit("psu_3", 32, 0xFFFF, Rarity.RARE);
    public static final PocketStorageUnit PSU4 = new PocketStorageUnit("psu_4", 64, 0xFFFFF, Rarity.EPIC);


    public PocketStorage() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        MinecraftForge.EVENT_BUS.addListener(this::pickupEvent);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void pickupEvent(EntityItemPickupEvent event) {
        if (event.getPlayer().openContainer instanceof PSUContainer || event.getPlayer().isSneaking())
            return;
        PlayerInventory playerInv = event.getPlayer().inventory;
        for (int i = 0; i <= 35; i++) {
            ItemStack stack = playerInv.getStackInSlot(i);
            if (stack.getItem() instanceof PocketStorageUnit && ((PocketStorageUnit) stack.getItem()).pickupEvent(event, stack)) {
                event.setResult(Event.Result.ALLOW);
                return;
            }
        }
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        network = PSUNetwork.getNetworkChannel();
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        ScreenManager.registerFactory(PSUContainer.type, PSUGui::new);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
            itemRegistryEvent.getRegistry().register(PSU1.setName());
            itemRegistryEvent.getRegistry().register(PSU2.setName());
            itemRegistryEvent.getRegistry().register(PSU3.setName());
            itemRegistryEvent.getRegistry().register(PSU4.setName());
        }

        @SubscribeEvent
        public static void onContainerRegistry(final RegistryEvent.Register<ContainerType<?>> containerRegistryEvent) {
            containerRegistryEvent.getRegistry().register(PSUContainer.type);
        }
        @SubscribeEvent
        public static void onRecipeRegistry(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
            event.getRegistry().register(new CopyDataRecipe.Serializer().setRegistryName(new ResourceLocation(MODID, "data_upgrade")));
        }
    }
}
