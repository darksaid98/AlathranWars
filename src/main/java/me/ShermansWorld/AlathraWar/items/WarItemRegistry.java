package me.ShermansWorld.AlathraWar.items;

import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.items.item.RamItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WarItemRegistry {

    private static WarItemRegistry instance;

    private static final HashMap<String, IWarItem> itemRegistry = new HashMap<>();

    public static final String namespace = Main.getInstance().getName().toLowerCase();

    /**
     * loads all items into the plugin registry list
     */
    public WarItemRegistry() {
        instance = this;

        Main.warLogger.log(Helper.chatLabel() + "Registering AlathraWar items.");

        itemRegistry.put(registryName("ram"), new RamItem());
        //THIS BREAKS EVERYTHING???? TODO
//            Bukkit.getServer().addRecipe(new RamItem().getRecipe());

        Main.warLogger.log(Helper.chatLabel() + "Registered AlathraWar items.");
    }

    public static WarItemRegistry getInstance() {
        return instance;
    }

    /**
     * generate a registry name
     * @param item
     * @return
     */
    public static String registryName(String item) {
        return namespace + ":" + item;
    }

    public Map<String, ItemStack> getItemRegistry() {
        return itemRegistry.entrySet()
                .stream()
                .filter(v -> v.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        v -> v.getValue().getItemStack()));
    }

    /**
     * returns the associated itemstack with the item key, use for finding without namespace
     * @param registryName
     * @return
     */
    public ItemStack getOrNull(String registryName) {
        return itemRegistry.get(registryName(registryName)).getItemStack().clone();
    }


    /**
     * returns the associated itemstack with the item key, use for finding with namespace
     * @param registryName
     * @return
     */
    public ItemStack getOrNullNamespace(String registryName) {
        return itemRegistry.get(registryName).getItemStack().clone();
    }

    /**
     * returns the associated IWarItem with the item key, use for finding with namespace
     * @param registryName
     * @return
     */
    public IWarItem getWarItem(String registryName) {
        return itemRegistry.get(registryName);
    }

}
