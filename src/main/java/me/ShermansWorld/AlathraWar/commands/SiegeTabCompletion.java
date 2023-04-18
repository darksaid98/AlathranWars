package me.ShermansWorld.AlathraWar.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;

import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.War;
import me.ShermansWorld.AlathraWar.data.SiegeData;
import me.ShermansWorld.AlathraWar.data.WarData;

public class SiegeTabCompletion implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player)) return consoleTabComplete(sender, args);

		List<String> completions = new ArrayList<>();
		Player p = (Player) sender;

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
				if (!WarData.getWars().isEmpty()) {
					for (War war : WarData.getWars()) {
						completions.add(war.getName());
					}
					return startList(args[1], completions);
				}
			} else if (args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("abandon")) {
				if (!SiegeData.getSieges().isEmpty()) {
					for (Siege siege : SiegeData.getSieges()) {
						completions.add(String.valueOf(siege.getTown().getName()));
					}
					return startList(args[1], completions);
				}
			}
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("start")) {
				if (!TownyAPI.getInstance().getTowns().isEmpty()) {
					for (Town town : TownyAPI.getInstance().getTowns()) {
						completions.add(town.getName());
					}
                    return startList(args[2], completions);
				}
			}
		} //*/
		return Collections.emptyList();
	}

    private List<String> consoleTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    private List<String> startList(String start, List<String> list) {
        ArrayList<String> returnList = new ArrayList<String>();
        for (String str : list) {
            if (str.startsWith(start)) returnList.add(str);
        }
        return returnList;
    }
}

