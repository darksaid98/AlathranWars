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
		
        p.sendMessage(ChatColor.YELLOW + "Contract Command" );
		
		if(!p.hasPermission("AlathraWar.UseRoles")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return false;
        }
		if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            p.sendMessage(ChatColor.RED + "! "+ ChatColor.RESET + ChatColor.GOLD + "---- Contracts Help ----");
            p.sendMessage(ChatColor.RED + "! " + ChatColor.GREEN + "/contract list");
            return false;
        }
		else {
            p.sendMessage(ChatColor.RED + "Unknown sub-command. Do " + ChatColor.YELLOW + "/contract help" + ChatColor.RED + " for more info.");
        }
		
		return false;
	}
}