package com.github.alathra.alathranwars.listeners.war;

import com.github.alathra.alathranwars.hook.NameColorHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        NameColorHandler.getInstance().calculatePlayerColors(p);
    }
}
