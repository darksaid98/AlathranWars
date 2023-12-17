package com.github.alathra.AlathranWars.listeners.siege;

import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.utility.Utils;
import com.palmergames.bukkit.towny.event.actions.TownyItemuseEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemUseListener implements Listener {
    /**
     * Prevent using ender pearls in a combat zone.
     *
     * @param e the e
     */
    @EventHandler
    public void onEnderPearl(TownyItemuseEvent e) {
        Siege siege = Utils.getClosestSiege(e.getPlayer(), true);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(e.getPlayer(), siege)) return;

        if (!e.getMaterial().equals(Material.ENDER_PEARL)) return;

        e.setCancelMessage("You cannot throw ender pearls during a siege!");
        e.setCancelled(true);
    }
}
