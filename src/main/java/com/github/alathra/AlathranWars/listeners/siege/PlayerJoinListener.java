package com.github.alathra.AlathranWars.listeners.siege;

import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.enums.BattleSide;
import com.github.alathra.AlathranWars.holder.WarManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (WarManager.getInstance().isPlayerInAnySiege(p)) {
            for (Siege siege : WarManager.getInstance().getPlayerSieges(p)) {
                final BattleSide side = siege.getPlayerSideInSiege(p);
                siege.addOnlinePlayer(p, side);
            }
        }
    }
}
