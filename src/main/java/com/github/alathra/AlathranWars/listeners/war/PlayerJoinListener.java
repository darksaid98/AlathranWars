package com.github.alathra.AlathranWars.listeners.war;

import com.github.alathra.AlathranWars.conflict.Side;
import com.github.alathra.AlathranWars.conflict.War;
import com.github.alathra.AlathranWars.holder.WarManager;
import com.github.alathra.AlathranWars.hooks.TABHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerJoinListener implements Listener {
    public static void checkPlayer(Player p) {
        final UUID uuid = p.getUniqueId();

        if (WarManager.getInstance().isPlayerInAnyWars(uuid)) {
            for (final War war : WarManager.getInstance().getWars()) {
                if (war.getSide1().isPlayerOnSide(uuid) && !war.getSide1().isPlayerSurrendered(uuid)) {
                    TABHook.assignSide1WarSuffix(p, war);
                } else if (war.getSide2().isPlayerOnSide(uuid) && !war.getSide2().isPlayerSurrendered(uuid)) {
                    TABHook.assignSide2WarSuffix(p, war);
                } else if (war.getSide1().isPlayerSurrendered(uuid) || war.getSide2().isPlayerSurrendered(uuid)) {
                    TABHook.resetPrefix(p);
                }
            }
        } else {
            TABHook.resetPrefix(p);
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (WarManager.getInstance().isPlayerInAnyWars(p)) {
            for (War war : WarManager.getInstance().getWars()) {
                if (war.isPlayerInWar(p)) {
                    Side side = war.getPlayerSide(p);
                    if (side != null) {
                        side.addOnlinePlayer(p);
                    }
                }
            }
        }
    }
}
