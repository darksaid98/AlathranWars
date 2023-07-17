package com.github.alathra.AlathranWars.listeners.siege;

import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.holder.WarManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class BlockBreakPlaceListener implements Listener {
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
    private void onBlockBreak(BlockBreakEvent e) {
        final Block block = e.getBlock();

        if (allowedBlocks.contains(block.getType()))
            return;

        if (e.getPlayer().hasPermission("AlathranWars.break"))
            return;

        final @Nullable Town town = WorldCoord.parseWorldCoord(block).getTownOrNull();

        if (town == null)
            return;

        for (final Siege siege : WarManager.getInstance().getSieges()) {
            if (siege.getTown().equals(town)) {
                e.getPlayer().sendMessage(new ColorParser("<red>You can not break blocks during sieges.").build());
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        final Block block = e.getBlock();

        if (allowedBlocks.contains(block.getType()))
            return;

        if (e.getPlayer().hasPermission("AlathranWars.place"))
            return;

        final @Nullable Town town = WorldCoord.parseWorldCoord(block).getTownOrNull();

        if (town == null)
            return;

        for (final Siege siege : WarManager.getInstance().getSieges()) {
            if (siege.getTown().equals(town)) {
                e.getPlayer().sendMessage(new ColorParser("<red>You can not place blocks during sieges.").build());
                e.setCancelled(true);
                return;
            }
        }
    }
}
