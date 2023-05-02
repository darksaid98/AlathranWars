package me.ShermansWorld.AlathraWar.commands;

import me.ShermansWorld.AlathraWar.*;
import me.ShermansWorld.AlathraWar.data.RaidData;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;
import com.palmergames.bukkit.towny.TownyAPI;
import me.ShermansWorld.AlathraWar.data.SiegeData;
import me.ShermansWorld.AlathraWar.data.WarData;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import com.palmergames.bukkit.towny.object.Town;

import java.util.HashSet;
import org.bukkit.command.CommandExecutor;

public class SiegeCommands implements CommandExecutor {    
    public SiegeCommands(final Main plugin) {
        plugin.getCommand("siege").setExecutor((CommandExecutor) this);
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        CommandHelper.logCommand(sender, label, args);
        if (args.length == 0) {
            sender.sendMessage(String.valueOf(Helper.chatLabel()) + "Invalid Arguments. /siege help");
            return true;
        }
        
        // Start
        // Stop
        // Abandon
        // List
        // Help

        switch (args[0].toLowerCase()) {
            case "start" -> {
                siegeStart(sender, args, false);
                return true;
            }
            case "stop" -> {
                siegeStop(sender, args);
                return true;
            }
            case "abandon" -> {
                siegeAbandon(sender, args);
                return true;
            }
            case "list" -> {
                siegeList(sender, args);
                return true;
            }
            case "help" -> {
                siegeHelp(sender, args);
                return true;
            }
        }
        return false;
    }

    protected static void siegeStart(CommandSender sender, String[] args, boolean admin) {
//        if (!(sender instanceof Player)) return;
        //if this is admin mode use the forth arg instad of sender.
        //if player is null after this then force end
        Player siegeOwner = null;
        if((sender instanceof Player)) {
            siegeOwner = (Player) sender;
        }
        if(admin && args.length >= 5) {
            siegeOwner = Bukkit.getPlayer(args[3]);
            if(siegeOwner == null) {
                sender.sendMessage("Player Does not exist");
                return;
            }
        }

        if (siegeOwner == null) {
            sender.sendMessage("Owner is Null");
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(Helper.chatLabel() + "/war siege [war] [town]");
            return;
        }

        // War check
        War war = WarData.getWar(args[1]);
        if (war == null) {
            siegeOwner.sendMessage(String.valueOf(Helper.chatLabel()) + "That war does not exist! /siege start [war] [town]");
            if(admin) sender.sendMessage(String.valueOf(Helper.chatLabel()) + "That war does not exist! /siege start [war] [town]");
            return;
        }


        // Player participance check
        Town leaderTown = TownyAPI.getInstance().getResident(siegeOwner).getTownOrNull();
        int side = war.getSide(leaderTown.getName().toLowerCase());

        //DEBUG PRINT
        Main.warLogger.log("Leader Town: " + leaderTown.getName());
        Main.warLogger.log("Siege Owner: " + siegeOwner.getName());
        sender.sendMessage("Leader Town: " + leaderTown.getName());
        sender.sendMessage("Siege Owner: " + siegeOwner.getName());
        //DEBUG PRINT
        for (String t : war.getSide1Towns()) {
            Main.warLogger.log("Side1 town: " + t);
            sender.sendMessage("Side1 town: " + t);
        }
        for (String t : war.getSide2Towns()) {
            Main.warLogger.log("Side2 town: " + t);
            sender.sendMessage("Side2 town: " + t);
        }

        if (side == 0) {
            siegeOwner.sendMessage(Helper.chatLabel() + "You are not in this war.");
            if(admin) sender.sendMessage(Helper.chatLabel() + "You are not in this war.");
            return;
        } else if (side == -1) {
            siegeOwner.sendMessage(Helper.chatLabel() + "You have surrendered.");
            if(admin) sender.sendMessage(Helper.chatLabel() + "You have surrendered.");
            return;
        }

        //Minutemen countermeasures, 86400 * 4 time. 86400 seconds in a day, 4 days min playtime
        //Minuteman countermeasures
        int minuteman = CommandHelper.isPlayerMinuteman(siegeOwner.getName());
        boolean override = false;
        if (admin) {
            if (args.length >= 6) {
                if (Boolean.parseBoolean(args[5])) {
                    //player has joined to recently
                    sender.sendMessage(ChatColor.RED + "Warning! Ignoring Minuteman Countermeasure!");
                    override = true;
                }
            }
        }
        //override?
        if(!override) {
            if (minuteman != 0) {
                if (minuteman == 1) {
                    //player has joined to recently
                    if (admin) sender.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayerAge") + " days from joining.");
                    siegeOwner.sendMessage(ChatColor.RED + "You have joined the server too recently! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayerAge") + " days from joining.");
                    return;
                } else if (minuteman == 2) {
                    //player has played too little
                    if (admin) sender.sendMessage(ChatColor.RED + "You have not played enough! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayTime") + " hours of play.");
                    siegeOwner.sendMessage(ChatColor.RED + "You have not played enough! You can only join a raid after " + Main.getInstance().getConfig().getInt("minimumPlayTime") + " hours of play.");
                    return;
                }
            }
        }

        // Town check
        Town town = TownyAPI.getInstance().getTown(args[2]);
        if (town == null) {
            siegeOwner.sendMessage(String.valueOf(Helper.chatLabel()) + "That town does not exist! /siege start [war] [town]");
            if(admin) sender.sendMessage(String.valueOf(Helper.chatLabel()) + "That town does not exist! /siege start [war] [town]");
            return;
        }
        Main.warLogger.log("Attacked Town: " + town.getName().toLowerCase());
        sender.sendMessage("Attacked Town: " + town.getName().toLowerCase());

        // Is being raided check
        for ( Raid r : RaidData.getRaids()) {
            if(r.getRaidedTown().getName().equalsIgnoreCase(town.getName())) {
                siegeOwner.sendMessage(String.valueOf(Helper.chatLabel()) + "That town is already currently being raided! Cannot siege at this time!");
                if(admin) sender.sendMessage(String.valueOf(Helper.chatLabel()) + "That town is already currently being raided! Cannot siege at this time!");
                return;
            }
        }
        
        // Attacking own side
        if (war.getSide(town) == side) {
            siegeOwner.sendMessage(Helper.chatLabel() + "You cannot attack your own towns.");
            if(admin) sender.sendMessage(Helper.chatLabel() + "You cannot attack your own towns.");
            return;
        }

        Siege siege = new Siege(war, town, siegeOwner);
        SiegeData.addSiege(siege);
        war.addSiege(siege);

        Bukkit.broadcastMessage(Helper.chatLabel() + "A siege has been laid on " + siege.getTown() + " by " + siege.getAttackerSide() + "!");

        war.save();
        siege.start();
    }

    private static void siegeStop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("AlathraWar.admin")) {
            sender.sendMessage(String.valueOf(Helper.chatLabel()) + Helper.color("&cYou do not have permission to do this"));
            return;
        }
        HashSet<Siege> sieges = SiegeData.getSieges();
        if (sieges.isEmpty()) {
            sender.sendMessage(String.valueOf(Helper.chatLabel()) + "There are currently no sieges in progress");
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(Helper.chatLabel() + "/siege stop [town]");
            return;
        }

        // War check
        War war = WarData.getWar(args[1]);
        if (war == null) {
            sender.sendMessage(String.valueOf(Helper.chatLabel()) + "That war does not exist! /siege stop [war] [town]");
            return;
        }

        for (Siege siege : sieges) {
            if (siege.getTown().getName().equalsIgnoreCase(args[2])) {
                sender.sendMessage(String.valueOf(Helper.chatLabel()) + "siege cancelled");
                Bukkit.broadcastMessage(String.valueOf(Helper.chatLabel()) + "The siege at " + siege.getTown().getName() + " has been cancelled by an admin");
                //siege.clearBeacon();
                siege.stop();
                return;
            }
        }
        sender.sendMessage(String.valueOf(Helper.chatLabel()) + "A siege could not be found with this town name! Type /siege list to view current sieges");
    }
    
    private static void siegeAbandon(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) return;
        final Player p = (Player) sender;

        if (args.length < 3) {
            sender.sendMessage(Helper.chatLabel() + "/siege abandon [town]");
            return;
        }

        // War check
        War war = WarData.getWar(args[1]);
        if (war == null) {
            p.sendMessage(String.valueOf(Helper.chatLabel()) + "That war does not exist! /siege abandon [war] [town]");
            return;
        }

        Siege siege = SiegeData.getSiege(args[2]);
        if (siege == null) {
            p.sendMessage(Helper.chatLabel() + "Invalid siege");
            return;
        }

        OfflinePlayer oPlayer = siege.getSiegeLeader();
        if (oPlayer != (OfflinePlayer) p) {
            p.sendMessage(Helper.chatLabel() + "You are not the leader of this siege.");
            return;
        }

        Bukkit.broadcastMessage(String.valueOf(Helper.chatLabel()) + "The siege at " + siege.getTown().getName() + " has been abandoned.");
        Main.warLogger.log(p.getName() + " abandoned the siege they started at " + siege.getTown().getName());
        siege.defendersWin();
                            
    }

    private static void siegeList(CommandSender sender, String[] args) {
        HashSet<Siege> sieges = SiegeData.getSieges();
        if (sieges.isEmpty()) {
            sender.sendMessage(String.valueOf(Helper.chatLabel()) + "There are currently no sieges in progress");
            return;
        } 

        sender.sendMessage(String.valueOf(Helper.chatLabel()) + "Sieges currently in progress:");
        for (Siege siege : sieges) {
            War war = siege.getWar();
            String color = (siege.getSide1AreAttackers() && (war.getSide(siege.getTown().getName().toLowerCase()) == 1) ) ? "§c" : "§9";
            sender.sendMessage(war.getName() + " - " + color + siege.getTown().getName().toLowerCase());
            sender.sendMessage(war.getSide1() + " - " + (siege.getSide1AreAttackers() ? siege.getAttackerPoints() : siege.getDefenderPoints()));
            sender.sendMessage(war.getSide2() + " - " + (siege.getSide1AreAttackers() ? siege.getDefenderPoints() : siege.getAttackerPoints()));
            sender.sendMessage("Time Left: " + (Siege.maxSiegeTicks - siege.getSiegeTicks())/1200 + " minutes");
            sender.sendMessage("-=-=-=-=-=-=-=-=-=-=-=-");
        }
        
    }

    private static void siegeHelp(CommandSender sender, String[] args) {
        if (sender.hasPermission("AlathraWar.admin")) {
            sender.sendMessage(Helper.chatLabel() + "/siege stop [war] [town]");
        }
        sender.sendMessage(Helper.chatLabel() + "/siege start [war] [town]");
        sender.sendMessage(Helper.chatLabel() + "/siege abandon [war] [town]");
        sender.sendMessage(Helper.chatLabel() + "/siege list");
    }
}
