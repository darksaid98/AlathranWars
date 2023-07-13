package me.ShermansWorld.AlathranWars.listeners;

import me.ShermansWorld.AlathranWars.conflict.War;
import me.ShermansWorld.AlathranWars.holder.WarManager;
import me.ShermansWorld.AlathranWars.hooks.TABHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class JoinListener implements Listener {
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
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player p = event.getPlayer();
    }
}
