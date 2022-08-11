package me.ShermansWorld.AlathraWar.hooks;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.War;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;

public class TABHook {
	
	public static boolean init = false;
	public static TabAPI tabAPI;

	public static void init() {
		tabAPI = TabAPI.getInstance();
		init = true;
	}
	
	public static void assignSide1WarSuffix(Player p, War war) {
		TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());
		tabAPI.getTablistFormatManager().setSuffix(tabPlayer, Helper.color(new StringBuilder("&c[").append(war.getSide1()).append("]&r").toString()));
	}
	
	public static void assignSide2WarSuffix(Player p, War war) {
		TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());
		tabAPI.getTablistFormatManager().setSuffix(tabPlayer, Helper.color(new StringBuilder("&9[").append(war.getSide2()).append("]&r").toString()));
	}
	
	public static void resetSuffix(Player p) {
		TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());
		tabAPI.getTablistFormatManager().resetSuffix(tabPlayer);
	}
	
	public static void resetSuffix(String playername) {
		@SuppressWarnings("deprecation")
		OfflinePlayer op = Bukkit.getOfflinePlayer(playername);
		try {
			if (op.isOnline()) {
				TabPlayer tabPlayer = tabAPI.getPlayer(playername);
				tabAPI.getTablistFormatManager().resetSuffix(tabPlayer);
			}
		} catch (NullPointerException e) {
			return;
		}
	}
	
	public static void removeColorPrefix(Player p, String prefix) {
		TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());
		prefix = prefix.substring(0, prefix.length()-2);
		tabAPI.getTablistFormatManager().setPrefix(tabPlayer, Helper.color(prefix));
	}
	
}
