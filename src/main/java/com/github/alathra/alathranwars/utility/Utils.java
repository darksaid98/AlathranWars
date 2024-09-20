package com.github.alathra.alathranwars.utility;

import com.github.alathra.alathranwars.conflict.battle.siege.Siege;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class Utils {
    private static final ArrayList<Material> weaponList = new ArrayList<>(List.of(
        Material.NETHERITE_SWORD,
        Material.NETHERITE_AXE,
        Material.DIAMOND_SWORD,
        Material.DIAMOND_AXE,
        Material.IRON_SWORD,
        Material.IRON_AXE
    ));
    private static final ArrayList<Material> armorList = new ArrayList<>(List.of(
        Material.NETHERITE_HELMET,
        Material.NETHERITE_CHESTPLATE,
        Material.NETHERITE_LEGGINGS,
        Material.NETHERITE_BOOTS,
        Material.DIAMOND_HELMET,
        Material.DIAMOND_CHESTPLATE,
        Material.DIAMOND_LEGGINGS,
        Material.DIAMOND_BOOTS,
        Material.IRON_HELMET,
        Material.IRON_CHESTPLATE,
        Material.IRON_LEGGINGS,
        Material.IRON_BOOTS
    ));

    /**
     * Damages all gear in a players inventory
     *
     * @param p
     * @author ShermansWorld
     */
    public static void damageAllGear(@NotNull Player p) {
        for (@Nullable ItemStack itemStack : p.getInventory().getContents()) {
            if (itemStack == null)
                continue;

            if (weaponList.contains(itemStack.getType())) {
                setWeaponDurability(itemStack);
            }
        }

        final ItemStack @NotNull [] armor = p.getInventory().getArmorContents();

        for (@Nullable ItemStack itemStack : armor) {
            if (itemStack == null)
                continue;

            if (armorList.contains(itemStack.getType())) {
                setArmorDurability(itemStack);
            }
        }
    }

    private static void setArmorDurability(@Nullable ItemStack itemStack) {
        if (itemStack == null)
            return;

        ItemMeta meta = itemStack.getItemMeta();

        if (meta == null)
            return;

        if (!(meta instanceof Damageable damageable))
            return;

        switch (itemStack.getType()) {
            case NETHERITE_HELMET -> {
                damageable.setDamage(damageable.getDamage() + 101);
                if (damageable.getDamage() > 400) {
                    damageable.setDamage(406);
                }
            }
            case NETHERITE_CHESTPLATE -> {
                damageable.setDamage(damageable.getDamage() + 148);
                if (damageable.getDamage() > 450) {
                    damageable.setDamage(591);
                }
            }
            case NETHERITE_LEGGINGS -> {
                damageable.setDamage(damageable.getDamage() + 138);
                if (damageable.getDamage() > 420) {
                    damageable.setDamage(554);
                }
            }
            case NETHERITE_BOOTS -> {
                damageable.setDamage(damageable.getDamage() + 120);
                if (damageable.getDamage() > 370) {
                    damageable.setDamage(480);
                }
            }
            case DIAMOND_HELMET -> {
                damageable.setDamage(damageable.getDamage() + 90);
                if (damageable.getDamage() > 280) {
                    damageable.setDamage(362);
                }
            }
            case DIAMOND_CHESTPLATE -> {
                damageable.setDamage(damageable.getDamage() + 132);
                if (damageable.getDamage() > 410) {
                    damageable.setDamage(527);
                }
            }
            case DIAMOND_LEGGINGS -> {
                damageable.setDamage(damageable.getDamage() + 123);
                if (damageable.getDamage() > 380) {
                    damageable.setDamage(494);
                }
            }
            case DIAMOND_BOOTS -> {
                damageable.setDamage(damageable.getDamage() + 107);
                if (damageable.getDamage() > 330) {
                    damageable.setDamage(428);
                }
            }
            case IRON_HELMET -> {
                damageable.setDamage(damageable.getDamage() + 41);
                if (damageable.getDamage() > 125) {
                    damageable.setDamage(164);
                }
            }
            case IRON_CHESTPLATE -> {
                damageable.setDamage(damageable.getDamage() + 60);
                if (damageable.getDamage() > 190) {
                    damageable.setDamage(239);
                }
            }
            case IRON_LEGGINGS -> {
                damageable.setDamage(damageable.getDamage() + 56);
                if (damageable.getDamage() > 180) {
                    damageable.setDamage(224);
                }
            }
            case IRON_BOOTS -> {
                damageable.setDamage(damageable.getDamage() + 48);
                if (damageable.getDamage() > 150) {
                    damageable.setDamage(194);
                }
            }
        }

        itemStack.setItemMeta(damageable);
    }

    private static void setWeaponDurability(@Nullable ItemStack itemStack) {
        if (itemStack == null)
            return;

        ItemMeta meta = itemStack.getItemMeta();

        if (meta == null)
            return;

        if (!(meta instanceof Damageable damageable))
            return;

        switch (itemStack.getType()) {
            case NETHERITE_SWORD, NETHERITE_AXE -> {
                damageable.setDamage(damageable.getDamage() + 507);
                if (damageable.getDamage() > 2000) {
                    damageable.setDamage(2030);
                }
            }
            case DIAMOND_SWORD, DIAMOND_AXE -> {
                damageable.setDamage(damageable.getDamage() + 390);
                if (damageable.getDamage() > 1500) {
                    damageable.setDamage(1560);
                }
            }
            case IRON_SWORD, IRON_AXE, TRIDENT -> {
                damageable.setDamage(damageable.getDamage() + 62);
                if (damageable.getDamage() > 200) {
                    damageable.setDamage(249);
                }
            }
        }

        itemStack.setItemMeta(damageable);
    }

    // TODO Implement usage in siege commands to make siege optional argument
    public static @Nullable Siege getClosestSiege(Player p, boolean checkIfInSiege) {
        Set<Siege> sieges = checkIfInSiege
            ? WarController.getInstance().getSieges().stream().filter(siege -> siege.isPlayerParticipating(p)).collect(Collectors.toSet())
            : WarController.getInstance().getSieges();

        @Nullable Siege siegeResult = null;
        final Location playerLoc = p.getLocation();
        double closestSiege = 100000000D;

        for (Siege siege : sieges) {
            final @Nullable Location location = siege.getTown().getSpawnOrNull();

            if (location == null) continue;
            if (!location.getWorld().equals(playerLoc.getWorld())) continue;

            final double distance = location.distance(playerLoc);
            if (distance < closestSiege) {
                siegeResult = siege;
                closestSiege = distance;
            }
        }

        return siegeResult;
    }

    public static boolean isOnSiegeBattlefield(Player p, Siege siege) {
        if (siege == null) return false;

        final Location pLocation = p.getLocation();
        final Location siegeLocation = siege.getTownSpawn();

        if (siegeLocation == null) return false;
        if (!pLocation.getWorld().equals(siegeLocation.getWorld())) return false;

        return (pLocation.distance(siegeLocation) <= Siege.BATTLEFIELD_RANGE);
    }

    /**
     * @param worldCoord worldCoord
     * @return List of connected chunks
     * @author NinjaMandalorian
     */
    private static @NotNull ArrayList<WorldCoord> getAdjCells(@NotNull WorldCoord worldCoord) {
        @NotNull ArrayList<WorldCoord> worldCoords = new ArrayList<>();

        int[] @NotNull [] XZarray = new int[][]{
            {-1, 0},
            {1, 0},
            {0, -1},
            {0, 1}
        }; // Array that contains relative orthogonal shifts from origin

        for (int[] pair : XZarray) {
            // Constructs new WorldCoord for comparison
            @NotNull WorldCoord tCoord = new WorldCoord(worldCoord.getWorldName(), worldCoord.getX() + pair[0], worldCoord.getZ() + pair[1]);
            if (tCoord.getTownOrNull() != null && tCoord.getTownOrNull() == worldCoord.getTownOrNull()) {
                // If in town, and in same town, adds to return list
                worldCoords.add(tCoord);
            }
        }
        return worldCoords;
    }

    /**
     * Gets all adjacently connected townblocks
     *
     * @param chunkCoord - WorldCoord to check at
     * @return List of WorldCoords
     * @author NinjaMandalorian
     */
    public static @NotNull ArrayList<WorldCoord> getCluster(WorldCoord chunkCoord) {
        // worldCoords is the returning array, searchList is the to-search list.
        @NotNull ArrayList<WorldCoord> worldCoords = new ArrayList<>();
        @NotNull ArrayList<WorldCoord> searchList = new ArrayList<>(Collections.singletonList(chunkCoord)); // Adds 1st chunk to list

        // Iterates through searchList, to create a full list of every adjacent cell.
        while (!searchList.isEmpty()) {
            WorldCoord toSearch = searchList.get(0); // Gets WorldCoord
            @NotNull ArrayList<WorldCoord> adjCells = getAdjCells(toSearch); // Gets adjacent cells

            for (WorldCoord cell : adjCells) {
                if (worldCoords.contains(cell)) continue; // If in final list, ignore.
                if (searchList.contains(cell)) continue; // If in to-search list, ignore

                // Otherwise, add to search-list.
                searchList.add(cell);
            }

            // Removes from search list and adds to finished list. After checking all adjacent chunks.
            searchList.remove(toSearch);
            worldCoords.add(toSearch);
        }

        return worldCoords; // Returns list
    }
}
