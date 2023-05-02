package me.ShermansWorld.AlathraWar.items.item;

import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.items.IWarItem;
import me.ShermansWorld.AlathraWar.items.WarItemRegistry;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Set;

public class RamItem implements IWarItem {

    public ItemStack getItemStack() {

        ItemStack ram = new ItemStack(Material.WOODEN_HOE);
        ItemMeta meta = ram.getItemMeta();

        meta.setDisplayName("Door Ram");
        ArrayList<String> lore = new ArrayList<String>();
        meta.setLore(lore);
        meta.setCustomModelData(14700);

        ram.setItemMeta(meta);

        return ram;
    }

    @Override
    public Recipe getRecipe() {
        ItemStack output = WarItemRegistry.getInstance().getOrNull("ram");
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "ram");
        ShapedRecipe recipe = new ShapedRecipe(key, output);
        recipe.shape("$@$", "B##");
        recipe.setIngredient('@', Material.STICK);
        recipe.setIngredient('$', Material.IRON_INGOT);
        recipe.setIngredient('B', Material.IRON_BLOCK);
        recipe.setIngredient('%', new RecipeChoice.MaterialChoice(Tag.LOGS));
        return recipe;
    }
}
