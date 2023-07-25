package com.github.alathra.AlathranWars.commands;

import com.github.alathra.AlathranWars.conflict.Side;
import com.github.alathra.AlathranWars.conflict.War;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.enums.BattleTeam;
import com.github.alathra.AlathranWars.holder.WarManager;
import com.github.alathra.AlathranWars.utility.UtilsChat;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

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
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "Invalid Arguments. /siege help").build());
            })
            .register();
    }

    public static CommandAPICommand commandStart() {
        return new CommandAPICommand("start")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.warWarArgument(
                    "war",
                    false,
                    false,
                    ""
                ),
                CommandUtil.customSiegeAttackableTownArgument(
                    "town",
                    "war",
                    false,
                    true
                ),
                new PlayerArgument("leader")
                    .setOptional(true)
                    .withPermission("AlathranWars.admin"),
                new BooleanArgument("minutemen")
                    .setOptional(true)
                    .withPermission("AlathranWars.admin")
            )
            .executesPlayer((Player p, CommandArguments args) -> siegeStart(p, args, false));
    }

    public static CommandAPICommand commandStop() {
        return new CommandAPICommand("stop")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.warWarArgument(
                    "war",
                    false,
                    false,
                    ""
                ),
                CommandUtil.customSiegeAttackableTownArgument(
                    "town",
                    "war",
                    false,
                    false
                )
            )
            .executesPlayer(SiegeCommand::siegeStop);
    }

    public static CommandAPICommand commandAbandon() {
        return new CommandAPICommand("abandon")
            .withArguments(
                CommandUtil.warWarArgument(
                    "war",
                    false,
                    false,
                    ""
                ),
                CommandUtil.customSiegeAttackableTownArgument(
                    "town",
                    "war",
                    false,
                    false
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


    protected static void siegeStart(@NotNull Player sender, @NotNull CommandArguments args, boolean admin) throws WrapperCommandSyntaxException {
        if (!(args.get("war") instanceof final @NotNull War war))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You need to specify a war.").build());

        if (!(args.get("town") instanceof final @NotNull Town town))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You need to specify a town.").build());

        //if this is admin mode use the forth arg instad of sender.
        //if player is null after this then force end
        final @NotNull Player siegeOwner = (Player) args.getOptional("leader").orElse(sender);

        if (siegeOwner == null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("You need to pick a valid leader.").build());

        // Player participance check
        @Nullable Town leaderTown = TownyAPI.getInstance().getResident(siegeOwner).getTownOrNull();
        @Nullable Side side = war.getTownSide(leaderTown);

        // TODO DEBUG PRINT
//        Main.warLogger.log("Leader Town: " + leaderTown.getName());
//        Main.warLogger.log("Siege Owner: " + siegeOwner.getName());
//        sender.sendMessage("Leader Town: " + leaderTown.getName());
//        sender.sendMessage("Siege Owner: " + siegeOwner.getName());

        // TODO DEBUG PRINT
        /*for (String t : war.getSide1Towns()) {
            Main.warLogger.log("Side1 town: " + t);
            sender.sendMessage("Side1 town: " + t);
        }
        for (String t : war.getSide2Towns()) {
            Main.warLogger.log("Side2 town: " + t);
            sender.sendMessage("Side2 town: " + t);
        }*/

        if (!war.isTownInWar(leaderTown)) {
            siegeOwner.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "You are not in this war.").build());
            if (admin) sender.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "You are not in this war.").build());
            return;
        } else if (side.isTownSurrendered(leaderTown)) {
            siegeOwner.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "You are under occupation.").build());
            if (admin) sender.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "You are under occupation.").build());
            return;
        }

        //Minutemen countermeasures, 86400 * 4 time. 86400 seconds in a day, 4 days min playtime
        //Minuteman countermeasures
        /*int minuteman = CommandHelper.isPlayerMinuteman(siegeOwner.getName());
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
        }*/

        // Is being raided check
        /*for (OldRaid r : RaidData.getRaids()) {
            if (r.getRaidedTown().getName().equalsIgnoreCase(town.getName())) {
                siegeOwner.sendMessage(UtilsChat.getPrefix() + "That town is already currently being raided! Cannot siege at this time!");
                if (admin)
                    sender.sendMessage(UtilsChat.getPrefix() + "That town is already currently being raided! Cannot siege at this time!");
                return;
            }
        }*/

        // Attacking own side
        if (side.isTownOnSide(town)) {
            siegeOwner.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "You cannot attack your own towns.").build());
            if (admin)
                sender.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "You cannot attack your own towns.").build());
            return;
        }

        @NotNull Siege siege = new Siege(war, town, siegeOwner);
        war.addSiege(siege);

        Bukkit.broadcast(ColorParser.of(UtilsChat.getPrefix() + siege.getTown() + " has been put to siege by " + siege.getAttackerSide().getName() + "!").build());

        if (admin)
            sender.sendMessage(ColorParser.of("<red>Forcefully started siege from the console.").build());

        siege.start();
        // TODO Debug logs...
//        Main.warLogger.log("Attacked Town: " + town.getName().toLowerCase());
//        sender.sendMessage("Attacked Town: " + town.getName().toLowerCase());
    }

    private static void siegeStop(@NotNull Player sender, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        if (!(args.get("war") instanceof final @NotNull War war))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You need to specify a war.").build());

        if (!(args.get("town") instanceof final @NotNull Town town))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You need to specify a town.").build());

        final @NotNull Set<Siege> sieges = war.getSieges();
        if (sieges.isEmpty())
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>There are currently no sieges in progress!").build());

        for (@NotNull Siege siege : sieges) {
            if (siege.getTown().getUUID().equals(town.getUUID()) && siege.getWar().equals(war)) {
                sender.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "siege cancelled.").build());
                Bukkit.broadcast(ColorParser.of(UtilsChat.getPrefix() + "The siege at " + siege.getTown().getName() + " has been cancelled by an admin.").build());
                siege.stop();
                return;
            }
        }

        throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "A siege could not be found with this town name! Type /siege list to view current sieges").build());
    }

    private static void siegeAbandon(Player p, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        if (!(args.get("war") instanceof final @NotNull War war))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You need to specify a war.").build());

        if (!(args.get("town") instanceof final @NotNull Town town))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You need to specify a town.").build());

        @Nullable Siege siege = null;

        for (@NotNull Siege siegeVar : war.getSieges()) {
            if (siegeVar.getTown().getUUID().equals(town.getUUID())) {
                siege = siegeVar;
                break;
            }
        }

        if (siege == null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "Invalid siege").build());

        @NotNull OfflinePlayer oPlayer = siege.getSiegeLeader();
        if (oPlayer != p) {
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "You are not the leader of this siege.").build());
        }

        Bukkit.broadcast(ColorParser.of(UtilsChat.getPrefix() + "The siege at " + siege.getTown().getName() + " has been abandoned.").build());
//        Main.warLogger.log(p.getName() + " abandoned the siege they started at " + siege.getTown().getName());
        siege.defendersWin();
    }

    private static void siegeList(@NotNull Player sender, CommandArguments args) throws WrapperCommandSyntaxException {
        @NotNull Set<War> wars = WarManager.getInstance().getWars();

        @NotNull Set<Siege> sieges = new HashSet<>();

        for (@NotNull War war : wars) {
            sieges.addAll(war.getSieges());
        }

        if (sieges.isEmpty())
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>There are currently no sieges in progress.").build());

        sender.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "Sieges currently in progress:").build());

        for (@NotNull Siege siege : sieges) {
            @NotNull War war = siege.getWar();

            final @NotNull String color = (siege.getSide1AreAttackers() && (war.getTownSide(siege.getTown()).getTeam().equals(BattleTeam.SIDE_1))) ? "<red>" : "<blue>";

            sender.sendMessage(
                ColorParser.of(
                    color + siege.getTown().getName() + "  <gray>- Progress: <progress> - Time: <time>min"
                )
                    .parseMinimessagePlaceholder("progress", "%.0f%%".formatted(siege.getSiegeProgressPercentage() * 100))
                    .parseMinimessagePlaceholder("time", String.valueOf(Duration.between(Instant.now(), siege.getEndTime()).toMinutes()))
                    .build()
            );
//            sender.sendMessage(ColorParser.of(war.getName() + " - " + color + siege.getTown().getName().toLowerCase()).build()); // TODO Fix info
//            sender.sendMessage(ColorParser.of(war.getSide1() + " - " + (siege.getSide1AreAttackers() ? siege.getAttackerPoints() : siege.getDefenderPoints())).build());
//            sender.sendMessage(ColorParser.of(war.getSide2() + " - " + (siege.getSide1AreAttackers() ? siege.getDefenderPoints() : siege.getAttackerPoints())).build());
//            sender.sendMessage(ColorParser.of("Time Left: " + (siege.getMaxSiegeTicks() - siege.getSiegeTicks()) / 1200 + " minutes").build());
//            sender.sendMessage(ColorParser.of("-=-=-=-=-=-=-=-=-=-=-=-").build());
        }
    }

    private static void siegeHelp(@NotNull Player sender, CommandArguments args) {
        if (sender.hasPermission("AlathranWars.admin")) {
            sender.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "/siege stop [war] [town]").build());
        }
        sender.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "/siege start [war] [town]").build());
        sender.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "/siege abandon [war] [town]").build());
        sender.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "/siege list").build());
    }
}
