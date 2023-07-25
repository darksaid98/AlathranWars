package com.github.alathra.AlathranWars.commands;

import com.github.alathra.AlathranWars.conflict.Side;
import com.github.alathra.AlathranWars.conflict.War;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.enums.BattleSide;
import com.github.alathra.AlathranWars.holder.WarManager;
import com.github.alathra.AlathranWars.listeners.war.PlayerJoinListener;
import com.github.alathra.AlathranWars.utility.UtilsChat;
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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.github.alathra.AlathranWars.enums.CommandArgsWar.*;

public class WarCommands {
    public WarCommands() {
        new CommandAPICommand("war")
            .withSubcommands(
                commandCreate(false),
                commandDelete(false),
                commandJoin(false),
                commandSurrender(false),
                commandList(),
                commandInfo()
            )
            .executesPlayer((sender, args) -> {
                if (args.count() == 0)
                    throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "Invalid Arguments. /war help").build());
            })
            .register();
    }

    public static CommandAPICommand commandCreate(boolean asAdmin) {
        return new CommandAPICommand("create")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.customTownAndNationArgument("side1", "war", false),
                CommandUtil.customTownAndNationArgument("side2", "war", false),
                new GreedyStringArgument("warlabel")
                    .replaceSuggestions(
                        ArgumentSuggestions.stringCollection(info -> List.of(
                            "Nations War of Conquest",
                            "Nations War for Survival",
                            "Nations War of Liberation"
                        ))
                    )
            )
            .executesPlayer(WarCommands::warCreate);
    }

    public static CommandAPICommand commandDelete(boolean asAdmin) {
        return new CommandAPICommand("delete")
            .withPermission("AlathranWars.admin")
            .withArguments(
                CommandUtil.warWarArgument("war", asAdmin, ALL_WARS, "")
            )
            .executesPlayer(WarCommands::warDelete);
    }

    public static CommandAPICommand commandJoin(boolean asAdmin) {
        return new CommandAPICommand("join")
            .withArguments(
                CommandUtil.warWarArgument("war", asAdmin, asAdmin ? ALL_WARS : OUT_WAR, ""),
                CommandUtil.warSideCreateArgument("side", "war", asAdmin, !asAdmin, ""),
                new PlayerArgument("player").setOptional(true).withPermission("AlathranWars.admin.join")
            )
            .executesPlayer((Player p, CommandArguments args) -> warJoinPlayer(p, args, false));
    }

    public static CommandAPICommand commandSurrender(boolean asAdmin) {
        return new CommandAPICommand("surrender")
            .withArguments(
                CommandUtil.warWarArgument("war", asAdmin, asAdmin ? ALL_WARS : IN_WAR, "player"),
                new PlayerArgument("player")
                    .setOptional(true)
                    .withPermission("AlathranWars.admin")
            )
            .executesPlayer((Player p, CommandArguments args) -> warSurrender(p, args, false));
    }

    public static CommandAPICommand commandList() {
        return new CommandAPICommand("list")
            .executesPlayer(WarCommands::warList);
    }

    public static CommandAPICommand commandInfo() {
        return new CommandAPICommand("info")
            .withArguments(
                CommandUtil.warWarArgument("war", false, ALL_WARS, "player")
            )
            .executesPlayer(WarCommands::warInfo);
    }

    protected static void warCreate(CommandSender p, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        if (!(args.get("warlabel") instanceof final String argLabel))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You need to specify a war label.").build());

        if (Objects.equals(args.get("side1"), args.get("side2")))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>Cannot declare war on oneself.").build());

        final UUID uuid1 = (UUID) args.get("side1");
        final UUID uuid2 = (UUID) args.get("side2");

        // Create new war depending on what argument was specified
        @Nullable Town town1 = TownyAPI.getInstance().getTown(uuid1);
        @Nullable Town town2 = TownyAPI.getInstance().getTown(uuid2);

        // Automatically escalate to nation level if possible
        @Nullable Nation nation1 = town1 == null ? TownyAPI.getInstance().getNation(uuid1) : town1.getNationOrNull();
        @Nullable Nation nation2 = town2 == null ? TownyAPI.getInstance().getNation(uuid2) : town2.getNationOrNull();

        // Implement permission checking here if this is ever going to be exposed to players

        // Create war depending on what args have been passed, attempt to create nation wars first
        if (nation1 != null) {
            if (nation2 != null) { // Nation vs Nation

                // Allow civil wars
                if (nation2.getUUID() != nation1.getUUID()) {
                    new War(argLabel, nation1, nation2);
                } else {
                    new War(argLabel, town1, town2);
                }

            } else if (town2 != null) { // Nation vs Town

                new War(argLabel, nation1, town2);

            }
        } else if (town1 != null) {

            if (town2 != null) { // Town vs Town

                new War(argLabel, town1, town2);

            } else if (nation2 != null) { // Nation vs Town

                new War(argLabel, town1, nation2);

            }

        }
    }

    private static void warDelete(Player p, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        war.draw();
    }

    protected static void warJoinPlayer(Player p, @NotNull CommandArguments args, boolean asAdmin) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        if (!(args.get("side") instanceof final @NotNull Side side))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You need to specify a side.").build());

        if (!(args.getOptional("player").orElse(p) instanceof Player argPlayer))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>Player not found.").build());

        if (war.isPlayerInWar(argPlayer))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You are already in this war.").build());

        argPlayer.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "You have joined the war.").build());
        side.addPlayer(argPlayer);
        PlayerJoinListener.checkPlayer(argPlayer);
        for (@NotNull Siege siege : war.getSieges()) {
            if (siege.getAttackerSide().equals(side)) {
                siege.addPlayer(argPlayer, BattleSide.ATTACKER);
            } else {
                siege.addPlayer(argPlayer, BattleSide.DEFENDER);
            }
        }
    }

    protected static void warSurrender(@NotNull Player p, @NotNull CommandArguments args, boolean asAdmin) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        if (!(args.getOptional("player").orElse(p) instanceof Player argPlayer))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>Player not found.").build());

        // Towny Resident Object
        @Nullable Resident res = TownyAPI.getInstance().getResident(argPlayer);
        if (res == null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Resident invalid.").build());

        // Town check
        @Nullable Town town = res.getTownOrNull();
        if (town == null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>You are not in a town.").build());

        @Nullable Side side = war.getTownSide(town);
        if (side == null)
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>You are not in that war.").build());

        final @Nullable Nation resNation = res.getNationOrNull();
        final @Nullable Town resTown = res.getTownOrNull();

        // TODO Test if resTown is reached
        if (resNation != null) {
            if (p.hasPermission("AlathranWars.nationsurrender") || res.isKing() || asAdmin) {
                // Has nation surrender permission
                argPlayer.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "You have surrendered the war for " + resNation.getName() + ".").build());
                Bukkit.broadcast(ColorParser.of(UtilsChat.getPrefix() + "The nation of " + resNation.getName() + " has surrendered!").build());
                side.surrenderNation(resNation);
                side.processSurrenders();
            } else {
                // Cannot surrender nation involvement
                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "You cannot surrender for your nation.").build());
            }
        } else if (resTown != null) {
            if (p.hasPermission("AlathranWars.townsurrender") || res.isMayor() || asAdmin) {
                // Is in indepdenent town & has surrender perms
                argPlayer.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "You have surrendered the war for " + resTown.getName() + ".").build());
                Bukkit.broadcast(ColorParser.of(UtilsChat.getPrefix() + "The town of " + resTown.getName() + " has surrendered!").build());
                side.surrenderTown(resTown); // TODO Process siege surrender here as well
                side.processSurrenders();
            } else {
                // No perms
                throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "You cannot surrender war for your town.").build());
            }
        }
    }

    private static void warList(@NotNull Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        Set<War> wars = WarManager.getInstance().getWars();

        if (wars.isEmpty()) {
            p.sendMessage(ColorParser.of("<red>There are no wars at the moment, the world is at peace.").build());
            return;
        }

        final StringBuilder msg = new StringBuilder();

        for (War war : wars) {
            msg.append("\n<white><bold>%s<reset>".formatted(war.getLabel()));
            msg.append("\n <grey>Sides: <white>%s <grey>vs <white>%s".formatted(war.getSide1().getName(), war.getSide2().getName()));
            msg.append("\n <grey>Score: <white>%s <grey>vs <white>%s".formatted(war.getSide1().getScore(), war.getSide2().getScore()));
            msg.append("\n");
        }

        p.sendMessage(
            ColorParser.of(
                msg.toString()
            ).build()
        );
    }

    private static void warInfo(Player p, CommandArguments args) throws WrapperCommandSyntaxException {
        War war = (War) args.get("war");
        if (war == null) return;

        final StringBuilder msg = new StringBuilder();

        for (Side side : war.getSides()) {
            msg.append("\n<white><bold>%s<reset>".formatted(side.getName()));
            msg.append("\n <grey>Score: <green>%s".formatted(side.getScore()));
            msg.append("\n <grey>Nations: <green>%s<grey>/<red>%s".formatted(side.getNations().size(), side.getNations().size() + side.getSurrenderedNations().size()));
            msg.append("\n <grey>Towns: <green>%s<grey>/<red>%s".formatted(side.getTowns().size(), side.getTowns().size() + side.getSurrenderedTowns().size()));
            msg.append("\n <grey>Players: <green>%s<grey>/<red>%s".formatted(side.getPlayersIncludingOffline().size(), side.getPlayersIncludingOffline().size() + side.getSurrenderedPlayersIncludingOffline().size()));
            msg.append("\n");
        }

        p.sendMessage(
            ColorParser.of(
                msg.toString()
            ).build()
        );
    }
}
