package me.ShermansWorld.AlathraWar;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

// the purpose of this class it to set the display names of all players in a war

public class JoinListener implements Listener {
	@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		for (War war : WarCommands.wars) {
			if (war.getSide1Players().contains(p.getName())) {
				p.setPlayerListName(Helper.color("&c[" + war.getSide1() + "]&r") + p.getName());
			} else if (war.getSide2Players().contains(p.getName())) {
				p.setPlayerListName(Helper.color("&9[" + war.getSide2() + "]&r") + p.getName());
			} else {
			}
		}
    }
   
}
