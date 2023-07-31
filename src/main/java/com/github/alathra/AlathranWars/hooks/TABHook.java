package com.github.alathra.AlathranWars.hooks;

import com.github.alathra.AlathranWars.conflict.War;
import com.github.alathra.AlathranWars.listeners.war.PlayerJoinListener;
import com.github.alathra.AlathranWars.utility.UtilsChat;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.EventBus;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;
import me.neznamy.tab.api.nametag.UnlimitedNameTagManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TABHook {
    private static boolean enabled = false;
    private static TabAPI tabAPI;

    public static void init() {
        if (enabled) return;

        try {
            tabAPI = TabAPI.getInstance();
            enabled = true;

            registerPlayerLoadListener();
        } catch (NoClassDefFoundError ignored) {
        }
    }

    private static void registerPlayerLoadListener() {
        if (!enabled) return;

        @Nullable EventBus eventBus = tabAPI.getEventBus();
        if (eventBus == null) return;

        eventBus.register(PlayerLoadEvent.class, e -> {
            @NotNull TabPlayer tabPlayer = e.getPlayer();
            if (tabPlayer.getPlayer() instanceof Player p)
                PlayerJoinListener.checkPlayer(p);
        });
    }

    public static void assignSide1WarSuffix(@NotNull Player p, @NotNull War war) {
        if (!enabled) return;

        @Nullable TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());

        if (tabAPI.getTabListFormatManager() == null) return;

        tabAPI.getTabListFormatManager().setSuffix(tabPlayer,
            UtilsChat.color(" &c[" + war.getSide1().getName() + "]&r")
        );

        if (TabAPI.getInstance().getNameTagManager() instanceof UnlimitedNameTagManager unm) {
            unm.setPrefix(tabPlayer, "&c");
        }
    }

    public static void assignSide2WarSuffix(@NotNull Player p, @NotNull War war) {
        if (!enabled) return;

        @Nullable TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());

        if (tabAPI.getTabListFormatManager() == null) return;

        tabAPI.getTabListFormatManager().setSuffix(tabPlayer, UtilsChat.color(" &9[") + war.getSide2().getName() + "]&r");

        if (TabAPI.getInstance().getNameTagManager() instanceof UnlimitedNameTagManager unm) {
            unm.setPrefix(tabPlayer, "&9");
        }
    }

    public static void assignSide1WarSuffixMerc(@NotNull Player p, @NotNull War war) {
        if (!enabled) return;

        @Nullable TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());

        if (tabAPI.getTabListFormatManager() == null) return;

        tabAPI.getTabListFormatManager().setSuffix(tabPlayer,
            UtilsChat.color(" &a[M]&c[") + war.getSide1() + "]&r");
    }

    public static void assignSide2WarSuffixMerc(@NotNull Player p, @NotNull War war) {
        if (!enabled) return;

        @Nullable TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());

        if (tabAPI.getTabListFormatManager() == null) return;

        tabAPI.getTabListFormatManager().setSuffix(tabPlayer,
            UtilsChat.color(" &a[M]&9[") + war.getSide2() + "]&r");
    }

    public static void resetSuffix(@NotNull Player p) {
        if (!enabled) return;

        @Nullable TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());

        if (tabAPI.getTabListFormatManager() == null) return;

        tabAPI.getTabListFormatManager().setSuffix(tabPlayer, null);
    }

    public static void resetSuffix(@NotNull String username) {
        if (!enabled) return;

        @NotNull
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);

        try {
            if (offlinePlayer.isOnline()) {
                @Nullable TabPlayer tabPlayer = tabAPI.getPlayer(username);

                if (tabAPI.getTabListFormatManager() == null) return;

                tabAPI.getTabListFormatManager().setSuffix(tabPlayer, null);
            }
        } catch (NullPointerException ignored) {
        }
    }

    public static void resetPrefix(@NotNull Player p) {
        if (!enabled) return;

        @Nullable TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());

        if (tabAPI.getTabListFormatManager() == null) return;

        tabAPI.getTabListFormatManager().setSuffix(tabPlayer, null);

        if (TabAPI.getInstance().getNameTagManager() instanceof UnlimitedNameTagManager unm) {
            unm.setPrefix(tabPlayer, null);
        }
    }

    public static void resetPrefix(@NotNull String playername) {
        if (!enabled) return;

        @NotNull
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playername);
        try {
            if (offlinePlayer.isOnline()) {
                @Nullable TabPlayer tabPlayer = tabAPI.getPlayer(playername);

                if (tabAPI.getTabListFormatManager() == null) return;

                tabAPI.getTabListFormatManager().setSuffix(tabPlayer, null);
            }
        } catch (NullPointerException ignored) {
        }
    }

    public static void removeColorPrefix(@NotNull Player p, @NotNull String prefix) {
        if (!enabled) return;

        @Nullable TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());

        if (prefix.length() > 2) {
            prefix = prefix.substring(0, prefix.length() - 2);
        } else {
            Bukkit.getLogger().info("There is an error when removing a color prefix, prefix not long enough:");
            Bukkit.getLogger().info("Player: " + p.displayName());
            Bukkit.getLogger().info("Prefix: " + prefix);
            Bukkit.getLogger().info("------");
        }


        if (tabAPI.getTabListFormatManager() == null) return;

        tabAPI.getTabListFormatManager().setPrefix(tabPlayer, UtilsChat.color(prefix));
    }

}