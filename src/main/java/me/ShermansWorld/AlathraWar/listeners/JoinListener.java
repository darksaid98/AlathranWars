package me.ShermansWorld.AlathraWar.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;

import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.War;
import me.ShermansWorld.AlathraWar.commands.WarCommands;
import me.ShermansWorld.AlathraWar.data.WarData;
import me.ShermansWorld.AlathraWar.hooks.TABHook;

import org.bukkit.event.Listener;

public class JoinListener implements Listener {
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> checkPlayer(p), 60L); // 20 Tick (1 Second) delay before run() is called
	}

    public static void checkPlayer(Player p) {
        Town town = TownyAPI.getInstance().getResident(p).getTownOrNull();
        if (town == null) return;
        String townName = town.getName();
        boolean inWar = false;
        for (final War war : WarData.getWars()) {
            if (war.getSide1Towns().contains(townName)) {
                TABHook.assignSide1WarSuffix(p, war);
                inWar = true;
            } else if (war.getSide2Towns().contains(townName)) {
                TABHook.assignSide2WarSuffix(p, war);
                inWar = true;
            }
        }
        if (!inWar) {
            TABHook.resetPrefix(p);
        }
    }
}
