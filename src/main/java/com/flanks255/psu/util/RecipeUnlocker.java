package com.flanks255.psu.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.ArrayList;
import java.util.List;

public class RecipeUnlocker {
    private static String modtag;
    private static int version;
    private static String MODID;

    public static void register(String modid, IEventBus bus, int recipeversion) {
        modtag = modid + "_unlocked";
        version = recipeversion;
        MODID = modid;
        bus.addListener(RecipeUnlocker::onPlayerLoggedIn);
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        CompoundTag tag = event.getPlayer().getPersistentData();
        if (tag.contains(modtag) && tag.getInt(modtag) >= version)
            return;

        Player player = event.getPlayer();
        if (player instanceof ServerPlayer) {
            MinecraftServer server = player.getServer();
            if (server != null) {
                List<Recipe<?>> recipes = new ArrayList<>(server.getRecipeManager().getRecipes());
                recipes.removeIf((recipe -> !recipe.getId().getNamespace().contains(MODID)));
                player.awardRecipes(recipes);
                tag.putInt(modtag, version);
            }
        }
    }
}
