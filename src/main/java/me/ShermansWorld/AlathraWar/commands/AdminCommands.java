package me.ShermansWorld.AlathraWar.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.util.TimeMgmt;
import me.ShermansWorld.AlathraWar.*;
import me.ShermansWorld.AlathraWar.data.RaidData;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import me.ShermansWorld.AlathraWar.data.SiegeData;
import me.ShermansWorld.AlathraWar.data.WarData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
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
     * -force leave raid  [war] [player] (timeout) //kicks from raid party
     *
     * //ultra low priority
     * //exclude players from join a raid or war for a time
     * //raid subcommand prevents joining or being raided
     * -exclude player war [war] (side) (timeout)
     * -exclude town war [war] (side) (timeout)
     * -exclude nation war [war] (side) (timeout)
     *
     * //ultra low priority
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
            } else if (args[0].equalsIgnoreCase("force")) {
                return force(p, args);
            } else if (args[0].equalsIgnoreCase("help")) {
                return help(p, args);
            } else if (args[0].equalsIgnoreCase("info")) {
                return info(p, args);
            } else if (args[0].equalsIgnoreCase("modify")) {
                return modify(p, args);
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
     * -force join siege [player] [war] [town] (side)
     * -force join raid [player] [war] [town] (side) //TODO time until must be in gather?
     * -force leave raid [war] [player] (timeout) //kicks from raid party
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
                        for (Raid r : RaidData.getRaids()) {
                            if(args.length >= 6) {
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    if (r.getSide1AreRaiders()) {
                                        if (args[5].equals(r.getWar().getSide1())) {
                                            r.raidersWin(r.getOwner(), r.getRaidScore());
                                            p.sendMessage(Helper.Chatlabel() + "Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (raiders) declared as victor.");
                                            Main.warLogger.log("Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (raiders) declared as victor.");
                                            return true;
                                        } else if (args[5].equals(r.getWar().getSide2())) {
                                            r.defendersWin(r.getRaidScore());
                                            p.sendMessage(Helper.Chatlabel() + "Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (defenders) declared as victor.");
                                            Main.warLogger.log("Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (defenders) declared as victor.");
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Side not found!");
                                            return false;
                                        }
                                    } else {
                                        if (args[5].equals(r.getWar().getSide1())) {
                                            r.defendersWin(r.getRaidScore());
                                            p.sendMessage(Helper.Chatlabel() + "Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (defenders) declared as victor.");
                                            Main.warLogger.log("Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (defenders) declared as victor.");
                                            return true;
                                        } else if (args[5].equals(r.getWar().getSide2())) {
                                            r.raidersWin(r.getOwner(), r.getRaidScore());
                                            p.sendMessage(Helper.Chatlabel() + "Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (raiders) declared as victor.");
                                            Main.warLogger.log("Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (raiders) declared as victor.");
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Side not found!");
                                            return false;
                                        }
                                    }
                                }
                            } else if(args.length  == 5){
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    r.noWinner();
                                    p.sendMessage(Helper.Chatlabel() + "Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " with no victor.");
                                    Main.warLogger.log("Raid forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " with no victor.");
                                    return true;
                                }
                            } else {
                                p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin force end raid [war] [town] (side)");
                                return false;
                            }
                        }
                        p.sendMessage(Helper.color("c") + "Raid not found!");
                        return false;

                    } else if(args[2].equalsIgnoreCase("siege")) {
                        for (Siege s : SiegeData.getSieges()) {
                            if(args.length >= 6) {
                                if (s.getWar().getName().equals(args[3]) && s.getTown().getName().equals(args[4])) {
                                    if (s.getSide1AreAttackers()) {
                                        if (args[5].equals(s.getWar().getSide1())) {
                                            s.attackersWin(s.getSiegeOwner());
                                            p.sendMessage(Helper.Chatlabel() + "Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (attackers) declared as victor.");
                                            Main.warLogger.log("Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (attackers) declared as victor.");
                                            return true;
                                        } else if (args[5].equals(s.getWar().getSide2())) {
                                            s.defendersWin();
                                            p.sendMessage(Helper.Chatlabel() + "Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (defenders) declared as victor.");
                                            Main.warLogger.log("Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (defenders) declared as victor.");
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Side not found!");
                                            return false;
                                        }
                                    } else {
                                        if (args[5].equals(s.getWar().getSide1())) {
                                            s.defendersWin();
                                            p.sendMessage(Helper.Chatlabel() + "Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (defenders) declared as victor.");
                                            Main.warLogger.log("Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (defenders) declared as victor.");
                                            return true;
                                        } else if (args[5].equals(s.getWar().getSide2())) {
                                            s.attackersWin(s.getSiegeOwner());
                                            p.sendMessage(Helper.Chatlabel() + "Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (attackers) declared as victor.");
                                            Main.warLogger.log("Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " (attackers) declared as victor.");
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Side not found!");
                                            return false;
                                        }
                                    }
                                }
                            } else if(args.length  == 5){
                                if (s.getWar().getName().equals(args[3]) && s.getTown().getName().equals(args[4])) {
                                    s.noWinner();
                                    p.sendMessage(Helper.Chatlabel() + "Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " with no victor.");
                                    Main.warLogger.log("Siege forcefully ended on " + args[4] + " in war " + args[3] + " with " + args[6] + " with no victor.");
                                    return true;
                                }
                            } else {
                                p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin force end siege [war] [town] (side)");
                                return false;
                            }
                        }
                        p.sendMessage(Helper.color("c") + "Siege not found!");
                        return false;
                    } else if(args[2].equalsIgnoreCase("war")) {
                        //TODO determine if needed
                        p.sendMessage(Helper.color("c") + "Unused! use /war delete");
                        return false;
                    } else {
                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin force end [raid/siege]");
                        return false;
                    }
                } else {
                    p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin force end [raid/siege]");
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
                        p.sendMessage(Helper.color("c") + "Error!");
                        //TODO when siege commands are done
                        return false;
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
                        if (args.length >= 6) {
                            for (Raid r : RaidData.getRaids()) {
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    if (Bukkit.getPlayer(args[5]) != null) {
                                        String[] fixed = new String[] {
                                                args[1],
                                                args[3],
                                                args[4],
                                                args[5]
                                        };
                                        RaidCommands.leaveRaid(p, fixed, true);
                                        p.sendMessage(Helper.Chatlabel() + "Forced player " + args[5] + " to leave raid on " + args[4] + " in war " + args[3]);
                                        Main.warLogger.log("Forced player " + args[5] + " to leave raid on " + args[4] + " in war " + args[3]);
                                        return true;
                                    } else {
                                        p.sendMessage(Helper.color("c") + "Player not found!");
                                        return false;
                                    }
                                }
                            }
                            p.sendMessage(Helper.color("c") + "Raid not found!");
                            return false;
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin force leave raid [war] [town] [player] (timeout)");
                            return false;
                        }
                    } else if(args[2].equalsIgnoreCase("siege")) {
                        //TODO determine if needed
                        p.sendMessage(Helper.color("c") + "Unused!");
                        return false;
                    } else if(args[2].equalsIgnoreCase("war")) {
                        //TODO determine if needed
                        p.sendMessage(Helper.color("c") + "Unused! use /war surrender");
                        return false;
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
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin force");
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin help");
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin info");
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin modify");
        return true;
    }

    /**
     * // low priority idea
     * -info war [war]
     *
     * -info raid [war] [town]
     *
     * -info siege [war] [town]
     *
      * @param p
     * @param args
     * @return
     */
    private static boolean info(Player p, String[] args) {
        if(args.length >= 2) {
            if(args[1].equalsIgnoreCase("war")) {
                if(args.length >= 3) {
                    for (War w : WarData.getWars()) {
                        if (w.getName().equals(args[2])) {
                            p.sendMessage(Helper.Chatlabel() + "Info dump for war: " + w.getName());
                            p.sendMessage(Helper.Chatlabel() + "oOo----------------------===----------------------oOo");
                            p.sendMessage(Helper.Chatlabel() + "Name: " + w.getName());
                            p.sendMessage(Helper.Chatlabel() + "Side 1: " + w.getSide1());
                            p.sendMessage(Helper.Chatlabel() + "Side 2: " + w.getSide2());
                            // TODO I think this works!
                            p.sendMessage(Helper.Chatlabel() + "Last Raid: " + TimeMgmt.getFormattedTimeValue(w.getLastRaidTime() * 1000));
                            p.sendMessage(Helper.Chatlabel() + "oOo----------------------===----------------------oOo");
                            String side1Towns = "";
                            String side1Players = "";
                            for (String t : w.getSide1Towns()) {
                                side1Towns += t;
                                side1Towns += ", ";
                            }
                            for (String pl : w.getSide1Players()) {
                                side1Players += pl;
                                side1Players += ", ";
                            }
                            //cut off last two characters
                            side1Towns = side1Towns.substring(0, side1Towns.length() - 3);
                            side1Players = side1Players.substring(0, side1Players.length() - 3);
                            p.sendMessage(Helper.Chatlabel() + w.getSide1() + " Towns: " + side1Towns);
                            p.sendMessage(Helper.Chatlabel() + w.getSide1() + " Players: " + side1Players);
                            p.sendMessage(Helper.Chatlabel() + "oOo----------------------===----------------------oOo");
                            String side2Towns = "";
                            String side2Players = "";
                            for (String t : w.getSide2Towns()) {
                                side2Towns += t;
                                side2Towns += ", ";
                            }
                            for (String pl : w.getSide2Players()) {
                                side2Players += pl;
                                side2Players += ", ";
                            }
                            //cut off last two characters
                            side2Towns = side2Towns.substring(0, side2Towns.length() - 3);
                            side2Players = side2Players.substring(0, side2Players.length() - 3);
                            p.sendMessage(Helper.Chatlabel() + w.getSide2() + " Towns: " + side2Towns);
                            p.sendMessage(Helper.Chatlabel() + w.getSide2() + " Players: " + side2Players);
                            return true;
                        }
                    }
                    p.sendMessage(Helper.color("c") + "Error Raid not found!");
                } else {
                    p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin info [raid/siege/war]");
                }
                return false;
            } else if(args[1].equalsIgnoreCase("raid")) {
                if(args.length >= 4) {
                    for (Raid r : RaidData.getRaids()) {
                        if (r.getWar().getName().equals(args[2]) && r.getRaidedTown().getName().equals(args[3])) {
                            p.sendMessage(Helper.Chatlabel() + "Info dump for raid: " + r.getName());
                            p.sendMessage(Helper.Chatlabel() + "oOo----------------------===----------------------oOo");
                            p.sendMessage(Helper.Chatlabel() + "Name: " + r.getName());
                            p.sendMessage(Helper.Chatlabel() + "Raiders: " + r.getRaiders());
                            p.sendMessage(Helper.Chatlabel() + "Defenders: " + r.getDefenders());
                            p.sendMessage(Helper.Chatlabel() + "Raid Score: " + r.getRaidScore());
                            p.sendMessage(Helper.Chatlabel() + "War: " + r.getWar().getName());
                            p.sendMessage(Helper.Chatlabel() + "Raided Town: " + r.getRaidedTown().getName());
                            p.sendMessage(Helper.Chatlabel() + "Gather Town: " + r.getGatherTown().getName());
                            p.sendMessage(Helper.Chatlabel() + "Current Phase: " + r.getPhase().name());
                            p.sendMessage(Helper.Chatlabel() + "Tick progress: " + r.getRaidTicks());
                            p.sendMessage(Helper.Chatlabel() + "Owner: " + r.getOwner());
                            p.sendMessage(Helper.Chatlabel() + "Gather Homeblock: " + r.getHomeBlockGather().toString());
                            p.sendMessage(Helper.Chatlabel() + "Raided Homeblock: " + r.getHomeBlockRaided().toString());
                            p.sendMessage(Helper.Chatlabel() + "oOo----------------------===----------------------oOo");
                            String activeRaiders = "";
                            for (String pl : r.getActiveRaiders()) {
                                activeRaiders += pl;
                                activeRaiders += ", ";
                            }
                            //cut off last two characters
                            activeRaiders = activeRaiders.substring(0, activeRaiders.length() - 3);
                            p.sendMessage(Helper.Chatlabel() + "Raiding Players: " + activeRaiders);
                            return true;
                        }
                    }
                    p.sendMessage(Helper.color("c") + "Error Raid not found!");
                    return false;
                } else {
                    p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin info [raid/siege/war]");
                    return false;
                }
            } else if(args[1].equalsIgnoreCase("siege")) {
                if(args.length >= 4) {
                    for (Siege s : SiegeData.getSieges()) {
                        if (s.getWar().getName().equals(args[2]) && s.getTown().getName().equals(args[3])) {
                            p.sendMessage(Helper.Chatlabel() + "Info dump for siege: " + s.getName());
                            p.sendMessage(Helper.Chatlabel() + "oOo----------------------===----------------------oOo");
                            p.sendMessage(Helper.Chatlabel() + "Name: " + s.getName());
                            p.sendMessage(Helper.Chatlabel() + "Attackers: " + s.getAttackers());
                            p.sendMessage(Helper.Chatlabel() + "Defenders: " + s.getDefenders());
                            p.sendMessage(Helper.Chatlabel() + "Attackers points: " + s.getAttackerPoints());
                            p.sendMessage(Helper.Chatlabel() + "Attackers points: " + s.getDefenderPoints());
                            p.sendMessage(Helper.Chatlabel() + "War: " + s.getWar().getName());
                            p.sendMessage(Helper.Chatlabel() + "Attacked Town: " + s.getTown().getName());
                            p.sendMessage(Helper.Chatlabel() + "Max Ticks: " + s.getMaxSiegeTicks());
                            p.sendMessage(Helper.Chatlabel() + "Tick progress: " + s.getSiegeTicks());
                            p.sendMessage(Helper.Chatlabel() + "Owner: " + s.getSiegeOwner());
                            p.sendMessage(Helper.Chatlabel() + "Homeblock: " + s.getHomeBlock().toString());
                            p.sendMessage(Helper.Chatlabel() + "oOo----------------------===----------------------oOo");
                            String attackers = "";
                            for (String pl : s.getAttackerPlayers()) {
                                attackers += pl;
                                attackers += ", ";
                            }
                            //cut off last two characters
                            attackers = attackers.substring(0, attackers.length() - 3);
                            p.sendMessage(Helper.Chatlabel() + "Attacking Players: " + attackers);
                            String defenders = "";
                            for (String pl : s.getAttackerPlayers()) {
                                defenders += pl;
                                defenders += ", ";
                            }
                            //cut off last two characters
                            defenders = defenders.substring(0, defenders.length() - 3);
                            p.sendMessage(Helper.Chatlabel() + "Defending Players: " + defenders);
                            return true;
                        }
                    }
                    p.sendMessage(Helper.color("c") + "Error Siege not found!");
                    return false;
                } else {
                    p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin info [raid/siege/war]");
                    return false;
                }
            } else {
                return fail(p, args, "syntax");
            }
        }
        return fail(p, args, "syntax");
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
     *
     * -modify siege score [war] [town] [side] [amt]
     * -modify siege townspawn [war] [town] (x) (Z)
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
                                if(r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    if(args[5].equalsIgnoreCase("add")) {
                                        r.addPointsToRaidScore(Integer.parseInt(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Added " + args[6] + " points to the raid score in the war " + args[3] + " on town " + args[4]);
                                        Main.warLogger.log("Added " + args[6] + " points to the raid score in the war " + args[3] + " on town " + args[4]);
                                        return true;
                                    } else if(args[5].equalsIgnoreCase("subtract")) {
                                        r.subtractPointsFromRaidScore(Integer.parseInt(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Subtracted " + args[6] + " points to the raid score in the war " + args[3] + " on town " + args[4]);
                                        Main.warLogger.log("Subtracted " + args[6] + " points to the raid score in the war " + args[3] + " on town " + args[4]);
                                        return true;
                                    } else if(args[5].equalsIgnoreCase("set")) {
                                        r.setRaidScore(Integer.parseInt(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Set " + args[6] + " points as the raid score in the war " + args[3] + " on town " + args[4]);
                                        Main.warLogger.log("Set " + args[6] + " points as the raid score in the war " + args[3] + " on town " + args[4]);
                                        return true;
                                    }  else {
                                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid score [add/subtract/set] [war] [town] [value]");
                                        return false;
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Raid cannot be found.");
                            return false;
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
                                                    r.setHomeBlockRaided(tb.getTownBlock());
                                                    r.setTownSpawnRaided(t.getSpawn());
                                                    p.sendMessage(Helper.Chatlabel() + "Set town spawn for raided town " + args[4] + " in war " + args[3] + " to " + p.getLocation().toString());
                                                    Main.warLogger.log("Set town spawn for raided town " + args[4] + " in war " + args[3] + " to [" + args[5] + "," + args[6] + "," + args[7] + "]");
                                                    return true;
                                                } else {
                                                    p.sendMessage(Helper.color("c") + "Town does not contain town block at [" + args[5] + "," + args[7] + "]");
                                                    return false;
                                                }
                                            } catch (NotRegisteredException e) {
                                                p.sendMessage(Helper.color("c") + "Error! Townblock does not exist!");
                                                return false;
                                            } catch (TownyException e) {
                                                p.sendMessage(Helper.color("c") + "Error!");
                                                return false;
                                            }
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Error! Wrong world!");
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
                                                    r.setHomeBlockRaided(tb.getTownBlock());
                                                    r.setTownSpawnRaided(t.getSpawn());
                                                    p.sendMessage(Helper.Chatlabel() + "Set town spawn for raided town " + args[5] + " in war " + args[4] + " to " + p.getLocation().toString());
                                                    Main.warLogger.log("Set town spawn for raided town " + args[5] + " in war " + args[4] + " to " + p.getLocation().toString());
                                                    return true;
                                                } else {
                                                    p.sendMessage(Helper.color("c") + "Town does not contain town block at your location [" + (int) p.getLocation().getX() + "," + (int) p.getLocation().getZ() + "]");
                                                    return false;
                                                }
                                            } catch (NotRegisteredException e) {
                                                p.sendMessage(Helper.color("c") + "Error! Townblock does not exist!");
                                                return false;
                                            } catch (TownyException e) {
                                                p.sendMessage(Helper.color("c") + "Error!");
                                                return false;
                                            }
                                        } else {
                                            return false;
                                        }
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Raid cannot be found.");
                            return false;
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
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Raid cannot be found.");
                            return false;
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
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Raid cannot be found.");
                            return false;
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
                                            p.sendMessage(Helper.Chatlabel() + "Set value for a chunk [" + args[7] + "," + args[8] + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            Main.warLogger.log("Set value for a chunk [" + args[7] + "," + args[8] + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            return true;
                                        } else if(args[5].equalsIgnoreCase("looted")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getWorld().getName(), Integer.parseInt(args[7]), Integer.parseInt(args[8]));
                                            Raid.LootBlock lb = r.getLootedChunks().get(wc);
                                            lb.finished = Boolean.parseBoolean(args[6]);
                                            p.sendMessage(Helper.Chatlabel() + "Set finished flag for a chunk [" + args[7] + "," + args[8] + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            Main.warLogger.log("Set finished flag for a chunk [" + args[7] + "," + args[8] + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            return true;
                                        } else if(args[5].equalsIgnoreCase("ticks")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getWorld().getName(), Integer.parseInt(args[7]), Integer.parseInt(args[8]));
                                            Raid.LootBlock lb = r.getLootedChunks().get(wc);
                                            lb.ticks = Integer.parseInt(args[6]);
                                            p.sendMessage(Helper.Chatlabel() + "Set ticks for a chunk [" + args[7] + "," + args[8] + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            Main.warLogger.log("Set ticks for a chunk [" + args[7] + "," + args[8] + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            return true;
                                        } else if(args[5].equalsIgnoreCase("reset")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getWorld().getName(), Integer.parseInt(args[6]), Integer.parseInt(args[7]));
                                            r.getLootedChunks().remove(wc);
                                            p.sendMessage(Helper.Chatlabel() + "Reset loot for a chunk [" + args[7] + "," + args[8] + "] in raid against " + args[4] + " in war " + args[3]);
                                            Main.warLogger.log("Reset loot for a chunk [" + args[7] + "," + args[8] + "] in raid against " + args[4] + " in war " + args[3]);
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
                                            p.sendMessage(Helper.Chatlabel() + "Set value for a chunk [" + p.getLocation().getX() + "," + p.getLocation().getZ() + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            Main.warLogger.log("Set value for a chunk [" + p.getLocation().getX() + "," + p.getLocation().getZ() + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            return true;
                                        } else if(args[5].equalsIgnoreCase("looted")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getLocation());
                                            Raid.LootBlock lb = r.getLootedChunks().get(wc);
                                            lb.finished = Boolean.parseBoolean(args[6]);
                                            p.sendMessage(Helper.Chatlabel() + "Set looted status for a chunk [" + p.getLocation().getX() + "," + p.getLocation().getZ() + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            Main.warLogger.log("Set looted status for a chunk [" + p.getLocation().getX() + "," + p.getLocation().getZ() + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            return true;
                                        } else if(args[5].equalsIgnoreCase("ticks")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getLocation());
                                            Raid.LootBlock lb = r.getLootedChunks().get(wc);
                                            lb.ticks = Integer.parseInt(args[6]);
                                            p.sendMessage(Helper.Chatlabel() + "Set ticks for a chunk [" + p.getLocation().getX() + "," + p.getLocation().getZ() + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            Main.warLogger.log("Set ticks for a chunk [" + p.getLocation().getX() + "," + p.getLocation().getZ() + "] in raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            return true;
                                        } else if(args[5].equalsIgnoreCase("reset")) {
                                            WorldCoord wc = WorldCoord.parseWorldCoord(p.getLocation());
                                            r.getLootedChunks().remove(wc);
                                            p.sendMessage(Helper.Chatlabel() + "Reset loot for a chunk [" + p.getLocation().getX() + "," + p.getLocation().getZ() + "] in raid against " + args[4] + " in war " + args[3]);
                                            Main.warLogger.log("Reset Loot for a chunk [" + p.getLocation().getX() + "," + p.getLocation().getZ() + "] in raid against " + args[4] + " in war " + args[3]);
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid loot [war] [town] [value,looted,ticks,reset] [amt] (x) (z)");
                                            return false;
                                        }
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Raid cannot be found.");
                            return false;
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid loot [war] [town] [value,looted,ticks,reset] [amt] (x) (z)");
                            return false;
                        }
                    }  else if (args[2].equalsIgnoreCase("time")) {
                        if(args.length >= 7) {
                            for (Raid r : RaidData.getRaids()) {
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    //parse phase
                                    if(args[5].equalsIgnoreCase("add")) {
                                        r.setRaidTicks(r.getRaidTicks() + Integer.parseInt(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Set time for raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                        Main.warLogger.log("Set time for raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                        return true;
                                    } else if(args[5].equalsIgnoreCase("set")) {
                                        int t = Integer.parseInt(args[6]);
                                        if(t >= r.getPhase().startTick) {
                                            r.setRaidTicks(t);
                                            p.sendMessage(Helper.Chatlabel() + "Set time for raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            Main.warLogger.log("Set time for raid against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Time set before current phase, use \"/alathrawaradmin modify raid phase\" instead");
                                            return false;
                                        }
                                    } else {
                                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid time [war] [town] [add/set] [value]");
                                        return false;
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Raid cannot be found.");
                            return false;
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid time [war] [town] [add/set] [value]");
                            return false;
                        }
                    }  else if (args[2].equalsIgnoreCase("owner")) {
                        if(args.length >= 6) {
                            for (Raid r : RaidData.getRaids()) {
                                if (r.getWar().getName().equals(args[3]) && r.getRaidedTown().getName().equals(args[4])) {
                                    Player own = Bukkit.getPlayer(args[5]);
                                    if(own != null) {
                                        r.setOwner(own);
                                        p.sendMessage(Helper.Chatlabel() + "Set owner of raid against " + args[4] + " in war " + args[3] + " to " + own.getName());
                                        Main.warLogger.log("Set owner of raid against " + args[4] + " in war " + args[3] + " to " + own.getName());
                                        return true;
                                    } else {
                                        p.sendMessage(Helper.color("c") + "Player not found!");
                                        return false;
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Raid cannot be found.");
                            return false;
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid owner [war] [town] [player]");
                            return false;
                        }
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
            } else if (args[1].equalsIgnoreCase("siege")) {
                if (args.length >= 3) {
                    if(args[2].equalsIgnoreCase("score")) {
                        if(args.length >= 8) {
                            for(Siege s: SiegeData.getSieges()) {
                                if (s.getWar().getName().equals(args[3]) && s.getTown().getName().equals(args[4])) {
                                    if(args[5].equalsIgnoreCase("add")) {
                                        if(s.getWar().getSide1().equals(args[6])) {
                                            if(s.getSide1AreAttackers()) {
                                                s.addPointsToAttackers(Integer.parseInt(args[7]));
                                                p.sendMessage(Helper.Chatlabel() + "Added " + args[7] + " points to side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                                Main.warLogger.log("Added " + args[7] + " points to side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                                return true;
                                            } else {
                                                s.addPointsToDefenders(Integer.parseInt(args[7]));
                                                p.sendMessage(Helper.Chatlabel() + "Added " + args[7] + " points to side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                                Main.warLogger.log("Added " + args[7] + " points to side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                                return true;
                                            }
                                        } else if(s.getWar().getSide2().equals(args[6])) {
                                            if(s.getSide1AreAttackers()) {
                                                s.addPointsToDefenders(Integer.parseInt(args[7]));
                                                p.sendMessage(Helper.Chatlabel() + "Added " + args[7] + " points to side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                                Main.warLogger.log("Added " + args[7] + " points to side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                                return true;
                                            } else {
                                                s.addPointsToAttackers(Integer.parseInt(args[7]));
                                                p.sendMessage(Helper.Chatlabel() + "Added " + args[7] + " points to side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                                Main.warLogger.log("Added " + args[7] + " points to side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                                return true;
                                            }
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Side not found");
                                            return false;
                                        }
                                    } else if(args[5].equalsIgnoreCase("set")) {
                                        if(s.getWar().getSide1().equals(args[6])) {
                                            if(s.getSide1AreAttackers()) {
                                                s.setAttackerPoints(Integer.parseInt(args[7]));
                                                p.sendMessage(Helper.Chatlabel() + "Set " + args[7] + " points for side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                                Main.warLogger.log("Set " + args[7] + " points for side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                                return true;
                                            } else {
                                                s.setDefenderPoints(Integer.parseInt(args[7]));
                                                p.sendMessage(Helper.Chatlabel() + "Set " + args[7] + " points for side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                                Main.warLogger.log("Set " + args[7] + " points for side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                                return true;
                                            }
                                        } else if(s.getWar().getSide2().equals(args[6])) {
                                            if(s.getSide1AreAttackers()) {
                                                s.setDefenderPoints(Integer.parseInt(args[7]));
                                                p.sendMessage(Helper.Chatlabel() + "Set " + args[7] + " points for side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                                Main.warLogger.log("Set " + args[7] + " points for side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                                return true;
                                            } else {
                                                s.setAttackerPoints(Integer.parseInt(args[7]));
                                                p.sendMessage(Helper.Chatlabel() + "Set " + args[7] + " points for side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                                Main.warLogger.log("Set " + args[7] + " points for side " + args[6] + " in sige on " + args[4] + " in war " + args[3]);
                                                return true;
                                            }
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Side not found");
                                            return false;
                                        }
                                    } else {
                                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify siege score [war] [town] [add/set] [side] [amt]");
                                        return false;
                                    }
                                } else {
                                    p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify siege score [war] [town] [add/set] [side] [amt]");
                                    return false;
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Siege cannot be found.");
                            return false;
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify siege score [war] [town] [add/set] [side] [amt]");
                            return false;
                        }
                    } else if(args[2].equalsIgnoreCase("townspawn")) {
                        if(args.length >= 5) {
                            for(Siege s: SiegeData.getSieges()) {
                                if (s.getWar().getName().equals(args[3]) && s.getTown().getName().equals(args[4])) {
                                    if (args.length == 6 || args.length == 7){
                                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify siege townspawn [war] [town] (x) (y) (Z)");
                                        return false;
                                    }
                                    if(args.length >= 8) {
                                        Town t = s.getTown();
                                        if(p.getWorld() == t.getWorld()) {
                                            try {
                                                WorldCoord tb = WorldCoord.parseWorldCoord(p.getWorld().getName(), Integer.parseInt(args[5]), Integer.parseInt(args[7]));
                                                if (t.hasTownBlock(tb)) {
                                                    t.setHomeBlock(tb.getTownBlock());
                                                    t.setSpawn(new Location(p.getWorld(), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7])));
                                                    s.setHomeBlock(tb.getTownBlock());
                                                    s.setTownSpawn(t.getSpawn());
                                                    p.sendMessage(Helper.Chatlabel() + "Set town spawn for sieged town " + args[4] + " in war " + args[3] + " to " + p.getLocation().toString());
                                                    Main.warLogger.log("Set town spawn for sieged town " + args[4] + " in war " + args[3] + " to [" + args[5] + "," + args[6] + "," + args[7] + "]");
                                                    return true;
                                                } else {
                                                    p.sendMessage(Helper.color("c") + "Town does not contain town block at [" + args[5] + "," + args[7] + "]");
                                                    return false;
                                                }
                                            } catch (NotRegisteredException e) {
                                                p.sendMessage(Helper.color("c") + "Error! Townblock does not exist!");
                                                return false;
                                            } catch (TownyException e) {
                                                p.sendMessage(Helper.color("c") + "Error!");
                                                return false;
                                            }
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Error! Wrong world!");
                                            return false;
                                        }
                                    } else {
                                        Town t = s.getTown();
                                        if(p.getWorld() == t.getWorld()) {
                                            try {
                                                WorldCoord tb = WorldCoord.parseWorldCoord(p.getWorld().getName(), (int) p.getLocation().getX(), (int) p.getLocation().getZ());
                                                if (t.hasTownBlock(tb)) {
                                                    t.setHomeBlock(tb.getTownBlock());
                                                    t.setSpawn(p.getLocation());
                                                    s.setHomeBlock(tb.getTownBlock());
                                                    s.setTownSpawn(t.getSpawn());
                                                    p.sendMessage(Helper.Chatlabel() + "Set town spawn for sieged town " + args[5] + " in war " + args[4] + " to " + p.getLocation().toString());
                                                    Main.warLogger.log("Set town spawn for sieged town " + args[5] + " in war " + args[4] + " to " + p.getLocation().toString());
                                                    return true;
                                                } else {
                                                    p.sendMessage(Helper.color("c") + "Town does not contain town block at your location [" + (int) p.getLocation().getX() + "," + (int) p.getLocation().getZ() + "]");
                                                    return false;
                                                }
                                            } catch (NotRegisteredException e) {
                                                p.sendMessage(Helper.color("c") + "Error! Townblock does not exist!");
                                                return false;
                                            } catch (TownyException e) {
                                                p.sendMessage(Helper.color("c") + "Error!");
                                                return false;
                                            }
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify siege townspawn [war] [town] (x) (y) (Z)");
                                            return false;
                                        }
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Siege cannot be found.");
                            return false;
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify siege townspawn [war] [town] (x) (y) (Z)");
                            return false;
                        }
                    } else if(args[2].equalsIgnoreCase("time")) {
                        if(args.length >= 7) {
                            for (Siege s : SiegeData.getSieges()) {
                                if (s.getWar().getName().equals(args[3]) && s.getTown().getName().equals(args[4])) {
                                    //parse phase
                                    if(args[5].equalsIgnoreCase("add")) {
                                        s.setSiegeTicks(s.getSiegeTicks() + Integer.parseInt(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Set time for siege against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                        Main.warLogger.log("Set time for siege against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                        return true;
                                    } else if(args[5].equalsIgnoreCase("set")) {
                                        s.setSiegeTicks(Integer.parseInt(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Set time for siege against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                        Main.warLogger.log("Set time for siege against " + args[4] + " in war " + args[3] + " to " + args[6]);
                                        return true;
                                    } else {
                                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify raid time [war] [town] [add/set] [value]");
                                        return false;
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Siege cannot be found.");
                            return false;
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify siege time [war] [town] [add/set/max] [value]");
                            return false;
                        }
                    } else if(args[2].equalsIgnoreCase("owner")) {
                        if(args.length >= 7) {
                            for (Siege s : SiegeData.getSieges()) {
                                if (s.getWar().getName().equals(args[3]) && s.getTown().getName().equals(args[4])) {
                                    Player own = Bukkit.getPlayer(args[5]);
                                    if(own != null) {
                                        if(s.attackerPlayers.contains(own.getName())) {
                                            s.setSiegeOwner(own);
                                            p.sendMessage(Helper.Chatlabel() + "Set owner of siege against " + args[4] + " in war " + args[3] + " to " + own.getName());
                                            Main.warLogger.log("Set owner of siege against " + args[4] + " in war " + args[3] + " to " + own.getName());
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Player not on attacking side!");
                                            return false;
                                        }
                                    } else {
                                        p.sendMessage(Helper.color("c") + "Player not found!");
                                        return false;
                                    }
                                }
                            }
                            p.sendMessage(Helper.Chatlabel() + Helper.color("c") + "Siege cannot be found.");
                            return false;
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify siege owner [war] [town] [newOwner]");
                            return false;
                        }
                    } else if(args[2].equalsIgnoreCase("move")) {
                        //TODO later
                        p.sendMessage(Helper.color("c") + "Error!");
                        return false;
                    } else {

                    }
                } else {
                    p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify [raid/siege/war] [propery]");
                    return false;
                }
            } else if (args[1].equalsIgnoreCase("war")) {
                if (args.length >= 3) {
                    if(args[2].equalsIgnoreCase("score")) {
                        if(args.length >= 6) {
                            for (War w : WarData.getWars()) {
                                if(w.getName().equals(args[3])) {
                                    //TODO war score
                                    p.sendMessage(Helper.color("c") + "Error!");
                                    return false;
                                }
                            }
                            p.sendMessage(Helper.color("c") + "Error: War not found!");
                            return false;
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify war score [war] [side] [add/set] [amt]");
                            return false;
                        }
                    } else if(args[2].equalsIgnoreCase("side")) {
                        if(args.length >= 6) {
                            for (War w : WarData.getWars()) {
                                if (w.getName().equals(args[3])) {
                                    if(w.getSide1().equals(args[4])) {
                                        w.setSide1(args[5]);
                                        p.sendMessage(Helper.Chatlabel() + "Set side " + args[4] + " to " + args[5] + " in war " + args[3]);
                                        Main.warLogger.log(Helper.Chatlabel() + "Set side " + args[4] + " to " + args[5] + " in war " + args[3]);
                                        return true;
                                    } else if (w.getSide2().equals(args[4])) {
                                        w.setSide2(args[5]);
                                        p.sendMessage(Helper.Chatlabel() + "Set side " + args[4] + " to " + args[5] + " in war " + args[3]);
                                        Main.warLogger.log(Helper.Chatlabel() + "Set side " + args[4] + " to " + args[5] + " in war " + args[3]);
                                        return true;
                                    } else {
                                        p.sendMessage(Helper.color("c") + "Error: Side not found!");
                                        return false;
                                    }
                                }
                            }
                            p.sendMessage(Helper.color("c") + "Error: War not found!");
                            return false;
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify war side [war] [side] [name]");
                            return false;
                        }
                    } else if(args[2].equalsIgnoreCase("name")) {
                        if(args.length >= 5) {
                            for (War w : WarData.getWars()) {
                                if (w.getName().equals(args[3])) {
                                    w.setName(args[4]);
                                    p.sendMessage(Helper.Chatlabel() + "Set name of war " + args[3] + " to " + args[4]);
                                    Main.warLogger.log(Helper.Chatlabel() + "Set name of war " + args[3] + " to " + args[4]);
                                    return true;
                                }
                            }
                            p.sendMessage(Helper.color("c") + "Error: War not found!");
                            return false;
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify war name [war] [name]");
                            return false;
                        }
                    }
                    //TODO IDK IF THIS WORKS
                    else if(args[2].equalsIgnoreCase("add")) {
                        if(args.length >= 7) {
                            for (War w : WarData.getWars()) {
                                if (w.getName().equals(args[3])) {
                                    if(!w.getSide1().equals(args[4]) && !w.getSide1().equals(args[4])) {
                                        p.sendMessage(Helper.color("c") + "Error: Side not found!");
                                        return false;
                                    }
                                    if(args[5].equalsIgnoreCase("town")) {
                                        Town t = TownyAPI.getInstance().getTown(args[6]);
                                        if (t != null) {
                                            w.addTown(t, args[5]);
                                            p.sendMessage(Helper.Chatlabel() + "Added town " + args[6] + " war " + args[3] + " on side " + args[4]);
                                            Main.warLogger.log(Helper.Chatlabel() + "Added town " + args[6] + " war " + args[3] + " on side " + args[4]);
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Error: Town not found!");
                                            return false;
                                        }
                                    } else if(args[5].equalsIgnoreCase("nation")) {
                                        Nation n = TownyAPI.getInstance().getNation(args[6]);
                                        if (n != null) {
                                            w.addNation(n, args[5]);
                                            p.sendMessage(Helper.Chatlabel() + "Added nation " + args[6] + " war " + args[3] + " on side " + args[4]);
                                            Main.warLogger.log(Helper.Chatlabel() + "Added nation " + args[6] + " war " + args[3] + " on side " + args[4]);
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Error: Nation not found!");
                                            return false;
                                        }
                                    } else {
                                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify war add [war] [side] town/nation [town/nation]");
                                        return false;
                                    }
                                }
                            }
                            p.sendMessage(Helper.color("c") + "Error: War not found!");
                            return false;
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify war add [war] [town/nation] town/nation ");
                            return false;
                        }
                    }
                    //TODO IDK IF THIS WORKS
                    else if(args[2].equalsIgnoreCase("surrender")) {
                        if(args.length >= 7) {
                            for (War w : WarData.getWars()) {
                                if (w.getName().equals(args[3])) {
                                    if(!w.getSide1().equals(args[4]) && !w.getSide1().equals(args[4])) {
                                        p.sendMessage(Helper.color("c") + "Error: Side not found!");
                                        return false;
                                    }
                                    if(args[5].equalsIgnoreCase("town")) {
                                        Town t = TownyAPI.getInstance().getTown(args[6]);
                                        if (t != null) {
                                            w.surrenderTown(t.getName());
                                            p.sendMessage(Helper.Chatlabel() + "Surrendered town " + args[6] + " war " + args[3] + " on side " + args[4]);
                                            Main.warLogger.log(Helper.Chatlabel() + "Surrendered town " + args[6] + " war " + args[3] + " on side " + args[4]);
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Error: Town not found!");
                                            return false;
                                        }
                                    } else if(args[5].equalsIgnoreCase("nation")) {
                                        Nation n = TownyAPI.getInstance().getNation(args[6]);
                                        if (n != null) {
                                            w.surrenderNation(n);
                                            p.sendMessage(Helper.Chatlabel() + "Surrendered nation " + args[6] + " war " + args[3] + " on side " + args[4]);
                                            Main.warLogger.log(Helper.Chatlabel() + "Surrendered nation " + args[6] + " war " + args[3] + " on side " + args[4]);
                                            return true;
                                        } else {
                                            p.sendMessage(Helper.color("c") + "Error: Nation not found!");
                                            return false;
                                        }
                                    } else {
                                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify war surrender [war] [side] town/nation [town/nation]");
                                        return false;
                                    }
                                }
                            }
                            p.sendMessage(Helper.color("c") + "Error: War not found!");
                            return false;
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify war surrender town [war] [town]");
                            return false;
                        }
                    } else if(args[2].equalsIgnoreCase("raidTime")) {
                        if(args.length >= 7) {
                            for (War w : WarData.getWars()) {
                                if (w.getName().equals(args[4])) {
                                    if(args[3].equalsIgnoreCase("add")) {
                                        w.setLastRaidTime(w.getLastRaidTime() + Integer.parseInt(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Added " + args[6] + " to last raid time in war " + args[3]);
                                        Main.warLogger.log(Helper.Chatlabel() + "Added " + args[6] + " to last raid time in war " + args[3]);
                                        return true;
                                    } else if(args[3].equalsIgnoreCase("set")) {
                                        w.setLastRaidTime(Long.parseLong(args[6]));
                                        p.sendMessage(Helper.Chatlabel() + "Set last raid time in war " + args[3] + " to " + args[6]);
                                        Main.warLogger.log(Helper.Chatlabel() + "Set last raid time in war " + args[3] + " to " + args[6]);
                                        return true;
                                    } else if(args[3].equalsIgnoreCase("reset")) {
                                        w.setLastRaidTime(0L);
                                        p.sendMessage(Helper.Chatlabel() + "Reset last raid time in war " + args[3]);
                                        Main.warLogger.log(Helper.Chatlabel() + "Reset last raid time in war " + args[3]);
                                        return true;
                                    } else {
                                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify war raidTime [add,set,reset] [war] [town] [amt]");
                                        return false;
                                    }
                                }
                            }
                            p.sendMessage(Helper.color("c") + "Error: War not found!");
                            return false;
                        } else {
                            p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify war raidTime [add,set,reset] [war] [town] [amt]");
                            return false;
                        }
                    } else {
                        p.sendMessage(Helper.color("c") + "Usage: /alathrawaradmin modify [raid/siege/war] [propery]");
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
        return fail(p, args, "syntax");
    }


    private static boolean rule(Player p, String[] args) {
        return false;
    }

    private static boolean fail(Player p, String[] args, String type) {
        switch (type) {
            case "permissions" -> {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("&cYou do not have permission to do this"));
                return false;
            }
            case "syntax" -> {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /alathrawaradmin help");
                return false;
            }
            default -> {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Something wrong. /alathrawaradmin help");
                return false;
            }
        }
    }
}
