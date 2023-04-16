package me.ShermansWorld.AlathraWar.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import me.ShermansWorld.AlathraWar.War;
import me.ShermansWorld.AlathraWar.data.WarData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class AdminTabCompletion implements TabCompleter {

    List<String> base = List.of(new String[]{
            "create",
            "force",
            "help",
            "info",
            "modify"
    });

    List<String> type = List.of(new String[]{
            "raid",
            "siege",
            "war"
    });

    List<String> raidModify = List.of(new String[] {
            "score",
            "homeblock",
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

    List<String> siegeModify = List.of(new String[] {
            "score",
            "homeblock",
            "time",
            "owner"
//            ,
//            "move"
    });

    List<String> warModify = List.of(new String[] {
            "score",
            "side",
            "name",
            "add",
            "surrender",
            "raidTime"
    });

    List<String> force = List.of(new String[] {
            "end",
            "join",
            "leave"
    });


    /**
     *
     * //done
     * //edit war/event in real time, side can be "both" to effect both
     * -modify raid score [war] [town] [value]
     * -modify raid homeblock [war] [town] (x) (Z)
     * -modify raid gather [war] [town] [town]
     * -modify raid phase [war] [town] [phase] //"next" to move to next phase
     * -modify raid loot [war] [town] (x) (z) [value,looted,ticks,reset] [amt] //no coords just does current chunk, reset deletes it from the list
     * -modify raid time [war] [town] [add/set] [value]
     * -modify raid owner [war] [town] [add/set] [value]
     * -modify raid move [war] [town] [newWar] //low priority, moves raid to other war
     * -modify raid clearActive [war] [town] //low priority
     *
     * -modify siege score [war] [town] [side] [amt]
     * -modify siege homeblock [war] [town] (x) (Z)
     * -modify siege time [war] [town] [add/set/max] [value] //max modified the max length
     * -modify siege owner [war] [town] [add/set] [value]
     * -modify siege move [war] [town] [newWar] //low priority, moves siege to other war
     *
     * -modify war score [war] [side] [amt]
     * -modify war side [war]  [side] [name]
     * -modify war name [war] [name]
     * -modify war add town [war] [town]
     * -modify war add nation [war] [nation]
     * -modify war surrender town [war] [town] //adds town to surrender list
     * -modify war surrender nation [war] [town] //adds all towns to surrender list
     * -modify war raidTime [add,set,reset] [war] [town] [amt] //set when last raid was
     *
     * // low priority idea
     * -info war score [war]
     * -info war surrenderedTowns [war]
     * -info war raids [war]
     * -info war sieges [war]
     * -info war towns [war]
     * -info war lastRaidTime [war]
     *
     * -info raid homeblock [war] [town]
     * -info raid lootedTownBlocks [war] [town]
     * -info raid timeSinceLastRaid [town]
     *
     * -info siege homeblock [war] [town]
     *
     * //done
     * //force make event or war, if owner isnt defined, idk havent decided
     * -create siege [war] [town] (owner)
     * -create raid [war] [raidTown] (gatherTown) (owner)
     * -create war [name] [side1] [side2]
     *
     * //force end a war/event, can declare winner side, or no winner
     * -force end war [war] (side/victor)
     * -force end siege [war] [town] (side/victor)
     * -force end raid [war] [town] (side/victor)
     *
     * //done
     * //force player into or out of a war/event
     * -force join war [player] [war] [side]
     * -force join siege [player] [war] [town] (side)
     * -force join raid [player] [war] [town] (side)
     *
     * //done
     * -force leave raid [war] [town] [player] (timeout) //kicks from raid party
     *
     *
     * @param sender Source of the command.  For players tab-completing a
     *     command inside of a command block, this will be the player, not
     *     the command block.
     * @param command Command which was executed
     * @param label Alias of the command which was used
     * @param args The arguments passed to the command, including final
     *     partial argument to be completed
     * @return
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        final Player p = (Player) sender;
        if (p.hasPermission("!AlathraWar.admin")) {
            return null;
        }

        if (args.length == 0) {
            return base;
        }else {
            if (args[0].equalsIgnoreCase("create")) {
                if(args.length > 2) {
                    if(args[1].equals("raid")) {
                        if(args.length > 3) {
                            if(args.length > 4) {
                                if(args.length > 5) {
                                    if(args.length > 6) {
                                        return null;
                                    } else {
                                        return CommandHelper.getPlayers();
                                    }
                                } else {
                                    return CommandHelper.getTownyTowns();
                                }
                            } else {
                                return CommandHelper.getTownyTowns();
                            }
                        } else {
                            return CommandHelper.getWarNames();
                        }
                    } else if(args[1].equals("siege")) {
                        if(args.length > 3) {
                            if(args.length > 4) {
                                if(args.length > 5) {
                                    return null;
                                } else {
                                    return CommandHelper.getPlayers();
                                }
                            } else {
                                return CommandHelper.getTownyTowns();
                            }
                        } else {
                            return CommandHelper.getWarNames();
                        }
                    } else if(args[1].equals("war")) {
                        return null;
                    } else {
                        return null;
                    }
                } else {
                    return type;
                }
            } else if (args[0].equalsIgnoreCase("force")) {
                if(args.length > 2) {
                    if(args[1].equalsIgnoreCase("end")) {
                        if(args.length > 3) {
                            if(args[2].equalsIgnoreCase("raid")) {
                                if(args.length > 4) {
                                    if(args.length > 5) {
                                        if(args.length > 6) {
                                            return null;
                                        } else {
                                            return CommandHelper.getWarSides(args[3]);
                                        }
                                    } else {
                                        return CommandHelper.getTownyTowns();
                                    }
                                } else {
                                    return CommandHelper.getWarNames();
                                }
                            } else if(args[2].equalsIgnoreCase("siege")) {
                                if(args.length > 4) {
                                    if(args.length > 5) {
                                        if (args.length > 6) {
                                            return null;
                                        } else {
                                            return CommandHelper.getWarSides(args[3]);
                                        }
                                    } else {
                                        return CommandHelper.getTownyTowns();
                                    }
                                } else {
                                    return CommandHelper.getWarNames();
                                }
                            } else if(args[2].equalsIgnoreCase("war")) {
                                if(args.length > 4) {
                                    if(args.length > 5) {
                                        return null;
                                    } else {
                                        return CommandHelper.getWarSides(args[3]);
                                    }
                                } else {
                                    return CommandHelper.getWarNames();
                                }
                            }
                        } else {
                            return type;
                        }
                    } else if(args[1].equalsIgnoreCase("join")) {
                        if(args.length > 3) {
                            if(args[2].equalsIgnoreCase("raid")) {
                                if(args.length > 4) {
                                    if(args.length > 5) {
                                        if (args.length > 6) {
                                            if (args.length > 7) {
                                                return null;
                                            } else {
                                                return CommandHelper.getWarSides(args[4]);
                                            }
                                        } else {
                                            return CommandHelper.getTownyTowns();
                                        }
                                    } else {
                                        return CommandHelper.getWarNames();
                                    }
                                } else {
                                    return CommandHelper.getPlayers();
                                }
                            } else if(args[2].equalsIgnoreCase("siege")) {
                                if(args.length > 4) {
                                    if(args.length > 5) {
                                        if (args.length > 6) {
                                            if (args.length > 7) {
                                                return null;
                                            } else {
                                                return CommandHelper.getWarSides(args[4]);
                                            }
                                        } else {
                                            return CommandHelper.getTownyTowns();
                                        }
                                    } else {
                                        return CommandHelper.getWarNames();
                                    }
                                } else {
                                    return CommandHelper.getPlayers();
                                }
                            } else if(args[2].equalsIgnoreCase("war")) {
                                if(args.length > 4) {
                                    if(args.length > 5) {
                                        if(args.length > 6) {
                                            return null;
                                        } else {
                                            return CommandHelper.getWarSides(args[4]);
                                        }
                                    } else {
                                        return CommandHelper.getWarNames();
                                    }
                                } else {
                                    return CommandHelper.getPlayers();
                                }
                            }
                        } else {
                            return type;
                        }
                    } else if(args[1].equalsIgnoreCase("leave")) {
                        if(args.length > 3) {
                            if(args[2].equalsIgnoreCase("raid")) {
                                if(args.length > 4) {
                                    if(args.length > 5) {
                                        if(args.length > 6) {
                                            return null;
                                        } else {
                                            return CommandHelper.getPlayers();
                                        }
                                    } else {
                                        return CommandHelper.getTownyTowns();
                                    }
                                } else {
                                    return CommandHelper.getWarNames();
                                }
                            } else {
                                return List.of(new String[]{"raid"});
                            }
                        } else {
                            return List.of(new String[]{"raid"});
                        }
                    } else {
                        return force;
                    }
                } else {
                    return force;
                }
            } else if (args[0].equalsIgnoreCase("help")) {
                return null;
            } else if (args[0].equalsIgnoreCase("info")) {
                if(args.length > 2) {
                    if(args[1].equalsIgnoreCase("raid")) {
                        if(args.length > 3) {
                            if(args.length > 4) {
                                return null;
                            } else {
                                return CommandHelper.getTownyTowns();
                            }
                        } else {
                            return CommandHelper.getWarNames();
                        }
                    } else if (args[1].equalsIgnoreCase("siege")) {
                        if(args.length > 3) {
                            if(args.length > 4) {
                                return null;
                            } else {
                                return CommandHelper.getTownyTowns();
                            }
                        } else {
                            return CommandHelper.getWarNames();
                        }
                    } else if (args[1].equalsIgnoreCase("war")) {
                        if(args.length > 3) {
                            return null;
                        } else {
                            return CommandHelper.getWarNames();
                        }
                    } else {
                        return type;
                    }
                } else {
                    return type;
                }
            } else if (args[0].equalsIgnoreCase("modify")) {
                if(args.length > 2) {
                    if(args[1].equalsIgnoreCase("raid")) {
                        if(args.length > 3) {
                            if(args.length > 4) {
                                if(args.length > 5) {
                                    return null;
                                } else {
                                    return CommandHelper.getTownyTowns();
                                }
                            } else {
                                return CommandHelper.getWarNames();
                            }
                        } else {
                            return raidModify;
                        }
                    } else if (args[1].equalsIgnoreCase("siege")) {
                        if(args.length > 3) {
                            if(args.length > 4) {
                                if(args.length > 5) {
                                    return null;
                                } else {
                                    return CommandHelper.getTownyTowns();
                                }
                            } else {
                                return CommandHelper.getWarNames();
                            }
                        } else {
                            return raidModify;
                        }
                    } else if (args[1].equalsIgnoreCase("war")) {
                        if(args.length > 3) {
                            if(args.length > 4) {
                                return null;
                            } else {
                                return CommandHelper.getWarNames();
                            }
                        } else {
                            return raidModify;
                        }
                    } else {
                        return type;
                    }
                } else {
                    return type;
                }
            } else {
                return base;
            }
        }
        return base;
    }
}
