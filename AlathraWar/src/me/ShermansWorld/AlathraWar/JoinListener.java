// 
// Decompiled by Procyon v0.5.36
// 

package me.ShermansWorld.AlathraWar;

import org.bukkit.event.EventHandler;
import java.util.Iterator;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.Listener;

public class JoinListener implements Listener
{
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player p = event.getPlayer();
        for (final War war : WarCommands.wars) {
            if (war.getSide1Players().contains(p.getName())) {
                p.setPlayerListName(String.valueOf(Helper.color(new StringBuilder("&c[").append(war.getSide1()).append("]&r").toString())) + p.getName());
            }
            else {
                if (!war.getSide2Players().contains(p.getName())) {
                    continue;
                }
                p.setPlayerListName(String.valueOf(Helper.color(new StringBuilder("&9[").append(war.getSide2()).append("]&r").toString())) + p.getName());
            }
        }
    }
}
