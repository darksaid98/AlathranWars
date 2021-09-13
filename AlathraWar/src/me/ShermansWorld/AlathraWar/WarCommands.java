package me.ShermansWorld.AlathraWar;


import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarCommands implements CommandExecutor {
	
	public static ArrayList<War> wars = new ArrayList<War>();

	public WarCommands(Main plugin) {
		plugin.getCommand("war").setExecutor((CommandExecutor) this); // command to run in chat
	}

	// Player that sends command
	// Command it sends
	// Alias of the command which was used
	// args for Other values within command

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		Player p = (Player) sender; // Convert sender into player
		
		// Bukkit.getServer().broadcastMessage("Hey kid");
		
		
		// Check arguments, 0 means /war
		
		if (args.length == 0) {
			p.sendMessage(Helper.Chatlabel() + "Invalid Arguments. /war help");
		} else if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("create")) {
				if (!p.hasPermission("!AlatrhaWar.admin")) {
					p.sendMessage(Helper.Chatlabel() + Helper.color("&cYou do not have permission to do this"));
					return false;
				}
				if (args.length == 4) {
					if (wars.isEmpty()) {
						War war = new War(args[1], args[2], args[3]);
						wars.add(war); // create a war and add it to the list
						Main.data.getConfig().set("Wars." +  args[1] + ".side1", args[2]);
						Main.data.getConfig().set("Wars." +  args[1] + ".side2", args[3]);
						Main.data.saveConfig();
						p.sendMessage(Helper.Chatlabel() + "War created with the name " + args[1] + ", " + args[2] + " vs. " + args[3]);
					} else {
						for (int i = 0; i < wars.size(); i++) {
							if (wars.get(i).getName().equalsIgnoreCase(args[1])) {
								p.sendMessage(Helper.Chatlabel() + "A war already exists with that name! To view wars type /war list");
								break;
							} else {
								wars.add(new War(args[1], args[2], args[3])); // create a war and add it to the list
								i++;
								p.sendMessage(Helper.Chatlabel() + "War created with the name " + args[1] + ", " + args[2] + " vs. " + args[3]);
							}
						}
					}
				} else {
					p.sendMessage(Helper.Chatlabel() + "Invalid Arguments. /war create [name] [side1] [side2]");
				}
			} else if (args[0].equalsIgnoreCase("delete")) {
				if (!p.hasPermission("!AlatrhaWar.admin")) {
					p.sendMessage(Helper.Chatlabel() + Helper.color("&cYou do not have permission to do this"));
					return false;
				}
				if (args.length == 2) {
					boolean found = false;
					for (int i = 0; i < wars.size(); i++) {
						if (wars.get(i).getName().equalsIgnoreCase(args[1])) {
							p.sendMessage(Helper.Chatlabel() + "The war named " + args[1] + " has been deleted");
							wars.remove(i);
							Main.data.getConfig().set("Wars." +  args[1], null);
							Main.data.saveConfig();
							found = true;
							break;
						}
					}
					if (!found) {
						p.sendMessage(Helper.Chatlabel() + "Could not find a war with that name! To view wars type /war list");
					}
				} else {
					p.sendMessage(Helper.Chatlabel() + "Invalid Arguments. /war delete [name]");
				}
			} else if (args[0].equalsIgnoreCase("list")) {
				if (wars.isEmpty()) {
					p.sendMessage(Helper.Chatlabel() + "There are currently no wars");
				} else {
					p.sendMessage(Helper.Chatlabel() + "CurrentWars:");
					for (War war : wars) {
						p.sendMessage(war.getName() + " - " + war.getSide1() + " vs. " + war.getSide2());;
					}
				}
				
			} else if (args[0].equalsIgnoreCase("join")) {
				boolean found = false;
				if (args.length == 3) {
					for (War war : wars) {
						if (war.getName().equalsIgnoreCase(args[1])) {
							if (war.getSide1().equalsIgnoreCase(args[2])) {
								if (war.getSide1Players().contains(p.getName())) {
									p.sendMessage(Helper.Chatlabel() + "You have already joined this war! Type /war leave [war] to leave the war");
									return false;
								}
								war.addPlayerSide1(p.getName());
								p.sendMessage(Helper.Chatlabel() + "You have joined the war on the side of " + war.getSide1());
								p.setPlayerListName(Helper.color("&c[" + war.getSide1() + "]&r") + p.getName());
								Main.data.getConfig().set("Wars." +  args[1] + ".side1players", war.getSide1Players());
								Main.data.saveConfig();
								Bukkit.getServer().broadcastMessage(Helper.Chatlabel() + p.getName() + " has joined " + war.getName() + " on the side of " + war.getSide1() + "!");
								found = true;
							} else if (war.getSide2().equalsIgnoreCase(args[2])) {
								if (war.getSide2Players().contains(p.getName())) {
									p.sendMessage(Helper.Chatlabel() + "You have already joined this war! Type /war leave [war] to leave the war");
									return false;
								}
								war.addPlayerSide2(p.getName());
								p.sendMessage(Helper.Chatlabel() + "You have joined the war on the side of " + war.getSide2());
								p.setPlayerListName(Helper.color("&9[" + war.getSide2() + "]&r") + p.getName());
								Main.data.getConfig().set("Wars." +  args[1] + ".side2players", war.getSide2Players());
								Main.data.saveConfig();
								Bukkit.getServer().broadcastMessage(Helper.Chatlabel() + p.getName() + " has joined " + war.getName() + " on the side of " + war.getSide2() + "!");
								found = true;
							}	
						}
					}
					if (!found) {
						p.sendMessage(Helper.Chatlabel() + "War not found. /war join [name] [side], type /war list to view current wars");
					}
				}  else {
					p.sendMessage(Helper.Chatlabel() + "Invalid Arguments. /war join [name] [side]");
				}
				
			} else if(args[0].equalsIgnoreCase("leave")) {
				if (args.length == 2) {
					boolean found = false;
					boolean found2 = false;
					for (War war : wars) {
						if (war.getName().equalsIgnoreCase(args[1])) {
							found = true;
							if (war.getSide1Players().contains(p.getName())) {
								found2 = true;
								war.removePlayerSide1(p.getName());
								Main.data.getConfig().set("Wars." +  args[1] + ".side1players", war.getSide1Players());
								Main.data.saveConfig();
								p.sendMessage(Helper.Chatlabel() + "You have left the war");
								p.setPlayerListName(p.getName()); 
								Bukkit.getServer().broadcastMessage(Helper.Chatlabel() + p.getName() + " has left " + war.getName() + ", they were on the side of " + war.getSide1());
							}
							if (war.getSide2Players().contains(p.getName())) {
								found2 = true;
								war.removePlayerSide2(p.getName());
								Main.data.getConfig().set("Wars." +  args[1] + ".side2players", war.getSide2Players());
								Main.data.saveConfig();
								p.sendMessage(Helper.Chatlabel() + "You have left the war");
								p.setPlayerListName(p.getName());
								Bukkit.getServer().broadcastMessage(Helper.Chatlabel() + p.getName() + " has left " + war.getName() + ", they were on the side of " + war.getSide2());
							}
						} 
					}
					if (!found) {
						p.sendMessage(Helper.Chatlabel() + "War not found. /war leave [name], type /war list to view current wars");
						return false;
					}
					if (!found2) {
						p.sendMessage(Helper.Chatlabel() + "You are not part of this war!");
						return false;
					}
				} else {
					p.sendMessage(Helper.Chatlabel() + "Invalid Arguments. /war leave [name]");
				}
			} else if (args[0].equalsIgnoreCase("info")) {
				boolean found = false;
				if (args.length == 2) {
					for (War war : wars) {
						if (war.getSide1Players().contains(args[1])) {
							if (!found) {
								p.sendMessage(Helper.Chatlabel() + args[1] + " is currently in the following wars");
							}
							found = true;
							p.sendMessage(war.getName() + " - " + " Fighting for " + war.getSide1());
						} else if (war.getSide2Players().contains(args[1])) {
							if (!found) {
								p.sendMessage(Helper.Chatlabel() + args[1] + " is currently in the following wars");
							}
							found = true;
							p.sendMessage(war.getName() + " - " + " Fighting for " + war.getSide2());
						}
					}
					if(!found) {
						p.sendMessage(Helper.Chatlabel() + "This player is not currently in any wars");
					}
				} else {
					p.sendMessage(Helper.Chatlabel() + "Invalid Arguments. /war info [player]");
				}

			} else if(args[0].equalsIgnoreCase("help")) {
				if (p.hasPermission("!AlatrhaWar.admin")) {
					p.sendMessage(Helper.Chatlabel() + "/war create [name] [side1] [side2]");
					p.sendMessage(Helper.Chatlabel() + "/war delete [name]");
				}
				p.sendMessage(Helper.Chatlabel() + "/war join [name] [side]");
				p.sendMessage(Helper.Chatlabel() + "/war leave [name]");
				p.sendMessage(Helper.Chatlabel() + "/war info [player]");


			} else {
				p.sendMessage(Helper.Chatlabel() + "Invalid Arguments. /war help");
			}
		} else {
			p.sendMessage(Helper.Chatlabel() + "Invalid Arguments. /war help");
		}

		return false;
	}

}
