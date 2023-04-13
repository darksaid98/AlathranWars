package me.ShermansWorld.AlathraWar.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.Raid;
import me.ShermansWorld.AlathraWar.data.RaidData;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

public class AdminCommands implements CommandExecutor {

    static {

    }

    public AdminCommands(final Main plugin) {
        plugin.getCommand("alathrawaradmin").setExecutor((CommandExecutor)this);
        plugin.getCommand("awa").setExecutor((CommandExecutor)this);
    }

    /**
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
     * //force make event or war, if owner isnt defined, idk havent decided
     * -create siege [war] [town] (owner)
     * -create raid [war] [raidTown] (gatherTown) (owner)
     * -create war [name] [side1] [side2]
     *
     * //force end a war/event, can declare winner side, or no winner
     * -force end war [name] (side/victor)
     * -force end siege [war] [town] (side/victor)
     * -force end raid [war] [town] (side/victor)
     *
     * //force player into or out of a war/event
     * -force join war [player] [war] [side]
     * -force join siege [player] [war] [town] (side)
     * -force join raid [player] [war] [town] (side)
     * -force leave war [war] [player] (timeout)
     * -force leave siege [war] [player] (timeout)
     * -force leave raid  [war] [player] (timeout) //kicks from raid party
     *
     *
     * //exclude players from join a raid or war for a time
     * //raid subcommand prevents joining or being raided
     * -exclude player war [war] (side) (timeout)
     * -exclude town war [war] (side) (timeout)
     * -exclude nation war [war] (side) (timeout)
     *
     * -exclude player raid [join/target] [war] (timeout)
     * -exclude town raid [join/target] [war] (timeout)
     * -exclude nation raid [join/target] [war] (timeout)
     *
     * // Ultra low priority idea
     * -rule raidersRespawnAtGatherTown [true/false]
     * -rule siegersRespawnAtTown [true/false]
     * -rule anyoneCanJoinRaid [true/false]
     *
     * @param sender Source of the command
     * @param command Command which was executed
     * @param label Alias of the command which was used
     * @param args Passed command arguments
     * @return Valid command
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Player p = (Player) sender;
        if (p.hasPermission("!AlathraWar.admin")) {
            return fail(p, args, "permissions");
        }

        if (args.length == 0) {
            return fail(p, args, "syntax");
        } else if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("create")) {
                return create(p, args);
            } else if (args[0].equalsIgnoreCase("exclude")) {
                return exclude(p, args);
            } else if (args[0].equalsIgnoreCase("force")) {
                return force(p, args);
            } else if (args[0].equalsIgnoreCase("help")) {
                return help(p, args);
            } else if (args[0].equalsIgnoreCase("info")) {
                return info(p, args);
            } else if (args[0].equalsIgnoreCase("modify")) {
                return modify(p, args);
            } else if (args[0].equalsIgnoreCase("rule")) {
                return rule(p, args);
            }
        }
        return false;
    }

    /**
     * //force make event or war, if owner isnt defined, idk havent decided
     * -create siege [war] [town] (owner)
     * -create raid [war] [raidTown] [gatherTown] (owner)
     * -create war [name] [side1] [side2]
    */
    private static boolean create(Player p, String[] args) {
        if(args.length >= 2) {
            if(args[1].equalsIgnoreCase("raid")) {
                if(args.length >= 4) {
                    //specific behavior exists if an admin ran this
                    //using same method so that this doesnt get fucked up if we go back and change original implementation
                    RaidCommands.startRaid(p, args, true);
                    return true;
                } else {
                    //defaultCode will bypass the custom gather town to force set owner
                    p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin create raid [war] [raidTown] (gatherTown/\"defaultCode\") (owner)");
                    return false;
                }
            } else if(args[1].equalsIgnoreCase("siege")) {
                if(args.length >= 4) {
                    //TODO once siege command is done
                    p.sendMessage(Helper.Chatlabel() + "Try again later!");
                } else {
                    p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin create siege [war] [town] (owner)");
                    return false;
                }
            } else if(args[1].equalsIgnoreCase("war")) {
                //this command basically is a copy or /war create
                if(args.length == 5) {
                    //should purge the first argument;
                    String[] adjusted = new String[] {
                            args[1],
                            args[2],
                            args[3],
                            args[4]
                    };
                    WarCommands.warCreate(p, adjusted);
                } else {
                    p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin create war [name] [side1] [side2]");
                    return false;
                }
            }
        } else {
            return fail(p, args, "syntax");
        }
        return fail(p, args, "syntax");
    }

    private static boolean exclude(Player p, String[] args) {
        return false;
    }

    /**
     * //force end a war/event, can declare winner side, or no winner
     * -force end war [name] (side/victor)
     * -force end siege [war] [town] (side/victor)
     * -force end raid [war] [town] (side/victor)
     *
     * //force player into or out of a war/event
     * -force join war [player] [war] [side]
     * -force join siege [player] [war] [town] (side)
     * -force join raid [player] [war] [town] (side) //TODO time until must be in gather?
     * -force leave war [war] [player] (timeout)
     * -force leave siege [war] [player] (timeout)
     * -force leave raid  [war] [player] (timeout) //kicks from raid party
     * -force surrender war [name] [town]
     *
     * @param p
     * @param args
     * @return
     */
    private static boolean force(Player p, String[] args) {
        if(args.length >= 2) {
            if(args[1].equalsIgnoreCase("end")) {
                if(args.length >= 3) {
                    if(args[2].equalsIgnoreCase("raid")) {

                    } else if(args[2].equalsIgnoreCase("siege")) {

                    } else if(args[2].equalsIgnoreCase("war")) {

                    } else {
                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin force end [raid/siege/war]");
                        return false;
                    }
                } else {
                    p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin force end [raid/siege/war]");
                    return false;
                }
            } else if(args[1].equalsIgnoreCase("join")) {
                if(args.length >= 3) {
                    if(args[2].equalsIgnoreCase("raid")) {
                        if(args.length >= 6) {
                            //fix args
                            String[] adjusted = new String[] {
                                    args[1], //join
                                    args[4], //war
                                    args[5], //town
                                    args[3], //player
                                    args.length >= 7 ? args[6] : null //side
                            };
                            RaidCommands.joinRaid(p, adjusted, true);
                            return true;
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin force join raid [player] [war] [town] (side)");
                            return false;
                        }
                    } else if(args[2].equalsIgnoreCase("siege")) {
                        //TODO when siege commands are done
                    } else if(args[2].equalsIgnoreCase("war")) {
                        if(args.length >= 6) {
                            //fix args to match
                            String[] adjusted = new String[] {
                                    args[1],
                                    args[4],
                                    args[5],
                                    args[3]
                            };
                            //find the player
                            if(Bukkit.getPlayer(args[3]) != null) {
                                p = Bukkit.getPlayer(args[3]);
                            } else {
                                p.sendMessage(Helper.color("c") + args[3] + " does not exist!");
                                p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin force join war [player] [war] [side]");
                                return false;
                            }
                            WarCommands.warJoin(p, adjusted, true);
                            p.sendMessage(Helper.color("c") + "Forced " + args[3] + " to join the war " + args[4] + " on side " + args[5]);
                            Main.warLogger.log("Forced " + args[3] + " to join the war " + args[4] + " on side " + args[5]);
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin force join war [player] [war] [side]");
                            return false;
                        }
                    } else {
                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin force join [raid/siege/war]");
                        return false;
                    }
                } else {
                    p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin force join [raid/siege/war]");
                    return false;
                }
            } else if(args[1].equalsIgnoreCase("leave")) {
                if(args.length >= 3) {
                    if(args[2].equalsIgnoreCase("raid")) {

                    } else if(args[2].equalsIgnoreCase("siege")) {

                    } else if(args[2].equalsIgnoreCase("war")) {
//                        if(args.length >= 6) {
//                            //fix args to match
//                            String[] adjusted = new String[] {
//                                    args[1],
//                                    args[4],
//                                    args[5],
//                                    args[3]
//                            };
//                            //find the player
//                            if(Bukkit.getPlayer(args[3]) != null) {
//                                p = Bukkit.getPlayer(args[3]);
//                            } else {
//                                p.sendMessage(Helper.color("c") + args[3] + " does not exist!");
//                                p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin force leave war [player] [war] (timeout)");
//                                return false;
//                            }
//                            WarCommands.warJoin(p, adjusted, true);
//                            p.sendMessage(Helper.color("c") + "Forced " + args[3] + " to leave the war " + args[4] + " from side " + args[5]);
//                            Main.warLogger.log("Forced " + args[3] + " to leave the war " + args[4] + " from side " + args[5]);
//                        } else {
//                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin force leave war [player] [war] (timeout)");
//                            return false;
//                        }
                    } else {
                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin force leave [raid/siege/war]");
                        return false;
                    }
                } else {
                    p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin force leave [raid/siege/war]");
                    return false;
                }
            } else {
                return fail(p, args, "syntax");
            }
        }
        return fail(p, args, "syntax");
    }

    private static boolean help(Player p, String[] args) {
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin create");
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin exclude");
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin force");
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin help");
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin info");
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin modify");
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin rule");
        return true;
    }

    private static boolean info(Player p, String[] args) {
        return false;
    }

    /**
     * //edit war/event in real time, side can be "both" to effect both
     * -modify raid score [add/set] [war] [town] [value]
     * -modify raid townspawn [war] [town] (x) (y) (Z)
     * -modify raid gather [war] [town] [town]
     * -modify raid phase [war] [town] [phase] //"next" to move to next phase
     * -modify raid loot [war] [town] [value,looted,ticks,reset] [amt] (x) (z)  //no coords just does current chunk, reset deletes it from the list
     * -modify raid time [war] [town] [add/set] [value]
     * -modify raid owner [war] [town] [add/set] [value]
     * -modify raid move [war] [town] [newWar] //low priority, moves raid to other war/ town
     * -modify raid clearActive [war] [town] //low priority
     *
     * -modify siege score [war] [town] [side] [amt]
     * -modify siege homeblock [war] [town] (x) (Z)
     * -modify siege time [war] [town] [add/set/max] [value] //max modified the max length
     * -modify siege owner [war] [town] [add/set] [value]
     * -modify siege move [war] [town] [newWar] //low priority, moves siege to other war/town
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
     * @param p
     * @param args
     * @return
     */
    private static boolean modify(Player p, String[] args) {
        if(args.length >= 2) {
            if (args[1].equalsIgnoreCase("raid")) {
                if (args.length >= 3) {
                    if (args[2].equalsIgnoreCase("score")) {
                        if(args.length >= 7) {
                            for(Raid r: RaidData.getRaids()) {
                                if(r.getWar().getName().equals(args[4]) && r.getRaidedTown().getName().equals(args[5])) {
                                    if(args[3].equalsIgnoreCase("add")) {
                                        r.addPointsToRaidScore(Integer.parseInt(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Added " + args[6] + " points to the raid score in the war " + args[4] + " on town " + args[5]);
                                        Main.warLogger.log("Added " + args[6] + " points to the raid score in the war " + args[4] + " on town " + args[5]);
                                        return true;
                                    } else if(args[3].equalsIgnoreCase("subtract")) {
                                        r.subtractPointsFromRaidScore(Integer.parseInt(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Subtracted " + args[6] + " points to the raid score in the war " + args[4] + " on town " + args[5]);
                                        Main.warLogger.log("Subtracted " + args[6] + " points to the raid score in the war " + args[4] + " on town " + args[5]);
                                        return true;
                                    } else if(args[3].equalsIgnoreCase("set")) {
                                        r.setRaidScore(Integer.parseInt(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Set " + args[6] + " points as the raid score in the war " + args[4] + " on town " + args[5]);
                                        Main.warLogger.log("Set " + args[6] + " points as the raid score in the war " + args[4] + " on town " + args[5]);
                                        return true;
                                    }  else {
                                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid score [add/subtract/set] [war] [town] [value]");
                                        return false;
                                    }
                                } else {
                                    p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Raid cannot be found.");
                                    return false;
                                }
                            }
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid score [add/subtract/set] [war] [town] [value]");
                            return false;
                        }
                    } else if (args[2].equalsIgnoreCase("townspawn")) {
                        if(args.length >= 5) {
                            for(Raid r: RaidData.getRaids()) {
                                if(r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    if (args.length == 6 || args.length == 7){
                                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid homeblock [war] [town] (x) (Z)");
                                        return false;
                                    }
                                    if(args.length >= 8) {
                                        Town t = r.getRaidedTown();
                                        if(p.getWorld() == t.getWorld()) {
                                            try {
                                                WorldCoord tb = WorldCoord.parseWorldCoord(p.getWorld().getName(), Integer.parseInt(args[5]), Integer.parseInt(args[7]));
                                                if (t.hasTownBlock(tb)) {
                                                    t.setHomeBlock(tb.getTownBlock());
                                                    t.setSpawn(new Location(p.getWorld(), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7])));
                                                    p.sendMessage(Helper.Chatlabel() + "Set town spawn for raided town " + args[4] + " in war " + args[3] + " to " + p.getLocation().toString());
                                                    Main.warLogger.log("Set town spawn for raided town " + args[4] + " in war " + args[3] + " to [" + args[5] + "," + args[6] + "," + args[7] + "]");
                                                    return true;
                                                } else {
                                                    p.sendMessage(Helper.color("c") + "Town does not contain town block at [" + args[5] + "," + args[7] + "]");
                                                    return false;
                                                }
                                                r.setHomeBlockRaided(tb.getTownBlock());
                                            } catch (NotRegisteredException e) {
                                                p.sendMessage(Helper.color("c") + "Error! Townblock does not exist!");
                                                return false;
                                                throw new RuntimeException(e);
                                            }
                                        } else {
                                            return false;
                                        }
                                    } else {
                                        Town t = r.getRaidedTown();
                                        if(p.getWorld() == t.getWorld()) {
                                            try {
                                                WorldCoord tb = WorldCoord.parseWorldCoord(p.getWorld().getName(), (int) p.getLocation().getX(), (int) p.getLocation().getZ());
                                                if (t.hasTownBlock(tb)) {
                                                    t.setHomeBlock(tb.getTownBlock());
                                                    t.setSpawn(p.getLocation());
                                                    p.sendMessage(Helper.Chatlabel() + "Set town spawn for raided town " + args[5] + " in war " + args[4] + " to " + p.getLocation().toString());
                                                    Main.warLogger.log("Set town spawn for raided town " + args[5] + " in war " + args[4] + " to " + p.getLocation().toString());
                                                    return true;
                                                } else {
                                                    p.sendMessage(Helper.color("c") + "Town does not contain town block at your location [" + (int) p.getLocation().getX() + "," + (int) p.getLocation().getZ() + "]");
                                                    return false;
                                                }
                                                r.setHomeBlockRaided(tb.getTownBlock());
                                            } catch (NotRegisteredException e) {
                                                p.sendMessage(Helper.color("c") + "Error! Townblock does not exist!");
                                                return false;
                                                throw new RuntimeException(e);
                                            }
                                        } else {
                                            return false;
                                        }
                                    }
                                } else {
                                    p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Raid cannot be found.");
                                    return false;
                                }
                            }
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid homeblock [war] [town] (x) (Z)");
                            return false;
                        }
                    } else if (args[2].equalsIgnoreCase("gather")) {
                        if(args.length >= 6) {
                            for(Raid r: RaidData.getRaids()) {
                                if(r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    Town t = TownyAPI.getInstance().getTown(args[5]);
                                    if(t != null) {
                                        r.setRaidedTown(t);
                                        try {
                                            r.setTownSpawnGather(t.getSpawn());
                                            r.setHomeBlockGather(t.getHomeBlock());
                                        } catch (TownyException e) {
                                            p.sendMessage(Helper.Chatlabel() + "Error!");
                                            throw new RuntimeException(e);
                                        }
                                        p.sendMessage(Helper.Chatlabel() + "Set town new gather town " + t.getName() + " for raid against " + args[4] + " in war " + args[3]);
                                        Main.warLogger.log("Set town new gather town " + t.getName() + " for raid against " + args[4] + " in war " + args[3]);
                                        return true;
                                    } else {
                                        p.sendMessage(Helper.color("c") + "Town does not exists!");
                                        return false;
                                    }
                                } else {
                                    p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Raid cannot be found.");
                                    return false;
                                }
                            }
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid gather [war] [town] [town]");
                            return false;
                        }
                    }  else if (args[2].equalsIgnoreCase("phase")) {
                        if(args.length >= 6) {
                            for (Raid r : RaidData.getRaids()) {
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    //parse phase
                                    RaidPhase ph;
                                    if (args[5].equalsIgnoreCase("next")) {
                                        ph = RaidPhase.getNext(r.getPhase());
                                    } else {
                                        ph = RaidPhase.getByName(args[5]);
                                    }

                                    //if we found it
                                    if (ph != null) {
                                        r.setPhase(ph);
                                        r.setRaidTicks(ph.startTick);

                                        p.sendMessage(Helper.Chatlabel() + "Set phase for raid against " + args[4] + " in war " + args[3] + " to " + ph.name());
                                        Main.warLogger.log("Set phase for raid against " + args[4] + " in war " + args[3] + " to " + ph.name());
                                        return true;
                                    } else {
                                        p.sendMessage(Helper.color("c") + "Phase does not exist!");
                                        return false;
                                    }
                                } else {
                                    p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Raid cannot be found.");
                                    return false;
                                }
                            }
                        } else {
                                p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid phase [war] [town] [phase]");
                                return false;
                        }
                    }  else if (args[2].equalsIgnoreCase("loot")) {
                        if(args.length >= 5) {
                            for (Raid r : RaidData.getRaids()) {
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    //parse phase
                                    if(!p.getWorld().equals(r.getRaidedTown().getWorld())) {
                                        p.sendMessage(Helper.Chatlabel() + "Error wrong world");
                                        return false;
                                    }
                                    if(args.length >= 9) {
                                        if(args[5].equalsIgnoreCase("value")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getWorld().getName(), Integer.parseInt(args[7]), Integer.parseInt(args[8]));
                                            Raid.LootBlock lb = r.getLootedChunks().get(wc);
                                            lb.value = Integer.parseInt(args[6]);
                                            return true;
                                        } else if(args[5].equalsIgnoreCase("looted")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getWorld().getName(), Integer.parseInt(args[7]), Integer.parseInt(args[8]));
                                            Raid.LootBlock lb = r.getLootedChunks().get(wc);
                                            lb.finished = Boolean.parseBoolean(args[6]);
                                            return true;
                                        } else if(args[5].equalsIgnoreCase("ticks")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getWorld().getName(), Integer.parseInt(args[7]), Integer.parseInt(args[8]));
                                            Raid.LootBlock lb = r.getLootedChunks().get(wc);
                                            lb.ticks = Integer.parseInt(args[6]);
                                            return true;
                                        } else if(args[5].equalsIgnoreCase("reset")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getWorld().getName(), Integer.parseInt(args[6]), Integer.parseInt(args[7]));
                                            r.getLootedChunks().remove(wc);
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid loot [war] [town] [value,looted,ticks,reset] [amt] (x) (z)");
                                            return false;
                                        }
                                    } else if (args.length == 8) {
                                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid loot [war] [town] [value,looted,ticks,reset] [amt] (x) (z)");
                                        return false;
                                    } else {
                                        if(args[5].equalsIgnoreCase("value")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getLocation());
                                            Raid.LootBlock lb = r.getLootedChunks().get(wc);
                                            lb.value = Integer.parseInt(args[6]);
                                            return true;
                                        } else if(args[5].equalsIgnoreCase("looted")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getLocation());
                                            Raid.LootBlock lb = r.getLootedChunks().get(wc);
                                            lb.finished = Boolean.parseBoolean(args[6]);
                                            return true;
                                        } else if(args[5].equalsIgnoreCase("ticks")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getLocation());
                                            Raid.LootBlock lb = r.getLootedChunks().get(wc);
                                            lb.ticks = Integer.parseInt(args[6]);
                                            return true;
                                        } else if(args[5].equalsIgnoreCase("reset")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getLocation());
                                            r.getLootedChunks().remove(wc);
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid loot [war] [town] [value,looted,ticks,reset] [amt] (x) (z)");
                                            return false;
                                        }
                                    }
                                } else {
                                    p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Raid cannot be found.");
                                    return false;
                                }
                            }
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid loot [war] [town] [value,looted,ticks,reset] [amt] (x) (z)");
                            return false;
                        }
                    }  else if (args[2].equalsIgnoreCase("time")) {

                    }  else if (args[2].equalsIgnoreCase("owner")) {

                    }  else if (args[2].equalsIgnoreCase("move")) {
                        //TODO later
                        p.sendMessage(Helper.color("c") + "Error!");
                        return false;
                    }  else if (args[2].equalsIgnoreCase("clearActive")) {
                        //TODO later
                        p.sendMessage(Helper.color("c") + "Error!");
                        return false;
                    }  else {
                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid [propery]");
                        return false;
                    }
                } else {
                    p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify [raid/siege/war] [propery]");
                    return false;
                }
            } else {
                return fail(p, args, "syntax");
            }
        } else {
            return fail(p, args, "syntax");
        }
    }


    private static boolean rule(Player p, String[] args) {
        return false;
    }

    private static boolean fail(Player p, String[] args, String type) {
        switch (type) {
            case "permissions": {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("&cYou do not have permission to do this"));
                return false;
            }
            case "syntax": {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /alathrawaradmin help");
                return false;
            }
            default: {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Something wrong. /alathrawaradmin help");
                return false;
            }
        }
    }
}
