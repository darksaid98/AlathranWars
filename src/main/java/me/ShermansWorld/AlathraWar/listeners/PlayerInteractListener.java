package me.ShermansWorld.AlathraWar.listeners;

import me.ShermansWorld.AlathraWar.Main;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class PlayerInteractListener implements Listener {

    //map of broken doors
    public Map<Door, Long> brokenDoors;

    /**
     *
     * @param event event
     * @author DunnoConz
     * @author AubriTheHuman
     */
    @EventHandler
    public void onPlayerKilled(final PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block clicked = event.getClickedBlock();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (action == Action.LEFT_CLICK_BLOCK && clicked.getType().toString().contains("DOOR")) { // right click + any door types
            Door door = (Door) clicked.getBlockData();
            // lock door in the opposite position
            if (item.getType() == Material.GUNPOWDER && item.getItemMeta().getDisplayName().contains("Door Ram")) {
                if (brokenDoors.get(door) != null && brokenDoors.get(door) > System.currentTimeMillis()) {
                    player.sendMessage("The door is already broken!");
                    event.setCancelled(true);
                    return;
                }

                player.sendMessage("Break it down alright!");
                brokenDoors.put(door, System.currentTimeMillis() + (1000L * Main.getInstance().getConfig().getInt("batteringRamEffectiveness")));
                return;
            }

            if (brokenDoors.get(door) != null && brokenDoors.get(door) > System.currentTimeMillis())  {
                player.sendMessage("Door is broken! " + String.valueOf(System.currentTimeMillis()) + " " + brokenDoors.get(door));
                event.setCancelled(true);
            }
        }
    }
}
