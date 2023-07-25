package com.github.alathra.AlathranWars.items;

import com.github.alathra.AlathranWars.Main;
import com.github.alathra.AlathranWars.items.item.RamItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class WarItemRegistry {

    public static final String namespace = Main.getInstance().getName().toLowerCase();
    private static final HashMap<String, IWarItem> itemRegistry = new HashMap<>();
    private static WarItemRegistry instance;

    /**
     * loads all items into the plugin registry list
     */
    public WarItemRegistry() {
        instance = this;

//        Main.warLogger.log(UtilsChat.getPrefix() + "Registering AlathranWars items.");

        itemRegistry.put(registryName("ram"), new RamItem());
        //THIS BREAKS EVERYTHING???? TODO
//            Bukkit.getServer().addRecipe(new RamItem().getRecipe());

//        Main.warLogger.log(UtilsChat.getPrefix() + "Registered AlathranWars items.");
    }

    public static WarItemRegistry getInstance() {
        return instance;
    }

    /**
     * generate a registry name
     *
     * @param item
     * @return
     */
    public static @NotNull String registryName(String item) {
        return namespace + ":" + item;
    }

    public @NotNull Map<String, ItemStack> getItemRegistry() {
        return itemRegistry.entrySet()
            .stream()
            .filter(v -> v.getValue() != null)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                v -> v.getValue().getItemStack()));
    }

    /**
     * returns the associated itemstack with the item key, use for finding without namespace
     *
     * @param registryName
     * @return
     */
    public @Nullable ItemStack getOrNull(String registryName) {
        return itemRegistry.get(registryName(registryName)).getItemStack().clone();
    }


    /**
     * returns the associated itemstack with the item key, use for finding with namespace
     *
     * @param registryName
     * @return
     */
    public @Nullable ItemStack getOrNullNamespace(String registryName) {
        return itemRegistry.get(registryName).getItemStack().clone();
    }

    /**
     * returns the associated IWarItem with the item key, use for finding with namespace
     *
     * @param registryName
     * @return
     */
    public IWarItem getWarItem(String registryName) {
        return itemRegistry.get(registryName);
    }

}
