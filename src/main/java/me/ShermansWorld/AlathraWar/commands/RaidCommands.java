package me.ShermansWorld.AlathraWar.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import me.ShermansWorld.AlathraWar.*;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import me.ShermansWorld.AlathraWar.data.SiegeData;
import me.ShermansWorld.AlathraWar.data.WarData;
import me.ShermansWorld.AlathraWar.data.RaidData;
import net.md_5.bungee.api.ChatColor;
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
        }else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("start")) {
                startRaid(p, args, false);
            } else if (args[0].equalsIgnoreCase("stop")) {
                stopRaid(p, args);
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
        return true;
    }

    /**
     * Starts a raid
     *
     * @param p
     * @param args
     */
    protected static void startRaid(CommandSender p, String[] args, boolean admin) {
        boolean warFound = false;
        boolean townExists = false;
        for (final War war : WarData.getWars()) {
            //if this is run as admin, shift our check forward a slot
            if (war.getName().equalsIgnoreCase(args[1 + (admin ? 1 : 0)])) {
                warFound = true;

                Player raidOwner = null;
                if(p instanceof Player) {
                    raidOwner = (Player) p;
                } else {
                    p.sendMessage("Running this from the console requires a Player Argument!");
                    return;
                }
                TownyWorld townyWorld;
                townyWorld = WorldCoord.parseWorldCoord(raidOwner.getLocation()).getTownyWorld();
                if(admin && args.length >= 6) {
                    raidOwner = Bukkit.getPlayer(args[5]);
                    if(raidOwner == null) {
                        p.sendMessage("Player Does not exist");
                        return;
                    }
                }

                String side = "";
                if (war.getSide1Players().contains(p.getName())) {
                    side = war.getSide1();
                } else if (war.getSide2Players().contains(p.getName())) {
                    side = war.getSide2();
                } else {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You are not in this war! Type /war join [war] [side]");
                    return;
                }

                //Minutemen countermeasures, 86400 * 4 time. 86400 seconds in a day, 4 days min playtime
                if (admin) {
                    if (System.currentTimeMillis() - CommandHelper.getPlayerJoinDate(args[3]) < 86400000L * Main.getInstance().getConfig().getInt("minimumPlayerAge") ) {
                        if(args.length >= 5) {
                            //player has joined to recently
                            p.sendMessage(ChatColor.RED + "Warning! Ignoring Minuteman Countermeasure!");
                        } else {
                            //player has joined to recently
                            p.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a war after 4 days from joining.");
                            raidOwner.getPlayer().sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a war after 4 days from joining.");
                            return;
                        }
                    }
                } else {
                    if (System.currentTimeMillis() - CommandHelper.getPlayerJoinDate(p.getName()) < 86400000 * Main.getInstance().getConfig().getInt("minimumPlayerAge") ) {
                        //player has joined to recently
                        p.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a war after 4 days from joining.");
                        return;
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
                            gatherTown = TownyAPI.getInstance().getTownOrNull(WorldCoord.parseWorldCoord(raidOwner.getLocation()).getTownBlock());
                        } else if(TownyAPI.getInstance().getTown(args[4]) != null) {
                            gatherTown = TownyAPI.getInstance().getTown(args[4]);
                        } else {
                            if (admin) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "No valid town set for gather town, defaulting to current location");
                            raidOwner.sendMessage(String.valueOf(Helper.Chatlabel()) + "No valid town set for gather town, defaulting to current location");
                            gatherTown = TownyAPI.getInstance().getTownOrNull(WorldCoord.parseWorldCoord(raidOwner.getLocation()).getTownBlock());
                        }
                    } else {
                        gatherTown = TownyAPI.getInstance().getTownOrNull(WorldCoord.parseWorldCoord(raidOwner.getLocation()).getTownBlock());
                    }
                } catch (NotRegisteredException e2) {
                    if (admin) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You must be within a town's claim to begin a raid. That town you are in will be the gathering town.");
                    raidOwner.sendMessage(String.valueOf(Helper.Chatlabel()) + "You must be within a town's claim to begin a raid. That town you are in will be the gathering town.");
                    return;
                }
                if (gatherTown == null) {
                    if (admin) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You must be within a town's claim to begin a raid. That town you are in will be the gathering town.");
                    raidOwner.sendMessage(String.valueOf(Helper.Chatlabel()) + "You must be within a town's claim to begin a raid. That town you are in will be the gathering town.");
                    return;
                }

                //parse the argunment for town
                for (final String entry : townyWorld.getTowns().keySet()) {
                    final Town raidedTown = townyWorld.getTowns().get(entry);

                    //if this is run as admin, shift our check forward a slot
                    if (raidedTown.getName().equalsIgnoreCase(args[2 + (admin ? 1 : 0)]) && gatherTown != null) {

                        //check if the town were attempting to raid is in the war
                        if(!war.getSide1Towns().contains(raidedTown.getName()) && !war.getSide2Towns().contains(raidedTown.getName())) {
                            if (war.getSurrenderedTowns().contains(raidedTown.getName())) {
                                if (admin) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "The town you are trying to raid has already surrendered!");
                                raidOwner.sendMessage(String.valueOf(Helper.Chatlabel()) + "The town you are trying to raid has already surrendered!");
                                return;
                            }
                            if (admin) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "The town you are trying to raid is not part of the war!");
                            raidOwner.sendMessage(String.valueOf(Helper.Chatlabel()) + "The town you are trying to raid is not part of the war!");
                            return;
                        }

                        // Is being sieged check
                        for (Siege s : SiegeData.getSieges()) {
                            if(s.getTown().getName().equals(raidedTown.getName())) {
                                raidOwner.sendMessage(String.valueOf(Helper.Chatlabel()) + "That town is already currently being sieged! Cannot raid at this time!");
                                if(admin) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "That town is already currently being sieged! Cannot raid at this time!");
                            }
                        }

                        townExists = true;

                        Raid raid2;
                        if (war.getSide1Players().contains(p.getName())) {
                            //Time and raid activity validity check
                            int c = RaidData.isValidRaid(war, side, raidedTown);
                            if (c == 2) {
                                //if were admin, see if an owner arg exists, and if so then use it
                                raid2 = new Raid(war, raidedTown, gatherTown, true, raidOwner);
                            } else if (c == 1) {
                                if (admin) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "This town is already being raided at this time!");
                                raidOwner.sendMessage(String.valueOf(Helper.Chatlabel()) + "This town is already being raided at this time!");
                                return;
                            } else if (c == 0) {
                                if (admin) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Your side has raided too recently!");
                                raidOwner.sendMessage(String.valueOf(Helper.Chatlabel()) + "Your side has raided too recently!");
                                return;
                            } else if (c == -1) {
                                if (admin) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "This town was raided too recently!");
                                raidOwner.sendMessage(String.valueOf(Helper.Chatlabel()) + "This town was raided too recently!");
                                return;
                            } else if (c == -2) {
                                if (admin) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "At least on member of the raided town must be online to defend it!");
                                raidOwner.sendMessage(String.valueOf(Helper.Chatlabel()) + "At least on member of the raided town must be online to defend it!");
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
                            int c = RaidData.isValidRaid(war, side, raidedTown);
                            if (c == 2) {
                                raid2 = new Raid(war, raidedTown, gatherTown, false, raidOwner);
                            } else if (c == 1) {
                                if (admin) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "This town is already being raided at this time!");
                                raidOwner.sendMessage(String.valueOf(Helper.Chatlabel()) + "This town is already being raided at this time!");
                                return;
                            } else if (c == 0) {
                                if (admin) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Your side has raided too recently!");
                                raidOwner.sendMessage(String.valueOf(Helper.Chatlabel()) + "Your side has raided too recently!");
                                return;
                            } else if (c == -1) {
                                if (admin) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "This town was raided too recently!");
                                raidOwner.sendMessage(String.valueOf(Helper.Chatlabel()) + "This town was raided too recently!");
                                return;
                            } else if (c == -2) {
                                if (admin) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "At least on member of the raided town must be online to defend it!");
                                raidOwner.sendMessage(String.valueOf(Helper.Chatlabel()) + "At least on member of the raided town must be online to defend it!");
                                return;
                            }  else {
                                throw new IllegalArgumentException();
                            }
                        }

                        // Player participance check
                        Town leaderTown = TownyAPI.getInstance().getResident(raidOwner).getTownOrNull();
                        int sideR = war.getSide(leaderTown.getName());
                        if (sideR == 0) {
                            if (admin) p.sendMessage(Helper.Chatlabel() + "You are not in this war.");
                            raidOwner.sendMessage(Helper.Chatlabel() + "You are not in this war.");
                            return;
                        } else if (sideR == -1) {
                            if (admin) p.sendMessage(Helper.Chatlabel() + "You have surrendered.");
                            raidOwner.sendMessage(Helper.Chatlabel() + "You have surrendered.");
                            return;
                        }

                        // Attacking own side
                        if (war.getSide(raidedTown) == sideR) {
                            if (admin) p.sendMessage(Helper.Chatlabel() + "You cannot attack your own towns.");
                            raidOwner.sendMessage(Helper.Chatlabel() + "You cannot attack your own towns.");
                            return;
                        }

                        //check player balance
                        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(raidOwner.getUniqueId());
                        if (Main.econ.getBalance(offlinePlayer) <= 1000.0) {
                            if (admin) p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You must have at least $1000 to put up to start a raid.");
                            raidOwner.sendMessage(String.valueOf(Helper.Chatlabel()) + "You must have at least $1000 to put up to start a raid.");
                            return;
                        }

                        //publish raid
                        Main.econ.withdrawPlayer(offlinePlayer, 1000);
                        raid2.addActiveRaider(raidOwner.getName());
                        RaidData.addRaid(raid2);
                        raid2.start(); //funny funny method haha
                        WarData.saveWar(raid2.getWar());

                        //broadcast
                        Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "As part of " + war.getName() + ", forces from " + raid2.getRaiderSide() + " are gathering to raid the town of " + raidedTown.getName() + "!");
                        Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "All players from the defending side have been drafted for the town's defense.");
                        Bukkit.broadcastMessage(Helper.Chatlabel() + "The raid of "
                                + raidedTown.getName() + " will begin in " + (int) (RaidPhase.TRAVEL.startTick / 20 / 60) + " minutes!");
                        Bukkit.broadcastMessage(Helper.Chatlabel() +
                                "The Raiders are gathering at " + gatherTown.getName() + " before making the journey over!");

                        Main.warLogger.log(raidOwner.getName() + " started a raid.");
                        if(admin) Main.warLogger.log(raidOwner.getName() + " started a raid from admin interference.");
                        Main.warLogger.log("As part of " + war.getName() + ", forces from " + raid2.getRaiderSide() + " are raiding the town of " + raidedTown.getName() + "!");
                        Main.warLogger.log("The Raiders are gathering at " + gatherTown.getName() + " before making the journey over!");
                    }
                }
            }
        }
        if (!warFound) {
            p.sendMessage(Helper.Chatlabel() + "That war does not exist! /raid start [war] [town]");
            return;
        }
        if (!townExists) {
            p.sendMessage(Helper.Chatlabel() + "That town does not exist! /raid start [war] [town]");
        }
    }

    /**
     * Stops the current raid by admins
     *
     * @param p
     * @param args
     */
    private static void stopRaid(CommandSender p, String[] args) {
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

        //find our desired raid and stop it
        Raid raid = RaidData.getRaidOrNull(args[1] + "-" + args[2]);

        if (raid != null) {
            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "raid cancelled");
            Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The raid of " + raid.getRaidedTown().getName() + " has been cancelled by an admin");
            raid.stop(); //this purges itself from raids
        } else {
            fail(p, args, "badName");
        }
    }

    protected static void joinRaid(CommandSender p, String[] args, boolean admin) {
        //Grab raid!
        Raid raid = RaidData.getRaidOrNull(args[1] + "-" + args[2]);
        if (raid != null) {
            if (raid.getWar().getName().equalsIgnoreCase(args[1])) {
                Player joiner = null;
                if(p instanceof Player) {
                    joiner = (Player) p;
                }

                if (admin) {
                    joiner = Bukkit.getPlayer(args[3]);

                    if(joiner != null) {
                        if (args[4] != null) {
                            if (args[4].equals(raid.getDefenderSide())) {
                                if (!raid.getDefenderPlayers().contains(joiner.getName()))
                                    raid.getDefenderPlayers().add(joiner.getName());
                                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Player " + joiner.getName() + " added to defender side.");
                                Main.warLogger.log("Player " + joiner.getName() + " added to defender side.");
                                joiner.sendMessage(String.valueOf(Helper.Chatlabel()) + "You were forcefully added to the defender side against the raid party on " + raid.getRaidedTown() + " by an admin.");
                                return;
                            } else {
                                if (!raid.getRaiderPlayers().contains(joiner.getName()))
                                    raid.getRaiderPlayers().add(joiner.getName());
                                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Player " + joiner.getName() + " added to raider side.");
                                Main.warLogger.log("Player " + joiner.getName() + " added to raider side.");
                                joiner.sendMessage(String.valueOf(Helper.Chatlabel()) + "You were forcefully added to the raid party on " + raid.getRaidedTown() + " by an admin. Leaving the gather town will remove you from the raid party.");
                            }
                        }
                    } else {
                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Player " + args[3] + " not found!");
                        return;
                    }
                }

                if(joiner == null) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Player not found! Is something broken?");
                    return;
                }

                if (!raid.getWar().getSide1Players().contains(joiner.getName()) && !raid.getWar().getSide2Players().contains(joiner.getName())) {
                    joiner.sendMessage(String.valueOf(Helper.Chatlabel()) + "You are not in this war! Type /war join [war] [side]");
                }

                //Minutemen countermeasures, 86400 * 4 time. 86400 seconds in a day, 4 days min playtime
                if (admin) {
                    if (System.currentTimeMillis() - CommandHelper.getPlayerJoinDate(args[3]) < 86400000L * Main.getInstance().getConfig().getInt("minimumPlayerAge") ) {
                        if(args.length >= 6) {
                            if (Boolean.parseBoolean(args[5])) {
                                //player has joined to recently
                                p.sendMessage(ChatColor.RED + "Warning! Ignoring Minuteman Countermeasure!");
                            } else {
                                //player has joined to recently
                                p.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a war after 4 days from joining.");
                                joiner.getPlayer().sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a war after 4 days from joining.");
                                return;
                            }
                        } else {
                            //player has joined to recently
                            p.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a war after 4 days from joining.");
                            joiner.getPlayer().sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a war after 4 days from joining.");
                            return;
                        }
                    }
                } else {
                    if (System.currentTimeMillis() - CommandHelper.getPlayerJoinDate(p.getName()) < 86400000L * Main.getInstance().getConfig().getInt("minimumPlayerAge") ) {
                        //player has joined to recently
                        p.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a war after 4 days from joining.");
                        return;
                    }
                }

                //check if gather phase
                if(!admin)  {
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
                } else {
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
                }

                raid.save();
            }
        } else {
            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "No raid is gathering in this town or this town does not exist!");
        }
    }

    protected static void leaveRaid(CommandSender player, String[] args, boolean admin) {
        //Get raid!
        Player p = null;
        if (admin) {
            p = Bukkit.getPlayer(args[3]);
            if (p == null) {
                player.sendMessage(String.valueOf(Helper.Chatlabel()) + "Error: player not found!");
                return;
            }
        }
        if(p == null) {
            p = (Player) player;
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

            raid.save();
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
    private static void abandonRaid(CommandSender p, String[] args) {
        //Get raid
        Raid raid = RaidData.getRaidOrNull(args[1] + "-" + args[2]);
        if (raid != null) {
            if (raid.getWar().getName().equalsIgnoreCase(args[1])) {
                if (!raid.getWar().getSide1Players().contains(p.getName()) && !raid.getWar().getSide2Players().contains(p.getName())) {
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You are not in this war! Type /war join [war] [side]");
                    return;
                }

                if (raid.getOwner().getName().equals(p.getName())) {
                    //check if gather phase
                    if (raid.getPhase() == RaidPhase.GATHER || raid.getPhase() == RaidPhase.START || raid.getPhase() == RaidPhase.TRAVEL || raid.getPhase() == RaidPhase.COMBAT) {
                        //force defender victory
                        Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The raid on " + raid.getRaidedTown().getName() + " has been abandoned by " + raid.getRaiderSide());
                        Main.warLogger.log(p.getName() + " abandoned the raid on " + raid.getRaidedTown().getName() + " they started at " + raid.getGatherTown().getName());
                        raid.defendersWin(raid.getRaiderScore(), raid.getDefenderScore());
                    } else {
                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You cannot abandon the raid on " + raid.getRaidedTown().getName() + "! It has a problem!");
                    }
                } else {
                    fail(p, args, "owner");
                }

            }
        }
        p.sendMessage(String.valueOf(Helper.Chatlabel()) + "This raid does not exist!");
    }

    private static void listRaids(CommandSender p, String[] args) {
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
            p.sendMessage("Raiders: " + raid.getRaiderSide());
            p.sendMessage("Defenders: " + raid.getDefenderSide());
            p.sendMessage("Raider Score: " + raid.getRaiderScore());
            p.sendMessage("Defender Score: " + raid.getDefenderScore());
            p.sendMessage("Raid Phase: " + raid.getPhase().name());
            p.sendMessage("Time Left: " + (RaidPhase.END.startTick - raid.getRaidTicks()) / 1200 + " minutes");
            p.sendMessage("-=-=-=-=-=-=-=-=-=-=-=-");
        }
    }

    private static void raidHelp(CommandSender p, String[] args) {
        if (p.hasPermission("!AlathraWar.admin")) {
            p.sendMessage(Helper.Chatlabel() + "/raid stop [name]");
        }
        p.sendMessage(Helper.Chatlabel() + "/raid start [war] [town]");
        p.sendMessage(Helper.Chatlabel() + "/raid join [war] [town]");
        p.sendMessage(Helper.Chatlabel() + "/raid leave [war] [town]");
        p.sendMessage(Helper.Chatlabel() + "/raid abandon [war] [town]");
        p.sendMessage(Helper.Chatlabel() + "/raid list");
    }

    private static void fail(CommandSender p, String[] args, String type) {
        switch (type) {
            case "permissions" -> p.sendMessage(String.valueOf(Helper.Chatlabel()) + "&cYou do not have permission to do this");
            case "syntax" -> p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Syntax. /raid help");
            case "noRaids" -> p.sendMessage(String.valueOf(Helper.Chatlabel()) + "There are currently no Raids in progress");
            case "badName" -> p.sendMessage(String.valueOf(Helper.Chatlabel()) + "A raid could not be found with this Name! Type /raid list to view current raids");
            case "owner" -> p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Only the player who started the raid can abandon it.");
            default -> p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /raid help");
        }
    }
}
