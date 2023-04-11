package me.ShermansWorld.AlathraWar.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import me.ShermansWorld.AlathraWar.Raid;
import me.ShermansWorld.AlathraWar.War;
import me.ShermansWorld.AlathraWar.data.RaidData;
import me.ShermansWorld.AlathraWar.data.WarData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RaidTabCompletion implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        Player p = (Player)sender;
        /*
        if (args.length == 1) {
            if (p.hasPermission("AlathraWar.admin")) {
                completions.add("stop");
            }
            completions.add("start");
            completions.add("abandon");
            completions.add("list");
            completions.add("help");
            completions.add("join");
            completions.add("leave");
            return completions;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("start")) {
                if (!WarData.getWars().isEmpty()) {
                    for (War war : WarData.getWars()) {
                        completions.add(war.getName());
                    }
                    return completions;
                }
            } else if (args[0].equalsIgnoreCase("stop")) {
                if (!RaidData.getRaids().isEmpty()) {
                    for (Raid raid : RaidData.getRaids()) {
                        completions.add(String.valueOf(raid.getName()));
                    }
                    return completions;
                }
            } else if (args[0].equalsIgnoreCase("join")) {
                if (!WarData.getWars().isEmpty()) {
                    for (War war : WarData.getWars()) {
                        completions.add(war.getName());
                    }
                    return completions;
                }
            } else if (args[0].equalsIgnoreCase("abandon")) {
                if (!WarData.getWars().isEmpty()) {
                    for (War war : WarData.getWars()) {
                        completions.add(war.getName());
                    }
                    return completions;
                }
            } else if (args[0].equalsIgnoreCase("leave")) {
                if (!WarData.getWars().isEmpty()) {
                    for (War war : WarData.getWars()) {
                        completions.add(war.getName());
                    }
                    return completions;
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("start")) {
                if (!TownyAPI.getInstance().getTowns().isEmpty()) {
                    for (Town town : TownyAPI.getInstance().getTowns()) {
                        completions.add(town.getName());
                    }
                    return completions;
                }
            } else if (args[0].equalsIgnoreCase("join")) {
                if (!TownyAPI.getInstance().getTowns().isEmpty()) {
                    for (Town town : TownyAPI.getInstance().getTowns()) {
                        completions.add(town.getName());
                    }
                    return completions;
                }
            } else if (args[0].equalsIgnoreCase("abandon")) {
                if (!TownyAPI.getInstance().getTowns().isEmpty()) {
                    for (Town town : TownyAPI.getInstance().getTowns()) {
                        completions.add(town.getName());
                    }
                    return completions;
                }
            } else if (args[0].equalsIgnoreCase("leave")) {
                if (!TownyAPI.getInstance().getTowns().isEmpty()) {
                    for (Town town : TownyAPI.getInstance().getTowns()) {
                        completions.add(town.getName());
                    }
                    return completions;
                }
            }
        }
        */
        return Collections.emptyList();
    }
}