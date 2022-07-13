package com.flanks255.psu.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;

public class Generator {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        generator.addProvider(true, new PSUItemModels(generator, event.getExistingFileHelper()));
        generator.addProvider(true, new PSURecipes(generator));
        generator.addProvider(true, new PSULang(generator));
    }
}
