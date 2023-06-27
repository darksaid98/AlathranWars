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
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "Invalid Arguments. /siege help").build());
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
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cUsage: /alathrawaradmin create siege [war] [town] (owner) (force).").build());

        //if this is admin mode use the forth arg instad of sender.
        //if player is null after this then force end
        final Player siegeOwner = (Player) args.getOptional("leader").orElse(sender);

        if (siegeOwner == null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser("You need to pick a valid leader.").build());
        

        /*if (args.length < 3) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "/war siege [war] [town]").build());
        }*/

        // War check
        War war = WarData.getWar(argWarName);
        if (war == null) {
            siegeOwner.sendMessage(Helper.chatLabel() + "That war does not exist! /siege start [war] [town]");
            if (admin)
                sender.sendMessage(Helper.chatLabel() + "That war does not exist! /siege start [war] [town]");
            return;
        }


        // Player participance check
        Town leaderTown = TownyAPI.getInstance().getResident(siegeOwner).getTownOrNull();
        TownWarState side = war.getState(leaderTown.getName().toLowerCase());

        // TODO DEBUG PRINT
        Main.warLogger.log("Leader Town: " + leaderTown.getName());
        Main.warLogger.log("Siege Owner: " + siegeOwner.getName());
        sender.sendMessage("Leader Town: " + leaderTown.getName());
        sender.sendMessage("Siege Owner: " + siegeOwner.getName());

        // TODO DEBUG PRINT
        for (String t : war.getSide1Towns()) {
            Main.warLogger.log("Side1 town: " + t);
            sender.sendMessage("Side1 town: " + t);
        }
        for (String t : war.getSide2Towns()) {
            Main.warLogger.log("Side2 town: " + t);
            sender.sendMessage("Side2 town: " + t);
        }

        if (side == TownWarState.NOT_PARTICIPANT) {
            siegeOwner.sendMessage(Helper.chatLabel() + "You are not in this war.");
            if (admin) sender.sendMessage(Helper.chatLabel() + "You are not in this war.");
            return;
        } else if (side == TownWarState.SURRENDERED) {
            siegeOwner.sendMessage(Helper.chatLabel() + "You have surrendered.");
            if (admin) sender.sendMessage(Helper.chatLabel() + "You have surrendered.");
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
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "Specify a town! /siege start [war] [town]").build());

        // Town check
        Town town = TownyAPI.getInstance().getTown(argTownName);
        if (town == null) {
            siegeOwner.sendMessage(Helper.chatLabel() + "That town does not exist! /siege start [war] [town]");
            if (admin)
                sender.sendMessage(Helper.chatLabel() + "That town does not exist! /siege start [war] [town]");
            return;
        }

        // Is being raided check
        for (Raid r : RaidData.getRaids()) {
            if (r.getRaidedTown().getName().equalsIgnoreCase(town.getName())) {
                siegeOwner.sendMessage(Helper.chatLabel() + "That town is already currently being raided! Cannot siege at this time!");
                if (admin)
                    sender.sendMessage(Helper.chatLabel() + "That town is already currently being raided! Cannot siege at this time!");
                return;
            }
        }

        // Attacking own side
        if (war.getState(town) == side) {
            siegeOwner.sendMessage(Helper.chatLabel() + "You cannot attack your own towns.");
            if (admin) sender.sendMessage(Helper.chatLabel() + "You cannot attack your own towns.");
            return;
        }

        Siege siege = new Siege(war, town, siegeOwner);
        SiegeData.addSiege(siege);
        war.addSiege(siege);

        Bukkit.broadcastMessage(Helper.chatLabel() + siege.getTown() + " has been put to siege by " + siege.getAttackerSide() + "!");

        if (admin)
            sender.sendMessage(Helper.color("&cForcefully started siege from the console."));

        // TODO Debug logs...
        Main.warLogger.log("Attacked Town: " + town.getName().toLowerCase());
        sender.sendMessage("Attacked Town: " + town.getName().toLowerCase());

        war.save();
        siege.start();
    }

    private static void siegeStop(Player sender, CommandArguments args) throws WrapperCommandSyntaxException {
        final HashSet<Siege> sieges = SiegeData.getSieges();
        if (sieges.isEmpty()) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "There are currently no sieges in progress").build());
        }

        if (args.count() < 2) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "/siege stop [town]").build());
        }

        if (!(args.get("war") instanceof final String argWarName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "No war name supplied. /siege stop [war] [town]").build());

        // War check
        War war = WarData.getWar(argWarName);
        if (war == null) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "That war does not exist! /siege stop [war] [town]").build());
        }

        if (!(args.get("town") instanceof final String argTownName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "No war name supplied. /siege stop [war] [town]").build());

        for (Siege siege : sieges) {
            if (siege.getTown().getName().equalsIgnoreCase(argTownName) && siege.getWar().equals(war)) {
                sender.sendMessage(new ColorParser(Helper.chatLabel() + "siege cancelled").build());
                Bukkit.broadcast(new ColorParser(Helper.chatLabel() + "The siege at " + siege.getTown().getName() + " has been cancelled by an admin.").build());
                //siege.clearBeacon();
                siege.stop();
                return;
            }
        }

        throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "A siege could not be found with this town name! Type /siege list to view current sieges").build());
    }

    private static void siegeAbandon(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        if (args.count() < 2) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "/siege abandon [town]").build());
        }

        // War check
        if (!(args.get("war") instanceof final String argWarName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "No war name supplied. /siege stop [war] [town]").build());

        // War check
        War war = WarData.getWar(argWarName);
        if (war == null) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "That war does not exist! /siege abandon [war] [town]").build());
        }

        if (!(args.get("town") instanceof final String argTown))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "No town name supplied. /siege stop [war] [town]").build());

        Siege siege = SiegeData.getSiege(argTown);
        if (siege == null) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "Invalid siege").build());
        }

        OfflinePlayer oPlayer = siege.getSiegeLeader();
        if (oPlayer != p) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "You are not the leader of this siege.").build());
        }

        Bukkit.broadcast(new ColorParser(Helper.chatLabel() + "The siege at " + siege.getTown().getName() + " has been abandoned.").build());
        Main.warLogger.log(p.getName() + " abandoned the siege they started at " + siege.getTown().getName());
        siege.defendersWin();
    }

    private static void siegeList(Player sender, CommandArguments args) throws WrapperCommandSyntaxException {
        HashSet<Siege> sieges = SiegeData.getSieges();
        if (sieges.isEmpty())
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "There are currently no sieges in progress").build());

        sender.sendMessage(new ColorParser(Helper.chatLabel() + "Sieges currently in progress:").build());
        for (Siege siege : sieges) {
            War war = siege.getWar();
            String color = (siege.getSide1AreAttackers() && (war.getState(siege.getTown().getName().toLowerCase()) == TownWarState.SIDE1)) ? "&c" : "&9";
            sender.sendMessage(new ColorParser(war.getName() + " - " + color + siege.getTown().getName().toLowerCase()).build());
            sender.sendMessage(new ColorParser(war.getSide1() + " - " + (siege.getSide1AreAttackers() ? siege.getAttackerPoints() : siege.getDefenderPoints())).build());
            sender.sendMessage(new ColorParser(war.getSide2() + " - " + (siege.getSide1AreAttackers() ? siege.getDefenderPoints() : siege.getAttackerPoints())).build());
            sender.sendMessage(new ColorParser("Time Left: " + (Siege.maxSiegeTicks - siege.getSiegeTicks()) / 1200 + " minutes").build());
            sender.sendMessage(new ColorParser("-=-=-=-=-=-=-=-=-=-=-=-").build());
        }
    }

    private static void siegeHelp(Player sender, CommandArguments args) {
        if (sender.hasPermission("AlathraWar.admin")) {
            sender.sendMessage(new ColorParser(Helper.chatLabel() + "/siege stop [war] [town]").build());
        }
        sender.sendMessage(new ColorParser(Helper.chatLabel() + "/siege start [war] [town]").build());
        sender.sendMessage(new ColorParser(Helper.chatLabel() + "/siege abandon [war] [town]").build());
        sender.sendMessage(new ColorParser(Helper.chatLabel() + "/siege list").build());
    }
}
