// 
// Decompiled by Procyon v0.5.36
// 

package me.ShermansWorld.AlathraWar;

import org.bukkit.event.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;

import org.bukkit.event.Listener;

public class JoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player p = event.getPlayer();
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
            	for (final War war : WarCommands.wars) {
                    if (war.getSide1Players().contains(p.getName())) {
                    	TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(p.getUniqueId());
                    	TabAPI.getInstance().getTablistFormatManager().setSuffix(tabPlayer, Helper.color(new StringBuilder("&c[").append(war.getSide1()).append("]&r").toString()));
                    	//p.setPlayerListName(String.valueOf(Helper.color(new StringBuilder("&c[").append(war.getSide1()).append("]&r").toString())) + p.getName());
                    } else if (war.getSide2Players().contains(p.getName())) {
                    	TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(p.getUniqueId());
                    	TabAPI.getInstance().getTablistFormatManager().setSuffix(tabPlayer, Helper.color(new StringBuilder("&9[").append(war.getSide2()).append("]&r").toString()));
                    }
                }
            }
        }, 60L); //20 Tick (1 Second) delay before run() is called
    }
}
