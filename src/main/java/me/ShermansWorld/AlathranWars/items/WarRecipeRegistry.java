package me.ShermansWorld.AlathranWars.items;

import me.ShermansWorld.AlathranWars.Main;
import org.bukkit.inventory.Recipe;

import java.util.HashMap;
import java.util.Set;

public class WarRecipeRegistry {

    private static final HashMap<String, Recipe> recipeRegistry = new HashMap<>();
    private static WarRecipeRegistry instance;

    public WarRecipeRegistry() {
        instance = this;

//        Main.warLogger.log(UtilsChat.getPrefix() + "Registering AlathranWars recipes.");

//        register(WarItemRegistry.registryName("ram"), new RamItem().getRecipes());

//        Main.warLogger.log(UtilsChat.getPrefix() + "Registered AlathranWars recipes.");
    }

    public static WarRecipeRegistry getInstance() {
        return instance;
    }

    public void register(String name, Set<Recipe> recipes) {
        for (Recipe recipe : recipes) {
//            Bukkit.getServer().addRecipe(recipe);
            recipeRegistry.put(name, recipe);
        }
    }

    public HashMap<String, Recipe> getRecipeRegistry() {
        return recipeRegistry;
    }

    /**
     * returns the associated itemstack with the item key, use for finding without namespace
     *
     * @param registryName recipe name without namespace
     * @return recipe
     */
    public Recipe getOrNull(String registryName) {
        return recipeRegistry.get(WarItemRegistry.registryName(registryName));
    }


    /**
     * returns the associated itemstack with the item key, use for finding with namespace
     *
     * @param registryName registry name including namespace
     * @return recipe
     */
    public Recipe getOrNullNamespace(String registryName) {
        return recipeRegistry.get(registryName);
    }
}
