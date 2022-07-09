package com.flanks255.psu.crafting;

import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

public class WrappedRecipe implements FinishedRecipe {
    final FinishedRecipe inner;
    RecipeSerializer<?> serializerOverride;

    public WrappedRecipe(FinishedRecipe innerIn) {
        inner = innerIn;
    }

    public WrappedRecipe(FinishedRecipe innerIn, RecipeSerializer<?> serializerOverrideIn) {
        inner = innerIn;
        serializerOverride = serializerOverrideIn;
    }

    public static Consumer<FinishedRecipe> Inject(Consumer<FinishedRecipe> consumer, RecipeSerializer<?> serializer) {
        return iFinishedRecipe -> consumer.accept(new WrappedRecipe(iFinishedRecipe, serializer));
    }

    @Override
    public void serializeRecipeData(@Nonnull JsonObject json) {
        inner.serializeRecipeData(json);
    }

    @Override
    @Nonnull
    public JsonObject serializeRecipe() {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("type", ForgeRegistries.RECIPE_SERIALIZERS.getKey(Objects.requireNonNullElseGet(this.serializerOverride, this.inner::getType)).toString());
        serializeRecipeData(jsonObject);
        return jsonObject;
    }

    @Override
    @Nonnull
    public ResourceLocation getId() {
        return inner.getId();
    }

    @Override
    @Nonnull
    public RecipeSerializer<?> getType() {
        return serializerOverride != null? serializerOverride:inner.getType();
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return inner.serializeAdvancement();
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return inner.getAdvancementId();
    }
}
