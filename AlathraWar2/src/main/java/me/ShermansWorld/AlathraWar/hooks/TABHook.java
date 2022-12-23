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
		tabAPI.getTablistFormatManager().setSuffix(tabPlayer,
				Helper.color("&c[") + war.getSide1() + "]&r");
	}

	public static void assignSide2WarSuffix(Player p, War war) {
		TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());
		tabAPI.getTablistFormatManager().setSuffix(tabPlayer,
				Helper.color("&9[") + war.getSide2() + "]&r");
	}
	
	public static void assignSide1WarSuffixMerc(Player p, War war) {
		TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());
		tabAPI.getTablistFormatManager().setSuffix(tabPlayer,
				Helper.color("&a[M]&c[") + war.getSide1() + "]&r");
	}
	
	public static void assignSide2WarSuffixMerc(Player p, War war) {
		TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());
		tabAPI.getTablistFormatManager().setSuffix(tabPlayer,
				Helper.color("&a[M]&9[") + war.getSide2() + "]&r");
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
	
	public static void resetPrefix(Player p) {
		TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());
		tabAPI.getTablistFormatManager().resetPrefix(tabPlayer);
	}

	public static void resetPrefix(String playername) {
		@SuppressWarnings("deprecation")
		OfflinePlayer op = Bukkit.getOfflinePlayer(playername);
		try {
			if (op.isOnline()) {
				TabPlayer tabPlayer = tabAPI.getPlayer(playername);
				tabAPI.getTablistFormatManager().resetPrefix(tabPlayer);
			}
		} catch (NullPointerException e) {
			return;
		}
	}

	public static void removeColorPrefix(Player p, String prefix) {
		TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());
		if(prefix.length() > 2) {
			prefix = prefix.substring(0, prefix.length() - 2);
		}
		else{
			Bukkit.getLogger().info("There is an error when removing a color prefix, prefix not long enough:");
			Bukkit.getLogger().info("Player: " + p.getDisplayName());
			Bukkit.getLogger().info("Prefix: " + prefix);
			Bukkit.getLogger().info("------");
		}
		tabAPI.getTablistFormatManager().setPrefix(tabPlayer, Helper.color(prefix));
	}

}