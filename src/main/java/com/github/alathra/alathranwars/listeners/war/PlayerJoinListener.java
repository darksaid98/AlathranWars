package com.github.alathra.alathranwars.listeners.war;

import com.github.alathra.alathranwars.conflict.war.War;
import com.github.alathra.alathranwars.conflict.war.WarController;
import com.github.alathra.alathranwars.conflict.war.side.Side;
import com.github.alathra.alathranwars.hooks.NameColorHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerJoinListener implements Listener {
    @EventHandler
    private void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        final @NotNull Player p = e.getPlayer();
        if (WarController.getInstance().isPlayerInAnyWars(p)) {
            for (@NotNull War war : WarController.getInstance().getWars()) {
                if (war.isPlayerInWar(p)) {
                    @Nullable Side side = war.getPlayerSide(p);
                    if (side != null) {
                        side.addOnlinePlayer(p);
                    }
                }
            }
        }
        NameColorHandler.getInstance().calculatePlayerColors(p);
    }
}
