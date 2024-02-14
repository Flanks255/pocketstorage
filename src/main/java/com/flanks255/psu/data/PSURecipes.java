package com.flanks255.psu.data;

import com.flanks255.psu.PocketStorage;
import com.flanks255.psu.crafting.UpgradeRecipe;
import com.flanks255.psu.util.NoAdvRecipeOutput;
import com.flanks255.psu.util.RecipeInjector;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

public class PSURecipes extends RecipeProvider {
    public PSURecipes(DataGenerator pGenerator) {
        super(pGenerator.getPackOutput());
    }
    @Override
    protected void buildRecipes(RecipeOutput theirOutput) {
        RecipeOutput output = new NoAdvRecipeOutput(theirOutput);

        Criterion<InventoryChangeTrigger.TriggerInstance> lul = has(Items.AIR);

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
            .save(output, new ResourceLocation(PocketStorage.MODID, "tier1"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PocketStorage.PSU2.get())
            .pattern("ABA")
            .pattern("CDC")
            .pattern("ABA")
            .define('A', Tags.Items.INGOTS_GOLD)
            .define('B', Items.PISTON)
            .define('C', Tags.Items.CHESTS)
            .define('D', PocketStorage.PSU1.get())
            .unlockedBy("", lul)
            .showNotification(false)
            .save(StorageUpgrade(output), new ResourceLocation(PocketStorage.MODID, "tier2"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PocketStorage.PSU3.get())
            .pattern("ABA")
            .pattern("CDC")
            .pattern("ABA")
            .define('A', Tags.Items.GEMS_DIAMOND)
            .define('B', Items.PISTON)
            .define('C', Tags.Items.CHESTS)
            .define('D', PocketStorage.PSU2.get())
            .unlockedBy("", lul)
            .showNotification(false)
            .save(StorageUpgrade(output), new ResourceLocation(PocketStorage.MODID, "tier3"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PocketStorage.PSU4.get())
            .pattern("ABA")
            .pattern("CDC")
            .pattern("ABA")
            .define('A', Items.PISTON)
            .define('B', Tags.Items.CHESTS)
            .define('C', Tags.Items.NETHER_STARS)
            .define('D', PocketStorage.PSU3.get())
            .unlockedBy("", lul)
            .showNotification(false)
            .save(StorageUpgrade(output), new ResourceLocation(PocketStorage.MODID, "tier4"));
    }

    @NotNull
    private static RecipeInjector<ShapedRecipe> StorageUpgrade(RecipeOutput output) {
        return new RecipeInjector<>(output, UpgradeRecipe::new);
    }
}
