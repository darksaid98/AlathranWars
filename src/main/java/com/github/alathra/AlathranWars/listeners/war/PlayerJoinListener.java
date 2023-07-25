package com.github.alathra.AlathranWars.listeners.war;

import com.github.alathra.AlathranWars.conflict.Side;
import com.github.alathra.AlathranWars.conflict.War;
import com.github.alathra.AlathranWars.holder.WarManager;
import com.github.alathra.AlathranWars.hooks.TABHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerJoinListener implements Listener {
    public static void checkPlayer(@NotNull Player p) {
        final @NotNull UUID uuid = p.getUniqueId();

        if (WarManager.getInstance().isPlayerInAnyWars(uuid)) {
            for (final @NotNull War war : WarManager.getInstance().getWars()) {
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
    private void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        final @NotNull Player p = e.getPlayer();
        if (WarManager.getInstance().isPlayerInAnyWars(p)) {
            for (@NotNull War war : WarManager.getInstance().getWars()) {
                if (war.isPlayerInWar(p)) {
                    @Nullable Side side = war.getPlayerSide(p);
                    if (side != null) {
                        side.addOnlinePlayer(p);
                    }
                }
            }
        }
    }
}
