package me.ShermansWorld.AlathraWar;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;


public class SiegeCommands implements CommandExecutor {
	
	public static ArrayList<Town> towns = new ArrayList<Town>();
	public static ArrayList<Siege> sieges = new ArrayList<Siege>();
	public static int maxID = Main.data2.getConfig().getInt("MaxID");
	
	public SiegeCommands(Main plugin) {
		plugin.getCommand("siege").setExecutor((CommandExecutor) this); // command to run in chat
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		Player p = (Player) sender; // Convert sender into player
		
		if (args.length == 0) {
			p.sendMessage(Helper.Chatlabel() + "Invalid Arguments. /siege help");
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("list")) {
				if (sieges.isEmpty()) {
					p.sendMessage(Helper.Chatlabel() + "There are currently no sieges in progress");
					return false;
				} else {
					p.sendMessage(Helper.Chatlabel() + "Sieges currently in progress:");
					for (Siege siege : sieges) {
						p.sendMessage("ID: " + String.valueOf(siege.getID()));
						p.sendMessage("War: " + siege.getWar().getName());
						p.sendMessage("Town: " + siege.getTown().getName());
						p.sendMessage("Attackers: " + siege.getAttackers());
						p.sendMessage("Defenders: " + siege.getDefenders());
						p.sendMessage("Attacker Points: " + String.valueOf(siege.getAttackerPoints()));
						p.sendMessage("Defender Points: " + String.valueOf(siege.getDefenderPoints()));
						p.sendMessage("Time Left: " + 
						String.valueOf((108000 - Main.data2.getConfig().getInt("Sieges." + String.valueOf(siege.getID() + ".siegeticks"))) / 1200) + " minutes" );
						p.sendMessage("-=-=-=-=-=-=-=-=-=-=-=-");
					}
				}
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("stop")) {
				if (!p.hasPermission("AlathraWar.admin")) {
					p.sendMessage(Helper.Chatlabel() + Helper.color("&cYou do not have permission to do this"));
					return false;
				}
				if (sieges.isEmpty()) {
					p.sendMessage(Helper.Chatlabel() + "There are currently no sieges in progress");
					return false;
				} else {
					boolean found = false;
					for (int i = 0; i < sieges.size(); i++) { // can't use shorthand here because stop() modifies sieges list
						if (String.valueOf(sieges.get(i).getID()).equalsIgnoreCase(args[1])) {
							found = true;
							p.sendMessage(Helper.Chatlabel() + "siege cancelled");
							Bukkit.broadcastMessage(Helper.Chatlabel() + "The siege at " + sieges.get(i).getTown().getName() + " has been cancelled by an admin");
							sieges.get(i).stop();
							break;
						}
					}
					if (!found) {
						p.sendMessage(Helper.Chatlabel() + "A siege could not be found with this ID! Type /siege list to view current sieges");
						return false;
					}
				}
			} else if (args[0].equalsIgnoreCase("abandon")) {
				String configOwner;
				boolean found = false;
				if (Main.data2.getConfig().getString("Sieges." + args[1] + ".owner") != null) {
					configOwner = Main.data2.getConfig().getString("Sieges." + args[1] + ".owner");
				} else {
					p.sendMessage(Helper.Chatlabel() + "Siege not found! Usage: /siege abandon [siege id]");
					return false;
				}
				if (configOwner.equalsIgnoreCase(p.getName())) {
					for (int i = 0; i < sieges.size(); i++) { // can't use shorthand here because stop() modifies sieges list
						if (String.valueOf(sieges.get(i).getID()).equalsIgnoreCase(args[1])) {
							found = true;
							Bukkit.broadcastMessage(Helper.Chatlabel() + "The siege at " + sieges.get(i).getTown().getName()
									+ " has been abandoned by " + sieges.get(i).getAttackers());
							sieges.get(i).defendersWin();
							sieges.get(i).stop();
							break;
						}
					}
				}
				if (!found) {
					p.sendMessage(Helper.Chatlabel() + "Only the player who started the siege can abandon it");
				}
			}
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
								maxID++;
								Main.data2.getConfig().set("MaxID", maxID);
								Main.data2.saveConfig();
								if (war.getSide1Players().contains(p.getName())) {
									siege = new Siege(maxID, war, town, war.getSide1(), war.getSide2(), true, false);
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".war", war.getName());
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".town", town.getName());
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".attackers", war.getSide1());
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".defenders", war.getSide2());
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".side1areattackers", true);
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".side2areattackers", false);
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".siegeticks", 0);
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".attackerpoints", 0);
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".defenderpoints", 0);
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".owner", p.getName());
									Main.data2.saveConfig();
								} else if (war.getSide2Players().contains(p.getName())) {
									siege = new Siege(maxID, war, town, war.getSide2(), war.getSide1(), false, true);
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".war", war.getName());
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".town", town.getName());
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".attackers", war.getSide2());
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".defenders", war.getSide1());
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".side1areattackers", false);
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".side2areattackers", true);
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".siegeticks", 0);
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".attackerpoints", 0);
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".defenderpoints", 0);
									Main.data2.getConfig().set("Sieges." + String.valueOf(maxID) + ".owner", p.getName());
									Main.data2.saveConfig();
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
										Main.data2.getConfig().set("Sieges." + String.valueOf(maxID), null);
										Main.data2.saveConfig();
										return false;
									}
									if (!currentTown.equals(town)) {
										p.sendMessage(Helper.Chatlabel() + "You must be within the town's claim to begin a siege");
										Main.data2.getConfig().set("Sieges." + String.valueOf(maxID), null);
										Main.data2.saveConfig();
										return false;
									}
									for (Siege siegeEntry : sieges) {
										if (siegeEntry.getTown().getName().equalsIgnoreCase(town.getName())) {
											p.sendMessage(Helper.Chatlabel() + "This town is already under siege by your side!");
											Main.data2.getConfig().set("Sieges." + String.valueOf(maxID), null);
											Main.data2.saveConfig();
											return false;
										}
									}
									OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(p.getName());
									if (Main.econ.getBalance(offlinePlayer) > 20000.0) {
										Main.econ.withdrawPlayer(offlinePlayer, 20000.0);
									} else {
										p.sendMessage(Helper.Chatlabel() + "You must have at least $20,000 to put up to start a siege");
										Main.data2.getConfig().set("Sieges." + String.valueOf(maxID), null);
										Main.data2.saveConfig();
										return false;
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
