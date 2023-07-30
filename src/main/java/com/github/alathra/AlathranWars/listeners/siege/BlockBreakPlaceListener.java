package com.github.alathra.AlathranWars.listeners.siege;

import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.enums.BattleSide;
import com.github.alathra.AlathranWars.utility.Utils;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.event.actions.TownyBuildEvent;
import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

public class BlockBreakPlaceListener implements Listener {
    private final static Set<Material> allowedBlocks = Set.of(
        Material.TNT,
        // Mortar & Cannons
        Material.IRON_BLOCK,
        Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
        Material.TORCH,
        Material.STONE_BUTTON,
        Material.BLACK_WOOL,
        // Beds
        Material.WHITE_BED,
        Material.YELLOW_BED,
        Material.BLACK_BED,
        Material.BLUE_BED,
        Material.BROWN_BED,
        Material.CYAN_BED,
        Material.GRAY_BED,
        Material.GREEN_BED,
        Material.LIGHT_BLUE_BED,
        Material.LIGHT_GRAY_BED,
        Material.LIME_BED,
        Material.MAGENTA_BED,
        Material.ORANGE_BED,
        Material.PINK_BED,
        Material.PURPLE_BED,
        Material.RED_BED
    );

    @EventHandler
    public void onBlockBreak(TownyDestroyEvent e) {
        Siege siege = Utils.getClosestSiege(e.getPlayer(), true);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(e.getPlayer(), siege)) return;

        final boolean isInsideTownClaims = e.hasTownBlock();
        final boolean isDefender = siege.getPlayerSideInSiege(e.getPlayer()).equals(BattleSide.DEFENDER);

        if (
            (isDefender || !isInsideTownClaims) && allowedBlocks.contains(e.getMaterial())
        ) {
            e.setCancelled(false);
            return;
        }

        if (e.getPlayer().hasPermission("AlathranWars.break")) {
            e.setCancelled(false);
            return;
        }

        e.setCancelled(true);
        e.setCancelMessage("");
        e.getPlayer().sendMessage(ColorParser.of("<red>You can not break blocks during sieges.").build());
    }

    @EventHandler
    public void onBlockPlace(TownyBuildEvent e) {
        Siege siege = Utils.getClosestSiege(e.getPlayer(), true);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(e.getPlayer(), siege)) return;

        final boolean isInsideTownClaims = e.hasTownBlock();
        final boolean isDefender = siege.getPlayerSideInSiege(e.getPlayer()).equals(BattleSide.DEFENDER);

        if (
            (isDefender || !isInsideTownClaims) && allowedBlocks.contains(e.getMaterial())
        ) {
            e.setCancelled(false);
            return;
        }

        if (e.getPlayer().hasPermission("AlathranWars.place")) {
            e.setCancelled(false);
            return;
        }

        e.setCancelled(true);
        e.setCancelMessage("");
        e.getPlayer().sendMessage(ColorParser.of("<red>You can not place blocks during sieges.").build());
    }

//    public void onIteract(Towny)

    /*@EventHandler(ignoreCancelled = true)
    public void onBlockPlaceTowny(BlockBreakEvent e) {
        Siege siege = Utils.getClosestSiege(e.getPlayer(), true);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(e.getPlayer(), siege)) return;

        if (e.getPlayer().hasPermission("AlathranWars.break")) {
            e.setCancelled(false);
            return;
        }

        if (!allowedBlocks.contains(e.getBlock().getType())) {
            e.getPlayer().sendMessage(ColorParser.of("<red>You can not break these blocks during sieges.").build());
            e.setCancelled(true);
            return;
        }

        e.setCancelled(false);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlaceTowny(BlockPlaceEvent e) {
        Siege siege = Utils.getClosestSiege(e.getPlayer(), true);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(e.getPlayer(), siege)) return;

        if (e.getPlayer().hasPermission("AlathranWars.place")) {
            e.setCancelled(false);
            return;
        }

        if (!allowedBlocks.contains(e.getBlock().getType())) {
            e.getPlayer().sendMessage(ColorParser.of("<red>You can not place these blocks during sieges.").build());
            e.setCancelled(true);
            return;
        }

        e.setCancelled(false);
    }*/
}
