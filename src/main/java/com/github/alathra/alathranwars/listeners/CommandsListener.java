package com.github.alathra.alathranwars.listeners;

import com.github.alathra.alathranwars.conflict.battle.siege.Siege;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.utility.Utils;
import com.github.alathra.alathranwars.utility.UtilsChat;
import com.github.milkdrinkers.colorparser.ColorParser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

public class CommandsListener implements Listener {
    final static String[] PREFIXES = new String[]{
        "", "towny", "essentials", "wild", "minecraft"
    };
    final static String[] PREFIXES_TOWNY = new String[]{
        "", "towny"
    };
    final static String[] BLACKLISTED_X_LONG = new String[]{
        "n set spawn", "nat set spawn", "nation set spawn",
        "t set spawn", "town set spawn",
        "t set homeblock", "town set homeblock"
    };
    final static String[] BLACKLISTED_LONG = new String[]{
        "n spawn", "nat spawn", "nation spawn",
        "t spawn", "town spawn",
        "t outpost", "town outpost"
    };
    final static String[] PAYMENT = new String[]{
        "n withdraw", "nat withdraw", "nation withdraw",
        "t withdraw", "town withdraw"
    };
    final static String[] BLACKLISTED_SHORT = new String[]{
        "homes",
        "wild", "rtp", "wilderness", "wildtp",
        "tpa", "tpahere", "tpaccept", "tpacancel",
        "etpa", "etpahere", "etpaccept", "etpacancel",
        "ehomes"

    };

    @EventHandler
    public void onCommandSend(final @NotNull PlayerCommandPreprocessEvent event) {

        @NotNull Player p = event.getPlayer();

        //if player is admin, ignore this behavior
//        if (p.hasPermission("!AlathranWars.admin")) return;

        String @NotNull [] args = event.getMessage().split(" ");

        /*
        Prevent any teleport command events from occurring
        If using PlayerTeleportEvent, it breaks the death logic

        allow n spawn and t spawn for defenders trapped in spawn
         */
        /*for (OldRaid oldRaid : RaidData.getRaids()) {
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
                                    p.sendMessage(UtilsChat.getPrefix() + Helper.color("<red>You cannot modify this property during a oldRaid!"));
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
                                    p.sendMessage(UtilsChat.getPrefix() + Helper.color("<red>You cannot withdraw money whilst in a oldRaid!"));
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
                                            p.sendMessage(UtilsChat.getPrefix() + Helper.color("<yellow>You are stuck in spawn and are allowed to teleport to your town or nation."));
                                            return;
                                        }
                                        p.sendMessage(UtilsChat.getPrefix() + Helper.color("<yellow>You are stuck in spawn and are allowed to teleport to your town or nation."));
                                        p.sendMessage(UtilsChat.getPrefix() + Helper.color("<yellow>Use /t spawn, or /n spawn"));
                                        event.setCancelled(true);
                                        return;
                                    }

                                    //else
                                    p.sendMessage(UtilsChat.getPrefix() + Helper.color("<red>You cannot teleport whilst in a oldRaid!"));
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
                                        p.sendMessage(UtilsChat.getPrefix() + Helper.color("<yellow>You are stuck in spawn and are allowed to teleport to your town or nation."));
                                        p.sendMessage(UtilsChat.getPrefix() + Helper.color("<yellow>Use /t spawn, or /n spawn"));
                                        event.setCancelled(true);
                                        return;
                                    }

                                    p.sendMessage(UtilsChat.getPrefix() + Helper.color("<red>You cannot teleport whilst in a oldRaid!"));
                                    event.setCancelled(true);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
*/
        /*
        Prevent players from teleporting during a siege
         */

        for (Siege siege : WarController.getInstance().getSieges(p)) {
            //payment check
            if (args.length >= 2) {
                //parse what we have, remove the starting sslash
                @NotNull String parse = (args[0].charAt(0) == '/' ? args[0].substring(1) : args[0]) + " " + args[1];
                for (@NotNull String prefix : PREFIXES_TOWNY) {
                    for (String cmd : PAYMENT) {
                        //check for each prefix
                        if (parse.equalsIgnoreCase(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                            p.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "<red>You cannot withdraw money whilst in a siege!").parseLegacy().build());
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }

            if (Utils.isOnSiegeBattlefield(p, siege)) {
                //set spawn and properties
                if (args.length >= 3) {
                    @NotNull String parse = (args[0].charAt(0) == '/' ? args[0].substring(1) : args[0]) + " " + args[1] + " " + args[2];
                    for (@NotNull String prefix : PREFIXES_TOWNY) {
                        //payment check
                        for (String cmd : BLACKLISTED_X_LONG) {
                            if (parse.equalsIgnoreCase(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                p.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "<red>You cannot modify this property during a siege!").parseLegacy().build());
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
                //n spawn and t spawn
                if (args.length >= 2) {
                    //parse what we have, remove the starting sslash
                    @NotNull String parse = (args[0].charAt(0) == '/' ? args[0].substring(1) : args[0]) + " " + args[1];
                    for (@NotNull String prefix : PREFIXES_TOWNY) {
                        for (String cmd : BLACKLISTED_LONG) {
                            //check for each prefix
                            if (parse.equalsIgnoreCase(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                //check if in spawn
                                if (p.getWorld().getName().equalsIgnoreCase("world")) {
                                    if (parse.equalsIgnoreCase("n spawn") || parse.equalsIgnoreCase("nat spawn") || parse.equalsIgnoreCase("nation spawn")
                                        || parse.equalsIgnoreCase("t spawn") || parse.equalsIgnoreCase("town spawn")
                                        || parse.equalsIgnoreCase("towny:n spawn") || parse.equalsIgnoreCase("towny:nat spawn") || parse.equalsIgnoreCase("towny:nation spawn")
                                        || parse.equalsIgnoreCase("towny:t spawn") || parse.equalsIgnoreCase("towny:town spawn")) {
                                        p.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "<yellow>You are stuck in spawn and are allowed to teleport to your town or nation.").parseLegacy().build());
                                        return;
                                    }
                                    p.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "<yellow>You are stuck in spawn and are allowed to teleport to your town or nation.").parseLegacy().build());
                                    p.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "<yellow>Use /t spawn, or /n spawn").parseLegacy().build());
                                    event.setCancelled(true);
                                    return;
                                }

                                //else
                                p.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "<red>You cannot teleport whilst in a oldSiege!").parseLegacy().build());
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
                //random tp commands
                if (args.length >= 1) {
                    //parse what we have, remove the starting slash
                    @NotNull String parse = args[0].charAt(0) == '/' ? args[0].substring(1) : args[0];
                    for (@NotNull String prefix : PREFIXES) {
                        for (String cmd : BLACKLISTED_SHORT) {
                            //check for each prefix
                            if (parse.equalsIgnoreCase(prefix + (prefix.isEmpty() ? "" : ":") + cmd)) {
                                //spawn world check
                                if (p.getWorld().getName().equalsIgnoreCase("world")) {
                                    p.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "<yellow>You are stuck in spawn and are allowed to teleport to your town or nation.").parseLegacy().build());
                                    p.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "<yellow>Use /t spawn, or /n spawn").parseLegacy().build());
                                    event.setCancelled(true);
                                    return;
                                }

                                p.sendMessage(ColorParser.of(UtilsChat.getPrefix() + "<red>You cannot teleport whilst in a oldSiege!").parseLegacy().build());
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
