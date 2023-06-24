package com.flanks255.psu.crafting;

import com.flanks255.psu.PocketStorage;
import com.google.gson.JsonObject;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class CopyDataRecipe extends ShapedRecipe {
    public CopyDataRecipe(final ResourceLocation id, final String group, CraftingBookCategory category, final int recipeWidth, final int recipeHeight, final NonNullList<Ingredient> ingredients, final ItemStack recipeOutput) {
        super(id, group, category, recipeWidth, recipeHeight, ingredients, recipeOutput);
    }

    public CopyDataRecipe(ShapedRecipe shapedRecipe) {
        super(shapedRecipe.getId(), shapedRecipe.getGroup(), shapedRecipe.category(), shapedRecipe.getRecipeWidth(), shapedRecipe.getRecipeHeight(), shapedRecipe.getIngredients(), shapedRecipe.getResultItem());
    }

    @Override
    public ItemStack assemble(@Nonnull CraftingContainer inv) {
        final ItemStack craftingResult = super.assemble(inv);
        TargetNBTIngredient donorIngredient = null;
        ItemStack dataSource = ItemStack.EMPTY;
        NonNullList<Ingredient> ingredients = getIngredients();
        for (Ingredient ingredient : ingredients) {
            if (ingredient instanceof TargetNBTIngredient) {
                donorIngredient = (TargetNBTIngredient) ingredient;
                break;
            }
        }
        if (!craftingResult.isEmpty()) {
            for (int i = 0; i < inv.getContainerSize(); i++) {
                final ItemStack item = inv.getItem(i);
                if (!item.isEmpty() && donorIngredient.test(item)) {
                    dataSource = item;
                    break;
                }
            }

            if (!dataSource.isEmpty() && dataSource.hasTag()) {
                craftingResult.setTag(dataSource.getTag().copy());
            }
        }

        return craftingResult;
    }


    public static class Serializer implements RecipeSerializer<CopyDataRecipe> {
        @Nullable
        @Override
        public CopyDataRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            return new CopyDataRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer));
        }

        @Override
        public CopyDataRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            try {
                return new CopyDataRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json));
            } catch (Exception exception) {
                PocketStorage.LOGGER.info("Error reading CopyData Recipe from packet: ", exception);
                throw exception;
            }
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CopyDataRecipe recipe) {
            try {
                RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
            } catch (Exception exception) {
                PocketStorage.LOGGER.info("Error writing CopyData Recipe to packet: ", exception);
                throw exception;
            }
        }
    }
}