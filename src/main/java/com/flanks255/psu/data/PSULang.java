package com.flanks255.psu.data;

import com.flanks255.psu.PocketStorage;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class PSULang extends LanguageProvider {
    public PSULang(DataGenerator gen, String locale) {
        super(gen, PocketStorage.MODID, locale);
    }

    @Override
    protected void addTranslations() {

    }
}
