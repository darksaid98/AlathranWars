package com.github.alathra.alathranwars.listeners.war;

import com.github.alathra.alathranwars.hook.NameColorHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        NameColorHandler.getInstance().calculatePlayerColors(p);
    }
}
