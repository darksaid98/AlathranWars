package me.ShermansWorld.AlathraWar.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.ShermansWorld.AlathraWar.War;
import me.ShermansWorld.AlathraWar.data.WarData;

public class WarTabCompletion implements TabCompleter{

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completions = new ArrayList<>();
		Player p = (Player)sender;
		if (args.length == 1) {
			if (p.hasPermission("AlathraWar.admin")) {
				completions.add("create");
				completions.add("delete");
			}
			completions.add("join");
			completions.add("surrender");
			completions.add("info");
			completions.add("list");
			completions.add("help");
			return completions;
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("surrender")) {
                ArrayList<War> wars = WarData.getWars();
				if (!wars.isEmpty()) {
					for (War war : wars) {
						completions.add(war.getName());
					}
					return completions;
				}
			} else if (args[0].equalsIgnoreCase("info")) {
				for (Player player : Bukkit.getOnlinePlayers()) {
					completions.add(player.getName());
				}
				return completions;
			}
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("join")) {
                ArrayList<War> wars = WarData.getWars();
				if (!wars.isEmpty()) {
					for (War war : wars) {
						if (war.getName().equalsIgnoreCase(args[1])) {
							completions.add(war.getSide1());
							completions.add(war.getSide2());
							return completions;
						}
					}
				}
			}
		}
		
		return Collections.emptyList();
	}
}