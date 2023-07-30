package com.github.alathra.AlathranWars.listeners.siege;

import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.utility.Utils;
import dev.geco.gsit.api.event.PreEntitySitEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerSitListener implements Listener {
    @EventHandler
    public void onSit2(PreEntitySitEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;

        Siege siege = Utils.getClosestSiege(p, false);
        if (siege == null) return;

        if (!Utils.isOnSiegeBattlefield(p, siege)) return;

        e.setCancelled(true);
    }
}
