package com.github.alathra.AlathranWars.commands;

import com.github.alathra.AlathranWars.Main;
import com.github.alathra.AlathranWars.conflict.Side;
import com.github.alathra.AlathranWars.conflict.War;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.enums.CommandArgsSiege;
import com.github.alathra.AlathranWars.enums.CommandArgsWar;
import com.github.alathra.AlathranWars.conflict.WarManager;
import com.github.alathra.AlathranWars.hooks.TownyHook;
import com.github.alathra.AlathranWars.items.WarItemRegistry;
import com.github.alathra.AlathranWars.utility.UtilsChat;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import dev.jorel.commandapi.SuggestionInfo;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandUtil {
    /*public Argument<World> customWorldArgument(String nodeName) {

        // Construct our CustomArgument that takes in a String input and returns a World object
        return new CustomArgument<World, String>(new StringArgument(nodeName), info -> {
            // Parse the world from our input
            World world = Bukkit.getWorld(info.input());

            if (world == null) {
                throw CustomArgumentException.fromMessageBuilder(new MessageBuilder("Unknown world: ").appendArgInput());
            } else {
                return world;
            }
        }).replaceSuggestions(ArgumentSuggestions.strings(info ->
            // List of world names on the server
            Bukkit.getWorlds().stream().map(World::getName).toArray(String[]::new))
        );
    }*/

    static public Argument<Nation> customNationArgument(String nodeName) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            final String argNationName = info.input();

            if (TownyAPI.getInstance().getNation(argNationName) == null)
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The nation does not exist!").parseLegacy().build());

            return TownyAPI.getInstance().getNation(argNationName);
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            final @NotNull List<String> nationNames = TownyHook.getNations()
                .stream()
                .map(Nation::getName)
                .sorted()
                .collect(Collectors.toList());

            return nationNames;
        }));
    }

    static public Argument<Town> customTownArgument(String nodeName) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            final String argTownName = info.input();

            if (TownyAPI.getInstance().getTown(argTownName) == null)
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The town does not exist!").parseLegacy().build());

            return TownyAPI.getInstance().getTown(argTownName);
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            final @NotNull List<String> townNames = TownyHook.getTowns()
                .stream()
                .map(Town::getName)
                .sorted()
                .collect(Collectors.toList());

            return townNames;
        }));
    }

    static public Argument<Nation> customNationNotInWarArgument(String nodeName, String warNodeName, boolean hideNationsInWar) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            final String argNationName = info.input();

            if (TownyAPI.getInstance().getNation(argNationName) == null)
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The nation does not exist!").parseLegacy().build());

            if (!hideNationsInWar || !(info.previousArgs().get(warNodeName) instanceof War war))
                return TownyAPI.getInstance().getNation(argNationName);

            @Nullable Nation nation = TownyAPI.getInstance().getNation(argNationName);

            if (war.getNations().contains(nation) || war.getSurrenderedNations().contains(nation))
                return nation;

            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The nation is already at war!").parseLegacy().build());
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            final @NotNull List<String> nationNames = TownyHook.getNations()
                .stream()
                .map(Nation::getName)
                .sorted()
                .collect(Collectors.toList());
            final @NotNull List<String> townNames = TownyHook.getTowns()
                .stream()
                .map(Town::getName)
                .sorted()
                .toList();

            nationNames.addAll(townNames);

            // Only proceed after this if we want to remove town already in the specified war
            if (!hideNationsInWar || !(info.previousArgs().get(warNodeName) instanceof War war))
                return nationNames;

            final @NotNull List<String> removeNames = Stream.concat( // Create a list of all nations in war
                war.getNations().stream().map(Nation::getName),
                war.getSurrenderedNations().stream().map(Nation::getName)
            ).toList();

            nationNames.removeAll(removeNames);

            return nationNames;
        }));
    }

    static public Argument<Town> customTownNotInWarArgument(String nodeName, String warNodeName, boolean hideTownsInWar) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            final String argTownName = info.input();

            if (TownyAPI.getInstance().getTown(argTownName) == null)
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The town does not exist!").parseLegacy().build());

            if (!hideTownsInWar || !(info.previousArgs().get(warNodeName) instanceof War war))
                return TownyAPI.getInstance().getTown(argTownName);

            @Nullable Town town = TownyAPI.getInstance().getTown(argTownName);

            if (war.getTowns().contains(town) || war.getSurrenderedTowns().contains(town))
                return town;

            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The town is already at war!").parseLegacy().build());
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            final @NotNull List<String> townNames = TownyHook.getTowns()
                .stream()
                .map(Town::getName)
                .sorted()
                .collect(Collectors.toList());

            // Only proceed after this if we want to remove town already in the specified war
            if (!hideTownsInWar || !(info.previousArgs().get(warNodeName) instanceof War war))
                return townNames;

            final @NotNull List<String> removeNames = Stream.concat( // Create a list of all nations & towns in war
                war.getTowns().stream().map(Town::getName),
                war.getSurrenderedTowns().stream().map(Town::getName)
            ).toList();

            townNames.removeAll(removeNames);

            return townNames;
        }));
    }

    static public Argument<Nation> customNationInWarArgument(String nodeName, String warNodeName) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            if (!(info.previousArgs().get(warNodeName) instanceof War war))
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The war does not exist!").parseLegacy().build());

            @Nullable Nation nation = TownyAPI.getInstance().getNation(info.input());

            if (nation == null)
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The nation does not exist!").parseLegacy().build());

            if (!war.isNationInWar(nation))
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The nation is not in that war!").parseLegacy().build());

            return nation;
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            if (!(info.previousArgs().get(warNodeName) instanceof War war))
                return Collections.emptyList();

            return Stream.concat( // Create a list of all nations & towns in war
                war.getNations().stream().map(Nation::getName),
                war.getSurrenderedNations().stream().map(Nation::getName)
            ).sorted().toList();
        }));
    }

    static public Argument<Town> customTownInWarArgument(String nodeName, String warNodeName) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            if (!(info.previousArgs().get(warNodeName) instanceof War war))
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The war does not exist!").parseLegacy().build());

            @Nullable Town town = TownyAPI.getInstance().getTown(info.input());

            if (town == null)
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The town does not exist!").parseLegacy().build());

            if (!war.isTownInWar(town))
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The town is not in that war!").parseLegacy().build());

            return town;
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            if (!(info.previousArgs().get(warNodeName) instanceof War war))
                return Collections.emptyList();

            return Stream.concat( // Create a list of all nations & towns in war
                war.getTowns().stream().map(Town::getName),
                war.getSurrenderedTowns().stream().map(Town::getName)
            ).sorted().toList();
        }));
    }


    /**
     * Accepts all towns and nations as argument. Returns a string.
     *
     * @param nodeName       the node name
     * @param warNodeName    the war node name
     * @param hideTownsInWar the hide towns in war
     * @return the town or nation UUID
     */
    static public Argument<UUID> customTownAndNationArgument(String nodeName, String warNodeName, boolean hideTownsInWar) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            final String argTownOrNationName = info.input();

            @Nullable Town town = TownyAPI.getInstance().getTown(argTownOrNationName);

            if (town != null)
                return town.getUUID();

            @Nullable Nation nation = TownyAPI.getInstance().getNation(argTownOrNationName);

            if (nation != null)
                return nation.getUUID();

            throw CustomArgument.CustomArgumentException.fromAdventureComponent(
                ColorParser.of(UtilsChat.getPrefix() + "<red>The town or nation <name> does not exist!")
                    .parseMinimessagePlaceholder("name", argTownOrNationName)
                    .build()
            );
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            final @NotNull List<String> nationNames = TownyHook.getNations()
                .stream()
                .map(Nation::getName)
                .sorted()
                .collect(Collectors.toList());
            final @NotNull List<String> townNames = TownyHook.getTowns()
                .stream()
                .map(Town::getName)
                .sorted()
                .toList();

            nationNames.addAll(townNames);

            // Only proceed after this if we want to remove town already in the specified war
            if (!hideTownsInWar || !(info.previousArgs().get(warNodeName) instanceof War war))
                return nationNames;

            final @NotNull List<String> removeNames = Stream.concat( // Create a list of all nations & towns in war
                war.getNations().stream().map(Nation::getName),
                Stream.concat(
                    war.getSurrenderedNations().stream().map(Nation::getName),
                    Stream.concat(
                        war.getTowns().stream().map(Town::getName),
                        war.getSurrenderedTowns().stream().map(Town::getName)
                    )
                )
            ).toList();

            nationNames.removeAll(removeNames);

            return nationNames;
        }));
    }

    public static Argument<Town> customSiegeAttackableTownArgument(String nodeName, String warNodeName, final boolean checkIfIn, final boolean checkIfSieged) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            if (!(info.previousArgs().get(warNodeName) instanceof War war))
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The war does not exist!").parseLegacy().build());

            @Nullable Town town = TownyAPI.getInstance().getTown(info.input());

            if (town == null)
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The town does not exist!").parseLegacy().build());

            if (!war.isTownInWar(town))
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The town is not in that war!").parseLegacy().build());

            if (checkIfSieged) {
                for (@NotNull Siege siege : WarManager.getInstance().getSieges()) {
                    if (siege.getTown().getUUID() == town.getUUID())
                        throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The town is already under siege!").parseLegacy().build());
                }
            }

            return town;
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            if (!(info.previousArgs().get(warNodeName) instanceof War war))
                return Collections.emptyList();

            final @NotNull List<String> townNames = new ArrayList<>();

            if (info.sender() instanceof Player p) {
                if (war.getSide1().isPlayerOnSide(p)) {
                    townNames.addAll(war.getSide2().getTowns()
                        .stream()
                        .map(TownyObject::getName)
                        .sorted()
                        .toList()
                    );
                } else if (war.getSide2().isPlayerOnSide(p)) {
                    townNames.addAll(war.getSide1().getTowns()
                        .stream()
                        .map(TownyObject::getName)
                        .sorted()
                        .toList()
                    );
                }
            }

            return townNames;
        }));
    }

    public static Argument<Siege> siegeSiegeArgument(String nodeName, final boolean isAdmin, final CommandArgsSiege checkSiege, final String playerNodeName) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            final String argSiegeName = info.input();

            @Nullable UUID siegeUUID = null;
            for (Siege siege : WarManager.getInstance().getSieges()) {
                if (siege.getName().equals(argSiegeName)) {
                    siegeUUID = siege.getUUID();
                }
            }

            final @Nullable Siege siege = WarManager.getInstance().getSiege(siegeUUID);
            if (siege == null)
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The siege <siege> does not exist!").parseMinimessagePlaceholder("siege", argSiegeName).build());

            switch (checkSiege) {
                case IN_SIEGE -> {
                    if (isAdmin) {
                        if (!(info.previousArgs().get(playerNodeName) instanceof Player player))
                            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The player does not exist!").build());

                        if (!siege.isPlayerInSiege(player))
                            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The player is already in that war.").build());
                    } else {
                        if (!(info.sender() instanceof Player player))
                            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>Only players can execute this command!").build());

                        if (!siege.isPlayerInSiege(player))
                            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You are already in this war.").build());
                    }
                }
                case OUT_SIEGE -> {
                    if (isAdmin) {
                        if (!(info.previousArgs().get(playerNodeName) instanceof Player player))
                            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The player does not exist!").build());

                        if (siege.isPlayerInSiege(player))
                            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The player is not in that war.").build());
                    } else {
                        if (!(info.sender() instanceof Player player))
                            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>Only players can execute this command!").build());

                        if (siege.isPlayerInSiege(player))
                            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You are not in that war.").build());
                    }
                }
            }

            return siege;
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> switch (checkSiege) {
            case ALL_SIEGES -> siegeAll(info, isAdmin);
            case IN_SIEGE -> siegeInside(info, isAdmin, playerNodeName);
            case OUT_SIEGE -> siegeOutside(info, isAdmin, playerNodeName);
        }));
    }

    private static List<String> siegeAll(SuggestionInfo<CommandSender> info, final boolean isAdmin) {
        return WarManager.getInstance().getSieges().stream().map(Siege::getName).toList();
    }

    private static List<String> siegeOutside(SuggestionInfo<CommandSender> info, final boolean isAdmin, final String playerNodeName) {
        List<String> siegeNames = new ArrayList<>();

        if (isAdmin) {
            if (info.previousArgs().get(playerNodeName) instanceof Player player) {
                siegeNames = WarManager.getInstance().getSieges().stream().filter(siege -> !siege.isPlayerInSiege(player)).map(Siege::getName).toList();
            }
        } else {
            if (info.sender() instanceof Player player) {
                siegeNames = WarManager.getInstance().getSieges().stream().filter(siege -> !siege.isPlayerInSiege(player)).map(Siege::getName).toList();
            }
        }

        return siegeNames;
    }

    private static List<String> siegeInside(SuggestionInfo<CommandSender> info, final boolean isAdmin, final String playerNodeName) {
        List<String> siegeNames = new ArrayList<>();

        if (isAdmin) {
            if (info.previousArgs().get(playerNodeName) instanceof Player player) {
                siegeNames = WarManager.getInstance().getSieges().stream().filter(siege -> siege.isPlayerInSiege(player)).map(Siege::getName).toList();
            }
        } else {
            if (info.sender() instanceof Player player) {
                siegeNames = WarManager.getInstance().getSieges().stream().filter(siege -> siege.isPlayerInSiege(player)).map(Siege::getName).toList();
            }
        }

        return siegeNames;
    }

    public static Argument<War> warWarArgument(String nodeName, final boolean isAdmin, final CommandArgsWar checkWar, final String playerNodeName) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            final String argWarName = info.input();

            final @Nullable War war = WarManager.getInstance().getWar(argWarName);
            if (war == null)
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The war <war> does not exist!").parseMinimessagePlaceholder("war", argWarName).build());

            switch (checkWar) {
                case IN_WAR -> {
                    if (isAdmin) {
                        if (!(info.previousArgs().get(playerNodeName) instanceof Player player))
                            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The player does not exist!").parseLegacy().build());

                        if (!war.isPlayerInWar(player))
                            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The player is already in that war.").parseLegacy().build());
                    } else {
                        if (!(info.sender() instanceof Player player))
                            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>Only players can execute this command!").parseLegacy().build());

                        if (!war.isPlayerInWar(player))
                            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You are already in this war.").parseLegacy().build());
                    }
                }
                case OUT_WAR -> {
                    if (isAdmin) {
                        if (!(info.previousArgs().get(playerNodeName) instanceof Player player))
                            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The player does not exist!").parseLegacy().build());

                        if (war.isPlayerInWar(player))
                            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The player is not in that war.").parseLegacy().build());
                    } else {
                        if (!(info.sender() instanceof Player player))
                            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>Only players can execute this command!").parseLegacy().build());

                        if (war.isPlayerInWar(player))
                            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You are not in that war.").parseLegacy().build());
                    }
                }
            }

            return war;
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> switch (checkWar) {
            case ALL_WARS -> warsAll(info, isAdmin);
            case IN_WAR -> warsInside(info, isAdmin, playerNodeName);
            case OUT_WAR -> warsOutside(info, isAdmin, playerNodeName);
        }));
    }

    private static List<String> warsAll(SuggestionInfo<CommandSender> info, final boolean isAdmin) {
        return WarManager.getInstance().getWarNames();
    }

    private static List<String> warsOutside(SuggestionInfo<CommandSender> info, final boolean isAdmin, final String playerNodeName) {
        List<String> warNames = new ArrayList<>();

        if (isAdmin) {
            if (info.previousArgs().get(playerNodeName) instanceof Player player) {
                warNames = WarManager.getInstance().getWars().stream().filter(war -> !war.isPlayerInWar(player)).map(War::getName).toList();
            }
        } else {
            if (info.sender() instanceof Player player) {
                warNames = WarManager.getInstance().getWars().stream().filter(war -> !war.isPlayerInWar(player)).map(War::getName).toList();
            }
        }

        return warNames;
    }

    private static List<String> warsInside(SuggestionInfo<CommandSender> info, final boolean isAdmin, final String playerNodeName) {
        List<String> warNames = new ArrayList<>();

        if (isAdmin) {
            if (info.previousArgs().get(playerNodeName) instanceof Player player) {
                warNames = WarManager.getInstance().getWars().stream().filter(war -> war.isPlayerInWar(player)).map(War::getName).toList();
            }
        } else {
            if (info.sender() instanceof Player player) {
                warNames = WarManager.getInstance().getWars().stream().filter(war -> war.isPlayerInWar(player)).map(War::getName).toList();
            }
        }

        return warNames;
    }

    public static Argument<Side> warSideCreateArgument(final String nodeName, final String warNodeName, final boolean isAdmin, final boolean checkIfIn, final String playerNodeName) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            if (!(info.previousArgs().get(warNodeName) instanceof War war))
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You need to specify a war.").parseLegacy().build());

            final String argSideName = info.input();

            if (!war.isSideValid(argSideName))
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The side does not exist.").parseLegacy().build());

            final @Nullable Side side = war.getSide(argSideName);

            if (side == null)
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The side is invalid.").parseLegacy().build());

            if (checkIfIn) {
                if (isAdmin) {
                    if (!(info.previousArgs().get(playerNodeName) instanceof Player player))
                        throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The player does not exist!").parseLegacy().build());

                    if (side.getPlayers().contains(player))
                        throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>The player is already on that side.").parseLegacy().build());
                } else {
                    if (!(info.sender() instanceof Player player))
                        throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>Only players can execute this command!").parseLegacy().build());

                    if (side.getPlayers().contains(player))
                        throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You are already on that side.").parseLegacy().build());
                }
            }

            return side;
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
                if (!(info.previousArgs().get(warNodeName) instanceof War war))
                    return Collections.emptyList();

                final @NotNull List<String> sideNames = new ArrayList<>();

                if (checkIfIn) {
                    if (isAdmin) {
                        if (info.previousArgs().get(playerNodeName) instanceof Player player) {
                            for (@NotNull Side side : war.getSides()) {
                                if (!side.getPlayers().contains(player))
                                    sideNames.add(side.getName());
                            }
                        }
                    } else {
                        if (info.sender() instanceof Player player) {
                            for (@NotNull Side side : war.getSides()) {
                                if (!side.getPlayers().contains(player))
                                    sideNames.add(side.getName());
                            }
                        }
                    }
                } else {
                    sideNames.add(war.getSide1().getName());
                    sideNames.add(war.getSide2().getName());
                }

                return sideNames;
            }
        ));
    }

    public static Argument<String> warTargetCreateArgument(final String nodeName, final String warNodeName, final boolean isAdmin) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            final String argTargetName = info.input();

            if (!(info.previousArgs().get(warNodeName) instanceof War war))
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>You need to specify a war.").parseLegacy().build());

            final boolean isNation = TownyAPI.getInstance().getNation(argTargetName) != null;
            final boolean isTown = TownyAPI.getInstance().getTown(argTargetName) != null;
            final boolean isPlayer = Bukkit.getPlayer(argTargetName) != null;

            if (isNation && !war.getNations().stream().map(Nation::getName).toList().contains(argTargetName))
                return argTargetName;

            if (isTown && !war.getTowns().stream().map(Town::getName).toList().contains(argTargetName))
                return argTargetName;

            if (isPlayer && !war.getPlayers().stream().map(Player::getName).toList().contains(argTargetName))
                return argTargetName;

            throw CustomArgument.CustomArgumentException.fromAdventureComponent(ColorParser.of(UtilsChat.getPrefix() + "<red>Invalid target.").build());
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> { // Returns list of every nation, town and player not in the war
                if (!(info.previousArgs().get(warNodeName) instanceof War war))
                    return Collections.emptyList();

                // Get all participants in war into list
                final List<String> inWar = Stream.concat(
                    war.getPlayers().stream().map(Player::getName),
                    Stream.concat(
                        war.getNations().stream().map(Nation::getName),
                        war.getTowns().stream().map(Town::getName)
                    )
                ).toList();

                // Make list of all nations, towns and player
                final List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
                final List<String> towns = TownyAPI.getInstance().getTowns().stream().map(Town::getName).toList();
                final List<String> nations = TownyAPI.getInstance().getNations().stream().map(Nation::getName).toList();

                return Stream.concat(
                        nations.stream(),
                        Stream.concat(
                            towns.stream(),
                            players.stream()
                        )
                    )
                        .filter(string -> !inWar.contains(string)) // Remove participants in te war
                        .toList();
            }
        ));
    }

    /**
     * @param player player to check
     * @return 0, 1, 2 (good to go, insufficient join, insufficient playtime)
     */
    public static int isPlayerMinuteman(String player) {
        @Nullable Resident res = TownyAPI.getInstance().getResident(player);
        if (res != null) {
            //getInstance registration time
            long regTime = res.getRegistered();
            int playTicks = Bukkit.getPlayer(res.getUUID()).getStatistic(Statistic.PLAY_ONE_MINUTE);
            int out = 0;
            if (Main.getInstance().getConfig().getInt("minimumPlayerAge") > 0) {
                //compare the join date, if they joined less that min age ago it is true
                if ((System.currentTimeMillis() - regTime) < 86400000L * Main.getInstance().getConfig().getInt("minimumPlayerAge"))
                    out = 1;
            }
            if (Main.getInstance().getConfig().getInt("minimumPlayTime") > 0) {
                //check playtime, if its less than min its true
                if (playTicks < (Main.getInstance().getConfig().getInt("minimumPlayTime") * (60 * 60 * 20))) out = 2;
            }

            return out;
        }
        //if the player doesnt exist just return true
        return 0;
    }

    /**
     * return list of all alathranwars items with namespace
     *
     * @return list of all alathranwars items namespaced
     */
    public static @NotNull List<String> getWarItems() {
        return new ArrayList<>(WarItemRegistry.getInstance().getItemRegistry().keySet());
    }
}
