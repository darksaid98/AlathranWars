package me.ShermansWorld.AlathraWar.listeners;

import me.ShermansWorld.AlathraWar.Raid;
import me.ShermansWorld.AlathraWar.data.RaidData;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.palmergames.bukkit.towny.object.WorldCoord;

import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.data.SiegeData;

import org.bukkit.event.Listener;

public class BlockBreakListener implements Listener {
	
	private static final Set<Material> allowedBlocks = new HashSet<>();
	
	static {
		allowedBlocks.add(Material.TNT);
	}
	
	@EventHandler
	public void onBlockBreak(final BlockBreakEvent event) {

		final Block block = event.getBlock();
		if (SiegeData.getSieges().isEmpty() && RaidData.getRaids().isEmpty()) {
			return;
		}
		for (final Siege siege : SiegeData.getSieges()) {
			if (siege.beaconLocs.contains(block.getLocation())) {
				event.setCancelled(true);
				return;
			}
		}
		if (allowedBlocks.contains(block.getType())) {
			return;
		}
		Player p = event.getPlayer();
		if(p.hasPermission("AlathraWar.break")) {
			return;
		}
		if (WorldCoord.parseWorldCoord(block).getTownOrNull() != null) {
			for (final Siege siege : SiegeData.getSieges()) {
				if (siege.getTown() == WorldCoord.parseWorldCoord(block).getTownOrNull()) {
					p.sendMessage(Helper.color("&cYou can not break blocks during sieges"));
					event.setCancelled(true);
					return;
				}
			}

			for (final Raid raid : RaidData.getRaids()) {
				if (raid.getRaidedTown() == WorldCoord.parseWorldCoord(block).getTownOrNull()) {
					p.sendMessage(Helper.color("&cYou can not break blocks during raids"));
					event.setCancelled(true);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onBlockPlace(final BlockPlaceEvent event) {
		final Block block = event.getBlock();
		if (SiegeData.getSieges().isEmpty() && RaidData.getRaids().isEmpty()) {
			return;
		}
		if (allowedBlocks.contains(block.getType())) {
			return;
		}
		Player p = event.getPlayer();
		if(p.hasPermission("AlathraWar.place")) {
			return;
		}
		if (WorldCoord.parseWorldCoord(block).getTownOrNull() != null) {
			for (final Siege siege : SiegeData.getSieges()) {
				if (siege.getTown() == WorldCoord.parseWorldCoord(block).getTownOrNull()) {
					p.sendMessage(Helper.color("&cYou can not place blocks during sieges"));
					event.setCancelled(true);
					return;
				}
			}

			for (final Raid raid : RaidData.getRaids()) {
				if (raid.getRaidedTown() == WorldCoord.parseWorldCoord(block).getTownOrNull()) {
					p.sendMessage(Helper.color("&cYou can not place blocks during raids"));
					event.setCancelled(true);
					return;
				}
			}
		}
	}
}
