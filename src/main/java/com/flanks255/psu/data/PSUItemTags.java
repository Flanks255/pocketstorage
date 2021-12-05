package com.flanks255.psu.data;

import com.flanks255.psu.PocketStorage;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

public class PSUItemTags extends ItemTagsProvider {
    public PSUItemTags(DataGenerator dataGenerator, BlockTagsProvider blockTagProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator, blockTagProvider, PocketStorage.MODID, existingFileHelper);
    }
    //forge:holds_items

    public static final ITag.INamedTag<Item> HOLDS_ITEMS = ItemTags.bind(new ResourceLocation("forge", "holds_items").toString());

    @Override
    protected void addTags() {
        this.tag(HOLDS_ITEMS).add(PocketStorage.PSU1.get());
        this.tag(HOLDS_ITEMS).add(PocketStorage.PSU2.get());
        this.tag(HOLDS_ITEMS).add(PocketStorage.PSU3.get());
        this.tag(HOLDS_ITEMS).add(PocketStorage.PSU4.get());
    }
}
