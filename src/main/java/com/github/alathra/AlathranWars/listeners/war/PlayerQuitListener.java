package com.github.alathra.AlathranWars.listeners.war;

import com.github.alathra.AlathranWars.conflict.Side;
import com.github.alathra.AlathranWars.conflict.War;
import com.github.alathra.AlathranWars.conflict.WarManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerQuitListener implements Listener {
    @EventHandler
    private void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        final @NotNull Player p = e.getPlayer();
        if (WarManager.getInstance().isPlayerInAnyWars(p)) {
            for (@NotNull War war : WarManager.getInstance().getWars()) {
                if (war.isPlayerInWar(p)) {
                    @Nullable Side side = war.getPlayerSide(p);
                    if (side != null) {
                        side.removeOnlinePlayer(p);
                    }
                }
            }
        }
    }
}
