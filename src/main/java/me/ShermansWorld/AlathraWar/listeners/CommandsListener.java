package me.ShermansWorld.AlathraWar.listeners;

import me.ShermansWorld.AlathraWar.deprecated.OldRaid;
import me.ShermansWorld.AlathraWar.deprecated.OldSiege;
import me.ShermansWorld.AlathraWar.data.RaidData;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import me.ShermansWorld.AlathraWar.data.SiegeData;
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
        for (OldRaid oldRaid : RaidData.getRaids()) {
            //Only do this during regular phases, start and end ignored in case something breaks
            if (oldRaid.getPhase() == RaidPhase.COMBAT || oldRaid.getPhase() == RaidPhase.TRAVEL || oldRaid.getPhase() == RaidPhase.GATHER) {
                //only do for active raiders and any defenders
                if (oldRaid.getActiveRaiders().contains(p.getName()) || oldRaid.getDefenderPlayers().contains(p.getName())) {
                    //set spawn and properties
                    if (args.length >= 3) {
                        String parse = (args[0].charAt(0) == '/' ? args[0].substring(1) : args[0]) + " " + args[1] + " " + args[2];
                        for (String prefix : prefixesTowny) {
                            //payment check
                            for (String cmd : blacklistedXLong) {
                                if (parse.equalsIgnoreCase(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                    p.sendMessage(UtilsChat.getPrefix() + Helper.color("&cYou cannot modify this property during a oldRaid!"));
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
                                if (parse.equalsIgnoreCase(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                    p.sendMessage(UtilsChat.getPrefix() + Helper.color("&cYou cannot withdraw money whilst in a oldRaid!"));
                                    event.setCancelled(true);
                                    return;
                                }
                            }

                            for (String cmd : blacklistedLong) {
                                //check for each prefix
                                if (parse.equalsIgnoreCase(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                    //check if in spawn
                                    if (p.getWorld().getName().equalsIgnoreCase("world")) {
                                        if (parse.equalsIgnoreCase("n spawn") || parse.equalsIgnoreCase("nat spawn") || parse.equalsIgnoreCase("nation spawn")
                                            || parse.equalsIgnoreCase("t spawn") || parse.equalsIgnoreCase("town spawn")
                                            || parse.equalsIgnoreCase("towny:n spawn") || parse.equalsIgnoreCase("towny:nat spawn") || parse.equalsIgnoreCase("towny:nation spawn")
                                            || parse.equalsIgnoreCase("towny:t spawn") || parse.equalsIgnoreCase("towny:town spawn")) {
                                            p.sendMessage(UtilsChat.getPrefix() + Helper.color("&eYou are stuck in spawn and are allowed to teleport to your town or nation."));
                                            return;
                                        }
                                        p.sendMessage(UtilsChat.getPrefix() + Helper.color("&eYou are stuck in spawn and are allowed to teleport to your town or nation."));
                                        p.sendMessage(UtilsChat.getPrefix() + Helper.color("&eUse /t spawn, or /n spawn"));
                                        event.setCancelled(true);
                                        return;
                                    }

                                    //else
                                    p.sendMessage(UtilsChat.getPrefix() + Helper.color("&cYou cannot teleport whilst in a oldRaid!"));
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
                                if (parse.equalsIgnoreCase(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                    //spawn world check
                                    if (p.getWorld().getName().equalsIgnoreCase("world")) {
                                        p.sendMessage(UtilsChat.getPrefix() + Helper.color("&eYou are stuck in spawn and are allowed to teleport to your town or nation."));
                                        p.sendMessage(UtilsChat.getPrefix() + Helper.color("&eUse /t spawn, or /n spawn"));
                                        event.setCancelled(true);
                                        return;
                                    }

                                    p.sendMessage(UtilsChat.getPrefix() + Helper.color("&cYou cannot teleport whilst in a oldRaid!"));
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
        for (OldSiege oldSiege : SiegeData.getSieges()) {
            //Only do this during regular phases, start and end ignored in case something breaks
            if (oldSiege.getSiegeTicks() > 0) {
                //only do for active raiders and any defenders
                if (oldSiege.getAttackerPlayers().contains(p.getName()) || oldSiege.getDefenderPlayers().contains(p.getName())) {
                    //set spawn and properties
                    if (args.length >= 3) {
                        String parse = (args[0].charAt(0) == '/' ? args[0].substring(1) : args[0]) + " " + args[1] + " " + args[2];
                        for (String prefix : prefixesTowny) {
                            //payment check
                            for (String cmd : blacklistedXLong) {
                                if (parse.equalsIgnoreCase(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                    p.sendMessage(UtilsChat.getPrefix() + Helper.color("&cYou cannot modify this property during a oldSiege!"));
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
                                if (parse.equalsIgnoreCase(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                    p.sendMessage(UtilsChat.getPrefix() + Helper.color("&cYou cannot withdraw money whilst in a oldSiege!"));
                                    event.setCancelled(true);
                                    return;
                                }
                            }

                            for (String cmd : blacklistedLong) {
                                //check for each prefix
                                if (parse.equalsIgnoreCase(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                    //check if in spawn
                                    if (p.getWorld().getName().equalsIgnoreCase("world")) {
                                        if (parse.equalsIgnoreCase("n spawn") || parse.equalsIgnoreCase("nat spawn") || parse.equalsIgnoreCase("nation spawn")
                                            || parse.equalsIgnoreCase("t spawn") || parse.equalsIgnoreCase("town spawn")
                                            || parse.equalsIgnoreCase("towny:n spawn") || parse.equalsIgnoreCase("towny:nat spawn") || parse.equalsIgnoreCase("towny:nation spawn")
                                            || parse.equalsIgnoreCase("towny:t spawn") || parse.equalsIgnoreCase("towny:town spawn")) {
                                            p.sendMessage(UtilsChat.getPrefix() + Helper.color("&eYou are stuck in spawn and are allowed to teleport to your town or nation."));
                                            return;
                                        }
                                        p.sendMessage(UtilsChat.getPrefix() + Helper.color("&eYou are stuck in spawn and are allowed to teleport to your town or nation."));
                                        p.sendMessage(UtilsChat.getPrefix() + Helper.color("&eUse /t spawn, or /n spawn"));
                                        event.setCancelled(true);
                                        return;
                                    }

                                    //else
                                    p.sendMessage(UtilsChat.getPrefix() + Helper.color("&cYou cannot teleport whilst in a oldSiege!"));
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
                                if (parse.equalsIgnoreCase(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                    //spawn world check
                                    if (p.getWorld().getName().equalsIgnoreCase("world")) {
                                        p.sendMessage(UtilsChat.getPrefix() + Helper.color("&eYou are stuck in spawn and are allowed to teleport to your town or nation."));
                                        p.sendMessage(UtilsChat.getPrefix() + Helper.color("&eUse /t spawn, or /n spawn"));
                                        event.setCancelled(true);
                                        return;
                                    }

                                    p.sendMessage(UtilsChat.getPrefix() + Helper.color("&cYou cannot teleport whilst in a oldSiege!"));
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
