package me.ShermansWorld.AlathraWar.items;

import me.ShermansWorld.AlathraWar.AlathraWarLogger;
import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Main;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;

public class WarRecipes {

    private static final HashMap<String, Recipe> recipeRegistry = new HashMap<>();

    public WarRecipes() {
        Main.warLogger.log(Helper.chatLabel() + "Registering AlathraWar recipes.");

        recipeRegistry.put(WarItems.registryName("ram"), ramRecipe());

        Main.warLogger.log(Helper.chatLabel() + "Registered AlathraWar recipes.");
    }


    public static HashMap<String, Recipe> getRecipeRegistry() {
        return recipeRegistry;
    }

    /**
     * returns the associated itemstack with the item key, use for finding without namespace
     * @param registryName recipe name without namespace
     * @return recipe
     */
    public static Recipe getOrNull(String registryName) {
        return recipeRegistry.get(WarItems.registryName(registryName));
    }


    /**
     * returns the associated itemstack with the item key, use for finding with namespace
     * @param registryName registry name including namespace
     * @return recipe
     */
    public static Recipe getOrNullNamespace(String registryName) {
        return recipeRegistry.get(registryName);
    }

    public Recipe ramRecipe() {
        ItemStack output = WarItems.getOrNull("ram");
        NamespacedKey key = new NamespacedKey((Plugin) Main.getInstance(), "ram");
        ShapedRecipe recipe = new ShapedRecipe(key, output);
        recipe.shape(new String[] { "$@$", "B##"});
        recipe.setIngredient('@', Material.STICK);
        recipe.setIngredient('$', Material.IRON_INGOT);
        recipe.setIngredient('B', Material.IRON_BLOCK);
        recipe.setIngredient('%', Material.OAK_LOG);
        return recipe;
    }
}
