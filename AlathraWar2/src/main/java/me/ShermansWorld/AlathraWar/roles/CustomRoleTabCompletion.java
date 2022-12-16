package me.ShermansWorld.AlathraWar.roles;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.ShermansWorld.AlathraWar.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "rawtypes" })
public class CustomRoleTabCompletion implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        Map pData = (Map) Main.rolesData.getData(player.getUniqueId());

        String cmdName = command.getName();
        List<String> completions = new ArrayList<>();
        if(cmdName.equalsIgnoreCase("mercenary")){
            if(args.length == 1) {
                completions.add("hire");
                if ((Boolean) pData.get("MercPermission")) {
                    completions.add("accept");
                    completions.add("decline");
                    completions.add("complete");
                }
                if (player.hasPermission("AlathraWar.Admin")) {
                    completions.add("add");
                    completions.add("remove");
                }
                return completions;
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("add")) {
                    return null;
                } else if (args[0].equalsIgnoreCase("remove")) {
                    return null;
                } else if (args[0].equalsIgnoreCase("accept")) {
                    // Get everyone who has a request
                    return null;
                } else if (args[0].equalsIgnoreCase("decline")) {
                    // Get everyone who has a request
                    return null;
                } else if (args[0].equalsIgnoreCase("hire")) {
                    return null;
                }
                return completions;
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("hire")) {
                    completions.add("10000");
                    completions.add("20000");
                    completions.add("50000");
                    completions.add("100000");
                    return completions;
                }
            } else {
                return null;
            }
        }
        else if (cmdName.equalsIgnoreCase("assassin")) {
            if(args.length == 1) {
                completions.add("hire");
                if ((Boolean) pData.get("AssassinPermission")) {
                    completions.add("accept");
                    completions.add("decline");
                    completions.add("complete");
                }
                if (player.hasPermission("AlathraWar.Admin")) {
                    completions.add("add");
                    completions.add("remove");
                }
                return completions;
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("add")) {
                    return null;
                } else if (args[0].equalsIgnoreCase("remove")) {
                    return null;
                } else if (args[0].equalsIgnoreCase("accept")) {
                    // Get everyone who has a request
                    return null;
                } else if (args[0].equalsIgnoreCase("decline")) {
                    // Get everyone who has a request
                    return null;
                } else if (args[0].equalsIgnoreCase("hire")) {
                    return null;
                }
                return completions;
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("hire")) {
                    completions.add("10000");
                    completions.add("20000");
                    completions.add("50000");
                    completions.add("100000");
                    return completions;
                }
            } else {
                return null;
            }
        }
        else if (cmdName.equalsIgnoreCase("contract")) {
        	if(args.length == 1) {
        		completions.add("list");
        		return completions;
        	}
        	else if(args.length == 2) {
        		if(player.hasPermission("AlathraWar.Admin")) {
        			return null;
        		} else {
        			return completions;
        		}
        	}
        }
        return null;
    }
}