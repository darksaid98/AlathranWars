package com.github.alathra.AlathranWars.listeners.items;

import com.github.alathra.AlathranWars.AlathranWars;
import com.github.alathra.AlathranWars.conflict.WarController;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.items.WarItemRegistry;
import com.github.alathra.AlathranWars.utility.UtilsChat;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerInteractListener implements Listener {

    //predefined foor materials
    private static final Set<Material> doors = new HashSet<>();

    static {
        doors.add(Material.ACACIA_DOOR);
        doors.add(Material.BIRCH_DOOR);
        doors.add(Material.CRIMSON_DOOR);
        doors.add(Material.DARK_OAK_DOOR);
        doors.add(Material.IRON_DOOR);
        doors.add(Material.JUNGLE_DOOR);
        doors.add(Material.MANGROVE_DOOR);
        doors.add(Material.OAK_DOOR);
        doors.add(Material.SPRUCE_DOOR);
        doors.add(Material.WARPED_DOOR);
    }

    //map of broken doors
    public final @NotNull Map<Location, Long> brokenDoors = new HashMap<>();

    /**
     * @param event event
     * @author DunnoConz
     * @author AubriTheHuman
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(final @NotNull PlayerInteractEvent event) {
        @NotNull Player player = event.getPlayer();
        @NotNull Action action = event.getAction();

        if (action == Action.LEFT_CLICK_BLOCK) {
            @Nullable Block clicked = event.getClickedBlock();
            @NotNull ItemStack item = player.getInventory().getItemInMainHand();
            if (clicked != null) {
                if (doors.contains(clicked.getType())) { // left click + any door types
                    @NotNull Door door = (Door) clicked.getBlockData();
                    // lock door in the opposite position
                    if (item.equals(WarItemRegistry.getInstance().getOrNull("ram"))) {
                        boolean inSiegeOrRaid = false;
                        //siege check
                        for (@NotNull Siege s : WarController.getInstance().getSieges()) {
                            for (@NotNull TownBlock townBlock : s.getTown().getTownBlocks()) {
                                if (WorldCoord.parseWorldCoord(clicked).equals(townBlock.getWorldCoord())) {
                                    // if we find one, just end no need to continue
                                    inSiegeOrRaid = true;
                                    break;
                                }
                            }
                        }


                        //if it wasnt in a siege, then
                        /*if (!inSiegeOrRaid) {
                            for (OldRaid r : RaidData.getRaids()) {
                                for (TownBlock townBlock : r.getRaidedTown().getTownBlocks()) {
                                    if (WorldCoord.parseWorldCoord(clicked).equals(townBlock.getWorldCoord())) {
                                        // if we find one, just end no need to continue
                                        inSiegeOrRaid = true;
                                        break;
                                    }
                                }
                            }
                        }*/

                        if (inSiegeOrRaid) {

                            // Returns if already broken.
                            if (doorBroken(clicked, door)) {
                                player.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "<red>The door is already broken!").parseLegacy().build());
                                event.setCancelled(true);
                                return;
                            }
                            player.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "<yellow>Break it down alright!").parseLegacy().build());
                            brokenDoors.put(getDoorPos(clicked, door), System.currentTimeMillis() + (1000L * AlathranWars.getInstance().getConfig().getInt("batteringRamEffectiveness")));
                            door.setOpen(!door.isOpen());
                            clicked.setBlockData(door);
                            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        } else {
                            player.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "<red>This item can only be used in a siege or raid!").parseLegacy().build());
                        }
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRClick(final @NotNull PlayerItemDamageEvent event) {
        @NotNull ItemStack item = event.getItem();
        if (item.equals(WarItemRegistry.getInstance().getOrNull("ram"))) {
            event.setCancelled(true);
        }
    }


    /**
     * @param event event
     * @author DunnoConz
     * @author AubriTheHuman
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRClick(final @NotNull PlayerInteractEvent event) {
        @NotNull Player player = event.getPlayer();
        @NotNull Action action = event.getAction();
        @Nullable Block clicked = event.getClickedBlock();
        @NotNull ItemStack item = player.getInventory().getItemInMainHand();
        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (clicked != null) {
                if (doors.contains(clicked.getType())) {
                    @NotNull Door door = (Door) clicked.getBlockData();
                    if (doorBroken(clicked, door)) {
                        player.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "Door is broken!").parseLegacy().build());
                        event.setCancelled(true);

                        return;
                    }
                }
            }

            if (item.equals(WarItemRegistry.getInstance().getOrNull("ram"))) {
                if (clicked != null) {
                    if (clicked.getType().name().equals("GRASS_BLOCK")
                        || clicked.getType().name().equals("DIRT")
                        || clicked.getType().name().equals("COARSE_DIRT")
                        || clicked.getType().name().equals("ROOTED_DIRT")
                        || clicked.getType().name().equals("DIRT_PATH")) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    private @NotNull Location getDoorPos(@NotNull Block clicked, @NotNull Door door) {
        if (door.getHalf() == Bisected.Half.BOTTOM) {
            return clicked.getLocation().clone();
        } else if (door.getHalf() == Bisected.Half.TOP) {
            return clicked.getLocation().subtract(0.0, 1.0, 0.0).clone();
        }
        return clicked.getLocation().clone();
    }

    private boolean doorBroken(@Nonnull Block clicked, @NotNull Door door) {
        if (door.getHalf() == Bisected.Half.BOTTOM) {
            if (brokenDoors.get(clicked.getLocation()) != null) {
                return brokenDoors.get(clicked.getLocation()) > System.currentTimeMillis();
            }
        } else if (door.getHalf() == Bisected.Half.TOP) {
            if (brokenDoors.get(clicked.getLocation().subtract(0.0, 1.0D, 0.0)) != null) {
                return brokenDoors.get(clicked.getLocation().subtract(0.0, 1.0D, 0.0)) > System.currentTimeMillis();
            }
        }
        return false;
    }
}
