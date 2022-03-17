package com.flanks255.psu.crafting;

import com.flanks255.psu.PocketStorage;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

import net.minecraft.world.item.crafting.Ingredient.ItemValue;
import net.minecraft.world.item.crafting.Ingredient.TagValue;
import net.minecraft.world.item.crafting.Ingredient.Value;

public class TargetNBTIngredient extends Ingredient {
    public TargetNBTIngredient(Stream<? extends Value> itemLists) {
        super(itemLists);
    }

    @Override
    @Nonnull
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    public static TargetNBTIngredient of(ItemLike itemProvider) {
        return new TargetNBTIngredient(Stream.of(new ItemValue(new ItemStack(itemProvider))));
    }
    public static TargetNBTIngredient of(ItemStack itemStack) {
        return new TargetNBTIngredient(Stream.of(new ItemValue(itemStack)));
    }
    @Nonnull
    public static TargetNBTIngredient of(@Nonnull TagKey<Item> tag) {
        return new TargetNBTIngredient(Stream.of(new TagValue(tag)));
    }



    @Override
    @Nonnull
    public JsonElement toJson() {
        JsonObject tmp = super.toJson().getAsJsonObject();
        tmp.addProperty("type", Serializer.NAME.toString());
        return tmp;
    }


    public static final Serializer SERIALIZER = new Serializer();
    public static class Serializer implements IIngredientSerializer<TargetNBTIngredient> {
        public static final ResourceLocation NAME = new ResourceLocation(PocketStorage.MODID, "nbt_target");

        @Override
        @Nonnull
        public TargetNBTIngredient parse(FriendlyByteBuf buffer) {
            return new TargetNBTIngredient(Stream.generate(() -> new ItemValue(buffer.readItem())).limit(buffer.readVarInt()));
        }

        @Override
        @Nonnull
        public TargetNBTIngredient parse(@Nonnull JsonObject json) {
            return new TargetNBTIngredient(Stream.of(Ingredient.valueFromJson(json)));
        }

        @Override
        public void write(FriendlyByteBuf buffer, TargetNBTIngredient ingredient) {
            ItemStack[] items = ingredient.getItems();
            buffer.writeVarInt(items.length);

            for (ItemStack stack : items)
                buffer.writeItem(stack);
        }
    }
}
