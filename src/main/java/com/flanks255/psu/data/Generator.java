package com.flanks255.psu.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

public class Generator {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        PSUBlockTags blockTags = new PSUBlockTags(generator, event.getExistingFileHelper());
        generator.addProvider(new PSUItemTags(generator, blockTags, event.getExistingFileHelper()));
        generator.addProvider(new PSUItemModels(generator, event.getExistingFileHelper()));
    }
}
