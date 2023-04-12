package me.ShermansWorld.AlathraWar.commands;

import com.palmergames.bukkit.towny.object.Town;
import me.ShermansWorld.AlathraWar.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class AdminCommands implements CommandExecutor {

    static {

    }

    public AdminCommands(final Main plugin) {
        plugin.getCommand("alathrawaradmin").setExecutor((CommandExecutor)this);
        plugin.getCommand("awa").setExecutor((CommandExecutor)this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }
}
