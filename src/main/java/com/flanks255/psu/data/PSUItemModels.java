package com.flanks255.psu.data;

import com.flanks255.psu.PocketStorage;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class PSUItemModels extends ItemModelProvider {
    public PSUItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, PocketStorage.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        generatePSU(PocketStorage.PSU1);
        generatePSU(PocketStorage.PSU2);
        generatePSU(PocketStorage.PSU3);
        generatePSU(PocketStorage.PSU4);
    }

    private void generatePSU(Supplier<Item> supplier) {
        String name = ForgeRegistries.ITEMS.getKey(supplier.get()).getPath();
        singleTexture(name, mcLoc("item/handheld"), "layer0", modLoc("item/psu"));
    }
}
