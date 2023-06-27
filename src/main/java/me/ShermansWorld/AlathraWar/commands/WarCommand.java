package me.ShermansWorld.AlathraWar.commands;

import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import me.ShermansWorld.AlathraWar.*;
import me.ShermansWorld.AlathraWar.data.WarData;
import me.ShermansWorld.AlathraWar.enums.TownWarState;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WarCommand {
    public WarCommand() {
        new CommandAPICommand("war")
            .withSubcommands(
                commandCreate(),
                commandDelete(),
                commandJoin(),
                commandSurrender(),
                commandList(),
                commandInfo(),
                commandHelp()
            )
            .executesPlayer((sender, args) -> {
                if (args.count() == 0)
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "Invalid Arguments. /war help").build());
            })
            .register();
    }

    public static CommandAPICommand commandCreate() {
        return new CommandAPICommand("create")
            .withPermission("AlathraWar.admin")
            .withArguments(
                new StringArgument("warname"),
                new StringArgument("side1")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> {
                            final List<String> nations = CommandHelper.getTownyNations();
                            final List<String> towns = CommandHelper.getTownyTowns();

                            Collections.sort(nations);
                            Collections.sort(towns);

                            nations.addAll(towns);

                            return nations;
                        })
                    ),
                new StringArgument("side2")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> {
                            final List<String> nations = CommandHelper.getTownyNations();
                            final List<String> towns = CommandHelper.getTownyTowns();

                            Collections.sort(nations);
                            Collections.sort(towns);

                            nations.addAll(towns);

                            return nations;
                        })
                    )
            )
            .executesPlayer(WarCommand::warCreate);
    }

    public static CommandAPICommand commandDelete() {
        return new CommandAPICommand("delete")
            .withPermission("AlathraWar.admin")
            .withArguments(
                new StringArgument("warname")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> WarData.getWarsNames())
                    )
            )
            .executesPlayer(WarCommand::warDelete);
    }

    public static CommandAPICommand commandJoin() {
        return new CommandAPICommand("join")
            .withArguments(
                new StringArgument("warname")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> CommandHelper.getWarNames())
                    ),
                new StringArgument("side")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> {
                            final String warname = (String) info.previousArgs().get("warname");

                            final War war = WarData.getWar(warname);

                            if (war == null) return Collections.emptyList();

                            return war.getSides();
                        })
                    ),
                new PlayerArgument("player").setOptional(true).withPermission("AlathraWar.admin")
            )
            .executesPlayer((Player p, CommandArguments args) -> warJoin(p, args, false));
    }

    public static CommandAPICommand commandSurrender() {
        return new CommandAPICommand("surrender")
            .withArguments(
                new StringArgument("warname")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> CommandHelper.getWarNames())
                    ),
                new PlayerArgument("player")
                    .setOptional(true)
                    .withPermission("AlathraWar.admin")
            )
            .executesPlayer((Player p, CommandArguments args) -> warSurrender(p, args, false));
    }

    public static CommandAPICommand commandList() {
        return new CommandAPICommand("list")
            .executesPlayer(WarCommand::warList);
    }

    public static CommandAPICommand commandInfo() {
        return new CommandAPICommand("info")
            .withArguments(
                new PlayerArgument("player")
            )
            .executesPlayer(WarCommand::warInfo);
    }

    public static CommandAPICommand commandHelp() {
        return new CommandAPICommand("help")
            .executesPlayer(WarCommand::warHelp);
    }

    protected static void warCreate(CommandSender p, CommandArguments args) throws WrapperCommandSyntaxException {
        if (args.count() >= 3) {
            if (!(args.get("warname") instanceof final String argWarName))
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a war name.").build());

            if (WarData.getWar(argWarName) != null)
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cA war already exists with that name.").build());

            if (!(args.get("side1") instanceof final String argSide1))
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a side.").build());

            if (!(args.get("side2") instanceof final String argSide2))
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a side.").build());

            if (argSide1.contains(argSide2))
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cCannot declare war on oneself.").build());

            // TODO Ensure side1 valid
            // TODO Ensure side2 valid
            // TODO Deep Check if war exists where the same sides are fighting???

            // TODO Add checks here for wether the towns/nations names are valid
            War war = new War(argWarName, argSide1, argSide2);
            WarData.addWar(war);
            war.save();

            Bukkit.broadcastMessage(Helper.chatLabel() + "War created with the name " + argWarName + ", "
                + argSide1 + " vs. " + argSide2);
            Main.warLogger.log(p.getName() + " created a new war with the name " + argWarName + ", " + argSide1
                + " vs. " + argSide2);
        } else {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel()
                + "Invalid Arguments. /war create [name] [side1] [side2]").build());
        }

    }

    private static void warDelete(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        if (args.count() >= 1) {
            if (!(args.get("warname") instanceof final String argWarName))
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a war name.").build());

            War war = WarData.getWar(argWarName);

            if (war == null)
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cWar not found. Type /war list to view current wars.").build());

            for (Siege s : war.getSieges()) {
                s.stop();
            }
            for (Raid r : war.getRaids()) {
                r.stop();
            }

            WarData.removeWar(war);

            Bukkit.broadcast(new ColorParser(Helper.chatLabel() + "War " + argWarName + " deleted.").build());
            Main.warLogger.log(p.getName() + " deleted " + argWarName);
        } else {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel()
                + "Invalid Arguments. /war delete [name]").build());
        }
    }

    /**
     * @param p     - executor
     * @param args
     * @param admin - admin behavior
     */
    protected static void warJoin(Player p, CommandArguments args, boolean admin) throws WrapperCommandSyntaxException {
        // Sufficient args check
        if (args.count() < 2)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel()
                + "/war join [name] [side], type /war list to view current wars").build());

        if (!(args.get("warname") instanceof final String argWarName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a war name.").build());

        War war = WarData.getWar(argWarName);

        if (war == null)
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cWar not found. /war join [name] [side], type /war list to view current wars.").build());

        if (!(args.get("side") instanceof final String argSide))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a side.").build());

        if (!war.isSideValid(argSide))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cThat side does not exist in that war.").build());

        if (!(args.getOptional("player").orElse(null) instanceof @Nullable Player argPlayer))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cPlayer not found.").build());

        if (argPlayer == null) {
            argPlayer = p;
        }

        // Towny Resident Object, if admin then dont use command runner, use declared player
        Resident res = TownyAPI.getInstance().getResident(argPlayer);

        // Town check
        Town town = res.getTownOrNull();
        if (town == null) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(ChatColor.RED + "You are not in a town.").build());
        }

        //Minuteman countermeasures
        final int minuteman = CommandHelper.isPlayerMinuteman(argPlayer.getName());
        final boolean minuteManOverride = (boolean) args.getOptional("minutemen").orElse(false);

        if (minuteManOverride) {
            p.sendMessage(ChatColor.RED + "Warning! Ignoring Minuteman Countermeasure!");
        }

        //override?
        if (!minuteManOverride) {
            if (minuteman != 0) {
                if (minuteman == 1) {
                    //player has joined to recently
                    if (admin)
                        p.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayerAge") + " days from joining.");
                    argPlayer.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayerAge") + " days from joining.");
                    return;
                } else if (minuteman == 2) {
                    //player has played too little
                    if (admin)
                        p.sendMessage(ChatColor.RED + "You have not played enough! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayTime") + " hours of play.");
                    argPlayer.sendMessage(ChatColor.RED + "You have not played enough! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayTime") + " hours of play.");
                    return;
                }
            }
        }

        // Side checks
        final TownWarState side = war.getState(town.getName().toLowerCase());
        if (side == TownWarState.SURRENDERED) {
            argPlayer.sendMessage(Helper.chatLabel() + "You've already surrendered!");
            return;
        } else if (side != TownWarState.NOT_PARTICIPANT) {
            argPlayer.sendMessage(Helper.chatLabel() + "You're already in this war!");
            return;
        }

        if (res.hasNation()) {
            if (res.getPlayer().hasPermission("AlathraWar.nationjoin") || res.isKing()) {
                // Has nation declaration permission
                war.addNation(res.getNationOrNull(), argSide);
                res.getPlayer().sendMessage(Helper.chatLabel() + "You have joined the war for " + res.getNationOrNull().getName());
                p.sendMessage(Helper.chatLabel() + "You have joined the war for " + res.getNationOrNull().getName());
                Bukkit.broadcastMessage(Helper.chatLabel() + "The nation of " + res.getNationOrNull().getName() + " has joined the war on the side of " + argSide + "!");
                war.save();
            } else {
                // Cannot declare nation involvement
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "You cannot declare war for your nation.").build());
            }
        } else if (res.hasTown()) {
            if (res.getPlayer().hasPermission("AlathraWar.townjoin") || res.isMayor()) {
                // Is in indepdenent town & has declaration perms
                war.addTown(res.getTownOrNull(), argSide);
                res.getPlayer().sendMessage(Helper.chatLabel() + "You have joined the war for " + res.getTownOrNull().getName());
                p.sendMessage(Helper.chatLabel() + "You have joined the war for " + res.getTownOrNull().getName());
                Bukkit.broadcastMessage(Helper.chatLabel() + "The town of " + res.getTownOrNull().getName() + " has joined the war on the side of " + argSide + "!");
                war.save();
            } else {
                // No perms
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "You cannot declare war for your town.").build());
            }
        }
    }

    protected static void warSurrender(Player p, CommandArguments args, boolean admin) throws WrapperCommandSyntaxException {
        // Sufficient args check
        if (args.count() < 1) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel()
                + "/war surrender [name], type /war list to view current wars").build());
        }

        if (!(args.get("warname") instanceof final String argWarName))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a war name.").build());

        // War check
        War war = WarData.getWar(argWarName);
        if (war == null) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel()
                + "War not found. Type /war list to view current wars").build());
        }

        if (!(args.get("reason") instanceof final String argReason))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a reason.").build());

        if (!(args.getOptional("player").orElse(null) instanceof @Nullable Player argPlayer))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cPlayer not found.").build());

        if (argPlayer == null) {
            argPlayer = p;
        }

        // Towny Resident Object
        Resident res = TownyAPI.getInstance().getResident(argPlayer);

        // Town check
        Town town = res.getTownOrNull();
        if (town == null) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(ChatColor.RED + "You are not in a town.").build());
        }

        if (res.hasNation()) {
            if (p.hasPermission("AlathraWar.nationsurrender") || res.isKing() || admin) {
                // Has nation surrender permission
                war.surrenderNation(res.getNationOrNull());
                argPlayer.sendMessage(Helper.chatLabel() + "You have surrendered the war for " + res.getNationOrNull().getName());
                Bukkit.broadcastMessage(Helper.chatLabel() + "The nation of " + res.getNationOrNull().getName() + " has surrendered! They were fighting for " + argReason + ".");
                war.save();
            } else {
                // Cannot surrender nation involvement
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "You cannot surrender war for your nation.").build());
            }
        } else if (res.hasTown()) {
            if (p.hasPermission("AlathraWar.townsurrender") || res.isMayor() || admin) {
                // Is in indepdenent town & has surrender perms
                war.surrenderTown(res.getTownOrNull().getName());
                argPlayer.sendMessage(Helper.chatLabel() + "You have surrendered the war for " + res.getTownOrNull().getName());
                Bukkit.broadcastMessage(Helper.chatLabel() + "The town of " + res.getTownOrNull().getName() + " has surrendered! They were fighting for " + argReason + ".");
                war.save();
            } else {
                // No perms
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "You cannot surrender war for your town.").build());
            }
        }

    }

    private static void warList(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        ArrayList<War> wars = WarData.getWars();

        if (wars.isEmpty()) {
            p.sendMessage("There are no current wars.");
        } else {
            p.sendMessage(Helper.chatLabel() + "Wars:");

            for (War war : wars) {
                p.sendMessage(war.getName() + " - " + war.getSide1() + " (" + war.getSide1Points() + ") vs. " + war.getSide2() + " (" + war.getSide2Points() + ")");
            }
        }
    }

    private static void warInfo(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        if (args.count() < 1) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "/war info [Player]").build());
        }

        if (!(args.get("player") instanceof Player argsPlayer))
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser("Specify a player.").build());

        Resident res = TownyAPI.getInstance().getResident(argsPlayer);
        if (res == null || res.getTownOrNull() == null) {
            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "Invalid town resident.").build());
        }

        p.sendMessage(new ColorParser(Helper.chatLabel() + p.getName() + "'s wars:").build());
        for (War war : WarData.getWars()) {
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
    }

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
//            ArrayList<War> warList = WarData.getWars();
//            sender.sendMessage("Wars: " + warList.size());
//            for (War war : WarData.getWars()) {
//                if (war == null) continue;
//                sender.sendMessage(war.toString());
//            }
//        }
//    }
}
