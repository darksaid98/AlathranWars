package me.ShermansWorld.AlathraWar.items;

import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Main;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WarItems {


    private static final HashMap<String, ItemStack> itemRegistry = new HashMap<>();

    public static final String namespace = Main.getInstance().getName().toLowerCase();

    /**
     * loads all items into the plugin registry list
     */
    public WarItems() {
        Main.warLogger.log(Helper.chatLabel() + "Registering AlathraWar items.");

        itemRegistry.put(registryName("ram"), getBatteringRam());

        Main.warLogger.log(Helper.chatLabel() + "Registered AlathraWar items.");
    }

    /**
     * generate a registry name
     * @param item
     * @return
     */
    private static String registryName(String item) {
        return namespace + ":" + item;
    }

    public static HashMap<String, ItemStack> getItemRegistry() {
        return itemRegistry;
    }

    /**
     * returns the associated itemstack with the item key, use for finding without namespace
     * @param registryName
     * @return
     */
    public static ItemStack getOrNull(String registryName) {
        return itemRegistry.get(registryName(registryName));
    }


    /**
     * returns the associated itemstack with the item key, use for finding with namespace
     * @param registryName
     * @return
     */
    public static ItemStack getOrNullNamespace(String registryName) {
        return itemRegistry.get(registryName);
    }


    public ItemStack getBatteringRam() {

        ItemStack ram = new ItemStack(Material.GUNPOWDER);
        ItemMeta meta = ram.getItemMeta();

        meta.setDisplayName("Door Ram");
        ArrayList<String> lore = new ArrayList<String>();
        meta.setLore(lore);

        ram.setItemMeta(meta);

        return ram;
    }

}
