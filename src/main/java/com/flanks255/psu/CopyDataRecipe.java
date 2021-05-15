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
        super(shapedRecipe.getId(), shapedRecipe.getGroup(), shapedRecipe.getRecipeWidth(), shapedRecipe.getRecipeHeight(), shapedRecipe.getIngredients(), shapedRecipe.getRecipeOutput());
    }

    @Override
    public ItemStack getCraftingResult(CraftingInventory inv) {
        final ItemStack craftingResult = super.getCraftingResult(inv);
        ItemStack dataSource = ItemStack.EMPTY;

        if (!craftingResult.isEmpty()) {
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                final ItemStack item = inv.getStackInSlot(i);
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
        public CopyDataRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {
            return new CopyDataRecipe(IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, buffer));
        }

        @Override
        public CopyDataRecipe read(ResourceLocation recipeId, JsonObject json) {
            try {
                return new CopyDataRecipe(IRecipeSerializer.CRAFTING_SHAPED.read(recipeId, json));
            } catch (Exception exception) {
                PocketStorage.LOGGER.info("Error reading CopyData Recipe from packet: ", exception);
                throw exception;
            }
        }

        @Override
        public void write(PacketBuffer buffer, CopyDataRecipe recipe) {
            try {
                IRecipeSerializer.CRAFTING_SHAPED.write(buffer, recipe);
            } catch (Exception exception) {
                PocketStorage.LOGGER.info("Error writing CopyData Recipe to packet: ", exception);
                throw exception;
            }
        }
    }
}