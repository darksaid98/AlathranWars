package me.ShermansWorld.AlathranWars.commands;

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
import me.ShermansWorld.AlathranWars.*;
import me.ShermansWorld.AlathranWars.data.RaidData;
import me.ShermansWorld.AlathranWars.data.RaidPhase;
import me.ShermansWorld.AlathranWars.data.SiegeData;
import me.ShermansWorld.AlathranWars.data.WarData;
import me.ShermansWorld.AlathranWars.deprecated.CommandHelper;
import me.ShermansWorld.AlathranWars.deprecated.OldRaid;
import me.ShermansWorld.AlathranWars.deprecated.OldSiege;
import me.ShermansWorld.AlathranWars.deprecated.OldWar;
import me.ShermansWorld.AlathranWars.enums.TownWarState;
import me.ShermansWorld.AlathranWars.utility.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "Invalid Arguments. /siege help").build());
            })
            .register();
    }

    public static CommandAPICommand commandRaid() {
        return new CommandAPICommand("raid")
            .withArguments(
                new StringArgument("war")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(
                            WarData.getWarsNames()
                        )
                    ),
                new StringArgument("town")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> {
                                final String warname = (String) info.previousArgs().get("war");
                                return CommandHelper.getTownyWarTowns(warname);
                            }
                        )
                    ),
                new StringArgument("gathertown")
                    .setOptional(true)
                    .withPermission("AlathranWars.admin")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> { // TODO Make getHostileTowns method where we reverse from list
                                final String warname = (String) info.previousArgs().get("war");
                                return CommandHelper.getTownyWarTowns(warname);
                            }
                        )
                    ),
                new PlayerArgument("leader")
                    .setOptional(true)
                    .withPermission("AlathranWars.admin"),
                new BooleanArgument("minutemen")
                    .setOptional(true)
                    .withPermission("AlathranWars.admin")

            )
            .executesPlayer((Player p, CommandArguments args) -> raidStart(p, args, false));
    }

    public static CommandAPICommand commandStop() {
        return new CommandAPICommand("stop")
            .withPermission("AlathranWars.admin")
            .withArguments(
                new StringArgument("war")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info ->
                            WarData.getWarsNames()
                        )
                    ),
                new StringArgument("town")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> {
                            final String warname = (String) info.previousArgs().get("war");
                            return CommandHelper.getTownyWarTowns(warname);
                        })
                    )
            )
            .executesPlayer(RaidCommand::raidStop);
    }

    public static CommandAPICommand commandJoin() {
        return new CommandAPICommand("join")
            .withArguments(
                new StringArgument("raid")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(
                            CommandHelper.getRaids()
                        )
                    ),
                new StringArgument("side")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> {
                            final String raidname = (String) info.previousArgs().get("oldRaid");

                            final OldRaid oldRaid = RaidData.getRaid(raidname);

                            if (oldRaid == null) return Collections.emptyList();

                            return List.of(oldRaid.getRaiderSide(), oldRaid.getDefenderSide());
                        })
                    ),
                new PlayerArgument("player")
                    .setOptional(true)
                    .withPermission("AlathranWars.admin"),
                new BooleanArgument("minutemen")
                    .setOptional(true)
                    .withPermission("AlathranWars.admin")

            )
            .executesPlayer((Player p, CommandArguments args) -> raidJoin(p, args, false));
    }

    public static CommandAPICommand commandLeave() {
        return new CommandAPICommand("leave")
            .withArguments(
                new StringArgument("war")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info ->
                            WarData.getWarsNames()
                        )
                    ),
                new StringArgument("town")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> {
                            final String warname = (String) info.previousArgs().get("war");
                            return CommandHelper.getTownyWarTowns(warname);
                        })
                    ),
                new PlayerArgument("player")
                    .setOptional(true)
                    .withPermission("AlathranWars.admin")
            )
            .executesPlayer((Player p, CommandArguments args) -> raidLeave(p, args, false));
    }

    public static CommandAPICommand commandAbandon() {
        return new CommandAPICommand("abandon")
            .withArguments(
                new StringArgument("war")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info ->
                            WarData.getWarsNames()
                        )
                    ),
                new StringArgument("town")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> {
                            final String warname = (String) info.previousArgs().get("war");
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

        if (!(args.get("war") instanceof final String argWarName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cUsage: /alathranwarsadmin create raid [war] [raidTown] (gatherTown/\"defaultCode\") (owner) (override)").build());

        for (final OldWar oldWar : WarData.getWars()) {
            if (oldWar.getName().equalsIgnoreCase(argWarName)) {
                warFound = true;

                final Player raidOwner = (Player) args.getOptional("leader").orElse(p);

                if (raidOwner == null) {
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser("OldRaid Owner is Null").build());
                }

                TownyWorld townyWorld;
                townyWorld = WorldCoord.parseWorldCoord(raidOwner.getLocation()).getTownyWorld();

                String side;
                if (oldWar.getSide1Players().contains(raidOwner.getName())) {
                    side = oldWar.getSide1();
                } else if (oldWar.getSide2Players().contains(raidOwner.getName())) {
                    side = oldWar.getSide2();
                } else {
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "You are not in this oldWar! Type /oldWar join [oldWar] [side]").build());
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
                                p.sendMessage(UtilsChat.getPrefix() + "No valid town set for gather town, defaulting to current location");
                            raidOwner.sendMessage(UtilsChat.getPrefix() + "No valid town set for gather town, defaulting to current location");
                            gatherTown = TownyAPI.getInstance().getTownOrNull(WorldCoord.parseWorldCoord(raidOwner.getLocation()).getTownBlock());
                        }
                    } else {
                        gatherTown = TownyAPI.getInstance().getTownOrNull(WorldCoord.parseWorldCoord(raidOwner.getLocation()).getTownBlock());
                    }
                } catch (NotRegisteredException e2) {
                    if (admin)
                        p.sendMessage(UtilsChat.getPrefix() + "You must be within a town's claim to begin a raid. That town you are in will be the gathering town.");
                    raidOwner.sendMessage(UtilsChat.getPrefix() + "You must be within a town's claim to begin a raid. That town you are in will be the gathering town.");
                    return;
                }
                if (gatherTown == null) {
                    if (admin)
                        p.sendMessage(UtilsChat.getPrefix() + "You must be within a town's claim to begin a raid. That town you are in will be the gathering town.");
                    raidOwner.sendMessage(UtilsChat.getPrefix() + "You must be within a town's claim to begin a raid. That town you are in will be the gathering town.");
                    return;
                }

                //parse the argunment for town
                for (final String entry : townyWorld.getTowns().keySet()) {
                    final Town raidedTown = townyWorld.getTowns().get(entry);

                    //if this is run as admin, shift our check forward a slot
                    if (raidedTown.getName().equalsIgnoreCase((String) args.get("town")) && gatherTown != null) {

                        Main.warLogger.log("Raided town: " + raidedTown.getName());
                        Main.warLogger.log("Gather town: " + gatherTown.getName());
                        Main.warLogger.log("OldRaid Owner: " + raidOwner.getName());
                        p.sendMessage("Raided town: " + raidedTown.getName());
                        p.sendMessage("Gather town: " + gatherTown.getName());
                        p.sendMessage("OldRaid Owner: " + raidOwner.getName());
                        //DEBUG PRINT
                        for (String t : oldWar.getSide1Towns()) {
                            Main.warLogger.log("Side1 town: " + t);
                            p.sendMessage("Side1 town: " + t);
                        }
                        for (String t : oldWar.getSide2Towns()) {
                            Main.warLogger.log("Side2 town: " + t);
                            p.sendMessage("Side2 town: " + t);
                        }

                        //check if the town were attempting to raid is in the oldWar
                        if (!oldWar.getSide1Towns().contains(raidedTown.getName().toLowerCase()) && !oldWar.getSide2Towns().contains(raidedTown.getName().toLowerCase())) {
                            if (oldWar.getSurrenderedTowns().contains(raidedTown.getName().toLowerCase())) {
                                if (admin)
                                    p.sendMessage(UtilsChat.getPrefix() + "The town you are trying to raid has already surrendered!");
                                raidOwner.sendMessage(UtilsChat.getPrefix() + "The town you are trying to raid has already surrendered!");
                                return;
                            }
                            if (admin)
                                p.sendMessage(UtilsChat.getPrefix() + "The town you are trying to raid is not part of the oldWar!");
                            raidOwner.sendMessage(UtilsChat.getPrefix() + "The town you are trying to raid is not part of the oldWar!");
                            return;
                        }

                        // Is being sieged check
                        for (OldSiege s : SiegeData.getSieges()) {
                            if (s.getTown().getName().equalsIgnoreCase(raidedTown.getName())) {
                                raidOwner.sendMessage(UtilsChat.getPrefix() + "That town is already currently being sieged! Cannot raid at this time!");
                                if (admin)
                                    p.sendMessage(UtilsChat.getPrefix() + "That town is already currently being sieged! Cannot raid at this time!");
                            }
                        }

                        townExists = true;

                        OldRaid oldRaid2;
                        if (oldWar.getSide1Players().contains(raidOwner.getName())) {
                            //Time and raid activity validity check
                            int c = RaidData.isValidRaid(oldWar, side, raidedTown);
                            if (c == 2) {
                                //if were admin, see if an owner arg exists, and if so then use it
                                oldRaid2 = new OldRaid(oldWar, raidedTown, gatherTown, true, raidOwner);
                            } else if (c == 1) {
                                if (admin)
                                    p.sendMessage(UtilsChat.getPrefix() + "This town is already being raided at this time!");
                                raidOwner.sendMessage(UtilsChat.getPrefix() + "This town is already being raided at this time!");
                                return;
                            } else if (c == 0) {
                                if (admin)
                                    p.sendMessage(UtilsChat.getPrefix() + "Your side has raided too recently!");
                                raidOwner.sendMessage(UtilsChat.getPrefix() + "Your side has raided too recently!");
                                return;
                            } else if (c == -1) {
                                if (admin)
                                    p.sendMessage(UtilsChat.getPrefix() + "This town was raided too recently!");
                                raidOwner.sendMessage(UtilsChat.getPrefix() + "This town was raided too recently!");
                                return;
                            } else if (c == -2) {
                                if (admin)
                                    p.sendMessage(UtilsChat.getPrefix() + "At least on member of the raided town must be online to defend it!");
                                raidOwner.sendMessage(UtilsChat.getPrefix() + "At least on member of the raided town must be online to defend it!");
                                return;
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } else {
                            //Something broke if this runs
                            if (!oldWar.getSide2Players().contains(raidOwner.getName())) {
                                Bukkit.getLogger().info("Unable to find player declaring raid in the oldWar");
                                return;
                            }
                            //Time and raid activity validity check
                            int c = RaidData.isValidRaid(oldWar, side, raidedTown);
                            if (c == 2) {
                                oldRaid2 = new OldRaid(oldWar, raidedTown, gatherTown, false, raidOwner);
                            } else if (c == 1) {
                                if (admin)
                                    p.sendMessage(UtilsChat.getPrefix() + "This town is already being raided at this time!");
                                raidOwner.sendMessage(UtilsChat.getPrefix() + "This town is already being raided at this time!");
                                return;
                            } else if (c == 0) {
                                if (admin)
                                    p.sendMessage(UtilsChat.getPrefix() + "Your side has raided too recently!");
                                raidOwner.sendMessage(UtilsChat.getPrefix() + "Your side has raided too recently!");
                                return;
                            } else if (c == -1) {
                                if (admin)
                                    p.sendMessage(UtilsChat.getPrefix() + "This town was raided too recently!");
                                raidOwner.sendMessage(UtilsChat.getPrefix() + "This town was raided too recently!");
                                return;
                            } else if (c == -2) {
                                if (admin)
                                    p.sendMessage(UtilsChat.getPrefix() + "At least on member of the raided town must be online to defend it!");
                                raidOwner.sendMessage(UtilsChat.getPrefix() + "At least on member of the raided town must be online to defend it!");
                                return;
                            } else {
                                throw new IllegalArgumentException();
                            }
                        }

                        // Player participance check
                        Town leaderTown = TownyAPI.getInstance().getResident(raidOwner).getTownOrNull();
                        TownWarState sideR = oldWar.getState(leaderTown.getName().toLowerCase());
                        if (sideR == TownWarState.NOT_PARTICIPANT) {
                            if (admin) p.sendMessage(UtilsChat.getPrefix() + "You are not in this oldWar.");
                            raidOwner.sendMessage(UtilsChat.getPrefix() + "You are not in this oldWar.");
                            return;
                        } else if (sideR == TownWarState.SURRENDERED) {
                            if (admin) p.sendMessage(UtilsChat.getPrefix() + "You have surrendered.");
                            raidOwner.sendMessage(UtilsChat.getPrefix() + "You have surrendered.");
                            return;
                        }

                        // Attacking own side
                        if (oldWar.getState(raidedTown) == sideR) {
                            if (admin) p.sendMessage(UtilsChat.getPrefix() + "You cannot attack your own towns.");
                            raidOwner.sendMessage(UtilsChat.getPrefix() + "You cannot attack your own towns.");
                            return;
                        }

                        //check player balance
                        final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(raidOwner.getUniqueId());
                        if (Main.econ.getBalance(offlinePlayer) <= 1000.0) {
                            if (admin)
                                p.sendMessage(UtilsChat.getPrefix() + "You must have at least $1000 to put up to start a raid.");
                            raidOwner.sendMessage(UtilsChat.getPrefix() + "You must have at least $1000 to put up to start a raid.");
                            return;
                        }

                        //publish raid
                        Main.econ.withdrawPlayer(offlinePlayer, 1000);
                        oldRaid2.addActiveRaider(raidOwner.getName());
                        RaidData.addRaid(oldRaid2);
                        oldRaid2.start(); //funny funny method haha
                        WarData.saveWar(oldRaid2.getWar());

                        //broadcast
                        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "As part of " + oldWar.getName() + ", forces from " + oldRaid2.getRaiderSide() + " are gathering to raid the town of " + raidedTown.getName() + "!");
                        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "All players from the defending side have been drafted for the town's defense.");
                        Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The raid of "
                            + raidedTown.getName() + " will begin in " + (RaidPhase.TRAVEL.startTick / 20 / 60) + " minutes!");
                        Bukkit.broadcastMessage(UtilsChat.getPrefix() +
                            "The Raiders are gathering at " + gatherTown.getName() + " before making the journey over!");

                        Main.warLogger.log(raidOwner.getName() + " started a raid.");
                        if (admin) {
                            Main.warLogger.log(raidOwner.getName() + " started a raid from admin interference.");
                            p.sendMessage(Helper.color("&cForcefully started raid from the console."));
                        }
                        Main.warLogger.log("As part of " + oldWar.getName() + ", forces from " + oldRaid2.getRaiderSide() + " are raiding the town of " + raidedTown.getName() + "!");
                        Main.warLogger.log("The Raiders are gathering at " + gatherTown.getName() + " before making the journey over!");
                    }
                }
            }
        }
        if (!warFound) {
            p.sendMessage(UtilsChat.getPrefix() + "That war does not exist! /raid start [war] [town]");
            return;
        }
        if (!townExists) {
            p.sendMessage(UtilsChat.getPrefix() + "That town does not exist! /raid start [war] [town]");
        }
    }

    /**
     * Stops the current raid by admins
     *
     * @param p
     * @param args
     */
    private static void raidStop(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        if (!(args.get("war") instanceof final String argWarName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cYou need to specify a war name!").build());

        if (!(args.get("town") instanceof final String argTown))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cYou need to specify a town name!").build());


        //are there even any raids
        if (RaidData.getRaids().isEmpty()) {
            fail(p, args, "noRaids");
        }

        //find our desired oldRaid and stop it
        OldRaid oldRaid = RaidData.getRaid(argWarName + "-" + argTown);

        if (oldRaid != null) {
            p.sendMessage(UtilsChat.getPrefix() + "oldRaid cancelled");
            Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The oldRaid of " + oldRaid.getRaidedTown().getName() + " has been cancelled by an admin");
            oldRaid.stop(); //this purges itself from raids
        } else {
            fail(p, args, "badName");
        }
    }

    protected static void raidJoin(Player p, CommandArguments args, boolean admin) throws WrapperCommandSyntaxException {
        if (!(args.get("oldRaid") instanceof final String argRaidName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cYou need to specify a oldRaid name!").build());

        if (!(args.get("side") instanceof final String argSide))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cYou need to specify a side!").build());

        final OldRaid oldRaid = RaidData.getRaid(argRaidName);
        if (oldRaid == null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThat oldRaid does not exist!").build());

        final OldWar oldWar = oldRaid.getWar();
        if (oldWar == null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe oldWar does not exist!").build());

        final Player joiner = (Player) args.getOptional("player").orElse(p);
        if (joiner == null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser("Joiner is Null").build());


        if (admin) {
            if (argSide != null) {
                if (argSide.equalsIgnoreCase(oldRaid.getDefenderSide())) {
                    if (!oldRaid.getDefenderPlayers().contains(joiner.getName()))
                        oldRaid.getDefenderPlayers().add(joiner.getName());

                    p.sendMessage(UtilsChat.getPrefix() + "Player " + joiner.getName() + " added to defender side.");
                    Main.warLogger.log("Player " + joiner.getName() + " added to defender side.");
                    joiner.sendMessage(UtilsChat.getPrefix() + "You were forcefully added to the defender side against the oldRaid party on " + oldRaid.getRaidedTown() + " by an admin.");
                    return;
                } else {
                    if (!oldRaid.getRaiderPlayers().contains(joiner.getName()))
                        oldRaid.getRaiderPlayers().add(joiner.getName());

                    p.sendMessage(UtilsChat.getPrefix() + "Player " + joiner.getName() + " added to raider side.");
                    Main.warLogger.log("Player " + joiner.getName() + " added to raider side.");
                    joiner.sendMessage(UtilsChat.getPrefix() + "You were forcefully added to the oldRaid party on " + oldRaid.getRaidedTown() + " by an admin. Leaving the gather town will remove you from the oldRaid party.");
                }
            }

        }

        if (!oldRaid.getWar().getSide1Players().contains(joiner.getName()) && !oldRaid.getWar().getSide2Players().contains(joiner.getName())) {
            joiner.sendMessage(UtilsChat.getPrefix() + "You are not in this oldWar! Type /oldWar join [oldWar] [side]");
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
                        p.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a oldRaid after " + Main.getInstance().getConfig().getInt("minimumPlayerAge") + " days from joining.");
                    joiner.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a oldRaid after " + Main.getInstance().getConfig().getInt("minimumPlayerAge") + " days from joining.");
                    return;
                } else if (minuteman == 2) {
                    //player has played too little
                    if (admin)
                        p.sendMessage(ChatColor.RED + "You have not played enough! You can only join a oldRaid after " + Main.getInstance().getConfig().getInt("minimumPlayTime") + " hours of play.");
                    joiner.sendMessage(ChatColor.RED + "You have not played enough! You can only join a oldRaid after " + Main.getInstance().getConfig().getInt("minimumPlayTime") + " hours of play.");
                    return;
                }
            }
        }

        //check if gather phase
        if (!admin) {
            if (oldRaid.getPhase() == RaidPhase.GATHER || oldRaid.getPhase() == RaidPhase.START) {
                try {
                    //check if the player is in the gather town
                    ArrayList<WorldCoord> cluster = Utils.getCluster(oldRaid.getGatherTown().getHomeBlock().getWorldCoord());
                    if (cluster.contains(WorldCoord.parseWorldCoord(joiner))) {
                        oldRaid.addActiveRaider(joiner.getName());
                        joiner.sendMessage(UtilsChat.getPrefix() + "You have joined the oldRaid on " + oldRaid.getRaidedTown().getName() + "!");
                        joiner.sendMessage(UtilsChat.getPrefix() + "Leaving " + oldRaid.getGatherTown().getName() + " will cause you to leave the oldRaid party.");
                    } else {
                        joiner.sendMessage(UtilsChat.getPrefix() + "You must be in the gathering town to join the oldRaid on " + oldRaid.getRaidedTown().getName() + ".");
                    }
                } catch (TownyException e) {
                    throw new RuntimeException(e);
                }
            } else {
                joiner.sendMessage(UtilsChat.getPrefix() + "You cannot join the oldRaid on " + oldRaid.getRaidedTown().getName() + "! It has already started!");
            }
        } else {
            try {
                //check if the player is in the gather town
                ArrayList<WorldCoord> cluster = Utils.getCluster(oldRaid.getGatherTown().getHomeBlock().getWorldCoord());
                if (cluster.contains(WorldCoord.parseWorldCoord(joiner))) {
                    oldRaid.addActiveRaider(joiner.getName());
                    joiner.sendMessage(UtilsChat.getPrefix() + "You have joined the oldRaid on " + oldRaid.getRaidedTown().getName() + "!");
                    joiner.sendMessage(UtilsChat.getPrefix() + "Leaving " + oldRaid.getGatherTown().getName() + " will cause you to leave the oldRaid party.");
                } else {
                    joiner.sendMessage(UtilsChat.getPrefix() + "You must be in the gathering town to join the oldRaid on " + oldRaid.getRaidedTown().getName() + ".");
                }
            } catch (TownyException e) {
                throw new RuntimeException(e);
            }
        }

        oldRaid.save();
    }

    protected static void raidLeave(Player player, CommandArguments args, boolean admin) throws WrapperCommandSyntaxException {
        //Get oldRaid!
        final Player p = (Player) args.getOptional("player").orElse(player);

        if (p == null) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser("Raider is Null").build());
        }

        if (!(args.get("war") instanceof final String argWarName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cYou need to specify a war name!").build());

        if (!(args.get("town") instanceof final String argTown))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cYou need to specify a town name!").build());


        OldRaid oldRaid = RaidData.getRaid(argWarName + "-" + argTown);
        if (oldRaid == null) {
            p.sendMessage(UtilsChat.getPrefix() + "No oldRaid is gathering in this town or this town does not exist!");
            if (admin)
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "No oldRaid is gathering in this town or this town does not exist!").build());
        }

        if (oldRaid.getWar().getName().equalsIgnoreCase(argWarName)) {
            if (!oldRaid.getWar().getSide1Players().contains(p.getName()) && !oldRaid.getWar().getSide2Players().contains(p.getName())) {
                p.sendMessage(UtilsChat.getPrefix() + "You are not in this war! Type /war join [war] [side]");
                if (admin)
                    player.sendMessage(UtilsChat.getPrefix() + "You are not in this war! Type /war join [war] [side]");
            }

            //check if gather phase
            if (oldRaid.getPhase() == RaidPhase.GATHER || oldRaid.getPhase() == RaidPhase.START) {
                try {
                    //check if player is in gather town
                    ArrayList<WorldCoord> cluster = Utils.getCluster(oldRaid.getGatherTown().getHomeBlock().getWorldCoord());
                    if (cluster.contains(WorldCoord.parseWorldCoord(p))) {
                        oldRaid.removeActiveRaider(p.getName());
                        p.sendMessage(UtilsChat.getPrefix() + "You have left the oldRaid on " + oldRaid.getRaidedTown().getName() + "!");
                        if (admin)
                            player.sendMessage(UtilsChat.getPrefix() + "You have left the oldRaid on " + oldRaid.getRaidedTown().getName() + "!");

                    } else {
                        p.sendMessage(UtilsChat.getPrefix() + "You cannot leave the oldRaid on " + oldRaid.getRaidedTown().getName() + "! It has already started!");
                        if (admin)
                            player.sendMessage(UtilsChat.getPrefix() + "You cannot leave the oldRaid on " + oldRaid.getRaidedTown().getName() + "! It has already started!");
                    }
                } catch (TownyException e) {
                    throw new RuntimeException(e);
                }
            }

            if (admin) {
                player.sendMessage(UtilsChat.getPrefix() + "Forced player " + p.getName() + " to leave oldRaid on " + oldRaid.getRaidedTown().getName() + " in war " + argWarName);
                Main.warLogger.log("Forced player " + p.getName() + " to leave oldRaid on " + oldRaid.getRaidedTown().getName() + " in war " + argWarName);
            }
        }

        oldRaid.save();
    }

    /**
     * Abandons the raid
     *
     * @param p
     * @param args
     */
    private static void raidAbandon(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        if (!(args.get("war") instanceof final String argWarName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cYou need to specify a war name!").build());

        if (!(args.get("town") instanceof final String argTown))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cYou need to specify a town name!").build());


        //Get oldRaid
        OldRaid oldRaid = RaidData.getRaid(argWarName + "-" + argTown);

        if (oldRaid == null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThis oldRaid does not exist!").build());


        if (oldRaid.getWar().getName().equalsIgnoreCase(argWarName)) {
            if (!oldRaid.getWar().getSide1Players().contains(p.getName()) && !oldRaid.getWar().getSide2Players().contains(p.getName())) {
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cYou are not in this war! Type /war join [war] [side]").build());
            }

            if (oldRaid.getOwner().getName().equalsIgnoreCase(p.getName())) {
                //check if gather phase
                if (oldRaid.getPhase() == RaidPhase.GATHER || oldRaid.getPhase() == RaidPhase.START || oldRaid.getPhase() == RaidPhase.TRAVEL || oldRaid.getPhase() == RaidPhase.COMBAT) {
                    //force defender victory
                    Bukkit.broadcast(new ColorParser(UtilsChat.getPrefix() + "The oldRaid on " + oldRaid.getRaidedTown().getName() + " has been abandoned by " + oldRaid.getRaiderSide()).build());
                    Main.warLogger.log(p.getName() + " abandoned the oldRaid on " + oldRaid.getRaidedTown().getName() + " they started at " + oldRaid.getGatherTown().getName());
                    oldRaid.defendersWin(oldRaid.getRaiderScore(), oldRaid.getDefenderScore());
                } else {
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cYou cannot abandon the oldRaid on " + oldRaid.getRaidedTown().getName() + "! It has a problem!").build());
                }
            } else {
                fail(p, args, "owner");
            }
        }
    }

    private static void raidsList(Player p, CommandArguments args) {
        if (RaidData.getRaids().isEmpty()) {
            p.sendMessage(new ColorParser(UtilsChat.getPrefix() + "There are currently no Raids in progress").build());
            return;
        }

        p.sendMessage(new ColorParser(UtilsChat.getPrefix() + "Raids currently in progress:").build());
        for (final OldRaid oldRaid : RaidData.getRaids()) {
            p.sendMessage(new ColorParser("Name: " + oldRaid.getName()).build());
            p.sendMessage(new ColorParser("OldWar: " + oldRaid.getWar().getName()).build());
            p.sendMessage(new ColorParser("Owner: " + oldRaid.getOwner().getName()).build());
            p.sendMessage(new ColorParser("Raiding: " + oldRaid.getRaidedTown().getName().toLowerCase()).build());
            p.sendMessage(new ColorParser("Gathering In: " + oldRaid.getGatherTown().getName().toLowerCase()).build());
            p.sendMessage(new ColorParser("Raiders: " + oldRaid.getRaiderSide()).build());
            p.sendMessage(new ColorParser("Defenders: " + oldRaid.getDefenderSide()).build());
            p.sendMessage(new ColorParser("Raider Score: " + oldRaid.getRaiderScore()).build());
            p.sendMessage(new ColorParser("Defender Score: " + oldRaid.getDefenderScore()).build());
            p.sendMessage(new ColorParser("OldRaid Phase: " + oldRaid.getPhase().name()).build());
            p.sendMessage(new ColorParser("Time Left: " + (RaidPhase.END.startTick - oldRaid.getRaidTicks()) / 1200 + " minutes").build());
            p.sendMessage(new ColorParser("-=-=-=-=-=-=-=-=-=-=-=-").build());
        }
    }

    private static void raidHelp(Player p, CommandArguments args) {
        if (p.hasPermission("AlathranWars.admin")) {
            p.sendMessage(new ColorParser(UtilsChat.getPrefix() + "/raid stop [name]").build());
        }
        p.sendMessage(new ColorParser(UtilsChat.getPrefix() + "/raid start [war] [town]").build());
        p.sendMessage(new ColorParser(UtilsChat.getPrefix() + "/raid join [war] [town]").build());
        p.sendMessage(new ColorParser(UtilsChat.getPrefix() + "/raid leave [war] [town]").build());
        p.sendMessage(new ColorParser(UtilsChat.getPrefix() + "/raid abandon [war] [town]").build());
        p.sendMessage(new ColorParser(UtilsChat.getPrefix() + "/raid list").build());
    }

    private static void fail(CommandSender p, CommandArguments args, String type) throws WrapperCommandSyntaxException {
        switch (type) {
            case "permissions" ->
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cYou do not have permission to do this.").build());
            case "syntax" ->
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cInvalid Syntax. /raid help").build());
            case "noRaids" ->
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThere are currently no Raids in progress.").build());
            case "badName" ->
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cA raid could not be found with this Name! Type /raid list to view current raids.").build());
            case "owner" ->
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cOnly the player who started the raid can abandon it.").build());
            default ->
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cInvalid Arguments. /raid help").build());
        }
    }
}
