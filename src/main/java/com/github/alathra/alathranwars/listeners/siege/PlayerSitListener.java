package com.github.alathra.alathranwars.listeners.siege;

import com.github.alathra.alathranwars.conflict.battle.siege.Siege;
import com.github.alathra.alathranwars.utility.Utils;
import dev.geco.gsit.api.event.PreEntitySitEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PlayerSitListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSit(PreEntitySitEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

        Siege siege = Utils.getClosestSiege(p, false);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(p, siege)) return;

        e.setCancelled(true);
    }
}
