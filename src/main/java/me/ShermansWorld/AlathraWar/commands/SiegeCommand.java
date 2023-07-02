package me.ShermansWorld.AlathraWar.commands;

import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
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
import me.ShermansWorld.AlathraWar.data.SiegeData;
import me.ShermansWorld.AlathraWar.data.WarData;
import me.ShermansWorld.AlathraWar.deprecated.CommandHelper;
import me.ShermansWorld.AlathraWar.deprecated.OldRaid;
import me.ShermansWorld.AlathraWar.deprecated.OldSiege;
import me.ShermansWorld.AlathraWar.deprecated.OldWar;
import me.ShermansWorld.AlathraWar.enums.TownWarState;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class SiegeCommand {
    public SiegeCommand() {
        new CommandAPICommand("siege")
            .withSubcommands(
                commandStart(),
                commandStop(),
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

    public static CommandAPICommand commandStart() {
        return new CommandAPICommand("start")
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
                        })
                    ),
                new PlayerArgument("leader")
                    .setOptional(true)
                    .withPermission("AlathraWar.admin"),
                new BooleanArgument("minutemen")
                    .setOptional(true)
                    .withPermission("AlathraWar.admin")
            )
            .executesPlayer((Player p, CommandArguments args) -> siegeStart(p, args, false));
    }

    public static CommandAPICommand commandStop() {
        return new CommandAPICommand("stop")
            .withPermission("AlathraWar.admin")
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
            .executesPlayer(SiegeCommand::siegeStop);
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
            .executesPlayer(SiegeCommand::siegeAbandon);
    }

    public static CommandAPICommand commandList() {
        return new CommandAPICommand("list")
            .executesPlayer(SiegeCommand::siegeList);
    }

    public static CommandAPICommand commandHelp() {
        return new CommandAPICommand("help")
            .executesPlayer(SiegeCommand::siegeHelp);
    }


    protected static void siegeStart(Player sender, CommandArguments args, boolean admin) throws WrapperCommandSyntaxException {
        if (!(args.get("war") instanceof final String argWarName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cUsage: /alathrawaradmin create oldSiege [oldWar] [town] (owner) (force).").build());

        //if this is admin mode use the forth arg instad of sender.
        //if player is null after this then force end
        final Player siegeOwner = (Player) args.getOptional("leader").orElse(sender);

        if (siegeOwner == null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser("You need to pick a valid leader.").build());
        

        /*if (args.length < 3) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "/oldWar oldSiege [oldWar] [town]").build());
        }*/

        // OldWar check
        OldWar oldWar = WarData.getWar(argWarName);
        if (oldWar == null) {
            siegeOwner.sendMessage(UtilsChat.getPrefix() + "That oldWar does not exist! /oldSiege start [oldWar] [town]");
            if (admin)
                sender.sendMessage(UtilsChat.getPrefix() + "That oldWar does not exist! /oldSiege start [oldWar] [town]");
            return;
        }


        // Player participance check
        Town leaderTown = TownyAPI.getInstance().getResident(siegeOwner).getTownOrNull();
        TownWarState side = oldWar.getState(leaderTown.getName().toLowerCase());

        // TODO DEBUG PRINT
        Main.warLogger.log("Leader Town: " + leaderTown.getName());
        Main.warLogger.log("OldSiege Owner: " + siegeOwner.getName());
        sender.sendMessage("Leader Town: " + leaderTown.getName());
        sender.sendMessage("OldSiege Owner: " + siegeOwner.getName());

        // TODO DEBUG PRINT
        for (String t : oldWar.getSide1Towns()) {
            Main.warLogger.log("Side1 town: " + t);
            sender.sendMessage("Side1 town: " + t);
        }
        for (String t : oldWar.getSide2Towns()) {
            Main.warLogger.log("Side2 town: " + t);
            sender.sendMessage("Side2 town: " + t);
        }

        if (side == TownWarState.NOT_PARTICIPANT) {
            siegeOwner.sendMessage(UtilsChat.getPrefix() + "You are not in this oldWar.");
            if (admin) sender.sendMessage(UtilsChat.getPrefix() + "You are not in this oldWar.");
            return;
        } else if (side == TownWarState.SURRENDERED) {
            siegeOwner.sendMessage(UtilsChat.getPrefix() + "You have surrendered.");
            if (admin) sender.sendMessage(UtilsChat.getPrefix() + "You have surrendered.");
            return;
        }

        //Minutemen countermeasures, 86400 * 4 time. 86400 seconds in a day, 4 days min playtime
        //Minuteman countermeasures
        int minuteman = CommandHelper.isPlayerMinuteman(siegeOwner.getName());
        boolean minuteManOverride = (boolean) args.getOptional("minutemen").orElse(false);

        if (minuteManOverride) {
            sender.sendMessage(ChatColor.RED + "Warning! Ignoring Minuteman Countermeasure!");
        }

        //override?
        if (!minuteManOverride) {
            if (minuteman != 0) {
                if (minuteman == 1) {
                    //player has joined to recently
                    if (admin)
                        sender.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayerAge") + " days from joining.");
                    siegeOwner.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayerAge") + " days from joining.");
                    return;
                } else if (minuteman == 2) {
                    //player has played too little
                    if (admin)
                        sender.sendMessage(ChatColor.RED + "You have not played enough! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayTime") + " hours of play.");
                    siegeOwner.sendMessage(ChatColor.RED + "You have not played enough! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayTime") + " hours of play.");
                    return;
                }
            }
        }

        if (!(args.get("town") instanceof final String argTownName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "Specify a town! /oldSiege start [oldWar] [town]").build());

        // Town check
        Town town = TownyAPI.getInstance().getTown(argTownName);
        if (town == null) {
            siegeOwner.sendMessage(UtilsChat.getPrefix() + "That town does not exist! /oldSiege start [oldWar] [town]");
            if (admin)
                sender.sendMessage(UtilsChat.getPrefix() + "That town does not exist! /oldSiege start [oldWar] [town]");
            return;
        }

        // Is being raided check
        for (OldRaid r : RaidData.getRaids()) {
            if (r.getRaidedTown().getName().equalsIgnoreCase(town.getName())) {
                siegeOwner.sendMessage(UtilsChat.getPrefix() + "That town is already currently being raided! Cannot oldSiege at this time!");
                if (admin)
                    sender.sendMessage(UtilsChat.getPrefix() + "That town is already currently being raided! Cannot oldSiege at this time!");
                return;
            }
        }

        // Attacking own side
        if (oldWar.getState(town) == side) {
            siegeOwner.sendMessage(UtilsChat.getPrefix() + "You cannot attack your own towns.");
            if (admin) sender.sendMessage(UtilsChat.getPrefix() + "You cannot attack your own towns.");
            return;
        }

        OldSiege oldSiege = new OldSiege(oldWar, town, siegeOwner);
        SiegeData.addSiege(oldSiege);
        oldWar.addSiege(oldSiege);

        Bukkit.broadcastMessage(UtilsChat.getPrefix() + oldSiege.getTown() + " has been put to oldSiege by " + oldSiege.getAttackerSide() + "!");

        if (admin)
            sender.sendMessage(Helper.color("&cForcefully started oldSiege from the console."));

        // TODO Debug logs...
        Main.warLogger.log("Attacked Town: " + town.getName().toLowerCase());
        sender.sendMessage("Attacked Town: " + town.getName().toLowerCase());

        oldWar.save();
        oldSiege.start();
    }

    private static void siegeStop(Player sender, CommandArguments args) throws WrapperCommandSyntaxException {
        final HashSet<OldSiege> oldSieges = SiegeData.getSieges();
        if (oldSieges.isEmpty()) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "There are currently no oldSieges in progress").build());
        }

        if (args.count() < 2) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "/siege stop [town]").build());
        }

        if (!(args.get("war") instanceof final String argWarName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "No oldWar name supplied. /siege stop [oldWar] [town]").build());

        // OldWar check
        OldWar oldWar = WarData.getWar(argWarName);
        if (oldWar == null) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "That oldWar does not exist! /siege stop [oldWar] [town]").build());
        }

        if (!(args.get("town") instanceof final String argTownName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "No oldWar name supplied. /siege stop [oldWar] [town]").build());

        for (OldSiege oldSiege : oldSieges) {
            if (oldSiege.getTown().getName().equalsIgnoreCase(argTownName) && oldSiege.getWar().equals(oldWar)) {
                sender.sendMessage(new ColorParser(UtilsChat.getPrefix() + "oldSiege cancelled").build());
                Bukkit.broadcast(new ColorParser(UtilsChat.getPrefix() + "The oldSiege at " + oldSiege.getTown().getName() + " has been cancelled by an admin.").build());
                //oldSiege.clearBeacon();
                oldSiege.stop();
                return;
            }
        }

        throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "A siege could not be found with this town name! Type /siege list to view current oldSieges").build());
    }

    private static void siegeAbandon(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        if (args.count() < 2) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "/oldSiege abandon [town]").build());
        }

        // OldWar check
        if (!(args.get("war") instanceof final String argWarName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "No oldWar name supplied. /oldSiege stop [oldWar] [town]").build());

        // OldWar check
        OldWar oldWar = WarData.getWar(argWarName);
        if (oldWar == null) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "That oldWar does not exist! /oldSiege abandon [oldWar] [town]").build());
        }

        if (!(args.get("town") instanceof final String argTown))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "No town name supplied. /oldSiege stop [oldWar] [town]").build());

        OldSiege oldSiege = SiegeData.getSiege(argTown);
        if (oldSiege == null) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "Invalid oldSiege").build());
        }

        OfflinePlayer oPlayer = oldSiege.getSiegeLeader();
        if (oPlayer != p) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "You are not the leader of this oldSiege.").build());
        }

        Bukkit.broadcast(new ColorParser(UtilsChat.getPrefix() + "The oldSiege at " + oldSiege.getTown().getName() + " has been abandoned.").build());
        Main.warLogger.log(p.getName() + " abandoned the oldSiege they started at " + oldSiege.getTown().getName());
        oldSiege.defendersWin();
    }

    private static void siegeList(Player sender, CommandArguments args) throws WrapperCommandSyntaxException {
        HashSet<OldSiege> oldSieges = SiegeData.getSieges();
        if (oldSieges.isEmpty())
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "There are currently no oldSieges in progress").build());

        sender.sendMessage(new ColorParser(UtilsChat.getPrefix() + "Sieges currently in progress:").build());
        for (OldSiege oldSiege : oldSieges) {
            OldWar oldWar = oldSiege.getWar();
            String color = (oldSiege.getSide1AreAttackers() && (oldWar.getState(oldSiege.getTown().getName().toLowerCase()) == TownWarState.SIDE1)) ? "&c" : "&9";
            sender.sendMessage(new ColorParser(oldWar.getName() + " - " + color + oldSiege.getTown().getName().toLowerCase()).build());
            sender.sendMessage(new ColorParser(oldWar.getSide1() + " - " + (oldSiege.getSide1AreAttackers() ? oldSiege.getAttackerPoints() : oldSiege.getDefenderPoints())).build());
            sender.sendMessage(new ColorParser(oldWar.getSide2() + " - " + (oldSiege.getSide1AreAttackers() ? oldSiege.getDefenderPoints() : oldSiege.getAttackerPoints())).build());
            sender.sendMessage(new ColorParser("Time Left: " + (OldSiege.maxSiegeTicks - oldSiege.getSiegeTicks()) / 1200 + " minutes").build());
            sender.sendMessage(new ColorParser("-=-=-=-=-=-=-=-=-=-=-=-").build());
        }
    }

    private static void siegeHelp(Player sender, CommandArguments args) {
        if (sender.hasPermission("AlathraWar.admin")) {
            sender.sendMessage(new ColorParser(UtilsChat.getPrefix() + "/siege stop [war] [town]").build());
        }
        sender.sendMessage(new ColorParser(UtilsChat.getPrefix() + "/siege start [war] [town]").build());
        sender.sendMessage(new ColorParser(UtilsChat.getPrefix() + "/siege abandon [war] [town]").build());
        sender.sendMessage(new ColorParser(UtilsChat.getPrefix() + "/siege list").build());
    }
}
