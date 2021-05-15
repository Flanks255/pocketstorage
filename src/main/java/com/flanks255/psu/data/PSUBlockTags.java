package com.flanks255.psu.data;

import com.flanks255.psu.PocketStorage;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class PSUBlockTags extends BlockTagsProvider {
    public PSUBlockTags(DataGenerator generatorIn, @Nullable ExistingFileHelper existingFileHelper) {
        super(generatorIn, PocketStorage.MODID, existingFileHelper);
    }
}
