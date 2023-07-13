package me.ShermansWorld.AlathranWars.listeners;

import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.object.WorldCoord;
import me.ShermansWorld.AlathranWars.conflict.battle.siege.Siege;
import me.ShermansWorld.AlathranWars.holder.WarManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Set;

public class BlockBreakListener implements Listener {
    private final static Set<Material> allowedBlocks = Set.of(
        Material.TNT,
        // Mortar & Cannons
        Material.IRON_BLOCK,
        Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
        Material.TORCH,
        Material.STONE_BUTTON,
        Material.BLACK_WOOL
    );

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {
        final Block block = e.getBlock();
        /*if (SiegeData.getSieges().isEmpty() *//*&& RaidData.getRaids().isEmpty()*//*) {
            return;
        }*/

        if (allowedBlocks.contains(block.getType())) {
            return;
        }

        if (e.getPlayer().hasPermission("AlathranWars.break")) {
            return;
        }

        for (final Siege siege : WarManager.getInstance().getSieges()) {
            if (siege.beaconLocations.contains(block.getLocation())) {
                e.setCancelled(true);
                return;
            }
        }

        if (WorldCoord.parseWorldCoord(block).getTownOrNull() != null) {
            for (final Siege siege : WarManager.getInstance().getSieges()) {
                if (siege.getTown() == WorldCoord.parseWorldCoord(block).getTownOrNull()) {
                    e.getPlayer().sendMessage(new ColorParser("<red>You can not break blocks during sieges").parseLegacy().build());
                    e.setCancelled(true);
                    return;
                }
            }

            /*for (final OldRaid oldRaid : RaidData.getRaids()) {
                if (oldRaid.getRaidedTown() == WorldCoord.parseWorldCoord(block).getTownOrNull()) {
                    p.sendMessage(Helper.color("<red>You can not break blocks during raids"));
                    event.setCancelled(true);
                    return;
                }
            }*/
        }
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Block block = event.getBlock();
        /*if (SiegeData.getSieges().isEmpty() && RaidData.getRaids().isEmpty()) {
            return;
        }*/
        if (allowedBlocks.contains(block.getType())) {
            return;
        }

        Player p = event.getPlayer();
        if (p.hasPermission("AlathranWars.place")) {
            return;
        }

        if (WorldCoord.parseWorldCoord(block).getTownOrNull() != null) {
            for (final Siege siege : WarManager.getInstance().getSieges()) {
                if (siege.getTown() == WorldCoord.parseWorldCoord(block).getTownOrNull()) {
                    p.sendMessage(new ColorParser("<red>You can not place blocks during sieges").parseLegacy().build());
                    event.setCancelled(true);
                    return;
                }
            }

            /*for (final OldRaid oldRaid : RaidData.getRaids()) {
                if (oldRaid.getRaidedTown() == WorldCoord.parseWorldCoord(block).getTownOrNull()) {
                    p.sendMessage(Helper.color("<red>You can not place blocks during raids"));
                    event.setCancelled(true);
                    return;
                }
            }*/
        }
    }
}
