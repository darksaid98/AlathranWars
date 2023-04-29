package me.ShermansWorld.AlathraWar.listeners;

import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.Raid;
import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.data.RaidData;
import me.ShermansWorld.AlathraWar.data.SiegeData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PlayerInteractListener implements Listener {

    //map of broken doors
    public Map<Door, Long> brokenDoors = new HashMap<Door, Long>() {
    };

    /**
     *
     * @param event event
     * @author DunnoConz
     * @author AubriTheHuman
     */
    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block clicked = event.getClickedBlock();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (action == Action.LEFT_CLICK_BLOCK && clicked.getType().toString().contains("DOOR")) { // right click + any door types
            Door door = (Door) clicked.getBlockData();
                // lock door in the opposite position
                if (item.getType() == Material.GUNPOWDER && item.getItemMeta().getDisplayName().contains("Door Ram")) {

                    boolean inSiegeOrRaid = false;
                    //siege check
                    for(Siege s : SiegeData.getSieges()) {
                        for (TownBlock townBlock : s.getTown().getTownBlocks()) {
                            if(WorldCoord.parseWorldCoord(clicked).equals(townBlock.getWorldCoord())) {
                                // if we find one, just end no need to continue
                                inSiegeOrRaid = true;
                                break;
                            }
                        }
                    }
                    //if it wasnt in a siege, then
                    if(!inSiegeOrRaid) {
                        for(Raid r : RaidData.getRaids()) {
                            for (TownBlock townBlock : r.getRaidedTown().getTownBlocks()) {
                                if(WorldCoord.parseWorldCoord(clicked).equals(townBlock.getWorldCoord())) {
                                    // if we find one, just end no need to continue
                                    inSiegeOrRaid = true;
                                    break;
                                }
                            }
                        }
                    }

                    if(inSiegeOrRaid) {
                        if (brokenDoors.get(door) != null && brokenDoors.get(door) > System.currentTimeMillis()) {
                            player.sendMessage(Helper.chatLabel() + Helper.color("&cThe door is already broken!"));
                            event.setCancelled(true);
                            return;
                        }

                        player.sendMessage(Helper.chatLabel() + Helper.color("&eBreak it down alright!"));
                        brokenDoors.put(door, System.currentTimeMillis() + (1000L * Main.getInstance().getConfig().getInt("batteringRamEffectiveness")));
                        door.setOpen(!door.isOpen());
                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        return;

                    } else {
                        player.sendMessage(Helper.chatLabel() + Helper.color("&cThis item can only be used in a siege or raid!"));
                        event.setCancelled(true);
                        return;
                    }
                }


        }

        if (action == Action.RIGHT_CLICK_BLOCK && clicked.getType().toString().contains("DOOR")) {
            Door door = (Door) clicked.getBlockData();
            if (brokenDoors.get(door) != null && brokenDoors.get(door) > System.currentTimeMillis())  {
                player.sendMessage(Helper.chatLabel() + Helper.color("Door is broken! " + String.valueOf(System.currentTimeMillis()) + " " + brokenDoors.get(door)));
                event.setCancelled(true);
            }
        }
    }
}
