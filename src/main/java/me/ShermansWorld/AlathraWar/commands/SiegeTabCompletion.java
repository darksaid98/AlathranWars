package me.ShermansWorld.AlathraWar.commands;

import com.palmergames.bukkit.towny.utils.NameUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;

public class SiegeTabCompletion implements TabCompleter {


	private static final List<String> baseAdmin = List.of(new String[] {
			"abandon",
			"help",
			"list",
			"start",
			"stop"
	});

	private static final List<String> base = List.of(new String[] {
			"abandon",
			"help",
			"list",
			"start"
	});

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		boolean flag = sender.hasPermission("AlathraWar.admin");

		if (args.length > 0) {
			if(args.length > 1) {
				switch (args[0]) {
					case "abandon", "start", "stop" -> {
						if (args.length > 2) {
							if (args.length > 3) {
								return Collections.emptyList();
							}
							return NameUtil.filterByStart(CommandHelper.getTownyWarTowns(args[1]), args[2]);
						} else {
							return NameUtil.filterByStart(CommandHelper.getWarNames(), args[1]);
						}
					}
					//help, list, debug
					default -> {
						return Collections.emptyList();
					}
				}
			} else {
				return NameUtil.filterByStart(flag ? baseAdmin : base, args[0]);
			}
		} else {
			return flag ? baseAdmin : base;
		}
	}
}

