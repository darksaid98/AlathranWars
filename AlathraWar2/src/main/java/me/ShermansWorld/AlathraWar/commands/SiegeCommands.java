package me.ShermansWorld.AlathraWar.commands;

import org.bukkit.OfflinePlayer;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.WorldCoord;

import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.War;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.palmergames.bukkit.towny.object.Town;
import java.util.ArrayList;
import org.bukkit.command.CommandExecutor;

public class SiegeCommands implements CommandExecutor
{
    public static ArrayList<Town> towns;
    public static ArrayList<Siege> sieges;
    public static int maxID;
    
    static {
        SiegeCommands.towns = new ArrayList<Town>();
        SiegeCommands.sieges = new ArrayList<Siege>();
        SiegeCommands.maxID = Main.siegeData.getConfig().getInt("MaxID");
    }
    
    public SiegeCommands(final Main plugin) {
        plugin.getCommand("siege").setExecutor((CommandExecutor)this);
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        final Player p = (Player)sender;
        if (args.length == 0) {
            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /siege help");
        }
        else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                if (SiegeCommands.sieges.isEmpty()) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "There are currently no sieges in progress");
                    return false;
                }
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Sieges currently in progress:");
                for (final Siege siege : SiegeCommands.sieges) {
                    p.sendMessage("ID: " + String.valueOf(siege.getID()));
                    p.sendMessage("War: " + siege.getWar().getName());
                    p.sendMessage("Town: " + siege.getTown().getName());
                    p.sendMessage("Attackers: " + siege.getAttackers());
                    p.sendMessage("Defenders: " + siege.getDefenders());
                    p.sendMessage("Attacker Points: " + String.valueOf(siege.getAttackerPoints()));
                    p.sendMessage("Defender Points: " + String.valueOf(siege.getDefenderPoints()));
                    p.sendMessage("Time Left: " + String.valueOf((108000 - Main.siegeData.getConfig().getInt("Sieges." + String.valueOf(String.valueOf(siege.getID()) + ".siegeticks"))) / 1200) + " minutes");
                    p.sendMessage("-=-=-=-=-=-=-=-=-=-=-=-");
                }
            } else if (args[0].equalsIgnoreCase("help")) {
                if (p.hasPermission("!AlathraWar.admin")) {
					p.sendMessage(Helper.Chatlabel() + "/siege stop [id]");
				}
				p.sendMessage(Helper.Chatlabel() + "/siege start [war] [town]");
				p.sendMessage(Helper.Chatlabel() + "/siege abandon [id]");
				p.sendMessage(Helper.Chatlabel() + "/siege list");
            } else {
            	p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /siege help");
            }
            
        }
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("stop")) {
                if (!p.hasPermission("AlathraWar.admin")) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("&cYou do not have permission to do this"));
                    return false;
                }
                if (SiegeCommands.sieges.isEmpty()) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "There are currently no sieges in progress");
                    return false;
                }
                boolean found = false;
                for (int i = 0; i < SiegeCommands.sieges.size(); ++i) {
                    if (String.valueOf(SiegeCommands.sieges.get(i).getID()).equalsIgnoreCase(args[1])) {
                        found = true;
                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + "siege cancelled");
                        Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The siege at " + SiegeCommands.sieges.get(i).getTown().getName() + " has been cancelled by an admin");
                        SiegeCommands.sieges.get(i).clearBeacon();
                        SiegeCommands.sieges.get(i).stop();
                        break;
                    }
                }
                if (!found) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "A siege could not be found with this ID! Type /siege list to view current sieges");
                    return false;
                }
            }
            else if (args[0].equalsIgnoreCase("abandon")) {
                boolean found2 = false;
                if (Main.siegeData.getConfig().getString("Sieges." + args[1] + ".owner") == null) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Siege not found! Usage: /siege abandon [siege id]");
                    return false;
                }
                final String configOwner = Main.siegeData.getConfig().getString("Sieges." + args[1] + ".owner");
                if (configOwner.equalsIgnoreCase(p.getName())) {
                    for (int j = 0; j < SiegeCommands.sieges.size(); ++j) {
                        if (String.valueOf(SiegeCommands.sieges.get(j).getID()).equalsIgnoreCase(args[1])) {
                            found2 = true;
                            Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The siege at " + SiegeCommands.sieges.get(j).getTown().getName() + " has been abandoned by " + SiegeCommands.sieges.get(j).getAttackers());
                            Main.warLogger.log(p.getName() + " abandoned the siege they started at " + SiegeCommands.sieges.get(j).getTown().getName());
                            SiegeCommands.sieges.get(j).defendersWin();
                            break;
                        }
                    }
                }
                if (!found2) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Only the player who started the siege can abandon it");
                }
            } else {
            	p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /siege help");
            }
        }
        else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("start")) {
                boolean warFound = false;
                boolean townExists = false;
                for (final War war : WarCommands.wars) {
                    if (war.getName().equalsIgnoreCase(args[1])) {
                        warFound = true;
                        if (!war.getSide1Players().contains(p.getName()) && !war.getSide2Players().contains(p.getName())) {
                            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You are not in this war! Type /war join [war] [side]");
                        }
                        TownyWorld townyWorld;
                        townyWorld = WorldCoord.parseWorldCoord(p.getLocation()).getTownyWorld();
                        if (!SiegeCommands.towns.isEmpty()) {
                            SiegeCommands.towns.clear();
                        }
                        for (final String entry : townyWorld.getTowns().keySet()) {
                            final Town town = townyWorld.getTowns().get(entry);
                            SiegeCommands.towns.add(town);
                            if (town.getName().equalsIgnoreCase(args[2])) {
                                townExists = true;
                                boolean attackingOwnSide = false;
                                ++SiegeCommands.maxID;
                                Main.siegeData.getConfig().set("MaxID", (Object)SiegeCommands.maxID);
                                Main.siegeData.saveConfig();
                                Siege siege2;
                                if (war.getSide1Players().contains(p.getName())) {
                                    siege2 = new Siege(SiegeCommands.maxID, war, town, war.getSide1(), war.getSide2(), true, false);
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".war", (Object)war.getName());
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".town", (Object)town.getName());
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".attackers", (Object)war.getSide1());
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".defenders", (Object)war.getSide2());
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".side1areattackers", (Object)true);
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".side2areattackers", (Object)false);
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".siegeticks", (Object)0);
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".attackerpoints", (Object)0);
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".defenderpoints", (Object)0);
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".owner", (Object)p.getName());
                                    Main.siegeData.saveConfig();
                                }
                                else {
                                    if (!war.getSide2Players().contains(p.getName())) {
                                        Bukkit.getLogger().info("Unable to find player declaring siege in the war");
                                        return false;
                                    }
                                    siege2 = new Siege(SiegeCommands.maxID, war, town, war.getSide2(), war.getSide1(), false, true);
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".war", (Object)war.getName());
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".town", (Object)town.getName());
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".attackers", (Object)war.getSide2());
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".defenders", (Object)war.getSide1());
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".side1areattackers", (Object)false);
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".side2areattackers", (Object)true);
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".siegeticks", (Object)0);
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".attackerpoints", (Object)0);
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".defenderpoints", (Object)0);
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID) + ".owner", (Object)p.getName());
                                    Main.siegeData.saveConfig();
                                }
                                final ArrayList<String> residentNames = new ArrayList<String>();
                                for (final Resident resident : town.getResidents()) {
                                    residentNames.add(resident.getName());
                                }
                                if (siege2.getSide1AreAttackers()) {
                                    for (final String playerName : war.getSide1Players()) {
                                        if (residentNames.contains(playerName)) {
                                            attackingOwnSide = true;
                                        }
                                    }
                                }
                                else {
                                    for (final String playerName : war.getSide2Players()) {
                                        if (residentNames.contains(playerName)) {
                                            attackingOwnSide = true;
                                        }
                                    }
                                }
                                if (attackingOwnSide) {
                                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You cannot siege a town with members on your side of the war!");
                                    return false;
                                }
                                Town currentTown;
                                try {
                                    currentTown = WorldCoord.parseWorldCoord(p.getLocation()).getTownBlock().getTown();
                                }
                                catch (NotRegisteredException e2) {
                                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You must be within the town's claim to begin a siege");
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID), (Object)null);
                                    Main.siegeData.saveConfig();
                                    return false;
                                }
                                if (!currentTown.equals(town)) {
                                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You must be within the town's claim to begin a siege");
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID), (Object)null);
                                    Main.siegeData.saveConfig();
                                    return false;
                                }
                                for (final Siege siegeEntry : SiegeCommands.sieges) {
                                    if (siegeEntry.getTown().getName().equalsIgnoreCase(town.getName())) {
                                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + "This town is already under siege by your side!");
                                        Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID), (Object)null);
                                        Main.siegeData.saveConfig();
                                        return false;
                                    }
                                }
                                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(p.getUniqueId());
                                if (Main.econ.getBalance(offlinePlayer) <= 20000.0) {
                                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You must have at least $20,000 to put up to start a siege");
                                    Main.siegeData.getConfig().set("Sieges." + String.valueOf(SiegeCommands.maxID), (Object)null);
                                    Main.siegeData.saveConfig();
                                    return false;
                                }
                                Main.econ.withdrawPlayer(offlinePlayer, 20000.0);
                                SiegeCommands.sieges.add(siege2);
                                siege2.start();
                                siege2.createBeacon();
                                Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "As part of " + war.getName() + ", forces from " + siege2.getAttackers() + " are laying siege to the town of " + town.getName() + "!");
                                Main.warLogger.log(p.getName() + " started a siege.");
                                Main.warLogger.log("As part of " + war.getName() + ", forces from " + siege2.getAttackers() + " are laying siege to the town of " + town.getName() + "!");
                            }
                        }
                    }
                }
                if (!warFound) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "That war does not exist! /siege start [war] [town]");
                    return false;
                }
                if (!townExists) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "That town does not exist! /siege start [war] [town]");
                }
            } else {
            	p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /siege help");
            }
        }
        else {
            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /siege help");
        }
        return false;
    }
}
