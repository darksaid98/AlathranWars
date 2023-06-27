package me.ShermansWorld.AlathraWar.commands;

import com.github.milkdrinkers.colorparser.ColorParser;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import me.ShermansWorld.AlathraWar.*;
import me.ShermansWorld.AlathraWar.data.RaidData;
import me.ShermansWorld.AlathraWar.data.SiegeData;
import me.ShermansWorld.AlathraWar.data.WarData;
import me.ShermansWorld.AlathraWar.enums.AdminCommandFailEnum;
import me.ShermansWorld.AlathraWar.items.WarItemRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Iterator;
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
        new CommandAPICommand("alathrawaradmin")
            .withAliases("awa")
            .withPermission("AlathraWar.admin")
            .withSubcommands(
                commandItem(),
                commandPurgeBars(),
                commandSave(),
                commandSaveAll(),
                commandLoad(),
                commandLoadAll(),
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

    @NotNull
    private static StringBuilder getDefenderPlayers(Raid raid) {
        StringBuilder defenderPlayers = new StringBuilder();
        for (String pl : raid.getDefenderPlayers()) {
            defenderPlayers.append(pl);
            defenderPlayers.append(", ");
        }
        //cut off last two characters
        if (defenderPlayers.length() > 2)
            defenderPlayers = new StringBuilder(defenderPlayers.substring(0, defenderPlayers.length() - 2));
        return defenderPlayers;
    }

    @NotNull
    private static StringBuilder getActiveRaiders(Raid raid) {
        StringBuilder activeRaiders = new StringBuilder();
        for (String pl : raid.getActiveRaiders()) {
            activeRaiders.append(pl);
            activeRaiders.append(", ");
        }
        //cut off last two characters
        if (activeRaiders.length() > 2)
            activeRaiders = new StringBuilder(activeRaiders.substring(0, activeRaiders.length() - 2));
        return activeRaiders;
    }

    @NotNull
    private static StringBuilder getSurrenderedTowns(War war) {
        StringBuilder surrenderedTowns = new StringBuilder();
        for (String t : war.getSurrenderedTowns()) {
            surrenderedTowns.append(t);
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
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + Helper.color("&cYou do not have permission to do this.")).build());
            }
            case SYNTAX -> {
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "Invalid Arguments. /alathrawaradmin help").build());
            }
            default -> {
                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "Something wrong. /alathrawaradmin help").build());
            }
        }
    }

    private static void finalizeRaid(Raid raid) {
        raid.save();
    }

    private static void finalizeSiege(Siege siege) {
        siege.save();
    }

    private static void finalizeWar(War war) {
        war.save();
    }

    private CommandAPICommand commandItem() {
        return new CommandAPICommand("item")
            .withArguments(
                new StringArgument("item")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(
                            CommandHelper.getWarItems()
                        )
                    ),
                new IntegerArgument("amount").setOptional(true),
                new PlayerArgument("player").setOptional(true)
            )
            .executesPlayer((Player sender, CommandArguments args) -> {
                // Parse item
                if (!(args.get("item") instanceof String argItem))
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cInvalid item argument.").build());

                ItemStack stack;
                if (argItem.contains(Main.getInstance().getName().toLowerCase())) {
                    stack = WarItemRegistry.getInstance().getOrNullNamespace(argItem);
                } else {
                    stack = WarItemRegistry.getInstance().getOrNull(argItem);
                }

                if (stack == null)
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cThe item is not an AlathraWar item.").build());

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
                sender.sendMessage(Helper.chatLabel() + Helper.color("Gave " + stack.getAmount() + "x " + name + " to " + argPlayer.getName()));
                Main.warLogger.log(Helper.chatLabel() + Helper.color("Gave " + stack.getAmount() + "x " + name + " to " + argPlayer.getName()));
            });
    }

    private CommandAPICommand commandPurgeBars() {
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

                sender.sendMessage(Helper.chatLabel() + Helper.color("&cCleared all boss bars that have been registered with AlathraWar."));
                sender.sendMessage(Helper.chatLabel() + Helper.color("&cNote: If any remain, please contact the plugin author or open an issue on GitHub. The data can be found under the tag CustomBossEvents: in level.dat! You will need an NBT Editing program to delete it manually."));

                saveAll(sender, args);
            });
    }

    private CommandAPICommand commandSave() {
        return new CommandAPICommand("save")
            .withArguments(
                new StringArgument("war")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(
                            WarData.getWarsNames()
                        )
                    )
            )
            .executes((CommandSender sender, CommandArguments args) -> {
                if (!(args.get("war") instanceof final String argWarName))
                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a war name.").build());

                for (War war : WarData.getWars()) {
                    if (war.getName().equalsIgnoreCase(argWarName)) {
                        WarData.saveWar(war);
                        sender.sendMessage(Helper.chatLabel() + Helper.color("&cForced Save of war: " + war.getName()));
                        return;
                    }
                }

                sender.sendMessage(Helper.chatLabel() + Helper.color("&cWar does not exist!"));
            });
    }

    private CommandAPICommand commandSaveAll() {
        return new CommandAPICommand("save-all")
            .executes((CommandSender sender, CommandArguments args) -> {
                for (War war : WarData.getWars()) WarData.saveWar(war);
                sender.sendMessage(Helper.chatLabel() + Helper.color("&cForced Save of all wars"));
            });
    }

    private CommandAPICommand commandLoad() {
        return new CommandAPICommand("load")
            .executes((CommandSender sender, CommandArguments args) -> {
                sender.sendMessage(new ColorParser(Helper.chatLabel() + "&cSupport for loading wars individually is currently unimplemented. Use /awa load-all instead").build());
            });

                /*if (args.length >= 3) {
                    if(Boolean.parseBoolean(args[2])) {
                        for (War war : WarData.getWars()) {
                            int index = -1;
                            if(war.getName().equalsIgnoreCase(args[1])) {
                                //kill raids
                                for (Raid raid : war.getRaids()) {
                                    raid.stop();
                                }
                                //kill sieges
                                for (Siege siege : war.getSieges()) {
                                    siege.stop();
                                }
                                //delete wars
                                index = WarData.getWars().lastIndexOf(war);
                                WarData.getWars().remove(war);
                            }
                            if(index < 0) {
                                sender.sendMessage(Helper.chatLabel() + Helper.color("&cError! Failed to delete war, forcing end."));
                                return true;
                            }
                            //reload data
                            ;
                            sender.sendMessage(Helper.chatLabel() + Helper.color("&cForcefully Reloaded all Wars!"));
                            return true;
                        }
                    }
                }*/
    }

    private CommandAPICommand commandLoadAll() {
        return new CommandAPICommand("load-all")
            .withArguments(new BooleanArgument("boolean").setOptional(true))
            .executes((CommandSender sender, CommandArguments args) -> {
                boolean argsBoolean = (boolean) args.getOptional("boolean").orElse(false);

                if (argsBoolean) {
                    for (War war : WarData.getWars()) {
                        //kill raids
                        for (Raid raid : war.getRaids()) {
                            raid.stop();
                        }

                        //kill sieges
                        for (Siege siege : war.getSieges()) {
                            siege.stop();
                        }

                        //delete wars
                        WarData.getWars().remove(war);
                    }

                    //reload data
                    Main.initData();
                    sender.sendMessage(Helper.chatLabel() + Helper.color("&cForcefully Reloaded all Wars!"));
                }

                sender.sendMessage(Helper.chatLabel() + Helper.color("Are you sure you want to do this?"));
                sender.sendMessage(Helper.chatLabel() + Helper.color("Doing this will forcefully stop and reload every war live. It can be very dangerous and may not work as intended."));
                sender.sendMessage(Helper.chatLabel() + Helper.color("To confirm do /awa load-all true"));
            });
    }

    private CommandAPICommand commandCreate() {
        return new CommandAPICommand("create")
            .withSubcommands(
                new CommandAPICommand("raid")
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
                            .withPermission("AlathraWar.admin")
                            .replaceSuggestions(
                                ArgumentSuggestions.stringCollection(info -> { // TODO Make getHostileTowns method where we reverse from list
                                        final String warname = (String) info.previousArgs().get("war");
                                        return CommandHelper.getTownyWarTowns(warname);
                                    }
                                )
                            ),
                        new PlayerArgument("leader")
                            .setOptional(true)
                            .withPermission("AlathraWar.admin"),
                        new BooleanArgument("minutemen")
                            .setOptional(true)
                            .withPermission("AlathraWar.admin")

                    )
                    .executesPlayer((Player p, CommandArguments args) -> RaidCommand.raidStart(p, args, true)),
                new CommandAPICommand("siege")
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
                    .executesPlayer((Player p, CommandArguments args) -> SiegeCommand.siegeStart(p, args, true)),
                new CommandAPICommand("war")
                    .withArguments(
                        new StringArgument("war"),
                        new StringArgument("side1")
                            .replaceSuggestions(
                                ArgumentSuggestions.stringCollection(info -> {
                                        final List<String> nations = CommandHelper.getTownyNations();
                                        final List<String> towns = CommandHelper.getTownyTowns();

                                        Collections.sort(nations);
                                        Collections.sort(towns);

                                        nations.addAll(towns);

                                        return nations;
                                    }
                                )
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
                                    }
                                )
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
                                new StringArgument("war")
                                    .replaceSuggestions(
                                        ArgumentSuggestions.strings(
                                            CommandHelper.getWarNames()
                                        )
                                    ),
                                new StringArgument("victor")
                                    .setOptional(true)
                                    .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                            final String warname = (String) info.previousArgs().get("war");

                                            final War war = WarData.getWar(warname);

                                            if (war == null)
                                                return Collections.emptyList();

                                            return war.getSides();
                                        })
                                    )
                            ).executesPlayer((Player sender, CommandArguments args) -> {
                                //TODO determine if needed
                                sender.sendMessage(Helper.color("&cUnused! use /war delete"));
                            }),
                        new CommandAPICommand("siege")
                            .withArguments(
                                new StringArgument("siege")
                                    .replaceSuggestions(
                                        ArgumentSuggestions.strings(
                                            CommandHelper.getSieges()
                                        )
                                    ),
                                new StringArgument("victor")
                                    .setOptional(true)
                                    .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                            final String siegename = (String) info.previousArgs().get("siege");

                                            final Siege siege = SiegeData.getSiege(siegename);

                                            if (siege == null)
                                                return Collections.emptyList();

                                            return List.of(siege.getAttackerSide(), siege.getDefenderSide(), "none");
                                        })
                                    )

                            ).executesPlayer((Player sender, CommandArguments args) -> {
                                if (!(args.get("siege") instanceof final String argSiegeName))
                                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a siege!").build());

                                final Siege siege = SiegeData.getSiege(argSiegeName);
                                if (siege == null)
                                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cThat siege does not exist!").build());

                                final War war = siege.getWar();
                                if (war == null)
                                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cThe war does not exist!").build());

                                final String argSide = (String) args.getOptional("victor").orElse("none");

                                // If tie
                                if (argSide.equals("none")) {
                                    siege.noWinner();
                                    sender.sendMessage(Helper.chatLabel() + "The siege has forcefully been lifted at %s in war %s with no victor.".formatted(siege.getTown().getName(), war.getName()));
                                    Main.warLogger.log("Siege of %s forcefully ended in war %s with no victor.".formatted(siege.getTown().getName(), war.getName()));
                                    return;
                                }

                                final String message = "The siege has forcefully been lifted at %s in war %s with the %s declared as victors.";
                                String victor;

                                // Figure who won & lost
                                if (siege.getSide1AreAttackers()) {
                                    if (argSide.equalsIgnoreCase(war.getSide1())) {
                                        siege.attackersWin(siege.getSiegeOwner());
                                        victor = "(attackers)";
                                    } else {
                                        siege.defendersWin();
                                        victor = "(defenders)";
                                    }
                                } else {
                                    if (argSide.equalsIgnoreCase(war.getSide1())) {
                                        siege.defendersWin();
                                        victor = "(defenders)";
                                    } else {
                                        siege.attackersWin(siege.getSiegeOwner());
                                        victor = "(attackers)";
                                    }
                                }

                                sender.sendMessage(Helper.chatLabel() + message.formatted(siege.getTown().getName(), war.getName(), victor));
                                Main.warLogger.log("Siege of %s forcefully ended in war %s with %s declared as victor.".formatted(siege.getTown().getName(), war.getName(), victor));
                            }),
                        new CommandAPICommand("raid")
                            .withArguments(
                                new StringArgument("raid")
                                    .replaceSuggestions(
                                        ArgumentSuggestions.strings(
                                            CommandHelper.getRaids()
                                        )
                                    ),
                                new StringArgument("victor")
                                    .setOptional(true)
                                    .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                            final String raidname = (String) info.previousArgs().get("raid");

                                            final Raid raid = RaidData.getRaid(raidname);

                                            if (raid == null)
                                                return Collections.emptyList();

                                            return List.of(raid.getRaiderSide(), raid.getDefenderSide(), "none");
                                        })
                                    )
                            ).executesPlayer((Player sender, CommandArguments args) -> {
                                if (!(args.get("raid") instanceof final String argRaidName))
                                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a raid name!").build());

                                final Raid raid = RaidData.getRaid(argRaidName);
                                if (raid == null)
                                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cThat raid does not exist!").build());

                                final War war = raid.getWar();
                                if (war == null)
                                    throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cThe war does not exist!").build());

                                final String argSide = (String) args.getOptional("victor").orElse("none");

                                // If tie
                                if (argSide.equals("none")) {
                                    raid.noWinner();
                                    sender.sendMessage(Helper.chatLabel() + "The raid has forcefully been ended at %s in war %s with no victor.".formatted(raid.getRaidedTown().getName(), war.getName()));
                                    Main.warLogger.log("Raid of %s forcefully ended in war %s with no victor.".formatted(raid.getRaidedTown().getName(), war.getName()));
                                    return;
                                }

                                final String message = "The raid has forcefully been ended at %s in war %s with the %s declared as victors.";
                                String victor;

                                // Figure who won & lost
                                if (raid.getSide1AreRaiders()) {
                                    if (argSide.equalsIgnoreCase(war.getSide1())) {
                                        raid.raidersWin(raid.getOwner(), raid.getRaiderScore(), raid.getDefenderScore());
                                        victor = "(raiders)";
                                    } else {
                                        raid.defendersWin(raid.getRaiderScore(), raid.getDefenderScore());
                                        victor = "(defenders)";
                                    }
                                } else {
                                    if (argSide.equalsIgnoreCase(war.getSide1())) {
                                        raid.defendersWin(raid.getRaiderScore(), raid.getDefenderScore());
                                        victor = "(defenders)";
                                    } else {
                                        raid.raidersWin(raid.getOwner(), raid.getRaiderScore(), raid.getDefenderScore());
                                        victor = "(raiders)";
                                    }
                                }

                                sender.sendMessage(Helper.chatLabel() + message.formatted(raid.getRaidedTown().getName(), war.getName(), victor));
                                Main.warLogger.log("Raid of %s forcefully ended in war %s with %s declared as victor.".formatted(raid.getRaidedTown().getName(), war.getName(), victor));
                            })
                    ),
                new CommandAPICommand("join")
                    .withSubcommands(
                        new CommandAPICommand("war")
                            .withArguments(
                                new PlayerArgument("player"),
                                new StringArgument("war")
                                    .replaceSuggestions(
                                        ArgumentSuggestions.strings(
                                            CommandHelper.getWarNames()
                                        )
                                    ),
                                new StringArgument("side")
                                    .setOptional(true)
                                    .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                            final String warname = (String) info.previousArgs().get("war");

                                            final War war = WarData.getWar(warname);

                                            if (war == null)
                                                return Collections.emptyList();

                                            return war.getSides();
                                        })
                                    )
                            ).executesPlayer((Player sender, CommandArguments args) -> WarCommand.warJoin(sender, args, true)),
                        new CommandAPICommand("siege")
                            .withArguments(
                                new PlayerArgument("player"),
                                new StringArgument("siege")
                                    .replaceSuggestions(
                                        ArgumentSuggestions.strings(
                                            CommandHelper.getSieges()
                                        )
                                    ),
                                new StringArgument("side")
                                    .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                            final String siegename = (String) info.previousArgs().get("siege");

                                            final Siege siege = SiegeData.getSiege(siegename);

                                            if (siege == null)
                                                return Collections.emptyList();

                                            return List.of(siege.getAttackerSide(), siege.getDefenderSide());
                                        })
                                    )
                            ).executesPlayer((Player sender, CommandArguments args) -> {
                                sender.sendMessage(Helper.color("&cError! Unimplemented!"));
                                // TODO implement siege force joining?
                            }),
                        new CommandAPICommand("raid")
                            .withArguments(
                                new PlayerArgument("player"),
                                new StringArgument("raid")
                                    .replaceSuggestions(
                                        ArgumentSuggestions.strings(
                                            CommandHelper.getRaids()
                                        )
                                    ),
                                new StringArgument("side")
                                    .replaceSuggestions(
                                        ArgumentSuggestions.stringCollection(info -> {
                                            final String raidname = (String) info.previousArgs().get("raid");

                                            final Raid raid = RaidData.getRaid(raidname);

                                            if (raid == null)
                                                return Collections.emptyList();

                                            return List.of(raid.getRaiderSide(), raid.getDefenderSide());
                                        })
                                    ),
                                new BooleanArgument("minutemen")
                                    .setOptional(true)
                            ).executesPlayer((Player sender, CommandArguments args) -> RaidCommand.raidJoin(sender, args, true))
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
                        new CommandAPICommand("raid").withArguments(
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
                        ).executesPlayer((Player sender, CommandArguments args) -> RaidCommand.raidLeave(sender, args, true))
                    )
            )
            .executes((CommandSender sender, CommandArguments args) -> fail(sender, args, AdminCommandFailEnum.SYNTAX));
    }

    private CommandAPICommand commandHelp() {
        return new CommandAPICommand("help")
            .executes((CommandSender sender, CommandArguments args) -> {
                sender.sendMessage(Helper.chatLabel() + "/awa create");
                sender.sendMessage(Helper.chatLabel() + "/awa force");
                sender.sendMessage(Helper.chatLabel() + "/awa help");
                sender.sendMessage(Helper.chatLabel() + "/awa info");
                sender.sendMessage(Helper.chatLabel() + "/awa modify");
                sender.sendMessage(Helper.chatLabel() + "/awa purgebars");
                sender.sendMessage(Helper.chatLabel() + "/awa save");
                sender.sendMessage(Helper.chatLabel() + "/awa save-all");
                sender.sendMessage(Helper.chatLabel() + "/awa load-all");
                sender.sendMessage(Helper.chatLabel() + "/awa item");
            });
    }

    private CommandAPICommand commandInfo() {
        return new CommandAPICommand("info")
            .withSubcommands(
                new CommandAPICommand("war")
                    .withArguments(
                        new StringArgument("war")
                            .replaceSuggestions(
                                ArgumentSuggestions.strings(
                                    CommandHelper.getWarNames()
                                )
                            )
                    )
                    .executes((CommandSender sender, CommandArguments args) -> {
                            if (!(args.get("war") instanceof final String argWarName))
                                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a war!").build());

                            final War war = WarData.getWar(argWarName);
                            if (war == null)
                                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cThat war does not exist!").build());

                            sender.sendMessage(Helper.chatLabel() + "Info dump for war: " + war.getName());
                            sender.sendMessage(Helper.chatLabel() + "oOo------------===------------oOo");
                            sender.sendMessage(Helper.chatLabel() + "Name: " + war.getName());
                            sender.sendMessage(Helper.chatLabel() + "Side 1: " + war.getSide1());
                            sender.sendMessage(Helper.chatLabel() + "Side 2: " + war.getSide2());
                            sender.sendMessage(Helper.chatLabel() + "Side 1 Score: " + war.getSide1Points());
                            sender.sendMessage(Helper.chatLabel() + "Side 2 Score: " + war.getSide2Points());
                            sender.sendMessage(Helper.chatLabel() + "Last Raid for Side 1: " + new Timestamp(((long) war.getLastRaidTimeSide1()) * 1000L));
                            sender.sendMessage(Helper.chatLabel() + "Last Raid for Side 2: " + new Timestamp(((long) war.getLastRaidTimeSide2()) * 1000L));
                            sender.sendMessage(Helper.chatLabel() + "oOo------------===------------oOo");
                            StringBuilder side1Towns = new StringBuilder();
                            StringBuilder side1Players = new StringBuilder();
                            for (String t : war.getSide1Towns()) {
                                side1Towns.append(t);
                                side1Towns.append(", ");
                            }
                            for (String pl : war.getSide1Players()) {
                                side1Players.append(pl);
                                side1Players.append(", ");
                            }
                            //cut off last two characters
                            if (side1Towns.length() > 2)
                                side1Towns = new StringBuilder(side1Towns.substring(0, side1Towns.length() - 2));
                            if (side1Players.length() > 2)
                                side1Players = new StringBuilder(side1Players.substring(0, side1Players.length() - 2));
                            sender.sendMessage(Helper.chatLabel() + war.getSide1() + " Towns: " + side1Towns);
                            sender.sendMessage(Helper.chatLabel() + war.getSide1() + " Players: " + side1Players);

                            sender.sendMessage(Helper.chatLabel() + "oOo------------===------------oOo");
                            StringBuilder side2Towns = new StringBuilder();
                            StringBuilder side2Players = new StringBuilder();
                            for (String t : war.getSide2Towns()) {
                                side2Towns.append(t);
                                side2Towns.append(", ");
                            }
                            for (String pl : war.getSide2Players()) {
                                side2Players.append(pl);
                                side2Players.append(", ");
                            }
                            //cut off last two characters
                            if (side2Towns.length() > 2)
                                side2Towns = new StringBuilder(side2Towns.substring(0, side2Towns.length() - 2));
                            if (side2Players.length() > 2)
                                side2Players = new StringBuilder(side2Players.substring(0, side2Players.length() - 2));
                            sender.sendMessage(Helper.chatLabel() + war.getSide2() + " Towns: " + side2Towns);
                            sender.sendMessage(Helper.chatLabel() + war.getSide2() + " Players: " + side2Players);

                            sender.sendMessage(Helper.chatLabel() + "oOo------------===------------oOo");
                            final StringBuilder surrenderedTowns = getSurrenderedTowns(war);
                            sender.sendMessage(Helper.chatLabel() + "Surrendered Towns: " + surrenderedTowns);
                        }
                    ),
                new CommandAPICommand("siege")
                    .withArguments(
                        new StringArgument("siege")
                            .replaceSuggestions(
                                ArgumentSuggestions.strings(
                                    CommandHelper.getSieges()
                                )
                            )
                    )
                    .executes((CommandSender sender, CommandArguments args) -> {
                        if (!(args.get("siege") instanceof final String argSiegeName))
                            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a siege!").build());

                        final Siege siege = SiegeData.getSiege(argSiegeName);
                        if (siege == null)
                            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cThat siege does not exist!").build());


                        sender.sendMessage(Helper.chatLabel() + "Info dump for siege: " + siege.getName());
                        sender.sendMessage(Helper.chatLabel() + "oOo------------===------------oOo");
                        sender.sendMessage(Helper.chatLabel() + "Name: " + siege.getName());
                        sender.sendMessage(Helper.chatLabel() + "Attackers: " + siege.getAttackerSide());
                        sender.sendMessage(Helper.chatLabel() + "Defenders: " + siege.getDefenderSide());
                        sender.sendMessage(Helper.chatLabel() + "Attacker points: " + siege.getAttackerPoints());
                        sender.sendMessage(Helper.chatLabel() + "Defender points: " + siege.getDefenderPoints());
                        sender.sendMessage(Helper.chatLabel() + "War: " + siege.getWar().getName());
                        sender.sendMessage(Helper.chatLabel() + "Attacked Town: " + siege.getTown().getName());
                        sender.sendMessage(Helper.chatLabel() + "Max Ticks: " + siege.getMaxSiegeTicks());
                        sender.sendMessage(Helper.chatLabel() + "Tick progress: " + siege.getSiegeTicks());
                        sender.sendMessage(Helper.chatLabel() + "Owner: " + siege.getSiegeOwner());
                        sender.sendMessage(Helper.chatLabel() + "Homeblock: " + siege.getHomeBlock().toString());
                        sender.sendMessage(Helper.chatLabel() + "oOo------------===------------oOo");
                        StringBuilder attackers = new StringBuilder();
                        for (String pl : siege.getAttackerPlayers()) {
                            attackers.append(pl);
                            attackers.append(", ");
                        }
                        //cut off last two characters
                        if (attackers.length() > 2)
                            attackers = new StringBuilder(attackers.substring(0, attackers.length() - 2));
                        sender.sendMessage(Helper.chatLabel() + "Attacking Players: " + attackers);
                        StringBuilder defenders = new StringBuilder();
                        for (String pl : siege.getDefenderPlayers()) {
                            defenders.append(pl);
                            defenders.append(", ");
                        }
                        //cut off last two characters
                        if (defenders.length() > 2)
                            defenders = new StringBuilder(defenders.substring(0, defenders.length() - 2));
                        sender.sendMessage(Helper.chatLabel() + "Defending Players: " + defenders);
                    }),
                new CommandAPICommand("raid")
                    .withArguments(
                        new StringArgument("raid")
                            .replaceSuggestions(
                                ArgumentSuggestions.strings(
                                    CommandHelper.getRaids()
                                )
                            )
                    )
                    .executes((CommandSender sender, CommandArguments args) -> {
                        if (!(args.get("raid") instanceof final String argRaidName))
                            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a raid name!").build());

                        final Raid raid = RaidData.getRaid(argRaidName);
                        if (raid == null)
                            throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cThat raid does not exist!").build());

                        sender.sendMessage(Helper.chatLabel() + "Info dump for raid: " + raid.getName());
                        sender.sendMessage(Helper.chatLabel() + "oOo------------===------------oOo");
                        sender.sendMessage(Helper.chatLabel() + "Name: " + raid.getName());
                        sender.sendMessage(Helper.chatLabel() + "Raiders: " + raid.getRaiderSide());
                        sender.sendMessage(Helper.chatLabel() + "Defenders: " + raid.getDefenderSide());
                        sender.sendMessage(Helper.chatLabel() + "Side1Raiders: " + raid.getSide1AreRaiders());
                        sender.sendMessage(Helper.chatLabel() + "Raider Score: " + raid.getRaiderScore());
                        sender.sendMessage(Helper.chatLabel() + "Defender Score: " + raid.getDefenderScore());
                        sender.sendMessage(Helper.chatLabel() + "War: " + raid.getWar().getName());
                        sender.sendMessage(Helper.chatLabel() + "Raided Town: " + raid.getRaidedTown().getName());
                        sender.sendMessage(Helper.chatLabel() + "Gather Town: " + raid.getGatherTown().getName());
                        sender.sendMessage(Helper.chatLabel() + "Current Phase: " + raid.getPhase().name());
                        sender.sendMessage(Helper.chatLabel() + "Tick progress: " + raid.getRaidTicks());
                        sender.sendMessage(Helper.chatLabel() + "Owner: " + raid.getOwner().getName());
                        sender.sendMessage(Helper.chatLabel() + "Gather Homeblock: " + (raid.getHomeBlockGather() == null ? "NONE" : raid.getHomeBlockGather().toString()));
                        sender.sendMessage(Helper.chatLabel() + "Raided Homeblock: " + (raid.getHomeBlockRaided() == null ? "NONE" : raid.getHomeBlockRaided().toString()));
                        sender.sendMessage(Helper.chatLabel() + "oOo------------===------------oOo");
                        final StringBuilder activeRaiders = getActiveRaiders(raid);
                        sender.sendMessage(Helper.chatLabel() + "Raiding Players: " + activeRaiders);

                        sender.sendMessage(Helper.chatLabel() + "oOo------------===------------oOo");
                        final StringBuilder defenderPlayers = getDefenderPlayers(raid);
                        sender.sendMessage(Helper.chatLabel() + "Defending Players: " + defenderPlayers);
                    })
            )
            .executes((CommandSender sender, CommandArguments args) -> fail(sender, args, AdminCommandFailEnum.SYNTAX));
    }

    private void saveAll(CommandSender sender, CommandArguments args) {
        for (War war : WarData.getWars())
            WarData.saveWar(war);
        sender.sendMessage(Helper.chatLabel() + Helper.color("&cForced Save of all wars."));
    }

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
                            new StringArgument("war")
                                .replaceSuggestions(
                                    ArgumentSuggestions.strings(
                                        CommandHelper.getWarNames()
                                    )
                                ),
                            new StringArgument("side")
                                .setOptional(true)
                                .replaceSuggestions(
                                    ArgumentSuggestions.stringCollection(info -> {
                                        final String warname = (String) info.previousArgs().get("war");

                                        final War war = WarData.getWar(warname);

                                        if (war == null)
                                            return Collections.emptyList();

                                        return war.getSides();
                                    })
                                ),
                            new IntegerArgument("amount", 1, 10000)
                        ).executesPlayer((Player p, CommandArguments args) -> {
                            if (!(args.get("action") instanceof final String argAction))
                                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify an action.").build());

                            if (!List.of("add", "subtract", "set").contains(argAction))
                                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cInvalid action.").build());

                            if (!(args.get("war") instanceof final String argWarName))
                                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a war.").build());

                            final War war = WarData.getWar(argWarName);
                            if (war == null)
                                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cThe war does not exist!").build());

                            if (!(args.get("side") instanceof final String argSide))
                                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cYou need to specify a side.").build());

                            if (!war.isSideValid(argSide))
                                throw CommandAPIBukkit.failWithAdventureComponent(new ColorParser(Helper.chatLabel() + "&cThe side does not exist.").build());

                            final int amount = (int) args.get("amount");

                            switch (argAction) {
                                case "add" -> {
                                    if (war.getSide1().equals(argSide)) {
                                        war.addSide1Points(amount);
                                    } else {
                                        war.addSide2Points(amount);
                                    }

                                    p.sendMessage(Helper.chatLabel() + "Added " + amount + " points to the raid war in the war " + war.getName() + " on side " + argSide);
                                    Main.warLogger.log("Added " + amount + " points to the raid war in the war " + war.getName() + " on side " + argSide);
                                }
                                case "subtract" -> {
                                    if (war.getSide1().equals(argSide)) {
                                        war.addSide1Points(-amount);
                                    } else {
                                        war.addSide2Points(-amount);
                                    }

                                    p.sendMessage(Helper.chatLabel() + "Subtracted " + amount + " points to the war score in the war " + war.getName() + " on side " + argSide);
                                    Main.warLogger.log("Subtracted " + amount + " points to the war score in the war " + war.getName() + " on side " + argSide);
                                }
                                case "set" -> {
                                    if (war.getSide1().equals(argSide)) {
                                        war.setSide1Points(amount);
                                    } else {
                                        war.setSide2Points(amount);
                                    }

                                    p.sendMessage(Helper.chatLabel() + "Set " + amount + " points as the war score in the war " + war.getName() + " on side " + argSide);
                                    Main.warLogger.log("Set " + amount + " points as the war score in the war " + war.getName() + " on side " + argSide);
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
