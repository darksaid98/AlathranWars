package com.github.alathra.AlathranWars.listeners.siege;

import com.github.alathra.AlathranWars.utility.Utils;
import com.github.alathra.AlathranWars.utility.UtilsChat;
import com.github.milkdrinkers.colorparser.ColorParser;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.holder.WarManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.Nullable;

public class PlayerDeathListener implements Listener {
    private final static int BATTLE_RADIUS = 300;
    private final static int KILL_POINTS_OFF = 20; // Point for an offensive kill
    private final static int KILL_POINTS_DEF = 20; // Point for a defensive kill

    /*@EventHandler
    private void onPlayerDeath(PlayerDeathEvent e) {
        final Player victim = e.getPlayer();
        final Player attacker = e.getPlayer().getKiller();

        if (attacker == null)
            return;
        
        final @Nullable Town town = WorldCoord.parseWorldCoord(victim).getTownOrNull();

        for (Siege siege : WarManager.getInstance().getSieges()) {
            boolean isPlayerInCombatZone = false;
            
            final TownBlock homeBlock = siege.getTown().getHomeBlockOrNull();
            if (homeBlock == null)
                continue;
            
            final int homeBlockXCoord = homeBlock.getCoord().getX() * 16;
            final int homeBlockZCoord = homeBlock.getCoord().getZ() * 16;
            
            if (victim.getLocation().distance(new Location(victim.getWorld(), homeBlockXCoord, victim.getLocation().getBlockY(), homeBlockZCoord)) <= BATTLE_RADIUS) {
//            if (Math.abs(killed.getLocation().getBlockX() - homeBlockXCoord) <= BATTLE_RADIUS && Math.abs(killed.getLocation().getBlockZ() - homeBlockZCoord) <= BATTLE_RADIUS) {
                isPlayerInCombatZone = true;
            }
            
            if (siege.getAttackerPlayers().contains(attacker) && (siege.getTown().equals(town) || isPlayerInCombatZone)) {
                if (siege.getDefenderPlayers().contains(victim)) {
                    siege.addPointsToAttackers(KILL_POINTS_OFF);

                    for (final Player p : siege.getAttackerPlayers()) {
                        p.sendMessage(new ColorParser(UtilsChat.getPrefix() + "Defender killed! + "+KILL_POINTS_OFF+" Attacker Points.").build());
                    }
                    for (final Player p : siege.getDefenderPlayers()) {
                        p.sendMessage(new ColorParser(UtilsChat.getPrefix() + "Defender killed! + "+KILL_POINTS_OFF+" Attacker Points.").build());
                    }
                }

                this.siegeKill(victim, e);

                return;
            }
            
            if (siege.getDefenderPlayers().contains(attacker) && (siege.getTown().equals(town) || isPlayerInCombatZone)) {
                if (siege.getAttackerPlayers().contains(victim)) {
                    siege.addPointsToDefenders(KILL_POINTS_DEF);

                    for (final Player p : siege.getAttackerPlayers()) {
                        p.sendMessage(new ColorParser(UtilsChat.getPrefix() + "Attacker killed! + "+KILL_POINTS_DEF+" Defender Points.").build());
                    }
                    for (final Player p : siege.getDefenderPlayers()) {
                        p.sendMessage(new ColorParser(UtilsChat.getPrefix() + "Attacker killed! + "+KILL_POINTS_DEF+" Defender Points.").build());
                    }
                }

                this.siegeKill(victim, e);

                return;
            }
            
            if ((siege.getTown().equals(town) || isPlayerInCombatZone)) {
                this.oocKill(victim, e);
            }
        }
    }*/

    /**
     * Damage all gear held by the player (and then send them to spawn?)
     * They don't lose items from death.
     *
     * @param victim killed player
     * @param e  event
     */
    private void siegeKill(Player victim, PlayerDeathEvent e) {
        //Helper
        Utils.damageAllGear(victim);

        //Siege specific
//        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + victim.getName()); // TODO Re enable?
        e.setKeepInventory(true);
        e.getDrops().clear();
        e.setKeepLevel(true);
    }

    /**
     * Dont damage items held by the player
     * They don't lose items from death.
     *
     * @param victim killed player
     * @param e  event
     */
    private void oocKill(Player victim, PlayerDeathEvent e) {
        //Siege specific
//        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "spawn " + victim.getName()); // TODO Re enable?
        e.setKeepInventory(true);
        e.getDrops().clear();
        e.setKeepLevel(true);
    }
}
