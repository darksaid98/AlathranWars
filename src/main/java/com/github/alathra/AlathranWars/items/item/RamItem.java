package com.github.alathra.AlathranWars.items.item;

import com.github.alathra.AlathranWars.Main;
import com.github.alathra.AlathranWars.items.WarItemRegistry;
import com.github.alathra.AlathranWars.items.IWarItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class RamItem implements IWarItem {

    public ItemStack getItemStack() {

        ItemStack ram = new ItemStack(Material.WOODEN_HOE);
        ItemMeta meta = ram.getItemMeta();

        meta.setDisplayName("Door Ram");
        ArrayList<String> lore = new ArrayList<>();
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
