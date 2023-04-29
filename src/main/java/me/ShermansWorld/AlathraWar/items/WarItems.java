package me.ShermansWorld.AlathraWar.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class WarItems {


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
