package me.ShermansWorld.AlathraWar.commands;

import com.palmergames.bukkit.towny.object.Town;
import me.ShermansWorld.AlathraWar.Helper;
import me.ShermansWorld.AlathraWar.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class AdminCommands implements CommandExecutor {

    static {

    }

    public AdminCommands(final Main plugin) {
        plugin.getCommand("alathrawaradmin").setExecutor((CommandExecutor)this);
        plugin.getCommand("awa").setExecutor((CommandExecutor)this);
    }

    /**
     * -War points edit
     * -siege score edit
     * -raid score edit
     *
     * -raid clear looted townblocks
     * -raid clear specific townblock looted status
     * -raid force next phase
     *
     * -force surrender of siege/raid/war
     * -force victory of siege/raid/war
     * -remove player from raid party
     * -force homeblock change in siege/raid
     * -cancel war (no victor)
     * -cancel siege (no victor)
     * -cancel raid (no victor)
     * -create siege
     * -create war
     *
     * -force join war
     * -force join siege
     * -forge join raid
     * -force leave war
     * -force leave siege
     * -force leave raid (perma exclude)
     *
     * -exclude player from war (banned from joining war)
     * -exclude town/nation from war
     * -exclude player from raid during war
     *
     * @param sender Source of the command
     * @param command Command which was executed
     * @param label Alias of the command which was used
     * @param args Passed command arguments
     * @return
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final Player p = (Player) sender;
        if (p.hasPermission("!AlathraWar.admin")) {
            fail(p, args, "permissions");
            return false;
        }

        if (args.length == 0) {
            fail(p, args, "syntax");
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                help(p, args);
            }
        }
        return false;
    }

    private static void help(Player p, String[] args) {
        p.sendMessage(Helper.Chatlabel() + "/alathrawaradmin start [war] [town]");
    }

    private static void fail(Player p, String[] args, String type) {
        switch (type) {
            case "permissions": {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + Helper.color("&cYou do not have permission to do this"));
                return;
            }
            case "syntax": {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /raid help");
                return;
            }
            default: {
                p.sendMessage(String.valueOf(Helper.Chatlabel()) + "Invalid Arguments. /raid help");
            }
        }
    }
}
