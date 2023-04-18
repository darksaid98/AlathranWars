package me.ShermansWorld.AlathraWar.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import me.ShermansWorld.AlathraWar.*;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import me.ShermansWorld.AlathraWar.data.WarData;
import me.ShermansWorld.AlathraWar.data.RaidData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class RaidCommands implements CommandExecutor {

    public RaidCommands(final Main plugin) {
        plugin.getCommand("raid").setExecutor((CommandExecutor)this);
    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        final Player p = (Player) sender;
        if (args.length == 0) {
            fail(p, args, "syntax");
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list")) {
                listRaids(p, args);
            } else if (args[0].equalsIgnoreCase("help")) {
                raidHelp(p, args);
            } else {
                fail(p, args, "syntax");
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("stop")) {
                stopRaid(p, args);
            } else {
                fail(p, args, "syntax");
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("start")) {
                startRaid(p, args, false);
            } else if (args[0].equalsIgnoreCase("join")) {
                joinRaid(p, args, false);
            } else if (args[0].equalsIgnoreCase("leave")) {
                leaveRaid(p, args, false);
            } else if (args[0].equalsIgnoreCase("abandon")) {
                abandonRaid(p, args);
            } else {
                fail(p, args, "syntax");
            }
        } else {
            fail(p, args, "syntax");
        }
        return false;

    }

    /**
     * Starts a raid
     *
     * @param p
     * @param args
     */
    protected static void startRaid(Player p, String[] args, boolean admin) {
        boolean warFound = false;
        boolean townExists = false;
        for (final War war : WarData.getWars()) {
            //if this is run as admin, shift our check forward a slot
            if (war.getName().equalsIgnoreCase(args[1 + (admin ? 1 : 0)])) {
                warFound = true;
                if (!war.getSide1Players().contains(p.getName()) && !war.getSide2Players().contains(p.getName())) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You are not in this war! Type /war join [war] [side]");
                }
                TownyWorld townyWorld;
                townyWorld = WorldCoord.parseWorldCoord(p.getLocation()).getTownyWorld();

                Player raidOwner = p;
                if(admin && args.length >= 6) {
                    raidOwner = Bukkit.getOfflinePlayer(args[5]).getPlayer();
                    if(raidOwner == null) {
                        raidOwner = p;
                    }
                }

                //check if in a town
                //takes the command runners town as the gather point
                Town gatherTown = null;
                try {
                    //if admin, and we have a gather town argument, use it
                    if(admin && args.length >= 5) {
                        if(args[4].equalsIgnoreCase("defaultCode")) {
                            //use default behavior
                            gatherTown = TownyAPI.getInstance().getTownOrNull(WorldCoord.parseWorldCoord(p.getLocation()).getTownBlock());
                        } else if(TownyAPI.getInstance().getTown(args[4]) != null) {
                            gatherTown = TownyAPI.getInstance().getTown(args[4]);
                        } else {
                            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "No valid town set for gather town, defaulting to current location");
                            gatherTown = TownyAPI.getInstance().getTownOrNull(WorldCoord.parseWorldCoord(p.getLocation()).getTownBlock());
                        }
                    } else {
                        gatherTown = TownyAPI.getInstance().getTownOrNull(WorldCoord.parseWorldCoord(p.getLocation()).getTownBlock());
                    }
                } catch (NotRegisteredException e2) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You must be within a town's claim to begin a raid. That town you are in will be the gathering town.");
                    return;
                }
                if (gatherTown == null) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You must be within a town's claim to begin a raid. That town you are in will be the gathering town.");
                    return;
                }

                //parse the argunment for town
                for (final String entry : townyWorld.getTowns().keySet()) {
                    final Town raidedTown = townyWorld.getTowns().get(entry);

                    //if this is run as admin, shift our check forward a slot
                    if (raidedTown.getName().equalsIgnoreCase(args[2 + (admin ? 1 : 0)]) && gatherTown != null) {
                        townExists = true;
                        boolean attackingOwnSide = false;
                        Raid raid2;
                        if (war.getSide1Players().contains(p.getName())) {
                            //Time and raid activity validity check
                            int c = RaidData.isValidRaid(war, raidedTown);
                            if (c == 2) {
                                //if were admin, see if an owner arg exists, and if so then use it
                                raid2 = new Raid(war, raidedTown, gatherTown, true, raidOwner);
                            } else if (c == 1) {
                                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "This town is already being raided at this time!");
                                return;
                            } else if (c == 0) {
                                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Your side has raided too recently!");
                                return;
                            } else if (c == -1) {
                                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "This town was raided too recently!");
                                return;
                            } else if (c == -2) {
                                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "This town has no online players to defend it!");
                                return;
                            }  else {
                                throw new IllegalArgumentException();
                            }
                        } else {
                            //Something broke if this runs
                            if (!war.getSide2Players().contains(p.getName())) {
                                Bukkit.getLogger().info("Unable to find player declaring raid in the war");
                                return;
                            }
                            //Time and raid activity validity check
                            int c = RaidData.isValidRaid(war, raidedTown);
                            if (c == 2) {
                                raid2 = new Raid(war, raidedTown, gatherTown, false, raidOwner);
                            } else if (c == 1) {
                                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "This town is already being raided at this time!");
                                return;
                            } else if (c == 0) {
                                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Your side has raided too recently!");
                                return;
                            } else if (c == -1) {
                                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "This town was raided too recently!");
                                return;
                            } else if (c == -2) {
                                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "At least on member of the raided town must be online to defend it!");
                                return;
                            }  else {
                                throw new IllegalArgumentException();
                            }
                        }

                        //validity check over attacking own towns
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
                        } else {
                            for (final String playerName : war.getSide2Players()) {
                                if (residentNames.contains(playerName)) {
                                    attackingOwnSide = true;
                                }
                            }
                        }
                        if (attackingOwnSide) {
                            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You cannot raid a town with members on your side of the war!");
                            return;
                        }

                        //check player balance
                        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(p.getUniqueId());
                        if (Main.econ.getBalance(offlinePlayer) <= 1000.0) {
                            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You must have at least $1000 to put up to start a raid.");
                            return;
                        }

                        //publish raid
                        Main.econ.withdrawPlayer(offlinePlayer, 1000);
                        RaidData.addRaid(raid2);
                        raid2.start(); //funny funny method haha
                        WarData.saveWar(raid2.getWar());

                        //broadcast
                        Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "As part of " + war.getName() + ", forces from " + raid2.getRaiders() + " are gathering to raid the town of " + raidedTown.getName() + "!");
                        Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "All players from the defending side have been drafted for the town's defense.");
                        Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The raid of "
                                + raidedTown.getName() + " will begin in " + (int) (RaidPhase.TRAVEL.startTick / 20 / 60) + " minutes!");
                        Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) +
                                "The Raiders are gathering at " + gatherTown.getName() + " before making the journey over!");

                        Main.warLogger.log(p.getName() + " started a raid.");
                        if(admin) Main.warLogger.log(p.getName() + " started a raid using admin methods.");
                        Main.warLogger.log("As part of " + war.getName() + ", forces from " + raid2.getRaiders() + " are raiding the town of " + raidedTown.getName() + "!");
                        Main.warLogger.log("The Raiders are gathering at " + gatherTown.getName() + " before making the journey over!");
                    }
                }
            }
        }
        if (!warFound) {
            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "That war does not exist! /raid start [war] [town]");
            return;
        }
        if (!townExists) {
            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "That town does not exist! /raid start [war] [town]");
        }
    }

    /**
     * Stops the current raid by admins
     *
     * @param p
     * @param args
     */
    private static void stopRaid(Player p, String[] args) {
        //admin check
        if (!p.hasPermission("AlathraWar.admin")) {
            fail(p, args, "permission");
            return;
        }

        //are there even any raids
        if (RaidData.getRaids().isEmpty()) {
            fail(p, args, "noRaids");
            return;
        }
        boolean found = false;

        //find our desired raid and stop it
        Raid raid = RaidData.getRaidOrNull(args[1]);
        if (raid != null) {
            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "raid cancelled");
            Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The raid of " + raid.getRaidedTown().getName() + " has been cancelled by an admin");
            raid.stop(); //this purges itself from raids
        } else {
            fail(p, args, "badName");
        }
    }

    protected static void joinRaid(Player p, String[] args, boolean admin) {
        //Grab raid!
        Raid raid = RaidData.getRaidOrNull(args[1] + "-" + args[2]);
        if (raid != null) {
            if (raid.getWar().getName().equalsIgnoreCase(args[1])) {
                Player joiner = p;
                if (admin) {
                    joiner = Bukkit.getOfflinePlayer(args[3]).getPlayer();

                    if(args[4] != null) {
                        if(args[4].equals(raid.getDefenders())) {
                            if(!raid.getDefenderPlayers().contains(joiner.getName()))
                                raid.getDefenderPlayers().add(joiner.getName());
                            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Player " + joiner.getName() + " added to defender side.");
                            Main.warLogger.log("Player " + joiner.getName() + " added to defender side.");
                            joiner.sendMessage(String.valueOf(Helper.Chatlabel()) + "You were forcefully added to the defender side against the raid party on " + raid.getRaidedTown() + " by an admin.");
                            return;
                        } else {
                            if(!raid.getRaiderPlayers().contains(joiner.getName()))
                                raid.getRaiderPlayers().add(joiner.getName());
                            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Player " + joiner.getName() + " added to raider side.");
                            Main.warLogger.log("Player " + joiner.getName() + " added to raider side.");
                            joiner.sendMessage(String.valueOf(Helper.Chatlabel()) + "You were forcefully added to the raid party on " + raid.getRaidedTown() + " by an admin. Leaving the gather town will remove you from the raid party.");
                        }
                    }
                }
                if (!raid.getWar().getSide1Players().contains(joiner.getName()) && !raid.getWar().getSide2Players().contains(joiner.getName())) {
                    joiner.sendMessage(String.valueOf(Helper.Chatlabel()) + "You are not in this war! Type /war join [war] [side]");
                }

                //check if gather phase
                if (raid.getPhase() == RaidPhase.GATHER || raid.getPhase() == RaidPhase.START) {
                    try {
                        //check if the player is in the gather town
                        ArrayList<WorldCoord> cluster = Helper.getCluster(raid.getGatherTown().getHomeBlock().getWorldCoord());
                        if (cluster.contains(WorldCoord.parseWorldCoord(joiner))) {
                            raid.addActiveRaider(joiner.getName());
                            joiner.sendMessage(String.valueOf(Helper.Chatlabel()) + "You have joined the raid on " + raid.getRaidedTown().getName() + "!");
                            joiner.sendMessage(String.valueOf(Helper.Chatlabel()) + "Leaving " + raid.getGatherTown().getName() + " will cause you to leave the raid party.");
                        } else {
                            joiner.sendMessage(String.valueOf(Helper.Chatlabel()) + "You must be in the gathering town to join the raid on " + raid.getRaidedTown().getName() + ".");
                        }
                    } catch (TownyException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    joiner.sendMessage(String.valueOf(Helper.Chatlabel()) + "You cannot join the raid on " + raid.getRaidedTown().getName() + "! It has already started!");
                }
            }
        } else {
            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "No raid is gathering in this town or this town does not exist!");
        }
    }

    protected static void leaveRaid(Player player, String[] args, boolean admin) {
        //Get raid!
        Player p = player;
        if (admin) {
            p = Bukkit.getPlayer(args[3]);
            if (p == null) {
                player.sendMessage(String.valueOf(Helper.Chatlabel()) + "Error: player not found!");
                return;
            }
        }

        Raid raid = RaidData.getRaidOrNull(args[1] + "-" + args[2]);
        if (raid != null) {
            if (raid.getWar().getName().equalsIgnoreCase(args[1])) {
                if (!raid.getWar().getSide1Players().contains(p.getName()) && !raid.getWar().getSide2Players().contains(p.getName())) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You are not in this war! Type /war join [war] [side]");
                    if (admin) player.sendMessage(String.valueOf(Helper.Chatlabel()) + "You are not in this war! Type /war join [war] [side]");
                }

                //check if gather phase
                if (raid.getPhase() == RaidPhase.GATHER || raid.getPhase() == RaidPhase.START) {
                    try {
                        //check if player is in gather town
                        ArrayList<WorldCoord> cluster = Helper.getCluster(raid.getGatherTown().getHomeBlock().getWorldCoord());
                        if (cluster.contains(WorldCoord.parseWorldCoord(p))) {
                            raid.removeActiveRaider(p.getName());
                            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You have left the raid on " + raid.getRaidedTown().getName() + "!");
                            if (admin) player.sendMessage(String.valueOf(Helper.Chatlabel()) + "You have left the raid on " + raid.getRaidedTown().getName() + "!");

                        } else {
                            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You cannot leave the raid on " + raid.getRaidedTown().getName() + "! It has already started!");
                            if (admin) player.sendMessage(String.valueOf(Helper.Chatlabel()) + "You cannot leave the raid on " + raid.getRaidedTown().getName() + "! It has already started!");
                        }
                    } catch (TownyException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else {
            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "No raid is gathering in this town or this town does not exist!");
            if (admin) player.sendMessage(String.valueOf(Helper.Chatlabel()) + "No raid is gathering in this town or this town does not exist!");
        }
    }

    /**
     * Abandons the raid
     *
     * @param p
     * @param args
     */
    private static void abandonRaid(Player p, String[] args) {
        //Get raid
        Raid raid = RaidData.getRaidOrNull(args[1] + "-" + args[2]);
        if (raid != null) {
            if (raid.getWar().getName().equalsIgnoreCase(args[1])) {
                if (!raid.getWar().getSide1Players().contains(p.getName()) && !raid.getWar().getSide2Players().contains(p.getName())) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You are not in this war! Type /war join [war] [side]");
                }

                if (!raid.getOwner().getName().equals(p.getName())) {
                    //check if gather phase
                    if (raid.getPhase() == RaidPhase.GATHER || raid.getPhase() == RaidPhase.START || raid.getPhase() == RaidPhase.TRAVEL || raid.getPhase() == RaidPhase.COMBAT) {
                        if (raid.getGatherTown().hasTownBlock(WorldCoord.parseWorldCoord(p))) {
                            //force defender victory
                            Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The raid on " + raid.getRaidedTown().getName() + " has been abandoned by " + raid.getRaiders());
                            Main.warLogger.log(p.getName() + " abandoned the raid on " + raid.getRaidedTown().getName() + " they started at " + raid.getGatherTown().getName());
                            raid.defendersWin(raid.getRaidScore());
                        }
                    } else {
                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You cannot abandon the raid on " + raid.getRaidedTown().getName() + "! It has a problem!");
                    }
                } else {
                    fail(p, args, "owner");
                }

            }
        } else {
            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "This raid does not exist!");
        }
    }

    private static void listRaids(Player p, String[] args) {
        if (RaidData.getRaids().isEmpty()) {
            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "There are currently no Raids in progress");
            return;
        }
        p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Raids currently in progress:");
        for (final Raid raid : RaidData.getRaids()) {
            p.sendMessage("Name: " + raid.getName());
            p.sendMessage("War: " + raid.getWar().getName());
            p.sendMessage("Owner: " + raid.getOwner().getName());
            p.sendMessage("Raiding: " + raid.getRaidedTown().getName());
            p.sendMessage("Gathering In: " + raid.getGatherTown().getName());
            p.sendMessage("Raiders: " + raid.getRaiders());
            p.sendMessage("Defenders: " + raid.getDefenders());
            p.sendMessage("Raid Score: " + String.valueOf(raid.getRaidScore()));
            p.sendMessage("Raid Phase: " + raid.getPhase().name());
            p.sendMessage("Time Left: " + String.valueOf((RaidPhase.END.startTick - raid.getRaidTicks()) / 1200) + " minutes");
            p.sendMessage("-=-=-=-=-=-=-=-=-=-=-=-");
        }
    }

    private static void raidHelp(Player p, String[] args) {
        if (p.hasPermission("!AlathraWar.admin")) {
            p.sendMessage(Helper.Chatlabel() + "/raid stop [name]");
        }
        p.sendMessage(Helper.Chatlabel() + "/raid start [war] [town]");
        p.sendMessage(Helper.Chatlabel() + "/raid join [war] [town]");
        p.sendMessage(Helper.Chatlabel() + "/raid leave [war] [town]");
        p.sendMessage(Helper.Chatlabel() + "/raid abandon [war] [town]");
        p.sendMessage(Helper.Chatlabel() + "/raid list");
    }

    private static void fail(Player p, String[] args, String type) {
        switch (type) {
            case "permissions": {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("&cYou do not have permission to do this"));
                return;
            }
            case "syntax": {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /raid help");
                return;
            }
            case "noRaids": {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "There are currently no Raids in progress");
                return;
            }
            case "badName": {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "A raid could not be found with this Name! Type /raid list to view current raids");
                return;
            }
            case "owner": {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Only the player who started the raid can abandon it.");
                return;
            }
            default: {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /raid help");

            }
        }
    }
}
