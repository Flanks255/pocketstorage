package com.flanks255.pocketstorage.crafting;

import com.flanks255.pocketstorage.PocketStorage;
import com.flanks255.pocketstorage.items.PocketStorageUnit;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import javax.annotation.Nonnull;


public class UpgradeRecipe extends ShapedRecipe {
    public UpgradeRecipe(final String group, CraftingBookCategory category, ShapedRecipePattern pattern, final ItemStack recipeOutput) {
        super(group, category, pattern, recipeOutput);
    }

    public UpgradeRecipe(ShapedRecipe shapedRecipe) {
        super(shapedRecipe.getGroup(), shapedRecipe.category(), shapedRecipe.pattern, shapedRecipe.getResultItem(RegistryAccess.EMPTY));    }


    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull CraftingInput input, @Nonnull HolderLookup.Provider provider) {
        final ItemStack craftingResult = super.assemble(input, provider);
        ItemStack dataSource = ItemStack.EMPTY;

        if (!craftingResult.isEmpty()) {
            for (int i = 0; i < input.size(); i++) {
                final ItemStack item = input.getItem(i);
                if (!item.isEmpty() && item.getItem() instanceof PocketStorageUnit) {
                    dataSource = item;
                    break;
                }
            }

            if (!dataSource.isEmpty()) {
                if (dataSource.has(PocketStorage.PSU_UUID.get())) {
                    craftingResult.set(PocketStorage.PSU_UUID.get(), dataSource.get(PocketStorage.PSU_UUID.get()));
                }
                else if (dataSource.has(DataComponents.CUSTOM_DATA)){ //Legacy support
                    if (dataSource.get(DataComponents.CUSTOM_DATA).contains("UUID")){
                        craftingResult.set(PocketStorage.PSU_UUID.get(), dataSource.get(DataComponents.CUSTOM_DATA).copyTag().getUUID("UUID"));
                    }
                }
            }
        }

        return craftingResult;
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return PocketStorage.UPGRADE_RECIPE.get();
    }

    public static class Serializer implements RecipeSerializer<UpgradeRecipe> {
        private static final MapCodec<UpgradeRecipe> CODEC = ShapedRecipe.Serializer.CODEC.xmap(UpgradeRecipe::new, UpgradeRecipe::new);
        private static final StreamCodec<RegistryFriendlyByteBuf, UpgradeRecipe> STREAM_CODEC = RecipeSerializer.SHAPED_RECIPE.streamCodec().map(UpgradeRecipe::new, UpgradeRecipe::new);

        @Override
        @Nonnull
        public MapCodec<UpgradeRecipe> codec() {
            return CODEC;
        }

        @Override
        @Nonnull
        public StreamCodec<RegistryFriendlyByteBuf, UpgradeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}