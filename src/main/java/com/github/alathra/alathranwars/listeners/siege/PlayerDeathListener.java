package com.github.alathra.alathranwars.listeners.siege;

import com.github.alathra.alathranwars.conflict.battle.siege.Siege;
import com.github.alathra.alathranwars.utility.Utils;
import com.palmergames.bukkit.towny.event.deathprice.NationPaysDeathPriceEvent;
import com.palmergames.bukkit.towny.event.deathprice.PlayerPaysDeathPriceEvent;
import com.palmergames.bukkit.towny.event.deathprice.TownPaysDeathPriceEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPaysDeathPrice(PlayerPaysDeathPriceEvent e) {
        Player p = e.getDeadResident().getPlayer();
        if (p == null) return;

        Siege siege = Utils.getClosestSiege(p, true);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(p, siege)) return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPaysDeathPrice(TownPaysDeathPriceEvent e) {
        Player p = e.getDeadResident().getPlayer();
        if (p == null) return;

        Siege siege = Utils.getClosestSiege(p, true);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(p, siege)) return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPaysDeathPrice(NationPaysDeathPriceEvent e) {
        Player p = e.getDeadResident().getPlayer();
        if (p == null) return;

        Siege siege = Utils.getClosestSiege(p, true);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(p, siege)) return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
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

    @EventHandler(priority = EventPriority.HIGHEST)
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
     * @param e event
     */
    private void siegeKill(PlayerDeathEvent e) {
//        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "spawn " + victim.getName()); // TODO Re enable?
//        Utils.damageAllGear(e.getPlayer());
        e.setKeepInventory(true);
        e.getDrops().clear();
        e.setKeepLevel(true);
        e.setDroppedExp(0);
    }

    /**
     * Dont damage items held by the player
     * They don't lose items from death.
     *
     * @param e event
     */
    private void oocKill(PlayerDeathEvent e) {
//        Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), "spawn " + victim.getName()); // TODO Re enable?
        e.setKeepInventory(true);
        e.getDrops().clear();
        e.setKeepLevel(true);
        e.setDroppedExp(0);
    }
}
