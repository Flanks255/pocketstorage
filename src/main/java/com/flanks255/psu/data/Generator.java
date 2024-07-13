package com.flanks255.psu.data;

import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class Generator {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        generator.addProvider(true, new PSUItemModels(generator, event.getExistingFileHelper()));
        generator.addProvider(true, new PSURecipes(generator, event.getLookupProvider()));
        generator.addProvider(true, new PSULang(generator));
    }
}
