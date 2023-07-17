package com.github.alathra.AlathranWars.listeners.siege;

import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.enums.BattleSide;
import com.github.alathra.AlathranWars.holder.WarManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        if (WarManager.getInstance().isPlayerInAnySiege(p)) {
            for (Siege siege : WarManager.getInstance().getPlayerSieges(p)) {
                final BattleSide side = siege.getPlayerSideInSiege(p);
                siege.removeOnlinePlayer(p, side);
            }
        }
    }
}
