package me.ShermansWorld.AlathraWar;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;

import net.milkbowl.vault.economy.Economy;

public class SiegeCommands implements CommandExecutor {
	
	public static ArrayList<Town> towns = new ArrayList<Town>();
	public static ArrayList<Siege> sieges = new ArrayList<Siege>();
	
	public SiegeCommands(Main plugin) {
		plugin.getCommand("siege").setExecutor((CommandExecutor) this); // command to run in chat
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		Player p = (Player) sender; // Convert sender into player
		World w = p.getWorld(); // Get world
		
		if (args.length == 0) {
			p.sendMessage(Helper.Chatlabel() + "Invalid Arguments. /siege help");
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("start")) {
				boolean warFound = false;
				boolean townExists = false;
				for (War war : WarCommands.wars) {
					if (war.getName().equalsIgnoreCase(args[1])) {
						warFound = true;
						if(!(war.getSide1Players().contains(p.getName())) && !(war.getSide2Players().contains(p.getName())) ) {
							p.sendMessage(Helper.Chatlabel() + "You are not in this war! Type /war join [war] [side]");
						}
						TownyWorld townyWorld;
						try {
							townyWorld = WorldCoord.parseWorldCoord(p.getLocation()).getTownyWorld();
						} catch (NotRegisteredException e) {
							Bukkit.getLogger().info("TownyWorld not found");
							return false;
						}
						if (!towns.isEmpty()) {
							towns.clear();
						}
						for (String entry: townyWorld.getTowns().keySet()) { // gets an updated list of towns on the server
							Town town = townyWorld.getTowns().get(entry);
							towns.add(town);
							if (town.getName().equalsIgnoreCase(args[2])) {
								townExists = true;
								Siege siege;
								boolean attackingOwnSide = false;
								if (war.getSide1Players().contains(p.getName())) {
									siege = new Siege(war, town, war.getSide1(), war.getSide2(), true, false);
								} else if (war.getSide2Players().contains(p.getName())) {
									siege = new Siege(war, town, war.getSide2(), war.getSide1(), false, true);
								} else {
									Bukkit.getLogger().info("Unable to find player declaring siege in the war");
									return false;
								}
								ArrayList<String> residentNames = new ArrayList<String>();
								for (Resident resident : town.getResidents()) {
									residentNames.add(resident.getName());
								}
								if (siege.getSide1AreAttackers()) {
									for (String playerName : war.getSide1Players()) {
										if (residentNames.contains(playerName)) {
											attackingOwnSide = true;
										}
									}
								} else {
									for (String playerName : war.getSide2Players()) {
										if (residentNames.contains(playerName)) {
											attackingOwnSide = true;
										}
									}
								}
								if (attackingOwnSide) {
									p.sendMessage(Helper.Chatlabel() + "You cannot siege a town with members on your side of the war!");
									return false;
								} else {
									Town currentTown;
									try {
										currentTown = WorldCoord.parseWorldCoord(p.getLocation()).getTownBlock().getTown();
									} catch (NotRegisteredException e) {
										p.sendMessage(Helper.Chatlabel() + "You must be within the town's claim to begin a siege");
										return false;
									}
									if (!currentTown.equals(town)) {
										p.sendMessage(Helper.Chatlabel() + "You must be within the town's claim to begin a siege");
										return false;
									}
									for (Siege siegeEntry : sieges) {
										if (siegeEntry.getTown().getName().equalsIgnoreCase(town.getName())) {
											p.sendMessage(Helper.Chatlabel() + "This town is already under siege by your side!");
											return false;
										}
									}
									sieges.add(siege);
									siege.start();
									Bukkit.broadcastMessage(Helper.Chatlabel() + "As part of " + war.getName() + ", forces from " + siege.getAttackers()
									+ " are laying siege to the town of " + town.getName() + "!");
								}
							}
						}
						
					}
				}
				if (!warFound) {
					p.sendMessage(Helper.Chatlabel() + "That war does not exist! /siege start [war] [town]");
					return false;
				}
				if (!townExists) {
					p.sendMessage(Helper.Chatlabel() + "That town does not exist! /siege start [war] [town]");
				}
			}
		} else {
			p.sendMessage(Helper.Chatlabel() + "Invalid Arguments. /siege help");
		}
		
		
		return false;
	}

}
