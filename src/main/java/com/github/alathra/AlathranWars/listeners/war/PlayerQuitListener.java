package com.github.alathra.AlathranWars.listeners.war;

import com.github.alathra.AlathranWars.conflict.Side;
import com.github.alathra.AlathranWars.conflict.War;
import com.github.alathra.AlathranWars.holder.WarManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        if (WarManager.getInstance().isPlayerInAnyWars(p)) {
            for (War war : WarManager.getInstance().getWars()) {
                if (war.isPlayerInWar(p)) {
                    Side side = war.getPlayerSide(p);
                    if (side != null) {
                        side.removeOnlinePlayer(p);
                    }
                }
            }
        }
    }
}
