package me.ShermansWorld.AlathraWar.listeners;

import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Raid;
import me.ShermansWorld.AlathraWar.commands.RaidCommands;
import me.ShermansWorld.AlathraWar.data.RaidPhase;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandsListener implements Listener
{
    @EventHandler(priority = EventPriority.HIGH)
    public void onCommandSend(final PlayerCommandPreprocessEvent event) {
        Player p = event.getPlayer();
        String[] args = event.getMessage().split(" ");

        /*
        Prevent any teleport command events from occuring
        If using PlayerTeleportEvent, it breaks the death logic
         */
        for(Raid raid : RaidCommands.raids) {
            //Only do this during regular phases, start and end ignored in case something breaks
            if(raid.getPhase() == RaidPhase.COMBAT || raid.getPhase() == RaidPhase.TRAVEL || raid.getPhase() == RaidPhase.GATHER) {
                //only do for active raiders and any defenders
                if(raid.getActiveRaiders().contains(p.getName()) || raid.getDefenders().contains(p.getName())) {
                    if (args.length >= 2) {
                        if (args[0].equalsIgnoreCase("/n") || args[0].equalsIgnoreCase("/nat") || args[0].equalsIgnoreCase("/nation")) {
                            if (args[1].equalsIgnoreCase("spawn")) {
                                p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("c") + "You cannot teleport whilst in a raid!");
                                event.setCancelled(true);
                                return;
                            }
                        }
                        if (args[0].equalsIgnoreCase("/t") || args[0].equalsIgnoreCase("/town")) {
                            if (args[1].equalsIgnoreCase("spawn")) {
                                p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("c") + "You cannot teleport whilst in a raid!");
                                event.setCancelled(true);
                                return;
                            }
                        }
                    } else if (args.length >= 1) {
                        if (args[0].equalsIgnoreCase("/home")) {
                            p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("c") + "You cannot teleport whilst in a raid!");
                            event.setCancelled(true);
                            return;
                        }
                        if (args[0].equalsIgnoreCase("/spawn")) {
                            p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("c") + "You cannot teleport whilst in a raid!");
                            event.setCancelled(true);
                            return;
                        }
                        if (args[0].equalsIgnoreCase("/tpa")) {
                            p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("c") + "You cannot teleport whilst in a raid!");
                            event.setCancelled(true);
                            return;
                        }
                        if (args[0].equalsIgnoreCase("/tpahere")) {
                            p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("c") + "You cannot teleport whilst in a raid!");
                            event.setCancelled(true);
                            return;
                        }
                        if (args[0].equalsIgnoreCase("/wild")) {
                            p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("c") + "You cannot teleport whilst in a raid!");
                            event.setCancelled(true);
                            return;
                        }
                        if (args[0].equalsIgnoreCase("/rtp")) {
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
