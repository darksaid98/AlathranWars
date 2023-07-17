package com.github.alathra.AlathranWars.utility;

import org.bukkit.ChatColor;

public class UtilsChat {
    public static String getPrefix() {
        return "<dark_gray>[<gradient:#D72A09:#B01F03>War<dark_gray>]<reset> ";
    }

    public static String color(final String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
