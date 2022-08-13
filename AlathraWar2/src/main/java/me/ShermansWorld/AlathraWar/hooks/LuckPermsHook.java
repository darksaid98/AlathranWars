package me.ShermansWorld.AlathraWar.hooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import me.ShermansWorld.AlathraWar.Main;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;

public class LuckPermsHook {
	public static boolean init = false;
	public static LuckPerms lp;
	private static UserManager um;

	public static HashMap<String, String> prefixMap = new HashMap<String, String>();

	public static void init() {
		RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		if (provider != null) {
			lp = provider.getProvider();
			um = lp.getUserManager();
		}
		init = true;
	}

	public static String getPrefix(String playername) {
		User user = um.getUser(playername);
		String prefix = user.getCachedData().getMetaData().getPrefix();
		if (prefix == null) {
			return "";
		} else {
			return prefix;
		}
	}

	public static void assignSide1WarColor(String playername, boolean isWarMerc) {
		String prefix = getPrefix(playername);
		boolean isGroupPrefix = false;
		if (!prefix.equals("")) {
			User user = um.getUser(playername);
			for (Group group : user.getInheritedGroups(user.getQueryOptions())) {
				String groupPrefix = group.getCachedData().getMetaData().getPrefix();
				if (groupPrefix.equals(prefix)) {
					isGroupPrefix = true;
				}
			}
			if (!isGroupPrefix) {
				prefixMap.put(playername, prefix);
				Main.prefixData.getConfig().set("Prefixes." + playername, prefix);
				Main.prefixData.saveConfig();
			}
		}
        if (isWarMerc) {
        	prefix = prefix + "&a[M]&r&c";
        } else {
        	prefix = prefix + "&c";
        }
		removePrefixNodes(playername);
		addPrefix(playername, prefix);
	}

	public static void assignSide2WarColor(String playername, boolean isWarMerc) {
		String prefix = getPrefix(playername);
		boolean isGroupPrefix = false;
		if (!prefix.equals("")) {
			User user = um.getUser(playername);
			for (Group group : user.getInheritedGroups(user.getQueryOptions())) {
				String groupPrefix = group.getCachedData().getMetaData().getPrefix();
				if (groupPrefix == null) {
					groupPrefix = "";
				}
				if (groupPrefix.equals(prefix)) {
					isGroupPrefix = true;
				}
			}
			if (!isGroupPrefix) {
				prefixMap.put(playername, prefix);
				Main.prefixData.getConfig().set("Prefixes." + playername, prefix);
				Main.prefixData.saveConfig();
			}
		}
		
        if (isWarMerc) {
        	prefix = prefix + "&a[M]&r&9";
        } else {
        	prefix = prefix + "&9";
        }
		removePrefixNodes(playername);
		addPrefix(playername, prefix);
	}

	public static void resetPrefix(String playername) {
		@SuppressWarnings("deprecation")
		OfflinePlayer op = Bukkit.getOfflinePlayer(playername);
		try {
			if (op.isOnline()) {
				removePrefixNodes(playername);
				if (prefixMap.containsKey(playername)) {
					addPrefix(playername, prefixMap.get(playername));
				}
			}
		} catch (NullPointerException e) {
			return;
		}
	}

	public static void removePrefixNodes(String playername) {
		ArrayList<Node> prefixNodes = new ArrayList<Node>();
		User user = um.getUser(playername);
		for (Node node : user.getNodes()) {
			if (node.getType().equals(NodeType.PREFIX)) {
				prefixNodes.add(node);
			}
		}

		for (int i = 0; i < prefixNodes.size(); i++) {
			user.data().remove(prefixNodes.get(i));
		}
		um.saveUser(user);
	}

	private static void addPrefix(String playername, String prefix) {
		User user = um.getUser(playername);
		user.data().add(PrefixNode.builder(prefix, 100).build());
		um.saveUser(user);
	}

}
