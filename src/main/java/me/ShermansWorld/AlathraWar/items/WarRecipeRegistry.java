package me.ShermansWorld.AlathraWar.items;

import me.ShermansWorld.AlathraWar.AlathraWarLogger;
import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.War;
import me.ShermansWorld.AlathraWar.items.item.RamItem;
import org.bukkit.inventory.Recipe;

import java.util.HashMap;
import java.util.Set;

public class WarRecipeRegistry {

    private static WarRecipeRegistry instance;

    private static final HashMap<String, Recipe> recipeRegistry = new HashMap<>();

    public WarRecipeRegistry() {
        instance = this;

        Main.warLogger.log(Helper.chatLabel() + "Registering AlathraWar recipes.");

//        register(WarItemRegistry.registryName("ram"), new RamItem().getRecipes());

        Main.warLogger.log(Helper.chatLabel() + "Registered AlathraWar recipes.");
    }

    public void register(String name, Set<Recipe> recipes) {
        for (Recipe recipe : recipes) {
//            Bukkit.getServer().addRecipe(recipe);
            recipeRegistry.put(name, recipe);
        }
    }

    public static WarRecipeRegistry getInstance() {
        return instance;
    }

    public HashMap<String, Recipe> getRecipeRegistry() {
        return recipeRegistry;
    }

    /**
     * returns the associated itemstack with the item key, use for finding without namespace
     * @param registryName recipe name without namespace
     * @return recipe
     */
    public Recipe getOrNull(String registryName) {
        return recipeRegistry.get(WarItemRegistry.registryName(registryName));
    }


    /**
     * returns the associated itemstack with the item key, use for finding with namespace
     * @param registryName registry name including namespace
     * @return recipe
     */
    public Recipe getOrNullNamespace(String registryName) {
        return recipeRegistry.get(registryName);
    }
}
