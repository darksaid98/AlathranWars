package me.ShermansWorld.AlathraWar.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.palmergames.bukkit.towny.utils.NameUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.ShermansWorld.AlathraWar.War;
import me.ShermansWorld.AlathraWar.data.WarData;

public class WarTabCompletion implements TabCompleter{

	private static final List<String> baseAdmin = List.of(new String[]{
			"create",
			"delete",
			"help",
			"info",
			"join",
			"list",
			"surrender"
	});

	private static final List<String> base = List.of(new String[]{
			"help",
			"info",
			"join",
			"list",
			"surrender"
	});

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		boolean flag = sender.hasPermission("AlathraWar.admin");

		if (args.length > 0) {
			if(args.length > 1) {
				switch (args[0]) {
					case "join" -> {
						if (args.length > 2) {
							if (args.length > 3) {
								return Collections.emptyList();
							}
							return NameUtil.filterByStart(CommandHelper.getWarSides(args[1]), args[2]);
						} else {
							return NameUtil.filterByStart(CommandHelper.getWarNames(), args[1]);
						}
					}
					case "info" -> {
						if (args.length > 2) {
							return Collections.emptyList();
						}
						return NameUtil.filterByStart(CommandHelper.getPlayers(), args[1]);
					}
					case "delete", "surrender" -> {
						if (args.length > 2) {
							return Collections.emptyList();
						}
						return NameUtil.filterByStart(CommandHelper.getWarNames(), args[1]);
					}
					//create, list, help
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