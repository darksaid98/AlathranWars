package me.ShermansWorld.AlathraWar.commands;

import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import me.ShermansWorld.AlathraWar.*;
import me.ShermansWorld.AlathraWar.data.RaidData;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import me.ShermansWorld.AlathraWar.data.SiegeData;
import me.ShermansWorld.AlathraWar.data.WarData;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;

public class RaidCommand {

    public RaidCommand() {
        new CommandAPICommand("raid")
                .withSubcommands(
                        commandRaid(),
                        commandStop(),
                        commandJoin(),
                        commandLeave(),
                        commandAbandon(),
                        commandList(),
                        commandHelp()
                )
                .executesPlayer((sender, args) -> {
                    if (args.count() == 0)
                        throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "Invalid Arguments. /siege help").build());
                })
                .register();
    }

    public static CommandAPICommand commandRaid() {
        return new CommandAPICommand("raid")
                .withArguments(
                        new StringArgument("warname")
                                .replaceSuggestions(
                                        ArgumentSuggestions.strings(
                                                WarData.getWarsNames()
                                        )
                                ),
                        new StringArgument("target")
                                .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                                    final String warname = (String) info.previousArgs().get("warname");
                                                    return CommandHelper.getTownyWarTowns(warname);
                                                }
                                        )
                                ),
                        new StringArgument("gathertown")
                                .setOptional(true)
                                .withPermission("AlathraWar.admin")
                                .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> { // TODO Make getHostileTowns method where we reverse from list
                                                    final String warname = (String) info.previousArgs().get("warname");
                                                    return CommandHelper.getTownyWarTowns(warname);
                                                }
                                        )
                                ),
                        new PlayerArgument("leader")
                                .setOptional(true)
                                .withPermission("AlathraWar.admin"),
                        new BooleanArgument("minutemen")
                                .setOptional(true)
                                .withPermission("AlathraWar.admin")

                )
                .executesPlayer((Player p, CommandArguments args) -> raidStart(p, args, false));
    }

    public static CommandAPICommand commandStop() {
        return new CommandAPICommand("stop")
                .withPermission("AlathraWar.admin")
                .withArguments(
                        new StringArgument("warname")
                                .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info ->
                                                WarData.getWarsNames()
                                        )
                                ),
                        new StringArgument("town")
                                .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                            final String warname = (String) info.previousArgs().get("warname");
                                            return CommandHelper.getTownyWarTowns(warname);
                                        })
                                )
                )
                .executesPlayer(RaidCommand::raidStop);
    }

    public static CommandAPICommand commandJoin() {
        return new CommandAPICommand("join")
                .withArguments(
                        new StringArgument("warname")
                                .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info ->
                                                WarData.getWarsNames()
                                        )
                                ),
                        new StringArgument("town")
                                .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                            final String warname = (String) info.previousArgs().get("warname");
                                            return CommandHelper.getTownyWarTowns(warname);
                                        })
                                ),
                        new PlayerArgument("player")
                                .setOptional(true)
                                .withPermission("AlathraWar.admin"),
                        new StringArgument("side")
                                .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                            String warName = (String) info.previousArgs().get("warname");

                                            War war = WarData.getWar(warName);
                                            if (war != null) {
                                                return war.getSides();
                                            }

                                            return Collections.emptyList();
                                        })
                                ),
                        new BooleanArgument("minutemen")
                                .setOptional(true)
                                .withPermission("AlathraWar.admin")

                )
                .executesPlayer((Player p, CommandArguments args) -> raidJoin(p, args, false));
    }

    public static CommandAPICommand commandLeave() {
        return new CommandAPICommand("leave")
                .withArguments(
                        new StringArgument("warname")
                                .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info ->
                                                WarData.getWarsNames()
                                        )
                                ),
                        new StringArgument("town")
                                .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                            final String warname = (String) info.previousArgs().get("warname");
                                            return CommandHelper.getTownyWarTowns(warname);
                                        })
                                ),
                        new PlayerArgument("player")
                                .setOptional(true)
                                .withPermission("AlathraWar.admin")
                )
                .executesPlayer((Player p, CommandArguments args) -> raidLeave(p, args, false));
    }

    public static CommandAPICommand commandAbandon() {
        return new CommandAPICommand("abandon")
                .withArguments(
                        new StringArgument("warname")
                                .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info ->
                                                WarData.getWarsNames()
                                        )
                                ),
                        new StringArgument("town")
                                .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                            final String warname = (String) info.previousArgs().get("warname");
                                            return CommandHelper.getTownyWarTowns(warname);
                                        })
                                )
                )
                .executesPlayer(RaidCommand::raidAbandon);
    }

    public static CommandAPICommand commandList() {
        return new CommandAPICommand("list")
                .executesPlayer(RaidCommand::raidsList);
    }

    public static CommandAPICommand commandHelp() {
        return new CommandAPICommand("help")
                .executesPlayer(RaidCommand::raidHelp);
    }

    /**
     * Starts a raid
     *
     * @param p
     * @param args
     */
    protected static void raidStart(Player p, CommandArguments args, boolean admin) throws WrapperCommandSyntaxException {
        boolean warFound = false;
        boolean townExists = false;

        if (!(args.get("warname") instanceof final String argWarName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a war name.").build());

        for (final War war : WarData.getWars()) {
            if (war.getName().equalsIgnoreCase(argWarName)) {
                warFound = true;

                final Player raidOwner = (Player) args.getOptional("leader").orElse(p);

                if (raidOwner == null) {
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser("Raid Owner is Null").build());
                }

                TownyWorld townyWorld;
                townyWorld = WorldCoord.parseWorldCoord(raidOwner.getLocation()).getTownyWorld();

                String side;
                if (war.getSide1Players().contains(raidOwner.getName())) {
                    side = war.getSide1();
                } else if (war.getSide2Players().contains(raidOwner.getName())) {
                    side = war.getSide2();
                } else {
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "You are not in this war! Type /war join [war] [side]").build());
                }

                //Minuteman countermeasures
                int minuteman = CommandHelper.isPlayerMinuteman(raidOwner.getName());
                boolean minuteManOverride = (boolean) args.getOptional("minutemen").orElse(false);

                if (minuteManOverride) {
                    p.sendMessage(ChatColor.RED + "Warning! Ignoring Minuteman Countermeasure!");
                }

                //minuteManOverride?
                if (!minuteManOverride) {
                    if (minuteman != 0) {
                        if (minuteman == 1) {
                            //player has joined to recently
                            if (admin)
                                p.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayerAge") + " days from joining.");
                            raidOwner.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayerAge") + " days from joining.");
                            return;
                        } else if (minuteman == 2) {
                            //player has played too little
                            if (admin)
                                p.sendMessage(ChatColor.RED + "You have not played enough! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayTime") + " hours of play.");
                            raidOwner.sendMessage(ChatColor.RED + "You have not played enough! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayTime") + " hours of play.");
                            return;
                        }
                    }
                }

                //check if in a town
                //takes the command runners town as the gather point
                Town gatherTown;
                try {
                    @Nullable String argGatherTown = (String) args.getOptional("gathertown").orElse(null);

                    if (admin && argGatherTown != null) {
                        if (argGatherTown.equalsIgnoreCase("defaultCode")) {
                            //use default behavior
                            gatherTown = TownyAPI.getInstance().getTownOrNull(WorldCoord.parseWorldCoord(raidOwner.getLocation()).getTownBlock());
                        } else if (TownyAPI.getInstance().getTown(argGatherTown) != null) {
                            gatherTown = TownyAPI.getInstance().getTown(argGatherTown);
                        } else {
                            if (admin)
                                p.sendMessage(Helper.chatLabel() + "No valid town set for gather town, defaulting to current location");
                            raidOwner.sendMessage(Helper.chatLabel() + "No valid town set for gather town, defaulting to current location");
                            gatherTown = TownyAPI.getInstance().getTownOrNull(WorldCoord.parseWorldCoord(raidOwner.getLocation()).getTownBlock());
                        }
                    } else {
                        gatherTown = TownyAPI.getInstance().getTownOrNull(WorldCoord.parseWorldCoord(raidOwner.getLocation()).getTownBlock());
                    }
                } catch (NotRegisteredException e2) {
                    if (admin)
                        p.sendMessage(Helper.chatLabel() + "You must be within a town's claim to begin a raid. That town you are in will be the gathering town.");
                    raidOwner.sendMessage(Helper.chatLabel() + "You must be within a town's claim to begin a raid. That town you are in will be the gathering town.");
                    return;
                }
                if (gatherTown == null) {
                    if (admin)
                        p.sendMessage(Helper.chatLabel() + "You must be within a town's claim to begin a raid. That town you are in will be the gathering town.");
                    raidOwner.sendMessage(Helper.chatLabel() + "You must be within a town's claim to begin a raid. That town you are in will be the gathering town.");
                    return;
                }

                //parse the argunment for town
                for (final String entry : townyWorld.getTowns().keySet()) {
                    final Town raidedTown = townyWorld.getTowns().get(entry);

                    //if this is run as admin, shift our check forward a slot
                    if (raidedTown.getName().equalsIgnoreCase((String) args.get("target")) && gatherTown != null) {

                        Main.warLogger.log("Raided town: " + raidedTown.getName());
                        Main.warLogger.log("Gather town: " + gatherTown.getName());
                        Main.warLogger.log("Raid Owner: " + raidOwner.getName());
                        p.sendMessage("Raided town: " + raidedTown.getName());
                        p.sendMessage("Gather town: " + gatherTown.getName());
                        p.sendMessage("Raid Owner: " + raidOwner.getName());
                        //DEBUG PRINT
                        for (String t : war.getSide1Towns()) {
                            Main.warLogger.log("Side1 town: " + t);
                            p.sendMessage("Side1 town: " + t);
                        }
                        for (String t : war.getSide2Towns()) {
                            Main.warLogger.log("Side2 town: " + t);
                            p.sendMessage("Side2 town: " + t);
                        }

                        //check if the town were attempting to raid is in the war
                        if (!war.getSide1Towns().contains(raidedTown.getName().toLowerCase()) && !war.getSide2Towns().contains(raidedTown.getName().toLowerCase())) {
                            if (war.getSurrenderedTowns().contains(raidedTown.getName().toLowerCase())) {
                                if (admin)
                                    p.sendMessage(Helper.chatLabel() + "The town you are trying to raid has already surrendered!");
                                raidOwner.sendMessage(Helper.chatLabel() + "The town you are trying to raid has already surrendered!");
                                return;
                            }
                            if (admin)
                                p.sendMessage(Helper.chatLabel() + "The town you are trying to raid is not part of the war!");
                            raidOwner.sendMessage(Helper.chatLabel() + "The town you are trying to raid is not part of the war!");
                            return;
                        }

                        // Is being sieged check
                        for (Siege s : SiegeData.getSieges()) {
                            if (s.getTown().getName().equalsIgnoreCase(raidedTown.getName())) {
                                raidOwner.sendMessage(Helper.chatLabel() + "That town is already currently being sieged! Cannot raid at this time!");
                                if (admin)
                                    p.sendMessage(Helper.chatLabel() + "That town is already currently being sieged! Cannot raid at this time!");
                            }
                        }

                        townExists = true;

                        Raid raid2;
                        if (war.getSide1Players().contains(raidOwner.getName())) {
                            //Time and raid activity validity check
                            int c = RaidData.isValidRaid(war, side, raidedTown);
                            if (c == 2) {
                                //if were admin, see if an owner arg exists, and if so then use it
                                raid2 = new Raid(war, raidedTown, gatherTown, true, raidOwner);
                            } else if (c == 1) {
                                if (admin)
                                    p.sendMessage(Helper.chatLabel() + "This town is already being raided at this time!");
                                raidOwner.sendMessage(Helper.chatLabel() + "This town is already being raided at this time!");
                                return;
                            } else if (c == 0) {
                                if (admin)
                                    p.sendMessage(Helper.chatLabel() + "Your side has raided too recently!");
                                raidOwner.sendMessage(Helper.chatLabel() + "Your side has raided too recently!");
                                return;
                            } else if (c == -1) {
                                if (admin)
                                    p.sendMessage(Helper.chatLabel() + "This town was raided too recently!");
                                raidOwner.sendMessage(Helper.chatLabel() + "This town was raided too recently!");
                                return;
                            } else if (c == -2) {
                                if (admin)
                                    p.sendMessage(Helper.chatLabel() + "At least on member of the raided town must be online to defend it!");
                                raidOwner.sendMessage(Helper.chatLabel() + "At least on member of the raided town must be online to defend it!");
                                return;
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } else {
                            //Something broke if this runs
                            if (!war.getSide2Players().contains(raidOwner.getName())) {
                                Bukkit.getLogger().info("Unable to find player declaring raid in the war");
                                return;
                            }
                            //Time and raid activity validity check
                            int c = RaidData.isValidRaid(war, side, raidedTown);
                            if (c == 2) {
                                raid2 = new Raid(war, raidedTown, gatherTown, false, raidOwner);
                            } else if (c == 1) {
                                if (admin)
                                    p.sendMessage(Helper.chatLabel() + "This town is already being raided at this time!");
                                raidOwner.sendMessage(Helper.chatLabel() + "This town is already being raided at this time!");
                                return;
                            } else if (c == 0) {
                                if (admin)
                                    p.sendMessage(Helper.chatLabel() + "Your side has raided too recently!");
                                raidOwner.sendMessage(Helper.chatLabel() + "Your side has raided too recently!");
                                return;
                            } else if (c == -1) {
                                if (admin)
                                    p.sendMessage(Helper.chatLabel() + "This town was raided too recently!");
                                raidOwner.sendMessage(Helper.chatLabel() + "This town was raided too recently!");
                                return;
                            } else if (c == -2) {
                                if (admin)
                                    p.sendMessage(Helper.chatLabel() + "At least on member of the raided town must be online to defend it!");
                                raidOwner.sendMessage(Helper.chatLabel() + "At least on member of the raided town must be online to defend it!");
                                return;
                            } else {
                                throw new IllegalArgumentException();
                            }
                        }

                        // Player participance check
                        Town leaderTown = TownyAPI.getInstance().getResident(raidOwner).getTownOrNull();
                        int sideR = war.getSide(leaderTown.getName().toLowerCase());
                        if (sideR == 0) {
                            if (admin) p.sendMessage(Helper.chatLabel() + "You are not in this war.");
                            raidOwner.sendMessage(Helper.chatLabel() + "You are not in this war.");
                            return;
                        } else if (sideR == -1) {
                            if (admin) p.sendMessage(Helper.chatLabel() + "You have surrendered.");
                            raidOwner.sendMessage(Helper.chatLabel() + "You have surrendered.");
                            return;
                        }

                        // Attacking own side
                        if (war.getSide(raidedTown) == sideR) {
                            if (admin) p.sendMessage(Helper.chatLabel() + "You cannot attack your own towns.");
                            raidOwner.sendMessage(Helper.chatLabel() + "You cannot attack your own towns.");
                            return;
                        }

                        //check player balance
                        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(raidOwner.getUniqueId());
                        if (Main.econ.getBalance(offlinePlayer) <= 1000.0) {
                            if (admin)
                                p.sendMessage(Helper.chatLabel() + "You must have at least $1000 to put up to start a raid.");
                            raidOwner.sendMessage(Helper.chatLabel() + "You must have at least $1000 to put up to start a raid.");
                            return;
                        }

                        //publish raid
                        Main.econ.withdrawPlayer(offlinePlayer, 1000);
                        raid2.addActiveRaider(raidOwner.getName());
                        RaidData.addRaid(raid2);
                        raid2.start(); //funny funny method haha
                        WarData.saveWar(raid2.getWar());

                        //broadcast
                        Bukkit.broadcastMessage(Helper.chatLabel() + "As part of " + war.getName() + ", forces from " + raid2.getRaiderSide() + " are gathering to raid the town of " + raidedTown.getName() + "!");
                        Bukkit.broadcastMessage(Helper.chatLabel() + "All players from the defending side have been drafted for the town's defense.");
                        Bukkit.broadcastMessage(Helper.chatLabel() + "The raid of "
                                + raidedTown.getName() + " will begin in " + (RaidPhase.TRAVEL.startTick / 20 / 60) + " minutes!");
                        Bukkit.broadcastMessage(Helper.chatLabel() +
                                "The Raiders are gathering at " + gatherTown.getName() + " before making the journey over!");

                        Main.warLogger.log(raidOwner.getName() + " started a raid.");
                        if (admin) Main.warLogger.log(raidOwner.getName() + " started a raid from admin interference.");
                        Main.warLogger.log("As part of " + war.getName() + ", forces from " + raid2.getRaiderSide() + " are raiding the town of " + raidedTown.getName() + "!");
                        Main.warLogger.log("The Raiders are gathering at " + gatherTown.getName() + " before making the journey over!");
                    }
                }
            }
        }
        if (!warFound) {
            p.sendMessage(Helper.chatLabel() + "That war does not exist! /raid start [war] [town]");
            return;
        }
        if (!townExists) {
            p.sendMessage(Helper.chatLabel() + "That town does not exist! /raid start [war] [town]");
        }
    }

    /**
     * Stops the current raid by admins
     *
     * @param p
     * @param args
     */
    private static void raidStop(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        if (!(args.get("warname") instanceof final String argWarName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a war name!").build());

        if (!(args.get("town") instanceof final String argTown))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a town name!").build());


        //are there even any raids
        if (RaidData.getRaids().isEmpty()) {
            fail(p, args, "noRaids");
        }

        //find our desired raid and stop it
        Raid raid = RaidData.getRaidOrNull(argWarName + "-" + argTown);

        if (raid != null) {
            p.sendMessage(Helper.chatLabel() + "raid cancelled");
            Bukkit.broadcastMessage(Helper.chatLabel() + "The raid of " + raid.getRaidedTown().getName() + " has been cancelled by an admin");
            raid.stop(); //this purges itself from raids
        } else {
            fail(p, args, "badName");
        }
    }

    protected static void raidJoin(Player p, CommandArguments args, boolean admin) throws WrapperCommandSyntaxException {
        if (!(args.get("warname") instanceof final String argWarName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a war name!").build());

        if (!(args.get("town") instanceof final String argTown))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a town name!").build());

        //Grab raid!
        Raid raid = RaidData.getRaidOrNull(argWarName + "-" + argTown);

        if (raid == null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "No raid is gathering in this town or this town does not exist!").build());

        if (raid.getWar().getName().equalsIgnoreCase(argWarName)) {
            final Player joiner = (Player) args.getOptional("player").orElse(p);

            if (joiner == null) {
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser("Joiner is Null").build());
            }

            if (!(args.get("side") instanceof final String argSide))
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a side!").build());

            if (admin) {
                if (argSide != null) {
                    if (argSide.equalsIgnoreCase(raid.getDefenderSide())) {
                        if (!raid.getDefenderPlayers().contains(joiner.getName()))
                            raid.getDefenderPlayers().add(joiner.getName());

                        p.sendMessage(Helper.chatLabel() + "Player " + joiner.getName() + " added to defender side.");
                        Main.warLogger.log("Player " + joiner.getName() + " added to defender side.");
                        joiner.sendMessage(Helper.chatLabel() + "You were forcefully added to the defender side against the raid party on " + raid.getRaidedTown() + " by an admin.");
                        return;
                    } else {
                        if (!raid.getRaiderPlayers().contains(joiner.getName()))
                            raid.getRaiderPlayers().add(joiner.getName());

                        p.sendMessage(Helper.chatLabel() + "Player " + joiner.getName() + " added to raider side.");
                        Main.warLogger.log("Player " + joiner.getName() + " added to raider side.");
                        joiner.sendMessage(Helper.chatLabel() + "You were forcefully added to the raid party on " + raid.getRaidedTown() + " by an admin. Leaving the gather town will remove you from the raid party.");
                    }
                }

            }

            if (!raid.getWar().getSide1Players().contains(joiner.getName()) && !raid.getWar().getSide2Players().contains(joiner.getName())) {
                joiner.sendMessage(Helper.chatLabel() + "You are not in this war! Type /war join [war] [side]");
            }

            //Minutemen countermeasures, 86400 * 4 time. 86400 seconds in a day, 4 days min playtime
            //also 12 hours min platim by default
            int minuteman = CommandHelper.isPlayerMinuteman(joiner.getName());
            boolean minuteManOverride = (boolean) args.getOptional("minutemen").orElse(false);

            if (minuteManOverride) {
                p.sendMessage(ChatColor.RED + "Warning! Ignoring Minuteman Countermeasure!");
            }

            if (!minuteManOverride) {
                if (minuteman != 0) {
                    if (minuteman == 1) {
                        //player has joined to recently
                        if (admin)
                            p.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayerAge") + " days from joining.");
                        joiner.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayerAge") + " days from joining.");
                        return;
                    } else if (minuteman == 2) {
                        //player has played too little
                        if (admin)
                            p.sendMessage(ChatColor.RED + "You have not played enough! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayTime") + " hours of play.");
                        joiner.sendMessage(ChatColor.RED + "You have not played enough! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayTime") + " hours of play.");
                        return;
                    }
                }
            }

            //check if gather phase
            if (!admin) {
                if (raid.getPhase() == RaidPhase.GATHER || raid.getPhase() == RaidPhase.START) {
                    try {
                        //check if the player is in the gather town
                        ArrayList<WorldCoord> cluster = Helper.getCluster(raid.getGatherTown().getHomeBlock().getWorldCoord());
                        if (cluster.contains(WorldCoord.parseWorldCoord(joiner))) {
                            raid.addActiveRaider(joiner.getName());
                            joiner.sendMessage(Helper.chatLabel() + "You have joined the raid on " + raid.getRaidedTown().getName() + "!");
                            joiner.sendMessage(Helper.chatLabel() + "Leaving " + raid.getGatherTown().getName() + " will cause you to leave the raid party.");
                        } else {
                            joiner.sendMessage(Helper.chatLabel() + "You must be in the gathering town to join the raid on " + raid.getRaidedTown().getName() + ".");
                        }
                    } catch (TownyException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    joiner.sendMessage(Helper.chatLabel() + "You cannot join the raid on " + raid.getRaidedTown().getName() + "! It has already started!");
                }
            } else {
                try {
                    //check if the player is in the gather town
                    ArrayList<WorldCoord> cluster = Helper.getCluster(raid.getGatherTown().getHomeBlock().getWorldCoord());
                    if (cluster.contains(WorldCoord.parseWorldCoord(joiner))) {
                        raid.addActiveRaider(joiner.getName());
                        joiner.sendMessage(Helper.chatLabel() + "You have joined the raid on " + raid.getRaidedTown().getName() + "!");
                        joiner.sendMessage(Helper.chatLabel() + "Leaving " + raid.getGatherTown().getName() + " will cause you to leave the raid party.");
                    } else {
                        joiner.sendMessage(Helper.chatLabel() + "You must be in the gathering town to join the raid on " + raid.getRaidedTown().getName() + ".");
                    }
                } catch (TownyException e) {
                    throw new RuntimeException(e);
                }
            }

            raid.save();
        }
    }

    protected static void raidLeave(Player player, CommandArguments args, boolean admin) throws WrapperCommandSyntaxException {
        //Get raid!
        final Player p = (Player) args.getOptional("leader").orElse(player);

        if (p == null) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser("Raided is Null").build());
        }

        if (!(args.get("warname") instanceof final String argWarName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a war name!").build());

        if (!(args.get("town") instanceof final String argTown))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a town name!").build());


        Raid raid = RaidData.getRaidOrNull(argWarName + "-" + argTown);
        if (raid == null) {
            p.sendMessage(Helper.chatLabel() + "No raid is gathering in this town or this town does not exist!");
            if (admin)
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "No raid is gathering in this town or this town does not exist!").build());
        }

        if (raid.getWar().getName().equalsIgnoreCase(argWarName)) {
            if (!raid.getWar().getSide1Players().contains(p.getName()) && !raid.getWar().getSide2Players().contains(p.getName())) {
                p.sendMessage(Helper.chatLabel() + "You are not in this war! Type /war join [war] [side]");
                if (admin)
                    player.sendMessage(Helper.chatLabel() + "You are not in this war! Type /war join [war] [side]");
            }

            //check if gather phase
            if (raid.getPhase() == RaidPhase.GATHER || raid.getPhase() == RaidPhase.START) {
                try {
                    //check if player is in gather town
                    ArrayList<WorldCoord> cluster = Helper.getCluster(raid.getGatherTown().getHomeBlock().getWorldCoord());
                    if (cluster.contains(WorldCoord.parseWorldCoord(p))) {
                        raid.removeActiveRaider(p.getName());
                        p.sendMessage(Helper.chatLabel() + "You have left the raid on " + raid.getRaidedTown().getName() + "!");
                        if (admin)
                            player.sendMessage(Helper.chatLabel() + "You have left the raid on " + raid.getRaidedTown().getName() + "!");

                    } else {
                        p.sendMessage(Helper.chatLabel() + "You cannot leave the raid on " + raid.getRaidedTown().getName() + "! It has already started!");
                        if (admin)
                            player.sendMessage(Helper.chatLabel() + "You cannot leave the raid on " + raid.getRaidedTown().getName() + "! It has already started!");
                    }
                } catch (TownyException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        raid.save();
    }

    /**
     * Abandons the raid
     *
     * @param p
     * @param args
     */
    private static void raidAbandon(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        if (!(args.get("warname") instanceof final String argWarName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a war name!").build());

        if (!(args.get("town") instanceof final String argTown))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a town name!").build());


        //Get raid
        Raid raid = RaidData.getRaidOrNull(argWarName + "-" + argTown);

        if (raid == null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cThis raid does not exist!").build());


        if (raid.getWar().getName().equalsIgnoreCase(argWarName)) {
            if (!raid.getWar().getSide1Players().contains(p.getName()) && !raid.getWar().getSide2Players().contains(p.getName())) {
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou are not in this war! Type /war join [war] [side]").build());
            }

            if (raid.getOwner().getName().equalsIgnoreCase(p.getName())) {
                //check if gather phase
                if (raid.getPhase() == RaidPhase.GATHER || raid.getPhase() == RaidPhase.START || raid.getPhase() == RaidPhase.TRAVEL || raid.getPhase() == RaidPhase.COMBAT) {
                    //force defender victory
                    Bukkit.broadcast(new ColorParser(Helper.chatLabel() + "The raid on " + raid.getRaidedTown().getName() + " has been abandoned by " + raid.getRaiderSide()).build());
                    Main.warLogger.log(p.getName() + " abandoned the raid on " + raid.getRaidedTown().getName() + " they started at " + raid.getGatherTown().getName());
                    raid.defendersWin(raid.getRaiderScore(), raid.getDefenderScore());
                } else {
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou cannot abandon the raid on " + raid.getRaidedTown().getName() + "! It has a problem!").build());
                }
            } else {
                fail(p, args, "owner");
            }
        }
    }

    private static void raidsList(Player p, CommandArguments args) {
        if (RaidData.getRaids().isEmpty()) {
            p.sendMessage(new ColorParser(Helper.chatLabel() + "There are currently no Raids in progress").build());
            return;
        }

        p.sendMessage(new ColorParser(Helper.chatLabel() + "Raids currently in progress:").build());
        for (final Raid raid : RaidData.getRaids()) {
            p.sendMessage(new ColorParser("Name: " + raid.getName()).build());
            p.sendMessage(new ColorParser("War: " + raid.getWar().getName()).build());
            p.sendMessage(new ColorParser("Owner: " + raid.getOwner().getName()).build());
            p.sendMessage(new ColorParser("Raiding: " + raid.getRaidedTown().getName().toLowerCase()).build());
            p.sendMessage(new ColorParser("Gathering In: " + raid.getGatherTown().getName().toLowerCase()).build());
            p.sendMessage(new ColorParser("Raiders: " + raid.getRaiderSide()).build());
            p.sendMessage(new ColorParser("Defenders: " + raid.getDefenderSide()).build());
            p.sendMessage(new ColorParser("Raider Score: " + raid.getRaiderScore()).build());
            p.sendMessage(new ColorParser("Defender Score: " + raid.getDefenderScore()).build());
            p.sendMessage(new ColorParser("Raid Phase: " + raid.getPhase().name()).build());
            p.sendMessage(new ColorParser("Time Left: " + (RaidPhase.END.startTick - raid.getRaidTicks()) / 1200 + " minutes").build());
            p.sendMessage(new ColorParser("-=-=-=-=-=-=-=-=-=-=-=-").build());
        }
    }

    private static void raidHelp(Player p, CommandArguments args) {
        if (p.hasPermission("AlathraWar.admin")) {
            p.sendMessage(new ColorParser(Helper.chatLabel() + "/raid stop [name]").build());
        }
        p.sendMessage(new ColorParser(Helper.chatLabel() + "/raid start [war] [town]").build());
        p.sendMessage(new ColorParser(Helper.chatLabel() + "/raid join [war] [town]").build());
        p.sendMessage(new ColorParser(Helper.chatLabel() + "/raid leave [war] [town]").build());
        p.sendMessage(new ColorParser(Helper.chatLabel() + "/raid abandon [war] [town]").build());
        p.sendMessage(new ColorParser(Helper.chatLabel() + "/raid list").build());
    }

    private static void fail(CommandSender p, CommandArguments args, String type) throws WrapperCommandSyntaxException {
        switch (type) {
            case "permissions" ->
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou do not have permission to do this.").build());
            case "syntax" ->
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cInvalid Syntax. /raid help").build());
            case "noRaids" ->
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cThere are currently no Raids in progress.").build());
            case "badName" ->
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cA raid could not be found with this Name! Type /raid list to view current raids.").build());
            case "owner" ->
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cOnly the player who started the raid can abandon it.").build());
            default ->
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cInvalid Arguments. /raid help").build());
        }
    }
}
