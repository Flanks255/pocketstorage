package com.flanks255.psu.data;

import com.flanks255.psu.PocketStorage;
import com.flanks255.psu.crafting.TargetNBTIngredient;
import com.flanks255.psu.crafting.WrappedRecipe;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.*;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.Tags;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;

import javax.annotation.Nonnull;

public class PSURecipes extends RecipeProvider {
    public PSURecipes(DataGenerator pGenerator) {
        super(pGenerator.getPackOutput());
    }

    @Override
    protected CompletableFuture<?> saveAdvancement(@Nonnull CachedOutput output, @Nonnull FinishedRecipe finishedRecipe, @Nonnull JsonObject advancementJson) {
        // No thank you, good day sir.
        return null;
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        InventoryChangeTrigger.TriggerInstance lul = has(Items.AIR);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PocketStorage.PSU1.get())
            .pattern("ABA")
            .pattern("CDC")
            .pattern("ABA")
            .define('A', Tags.Items.INGOTS_IRON)
            .define('B', Items.PISTON)
            .define('C', Tags.Items.DUSTS_REDSTONE)
            .define('D', Tags.Items.CHESTS)
            .unlockedBy("", lul)
            .showNotification(false)
            .save(consumer, new ResourceLocation(PocketStorage.MODID, "tier1"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PocketStorage.PSU2.get())
            .pattern("ABA")
            .pattern("CDC")
            .pattern("ABA")
            .define('A', Tags.Items.INGOTS_GOLD)
            .define('B', Items.PISTON)
            .define('C', Tags.Items.CHESTS)
            .define('D', TargetNBTIngredient.of(PocketStorage.PSU1.get()))
            .unlockedBy("", lul)
            .showNotification(false)
            .save(WrappedRecipe.Inject(consumer, PocketStorage.UPGRADE_RECIPE.get()), new ResourceLocation(PocketStorage.MODID, "tier2"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PocketStorage.PSU3.get())
            .pattern("ABA")
            .pattern("CDC")
            .pattern("ABA")
            .define('A', Tags.Items.GEMS_DIAMOND)
            .define('B', Items.PISTON)
            .define('C', Tags.Items.CHESTS)
            .define('D', TargetNBTIngredient.of(PocketStorage.PSU2.get()))
            .unlockedBy("", lul)
            .showNotification(false)
            .save(WrappedRecipe.Inject(consumer, PocketStorage.UPGRADE_RECIPE.get()), new ResourceLocation(PocketStorage.MODID, "tier3"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PocketStorage.PSU4.get())
            .pattern("ABA")
            .pattern("CDC")
            .pattern("ABA")
            .define('A', Items.PISTON)
            .define('B', Tags.Items.CHESTS)
            .define('C', Tags.Items.NETHER_STARS)
            .define('D', TargetNBTIngredient.of(PocketStorage.PSU3.get()))
            .unlockedBy("", lul)
            .showNotification(false)
            .save(WrappedRecipe.Inject(consumer, PocketStorage.UPGRADE_RECIPE.get()), new ResourceLocation(PocketStorage.MODID, "tier4"));
    }
}
