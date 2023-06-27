package me.ShermansWorld.AlathraWar.commands;

import com.palmergames.bukkit.towny.utils.NameUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class AdminTabCompletion implements TabCompleter {

    List<String> base = List.of("create",
        "force",
        "help",
        "info",
        "modify",
        "purgebars",
        "load",
        "load-all",
        "save",
        "save-all",
        "item");

    List<String> type = List.of(new String[]{
        "raid",
        "siege",
        "war"
    });

    List<String> raidModify = List.of(new String[]{
        "score",
        "townspawn",
        "gather",
        "phase",
        "loot",
        "time",
        "owner"
//            ,
//            "move"
//            ,
//            "clearActive"
    });

    List<String> siegeModify = List.of(new String[]{
        "score",
        "townspawn",
        "time",
        "owner"
//            ,
//            "move"
    });

    List<String> warModify = List.of(new String[]{
        "score",
        "side",
        "name",
        "add",
        "surrender",
        "unsurrender",
        "raidTimeWar",
        "raidTimeTown"
    });

    List<String> force = List.of(new String[]{
        "end",
        "join",
        "leave"
    });

    List<String> addSet = List.of(new String[]{
        "add",
        "set"
    });

    List<String> addSubSet = List.of(new String[]{
        "add",
        "subtract",
        "set"
    });

    List<String> boolSet = List.of(new String[]{
        "true",
        "false"
    });


    List<String> addSetReset = List.of(new String[]{
        "add",
        "set",
        "reset"
    });

    List<String> nationTown = List.of(new String[]{
        "town",
        "nation"
    });

    List<String> lootSet = List.of(new String[]{
        "value",
        "looted",
        "ticks",
        "reset"
    });

    List<String> empty = Collections.emptyList();


    /**
     * //done
     * //edit war/event in real time, side can be "both" to effect both
     * -modify raid score [war] [town] [add/set] [value]
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
     * //force make event or war, if owner isn't defined, IDK haven't decided
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
     * -force leave raid [war] [town] [player] (timeout) //kicks from raid party
     *
     * @param sender  Source of the command.  For players tab-completing a
     *                command inside a command block, this will be the player, not
     *                the command block.
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed
     * @return list of options
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("AlathraWar.admin")) {
            return empty;
        }

        if (args.length == 0) {
            return base;
        } else if (args.length == 1) {
            return NameUtil.filterByStart(base, args[0]);
        } else {
            if (args[0].equalsIgnoreCase("purgebars")) {
                return empty;
            } else if (args[0].equalsIgnoreCase("save")) {
                if (args.length > 2) {
                    return empty;
                } else {
                    return NameUtil.filterByStart(CommandHelper.getWarNames(), args[1]);
                }
            } else if (args[0].equalsIgnoreCase("save-all")) {
                return empty;
            } else if (args[0].equalsIgnoreCase("load")) {
                if (args.length > 2) {
                    return empty;
                } else {
                    return NameUtil.filterByStart(CommandHelper.getWarNames(), args[1]);
                }
            } else if (args[0].equalsIgnoreCase("load-all")) {
                return empty;
            } else if (args[0].equalsIgnoreCase("item")) {
                if (args.length > 2) {
                    if (args.length > 3) {
                        if (args.length > 4) {
                            return empty;
                        } else {
                            return NameUtil.filterByStart(CommandHelper.getPlayers(), args[3]);
                        }
                    } else {
                        return empty;
                    }
                } else {
                    return NameUtil.filterByStart(CommandHelper.getWarItems(), args[1]);
                }
            } else if (args[0].equalsIgnoreCase("create")) {
                if (args.length > 2) {
                    switch (args[1]) {
                        case "raid" -> {
                            if (args.length > 3) {
                                if (args.length > 4) {
                                    if (args.length > 5) {
                                        if (args.length > 6) {
                                            return empty;
                                        } else {
                                            return NameUtil.filterByStart(CommandHelper.getPlayers(), args[5]);
                                        }
                                    } else {
                                        return NameUtil.filterByStart(CommandHelper.getTownyWarTowns(args[2]), args[4]);
                                    }
                                } else {
                                    return NameUtil.filterByStart(CommandHelper.getTownyWarTowns(args[2]), args[3]);
                                }
                            } else {
                                return NameUtil.filterByStart(CommandHelper.getWarNames(), args[2]);
                            }
                        }
                        case "siege" -> {
                            if (args.length > 3) {
                                if (args.length > 4) {
                                    if (args.length > 5) {
                                        return empty;
                                    } else {
                                        return NameUtil.filterByStart(CommandHelper.getPlayers(), args[4]);
                                    }
                                } else {
                                    return NameUtil.filterByStart(CommandHelper.getTownyWarTowns(args[2]), args[3]);
                                }
                            } else {
                                return NameUtil.filterByStart(CommandHelper.getWarNames(), args[2]);
                            }
                        }
                        default -> {
                            return empty;
                        }
                    }
                } else {
                    return NameUtil.filterByStart(type, args[1]);
                }
            } else if (args[0].equalsIgnoreCase("force")) {
                if (args.length > 2) {
                    if (args[1].equalsIgnoreCase("end")) {
                        if (args.length > 3) {
                            if (args[2].equalsIgnoreCase("raid")) {
                                if (args.length > 4) {
                                    if (args.length > 5) {
                                        if (args.length > 6) {
                                            return empty;
                                        } else {
                                            return NameUtil.filterByStart(CommandHelper.getWarSides(args[3]), args[5]);
                                        }
                                    } else {
                                        return NameUtil.filterByStart(CommandHelper.getRaidTowns(), args[4]);
                                    }
                                } else {
                                    return NameUtil.filterByStart(CommandHelper.getWarNames(), args[3]);
                                }
                            } else if (args[2].equalsIgnoreCase("siege")) {
                                if (args.length > 4) {
                                    if (args.length > 5) {
                                        if (args.length > 6) {
                                            return empty;
                                        } else {
                                            return NameUtil.filterByStart(CommandHelper.getWarSides(args[3]), args[5]);
                                        }
                                    } else {
                                        return NameUtil.filterByStart(CommandHelper.getSiegeTowns(), args[4]);
                                    }
                                } else {
                                    return NameUtil.filterByStart(CommandHelper.getWarNames(), args[3]);
                                }
                            } else if (args[2].equalsIgnoreCase("war")) {
                                if (args.length > 4) {
                                    if (args.length > 5) {
                                        return empty;
                                    } else {
                                        return NameUtil.filterByStart(CommandHelper.getWarSides(args[3]), args[4]);
                                    }
                                } else {
                                    return NameUtil.filterByStart(CommandHelper.getWarNames(), args[3]);
                                }
                            }
                        } else {
                            return NameUtil.filterByStart(type, args[2]);
                        }
                    } else if (args[1].equalsIgnoreCase("join")) {
                        if (args.length > 3) {
                            if (args[2].equalsIgnoreCase("raid")) {
                                if (args.length > 4) {
                                    if (args.length > 5) {
                                        if (args.length > 6) {
                                            if (args.length > 7) {
                                                if (args.length > 8) {
                                                    return empty;
                                                } else {
                                                    return NameUtil.filterByStart(List.of(new String[]{"true", "false"}), args[7]);
                                                }
                                            } else {
                                                return NameUtil.filterByStart(CommandHelper.getWarSides(args[4]), args[6]);
                                            }
                                        } else {
                                            return NameUtil.filterByStart(CommandHelper.getRaidTowns(), args[5]);
                                        }
                                    } else {
                                        return NameUtil.filterByStart(CommandHelper.getWarNames(), args[4]);
                                    }
                                } else {
                                    return NameUtil.filterByStart(CommandHelper.getPlayers(), args[3]);
                                }
                            } else if (args[2].equalsIgnoreCase("siege")) {
                                if (args.length > 4) {
                                    if (args.length > 5) {
                                        if (args.length > 6) {
                                            if (args.length > 7) {
                                                if (args.length > 8) {
                                                    return empty;
                                                } else {
                                                    return NameUtil.filterByStart(List.of(new String[]{"true", "false"}), args[7]);
                                                }
                                            } else {
                                                return NameUtil.filterByStart(CommandHelper.getWarSides(args[4]), args[6]);
                                            }
                                        } else {
                                            return NameUtil.filterByStart(CommandHelper.getSiegeTowns(), args[5]);
                                        }
                                    } else {
                                        return NameUtil.filterByStart(CommandHelper.getWarNames(), args[4]);
                                    }
                                } else {
                                    return NameUtil.filterByStart(CommandHelper.getPlayers(), args[3]);
                                }
                            } else if (args[2].equalsIgnoreCase("war")) {
                                if (args.length > 4) {
                                    if (args.length > 5) {
                                        if (args.length > 6) {
                                            if (args.length > 7) {
                                                return empty;
                                            } else {
                                                return NameUtil.filterByStart(List.of(new String[]{"true", "false"}), args[6]);
                                            }
                                        } else {
                                            return NameUtil.filterByStart(CommandHelper.getWarSides(args[4]), args[5]);
                                        }
                                    } else {
                                        return NameUtil.filterByStart(CommandHelper.getWarNames(), args[4]);
                                    }
                                } else {
                                    return NameUtil.filterByStart(CommandHelper.getPlayers(), args[3]);
                                }
                            }
                        } else {
                            return NameUtil.filterByStart(type, args[2]);
                        }
                    } else if (args[1].equalsIgnoreCase("leave")) {
                        if (args.length > 3) {
                            if (args[2].equalsIgnoreCase("raid")) {
                                if (args.length > 4) {
                                    if (args.length > 5) {
                                        if (args.length > 6) {
                                            return empty;
                                        } else {
                                            return NameUtil.filterByStart(CommandHelper.getPlayers(), args[5]);
                                        }
                                    } else {
                                        return NameUtil.filterByStart(CommandHelper.getRaidTowns(), args[4]);
                                    }
                                } else {
                                    return NameUtil.filterByStart(CommandHelper.getWarNames(), args[3]);
                                }
                            } else {
                                return NameUtil.filterByStart(List.of(new String[]{"raid"}), args[3]);
                            }
                        } else {
                            return NameUtil.filterByStart(List.of(new String[]{"raid"}), args[2]);
                        }
                    } else {
                        return NameUtil.filterByStart(force, args[1]);
                    }
                } else {
                    return NameUtil.filterByStart(force, args[1]);
                }
            } else if (args[0].equalsIgnoreCase("help")) {
                return empty;
            } else if (args[0].equalsIgnoreCase("info")) {
                if (args.length > 2) {
                    if (args[1].equalsIgnoreCase("raid")) {
                        if (args.length > 3) {
                            if (args.length > 4) {
                                return empty;
                            } else {
                                return NameUtil.filterByStart(CommandHelper.getRaidTowns(), args[3]);
                            }
                        } else {
                            return NameUtil.filterByStart(CommandHelper.getWarNames(), args[2]);
                        }
                    } else if (args[1].equalsIgnoreCase("siege")) {
                        if (args.length > 3) {
                            if (args.length > 4) {
                                return empty;
                            } else {
                                return NameUtil.filterByStart(CommandHelper.getSiegeTowns(), args[3]);
                            }
                        } else {
                            return NameUtil.filterByStart(CommandHelper.getWarNames(), args[2]);
                        }
                    } else if (args[1].equalsIgnoreCase("war")) {
                        if (args.length > 3) {
                            return empty;
                        } else {
                            return NameUtil.filterByStart(CommandHelper.getWarNames(), args[2]);
                        }
                    } else {
                        return NameUtil.filterByStart(type, args[1]);
                    }
                } else {
                    return NameUtil.filterByStart(type, args[1]);
                }
            } else if (args[0].equalsIgnoreCase("awa")) {
                if (args.length > 2) {
                    return empty;
                } else {
                    return NameUtil.filterByStart(CommandHelper.getPlayers(), args[1]);
                }
            } else if (args[0].equalsIgnoreCase("modify")) {
                if (args.length > 2) {
                    if (args[1].equalsIgnoreCase("raid")) {
                        if (args.length > 3) {
                            if (args.length > 4) {
                                if (args.length > 5) {
                                    switch (args[2]) {
                                        /*
                                         * -modify raid score [war] [town] [add/subtract/set] [side] [value]
                                         * -modify raid townspawn [war] [town] (x) (y) (Z)
                                         * -modify raid gather [war] [town] [town]
                                         * -modify raid phase [war] [town] [phase] //"next" to move to next phase
                                         * -modify raid loot [war] [town] [value,looted,ticks,reset] [amt] (x) (z)  //no coords just does current chunk, reset deletes it from the list
                                         * -modify raid time [war] [town] [add/set] [value]
                                         * -modify raid owner [war] [town] [player]
                                         * -modify raid move [war] [town] [newWar] //low priority, moves raid to other war/ town
                                         * -modify raid clearActive [war] [town] //low priority
                                         */
                                        case "score" -> {
                                            if (args.length > 6) {
                                                if (args.length > 7) {
                                                    return empty;
                                                } else {
                                                    return NameUtil.filterByStart(CommandHelper.getWarSides(args[3]), args[5]);
                                                }
                                            } else {
                                                return NameUtil.filterByStart(addSubSet, args[5]);
                                            }
                                        }
                                        case "time" -> {
                                            if (args.length > 6) {
                                                return empty;
                                            } else {
                                                return NameUtil.filterByStart(addSet, args[5]);
                                            }
                                        }
                                        case "townspawn" -> {
                                            if (sender instanceof Player) {
                                                if (args.length > 6) {
                                                    if (args.length > 7) {
                                                        if (args.length > 8) {
                                                            return empty;
                                                        } else {
                                                            return List.of(new String[]{String.valueOf(((Player) sender).getLocation().getZ())});
                                                        }
                                                    } else {
                                                        return List.of(new String[]{String.valueOf(((Player) sender).getLocation().getY())});
                                                    }
                                                } else {
                                                    return List.of(new String[]{String.valueOf(((Player) sender).getLocation().getX())});
                                                }
                                            }
                                        }
                                        case "gather" -> {
                                            if (args.length > 6) {
                                                return empty;
                                            } else {
                                                return NameUtil.filterByStart(CommandHelper.getRaidTowns(), args[5]);
                                            }
                                        }
                                        case "phase" -> {
                                            if (args.length > 6) {
                                                return empty;
                                            } else {
                                                return NameUtil.filterByStart(CommandHelper.getRaidPhases(), args[5]);
                                            }
                                        }
                                        case "loot" -> {
                                            if (args.length > 6) {
                                                if (args.length > 7) {
                                                    if (args.length > 8) {
                                                        if (args.length > 9) {
                                                            return empty;
                                                        } else {
                                                            return List.of(new String[]{String.valueOf(((Player) sender).getLocation().getZ())});
                                                        }
                                                    } else {
                                                        return List.of(new String[]{String.valueOf(((Player) sender).getLocation().getX())});
                                                    }
                                                } else {
                                                    return empty;
                                                }
                                            } else {
                                                return NameUtil.filterByStart(lootSet, args[5]);
                                            }
                                        }
                                        case "owner" -> {
                                            if (args.length > 6) {
                                                return empty;
                                            } else {
                                                return NameUtil.filterByStart(CommandHelper.getPlayers(), args[5]);
                                            }
                                        }
                                        default -> {
                                            return empty;
                                        }
                                    }
                                } else {
                                    return NameUtil.filterByStart(CommandHelper.getRaidTowns(), args[4]);
                                }
                            } else {
                                return NameUtil.filterByStart(CommandHelper.getWarNames(), args[3]);
                            }
                        } else {
                            return NameUtil.filterByStart(raidModify, args[2]);
                        }
                    } else if (args[1].equalsIgnoreCase("siege")) {
                        if (args.length > 3) {
                            if (args.length > 4) {
                                if (args.length > 5) {
                                    switch (args[2]) {
                                        case "score", "time" -> {
                                            if (args.length > 6) {
                                                return empty;
                                            } else {
                                                return NameUtil.filterByStart(addSet, args[5]);
                                            }
                                        }
                                        case "townspawn" -> {
                                            if (args.length > 6) {
                                                if (args.length > 7) {
                                                    if (args.length > 8) {
                                                        return empty;
                                                    } else {
                                                        return List.of(new String[]{String.valueOf(((Player) sender).getLocation().getZ())});
                                                    }
                                                } else {
                                                    return List.of(new String[]{String.valueOf(((Player) sender).getLocation().getY())});
                                                }
                                            } else {
                                                return List.of(new String[]{String.valueOf(((Player) sender).getLocation().getX())});
                                            }
                                        }
                                        case "owner" -> {
                                            if (args.length > 6) {
                                                return empty;
                                            } else {
                                                return NameUtil.filterByStart(CommandHelper.getPlayers(), args[5]);
                                            }
                                        }
                                        default -> {
                                            return empty;
                                        }
                                    }
                                } else {
                                    return NameUtil.filterByStart(CommandHelper.getSiegeTowns(), args[4]);
                                }
                            } else {
                                return NameUtil.filterByStart(CommandHelper.getWarNames(), args[3]);
                            }
                        } else {
                            return NameUtil.filterByStart(siegeModify, args[2]);
                        }
                    } else if (args[1].equalsIgnoreCase("war")) {
                        if (args.length > 3) {
                            switch (args[2]) {
                                /*
                                 * -modify war score [war] [side] [add/subtract/set] [amt]
                                 * -modify war side [war] [side] [name]
                                 * -modify war name [war] [name]
                                 * -modify war add [war] [side] [town/nation] [town/nation]
                                 * -modify war add nation [war] [nation]
                                 * -modify war surrender town [war] [town] //adds town to surrender list
                                 * -modify war surrender nation [war] [town] //adds all towns to surrender list
                                 * -modify war unsurrender [war] town/nation [town/nation]
                                 * -modify war unsurrender [war] town/nation [town/nation]
                                 * -modify war raidTimeTown [add,set,reset] [war] [town] [amt] //set when last raid was
                                 */
                                case "score" -> {
                                    if (args.length > 4) {
                                        if (args.length > 5) {
                                            if (args.length > 6) {
                                                return empty;
                                            } else {
                                                return NameUtil.filterByStart(addSubSet, args[5]);
                                            }
                                        } else {
                                            return NameUtil.filterByStart(CommandHelper.getWarSides(args[3]), args[4]);
                                        }
                                    } else {
                                        return NameUtil.filterByStart(CommandHelper.getWarNames(), args[3]);
                                    }
                                }
                                //alathrawaradmin modify war raidTimeTown [add,set,reset] [town/war] [amt] [side]
                                case "raidTimeTown" -> {
                                    if (args.length > 4) {
                                        if (args.length > 5) {
                                            return empty;
                                        } else {
                                            return NameUtil.filterByStart(CommandHelper.getTownyTowns(), args[4]);
                                        }
                                    } else {
                                        return NameUtil.filterByStart(addSetReset, args[3]);
                                    }
                                }
                                //alathrawaradmin modify war raidTimeWar [add,set,reset] [war] [amt] [side]
                                case "raidTimeWar" -> {
                                    if (args.length > 4) {
                                        if (args.length > 5) {
                                            if (args[3].equalsIgnoreCase("reset")) {
                                                if (args.length > 6) {
                                                    return empty;
                                                } else {
                                                    return NameUtil.filterByStart(CommandHelper.getWarSides(args[4]), args[5]);
                                                }
                                            } else {
                                                if (args.length > 6) {
                                                    if (args.length > 7) {
                                                        return empty;
                                                    } else {
                                                        return NameUtil.filterByStart(CommandHelper.getWarSides(args[4]), args[6]);
                                                    }
                                                } else {
                                                    return empty;
                                                }
                                            }
                                        } else {
                                            return NameUtil.filterByStart(CommandHelper.getWarNames(), args[4]);
                                        }
                                    } else {
                                        return NameUtil.filterByStart(addSetReset, args[3]);

                                    }
                                }
                                case "side" -> {
                                    if (args.length > 4) {
                                        if (args.length > 5) {
                                            return empty;
                                        } else {
                                            return NameUtil.filterByStart(CommandHelper.getWarSides(args[3]), args[4]);
                                        }
                                    } else {
                                        return NameUtil.filterByStart(CommandHelper.getWarNames(), args[3]);
                                    }
                                }
                                case "name" -> {
                                    if (args.length > 4) {
                                        return empty;
                                    } else {
                                        return NameUtil.filterByStart(CommandHelper.getWarNames(), args[3]);
                                    }
                                }
                                case "add" -> {
                                    if (args.length > 4) {
                                        if (args.length > 5) {
                                            if (args.length > 6) {
                                                if (args.length > 7) {
                                                    if (args.length > 8) {
                                                        return empty;
                                                    } else {
                                                        return boolSet;
                                                    }
                                                } else {
                                                    if (args[5].equalsIgnoreCase("town")) {
                                                        return NameUtil.filterByStart(CommandHelper.getTownyTowns(), args[6]);
                                                    } else if (args[5].equalsIgnoreCase("nation")) {
                                                        return NameUtil.filterByStart(CommandHelper.getTownyNations(), args[6]);
                                                    } else {
                                                        return empty;
                                                    }
                                                }
                                            } else {
                                                return NameUtil.filterByStart(nationTown, args[5]);
                                            }
                                        } else {
                                            return NameUtil.filterByStart(CommandHelper.getWarSides(args[3]), args[4]);
                                        }
                                    } else {
                                        return NameUtil.filterByStart(CommandHelper.getWarNames(), args[3]);
                                    }
                                }
                                case "surrender" -> {
                                    if (args.length > 4) {
                                        if (args.length > 5) {
                                            if (args.length > 6) {
                                                if (args.length > 7) {
                                                    return empty;
                                                } else {
                                                    if (args[5].equalsIgnoreCase("town")) {
                                                        return NameUtil.filterByStart(CommandHelper.getTownyWarTowns(args[3]), args[6]);
                                                    } else if (args[5].equalsIgnoreCase("nation")) {
                                                        return NameUtil.filterByStart(CommandHelper.getTownyWarNations(args[3]), args[6]);
                                                    } else {
                                                        return empty;
                                                    }
                                                }
                                            } else {
                                                return NameUtil.filterByStart(nationTown, args[5]);
                                            }
                                        } else {
                                            return NameUtil.filterByStart(CommandHelper.getWarSides(args[3]), args[4]);
                                        }
                                    } else {
                                        return NameUtil.filterByStart(CommandHelper.getWarNames(), args[3]);
                                    }
                                }
                                case "unsurrender" -> {
                                    if (args.length > 4) {
                                        if (args.length > 5) {
                                            if (args.length > 6) {
                                                return empty;
                                            } else {
                                                if (args[4].equalsIgnoreCase("town")) {
                                                    return NameUtil.filterByStart(CommandHelper.getTownyWarTowns(args[3]), args[5]);
                                                } else if (args[4].equalsIgnoreCase("nation")) {
                                                    return NameUtil.filterByStart(CommandHelper.getTownyWarNations(args[3]), args[5]);
                                                } else {
                                                    return empty;
                                                }
                                            }
                                        } else {
                                            return NameUtil.filterByStart(nationTown, args[4]);
                                        }
                                    } else {
                                        return NameUtil.filterByStart(CommandHelper.getWarNames(), args[3]);
                                    }
                                }
                                default -> {
                                    return empty;
                                }
                            }

                        } else {
                            return NameUtil.filterByStart(warModify, args[2]);
                        }
                    } else {
                        return NameUtil.filterByStart(type, args[1]);
                    }
                } else {
                    return NameUtil.filterByStart(type, args[1]);
                }
            } else {
                return NameUtil.filterByStart(base, args[0]);
            }
        }
        return NameUtil.filterByStart(base, args[0]);
    }
}
