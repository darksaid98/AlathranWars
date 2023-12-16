package com.github.alathra.AlathranWars.listeners.siege;

import com.github.alathra.AlathranWars.conflict.WarManager;
import com.github.alathra.AlathranWars.conflict.battle.siege.Siege;
import com.github.alathra.AlathranWars.enums.battle.BattleSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerQuitListener implements Listener {
    @EventHandler
    private void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        final @NotNull Player p = e.getPlayer();
        if (WarManager.getInstance().isPlayerInAnySiege(p)) {
            for (@NotNull Siege siege : WarManager.getInstance().getPlayerSieges(p)) {
                final @NotNull BattleSide side = siege.getPlayerSideInSiege(p);
                siege.removeOnlinePlayer(p, side);
            }
        }
    }
}
