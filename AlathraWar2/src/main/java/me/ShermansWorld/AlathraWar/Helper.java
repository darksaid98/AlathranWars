package me.ShermansWorld.AlathraWar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Helper
{
    public static String color(final String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
    
    public static String Chatlabel() {
        return color("&6[&4AlathraWar&6]&r ");
    }
    
    public static void testMsg() {
    	Bukkit.broadcastMessage("test message");
    }
}
