package com.github.alathra.AlathranWars.listeners.siege;

import com.github.alathra.AlathranWars.conflict.WarController;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.enums.battle.BattleSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerJoinListener implements Listener {
    @EventHandler
    private void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        final @NotNull Player p = e.getPlayer();
        if (WarController.getInstance().isPlayerInAnySiege(p)) {
            for (@NotNull Siege siege : WarController.getInstance().getPlayerSieges(p)) {
                final @NotNull BattleSide side = siege.getPlayerSideInSiege(p);
                siege.addOnlinePlayer(p, side);
            }
        }
    }
}
