package com.flanks255.psu.data;

import com.flanks255.psu.PocketStorage;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.LanguageProvider;

public class PSULang extends LanguageProvider {
    public PSULang(DataGenerator gen) {
        super(gen.getPackOutput(), PocketStorage.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addUtil("invalid_uuid", "Unknown UUID");
        addUtil("migration", "Storage Unit Migrated.");
        addUtil("upgrade", "Storage Unit Upgraded.");
        addUtil("shift", "Press <%s> for info.");
        addUtil("key_shift", "Shift");
        addUtil("no_data_items", "Only simple items(no data) are allowed inside the Pocket Storage Unit.");
        addUtil("empty", "<  >");
        addUtil("count", "Count ");

        addUtil("deposit", "%s a chest to deposit contents.");
        addUtil("withdraw", "%s a chest to withdraw matching contents.");
        addUtil("sneak_right", "Sneak right-click");
        addUtil("sneak_left", "Sneak left-click");

        add(PocketStorage.PSU1.get(), "Pocket Storage Unit Tier: 1");
        addExtra(PocketStorage.PSU1.get(), "info", "Holds %s different items, up to %s of each.");
        addExtra(PocketStorage.PSU1.get(), "info2", "Automatically absorbs like items");
        addExtra(PocketStorage.PSU1.get(), "info3", "on pickup, voids excess.");

        add(PocketStorage.PSU2.get(), "Pocket Storage Unit Tier: 2");
        addExtra(PocketStorage.PSU2.get(), "info", "Holds %s different items, up to %s of each.");
        addExtra(PocketStorage.PSU2.get(), "info2", "Automatically absorbs like items");
        addExtra(PocketStorage.PSU2.get(), "info3", "on pickup, voids excess.");

        add(PocketStorage.PSU3.get(), "Pocket Storage Unit Tier: 3");
        addExtra(PocketStorage.PSU3.get(), "info", "Holds %s different items, up to %s of each.");
        addExtra(PocketStorage.PSU3.get(), "info2", "Automatically absorbs like items");
        addExtra(PocketStorage.PSU3.get(), "info3", "on pickup, voids excess.");

        add(PocketStorage.PSU4.get(), "Pocket Storage Unit Tier: 4");
        addExtra(PocketStorage.PSU4.get(), "info", "Holds %s different items, up to %s of each.");
        addExtra(PocketStorage.PSU4.get(), "info2", "Automatically absorbs like items");
        addExtra(PocketStorage.PSU4.get(), "info3", "on pickup, voids excess.");

    }

    private void addUtil(String key, String value) {
        add("pocketstorage.util." + key, value);
    }

    private void addExtra(Item item, String key, String value) {
        add(item.getDescriptionId() + "." + key, value);
    }
}
