package com.flanks255.psu.data;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

public class Generator {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        generator.addProvider(new PSUItemModels(generator, event.getExistingFileHelper()));
        generator.addProvider(new PSURecipes(generator));
        generator.addProvider(new PSULang(generator));
    }
}
