package me.ShermansWorld.AlathraWar.commands;

import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.conflict.War;
import me.ShermansWorld.AlathraWar.conflict.battle.Side;
import me.ShermansWorld.AlathraWar.holder.WarManager;
import me.ShermansWorld.AlathraWar.hooks.TownyHook;
import me.ShermansWorld.AlathraWar.items.WarItemRegistry;
import me.ShermansWorld.AlathraWar.utility.UtilsChat;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe nation does not exist!").parseLegacy().build());

            return TownyAPI.getInstance().getNation(argNationName);
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            final List<String> nationNames = TownyHook.getNations()
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
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe town does not exist!").parseLegacy().build());

            return TownyAPI.getInstance().getTown(argTownName);
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            final List<String> townNames = TownyHook.getTowns()
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
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe nation does not exist!").parseLegacy().build());

            if (!hideNationsInWar || !(info.previousArgs().get(warNodeName) instanceof War war))
                return TownyAPI.getInstance().getNation(argNationName);

            Nation nation = TownyAPI.getInstance().getNation(argNationName);

            if (war.getNations().contains(nation) || war.getSurrenderedNations().contains(nation))
                return nation;

            throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe nation is already at war!").parseLegacy().build());
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            final List<String> nationNames = TownyHook.getNations()
                .stream()
                .map(Nation::getName)
                .sorted()
                .collect(Collectors.toList());
            final List<String> townNames = TownyHook.getTowns()
                .stream()
                .map(Town::getName)
                .sorted()
                .collect(Collectors.toList());

            nationNames.addAll(townNames);

            // Only proceed after this if we want to remove town already in the specified war
            if (!hideNationsInWar || !(info.previousArgs().get(warNodeName) instanceof War war))
                return nationNames;

            final List<String> removeNames = Stream.concat( // Create a list of all nations in war
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
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe town does not exist!").parseLegacy().build());

            if (!hideTownsInWar || !(info.previousArgs().get(warNodeName) instanceof War war))
                return TownyAPI.getInstance().getTown(argTownName);

            Town town = TownyAPI.getInstance().getTown(argTownName);

            if (war.getTowns().contains(town) || war.getSurrenderedTowns().contains(town))
                return town;

            throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe town is already at war!").parseLegacy().build());
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            final List<String> townNames = TownyHook.getTowns()
                .stream()
                .map(Town::getName)
                .sorted()
                .collect(Collectors.toList());

            // Only proceed after this if we want to remove town already in the specified war
            if (!hideTownsInWar || !(info.previousArgs().get(warNodeName) instanceof War war))
                return townNames;

            final List<String> removeNames = Stream.concat( // Create a list of all nations & towns in war
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
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe war does not exist!").parseLegacy().build());

            Nation nation = TownyAPI.getInstance().getNation(info.input());

            if (nation == null)
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe nation does not exist!").parseLegacy().build());

            if (!war.isNationInWar(nation))
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe nation is not in that war!").parseLegacy().build());

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
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe war does not exist!").parseLegacy().build());

            Town town = TownyAPI.getInstance().getTown(info.input());

            if (town == null)
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe town does not exist!").parseLegacy().build());

            if (!war.isTownInWar(town))
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe town is not in that war!").parseLegacy().build());

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

    static public Argument<String> customTownAndNationsArgument(String nodeName, String warNodeName, boolean hideTownsInWar) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            final String argTownOrNationName = info.input();

            if (TownyAPI.getInstance().getTown(argTownOrNationName) != null)
                return TownyAPI.getInstance().getTown(argTownOrNationName).getName();

            if (TownyAPI.getInstance().getNation(argTownOrNationName) != null)
                return TownyAPI.getInstance().getNation(argTownOrNationName).getName();

            throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe town or nation does not exist!").parseLegacy().build());
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            final List<String> nationNames = TownyHook.getNations()
                .stream()
                .map(Nation::getName)
                .sorted()
                .collect(Collectors.toList());
            final List<String> townNames = TownyHook.getTowns()
                .stream()
                .map(Town::getName)
                .sorted()
                .collect(Collectors.toList());

            nationNames.addAll(townNames);

            // Only proceed after this if we want to remove town already in the specified war
            if (!hideTownsInWar || !(info.previousArgs().get(warNodeName) instanceof War war))
                return nationNames;

            final List<String> removeNames = Stream.concat( // Create a list of all nations & towns in war
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

    /*static public Argument<String> warSideCreateArgument(String nodeName) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            final String argSideName = info.input();

            return argSideName;
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            final List<Nation> nations = TownyHook.getNations();
            final List<Town> towns = TownyHook.getTowns();

            final List<String> nationNames = new ArrayList<>();
            final List<String> townNames = new ArrayList<>();

            for (Nation nation : nations)
                nationNames.add(nation.getName());

            for (Town town : towns)
                townNames.add(town.getName());

            Collections.sort(nationNames);
            Collections.sort(townNames);
            nationNames.addAll(townNames);

            return nationNames;
        }));
    }*/


    public static Argument<War> warWarArgument(String nodeName, final boolean isAdmin, final boolean checkIfIn, final String playerNodeName) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            final String argWarName = info.input();

            final War war = WarManager.getInstance().getWar(argWarName);
            if (war == null)
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe war <war> does not exist!").parseLegacy().parseMinimessagePlaceholder("war", argWarName).build());

            if (checkIfIn) {
                if (isAdmin) {
                    if (!(info.previousArgs().get(playerNodeName) instanceof Player player))
                        throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe player does not exist!").parseLegacy().build());

                    if (war.isPlayerInWar(player))
                        throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe player is already in that war.").parseLegacy().build());
                } else {
                    if (!(info.sender() instanceof Player player))
                        throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cOnly players can execute this command!").parseLegacy().build());

                    if (war.isPlayerInWar(player))
                        throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cYou are already in this war.").parseLegacy().build());
                }
            }

            return war;

        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
            final List<String> warNames = new ArrayList<>();

            if (checkIfIn) {
                if (isAdmin) {
                    if (info.previousArgs().get(playerNodeName) instanceof Player player) {
                        for (String name : WarManager.getInstance().getWarNames()) {
                            if (WarManager.getInstance().getWar(name) != null && !WarManager.getInstance().getWar(name).isPlayerInWar(player))
                                warNames.add(name);
                        }
                    }
                } else {
                    if (info.sender() instanceof Player player) {
                        for (String name : WarManager.getInstance().getWarNames()) {
                            if (WarManager.getInstance().getWar(name) != null && !WarManager.getInstance().getWar(name).isPlayerInWar(player))
                                warNames.add(name);
                        }
                    }
                }
            } else {
                warNames.addAll(WarManager.getInstance().getWarNames());
            }

            return warNames;
        }));
    }

    public static Argument<Side> warSideCreateArgument(final String nodeName, final String warNodeName, final boolean isAdmin, final boolean checkIfIn, final String playerNodeName) {
        return new CustomArgument<>(new StringArgument(nodeName), info -> {
            if (!(info.previousArgs().get(warNodeName) instanceof War war))
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cYou need to specify a war.").parseLegacy().build());

            final String argSideName = info.input();

            if (!war.isSideValid(argSideName))
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe side does not exist.").parseLegacy().build());

            final Side side = war.getSide(argSideName);

            if (side == null)
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe side is invalid.").parseLegacy().build());

            if (checkIfIn) {
                if (isAdmin) {
                    if (!(info.previousArgs().get(playerNodeName) instanceof Player player))
                        throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe player does not exist!").parseLegacy().build());

                    if (side.getPlayers().contains(player))
                        throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cThe player is already on that side.").parseLegacy().build());
                } else {
                    if (!(info.sender() instanceof Player player))
                        throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cOnly players can execute this command!").parseLegacy().build());

                    if (side.getPlayers().contains(player))
                        throw CustomArgument.CustomArgumentException.fromAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "&cYou are already on that side.").parseLegacy().build());
                }
            }

            return side;
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
                 if (!(info.previousArgs().get(warNodeName) instanceof War war))
                    return Collections.emptyList();

                final List<String> sideNames = new ArrayList<>();

                if (checkIfIn) {
                    if (isAdmin) {
                        if (info.previousArgs().get(playerNodeName) instanceof Player player) {
                            for (Side side : war.getSides()) {
                                if (!side.getPlayers().contains(player))
                                    sideNames.add(side.getName());
                            }
                        }
                    } else {
                        if (info.sender() instanceof Player player) {
                            for (Side side : war.getSides()) {
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

    /**
     * @param player player to check
     * @return 0, 1, 2 (good to go, insufficient join, insufficient playtime)
     */
    public static int isPlayerMinuteman(String player) {
        Resident res = TownyAPI.getInstance().getResident(player);
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
     * return list of all alathrawar items with namespace
     *
     * @return list of all alathrawar items namespaced
     */
    public static List<String> getWarItems() {
        return new ArrayList<>(WarItemRegistry.getInstance().getItemRegistry().keySet());
    }
}
