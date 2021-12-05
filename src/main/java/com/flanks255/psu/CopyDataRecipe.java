package com.flanks255.psu;

import com.flanks255.psu.items.PocketStorageUnit;
import com.google.gson.JsonObject;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;


public class CopyDataRecipe extends ShapedRecipe {
    public CopyDataRecipe(final ResourceLocation id, final String group, final int recipeWidth, final int recipeHeight, final NonNullList<Ingredient> ingredients, final ItemStack recipeOutput) {
        super(id, group, recipeWidth, recipeHeight, ingredients, recipeOutput);
    }

    public CopyDataRecipe(ShapedRecipe shapedRecipe) {
        super(shapedRecipe.getId(), shapedRecipe.getGroup(), shapedRecipe.getRecipeWidth(), shapedRecipe.getRecipeHeight(), shapedRecipe.getIngredients(), shapedRecipe.getResultItem());
    }

    @Override
    public ItemStack assemble(CraftingInventory inv) {
        final ItemStack craftingResult = super.assemble(inv);
        ItemStack dataSource = ItemStack.EMPTY;

        if (!craftingResult.isEmpty()) {
            for (int i = 0; i < inv.getContainerSize(); i++) {
                final ItemStack item = inv.getItem(i);
                if (!item.isEmpty() && item.getItem() instanceof PocketStorageUnit) {
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


    public static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<CopyDataRecipe> {
        @Nullable
        @Override
        public CopyDataRecipe fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
            return new CopyDataRecipe(IRecipeSerializer.SHAPED_RECIPE.fromNetwork(recipeId, buffer));
        }

        @Override
        public CopyDataRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            try {
                return new CopyDataRecipe(IRecipeSerializer.SHAPED_RECIPE.fromJson(recipeId, json));
            } catch (Exception exception) {
                PocketStorage.LOGGER.info("Error reading CopyData Recipe from packet: ", exception);
                throw exception;
            }
        }

        @Override
        public void toNetwork(PacketBuffer buffer, CopyDataRecipe recipe) {
            try {
                IRecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
            } catch (Exception exception) {
                PocketStorage.LOGGER.info("Error writing CopyData Recipe to packet: ", exception);
                throw exception;
            }
        }
    }
}