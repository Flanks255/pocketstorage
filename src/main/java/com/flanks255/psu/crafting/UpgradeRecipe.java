package com.flanks255.psu.crafting;

import com.flanks255.psu.PocketStorage;
import com.flanks255.psu.items.PocketStorageUnit;
import com.mojang.serialization.Codec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class UpgradeRecipe extends ShapedRecipe {
    public UpgradeRecipe(final String group, CraftingBookCategory category, ShapedRecipePattern pattern, final ItemStack recipeOutput) {
        super(group, category, pattern, recipeOutput);
    }

    public UpgradeRecipe(ShapedRecipe shapedRecipe) {
        super(shapedRecipe.getGroup(), shapedRecipe.category(), shapedRecipe.pattern, shapedRecipe.getResultItem(RegistryAccess.EMPTY));    }


    @Override
    public ItemStack assemble(@Nonnull CraftingContainer inv, RegistryAccess thing) {
        final ItemStack craftingResult = super.assemble(inv, thing);
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

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PocketStorage.UPGRADE_RECIPE.get();
    }

    public static class Serializer implements RecipeSerializer<UpgradeRecipe> {
        private static final Codec<UpgradeRecipe> CODEC = ShapedRecipe.Serializer.CODEC.xmap(UpgradeRecipe::new, UpgradeRecipe::new);
        @Nullable
        @Override
        public UpgradeRecipe fromNetwork(FriendlyByteBuf buffer) {
            return new UpgradeRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(buffer));
        }

        @Override
        public @NotNull Codec<UpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull UpgradeRecipe recipe) {
            try {
                RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
            } catch (Exception exception) {
                PocketStorage.LOGGER.info("Error writing CopyData Recipe to packet: ", exception);
                throw exception;
            }
        }
    }
}