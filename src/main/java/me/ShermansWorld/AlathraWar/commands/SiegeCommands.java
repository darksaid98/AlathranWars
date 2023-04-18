package me.ShermansWorld.AlathraWar.commands;

import org.bukkit.OfflinePlayer;
import com.palmergames.bukkit.towny.TownyAPI;
import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.Siege;
import me.ShermansWorld.AlathraWar.War;
import me.ShermansWorld.AlathraWar.data.SiegeData;
import me.ShermansWorld.AlathraWar.data.WarData;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.palmergames.bukkit.towny.object.Town;
import java.util.ArrayList;
import java.util.HashSet;
import org.bukkit.command.CommandExecutor;

public class SiegeCommands implements CommandExecutor {    
    public SiegeCommands(final Main plugin) {
        plugin.getCommand("siege").setExecutor((CommandExecutor) this);
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length == 0) {
            sender.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /siege help");
            return true;
        }
        
        // Start
        // Stop
        // Abandon
        // List
        // Help

        switch (args[0].toLowerCase()) {
            case "start":
                siegeStart(sender, args);
                return true;
            case "stop":
                siegeStop(sender, args);
                return true;
            case "abandon":
                siegeAbandon(sender, args);
                return true;
            case "list":
                siegeList(sender, args);
                return true;
            case "help":
                siegeHelp(sender, args);
                return true;
            case "debug":
                siegeDebug(sender, args);
                return true;
        }
        return false;
    }

    private static void siegeStart(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        final Player player = (Player) sender;

        if (args.length < 3) {
            sender.sendMessage(Helper.Chatlabel() + "/war siege [war] [town]");
            return;
        }

        // War check
        War war = WarData.getWar(args[1]);
        if (war == null) {
            player.sendMessage(String.valueOf(Helper.Chatlabel()) + "That war does not exist! /siege start [war] [town]");
            return;
        }

        // Player participance check
        Town leaderTown = TownyAPI.getInstance().getResident(player).getTownOrNull();
        int side = war.getSide(leaderTown.getName());
        if (side == 0) {
            player.sendMessage(Helper.Chatlabel() + "You are not in this war.");
            return;
        } else if (side == -1) {
            player.sendMessage(Helper.Chatlabel() + "You have surrendered.");
            return;
        }

        // Town check
        Town town = TownyAPI.getInstance().getTown(args[2]);
        if (town == null) {
            player.sendMessage(String.valueOf(Helper.Chatlabel()) + "That town does not exist! /siege start [war] [town]");
            return;
        }
        
        // Attacking own side
        if (war.getSide(town) == side) {
            player.sendMessage(Helper.Chatlabel() + "You cannot attack your own towns.");
            return;
        }

        Siege siege = new Siege(war, town, player);
        SiegeData.addSiege(siege);
        war.addSiege(siege);
        war.save();
        siege.start();
    }

    private static void siegeStop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("AlathraWar.admin")) {
            sender.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("&cYou do not have permission to do this"));
            return;
        }
        HashSet<Siege> sieges = SiegeData.getSieges();
        if (sieges.isEmpty()) {
            sender.sendMessage(String.valueOf(Helper.Chatlabel()) + "There are currently no sieges in progress");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Helper.Chatlabel() + "/war stop [town]");
            return;
        }
        for (Siege siege : sieges) {
            if (siege.getTown().getName().equalsIgnoreCase(args[1])) {
                sender.sendMessage(String.valueOf(Helper.Chatlabel()) + "siege cancelled");
                Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The siege at " + siege.getTown().getName() + " has been cancelled by an admin");
                //siege.clearBeacon();
                siege.stop();
                return;
            }
        }
        sender.sendMessage(String.valueOf(Helper.Chatlabel()) + "A siege could not be found with this town name! Type /siege list to view current sieges");
    }
    
    private static void siegeAbandon(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        final Player p = (Player) sender;

        if (args.length < 2) {
            sender.sendMessage(Helper.Chatlabel() + "/war abandon [town]");
            return;
        }

        Siege siege = SiegeData.getSiege(args[1]);
        if (siege == null) {
            p.sendMessage(Helper.Chatlabel() + "Invalid siege");
            return;
        }

        OfflinePlayer oPlayer = siege.getSiegeLeader();
        if (oPlayer != (OfflinePlayer) p) {
            p.sendMessage(Helper.Chatlabel() + "You are not the leader of this siege.");
            return;
        }

        Bukkit.broadcastMessage(String.valueOf(Helper.Chatlabel()) + "The siege at " + siege.getTown().getName() + " has been abandoned.");
        Main.warLogger.log(p.getName() + " abandoned the siege they started at " + siege.getTown().getName());
        siege.defendersWin();
                            
    }

    private static void siegeList(CommandSender sender, String[] args) {
        HashSet<Siege> sieges = SiegeData.getSieges();
        if (sieges.isEmpty()) {
            sender.sendMessage(String.valueOf(Helper.Chatlabel()) + "There are currently no sieges in progress");
            return;
        } 

        sender.sendMessage(String.valueOf(Helper.Chatlabel()) + "Sieges currently in progress:");
        for (Siege siege : sieges) {
            War war = siege.getWar();
            String color = (siege.getSide1AreAttackers() && (war.getSide(siege.getTown().getName()) == 1) ) ? "§c" : "§9";
            sender.sendMessage(war.getName() + " - " + color + siege.getTown().getName());
            sender.sendMessage(war.getSide1() + " - " + (siege.getSide1AreAttackers() ? siege.getAttackerPoints() : siege.getDefenderPoints()));
            sender.sendMessage(war.getSide2() + " - " + (siege.getSide1AreAttackers() ? siege.getDefenderPoints() : siege.getAttackerPoints()));
            sender.sendMessage("Time Left: " + (Siege.maxSiegeTicks - siege.getSiegeTicks())/1200 + " minutes");
            sender.sendMessage("-=-=-=-=-=-=-=-=-=-=-=-");
        }
        
    }

    private static void siegeHelp(CommandSender sender, String[] args) {
        if (sender.hasPermission("AlathraWar.admin")) {
            sender.sendMessage(Helper.Chatlabel() + "/siege stop [town]");
        }
        sender.sendMessage(Helper.Chatlabel() + "/siege start [war] [town]");
        sender.sendMessage(Helper.Chatlabel() + "/siege abandon [town]");
        sender.sendMessage(Helper.Chatlabel() + "/siege list");
    }

    private static void siegeDebug(CommandSender sender, String[] args) {
        if (!(sender instanceof OfflinePlayer)) return;
        final Player player = (Player) sender;
        if (!sender.isOp()) return;
        sender.sendMessage("DEBUG");
        sender.sendMessage(WarData.getWars().toString());
        for (War war : WarData.getWars()) {
            sender.sendMessage(war.getName());
            ArrayList<Siege> sieges = war.getSieges();
            sender.sendMessage(sieges.toString());
        }


        if (args.length > 1 && args[1].equalsIgnoreCase("new")) {
            War war = WarData.getWar(args[2]);
            Town town = TownyAPI.getInstance().getTown(args[3]);
            Siege siege = new Siege(war, town, (OfflinePlayer) player);
            SiegeData.addSiege(siege);
            war.addSiege(siege);
            war.save();
            siege.start();
        }
    }

}
