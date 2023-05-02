package me.ShermansWorld.AlathraWar.listeners;

import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.Raid;
import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.data.RaidData;
import me.ShermansWorld.AlathraWar.data.SiegeData;
import me.ShermansWorld.AlathraWar.items.WarItemRegistry;
import org.bukkit.Location;
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

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class PlayerInteractListener implements Listener {

    //map of broken doors
    public Map<Location, Long> brokenDoors = new HashMap<Location, Long>();

    /**
     * @param event event
     * @author DunnoConz
     * @author AubriTheHuman
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_BLOCK) {
            Block clicked = event.getClickedBlock();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (clicked != null) {
                if (clicked.getType().toString().contains("DOOR")) { // left click + any door types
                    Door door = (Door) clicked.getBlockData();
                    // lock door in the opposite position
                    if (item.equals(Main.itemRegistry.getOrNull("ram"))) {
                        boolean inSiegeOrRaid = false;
                        //siege check
                        for (Siege s : SiegeData.getSieges()) {
                            for (TownBlock townBlock : s.getTown().getTownBlocks()) {
                                if (WorldCoord.parseWorldCoord(clicked).equals(townBlock.getWorldCoord())) {
                                    // if we find one, just end no need to continue
                                    inSiegeOrRaid = true;
                                    break;
                                }
                            }
                        }

                        //if it wasnt in a siege, then
                        if (!inSiegeOrRaid) {
                            for (Raid r : RaidData.getRaids()) {
                                for (TownBlock townBlock : r.getRaidedTown().getTownBlocks()) {
                                    if (WorldCoord.parseWorldCoord(clicked).equals(townBlock.getWorldCoord())) {
                                        // if we find one, just end no need to continue
                                        inSiegeOrRaid = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (inSiegeOrRaid) {

                            // Returns if already broken.
                            if (doorBroken(clicked, door)) {
                                player.sendMessage(Helper.chatLabel() + Helper.color("&cThe door is already broken!"));
                                event.setCancelled(true);
                                return;
                            }
                            player.sendMessage(Helper.chatLabel() + Helper.color("&eBreak it down alright!"));
                            brokenDoors.put(getDoorPos(clicked, door), System.currentTimeMillis() + (1000L * Main.getInstance().getConfig().getInt("batteringRamEffectiveness")));
                            door.setOpen(!door.isOpen());
                            clicked.setBlockData(door);
                            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                            Main.warLogger.log("User " + player.getName() + " has broken down door" + clicked.getLocation().toString());
                        } else {
                            player.sendMessage(Helper.chatLabel() + Helper.color("&cThis item can only be used in a siege or raid!"));
                        }
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRClick(final PlayerItemDamageEvent event) {
        ItemStack item = event.getItem();
        if (item.equals(WarItemRegistry.getOrNull("ram"))) {
            event.setCancelled(true);
        }
    }


    /**
     * @param event event
     * @author DunnoConz
     * @author AubriTheHuman
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRClick(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block clicked = event.getClickedBlock();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (clicked != null) {
                if (clicked.getType().toString().contains("DOOR")) {
                    Door door = (Door) clicked.getBlockData();
                    if (doorBroken(clicked, door)) {
                        player.sendMessage(Helper.chatLabel() + Helper.color("Door is broken!"));
                        event.setCancelled(true);

                        return;
                    }
                }
            }

            if (item.equals(WarItemRegistry.getOrNull("ram"))) {
                if (clicked != null) {
                    if (clicked.getType().name().equals("GRASS_BLOCK")
                            || clicked.getType().name().equals("DIRT")
                            || clicked.getType().name().equals("COARSE_DIRT")
                            || clicked.getType().name().equals("ROOTED_DIRT")
                            || clicked.getType().name().equals("DIRT_PATH")) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    private Location getDoorPos(Block clicked, Door door) {
        if(door.getHalf() == Bisected.Half.BOTTOM) {
            return clicked.getLocation().clone();
        } else if(door.getHalf() == Bisected.Half.TOP) {
            return clicked.getLocation().subtract(0.0, 1.0, 0.0).clone();
        }
        return clicked.getLocation().clone();
    }

    private boolean doorBroken(@Nonnull Block clicked, Door door) {
        if(door.getHalf() == Bisected.Half.BOTTOM) {
            if(brokenDoors.get(clicked.getLocation()) != null) {
                return brokenDoors.get(clicked.getLocation()) > System.currentTimeMillis();
            }
        } else if(door.getHalf() == Bisected.Half.TOP) {
            if(brokenDoors.get(clicked.getLocation().subtract(0.0, 1.0D, 0.0)) != null) {
                return brokenDoors.get(clicked.getLocation().subtract(0.0, 1.0D, 0.0)) > System.currentTimeMillis();
            }
        }
        return false;
    }
}
