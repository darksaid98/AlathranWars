package me.ShermansWorld.AlathraWar.listeners;

import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Raid;
import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.War;
import me.ShermansWorld.AlathraWar.commands.RaidCommands;
import me.ShermansWorld.AlathraWar.commands.SiegeCommands;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import me.ShermansWorld.AlathraWar.data.WarData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandsListener implements Listener
{

    final static String[] prefixes = new String[] {
        "", "towny", "essentials", "wild", "minecraft"
    };
    final static String[] blacklistedLong = new String[] {
            "n spawn", "nat spawn", "nation spawn",
            "t spawn", "town spawn"
    };
    final static String[] blacklistedShort = new String[] {
            "home", "homes", "warp", "warps",
            "wild", "rtp", "spawn", "wilderness", "wildtp",
            "tpa", "tpahere", "tpaccept", "tpacancel",
            "etpa", "etpahere", "etpaccept", "etpacancel",
            "ehome", "ehomes", "ewarp", "ewarps"

    };



    @EventHandler(priority = EventPriority.HIGH)
    public void onCommandSend(final PlayerCommandPreprocessEvent event) {

        Player p = event.getPlayer();

        //if player is admin, ignore this behavior
        if (p.hasPermission("!AlathraWar.admin")) return;

        String[] args = event.getMessage().split(" ");
        /*
        Prevent any teleport command events from occuring
        If using PlayerTeleportEvent, it breaks the death logic

        allow n spawn and t spawn for defenders trapped in spawn
         */
        for(War war : WarData.getWars()) {
            for(Raid raid : war.getRaids()) {
                //Only do this during regular phases, start and end ignored in case something breaks
                if (raid.getPhase() == RaidPhase.COMBAT || raid.getPhase() == RaidPhase.TRAVEL || raid.getPhase() == RaidPhase.GATHER) {
                    //only do for active raiders and any defenders
                    if (raid.getActiveRaiders().contains(p.getName()) || raid.getDefenders().contains(p.getName())) {
                        //n spawn and t spawn
                        if (args.length >= 2) {
                            //parse what we have, remove the starting sslash
                            String parse = (args[0].charAt(0) == '/' ? args[0].substring(1, args[0].length() - 1) : args[0]) + " " + args[1];
                            for (String prefix : prefixes) {
                                for (String cmd : blacklistedLong) {
                                    //check for each prefix
                                    if (parse.equals(prefix + ":" + cmd)) {
                                        //check if in spawn
                                        if (p.getWorld().getName().equals("world")) {
                                            if (parse.equals("n spawn") || parse.equals("nat spawn") || parse.equals("nation spawn")
                                                    || parse.equals("t spawn") || parse.equals("town spawn")
                                                    || parse.equals("towny:n spawn") || parse.equals("towny:nat spawn") || parse.equals("towny:nation spawn")
                                                    || parse.equals("towny:t spawn") || parse.equals("towny:town spawn")) {
                                                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You are stuck in spawn and allowed to teleport to your town or nation!");
                                                return;
                                            }
                                        }

                                        //else
                                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("c") + "You cannot teleport whilst in a raid!");
                                        event.setCancelled(true);
                                        return;
                                    }
                                }
                            }
                        } else if (args.length >= 1) {
                            //parse what we have, remove the starting slash
                            String parse = args[0].charAt(0) == '/' ? args[0].substring(1, args[0].length() - 1) : args[0];
                            for (String prefix : prefixes) {
                                for (String cmd : blacklistedLong) {
                                    //check for each prefix
                                    if (parse.equals(prefix + ":" + cmd)) {
                                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("c") + "You cannot teleport whilst in a raid!");
                                        event.setCancelled(true);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /*
            Prevent players from teleporting during a siege
             */
            for(Siege siege : war.getSieges()) {
                //Only do this during regular phases, start and end ignored in case something breaks
                if (siege.getSiegeTicks() > 0) {
                    //only do for active raiders and any defenders
                    if (siege.getAttackerPlayers().contains(p.getName()) || siege.getDefenders().contains(p.getName())) {
                        //n spawn and t spawn
                        if (args.length >= 2) {
                            //parse what we have, remove the starting sslash
                            String parse = (args[0].charAt(0) == '/' ? args[0].substring(1, args[0].length() - 1) : args[0]) + " " + args[1];
                            for (String prefix : prefixes) {
                                for (String cmd : blacklistedLong) {
                                    //check for each prefix
                                    if (parse.equals(prefix + ":" + cmd)) {
                                        //check if in spawn
                                        if (p.getWorld().getName().equals("world")) {
                                            if (parse.equals("n spawn") || parse.equals("nat spawn") || parse.equals("nation spawn")
                                                    || parse.equals("t spawn") || parse.equals("town spawn")
                                                    || parse.equals("towny:n spawn") || parse.equals("towny:nat spawn") || parse.equals("towny:nation spawn")
                                                    || parse.equals("towny:t spawn") || parse.equals("towny:town spawn")) {
                                                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "You are stuck in spawn and allowed to teleport to your town or nation!");
                                                return;
                                            }
                                        }

                                        //else
                                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("c") + "You cannot teleport whilst in a raid!");
                                        event.setCancelled(true);
                                        return;
                                    }
                                }
                            }
                        } else if (args.length >= 1) {
                            //parse what we have, remove the starting slash
                            String parse = (args[0].charAt(0) == '/' ? args[0].substring(1, args[0].length() - 1) : args[0]) + " " + args[1];
                            for (String prefix : prefixes) {
                                for (String cmd : blacklistedLong) {
                                    //check for each prefix
                                    if (parse.equals(prefix + ":" + cmd)) {
                                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("c") + "You cannot teleport whilst in a raid!");
                                        event.setCancelled(true);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
