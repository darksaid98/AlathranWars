// 
// Decompiled by Procyon v0.5.36
// 

package me.ShermansWorld.AlathraWar;

import org.bukkit.event.EventHandler;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
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
            if (siege.beaconLocs.contains(event.getBlock().getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
