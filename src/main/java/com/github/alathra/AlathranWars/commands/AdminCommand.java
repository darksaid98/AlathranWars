package com.github.alathra.AlathranWars.commands;

import com.github.alathra.AlathranWars.Main;
import com.github.alathra.AlathranWars.conflict.Side;
import com.github.alathra.AlathranWars.items.WarItemRegistry;
import com.github.alathra.AlathranWars.utility.UtilsChat;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.object.Town;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import com.github.alathra.AlathranWars.conflict.War;
import com.github.alathra.AlathranWars.enums.AdminCommandFailEnum;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AdminCommand {

    public AdminCommand() {

        /**
         * //done
         * //edit war/event in real time, side can be "both" to effect both
         * -modify raid score [war] [town] [value]
         * -modify raid townspawn [war] [town] (x) (Z)
         * -modify raid gather [war] [town] [town]
         * -modify raid phase [war] [town] [phase] //"next" to move to next phase
         * -modify raid loot [war] [town] (x) (z) [value,looted,ticks,reset] [amt] //no coords just does current chunk, reset deletes it from the list
         * -modify raid time [war] [town] [add/set] [value]
         * -modify raid owner [war] [town] [add/set] [value]
         * -modify raid move [war] [town] [newWar] //low priority, moves raid to other war
         * -modify raid clearActive [war] [town] //low priority
         * <p>
         * -modify siege score [war] [town] [side] [amt]
         * -modify siege homeblock [war] [town] (x) (Z)
         * -modify siege time [war] [town] [add/set/max] [value] //max modified the max length
         * -modify siege owner [war] [town] [add/set] [value]
         * -modify siege move [war] [town] [newWar] //low priority, moves siege to other war
         * <p>
         * -modify war score [war] [side] [amt]
         * -modify war side [war]  [side] [name]
         * -modify war name [war] [name]
         * -modify war add town [war] [town]
         * -modify war add nation [war] [nation]
         * -modify war surrender town [war] [town] //adds town to surrender list
         * -modify war surrender nation [war] [town] //adds all towns to surrender list
         * -modify war raidTime [add,set,reset] [war] [town] [amt] //set when last raid was
         * <p>
         * // low priority idea
         * -info war score [war]
         * -info war surrenderedTowns [war]
         * -info war raids [war]
         * -info war sieges [war]
         * -info war towns [war]
         * -info war lastRaidTime [war]
         * <p>
         * -info raid homeblock [war] [town]
         * -info raid lootedTownBlocks [war] [town]
         * -info raid timeSinceLastRaid [town]
         * <p>
         * -info siege homeblock [war] [town]
         * <p>
         * //done
         * //force make event or war, if owner isnt defined, idk havent decided
         * -create siege [war] [town] (owner)
         * -create raid [war] [raidTown] (gatherTown) (owner)
         * -create war [name] [side1] [side2]
         * <p>
         * //force end a war/event, can declare winner side, or no winner
         * -force end war [war] (side/victor)
         * -force end siege [war] [town] (side/victor)
         * -force end raid [war] [town] (side/victor)
         * <p>
         * //done
         * //force player into or out of a war/event
         * -force join war [player] [war] [side]
         * -force join siege [player] [war] [town] (side)
         * -force join raid [player] [war] [town] (side)
         * <p>
         * //done
         * -force leave raid [war] [player] (timeout) //kicks from raid party
         * <p>
         * // Ultra low priority idea
         * -rule raidersRespawnAtGatherTown [true/false]
         * -rule siegersRespawnAtTown [true/false]
         * -rule anyoneCanJoinRaid [true/false]
         *
         * @param sender  Source of the command
         * @param command Command which was executed
         * @param label   Alias of the command which was used
         * @param args    Passed command arguments
         * @return Valid command
         */
        new CommandAPICommand("alathranwarsadmin")
            .withAliases("awa")
            .withPermission("AlathranWars.admin")
            .withSubcommands(
                commandItem(),
//                commandPurgeBars(),
//                commandSave(),
//                commandSaveAll(),
                commandLoad(),
//                commandLoadAll(),
                commandCreate(),
                commandForce(),
                commandHelp(),
                commandInfo()/*,
                commandModify()*/
            )
            .executes((sender, args) -> {
                fail(sender, args, AdminCommandFailEnum.SYNTAX);
            })
            .register();
    }

    /*@NotNull
    private static StringBuilder getDefenderPlayers(OldRaid oldRaid) {
        StringBuilder defenderPlayers = new StringBuilder();
        for (String pl : oldRaid.getDefenderPlayers()) {
            defenderPlayers.append(pl);
            defenderPlayers.append(", ");
        }
        //cut off last two characters
        if (defenderPlayers.length() > 2)
            defenderPlayers = new StringBuilder(defenderPlayers.substring(0, defenderPlayers.length() - 2));
        return defenderPlayers;
    }

    @NotNull
    private static StringBuilder getActiveRaiders(OldRaid oldRaid) {
        StringBuilder activeRaiders = new StringBuilder();
        for (String pl : oldRaid.getActiveRaiders()) {
            activeRaiders.append(pl);
            activeRaiders.append(", ");
        }
        //cut off last two characters
        if (activeRaiders.length() > 2)
            activeRaiders = new StringBuilder(activeRaiders.substring(0, activeRaiders.length() - 2));
        return activeRaiders;
    }*/

    @NotNull
    private static StringBuilder getSurrenderedTowns(War war) {
        StringBuilder surrenderedTowns = new StringBuilder();
        for (Town t : war.getSurrenderedTowns()) {
            surrenderedTowns.append(t.getName());
            surrenderedTowns.append(", ");
        }
        //cut off last two characters
        if (surrenderedTowns.length() > 2)
            surrenderedTowns = new StringBuilder(surrenderedTowns.substring(0, surrenderedTowns.length() - 2));
        return surrenderedTowns;
    }


    private static boolean fail(CommandSender p, CommandArguments args, AdminCommandFailEnum type) throws WrapperCommandSyntaxException {
        switch (type) {
            case PERMISSIONS -> {
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>You do not have permission to do this.").build());
            }
            case SYNTAX -> {
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "Invalid Arguments. /alathranwarsadmin help").build());
            }
            default -> {
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "Something wrong. /alathranwarsadmin help").build());
            }
        }
    }

    /*private static void finalizeRaid(OldRaid oldRaid) {
        oldRaid.save();
    }

    private static void finalizeSiege(Siege oldSiege) {
        oldSiege.save();
    }*/

    private static void finalizeWar(War war) {
//        war.save();
        // TODO Saving logic
    }

    private CommandAPICommand commandItem() {
        return new CommandAPICommand("item")
            .withArguments(
                new StringArgument("item")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(
                            CommandUtil.getWarItems()
                        )
                    ),
                new IntegerArgument("amount").setOptional(true),
                new PlayerArgument("player").setOptional(true)
            )
            .executesPlayer((Player sender, CommandArguments args) -> {
                // Parse item
                if (!(args.get("item") instanceof String argItem))
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>Invalid item argument.").build());

                ItemStack stack;
                if (argItem.contains(Main.getInstance().getName().toLowerCase())) {
                    stack = WarItemRegistry.getInstance().getOrNullNamespace(argItem);
                } else {
                    stack = WarItemRegistry.getInstance().getOrNull(argItem);
                }

                if (stack == null)
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>The item is not an AlathranWars item.").build());

                // Parse amount
                int argAmount = (int) args.getOptional("amount").orElse(1);

                if (argAmount > 64)
                    argAmount = 64;
                else if (argAmount < 1) {
                    argAmount = 1;
                }
                stack.setAmount(argAmount);

                // Determine who gets the item
                Player argPlayer = (Player) args.getOptional("player").orElse(sender);

                //give
                argPlayer.getInventory().addItem(stack);

                //log
                final Component name = stack.getItemMeta() == null ? new ColorParser(stack.toString()).build() : stack.getItemMeta().displayName();
                sender.sendMessage(UtilsChat.getPrefix() + "Gave " + stack.getAmount() + "x " + name + " to " + argPlayer.getName());
                Main.warLogger.log(UtilsChat.getPrefix() + "Gave " + stack.getAmount() + "x " + name + " to " + argPlayer.getName());
            });
    }

    /*private CommandAPICommand commandPurgeBars() {
        return new CommandAPICommand("purgebars")
            .executes((CommandSender sender, CommandArguments args) -> {
                for (Iterator<KeyedBossBar> it = Bukkit.getBossBars(); it.hasNext(); ) {
                    KeyedBossBar b = it.next();
                    if (b.getKey().getNamespace().equalsIgnoreCase(Main.getInstance().getName())) {
                        b.setVisible(false);
                        b.removeAll();
                        Bukkit.removeBossBar(b.getKey());
                    }
                }

                sender.sendMessage(UtilsChat.getPrefix() + "<red>Cleared all boss bars that have been registered with AlathranWars.");
                sender.sendMessage(UtilsChat.getPrefix() + "<red>Note: If any remain, please contact the plugin author or open an issue on GitHub. The data can be found under the tag CustomBossEvents: in level.dat! You will need an NBT Editing program to delete it manually.");

                saveAll(sender, args);
            });
    }*/

    /*private CommandAPICommand commandSave() {
        return new CommandAPICommand("save")
            .withArguments(
                new StringArgument("war")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(
                            WarManager.getInstance().getWarNames()
                        )
                    )
            )
            .executes((CommandSender sender, CommandArguments args) -> {
                if (!(args.get("war") instanceof final String argWarName))
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>You need to specify a war name.").build());

                for (War war : WarManager.getInstance().getWars()) {
                    if (war.getName().equalsIgnoreCase(argWarName)) {
                        WarManager.getInstance().saveWar(war);
                        sender.sendMessage(UtilsChat.getPrefix() + "<red>Forced Save of war: " + war.getName());
                        return;
                    }
                }

                sender.sendMessage(UtilsChat.getPrefix() + "<red>War does not exist!");
            });
    }*/

    /*private CommandAPICommand commandSaveAll() {
        return new CommandAPICommand("save-all")
            .executes((CommandSender sender, CommandArguments args) -> {
                for (War war : WarManager.getInstance().getWars()) WarManager.getInstance().saveWar(war);
                sender.sendMessage(UtilsChat.getPrefix() + "<red>Forced Save of all wars");
            });
    }*/

    private CommandAPICommand commandLoad() {
        return new CommandAPICommand("load")
            .executes((CommandSender sender, CommandArguments args) -> {
                sender.sendMessage(new ColorParser(UtilsChat.getPrefix() + "<red>Support for loading wars individually is currently unimplemented. Use /awa load-all instead").build());
            });

                /*if (args.length >= 3) {
                    if(Boolean.parseBoolean(args[2])) {
                        for (War war : WarManager.getInstance().getWars()) {
                            int index = -1;
                            if(war.getName().equalsIgnoreCase(args[1])) {
                                //kill raids
                                for (OldRaid raid : war.getRaids()) {
                                    raid.stop();
                                }
                                //kill sieges
                                for (Siege siege : war.getSieges()) {
                                    siege.stop();
                                }
                                //delete wars
                                index = WarManager.getInstance().getWars().lastIndexOf(war);
                                WarManager.getInstance().getWars().remove(war);
                            }
                            if(index < 0) {
                                sender.sendMessage(UtilsChat.getPrefix() + "<red>Error! Failed to delete war, forcing end."));
                                return true;
                            }
                            //reload data
                            ;
                            sender.sendMessage(UtilsChat.getPrefix() + "<red>Forcefully Reloaded all Wars!"));
                            return true;
                        }
                    }
                }*/
    }

    /*private CommandAPICommand commandLoadAll() {
        return new CommandAPICommand("load-all")
            .withArguments(new BooleanArgument("boolean").setOptional(true))
            .executes((CommandSender sender, CommandArguments args) -> {
                boolean argsBoolean = (boolean) args.getOptional("boolean").orElse(false);

                if (argsBoolean) {
                    for (War war : WarManager.getInstance().getWars()) {
                        //kill raids
                        for (OldRaid oldRaid : war.getRaids()) {
                            oldRaid.stop();
                        }

                        //kill sieges
                        for (Siege oldSiege : war.getSieges()) {
                            oldSiege.stop();
                        }

                        //delete wars
                        WarManager.getInstance().getWars().remove(war);
                    }

                    //reload data
                    Main.initData();
                    sender.sendMessage(UtilsChat.getPrefix() + "<red>Forcefully Reloaded all Wars!");
                }

                sender.sendMessage(UtilsChat.getPrefix() + "Are you sure you want to do this?");
                sender.sendMessage(UtilsChat.getPrefix() + "Doing this will forcefully stop and reload every war live. It can be very dangerous and may not work as intended.");
                sender.sendMessage(UtilsChat.getPrefix() + "To confirm do /awa load-all true");
            });
    }*/

    private CommandAPICommand commandCreate() {
        return new CommandAPICommand("create")
            .withSubcommands(
                /*new CommandAPICommand("raid")
                    .withArguments(
                        new StringArgument("war")
                            .replaceSuggestions(
                                ArgumentSuggestions.strings(
                                    WarManager.getInstance().getWarsNames()
                                )
                            ),
                        new StringArgument("town")
                            .replaceSuggestions(
                                ArgumentSuggestions.stringCollection(info -> {
                                        final String warname = (String) info.previousArgs().get("war");
                                        return CommandUtil.getTownyWarTowns(warname);
                                    }
                                )
                            ),
                        new StringArgument("gathertown")
                            .setOptional(true)
                            .withPermission("AlathranWars.admin")
                            .replaceSuggestions(
                                ArgumentSuggestions.stringCollection(info -> { // TODO Make getHostileTowns method where we reverse from list
                                        final String warname = (String) info.previousArgs().get("war");
                                        return CommandUtil.getTownyWarTowns(warname);
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
                    .executesPlayer((Player p, CommandArguments args) -> RaidCommand.raidStart(p, args, true)),*/
                new CommandAPICommand("siege")
                    .withArguments(
                        CommandUtil.warWarArgument("war", false, false, ""),
                        CommandUtil.customTownInWarArgument("town", "war"),
                        new PlayerArgument("leader")
                            .setOptional(true)
                            .withPermission("AlathranWars.admin"),
                        new BooleanArgument("minutemen")
                            .setOptional(true)
                            .withPermission("AlathranWars.admin")
                    )
                    .executesPlayer((Player p, CommandArguments args) -> SiegeCommand.siegeStart(p, args, true)),
                new CommandAPICommand("war")
                    .withArguments(
                        CommandUtil.customTownAndNationsArgument("side1", "war", true),
                        CommandUtil.customTownAndNationsArgument("side2", "war", true),
                        new GreedyStringArgument("warlabel")
                            .replaceSuggestions(
                                ArgumentSuggestions.stringCollection(info -> List.of(
                                    "Nations War of Conquest",
                                    "Nations War for Survival",
                                    "Nations War of Liberation"
                                ))
                            )
                    )
                    .executesPlayer(WarCommand::warCreate)
            )
            .executesPlayer((Player p, CommandArguments args) -> {
                fail(p, args, AdminCommandFailEnum.SYNTAX);
            });
    }

    private CommandAPICommand commandForce() {
        return new CommandAPICommand("force")
            .withSubcommands(
                new CommandAPICommand("end")
                    .withSubcommands(
                        new CommandAPICommand("war")
                            .withArguments(
                                CommandUtil.warWarArgument("war", false, false, "player"),
                                CommandUtil.warSideCreateArgument("victor", "war", true, false, "")
                                    .setOptional(true)
                            ).executesPlayer((Player sender, CommandArguments args) -> {
                                //TODO determine if needed
                                sender.sendMessage("<red>Unused! use /war delete");
                            })/*,
                        new CommandAPICommand("siege") // TODO Very needed
                            .withArguments(
                                new StringArgument("siege")
                                    .replaceSuggestions(
                                        ArgumentSuggestions.strings(
                                            CommandUtil.getSieges()
                                        )
                                    ),
                                new StringArgument("victor")
                                    .setOptional(true)
                                    .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                            final String siegename = (String) info.previousArgs().get("oldSiege");

                                            final Siege oldSiege = SiegeData.getSiege(siegename);

                                            if (oldSiege == null)
                                                return Collections.emptyList();

                                            return List.of(oldSiege.getAttackerSide(), oldSiege.getDefenderSide(), "none");
                                        })
                                    )

                            ).executesPlayer((Player sender, CommandArguments args) -> {
                                if (!(args.get("oldSiege") instanceof final String argSiegeName))
                                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>You need to specify a oldSiege!").build());

                                final Siege oldSiege = SiegeData.getSiege(argSiegeName);
                                if (oldSiege == null)
                                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>That oldSiege does not exist!").build());

                                final War war = oldSiege.getWar();
                                if (war == null)
                                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>The war does not exist!").build());

                                final String argSide = (String) args.getOptional("victor").orElse("none");

                                // If tie
                                if (argSide.equals("none")) {
                                    oldSiege.noWinner();
                                    sender.sendMessage(UtilsChat.getPrefix() + "The oldSiege has forcefully been lifted at %s in war %s with no victor.".formatted(oldSiege.getTown().getName(), war.getName()));
                                    Main.warLogger.log("Siege of %s forcefully ended in war %s with no victor.".formatted(oldSiege.getTown().getName(), war.getName()));
                                    return;
                                }

                                final String message = "The oldSiege has forcefully been lifted at %s in war %s with the %s declared as victors.";
                                String victor;

                                // Figure who won & lost
                                if (oldSiege.getSide1AreAttackers()) {
                                    if (argSide.equalsIgnoreCase(war.getSide1())) {
                                        oldSiege.attackersWin(oldSiege.getSiegeOwner());
                                        victor = "(attackers)";
                                    } else {
                                        oldSiege.defendersWin();
                                        victor = "(defenders)";
                                    }
                                } else {
                                    if (argSide.equalsIgnoreCase(war.getSide1())) {
                                        oldSiege.defendersWin();
                                        victor = "(defenders)";
                                    } else {
                                        oldSiege.attackersWin(oldSiege.getSiegeOwner());
                                        victor = "(attackers)";
                                    }
                                }

                                sender.sendMessage(UtilsChat.getPrefix() + message.formatted(oldSiege.getTown().getName(), war.getName(), victor));
                                Main.warLogger.log("Siege of %s forcefully ended in war %s with %s declared as victor.".formatted(oldSiege.getTown().getName(), war.getName(), victor));
                            })*/ /*,
                        new CommandAPICommand("raid")
                            .withArguments(
                                new StringArgument("raid")
                                    .replaceSuggestions(
                                        ArgumentSuggestions.strings(
                                            CommandUtil.getRaids()
                                        )
                                    ),
                                new StringArgument("victor")
                                    .setOptional(true)
                                    .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                            final String raidname = (String) info.previousArgs().get("oldRaid");

                                            final OldRaid oldRaid = RaidData.getRaid(raidname);

                                            if (oldRaid == null)
                                                return Collections.emptyList();

                                            return List.of(oldRaid.getRaiderSide(), oldRaid.getDefenderSide(), "none");
                                        })
                                    )
                            ).executesPlayer((Player sender, CommandArguments args) -> {
                                if (!(args.get("oldRaid") instanceof final String argRaidName))
                                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>You need to specify a oldRaid name!").build());

                                final OldRaid oldRaid = RaidData.getRaid(argRaidName);
                                if (oldRaid == null)
                                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>That oldRaid does not exist!").build());

                                final War war = oldRaid.getWar();
                                if (war == null)
                                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>The war does not exist!").build());

                                final String argSide = (String) args.getOptional("victor").orElse("none");

                                // If tie
                                if (argSide.equals("none")) {
                                    oldRaid.noWinner();
                                    sender.sendMessage(UtilsChat.getPrefix() + "The oldRaid has forcefully been ended at %s in war %s with no victor.".formatted(oldRaid.getRaidedTown().getName(), war.getName()));
                                    Main.warLogger.log("OldRaid of %s forcefully ended in war %s with no victor.".formatted(oldRaid.getRaidedTown().getName(), war.getName()));
                                    return;
                                }

                                final String message = "The oldRaid has forcefully been ended at %s in war %s with the %s declared as victors.";
                                String victor;

                                // Figure who won & lost
                                if (oldRaid.getSide1AreRaiders()) {
                                    if (argSide.equalsIgnoreCase(war.getSide1())) {
                                        oldRaid.raidersWin(oldRaid.getOwner(), oldRaid.getRaiderScore(), oldRaid.getDefenderScore());
                                        victor = "(raiders)";
                                    } else {
                                        oldRaid.defendersWin(oldRaid.getRaiderScore(), oldRaid.getDefenderScore());
                                        victor = "(defenders)";
                                    }
                                } else {
                                    if (argSide.equalsIgnoreCase(war.getSide1())) {
                                        oldRaid.defendersWin(oldRaid.getRaiderScore(), oldRaid.getDefenderScore());
                                        victor = "(defenders)";
                                    } else {
                                        oldRaid.raidersWin(oldRaid.getOwner(), oldRaid.getRaiderScore(), oldRaid.getDefenderScore());
                                        victor = "(raiders)";
                                    }
                                }

                                sender.sendMessage(UtilsChat.getPrefix() + message.formatted(oldRaid.getRaidedTown().getName(), war.getName(), victor));
                                Main.warLogger.log("OldRaid of %s forcefully ended in war %s with %s declared as victor.".formatted(oldRaid.getRaidedTown().getName(), war.getName(), victor));
                            })*/
                    ),
                new CommandAPICommand("join")
                    .withSubcommands(
                        new CommandAPICommand("war")
                            .withArguments(
                                new PlayerArgument("player"),
                                CommandUtil.warWarArgument(
                                    "war",
                                    true,
                                    true,
                                    "player"
                                ),
                                CommandUtil.warSideCreateArgument(
                                    "side",
                                    "war",
                                    true,
                                    true,
                                    "player"
                                ).setOptional(true)
                                /*new StringArgument("side")
                                    .setOptional(true)
                                    .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                            final String warname = (String) info.previousArgs().get("war");

                                            final War war = WarManager.getInstance().getWar(warname);

                                            if (war == null)
                                                return Collections.emptyList();

                                            return war.getSides();
                                        })
                                    )*/
                            ).executesPlayer((Player sender, CommandArguments args) -> WarCommand.warJoinPlayer(sender, args, true))/*,
                        new CommandAPICommand("siege") // TODO Very needed
                            .withArguments(
                                new PlayerArgument("player"),
                                new StringArgument("siege")
                                    .replaceSuggestions(
                                        ArgumentSuggestions.strings(
                                            CommandUtil.getSieges()
                                        )
                                    ),
                                new StringArgument("side")
                                    .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                            final String siegename = (String) info.previousArgs().get("oldSiege");

                                            final Siege oldSiege = SiegeData.getSiege(siegename);

                                            if (oldSiege == null)
                                                return Collections.emptyList();

                                            return List.of(oldSiege.getAttackerSide(), oldSiege.getDefenderSide());
                                        })
                                    )
                            ).executesPlayer((Player sender, CommandArguments args) -> {
                                sender.sendMessage("<red>Error! Unimplemented!");
                                // TODO implement siege force joining?
                            })*/ /*,
                        new CommandAPICommand("raid")
                            .withArguments(
                                new PlayerArgument("player"),
                                new StringArgument("raid")
                                    .replaceSuggestions(
                                        ArgumentSuggestions.strings(
                                            CommandUtil.getRaids()
                                        )
                                    ),
                                new StringArgument("side")
                                    .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                            final String raidname = (String) info.previousArgs().get("oldRaid");

                                            final OldRaid oldRaid = RaidData.getRaid(raidname);

                                            if (oldRaid == null)
                                                return Collections.emptyList();

                                            return List.of(oldRaid.getRaiderSide(), oldRaid.getDefenderSide());
                                        })
                                    ),
                                new BooleanArgument("minutemen")
                                    .setOptional(true)
                            ).executesPlayer((Player sender, CommandArguments args) -> RaidCommand.raidJoin(sender, args, true))*/
                    ),
                new CommandAPICommand("leave")
                    .withSubcommands(
                                        /*new CommandAPICommand("war")
                                                .executesPlayer((Player sender, CommandArguments args) -> {

                                                })
                                        ,*/ // TODO unimplemented, original comment "determine if needed"
                                        /*new CommandAPICommand("siege")
                                                .executesPlayer((Player sender, CommandArguments args) -> {

                                                })
                                        ,*/ // TODO unimplemented, original comment "determine if needed"
                        /*new CommandAPICommand("raid").withArguments(
                            new StringArgument("war")
                                .replaceSuggestions(
                                    ArgumentSuggestions.stringCollection(info ->
                                        WarManager.getInstance().getWarsNames()
                                    )
                                ),
                            new StringArgument("town")
                                .replaceSuggestions(
                                    ArgumentSuggestions.stringCollection(info -> {
                                        final String warname = (String) info.previousArgs().get("war");
                                        return CommandUtil.getTownyWarTowns(warname);
                                    })
                                ),
                            new PlayerArgument("player")
                        ).executesPlayer((Player sender, CommandArguments args) -> RaidCommand.raidLeave(sender, args, true))*/
                    )
            )
            .executes((CommandSender sender, CommandArguments args) -> fail(sender, args, AdminCommandFailEnum.SYNTAX));
    }

    private CommandAPICommand commandHelp() {
        return new CommandAPICommand("help")
            .executes((CommandSender sender, CommandArguments args) -> {
                sender.sendMessage(UtilsChat.getPrefix() + "/awa create");
                sender.sendMessage(UtilsChat.getPrefix() + "/awa force");
                sender.sendMessage(UtilsChat.getPrefix() + "/awa help");
                sender.sendMessage(UtilsChat.getPrefix() + "/awa info");
                sender.sendMessage(UtilsChat.getPrefix() + "/awa modify");
                sender.sendMessage(UtilsChat.getPrefix() + "/awa purgebars");
                sender.sendMessage(UtilsChat.getPrefix() + "/awa save");
                sender.sendMessage(UtilsChat.getPrefix() + "/awa save-all");
                sender.sendMessage(UtilsChat.getPrefix() + "/awa load-all");
                sender.sendMessage(UtilsChat.getPrefix() + "/awa item");
            });
    }

    private CommandAPICommand commandInfo() {
        return new CommandAPICommand("info")
            .withSubcommands(
                new CommandAPICommand("war")
                    .withArguments(
                        CommandUtil.warWarArgument(
                            "war",
                            false,
                            false,
                            ""
                        )
                    )
                    .executes((CommandSender sender, CommandArguments args) -> {
                            if (!(args.get("war") instanceof final War war))
                                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>You need to specify a war!").build());

//                            final War war = WarManager.getInstance().getWar(argWarName);
//                            if (war == null)
//                                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>That war does not exist!").build());

                            sender.sendMessage(UtilsChat.getPrefix() + "Info dump for war: " + war.getName());
                            sender.sendMessage(UtilsChat.getPrefix() + "oOo------------===------------oOo");
                            sender.sendMessage(UtilsChat.getPrefix() + "Name: " + war.getName());
                            sender.sendMessage(UtilsChat.getPrefix() + "Side 1: " + war.getSide1());
                            sender.sendMessage(UtilsChat.getPrefix() + "Side 2: " + war.getSide2());
                            sender.sendMessage(UtilsChat.getPrefix() + "Side 1 Score: " + war.getSide1().getScore());
                            sender.sendMessage(UtilsChat.getPrefix() + "Side 2 Score: " + war.getSide2().getScore());
//                            sender.sendMessage(UtilsChat.getPrefix() + "Last Raid for Side 1: " + new Timestamp(((long) war.getLastRaidTimeSide1()) * 1000L)); // TODO
//                            sender.sendMessage(UtilsChat.getPrefix() + "Last Raid for Side 2: " + new Timestamp(((long) war.getLastRaidTimeSide2()) * 1000L));
                            sender.sendMessage(UtilsChat.getPrefix() + "oOo------------===------------oOo");
                            StringBuilder side1Towns = new StringBuilder();
                            StringBuilder side1Players = new StringBuilder();

                            for (Town town : war.getSide1().getTowns()) {
                                side1Towns.append(town.getName());
                                side1Towns.append(", ");
                            }
                            for (Player player : war.getSide1().getPlayers()) {
                                side1Players.append(player.getName());
                                side1Players.append(", ");
                            }

                            //cut off last two characters
                            if (side1Towns.length() > 2)
                                side1Towns = new StringBuilder(side1Towns.substring(0, side1Towns.length() - 2));
                            if (side1Players.length() > 2)
                                side1Players = new StringBuilder(side1Players.substring(0, side1Players.length() - 2));
                            sender.sendMessage(UtilsChat.getPrefix() + war.getSide1() + " Towns: " + side1Towns);
                            sender.sendMessage(UtilsChat.getPrefix() + war.getSide1() + " Players: " + side1Players);

                            sender.sendMessage(UtilsChat.getPrefix() + "oOo------------===------------oOo");
                            StringBuilder side2Towns = new StringBuilder();
                            StringBuilder side2Players = new StringBuilder();

                            for (Town town : war.getSide2().getTowns()) {
                                side2Towns.append(town);
                                side2Towns.append(", ");
                            }
                            for (Player player : war.getSide2().getPlayers()) {
                                side2Players.append(player);
                                side2Players.append(", ");
                            }

                            //cut off last two characters
                            if (side2Towns.length() > 2)
                                side2Towns = new StringBuilder(side2Towns.substring(0, side2Towns.length() - 2));
                            if (side2Players.length() > 2)
                                side2Players = new StringBuilder(side2Players.substring(0, side2Players.length() - 2));
                            sender.sendMessage(UtilsChat.getPrefix() + war.getSide2() + " Towns: " + side2Towns);
                            sender.sendMessage(UtilsChat.getPrefix() + war.getSide2() + " Players: " + side2Players);

                            sender.sendMessage(UtilsChat.getPrefix() + "oOo------------===------------oOo");
                            final StringBuilder surrenderedTowns = getSurrenderedTowns(war);
                            sender.sendMessage(UtilsChat.getPrefix() + "Surrendered Towns: " + surrenderedTowns);
                        }
                    )/*,
                new CommandAPICommand("siege")
                    .withArguments(
                        new StringArgument("siege")
                            .replaceSuggestions(
                                ArgumentSuggestions.strings(
                                    CommandUtil.getSieges()
                                )
                            )
                    )
                    .executes((CommandSender sender, CommandArguments args) -> { // TODO Needed
                        if (!(args.get("oldSiege") instanceof final String argSiegeName))
                            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>You need to specify a oldSiege!").build());

                        final Siege oldSiege = SiegeData.getSiege(argSiegeName);
                        if (oldSiege == null)
                            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>That oldSiege does not exist!").build());


                        sender.sendMessage(UtilsChat.getPrefix() + "Info dump for oldSiege: " + oldSiege.getName());
                        sender.sendMessage(UtilsChat.getPrefix() + "oOo------------===------------oOo");
                        sender.sendMessage(UtilsChat.getPrefix() + "Name: " + oldSiege.getName());
                        sender.sendMessage(UtilsChat.getPrefix() + "Attackers: " + oldSiege.getAttackerSide());
                        sender.sendMessage(UtilsChat.getPrefix() + "Defenders: " + oldSiege.getDefenderSide());
                        sender.sendMessage(UtilsChat.getPrefix() + "Attacker points: " + oldSiege.getAttackerPoints());
                        sender.sendMessage(UtilsChat.getPrefix() + "Defender points: " + oldSiege.getDefenderPoints());
                        sender.sendMessage(UtilsChat.getPrefix() + "War: " + oldSiege.getWar().getName());
                        sender.sendMessage(UtilsChat.getPrefix() + "Attacked Town: " + oldSiege.getTown().getName());
                        sender.sendMessage(UtilsChat.getPrefix() + "Max Ticks: " + oldSiege.getMaxSiegeTicks());
                        sender.sendMessage(UtilsChat.getPrefix() + "Tick progress: " + oldSiege.getSiegeTicks());
                        sender.sendMessage(UtilsChat.getPrefix() + "Owner: " + oldSiege.getSiegeOwner());
                        sender.sendMessage(UtilsChat.getPrefix() + "Homeblock: " + oldSiege.getHomeBlock().toString());
                        sender.sendMessage(UtilsChat.getPrefix() + "oOo------------===------------oOo");
                        StringBuilder attackers = new StringBuilder();
                        for (String pl : oldSiege.getAttackerPlayers()) {
                            attackers.append(pl);
                            attackers.append(", ");
                        }
                        //cut off last two characters
                        if (attackers.length() > 2)
                            attackers = new StringBuilder(attackers.substring(0, attackers.length() - 2));
                        sender.sendMessage(UtilsChat.getPrefix() + "Attacking Players: " + attackers);
                        StringBuilder defenders = new StringBuilder();
                        for (String pl : oldSiege.getDefenderPlayers()) {
                            defenders.append(pl);
                            defenders.append(", ");
                        }
                        //cut off last two characters
                        if (defenders.length() > 2)
                            defenders = new StringBuilder(defenders.substring(0, defenders.length() - 2));
                        sender.sendMessage(UtilsChat.getPrefix() + "Defending Players: " + defenders);
                    })*/ /*,
                new CommandAPICommand("raid")
                    .withArguments(
                        new StringArgument("raid")
                            .replaceSuggestions(
                                ArgumentSuggestions.strings(
                                    CommandUtil.getRaids()
                                )
                            )
                    )
                    .executes((CommandSender sender, CommandArguments args) -> {
                        if (!(args.get("oldRaid") instanceof final String argRaidName))
                            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>You need to specify a oldRaid name!").build());

                        final OldRaid oldRaid = RaidData.getRaid(argRaidName);
                        if (oldRaid == null)
                            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>That oldRaid does not exist!").build());

                        sender.sendMessage(UtilsChat.getPrefix() + "Info dump for oldRaid: " + oldRaid.getName());
                        sender.sendMessage(UtilsChat.getPrefix() + "oOo------------===------------oOo");
                        sender.sendMessage(UtilsChat.getPrefix() + "Name: " + oldRaid.getName());
                        sender.sendMessage(UtilsChat.getPrefix() + "Raiders: " + oldRaid.getRaiderSide());
                        sender.sendMessage(UtilsChat.getPrefix() + "Defenders: " + oldRaid.getDefenderSide());
                        sender.sendMessage(UtilsChat.getPrefix() + "Side1Raiders: " + oldRaid.getSide1AreRaiders());
                        sender.sendMessage(UtilsChat.getPrefix() + "Raider Score: " + oldRaid.getRaiderScore());
                        sender.sendMessage(UtilsChat.getPrefix() + "Defender Score: " + oldRaid.getDefenderScore());
                        sender.sendMessage(UtilsChat.getPrefix() + "War: " + oldRaid.getWar().getName());
                        sender.sendMessage(UtilsChat.getPrefix() + "Raided Town: " + oldRaid.getRaidedTown().getName());
                        sender.sendMessage(UtilsChat.getPrefix() + "Gather Town: " + oldRaid.getGatherTown().getName());
                        sender.sendMessage(UtilsChat.getPrefix() + "Current Phase: " + oldRaid.getPhase().name());
                        sender.sendMessage(UtilsChat.getPrefix() + "Tick progress: " + oldRaid.getRaidTicks());
                        sender.sendMessage(UtilsChat.getPrefix() + "Owner: " + oldRaid.getOwner().getName());
                        sender.sendMessage(UtilsChat.getPrefix() + "Gather Homeblock: " + (oldRaid.getHomeBlockGather() == null ? "NONE" : oldRaid.getHomeBlockGather().toString()));
                        sender.sendMessage(UtilsChat.getPrefix() + "Raided Homeblock: " + (oldRaid.getHomeBlockRaided() == null ? "NONE" : oldRaid.getHomeBlockRaided().toString()));
                        sender.sendMessage(UtilsChat.getPrefix() + "oOo------------===------------oOo");
                        final StringBuilder activeRaiders = getActiveRaiders(oldRaid);
                        sender.sendMessage(UtilsChat.getPrefix() + "Raiding Players: " + activeRaiders);

                        sender.sendMessage(UtilsChat.getPrefix() + "oOo------------===------------oOo");
                        final StringBuilder defenderPlayers = getDefenderPlayers(oldRaid);
                        sender.sendMessage(UtilsChat.getPrefix() + "Defending Players: " + defenderPlayers);
                    })*/
            )
            .executes((CommandSender sender, CommandArguments args) -> fail(sender, args, AdminCommandFailEnum.SYNTAX));
    }

    /*private void saveAll(CommandSender sender, CommandArguments args) {
        for (War war : WarManager.getInstance().getWars())
            WarManager.getInstance().saveWar(war);
        sender.sendMessage(UtilsChat.getPrefix() + "<red>Forced Save of all wars.");
    }*/

    /**
     * //edit war/event in real time, side can be "both" to effect both
     * -modify raid score [add/set] [war] [town] [value]
     * -modify raid townspawn [war] [town] (x) (y) (Z)
     * -modify raid gather [war] [town] [town]
     * -modify raid phase [war] [town] [phase] //"next" to move to next phase
     * -modify raid loot [war] [town] [value,looted,ticks,reset] [amt] (x) (z)  //no coords just does current chunk, reset deletes it from the list
     * -modify raid time [war] [town] [add/set] [value]
     * -modify raid owner [war] [town] [player]
     * -modify raid move [war] [town] [newWar] //low priority, moves raid to other war/ town
     * -modify raid clearActive [war] [town] //low priority
     * <p>
     * -modify siege score [war] [town] [side] [amt]
     * -modify siege townspawn [war] [town] (x) (Z)
     * -modify siege time [war] [town] [add/set/max] [value] //max modified the max length
     * -modify siege owner [war] [town] [add/set] [value]
     * -modify siege move [war] [town] [newWar] //low priority, moves siege to other war/town
     * <p>
     * -modify war score [war] [side] [amt]
     * -modify war side [war]  [side] [name]
     * -modify war name [war] [name]
     * -modify war add town [war] [town]
     * -modify war add nation [war] [nation]
     * -modify war surrender town [war] [town] //adds town to surrender list
     * -modify war surrender nation [war] [town] //adds all towns to surrender list
     * -modify war raidTime [add,set,reset] [war] [town] [amt] //set when last raid was
     */
    private CommandAPICommand commandModify() {
        return new CommandAPICommand("modify")
            .withSubcommands(
                new CommandAPICommand("war").withSubcommands(
                    new CommandAPICommand("score")
                        .withArguments(
                            new TextArgument("action")
                                .replaceSuggestions(
                                    ArgumentSuggestions.stringCollection(info -> List.of("add", "subtract", "set"))
                                ),
                            CommandUtil.warWarArgument(
                                "war",
                                true,
                                false,
                                ""
                            ),
                            CommandUtil.warSideCreateArgument(
                                "side",
                                "war",
                                true,
                                false,
                                ""
                            ),
                            new IntegerArgument("amount", 1, 10000)
                        ).executesPlayer((Player p, CommandArguments args) -> {
                            if (!(args.get("action") instanceof final String argAction))
                                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>You need to specify an action.").build());

                            if (!List.of("add", "subtract", "set").contains(argAction))
                                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>Invalid action.").build());

                            if (!(args.get("war") instanceof final War war))
                                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>The war does not exist!").build());

                            if (!(args.get("side") instanceof final Side side))
                                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(UtilsChat.getPrefix() + "<red>The side does not exist.").build());

                            final int amount = (int) args.get("amount");

                            switch (argAction) {
                                case "add" -> {
                                    if (war.getSide1().equals(side)) {
                                        war.getSide1().addScore(amount);
                                    } else {
                                        war.getSide2().addScore(amount);
                                    }

                                    p.sendMessage(UtilsChat.getPrefix() + "Added " + amount + " points to the war in the war " + war.getName() + " on side " + side);
//                                    Main.warLogger.log("Added " + amount + " points to the war in the war " + war.getName() + " on side " + side);
                                }
                                case "subtract" -> {
                                    if (war.getSide1().equals(side)) {
                                        war.getSide1().addScore(-amount);
                                    } else {
                                        war.getSide2().addScore(-amount);
                                    }

                                    p.sendMessage(UtilsChat.getPrefix() + "Subtracted " + amount + " points to the war score in the war " + war.getName() + " on side " + side);
//                                    Main.warLogger.log("Subtracted " + amount + " points to the war score in the war " + war.getName() + " on side " + side);
                                }
                                case "set" -> {
                                    if (war.getSide1().equals(side)) {
                                        war.getSide1().addScore(amount);
                                    } else {
                                        war.getSide2().addScore(amount);
                                    }

                                    p.sendMessage(UtilsChat.getPrefix() + "Set " + amount + " points as the war score in the war " + war.getName() + " on side " + side);
//                                    Main.warLogger.log("Set " + amount + " points as the war score in the war " + war.getName() + " on side " + side);
                                }
                            }

                            finalizeWar(war);
                        }),
                    new CommandAPICommand("side")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        }),
                    new CommandAPICommand("name")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        }),
                    new CommandAPICommand("add").withSubcommands(
                        new CommandAPICommand("town")
                            .executesPlayer((Player p, CommandArguments args) -> {

                            }),
                        new CommandAPICommand("nation")
                            .executesPlayer((Player p, CommandArguments args) -> {

                            })
                    ),
                    new CommandAPICommand("surrender").withSubcommands(
                        new CommandAPICommand("town")
                            .executesPlayer((Player p, CommandArguments args) -> {

                            }),
                        new CommandAPICommand("nation")
                            .executesPlayer((Player p, CommandArguments args) -> {

                            })
                    ),
                    new CommandAPICommand("raidtime")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        })
                ),
                new CommandAPICommand("siege").withSubcommands(
                    new CommandAPICommand("score")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        }),
                    new CommandAPICommand("townspawn")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        }),
                    new CommandAPICommand("time")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        }),
                    new CommandAPICommand("owner")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        }),
                    new CommandAPICommand("move")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        })
                ),
                new CommandAPICommand("raid").withSubcommands(
                    new CommandAPICommand("score")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        }),
                    new CommandAPICommand("townspawn")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        }),
                    new CommandAPICommand("gather")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        }),
                    new CommandAPICommand("phase")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        }),
                    new CommandAPICommand("loot")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        }),
                    new CommandAPICommand("time")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        }),
                    new CommandAPICommand("owner")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        }),
                    new CommandAPICommand("move")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        }),
                    new CommandAPICommand("clearActive")
                        .executesPlayer((Player p, CommandArguments args) -> {

                        })
                )


            )
            .executes((CommandSender sender, CommandArguments args) -> {

            });
    }
}
