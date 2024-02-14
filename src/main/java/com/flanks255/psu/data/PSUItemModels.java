package com.flanks255.psu.data;

import com.flanks255.psu.PocketStorage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Supplier;

public class PSUItemModels extends ItemModelProvider {
    public PSUItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator.getPackOutput(), PocketStorage.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        generatePSU(PocketStorage.PSU1);
        generatePSU(PocketStorage.PSU2);
        generatePSU(PocketStorage.PSU3);
        generatePSU(PocketStorage.PSU4);
    }

    private void generatePSU(Supplier<Item> supplier) {
        String name = BuiltInRegistries.ITEM.getKey(supplier.get()).getPath();
        singleTexture(name, mcLoc("item/handheld"), "layer0", modLoc("item/psu"));
    }
}
