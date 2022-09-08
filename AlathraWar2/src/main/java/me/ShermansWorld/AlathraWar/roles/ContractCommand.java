package me.ShermansWorld.AlathraWar.roles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ShermansWorld.AlathraWar.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ContractCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		Player p = (Player) sender;

        //LOAD PLAYER DATA
        UUID pID = p.getUniqueId();
        Map pData = (Map) Main.rolesData.getData(pID);
		
        p.sendMessage(ChatColor.YELLOW + "Contract Command" );
		return false;
	}
}