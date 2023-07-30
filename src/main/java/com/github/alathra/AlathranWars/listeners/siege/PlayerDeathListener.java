package com.github.alathra.AlathranWars.listeners.siege;

import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.utility.Utils;
import com.ranull.graves.event.GraveCreateEvent;
import io.github.thatsmusic99.headsplus.api.events.PlayerHeadDropEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
    @EventHandler
    private void onHeadDrop(PlayerHeadDropEvent e) {
        Siege siege = Utils.getClosestSiege(e.getDeadPlayer(), false);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(e.getDeadPlayer(), siege)) return;

        e.setCancelled(true);
    }

    @EventHandler
    private void onGraveCreate(GraveCreateEvent e) {
        if (!e.getEntityType().equals(EntityType.PLAYER)) return;
        if (!(e.getEntity() instanceof Player p)) return;

        Siege siege = Utils.getClosestSiege(p, false);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(p, siege)) return;

        e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent e) {
        Player victim = e.getPlayer();
        Player attacker = e.getPlayer().getKiller();
        if (attacker == null) return;
        
        Siege siege = Utils.getClosestSiege(victim, false);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(victim, siege)) return;

        if (siege.isPlayerInSiege(victim) && siege.isPlayerInSiege(attacker)) {
            siegeKill(e);
        } else {
            oocKill(e);
        }
    }

    @EventHandler
    private void onPlayerSuicide(PlayerDeathEvent e) {
        Player victim = e.getPlayer();
        Player attacker = e.getPlayer().getKiller();
        if (attacker != null) return;

        Siege siege = Utils.getClosestSiege(victim, false);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(victim, siege)) return;

        if (siege.isPlayerInSiege(victim)) {
            siegeKill(e);
        } else {
            oocKill(e);
        }
    }

    /**
     * Damage all gear held by the player (and then send them to spawn?)
     * They don't lose items from death.
     *
     * @param e      event
     */
    private void siegeKill(PlayerDeathEvent e) {
//        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + victim.getName()); // TODO Re enable?
//        Utils.damageAllGear(e.getPlayer());
        e.setKeepInventory(true);
        e.getDrops().clear();
        e.setKeepLevel(true);
    }

    /**
     * Dont damage items held by the player
     * They don't lose items from death.
     *
     * @param e      event
     */
    private void oocKill(PlayerDeathEvent e) {
//        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "spawn " + victim.getName()); // TODO Re enable?
        e.setKeepInventory(true);
        e.getDrops().clear();
        e.setKeepLevel(true);
    }
}
