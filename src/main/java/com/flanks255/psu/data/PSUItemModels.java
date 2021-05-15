package com.flanks255.psu.data;

import com.flanks255.psu.PocketStorage;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class PSUItemModels extends ItemModelProvider {
    public PSUItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, PocketStorage.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        singleTexture(PocketStorage.PSU1.get().getRegistryName().getPath(), mcLoc("item/handheld"), "layer0", modLoc("item/psu"));
        singleTexture(PocketStorage.PSU2.get().getRegistryName().getPath(), mcLoc("item/handheld"), "layer0", modLoc("item/psu"));
        singleTexture(PocketStorage.PSU3.get().getRegistryName().getPath(), mcLoc("item/handheld"), "layer0", modLoc("item/psu"));
        singleTexture(PocketStorage.PSU4.get().getRegistryName().getPath(), mcLoc("item/handheld"), "layer0", modLoc("item/psu"));
    }
}
