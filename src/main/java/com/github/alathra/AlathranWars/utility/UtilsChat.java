package com.github.alathra.AlathranWars.utility;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public abstract class UtilsChat {
    public static @NotNull String getPrefix() {
        return "<dark_gray>[<gradient:#D72A09:#B01F03>War<dark_gray>]<reset> ";
    }

    public static @NotNull String color(final @NotNull String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
}
