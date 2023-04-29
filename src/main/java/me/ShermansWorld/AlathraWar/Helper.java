package me.ShermansWorld.AlathraWar;

import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class Helper
{
    public static String color(final String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
    
    public static String chatLabel() {
        return color("&6[&4AlathraWar&6]&r ");
    }
    
    public static void testMsg() {
    	Bukkit.broadcastMessage("test message");
    }

    /**
     * Damages all gear in a players inventory
     *
     * @param p
     * @author ShermansWorld
     */
    public static void damageAllGear(Player p) {
        final Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getSize(); ++i) {
            final ItemStack is = inv.getItem(i);
            if (is != null && (is.getType() == Material.NETHERITE_AXE || is.getType() == Material.NETHERITE_SWORD || is.getType() == Material.DIAMOND_AXE || is.getType() == Material.DIAMOND_SWORD || is.getType() == Material.IRON_AXE || is.getType() == Material.IRON_SWORD)) {
                ItemMeta meta = is.getItemMeta();
                final Damageable damageable = (Damageable)meta;
                if (is.getType() == Material.NETHERITE_SWORD || is.getType() == Material.NETHERITE_AXE) {
                    damageable.setDamage(damageable.getDamage() + 507);
                    if (damageable.getDamage() > 2000) {
                        damageable.setDamage(2030);
                    }
                }
                else if (is.getType() == Material.DIAMOND_SWORD || is.getType() == Material.DIAMOND_AXE) {
                    damageable.setDamage(damageable.getDamage() + 390);
                    if (damageable.getDamage() > 1500) {
                        damageable.setDamage(1560);
                    }
                }
                else if (is.getType() == Material.IRON_SWORD || is.getType() == Material.IRON_AXE) {
                    damageable.setDamage(damageable.getDamage() + 62);
                    if (damageable.getDamage() > 200) {
                        damageable.setDamage(249);
                    }
                }
                meta = (ItemMeta)damageable;
                is.setItemMeta(meta);
            }
        }
        final ItemStack[] armor = p.getInventory().getArmorContents();
        for (int j = 0; j < armor.length; ++j) {
            if (armor[j] != null) {
                final ItemStack is2 = armor[j];
                if (is2.getType() == Material.NETHERITE_HELMET || is2.getType() == Material.NETHERITE_CHESTPLATE || is2.getType() == Material.NETHERITE_LEGGINGS || is2.getType() == Material.NETHERITE_BOOTS || is2.getType() == Material.DIAMOND_HELMET || is2.getType() == Material.DIAMOND_CHESTPLATE || is2.getType() == Material.DIAMOND_LEGGINGS || is2.getType() == Material.NETHERITE_BOOTS || is2.getType() == Material.IRON_HELMET || is2.getType() == Material.IRON_CHESTPLATE || is2.getType() == Material.IRON_LEGGINGS || is2.getType() == Material.IRON_BOOTS) {
                    ItemMeta meta2 = is2.getItemMeta();
                    final Damageable damageable2 = (Damageable)meta2;
                    if (is2.getType() == Material.NETHERITE_HELMET) {
                        damageable2.setDamage(damageable2.getDamage() + 101);
                        if (damageable2.getDamage() > 400) {
                            damageable2.setDamage(406);
                        }
                    }
                    if (is2.getType() == Material.NETHERITE_CHESTPLATE) {
                        damageable2.setDamage(damageable2.getDamage() + 148);
                        if (damageable2.getDamage() > 450) {
                            damageable2.setDamage(591);
                        }
                    }
                    if (is2.getType() == Material.NETHERITE_LEGGINGS) {
                        damageable2.setDamage(damageable2.getDamage() + 138);
                        if (damageable2.getDamage() > 420) {
                            damageable2.setDamage(554);
                        }
                    }
                    if (is2.getType() == Material.NETHERITE_BOOTS) {
                        damageable2.setDamage(damageable2.getDamage() + 120);
                        if (damageable2.getDamage() > 370) {
                            damageable2.setDamage(480);
                        }
                    }
                    if (is2.getType() == Material.DIAMOND_HELMET) {
                        damageable2.setDamage(damageable2.getDamage() + 90);
                        if (damageable2.getDamage() > 280) {
                            damageable2.setDamage(362);
                        }
                    }
                    if (is2.getType() == Material.DIAMOND_CHESTPLATE) {
                        damageable2.setDamage(damageable2.getDamage() + 132);
                        if (damageable2.getDamage() > 410) {
                            damageable2.setDamage(527);
                        }
                    }
                    if (is2.getType() == Material.DIAMOND_LEGGINGS) {
                        damageable2.setDamage(damageable2.getDamage() + 123);
                        if (damageable2.getDamage() > 380) {
                            damageable2.setDamage(494);
                        }
                    }
                    if (is2.getType() == Material.DIAMOND_BOOTS) {
                        damageable2.setDamage(damageable2.getDamage() + 107);
                        if (damageable2.getDamage() > 330) {
                            damageable2.setDamage(428);
                        }
                    }
                    if (is2.getType() == Material.IRON_HELMET) {
                        damageable2.setDamage(damageable2.getDamage() + 41);
                        if (damageable2.getDamage() > 125) {
                            damageable2.setDamage(164);
                        }
                    }
                    if (is2.getType() == Material.IRON_CHESTPLATE) {
                        damageable2.setDamage(damageable2.getDamage() + 60);
                        if (damageable2.getDamage() > 190) {
                            damageable2.setDamage(239);
                        }
                    }
                    if (is2.getType() == Material.IRON_LEGGINGS) {
                        damageable2.setDamage(damageable2.getDamage() + 56);
                        if (damageable2.getDamage() > 180) {
                            damageable2.setDamage(224);
                        }
                    }
                    if (is2.getType() == Material.IRON_BOOTS) {
                        damageable2.setDamage(damageable2.getDamage() + 48);
                        if (damageable2.getDamage() > 150) {
                            damageable2.setDamage(194);
                        }
                    }
                    meta2 = (ItemMeta)damageable2;
                    is2.setItemMeta(meta2);
                }
            }
        }
    }

    /**
     *
     * @param worldCoord worldCoord
     * @return List of connected chunks
     * @author NinjaMandalorian
     */
    private static ArrayList<WorldCoord> getAdjCells(WorldCoord worldCoord) {
        ArrayList<WorldCoord> ReturnList = new ArrayList<>();

        int[][] XZarray = new int[][]{
                {-1, 0},
                {1, 0},
                {0, -1},
                {0, 1}
        }; // Array that contains relative orthogonal shifts from origin
        for ( int[] pair: XZarray) {
            // Constructs new WorldCoord for comparison
            WorldCoord tCoord = new WorldCoord(worldCoord.getWorldName(), worldCoord.getX() + pair[0], worldCoord.getZ() + pair[1]);
            if (tCoord.getTownOrNull() != null && tCoord.getTownOrNull() == worldCoord.getTownOrNull()) {
                // If in town, and in same town, adds to return list
                ReturnList.add(tCoord);
            }
        }
        return ReturnList;
    }

    /** Gets all adjacently connected townblocks
     * @param chunkCoord - WorldCoord to check at
     * @return List of WorldCoords
     * @author NinjaMandalorian
     */
    public static ArrayList<WorldCoord> getCluster(WorldCoord chunkCoord){
        // WCoordList is the returning array, SearchList is the to-search list.
        ArrayList<WorldCoord> WCoordList = new ArrayList<>();
        ArrayList<WorldCoord> SearchList = new ArrayList<>();
        SearchList.add(chunkCoord); // Adds 1st chunk to list

        // Iterates through SearchList, to create a full list of every adjacent cell.
        while (SearchList.size() > 0) {
            // Gets WorldCoord
            WorldCoord toSearch = SearchList.get(0);
            // Gets adjacent cells
            ArrayList<WorldCoord> adjCells = getAdjCells(toSearch);
            for (WorldCoord cell : adjCells) {
                // If in final list, ignore.
                if (WCoordList.contains(cell)) {
                    continue;
                    // If in to-search list, ignore
                } else if (SearchList.contains(cell)) {
                    continue;
                    // Otherwise, add to search-list.
                } else {
                    SearchList.add(cell);
                }
            }
            // Removes from search list and adds to finished list. After checking all adjacent chunks.
            SearchList.remove(toSearch);
            WCoordList.add(toSearch);
        }

        return WCoordList; // Returns list
    }
}
