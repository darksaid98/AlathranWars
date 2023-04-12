package me.ShermansWorld.AlathraWar.commands;

import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.War;
import me.ShermansWorld.AlathraWar.data.WarData;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import org.bukkit.command.CommandExecutor;

public class WarCommands implements CommandExecutor {

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

            switch(args[0].toLowerCase()) {
                case "create":
                    warCreate(p, args);
                    return true;
                case "delete":
                    warDelete(p, args);
                    return true;
                case "join":
                    warJoin(p, args);
                    return true;
                case "surrender":
                    warSurrender(p, args);
                    return true;
                case "list":
                    warList(p, args);
                    return true;
                case "info":
                    warInfo(p, args);
                    return true;
                case "help":
                    warHelp(p, args);
                    return true;
                default:
                    p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /war help");
                    return true;
            }
       
        } 
		return false;
	}

    public static void warCreate(Player p, String[] args) {
        if (!p.hasPermission("AlathraWar.admin")) {
            p.sendMessage("You do not have permission to run this command.");
            return;
        }
        if (args.length >= 4) {
            War war = new War(args[1], args[2], args[3]);
            WarData.addWar(war);
            war.save();

            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "War created with the name " + args[1] + ", "
                    + args[2] + " vs. " + args[3]);
            Main.warLogger.log(p.getName() + " created a new war with the name " + args[1] + ", " + args[2]
                    + " vs. " + args[3]);
                    return;
        } else {
            p.sendMessage(String.valueOf(Helper.Chatlabel())
                    + "Invalid Arguments. /war create [name] [side1] [side2]");
        }

    }

    private static void warDelete(Player p, String[] args) {
        if (!p.hasPermission("AlathraWar.admin")) {
            p.sendMessage("You do not have permission to run this command.");
            return;
        }

        if (args.length >= 2) {
            War war = WarData.getWar(args[1]);

            // War Check
            if (war == null) {
                p.sendMessage(Helper.Chatlabel()
                        + "War not found. Type /war list to view current wars.");
                return;
            }

            WarData.removeWar(war);

            p.sendMessage(String.valueOf(Helper.Chatlabel()) + "War " + args[1] + " deleted.");
            Main.warLogger.log(p.getName() + " deleted " + args[1]);
                    return;
        } else {
            p.sendMessage(String.valueOf(Helper.Chatlabel())
                    + "Invalid Arguments. /war delete [name]");
        }
    }

    private static void warJoin(Player p, String[] args) {
        // Sufficient args check
        if (args.length < 3) {
            p.sendMessage(Helper.Chatlabel()
                    + "/war join [name] [side], type /war list to view current wars");
            return;
        }

        // War check
        War war = WarData.getWar(args[1]);
        if (war == null) {
            p.sendMessage(Helper.Chatlabel()
                    + "War not found. /war join [name] [side], type /war list to view current wars");
            return;
        }

        // Towny Resident Object
        Resident res = TownyAPI.getInstance().getResident(p);

        // Town check
        Town town = res.getTownOrNull();
        if (town == null) {
            p.sendMessage(ChatColor.RED + "You are not in a town.");
            return;
        }

        // Side checks
        int side = war.getSide(town.getName().toLowerCase());
        if (side == -1) {
            p.sendMessage(Helper.Chatlabel() + "You've already surrendered!");
            return;
        } else if (side > 0) {
            p.sendMessage(Helper.Chatlabel() + "You're already in this war!");
            return;
        }

        if (res.hasNation()) {
            if(p.hasPermission("AlathraWar.nationjoin" )|| res.isKing()) {
                // Has nation declaration permission
                war.addNation(res.getNationOrNull(), args[2]);
                p.sendMessage(Helper.Chatlabel() + "You have joined the war for " + res.getNationOrNull().getName());
                war.save();
                return;
            } else {
                // Cannot declare nation involvement
                p.sendMessage(Helper.Chatlabel() + "You cannot declare war for your nation.");
                return;
            }
        } else if (res.hasTown()) {
            if (p.hasPermission("AlathraWar.townjoin") || res.isMayor()) {
                // Is in indepdenent town & has declaration perms
                war.addTown(res.getTownOrNull(), args[2]);
                p.sendMessage(Helper.Chatlabel() + "You have joined the war for " + res.getTownOrNull().getName());
                war.save();
                return;
            } else {
                // No perms
                p.sendMessage(Helper.Chatlabel() + "You cannot declare war for your town.");
                return;
            }
        }
    }

    private static void warSurrender(Player p, String[] args) {
        // Sufficient args check
        if (args.length < 2) {
            p.sendMessage(Helper.Chatlabel()
                    + "/war surrender [name], type /war list to view current wars");
            return;
        }

        // War check
        War war = WarData.getWar(args[1]);
        if (war == null) {
            p.sendMessage(Helper.Chatlabel()
                    + "War not found. Type /war list to view current wars");
            return;
        }

        // Towny Resident Object
        Resident res = TownyAPI.getInstance().getResident(p);

        // Town check
        Town town = res.getTownOrNull();
        if (town == null) {
            p.sendMessage(ChatColor.RED + "You are not in a town.");
            return;
        }
        
        if (res.hasNation()) {
            if(p.hasPermission("AlathraWar.nationsurrender" )|| res.isKing()) {
                // Has nation surrender permission
                war.surrenderNation(res.getNationOrNull());
                p.sendMessage(Helper.Chatlabel() + "You have surrendered the war for " + res.getNationOrNull().getName());
                war.save();
                return;
            } else {
                // Cannot surrender nation involvement
                p.sendMessage(Helper.Chatlabel() + "You cannot surrender war for your nation.");
                return;
            }
        } else if (res.hasTown()) {
            if (p.hasPermission("AlathraWar.townsurrender") || res.isMayor()) {
                // Is in indepdenent town & has surrender perms
                war.surrenderTown(res.getTownOrNull().getName());
                p.sendMessage(Helper.Chatlabel() + "You have surrendered the war for " + res.getTownOrNull().getName());
                war.save();
                return;
            } else {
                // No perms
                p.sendMessage(Helper.Chatlabel() + "You cannot surrender war for your town.");
                return;
            }
        }

    }

    private static void warList(Player p, String[] args) {
        p.sendMessage(Helper.Chatlabel()+ "Wars:");
        ArrayList<War> wars = WarData.getWars();
        if (wars.size() < 1) {
            p.sendMessage("There are no current wars.");
        } else {
            for (War war : wars) {
                p.sendMessage(war.getName() + " - " + war.getSide1() + " vs. " + war.getSide2());
            }
        }
    }

    private static void warInfo(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(Helper.Chatlabel() + "/war info [Player]");
        }

        Resident res = TownyAPI.getInstance().getResident(args[1]);
        if (res == null || res.getTownOrNull() == null) {
            p.sendMessage(Helper.Chatlabel() + "Invalid town resident.");
        }

        p.sendMessage(Helper.Chatlabel() + p.getName() + "'s wars:");
        for (War war : WarData.getWars()) {
            int side = war.getSide(args[1]);
            if (side != 0) {
                if (side == -1) {
                    p.sendMessage(war.getName() +  " - Surrendered");
                } else if (side == 1) {
                    p.sendMessage(war.getName() +  " - " + war.getSide1());
                } else if (side == 2) {
                    p.sendMessage(war.getName() +  " - " + war.getSide2());
                }
            }
        }
    }

    private static void warHelp(Player p, String[] args) {
        // TODO
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
