package me.ShermansWorld.AlathranWars.commands;

import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import me.ShermansWorld.AlathranWars.conflict.War;
import me.ShermansWorld.AlathranWars.conflict.Side;
import me.ShermansWorld.AlathranWars.holder.WarManager;
import me.ShermansWorld.AlathranWars.utility.UtilsChat;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class WarCommand {
    public WarCommand() {
        new CommandAPICommand("war")
            .withSubcommands(
                commandCreate(),
                commandDelete(),
                commandJoin(),
                commandSurrender(),
                commandList(),
//                commandInfo(),
                commandHelp()
            )
            .executesPlayer((sender, args) -> {
                if (args.count() == 0)
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "Invalid Arguments. /war help").build());
            })
            .register();
    }

    public static CommandAPICommand commandCreate() {
        return new CommandAPICommand("create")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.customTownAndNationsArgument("side1", "war", false),
                CommandUtil.customTownAndNationsArgument("side2", "war", false),
                new GreedyStringArgument("warlabel")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> List.of(
                            "Nations War of Conquest",
                            "Nations War for Survival",
                            "Nations War of Liberation"
                        ))
                    )
            )
            .executesPlayer(WarCommand::warCreate);
    }

    public static CommandAPICommand commandDelete() {
        return new CommandAPICommand("delete")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.warWarArgument("war", false, false, "")
            )
            .executesPlayer(WarCommand::warDelete);
    }

    public static CommandAPICommand commandJoin() {
        return new CommandAPICommand("join")
            .withArguments(
                CommandUtil.warWarArgument("war", false, true, ""),
                CommandUtil.warSideCreateArgument("side", "war", false, true, ""),
                new PlayerArgument("player").setOptional(true).withPermission("AlathranWars.admin")
            )
            .executesPlayer((Player p, CommandArguments args) -> warJoinPlayer(p, args, false));
    }

    public static CommandAPICommand commandSurrender() {
        return new CommandAPICommand("surrender")
            .withArguments(
                CommandUtil.warWarArgument("war", false, false, "player"),
                new PlayerArgument("player")
                    .setOptional(true)
                    .withPermission("AlathranWars.admin")
            )
            .executesPlayer((Player p, CommandArguments args) -> warSurrender(p, args, false));
    }

    public static CommandAPICommand commandList() {
        return new CommandAPICommand("list")
            .executesPlayer(WarCommand::warList);
    }

    /*public static CommandAPICommand commandInfo() {
        return new CommandAPICommand("info")
            .withArguments(
                new PlayerArgument("player")
            )
            .executesPlayer(WarCommand::warInfo);
    }*/

    public static CommandAPICommand commandHelp() {
        return new CommandAPICommand("help")
            .executesPlayer(WarCommand::warHelp);
    }

    protected static void warCreate(CommandSender p, CommandArguments args) throws WrapperCommandSyntaxException {
        if (!(args.get("warlabel") instanceof final String argLabel))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>You need to specify a war label.").build());

        if (Objects.equals(args.get("side1"), args.get("side2")))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>Cannot declare war on oneself.").build());

        String townName1 = (String) args.get("side1");
        String townName2 = (String) args.get("side2");

        // Create new war depending on what argument was specified
        @Nullable Town town1 = TownyAPI.getInstance().getTown(townName1);
        @Nullable Town town2 = TownyAPI.getInstance().getTown(townName2);
        @Nullable Nation nation1 = TownyAPI.getInstance().getNation(townName1);
        @Nullable Nation nation2 = TownyAPI.getInstance().getNation(townName2);

        // Automatically elevate town wars to nation level if possible (This may be undesirable in terms of allowing civil wars etc)
        if (town1 != null && town1.getNationOrNull() != null) {
            nation1 = town1.getNationOrNull();
        }

        if (town2 != null && town2.getNationOrNull() != null) {
            nation2 = town2.getNationOrNull();
        }

        // TODO Ensure side1 valid
        // TODO Ensure side2 valid
        // TODO Deep Check if war exists where the same sides are fighting???

        // Create war depending on what args have been passed, attempt to create nation wars first
        if (nation1 != null) {
            if (nation2 != null) {
                new War(argLabel, nation1, nation2);
            } else if (town2 != null) {
                new War(argLabel, nation1, town2);
            }
        } else if (town1 != null) {
            if (town2 != null) {
                new War(argLabel, town1, town2);
            } else if (nation2 != null) {
                new War(argLabel, town1, nation2);
            }
        }


        /*for (War war1 : WarManager.getInstance().getWars()) {
            war.
        }*/


        // Check if side1 is nation or town
        // Check if side2 is nation or town
        // Check if opposing factions are already at war?

        /*Main.warLogger.log(p.getName() + " created a new war with the name " + argWarName + ", " + argSide1
            + " vs. " + argSide2);*/

    }

    private static void warDelete(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");

        war.draw();
//        Main.warLogger.log(p.getName() + " deleted " + argWarName);
    }

    protected static void warJoinPlayer(Player p, CommandArguments args, boolean admin) throws WrapperCommandSyntaxException {
        if (!(args.get("war") instanceof final War war))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>You need to specify a war name.").build());

        if (!(args.get("side") instanceof final Side side))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>You need to specify a side.").build());

        if (!(args.getOptional("player").orElse(p) instanceof Player argPlayer))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>Player not found.").build());

        if (war.isPlayerInWar(argPlayer))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>You are already in this war.").build());

        argPlayer.sendMessage(new ColorParser(UtilsChat.getPrefix() + "You have joined the war.").build());
        side.addPlayer(argPlayer);
    }

    /**
     * @param p     - executor
     * @param args
     * @param admin - admin behavior
     */
    protected static void warJoin(Player p, CommandArguments args, boolean admin) throws WrapperCommandSyntaxException {
        if (!(args.get("war") instanceof final War war))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>You need to specify a war name.").build());

        if (!(args.get("side") instanceof final Side side))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>You need to specify a side.").build());

        if (!(args.getOptional("player").orElse(p) instanceof Player argPlayer))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>Player not found.").build());

        // Towny Resident Object, if admin then dont use command runner, use declared player
        Resident res = TownyAPI.getInstance().getResident(argPlayer);

        // Town check
        Town town = res.getTownOrNull();
        if (town == null) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(ChatColor.RED + "You are not in a town.").build());
        }

        //Minuteman countermeasures
//        final int minuteman = CommandHelper.isPlayerMinuteman(argPlayer.getName());
//        final boolean minuteManOverride = (boolean) args.getOptional("minutemen").orElse(false);
//
//        if (minuteManOverride) {
//            p.sendMessage(ChatColor.RED + "Warning! Ignoring Minuteman Countermeasure!");
//        }
//
//        //override?
//        if (!minuteManOverride) {
//            if (minuteman != 0) {
//                if (minuteman == 1) {
//                    //player has joined to recently
//                    if (admin)
//                        p.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayerAge") + " days from joining.");
//                    argPlayer.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayerAge") + " days from joining.");
//                    return;
//                } else if (minuteman == 2) {
//                    //player has played too little
//                    if (admin)
//                        p.sendMessage(ChatColor.RED + "You have not played enough! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayTime") + " hours of play.");
//                    argPlayer.sendMessage(ChatColor.RED + "You have not played enough! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayTime") + " hours of play.");
//                    return;
//                }
//            }
//        }

        // Side checks
        if (war.getSurrenderedTowns().contains(town)) {
            argPlayer.sendMessage(UtilsChat.getPrefix() + "You've already surrendered!");
            return;
        }

        if (res.hasNation()) {
            if (res.getPlayer().hasPermission("AlathranWars.nationjoin") || res.isKing()) {
                // Has nation declaration permission
                side.addNation(res.getNationOrNull());
                res.getPlayer().sendMessage(UtilsChat.getPrefix() + "You have joined the war for " + res.getNationOrNull().getName());
                p.sendMessage(UtilsChat.getPrefix() + "You have joined the war for " + res.getNationOrNull().getName());
                Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The nation of " + res.getNationOrNull().getName() + " has joined the war on the side of " + side.getName() + "!");
            } else {
                // Cannot declare nation involvement
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "You cannot declare war for your nation.").build());
            }
        } else if (res.hasTown()) {
            if (res.getPlayer().hasPermission("AlathranWars.townjoin") || res.isMayor()) {
                // Is in indepdenent town & has declaration perms
                side.addTown(res.getTownOrNull());
                res.getPlayer().sendMessage(UtilsChat.getPrefix() + "You have joined the war for " + res.getTownOrNull().getName());
                p.sendMessage(UtilsChat.getPrefix() + "You have joined the war for " + res.getTownOrNull().getName());
                Bukkit.broadcastMessage(UtilsChat.getPrefix() + "The town of " + res.getTownOrNull().getName() + " has joined the war on the side of " + side.getName() + "!");
            } else {
                // No perms
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "You cannot declare war for your town.").build());
            }
        }
    }

    protected static void warSurrender(Player p, CommandArguments args, boolean admin) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");

        if (!(args.getOptional("player").orElse(p) instanceof Player argPlayer))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>Player not found.").build());

        // Towny Resident Object
        Resident res = TownyAPI.getInstance().getResident(argPlayer);
        if (res == null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser("<red>Resident invalid.").build());

        // Town check
        Town town = res.getTownOrNull();
        if (town == null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser("<red>You are not in a town.").build());

        Side side = war.getTownSide(town);
        if (side == null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser("<red>You are not in the war.").build());

        final Nation resNation = res.getNationOrNull();
        final Town resTown = res.getTownOrNull();

        if (resNation != null) {
            if (p.hasPermission("AlathranWars.nationsurrender") || res.isKing() || admin) {
                // Has nation surrender permission
                argPlayer.sendMessage(new ColorParser(UtilsChat.getPrefix() + "You have surrendered the war for " + resNation.getName() + ".").build());
                Bukkit.broadcast(new ColorParser(UtilsChat.getPrefix() + "The nation of " + resNation.getName() + " has surrendered!").build());
                side.surrenderNation(resNation);
                side.processSurrenders();
            } else {
                // Cannot surrender nation involvement
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "You cannot surrender war for your nation.").build());
            }
        } else if (resTown != null) {
            if (p.hasPermission("AlathranWars.townsurrender") || res.isMayor() || admin) {
                // Is in indepdenent town & has surrender perms
                argPlayer.sendMessage(new ColorParser(UtilsChat.getPrefix() + "You have surrendered the war for " + resTown.getName() + ".").build());
                Bukkit.broadcast(new ColorParser(UtilsChat.getPrefix() + "The town of " + resTown.getName() + " has surrendered!").build());
                side.surrenderTown(resTown);
                side.processSurrenders();
            } else {
                // No perms
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "You cannot surrender war for your town.").build());
            }
        }

    }

    private static void warList(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        Set<War> wars = WarManager.getInstance().getWars();

        if (wars.isEmpty()) {
            p.sendMessage(new ColorParser("There are no wars at the moment, the world is at peace.").build());
        } else {
            p.sendMessage(new ColorParser(UtilsChat.getPrefix() + "Wars:").build());

            for (War war : wars) {
                p.sendMessage(new ColorParser(war.getName() + " - " + war.getSide1().getName() + " (" + war.getSide1().getScore() + ") vs. " + war.getSide2().getName() + " (" + war.getSide2().getScore() + ")").build());
            }
        }
    }

    /*private static void warInfo(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        if (args.count() < 1) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "/war info [Player]").build());
        }

        if (!(args.get("player") instanceof Player argsPlayer))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser("Specify a player.").build());

        Resident res = TownyAPI.getInstance().getResident(argsPlayer);
        if (res == null || res.getTownOrNull() == null) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "Invalid town resident.").build());
        }

        p.sendMessage(new ColorParser(UtilsChat.getPrefix() + p.getName() + "'s wars:").build());
        for (War war : WarManager.getInstance().getWars()) {
            TownWarState side = war.getState(res.getTownOrNull());
            if (side != TownWarState.NOT_PARTICIPANT) {
                if (side == TownWarState.SURRENDERED) {
                    p.sendMessage(war.getName() + " - Surrendered");
                } else if (side == TownWarState.SIDE1) {
                    p.sendMessage(war.getName() + " - " + war.getSide1());
                } else if (side == TownWarState.SIDE2) {
                    p.sendMessage(war.getName() + " - " + war.getSide2());
                }
            }
        }
    }*/

    private static void warHelp(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        // TODO
    }

    /**
     * Method for console access
     *
     * @param sender
     * @param args
     */
//    private static void consoleCommand(CommandSender sender, CommandArguments args) {
//        if (args.length < 1) return;
//        if (args[0].equalsIgnoreCase("list")) {
//            ArrayList<War> warList = WarManager.getInstance().getWars();
//            sender.sendMessage("Wars: " + warList.size());
//            for (War war : WarManager.getInstance().getWars()) {
//                if (war == null) continue;
//                sender.sendMessage(war.toString());
//            }
//        }
//    }
}
