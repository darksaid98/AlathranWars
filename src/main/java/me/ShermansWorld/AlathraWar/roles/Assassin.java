package me.ShermansWorld.AlathraWar.roles;

import java.util.Map;

import org.bukkit.entity.Player;

import me.ShermansWorld.AlathraWar.Main;

public class Assassin {
	public static boolean hasAssassinRole(Player p) {
		Map userData = (Map) Main.rolesData.getData(p.getUniqueId());
		if ((Boolean) userData.get("AssassinPermission")) {
			return true;
		} else {
			return false;
		}
	}
}
