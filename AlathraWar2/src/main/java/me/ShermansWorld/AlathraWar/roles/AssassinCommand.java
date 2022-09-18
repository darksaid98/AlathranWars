package me.ShermansWorld.AlathraWar.roles;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ShermansWorld.AlathraWar.Main;
import me.ShermansWorld.AlathraWar.UUIDFetcher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AssassinCommand implements CommandExecutor {

    public static Map<String, Object> ActiveAssassinRequests = new HashMap<String, Object>();

    public AssassinCommand(final Main plugin) {
        plugin.getCommand("assassin").setExecutor((CommandExecutor) this);
        plugin.getCommand("assassin").setTabCompleter(new CustomRoleTabCompletion());
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Player p = (Player) sender;

        //LOAD PLAYER DATA
        UUID pID = p.getUniqueId();
        Map pData = (Map) Main.rolesData.getData(pID);
        //Bukkit.getwarLogger().info(Arrays.asList(pData).toString());

        if(!p.hasPermission("AlathraWar.UseRoles")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return false;
        }
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            p.sendMessage(ChatColor.RED + "! "+ ChatColor.RESET + ChatColor.GOLD + "---- Assassin Help ----");
            p.sendMessage(ChatColor.RED + "! " + ChatColor.GREEN + "/assassin hire <player> <price>");
            if ((Boolean) pData.get("AssassinPermission")) {
                p.sendMessage(ChatColor.RED + "! " + ChatColor.GREEN + "/assassin accept <player>");
                p.sendMessage(ChatColor.RED + "! " + ChatColor.GREEN + "/assassin decline <player>");
            }
            if (p.hasPermission("AlathraWar.Admin")) {
                p.sendMessage(ChatColor.RED + "! " + ChatColor.GREEN + "/assassin add <player>");
                p.sendMessage(ChatColor.RED + "! " + ChatColor.GREEN + "/assassin remove <player>");
            }
            return false;
        }
        else if(args[0].equalsIgnoreCase("hire")) {
            // Valid number of arguments
            if (args.length < 3) {
                p.sendMessage(ChatColor.RED + "/assassin hire <player> <price>");
                return false;
            }

            // Target Validator
            Player target = Bukkit.getPlayer(args[1]);   	
            if (!(target instanceof Player)) {
                p.sendMessage(ChatColor.RED + "Please enter a valid player.");
                return false;
            }
            UUID targetID = target.getUniqueId();
            
            
            // Number Validator
            double val = 0;
            try {
                val = Double.parseDouble(args[2]);
            } catch (Exception e) {
                p.sendMessage(ChatColor.RED + "Please enter a valid price.");
                return false;
            }

            // Minimum Price
            if (val < 10000) {
                p.sendMessage(ChatColor.DARK_RED + "You must comply with the minimum pricing of the server.");
                return false;
            }

            // Don't have enough money.
            double pVal = Main.econ.getBalance(p);
            if (pVal < val) {
                p.sendMessage(ChatColor.DARK_RED + "You do not have a high enough balance.");
                return false;
            }


            //
            String targetStrID = targetID.toString();
            Map<String, Object> TargetRequests = null;

            Bukkit.getLogger().info(String.valueOf(ActiveAssassinRequests.containsKey(targetStrID)));
            if (ActiveAssassinRequests.containsKey(targetStrID)) {
                TargetRequests = (Map) ActiveAssassinRequests.get(targetStrID);
            } else {
                TargetRequests = new HashMap<String, Object>();
            }

            if (TargetRequests.containsKey(pID.toString())) {
                p.sendMessage(ChatColor.RED + "You already have a pending contract.");
                return false;
            }

            Map targetData = (Map) Main.rolesData.getData(targetID);

            if (targetData.containsKey(pID.toString())) {
                p.sendMessage(ChatColor.RED + "You already have an assassin contract with this user.");
                return false;
            }
            if (!(Boolean) targetData.get("MercPermission")) {
                p.sendMessage(ChatColor.RED + "This user is not an assassin.");
                return false;
            }

            // Adding request to pending contracts
            Map contractData = new HashMap<String, String>();
            contractData.put("Value", val);
            TargetRequests.put(pID.toString(), contractData);
            ActiveAssassinRequests.put(targetStrID, TargetRequests);

            // Messaging and logging
            p.sendMessage(ChatColor.GREEN + "You have requested an assassin contract with " + target.getDisplayName() + " for " + ChatColor.YELLOW + Main.econ.format(val) + ".");
            target.sendMessage(target.getDisplayName() + ChatColor.GREEN + " has requested an assassin contract worth " + ChatColor.YELLOW + Main.econ.format(val) + ".");
            Main.warLogger.log("User " + p.getName() + " requested assassin work from " + p.getName() + ", value " + val);
        }
        else if (args[0].equalsIgnoreCase("accept")) {
            // Valid number of arguments
            if (args.length < 2) {
                p.sendMessage(ChatColor.RED + "/assassin accept <player>");
                return false;
            }
            
            
            // Checks and defining target
            Player target = Bukkit.getPlayer(args[1]);
            if (!(target instanceof Player)) {
                p.sendMessage(ChatColor.RED + "Please enter a valid player.");
                return false;
            }

            Map AssassinRequests = (Map) ActiveAssassinRequests.get(pID.toString());
            if (AssassinRequests == null) {
                p.sendMessage(ChatColor.RED + "You do not have any pending contracts.");
                return false;
            }

            if(AssassinRequests.isEmpty() || !AssassinRequests.containsKey(target.getUniqueId().toString())) {
                p.sendMessage(ChatColor.RED + "This player has not made any requests.");
                return false;
            }
            Map Request = (Map) AssassinRequests.get(target.getUniqueId().toString());


            if (!(Boolean) pData.get("AssassinPermission")) {
                p.sendMessage("You don't have assassin perms?");
                return false;
            }

            Map Contracts = (Map) pData.get("Contracts");
            if (Contracts.containsKey(target.getUniqueId().toString())) {
                p.sendMessage(ChatColor.RED + "You already have a contract with this person.");
                return false;
            }

            // Adds contract to userdata
            Map ContractData = new HashMap<String, Object>();
            ContractData.put("Type", "Assassin");
            ContractData.put("Value", Request.get("Value"));
            Contracts.put(target.getUniqueId().toString(), ContractData);

            Main.rolesData.editData(pID, "Contracts", Contracts);

            // Removes from ActiveMercRequests list
            AssassinRequests.remove(target.getUniqueId().toString());
            ActiveAssassinRequests.put(pID.toString(), AssassinRequests);

            // Money Changes
            Double Value = (Double) Request.get("Value");
            Main.econ.withdrawPlayer(target, Value);
            Main.econ.depositPlayer(p, Value);


            // Messaging and logging
            p.sendMessage(ChatColor.GREEN + "You have " + ChatColor.YELLOW + "accepted " + target.getDisplayName() + ChatColor.GREEN + "'s request.");
            target.sendMessage(ChatColor.GREEN + "Your request to " + p.getDisplayName() + ChatColor.GREEN + " has been "+ ChatColor.YELLOW + "accepted" + ChatColor.YELLOW + ".");
            Main.warLogger.log("User " + p.getName() + " accepted assassin work for " + target.getName());
            return true;
        }
        else if (args[0].equalsIgnoreCase("decline") || args[0].equalsIgnoreCase("deny")) {
            // Valid number of arguments
            if (args.length < 2) {
                p.sendMessage(ChatColor.RED + "/assassin decline <player>");
                return false;
            }

            //Checks and defining target
            Player target = Bukkit.getPlayer(args[1]);
            UUID targetID;
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

            Map AssassinRequests = (Map) ActiveAssassinRequests.get(pID.toString());
            if (AssassinRequests == null) {
                p.sendMessage(ChatColor.RED + "You do not have any pending assassin contracts.");
                return false;
            }

            if(AssassinRequests.isEmpty() || !AssassinRequests.containsKey(targetID.toString())) {
                p.sendMessage(ChatColor.RED + "This player has not made any requests.");
                return false;
            }

            // Removes from ActiveAssassinRequests list
            AssassinRequests.remove(targetID.toString());
            ActiveAssassinRequests.put(pID.toString(), AssassinRequests);

            // Messaging and logging
            p.sendMessage(ChatColor.GREEN + "You have " + ChatColor.RED + "denied " + msgName + ChatColor.GREEN + "'s request.");
            if(target != null) {
            	target.sendMessage(ChatColor.GREEN + "Your request to " + p.getDisplayName() + ChatColor.GREEN + " has been "+ ChatColor.RED + "denied" + ChatColor.GREEN + ".");
                Main.warLogger.log("User " + p.getName() + " declined assassin work for " + target.getName());
            }
            else {
            	Main.warLogger.log("User " + p.getName() + " declined assassin work for " + msgName);
            }
           
            return true;
        }
        else if (args[0].equalsIgnoreCase("add")) {
            // Valid number of arguments
            if (args.length < 2) {
                p.sendMessage(ChatColor.RED + "/assassin add <player>");
                return false;
            }

            Player target = Bukkit.getPlayer(args[1]);
            UUID targetID;
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
            
            // Gets data and re-writes
            Map targetData = (Map) Main.rolesData.getData(targetID);
            Boolean AssassinPerm = (Boolean) targetData.get("AssassinPermission");
            if (AssassinPerm) {
                p.sendMessage(ChatColor.RED + "This user already has assassin permissions!");
                return false;
            }
            Main.rolesData.editData(targetID, "AssassinPermission", true);

            // Messaging and logging
            p.sendMessage(ChatColor.GREEN + "User " + msgName + ChatColor.YELLOW + " may " + ChatColor.GREEN + "accept assassin work.");
            if(target != null) {
            	target.sendMessage(ChatColor.GREEN + "You " + ChatColor.YELLOW + " may now " + ChatColor.GREEN + "accept assassin work.");
            	Main.warLogger.log("User " + p.getName() + " gave " + target.getName() + " assassin permissions");
            }
            else {
            	Main.warLogger.log("User " + p.getName() + " gave " + msgName + " assassin permissions");
            }
        }
        else if (args[0].equalsIgnoreCase("remove")) {
            // Valid number of arguments
            if (args.length < 2) {
                p.sendMessage(ChatColor.RED + "/assassin remove <player>");
                return false;
            }

            
            Player target = Bukkit.getPlayer(args[1]);
            UUID targetID;
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

            // Gets data and re-writes
            Map targetData = (Map) Main.rolesData.getData(targetID);
            Boolean AssassinPerm = (Boolean) targetData.get("AssassinPermission");
            if (!AssassinPerm) {
                p.sendMessage(ChatColor.RED + "This user has no assassin permissions!");
                return false;
            }
            Main.rolesData.editData(targetID, "AssassinPermission", false);

            // Messaging and logging
            p.sendMessage(ChatColor.GREEN + "User " + msgName + ChatColor.RED + " may not " + ChatColor.GREEN + "accept assassin work.");
            if(target != null) {
            	target.sendMessage(ChatColor.GREEN + "You " + ChatColor.RED + " may no longer " + ChatColor.GREEN + "accept assassin work.");
                Main.warLogger.log("User " + p.getName() + " removed " + target.getName() + "'s assassin permissions");
            }
            else {
            	Main.warLogger.log("User " + p.getName() + " removed " + msgName + "'s assassin permissions");
            }
            
        }
        else if (args[0].equalsIgnoreCase("complete")) {
            // Valid number of arguments
            if (args.length < 2) {
                p.sendMessage(ChatColor.RED + "/assassin complete <player>");
                return false;
            }

            Player target = Bukkit.getPlayer(args[1]);
            UUID targetID;
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

            // Removal of contract from userdata
            Map Contracts = (Map) pData.get("Contracts");
            if (!Contracts.containsKey(targetID.toString())) {
                p.sendMessage(ChatColor.RED + "You have no assassin contract with this user!");
                return false;
            }
            Contracts.remove(targetID.toString());
            Main.rolesData.editData(pID, "Contracts", Contracts);

            // Messaging and logging
            p.sendMessage(ChatColor.GREEN + "You have " + ChatColor.GOLD + "completed " + msgName + ChatColor.GREEN + "'s assassin contract.");
            if(target != null) {
            	target.sendMessage(ChatColor.GREEN + "Your assassin contract with " + p.getDisplayName() + ChatColor.GREEN + " has been "+ ChatColor.GOLD + "completed" + ChatColor.GREEN + ".");
                Main.warLogger.log("User " + p.getName() + " completed " + target.getName() + "'s assassin contract.");
            }
            else {
            	Main.warLogger.log("User " + p.getName() + " completed " + msgName + "'s assassin contract.");
            }
            
        }
        else {
            p.sendMessage(ChatColor.RED + "Unknown sub-command. Do " + ChatColor.YELLOW + "/assassin help" + ChatColor.RED + " for more info.");
        }

        return false;
    }
}
