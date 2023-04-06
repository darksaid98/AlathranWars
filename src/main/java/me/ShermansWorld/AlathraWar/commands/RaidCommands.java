package me.ShermansWorld.AlathraWar.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import me.ShermansWorld.AlathraWar.*;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class RaidCommands implements CommandExecutor
{
    public static ArrayList<Town> towns;
    public static ArrayList<Raid> raids;
    public static int maxID;

    static {
        RaidCommands.towns = new ArrayList<Town>();
        RaidCommands.raids = new ArrayList<Raid>();
        RaidCommands.maxID = Main.raidData.getConfig().getInt("MaxID");
    }

    public RaidCommands(final Main plugin) {
        plugin.getCommand("raid").setExecutor((CommandExecutor)this);
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        final Player p = (Player)sender;
        if (args.length == 0) {
            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /raid help");
        }
        else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                if (RaidCommands.raids.isEmpty()) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "There are currently no Raids in progress");
                    return false;
                }
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Raids currently in progress:");
                for (final Raid raid : RaidCommands.raids) {
                    p.sendMessage("ID: " + String.valueOf(raid.getID()));
                    p.sendMessage("War: " + raid.getWar().getName());
                    p.sendMessage("Raiding: " + raid.getRaidedTown().getName());
                    p.sendMessage("Gathering In: " + raid.getGatherTown().getName());
                    p.sendMessage("Raiders: " + raid.getRaiders());
                    p.sendMessage("Defenders: " + raid.getDefenders());
                    p.sendMessage("Raid Score: " + String.valueOf(raid.getRaidScore()));
                    p.sendMessage("Raid Phase: " + raid.getPhase().name());
                    p.sendMessage("Time Left: " + String.valueOf((72000 - Main.raidData.getConfig().getInt("Raids." + String.valueOf(String.valueOf(raid.getID()) + ".raidticks"))) / 1200) + " minutes");
                    p.sendMessage("-=-=-=-=-=-=-=-=-=-=-=-");
                }
            } else if (args[0].equalsIgnoreCase("help")) {
                if (p.hasPermission("!AlathraWar.admin")) {
                    p.sendMessage(Helper.Chatlabel() + "/raid stop [id]");
                }
                p.sendMessage(Helper.Chatlabel() + "/raid start [war] [town]");
                p.sendMessage(Helper.Chatlabel() + "/raid join [war] [town]");
                p.sendMessage(Helper.Chatlabel() + "/raid leave [war] [town]");
                p.sendMessage(Helper.Chatlabel() + "/raid abandon [id]");
                p.sendMessage(Helper.Chatlabel() + "/raid list");
            } else {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /raid help");
            }

        }
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("stop")) {
                if (!p.hasPermission("AlathraWar.admin")) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("&cYou do not have permission to do this"));
                    return false;
                }
                if (RaidCommands.raids.isEmpty()) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "There are currently no Raids in progress");
                    return false;
                }
                boolean found = false;
                for (int i = 0; i < RaidCommands.raids.size(); ++i) {
                    if (String.valueOf(RaidCommands.raids.get(i).getID()).equalsIgnoreCase(args[1])) {
                        found = true;
                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + "raid cancelled");
                        Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The raid of " + RaidCommands.raids.get(i).getRaidedTown().getName() + " has been cancelled by an admin");
                        RaidCommands.raids.get(i).stop();
                        break;
                    }
                }
                if (!found) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "A raid could not be found with this ID! Type /raid list to view current raids");
                    return false;
                }
            }
            else if (args[0].equalsIgnoreCase("abandon")) {
                boolean found2 = false;
                if (Main.raidData.getConfig().getString("Raids." + args[1] + ".owner") == null) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Raid not found! Usage: /raid abandon [raid id]");
                    return false;
                }
                final String configOwner = Main.raidData.getConfig().getString("Raids." + args[1] + ".owner");
                if (configOwner.equalsIgnoreCase(p.getName())) {
                    for (int j = 0; j < RaidCommands.raids.size(); ++j) {
                        if (String.valueOf(RaidCommands.raids.get(j).getID()).equalsIgnoreCase(args[1])) {
                            found2 = true;
                            Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The raid on " + RaidCommands.raids.get(j).getRaidedTown().getName() + " has been abandoned by " + RaidCommands.raids.get(j).getRaiders());
                            Main.warLogger.log(p.getName() + " abandoned the raid on " + RaidCommands.raids.get(j).getRaidedTown().getName() + " they started at " + RaidCommands.raids.get(j).getGatherTown().getName());
                            RaidCommands.raids.get(j).defendersWin(raids.get(j).getRaidScore());
                            break;
                        }
                    }
                }
                if (!found2) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Only the player who started the raid can abandon it");
                }
            } else {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /raid help");
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
                        if (!RaidCommands.towns.isEmpty()) {
                            RaidCommands.towns.clear();
                        }

                        for (final String entry : townyWorld.getTowns().keySet()) {
                            final Town raidedTown = townyWorld.getTowns().get(entry);
                            //takes the command runners town
                            final Town gatherTown = TownyAPI.getInstance().getTown(p.getLocation());
                            RaidCommands.towns.add(raidedTown);
                            if (raidedTown.getName().equalsIgnoreCase(args[2]) && gatherTown != null) {
                                townExists = true;
                                boolean attackingOwnSide = false;
                                ++RaidCommands.maxID;
                                Main.raidData.getConfig().set("MaxID", (Object)RaidCommands.maxID);
                                Main.raidData.saveConfig();
                                Raid raid2;
                                if (war.getSide1Players().contains(p.getName())) {
                                    raid2 = new Raid(RaidCommands.maxID, war, raidedTown, gatherTown, war.getSide1(), war.getSide2(), true, false);
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".war", (Object)war.getName());
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".raidedTown", (Object)raidedTown.getName());
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".gatherTown", (Object)gatherTown.getName());
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".raiders", (Object)war.getSide1());
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".activeRaiders", raid2.getActiveRaiders());
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".defenders", (Object)war.getSide2());
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".side1areraiders", (Object)true);
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".side2areraiders", (Object)false);
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".raidticks", (Object)0);
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".raidScore", (Object)0);
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".owner", (Object)p.getName());
                                    Main.raidData.saveConfig();
                                }
                                else {
                                    if (!war.getSide2Players().contains(p.getName())) {
                                        Bukkit.getLogger().info("Unable to find player declaring raid in the war");
                                        return false;
                                    }
                                    raid2 = new Raid(RaidCommands.maxID, war, raidedTown, gatherTown, war.getSide2(), war.getSide1(), false, true);
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".war", (Object)war.getName());
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".raidedTown", (Object)raidedTown.getName());
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".gatherTown", (Object)gatherTown.getName());
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".raiders", (Object)war.getSide2());
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".defenders", (Object)war.getSide1());
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".side1areraiders", (Object)false);
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".side2areraiders", (Object)true);
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".raidticks", (Object)0);
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".raidScore", (Object)0);
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID) + ".owner", (Object)p.getName());
                                    Main.raidData.saveConfig();
                                }
                                final ArrayList<String> residentNames = new ArrayList<String>();
                                for (final Resident resident : raidedTown.getResidents()) {
                                    residentNames.add(resident.getName());
                                }
                                if (raid2.getSide1AreRaiders()) {
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
                                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You cannot raid a town with members on your side of the war!");
                                    return false;
                                }
                                Town currentTown;
                                try {
                                    currentTown = WorldCoord.parseWorldCoord(p.getLocation()).getTownBlock().getTown();
                                }
                                catch (NotRegisteredException e2) {
                                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You must be within a town's claim to begin a raid. That town you are in will be the gathering town.");
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID), (Object)null);
                                    Main.raidData.saveConfig();
                                    return false;
                                }
                                if (!currentTown.equals(gatherTown)) {
                                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You must be within a town's claim to begin a raid. That town you are in will be the gathering town.");
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID), (Object)null);
                                    Main.raidData.saveConfig();
                                    return false;
                                }
                                for (final Raid raidEntry : RaidCommands.raids) {
                                    if (raidEntry.getRaidedTown().getName().equalsIgnoreCase(raidedTown.getName())) {
                                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + "This town is already under raid by your side!");
                                        Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID), (Object)null);
                                        Main.raidData.saveConfig();
                                        return false;
                                    }
                                    //TODO check when last raid was
                                    int valid = Main.raidData.isValidRaid(raidEntry.getRaidedTown());
                                    if (valid <= -1) {
                                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + "This town has been raided too recently! (24 hour cooldown)");
                                        Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID), (Object)null);
                                        Main.raidData.saveConfig();
                                        return false;
                                    } else if (valid == 0) {
                                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Your side has raided too recently! (6 hour cooldown)");
                                        Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID), (Object)null);
                                        Main.raidData.saveConfig();
                                        return false;
                                    }
                                }
                                final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(p.getUniqueId());
                                if (Main.econ.getBalance(offlinePlayer) <= 2500.0) {
                                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You must have at least $2500 to put up to start a raid.");
                                    Main.raidData.getConfig().set("Raids." + String.valueOf(RaidCommands.maxID), (Object)null);
                                    Main.raidData.saveConfig();
                                    return false;
                                }
                                Main.econ.withdrawPlayer(offlinePlayer, 2500);
                                RaidCommands.raids.add(raid2);
                                raid2.start();
                                Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "As part of " + war.getName() + ", forces from " + raid2.getRaiders() + " are gathering to raid the town of " + raidedTown.getName() + "!");
                                Main.warLogger.log(p.getName() + " started a raid.");
                                Main.warLogger.log("As part of " + war.getName() + ", forces from " + raid2.getRaiders() + " are raiding the town of " + raidedTown.getName() + "!");
                            }
                        }
                    }
                }
                if (!warFound) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "That war does not exist! /raid start [war] [town]");
                    return false;
                }
                if (!townExists) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "That town does not exist! /raid start [war] [town]");
                }
            } else if (args[0].equalsIgnoreCase("join")) {
                boolean warFound = false;
                boolean townExists = false;
                boolean raidFound = false;
                for (final War war : WarCommands.wars) {
                    if (war.getName().equalsIgnoreCase(args[1])) {
                        warFound = true;
                        if (!war.getSide1Players().contains(p.getName()) && !war.getSide2Players().contains(p.getName())) {
                            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You are not in this war! Type /war join [war] [side]");
                        }
                        TownyWorld townyWorld;
                        townyWorld = WorldCoord.parseWorldCoord(p.getLocation()).getTownyWorld();
                        if (!RaidCommands.towns.isEmpty()) {
                            RaidCommands.towns.clear();
                        }

                        //find the associated raid
                        Raid raid = null;
                        for(Raid r : raids) {
                            if(r.getWar().getId() == war.getId()) {
                                raidFound = true;
                                raid = r;
                            }
                        }

                        //if we find the raid
                        if(raidFound) {
                            //check if gather phase
                            if(raid.getPhase() == RaidPhase.GATHER || raid.getPhase() == RaidPhase.START) {
                                try {
                                    ArrayList<WorldCoord> cluster = Helper.getCluster(raid.getGatherTown().getHomeBlock().getWorldCoord());
                                    if(cluster.contains(WorldCoord.parseWorldCoord(p))) {
                                        raid.addActiveRaider(p.getName());
                                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You have joined the raid on " + raid.getRaidedTown().getName() + "!");
                                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Leaving " + raid.getGatherTown().getName() + " will cause you to leave the raid party.");
                                    }
                                } catch (TownyException e) {
                                    throw new RuntimeException(e);
                                }

                            } else {
                                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You cannot join the raid on " + raid.getRaidedTown().getName() + "! It has already started!");
                            }
                        }
                    }
                }
                if (!warFound) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "That war does not exist! /raid start [war] [town]");
                    return false;
                }
                if (!townExists) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "That town does not exist! /raid start [war] [town]");
                }
                if (!raidFound) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "No raid is gathering in this town!");
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("leave")) {
                boolean warFound = false;
                boolean townExists = false;
                boolean raidFound = false;
                for (final War war : WarCommands.wars) {
                    if (war.getName().equalsIgnoreCase(args[1])) {
                        warFound = true;
                        if (!war.getSide1Players().contains(p.getName()) && !war.getSide2Players().contains(p.getName())) {
                            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You are not in this war! Type /war join [war] [side]");
                        }
                        TownyWorld townyWorld;
                        townyWorld = WorldCoord.parseWorldCoord(p.getLocation()).getTownyWorld();
                        if (!RaidCommands.towns.isEmpty()) {
                            RaidCommands.towns.clear();
                        }

                        //find the associated raid
                        Raid raid = null;
                        for(Raid r : raids) {
                            if(r.getWar().getId() == war.getId()) {
                                raidFound = true;
                                raid = r;
                            }
                        }

                        //if we find the raid
                        if(raidFound) {
                            //check if gather phase
                            if(raid.getPhase() == RaidPhase.GATHER || raid.getPhase() == RaidPhase.START) {
                                if(raid.getGatherTown().hasTownBlock(WorldCoord.parseWorldCoord(p))) {
                                    raid.removeActiveRaider(p.getName());
                                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You have left the raid on " + raid.getRaidedTown().getName() + "!");
                                }
                            } else {
                                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You cannot leave the raid on " + raid.getRaidedTown().getName() + "! It has already started!");
                            }
                        }
                    }
                }
                if (!warFound) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "That war does not exist! /raid start [war] [town]");
                    return false;
                }
                if (!townExists) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "That town does not exist! /raid start [war] [town]");
                }
                if (!raidFound) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "No raid is gathering in this town!");
                    return false;
                }
            } else {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /raid help");
            }
        }
        else {
            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /raid help");
        }
        return false;
    }
}
