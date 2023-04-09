package me.ShermansWorld.AlathraWar.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;

import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.commands.SiegeCommands;
import me.ShermansWorld.AlathraWar.data.SiegeData;

import org.bukkit.event.Listener;

public class BlockBreakListener implements Listener
{
    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        final Block block = event.getBlock();
        if (SiegeData.getSieges().isEmpty()) {
            return;
        }
        for (final Siege siege : SiegeData.getSieges()) {
            if (siege.beaconLocs.contains(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
