package me.ShermansWorld.AlathraWar.roles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.UUIDFetcher;

import java.util.Map;
import java.util.UUID;


public class ContractCommand implements CommandExecutor {

	public ContractCommand(final Main plugin) {
        plugin.getCommand("contract").setExecutor((CommandExecutor) this);
        plugin.getCommand("contract").setTabCompleter(new CustomRoleTabCompletion());
    }
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		Player p = (Player) sender;

        //LOAD PLAYER DATA
        UUID pID = p.getUniqueId();
        Map pData = (Map) Main.rolesData.getData(pID);
		
		if(!p.hasPermission("AlathraWar.UseRoles")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return false;
        }
		if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            p.sendMessage(ChatColor.RED + "! "+ ChatColor.RESET + ChatColor.GOLD + "---- Contracts Help ----");
            if (p.hasPermission("AlathraWar.Admin")) {
            	p.sendMessage(ChatColor.RED + "! " + ChatColor.GREEN + "/contract list <player>");
            }
            else {
            	p.sendMessage(ChatColor.RED + "! " + ChatColor.GREEN + "/contract list");
            }
            return false;
        }
		else if(args[0].equalsIgnoreCase("list")) {
			UUID targetID;
			if(p.hasPermission("AlathraWar.Admin") && args.length > 1) {
				Player target = Bukkit.getPlayer(args[1]);
	            String msgName;
	            if(target != null) {
	            	targetID = target.getUniqueId();
	            	msgName = target.getDisplayName();
	            }
	            else {
	            	targetID = UUIDFetcher.getUUID(args[1]);
	            	if(targetID == null) {
	            		p.sendMessage(ChatColor.RED + "Please enter a valid player.");
	            		return false;
	            	}
	            	msgName = args[1];
	            }
			}
			else {
				targetID = pID;
			}
				
			if(targetID == null) {
				p.sendMessage("ERROR");
				return false;
			}
			Map tData = (Map) Main.rolesData.getData(targetID);
			Map<String, Object> tContracts = (Map<String, Object>) tData.get("Contracts");
			
			if(tContracts.size() < 1) {
				if(pID == targetID) {
					p.sendMessage(ChatColor.GREEN + "You have " + ChatColor.GOLD + "0" + ChatColor.GREEN + " current contracts.");
				} else {
					p.sendMessage(ChatColor.GREEN + "They have " + ChatColor.GOLD + "0" + ChatColor.GREEN + " current contracts.");
				}
			}
			else {
				p.sendMessage(ChatColor.GOLD + "Current Contracts:");
				for (String key : tContracts.keySet()) {
					Map<String,Object> Contract = (Map<String, Object>) tContracts.get(key);
					String ContractName = Bukkit.getOfflinePlayer(UUID.fromString(key)).getName();
					String ContractType = (String) Contract.get("Type");
					if(ContractType.equalsIgnoreCase("Assassin")) {
						p.sendMessage(ChatColor.GREEN + "- " + ContractName + " for " + ChatColor.DARK_PURPLE + Main.econ.format((double) Contract.get("Value")));
					}
					else if(ContractType.equalsIgnoreCase("Merc")) {
						p.sendMessage(ChatColor.GREEN + "- " + ContractName + " for " + ChatColor.DARK_AQUA + Main.econ.format((double) Contract.get("Value")));
					}
				}
			}
			
		}
		else {
            p.sendMessage(ChatColor.RED + "Unknown sub-command. Do " + ChatColor.YELLOW + "/contract help" + ChatColor.RED + " for more info.");
        }
		
		return false;
	}
}