package me.ShermansWorld.AlathraWar.roles;

import java.util.Map;

import org.bukkit.entity.Player;

import me.ShermansWorld.AlathraWar.Main;

public class Merc {
	public static boolean hasMercRole(Player p) {
		Map userData = (Map) Main.rolesData.getData(p.getUniqueId());
		if ((Boolean) userData.get("MercPermission")) {
			return true;
		} else {
			return false;
		}
	}
}
