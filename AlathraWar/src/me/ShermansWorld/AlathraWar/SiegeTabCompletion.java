package me.ShermansWorld.AlathraWar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;

public class SiegeTabCompletion implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		List<String> completions = new ArrayList<>();
		Player p = (Player)sender;
		if (args.length == 1) {
			if (p.hasPermission("AlathraWar.admin")) {
				completions.add("stop");
			}
			completions.add("start");
			completions.add("abandon");
			completions.add("list");
			completions.add("help");
			return completions;
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("start")) {
				if (!WarCommands.wars.isEmpty()) {
					for (War war : WarCommands.wars) {
						completions.add(war.getName());
					}
					return completions;
				}
			} else if (args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("abandon")) {
				if (!SiegeCommands.sieges.isEmpty()) {
					for (Siege siege : SiegeCommands.sieges) {
						completions.add(String.valueOf(siege.getID()));
					}
					return completions;
				}
			}
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("start")) {
				if (!TownyAPI.getInstance().getTowns().isEmpty()) {
					for (Town town : TownyAPI.getInstance().getTowns()) {
						completions.add(town.getName());
					}
					return completions;
				}
			}
		}
		return Collections.emptyList();
	}
}

