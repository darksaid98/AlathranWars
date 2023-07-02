package me.ShermansWorld.AlathraWar.hooks;

import com.github.milkdrinkers.colorparser.ColorParser;
import me.ShermansWorld.AlathraWar.deprecated.OldWar;
import me.ShermansWorld.AlathraWar.listeners.JoinListener;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.EventBus;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class TABHook {
    private static boolean enabled = false;
    private static TabAPI tabAPI;

    public static void init() {
        if (enabled) return;

        tabAPI = TabAPI.getInstance();
        enabled = true;

        registerPlayerLoadListener();
    }

    private static void registerPlayerLoadListener() {
        if (!enabled) return;

        EventBus eventBus = tabAPI.getEventBus();
        if (eventBus == null) return;

        eventBus.register(PlayerLoadEvent.class, e -> {
            TabPlayer tabPlayer = e.getPlayer();
            if (tabPlayer.getPlayer() instanceof Player p)
                JoinListener.checkPlayer(p);
        });
    }

    public static void assignSide1WarSuffix(Player p, OldWar oldWar) {
        if (!enabled) return;

        TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());

        if (tabAPI.getTabListFormatManager() == null) return;

        tabAPI.getTabListFormatManager().setSuffix(tabPlayer,
            new ColorParser(" &c[" + oldWar.getSide1() + "]&r").parseLegacy().build());
    }

    public static void assignSide2WarSuffix(Player p, OldWar oldWar) {
        if (!enabled) return;

        TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());

        if (tabAPI.getTabListFormatManager() == null) return;

        tabAPI.getTabListFormatManager().setSuffix(tabPlayer, Helper.color(" &9[") + oldWar.getSide2() + "]&r");
    }

    public static void assignSide1WarSuffixMerc(Player p, OldWar oldWar) {
        if (!enabled) return;

        TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());

        if (tabAPI.getTabListFormatManager() == null) return;

        tabAPI.getTabListFormatManager().setSuffix(tabPlayer,
            Helper.color(" &a[M]&c[") + oldWar.getSide1() + "]&r");
    }

    public static void assignSide2WarSuffixMerc(Player p, OldWar oldWar) {
        if (!enabled) return;

        TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());

        if (tabAPI.getTabListFormatManager() == null) return;

        tabAPI.getTabListFormatManager().setSuffix(tabPlayer,
            Helper.color(" &a[M]&9[") + oldWar.getSide2() + "]&r");
    }

    public static void resetSuffix(Player p) {
        if (!enabled) return;

        TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());

        if (tabAPI.getTabListFormatManager() == null) return;

        tabAPI.getTabListFormatManager().setSuffix(tabPlayer, null);
    }

    public static void resetSuffix(String username) {
        if (!enabled) return;

        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(username);

        try {
            if (offlinePlayer.isOnline()) {
                TabPlayer tabPlayer = tabAPI.getPlayer(username);

                if (tabAPI.getTabListFormatManager() == null) return;

                tabAPI.getTabListFormatManager().setSuffix(tabPlayer, null);
            }
        } catch (NullPointerException e) {
        }
    }

    public static void resetPrefix(Player p) {
        if (!enabled) return;

        TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());

        if (tabAPI.getTabListFormatManager() == null) return;

        tabAPI.getTabListFormatManager().setSuffix(tabPlayer, null);
    }

    public static void resetPrefix(String playername) {
        if (!enabled) return;

        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playername);
        try {
            if (offlinePlayer.isOnline()) {
                TabPlayer tabPlayer = tabAPI.getPlayer(playername);

                if (tabAPI.getTabListFormatManager() == null) return;

                tabAPI.getTabListFormatManager().setSuffix(tabPlayer, null);
            }
        } catch (NullPointerException e) {
        }
    }

    public static void removeColorPrefix(Player p, String prefix) {
        if (!enabled) return;

        TabPlayer tabPlayer = tabAPI.getPlayer(p.getUniqueId());

        if (prefix.length() > 2) {
            prefix = prefix.substring(0, prefix.length() - 2);
        } else {
            Bukkit.getLogger().info("There is an error when removing a color prefix, prefix not long enough:");
            Bukkit.getLogger().info("Player: " + p.displayName());
            Bukkit.getLogger().info("Prefix: " + prefix);
            Bukkit.getLogger().info("------");
        }


        if (tabAPI.getTabListFormatManager() == null) return;

        tabAPI.getTabListFormatManager().setPrefix(tabPlayer, Helper.color(prefix));
    }

}