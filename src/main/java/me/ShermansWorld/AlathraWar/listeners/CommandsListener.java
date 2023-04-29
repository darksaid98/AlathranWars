package me.ShermansWorld.AlathraWar.listeners;

import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Raid;
import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.War;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import me.ShermansWorld.AlathraWar.data.WarData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandsListener implements Listener {

    final static String[] prefixes = new String[]{
            "", "towny", "essentials", "wild", "minecraft"
    };
    final static String[] prefixesTowny = new String[]{
            "", "towny"
    };
    final static String[] blacklistedXLong = new String[]{
            "n set spawn", "nat set spawn", "nation set spawn",
            "t set spawn", "town set spawn",
            "t set homeblock", "town set homeblock",
            "t set name", "town set name",
            "n set name", "nat set name", "nation set name"
    };
    final static String[] blacklistedLong = new String[]{
            "n spawn", "nat spawn", "nation spawn",
            "t spawn", "town spawn",
            "t outpost", "town outpost"
    };
    final static String[] payment = new String[]{
            "n withdraw", "nat withdraw", "nation withdraw",
            "t withdraw", "town withdraw"
    };
    final static String[] blacklistedShort = new String[]{
            "home", "homes", "warp", "warps",
            "wild", "rtp", "spawn", "wilderness", "wildtp",
            "tpa", "tpahere", "tpaccept", "tpacancel",
            "etpa", "etpahere", "etpaccept", "etpacancel",
            "ehome", "ehomes", "ewarp", "ewarps"

    };


    @EventHandler
    public void onCommandSend(final PlayerCommandPreprocessEvent event) {

        Player p = event.getPlayer();

        //if player is admin, ignore this behavior
//        if (p.hasPermission("!AlathraWar.admin")) return;

        String[] args = event.getMessage().split(" ");

        /*
        Prevent any teleport command events from occurring
        If using PlayerTeleportEvent, it breaks the death logic

        allow n spawn and t spawn for defenders trapped in spawn
         */
        for (War war : WarData.getWars()) {
            for (Raid raid : war.getRaids()) {
                //Only do this during regular phases, start and end ignored in case something breaks
                if (raid.getPhase() == RaidPhase.COMBAT || raid.getPhase() == RaidPhase.TRAVEL || raid.getPhase() == RaidPhase.GATHER) {
                    //only do for active raiders and any defenders
                    if (raid.getActiveRaiders().contains(p.getName()) || raid.getDefenderPlayers().contains(p.getName())) {
                        //set spawn and properties
                        if (args.length >= 3) {
                            String parse = (args[0].charAt(0) == '/' ? args[0].substring(1) : args[0]) + " " + args[1] + " " + args[2];
                            for (String prefix : prefixesTowny) {
                                //payment check
                                for (String cmd : blacklistedXLong) {
                                    if (parse.equals(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                        p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&cYou cannot modify this property during a raid!"));
                                        event.setCancelled(true);
                                        return;
                                    }
                                }
                            }
                        }
                        //n spawn and t spawn
                        if (args.length >= 2) {
                            //parse what we have, remove the starting sslash
                            String parse = (args[0].charAt(0) == '/' ? args[0].substring(1) : args[0]) + " " + args[1];
                            for (String prefix : prefixesTowny) {
                                //payment check
                                for (String cmd : payment) {
                                    //check for each prefix
                                    if (parse.equals(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                        p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&cYou cannot withdraw money whilst in a raid!"));
                                        event.setCancelled(true);
                                        return;
                                    }
                                }

                                for (String cmd : blacklistedLong) {
                                    //check for each prefix
                                    if (parse.equals(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                        //check if in spawn
                                        if (p.getWorld().getName().equals("world")) {
                                            if (parse.equals("n spawn") || parse.equals("nat spawn") || parse.equals("nation spawn")
                                                    || parse.equals("t spawn") || parse.equals("town spawn")
                                                    || parse.equals("towny:n spawn") || parse.equals("towny:nat spawn") || parse.equals("towny:nation spawn")
                                                    || parse.equals("towny:t spawn") || parse.equals("towny:town spawn")) {
                                                p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&eYou are stuck in spawn and are allowed to teleport to your town or nation."));
                                                return;
                                            }
                                            p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&eYou are stuck in spawn and are allowed to teleport to your town or nation."));
                                            p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&eUse /t spawn, or /n spawn"));
                                            event.setCancelled(true);
                                            return;
                                        }

                                        //else
                                        p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&cYou cannot teleport whilst in a raid!"));
                                        event.setCancelled(true);
                                        return;
                                    }
                                }
                            }
                        }
                        //random tp commands
                        if (args.length >= 1) {
                            //parse what we have, remove the starting slash
                            String parse = args[0].charAt(0) == '/' ? args[0].substring(1) : args[0];
                            for (String prefix : prefixes) {
                                for (String cmd : blacklistedShort) {
                                    //check for each prefix
                                    if (parse.equals(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                        //spawn world check
                                        if (p.getWorld().getName().equals("world")) {
                                            p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&eYou are stuck in spawn and are allowed to teleport to your town or nation."));
                                            p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&eUse /t spawn, or /n spawn"));
                                            event.setCancelled(true);
                                            return;
                                        }

                                        p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&cYou cannot teleport whilst in a raid!"));
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
            for (Siege siege : war.getSieges()) {
                //Only do this during regular phases, start and end ignored in case something breaks
                if (siege.getSiegeTicks() > 0) {
                    //only do for active raiders and any defenders
                    if (siege.getAttackerPlayers().contains(p.getName()) || siege.getDefenderPlayers().contains(p.getName())) {
                        //set spawn and properties
                        if (args.length >= 3) {
                            String parse = (args[0].charAt(0) == '/' ? args[0].substring(1) : args[0]) + " " + args[1] + " " + args[2];
                            for (String prefix : prefixesTowny) {
                                //payment check
                                for (String cmd : blacklistedXLong) {
                                    if (parse.equals(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                        p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&cYou cannot modify this property during a siege!"));
                                        event.setCancelled(true);
                                        return;
                                    }
                                }
                            }
                        }
                        //n spawn and t spawn
                        if (args.length >= 2) {
                            //parse what we have, remove the starting sslash
                            String parse = (args[0].charAt(0) == '/' ? args[0].substring(1) : args[0]) + " " + args[1];
                            for (String prefix : prefixesTowny) {
                                //payment check
                                for (String cmd : payment) {
                                    //check for each prefix
                                    if (parse.equals(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                        p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&cYou cannot withdraw money whilst in a siege!"));
                                        event.setCancelled(true);
                                        return;
                                    }
                                }

                                for (String cmd : blacklistedLong) {
                                    //check for each prefix
                                    if (parse.equals(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                        //check if in spawn
                                        if (p.getWorld().getName().equals("world")) {
                                            if (parse.equals("n spawn") || parse.equals("nat spawn") || parse.equals("nation spawn")
                                                    || parse.equals("t spawn") || parse.equals("town spawn")
                                                    || parse.equals("towny:n spawn") || parse.equals("towny:nat spawn") || parse.equals("towny:nation spawn")
                                                    || parse.equals("towny:t spawn") || parse.equals("towny:town spawn")) {
                                                p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&eYou are stuck in spawn and are allowed to teleport to your town or nation."));
                                                return;
                                            }
                                            p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&eYou are stuck in spawn and are allowed to teleport to your town or nation."));
                                            p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&eUse /t spawn, or /n spawn"));
                                            event.setCancelled(true);
                                            return;
                                        }

                                        //else
                                        p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&cYou cannot teleport whilst in a siege!"));
                                        event.setCancelled(true);
                                        return;
                                    }
                                }
                            }
                        }
                        //random tp commands
                        if (args.length >= 1) {
                            //parse what we have, remove the starting slash
                            String parse = args[0].charAt(0) == '/' ? args[0].substring(1) : args[0];
                            for (String prefix : prefixes) {
                                for (String cmd : blacklistedShort) {
                                    //check for each prefix
                                    if (parse.equals(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                        //spawn world check
                                        if (p.getWorld().getName().equals("world")) {
                                            p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&eYou are stuck in spawn and are allowed to teleport to your town or nation."));
                                            p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&eUse /t spawn, or /n spawn"));
                                            event.setCancelled(true);
                                            return;
                                        }

                                        p.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&cYou cannot teleport whilst in a siege!"));
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
