package me.ShermansWorld.AlathraWar.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;

import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.War;
import me.ShermansWorld.AlathraWar.data.WarData;
import me.ShermansWorld.AlathraWar.hooks.TABHook;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import org.bukkit.command.CommandExecutor;

public class WarCommands implements CommandExecutor {
	public static ArrayList<War> wars;

	static {
		WarCommands.wars = new ArrayList<War>();
	}

	public WarCommands(final Main plugin) {
		plugin.getCommand("war").setExecutor((CommandExecutor) this);
	}

	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            consoleCommand(sender, args);
            return true;
        }

		final Player p = (Player) sender;
		if (args.length == 0) {
			p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /war help");
            return true;
		} else if (args.length >= 1) {
            // Create - Admin Command
            // Delete - Admin Command
            // Join - Auto works out town/nation e.t.c.
            // Surrender - Auto works out town/nation e.t.c.
            // List - Lists all current wars
            // Info - Info on a player and their wars.

            switch(args[0].toLowerCase()) {
                case "create":
                    if (!p.hasPermission("AlathraWar.admin")) {
                        p.sendMessage("You do not have permission to run this command.");
                        return true;
                    }
                    if (args.length >= 4) {
                        War war = new War(args[1], args[2], args[3]);
                        WarData.addWar(war);
                        war.save();

                        p.sendMessage(String.valueOf(Helper.Chatlabel()) + "War created with the name " + args[1] + ", "
								+ args[2] + " vs. " + args[3]);
						Main.warLogger.log(p.getName() + " created a new war with the name " + args[1] + ", " + args[2]
								+ " vs. " + args[3]);
                                return true;
                    } else {
                        p.sendMessage(String.valueOf(Helper.Chatlabel())
                                + "Invalid Arguments. /war create [name] [side1] [side2]");
                    }

                    return true;
                case "delete":
                    if (!p.hasPermission("AlathraWar.admin")) {
                        p.sendMessage("You do not have permission to run this command.");
                        return true;
                    }
                    return true;
                case "join":
                    // Sufficient args check
                    if (args.length < 3) {
                        p.sendMessage(Helper.Chatlabel()
								+ "/war join [name] [side], type /war list to view current wars");
                        return true;
                    }

                    // War check
                    War war = WarData.getWar(args[1]);
                    if (war == null) {
                        p.sendMessage(Helper.Chatlabel()
								+ "War not found. /war join [name] [side], type /war list to view current wars");
                        return true;
                    }

                    // Side check
                    if (!war.getSide1().equalsIgnoreCase(args[2]) && !war.getSide2().equalsIgnoreCase(args[2])) {
                        p.sendMessage(Helper.Chatlabel()
								+ "Side not found. Type /war list to view current wars");
                        return true;
                    }

                    Resident res = TownyAPI.getInstance().getResident(p);
                    if (res.hasNation()) {
                        if(p.hasPermission("AlathraWar.nationjoin" )|| res.isKing()) {
                            // Has nation declaration permission
                            war.addNation(res.getNationOrNull(), args[2]);
                            p.sendMessage(Helper.Chatlabel() + "You have joined the war for " + res.getNationOrNull().getName());
                            war.save();
                            return true;
                        } else {
                            // Cannot declare nation involvement
                            p.sendMessage(Helper.Chatlabel() + "You cannot declare war for your nation.");
                            return true;
                        }
                    } else if (res.hasTown()) {
                        if (p.hasPermission("AlathraWar.townjoin") || res.isMayor()) {
                            // Is in indepdenent town & has declaration perms
                            war.addTown(res.getTownOrNull(), args[2]);
                            p.sendMessage(Helper.Chatlabel() + "You have joined the war for " + res.getTownOrNull().getName());
                            war.save();
                            return true;
                        } else {
                            // No perms
                            p.sendMessage(Helper.Chatlabel() + "You cannot declare war for your town.");
                            return true;
                        }
                    }

                    return true;
                case "surrender":
                    return true;
                case "list":
                    
                    return true;
                case "info":
                    return true;
                default:
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /war help");
                    return true;
            }

			
		
        } 
		return false;
	}

    /**
     * Method for console access
     * @param sender
     * @param args
     */
    private static void consoleCommand(CommandSender sender, String[] args) {
        if (args.length < 1) return;
        if (args[0].equalsIgnoreCase("list")) {
            ArrayList<War> warList = WarData.getWars();
            sender.sendMessage("Wars: " + warList.size());
            for (War war : WarData.getWars()) {
                if (war == null) continue;
                sender.sendMessage(war.toString());
            }
        }
    }

}
