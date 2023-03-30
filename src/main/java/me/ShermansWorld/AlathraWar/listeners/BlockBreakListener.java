package me.ShermansWorld.AlathraWar.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;

import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.commands.SiegeCommands;

import org.bukkit.event.Listener;

public class BlockBreakListener implements Listener
{
    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        final Block block = event.getBlock();
        if (SiegeCommands.sieges.isEmpty()) {
            return;
        }
        for (final Siege siege : SiegeCommands.sieges) {
            if (siege.beaconLocs.contains(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
